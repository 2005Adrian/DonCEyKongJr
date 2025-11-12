@echo off
setlocal enabledelayedexpansion

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: DonCEyKongJr - Script de Compilación y Ejecución
:: Detecta automáticamente las herramientas necesarias
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

title DonCEyKongJr - Launcher

:MAIN_MENU
cls
echo.
echo =========================================
echo        DonCEyKongJr - Launcher
echo =========================================
echo.
echo [1] Ejecutar Servidor
echo.
echo [2] Ejecutar Cliente
echo.
echo [0] Salir
echo.
echo =========================================
echo.

set /p choice="Seleccione una opcion: "

if "%choice%"=="1" goto BUILD_RUN_SERVER
if "%choice%"=="2" goto BUILD_RUN_CLIENT
if "%choice%"=="0" goto EXIT

echo Opcion invalida
timeout /t 2 >nul
goto MAIN_MENU

::::::::::::::::::::::::::::::::::::::::::
:: SERVIDOR JAVA
::::::::::::::::::::::::::::::::::::::::::

:BUILD_SERVER
cls
echo %CYAN%=========================================%RESET%
echo %GREEN%Compilando Servidor Java...%RESET%
echo %CYAN%=========================================%RESET%
echo.

cd servidor-java

:: Verificar si existe gradlew.bat
if not exist "gradlew.bat" (
    echo %RED%Error: No se encontró gradlew.bat%RESET%
    echo %YELLOW%Intentando usar gradle global...%RESET%
    gradle build
) else (
    gradlew.bat build
)

if errorlevel 1 (
    echo.
    echo %RED%Error al compilar el servidor%RESET%
    cd ..
    pause
    goto MAIN_MENU
)

echo.
echo %GREEN%✓ Servidor compilado exitosamente%RESET%
cd ..
pause
goto MAIN_MENU

:RUN_SERVER
cls
echo %CYAN%=========================================%RESET%
echo %GREEN%Ejecutando Servidor...%RESET%
echo %CYAN%=========================================%RESET%
echo.

cd servidor-java

if not exist "gradlew.bat" (
    echo %YELLOW%Usando gradle global...%RESET%
    gradle run
) else (
    gradlew.bat run
)

cd ..
echo.
echo %YELLOW%Servidor detenido%RESET%
pause
goto MAIN_MENU

:BUILD_RUN_SERVER
cls
echo =========================================
echo    Compilando y Ejecutando Servidor...
echo =========================================
echo.

:: Guardar directorio actual
pushd servidor-java

:: Compilar primero
echo Compilando servidor...
call gradlew.bat build

if errorlevel 1 (
    echo.
    echo ERROR: No se pudo compilar el servidor
    popd
    pause
    goto MAIN_MENU
)

echo Compilacion exitosa
echo.
echo Iniciando servidor...
echo =========================================
echo.

:: Ejecutar servidor
call gradlew.bat run

popd
echo.
echo Servidor detenido
pause
goto MAIN_MENU

:BUILD_SERVER_SILENT
cd servidor-java
if not exist "gradlew.bat" (
    gradle build
) else (
    gradlew.bat build
)
cd ..
exit /b %errorlevel%

::::::::::::::::::::::::::::::::::::::::::
:: CLIENTE C
::::::::::::::::::::::::::::::::::::::::::

:BUILD_CLIENT
cls
echo %CYAN%=========================================%RESET%
echo %GREEN%Compilando Cliente C...%RESET%
echo %CYAN%=========================================%RESET%
echo.

cd cliente-c

:: Detectar GCC automáticamente
call :DETECT_GCC
if "!GCC_PATH!"=="" (
    echo %RED%Error: No se pudo encontrar GCC%RESET%
    echo %YELLOW%Por favor instala MinGW o MSYS2%RESET%
    cd ..
    pause
    goto MAIN_MENU
)

echo %CYAN%Usando GCC: %RESET%%GREEN%!GCC_PATH!%RESET%
echo.

:: Detectar mingw32-make
call :DETECT_MAKE
if "!MAKE_PATH!"=="" (
    echo %RED%Error: No se pudo encontrar mingw32-make%RESET%
    cd ..
    pause
    goto MAIN_MENU
)

