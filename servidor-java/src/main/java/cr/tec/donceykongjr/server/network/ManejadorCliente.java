package cr.tec.donceykongjr.server.network;

import java.io.*;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private Socket socket;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            salida.println("Conectado al servidor DonCEy Kong Jr!");
            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                System.out.println("📩 Cliente dice: " + mensaje);
                salida.println("Eco: " + mensaje);
            }
        } catch (IOException e) {
            System.out.println("⚠️ Cliente desconectado: " + e.getMessage());
        }
    }
}
