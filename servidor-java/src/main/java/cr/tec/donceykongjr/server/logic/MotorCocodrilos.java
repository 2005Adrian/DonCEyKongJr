package cr.tec.donceykongjr.server.logic;

import cr.tec.donceykongjr.server.logic.entidades.*;
import cr.tec.donceykongjr.server.logic.patrones.FactoryEntidad;
import cr.tec.donceykongjr.server.util.LoggerUtil;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Motor independiente para el subsistema de cocodrilos en DonCEy Kong Jr.
 *
 * <p>Este motor orquesta de forma autónoma el ciclo completo de vida de los cocodrilos,
 * incluyendo creación, actualización física, eliminación y telemetría. Opera en un hilo
 * independiente del GameLoop principal para desacoplar la lógica de enemigos del resto
 * del juego.</p>
 *
 * <p><b>Características principales:</b></p>
 * <ul>
 *   <li><b>Fixed Timestep Determinista:</b> Actualización a tasa fija configurable (típicamente 10 TPS)</li>
 *   <li><b>Thread-Safe:</b> Sincronización mediante locks, estructuras concurrentes y cola de comandos</li>
 *   <li><b>Dificultad Escalable:</b> Factor multiplicador que acelera todos los cocodrilos simultáneamente</li>
 *   <li><b>Telemetría Integrada:</b> Métricas de rendimiento en tiempo real (TPS, lag, contadores)</li>
 *   <li><b>Snapshots Inmutables:</b> Acceso thread-safe al estado sin bloqueos prolongados</li>
 *   <li><b>Orden Determinista:</b> Actualización ordenada por ID para reproducibilidad</li>
 * </ul>
 *
 * <p><b>Arquitectura del motor:</b></p>
 * <pre>
 * MotorCocodrilos
 *  ├── ScheduledExecutorService (hilo daemon independiente)
 *  ├── Cola de Comandos (creación/eliminación thread-safe)
 *  ├── ConcurrentHashMap&lt;String, Cocodrilo&gt; (cocodrilos activos)
 *  ├── ConcurrentHashMap&lt;Integer, Liana&gt; (validación de rangos)
 *  └── TelemetriaCocodrilos (métricas de rendimiento)
 * </pre>
 *
 * <p><b>Flujo de ejecución de un tick:</b></p>
 * <ol>
 *   <li>Procesar cola de comandos pendientes (crear/eliminar cocodrilos)</li>
 *   <li>Actualizar física de cada cocodrilo con deltaTime escalado (dtFijo * factorDificultad)</li>
 *   <li>Limpiar cocodrilos marcados como ELIMINADO</li>
 *   <li>Incrementar contador de tick</li>
 *   <li>Registrar métricas de rendimiento en telemetría</li>
 * </ol>
 *
 * <p><b>Uso del patrón Command Queue:</b></p>
 * Las operaciones de creación y eliminación se encolan mediante {@link ConcurrentLinkedQueue}
 * y se procesan en el tick siguiente. Esto garantiza thread-safety sin bloquear
 * al hilo llamante (típicamente el GameManager o AdminGUI).
 *
 * <p><b>Factor de dificultad:</b></p>
 * El factorDificultad multiplica el deltaTime efectivo, acelerando todos los cocodrilos
 * proporcionalmente. Ejemplo:
 * <pre>
 * factorDificultad = 1.0  → velocidad normal (60 unidades/s)
 * factorDificultad = 1.5  → velocidad 1.5x (90 unidades/s)
 * factorDificultad = 2.0  → velocidad 2.0x (120 unidades/s)
 * </pre>
 *
 * <p><b>Ejemplo de uso completo:</b></p>
 * <pre>{@code
 * // Crear motor con 10 TPS (dt = 0.1 segundos)
 * MotorCocodrilos motor = new MotorCocodrilos(0.1);
 *
 * // Registrar lianas para validación
 * motor.registrarLiana(new Liana("L_0", 0, 0, 0, 0.0, 500.0));
 * motor.registrarLiana(new Liana("L_1", 0, 0, 1, 0.0, 500.0));
 *
 * // Iniciar el motor
 * motor.start(); // Hilo daemon comienza a ejecutar ticks
 *
 * // Crear cocodrilos (operación thread-safe, encola comando)
 * String id1 = motor.crearCocodriloRojo(0, 100.0, 60.0, -1);
 * String id2 = motor.crearCocodriloAzul(1, 200.0, 50.0);
 *
 * // Incrementar dificultad tras rescate exitoso (+10%)
 * motor.incrementarDificultad(1.10);
 *
 * // Obtener snapshot inmutable del estado
 * SnapshotSistemaCocodrilos snapshot = motor.getSnapshot();
 * System.out.println("Cocodrilos activos: " + snapshot.getTotalCocodrilosActivos());
 *
 * // Ver telemetría
 * System.out.println(motor.getTelemetria().generarReporte());
 *
 * // Detener el motor al cerrar servidor
 * motor.stop();
 * }</pre>
 *
 * <p><b>Thread-safety:</b></p>
 * <ul>
 *   <li>Toda modificación del estado se ejecuta dentro del lock synchronized</li>
 *   <li>ConcurrentHashMap para acceso multi-thread a colecciones</li>
 *   <li>AtomicBoolean/AtomicInteger/AtomicLong para flags y contadores</li>
 *   <li>Snapshots inmutables para lectura sin bloqueos</li>
 * </ul>
 *
 * <p><b>Rendimiento:</b></p>
 * Con 100 cocodrilos a 10 TPS, el tick típicamente ejecuta en 1-5ms.
 * La telemetría advierte si el tick excede el dtFijo configurado (lag).
 *
 * @author DonCEyKongJr Team
 * @version 1.0
 * @see Cocodrilo
 * @see CocodriloRojo
 * @see CocodriloAzul
 * @see TelemetriaCocodrilos
 * @see SnapshotSistemaCocodrilos
 * @see FactoryEntidad
 */
public class MotorCocodrilos {

    // ==================== CONFIGURACIÓN ====================

    /**
     * Delta time fijo en segundos para cada tick del motor.
     *
     * <p>Determina la tasa de actualización del motor. Ejemplos:</p>
     * <ul>
     *   <li>0.1 segundos → 10 TPS (ticks por segundo)</li>
     *   <li>0.05 segundos → 20 TPS</li>
     * </ul>
     *
     * <p>Este valor es inmutable tras la construcción del motor.</p>
     *
     * @see #dtFijoMs
     */
    private final double dtFijo;

