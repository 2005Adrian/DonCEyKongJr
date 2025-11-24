package cr.tec.donceykongjr.server.logic;

import cr.tec.donceykongjr.server.logic.entidades.*;
import cr.tec.donceykongjr.server.util.LoggerUtil;

/**
 * Ejemplo de uso del subsistema de cocodrilos.
 * Demuestra cómo configurar, iniciar y usar el motor de cocodrilos de forma independiente.
 */
public class EjemploMotorCocodrilos {

    public static void main(String[] args) throws InterruptedException {
        LoggerUtil.info("=== INICIO EJEMPLO MOTOR COCODRILOS ===");

        // 1. Crear el motor con fixed timestep de 0.1 segundos (10 TPS)
        MotorCocodrilos motor = new MotorCocodrilos(0.1);

        // 2. Registrar lianas en el sistema
        registrarLianas(motor);

        // 3. Crear cocodrilos iniciales
        crearCocodrilosIniciales(motor);

        // 4. Iniciar el motor (comienza el loop de actualización)
        motor.start();

        // 5. Dejar correr el sistema durante 5 segundos
        LoggerUtil.info("Motor ejecutando...");
        Thread.sleep(5000);

        // 6. Obtener y mostrar snapshot del estado
        mostrarSnapshot(motor);

        // 7. Incrementar dificultad
        LoggerUtil.info("\n--- INCREMENTANDO DIFICULTAD ---");
        motor.incrementarDificultad(1.5); // Aumentar velocidad 50%

        // 8. Continuar ejecutando
        Thread.sleep(3000);

        // 9. Crear más cocodrilos dinámicamente
        LoggerUtil.info("\n--- CREANDO MÁS COCODRILOS ---");
        motor.crearCocodriloRojo(2, 150.0, 30.0, -1);
        motor.crearCocodriloAzul(3, 100.0, 25.0);

        Thread.sleep(3000);

        // 10. Mostrar telemetría final
        mostrarTelemetria(motor);

        // 11. Mostrar snapshot final
        mostrarSnapshot(motor);

        // 12. Detener el motor
        motor.stop();

        LoggerUtil.info("\n=== FIN EJEMPLO ===");
    }

    /**
     * Registra las lianas del juego en el motor.
     */
    private static void registrarLianas(MotorCocodrilos motor) {
        LoggerUtil.info("\n--- REGISTRANDO LIANAS ---");

        // Crear 5 lianas verticales con diferentes alturas
        for (int i = 0; i < 5; i++) {
            double alturaMin = 50.0;
            double alturaMax = 450.0;

            Liana liana = new Liana(
                    "LIANA_" + i,
                    i * 100.0, // X separadas
                    alturaMin,
                    i,
                    alturaMin,
                    alturaMax
            );

            motor.registrarLiana(liana);
            LoggerUtil.info(String.format("Liana %d: [%.0f, %.0f]", i, alturaMin, alturaMax));
        }
    }

    /**
     * Crea cocodrilos iniciales en diferentes lianas.
     */
    private static void crearCocodrilosIniciales(MotorCocodrilos motor) {
        LoggerUtil.info("\n--- CREANDO COCODRILOS INICIALES ---");

        // Crear 3 cocodrilos rojos
        for (int i = 0; i < 3; i++) {
            int liana = i;
            double yInicial = 150.0 + (i * 50.0);
            double velocidad = 20.0 + (i * 5.0);
            int direccion = (i % 2 == 0) ? -1 : 1; // Alternar dirección

            String id = motor.crearCocodriloRojo(liana, yInicial, velocidad, direccion);
            LoggerUtil.info(String.format("Cocodrilo rojo creado: %s (liana=%d, y=%.0f, vel=%.0f, dir=%+d)",
                    id, liana, yInicial, velocidad, direccion));
        }

        // Crear 2 cocodrilos azules
        for (int i = 0; i < 2; i++) {
            int liana = i + 3;
            double yInicial = 100.0;
            double velocidad = 15.0;

            String id = motor.crearCocodriloAzul(liana, yInicial, velocidad);
            LoggerUtil.info(String.format("Cocodrilo azul creado: %s (liana=%d, y=%.0f, vel=%.0f)",
                    id, liana, yInicial, velocidad));
        }
    }

