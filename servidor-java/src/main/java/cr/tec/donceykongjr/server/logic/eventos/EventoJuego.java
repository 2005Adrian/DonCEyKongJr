package cr.tec.donceykongjr.server.logic.eventos;

/**
 * Representa un evento del juego.
 * Se usa para notificar cambios importantes como recogida de frutas, colisiones, etc.
 */
public class EventoJuego {
    public enum TipoEvento {
        FRUIT_TAKEN,      // Fruta recogida
        PLAYER_HIT,       // Jugador golpeado por cocodrilo
        PLAYER_ELIMINATED, // Jugador eliminado
        LEVEL_UP,         // Subida de nivel
        PLAYER_WIN        // Jugador alcanzó el objetivo
    }
    
    private TipoEvento tipo;
    private Object payload; // Datos adicionales del evento
    
    /**
     * Crea un nuevo evento.
     */
    public EventoJuego(TipoEvento tipo, Object payload) {
        this.tipo = tipo;
        this.payload = payload;
    }
    
    /**
     * Crea un evento sin payload.
     */
    public EventoJuego(TipoEvento tipo) {
        this(tipo, null);
    }
    
    public TipoEvento getTipo() {
        return tipo;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    @Override
    public String toString() {
        return "EventoJuego{" +
                "tipo=" + tipo +
                ", payload=" + payload +
                '}';
    }
}