    /**
     * Delta time fijo en milisegundos (conversión de {@link #dtFijo}).
     *
     * <p>Se utiliza para programar el {@link ScheduledExecutorService}
     * y para calcular desviaciones en la telemetría.</p>
     *
     * <p>Calculado como: {@code (long) (dtFijo * 1000)}</p>
     */
    private final long dtFijoMs;

    /**
     * Factor multiplicador de dificultad que escala la velocidad de los cocodrilos.
     *
     * <p>Rango válido: {@code factorDificultad >= 1.0}</p>
     *
     * <p><b>Efecto en el juego:</b></p>
     * <pre>
     * velocidadEfectiva = velocidadBase * factorDificultad
     * deltaTimeEfectivo = dtFijo * factorDificultad
     * </pre>
     *
     * <p>Modificado mediante {@link #incrementarDificultad(double)}.
     * Marcado como volatile para visibilidad inmediata entre hilos.</p>
     *
     * @see #incrementarDificultad(double)
     */
    private volatile double factorDificultad;

    // ==================== ESTADO DEL MOTOR ====================

    /**
     * Indica si el motor está actualmente ejecutando ticks.
     *
     * <p>AtomicBoolean garantiza thread-safety sin sincronización.
     * Utilizado para prevenir múltiples inicios y para validar detención.</p>
     *
     * @see #start()
     * @see #stop()
     * @see #isEjecutando()
     */
    private final AtomicBoolean ejecutando;

    /**
     * Contador del número de ticks ejecutados desde el inicio del motor.
     *
     * <p>Se incrementa en cada tick completado exitosamente.
     * Útil para debugging y sincronización con GameManager.</p>
     *
     * @see #ejecutarTick()
     */
    private final AtomicLong tickActual;

    /**
     * Executor programado que ejecuta el tick del motor a intervalos fijos.
     *
     * <p>Configuración:</p>
     * <ul>
     *   <li>Single-threaded: Solo un tick ejecuta a la vez (orden secuencial)</li>
     *   <li>Daemon thread: No impide el cierre de la JVM</li>
     *   <li>scheduleAtFixedRate: Tasa fija independiente de la duración del tick</li>
     * </ul>
     *
     * @see #start()
     * @see #stop()
     */
    private ScheduledExecutorService ejecutor;

    // ==================== COLECCIONES THREAD-SAFE ====================

    /**
     * Mapa de cocodrilos activos indexados por ID único.
     *
     * <p>Tipo: {@link ConcurrentHashMap} para acceso multi-thread seguro.</p>
     *
     * <p><b>Key:</b> String - ID único generado por {@link #generarId(String)}</p>
     * <p><b>Value:</b> {@link Cocodrilo} - Instancia del cocodrilo (Rojo o Azul)</p>
     *
     * <p>Ciclo de vida:</p>
     * <ol>
     *   <li>Creación: {@link #crearCocodriloRojo} / {@link #crearCocodriloAzul} encola comando</li>
     *   <li>Inserción: {@link #procesarColaComandos} inserta en el mapa</li>
     *   <li>Actualización: {@link #actualizarCocodrilos} itera sobre values()</li>
     *   <li>Eliminación: {@link #limpiarCocodrilosEliminados} remueve si {@code isEliminado()}</li>
     * </ol>
     *
     * @see #sincronizarCocodrilosDesdeMotor()
     */
    private final Map<String, Cocodrilo> cocodrilos;

    /**
     * Mapa de lianas registradas en el sistema, indexadas por ID de liana.
     *
     * <p>Tipo: {@link ConcurrentHashMap} para registros thread-safe.</p>
     *
     * <p><b>Propósito:</b></p>
     * <ul>
     *   <li>Validar que la liana existe antes de crear un cocodrilo</li>
     *   <li>Obtener rangos (alturaMin, alturaMax) para validación de posición Y</li>
     *   <li>Pasar límites al constructor del cocodrilo para física correcta</li>
     * </ul>
     *
     * <p>Las lianas deben ser registradas mediante {@link #registrarLiana(Liana)}
     * antes de poder crear cocodrilos en ellas.</p>
     *
     * @see #registrarLiana(Liana)
     * @see #crearCocodriloRojo
     * @see #crearCocodriloAzul
     */
    private final Map<Integer, Liana> lianas;

    /**
     * Cola concurrente de comandos pendientes (creación/eliminación de cocodrilos).
     *
     * <p>Implementa el patrón <b>Command Queue</b> para thread-safety:</p>
     * <ul>
     *   <li>Hilos externos (GameManager, AdminGUI) encolan comandos mediante offer()</li>
     *   <li>Hilo del motor consume comandos en {@link #procesarColaComandos()}</li>
     *   <li>Operaciones lock-free gracias a {@link ConcurrentLinkedQueue}</li>
     * </ul>
     *
     * <p><b>Ejemplo de comando de creación:</b></p>
     * <pre>{@code
     * colaComandos.offer(() -> {
     *     CocodriloRojo croc = FactoryEntidad.crearCocodriloRojo(...);
     *     cocodrilos.put(id, croc);
     *     telemetria.registrarCreacion();
     * });
     * }</pre>
     *
     * @see #crearCocodriloRojo
     * @see #crearCocodriloAzul
     * @see #eliminarCocodrilo
     * @see #procesarColaComandos()
     */
    private final Queue<Runnable> colaComandos;

    // ==================== GENERACIÓN DE IDS ====================

    /**
     * Contador atómico para generar IDs únicos de cocodrilos.
     *
     * <p>Se incrementa cada vez que se genera un nuevo ID mediante {@link #generarId(String)}.</p>
     *
     * <p><b>Formato del ID generado:</b></p>
     * <pre>
     * CROC_{TIPO}_{TIMESTAMP}_{CONTADOR}
     * Ejemplo: CROC_ROJO_1705345678901_42
     * </pre>
     *
     * <p>La combinación de timestamp + contador garantiza unicidad
     * incluso si se crean múltiples cocodrilos en el mismo milisegundo.</p>
     *
     * @see #generarId(String)
     */
    private final AtomicInteger contadorIds;

    // ==================== TELEMETRÍA ====================

    /**
     * Sistema de telemetría que registra métricas de rendimiento del motor.
     *
     * <p>Métricas capturadas:</p>
     * <ul>
     *   <li>Cocodrilos creados/eliminados (contadores)</li>
     *   <li>Ticks ejecutados</li>
     *   <li>Duración de cada tick (promedio y último)</li>
     *   <li>Desviación respecto al dtFijo esperado</li>
     *   <li>TPS (ticks por segundo) promedio</li>
     *   <li>Uptime del motor</li>
     * </ul>
     *
     * <p>Accesible mediante {@link #getTelemetria()} para monitoreo en tiempo real.</p>
     *
     * @see TelemetriaCocodrilos
     * @see #getTelemetria()
     */
    private final TelemetriaCocodrilos telemetria;

