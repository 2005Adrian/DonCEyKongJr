@echo off
chcp 65001 > nul
cls

REM Cambiar al directorio donde está este script
cd /d "%~dp0"

echo ========================================
echo    CLIENTE DonCEyKongJr
echo ========================================
echo.
echo Ubicación: %CD%
echo.

echo [1/3] Verificando GCC (compilador C)...
gcc --version >nul 2>nul
if errorlevel 1 (
    echo ❌ ERROR: GCC no está instalado o no está en el PATH
    echo.
    echo Por favor instala MinGW-Builds desde:
    echo https://www.mingw-w64.org/downloads/
    echo.
    echo O instala TDM-GCC desde:
    echo https://jmeubank.github.io/tdm-gcc/
    echo.
    echo Luego agrega la carpeta bin al PATH del sistema
    echo.
    pause
    exit /b 1
)
echo ✓ GCC encontrado
echo.

echo [2/3] Compilando cliente...
echo ----------------------------------------
gcc cliente_prueba.c -o cliente_prueba.exe -lws2_32

if errorlevel 1 (
    echo.
    echo ❌ ERROR: La compilación falló
    echo.
    echo Verifica que:
    echo - El archivo cliente_prueba.c existe
    echo - No hay errores de sintaxis en el código
    echo.
    pause
    exit /b 1
)
echo ✓ Cliente compilado correctamente
echo.

echo [3/3] Ejecutando cliente...
echo ----------------------------------------
echo.
echo Conectando al servidor en 127.0.0.1:5000...
echo (Asegúrate de que el servidor esté corriendo)
echo.

cliente_prueba.exe

if errorlevel 1 (
    echo.
    echo ⚠ El cliente se cerró con un error
    echo.
    echo Verifica que:
    echo 1. El servidor esté corriendo (iniciar-servidor.bat)
    echo 2. El puerto 5000 esté abierto
    echo 3. No haya firewall bloqueando la conexión
    echo.
)

echo.
echo ========================================
echo Cliente cerrado
echo ========================================
pause
