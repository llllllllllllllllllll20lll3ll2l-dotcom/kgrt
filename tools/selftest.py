"""Проверка распознавания руки и координат кликов.

Запуск:  python tools/selftest.py
Не требует телефона: если нет кадра, генерирует тестовый кадр из шаблонов карт.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import cv2
import numpy as np

from bot import screen_layout as L
from bot.adb_controller import ADBController, _FrameStream
from bot.card_recognizer import TEMPLATES_DIR
from bot.ai_brain import AIBrain, TACTICS
from bot.card_recognizer import CardRecognizer
from bot.deck import USER_DECK
from bot.vision import GameState, Vision

DECK_ELIXIR = {c.id: c.elixir for c in USER_DECK}


def make_fake_hand(card_ids):
    """Собирает синтетический кадр 720x1604 с нужными картами в руке."""
    frame = np.full((1604, 720, 3), (90, 70, 40), dtype=np.uint8)
    for i, cid in enumerate(card_ids):
        x, y = L.hand_slot_tap(i, 720, 1604)
        tpl = cv2.imread(os.path.join(TEMPLATES_DIR, f"{cid}.png"))
        if tpl is None:
            continue
        scale = 2.4
        tw, th = int(tpl.shape[1] * scale), int(tpl.shape[0] * scale)
        resized = cv2.resize(tpl, (tw, th), interpolation=cv2.INTER_LINEAR)
        x1 = max(0, x - tw // 2)
        y1 = max(0, y - int(th * 0.55))
        frame[y1:y1 + th, x1:x1 + tw] = resized[: min(th, frame.shape[0] - y1),
                                                 : min(tw, frame.shape[1] - x1)]
    return frame


def slot_for(state, decision):
    if not decision or not decision.card_id:
        return None
    for i, cid in enumerate(state.hand):
        if cid == decision.card_id:
            return i
    variants = {decision.card_id}
    if decision.card_id.endswith("_evo"):
        variants.add(decision.card_id[:-4])
    else:
        variants.add(decision.card_id + "_evo")
    for i, cid in enumerate(state.hand):
        if cid in variants:
            return i
    return None


def main():
    failures = []

    rec = CardRecognizer()
    print(f"Шаблонов загружено: {len(rec.available())}")
    if len(rec.available()) < 10:
        failures.append("мало шаблонов карт")

    # 1. Распознавание на синтетическом кадре
    hand = ["lumberjack", "giant_minion", "hero_tombstone", "witch"]
    frame = make_fake_hand(hand)
    vision = Vision()
    details = vision.hand_slots_detail(frame)
    print("\nРаспознавание тестовой руки:")
    for d in details:
        print(f"  слот {d['slot']}: {d['card_id']}  ({d['score']:.2f}/{d['margin']:.2f})")
    found = [d["card_id"] for d in details]
    if found != hand:
        failures.append(f"распознавание: ожидалось {hand}, получено {found}")

    # 2. Координаты не попадают в зону «След.»
    for i in range(4):
        x, y = vision.card_slot_xy(i, 720, 1604)
        if not (180 <= x <= 680 and 1340 <= y <= 1500):
            failures.append(f"слот {i}: точка клика вне руки ({x},{y})")
    print(f"\nКлики по слотам: {[vision.card_slot_xy(i, 720, 1604) for i in range(4)]}")

    # 3. Точки арены только на своей половине
    for pos in L.ARENA_POINTS:
        _, y = vision.arena_xy(pos, 720, 1604)
        if not (0.455 * 1604 <= y <= 0.80 * 1604):
            failures.append(f"арена {pos}: y={y} вне разрешённой зоны")
    print(f"Мост справа: {vision.arena_xy('right_bridge', 720, 1604)}")

    # 4. Решения всегда указывают на карту из руки и по эликсиру
    print("\nРешения по тактикам:")
    for elixir in (1.0, 3.0, 6.0, 10.0):
        for tactic in TACTICS:
            state = GameState(elixir=elixir, hand=list(hand), frame_ok=True)
            d = AIBrain(tactic=tactic).decide(state)
            if not d.card_id:
                continue
            slot = slot_for(state, d)
            cost = DECK_ELIXIR.get(d.card_id, DECK_ELIXIR.get(d.card_id.replace("_evo", "")))
            if slot is None:
                failures.append(f"{tactic}@{elixir}: {d.card_id} нет в руке {hand}")
            if cost is not None and cost > elixir:
                failures.append(f"{tactic}@{elixir}: {d.card_id} стоит {cost}")
        print(f"  эликсир {elixir}: ок")

    # 5. Скорость распознавания руки (важно: цикл бота не должен тормозить)
    import time
    frame = make_fake_hand(hand)
    vision.hand_slots_detail(frame)  # прогрев
    t0 = time.perf_counter()
    for _ in range(5):
        vision.hand_slots_detail(frame)
    ms = (time.perf_counter() - t0) / 5 * 1000
    print(f"\nРаспознавание руки: {ms:.0f} мс на кадр")
    if ms > 250:
        failures.append(f"распознавание слишком медленное: {ms:.0f} мс")

    # 6. Разбор сырого кадра screencap и потока кадров (без телефона)
    import io
    import struct
    W, H = 720, 1604
    px = np.zeros((H, W, 4), dtype=np.uint8)
    px[..., 0] = 200
    for header in (12, 16):
        raw = struct.pack("<III", W, H, 1) + (b"\0" * (header - 12)) + px.tobytes()
        decoded = ADBController.decode_raw(raw)
        if decoded is None or decoded.shape != (H, W, 3):
            failures.append(f"decode_raw с заголовком {header} байт не сработал")
        else:
            print(f"raw-кадр ({header} байт заголовка): {decoded.shape} ок")

    def make_stream(n, header):
        buf = bytearray()
        for i in range(n):
            p = px.copy()
            p[..., 0] = i
            buf += struct.pack("<III", W, H, 1)
            if header == 16:
                buf += struct.pack("<I", 1)
            buf += p.tobytes()
        return bytes(buf)

    class _FakeProc:
        def __init__(self, data):
            self.stdout = io.BytesIO(data)

    for header in (12, 16):
        stream = _FrameStream(_FakeProc(make_stream(3, header)))
        got = [stream.read_frame() for _ in range(3)]
        if any(f is None for f in got):
            failures.append(f"поток кадров (заголовок {header}) читается не полностью")
        else:
            print(f"поток кадров ({header} байт заголовка): 3/3 ок")

    print()
    if failures:
        print("ОШИБКИ:")
        for f in failures:
            print("  -", f)
        sys.exit(1)
    print("Все проверки пройдены.")


if __name__ == "__main__":
    main()
