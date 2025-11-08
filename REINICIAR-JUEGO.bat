@echo off
chcp 65001 > nul
cls

echo ========================================
echo    REINICIAR JUEGO - DonCEyKongJr
echo ========================================
echo.
echo Este script:
echo 1. Recompila el servidor con los cambios
echo 2. Recompila el cliente modular
echo 3. Inicia el servidor
echo 4. Espera para que inicies el cliente
echo.
pause

cd /d "%~dp0"

echo.
echo [1/3] Recompilando SERVIDOR...
echo ========================================
cd servidor-java
call gradlew clean build -x test
if errorlevel 1 (
    echo ❌ Error al compilar el servidor
    pause
    exit /b 1
)
echo ✓ Servidor compilado
cd ..

echo.
echo [2/3] Recompilando CLIENTE...
echo ========================================
cd cliente-c\src
gcc main.c game.c network.c render.c input.c -o client_gui_completo.exe -lws2_32 -lgdi32 -lmsimg32 -mwindows
if errorlevel 1 (
    echo ❌ Error al compilar el cliente
    pause
    exit /b 1
)
echo ✓ Cliente compilado
cd ..\..

echo.
echo [3/3] Iniciando SERVIDOR...
echo ========================================
echo.
start "DonCEyKongJr - Servidor" cmd /c "cd servidor-java && gradlew run && pause"

echo.
echo ✓ Servidor iniciándose en ventana separada
echo.
echo ========================================
echo Cuando veas "servidor completamente iniciado"
echo en la ventana del servidor, presiona una
echo tecla aquí para iniciar el CLIENTE
echo ========================================
pause

echo.
echo Iniciando CLIENTE...
cd cliente-c\src
start "" client_gui_completo.exe

echo.
echo ========================================
echo ✓ Juego iniciado
echo ========================================
echo.
pause