    // ==================== SINCRONIZACIÓN ====================

    /**
     * Lock de sincronización para operaciones críticas del motor.
     *
     * <p>Protege las siguientes operaciones:</p>
     * <ul>
     *   <li>Procesamiento de la cola de comandos</li>
     *   <li>Actualización de cocodrilos</li>
     *   <li>Limpieza de cocodrilos eliminados</li>
     *   <li>Generación de snapshots</li>
     *   <li>Modificación del factor de dificultad</li>
     * </ul>
     *
     * <p>Garantiza que el tick se ejecute atómicamente sin interferencia
     * de hilos externos que solicitan snapshots o incrementan dificultad.</p>
     */
    private final Object lock = new Object();

    // ==================== CONSTRUCTOR ====================

    /**
     * Crea un motor de cocodrilos con delta time fijo específico.
     *
     * <p>Inicializa todas las estructuras de datos y configura el sistema
     * de telemetría. El motor queda en estado detenido hasta llamar a {@link #start()}.</p>
     *
     * <p><b>Configuración típica:</b></p>
     * <ul>
     *   <li>dtFijo = 0.1 → 10 TPS (10 ticks por segundo)</li>
     *   <li>factorDificultad inicial = 1.0 (sin modificación)</li>
     * </ul>
     *
     * <p><b>Secuencia de inicialización:</b></p>
     * <ol>
     *   <li>Validar que dtFijo {@code > 0}</li>
     *   <li>Convertir dtFijo a milisegundos</li>
     *   <li>Inicializar factorDificultad a 1.0</li>
     *   <li>Crear estructuras concurrentes (mapas, cola, atomics)</li>
     *   <li>Crear sistema de telemetría</li>
     *   <li>Registrar inicialización en log</li>
     * </ol>
     *
     * @param dtFijo Delta time fijo en segundos (debe ser {@code > 0}). Ejemplos:
     *               <ul>
     *                 <li>0.1 para 10 TPS</li>
     *                 <li>0.05 para 20 TPS</li>
     *                 <li>0.2 para 5 TPS</li>
     *               </ul>
     * @throws IllegalArgumentException Si {@code dtFijo <= 0}
     * @see #start()
     */
    public MotorCocodrilos(double dtFijo) {
        // Validación del parámetro
        if (dtFijo <= 0) {
            throw new IllegalArgumentException("dtFijo debe ser mayor a 0");
        }

        // Configuración de tiempo
        this.dtFijo = dtFijo;
        this.dtFijoMs = (long) (dtFijo * 1000); // Conversión a milisegundos
        this.factorDificultad = 1.0; // Dificultad base sin modificación

        // Estado del motor
        this.ejecutando = new AtomicBoolean(false);
        this.tickActual = new AtomicLong(0);

        // Colecciones thread-safe
        this.cocodrilos = new ConcurrentHashMap<>();
        this.lianas = new ConcurrentHashMap<>();
        this.colaComandos = new ConcurrentLinkedQueue<>();

        // Generación de IDs
        this.contadorIds = new AtomicInteger(0);

        // Telemetría (pasar dtFijo en ms para cálculo de desviación)
        this.telemetria = new TelemetriaCocodrilos((double) dtFijoMs);

        LoggerUtil.info("MotorCocodrilos inicializado con dt=" + dtFijo + "s");
    }

    // ==================== CICLO DE VIDA DEL MOTOR ====================

