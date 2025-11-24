package cr.tec.donceykongjr.server;

import cr.tec.donceykongjr.server.gui.AdminGUI;
import cr.tec.donceykongjr.server.logic.GameLoop;
import cr.tec.donceykongjr.server.logic.GameManager;
import cr.tec.donceykongjr.server.network.ServidorJuego;
import cr.tec.donceykongjr.server.util.Config;
import cr.tec.donceykongjr.server.util.LoggerUtil;

/**
 * Clase principal del servidor DonCEy Kong Jr.
 *
 * <p>Esta clase es el punto de entrada de la aplicación servidor. Se encarga de:</p>
 * <ul>
 *   <li>Inicializar todos los componentes del sistema (GameManager, GameLoop, Servidor TCP)</li>
 *   <li>Configurar la interfaz gráfica de administración</li>
 *   <li>Gestionar el ciclo de vida del servidor</li>
 *   <li>Coordinar el cierre ordenado de todos los subsistemas</li>
 * </ul>
 *
 * <p><b>Arquitectura del servidor:</b></p>
 * <pre>
 * Main
 *  ├── GameManager (lógica del juego y estado)
 *  ├── GameLoop (ciclo de actualización a 20 TPS)
 *  ├── ServidorJuego (red TCP en puerto 5555)
 *  └── AdminGUI (interfaz gráfica de administración)
 * </pre>
 *
 * <p><b>Flujo de inicialización:</b></p>
 * <ol>
 *   <li>Crear GameManager (inicializa entidades, lianas, motor de cocodrilos)</li>
 *   <li>Crear y arrancar GameLoop (fixed timestep de 50ms)</li>
 *   <li>Crear y arrancar ServidorJuego en hilo separado</li>
 *   <li>Mostrar AdminGUI (interfaz Swing)</li>
 *   <li>Registrar shutdown hook para cierre limpio</li>
 * </ol>
 *
 * @author DonCEyKongJr Team
 * @version 1.0
 * @see GameManager
 * @see GameLoop
 * @see ServidorJuego
 * @see AdminGUI
 */
public class Main {

    /** Gestor principal del juego. Contiene toda la lógica de negocio y estado del juego. */
    private static GameManager gameManager;

    /** Bucle de actualización del juego. Ejecuta {@link GameManager#actualizar(double)} a 20 TPS. */
    private static GameLoop gameLoop;

    /** Servidor TCP que acepta conexiones de clientes en el puerto configurado. */
    private static ServidorJuego servidor;

    /** Interfaz gráfica de administración basada en Swing. */
    private static AdminGUI adminGUI;

    /** Hilo dedicado para ejecutar el servidor de red sin bloquear el hilo principal. */
    private static Thread hiloServidor;

    /**
     * Constructor privado para prevenir instanciación.
     * Esta es una clase utilitaria que solo contiene el punto de entrada main().
     */
    private Main() {
        throw new AssertionError("No se debe instanciar la clase Main");
    }

