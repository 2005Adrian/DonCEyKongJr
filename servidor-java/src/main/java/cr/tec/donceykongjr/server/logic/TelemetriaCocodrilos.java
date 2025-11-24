package cr.tec.donceykongjr.server.logic;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sistema de telemetría thread-safe para el motor de cocodrilos en DonCEy Kong Jr.
 *
 * <p>Esta clase captura y almacena métricas de rendimiento y contadores de operaciones
 * del {@link MotorCocodrilos}, permitiendo monitoreo en tiempo real del subsistema
 * de enemigos sin afectar el rendimiento del game loop.</p>
 *
 * <p><b>Categorías de métricas capturadas:</b></p>
 * <ul>
 *   <li><b>Contadores:</b> Cocodrilos creados, eliminados, ticks ejecutados</li>
 *   <li><b>Performance:</b> Tiempo por tick (promedio, último, desviación)</li>
 *   <li><b>Estado:</b> Tiempo de ejecución total, TPS promedio, uptime</li>
 * </ul>
 *
 * <p><b>Thread-safety:</b></p>
 * Toda la clase es thread-safe mediante el uso de tipos atómicos ({@link AtomicInteger},
 * {@link AtomicLong}) y variables volátiles. No requiere sincronización externa
 * para lecturas ni escrituras.
 *
 * <p><b>Tipos atómicos utilizados:</b></p>
 * <ul>
 *   <li>{@link AtomicInteger}: Para contadores que se incrementan frecuentemente</li>
 *   <li>{@link AtomicLong}: Para acumuladores de tiempo de gran magnitud</li>
 *   <li>volatile double: Para métricas calculadas que se leen desde múltiples hilos</li>
 * </ul>
 *
 * <p><b>Métricas principales:</b></p>
 * <pre>
 * - cocodrilosCreados: Total de cocodrilos creados desde inicio del motor
 * - cocodrilosEliminados: Total de cocodrilos eliminados (manual o automático)
 * - ticksEjecutados: Número total de ticks completados
 * - tiempoTotalEjecucionMs: Suma de duración de todos los ticks
 * - ultimoTickDuracionMs: Duración del último tick en milisegundos
 * - ultimoTickDesviacionMs: Desviación del último tick respecto al dt esperado
 * - promedioTickDuracionMs: Promedio móvil de duración de ticks
 * </pre>
 *
 * <p><b>Uso típico:</b></p>
 * <pre>{@code
 * // En MotorCocodrilos
 * TelemetriaCocodrilos telemetria = new TelemetriaCocodrilos(100.0); // dt esperado = 100ms
 *
 * // Al crear cocodrilo
 * telemetria.registrarCreacion();
 *
 * // Al eliminar cocodrilo
 * telemetria.registrarEliminacion();
 *
 * // En cada tick
 * long inicio = System.currentTimeMillis();
 * // ... ejecutar lógica del tick ...
 * long duracion = System.currentTimeMillis() - inicio;
 * telemetria.registrarTick(duracion);
 *
 * // Consultar métricas
 * System.out.println(telemetria.generarReporte());
 * System.out.println("TPS promedio: " + telemetria.getTicksPorSegundoPromedio());
 * }</pre>
 *
 * <p><b>Performance:</b></p>
 * Las operaciones de registro (incremento de atomics) son extremadamente rápidas
 * (sub-microsegundo en hardware moderno). El cálculo de métricas derivadas
 * (promedios, TPS) se realiza on-demand en los getters, sin overhead en el tick.
 *
 * <p><b>Ejemplo de reporte generado:</b></p>
 * <pre>
 * === TELEMETRÍA MOTOR COCODRILOS ===
 * Uptime: 120.50 segundos
 * Cocodrilos creados: 45
 * Cocodrilos eliminados: 12
 * Cocodrilos activos: 33
 * Ticks ejecutados: 1205
 * TPS promedio: 10.00
 * Duración promedio tick: 2.30 ms
 * Último tick: 3 ms (desviación: +1.00 ms)
 * dt esperado: 100.00 ms
 * </pre>
 *
 * @author DonCEyKongJr Team
 * @version 1.0
 * @see MotorCocodrilos
 * @see AtomicInteger
 * @see AtomicLong
 */
