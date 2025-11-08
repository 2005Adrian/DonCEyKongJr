# Cliente DonCEy Kong Jr (C)

Cliente en C para el juego DonCEy Kong Jr. Se conecta al servidor por sockets TCP y permite controlar al personaje en tiempo real.

**Versión Modular con Soporte para Sprites:**
- ⭐ **GUI COMPLETA** - Versión modular con todos los elementos visuales
- 🎨 **Sistema de Sprites** - Soporte para imágenes BMP personalizadas
- 🐛 **Herramientas de Debug** - Cliente de debug JSON incluido

## Estructura del Proyecto

```
cliente-c/
├── sprites/                         # 🎨 Sprites del juego (BMP)
│   ├── jr.bmp                       # Sprite de Jr
│   ├── donkey.bmp                   # Sprite de Donkey Kong
│   ├── cocodrilo_rojo.bmp          # Sprite cocodrilo rojo
│   ├── cocodrilo_azul.bmp          # Sprite cocodrilo azul
│   ├── banana.bmp                   # Sprite fruta
│   ├── corazon.bmp                  # Sprite corazón HUD
│   └── README.md                    # Guía para crear sprites
│
├── src/
│   ├── 📋 Configuración
│   ├── constants.h                  # Constantes del juego
│   ├── structs.h                    # Estructuras de datos
│   │
│   ├── ⭐ Código Modular
│   ├── main.c                       # Punto de entrada y ventana
│   ├── game.c / game.h              # Lógica y variables globales
│   ├── network.c / network.h        # Comunicación y parseo JSON
│   ├── render.c / render.h          # Sistema de renderizado
│   ├── input.c / input.h            # Manejo de controles
│   ├── sprites.c / sprites.h        # Sistema de sprites (BMP)
│   │
│   ├── 🛠️ Scripts y Herramientas
│   ├── iniciar-cliente-completo.bat # Compilar y ejecutar juego
│   ├── debug-json.bat               # Debug del protocolo JSON
│   ├── test_json_debug.c            # Cliente de debug
│   │
│   └── 🎮 Ejecutables
│       ├── client_gui_completo.exe  # Juego principal
│       └── test_json_debug.exe      # Debug tool
│
└── README.md                        # Este archivo

```

## Requisitos

- **GCC** (MinGW-w64 15.2.0 o superior)
- **Windows** (usa Winsock2 y GDI32)
- **Servidor Java** corriendo en `127.0.0.1:5555`

## Compilación y Ejecución

### ⭐ Versión COMPLETA (Recomendado)

```batch
cd src
iniciar-cliente-completo.bat
```

**El cliente gráfico completo incluye:**
- ✨ Escenario completo con 4 lianas verticales
- 🏗️ 3 plataformas horizontales + zona de abismo
- 🦍 Donkey Kong en jaula en la parte superior
- 👾 Sprites mejorados para Jr, cocodrilos (rojos y azules) y frutas
- ❤️ HUD visual con corazones para vidas
- 📊 Puntaje grande y nivel actual
- 🎬 Pantallas de título, game over y victoria
- ✨ Animaciones suaves y efectos visuales
- 🎨 Gráficos mejorados con colores y texturas
- 📡 Indicador de conexión en tiempo real

**Compilación manual (estructura modular):**
```batch
gcc main.c game.c network.c render.c input.c -o client_gui_completo.exe -lws2_32 -lgdi32 -lmsimg32 -mwindows
client_gui_completo.exe
```

La versión modular separa el código en:
- **main.c**: Ventana y loop principal
- **game.c**: Variables globales y utilidades
- **network.c**: Red y parseo JSON (con manejo correcto de timeouts)
- **render.c**: Todas las funciones de dibujo y HUD con debug
- **input.c**: Procesamiento de teclas

### Agregar Sprites Personalizados

El juego soporta sprites en formato BMP. Lee [sprites/README.md](sprites/README.md) para instrucciones detalladas.

**Rápido:**
1. Crea imágenes BMP de 24-bit en Paint o GIMP
2. Guárdalas en `cliente-c/sprites/`
3. Nombres: `jr.bmp`, `donkey.bmp`, `cocodrilo_rojo.bmp`, etc.
4. Fondo blanco = transparente
5. Reinicia el juego

Si no hay sprites, el juego usa gráficos dibujados (funciona igual).

## Controles del Juego

| Tecla | Acción |
|-------|--------|
| **W** | Subir por la liana |
| **S** | Bajar por la liana |
| **A** | Moverse a la izquierda |
| **D** | Moverse a la derecha |
| **ESPACIO** | Saltar (para colgarse de lianas) |
| **E** | Agarrar liana |
| **Q** | Salir del juego |

## Arquitectura

### Archivos de Configuración

#### `constants.h`
Define todas las constantes del juego:
- **Red**: IP del servidor (`127.0.0.1`), puerto (`5555`), tamaño de buffer
- **Juego**: Número máximo de lianas, cocodrilos, frutas y jugadores
- **Pantalla**: Dimensiones de la consola (80x30)
- **Controles**: Mapeo de teclas
- **Renderizado**: Caracteres ASCII para cada entidad