    /**
     * Muestra un snapshot del estado actual del sistema.
     */
    private static void mostrarSnapshot(MotorCocodrilos motor) {
        LoggerUtil.info("\n--- SNAPSHOT DEL SISTEMA ---");

        SnapshotSistemaCocodrilos snapshot = motor.getSnapshot();

        LoggerUtil.info(String.format("Tick actual: %d", snapshot.getTickActual()));
        LoggerUtil.info(String.format("Factor dificultad: %.2f", snapshot.getFactorDificultad()));
        LoggerUtil.info(String.format("Cocodrilos activos: %d", snapshot.getTotalCocodrilosActivos()));
        LoggerUtil.info(String.format("Cocodrilos eliminados: %d", snapshot.getTotalCocodrilosEliminados()));

        LoggerUtil.info("\nCocodrilos:");
        for (SnapshotCocodrilo croc : snapshot.getCocodrilos()) {
            if (croc.isActivo()) {
                LoggerUtil.info(String.format("  %s - tipo=%s, liana=%d, y=%.2f, vel=%.1f, dir=%+d",
                        croc.getId(), croc.getTipo(), croc.getLianaId(), croc.getY(),
                        croc.getVelocidadBase(), croc.getDireccion()));
            }
        }
    }

    /**
     * Muestra la telemetría del motor.
     */
    private static void mostrarTelemetria(MotorCocodrilos motor) {
        LoggerUtil.info("\n" + motor.getTelemetria().generarReporte());
    }

    /**
     * Ejemplo de integración con el GameManager existente.
     * Este método muestra cómo usar el motor en conjunto con el juego principal.
     */
    public static MotorCocodrilos integrarConGameManager(GameManager gameManager) {
        // 1. Crear motor con el mismo dt que el juego (o independiente)
        MotorCocodrilos motor = new MotorCocodrilos(0.1);

        // 2. Registrar lianas del GameManager en el motor
        // Nota: Necesitarías un getter en GameManager para acceder a las lianas
        // for (Liana liana : gameManager.getLianas()) {
        //     motor.registrarLiana(liana);
        // }

        // 3. Crear cocodrilos iniciales basados en configuración del juego
        // motor.crearCocodriloRojo(0, 150.0, 20.0, -1);
        // motor.crearCocodriloAzul(1, 100.0, 15.0);

        // 4. Iniciar el motor
        motor.start();

        // 5. En el GameManager, puedes obtener snapshots para sincronizar
        // SnapshotSistemaCocodrilos snapshot = motor.getSnapshot();
        // Actualizar la lista de cocodrilos del GameManager basándose en el snapshot

        // 6. Cuando el nivel sube, incrementar dificultad
        // motor.incrementarDificultad(1.2);

        return motor;
    }

    /**
     * Ejemplo de test determinista usando tickForzado.
     */
    public static void ejemploTestDeterminista() {
        LoggerUtil.info("\n=== EJEMPLO TEST DETERMINISTA ===");

        // Crear motor pero NO iniciarlo
        MotorCocodrilos motor = new MotorCocodrilos(0.1);

        // Registrar liana
        Liana liana = new Liana("TEST_LIANA", 0, 50.0, 0, 50.0, 450.0);
        motor.registrarLiana(liana);

        // Crear cocodrilo rojo
        String id = motor.crearCocodriloRojo(0, 250.0, 50.0, -1);

        // Ejecutar ticks manuales para testing determinista
        for (int i = 0; i < 10; i++) {
            motor.tickForzado(0.1);

            SnapshotSistemaCocodrilos snapshot = motor.getSnapshot();
            SnapshotCocodrilo croc = snapshot.getCocodrilos().get(0);

            LoggerUtil.info(String.format("Tick %d: y=%.2f, dir=%+d",
                    i, croc.getY(), croc.getDireccion()));
        }

        LoggerUtil.info("Test determinista completado");
    }
}
