package cr.tec.donceykongjr.server.logic.patrones;

import cr.tec.donceykongjr.server.logic.entidades.*;
import cr.tec.donceykongjr.server.util.Config;

/**
 * Factory para crear entidades del juego.
 * Implementa el patrón Factory Method para la creación de todas las entidades.
 * Centraliza la lógica de creación y generación de IDs.
 */
public class FactoryEntidad {
    private static int contadorCocodrilos = 0;
    private static int contadorFrutas = 0;

    // ========== MÉTODOS PRINCIPALES (RECOMENDADOS) ==========

    /**
     * Crea un cocodrilo rojo con parámetros completos.
     * Este es el método recomendado para crear cocodrilos rojos.
     *
     * @param id Identificador único del cocodrilo
     * @param lianaId ID de la liana donde se moverá
     * @param yInicial Posición Y inicial
     * @param velocidadBase Velocidad base de movimiento
     * @param direccion Dirección inicial: +1 (sube) o -1 (baja)
     * @param alturaMin Límite inferior de movimiento
     * @param alturaMax Límite superior de movimiento
     * @return Nueva instancia de CocodriloRojo
     */
    public static CocodriloRojo crearCocodriloRojo(String id, int lianaId, double yInicial,
                                                    double velocidadBase, int direccion,
                                                    double alturaMin, double alturaMax) {
        return new CocodriloRojo(id, lianaId, yInicial, velocidadBase, direccion, alturaMin, alturaMax);
    }

    /**
     * Crea un cocodrilo azul con parámetros completos.
     * Este es el método recomendado para crear cocodrilos azules.
     *
     * @param id Identificador único del cocodrilo
     * @param lianaId ID de la liana donde caerá
     * @param yInicial Posición Y inicial
     * @param velocidadBase Velocidad base de caída
     * @param limiteInferior Límite inferior donde se eliminará
     * @return Nueva instancia de CocodriloAzul
     */
    public static CocodriloAzul crearCocodriloAzul(String id, int lianaId, double yInicial,
                                                    double velocidadBase, double limiteInferior) {
        return new CocodriloAzul(id, lianaId, yInicial, velocidadBase, limiteInferior);
    }

    /**
     * Crea una fruta con parámetros completos.
     *
     * @param id Identificador único de la fruta
     * @param liana ID de la liana
     * @param y Posición Y
     * @param puntos Puntos que otorga
     * @return Nueva instancia de Fruta
     */
    public static Fruta crearFruta(String id, int liana, double y, int puntos) {
        return new Fruta(id, 0, y, liana, puntos);
    }

    /**
     * Crea un jugador asociado a las lianas activas.
     *
     * @param id Identificador del jugador
     * @param x Posición X inicial
     * @param y Posición Y inicial
     * @param liana Liana de referencia
     * @param lianas Lista de lianas disponibles
     * @return Nueva instancia de Jugador
     */
    public static Jugador crearJugador(String id, double x, double y, int liana, java.util.List<Liana> lianas) {
        return new Jugador(id, x, y, liana, lianas);
    }

    // ========== MÉTODOS LEGACY (DEPRECATED) ==========

    /**
     * Crea un cocodrilo rojo con generación automática de ID.
     * @deprecated Usar {@link #crearCocodriloRojo(String, int, double, double, int, double, double)} con ID explícito
     */
    @Deprecated
    public static Cocodrilo crearCocodriloRojo(int liana, double y, double velocidad) {
        contadorCocodrilos++;
        String id = "CROJO_" + contadorCocodrilos;
        return new CocodriloRojo(id, 0, y, liana, velocidad);
    }

    /**
     * Crea un cocodrilo azul con generación automática de ID.
     * @deprecated Usar {@link #crearCocodriloAzul(String, int, double, double, double)} con ID explícito
     */
    @Deprecated
    public static Cocodrilo crearCocodriloAzul(int liana, double y, double velocidad) {
        contadorCocodrilos++;
        String id = "CAZUL_" + contadorCocodrilos;
        return new CocodriloAzul(id, 0.0, y, liana, velocidad);
    }

    /**
     * Crea una fruta con generación automática de ID.
     *
     * @param liana ID de la liana
     * @param y Posición Y
     * @param puntos Puntos que otorga
     * @return Nueva instancia de Fruta
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
     * Resetea los contadores (útil para pruebas o reinicios).
     */
    public static void resetearContadores() {
        contadorCocodrilos = 0;
        contadorFrutas = 0;
    }
}

