# ANALISIS EXHAUSTIVO DE LA ARQUITECTURA DEL SERVIDOR JAVA

## 1. JERARQUIA DE CLASES DE ENTIDADES

### Arbol de Herencia

```
Entidad (abstract)
├── Jugador
├── Cocodrilo (abstract)
│   ├── CocodriloRojo
│   └── CocodriloAzul
├── Fruta
└── Liana
```

Base: Todos heredan id, x, y, liana

## 2. JUGADOR - MAQUINA DE ESTADOS COMPLETA

Estados:
- SUELO: En reposo, puede saltar
- SALTANDO: En aire, puede agarrar liana
- EN_LIANA: Agarrado, puede subir/bajar/saltar
- MUERTO: Sin vidas
- CELEBRANDO: Completando rescate

Atributos:
- vx, vy: velocidades
- lianaId: NULL si no agarrado, INTEGER si en liana
- score, vidas: gameplay
- grabBufferTimer: ventana para agarrar lianas (0.30s)

Fisica:
- GRAVEDAD = 520.0 unidades/s²
- VEL_SALTO = 260.0 (hacia arriba)
- VEL_LIANA = 180.0 (subir/bajar)
- VEL_HORIZONTAL = 6.0

Input Processing:
- Thread-safe con inputLock
- Acumula: LEFT, RIGHT, UP, DOWN, JUMP, GRAB
- Consume en cada tick

## 3. COCODRILOS

Base Cocodrilo:
- tipo: ROJO o AZUL
- lianaId: en qué liana
- velocidadBase: velocidad sin multiplicador
- direccion: +1 o -1
- estado: ACTIVO o ELIMINADO

CocodriloRojo:
- Sube/baja dentro de [alturaMin, alturaMax]
- Rebota en límites (invierte dirección)
- NUNCA se elimina

CocodriloAzul:
- Siempre baja (dirección forzada -1)
- Se elimina cuando y >= limiteInferior
- No rebota

## 4. GAMEMANAGER - ORQUESTADOR CENTRAL

Ciclo cada 50ms (20 TPS):
1. Sincroniza cocodrilos desde motor
2. Actualiza jugadores
3. Detecta colisiones (jugador-cocodrilo, frutas)
4. Verifica objetivos (rescate)
5. Gestiona celebración
6. Notifica clientes

Detecciones:
- Colisión: misma liana + |deltaY| <= 30.0
- Fruta: misma liana + |deltaY| <= 24.0
- Abismo: y < 0.0
- Objetivo: liana 2 + y <= 60.0

## 5. MOTOR DE COCODRILOS (Subsistema Independiente)

Corre cada 100ms (10 TPS) en hilo separado:
- Fixed timestep determinista
- dt efectivo = 0.1 * factorDificultad
- Thread-safe con ConcurrentHashMap + sincronización
- Cola de comandos (crear/eliminar)

API:
- crearCocodriloRojo(lianaId, y, velocidad, dirección)
- crearCocodriloAzul(lianaId, y, velocidad)
- incrementarDificultad(multiplicador)
- getSnapshot(): SnapshotSistemaCocodrilos

## 6. SNAPSHOTS (Inmutables)

SnapshotJugador: estado completo del jugador en un momento
SnapshotCocodrilo: estado de un cocodrilo
SnapshotSistemaCocodrilos: todos los cocodrilos + métricas

Propósito: Thread-safe, sin race conditions

## 7. COMUNICACION CLIENTE-SERVIDOR

ManejadorCliente por conexión:
- Lee JSON del socket
- Interpreta Mensaje (INPUT, STATE, EVENT, CONNECT, DISCONNECT)
- Se registra como Observer del GameManager
- Envía estado cada tick

Formato Mensaje:
- type: TipoMensaje enum
- id: jugador/cliente
- action: acción (LEFT, JUMP, etc)
- payload: datos adicionales
- data: mapa de estado completo

STATE enviado cada tick:
- jugadores: [id, x, y, vx, vy, liana, lianaId, state, facing, lives, score, active, celebrating]
- crocodiles: [id, kind, liana, y]
- fruits: [id, liana, y, points]
- tick, speedMultiplier, paused, celebrationPending, celebrationTimer

