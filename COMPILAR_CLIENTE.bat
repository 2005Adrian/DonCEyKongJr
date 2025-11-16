@echo off
echo Compilando cliente...
cd cliente-c

set GCC_PATH=C:\ProgramData\mingw64\mingw64\bin\gcc.exe
set MAKE_PATH=C:\ProgramData\mingw64\mingw64\bin\mingw32-make.exe

cmake -B build -G "MinGW Makefiles" -DCMAKE_C_COMPILER="%GCC_PATH%" -DCMAKE_MAKE_PROGRAM="%MAKE_PATH%"
cmake --build build

if errorlevel 1 (
    echo ERROR: No se pudo compilar el cliente
    pause
    exit /b 1
)

echo Cliente compilado exitosamente
cd ..
pause