    /**
     * Punto de entrada principal de la aplicación servidor.
     *
     * <p>Inicializa todos los componentes del servidor en el siguiente orden:</p>
     * <ol>
     *   <li><b>GameManager:</b> Inicializa el estado del juego (lianas, entidades, motor de cocodrilos)</li>
     *   <li><b>GameLoop:</b> Inicia el bucle de actualización a 20 TPS (ticks por segundo)</li>
     *   <li><b>ServidorJuego:</b> Abre el socket TCP y comienza a aceptar conexiones de clientes</li>
     *   <li><b>AdminGUI:</b> Muestra la interfaz gráfica de administración</li>
     *   <li><b>Shutdown Hook:</b> Registra el método de cierre limpio para señales del sistema</li>
     * </ol>
     *
     * <p><b>Argumentos de línea de comandos:</b></p>
     * <ul>
     *   <li><code>args[0]</code> (opcional): Puerto TCP para el servidor (por defecto: 5555)</li>
     * </ul>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>
     * // Usar puerto por defecto (5555)
     * java cr.tec.donceykongjr.server.Main
     *
     * // Usar puerto personalizado
     * java cr.tec.donceykongjr.server.Main 8080
     * </pre>
     *
     * <p><b>Comportamiento de hilos:</b></p>
     * <ul>
     *   <li>Hilo principal: Bloquea esperando que termine el servidor (join)</li>
     *   <li>Hilo servidor: Ejecuta {@link ServidorJuego#iniciar()} para aceptar conexiones</li>
     *   <li>Hilo GameLoop: {@link GameLoop} ejecuta actualizaciones en su propio ScheduledExecutorService</li>
     *   <li>Hilo MotorCocodrilos: Subsistema independiente con su propio executor (10 TPS)</li>
     *   <li>Hilo EDT (Swing): Interfaz gráfica AdminGUI en el Event Dispatch Thread</li>
     * </ul>
     *
     * @param args Argumentos de línea de comandos. args[0] puede especificar el puerto del servidor.
     * @throws NumberFormatException Si args[0] no es un número válido
     * @see GameManager
     * @see GameLoop
     * @see ServidorJuego
     * @see AdminGUI
     * @see Config#PUERTO_DEFAULT
     */
    public static void main(String[] args) {
        // Mensaje de bienvenida
        LoggerUtil.info("=== servidor doncey kong jr ===");
        LoggerUtil.info("iniciando componentes...");

        // PASO 1: Inicializar GameManager
        // Crea el estado del juego: lianas, jugadores, cocodrilos, frutas, Mario
        // También inicializa el MotorCocodrilos independiente
        gameManager = new GameManager();
        LoggerUtil.info("gamemanager inicializado");

        // PASO 2: Inicializar GameLoop
        // Crea el bucle de actualización con fixed timestep de 50ms (20 TPS)
        // El GameLoop llama a gameManager.actualizar(deltaTime) cada tick
        gameLoop = new GameLoop(gameManager);
        gameLoop.iniciar(); // Inicia el ScheduledExecutorService
        LoggerUtil.info("gameloop iniciado");

        // PASO 3: Inicializar Servidor TCP
        // Parsea el puerto desde argumentos o usa el valor por defecto (5555)
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : Config.PUERTO_DEFAULT;
        servidor = new ServidorJuego(puerto, gameManager);

        // Crear y arrancar hilo del servidor para no bloquear el hilo principal
        // El lambda () -> servidor.iniciar() se ejecuta en hiloServidor
        hiloServidor = new Thread(() -> servidor.iniciar());
        hiloServidor.start();

        // PASO 4: Inicializar GUI de Administración
        // Crea la ventana Swing con controles para pausar, crear entidades, etc.
        adminGUI = new AdminGUI(gameManager);
        adminGUI.mostrar(); // Hace visible la ventana en el EDT (Event Dispatch Thread)

        // PASO 5: Registrar shutdown hook para cierre ordenado
        // Cuando se recibe SIGTERM/SIGINT, se llama a cerrarServidor()
        // Esto garantiza que se detengan todos los hilos y se cierren sockets
        Runtime.getRuntime().addShutdownHook(new Thread(Main::cerrarServidor));

        LoggerUtil.info("servidor completamente iniciado. listo para recibir conexiones.");

        // ESPERAR: Bloquear el hilo principal hasta que termine el servidor
        // hiloServidor.join() espera a que el hilo del servidor finalice
        // Esto mantiene viva la aplicación mientras el servidor está activo
        try {
            hiloServidor.join();
        } catch (InterruptedException e) {
            // Si el hilo es interrumpido, registrar el error
            LoggerUtil.error("hilo del servidor interrumpido");
            // Restaurar el estado de interrupción del hilo
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Cierra el servidor de forma ordenada y libera todos los recursos.
     *
     * <p>Este método es invocado automáticamente por el shutdown hook registrado
     * en {@link #main(String[])} cuando la JVM recibe una señal de terminación
     * (SIGTERM, SIGINT, cierre de ventana, etc.).</p>
     *
     * <p><b>Secuencia de cierre:</b></p>
     * <ol>
     *   <li>Detener el servidor TCP (cierra el ServerSocket y desconecta clientes)</li>
     *   <li>Detener el GameLoop (cancela el ScheduledExecutorService)</li>
     *   <li>El GameManager automáticamente detiene el MotorCocodrilos en su shutdown()</li>
     * </ol>
     *
     * <p><b>Thread-safety:</b> Este método puede ser llamado desde el hilo del shutdown hook,
     * por lo que debe ser thread-safe. Utiliza verificaciones null para evitar NPE.</p>
     *
     * @see Runtime#addShutdownHook(Thread)
     * @see ServidorJuego#detener()
     * @see GameLoop#detener()
     */
    private static void cerrarServidor() {
        LoggerUtil.info("cerrando servidor...");

        // Detener el servidor TCP primero
        // Esto cierra el ServerSocket y desconecta todos los clientes
        if (servidor != null) {
            servidor.detener();
        }

        // Detener el GameLoop
        // Esto cancela el ScheduledExecutorService y detiene las actualizaciones
        if (gameLoop != null) {
            gameLoop.detener();
        }

        // Nota: GameManager.shutdown() se llama automáticamente cuando se detiene
        // el GameLoop, lo que a su vez detiene el MotorCocodrilos

        LoggerUtil.info("servidor cerrado correctamente");
    }
}
