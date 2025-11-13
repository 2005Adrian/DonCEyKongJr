@echo off
echo ==========================================
echo Compilando Servidor Java...
echo ==========================================
cd servidor-java
call gradlew.bat build --no-daemon
if errorlevel 1 (
    echo ERROR: Fallo al compilar servidor
    cd ..
    pause
    exit /b 1
)
cd ..
echo.
echo ==========================================
echo Servidor compilado exitosamente!
echo ==========================================
echo.
echo NOTA: Para compilar el cliente C, ejecuta BUILD_MENU.bat y selecciona opcion 2
pause