echo %CYAN%Usando Make: %RESET%%GREEN%!MAKE_PATH!%RESET%
echo.

:: Limpiar build anterior si existe con cache inválido
if exist "build\CMakeCache.txt" (
    echo %YELLOW%Verificando cache de CMake...%RESET%
    findstr /C:"OneDrive" build\CMakeCache.txt >nul 2>&1
    if !errorlevel! equ 0 (
        echo %YELLOW%Detectado cache antiguo, limpiando...%RESET%
        rmdir /s /q build 2>nul
    )
)

:: Configurar CMake
echo %CYAN%Configurando proyecto con CMake...%RESET%
cmake -B build -G "MinGW Makefiles" -DCMAKE_C_COMPILER="!GCC_PATH!" -DCMAKE_MAKE_PROGRAM="!MAKE_PATH!"

if errorlevel 1 (
    echo.
    echo %RED%Error en la configuración de CMake%RESET%
    cd ..
    pause
    goto MAIN_MENU
)

:: Compilar
echo.
echo %CYAN%Compilando cliente...%RESET%
cmake --build build

if errorlevel 1 (
    echo.
    echo %RED%Error al compilar el cliente%RESET%
    cd ..
    pause
    goto MAIN_MENU
)

echo.
echo %GREEN%✓ Cliente compilado exitosamente%RESET%
cd ..
pause
goto MAIN_MENU

:RUN_CLIENT
cls
echo %CYAN%=========================================%RESET%
echo %GREEN%Ejecutando Cliente...%RESET%
echo %CYAN%=========================================%RESET%
echo.

cd cliente-c

if not exist "build\DonCEyKongJr-Client.exe" (
    echo %RED%Error: El cliente no está compilado%RESET%
    echo %YELLOW%Por favor, compila primero el cliente%RESET%
    cd ..
    pause
    goto MAIN_MENU
)

start "" "build\DonCEyKongJr-Client.exe"

echo %GREEN%Cliente iniciado%RESET%
cd ..
timeout /t 2 >nul
goto MAIN_MENU

:BUILD_RUN_CLIENT
cls
echo =========================================
echo    Compilando y Ejecutando Cliente...
echo =========================================
echo.

cd cliente-c

:: Detectar GCC automáticamente
call :DETECT_GCC
if "!GCC_PATH!"=="" (
    echo ERROR: No se pudo encontrar GCC
    echo Por favor instala MinGW o MSYS2
    cd ..
    pause
    goto MAIN_MENU
)

echo Usando GCC: !GCC_PATH!

:: Detectar mingw32-make
call :DETECT_MAKE
if "!MAKE_PATH!"=="" (
    echo ERROR: No se pudo encontrar mingw32-make
    cd ..
    pause
    goto MAIN_MENU
)

echo Usando Make: !MAKE_PATH!
echo.

:: Limpiar build anterior si existe con cache inválido
if exist "build\CMakeCache.txt" (
    findstr /C:"OneDrive" build\CMakeCache.txt >nul 2>&1
    if !errorlevel! equ 0 (
        echo Limpiando cache antiguo...
        rmdir /s /q build 2>nul
    )
)

:: Compilar
echo Compilando cliente...
cmake -B build -G "MinGW Makefiles" -DCMAKE_C_COMPILER="!GCC_PATH!" -DCMAKE_MAKE_PROGRAM="!MAKE_PATH!"

if errorlevel 1 (
    echo.
    echo ERROR: Error en la configuracion de CMake
    cd ..
    pause
    goto MAIN_MENU
)

cmake --build build

if errorlevel 1 (
    echo.
    echo ERROR: No se pudo compilar el cliente
    cd ..
    pause
    goto MAIN_MENU
)

echo.
echo Compilacion exitosa
echo Iniciando cliente...
echo.

:: Ejecutar cliente
start "" "build\DonCEyKongJr-Client.exe"

echo Cliente iniciado
cd ..
timeout /t 2 >nul
goto MAIN_MENU

