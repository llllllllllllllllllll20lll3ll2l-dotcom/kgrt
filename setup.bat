@echo off
chcp 65001 >nul
title KGRT Bot - автоматическая установка
echo ==================================================
echo   Clash Royale Bot KGRT - автоматическая установка
echo ==================================================
echo.

:: 1. Проверка Python
echo [1/5] Проверка Python...
python --version >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Python не найден!
    echo Скачайте с https://python.org/downloads/ и поставьте галочку Add to PATH
    pause
    exit /b 1
)
python --version
echo.

:: 2. Установка ADB (Platform Tools)
echo [2/5] Проверка ADB...
if exist "C:\platform-tools\adb.exe" (
    echo ADB уже установлен: C:\platform-tools\adb.exe
) else (
    echo Скачивание Platform Tools...
    powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest 'https://dl.google.com/android/repository/platform-tools-latest-windows.zip' -OutFile 'C:\platform-tools.zip'; Expand-Archive 'C:\platform-tools.zip' -DestinationPath 'C:\' -Force; Remove-Item 'C:\platform-tools.zip' -Force"
    if exist "C:\platform-tools\adb.exe" (
        echo ADB установлен!
    ) else (
        echo [ОШИБКА] Не удалось скачать ADB. Проверьте интернет.
        pause
        exit /b 1
    )
)
:: Добавить в PATH (для будущих терминалов)
setx Path "%Path%;C:\platform-tools" >nul 2>&1
:: Добавить в PATH для текущего сеанса
set "Path=%Path%;C:\platform-tools"
echo.

:: 3. Проверка ADB
echo [3/5] Проверка ADB...
adb version
echo.
echo Подключённые устройства:
adb devices
echo Если список пустой - подключите телефон и включите Отладку по USB!
echo.

:: 4. Установка зависимостей
echo [4/5] Установка зависимостей Python...
pip install -r requirements.txt
echo.

:: 5. Запуск бота
echo [5/5] Запуск бота...
echo ==================================================
echo   Подключите телефон (USB-отладка) и откройте Clash Royale
echo   Затем во вкладке Бот нажмите: Подключить ADB - Запустить бота
echo ==================================================
echo.
python run_bot.py
pause
