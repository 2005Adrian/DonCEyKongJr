package cr.tec.donceykongjr.server.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para logging simple del servidor.
 * Proporciona métodos estáticos para registrar eventos con timestamps.
 */
public class LoggerUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Registra un mensaje informativo.
     */
    public static void info(String mensaje) {
        System.out.println("[" + LocalDateTime.now().format(FORMATTER) + "] [INFO] " + mensaje);
    }
    
    /**
     * Registra un mensaje de advertencia.
     */
    public static void warning(String mensaje) {
        System.out.println("[" + LocalDateTime.now().format(FORMATTER) + "] [WARN] " + mensaje);
    }
    
    /**
     * Registra un mensaje de error.
     */
    public static void error(String mensaje) {
        System.err.println("[" + LocalDateTime.now().format(FORMATTER) + "] [ERROR] " + mensaje);
    }
    
    /**
     * Registra un mensaje de depuración.
     */
    public static void debug(String mensaje) {
        System.out.println("[" + LocalDateTime.now().format(FORMATTER) + "] [DEBUG] " + mensaje);
    }
}

