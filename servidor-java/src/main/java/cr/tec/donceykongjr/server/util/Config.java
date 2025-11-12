package cr.tec.donceykongjr.server.util;

/**
 * Configuración global del servidor.
 * Centraliza valores configurables como puertos, límites y constantes del juego.
 */
public class Config {
    /** Puerto por defecto del servidor TCP */
    public static final int PUERTO_DEFAULT = 5555;
    
    /** Máximo número de jugadores simultáneos */
    public static final int MAX_JUGADORES = 2;
    
    /** Máximo número de espectadores por jugador */
    public static final int MAX_ESPECTADORES_POR_JUGADOR = 2;
    
    /** Ticks por segundo del juego (FPS del servidor) */
    public static final int TICKS_POR_SEGUNDO = 20;
    
    /** Intervalo entre ticks en milisegundos */
    public static final long INTERVALO_TICK_MS = 1000 / TICKS_POR_SEGUNDO;
    
    /** Velocidad base de movimiento de entidades */
    public static final double VELOCIDAD_BASE = 1.0;
    
    /** Velocidad de incremento por nivel */
    public static final double MULTIPLICADOR_VELOCIDAD_NIVEL = 0.3;
    
    /** Posición objetivo (altura máxima) para completar nivel */
    public static final int POSICION_OBJETIVO_Y = 0;
    
    /** Vidas iniciales de cada jugador */
    public static final int VIDAS_INICIALES = 999999;
    
    /** Puntos base por fruta */
    public static final int PUNTOS_FRUTA_BASE = 50;
    
    /** Constructor privado para evitar instanciación */
    private Config() {}
}

