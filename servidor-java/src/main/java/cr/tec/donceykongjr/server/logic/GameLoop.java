package cr.tec.donceykongjr.server.logic;

import cr.tec.donceykongjr.server.util.Config;
import cr.tec.donceykongjr.server.util.LoggerUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Loop principal del juego que actualiza el estado periódicamente.
 * Usa ScheduledExecutorService para mantener un tick constante.
 */
public class GameLoop {
    private GameManager gameManager;
    private ScheduledExecutorService scheduler;
    private boolean ejecutando;
    private double deltaTime;
    
    /**
     * Constructor del GameLoop.
     */
    public GameLoop(GameManager gameManager) {
        this.gameManager = gameManager;
        this.ejecutando = false;
        this.deltaTime = 1.0 / Config.TICKS_POR_SEGUNDO;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * Inicia el loop del juego.
     */
    public void iniciar() {
        if (ejecutando) {
            LoggerUtil.warning("el gameloop ya esta ejecutandose");
            return;
        }
        
        ejecutando = true;
        LoggerUtil.info("gameloop iniciado (" + Config.TICKS_POR_SEGUNDO + " ticks/segundo)");
        
        scheduler.scheduleAtFixedRate(
            this::tick,
            0,
            Config.INTERVALO_TICK_MS,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Ejecuta un tick del juego.
     */
    private void tick() {
        try {
            gameManager.actualizar(deltaTime);
        } catch (Exception e) {
            LoggerUtil.error("error en gameloop: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Detiene el loop del juego.
     */
    public void detener() {
        if (!ejecutando) {
            return;
        }
        
        ejecutando = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        LoggerUtil.info("gameloop detenido");
    }
    
    /**
     * Verifica si el loop está ejecutándose.
     */
    public boolean isEjecutando() {
        return ejecutando;
    }
}

