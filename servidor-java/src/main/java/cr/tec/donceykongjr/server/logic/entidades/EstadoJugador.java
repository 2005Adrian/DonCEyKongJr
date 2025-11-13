package cr.tec.donceykongjr.server.logic.entidades;

/**
 * Estados posibles del jugador (Donkey Kong Jr.).
 */
public enum EstadoJugador {
    /** Jugador en el suelo */
    SUELO,

    /** Jugador agarrado a una liana */
    EN_LIANA,

    /** Jugador en el aire (saltando o cayendo) */
    SALTANDO,

    /** Jugador muerto (sin vidas) */
    MUERTO,

    /** Jugador celebrando el rescate */
    CELEBRANDO
}
