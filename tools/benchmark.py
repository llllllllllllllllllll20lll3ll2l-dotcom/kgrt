"""Замер скорости: сколько времени занимает каждый шаг на вашем телефоне.

Запуск:  python tools/benchmark.py

Показывает:
- сколько идёт захват кадра каждым способом (поток / raw / PNG);
- сколько идёт распознавание руки и весь анализ;
- сколько реально кадров в секунду получается в цикле бота.
"""
import os
import sys
import time

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from bot.adb_controller import ADBController
from bot.vision import Vision


def timeit(label, fn, n=10):
    try:
        fn()
    except Exception as e:
        print(f"{label}: ошибка {e}")
        return None
    t0 = time.perf_counter()
    ok = 0
    for _ in range(n):
        try:
            if fn() is not None:
                ok += 1
        except Exception:
            pass
    ms = (time.perf_counter() - t0) / n * 1000
    print(f"{label:28s} {ms:7.1f} мс   (успешно {ok}/{n})")
    return ms


def main():
    adb = ADBController()
    devices = adb.list_devices()
    if not devices:
        print("Телефон не найден. Проверьте USB-отладку.")
        return
    adb.connect(devices[0])
    w, h = adb.get_screen_size()
    print(f"Телефон: {devices[0]}  {w}x{h}\n")

    print("--- Захват кадра ---")
    png_ms = timeit("PNG (exec-out -p)", adb.screencap, n=5)
    raw_ms = timeit("raw (exec-out screencap)", lambda: adb.decode_raw(adb.screencap_raw() or b""), n=5)

    if adb.start_stream():
        time.sleep(0.2)
        # первый кадр может быть медленнее (определение заголовка)
        adb.stream_frame()
        stream_ms = timeit("поток (непрерывный)", adb.stream_frame, n=15)
    else:
        stream_ms = None
        print("поток: не запустился")

    print("\n--- Анализ кадра ---")
    frame = adb.screencap_frame()
    if frame is None:
        print("кадр не получен")
        adb.close()
        return
    vision = Vision()
    vision.analyze_frame(frame)
    timeit("анализ целиком", lambda: vision.analyze_frame(frame), n=10)
    timeit("только рука (4 слота)", lambda: vision.hand_slots_detail(frame), n=10)

    print("\n--- Итог ---")
    best = min(x for x in (stream_ms, raw_ms, png_ms) if x)
    t0 = time.perf_counter()
    for _ in range(10):
        vision.analyze_frame(frame)
    analysis_ms = (time.perf_counter() - t0) / 10 * 1000
    total = best + analysis_ms
    print(f"захват {best:.0f} мс + анализ {analysis_ms:.0f} мс = {total:.0f} мс на цикл")
    print(f"ожидаемо кадров в секунду: {1000/total:.0f}")

    st = vision.analyze_frame(frame)
    print(f"\nРука сейчас: {st.hand}")
    print(f"Эликсир: {st.elixir}")
    adb.close()


if __name__ == "__main__":
    main()
