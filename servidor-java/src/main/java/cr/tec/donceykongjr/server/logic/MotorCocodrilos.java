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
 * Motor independiente para el subsistema de cocodrilos.
 * Orquesta el ciclo de actualización con fixed timestep, creación y eliminación thread-safe.
 *
 * Características:
 * - Fixed timestep determinista con hilo dedicado
 * - Thread-safe mediante sincronización y colas de comandos
 * - Factor de dificultad escalable
 * - Telemetría integrada
 * - Snapshots inmutables del estado
 * - Orden de actualización determinista
 */
public class MotorCocodrilos {
    // Configuración
    private final double dtFijo; // Delta time fijo en segundos
    private final long dtFijoMs; // Delta time fijo en milisegundos
    private volatile double factorDificultad; // Factor de dificultad (≥ 1.0)

    // Estado del motor
    private final AtomicBoolean ejecutando;
    private final AtomicLong tickActual;
    private ScheduledExecutorService ejecutor;

    // Colecciones thread-safe
    private final Map<String, Cocodrilo> cocodrilos; // ID -> Cocodrilo
    private final Map<Integer, Liana> lianas; // lianaId -> Liana
    private final Queue<Runnable> colaComandos; // Cola de comandos pendientes

    // Generación de IDs
    private final AtomicInteger contadorIds;

    // Telemetría
    private final TelemetriaCocodrilos telemetria;

    // Lock para sincronización
    private final Object lock = new Object();

    /**
     * Crea un motor de cocodrilos con configuración específica.
     *
     * @param dtFijo Delta time fijo en segundos (ej: 0.1 = 10 ticks/segundo)
     */
    public MotorCocodrilos(double dtFijo) {
        if (dtFijo <= 0) {
            throw new IllegalArgumentException("dtFijo debe ser mayor a 0");
        }

        this.dtFijo = dtFijo;
        this.dtFijoMs = (long) (dtFijo * 1000);
        this.factorDificultad = 1.0;
        this.ejecutando = new AtomicBoolean(false);
        this.tickActual = new AtomicLong(0);
        this.cocodrilos = new ConcurrentHashMap<>();
        this.lianas = new ConcurrentHashMap<>();
        this.colaComandos = new ConcurrentLinkedQueue<>();
        this.contadorIds = new AtomicInteger(0);
        this.telemetria = new TelemetriaCocodrilos((double) dtFijoMs);

        LoggerUtil.info("MotorCocodrilos inicializado con dt=" + dtFijo + "s");
    }