    /**
     * Inicia el motor de cocodrilos en su propio hilo con fixed timestep.
     *
     * <p>Crea un {@link ScheduledExecutorService} con un solo hilo daemon
     * y programa la ejecución periódica de {@link #ejecutarTick()} cada
     * {@link #dtFijoMs} milisegundos.</p>
     *
     * <p><b>Configuración del executor:</b></p>
     * <ul>
     *   <li>Single-threaded: Garantiza orden secuencial de ticks</li>
     *   <li>Daemon thread: No previene el cierre de la JVM</li>
     *   <li>Nombre "MotorCocodrilos": Facilita debugging en thread dumps</li>
     *   <li>scheduleAtFixedRate: Tasa fija independiente de la duración del tick</li>
     * </ul>
     *
     * <p><b>Diferencia entre scheduleAtFixedRate y scheduleWithFixedDelay:</b></p>
     * <ul>
     *   <li><b>scheduleAtFixedRate</b> (usado aquí): Próximo tick a los X ms del INICIO del anterior
     *       → Mantiene 10 TPS constantes aunque el tick tarde más de lo esperado</li>
     *   <li><b>scheduleWithFixedDelay</b>: Próximo tick a los X ms del FIN del anterior
     *       → TPS varía según la duración del tick</li>
     * </ul>
     *
     * <p><b>Comportamiento ante múltiples llamadas:</b></p>
     * Si el motor ya está ejecutando, registra una advertencia y retorna sin hacer nada.
     * No lanza excepción para simplificar código cliente.
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * MotorCocodrilos motor = new MotorCocodrilos(0.1);
     * motor.registrarLiana(liana0);
     * motor.registrarLiana(liana1);
     * motor.start(); // Inicia el bucle de actualización
     * // ... el motor ejecuta ticks automáticamente cada 100ms ...
     * }</pre>
     *
     * @see #stop()
     * @see #ejecutarTick()
     */
    public void start() {
        // Prevenir múltiples inicios
        if (ejecutando.get()) {
            LoggerUtil.warning("El motor ya está ejecutando");
            return;
        }

        // Marcar como ejecutando
        ejecutando.set(true);

        // Crear executor con hilo daemon
        ejecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MotorCocodrilos");
            t.setDaemon(true); // No previene cierre de JVM
            return t;
        });

        // Programar ejecución a tasa fija
        // - this::ejecutarTick: Método a invocar
        // - 0: Delay inicial (empezar inmediatamente)
        // - dtFijoMs: Periodo entre ticks
        // - MILLISECONDS: Unidad de tiempo
        ejecutor.scheduleAtFixedRate(
                this::ejecutarTick,
                0,
                dtFijoMs,
                TimeUnit.MILLISECONDS
        );

        LoggerUtil.info("MotorCocodrilos iniciado (TPS=" + (1.0 / dtFijo) + ")");
    }

    /**
     * Detiene el motor de cocodrilos de forma segura y ordenada.
     *
     * <p>Realiza un cierre graceful del {@link ScheduledExecutorService},
     * esperando a que finalice el tick actual antes de terminar.</p>
     *
     * <p><b>Secuencia de cierre:</b></p>
     * <ol>
     *   <li>Verificar si el motor está ejecutando (si no, retornar silenciosamente)</li>
     *   <li>Marcar {@code ejecutando = false} para indicar detención</li>
     *   <li>Llamar a {@code executor.shutdown()} → no acepta nuevos ticks</li>
     *   <li>Esperar hasta 5 segundos a que finalice el tick actual ({@code awaitTermination})</li>
     *   <li>Si no termina en 5 segundos, forzar cierre ({@code shutdownNow()})</li>
     *   <li>Si {@code awaitTermination} es interrumpido, forzar cierre y restaurar flag</li>
     *   <li>Registrar detención en log</li>
     * </ol>
     *
     * <p><b>Thread-safety:</b></p>
     * Este método puede ser llamado desde cualquier hilo (típicamente desde el
     * shutdown hook en {@code Main.cerrarServidor()}).
     *
     * <p><b>Garantías:</b></p>
     * <ul>
     *   <li>No se programarán nuevos ticks después de shutdown()</li>
     *   <li>El tick actual se completa normalmente (hasta 5 segundos de timeout)</li>
     *   <li>Recursos del ExecutorService son liberados</li>
     *   <li>El motor puede ser reiniciado con {@link #start()} tras la detención</li>
     * </ul>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * // En shutdown hook del servidor
     * private static void cerrarServidor() {
     *     if (motorCocodrilos != null) {
     *         motorCocodrilos.stop(); // Detiene el motor ordenadamente
     *     }
     * }
     * }</pre>
     *
     * @see #start()
     * @see ScheduledExecutorService#shutdown()
     * @see ScheduledExecutorService#awaitTermination(long, TimeUnit)
     */
    public void stop() {
        // Si no está ejecutando, no hay nada que detener
        if (!ejecutando.get()) {
            LoggerUtil.warning("El motor no está ejecutando");
            return;
        }

        // Marcar como no ejecutando
        ejecutando.set(false);

        if (ejecutor != null) {
            // Iniciar cierre ordenado (no acepta nuevas tareas)
            ejecutor.shutdown();

            try {
                // Esperar hasta 5 segundos a que termine el tick actual
                if (!ejecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    // Si no terminó en 5 segundos, forzar cierre inmediato
                    // shutdownNow() intenta interrumpir las tareas en ejecución
                    ejecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                // Si este hilo es interrumpido mientras espera, forzar cierre
                ejecutor.shutdownNow();
                // Restaurar el flag de interrupción del hilo actual
                Thread.currentThread().interrupt();
            }
        }

        LoggerUtil.info("MotorCocodrilos detenido");
    }

    /**
     * Ejecuta un tick manual del motor con delta time específico.
     *
     * <p>Útil para testing y debugging. Ejecuta la secuencia completa de un tick
     * (procesar comandos, actualizar cocodrilos, limpiar eliminados) de forma
     * sincronizada, ignorando el factor de dificultad.</p>
     *
     * <p><b>Uso principal:</b> Testing unitario del motor sin necesidad de
     * iniciar el ScheduledExecutorService.</p>
     *
     * <p><b>Diferencia con tick automático:</b></p>
     * <ul>
     *   <li>Tick automático: usa {@code dtFijo * factorDificultad}</li>
     *   <li>Tick manual: usa el {@code dt} proporcionado directamente</li>
     * </ul>
     *
     * <p><b>Thread-safety:</b> Sincronizado mediante lock, puede ser llamado
     * desde cualquier hilo.</p>
     *
     * <p><b>Ejemplo de uso en tests:</b></p>
     * <pre>{@code
     * MotorCocodrilos motor = new MotorCocodrilos(0.1);
     * // NO llamar a start() en tests
     * motor.registrarLiana(liana);
     * motor.crearCocodriloRojo(0, 100.0, 60.0, -1);
     * motor.tickForzado(0.1); // Ejecutar un tick manual
     * SnapshotSistemaCocodrilos snap = motor.getSnapshot();
     * // Verificar que el cocodrilo se movió...
     * }</pre>
     *
     * @param dt Delta time en segundos a usar para este tick
     * @see #ejecutarTick()
     */
    public void tickForzado(double dt) {
        synchronized (lock) {
            procesarColaComandos();
            actualizarCocodrilos(dt);
            limpiarCocodrilosEliminados();
        }
    }

    // ==================== CREACIÓN DE COCODRILOS ====================

    /**
     * Crea un cocodrilo rojo y lo agrega al sistema mediante la cola de comandos.
     *
     * <p><b>Cocodrilo Rojo:</b> Patrulla entre {@code alturaMin} y {@code alturaMax}
     * de la liana, rebotando en los límites. Dirección inicial configurable.</p>
     *
     * <p><b>Validaciones realizadas:</b></p>
     * <ol>
     *   <li>Verificar que la liana existe en {@link #lianas}</li>
     *   <li>Verificar que yInicial está en rango [alturaMin, alturaMax] de la liana</li>
     * </ol>
     *
     * <p>Si las validaciones fallan, retorna null y registra advertencia en log.</p>
     *
     * <p><b>Proceso de creación (asíncrono):</b></p>
     * <ol>
     *   <li>Generar ID único mediante {@link #generarId(String)}</li>
     *   <li>Encolar comando de creación en {@link #colaComandos}</li>
     *   <li>Retornar ID inmediatamente (el cocodrilo se crea en el siguiente tick)</li>
     * </ol>
     *
     * <p><b>Comando encolado:</b></p>
     * <pre>{@code
     * colaComandos.offer(() -> {
     *     CocodriloRojo croc = FactoryEntidad.crearCocodriloRojo(...);
     *     cocodrilos.put(id, croc);
     *     telemetria.registrarCreacion();
     * });
     * }</pre>
     *
     * <p><b>Thread-safety:</b> Esta operación es thread-safe gracias a la cola concurrente.
     * Puede ser llamada desde cualquier hilo (GameManager, AdminGUI, etc.).</p>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * // Crear cocodrilo rojo en liana 0, posición Y=150, velocidad 60 u/s, dirección hacia arriba
     * String id = motor.crearCocodriloRojo(0, 150.0, 60.0, -1);
     * if (id != null) {
     *     System.out.println("Cocodrilo creado con ID: " + id);
     * } else {
     *     System.out.println("Error: No se pudo crear el cocodrilo");
     * }
     * }</pre>
     *
     * @param lianaId ID de la liana donde crear el cocodrilo (debe estar registrada)
     * @param yInicial Posición Y inicial del cocodrilo (debe estar en rango de la liana)
     * @param velocidadBase Velocidad base en unidades por segundo (típicamente 60.0)
     * @param direccion Dirección inicial de movimiento: {@code -1} = arriba, {@code +1} = abajo
     * @return ID único del cocodrilo creado, o {@code null} si hubo error de validación
     * @see CocodriloRojo
     * @see FactoryEntidad#crearCocodriloRojo
     * @see #crearCocodriloAzul
     */
    public String crearCocodriloRojo(int lianaId, double yInicial,
                                     double velocidadBase, int direccion) {
        // Validar que la liana existe
        Liana liana = lianas.get(lianaId);
        if (liana == null) {
            LoggerUtil.warning("No existe liana con ID " + lianaId);
            return null;
        }

        // Validar que yInicial está en el rango de la liana
        if (!liana.estaEnRango(yInicial)) {
            LoggerUtil.warning("yInicial fuera de rango de la liana");
            return null;
        }

        // Generar ID único para el cocodrilo
        String id = generarId("ROJO");

        // Encolar comando de creación (thread-safe, no bloquea)
        colaComandos.offer(() -> {
            try {
                // Usar Factory para crear el cocodrilo (patrón Factory Method)
                CocodriloRojo cocodrilo = FactoryEntidad.crearCocodriloRojo(
                        id, lianaId, yInicial, velocidadBase, direccion,
                        liana.getAlturaMin(), liana.getAlturaMax()
                );
                // Insertar en el mapa de cocodrilos activos
                cocodrilos.put(id, cocodrilo);
                // Registrar métrica en telemetría
                telemetria.registrarCreacion();
                LoggerUtil.debug("Cocodrilo rojo creado: " + id);
            } catch (Exception e) {
                LoggerUtil.warning("Error creando cocodrilo rojo: " + e.getMessage());
            }
        });

        return id;
    }

    /**
     * Crea un cocodrilo azul y lo agrega al sistema mediante la cola de comandos.
     *
     * <p><b>Cocodrilo Azul:</b> Cae continuamente desde {@code yInicial} hacia abajo,
     * eliminándose al alcanzar {@code alturaMax} de la liana (nivel del agua).</p>
     *
     * <p><b>Validaciones realizadas:</b></p>
     * <ol>
     *   <li>Verificar que la liana existe en {@link #lianas}</li>
     *   <li>yInicial no se valida explícitamente (puede estar fuera de rango para efectos especiales)</li>
     * </ol>
     *
     * <p><b>Proceso de creación (asíncrono):</b></p>
     * <ol>
     *   <li>Generar ID único mediante {@link #generarId(String)}</li>
     *   <li>Encolar comando de creación en {@link #colaComandos}</li>
     *   <li>Retornar ID inmediatamente (el cocodrilo se crea en el siguiente tick)</li>
     * </ol>
     *
     * <p><b>Thread-safety:</b> Operación thread-safe mediante cola concurrente.</p>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * // Crear cocodrilo azul en liana 1, posición Y=200, velocidad 50 u/s
     * String id = motor.crearCocodriloAzul(1, 200.0, 50.0);
     * if (id != null) {
     *     System.out.println("Cocodrilo azul creado con ID: " + id);
     * }
     * }</pre>
     *
     * @param lianaId ID de la liana donde crear el cocodrilo (debe estar registrada)
     * @param yInicial Posición Y inicial del cocodrilo
     * @param velocidadBase Velocidad base de caída en unidades por segundo (típicamente 50.0)
     * @return ID único del cocodrilo creado, o {@code null} si hubo error de validación
     * @see CocodriloAzul
     * @see FactoryEntidad#crearCocodriloAzul
     * @see #crearCocodriloRojo
     */
    public String crearCocodriloAzul(int lianaId, double yInicial,
                                     double velocidadBase) {
        // Validar que la liana existe
        Liana liana = lianas.get(lianaId);
        if (liana == null) {
            LoggerUtil.warning("No existe liana con ID " + lianaId);
            return null;
        }

        // Generar ID único
        String id = generarId("AZUL");

        // Encolar comando de creación (thread-safe)
        colaComandos.offer(() -> {
            try {
                // Usar Factory para crear cocodrilo azul
                CocodriloAzul cocodrilo = FactoryEntidad.crearCocodriloAzul(
                        id, lianaId, yInicial, velocidadBase,
                        liana.getAlturaMax() // Límite inferior = alturaMax de liana (nivel del agua)
                );
                cocodrilos.put(id, cocodrilo);
                telemetria.registrarCreacion();
                LoggerUtil.debug("Cocodrilo azul creado: " + id);
            } catch (Exception e) {
                LoggerUtil.warning("Error creando cocodrilo azul: " + e.getMessage());
            }
        });

        return id;
    }

    /**
     * Elimina un cocodrilo del sistema por su ID.
     *
     * <p>Encola un comando que marca el cocodrilo como ELIMINADO.
     * El cocodrilo será removido del mapa en el siguiente tick durante
     * {@link #limpiarCocodrilosEliminados()}.</p>
     *
     * <p><b>Proceso de eliminación (asíncrono):</b></p>
     * <ol>
     *   <li>Encolar comando que llama a {@code cocodrilo.eliminar()}</li>
     *   <li>En el siguiente tick, {@code cocodrilo.isEliminado()} retorna true</li>
     *   <li>{@link #limpiarCocodrilosEliminados()} remueve el cocodrilo del mapa</li>
     *   <li>Incrementar contador de telemetría</li>
     * </ol>
     *
     * <p><b>Comportamiento ante ID inválido:</b> Si el ID no existe en el mapa,
     * el comando no hace nada (verificación dentro del comando encolado).</p>
     *
     * <p><b>Thread-safety:</b> Operación thread-safe mediante cola concurrente.</p>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * String id = motor.crearCocodriloRojo(0, 100.0, 60.0, -1);
     * // ... después de un tiempo ...
     * motor.eliminarCocodrilo(id); // Marca para eliminación
     * // El cocodrilo desaparece del sistema en el siguiente tick
     * }</pre>
     *
     * @param id ID del cocodrilo a eliminar
     * @see Cocodrilo#eliminar()
     * @see #limpiarCocodrilosEliminados()
     */
    public void eliminarCocodrilo(String id) {
        colaComandos.offer(() -> {
            Cocodrilo cocodrilo = cocodrilos.get(id);
            if (cocodrilo != null) {
                cocodrilo.eliminar(); // Marca estado como ELIMINADO
                LoggerUtil.debug("Cocodrilo eliminado: " + id);
            }
        });
    }

    // ==================== DIFICULTAD ====================

    /**
     * Incrementa el factor de dificultad, aumentando la velocidad efectiva de todos los cocodrilos.
     *
     * <p><b>Efecto:</b> Multiplica el {@link #factorDificultad} actual por el multiplicador
     * proporcionado. Esto escala la velocidad de TODOS los cocodrilos proporcionalmente.</p>
     *
     * <p><b>Fórmula:</b></p>
     * <pre>
     * factorDificultad_nuevo = factorDificultad_actual * multiplicador
     * velocidadEfectiva = velocidadBase * factorDificultad_nuevo
     * </pre>
     *
     * <p><b>Ejemplo de progresión:</b></p>
     * <pre>
     * Inicial: factorDificultad = 1.0
     * Rescate 1: incrementarDificultad(1.10) → factor = 1.10 (+10%)
     * Rescate 2: incrementarDificultad(1.10) → factor = 1.21 (+21%)
     * Rescate 3: incrementarDificultad(1.10) → factor = 1.33 (+33%)
     * </pre>
     *
     * <p><b>Uso típico:</b> Llamado por {@code GameManager.iniciarCelebracion()}
     * cuando un jugador completa un rescate exitoso, usando el valor de
     * {@code Config.COCODRILO_INCREMENTO_DIFICULTAD} (típicamente 1.10).</p>
     *
     * <p><b>Thread-safety:</b> Sincronizado mediante lock para garantizar
     * atomicidad de la operación de multiplicación.</p>
     *
     * @param multiplicador Factor multiplicador a aplicar (debe ser {@code > 1.0})
     * @throws IllegalArgumentException Si {@code multiplicador <= 1.0}
     * @see #getFactorDificultad()
     */
    public void incrementarDificultad(double multiplicador) {
        if (multiplicador <= 1.0) {
            throw new IllegalArgumentException("Multiplicador debe ser > 1.0");
        }

        synchronized (lock) {
            this.factorDificultad *= multiplicador;
            LoggerUtil.info("Dificultad aumentada a: " + String.format("%.2f", factorDificultad));
        }
    }

    // ==================== REGISTRO DE LIANAS ====================

    /**
     * Registra una liana en el sistema para validación de rangos.
     *
     * <p>Las lianas deben ser registradas ANTES de poder crear cocodrilos en ellas.
     * El registro proporciona:</p>
     * <ul>
     *   <li>Validación de existencia de la liana en {@link #crearCocodriloRojo} / {@link #crearCocodriloAzul}</li>
     *   <li>Acceso a {@code alturaMin} y {@code alturaMax} para validación de posición Y</li>
     *   <li>Parámetros necesarios para física correcta del cocodrilo</li>
     * </ul>
     *
     * <p><b>Típicamente llamado desde:</b> {@code GameManager.inicializarLianas()}</p>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * MotorCocodrilos motor = new MotorCocodrilos(0.1);
     *
     * // Registrar lianas antes de crear cocodrilos
     * motor.registrarLiana(new Liana("L_0", 0, 0, 0, 0.0, 500.0));
     * motor.registrarLiana(new Liana("L_1", 0, 0, 1, 0.0, 500.0));
     *
     * // Ahora podemos crear cocodrilos en esas lianas
     * motor.crearCocodriloRojo(0, 100.0, 60.0, -1);
     * }</pre>
     *
     * @param liana Liana a registrar en el sistema (no debe ser null)
     * @see Liana
     * @see #crearCocodriloRojo
     * @see #crearCocodriloAzul
     */
    public void registrarLiana(Liana liana) {
        lianas.put(liana.getLiana(), liana);
        LoggerUtil.debug("Liana registrada: " + liana.getId());
    }

    // ==================== SNAPSHOTS ====================

    /**
     * Obtiene un snapshot inmutable del estado actual completo del sistema de cocodrilos.
     *
     * <p>Proporciona acceso thread-safe al estado sin bloqueos prolongados.
     * Típicamente usado por {@code GameManager.sincronizarCocodrilosDesdeMotor()}
     * para detección de colisiones.</p>
     *
     * <p><b>Contenido del snapshot:</b></p>
     * <ul>
     *   <li>Lista inmutable de {@link SnapshotCocodrilo} de todos los cocodrilos activos</li>
     *   <li>{@link #factorDificultad} actual</li>
     *   <li>{@link #tickActual} (número de tick)</li>
     *   <li>Cantidad de cocodrilos activos</li>
     *   <li>Cocodrilos eliminados (de telemetría)</li>
     * </ul>
     *
     * <p><b>Thread-safety:</b> Sincronizado mediante lock. El snapshot se genera
     * atómicamente, congelando el estado en un instante específico.</p>
     *
     * <p><b>Inmutabilidad:</b> El snapshot retornado es completamente inmutable.
     * Los cambios en el motor no afectan snapshots anteriores.</p>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * SnapshotSistemaCocodrilos snapshot = motor.getSnapshot();
     *
     * System.out.println("Tick actual: " + snapshot.getTickActual());
     * System.out.println("Factor dificultad: " + snapshot.getFactorDificultad());
     * System.out.println("Cocodrilos activos: " + snapshot.getTotalCocodrilosActivos());
     *
     * for (SnapshotCocodrilo croc : snapshot.getCocodrilos()) {
     *     System.out.println("  - " + croc.getId() + " en liana " + croc.getLianaId());
     * }
     * }</pre>
     *
     * @return Snapshot inmutable del estado completo del sistema
     * @see SnapshotSistemaCocodrilos
     * @see SnapshotCocodrilo
     */
    public SnapshotSistemaCocodrilos getSnapshot() {
        synchronized (lock) {
            // Convertir todos los cocodrilos a snapshots inmutables
            List<SnapshotCocodrilo> snapshots = cocodrilos.values().stream()
                    .map(SnapshotCocodrilo::new) // Crear snapshot de cada cocodrilo
                    .collect(Collectors.toList());

            // Contar cocodrilos activos (no marcados como ELIMINADO)
            int activos = (int) cocodrilos.values().stream()
                    .filter(Cocodrilo::isActivo)
                    .count();

            // Crear snapshot del sistema completo
            return new SnapshotSistemaCocodrilos(
                    snapshots,
                    factorDificultad,
                    tickActual.get(),
                    activos,
                    telemetria.getCocodrilosEliminados()
            );
        }
    }

    /**
     * Obtiene la instancia de telemetría del motor para consulta de métricas.
     *
     * <p>Proporciona acceso a métricas en tiempo real sin necesidad de
     * sincronización (la telemetría usa atomics internamente).</p>
     *
     * <p><b>Uso típico:</b></p>
     * <pre>{@code
     * TelemetriaCocodrilos tel = motor.getTelemetria();
     * System.out.println(tel.generarReporte());
     * System.out.println("TPS promedio: " + tel.getTicksPorSegundoPromedio());
     * System.out.println("Última duración: " + tel.getUltimoTickDuracionMs() + "ms");
     * }</pre>
     *
     * @return Instancia de {@link TelemetriaCocodrilos} del motor
     * @see TelemetriaCocodrilos
     */
    public TelemetriaCocodrilos getTelemetria() {
        return telemetria;
    }

    // ==================== MÉTODOS PRIVADOS DEL CICLO DE ACTUALIZACIÓN ====================

    /**
     * Ejecuta un tick completo del motor (llamado automáticamente por el executor).
     *
     * <p>Este método es invocado cada {@link #dtFijoMs} milisegundos por el
     * {@link ScheduledExecutorService}. Implementa el bucle principal del motor.</p>
     *
     * <p><b>Secuencia de ejecución:</b></p>
     * <ol>
     *   <li>Verificar que {@link #ejecutando} sea true (prevenir ejecución tras detención)</li>
     *   <li>Capturar timestamp de inicio del tick</li>
     *   <li>Dentro de synchronized(lock):
     *       <ul>
     *         <li>{@link #procesarColaComandos()} - Crear/eliminar cocodrilos pendientes</li>
     *         <li>{@link #actualizarCocodrilos(double)} - Actualizar física con dt escalado</li>
     *         <li>{@link #limpiarCocodrilosEliminados()} - Remover cocodrilos marcados</li>
     *         <li>Incrementar {@link #tickActual}</li>
     *       </ul>
     *   </li>
     *   <li>Capturar timestamp de fin y calcular duración</li>
     *   <li>Registrar métricas en {@link #telemetria}</li>
     *   <li>Si duración {@code > dtFijoMs}, registrar advertencia de lag</li>
     * </ol>
     *
     * <p><b>Manejo de errores:</b></p>
     * Cualquier excepción lanzada durante el tick es capturada, registrada
     * en el log, e impreso el stack trace. El motor continúa ejecutando
     * en el siguiente tick programado.
     *
     * <p><b>Escalado de dificultad:</b></p>
     * El deltaTime efectivo se calcula como {@code dtFijo * factorDificultad},
     * acelerando todos los cocodrilos proporcionalmente.
     *
     * <p><b>Detección de lag:</b></p>
     * Si la duración del tick excede {@link #dtFijoMs}, se registra una
     * advertencia con la desviación. Ejemplo:
     * <pre>
     * Tick lag: 120ms (esperado: 100ms)
     * </pre>
     *
     * @see #start()
     * @see #procesarColaComandos()
     * @see #actualizarCocodrilos(double)
     * @see #limpiarCocodrilosEliminados()
     */
    private void ejecutarTick() {
        // Prevenir ejecución si el motor fue detenido
        if (!ejecutando.get()) return;

        // Capturar timestamp de inicio para medir duración del tick
        long inicio = System.currentTimeMillis();

        try {
            synchronized (lock) {
                // 1. Procesar comandos pendientes (crear/eliminar cocodrilos)
                procesarColaComandos();

                // 2. Actualizar física de cocodrilos con dt escalado por dificultad
                actualizarCocodrilos(dtFijo * factorDificultad);

                // 3. Limpiar cocodrilos marcados como ELIMINADO
                limpiarCocodrilosEliminados();

                // 4. Incrementar contador de tick
                tickActual.incrementAndGet();
            }
        } catch (Exception e) {
            // Capturar cualquier excepción para evitar que el motor se detenga
            LoggerUtil.warning("Error en tick del motor: " + e.getMessage());
            e.printStackTrace(); // Stack trace completo para debugging
            // El bucle continúa en el siguiente tick programado
        }

        // Calcular duración del tick
        long duracion = System.currentTimeMillis() - inicio;

        // Registrar métricas en telemetría
        telemetria.registrarTick(duracion);

        // Advertir si el tick tomó más tiempo del esperado (lag)
        if (duracion > dtFijoMs) {
            LoggerUtil.warning(String.format("Tick lag: %dms (esperado: %dms)",
                    duracion, dtFijoMs));
        }
    }

    /**
     * Procesa todos los comandos pendientes en la cola (creación/eliminación).
     *
     * <p>Consume comandos de {@link #colaComandos} hasta que la cola esté vacía.
     * Cada comando es un {@link Runnable} que típicamente crea o elimina un cocodrilo.</p>
     *
     * <p><b>Comportamiento:</b></p>
     * <ul>
     *   <li>Itera mientras {@code colaComandos.poll()} retorne un comando</li>
     *   <li>Ejecuta cada comando mediante {@code comando.run()}</li>
     *   <li>Captura excepciones de comandos individuales para continuar procesando</li>
     * </ul>
     *
     * <p><b>Thread-safety:</b></p>
     * Este método se ejecuta dentro del lock en {@link #ejecutarTick()},
     * garantizando que solo un hilo procesa comandos a la vez.
     *
     * <p><b>Manejo de errores:</b></p>
     * Si un comando lanza excepción, se registra advertencia en el log
     * pero se continúa procesando los comandos restantes.
     *
     * @see #crearCocodriloRojo
     * @see #crearCocodriloAzul
     * @see #eliminarCocodrilo
     */
    private void procesarColaComandos() {
        Runnable comando;
        // Consumir todos los comandos encolados
        while ((comando = colaComandos.poll()) != null) {
            try {
                comando.run(); // Ejecutar comando (crear/eliminar cocodrilo)
            } catch (Exception e) {
                // Registrar error pero continuar con comandos restantes
                LoggerUtil.warning("Error ejecutando comando: " + e.getMessage());
            }
        }
    }

    /**
     * Actualiza la física de todos los cocodrilos activos con orden determinista.
     *
     * <p><b>Orden determinista:</b> Los cocodrilos se ordenan por ID alfabéticamente
     * antes de actualizar. Esto garantiza reproducibilidad en tests y debugging.</p>
     *
     * <p><b>Proceso de actualización:</b></p>
     * <ol>
     *   <li>Copiar {@code cocodrilos.values()} a una lista</li>
     *   <li>Ordenar lista por {@code Cocodrilo.getId()}</li>
     *   <li>Para cada cocodrilo activo, llamar a {@code cocodrilo.mover(dt)}</li>
     * </ol>
     *
     * <p><b>Filtrado:</b> Solo se actualizan cocodrilos con {@code isActivo() == true}.
     * Los cocodrilos marcados como ELIMINADO se ignoran y serán removidos en
     * {@link #limpiarCocodrilosEliminados()}.</p>
     *
     * <p><b>Delta time escalado:</b></p>
     * El parámetro {@code dt} ya viene escalado por {@link #factorDificultad}
     * desde {@link #ejecutarTick()}:
     * <pre>
     * dt = dtFijo * factorDificultad
     * </pre>
     *
     * @param dt Delta time efectivo en segundos (ya escalado por dificultad)
     * @see Cocodrilo#mover(double)
     * @see CocodriloRojo#mover(double)
     * @see CocodriloAzul#mover(double)
     */
    private void actualizarCocodrilos(double dt) {
        // Copiar cocodrilos a lista para ordenamiento determinista
        List<Cocodrilo> lista = new ArrayList<>(cocodrilos.values());

        // Ordenar por ID para reproducibilidad
        lista.sort(Comparator.comparing(Cocodrilo::getId));

        // Actualizar cada cocodrilo activo
        for (Cocodrilo cocodrilo : lista) {
            if (cocodrilo.isActivo()) {
                cocodrilo.mover(dt); // Delegar actualización física al cocodrilo
            }
        }
    }

    /**
     * Limpia cocodrilos marcados como ELIMINADO del mapa de cocodrilos activos.
     *
     * <p>Utiliza {@link Map#removeIf} para remover eficientemente todos los
     * cocodrilos que retornan {@code true} en {@code isEliminado()}.</p>
     *
     * <p><b>Proceso de limpieza:</b></p>
     * <ol>
     *   <li>Iterar sobre {@code cocodrilos.values()}</li>
     *   <li>Para cada cocodrilo, verificar {@code c.isEliminado()}</li>
     *   <li>Si es true:
     *       <ul>
     *         <li>Incrementar contador en {@link #telemetria}</li>
     *         <li>Remover del mapa</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * <p><b>Cuándo se marca un cocodrilo como ELIMINADO:</b></p>
     * <ul>
     *   <li>Llamada a {@link #eliminarCocodrilo(String)} por GameManager o AdminGUI</li>
     *   <li>Cocodrilo azul alcanza {@code alturaMax} (nivel del agua)</li>
     *   <li>Cocodrilo rojo rebota en límites y se marca para eliminación (caso especial)</li>
     * </ul>
     *
     * @see Cocodrilo#isEliminado()
     * @see Cocodrilo#eliminar()
     * @see TelemetriaCocodrilos#registrarEliminacion()
     */
    private void limpiarCocodrilosEliminados() {
        // Remover cocodrilos eliminados y actualizar telemetría
        cocodrilos.values().removeIf(c -> {
            if (c.isEliminado()) {
                telemetria.registrarEliminacion(); // Incrementar contador
                return true; // Remover del mapa
            }
            return false; // Mantener en el mapa
        });
    }

    /**
     * Genera un ID único para un cocodrilo recién creado.
     *
     * <p><b>Formato del ID:</b></p>
     * <pre>
     * CROC_{TIPO}_{TIMESTAMP}_{CONTADOR}
     * </pre>
     *
     * <p><b>Componentes:</b></p>
     * <ul>
     *   <li><b>CROC</b>: Prefijo fijo (CROCodrilo)</li>
     *   <li><b>TIPO</b>: "ROJO" o "AZUL" según el parámetro</li>
     *   <li><b>TIMESTAMP</b>: {@code System.currentTimeMillis()} (13 dígitos)</li>
     *   <li><b>CONTADOR</b>: {@link #contadorIds} incrementado atómicamente</li>
     * </ul>
     *
     * <p><b>Ejemplos:</b></p>
     * <pre>
     * CROC_ROJO_1705345678901_1
     * CROC_AZUL_1705345678905_2
     * CROC_ROJO_1705345678905_3
     * </pre>
     *
     * <p><b>Unicidad:</b></p>
     * La combinación de timestamp + contador garantiza unicidad incluso
     * si se crean múltiples cocodrilos en el mismo milisegundo.
     *
     * <p><b>Thread-safety:</b> {@link AtomicInteger#incrementAndGet()} garantiza
     * que cada ID tenga un contador único sin necesidad de sincronización.</p>
     *
     * @param tipo Tipo de cocodrilo: "ROJO" o "AZUL"
     * @return ID único del cocodrilo
     */
    private String generarId(String tipo) {
        return String.format("CROC_%s_%d_%d",
                tipo, System.currentTimeMillis(), contadorIds.incrementAndGet());
    }

    // ==================== GETTERS ====================

    /**
     * Verifica si el motor está actualmente ejecutando ticks.
     *
     * @return {@code true} si el motor está ejecutando, {@code false} en caso contrario
     * @see #start()
     * @see #stop()
     */
    public boolean isEjecutando() {
        return ejecutando.get();
    }

    /**
     * Obtiene el delta time fijo configurado para este motor.
     *
     * @return Delta time en segundos (ejemplo: 0.1 para 10 TPS)
     */
    public double getDtFijo() {
        return dtFijo;
    }

    /**
     * Obtiene el factor de dificultad actual del motor.
     *
     * <p>Rango: {@code >= 1.0}. Valores típicos:</p>
     * <ul>
     *   <li>1.0: Dificultad base sin modificación</li>
     *   <li>1.5: Velocidad 1.5x</li>
     *   <li>2.0: Velocidad 2.0x</li>
     * </ul>
     *
     * @return Factor de dificultad actual
     * @see #incrementarDificultad(double)
     */
    public double getFactorDificultad() {
        return factorDificultad;
    }

    /**
     * Obtiene el número de tick actual del motor.
     *
     * <p>Se incrementa en 1 por cada tick completado exitosamente.
     * Útil para debugging y sincronización.</p>
     *
     * @return Número de tick actual (comienza en 0)
     */
    public long getTickActual() {
        return tickActual.get();
    }

    /**
     * Obtiene el número total de cocodrilos en el sistema (activos + eliminados pendientes).
     *
     * <p><b>Nota:</b> Incluye cocodrilos marcados como ELIMINADO que aún no han sido
     * limpiados. Para obtener solo activos, usar {@code getSnapshot().getTotalCocodrilosActivos()}.</p>
     *
     * @return Número total de cocodrilos en el mapa
     */
    public int getCantidadCocodrilos() {
        return cocodrilos.size();
    }

    /**
     * Obtiene el número de lianas registradas en el motor.
     *
     * @return Número de lianas registradas
     * @see #registrarLiana(Liana)
     */
    public int getCantidadLianas() {
        return lianas.size();
    }
}
