"""Калибровка: проверка координат руки, арены и распознавания карт.

Запустите на Windows с подключённым телефоном:
    python tools/calibrate.py

Скрипт:
1. Делает кадр экрана и сохраняет его (calibration_frame.png).
2. Показывает, какие карты бот видит в руке и с какой уверенностью.
3. Рисует точки, куда бот будет кликать (слоты руки и арена),
   и сохраняет размеченный кадр (calibration_marked.png).
4. Делает пробный клик по слоту (если запустить с флагом --tap).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import cv2
import numpy as np

from bot.adb_controller import ADBController
from bot.vision import Vision


def main():
    do_tap = "--tap" in sys.argv

    adb = ADBController()
    devices = adb.list_devices()
    if not devices:
        print("Телефон не найден. Проверьте USB-отладку и `adb devices`.")
        return
    adb.connect(devices[0])
    w, h = adb.get_screen_size()
    print(f"Экран: {w}x{h}")

    png = adb.screencap()
    if not png:
        print("Не удалось получить кадр.")
        return

    arr = np.frombuffer(png, dtype=np.uint8)
    img = cv2.imdecode(arr, cv2.IMREAD_COLOR)
    cv2.imwrite("calibration_frame.png", img)
    print("Кадр сохранён: calibration_frame.png")

    vision = Vision()
    print(f"\nШаблонов карт загружено: {len(vision.recognizer.available())}")
    missing = [c for c in vision.recognizer.available()]
    print("  " + ", ".join(missing) if missing else "  нет")

    print("\n=== РУКА ===")
    slots = vision.hand_slots_detail(img)
    for s in slots:
        card = s["card_id"] or "не распознано"
        print(f"Слот {s['slot']}: {card:16s} уверенность {s['score']:.2f} "
              f"отрыв {s['margin']:.2f}  клик {s['tap']}")

    print("\n=== АРЕНА (своя половина) ===")
    for pos in ("left_bridge", "right_bridge", "left_mid", "right_mid",
                "left_def", "right_def", "left_back", "right_back",
                "center", "king"):
        print(f"  {pos:14s} {vision.arena_xy(pos, w, h)}")

    marked = vision.recognizer.annotate(img, slots)
    for pos in ("left_bridge", "right_bridge", "left_mid", "right_mid", "center"):
        x, y = vision.arena_xy(pos, w, h)
        cv2.circle(marked, (x, y), 8, (0, 0, 255), 2)
        cv2.putText(marked, pos, (x + 10, y), cv2.FONT_HERSHEY_SIMPLEX,
                    0.4, (0, 0, 255), 1, cv2.LINE_AA)
    cv2.imwrite("calibration_marked.png", marked)
    print("\nРазмеченный кадр: calibration_marked.png")
    print("Откройте его: зелёные рамки = карты распознаны, красные = нет.")

    if do_tap:
        print("\nПробный клик по слоту 0...")
        x, y = vision.card_slot_xy(0, w, h)
        adb.tap(x, y)
        print(f"Клик отправлен: ({x},{y}). Карта должна подсветиться в игре.")


if __name__ == "__main__":
    main()
