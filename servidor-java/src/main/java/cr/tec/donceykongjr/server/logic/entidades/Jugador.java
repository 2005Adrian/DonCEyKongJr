package cr.tec.donceykongjr.server.logic.entidades;

import cr.tec.donceykongjr.server.util.Config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representa al jugador (Donkey Kong Jr.) con fisica y maquina de estados completa.
 */
public class Jugador extends Entidad {
    private final Object inputLock = new Object();
    private final Map<Integer, Liana> lianasPorId;
    private final double spawnX;
    private final double spawnY;
    private final int spawnLiana;
    private final int maxLianaIndex;

    private double vx;
    private double vy;
    private EstadoJugador estado;
    private DireccionJugador facing;
    private Integer lianaId;
    private int score;
    private int vidas;
    private boolean activo;
    private double grabBufferTimer;
    private double celebracionTimer;
    private double cambioLianaCooldown; // Cooldown para prevenir múltiples cambios de liana

    private boolean moveLeftRequested;
    private boolean moveRightRequested;
    private boolean moveUpRequested;
    private boolean moveDownRequested;
    private boolean jumpRequested;

    /**
     * Crea un nuevo jugador listo para integrarse al mapa actual.
     *
     * @param id identificador del jugador
     * @param spawnX posicion X inicial (en unidades de liana)
     * @param spawnY posicion Y inicial
     * @param spawnLiana liana de referencia para renderizar
     * @param lianasDisponibles listado de lianas validas
     */
    public Jugador(String id, double spawnX, double spawnY, int spawnLiana, List<Liana> lianasDisponibles) {
        super(id, spawnX, spawnY, spawnLiana);
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnLiana = spawnLiana;
        this.vx = 0;
        this.vy = 0;
        this.estado = EstadoJugador.SUELO;
        this.facing = DireccionJugador.RIGHT;
        this.lianaId = null;
        this.score = 0;
        this.vidas = Config.VIDAS_INICIALES;
        this.activo = true;
        this.lianasPorId = Collections.unmodifiableMap(
                Objects.requireNonNull(lianasDisponibles, "lianasDisponibles no puede ser null")
                        .stream()
                        .collect(Collectors.toMap(Liana::getLiana, l -> l, (a, b) -> a, HashMap::new))
        );
        this.maxLianaIndex = lianasPorId.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        respawnEnSpawn();
    }

    @Override
    public void actualizar(double deltaTime) {
        if (!activo) {
            return;
        }

        actualizarTemporizadores(deltaTime);

        if (estado == EstadoJugador.MUERTO || estado == EstadoJugador.CELEBRANDO) {
            return;
        }

        InputSnapshot input = consumirInputs();

        switch (estado) {
            case SUELO -> actualizarEnSuelo(input, deltaTime);
            case SALTANDO -> actualizarEnSalto(input, deltaTime);
            case EN_LIANA -> actualizarEnLiana(input, deltaTime);
            default -> {
                // Otros estados no aplican movimiento
            }
        }

        clampHorizontal();
        actualizarIndiceVisual();
    }

    private void actualizarTemporizadores(double deltaTime) {
        if (grabBufferTimer > 0) {
            grabBufferTimer = Math.max(0, grabBufferTimer - deltaTime);
        }
        if (estado == EstadoJugador.CELEBRANDO && celebracionTimer > 0) {
            celebracionTimer = Math.max(0, celebracionTimer - deltaTime);
        }
        if (cambioLianaCooldown > 0) {
            cambioLianaCooldown = Math.max(0, cambioLianaCooldown - deltaTime);
        }
    }

