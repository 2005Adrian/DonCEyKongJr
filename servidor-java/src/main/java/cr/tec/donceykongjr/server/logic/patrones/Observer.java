package cr.tec.donceykongjr.server.logic.patrones;

/**
 * Interfaz para el patrón Observer.
 * Permite que los objetos sean notificados de cambios en los sujetos.
 */
public interface Observer {
    /**
     * Método llamado cuando el sujeto notifica un cambio.
     * @param dato datos adicionales del cambio (puede ser null)
     */
    void actualizar(Object dato);
}

