package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Vista inmutable de un cocodrilo en un momento específico.
 * Utilizada para exponer el estado sin comprometer thread-safety.
 * Los snapshots son seguros para lectura concurrente desde múltiples hilos.
 */
public final class SnapshotCocodrilo {
    private final String id;
    private final Cocodrilo.TipoCocodrilo tipo;
    private final int lianaId;
    private final double y;
    private final double velocidadBase;
    private final int direccion;
    private final EstadoCocodrilo estado;
    private final long creadoEn;
    private final long snapshotTimestamp;

    /**
     * Crea un snapshot inmutable desde un cocodrilo.
     *
     * @param cocodrilo Cocodrilo del cual tomar el snapshot
     */
    public SnapshotCocodrilo(Cocodrilo cocodrilo) {
        this.id = cocodrilo.getId();
        this.tipo = cocodrilo.getTipo();
        this.lianaId = cocodrilo.getLianaId();
        this.y = cocodrilo.getY();
        this.velocidadBase = cocodrilo.getVelocidadBase();
        this.direccion = cocodrilo.getDireccion();
        this.estado = cocodrilo.getEstado();
        this.creadoEn = cocodrilo.getCreadoEn();
        this.snapshotTimestamp = System.currentTimeMillis();
    }

    // Getters (sin setters - clase inmutable)

    public String getId() {
        return id;
    }

    public Cocodrilo.TipoCocodrilo getTipo() {
        return tipo;
    }

    public int getLianaId() {
        return lianaId;
    }

    public double getY() {
        return y;
    }

    public double getVelocidadBase() {
        return velocidadBase;
    }

    public int getDireccion() {
        return direccion;
    }

    public EstadoCocodrilo getEstado() {
        return estado;
    }

    public long getCreadoEn() {
        return creadoEn;
    }

    public long getSnapshotTimestamp() {
        return snapshotTimestamp;
    }

    public boolean isActivo() {
        return estado == EstadoCocodrilo.ACTIVO;
    }

    @Override
    public String toString() {
        return String.format("Snapshot[id=%s, tipo=%s, liana=%d, y=%.2f, vel=%.2f, dir=%+d, estado=%s]",
                id, tipo, lianaId, y, velocidadBase, direccion, estado);
    }
}