    private void actualizarEnSuelo(InputSnapshot input, double deltaTime) {
        // Verificar si el jugador está realmente sobre suelo sólido
        if (!estaEnSuelo()) {
            // No hay suelo debajo, comenzar a caer
            estado = EstadoJugador.SALTANDO;
            vy = 0; // Comenzar a caer desde velocidad 0
            return;
        }

        // Movimiento horizontal en el suelo (caminar)
        aplicarMovimientoHorizontal(input.horizontalDirection(), deltaTime, false); // En suelo: control normal
        vy = 0;

        // Mantener al jugador en el nivel del suelo
        y = Config.ALTURA_SUELO;

        // Si presiona UP o DOWN y hay una liana cerca, agarrarla automáticamente
        double verticalDir = input.verticalDirection();
        if (verticalDir != 0) {
            Integer candidata = encontrarLianaDisponible();
            if (candidata != null) {
                Liana liana = lianasPorId.get(candidata);
                if (liana != null && liana.estaEnRango(y)) {
                    estado = EstadoJugador.EN_LIANA;
                    lianaId = candidata;
                    x = candidata;
                    vx = 0;
                    limitarYALiana();
                    actualizarIndiceVisual();
                    return; // Ya agarró la liana, no procesar salto
                }
            }
        }

        if (input.jump) {
            iniciarSalto();
        }
    }

    /**
     * Verifica si el jugador está sobre suelo sólido.
     * Por ahora, el suelo existe en toda la extensión horizontal en Y=ALTURA_SUELO.
     */
    private boolean estaEnSuelo() {
        // El suelo es una plataforma completa en Y=475 que cubre todas las columnas (0-7)
        return y >= Config.ALTURA_SUELO - 5 && y <= Config.ALTURA_SUELO + 5;
    }

    private void actualizarEnSalto(InputSnapshot input, double deltaTime) {
        aplicarMovimientoHorizontal(input.horizontalDirection(), deltaTime, true); // En aire: control limitado

        vy += Config.JUGADOR_GRAVEDAD * deltaTime;
        y += vy * deltaTime;

        // Si alcanza el suelo seguro, aterriza
        if (y >= Config.ALTURA_SUELO && vy > 0) {
            y = Config.ALTURA_SUELO;
            estado = EstadoJugador.SUELO;
            vy = 0;
        }

        // Intentar agarrar liana durante el salto
        if (grabBufferTimer > 0) {
            intentarAgarrarLiana();
        }

        // NOTA: Si cae más allá de ALTURA_SUELO, el GameManager detectará la caída al agua
    }

    private void actualizarEnLiana(InputSnapshot input, double deltaTime) {
        // Movimiento vertical en la liana actual
        double verticalDir = input.verticalDirection();
        if (verticalDir != 0) {
            vy = verticalDir * Config.JUGADOR_VEL_LIANA;
            y += vy * deltaTime;
            limitarYALiana();
        } else {
            vy = 0;
        }

        // Movimiento horizontal: cambiar de liana (con cooldown para prevenir saltos múltiples)
        int horizontalDir = input.horizontalDirection();
        if (horizontalDir != 0 && cambioLianaCooldown <= 0) {
            // Intentar moverse a liana adyacente
            int lianaDestino = lianaId + horizontalDir;
            if (lianasPorId.containsKey(lianaDestino)) {
                Liana destino = lianasPorId.get(lianaDestino);
                if (destino != null && destino.estaEnRango(y)) {
                    lianaId = lianaDestino;
                    x = lianaDestino;
                    limitarYALiana();
                    actualizarIndiceVisual();
                    facing = horizontalDir < 0 ? DireccionJugador.LEFT : DireccionJugador.RIGHT;
                    // Activar cooldown para prevenir cambios múltiples
                    cambioLianaCooldown = Config.JUGADOR_CAMBIO_LIANA_COOLDOWN;
                }
            }
        }

        if (input.jump) {
            soltarLiana();
            iniciarSaltoDesdeLiana();
        }
    }

