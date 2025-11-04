package cr.tec.donceykongjr.server.logic;

import cr.tec.donceykongjr.server.logic.entidades.*;
import cr.tec.donceykongjr.server.logic.eventos.EventoJuego;
import cr.tec.donceykongjr.server.logic.patrones.FactoryEntidad;
import cr.tec.donceykongjr.server.logic.patrones.Observer;
import cr.tec.donceykongjr.server.logic.patrones.Subject;
import cr.tec.donceykongjr.server.util.Config;
import cr.tec.donceykongjr.server.util.LoggerUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona el estado del juego y todas las entidades.
 * Implementa el patrón Observer para notificar cambios a los clientes.
 */
public class GameManager extends Subject {
    private Map<String, Jugador> jugadores;
    private List<Cocodrilo> cocodrilos;
    private List<Fruta> frutas;
    private List<Liana> lianas;
    private int nivel;
    private double velocidadMultiplicador;
    private boolean pausado;
    private long tickActual;
    
    /**
     * Constructor del GameManager.
     */
    public GameManager() {
        this.jugadores = new ConcurrentHashMap<>();
        this.cocodrilos = new ArrayList<>();
        this.frutas = new ArrayList<>();
        this.lianas = new ArrayList<>();
        this.nivel = 1;
        this.velocidadMultiplicador = Config.VELOCIDAD_BASE;
        this.pausado = false;
        this.tickActual = 0;
        inicializarLianas();
    }
    
    /**
     * Inicializa las lianas del juego.
     */
    private void inicializarLianas() {
        // Crear 5 lianas verticales
        for (int i = 0; i < 5; i++) {
            Liana liana = new Liana("L_" + i, 0, 0, i, 10);
            lianas.add(liana);
        }
    }
    
    /**
     * Actualiza el estado del juego en cada tick.
     */
    public void actualizar(double deltaTime) {
        if (pausado) return;
        
        tickActual++;
        
        // Actualizar cocodrilos
        cocodrilos.removeIf(c -> !c.isActivo());
        for (Cocodrilo cocodrilo : cocodrilos) {
            cocodrilo.actualizar(deltaTime * velocidadMultiplicador);
        }
        
        // Actualizar jugadores
        for (Jugador jugador : jugadores.values()) {
            if (jugador.isActivo()) {
                jugador.actualizar(deltaTime);
            }
        }
        
        // Detectar colisiones jugador-cocodrilo
        detectarColisionesJugadorCocodrilo();
        
        // Detectar recogida de frutas
        detectarRecogidaFrutas();
        
        // Verificar objetivos
        verificarObjetivos();
    }
    
    /**
     * Detecta colisiones entre jugadores y cocodrilos.
     */
    private void detectarColisionesJugadorCocodrilo() {
        for (Jugador jugador : jugadores.values()) {
            if (!jugador.isActivo()) continue;
            
            for (Cocodrilo cocodrilo : cocodrilos) {
                if (!cocodrilo.isActivo()) continue;
                
                if (jugador.colisionaCon(cocodrilo)) {
                    boolean sigueVivo = jugador.perderVida();
                    
                    Map<String, Object> payloadHit = new HashMap<>();
                    payloadHit.put("playerId", jugador.getId());
                    payloadHit.put("crocodileId", cocodrilo.getId());
                    EventoJuego evento = new EventoJuego(EventoJuego.TipoEvento.PLAYER_HIT, payloadHit);
                    notificarObservadores(evento);
                    
                    if (!sigueVivo) {
                        Map<String, Object> payloadElim = new HashMap<>();
                        payloadElim.put("playerId", jugador.getId());
                        evento = new EventoJuego(EventoJuego.TipoEvento.PLAYER_ELIMINATED, payloadElim);
                        notificarObservadores(evento);
                        LoggerUtil.info("jugador " + jugador.getId() + " eliminado");
                    }
                }
            }
        }
    }
    
    /**
     * Detecta cuando un jugador recoge una fruta.
     */
    private void detectarRecogidaFrutas() {
        for (Jugador jugador : jugadores.values()) {
            if (!jugador.isActivo()) continue;
            
            Iterator<Fruta> iter = frutas.iterator();
            while (iter.hasNext()) {
                Fruta fruta = iter.next();
                if (!fruta.isRecogida() && jugador.colisionaCon(fruta)) {
                    fruta.recoger();
                    jugador.agregarPuntos(fruta.getPuntos());
                    
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("playerId", jugador.getId());
                    payload.put("points", fruta.getPuntos());
                    EventoJuego evento = new EventoJuego(EventoJuego.TipoEvento.FRUIT_TAKEN, payload);
                    notificarObservadores(evento);
                    
                    iter.remove();
                    LoggerUtil.debug("jugador " + jugador.getId() + " recogio fruta: " + fruta.getPuntos() + " puntos");
                }
            }
        }
    }
    
