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

    /**
     * Tipo de cliente conectado.
     */
    private enum TipoCliente {
        PLAYER,      // Jugador activo que puede enviar inputs
        SPECTATOR,   // Espectador que solo recibe estado
        UNDEFINED    // No se ha determinado el tipo
    }

    private Socket socket;
    private GameManager gameManager;
    private BufferedReader entrada;
    private PrintWriter salida;
    private final Object salidaLock = new Object();
    private String jugadorId;
    private boolean conectado;
    private TipoCliente tipoCliente;
    
    /**
     * Constructor del manejador de cliente.
     */
    public ManejadorCliente(Socket socket, GameManager gameManager) {
        this.socket = socket;
        this.gameManager = gameManager;
        this.conectado = true;
        this.tipoCliente = TipoCliente.UNDEFINED;
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
     * Determina si el cliente es jugador o espectador según el campo clientType.
     * Si no se especifica clientType, se asume "PLAYER" por compatibilidad.
     */
    private void manejarConexion(Mensaje mensaje) {
        if (mensaje.getId() == null) {
            enviarError("ID de cliente requerido");
            desconectar();
            return;
        }

        jugadorId = mensaje.getId();

        // Extraer tipo de cliente del mensaje (default: PLAYER)
        String clientTypeStr = mensaje.getClientType();
        if (clientTypeStr == null || clientTypeStr.isEmpty()) {
            clientTypeStr = "PLAYER";  // Compatibilidad con clientes antiguos
        }

        // Procesar según tipo de cliente
        if ("PLAYER".equalsIgnoreCase(clientTypeStr)) {
            manejarConexionJugador();
        } else if ("SPECTATOR".equalsIgnoreCase(clientTypeStr)) {
            manejarConexionEspectador();
        } else {
            enviarError("Tipo de cliente inválido: " + clientTypeStr);
            desconectar();
        }
    }

    /**
     * Procesa la conexión de un jugador.
     */
    private void manejarConexionJugador() {
        boolean agregado = gameManager.agregarJugador(jugadorId, 0, 5, 0);

        if (agregado) {
            tipoCliente = TipoCliente.PLAYER;
            LoggerUtil.info("jugador " + jugadorId + " registrado exitosamente");
            enviarEstado();
        } else {
            enviarError("No se puede conectar como jugador: límite alcanzado (máximo " +
                       gameManager.contarJugadoresActivos() + " jugador)");
            LoggerUtil.warning("conexión de jugador " + jugadorId + " rechazada: límite alcanzado");
            desconectar();
        }
    }

    /**
     * Procesa la conexión de un espectador.
     */
    private void manejarConexionEspectador() {
        // Verificar que haya al menos un jugador activo
        if (!gameManager.hayJugadorActivo()) {
            enviarError("No hay partidas activas para observar");
            LoggerUtil.warning("espectador " + jugadorId + " rechazado: no hay jugadores activos");
            desconectar();
            return;
        }

        // Intentar registrar como espectador
        boolean registrado = gameManager.registrarEspectador(jugadorId);

        if (registrado) {
            tipoCliente = TipoCliente.SPECTATOR;
            LoggerUtil.info("espectador " + jugadorId + " conectado exitosamente");
            enviarEstado();
        } else {
            enviarError("No se puede conectar como espectador: límite alcanzado");
            LoggerUtil.warning("conexión de espectador " + jugadorId + " rechazada: límite alcanzado");
            desconectar();
        }
    }
    
    /**
     * Maneja el input de un jugador.
     * Los espectadores NO pueden enviar inputs.
     */
    private void manejarInput(Mensaje mensaje) {
        // Verificar que sea un jugador
        if (tipoCliente != TipoCliente.PLAYER) {
            enviarError("Los espectadores no pueden enviar inputs");
            LoggerUtil.warning("espectador " + jugadorId + " intentó enviar input (rechazado)");
            return;
        }

        if (jugadorId == null) {
            enviarError("Cliente no identificado");
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

        // Eliminar del GameManager según tipo
        if (jugadorId != null) {
            if (tipoCliente == TipoCliente.PLAYER) {
                gameManager.eliminarJugador(jugadorId);
                LoggerUtil.info("jugador " + jugadorId + " desconectado");
            } else if (tipoCliente == TipoCliente.SPECTATOR) {
                gameManager.eliminarEspectador(jugadorId);
                LoggerUtil.info("espectador " + jugadorId + " desconectado");
            }
        }

        // Eliminar como observador
        gameManager.eliminarObservador(this);

        // Cerrar socket
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
