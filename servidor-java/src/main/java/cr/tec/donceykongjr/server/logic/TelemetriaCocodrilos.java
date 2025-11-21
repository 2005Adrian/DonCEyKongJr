package cr.tec.donceykongjr.server.logic;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sistema de telemetría thread-safe para el motor de cocodrilos.
 * Registra métricas de rendimiento y contadores de operaciones.
 *
 * Métricas incluidas:
 * - Contadores: cocodrilos creados, eliminados, ticks ejecutados
 * - Performance: tiempo por tick, desviación del dt esperado
 * - Estado: tiempo de ejecución total, ticks por segundo promedio
 */
public class TelemetriaCocodrilos {
    // Contadores atómicos (thread-safe)
    private final AtomicInteger cocodrilosCreados;
    private final AtomicInteger cocodrilosEliminados;
    private final AtomicLong ticksEjecutados;

    // Métricas de tiempo
    private final AtomicLong tiempoTotalEjecucionMs;
    private final AtomicLong ultimoTickDuracionMs;
    private volatile double ultimoTickDesviacionMs;
    private volatile double promedioTickDuracionMs;

    // Configuración
    private final double dtEsperadoMs;

    // Timestamp de inicio
    private final long iniciadoEn;

    /**
     * Crea una nueva instancia de telemetría.
     *
     * @param dtEsperadoMs Delta time esperado en milisegundos (para calcular desviación)
     */
    public TelemetriaCocodrilos(double dtEsperadoMs) {
        this.cocodrilosCreados = new AtomicInteger(0);
        this.cocodrilosEliminados = new AtomicInteger(0);
        this.ticksEjecutados = new AtomicLong(0);
        this.tiempoTotalEjecucionMs = new AtomicLong(0);
        this.ultimoTickDuracionMs = new AtomicLong(0);
        this.ultimoTickDesviacionMs = 0.0;
        this.promedioTickDuracionMs = 0.0;
        this.dtEsperadoMs = dtEsperadoMs;
        this.iniciadoEn = System.currentTimeMillis();
    }

    /**
     * Registra la creación de un cocodrilo.
     */
    public void registrarCreacion() {
        cocodrilosCreados.incrementAndGet();
    }

    /**
     * Registra la eliminación de un cocodrilo.
     */
    public void registrarEliminacion() {
        cocodrilosEliminados.incrementAndGet();
    }

    /**
     * Registra la ejecución de un tick con su duración.
     *
     * @param duracionMs Duración del tick en milisegundos
     */
    public void registrarTick(long duracionMs) {
        ticksEjecutados.incrementAndGet();
        ultimoTickDuracionMs.set(duracionMs);
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
     * Reinicia todos los contadores y métricas.
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

    // Getters

    public int getCocodrilosCreados() {
        return cocodrilosCreados.get();
    }

    public int getCocodrilosEliminados() {
        return cocodrilosEliminados.get();
    }

    public long getTicksEjecutados() {
        return ticksEjecutados.get();
    }

    public long getTiempoTotalEjecucionMs() {
        return tiempoTotalEjecucionMs.get();
    }

    public long getUltimoTickDuracionMs() {
        return ultimoTickDuracionMs.get();
    }

    public double getUltimoTickDesviacionMs() {
        return ultimoTickDesviacionMs;
    }

    public double getPromedioTickDuracionMs() {
        return promedioTickDuracionMs;
    }

    public double getDtEsperadoMs() {
        return dtEsperadoMs;
    }

    public long getIniciadoEn() {
        return iniciadoEn;
    }

    /**
     * Calcula el TPS (ticks por segundo) promedio.
     */
    public double getTicksPorSegundoPromedio() {
        long ticks = ticksEjecutados.get();
        long tiempoTotal = tiempoTotalEjecucionMs.get();
        if (tiempoTotal == 0) return 0.0;
        return (ticks * 1000.0) / tiempoTotal;
    }

    /**
     * Calcula el tiempo de uptime en segundos.
     */
    public double getUptimeSegundos() {
        return (System.currentTimeMillis() - iniciadoEn) / 1000.0;
    }

    /**
     * Genera un reporte de telemetría en formato legible.
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

    @Override
    public String toString() {
        return String.format("Telemetria[ticks=%d, creados=%d, eliminados=%d, TPS=%.1f, avg=%.1fms]",
                getTicksEjecutados(), getCocodrilosCreados(), getCocodrilosEliminados(),
                getTicksPorSegundoPromedio(), getPromedioTickDuracionMs());
    }
}
