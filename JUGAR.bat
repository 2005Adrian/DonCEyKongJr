@echo off
title DonCEyKongJr - Inicio Automatico
echo.
echo =========================================
echo     DonCEyKongJr - Inicio Automatico
echo =========================================
echo.
echo Compilando y ejecutando todo automaticamente...
echo.

:: ============================================
:: PASO 1: COMPILAR SERVIDOR JAVA
:: ============================================
echo [PASO 1/3] Compilando servidor Java...
cd servidor-java
call gradlew.bat build -x test --console=plain -q
if errorlevel 1 (
    echo.
    echo ERROR: Fallo la compilacion del servidor Java
    pause
    exit /b 1
)
cd ..
echo    ✓ Servidor Java compilado exitosamente
echo.

:: ============================================
:: PASO 2: COMPILAR CLIENTE C
:: ============================================
echo [PASO 2/3] Compilando cliente C...

:: Crear directorio build si no existe
if not exist "cliente-c\build" mkdir "cliente-c\build"

:: Configurar CMake
cd cliente-c\build
cmake .. -G "MinGW Makefiles" -DCMAKE_BUILD_TYPE=Release >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: CMake fallo al configurar el proyecto
    echo Verifica que CMake y MinGW esten instalados y en el PATH
    cd ..\..
    pause
    exit /b 1
)

:: Compilar con MinGW
mingw32-make >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Fallo la compilacion del cliente C
    echo Verifica que MinGW este instalado correctamente
    cd ..\..
    pause
    exit /b 1
)
cd ..\..
echo    ✓ Cliente C compilado exitosamente
echo.

:: Verificar que el ejecutable existe
if not exist "cliente-c\build\DonCEyKongJr-Client.exe" (
    echo.
    echo ERROR: El ejecutable del cliente no se genero correctamente
    pause
    exit /b 1
)

:: ============================================
:: PASO 3: EJECUTAR TODO
:: ============================================
echo [PASO 3/3] Iniciando componentes...
echo.

echo    [1/3] Iniciando SERVIDOR (puerto 5555)...
start "DonCEyKongJr Servidor" cmd /k "cd /d %~dp0servidor-java && gradlew.bat run"
timeout /t 5 /nobreak >nul

echo    [2/3] Iniciando CLIENTE JUGADOR...
start "" "cliente-c\build\DonCEyKongJr-Client.exe"
timeout /t 2 /nobreak >nul

echo    [3/3] Iniciando CLIENTE ESPECTADOR...
start "" "cliente-c\build\DonCEyKongJr-Client.exe" --spectator

echo.
echo =========================================
echo  ✓ TODO INICIADO EXITOSAMENTE!
echo =========================================
echo.
echo Ventanas abiertas:
echo   - Servidor (ventana cmd negra)
echo   - Cliente JUGADOR (titulo: "DonCEy Kong Jr - JUGADOR")
echo   - Cliente ESPECTADOR (titulo: "DonCEy Kong Jr - ESPECTADOR")
echo.
echo =========================================
echo  PRUEBAS A REALIZAR
echo =========================================
echo.
echo 1. Mueve al JUGADOR con W/A/S/D + SPACE
echo    El ESPECTADOR debe ver los movimientos en tiempo real
echo.
echo 2. Intenta presionar teclas en la ventana ESPECTADOR
echo    No debe hacer nada (el servidor rechaza los inputs)
echo.
echo 3. Intenta conectar un TERCER cliente ejecutando:
echo    cliente-c\build\DonCEyKongJr-Client.exe
echo    Debe ser RECHAZADO (limite: 1 jugador + 1 espectador)
echo.
echo 4. Cierra el ESPECTADOR
echo    El JUGADOR debe seguir jugando normalmente
echo.
echo =========================================
echo.
echo Controles del jugador:
echo   W/A/S/D : Moverse
echo   SPACE   : Saltar
echo   ESC     : Salir
echo.
echo Para CERRAR TODO:
echo   - Cierra las ventanas de juego (X)
echo   - Cierra la ventana del servidor (X o Ctrl+C)
echo.
echo Esta ventana se cerrara en 15 segundos...
timeout /t 15
exit