public class TelemetriaCocodrilos {

    // ==================== CONTADORES ATÓMICOS (THREAD-SAFE) ====================

    /**
     * Contador atómico de cocodrilos creados desde el inicio del motor.
     *
     * <p>Se incrementa cada vez que {@link #registrarCreacion()} es llamado
     * (típicamente desde {@link MotorCocodrilos#crearCocodriloRojo} o
     * {@link MotorCocodrilos#crearCocodriloAzul}).</p>
     *
     * <p><b>Thread-safety:</b> {@link AtomicInteger#incrementAndGet()} garantiza
     * operación atómica sin necesidad de sincronización.</p>
     *
     * @see #registrarCreacion()
     * @see #getCocodrilosCreados()
     */
    private final AtomicInteger cocodrilosCreados;

    /**
     * Contador atómico de cocodrilos eliminados desde el inicio del motor.
     *
     * <p>Se incrementa cada vez que {@link #registrarEliminacion()} es llamado
     * (típicamente desde {@link MotorCocodrilos#limpiarCocodrilosEliminados()}).</p>
     *
     * <p><b>Causas de eliminación:</b></p>
     * <ul>
     *   <li>Eliminación manual vía {@code MotorCocodrilos.eliminarCocodrilo(id)}</li>
     *   <li>Cocodrilo azul alcanza el nivel del agua (alturaMax)</li>
     *   <li>Cocodrilo rojo marcado para eliminación (casos especiales)</li>
     * </ul>
     *
     * @see #registrarEliminacion()
     * @see #getCocodrilosEliminados()
     */
    private final AtomicInteger cocodrilosEliminados;

    /**
     * Contador atómico de ticks ejecutados completamente desde el inicio del motor.
     *
     * <p>Se incrementa en cada llamada a {@link #registrarTick(long)},
     * típicamente al final de {@link MotorCocodrilos#ejecutarTick()}.</p>
     *
     * <p><b>Uso:</b> Permite calcular TPS promedio y validar que el motor
     * está ejecutando correctamente.</p>
     *
     * @see #registrarTick(long)
     * @see #getTicksEjecutados()
     * @see #getTicksPorSegundoPromedio()
     */
    private final AtomicLong ticksEjecutados;

    // ==================== MÉTRICAS DE TIEMPO ====================

    /**
     * Acumulador atómico del tiempo total de ejecución de todos los ticks en milisegundos.
     *
     * <p>Suma la duración de cada tick registrado. Utilizado para calcular
     * {@link #promedioTickDuracionMs} y {@link #getTicksPorSegundoPromedio()}.</p>
     *
     * <p><b>Cálculo:</b></p>
     * <pre>
     * tiempoTotalEjecucionMs += duracionTickMs (en cada tick)
     * promedioTickDuracionMs = tiempoTotalEjecucionMs / ticksEjecutados
     * </pre>
     *
     * <p><b>Thread-safety:</b> {@link AtomicLong#addAndGet(long)} garantiza
     * adición atómica sin race conditions.</p>
     *
     * @see #registrarTick(long)
     * @see #getTiempoTotalEjecucionMs()
     * @see #getPromedioTickDuracionMs()
     */
    private final AtomicLong tiempoTotalEjecucionMs;

    /**
     * Duración del último tick ejecutado, en milisegundos.
     *
     * <p>Actualizado en cada llamada a {@link #registrarTick(long)}.
     * Útil para monitoreo en tiempo real de la carga del motor.</p>
     *
     * <p><b>Interpretación:</b></p>
     * <ul>
     *   <li>Valor típico: 1-5ms para ~100 cocodrilos a 10 TPS</li>
     *   <li>Valor alto (>10ms): Posible sobrecarga, considerar optimización</li>
     *   <li>Valor muy alto (>dtEsperadoMs): Lag detectado, advertencia emitida</li>
     * </ul>
     *
     * @see #registrarTick(long)
     * @see #getUltimoTickDuracionMs()
     */
    private final AtomicLong ultimoTickDuracionMs;

