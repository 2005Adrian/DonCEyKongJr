@echo off
chcp 65001 > nul
cls

REM Cambiar al directorio donde está este script
cd /d "%~dp0"

echo ========================================
echo    SERVIDOR DonCEyKongJr
echo ========================================
echo.
echo Ubicación: %CD%
echo.

echo [1/3] Verificando Java...
java -version 2>nul
if errorlevel 1 (
    echo ❌ ERROR: Java no está instalado o no está en el PATH
    echo.
    echo Por favor instala Java JDK 21 o superior desde:
    echo https://adoptium.net/
    echo.
    pause
    exit /b 1
)
echo ✓ Java encontrado
echo.

echo [2/3] Verificando Gradle...
where gradle >nul 2>nul
if errorlevel 1 (
    echo ⚠ Gradle no encontrado globalmente, usando Gradle Wrapper...
    if exist gradlew.bat (
        echo ✓ Gradle Wrapper encontrado
    ) else (
        echo ❌ ERROR: No se encuentra gradlew.bat
        echo Por favor ejecuta este script desde la carpeta servidor-java
        pause
        exit /b 1
    )
) else (
    echo ✓ Gradle encontrado
)
echo.

echo [3/3] Compilando y ejecutando el servidor...
echo ----------------------------------------
echo.

if exist gradlew.bat (
    call gradlew.bat run
) else (
    gradle run
)

if errorlevel 1 (
    echo.
    echo ❌ ERROR: El servidor falló al iniciar
    echo.
    echo Posibles causas:
    echo - El puerto 5000 ya está en uso
    echo - Error de compilación
    echo.
    echo Intenta:
    echo 1. Cerrar otros servidores corriendo
    echo 2. Ejecutar: gradlew clean build
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Servidor cerrado
echo ========================================
pause
