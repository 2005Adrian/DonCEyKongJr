package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Representa a Mario como un obstáculo estático en una liana.
 * Mario permanece en una posición fija y el contacto con él causa la muerte del jugador.
 */
public class Mario {
    private final String id;
    private final int lianaId;
    private final double y;
    private boolean activo;

    /**
     * Crea una nueva instancia de Mario.
     *
     * @param id Identificador único
     * @param lianaId ID de la liana donde se ubica
     * @param y Posición Y en la liana
     */
    public Mario(String id, int lianaId, double y) {
        this.id = id;
        this.lianaId = lianaId;
        this.y = y;
        this.activo = true;
    }

    // Getters

    public String getId() {
        return id;
    }

    public int getLianaId() {
        return lianaId;
    }

    public double getY() {
        return y;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return String.format("Mario[id=%s, liana=%d, y=%.2f, activo=%b]",
                id, lianaId, y, activo);
    }
}