    /**
     * Desviación del último tick respecto al delta time esperado, en milisegundos.
     *
     * <p><b>Cálculo:</b></p>
     * <pre>
     * ultimoTickDesviacionMs = duracionMs - dtEsperadoMs
     * </pre>
     *
     * <p><b>Interpretación:</b></p>
     * <ul>
     *   <li>Valor positivo: Tick tardó más de lo esperado (lag)</li>
     *   <li>Valor negativo: Tick fue más rápido de lo esperado (normal)</li>
     *   <li>Valor cercano a 0: Rendimiento óptimo</li>
     * </ul>
     *
     * <p><b>Ejemplo:</b></p>
     * <pre>
     * dtEsperadoMs = 100ms
     * duracionMs = 103ms
     * → ultimoTickDesviacionMs = +3ms (lag leve)
     * </pre>
     *
     * <p>Marcado como volatile para visibilidad inmediata entre hilos.</p>
     *
     * @see #registrarTick(long)
     * @see #getUltimoTickDesviacionMs()
     */
    private volatile double ultimoTickDesviacionMs;

    /**
     * Promedio móvil de la duración de ticks en milisegundos.
     *
     * <p><b>Cálculo:</b></p>
     * <pre>
     * promedioTickDuracionMs = tiempoTotalEjecucionMs / ticksEjecutados
     * </pre>
     *
     * <p>Actualizado en cada tick mediante {@link #registrarTick(long)}.
     * Proporciona una métrica suavizada del rendimiento del motor a lo largo del tiempo.</p>
     *
     * <p><b>Uso:</b> Detectar degradación gradual del rendimiento,
     * validar optimizaciones, dimensionar capacidad del servidor.</p>
     *
     * <p>Marcado como volatile para visibilidad inmediata entre hilos.</p>
     *
     * @see #registrarTick(long)
     * @see #getPromedioTickDuracionMs()
     */
    private volatile double promedioTickDuracionMs;

    // ==================== CONFIGURACIÓN ====================

    /**
     * Delta time esperado en milisegundos para cada tick del motor.
     *
     * <p>Configurado en el constructor basándose en el dt fijo del {@link MotorCocodrilos}.
     * Utilizado para calcular {@link #ultimoTickDesviacionMs} y detectar lag.</p>
     *
     * <p><b>Valores típicos:</b></p>
     * <ul>
     *   <li>100.0 ms → 10 TPS</li>
     *   <li>50.0 ms → 20 TPS</li>
     *   <li>200.0 ms → 5 TPS</li>
     * </ul>
     *
     * <p>Este valor es inmutable tras la construcción.</p>
     *
     * @see #TelemetriaCocodrilos(double)
     * @see #getDtEsperadoMs()
     */
    private final double dtEsperadoMs;

    // ==================== TIMESTAMP DE INICIO ====================

    /**
     * Timestamp de inicialización del sistema de telemetría, en milisegundos Unix.
     *
     * <p>Capturado mediante {@code System.currentTimeMillis()} en el constructor.
     * Utilizado para calcular {@link #getUptimeSegundos()}.</p>
     *
     * <p><b>Uso:</b></p>
     * <pre>
     * uptimeSegundos = (System.currentTimeMillis() - iniciadoEn) / 1000.0
     * </pre>
     *
     * @see #TelemetriaCocodrilos(double)
     * @see #getIniciadoEn()
     * @see #getUptimeSegundos()
     */
    private final long iniciadoEn;

    // ==================== CONSTRUCTOR ====================

