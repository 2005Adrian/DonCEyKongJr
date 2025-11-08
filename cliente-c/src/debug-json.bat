@echo off
chcp 65001 > nul
cls

echo ========================================
echo    DEBUG JSON - DonCEyKongJr
echo ========================================
echo.
echo Este programa muestra el JSON que envía
echo el servidor para ayudar a debuggear.
echo.

cd /d "%~dp0"

echo Compilando...
gcc test_json_debug.c -o test_json_debug.exe -lws2_32

if errorlevel 1 (
    echo ❌ Error al compilar
    pause
    exit /b 1
)

echo ✓ Compilado
echo.
echo IMPORTANTE: El servidor debe estar corriendo
echo.
pause

test_json_debug.exe

pause