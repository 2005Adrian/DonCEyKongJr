@echo off
chcp 65001 > nul

REM Guardar directorio original y cambiar al directorio del script
set "ORIGINAL_DIR=%CD%"
cd /d "%~dp0"

:inicio
cls
echo ========================================
echo    DonCEyKongJr - Launcher
echo ========================================
echo.
echo Ubicación: %CD%
echo.
echo Selecciona qué deseas ejecutar:
echo.
echo [1] Servidor (Java)
echo [2] Cliente COMPLETO (Juego Gráfico)
echo [3] Verificar Versiones
echo [4] Compilar Todo
echo [0] Salir
echo.
echo ========================================

set /p opcion="Ingresa tu opción (0-4): "

if "%opcion%"=="1" goto servidor
if "%opcion%"=="2" goto cliente_completo
if "%opcion%"=="3" goto verificar
if "%opcion%"=="4" goto compilar
if "%opcion%"=="0" goto salir

echo.
echo ❌ Opción inválida
timeout /t 2 > nul
goto inicio

:servidor
cls
echo ========================================
echo Iniciando SERVIDOR...
echo ========================================
echo.
cd servidor-java
if exist iniciar-servidor.bat (
    call iniciar-servidor.bat
) else (
    echo ❌ ERROR: No se encuentra iniciar-servidor.bat
    pause
)
goto fin

:cliente_completo
cls
echo ========================================
echo Iniciando CLIENTE COMPLETO...
echo ========================================
echo.
echo Este es el juego completo con:
echo - Escenario completo (4 lianas, plataformas, abismo)
echo - Donkey Kong en jaula
echo - Sprites mejorados y animaciones
echo - HUD visual con corazones
echo.

cd cliente-c\src

REM Verificar si existe el ejecutable
if not exist client_gui_completo.exe (
    echo [!] El ejecutable no existe. Compilando primero...
    echo.
    echo [1/2] Verificando GCC...
    gcc --version >nul 2>nul
    if errorlevel 1 (
        echo ❌ ERROR: GCC no está instalado o no está en el PATH
        echo.
        pause
        goto fin
    )
    echo ✓ GCC encontrado
    echo.

    echo [2/2] Compilando cliente gráfico completo...
    gcc main.c game.c network.c render.c input.c sprites.c -o client_gui_completo.exe -lws2_32 -lgdi32 -lmsimg32 -mwindows

    if errorlevel 1 (
        echo ❌ ERROR: La compilación falló
        pause
        goto fin
    )
    echo ✓ Compilado correctamente
    echo.
)

echo Ejecutando cliente gráfico completo...
echo.
echo IMPORTANTE:
echo 1. Asegúrate de que el servidor esté corriendo primero
echo 2. Usa las teclas W/A/S/D para moverte
echo 3. ESPACIO para saltar, E para agarrar liana
echo 4. ESC para salir
echo.
echo Iniciando en 2 segundos...
timeout /t 2 /nobreak > nul

start "" client_gui_completo.exe

echo.
echo ✓ Cliente gráfico completo iniciado
echo.
timeout /t 2 > nul
goto fin

:verificar
cls
echo ========================================
echo Verificando Versiones...
echo ========================================
echo.
if exist check-versions.bat (
    call check-versions.bat
) else (
    echo [Java]
    java -version
    echo.
    echo [Gradle]
    gradle --version
    echo.
    echo [GCC]
    gcc --version
    echo.
    pause
)
goto fin

:compilar
cls
echo ========================================
echo Compilando Proyecto Completo...
echo ========================================
echo.

echo [1/2] Compilando SERVIDOR (Java)...
echo ----------------------------------------
cd servidor-java
if exist gradlew.bat (
    call gradlew.bat clean build
) else (
    gradle clean build
)

if errorlevel 1 (
    echo ❌ ERROR al compilar el servidor
    pause
    goto fin
)
echo ✓ Servidor compilado
echo.

cd ..

echo [2/2] Compilando CLIENTE COMPLETO (C)...
echo ----------------------------------------
cd cliente-c\src

echo   Compilando cliente gráfico completo (modular + sprites)...
gcc main.c game.c network.c render.c input.c sprites.c -o client_gui_completo.exe -lws2_32 -lgdi32 -lmsimg32 -mwindows
if errorlevel 1 (
    echo ❌ ERROR al compilar el cliente gráfico completo
    pause
    goto fin
)
echo   ✓ Cliente gráfico completo compilado
echo.

cd ..\..

echo ========================================
echo ✓ Compilación completada exitosamente
echo ========================================
pause
goto fin

:salir
cls
echo.
echo Saliendo...
exit /b 0

:fin
cd "%~dp0"
echo.
echo ========================================
echo Presiona cualquier tecla para volver
echo ========================================
pause > nul
goto inicio