    /**
     * Crea una nueva instancia del sistema de telemetría.
     *
     * <p>Inicializa todos los contadores a cero y configura el delta time esperado
     * para cálculo de desviaciones.</p>
     *
     * <p><b>Secuencia de inicialización:</b></p>
     * <ol>
     *   <li>Inicializar contadores atómicos a 0 (creados, eliminados, ticks)</li>
     *   <li>Inicializar acumuladores de tiempo a 0</li>
     *   <li>Configurar dtEsperadoMs basándose en el parámetro</li>
     *   <li>Capturar timestamp de inicio ({@code System.currentTimeMillis()})</li>
     * </ol>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * // Motor de 10 TPS → dt = 0.1s = 100ms
     * double dtFijo = 0.1;
     * double dtEsperadoMs = dtFijo * 1000; // 100.0 ms
     * TelemetriaCocodrilos telemetria = new TelemetriaCocodrilos(dtEsperadoMs);
     * }</pre>
     *
     * @param dtEsperadoMs Delta time esperado en milisegundos (para calcular desviación).
     *                     Ejemplo: 100.0 para un motor de 10 TPS
     * @see MotorCocodrilos#MotorCocodrilos(double)
     */
    public TelemetriaCocodrilos(double dtEsperadoMs) {
        // Inicializar contadores atómicos a cero
        this.cocodrilosCreados = new AtomicInteger(0);
        this.cocodrilosEliminados = new AtomicInteger(0);
        this.ticksEjecutados = new AtomicLong(0);

        // Inicializar acumuladores de tiempo
        this.tiempoTotalEjecucionMs = new AtomicLong(0);
        this.ultimoTickDuracionMs = new AtomicLong(0);

        // Inicializar métricas calculadas
        this.ultimoTickDesviacionMs = 0.0;
        this.promedioTickDuracionMs = 0.0;

        // Configurar delta time esperado
        this.dtEsperadoMs = dtEsperadoMs;

        // Capturar timestamp de inicio para uptime
        this.iniciadoEn = System.currentTimeMillis();
    }

    // ==================== MÉTODOS DE REGISTRO ====================

    /**
     * Registra la creación de un cocodrilo.
     *
     * <p>Incrementa el contador {@link #cocodrilosCreados} de forma atómica.
     * Llamado por {@link MotorCocodrilos#procesarColaComandos()} cuando
     * un comando de creación se ejecuta exitosamente.</p>
     *
     * <p><b>Thread-safety:</b> Puede ser llamado desde cualquier hilo
     * sin necesidad de sincronización externa.</p>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * // En MotorCocodrilos.crearCocodriloRojo
     * colaComandos.offer(() -> {
     *     CocodriloRojo croc = FactoryEntidad.crearCocodriloRojo(...);
     *     cocodrilos.put(id, croc);
     *     telemetria.registrarCreacion(); // <-- Aquí
     * });
     * }</pre>
     *
     * @see #getCocodrilosCreados()
     * @see MotorCocodrilos#crearCocodriloRojo
     * @see MotorCocodrilos#crearCocodriloAzul
     */
    public void registrarCreacion() {
        cocodrilosCreados.incrementAndGet();
    }

    /**
     * Registra la eliminación de un cocodrilo.
     *
     * <p>Incrementa el contador {@link #cocodrilosEliminados} de forma atómica.
     * Llamado por {@link MotorCocodrilos#limpiarCocodrilosEliminados()} cuando
     * un cocodrilo marcado como ELIMINADO es removido del sistema.</p>
     *
     * <p><b>Thread-safety:</b> Puede ser llamado desde cualquier hilo
     * sin necesidad de sincronización externa.</p>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * // En MotorCocodrilos.limpiarCocodrilosEliminados
     * cocodrilos.values().removeIf(c -> {
     *     if (c.isEliminado()) {
     *         telemetria.registrarEliminacion(); // <-- Aquí
     *         return true;
     *     }
     *     return false;
     * });
     * }</pre>
     *
     * @see #getCocodrilosEliminados()
     * @see MotorCocodrilos#limpiarCocodrilosEliminados()
     */
    public void registrarEliminacion() {
        cocodrilosEliminados.incrementAndGet();
    }

