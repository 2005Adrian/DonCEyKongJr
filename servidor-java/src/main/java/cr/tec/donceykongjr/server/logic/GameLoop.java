package cr.tec.donceykongjr.server.logic;

import cr.tec.donceykongjr.server.util.Config;
import cr.tec.donceykongjr.server.util.LoggerUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Bucle principal del juego que actualiza el estado a intervalos fijos (Fixed Timestep).
 *
 * <p>Esta clase implementa el patrón <b>Game Loop</b> con fixed timestep, garantizando
 * que la lógica del juego se actualice a una tasa constante independientemente de la
 * velocidad de renderizado de los clientes o del rendimiento del sistema.</p>
 *
 * <p><b>Características principales:</b></p>
 * <ul>
 *   <li>Tasa fija: 20 TPS (ticks por segundo) = 50ms por tick</li>
 *   <li>Utiliza {@link ScheduledExecutorService} para garantizar precisión temporal</li>
 *   <li>Thread-safe: Se ejecuta en su propio hilo dedicado</li>
 *   <li>Manejo robusto de errores sin detener el bucle</li>
 *   <li>Cierre ordenado con timeout</li>
 * </ul>
 *
 * <p><b>Flujo de ejecución:</b></p>
 * <pre>
 * 1. iniciar() → Programa ejecución periódica cada 50ms
 * 2. tick() → Se ejecuta cada 50ms
 *    ├── gameManager.actualizar(deltaTime)
 *    └── Manejo de excepciones
 * 3. detener() → Cancela ejecuciones futuras y espera terminación
 * </pre>
 *
 * <p><b>Ventajas del Fixed Timestep:</b></p>
 * <ul>
 *   <li>Simulación física determinista y predecible</li>
 *   <li>Mismo comportamiento en diferentes velocidades de hardware</li>
 *   <li>Facilita sincronización cliente-servidor</li>
 *   <li>Evita problemas de precisión numérica de variable timestep</li>
 * </ul>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * GameManager gameManager = new GameManager();
 * GameLoop gameLoop = new GameLoop(gameManager);
 *
 * // Iniciar el bucle
 * gameLoop.iniciar();
 *
 * // ... el juego se ejecuta a 20 TPS ...
 *
 * // Detener el bucle cuando termine el servidor
 * gameLoop.detener();
 * }</pre>
 *
 * @author DonCEyKongJr Team
 * @version 1.0
 * @see GameManager#actualizar(double)
 * @see Config#TICKS_POR_SEGUNDO
 * @see Config#INTERVALO_TICK_MS
 */
public class GameLoop {

    /** Referencia al gestor del juego que será actualizado en cada tick. */
    private GameManager gameManager;

    /**
     * Ejecutor programado que gestiona la ejecución periódica del tick.
     * Utiliza un pool de un solo hilo para garantizar ejecución secuencial.
     */
    private ScheduledExecutorService scheduler;

    /** Indica si el bucle está actualmente en ejecución. */
    private boolean ejecutando;

    /**
     * Delta time fijo en segundos que se pasa a cada actualización.
     * Calculado como 1.0 / TPS. Para 20 TPS: deltaTime = 0.05 segundos.
     */
    private double deltaTime;

