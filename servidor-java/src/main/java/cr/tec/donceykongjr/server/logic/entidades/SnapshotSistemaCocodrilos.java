package cr.tec.donceykongjr.server.logic.entidades;

import java.util.Collections;
import java.util.List;

/**
 * Vista inmutable del estado completo del sistema de cocodrilos.
 * Incluye todos los cocodrilos activos y métricas del sistema.
 * Thread-safe para lectura concurrente.
 */
public final class SnapshotSistemaCocodrilos {
    private final List<SnapshotCocodrilo> cocodrilos;
    private final Double factorDificultad;
    private final Long tickActual;
    private final Long timestamp;
    private final Integer totalCocodrilosActivos;
    private final Integer totalCocodrilosEliminados;

    /**
     * Crea un snapshot inmutable del sistema completo.
     *
     * @param cocodrilos Lista inmutable de snapshots de cocodrilos
     * @param factorDificultad Factor de dificultad actual
     * @param tickActual Número de tick actual
     * @param totalCocodrilosActivos Total de cocodrilos activos
     * @param totalCocodrilosEliminados Total de cocodrilos eliminados (histórico)
     */
    public SnapshotSistemaCocodrilos(List<SnapshotCocodrilo> cocodrilos,
                                     Double factorDificultad,
                                     Long tickActual,
                                     Integer totalCocodrilosActivos,
                                     Integer totalCocodrilosEliminados) {
        // Crear copia inmutable defensiva
        this.cocodrilos = Collections.unmodifiableList(cocodrilos);
        this.factorDificultad = factorDificultad;
        this.tickActual = tickActual;
        this.totalCocodrilosActivos = totalCocodrilosActivos;
        this.totalCocodrilosEliminados = totalCocodrilosEliminados;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters

    /**
     * Retorna una lista inmutable de snapshots de cocodrilos.
     */
    public List<SnapshotCocodrilo> getCocodrilos() {
        return cocodrilos;
    }

    public Double getFactorDificultad() {
        return factorDificultad;
    }

    public Long getTickActual() {
        return tickActual;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Integer getTotalCocodrilosActivos() {
        return totalCocodrilosActivos;
    }

    public Integer getTotalCocodrilosEliminados() {
        return totalCocodrilosEliminados;
    }

    public Integer getTotalCocodrilos() {
        return cocodrilos.size();
    }

    @Override
    public String toString() {
        return String.format("SnapshotSistema[tick=%d, cocodrilos=%d, activos=%d, eliminados=%d, dificultad=%.2f]",
                tickActual, getTotalCocodrilos(), totalCocodrilosActivos, totalCocodrilosEliminados, factorDificultad);
    }
}
