package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Clase base abstracta para todas las entidades del juego.
 * Define la estructura común de posición, ID y comportamiento básico.
 */
public abstract class Entidad {
    protected String id;
    protected double x;
    protected double y;
    protected int liana; // Número de liana en la que se encuentra (0 = izquierda, 1 = centro, etc.)
    
    /**
     * Constructor base para entidades.
     */
    public Entidad(String id, double x, double y, int liana) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.liana = liana;
    }
    
    /**
     * Actualiza el estado de la entidad en cada tick del juego.
     * @param deltaTime tiempo transcurrido desde el último tick
     */
    public abstract void actualizar(double deltaTime);
    
    /**
     * Verifica si hay colisión con otra entidad.
     */
    public boolean colisionaCon(Entidad otra) {
        if (this == otra) return false;
        // Colisión simple basada en distancia
        double distancia = Math.sqrt(Math.pow(this.x - otra.x, 2) + Math.pow(this.y - otra.y, 2));
        return distancia < 0.5 && this.liana == otra.liana;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public int getLiana() {
        return liana;
    }
    
    public void setLiana(int liana) {
        this.liana = liana;
    }
}