    /**
     * Constructor del GameLoop.
     *
     * <p>Inicializa el bucle con el GameManager proporcionado y configura
     * el delta time basándose en la tasa de ticks configurada.</p>
     *
     * <p><b>Cálculo del delta time:</b></p>
     * <pre>
     * deltaTime = 1.0 / TICKS_POR_SEGUNDO
     * Ejemplo: 1.0 / 20 = 0.05 segundos = 50 milisegundos
     * </pre>
     *
     * @param gameManager El gestor del juego que será actualizado. No debe ser null.
     * @throws NullPointerException Si gameManager es null (comportamiento implícito)
     * @see Config#TICKS_POR_SEGUNDO
     */
    public GameLoop(GameManager gameManager) {
        this.gameManager = gameManager;
        this.ejecutando = false;
        // Calcular delta time: para 20 TPS = 1.0/20 = 0.05 segundos
        this.deltaTime = 1.0 / Config.TICKS_POR_SEGUNDO;
        // Crear scheduler con un solo hilo (single-threaded)
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Inicia el bucle del juego con ejecución periódica.
     *
     * <p>Programa la ejecución del método {@link #tick()} cada {@link Config#INTERVALO_TICK_MS}
     * milisegundos utilizando {@link ScheduledExecutorService#scheduleAtFixedRate}.</p>
     *
     * <p><b>Comportamiento:</b></p>
     * <ul>
     *   <li>Si el bucle ya está ejecutándose, registra una advertencia y retorna sin hacer nada</li>
     *   <li>Marca el estado como ejecutando</li>
     *   <li>Programa ejecución inmediata (delay inicial = 0) y luego cada 50ms</li>
     *   <li>La ejecución es a "tasa fija": el próximo tick se programa desde el inicio del anterior</li>
     * </ul>
     *
     * <p><b>Diferencia con scheduleWithFixedDelay:</b></p>
     * <ul>
     *   <li><code>scheduleAtFixedRate</code>: Próximo tick a los 50ms del INICIO del anterior</li>
     *   <li><code>scheduleWithFixedDelay</code>: Próximo tick a los 50ms del FIN del anterior</li>
     * </ul>
     *
     * <p>Usamos <code>scheduleAtFixedRate</code> para mantener la tasa de 20 TPS constante,
     * aunque un tick individual se retrase.</p>
     *
     * @see #tick()
     * @see #detener()
     * @see Config#INTERVALO_TICK_MS
     */
    public void iniciar() {
        // Validar que no esté ya ejecutándose
        if (ejecutando) {
            LoggerUtil.warning("el gameloop ya esta ejecutandose");
            return;
        }

        // Marcar como ejecutando
        ejecutando = true;
        LoggerUtil.info("gameloop iniciado (" + Config.TICKS_POR_SEGUNDO + " ticks/segundo)");

        // Programar ejecución periódica del tick
        // - this::tick: Método a ejecutar
        // - 0: Delay inicial (empezar inmediatamente)
        // - INTERVALO_TICK_MS: Periodo entre ejecuciones (50ms)
        // - MILLISECONDS: Unidad de tiempo
        scheduler.scheduleAtFixedRate(
            this::tick,
            0,
            Config.INTERVALO_TICK_MS,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Ejecuta un único tick (actualización) del juego.
     *
     * <p>Este método se invoca automáticamente cada {@link Config#INTERVALO_TICK_MS}
     * milisegundos por el {@link ScheduledExecutorService}. Delega la actualización
     * al {@link GameManager#actualizar(double)} pasando el delta time fijo.</p>
     *
     * <p><b>Manejo de errores:</b></p>
     * <ul>
     *   <li>Cualquier excepción lanzada por gameManager.actualizar() es capturada</li>
     *   <li>Se registra el error sin detener el bucle</li>
     *   <li>El stack trace se imprime para debugging</li>
     *   <li>El siguiente tick se ejecutará normalmente</li>
     * </ul>
     *
     * <p>Esto garantiza que un bug en la lógica del juego no detenga completamente
     * el servidor, permitiendo diagnóstico y potencial recuperación.</p>
     *
     * <p><b>Secuencia de actualización en GameManager:</b></p>
     * <pre>
     * tick() → gameManager.actualizar(0.05)
     *   ├── Sincronizar cocodrilos del motor
     *   ├── Actualizar física de jugadores
     *   ├── Detectar colisiones
     *   ├── Verificar objetivos
     *   └── Notificar observadores (enviar estado a clientes)
     * </pre>
     *
     * @see GameManager#actualizar(double)
     */
    private void tick() {
        try {
            // Actualizar el estado del juego con deltaTime fijo (0.05 segundos)
            gameManager.actualizar(deltaTime);
        } catch (Exception e) {
            // Capturar cualquier excepción para evitar que el bucle se detenga
            LoggerUtil.error("error en gameloop: " + e.getMessage());
            // Imprimir stack trace completo para debugging
            e.printStackTrace();
            // El bucle continúa ejecutándose en el siguiente tick
        }
    }

    /**
     * Detiene el bucle del juego de forma ordenada.
     *
     * <p>Realiza un cierre ordenado del {@link ScheduledExecutorService}, esperando
     * a que termine la tarea actual y cancelando las futuras.</p>
     *
     * <p><b>Secuencia de cierre:</b></p>
     * <ol>
     *   <li>Verificar si el bucle está ejecutándose (si no, retornar)</li>
     *   <li>Marcar ejecutando = false</li>
     *   <li>Llamar a scheduler.shutdown() (no acepta nuevas tareas)</li>
     *   <li>Esperar hasta 2 segundos a que termine la tarea actual (awaitTermination)</li>
     *   <li>Si no termina en 2 segundos, forzar cierre (shutdownNow)</li>
     *   <li>Si se interrumpe la espera, forzar cierre y restaurar flag de interrupción</li>
     * </ol>
     *
     * <p><b>Thread-safety:</b> Este método puede ser llamado desde cualquier hilo
     * (típicamente desde el shutdown hook o desde la GUI de administración).</p>
     *
     * <p><b>Garantías:</b></p>
     * <ul>
     *   <li>No se programarán nuevos ticks después de shutdown()</li>
     *   <li>El tick actual se completa normalmente (hasta 2 segundos de timeout)</li>
     *   <li>Recursos del ExecutorService son liberados</li>
     * </ul>
     *
     * @see ScheduledExecutorService#shutdown()
     * @see ScheduledExecutorService#awaitTermination(long, TimeUnit)
     * @see ScheduledExecutorService#shutdownNow()
     */
    public void detener() {
        // Si no está ejecutando, no hay nada que detener
        if (!ejecutando) {
            return;
        }

        // Marcar como no ejecutando
        ejecutando = false;

        // Iniciar cierre ordenado: no acepta nuevas tareas
        scheduler.shutdown();

        try {
            // Esperar hasta 2 segundos a que termine la tarea actual
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                // Si no terminó en 2 segundos, forzar cierre inmediato
                // shutdownNow() intenta interrumpir las tareas en ejecución
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Si este hilo es interrumpido mientras espera, forzar cierre
            scheduler.shutdownNow();
            // Restaurar el flag de interrupción del hilo actual
            Thread.currentThread().interrupt();
        }

        LoggerUtil.info("gameloop detenido");
    }

    /**
     * Verifica si el bucle está actualmente en ejecución.
     *
     * <p>Este método puede ser utilizado para:</p>
     * <ul>
     *   <li>Validar estado antes de llamar a {@link #iniciar()} o {@link #detener()}</li>
     *   <li>Mostrar estado en la interfaz de administración</li>
     *   <li>Debugging y diagnóstico</li>
     * </ul>
     *
     * @return {@code true} si el bucle está ejecutándose, {@code false} en caso contrario
     */
    public boolean isEjecutando() {
        return ejecutando;
    }
}
