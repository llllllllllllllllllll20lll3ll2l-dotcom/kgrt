# 🪟 Установка на Windows (пошагово)

## 1. Python
1. Скачайте Python 3.10+ с https://python.org/downloads/
2. При установке поставьте галочку **Add python.exe to PATH**
3. Проверьте: `python --version`

## 2. Android Platform Tools (ADB)
1. Скачайте с https://developer.android.com/tools/releases/platform-tools
2. Распакуйте, например, в `C:\platform-tools`
3. Добавьте в PATH: Win+R → `sysdm.cpl` → Дополнительно → Переменные среды → Path → Добавить `C:\platform-tools`
4. Проверьте: `adb version`

## 3. Зависимости бота
```bat
pip install -r requirements.txt
```
Если `pytesseract` не работает — установите Tesseract OCR с https://github.com/UB-Mannheim/tesseract/wiki

## 4. Телефон
1. Настройки → О телефоне → 7 раз тап по номеру сборки (включится режим разработчика)
2. Настройки → Система → Для разработчиков → Отладка по USB (вкл)
3. Подключите USB, на телефоне нажмите «Разрешить отладку по USB»

## 5. Запуск
```bat
python run_bot.py
```
1. Вкладка «Бот» → «Подключить ADB» (должен найти телефон)
2. Откройте Clash Royale на телефоне
3. «Запустить бота» — бот будет играть сам
4. Тактику меняйте радиокнопками прямо во время боя

## 6. Калибровка (если клики мимо)
```bat
python tools/calibrate.py
```
Откроется `calibration_frame.png`. Если слоты карт не совпадают — отредактируйте `ZONES` в `bot/vision.py`.

## 7. scrcpy (опционально, для зеркалирования)
Скачайте с https://github.com/Genymobile/scrcpy/releases
Во вкладке «Бот» → «Открыть scrcpy».

## ⚠️ Предупреждение
Автоматизация может нарушать правила Supercell. Используйте на свой риск. Рекомендуется тестировать на твике.
