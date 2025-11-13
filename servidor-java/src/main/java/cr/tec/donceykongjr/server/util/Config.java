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
    public static final double JUGADOR_VEL_HORIZONTAL = 6.0;
    public static final double JUGADOR_VEL_LIANA = 180.0;
    public static final double JUGADOR_VEL_SALTO = 260.0;
    public static final double JUGADOR_VEL_SALTO_LIANA = 220.0;
    public static final double JUGADOR_GRAVEDAD = 520.0;
    public static final double JUGADOR_DISTANCIA_ENGANCHE = 0.35;
    public static final double JUGADOR_GRAB_BUFFER = 0.30;
    public static final double JUGADOR_DELTA_Y_COCODRILO = 30.0;
    public static final double JUGADOR_DELTA_Y_FRUTA = 24.0;
    public static final double JUGADOR_TIEMPO_CELEBRACION = 1.5;
    public static final double JUGADOR_SPAWN_Y = 480.0;
    public static final int JUGADOR_SPAWN_LIANA = 2;
    public static final double JUGADOR_SPAWN_X = JUGADOR_SPAWN_LIANA;

    /** Reglas de objetivo/rescate */
    public static final double OBJETIVO_Y = 60.0;
    public static final int OBJETIVO_LIANA = 2;

    /** Escalado de dificultad tras cada rescate */
    public static final double COCODRILO_INCREMENTO_DIFICULTAD = 1.10;

    /** Constructor privado para evitar instanciacion */
    private Config() {}
}
