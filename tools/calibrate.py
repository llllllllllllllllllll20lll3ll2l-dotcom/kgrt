"""Калибровка: определение координат слотов карт и арены на вашем экране.
Запустите на Windows с подключённым телефоном: python tools/calibrate.py
"""
import sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from bot.adb_controller import ADBController
from bot.vision import Vision
import cv2
import numpy as np

def main():
    adb = ADBController()
    devices = adb.list_devices()
    if not devices:
        print("Телефон не найден. Проверьте USB-отладку.")
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
    v = Vision()
    for i in range(4):
        print(f"Слот {i}: {v.card_slot_xy(i, w, h)}")
    for pos in ["left_def", "right_def", "left_mid", "right_mid", "left_atk", "right_atk", "center"]:
        print(f"{pos}: {v.arena_xy(pos, w, h)}")
    print("\nОткройте calibration_frame.png и сверьте координаты.")
    print("Если слоты не совпадают — отредактируйте ZONES в bot/vision.py")

if __name__ == "__main__":
    main()