    /**
     * Aplica movimiento horizontal con aceleración gradual y fricción.
     *
     * @param direccion -1 (izquierda), 0 (ninguna), 1 (derecha)
     * @param deltaTime tiempo transcurrido en segundos
     * @param enAire true si el jugador está en el aire (control limitado)
     */
    private void aplicarMovimientoHorizontal(int direccion, double deltaTime, boolean enAire) {
        // Calcular aceleración (reducida en el aire)
        double aceleracion = Config.JUGADOR_ACELERACION_HORIZONTAL;
        if (enAire) {
            aceleracion *= Config.JUGADOR_CONTROL_AEREO_MULT; // 60% de control en el aire
        }

        if (direccion != 0) {
            // Acelerar en la dirección indicada
            double velocidadObjetivo = direccion * Config.JUGADOR_VEL_HORIZONTAL_MAX;

            // Aceleración gradual hacia la velocidad objetivo
            if (Math.abs(vx - velocidadObjetivo) > 0.1) {
                double deltaV = aceleracion * deltaTime * direccion;
                vx += deltaV;

                // Limitar a velocidad máxima
                if (direccion > 0) {
                    vx = Math.min(vx, Config.JUGADOR_VEL_HORIZONTAL_MAX);
                } else {
                    vx = Math.max(vx, -Config.JUGADOR_VEL_HORIZONTAL_MAX);
                }
            } else {
                vx = velocidadObjetivo;
            }

            facing = direccion < 0 ? DireccionJugador.LEFT : DireccionJugador.RIGHT;
        } else {
            // Sin input: aplicar fricción para desacelerar
            if (Math.abs(vx) > 0.1) {
                double friccion = Config.JUGADOR_FRICCION * deltaTime;
                if (vx > 0) {
                    vx = Math.max(0, vx - friccion);
                } else {
                    vx = Math.min(0, vx + friccion);
                }
            } else {
                vx = 0;
            }
        }

        // Aplicar velocidad a posición
        x += vx * deltaTime;
    }

    private void clampHorizontal() {
        x = Math.max(0, Math.min(maxLianaIndex, x));
    }

    private void limitarYALiana() {
        Liana liana = obtenerLianaActual();
        if (liana != null) {
            y = liana.limitarY(y);
        } else {
            y = Math.max(Config.JUGADOR_Y_MIN, Math.min(Config.JUGADOR_Y_MAX, y));
        }
    }

    private void iniciarSalto() {
        estado = EstadoJugador.SALTANDO;
        vy = -Config.JUGADOR_VEL_SALTO;
    }

    private void iniciarSaltoDesdeLiana() {
        estado = EstadoJugador.SALTANDO;
        vy = -Config.JUGADOR_VEL_SALTO_LIANA;
    }

    private void soltarLiana() {
        lianaId = null;
        grabBufferTimer = 0;
        actualizarIndiceVisual();
    }

    private void intentarAgarrarLiana() {
        if (estado != EstadoJugador.SALTANDO) {
            return;
        }

        Integer candidata = encontrarLianaDisponible();
        if (candidata != null) {
            Liana liana = lianasPorId.get(candidata);
            if (liana != null && liana.estaEnRango(y)) {
                estado = EstadoJugador.EN_LIANA;
                lianaId = candidata;
                x = candidata;
                vx = 0;
                vy = 0;
                grabBufferTimer = 0;
                limitarYALiana();
                actualizarIndiceVisual();
            }
        }
    }

    private Integer encontrarLianaDisponible() {
        double mejorDistancia = Double.MAX_VALUE;
        Integer candidata = null;

        for (Map.Entry<Integer, Liana> entry : lianasPorId.entrySet()) {
            double distancia = Math.abs(x - entry.getKey());
            if (distancia <= Config.JUGADOR_DISTANCIA_ENGANCHE && distancia < mejorDistancia) {
                candidata = entry.getKey();
                mejorDistancia = distancia;
            }
        }

        return candidata;
    }

    private Liana obtenerLianaActual() {
        return lianaId == null ? null : lianasPorId.get(lianaId);
    }

    private void actualizarIndiceVisual() {
        int renderIndex = (int) Math.round(Math.max(0, Math.min(maxLianaIndex, x)));
        super.setLiana(renderIndex);
    }