    /**
     * Registra la ejecución de un tick con su duración.
     *
     * <p>Actualiza todas las métricas relacionadas con el rendimiento del tick:</p>
     * <ul>
     *   <li>Incrementa {@link #ticksEjecutados}</li>
     *   <li>Actualiza {@link #ultimoTickDuracionMs}</li>
     *   <li>Acumula en {@link #tiempoTotalEjecucionMs}</li>
     *   <li>Calcula {@link #ultimoTickDesviacionMs} respecto a {@link #dtEsperadoMs}</li>
     *   <li>Actualiza {@link #promedioTickDuracionMs}</li>
     * </ul>
     *
     * <p><b>Secuencia de actualización:</b></p>
     * <ol>
     *   <li>Incrementar contador de ticks ejecutados</li>
     *   <li>Registrar duración del último tick</li>
     *   <li>Acumular tiempo total de ejecución</li>
     *   <li>Calcular desviación: {@code duracionMs - dtEsperadoMs}</li>
     *   <li>Recalcular promedio: {@code tiempoTotal / ticks}</li>
     * </ol>
     *
     * <p><b>Thread-safety:</b> Todas las operaciones utilizan atomics,
     * garantizando consistencia incluso si múltiples hilos llaman este método
     * (aunque en la práctica solo lo llama el hilo del motor).</p>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * // En MotorCocodrilos.ejecutarTick
     * long inicio = System.currentTimeMillis();
     * try {
     *     // ... ejecutar lógica del tick ...
     * } finally {
     *     long duracion = System.currentTimeMillis() - inicio;
     *     telemetria.registrarTick(duracion); // <-- Aquí
     * }
     * }</pre>
     *
     * @param duracionMs Duración del tick en milisegundos
     * @see #getUltimoTickDuracionMs()
     * @see #getUltimoTickDesviacionMs()
     * @see #getPromedioTickDuracionMs()
     * @see MotorCocodrilos#ejecutarTick()
     */
    public void registrarTick(long duracionMs) {
        // Incrementar contador de ticks
        ticksEjecutados.incrementAndGet();

        // Actualizar duración del último tick
        ultimoTickDuracionMs.set(duracionMs);

        // Acumular tiempo total de ejecución
        tiempoTotalEjecucionMs.addAndGet(duracionMs);

        // Calcular desviación respecto al dt esperado
        this.ultimoTickDesviacionMs = duracionMs - dtEsperadoMs;

        // Actualizar promedio de duración de tick
        long ticks = ticksEjecutados.get();
        if (ticks > 0) {
            this.promedioTickDuracionMs = (double) tiempoTotalEjecucionMs.get() / ticks;
        }
    }

    /**
     * Reinicia todos los contadores y métricas a sus valores iniciales.
     *
     * <p>Resetea el sistema de telemetría como si acabara de ser creado,
     * preservando solo {@link #dtEsperadoMs} e {@link #iniciadoEn}.</p>
     *
     * <p><b>Valores después del reinicio:</b></p>
     * <pre>
     * cocodrilosCreados = 0
     * cocodrilosEliminados = 0
     * ticksEjecutados = 0
     * tiempoTotalEjecucionMs = 0
     * ultimoTickDuracionMs = 0
     * ultimoTickDesviacionMs = 0.0
     * promedioTickDuracionMs = 0.0
     * </pre>
     *
     * <p><b>Uso típico:</b> Testing o reseteo manual del sistema durante
     * desarrollo. Rara vez usado en producción.</p>
     *
     * <p><b>Ejemplo de uso:</b></p>
     * <pre>{@code
     * TelemetriaCocodrilos tel = motor.getTelemetria();
     * tel.reiniciar(); // Resetear todas las métricas
     * }</pre>
     */
    public void reiniciar() {
        cocodrilosCreados.set(0);
        cocodrilosEliminados.set(0);
        ticksEjecutados.set(0);
        tiempoTotalEjecucionMs.set(0);
        ultimoTickDuracionMs.set(0);
        ultimoTickDesviacionMs = 0.0;
        promedioTickDuracionMs = 0.0;
    }

    // ==================== GETTERS ====================

    /**
     * Obtiene el número total de cocodrilos creados desde el inicio del motor.
     *
     * @return Número de cocodrilos creados (siempre {@code >= 0})
     * @see #registrarCreacion()
     */
    public int getCocodrilosCreados() {
        return cocodrilosCreados.get();
    }

    /**
     * Obtiene el número total de cocodrilos eliminados desde el inicio del motor.
     *
     * @return Número de cocodrilos eliminados (siempre {@code >= 0})
     * @see #registrarEliminacion()
     */
    public int getCocodrilosEliminados() {
        return cocodrilosEliminados.get();
    }

    /**
     * Obtiene el número total de ticks ejecutados desde el inicio del motor.
     *
     * @return Número de ticks ejecutados (siempre {@code >= 0})
     * @see #registrarTick(long)
     */
    public long getTicksEjecutados() {
        return ticksEjecutados.get();
    }