    /**
     * Inicia el motor en su propio hilo con fixed timestep.
     */
    public void start() {
        if (ejecutando.get()) {
            LoggerUtil.warning("El motor ya está ejecutando");
            return;
        }

        ejecutando.set(true);
        ejecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MotorCocodrilos");
            t.setDaemon(true);
            return t;
        });

        // Programar ejecución a tasa fija (fixed timestep)
        ejecutor.scheduleAtFixedRate(
                this::ejecutarTick,
                0,
                dtFijoMs,
                TimeUnit.MILLISECONDS
        );

        LoggerUtil.info("MotorCocodrilos iniciado (TPS=" + (1.0 / dtFijo) + ")");
    }

    /**
     * Detiene el motor de forma segura.
     */
    public void stop() {
        if (!ejecutando.get()) {
            LoggerUtil.warning("El motor no está ejecutando");
            return;
        }

        ejecutando.set(false);

        if (ejecutor != null) {
            ejecutor.shutdown();
            try {
                if (!ejecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    ejecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                ejecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        LoggerUtil.info("MotorCocodrilos detenido");
    }

    /**
     * Ejecuta un tick manual con dt específico (útil para testing).
     *
     * @param dt Delta time en segundos
     */
    public void tickForzado(double dt) {
        synchronized (lock) {
            procesarColaComandos();
            actualizarCocodrilos(dt);
            limpiarCocodrilosEliminados();
        }
    }

    /**
     * Crea un cocodrilo rojo y lo agrega al sistema.
     *
     * @param lianaId ID de la liana
     * @param yInicial Posición Y inicial
     * @param velocidadBase Velocidad base
     * @param direccion Dirección inicial (+1 o -1)
     * @return ID del cocodrilo creado, o null si hubo error
     */
    public String crearCocodriloRojo(int lianaId, double yInicial,
                                     double velocidadBase, int direccion) {
        // Validar liana
        Liana liana = lianas.get(lianaId);
        if (liana == null) {
            LoggerUtil.warning("No existe liana con ID " + lianaId);
            return null;
        }

        // Validar posición inicial
        if (!liana.estaEnRango(yInicial)) {
            LoggerUtil.warning("yInicial fuera de rango de la liana");
            return null;
        }

        String id = generarId("ROJO");

        // Encolar comando de creación (thread-safe)
        colaComandos.offer(() -> {
            try {
                // Usar Factory para crear cocodrilos (patrón Factory Method)
                CocodriloRojo cocodrilo = FactoryEntidad.crearCocodriloRojo(
                        id, lianaId, yInicial, velocidadBase, direccion,
                        liana.getAlturaMin(), liana.getAlturaMax()
                );
                cocodrilos.put(id, cocodrilo);
                telemetria.registrarCreacion();
                LoggerUtil.debug("Cocodrilo rojo creado: " + id);
            } catch (Exception e) {
                LoggerUtil.warning("Error creando cocodrilo rojo: " + e.getMessage());
            }
        });

        return id;
    }

    /**
     * Crea un cocodrilo azul y lo agrega al sistema.
     *
     * @param lianaId ID de la liana
     * @param yInicial Posición Y inicial
     * @param velocidadBase Velocidad base
     * @return ID del cocodrilo creado, o null si hubo error
     */
    public String crearCocodriloAzul(int lianaId, double yInicial,
                                     double velocidadBase) {
        // Validar liana
        Liana liana = lianas.get(lianaId);
        if (liana == null) {
            LoggerUtil.warning("No existe liana con ID " + lianaId);
            return null;
        }

        String id = generarId("AZUL");

        // Encolar comando de creación (thread-safe)
        colaComandos.offer(() -> {
            try {
                // Usar Factory para crear cocodrilos (patrón Factory Method)
                CocodriloAzul cocodrilo = FactoryEntidad.crearCocodriloAzul(
                        id, lianaId, yInicial, velocidadBase,
                        liana.getAlturaMax() // Límite inferior = alturaMax de liana
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
     * Elimina un cocodrilo del sistema por ID.
     *
     * @param id ID del cocodrilo a eliminar
     */
    public void eliminarCocodrilo(String id) {
        colaComandos.offer(() -> {
            Cocodrilo cocodrilo = cocodrilos.get(id);
            if (cocodrilo != null) {
                cocodrilo.eliminar();
                LoggerUtil.debug("Cocodrilo eliminado: " + id);
            }
        });
    }

    /**
     * Incrementa el factor de dificultad, aumentando la velocidad efectiva.
     *
     * @param multiplicador Factor multiplicador (debe ser > 1.0)
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

    /**
     * Registra una liana en el sistema.
     *
     * @param liana Liana a registrar
     */
    public void registrarLiana(Liana liana) {
        lianas.put(liana.getLiana(), liana);
        LoggerUtil.debug("Liana registrada: " + liana.getId());
    }

    /**
     * Obtiene un snapshot inmutable del estado actual del sistema.
     *
     * @return Snapshot thread-safe del sistema
     */
    public SnapshotSistemaCocodrilos getSnapshot() {
        synchronized (lock) {
            List<SnapshotCocodrilo> snapshots = cocodrilos.values().stream()
                    .map(SnapshotCocodrilo::new)
                    .collect(Collectors.toList());

            int activos = (int) cocodrilos.values().stream()
                    .filter(Cocodrilo::isActivo)
                    .count();

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
     * Obtiene la telemetría actual del motor.
     */
    public TelemetriaCocodrilos getTelemetria() {
        return telemetria;
    }

    // Métodos privados del ciclo de actualización

    /**
     * Ejecuta un tick del motor (llamado por el executor).
     */
    private void ejecutarTick() {
        if (!ejecutando.get()) return;

        long inicio = System.currentTimeMillis();

        try {
            synchronized (lock) {
                procesarColaComandos();
                actualizarCocodrilos(dtFijo * factorDificultad);
                limpiarCocodrilosEliminados();
                tickActual.incrementAndGet();
            }
        } catch (Exception e) {
            LoggerUtil.warning("Error en tick del motor: " + e.getMessage());
            e.printStackTrace();
        }

        long duracion = System.currentTimeMillis() - inicio;
        telemetria.registrarTick(duracion);

        // Advertir si el tick tomó más tiempo del esperado
        if (duracion > dtFijoMs) {
            LoggerUtil.warning(String.format("Tick lag: %dms (esperado: %dms)",
                    duracion, dtFijoMs));
        }
    }

    /**
     * Procesa todos los comandos pendientes en la cola (crear/eliminar).
     */
    private void procesarColaComandos() {
        Runnable comando;
        while ((comando = colaComandos.poll()) != null) {
            try {
                comando.run();
            } catch (Exception e) {
                LoggerUtil.warning("Error ejecutando comando: " + e.getMessage());
            }
        }
    }

    /**
     * Actualiza todos los cocodrilos activos en orden determinista.
     */
    private void actualizarCocodrilos(double dt) {
        // Ordenar por ID para determinismo
        List<Cocodrilo> lista = new ArrayList<>(cocodrilos.values());
        lista.sort(Comparator.comparing(Cocodrilo::getId));

        for (Cocodrilo cocodrilo : lista) {
            if (cocodrilo.isActivo()) {
                cocodrilo.mover(dt);
            }
        }
    }

    /**
     * Limpia cocodrilos marcados como ELIMINADO.
     */
    private void limpiarCocodrilosEliminados() {
        cocodrilos.values().removeIf(c -> {
            if (c.isEliminado()) {
                telemetria.registrarEliminacion();
                return true;
            }
            return false;
        });
    }

    /**
     * Genera un ID único para un cocodrilo.
     */
    private String generarId(String tipo) {
        return String.format("CROC_%s_%d_%d",
                tipo, System.currentTimeMillis(), contadorIds.incrementAndGet());
    }

    // Getters

    public boolean isEjecutando() {
        return ejecutando.get();
    }

    public double getDtFijo() {
        return dtFijo;
    }

    public double getFactorDificultad() {
        return factorDificultad;
    }

    public long getTickActual() {
        return tickActual.get();
    }

    public int getCantidadCocodrilos() {
        return cocodrilos.size();
    }

    public int getCantidadLianas() {
        return lianas.size();
    }
}
