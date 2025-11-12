package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Vista inmutable de un cocodrilo en un momento específico.
 * Utilizada para exponer el estado sin comprometer thread-safety.
 * Los snapshots son seguros para lectura concurrente desde múltiples hilos.
 */
public final class SnapshotCocodrilo {
    private final String id;
    private final Cocodrilo.TipoCocodrilo tipo;
    private final Integer lianaId;
    private final Double y;
    private final Double velocidadBase;
    private final Integer direccion;
    private final EstadoCocodrilo estado;
    private final Long creadoEn;
    private final Long snapshotTimestamp;

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

    public Integer getLianaId() {
        return lianaId;
    }

    public Double getY() {
        return y;
    }

    public Double getVelocidadBase() {
        return velocidadBase;
    }

    public Integer getDireccion() {
        return direccion;
    }

    public EstadoCocodrilo getEstado() {
        return estado;
    }

    public Long getCreadoEn() {
        return creadoEn;
    }

    public Long getSnapshotTimestamp() {
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
