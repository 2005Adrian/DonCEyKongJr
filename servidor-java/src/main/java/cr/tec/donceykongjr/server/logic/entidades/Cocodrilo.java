package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Clase base abstracta para los cocodrilos del juego.
 * Define el comportamiento común de los cocodrilos.
 */
public abstract class Cocodrilo extends Entidad {
    protected double velocidad;
    protected boolean activo;
    protected TipoCocodrilo tipo;
    
    /**
     * Tipos de cocodrilos disponibles.
     */
    public enum TipoCocodrilo {
        ROJO, AZUL
    }
    
    /**
     * Constructor base para cocodrilos.
     */
    public Cocodrilo(String id, double x, double y, int liana, TipoCocodrilo tipo, double velocidad) {
        super(id, x, y, liana);
        this.tipo = tipo;
        this.velocidad = velocidad;
        this.activo = true;
    }
    
    @Override
    public abstract void actualizar(double deltaTime);
    
    /**
     * Desactiva el cocodrilo (cuando es eliminado).
     */
    public void desactivar() {
        this.activo = false;
    }
    
    public TipoCocodrilo getTipo() {
        return tipo;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public double getVelocidad() {
        return velocidad;
    }
    
    public void setVelocidad(double velocidad) {
        this.velocidad = velocidad;
    }
}

