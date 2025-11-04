package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Cocodrilo rojo que sube y baja en una sola liana.
 * Se mueve verticalmente en un rango limitado.
 */
public class CocodriloRojo extends Cocodrilo {
    private double yMinimo;
    private double yMaximo;
    private boolean subiendo; // true = subiendo, false = bajando
    
    /**
     * Crea un cocodrilo rojo.
     */
    public CocodriloRojo(String id, double x, double y, int liana, double velocidad) {
        super(id, x, y, liana, TipoCocodrilo.ROJO, velocidad);
        this.yMinimo = y - 2; // Rango de movimiento
        this.yMaximo = y + 2;
        this.subiendo = true;
    }
    
    @Override
    public void actualizar(double deltaTime) {
        if (!activo) return;
        
        if (subiendo) {
            y -= velocidad * deltaTime;
            if (y <= yMinimo) {
                y = yMinimo;
                subiendo = false;
            }
        } else {
            y += velocidad * deltaTime;
            if (y >= yMaximo) {
                y = yMaximo;
                subiendo = true;
            }
        }
    }
}

