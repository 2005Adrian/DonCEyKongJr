package cr.tec.donceykongjr.server.network;

import cr.tec.donceykongjr.server.logic.GameManager;
import cr.tec.donceykongjr.server.logic.eventos.EventoJuego;
import cr.tec.donceykongjr.server.logic.patrones.Observer;
import cr.tec.donceykongjr.server.util.LoggerUtil;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * Maneja la comunicación con un cliente individual.
 * Implementa Observer para recibir actualizaciones del GameManager.
 */
public class ManejadorCliente implements Runnable, Observer {
    private Socket socket;
    private GameManager gameManager;
    private BufferedReader entrada;
    private PrintWriter salida;
    private final Object salidaLock = new Object();
    private String jugadorId;
    private boolean conectado;
    private boolean esJugador;
    
    /**
     * Constructor del manejador de cliente.
     */
    public ManejadorCliente(Socket socket, GameManager gameManager) {
        this.socket = socket;
        this.gameManager = gameManager;
        this.conectado = true;
        this.esJugador = false;
    }
    
    @Override
    public void run() {
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            
            // Registrar como observador
            gameManager.agregarObservador(this);
            
            // Enviar estado inicial
            enviarEstado();
            
            // Leer mensajes del cliente
            String linea;
            while (conectado && (linea = entrada.readLine()) != null) {
                procesarMensaje(linea);
            }
        } catch (IOException e) {
            LoggerUtil.warning("cliente desconectado: " + e.getMessage());
        } finally {
            desconectar();
        }
    }
    
    /**
     * Procesa un mensaje JSON recibido del cliente.
     */
    private void procesarMensaje(String json) {
        try {
            Mensaje mensaje = JsonUtil.fromJson(json);
            if (mensaje == null) {
                LoggerUtil.warning("mensaje json invalido recibido");
                return;
            }
            
            switch (mensaje.getType()) {
                case CONNECT:
                    manejarConexion(mensaje);
                    break;
                case INPUT:
                    manejarInput(mensaje);
                    break;
                case DISCONNECT:
                    desconectar();
                    break;
                default:
                    LoggerUtil.debug("tipo de mensaje no reconocido: " + mensaje.getType());
            }
        } catch (Exception e) {
            LoggerUtil.error("error al procesar mensaje: " + e.getMessage());
            enviarError("error al procesar mensaje: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la conexión de un nuevo cliente.
     */
    private void manejarConexion(Mensaje mensaje) {
        if (mensaje.getId() != null) {
            jugadorId = mensaje.getId();
            // Intentar agregar como jugador
            if (gameManager.agregarJugador(jugadorId, 0, 5, 0)) {
                esJugador = true;
                LoggerUtil.info("jugador " + jugadorId + " registrado");
                enviarEstado();
            } else {
                // Si no se pudo agregar como jugador, es espectador
                esJugador = false;
                LoggerUtil.info("cliente " + jugadorId + " conectado como espectador");
                enviarEstado();
            }
        }
    }
    
    /**
     * Maneja el input de un jugador.
     */
    private void manejarInput(Mensaje mensaje) {
        if (!esJugador || jugadorId == null) {
            enviarError("No eres un jugador registrado");
            return;
        }
        
        String accion = mensaje.getAction();
        if (accion != null) {
            gameManager.procesarInput(jugadorId, accion);
        }
    }
    
    /**
     * Envía el estado actual del juego al cliente.
     */
    private void enviarEstado() {
        Map<String, Object> estado = gameManager.getEstadoJuego();
        String json = JsonUtil.crearMensajeEstado(estado);
        enviarJson(json);
    }
    
    /**
     * Envía un mensaje de error al cliente.
     */
    private void enviarError(String mensajeError) {
        String json = JsonUtil.crearMensajeError(mensajeError);
        enviarJson(json);
    }
    
    /**
     * Desconecta el cliente y limpia recursos.
     */
    private void desconectar() {
        conectado = false;
        
        if (jugadorId != null && esJugador) {
            gameManager.eliminarJugador(jugadorId);
            LoggerUtil.info("jugador " + jugadorId + " desconectado");
        }
        
        gameManager.eliminarObservador(this);
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            LoggerUtil.error("error al cerrar socket: " + e.getMessage());
        }
    }
    
    /**
     * Implementación de Observer: se llama cuando el GameManager notifica cambios.
     */
    @Override
    public void actualizar(Object dato) {
        if (!conectado) return;
        
        try {
            // Si es un evento específico, enviarlo
            if (dato instanceof EventoJuego) {
                EventoJuego evento = (EventoJuego) dato;
                enviarEvento(evento);
            } else {
                enviarEstado();
            }
        } catch (Exception e) {
            LoggerUtil.error("error al actualizar cliente: " + e.getMessage());
        }
    }

    private void enviarEvento(EventoJuego evento) {
        String json = JsonUtil.crearMensajeEvento(evento.getTipo().toString(), evento.getPayload());
        enviarJson(json);
    }

    private void enviarJson(String json) {
        if (json == null || salida == null || !conectado) {
            return;
        }
        synchronized (salidaLock) {
            salida.println(json);
        }
    }
}
