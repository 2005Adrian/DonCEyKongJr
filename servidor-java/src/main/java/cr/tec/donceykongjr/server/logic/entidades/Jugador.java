package cr.tec.donceykongjr.server.logic.entidades;

import cr.tec.donceykongjr.server.util.Config;

/**
 * Representa un jugador en el juego.
 * Maneja posición, vidas, puntaje y estado del jugador.
 */
public class Jugador extends Entidad {
    private int vidas;
    private int puntaje;
    private boolean activo;
    private boolean agarrando; // Si está agarrado a una liana
    
    /**
     * Crea un nuevo jugador.
     */
    public Jugador(String id, double x, double y, int liana) {
        super(id, x, y, liana);
        this.vidas = Config.VIDAS_INICIALES;
        this.puntaje = 0;
        this.activo = true;
        this.agarrando = false;
    }
    
    @Override
    public void actualizar(double deltaTime) {
        // El movimiento del jugador se maneja por input del cliente
        // Este método se puede usar para aplicar gravedad o física adicional
    }
    
    /**
     * Mueve el jugador hacia arriba.
     */
    public void moverArriba(double velocidad) {
        if (activo) {
            this.y -= velocidad;
        }
    }
    
    /**
     * Mueve el jugador hacia abajo.
     */
    public void moverAbajo(double velocidad) {
        if (activo) {
            this.y += velocidad;
        }
    }
    
    /**
     * Mueve el jugador hacia la izquierda.
     */
    public void moverIzquierda(double velocidad) {
        if (activo && liana > 0) {
            this.liana--;
        }
    }
    
    /**
     * Mueve el jugador hacia la derecha.
     */
    public void moverDerecha(double velocidad) {
        if (activo) {
            this.liana++;
        }
    }
    
    /**
     * Realiza un salto.
     */
    public void saltar(double velocidad) {
        if (activo) {
            this.y -= velocidad * 1.5; // Salto más rápido
        }
    }
    
    /**
     * Agarra una liana.
     */
    public void agarrarLiana() {
        this.agarrando = true;
    }
    
    /**
     * Suelta una liana.
     */
    public void soltarLiana() {
        this.agarrando = false;
    }
    
    /**
     * Incrementa el puntaje.
     */
    public void agregarPuntos(int puntos) {
        this.puntaje += puntos;
    }
    
    /**
     * Reduce una vida. Retorna true si el jugador aún tiene vidas.
     */
    public boolean perderVida() {
        vidas--;
        if (vidas <= 0) {
            activo = false;
            return false;
        }
        return true;
    }
    
    /**
     * Verifica si el jugador ha alcanzado el objetivo (parte superior).
     */
    public boolean haAlcanzadoObjetivo() {
        return y <= Config.POSICION_OBJETIVO_Y && activo;
    }
    
    // Getters y Setters
    public int getVidas() {
        return vidas;
    }
    
    public int getPuntaje() {
        return puntaje;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public boolean isAgarrando() {
        return agarrando;
    }
}

