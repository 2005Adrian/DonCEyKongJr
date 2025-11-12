package cr.tec.donceykongjr.server.logic.entidades;

import java.time.Instant;

/**
 * Clase base abstracta para los cocodrilos del juego.
 * Define el comportamiento común y el modelo de datos completo según especificaciones.
 *
 * Modelo de datos:
 * - id: identificador único (heredado de Entidad)
 * - tipo: ROJO o AZUL
 * - lianaId: identificador de la liana donde se mueve
 * - y: posición vertical actual
 * - velocidadBase: velocidad base sin aplicar factor de dificultad
 * - direccion: +1 (sube) o -1 (baja)
 * - estado: ACTIVO o ELIMINADO
 * - creadoEn: timestamp de creación
 */
public abstract class Cocodrilo extends Entidad {
    protected TipoCocodrilo tipo;
    protected Integer lianaId;
    protected Double velocidadBase;
    protected Integer direccion; // +1 = sube, -1 = baja
    protected EstadoCocodrilo estado;
    protected Long creadoEn; // timestamp en milisegundos

    // Campos legacy para compatibilidad
    @Deprecated
    protected double velocidad;
    @Deprecated
    protected boolean activo;

    /**
     * Tipos de cocodrilos disponibles.
     */
    public enum TipoCocodrilo {
        ROJO, AZUL
    }

    /**
     * Constructor completo con todos los campos del modelo.
     *
     * @param id Identificador único del cocodrilo
     * @param tipo Tipo de cocodrilo (ROJO o AZUL)
     * @param lianaId ID de la liana donde se mueve
     * @param yInicial Posición Y inicial
     * @param velocidadBase Velocidad base (sin factor de dificultad)
     * @param direccion Dirección inicial: +1 (sube) o -1 (baja)
     */
    public Cocodrilo(String id, TipoCocodrilo tipo, Integer lianaId, Double yInicial,
                     Double velocidadBase, Integer direccion) {
        super(id, 0.0, yInicial, lianaId);
        this.tipo = tipo;
        this.lianaId = lianaId;
        this.velocidadBase = velocidadBase;
        this.direccion = direccion;
        this.estado = EstadoCocodrilo.ACTIVO;
        this.creadoEn = Instant.now().toEpochMilli();

        // Compatibilidad legacy
        this.velocidad = velocidadBase;
        this.activo = true;
    }

    /**
     * Constructor legacy para compatibilidad con código existente.
     */
    @Deprecated
    public Cocodrilo(String id, double x, double y, int liana, TipoCocodrilo tipo, double velocidad) {
        super(id, x, y, liana);
        this.tipo = tipo;
        this.lianaId = liana;
        this.velocidadBase = velocidad;
        this.direccion = -1; // Por defecto baja
        this.estado = EstadoCocodrilo.ACTIVO;
        this.creadoEn = Instant.now().toEpochMilli();

        // Compatibilidad legacy
        this.velocidad = velocidad;
        this.activo = true;
    }

    /**
     * Método abstracto de actualización legacy.
     * Las subclases deben implementar mover(dt) en su lugar.
     */
    @Override
    public void actualizar(double deltaTime) {
        mover(deltaTime);
    }

    /**
     * Método abstracto que implementa la lógica de movimiento específica.
     * Cada tipo de cocodrilo implementa su propia estrategia.
     *
     * @param dt Delta time (intervalo de tiempo fijo)
     */
    public abstract void mover(double dt);

    /**
     * Desactiva el cocodrilo marcándolo como ELIMINADO.
     */
    public void eliminar() {
        this.estado = EstadoCocodrilo.ELIMINADO;
        this.activo = false; // Compatibilidad legacy
    }

    /**
     * Invierte la dirección del movimiento.
     * Útil para cocodrilos rojos que rebotan.
     */
    protected void invertirDireccion() {
        this.direccion = -this.direccion;
    }

    // Getters y Setters

    public TipoCocodrilo getTipo() {
        return tipo;
    }

    public Integer getLianaId() {
        return lianaId;
    }

    public Double getVelocidadBase() {
        return velocidadBase;
    }

    public void setVelocidadBase(Double velocidadBase) {
        this.velocidadBase = velocidadBase;
        this.velocidad = velocidadBase; // Compatibilidad legacy
    }

    public Integer getDireccion() {
        return direccion;
    }

    public void setDireccion(Integer direccion) {
        if (direccion != 1 && direccion != -1) {
            throw new IllegalArgumentException("Direccion debe ser +1 o -1");
        }
        this.direccion = direccion;
    }

    public EstadoCocodrilo getEstado() {
        return estado;
    }

    public Long getCreadoEn() {
        return creadoEn;
    }

    public boolean isActivo() {
        return estado == EstadoCocodrilo.ACTIVO;
    }

    public boolean isEliminado() {
        return estado == EstadoCocodrilo.ELIMINADO;
    }

    // Métodos legacy para compatibilidad

    @Deprecated
    public void desactivar() {
        eliminar();
    }

    @Deprecated
    public double getVelocidad() {
        return velocidadBase;
    }

    @Deprecated
    public void setVelocidad(double velocidad) {
        setVelocidadBase(velocidad);
    }
}

