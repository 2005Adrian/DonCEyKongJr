package cr.tec.donceykongjr.server;

import cr.tec.donceykongjr.server.cli.ConsolaAdmin;
import cr.tec.donceykongjr.server.logic.GameLoop;
import cr.tec.donceykongjr.server.logic.GameManager;
import cr.tec.donceykongjr.server.network.ServidorJuego;
import cr.tec.donceykongjr.server.util.Config;
import cr.tec.donceykongjr.server.util.LoggerUtil;

/**
 * Clase principal del servidor DonCEy Kong Jr.
 * Inicializa el GameManager, GameLoop, ServidorJuego y ConsolaAdmin.
 */
public class Main {
    private static GameManager gameManager;
    private static GameLoop gameLoop;
    private static ServidorJuego servidor;
    private static ConsolaAdmin consola;
    private static Thread hiloConsola;
    private static Thread hiloServidor;
    
    public static void main(String[] args) {
        LoggerUtil.info("=== servidor doncey kong jr ===");
        LoggerUtil.info("iniciando componentes...");
        
        // Inicializar GameManager
        gameManager = new GameManager();
        LoggerUtil.info("gamemanager inicializado");
        
        // Inicializar GameLoop
        gameLoop = new GameLoop(gameManager);
        gameLoop.iniciar();
        LoggerUtil.info("gameloop iniciado");
        
        // Inicializar Servidor
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : Config.PUERTO_DEFAULT;
        servidor = new ServidorJuego(puerto, gameManager);
        hiloServidor = new Thread(() -> servidor.iniciar());
        hiloServidor.start();
        
        // Inicializar Consola de Administración
        consola = new ConsolaAdmin(gameManager);
        hiloConsola = new Thread(consola);
        hiloConsola.start();
        
        // Registrar hook para cerrar limpiamente
        Runtime.getRuntime().addShutdownHook(new Thread(Main::cerrarServidor));
        
        LoggerUtil.info("servidor completamente iniciado. listo para recibir conexiones.");
        LoggerUtil.info("escribe comandos en la consola de administracion (help para ayuda)");
        
        // Esperar a que termine el hilo del servidor
        try {
            hiloServidor.join();
        } catch (InterruptedException e) {
            LoggerUtil.error("hilo del servidor interrumpido");
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Cierra el servidor limpiamente.
     */
    private static void cerrarServidor() {
        LoggerUtil.info("cerrando servidor...");
        
        if (consola != null) {
            consola.detener();
        }
        
        if (servidor != null) {
            servidor.detener();
        }
        
        if (gameLoop != null) {
            gameLoop.detener();
        }
        
        LoggerUtil.info("servidor cerrado correctamente");
    }
}
