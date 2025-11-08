@echo off
chcp 65001 > nul
cls

REM Cambiar al directorio donde está este script
cd /d "%~dp0"

echo ========================================
echo    CLIENTE COMPLETO DonCEyKongJr
echo ========================================
echo.
echo Este es el cliente gráfico COMPLETO con:
echo - Escenario completo (4 lianas, 3 plataformas, abismo)
echo - Donkey Kong en jaula
echo - Sprites mejorados para Jr, cocodrilos y frutas
echo - HUD visual con corazones y puntaje
echo - Pantallas de título, game over y victoria
echo - Animaciones y efectos visuales
echo.
echo Ubicación: %CD%
echo.

echo [1/3] Verificando GCC (compilador C)...
gcc --version >nul 2>nul
if errorlevel 1 (
    echo ❌ ERROR: GCC no está instalado o no está en el PATH
    echo.
    pause
    exit /b 1
)
echo ✓ GCC encontrado
echo.

echo [2/3] Compilando cliente gráfico completo (modular)...
echo ----------------------------------------
echo   Estructura modular: main.c + game.c + network.c + render.c + input.c
gcc main.c game.c network.c render.c input.c -o client_gui_completo.exe -lws2_32 -lgdi32 -lmsimg32 -mwindows

if errorlevel 1 (
    echo.
    echo ❌ ERROR: La compilación falló
    echo.
    echo   Archivos requeridos:
    echo   - main.c (principal)
    echo   - game.c game.h (lógica)
    echo   - network.c network.h (red)
    echo   - render.c render.h (gráficos)
    echo   - input.c input.h (controles)
    echo.
    pause
    exit /b 1
)
echo ✓ Cliente gráfico completo compilado correctamente
echo.

echo [3/3] Ejecutando cliente gráfico completo...
echo ========================================
echo.
echo IMPORTANTE:
echo 1. Asegúrate de que el servidor esté corriendo primero
echo 2. Usa las teclas W/A/S/D para moverte
echo 3. ESPACIO para saltar, E para agarrar liana
echo 4. ESC para salir
echo.
echo Cerrando esta ventana en 3 segundos...
timeout /t 3 /nobreak > nul

start "" client_gui_completo.exe

echo.
echo ========================================
echo Cliente gráfico completo iniciado
echo ========================================
pause
