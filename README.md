# DonCEyKongJr - Multiplayer Game

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![C](https://img.shields.io/badge/C-11-blue?logo=c)
![Gradle](https://img.shields.io/badge/Gradle-8.11.1-green?logo=gradle)
![CMake](https://img.shields.io/badge/CMake-3.20+-blue?logo=cmake)
![License](https://img.shields.io/badge/License-MIT-yellow)

**Juego multiplayer cliente-servidor de DonkeyKong Jr**

Cliente nativo en C • Servidor en Java • Instaladores multiplataforma

</div>

---

## 📋 Descripción

DonCEyKongJr es un juego multiplayer basado en arquitectura cliente-servidor:

- **Servidor (Java)**: Maneja la lógica del juego, estado compartido, y sincronización entre clientes
- **Cliente (C)**: Interfaz gráfica nativa con renderizado optimizado usando GDI (Windows)
- **Protocolo**: Comunicación TCP/IP con mensajes JSON
- **Arquitectura**: Patrón Observer, Factory, y sincronización a 20 TPS

---

## 🚀 Inicio Rápido ("Un Botón")

### Prerrequisitos

Asegúrate de tener instalado:

| Herramienta | Versión Mínima | Descarga |
|-------------|----------------|----------|
| **Java JDK** | 21 (Temurin LTS) | [Adoptium](https://adoptium.net/) |
| **CMake** | 3.20+ | [cmake.org](https://cmake.org/download/) |
| **GCC/MinGW** | 11+ | [MinGW-w64](https://www.mingw-w64.org/) (Windows) |

### Build y Ejecución

#### Opción 1: Build completo (Servidor + Cliente)

```bash
cd servidor-java
./gradlew buildAll
```

#### Opción 2: Solo servidor

```bash
cd servidor-java
./gradlew build
./gradlew run
```

El servidor se iniciará en `localhost:5555`

#### Opción 3: Solo cliente nativo

```bash
cd cliente-c
mkdir build && cd build
cmake .. -DCMAKE_BUILD_TYPE=Release
make
./DonCEyKongJr-Client
```

En Windows con MinGW:

```bash
cd cliente-c
mkdir build && cd build
cmake .. -G "MinGW Makefiles" -DCMAKE_BUILD_TYPE=Release
mingw32-make
.\DonCEyKongJr-Client.exe
```

---

## 📦 Crear Instaladores

### Instalador completo (jpackage)

Genera instaladores nativos para el sistema operativo actual:

```bash
cd servidor-java
./gradlew packageAll
```

Esto creará:
- **Windows**: `servidor-java/build/jpackage/DonCEyKongJr-1.0.0.msi`
- **macOS**: `servidor-java/build/jpackage/DonCEyKongJr-1.0.0.dmg`
- **Linux**: `servidor-java/build/jpackage/DonCEyKongJr-1.0.0.deb`

Los instaladores incluyen:
✅ Runtime Java optimizado (jlink)
✅ Cliente nativo compilado
✅ Sprites y recursos
✅ Lanzadores del sistema

### Runtime portable (jlink)

Crear un runtime Java customizado sin instalador:

```bash
cd servidor-java
./gradlew jlink
```

El runtime se generará en `servidor-java/build/jlink/image/`

Para ejecutar:

```bash
cd servidor-java/build/jlink/image/bin
./DonCEyKongJr-Server
```

---

## 🛠️ Comandos de Gradle

### Desarrollo

```bash
# Ejecutar servidor en modo desarrollo
./gradlew run

# Ejecutar con debugging (puerto 5005)
./gradlew run -Pdebug

# Ejecutar tests
./gradlew test

# Ver ayuda de tasks disponibles
./gradlew help
```

### Build

```bash
# Build solo servidor Java
./gradlew build

# Build solo cliente C nativo
./gradlew buildNativeClient

# Build todo (servidor + cliente)
./gradlew buildAll
```

### Distribución

```bash
# Crear runtime customizado con jlink
./gradlew jlink

# Crear instalador con jpackage
./gradlew jpackage

# Crear instalador completo (build + jlink + jpackage)
./gradlew packageAll
```

### Limpieza

```bash
# Limpiar solo build de Java
./gradlew clean

# Limpiar todo (Java + C)
./gradlew cleanAll
```

---

## 🏗️ Estructura del Proyecto

```
DonCEyKongJr/
├── .github/
│   └── workflows/
│       └── build.yml              # CI/CD multiplataforma
│
├── servidor-java/                 # SERVIDOR JAVA
│   ├── build.gradle               # Configuración Gradle moderna
│   ├── gradle.properties          # Propiedades del proyecto
│   ├── gradlew / gradlew.bat      # Gradle Wrapper
│   └── src/main/java/
│       └── cr/tec/donceykongjr/server/
│           ├── Main.java          # Punto de entrada
│           ├── network/           # Servidor TCP, protocolo JSON
│           ├── logic/             # GameManager, GameLoop (20 TPS)
│           ├── gui/               # Interfaz gráfica admin
│           └── util/              # Utilidades, logging, config
│
├── cliente-c/                     # CLIENTE NATIVO C
│   ├── CMakeLists.txt             # Build multiplataforma
│   ├── sprites/                   # Recursos gráficos
│   └── src/
│       ├── main.c                 # Punto de entrada
│       ├── network.c/h            # Cliente TCP
│       ├── render.c/h             # Renderizado GDI
│       ├── game.c/h               # Lógica del juego
│       ├── sprites.c/h            # Gestión de sprites
│       └── input.c/h              # Manejo de input
│
├── .gitignore                     # Ignora builds, binarios, temporales
└── README.md                      # Este archivo
```

---

## 🔧 Configuración Técnica

### Java (Servidor)

- **JDK**: 21 (Temurin)
- **Bytecode Target**: Java 21 (`--release 21`)
- **Toolchain**: Gradle Toolchains con vendor=ADOPTIUM
- **Build System**: Gradle 8.11.1
- **Dependencias**:
  - `com.google.code.gson:2.11.0` - Serialización JSON
  - `net.java.dev.jna:5.15.0` - Integración con código nativo (opcional)
  - `org.junit.jupiter:5.11.0` - Testing

### C (Cliente)

- **Estándar**: C11
- **Build System**: CMake 3.20+
- **Compilador**: GCC 11+ / MinGW (Windows)
- **Librerías**:
  - `ws2_32` - Sockets Windows
  - `gdi32` - Gráficos (GDI)
  - `msimg32` - Funciones de imagen

### Protocolo de Comunicación

- **Protocolo**: TCP/IP
- **Puerto**: 5555 (configurable en `Config.java`)
- **Formato**: JSON
- **Ejemplo de mensaje**:

```json
{
  "tipo": "MOVIMIENTO",
  "jugador_id": "Player_1234",
  "x": 100,
  "y": 50,
  "direccion": "DERECHA"
}
```

---

## 🤖 CI/CD (GitHub Actions)

El proyecto incluye workflows automáticos que se ejecutan en cada push:

### Plataformas soportadas

- ✅ **Ubuntu Latest** (Linux .deb)
- ✅ **Windows Latest** (Windows .msi)
- ✅ **macOS Latest** (macOS .dmg)

### Pipeline

1. **Setup**: Instala JDK 21, CMake, y compiladores nativos
2. **Build**: Compila servidor Java y cliente C
3. **Test**: Ejecuta suite de tests
4. **Package**: Genera instaladores con jpackage
5. **Upload**: Sube artefactos a GitHub Actions
6. **Release**: Publica instaladores en GitHub Releases (en tags)

### Ver builds

Visita la pestaña **Actions** en GitHub para ver el estado de los builds.

---

## 📖 Guía de Desarrollo

### Agregar nueva entidad al juego

1. Crear clase en `servidor-java/src/main/java/.../logic/entidades/`
2. Extender `Entidad.java`
3. Implementar lógica en `GameLoop.java`
4. Agregar factory en `FactoryEntidad.java`

### Modificar protocolo de red

1. Actualizar `Mensaje.java` con nuevo tipo
2. Implementar parsing en `ManejadorCliente.java`
3. Actualizar cliente C en `network.c` para parsear JSON

### Cambiar configuración del servidor

Editar constantes en `servidor-java/src/main/java/.../util/Config.java`:

```java
public static final int PUERTO_DEFAULT = 5555;
public static final int MAX_JUGADORES = 2;
public static final int TICKS_POR_SEGUNDO = 20;
```

---

## 🐛 Troubleshooting

### Error: "Java 21 not found"

Asegúrate de tener Java 21 instalado. Gradle usará toolchains para descargar automáticamente la versión correcta:

```bash
# Verificar versión de Java
java -version

# Forzar re-descarga de toolchain
./gradlew clean build --refresh-dependencies
```

### Error: "CMake not found" (Windows)

Instala CMake y agrégalo al PATH:

```bash
# Verificar instalación
cmake --version

# Agregar a PATH si es necesario
setx PATH "%PATH%;C:\Program Files\CMake\bin"
```

### Error: "mingw32-make not found" (Windows)

Instala MinGW-w64 y verifica que esté en el PATH:

```bash
# Verificar GCC
gcc --version

# Verificar make
mingw32-make --version
```

### Cliente no conecta al servidor

1. Verifica que el servidor esté corriendo (`./gradlew run`)
2. Verifica el puerto en `constants.h` del cliente
3. Verifica firewall/antivirus no bloquee el puerto 5555

---

## 📝 Licencia

Copyright © 2025 TEC - DonCEyKongJr Team

Proyecto académico desarrollado para el Tecnológico de Costa Rica.

---

## 👥 Contribuciones

Este es un proyecto académico. Para contribuir:

1. Fork el repositorio
2. Crea una rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agrega nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

---

## 📞 Soporte

Para preguntas o problemas:

- **Issues**: [GitHub Issues](https://github.com/2005Adrian/DonCEyKongJr/issues)
- **Documentación adicional**: Ver [VERSIONES.md](VERSIONES.md) e [INSTRUCCIONES_SIMPLES.md](INSTRUCCIONES_SIMPLES.md)

---

<div align="center">

**Hecho con ❤️ para el curso de Datos II - TEC**

[Reportar Bug](https://github.com/2005Adrian/DonCEyKongJr/issues) •
[Solicitar Feature](https://github.com/2005Adrian/DonCEyKongJr/issues)

</div>