#### `structs.h`
Define las estructuras de datos:
- **Jugador**: `id`, posición `(x,y)`, `liana`, vidas, puntaje, activo
- **Cocodrilo**: `id`, tipo (`"ROJO"` o `"AZUL"`), `liana`, posición `y`
- **Fruta**: `id`, `liana`, posición `y`, puntos
- **EstadoJuego**: Contiene todos los jugadores, cocodrilos y frutas
- **MensajeCliente**: Formato para enviar inputs al servidor

### Flujo del Cliente

1. **Conexión**
   - Inicializa Winsock2
   - Crea socket TCP
   - Conecta a `127.0.0.1:5555`
   - Envía mensaje JSON de conexión con ID único

2. **Loop Principal**
   ```
   while (juegoActivo):
       - Recibir estado del servidor (JSON)
       - Parsear estado y actualizar estructuras locales
       - Detectar teclas presionadas (sin bloqueo)
       - Enviar inputs al servidor (JSON)
       - Renderizar juego en consola (cada 100ms)
   ```

3. **Comunicación con Servidor**

   **Envío (Cliente → Servidor)**:
   ```json
   {
       "type": "INPUT",
       "playerId": "Player_1234",
       "action": "MOVE_UP",
       "velocity": 1.0
   }
   ```

   **Recepción (Servidor → Cliente)**:
   ```json
   {
       "tick": 12345,
       "level": 2,
       "paused": false,
       "players": [...],
       "crocodiles": [...],
       "fruits": [...]
   }
   ```

4. **Renderizado**
   - Crea matriz de caracteres 80x30
   - Dibuja lianas verticales (`|`)
   - Dibuja jugadores (`J`)
   - Dibuja cocodrilos rojos (`R`) y azules (`A`)
   - Dibuja frutas (`F`)
   - Imprime en consola con `cls` entre frames

5. **Desconexión**
   - Envía mensaje `{"type":"DISCONNECT"}`
   - Cierra socket
   - Limpia Winsock

## Protocolo de Red

### Formato de Mensajes

Todos los mensajes son JSON terminados en `\n`:

**CONNECT** (cliente → servidor):
```json
{"type":"CONNECT","playerId":"Player_5432"}
```

**INPUT** (cliente → servidor):
```json
{
    "type":"INPUT",
    "playerId":"Player_5432",
    "action":"MOVE_UP",
    "velocity":1.0
}
```

Acciones válidas: `MOVE_UP`, `MOVE_DOWN`, `LEFT`, `RIGHT`, `JUMP`, `GRAB`

**STATE** (servidor → cliente):
```json
{
    "tick":123,
    "level":1,
    "speedMultiplier":1.0,
    "paused":false,
    "players":[
        {"id":"Player_5432","x":5.0,"y":3.0,"liana":2,"lives":3,"score":100,"active":true}
    ],
    "crocodiles":[
        {"id":"Croc_1","kind":"ROJO","liana":1,"y":5.0}
    ],
    "fruits":[
        {"id":"Fruit_1","liana":3,"y":7.0,"points":10}
    ]
}
```

**DISCONNECT** (cliente → servidor):
```json
{"type":"DISCONNECT","playerId":"Player_5432"}
```

## Notas Técnicas

### Parseo JSON
El cliente actual usa parseo manual simple con `strstr()` y `sscanf()`. Esto funciona para valores básicos como `level`, `paused`, `tick`.

Para parsear arrays completos de jugadores/cocodrilos/frutas se recomienda integrar **cJSON**:
```c
// Ejemplo con cJSON (no incluido actualmente):
cJSON *json = cJSON_Parse(buffer);
cJSON *players = cJSON_GetObjectItem(json, "players");
// ... parsear array
cJSON_Delete(json);
```

### Control sin Bloqueo
Usa `_kbhit()` (Windows) para detectar teclas sin bloquear el loop principal:
```c
if (_kbhit()) {
    char tecla = _getch();
    procesarTecla(tecla);
}
```

### Timeout de Socket
Configura timeout de 100ms para `recv()` para evitar bloqueos:
```c
DWORD timeout = 100;
setsockopt(sockCliente, SOL_SOCKET, SO_RCVTIMEO, (char*)&timeout, sizeof(timeout));
```

## Troubleshooting

### Error: "No se pudo conectar al servidor"
- Verifica que el servidor Java esté corriendo
- Ejecuta `servidor-java/iniciar-servidor.bat` primero
- Verifica que el puerto 5555 esté libre

### Error: "GCC no está instalado"
- Descarga MinGW-w64 desde: https://www.mingw-w64.org/
- Agrega `C:\mingw64\bin` al PATH del sistema
- Reinicia la terminal

### Error: "Conexión perdida con el servidor"
- El servidor se cerró inesperadamente
- Verifica logs del servidor Java
- Reinicia ambos (servidor y cliente)

### El juego no renderiza correctamente
- Asegúrate de usar consola con al menos 80x30 caracteres
- Ajusta `SCREEN_WIDTH` y `SCREEN_HEIGHT` en `constants.h`
- Recompila: `gcc client.c -o client.exe -lws2_32`

## Mejoras Futuras

- [ ] Integrar librería cJSON para parseo completo del estado
- [ ] Agregar colores en consola (Windows console API)
- [ ] Implementar interpolación de movimientos
- [ ] Agregar efectos de sonido (Windows Beep API)
- [ ] Soporte para múltiples jugadores locales
- [ ] GUI con SDL2 o ncurses

## Licencia

Proyecto educativo - Tecnológico de Costa Rica