EVENT tipos: FRUIT_TAKEN, PLAYER_HIT, PLAYER_ELIMINATED, PLAYER_WIN

## 8. SISTEMA DE COORDENADAS

X (horizontal): 0.0 a 4.0 (indices de liana como floats)
Y (vertical): 0.0 a 500.0
- Y=0 arriba (rescate)
- Y=60 objetivo
- Y=480 spawn
- Y>500 abismo

Colisión en Entidad:
- distancia euclidiana < 0.5
- misma liana

Pero en GameManager se usa:
- |deltaY| < umbrales específicos

## 9. CONFIG - CONSTANTES GLOBALES

Timing:
- TICKS_POR_SEGUNDO = 20 (GameLoop)
- INTERVALO_TICK_MS = 50

Jugador:
- VIDAS_INICIALES = 3
- JUGADOR_VEL_HORIZONTAL = 6.0
- JUGADOR_VEL_LIANA = 180.0
- JUGADOR_VEL_SALTO = 260.0
- JUGADOR_GRAVEDAD = 520.0
- JUGADOR_DISTANCIA_ENGANCHE = 0.35
- JUGADOR_GRAB_BUFFER = 0.30
- JUGADOR_DELTA_Y_COCODRILO = 30.0
- JUGADOR_DELTA_Y_FRUTA = 24.0
- JUGADOR_TIEMPO_CELEBRACION = 1.5
- OBJETIVO_LIANA = 2, OBJETIVO_Y = 60.0

Dificultad:
- COCODRILO_INCREMENTO_DIFICULTAD = 1.10 (por rescate)

## 10. LIMITACIONES Y AREAS DE REFACTORIZACION

1. Mezcla de coords: X flotante (lianas), Y píxeles
   → Definir sistema formal o documentar bien

2. Sincronización GameManager-Motor:
   Crea instancias temporales de Cocodrilo cada tick
   → Trabajar directamente con snapshots

3. lianaId puede ser NULL:
   Causa confusion en logica de colisiones
   → Consolidar o aclarar diferencia con render liana

4. Input processing:
   Actualmente funciona bien pero podría usar Queue
   → Aceptable, no es problema urgente

5. Factor dificultad no sincronizado:
   Motor corre a 10 TPS independiente
   → Documentado pero es limite diseño

6. Telemeter ia incompleta:
   Solo cocodrilos, no jugadores/colisiones
   → Expandir si se necesita debug

7. Pausa global desincronizada:
   GameLoop pausa pero Motor sigue
   → Sincronizar o documentar

8. NO hay sistema de tiles formal:
   Todo es posicional flotante
   → Aceptable pero limita ciertas mecanicas

9. Colisiones simplistas:
   Sin predicción de futuros, sin swept volumes
   → OK con dt fijo pequeño, pero documentar

10. Factory parcialmente usado:
    MotorCocodrilos genera IDs propios
    → Consolidar o documentar separación clara

## 11. FLUJO COMPLETO

### Inicializacion
GameManager.__init__:
- 5 lianas (indices 0-4, y [0, 500])
- MotorCocodrilos(dt=0.1s)
- 2 CocodrilosRojos iniciales
- 2-4 Frutas random
- Inicia motor

GameLoop inicia 20 TPS

### Conexion Cliente
Socket → ManejadorCliente → CONNECT
→ gameManager.agregarJugador()
→ Crea Jugador o registra espectador
→ Envía STATE inicial

### Game Loop (cada 50ms)
- Sincroniza cocodrilos del motor
- Actualiza jugadores
- Detecta colisiones/frutas/objetivos
- Notifica clientes

### Motor (cada 100ms)
- Procesa comandos
- Actualiza cocodrilos con dt * factor
- Limpia eliminados

### Input
Client INPUT → ManejadorCliente → procesarInput()
→ Jugador.registrarInput(accion)
→ Siguiente tick: consume en actualizar()

### Rescate
Jugador en (liana=2, y<=60)
→ iniciarCelebracion()
→ factorDificultad *= 1.10
→ Timer 1.5s
→ reiniciarManteniendoMapa()

