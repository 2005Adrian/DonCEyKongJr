package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Estados posibles de un cocodrilo en el sistema.
 */
public enum EstadoCocodrilo {
    /**
     * El cocodrilo está activo y moviéndose normalmente.
     */
    ACTIVO,

    /**
     * El cocodrilo ha sido eliminado del sistema (cayó fuera de límites, etc.).
     */
    ELIMINADO
}
