@echo off
title DonCEyKongJr - Iniciar Juego
echo.
echo =========================================
echo     DonCEyKongJr - Iniciando Juego
echo =========================================
echo.

:: Verificar si ya está compilado
if not exist "servidor-java\build\libs" (
    echo Compilando servidor por primera vez...
    cd servidor-java
    call gradlew.bat build -x test
    cd ..
)

if not exist "cliente-c\build\DonCEyKongJr-Client.exe" (
    echo.
    echo Cliente no compilado. Por favor, ejecuta BUILD_MENU.bat
    echo y selecciona la opcion [2] para compilar el cliente.
    pause
    exit /b 1
)

echo.
echo [1/2] Iniciando servidor en nueva ventana...
start "DonCEyKongJr Servidor" cmd /k "cd /d %~dp0servidor-java && gradlew.bat run"

echo.
echo Esperando 5 segundos para que el servidor inicie...
timeout /t 5 /nobreak

echo.
echo [2/2] Iniciando cliente...
start "" "cliente-c\build\DonCEyKongJr-Client.exe"

echo.
echo =========================================
echo  Juego iniciado exitosamente!
echo =========================================
echo.
echo - Servidor: Ventana separada
echo - Cliente: Ventana de juego
echo.
echo Controles:
echo   W/A/S/D : Moverse
echo   SPACE   : Saltar
echo   ESC     : Salir
echo.
echo Para cerrar el servidor, cierra la ventana del servidor.
echo.
timeout /t 3
exit