    /**
     * Verifica si algún jugador alcanzó el objetivo.
     */
    private void verificarObjetivos() {
        for (Jugador jugador : jugadores.values()) {
            if (jugador.isActivo() && jugador.haAlcanzadoObjetivo()) {
                nivelUp();
                Map<String, Object> payload = new HashMap<>();
                payload.put("playerId", jugador.getId());
                payload.put("level", nivel);
                EventoJuego evento = new EventoJuego(EventoJuego.TipoEvento.PLAYER_WIN, payload);
                notificarObservadores(evento);
            }
        }
    }
    
    /**
     * Aumenta el nivel y la velocidad del juego.
     */
    public void nivelUp() {
        nivel++;
        velocidadMultiplicador += Config.MULTIPLICADOR_VELOCIDAD_NIVEL;
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("level", nivel);
        payload.put("speedMultiplier", velocidadMultiplicador);
        EventoJuego evento = new EventoJuego(EventoJuego.TipoEvento.LEVEL_UP, payload);
        notificarObservadores(evento);
        
        LoggerUtil.info("nivel subido a " + nivel + ". velocidad: " + velocidadMultiplicador);
    }
    
    /**
     * Agrega un jugador al juego.
     */
    public boolean agregarJugador(String id, double x, double y, int liana) {
        if (jugadores.size() >= Config.MAX_JUGADORES) {
            LoggerUtil.warning("no se puede agregar mas jugadores. maximo alcanzado.");
            return false;
        }
        
        if (jugadores.containsKey(id)) {
            LoggerUtil.warning("jugador " + id + " ya existe.");
            return false;
        }
        
        Jugador jugador = FactoryEntidad.crearJugador(id, x, y, liana);
        jugadores.put(id, jugador);
        LoggerUtil.info("jugador " + id + " agregado al juego");
        notificarObservadores();
        return true;
    }
    
    /**
     * Elimina un jugador del juego.
     */
    public void eliminarJugador(String id) {
        jugadores.remove(id);
        LoggerUtil.info("jugador " + id + " eliminado del juego");
        notificarObservadores();
    }
    
    /**
     * Obtiene un jugador por ID.
     */
    public Jugador getJugador(String id) {
        return jugadores.get(id);
    }
    
    /**
     * Procesa input de un jugador.
     */
    public void procesarInput(String jugadorId, String accion, double velocidad) {
        Jugador jugador = jugadores.get(jugadorId);
        if (jugador == null || !jugador.isActivo()) return;
        
        switch (accion) {
            case "MOVE_UP":
                jugador.moverArriba(velocidad * velocidadMultiplicador);
                break;
            case "MOVE_DOWN":
                jugador.moverAbajo(velocidad * velocidadMultiplicador);
                break;
            case "LEFT":
                jugador.moverIzquierda(velocidad * velocidadMultiplicador);
                break;
            case "RIGHT":
                jugador.moverDerecha(velocidad * velocidadMultiplicador);
                break;
            case "JUMP":
                jugador.saltar(velocidad * velocidadMultiplicador);
                break;
            case "GRAB":
                jugador.agarrarLiana();
                break;
        }
        
        notificarObservadores();
    }
    
    /**
     * Agrega un cocodrilo rojo.
     */
    public void agregarCocodriloRojo(int liana, double y) {
        Cocodrilo cocodrilo = FactoryEntidad.crearCocodriloRojo(liana, y, velocidadMultiplicador);
        cocodrilos.add(cocodrilo);
        LoggerUtil.info("cocodrilo rojo creado en liana " + liana + ", y=" + y);
        notificarObservadores();
    }
    
    /**
     * Agrega un cocodrilo azul.
     */
    public void agregarCocodriloAzul(int liana, double y) {
        Cocodrilo cocodrilo = FactoryEntidad.crearCocodriloAzul(liana, y, velocidadMultiplicador);
        cocodrilos.add(cocodrilo);
        LoggerUtil.info("cocodrilo azul creado en liana " + liana + ", y=" + y);
        notificarObservadores();
    }
    
