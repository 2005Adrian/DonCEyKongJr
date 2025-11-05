# Versiones Requeridas - DonCEyKongJr

Este documento especifica todas las versiones de software necesarias para compilar y ejecutar correctamente el proyecto DonCEyKongJr.

## Resumen Ejecutivo

| Componente | Versión Mínima | Versión Recomendada | Versión Detectada |
|------------|----------------|---------------------|-------------------|
| Java JDK   | 21             | 21 o 25            | 25 (Temurin)      |
| Gradle     | 9.1.0          | 9.1.0              | 9.1.0             |
| GCC/MinGW  | 8.0            | 15.x               | 15.2.0            |
| Git        | 2.0            | Última             | -                 |
| Windows    | 10             | 10/11              | 11                |

---

## 1. Java Development Kit (JDK)

### Versión Requerida
- **Mínima**: Java 21 LTS
- **Recomendada**: Java 21 LTS o Java 25 LTS
- **Actualmente instalada**: OpenJDK 25 (Temurin-25+36-LTS)

### Distribución Recomendada
- **Eclipse Temurin** (antes AdoptOpenJDK)
- Descarga: https://adoptium.net/

### Configuración del Proyecto
El proyecto está configurado para:
- **Toolchain**: Java 21 (para ejecutar Gradle)
- **Source Compatibility**: Java 1.8 (para compatibilidad del código)
- **Target Compatibility**: Java 1.8

### Verificar instalación
```bash
java -version
javac -version
```

**Salida esperada**:
```
openjdk version "25" 2025-09-16 LTS
OpenJDK Runtime Environment Temurin-25+36 (build 25+36-LTS)
OpenJDK 64-Bit Server VM Temurin-25+36 (build 25+36-LTS, mixed mode, sharing)
```

---

## 2. Gradle Build Tool

### Versión Requerida
- **Exacta**: Gradle 9.1.0
- **Build time**: 2025-09-18 13:05:56 UTC

### Componentes incluidos
- **Kotlin**: 2.2.0
- **Groovy**: 4.0.28
- **Ant**: 1.10.15

### Instalación
El proyecto incluye Gradle Wrapper, por lo que NO necesitas instalar Gradle globalmente.

#### Opción 1: Usar Gradle Wrapper (Recomendado)
```bash
cd servidor-java
./gradlew --version     # Linux/Mac
gradlew.bat --version   # Windows
```

#### Opción 2: Instalación Global
Si deseas instalar Gradle globalmente:
- Descarga: https://gradle.org/releases/
- Versión específica: https://gradle.org/release-candidate/

### Verificar instalación
```bash
gradle --version
```

**Salida esperada**:
```
------------------------------------------------------------
Gradle 9.1.0
------------------------------------------------------------

Build time:    2025-09-18 13:05:56 UTC
Revision:      e45a8dbf2470c2e2474ccc25be9f49331406a07e

Kotlin:        2.2.0
Groovy:        4.0.28
Ant:           Apache Ant(TM) version 1.10.15 compiled on August 25 2024
Launcher JVM:  25 (Eclipse Adoptium 25+36-LTS)
Daemon JVM:    C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot
OS:            Windows 11 10.0 amd64
```

---

## 3. GCC - Compilador C (para el Cliente)

### Versión Requerida
- **Mínima**: GCC 8.0
- **Recomendada**: GCC 15.x
- **Actualmente instalada**: GCC 15.2.0 (MinGW-Builds)

### Distribuciones Soportadas

#### Windows
1. **MinGW-Builds** (Recomendado)
   - Versión: 15.2.0 o superior
   - Descarga: https://www.mingw-w64.org/downloads/
   - Incluye: x86_64-posix-seh

2. **TDM-GCC**
   - Descarga: https://jmeubank.github.io/tdm-gcc/

3. **WSL (Windows Subsystem for Linux)**
   - Instalar Ubuntu desde Microsoft Store
   - Luego: `sudo apt install gcc`

#### Linux
```bash
sudo apt install gcc         # Debian/Ubuntu
sudo yum install gcc         # RedHat/CentOS
```

#### macOS
```bash
xcode-select --install
brew install gcc
```

### Verificar instalación
```bash
gcc --version
```

**Salida esperada**:
```
gcc.exe (x86_64-posix-seh-rev0, Built by MinGW-Builds project) 15.2.0
Copyright (C) 2025 Free Software Foundation, Inc.
```

### Librerías Necesarias
El cliente C requiere:
- **Winsock2** (ws2_32.lib) - Incluida en Windows SDK
- **Windows SDK** - Viene con MinGW-Builds

---

## 4. Dependencias del Proyecto

### Servidor Java (servidor-java/build.gradle)

#### Dependencias de Producción
```gradle
implementation 'com.google.code.gson:gson:2.11.0'
```

- **Gson 2.11.0**: Librería de Google para serialización/deserialización JSON
- Descarga automática desde Maven Central

#### Dependencias de Testing (Opcional)
```gradle
testImplementation 'org.junit.jupiter:junit-jupiter:5.9.0'
```