    /**
     * Registra un input recibido desde la red para procesarlo en el siguiente tick.
     *
     * @param accion comando recibido
     * @return true si se acepto, false si se ignoro por estado invalido
     */
    public boolean registrarInput(String accion) {
        if (!activo || estado == EstadoJugador.MUERTO || estado == EstadoJugador.CELEBRANDO) {
            return false;
        }
        if (accion == null) {
            return false;
        }

        String comando = accion.trim().toUpperCase(Locale.ROOT);
        synchronized (inputLock) {
            switch (comando) {
                case "LEFT":
                case "MOVE_LEFT":
                    moveLeftRequested = true;
                    return true;
                case "RIGHT":
                case "MOVE_RIGHT":
                    moveRightRequested = true;
                    return true;
                case "MOVE_UP":
                case "UP":
                    moveUpRequested = true;
                    return true;
                case "MOVE_DOWN":
                case "DOWN":
                    moveDownRequested = true;
                    return true;
                case "JUMP":
                    jumpRequested = true;
                    return true;
                case "GRAB":
                    grabBufferTimer = Config.JUGADOR_GRAB_BUFFER;
                    return true;
                default:
                    return false;
            }
        }
    }

    private InputSnapshot consumirInputs() {
        synchronized (inputLock) {
            InputSnapshot snapshot = new InputSnapshot(
                    moveLeftRequested,
                    moveRightRequested,
                    moveUpRequested,
                    moveDownRequested,
                    jumpRequested
            );

            moveLeftRequested = false;
            moveRightRequested = false;
            moveUpRequested = false;
            moveDownRequested = false;
            jumpRequested = false;

            return snapshot;
        }
    }

    /**
     * Reduce una vida. Retorna true si el jugador aun dispone de vidas para continuar.
     */
    public boolean perderVida() {
        if (!activo) {
            return false;
        }

        vidas = Math.max(0, vidas - 1);
        estado = EstadoJugador.MUERTO;
        vx = 0;
        vy = 0;
        lianaId = null;
        grabBufferTimer = 0;
        cambioLianaCooldown = 0;
        actualizarIndiceVisual();

        if (vidas <= 0) {
            activo = false;
            return false;
        }
        return true;
    }

    /**
     * Reposiciona al jugador en el punto de reaparicion conservando puntaje.
     */
    public void respawnEnSpawn() {
        x = spawnX;
        y = spawnY;
        vx = 0;
        vy = 0;
        lianaId = null;
        estado = EstadoJugador.SUELO;
        grabBufferTimer = 0;
        celebracionTimer = 0;
        cambioLianaCooldown = 0;
        if (vidas > 0) {
            activo = true;
        }
        actualizarIndiceVisual();
    }

    public void agregarPuntos(int puntos) {
        if (puntos > 0) {
            score += puntos;
        }
    }

    public void iniciarCelebracion() {
        estado = EstadoJugador.CELEBRANDO;
        celebracionTimer = Config.JUGADOR_TIEMPO_CELEBRACION;
        vx = 0;
        vy = 0;
        cambioLianaCooldown = 0;
    }

    public boolean celebracionListaParaReinicio() {
        return estado == EstadoJugador.CELEBRANDO && celebracionTimer <= 0;
    }

    public boolean estaCelebrando() {
        return estado == EstadoJugador.CELEBRANDO;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public EstadoJugador getEstado() {
        return estado;
    }

    public DireccionJugador getFacing() {
        return facing;
    }

    public Integer getLianaId() {
        return lianaId;
    }

    public int getRenderLiana() {
        return super.getLiana();
    }

    public int getPuntaje() {
        return score;
    }

    public int getVidas() {
        return vidas;
    }

    public boolean isActivo() {
        return activo;
    }

    public double getCelebracionTimer() {
        return celebracionTimer;
    }

    private static final class InputSnapshot {
        private final boolean left;
        private final boolean right;
        private final boolean up;
        private final boolean down;
        private final boolean jump;

        private InputSnapshot(boolean left, boolean right, boolean up, boolean down, boolean jump) {
            this.left = left;
            this.right = right;
            this.up = up;
            this.down = down;
            this.jump = jump;
        }

        int horizontalDirection() {
            if (left && right) {
                return 0;
            }
            if (left) {
                return -1;
            }
            if (right) {
                return 1;
            }
            return 0;
        }

        double verticalDirection() {
            if (up && down) {
                return 0;
            }
            if (up) {
                return -1;
            }
            if (down) {
                return 1;
            }
            return 0;
        }
    }
}