:BUILD_CLIENT_SILENT
cd cliente-c
call :DETECT_GCC
call :DETECT_MAKE
if exist "build\CMakeCache.txt" (
    findstr /C:"OneDrive" build\CMakeCache.txt >nul 2>&1
    if !errorlevel! equ 0 (
        rmdir /s /q build 2>nul
    )
)
cmake -B build -G "MinGW Makefiles" -DCMAKE_C_COMPILER="!GCC_PATH!" -DCMAKE_MAKE_PROGRAM="!MAKE_PATH!" >nul 2>&1
cmake --build build
cd ..
exit /b %errorlevel%

::::::::::::::::::::::::::::::::::::::::::
:: COMPILAR TODO
::::::::::::::::::::::::::::::::::::::::::

:BUILD_ALL
cls
echo %CYAN%=========================================%RESET%
echo %GREEN%Compilando Todo...%RESET%
echo %CYAN%=========================================%RESET%
echo.

echo %YELLOW%[1/2] Compilando Servidor...%RESET%
call :BUILD_SERVER_SILENT
if errorlevel 1 (
    echo %RED%Error al compilar servidor%RESET%
    pause
    goto MAIN_MENU
)
echo %GREEN%✓ Servidor compilado%RESET%
echo.

echo %YELLOW%[2/2] Compilando Cliente...%RESET%
call :BUILD_CLIENT_SILENT
if errorlevel 1 (
    echo %RED%Error al compilar cliente%RESET%
    pause
    goto MAIN_MENU
)
echo %GREEN%✓ Cliente compilado%RESET%
echo.

echo %GREEN%=========================================%RESET%
echo %GREEN%  ✓ Compilación completa exitosa%RESET%
echo %GREEN%=========================================%RESET%
pause
goto MAIN_MENU

::::::::::::::::::::::::::::::::::::::::::
:: LIMPIAR
::::::::::::::::::::::::::::::::::::::::::

:CLEAN_ALL
cls
echo %CYAN%=========================================%RESET%
echo %YELLOW%Limpiando builds...%RESET%
echo %CYAN%=========================================%RESET%
echo.

echo Limpiando servidor...
cd servidor-java
if exist "gradlew.bat" (
    gradlew.bat clean
) else (
    gradle clean
)
cd ..
echo %GREEN%✓ Servidor limpio%RESET%

echo.
echo Limpiando cliente...
cd cliente-c
if exist "build" rmdir /s /q build
cd ..
echo %GREEN%✓ Cliente limpio%RESET%

echo.
echo %GREEN%✓ Limpieza completa%RESET%
pause
goto MAIN_MENU

::::::::::::::::::::::::::::::::::::::::::
:: VERIFICAR DEPENDENCIAS
::::::::::::::::::::::::::::::::::::::::::

:CHECK_DEPS
cls
echo %CYAN%=========================================%RESET%
echo %GREEN%Verificando Dependencias...%RESET%
echo %CYAN%=========================================%RESET%
echo.

:: Verificar Java
echo %CYAN%Verificando Java...%RESET%
java -version >nul 2>&1
if errorlevel 1 (
    echo %RED%✗ Java no encontrado%RESET%
) else (
    java -version 2>&1 | findstr "version"
    echo %GREEN%✓ Java encontrado%RESET%
)
echo.

:: Verificar Gradle
echo %CYAN%Verificando Gradle...%RESET%
if exist "servidor-java\gradlew.bat" (
    echo %GREEN%✓ Gradle Wrapper encontrado%RESET%
) else (
    gradle -version >nul 2>&1
    if errorlevel 1 (
        echo %RED%✗ Gradle no encontrado%RESET%
    ) else (
        gradle -version | findstr "Gradle"
        echo %GREEN%✓ Gradle encontrado%RESET%
    )
)
echo.

:: Verificar GCC
echo %CYAN%Verificando GCC...%RESET%
call :DETECT_GCC
if "!GCC_PATH!"=="" (
    echo %RED%✗ GCC no encontrado%RESET%
) else (
    echo %GREEN%✓ GCC encontrado: !GCC_PATH!%RESET%
    "!GCC_PATH!" --version | findstr "gcc"
)
echo.

