# 🎮 Servidor DonCEy Kong Jr - Guía de Ejecución

## ✅ Qué deberías ver al ejecutar el servidor

Cuando ejecutes `./gradlew run`, deberías ver:

### 1. **Mensajes de Inicio**
```
[YYYY-MM-DD HH:MM:SS] [INFO] === SERVIDOR DONCEY KONG JR ===
[YYYY-MM-DD HH:MM:SS] [INFO] Iniciando componentes...
[YYYY-MM-DD HH:MM:SS] [INFO] GameManager inicializado
[YYYY-MM-DD HH:MM:SS] [INFO] GameLoop iniciado (20 ticks/segundo)
[YYYY-MM-DD HH:MM:SS] [INFO] Servidor iniciado en puerto 5000
[YYYY-MM-DD HH:MM:SS] [INFO] Esperando jugadores...
[YYYY-MM-DD HH:MM:SS] [INFO] Servidor completamente iniciado. Listo para recibir conexiones.
[YYYY-MM-DD HH:MM:SS] [INFO] Escribe comandos en la consola de administración (help para ayuda)
```

### 2. **Consola de Administración**
```
=== CONSOLA DE ADMINISTRACIÓN ===
Comandos disponibles:
  create red <liana> <y>     - Crea un cocodrilo rojo
  create blue <liana> <y>    - Crea un cocodrilo azul
  fruit add <liana> <y> <points> - Agrega una fruta
  fruit del <liana> <y>      - Elimina una fruta
  list entities              - Lista todas las entidades
  level up                   - Sube el nivel
  pause                     - Pausa el juego
  resume                    - Reanuda el juego
  help                      - Muestra esta ayuda
  quit                      - Cierra la consola
==================================

> 
```

### 3. **Cuando un Cliente se Conecta**
```
[YYYY-MM-DD HH:MM:SS] [INFO] Cliente conectado desde 127.0.0.1
[YYYY-MM-DD HH:MM:SS] [INFO] Jugador J1 registrado
```

### 4. **Ejemplos de Comandos en la Consola**

#### Crear un cocodrilo rojo en la liana 1, posición Y=3:
```
> create red 1 3
✓ Cocodrilo rojo creado en liana 1, y=3.0
[YYYY-MM-DD HH:MM:SS] [INFO] Cocodrilo rojo creado en liana 1, y=3.0
```

#### Agregar una fruta:
```
> fruit add 2 4 50
✓ Fruta agregada en liana 2, y=4.0, puntos=50
[YYYY-MM-DD HH:MM:SS] [INFO] Fruta creada en liana 2, y=4.0, puntos=50
```

#### Listar todas las entidades:
```
> list entities
=== ENTIDADES DEL JUEGO ===
Jugadores: 1
  - J1 (liana=0, y=5.00, vidas=3, puntos=0)
Cocodrilos: 1
  - CROJO_1 (ROJO, liana=1, y=3.00)
Frutas: 1
  - F_1 (liana=2, y=4.00, puntos=50)
```

#### Subir de nivel:
```
> level up
✓ Nivel subido a 2
[YYYY-MM-DD HH:MM:SS] [INFO] Nivel subido a 2. Velocidad: 1.3
```

## 🔌 Probar con un Cliente

### Ejemplo de mensaje JSON que envía un cliente:

**Conexión:**
```json
{"type":"CONNECT","id":"J1"}
```

**Input del jugador:**
```json
{"type":"INPUT","id":"J1","action":"MOVE_UP"}
```

### Respuesta del servidor (Estado del juego):
```json
{
  "type": "STATE",
  "data": {
    "tick": 125,
    "level": 1,
    "speedMultiplier": 1.0,
    "paused": false,
    "players": [
      {
        "id": "J1",
        "x": 0.0,
        "y": 4.5,
        "liana": 0,
        "lives": 3,
        "score": 0,
        "active": true
      }
    ],
    "crocodiles": [
      {
        "id": "CROJO_1",
        "kind": "ROJO",
        "liana": 1,
        "y": 3.0
      }
    ],
    "fruits": [
      {
        "id": "F_1",
        "liana": 2,
        "y": 4.0,
        "points": 50
      }
    ]
  }
}
```

### Evento cuando se recoge una fruta:
```json
{
  "type": "EVENT",
  "name": "FRUIT_TAKEN",
  "payload": {
    "playerId": "J1",
    "points": 50
  }
}
```

## 🛠️ Comandos Útiles

### Ejecutar el servidor:
```bash
./gradlew run
```

### Compilar sin ejecutar:
```bash
./gradlew build
```

### Limpiar y recompilar:
```bash
./gradlew clean build
```

## 📝 Notas

- El servidor escucha en el **puerto 5000** por defecto
- Máximo **2 jugadores** simultáneos
- El juego se actualiza a **20 ticks por segundo**
- Los logs incluyen timestamps automáticos
- La consola de administración permite controlar el juego en tiempo real

## ⚠️ Solución de Problemas

Si ves errores de conexión, verifica:
1. Que el puerto 5000 no esté en uso: `netstat -an | findstr 5000`
2. Que el firewall permita conexiones en el puerto 5000
3. Que el cliente esté conectándose a la IP correcta del servidor

