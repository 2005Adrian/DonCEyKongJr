package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Cocodrilo azul que baja hasta el final y luego "cae".
 * Se mueve hacia abajo y desaparece al llegar al final.
 */
public class CocodriloAzul extends Cocodrilo {
    private double yFinal; // Posición Y final donde desaparece
    private boolean cayendo;
    
    /**
     * Crea un cocodrilo azul.
     */
    public CocodriloAzul(String id, double x, double y, int liana, double velocidad) {
        super(id, x, y, liana, TipoCocodrilo.AZUL, velocidad);
        this.yFinal = y + 10; // Baja 10 unidades
        this.cayendo = false;
    }
    
    @Override
    public void actualizar(double deltaTime) {
        if (!activo) return;
        
        if (!cayendo) {
            // Baja normalmente
            y += velocidad * deltaTime;
            if (y >= yFinal) {
                cayendo = true;
            }
        } else {
            // Cae más rápido
            y += velocidad * 2 * deltaTime;
            // Si cae demasiado, se desactiva
            if (y > yFinal + 5) {
                activo = false;
            }
        }
    }
}

