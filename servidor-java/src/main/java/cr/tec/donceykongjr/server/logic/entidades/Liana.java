package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Representa una liana en el juego.
 * Las lianas son estructuras verticales por las que pueden subir/bajar jugadores y cocodrilos.
 * Define límites claros (alturaMin, alturaMax) para el movimiento de entidades.
 */
public class Liana extends Entidad {
    private Double alturaMin; // Límite inferior de la liana (Y mínimo)
    private Double alturaMax; // Límite superior de la liana (Y máximo)

    /**
     * Crea una nueva liana con límites específicos.
     *
     * @param id Identificador único de la liana
     * @param x Posición X de la liana
     * @param y Posición Y base de la liana (normalmente alturaMin)
     * @param liana Índice de la liana
     * @param alturaMin Límite inferior (Y mínimo)
     * @param alturaMax Límite superior (Y máximo)
     * @throws IllegalArgumentException si alturaMin >= alturaMax
     */
    public Liana(String id, double x, double y, int liana, Double alturaMin, Double alturaMax) {
        super(id, x, y, liana);
        if (alturaMin >= alturaMax) {
            throw new IllegalArgumentException("alturaMin debe ser menor que alturaMax");
        }
        this.alturaMin = alturaMin;
        this.alturaMax = alturaMax;
    }

    /**
     * Constructor legacy para compatibilidad con código existente.
     * Calcula alturaMin y alturaMax basándose en y y altura.
     */
    public Liana(String id, double x, double y, int liana, int altura) {
        super(id, x, y, liana);
        this.alturaMin = y;
        this.alturaMax = y + altura;
    }

    @Override
    public void actualizar(double deltaTime) {
        // Las lianas son estáticas, no necesitan actualización
    }

    /**
     * Verifica si una posición Y está dentro de los límites de la liana.
     */
    public boolean estaEnRango(double y) {
        return y >= alturaMin && y <= alturaMax;
    }

    /**
     * Obtiene la altura total de la liana.
     */
    public Double getAltura() {
        return alturaMax - alturaMin;
    }

    /**
     * Obtiene el límite inferior de la liana.
     */
    public Double getAlturaMin() {
        return alturaMin;
    }

    /**
     * Obtiene el límite superior de la liana.
     */
    public Double getAlturaMax() {
        return alturaMax;
    }

    /**
     * Ajusta una posición Y para que quede dentro de los límites de la liana.
     *
     * @param y Posición Y a ajustar
     * @return Posición Y ajustada dentro de [alturaMin, alturaMax]
     */
    public Double limitarY(double y) {
        if (y < alturaMin) return alturaMin;
        if (y > alturaMax) return alturaMax;
        return y;
    }
}

