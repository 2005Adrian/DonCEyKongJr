package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Vista inmutable del estado de un jugador en un instante del tiempo.
 * Permite exponer informacion al cliente de forma thread-safe.
 */
public final class SnapshotJugador {
    private final String id;
    private final double x;
    private final double y;
    private final double vx;
    private final double vy;
    private final EstadoJugador estado;
    private final Integer lianaId;
    private final int renderLiana;
    private final DireccionJugador facing;
    private final int score;
    private final int lives;
    private final boolean activo;
    private final boolean celebrando;
    private final long snapshotTimestamp;

    /**
     * Construye un snapshot a partir del estado actual del jugador.
     *
     * @param jugador jugador del cual capturar los datos
     */
    public SnapshotJugador(Jugador jugador) {
        this.id = jugador.getId();
        this.x = jugador.getX();
        this.y = jugador.getY();
        this.vx = jugador.getVx();
        this.vy = jugador.getVy();
        this.estado = jugador.getEstado();
        this.lianaId = jugador.getLianaId();
        this.renderLiana = jugador.getLiana();
        this.facing = jugador.getFacing();
        this.score = jugador.getPuntaje();
        this.lives = jugador.getVidas();
        this.activo = jugador.isActivo();
        this.celebrando = jugador.estaCelebrando();
        this.snapshotTimestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public EstadoJugador getEstado() {
        return estado;
    }

    public Integer getLianaId() {
        return lianaId;
    }

    public int getRenderLiana() {
        return renderLiana;
    }

    public DireccionJugador getFacing() {
        return facing;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public boolean isActivo() {
        return activo;
    }

    public boolean isCelebrando() {
        return celebrando;
    }

    public long getSnapshotTimestamp() {
        return snapshotTimestamp;
    }
}