    /**
     * Agrega una fruta.
     */
    public boolean agregarFruta(int liana, double y, int puntos) {
        Fruta fruta = FactoryEntidad.crearFruta(liana, y, puntos);
        frutas.add(fruta);
        LoggerUtil.info("fruta creada en liana " + liana + ", y=" + y + ", puntos=" + puntos);
        notificarObservadores();
        return true;
    }
    
    /**
     * Elimina una fruta.
     */
    public boolean eliminarFruta(int liana, double y) {
        Iterator<Fruta> iter = frutas.iterator();
        while (iter.hasNext()) {
            Fruta fruta = iter.next();
            if (fruta.getLiana() == liana && Math.abs(fruta.getY() - y) < 0.5) {
                iter.remove();
                LoggerUtil.info("fruta eliminada en liana " + liana + ", y=" + y);
                notificarObservadores();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Obtiene el estado actual del juego para enviarlo a los clientes.
     */
    public Map<String, Object> getEstadoJuego() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("tick", tickActual);
        estado.put("level", nivel);
        estado.put("speedMultiplier", velocidadMultiplicador);
        estado.put("paused", pausado);
        
        // Jugadores
        List<Map<String, Object>> jugadoresData = new ArrayList<>();
        for (Jugador jugador : jugadores.values()) {
            Map<String, Object> j = new HashMap<>();
            j.put("id", jugador.getId());
            j.put("x", jugador.getX());
            j.put("y", jugador.getY());
            j.put("liana", jugador.getLiana());
            j.put("lives", jugador.getVidas());
            j.put("score", jugador.getPuntaje());
            j.put("active", jugador.isActivo());
            jugadoresData.add(j);
        }
        estado.put("players", jugadoresData);
        
        // Cocodrilos
        List<Map<String, Object>> cocodrilosData = new ArrayList<>();
        for (Cocodrilo cocodrilo : cocodrilos) {
            if (cocodrilo.isActivo()) {
                Map<String, Object> c = new HashMap<>();
                c.put("id", cocodrilo.getId());
                c.put("kind", cocodrilo.getTipo().toString());
                c.put("liana", cocodrilo.getLiana());
                c.put("y", cocodrilo.getY());
                cocodrilosData.add(c);
            }
        }
        estado.put("crocodiles", cocodrilosData);
        
        // Frutas
        List<Map<String, Object>> frutasData = new ArrayList<>();
        for (Fruta fruta : frutas) {
            if (!fruta.isRecogida()) {
                Map<String, Object> f = new HashMap<>();
                f.put("id", fruta.getId());
                f.put("liana", fruta.getLiana());
                f.put("y", fruta.getY());
                f.put("points", fruta.getPuntos());
                frutasData.add(f);
            }
        }
        estado.put("fruits", frutasData);
        
        return estado;
    }
    
    /**
     * Lista todas las entidades (para CLI).
     */
    public String listarEntidades() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ENTIDADES DEL JUEGO ===\n");
        sb.append("Jugadores: ").append(jugadores.size()).append("\n");
        for (Jugador j : jugadores.values()) {
            sb.append("  - ").append(j.getId()).append(" (liana=").append(j.getLiana())
              .append(", y=").append(String.format("%.2f", j.getY()))
              .append(", vidas=").append(j.getVidas())
              .append(", puntos=").append(j.getPuntaje()).append(")\n");
        }
        sb.append("Cocodrilos: ").append(cocodrilos.size()).append("\n");
        for (Cocodrilo c : cocodrilos) {
            sb.append("  - ").append(c.getId()).append(" (").append(c.getTipo())
              .append(", liana=").append(c.getLiana())
              .append(", y=").append(String.format("%.2f", c.getY())).append(")\n");
        }
        sb.append("Frutas: ").append(frutas.size()).append("\n");
        for (Fruta f : frutas) {
            sb.append("  - ").append(f.getId()).append(" (liana=").append(f.getLiana())
              .append(", y=").append(String.format("%.2f", f.getY()))
              .append(", puntos=").append(f.getPuntos()).append(")\n");
        }
        return sb.toString();
    }
    
    // Getters y Setters
    public boolean isPausado() {
        return pausado;
    }
    
    public void setPausado(boolean pausado) {
        this.pausado = pausado;
        LoggerUtil.info("juego " + (pausado ? "pausado" : "reanudado"));
        notificarObservadores();
    }
    
    public int getNivel() {
        return nivel;
    }
    
    public double getVelocidadMultiplicador() {
        return velocidadMultiplicador;
    }
    
    public long getTickActual() {
        return tickActual;
    }
}

