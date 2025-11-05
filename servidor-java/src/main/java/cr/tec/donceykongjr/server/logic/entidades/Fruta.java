package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Representa una fruta en el juego.
 * Otorga puntos cuando un jugador la recoge.
 */
public class Fruta extends Entidad {
    private int puntos;
    private boolean recogida;
    
    /**
     * Crea una nueva fruta.
     */
    public Fruta(String id, double x, double y, int liana, int puntos) {
        super(id, x, y, liana);
        this.puntos = puntos;
        this.recogida = false;
    }
    
    @Override
    public void actualizar(double deltaTime) {
        // Las frutas son estáticas hasta ser recogidas
    }
    
    /**
     * Marca la fruta como recogida.
     */
    public void recoger() {
        this.recogida = true;
    }
    
    /**
     * Verifica si la fruta ha sido recogida.
     */
    public boolean isRecogida() {
        return recogida;
    }
    
    public int getPuntos() {
        return puntos;
    }
}

