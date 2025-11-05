@echo off
chcp 65001 > nul

REM Cambiar al directorio donde está este script
cd /d "%~dp0"

cls
echo ========================================
echo DETECTOR DE VERSIONES - DonCEyKongJr
echo ========================================
echo.
echo Ubicación: %CD%
echo.

echo [Java]
echo ----------------------------------------
java -version 2>&1
if errorlevel 1 (
    echo ❌ Java no encontrado
) else (
    echo ✓ Java instalado
)
echo.

echo [Javac - Compilador Java]
echo ----------------------------------------
javac -version 2>&1
if errorlevel 1 (
    echo ❌ Javac no encontrado
)
echo.

echo [Gradle Wrapper]
echo ----------------------------------------
cd servidor-java
if exist gradlew.bat (
    call gradlew --version
    echo ✓ Gradle Wrapper disponible
) else (
    echo ❌ Gradle Wrapper no encontrado
)
cd ..
echo.

echo [Gradle Global (si existe)]
echo ----------------------------------------
gradle --version 2>&1
if errorlevel 1 (
    echo ⚠ Gradle global no instalado (no requerido)
)
echo.

echo [GCC - Compilador C (MinGW/TDM-GCC)]
echo ----------------------------------------
gcc --version 2>&1
if errorlevel 1 (
    echo ❌ GCC no encontrado
) else (
    echo ✓ GCC instalado
)
echo.

echo [Git]
echo ----------------------------------------
git --version 2>&1
if errorlevel 1 (
    echo ⚠ Git no encontrado (opcional)
)
echo.

echo [Sistema Operativo]
echo ----------------------------------------
systeminfo | findstr /B /C:"OS Name" /C:"OS Version"
echo.

echo ========================================
echo VERIFICACIÓN COMPLETADA
echo ========================================
echo.
echo Verifica que tengas:
echo  ✓ Java instalado
echo  ✓ GCC instalado
echo  ✓ Gradle Wrapper disponible
echo.
pause