:: Verificar Make
echo %CYAN%Verificando MinGW Make...%RESET%
call :DETECT_MAKE
if "!MAKE_PATH!"=="" (
    echo %RED%✗ mingw32-make no encontrado%RESET%
) else (
    echo %GREEN%✓ Make encontrado: !MAKE_PATH!%RESET%
)
echo.

:: Verificar CMake
echo %CYAN%Verificando CMake...%RESET%
cmake --version >nul 2>&1
if errorlevel 1 (
    echo %RED%✗ CMake no encontrado%RESET%
) else (
    cmake --version | findstr "version"
    echo %GREEN%✓ CMake encontrado%RESET%
)
echo.

echo %CYAN%=========================================%RESET%
pause
goto MAIN_MENU

::::::::::::::::::::::::::::::::::::::::::
:: FUNCIONES DE DETECCIÓN
::::::::::::::::::::::::::::::::::::::::::

:DETECT_GCC
:: Buscar GCC en ubicaciones comunes
set "GCC_PATH="

:: Intentar usar 'where' primero
where gcc.exe >nul 2>&1
if !errorlevel! equ 0 (
    for /f "tokens=*" %%i in ('where gcc.exe 2^>nul') do (
        set "GCC_PATH=%%i"
        goto :gcc_found
    )
)

:: Buscar en MSYS2
if exist "C:\msys64\mingw64\bin\gcc.exe" (
    set "GCC_PATH=C:\msys64\mingw64\bin\gcc.exe"
    goto :gcc_found
)

if exist "C:\msys64\ucrt64\bin\gcc.exe" (
    set "GCC_PATH=C:\msys64\ucrt64\bin\gcc.exe"
    goto :gcc_found
)

:: Buscar en MinGW
if exist "C:\MinGW\bin\gcc.exe" (
    set "GCC_PATH=C:\MinGW\bin\gcc.exe"
    goto :gcc_found
)

if exist "C:\ProgramData\mingw64\mingw64\bin\gcc.exe" (
    set "GCC_PATH=C:\ProgramData\mingw64\mingw64\bin\gcc.exe"
    goto :gcc_found
)

:gcc_found
exit /b 0

:DETECT_MAKE
:: Buscar mingw32-make en ubicaciones comunes
set "MAKE_PATH="

:: Intentar usar 'where' primero
where mingw32-make.exe >nul 2>&1
if !errorlevel! equ 0 (
    for /f "tokens=*" %%i in ('where mingw32-make.exe 2^>nul') do (
        set "MAKE_PATH=%%i"
        goto :make_found
    )
)

:: Buscar en la misma carpeta que GCC
if not "!GCC_PATH!"=="" (
    for %%i in ("!GCC_PATH!") do set "GCC_DIR=%%~dpi"
    if exist "!GCC_DIR!mingw32-make.exe" (
        set "MAKE_PATH=!GCC_DIR!mingw32-make.exe"
        goto :make_found
    )
)

:: Buscar en MSYS2
if exist "C:\msys64\mingw64\bin\mingw32-make.exe" (
    set "MAKE_PATH=C:\msys64\mingw64\bin\mingw32-make.exe"
    goto :make_found
)

if exist "C:\msys64\ucrt64\bin\mingw32-make.exe" (
    set "MAKE_PATH=C:\msys64\ucrt64\bin\mingw32-make.exe"
    goto :make_found
)

:: Buscar en MinGW
if exist "C:\MinGW\bin\mingw32-make.exe" (
    set "MAKE_PATH=C:\MinGW\bin\mingw32-make.exe"
    goto :make_found
)

if exist "C:\ProgramData\mingw64\mingw64\bin\mingw32-make.exe" (
    set "MAKE_PATH=C:\ProgramData\mingw64\mingw64\bin\mingw32-make.exe"
    goto :make_found
)

:make_found
exit /b 0

::::::::::::::::::::::::::::::::::::::::::
:: SALIR
::::::::::::::::::::::::::::::::::::::::::

:EXIT
cls
echo.
echo Gracias por usar DonCEyKongJr Launcher
echo.
timeout /t 2 >nul
exit /b 0
