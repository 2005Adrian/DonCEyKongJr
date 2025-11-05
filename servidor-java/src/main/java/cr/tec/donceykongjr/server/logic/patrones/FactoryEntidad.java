package cr.tec.donceykongjr.server.logic.patrones;

import cr.tec.donceykongjr.server.logic.entidades.*;
import cr.tec.donceykongjr.server.util.Config;

/**
 * Factory para crear entidades del juego.
 * Implementa el patrón Factory Method para la creación de cocodrilos y frutas.
 */
public class FactoryEntidad {
    private static int contadorCocodrilos = 0;
    private static int contadorFrutas = 0;
    
    /**
     * Crea un cocodrilo rojo.
     */
    public static Cocodrilo crearCocodriloRojo(int liana, double y, double velocidad) {
        contadorCocodrilos++;
        String id = "CROJO_" + contadorCocodrilos;
        return new CocodriloRojo(id, 0, y, liana, velocidad);
    }
    
    /**
     * Crea un cocodrilo azul.
     */
    public static Cocodrilo crearCocodriloAzul(int liana, double y, double velocidad) {
        contadorCocodrilos++;
        String id = "CAZUL_" + contadorCocodrilos;
        return new CocodriloAzul(id, 0, y, liana, velocidad);
    }
    
    /**
     * Crea una fruta.
     */
    public static Fruta crearFruta(int liana, double y, int puntos) {
        contadorFrutas++;
        String id = "F_" + contadorFrutas;
        return new Fruta(id, 0, y, liana, puntos);
    }
    
    /**
     * Crea una fruta con puntos por defecto.
     */
    public static Fruta crearFruta(int liana, double y) {
        return crearFruta(liana, y, Config.PUNTOS_FRUTA_BASE);
    }
    
    /**
     * Crea un jugador.
     */
    public static Jugador crearJugador(String id, double x, double y, int liana) {
        return new Jugador(id, x, y, liana);
    }
    
    /**
     * Resetea los contadores (útil para pruebas o reinicios).
     */
    public static void resetearContadores() {
        contadorCocodrilos = 0;
        contadorFrutas = 0;
    }
}

