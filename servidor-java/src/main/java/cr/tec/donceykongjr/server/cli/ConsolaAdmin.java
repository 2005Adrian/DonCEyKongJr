package cr.tec.donceykongjr.server.cli;

import cr.tec.donceykongjr.server.logic.GameManager;
import cr.tec.donceykongjr.server.util.LoggerUtil;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Consola de administración para el servidor.
 * Permite ejecutar comandos mientras el servidor está en ejecución.
 * Usa un hilo separado para leer stdin sin bloquear el loop del juego.
 */
public class ConsolaAdmin implements Runnable {
    private GameManager gameManager;
    private Scanner scanner;
    private AtomicBoolean ejecutando;
    
    /**
     * Constructor de la consola de administración.
     */
    public ConsolaAdmin(GameManager gameManager) {
        this.gameManager = gameManager;
        this.scanner = new Scanner(System.in);
        this.ejecutando = new AtomicBoolean(true);
    }
    
    @Override
    public void run() {
        // Verificar si hay consola disponible
        if (System.console() == null) {
            LoggerUtil.warning("consola de administracion no disponible (no hay consola interactiva).");
            LoggerUtil.info("el servidor seguira funcionando. puedes controlarlo desde clientes conectados.");
            LoggerUtil.info("para usar la consola, ejecuta: java -cp build/classes/java/main cr.tec.donceykongjr.server.Main");
            return;
        }
        
        mostrarAyuda();
        
        while (ejecutando.get()) {
            try {
                // Usar System.console() que es más confiable
                java.io.Console console = System.console();
                if (console == null) {
                    break; // Si la consola se perdió, salir
                }
                String linea = console.readLine("> ");
                
                if (linea == null || linea.trim().isEmpty()) {
                    continue;
                }
                
                String[] partes = linea.trim().split("\\s+");
                String comando = partes[0].toLowerCase();
                
                switch (comando) {
                    case "create":
                        manejarCreate(partes);
                        break;
                    case "fruit":
                        manejarFruit(partes);
                        break;
                    case "list":
                        manejarList(partes);
                        break;
                    case "pause":
                        gameManager.setPausado(true);
                        System.out.println("juego pausado");
                        break;
                    case "resume":
                        gameManager.setPausado(false);
                        System.out.println("juego reanudado");
                        break;
                    case "quit":
                    case "exit":
                        ejecutando.set(false);
                        System.out.println("cerrando consola de administracion...");
                        break;
                    case "help":
                        mostrarAyuda();
                        break;
                    default:
                        System.out.println("comando desconocido: " + comando);
                        System.out.println("escribe 'help' para ver los comandos disponibles");
                }
            } catch (Exception e) {
                LoggerUtil.error("error en consola: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        if (scanner != null) {
            scanner.close();
        }
    }
    
    /**
     * Muestra la ayuda de comandos.
     */
    private void mostrarAyuda() {
        System.out.println("\n=== consola de administracion ===");
        System.out.println("comandos disponibles:");
        System.out.println("  create red <liana> <y>     - crea un cocodrilo rojo");
        System.out.println("  create blue <liana> <y>    - crea un cocodrilo azul");
        System.out.println("  fruit add <liana> <y> <points> - agrega una fruta");
        System.out.println("  fruit del <liana> <y>      - elimina una fruta");
        System.out.println("  list entities              - lista todas las entidades");
        System.out.println("  pause                     - pausa el juego");
        System.out.println("  resume                    - reanuda el juego");
        System.out.println("  help                      - muestra esta ayuda");
        System.out.println("  quit                      - cierra la consola");
        System.out.println("==================================\n");
    }
    
    /**
     * Maneja comandos de creación de entidades.
     */
    private void manejarCreate(String[] partes) {
        if (partes.length < 4) {
            System.out.println("uso: create <red|blue> <liana> <y>");
            return;
        }
        
        String tipo = partes[1].toLowerCase();
        try {
            int liana = Integer.parseInt(partes[2]);
            double y = Double.parseDouble(partes[3]);

            if (tipo.equals("red")) {
                String error = gameManager.agregarCocodriloRojo(liana, y);
                if (error != null) {
                    System.out.println("[error] " + error);
                } else {
                    System.out.println("[ok] cocodrilo rojo creado en liana " + liana + ", y=" + y);
                }
            } else if (tipo.equals("blue")) {
                String error = gameManager.agregarCocodriloAzul(liana, y);
                if (error != null) {
                    System.out.println("[error] " + error);
                } else {
                    System.out.println("[ok] cocodrilo azul creado en liana " + liana + ", y=" + y);
                }
            } else {
                System.out.println("[error] tipo de cocodrilo invalido. use 'red' o 'blue'");
            }
        } catch (NumberFormatException e) {
            System.out.println("[error] liana y y deben ser numeros");
        }
    }
    
    /**
     * Maneja comandos de frutas.
     */
    private void manejarFruit(String[] partes) {
        if (partes.length < 2) {
            System.out.println("uso: fruit <add|del> <liana> <y> [points]");
            return;
        }
        
        String accion = partes[1].toLowerCase();
        
        if (accion.equals("add")) {
            if (partes.length < 5) {
                System.out.println("uso: fruit add <liana> <y> <points>");
                return;
            }
            try {
                int liana = Integer.parseInt(partes[2]);
                double y = Double.parseDouble(partes[3]);
                int puntos = Integer.parseInt(partes[4]);

                String error = gameManager.agregarFruta(liana, y, puntos);
                if (error != null) {
                    System.out.println("[error] " + error);
                } else {
                    System.out.println("[ok] fruta agregada en liana " + liana + ", y=" + y + ", puntos=" + puntos);
                }
            } catch (NumberFormatException e) {
                System.out.println("[error] liana, y y points deben ser numeros");
            }
        } else if (accion.equals("del")) {
            if (partes.length < 4) {
                System.out.println("uso: fruit del <liana> <y>");
                return;
            }
            try {
                int liana = Integer.parseInt(partes[2]);
                double y = Double.parseDouble(partes[3]);
                
                if (gameManager.eliminarFruta(liana, y)) {
                    System.out.println("[ok] fruta eliminada en liana " + liana + ", y=" + y);
                } else {
                    System.out.println("[error] no se encontro fruta en liana " + liana + ", y=" + y);
                }
            } catch (NumberFormatException e) {
                System.out.println("error: liana y y deben ser numeros");
            }
        } else {
            System.out.println("accion invalida. use 'add' o 'del'");
        }
    }
    
    /**
     * Maneja comandos de listado.
     */
    private void manejarList(String[] partes) {
        if (partes.length < 2) {
            System.out.println("uso: list entities");
            return;
        }
        
        String tipo = partes[1].toLowerCase();
        if (tipo.equals("entities")) {
            System.out.println(gameManager.listarEntidades());
        } else {
            System.out.println("tipo de listado invalido. use 'entities'");
        }
    }
    
    /**
     * Detiene la consola.
     */
    public void detener() {
        ejecutando.set(false);
    }
}

