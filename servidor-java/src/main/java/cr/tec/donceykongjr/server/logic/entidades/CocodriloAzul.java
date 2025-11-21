package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Cocodrilo azul que desciende verticalmente por una liana.
 * Al sobrepasar el límite inferior se elimina (simula caer fuera de la liana).
 *
 * Comportamiento:
 * - Siempre desciende (dirección forzada a -1)
 * - Se mueve hacia abajo continuamente
 * - Al cruzar el límite inferior (alturaMax), se marca como ELIMINADO
 * - No rebota ni invierte dirección
 */
public class CocodriloAzul extends Cocodrilo {
    private double limiteInferior; // Límite inferior donde se elimina

    /**
     * Constructor completo con límite inferior explícito.
     *
     * @param id Identificador único
     * @param lianaId ID de la liana
     * @param yInicial Posición Y inicial
     * @param velocidadBase Velocidad base de caída
     * @param limiteInferior Límite inferior donde el cocodrilo se elimina
     */
    public CocodriloAzul(String id, int lianaId, double yInicial,
                         double velocidadBase, double limiteInferior) {
        super(id, TipoCocodrilo.AZUL, lianaId, yInicial, velocidadBase, -1);

        // Forzar dirección a -1 (siempre baja)
        this.direccion = -1;
        this.limiteInferior = limiteInferior;
    }

    /**
     * Constructor legacy para compatibilidad.
     */
    @Deprecated
    public CocodriloAzul(String id, double x, double y, int liana, double velocidad) {
        super(id, x, y, liana, TipoCocodrilo.AZUL, velocidad);
        this.direccion = -1; // Siempre baja
        this.limiteInferior = y + 10.0; // Límite legacy
    }

    /**
     * Implementa el movimiento del cocodrilo azul.
     * Desciende continuamente y se elimina al cruzar el límite inferior.
     *
     * @param dt Delta time (intervalo fijo de tiempo)
     */
    @Override
    public void mover(double dt) {
        if (estado != EstadoCocodrilo.ACTIVO) return;

        // Siempre baja (Y crece hacia abajo)
        y += velocidadBase * dt;

        // Verificar si ha sobrepasado el límite inferior
        if (y >= limiteInferior) {
            eliminar();
        }
    }

    /**
     * Sobrescribe setDireccion para evitar cambiar la dirección.
     * Los cocodrilos azules siempre bajan.
     */
    @Override
    public void setDireccion(int direccion) {
        // No permitir cambiar la dirección en cocodrilos azules
        this.direccion = -1;
    }

    // Getters

    public double getLimiteInferior() {
        return limiteInferior;
    }
}

