"""Точка входа: запуск бота с интерфейсом (Windows)."""
import sys
import os

# Для Windows: добавляем путь
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from gui.main_window import run

if __name__ == "__main__":
    print("Запуск Clash Royale Bot (KGRT)...")
    print("Требуется: Python 3.10+, adb в PATH, телефон с USB-отладкой")
    run()
