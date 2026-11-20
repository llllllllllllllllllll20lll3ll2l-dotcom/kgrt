@echo off
chcp 65001 >nul
title KGRT Bot - автоматическая установка
echo ==================================================
echo   Clash Royale Bot KGRT - автоматическая установка
echo ==================================================
echo.

:: Папка установки ADB (без прав администратора)
set "ADB_DIR=%USERPROFILE%\platform-tools"
set "ADB_ZIP=%USERPROFILE%\platform-tools.zip"

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
if exist "%ADB_DIR%\adb.exe" (
    echo ADB уже установлен: %ADB_DIR%\adb.exe
) else if exist "C:\platform-tools\adb.exe" (
    echo ADB найден: C:\platform-tools\adb.exe
    set "ADB_DIR=C:\platform-tools"
) else (
    echo Скачивание Platform Tools в %USERPROFILE%...
    powershell -ExecutionPolicy Bypass -Command "Invoke-WebRequest 'https://dl.google.com/android/repository/platform-tools-latest-windows.zip' -OutFile '%ADB_ZIP%'; Expand-Archive '%ADB_ZIP%' -DestinationPath '%USERPROFILE%\' -Force; Remove-Item '%ADB_ZIP%' -Force"
    if exist "%ADB_DIR%\adb.exe" (
        echo ADB установлен: %ADB_DIR%\adb.exe
    ) else (
        echo [ОШИБКА] Не удалось скачать ADB. Проверьте интернет.
        echo Попробуйте вручную: https://developer.android.com/tools/releases/platform-tools
        pause
        exit /b 1
    )
)
:: Добавить в PATH (для будущих терминалов)
setx Path "%Path%;%ADB_DIR%" >nul 2>&1
:: Добавить в PATH для текущего сеанса
set "Path=%Path%;%ADB_DIR%"
echo.

:: 3. Проверка ADB
echo [3/5] Проверка ADB...
"%ADB_DIR%\adb.exe" version
echo.
echo Подключённые устройства:
"%ADB_DIR%\adb.exe" devices
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
