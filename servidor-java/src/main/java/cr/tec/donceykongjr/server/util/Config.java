package cr.tec.donceykongjr.server.util;

/**
 * Configuracion global del servidor.
 * Centraliza valores configurables como puertos, limites y constantes del juego.
 */
public class Config {
    /** Puerto por defecto del servidor TCP */
    public static final int PUERTO_DEFAULT = 5555;

    /** Maximo numero de jugadores simultaneos */
    public static final int MAX_JUGADORES = 2;

    /** Maximo numero de espectadores por jugador */
    public static final int MAX_ESPECTADORES_POR_JUGADOR = 2;

    /** Ticks por segundo del juego (FPS del servidor) */
    public static final int TICKS_POR_SEGUNDO = 20;

    /** Intervalo entre ticks en milisegundos */
    public static final long INTERVALO_TICK_MS = 1000 / TICKS_POR_SEGUNDO;

    /** Velocidad base de movimiento de entidades */
    public static final double VELOCIDAD_BASE = 1.0;

    /** Vidas iniciales de cada jugador */
    public static final int VIDAS_INICIALES = 3;

    /** Puntos base por fruta */
    public static final int PUNTOS_FRUTA_BASE = 50;

    /** Coordenadas y fisica del jugador */
    public static final double JUGADOR_Y_MIN = 0.0;
    public static final double JUGADOR_Y_MAX = 500.0;

    // Nivel del agua (peligro mortal)
    public static final double NIVEL_AGUA = 485.0;              // Por debajo de esto es suelo seguro
    public static final double ALTURA_SUELO = 475.0;            // Altura de la plataforma de suelo inferior

    // Velocidades y aceleración horizontal (MEJORADO)
    public static final double JUGADOR_VEL_HORIZONTAL_MAX = 1.5;      // Velocidad máxima horizontal (caminar suave, no saltar bloques)
    public static final double JUGADOR_ACELERACION_HORIZONTAL = 12.0; // Aceleración gradual en unidades/s²
    public static final double JUGADOR_FRICCION = 10.0;               // Desaceleración al soltar tecla
    public static final double JUGADOR_CONTROL_AEREO_MULT = 0.6;      // Control horizontal en el aire (60% del normal)

    // Velocidades verticales
    public static final double JUGADOR_VEL_LIANA = 180.0;
    public static final double JUGADOR_VEL_SALTO = 220.0;             // Velocidad inicial salto (reducida de 260.0)
    public static final double JUGADOR_VEL_SALTO_LIANA = 220.0;
    public static final double JUGADOR_GRAVEDAD = 520.0;

    // Mecánicas de agarre y colisión
    public static final double JUGADOR_DISTANCIA_ENGANCHE = 0.35;
    public static final double JUGADOR_GRAB_BUFFER = 0.30;
    public static final double JUGADOR_CAMBIO_LIANA_COOLDOWN = 0.25; // Cooldown entre cambios de liana (250ms)
    public static final double JUGADOR_DELTA_Y_COCODRILO = 30.0;
    public static final double JUGADOR_DELTA_Y_FRUTA = 24.0;
    public static final double JUGADOR_TIEMPO_CELEBRACION = 1.5;

    // Spawn inicial
    public static final double JUGADOR_SPAWN_Y = ALTURA_SUELO;  // En el suelo seguro (475.0)
    public static final int JUGADOR_SPAWN_LIANA = 1;            // Columna 1 (segunda liana)
    public static final double JUGADOR_SPAWN_X = JUGADOR_SPAWN_LIANA;

    /** Reglas de objetivo/rescate - Columna 7 (plataforma de victoria) */
    public static final double OBJETIVO_Y = 60.0;        // Parte superior
    public static final int OBJETIVO_LIANA = 7;          // Columna 8 (índice 7)

    /** Escalado de dificultad tras cada rescate */
    public static final double COCODRILO_INCREMENTO_DIFICULTAD = 1.10;

    /** Constructor privado para evitar instanciacion */
    private Config() {}
}
