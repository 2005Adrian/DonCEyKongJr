package cr.tec.donceykongjr.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorJuego {
    private ServerSocket serverSocket;
    private boolean enEjecucion = true;

    public ServidorJuego(int puerto) {
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en el puerto " + puerto);
        } catch (IOException e) {
            System.out.println("❌ Error al iniciar el servidor: " + e.getMessage());
        }
    }

    public void iniciar() {
        try {
            while (enEjecucion) {
                Socket cliente = serverSocket.accept();
                System.out.println("🟢 Cliente conectado desde " + cliente.getInetAddress());
                ManejadorCliente manejador = new ManejadorCliente(cliente);
                new Thread(manejador).start();
            }
        } catch (IOException e) {
            System.out.println("❌ Error al aceptar conexiones: " + e.getMessage());
        }
    }

    public void detener() {
        enEjecucion = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error al cerrar el servidor: " + e.getMessage());
        }
    }
}