    /**
     * Obtiene el tiempo total acumulado de ejecución de todos los ticks.
     *
     * <p>Suma de la duración de cada tick registrado. Útil para calcular
     * métricas derivadas como el promedio.</p>
     *
     * @return Tiempo total en milisegundos (siempre {@code >= 0})
     * @see #getPromedioTickDuracionMs()
     */
    public long getTiempoTotalEjecucionMs() {
        return tiempoTotalEjecucionMs.get();
    }

    /**
     * Obtiene la duración del último tick ejecutado.
     *
     * <p><b>Interpretación:</b></p>
     * <ul>
     *   <li>0 ms: Ningún tick ejecutado aún</li>
     *   <li>1-5 ms: Rendimiento óptimo para ~100 cocodrilos</li>
     *   <li>10+ ms: Posible sobrecarga, verificar cantidad de entidades</li>
     * </ul>
     *
     * @return Duración en milisegundos del último tick (siempre {@code >= 0})
     * @see #registrarTick(long)
     */
    public long getUltimoTickDuracionMs() {
        return ultimoTickDuracionMs.get();
    }

    /**
     * Obtiene la desviación del último tick respecto al delta time esperado.
     *
     * <p><b>Interpretación:</b></p>
     * <ul>
     *   <li>Valor positivo: Lag (tick tardó más de lo esperado)</li>
     *   <li>Valor negativo: Tick fue más rápido (normal)</li>
     *   <li>Cercano a 0: Rendimiento óptimo</li>
     * </ul>
     *
     * <p><b>Ejemplo:</b></p>
     * <pre>
     * dtEsperado = 100ms, duración = 105ms → desviación = +5ms
     * dtEsperado = 100ms, duración = 98ms  → desviación = -2ms
     * </pre>
     *
     * @return Desviación en milisegundos (puede ser positiva o negativa)
     * @see #registrarTick(long)
     */
    public double getUltimoTickDesviacionMs() {
        return ultimoTickDesviacionMs;
    }

    /**
     * Obtiene el promedio móvil de duración de ticks.
     *
     * <p><b>Cálculo:</b></p>
     * <pre>
     * promedioTickDuracionMs = tiempoTotalEjecucionMs / ticksEjecutados
     * </pre>
     *
     * <p>Proporciona una métrica suavizada del rendimiento a lo largo del tiempo,
     * útil para detectar degradación gradual.</p>
     *
     * @return Promedio de duración en milisegundos (0.0 si no hay ticks registrados)
     * @see #registrarTick(long)
     */
    public double getPromedioTickDuracionMs() {
        return promedioTickDuracionMs;
    }

    /**
     * Obtiene el delta time esperado configurado para este sistema.
     *
     * @return Delta time esperado en milisegundos
     * @see #TelemetriaCocodrilos(double)
     */
    public double getDtEsperadoMs() {
        return dtEsperadoMs;
    }

    /**
     * Obtiene el timestamp de inicialización del sistema de telemetría.
     *
     * <p>Valor en milisegundos Unix ({@code System.currentTimeMillis()})
     * capturado en el constructor.</p>
     *
     * @return Timestamp de inicio en milisegundos Unix
     * @see #getUptimeSegundos()
     */
    public long getIniciadoEn() {
        return iniciadoEn;
    }

    // ==================== MÉTRICAS CALCULADAS ====================

    /**
     * Calcula el TPS (ticks por segundo) promedio del motor.
     *
     * <p><b>Fórmula:</b></p>
     * <pre>
     * TPS = (ticksEjecutados * 1000.0) / tiempoTotalEjecucionMs
     * </pre>
     *
     * <p><b>Interpretación:</b></p>
     * <ul>
     *   <li>TPS cercano al esperado (ej: 10.0): Rendimiento óptimo</li>
     *   <li>TPS menor al esperado (ej: 8.5): Lag sostenido, motor sobrecargado</li>
     *   <li>TPS mayor al esperado: Imposible con scheduleAtFixedRate</li>
     * </ul>
     *
     * <p><b>Ejemplo:</b></p>
     * <pre>
     * ticksEjecutados = 1000
     * tiempoTotalEjecucionMs = 100000 (100 segundos)
     * TPS = (1000 * 1000) / 100000 = 10.0
     * </pre>
     *
     * @return TPS promedio, o 0.0 si {@code tiempoTotalEjecucionMs == 0}
     */
    public double getTicksPorSegundoPromedio() {
        long ticks = ticksEjecutados.get();
        long tiempoTotal = tiempoTotalEjecucionMs.get();

        // Evitar división por cero
        if (tiempoTotal == 0) return 0.0;

        // Calcular TPS: (ticks * 1000ms/s) / tiempoTotal_ms
        return (ticks * 1000.0) / tiempoTotal;
    }

