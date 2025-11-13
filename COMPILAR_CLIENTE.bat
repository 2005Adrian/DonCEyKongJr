@echo off
echo ================================================
echo Compilando Cliente C - DonCEy Kong Jr
echo ================================================
echo.

cd cliente-c

echo Detectando GCC...
where gcc.exe >nul 2>&1
if errorlevel 1 (
    echo ERROR: GCC no encontrado
    echo Por favor instala MinGW o MSYS2
    pause
    exit /b 1
)

echo Detectando CMake...
where cmake.exe >nul 2>&1
if errorlevel 1 (
    echo ERROR: CMake no encontrado
    echo Por favor instala CMake
    pause
    exit /b 1
)

echo.
echo Limpiando build anterior...
if exist build rmdir /s /q build

echo.
echo Configurando proyecto con CMake...
cmake -B build -G "MinGW Makefiles"
if errorlevel 1 (
    echo ERROR: Configuracion de CMake fallo
    cd ..
    pause
    exit /b 1
)

echo.
echo Compilando...
cmake --build build
if errorlevel 1 (
    echo ERROR: Compilacion fallo
    cd ..
    pause
    exit /b 1
)

echo.
echo ================================================
echo COMPILACION EXITOSA!
echo ================================================
echo.
echo El ejecutable esta en: cliente-c\build\DonCEyKongJr-Client.exe
echo.
echo Para ejecutar:
echo 1. Asegurate de que el servidor este corriendo
echo 2. Ejecuta: cliente-c\build\DonCEyKongJr-Client.exe
echo.
cd ..
pause
