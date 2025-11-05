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
echo [2] Cliente (C)
echo [3] Verificar Versiones
echo [4] Compilar Todo
echo [0] Salir
echo.
echo ========================================

set /p opcion="Ingresa tu opción (0-4): "

if "%opcion%"=="1" goto servidor
if "%opcion%"=="2" goto cliente
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

:cliente
cls
echo ========================================
echo Iniciando CLIENTE...
echo ========================================
echo.
cd cliente-c\src
if exist iniciar-cliente.bat (
    call iniciar-cliente.bat
) else (
    echo ❌ ERROR: No se encuentra iniciar-cliente.bat
    pause
)
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

echo [2/2] Compilando CLIENTE (C)...
echo ----------------------------------------
cd cliente-c\src
gcc cliente_prueba.c -o cliente_prueba.exe -lws2_32

if errorlevel 1 (
    echo ❌ ERROR al compilar el cliente
    pause
    goto fin
)
echo ✓ Cliente compilado
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
