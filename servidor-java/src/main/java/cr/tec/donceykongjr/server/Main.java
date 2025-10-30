package cr.tec.donceykongjr.server;

import cr.tec.donceykongjr.server.network.ServidorJuego;

public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Iniciando servidor DonCEy Kong Jr...");
        ServidorJuego servidor = new ServidorJuego(5000);
        servidor.iniciar();
    }
}
