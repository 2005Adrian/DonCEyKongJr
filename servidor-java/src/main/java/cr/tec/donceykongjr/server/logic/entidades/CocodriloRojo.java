package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Cocodrilo rojo que sube y baja en una sola liana.
 * Se mueve verticalmente dentro de un rango limitado, invirtiendo dirección al tocar límites.
 *
 * Comportamiento:
 * - Movimiento vertical dentro de [alturaMin, alturaMax]
 * - Al tocar límite superior o inferior, invierte dirección automáticamente
 * - Nunca sale de la liana ni se elimina
 * - Dirección: +1 (sube hacia arriba, Y decrece) o -1 (baja hacia abajo, Y crece)
 */
public class CocodriloRojo extends Cocodrilo {
    private double alturaMin; // Límite inferior (Y mínimo)
    private double alturaMax; // Límite superior (Y máximo)

    /**
     * Constructor completo con límites explícitos.
     *
     * @param id Identificador único
     * @param lianaId ID de la liana
     * @param yInicial Posición Y inicial (debe estar entre alturaMin y alturaMax)
     * @param velocidadBase Velocidad base
     * @param direccion Dirección inicial: +1 (sube) o -1 (baja)
     * @param alturaMin Límite inferior de movimiento
     * @param alturaMax Límite superior de movimiento
     */
    public CocodriloRojo(String id, int lianaId, double yInicial, double velocidadBase,
                         int direccion, double alturaMin, double alturaMax) {
        super(id, TipoCocodrilo.ROJO, lianaId, yInicial, velocidadBase, direccion);

        if (alturaMin >= alturaMax) {
            throw new IllegalArgumentException("alturaMin debe ser menor que alturaMax");
        }
        if (yInicial < alturaMin || yInicial > alturaMax) {
            throw new IllegalArgumentException("yInicial debe estar entre alturaMin y alturaMax");
        }

        this.alturaMin = alturaMin;
        this.alturaMax = alturaMax;
    }

    /**
     * Constructor legacy para compatibilidad.
     * Usa un rango de movimiento relativo a la posición inicial.
     */
    @Deprecated
    public CocodriloRojo(String id, double x, double y, int liana, double velocidad) {
        super(id, x, y, liana, TipoCocodrilo.ROJO, velocidad);
        this.alturaMin = y - 2.0; // Rango de movimiento legacy
        this.alturaMax = y + 2.0;
        this.direccion = -1; // Empieza bajando
    }

    /**
     * Implementa el movimiento del cocodrilo rojo.
     * Se mueve verticalmente aplicando la dirección y velocidad,
     * rebotando en los límites.
     *
     * @param dt Delta time (intervalo fijo de tiempo)
     */
    @Override
    public void mover(double dt) {
        if (estado != EstadoCocodrilo.ACTIVO) return;

        // Nota: En coordenadas de pantalla, Y crece hacia abajo
        // direccion = +1 significa "subir" (decrecer Y)
        // direccion = -1 significa "bajar" (crecer Y)

        // Calcular nuevo Y aplicando velocidad y dirección
        // Dirección +1 (sube) = Y decrece
        // Dirección -1 (baja) = Y crece
        double desplazamiento = velocidadBase * dt * (-direccion);
        y += desplazamiento;

        // Verificar límites y rebotar
        if (y <= alturaMin) {
            y = alturaMin;
            invertirDireccion(); // Cambia de subir a bajar
        } else if (y >= alturaMax) {
            y = alturaMax;
            invertirDireccion(); // Cambia de bajar a subir
        }
    }

    // Getters

    public double getAlturaMin() {
        return alturaMin;
    }

    public double getAlturaMax() {
        return alturaMax;
    }
}