- **JUnit Jupiter 5.9.0**: Framework de testing para Java

### Cliente C (cliente-c/)

#### Dependencias del Sistema
- **winsock2.h**: Biblioteca de sockets de Windows
- **ws2tcpip.h**: Funciones extendidas TCP/IP
- **ws2_32.lib**: Librería Winsock (enlazada automáticamente)

Estas librerías vienen incluidas con:
- Windows SDK (pre-instalado en Windows)
- MinGW-Builds

---

## 5. Sistema Operativo

### Requisitos
- **Windows**: 10 o 11 (64 bits)
- **Linux**: Cualquier distribución moderna (Ubuntu 20.04+, Fedora 35+, etc.)
- **macOS**: 10.15 Catalina o superior

### Sistema Actual
- **SO**: Windows 11
- **Versión**: 10.0.26200.7019
- **Arquitectura**: amd64 (64 bits)

---

## 6. Herramientas Adicionales Recomendadas

### Control de Versiones
- **Git**: 2.x o superior
- Descarga: https://git-scm.com/downloads

```bash
git --version
```

### IDE/Editor de Código (Opcional)

#### Para Java
- **IntelliJ IDEA** 2023.x+ (Recomendado)
- **Eclipse** 2023+ con plugin Gradle
- **VSCode** con extensiones:
  - Extension Pack for Java
  - Gradle for Java

#### Para C
- **Visual Studio Code** con extensión C/C++
- **Code::Blocks**
- **CLion**

---

## 7. Configuración de Variables de Entorno

### JAVA_HOME
Asegúrate de tener configurada la variable `JAVA_HOME`:

#### Windows
```cmd
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot"
setx PATH "%PATH%;%JAVA_HOME%\bin"
```

#### Linux/macOS
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

### Verificar
```bash
echo %JAVA_HOME%      # Windows CMD
echo $JAVA_HOME       # Linux/macOS/Git Bash
```

---

## 8. Script de Verificación de Versiones

El proyecto incluye un script para verificar todas las versiones instaladas:

### Windows
```bash
check-versions.bat
```

### Linux/macOS
Ejecuta manualmente:
```bash
java -version
gradle --version
gcc --version
git --version
```

---

## 9. Resolución de Problemas Comunes

### Problema: "java: command not found"
**Solución**:
1. Instala Java JDK (ver sección 1)
2. Configura JAVA_HOME (ver sección 7)
3. Agrega Java al PATH

### Problema: "gradlew: command not found"
**Solución**:
```bash
# Windows
.\gradlew --version

# Linux/macOS
chmod +x gradlew
./gradlew --version
```

### Problema: "gcc: command not found" (Windows)
**Solución**:
1. Instala MinGW-Builds
2. Agrega `C:\mingw64\bin` al PATH del sistema

### Problema: Gradle usa Java incorrecta
**Solución**:
```bash
# Ver qué Java usa Gradle
gradle --version

# Forzar Java específica
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.0.36-hotspot
gradle --version
```

### Problema: Error de compilación del servidor
**Solución**:
```bash
cd servidor-java
gradle clean build --refresh-dependencies
```

### Problema: Error de enlazado Winsock en cliente C
**Solución**:
Asegúrate de compilar con:
```bash
gcc cliente_prueba.c -o cliente_prueba.exe -lws2_32
```

---

## 10. Comandos de Compilación y Ejecución

### Servidor Java

#### Compilar
```bash
cd servidor-java
gradle build
```

#### Ejecutar
```bash
gradle run
```

#### Limpiar y reconstruir
```bash
gradle clean build
```

### Cliente C

#### Compilar (Windows)
```bash
cd cliente-c/src
gcc cliente_prueba.c -o cliente_prueba.exe -lws2_32
```

#### Ejecutar
```bash
cliente_prueba.exe
```

#### Compilar (Linux/macOS)
```bash
cd cliente-c/src
gcc cliente_prueba.c -o cliente_prueba -pthread
```

---

## 11. Verificación Final

Ejecuta estos comandos para verificar que todo está correctamente instalado:

```bash
# 1. Verificar Java
java -version

# 2. Verificar Gradle
gradle --version

# 3. Verificar GCC
gcc --version

# 4. Compilar servidor
cd servidor-java
gradle build

# 5. Compilar cliente
cd ../cliente-c/src
gcc cliente_prueba.c -o cliente_prueba.exe -lws2_32

# 6. Ejecutar tests del servidor
cd ../../servidor-java
gradle test
```

Si todos los comandos se ejecutan sin errores, tu entorno está correctamente configurado.

---

## Resumen de URLs de Descarga

| Componente | URL |
|------------|-----|
| Java JDK (Temurin) | https://adoptium.net/ |
| Gradle | https://gradle.org/releases/ |
| MinGW-Builds | https://www.mingw-w64.org/downloads/ |
| Git | https://git-scm.com/downloads |
| IntelliJ IDEA | https://www.jetbrains.com/idea/download/ |
| VSCode | https://code.visualstudio.com/ |

---

**Última actualización**: 2025-11-04
**Versión del documento**: 1.0.0
