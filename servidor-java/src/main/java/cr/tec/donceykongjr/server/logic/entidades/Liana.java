package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Representa una liana en el juego.
 * Las lianas son estructuras verticales por las que pueden subir/bajar jugadores y cocodrilos.
 */
public class Liana extends Entidad {
    private int altura; // Altura de la liana
    
    /**
     * Crea una nueva liana.
     */
    public Liana(String id, double x, double y, int liana, int altura) {
        super(id, x, y, liana);
        this.altura = altura;
    }
    
    @Override
    public void actualizar(double deltaTime) {
        // Las lianas son estáticas, no necesitan actualización
    }
    
    /**
     * Verifica si una posición Y está dentro de los límites de la liana.
     */
    public boolean estaEnRango(double y) {
        return y >= this.y && y <= (this.y + altura);
    }
    
    public int getAltura() {
        return altura;
    }
}