    /**
     * Calcula el tiempo de uptime del motor en segundos.
     *
     * <p><b>Fórmula:</b></p>
     * <pre>
     * uptime = (System.currentTimeMillis() - iniciadoEn) / 1000.0
     * </pre>
     *
     * <p>Representa el tiempo transcurrido desde la creación del sistema
     * de telemetría (típicamente coincide con el inicio del {@link MotorCocodrilos}).</p>
     *
     * <p><b>Ejemplo:</b></p>
     * <pre>
     * iniciadoEn = 1705345678000
     * currentTimeMillis = 1705345798500
     * uptime = (1705345798500 - 1705345678000) / 1000.0 = 120.5 segundos
     * </pre>
     *
     * @return Uptime en segundos (siempre {@code >= 0.0})
     */
    public double getUptimeSegundos() {
        return (System.currentTimeMillis() - iniciadoEn) / 1000.0;
    }

    /**
     * Genera un reporte completo de telemetría en formato legible.
     *
     * <p>Incluye todas las métricas principales en un formato estructurado
     * para logs, consola de administración, o debugging.</p>
     *
     * <p><b>Formato del reporte:</b></p>
     * <pre>
     * === TELEMETRÍA MOTOR COCODRILOS ===
     * Uptime: 120.50 segundos
     * Cocodrilos creados: 45
     * Cocodrilos eliminados: 12
     * Cocodrilos activos: 33
     * Ticks ejecutados: 1205
     * TPS promedio: 10.00
     * Duración promedio tick: 2.30 ms
     * Último tick: 3 ms (desviación: +1.00 ms)
     * dt esperado: 100.00 ms
     * </pre>
     *
     * <p><b>Uso típico:</b></p>
     * <pre>{@code
     * TelemetriaCocodrilos tel = motor.getTelemetria();
     * System.out.println(tel.generarReporte());
     * // O en AdminGUI:
     * adminGUI.agregarLog(tel.generarReporte());
     * }</pre>
     *
     * @return String con el reporte completo de telemetría
     */
    public String generarReporte() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TELEMETRÍA MOTOR COCODRILOS ===\n");
        sb.append(String.format("Uptime: %.2f segundos\n", getUptimeSegundos()));
        sb.append(String.format("Cocodrilos creados: %d\n", getCocodrilosCreados()));
        sb.append(String.format("Cocodrilos eliminados: %d\n", getCocodrilosEliminados()));
        sb.append(String.format("Cocodrilos activos: %d\n", getCocodrilosCreados() - getCocodrilosEliminados()));
        sb.append(String.format("Ticks ejecutados: %d\n", getTicksEjecutados()));
        sb.append(String.format("TPS promedio: %.2f\n", getTicksPorSegundoPromedio()));
        sb.append(String.format("Duración promedio tick: %.2f ms\n", getPromedioTickDuracionMs()));
        sb.append(String.format("Último tick: %d ms (desviación: %+.2f ms)\n",
                getUltimoTickDuracionMs(), getUltimoTickDesviacionMs()));
        sb.append(String.format("dt esperado: %.2f ms\n", getDtEsperadoMs()));
        return sb.toString();
    }

    /**
     * Genera una representación compacta del estado de la telemetría.
     *
     * <p><b>Formato:</b></p>
     * <pre>
     * Telemetria[ticks=1205, creados=45, eliminados=12, TPS=10.0, avg=2.3ms]
     * </pre>
     *
     * <p>Útil para logging conciso o debugging rápido.</p>
     *
     * @return String compacto con métricas clave
     */
    @Override
    public String toString() {
        return String.format("Telemetria[ticks=%d, creados=%d, eliminados=%d, TPS=%.1f, avg=%.1fms]",
                getTicksEjecutados(), getCocodrilosCreados(), getCocodrilosEliminados(),
                getTicksPorSegundoPromedio(), getPromedioTickDuracionMs());
    }
}
