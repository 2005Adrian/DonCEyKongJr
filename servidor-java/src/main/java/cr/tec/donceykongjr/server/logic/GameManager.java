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
    private double velocidadMultiplicador;
    private boolean pausado;
    private long tickActual;
    private double celebracionRestante;
    private boolean reinicioPendiente;

    // Motor de cocodrilos independiente
    private MotorCocodrilos motorCocodrilos;

    /**
     * Constructor del GameManager.
     */
    public GameManager() {
        this.jugadores = new ConcurrentHashMap<>();
        this.cocodrilos = new ArrayList<>();
        this.frutas = new ArrayList<>();
        this.lianas = new ArrayList<>();
        this.velocidadMultiplicador = Config.VELOCIDAD_BASE;
        this.pausado = false;
        this.tickActual = 0;
        this.celebracionRestante = 0;
        this.reinicioPendiente = false;

        // Crear motor de cocodrilos con dt=0.1s (10 TPS)
        this.motorCocodrilos = new MotorCocodrilos(0.1);

        inicializarLianas();
        inicializarEntidades();

        // Iniciar motor de cocodrilos
        motorCocodrilos.start();
        LoggerUtil.info("Motor de cocodrilos iniciado");
    }
    
    /**
     * Inicializa las lianas del juego.
     */
    private void inicializarLianas() {
        // Crear 5 lianas verticales con límites explícitos
        for (int i = 0; i < 5; i++) {
            Liana liana = new Liana("L_" + i, 0, 0, i, 0.0, 500.0);
            lianas.add(liana);

            // Registrar liana en el motor de cocodrilos
            motorCocodrilos.registrarLiana(liana);
        }
    }

    /**
     * Inicializa las entidades del juego (cocodrilos y frutas).
     */
    private void inicializarEntidades() {
        // Crear 2 cocodrilos rojos iniciales usando el motor (velocidad x2)
        motorCocodrilos.crearCocodriloRojo(0, 150.0, 60.0, -1); // Liana 0, empieza bajando
        motorCocodrilos.crearCocodriloRojo(2, 300.0, 70.0, 1);  // Liana 2, empieza subiendo

        LoggerUtil.info("2 cocodrilos rojos creados en el motor");

        // Crear frutas iniciales (2-4 frutas en diferentes lianas)
        Random random = new Random();
        int numFrutas = 2 + random.nextInt(3);
        for (int i = 0; i < numFrutas; i++) {
            int liana = random.nextInt(Math.max(1, lianas.size()));
            double y = 150.0 + random.nextDouble() * 250.0; // Y entre 150 y 400
            int puntos = 10; // Puntos base

            Fruta fruta = new Fruta("FRUTA_" + i, 0.0, y, liana, puntos);
            frutas.add(fruta);
            LoggerUtil.info("fruta creada en liana " + liana + ", y=" + String.format("%.2f", y) + ", puntos=" + puntos);
        }

        LoggerUtil.info("inicialización completada: 2 cocodrilos rojos, " + numFrutas + " frutas");
    }

    /**
     * Actualiza el estado del juego en cada tick.
     */
    public void actualizar(double deltaTime) {
        tickActual++;

        if (pausado) {
            notificarObservadores();
            return;
        }

        // Sincronizar cocodrilos del motor a la lista local (para colisiones)
        sincronizarCocodrilosDesdeMotor();

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

        // Detectar caidas al abismo
        detectarCaidaAbismo();

        // Verificar objetivos
        verificarObjetivos();

        // Gestionar celebraciones pendientes
        actualizarCelebracion(deltaTime);

        // Notificar a los clientes sobre el estado actualizado
        notificarObservadores();
    }

    /**
     * Sincroniza los cocodrilos del motor con la lista local para detección de colisiones.
     */
    private void sincronizarCocodrilosDesdeMotor() {
        // Obtener snapshot del motor
        SnapshotSistemaCocodrilos snapshot = motorCocodrilos.getSnapshot();

        // Limpiar lista actual
        cocodrilos.clear();

        // Convertir snapshots a instancias de Cocodrilo para colisiones
        for (SnapshotCocodrilo snap : snapshot.getCocodrilos()) {
            if (snap.isActivo()) {
                // Crear instancia temporal para detección de colisiones
                Cocodrilo temp;
                if (snap.getTipo() == Cocodrilo.TipoCocodrilo.ROJO) {
                    temp = new CocodriloRojo(snap.getId(), 0, snap.getY(), snap.getLianaId(), snap.getVelocidadBase());
                } else {
                    temp = new CocodriloAzul(snap.getId(), 0, snap.getY(), snap.getLianaId(), snap.getVelocidadBase());
                }
                cocodrilos.add(temp);
            }
        }
    }
    
    /**
     * Detecta colisiones entre jugadores y cocodrilos.
     */
    private void detectarColisionesJugadorCocodrilo() {
        for (Jugador jugador : jugadores.values()) {
            if (!jugador.isActivo() || jugador.estaCelebrando()) {
                continue;
            }
            Integer lianaJugador = jugador.getLianaId();
            if (lianaJugador == null) {
                continue;
            }

            for (Cocodrilo cocodrilo : cocodrilos) {
                if (!cocodrilo.isActivo()) {
                    continue;
                }
                if (!Objects.equals(lianaJugador, cocodrilo.getLianaId())) {
                    continue;
                }

                if (Math.abs(jugador.getY() - cocodrilo.getY()) <= Config.JUGADOR_DELTA_Y_COCODRILO) {
                    manejarGolpeJugador(jugador, "CROCODILE:" + cocodrilo.getId());
                    break;
                }
            }
        }
    }
    
    /**
     * Detecta cuando un jugador recoge una fruta.
     */
    private void detectarRecogidaFrutas() {
        for (Jugador jugador : jugadores.values()) {
            if (!jugador.isActivo() || jugador.estaCelebrando()) continue;
            Integer lianaJugador = jugador.getLianaId();
            if (lianaJugador == null) continue;
            
            Iterator<Fruta> iter = frutas.iterator();
            while (iter.hasNext()) {
                Fruta fruta = iter.next();
                if (!fruta.isRecogida()
                        && lianaJugador == fruta.getLiana()
                        && Math.abs(jugador.getY() - fruta.getY()) <= Config.JUGADOR_DELTA_Y_FRUTA) {
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
     * Detecta si algun jugador salio de los limites verticales permitidos.
     */
    private void detectarCaidaAbismo() {
        for (Jugador jugador : jugadores.values()) {
            if (!jugador.isActivo() || jugador.estaCelebrando()) continue;
            if (jugador.getY() < Config.JUGADOR_Y_MIN) {
                manejarGolpeJugador(jugador, "ABYSS");
            }
        }
    }

    private void manejarGolpeJugador(Jugador jugador, String causa) {
        boolean sigueEnJuego = jugador.perderVida();

        Map<String, Object> payloadHit = new HashMap<>();
        payloadHit.put("playerId", jugador.getId());
        payloadHit.put("cause", causa);
        EventoJuego evento = new EventoJuego(EventoJuego.TipoEvento.PLAYER_HIT, payloadHit);
        notificarObservadores(evento);

        if (!sigueEnJuego) {
            Map<String, Object> payloadElim = new HashMap<>();
            payloadElim.put("playerId", jugador.getId());
            evento = new EventoJuego(EventoJuego.TipoEvento.PLAYER_ELIMINATED, payloadElim);
            notificarObservadores(evento);
            LoggerUtil.info("jugador " + jugador.getId() + " eliminado");
        } else {
            jugador.respawnEnSpawn();
        }
    }
    
        /**
     * Verifica si algun jugador alcanzo el objetivo.
     */
    private void verificarObjetivos() {
        if (reinicioPendiente) {
            return;
        }

        for (Jugador jugador : jugadores.values()) {
            if (!jugador.isActivo() || jugador.estaCelebrando()) {
                continue;
            }
            Integer lianaActual = jugador.getLianaId();
            if (lianaActual == null) {
                continue;
            }

            if (lianaActual == Config.OBJETIVO_LIANA && jugador.getY() <= Config.OBJETIVO_Y) {
                iniciarCelebracion(jugador);
                break;
            }
        }
    }

    private void iniciarCelebracion(Jugador jugador) {
        reinicioPendiente = true;
        celebracionRestante = Config.JUGADOR_TIEMPO_CELEBRACION;
        jugador.iniciarCelebracion();

        try {
            motorCocodrilos.incrementarDificultad(Config.COCODRILO_INCREMENTO_DIFICULTAD);
        } catch (IllegalArgumentException e) {
            LoggerUtil.warning("no se pudo incrementar dificultad: " + e.getMessage());
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("playerId", jugador.getId());
        EventoJuego evento = new EventoJuego(EventoJuego.TipoEvento.PLAYER_WIN, payload);
        notificarObservadores(evento);

        LoggerUtil.info("Rescate completado por " + jugador.getId() + ". Factor cocodrilos x" +
                String.format("%.2f", motorCocodrilos.getFactorDificultad()));
    }

    private void actualizarCelebracion(double deltaTime) {
        if (!reinicioPendiente) {
            return;
        }
        celebracionRestante -= deltaTime;
        if (celebracionRestante <= 0) {
            reiniciarManteniendoMapa();
        }
    }

    /**
     * Reinicia el mapa manteniendo la configuracion actual.
     * Reubica a los jugadores sin alterar cocodrilos ni frutas.
     */
    public void reiniciarManteniendoMapa() {
        for (Jugador jugador : jugadores.values()) {
            jugador.respawnEnSpawn();
        }

        celebracionRestante = 0;
        reinicioPendiente = false;
        LoggerUtil.info("Mapa reiniciado. Factor cocodrilos x" +
                String.format("%.2f", motorCocodrilos.getFactorDificultad()));
    }

    /**
     * Metodo legacy para compatibilidad con CLI.
     */
    @Deprecated
    public void reiniciar() {
        reiniciarManteniendoMapa();
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
        
        Jugador jugador = FactoryEntidad.crearJugador(
                id,
                Config.JUGADOR_SPAWN_X,
                Config.JUGADOR_SPAWN_Y,
                Config.JUGADOR_SPAWN_LIANA,
                lianas
        );
        jugadores.put(id, jugador);
        LoggerUtil.info("jugador " + id + " agregado al juego");
        return true;
    }
    
    /**
     * Elimina un jugador del juego.
     */
    public void eliminarJugador(String id) {
        jugadores.remove(id);
        LoggerUtil.info("jugador " + id + " eliminado del juego");
        if (jugadores.isEmpty()) {
            reinicioPendiente = false;
            celebracionRestante = 0;
        }
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
    public void procesarInput(String jugadorId, String accion) {
        Jugador jugador = jugadores.get(jugadorId);
        if (jugador == null) {
            return;
        }

        boolean aceptado = jugador.registrarInput(accion);
        if (!aceptado) {
            LoggerUtil.debug("input ignorado para jugador " + jugadorId + ": " + accion);
            return;
        }
    }
    
    /**
     * Agrega un cocodrilo rojo usando el motor.
     * @return null si se creó exitosamente, mensaje de error si falló
     */
    public String agregarCocodriloRojo(int liana, double y) {
        // Validar liana
        if (liana < 0 || liana >= lianas.size()) {
            String error = "Liana inválida. Debe ser entre 0 y " + (lianas.size() - 1);
            LoggerUtil.warning(error);
            return error;
        }

        // Validar posición Y
        Liana lianaObj = lianas.get(liana);
        if (y < lianaObj.getAlturaMin() || y > lianaObj.getAlturaMax()) {
            String error = String.format("Posición Y inválida. Debe estar entre %.1f y %.1f",
                lianaObj.getAlturaMin(), lianaObj.getAlturaMax());
            LoggerUtil.warning(error);
            return error;
        }

        // Crear cocodrilo rojo usando el motor (dirección aleatoria, velocidad x2)
        int direccion = (Math.random() < 0.5) ? -1 : 1;
        String id = motorCocodrilos.crearCocodriloRojo(liana, y, 60.0, direccion);

        if (id != null) {
            LoggerUtil.info("cocodrilo rojo creado en motor: liana=" + liana + ", y=" + y + ", id=" + id);
            return null; // Éxito
        } else {
            String error = "No se pudo crear el cocodrilo en el motor";
            LoggerUtil.warning(error);
            return error;
        }
    }

    /**
     * Agrega un cocodrilo azul usando el motor.
     * @return null si se creó exitosamente, mensaje de error si falló
     */
    public String agregarCocodriloAzul(int liana, double y) {
        // Validar liana
        if (liana < 0 || liana >= lianas.size()) {
            String error = "Liana inválida. Debe ser entre 0 y " + (lianas.size() - 1);
            LoggerUtil.warning(error);
            return error;
        }

        // Validar posición Y
        Liana lianaObj = lianas.get(liana);
        if (y < lianaObj.getAlturaMin() || y > lianaObj.getAlturaMax()) {
            String error = String.format("Posición Y inválida. Debe estar entre %.1f y %.1f",
                lianaObj.getAlturaMin(), lianaObj.getAlturaMax());
            LoggerUtil.warning(error);
            return error;
        }

        // Crear cocodrilo azul usando el motor (velocidad x2)
        String id = motorCocodrilos.crearCocodriloAzul(liana, y, 50.0);

        if (id != null) {
            LoggerUtil.info("cocodrilo azul creado en motor: liana=" + liana + ", y=" + y + ", id=" + id);
            return null; // Éxito
        } else {
            String error = "No se pudo crear el cocodrilo en el motor";
            LoggerUtil.warning(error);
            return error;
        }
    }
    
    /**
     * Agrega una fruta.
     * @return null si se creó exitosamente, mensaje de error si falló
     */
    public String agregarFruta(int liana, double y, int puntos) {
        // Validar liana
        if (liana < 0 || liana >= lianas.size()) {
            String error = "Liana inválida. Debe ser entre 0 y " + (lianas.size() - 1);
            LoggerUtil.warning(error);
            return error;
        }

        // Validar posición Y
        Liana lianaObj = lianas.get(liana);
        if (y < lianaObj.getAlturaMin() || y > lianaObj.getAlturaMax()) {
            String error = String.format("Posición Y inválida. Debe estar entre %.1f y %.1f",
                lianaObj.getAlturaMin(), lianaObj.getAlturaMax());
            LoggerUtil.warning(error);
            return error;
        }

        // Validar que no haya fruta duplicada en la misma posición
        for (Fruta f : frutas) {
            if (f.getLiana() == liana && Math.abs(f.getY() - y) < 10.0) {
                String error = String.format("Ya existe una fruta en esa posición (liana=%d, y=%.1f). " +
                    "Las frutas deben estar separadas por al menos 10 unidades.", liana, f.getY());
                LoggerUtil.warning(error);
                return error;
            }
        }

        // Validar puntos
        if (puntos <= 0) {
            String error = "Los puntos deben ser mayores a 0";
            LoggerUtil.warning(error);
            return error;
        }

        Fruta fruta = FactoryEntidad.crearFruta(liana, y, puntos);
        frutas.add(fruta);
        LoggerUtil.info("fruta creada en liana " + liana + ", y=" + y + ", puntos=" + puntos);
        return null; // Éxito
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
        estado.put("speedMultiplier", motorCocodrilos.getFactorDificultad());
        estado.put("paused", pausado);

        // Jugadores
        List<Map<String, Object>> jugadoresData = new ArrayList<>();
        for (Jugador jugador : jugadores.values()) {
            SnapshotJugador snapshot = new SnapshotJugador(jugador);
            Map<String, Object> j = new HashMap<>();
            j.put("id", snapshot.getId());
            j.put("x", snapshot.getX());
            j.put("y", snapshot.getY());
            j.put("vx", snapshot.getVx());
            j.put("vy", snapshot.getVy());
            j.put("liana", snapshot.getRenderLiana());
            j.put("lianaId", snapshot.getLianaId());
            j.put("state", snapshot.getEstado().name());
            j.put("facing", snapshot.getFacing().name());
            j.put("lives", snapshot.getLives());
            j.put("score", snapshot.getScore());
            j.put("active", snapshot.isActivo());
            j.put("celebrating", snapshot.isCelebrando());
            jugadoresData.add(j);
        }
        estado.put("players", jugadoresData);
        estado.put("celebrationPending", reinicioPendiente);
        estado.put("celebrationTimer", Math.max(celebracionRestante, 0));

        // Cocodrilos - obtener del motor
        List<Map<String, Object>> cocodrilosData = new ArrayList<>();
        SnapshotSistemaCocodrilos snapshot = motorCocodrilos.getSnapshot();
        for (SnapshotCocodrilo croc : snapshot.getCocodrilos()) {
            if (croc.isActivo()) {
                Map<String, Object> c = new HashMap<>();
                c.put("id", croc.getId());
                c.put("kind", croc.getTipo().toString());
                c.put("liana", croc.getLianaId());
                c.put("y", croc.getY());
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
     * Detiene el motor de cocodrilos y limpia recursos.
     */
    public void shutdown() {
        if (motorCocodrilos != null) {
            motorCocodrilos.stop();
            LoggerUtil.info("Motor de cocodrilos detenido");
        }
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
              .append(", puntos=").append(j.getPuntaje())
              .append(", estado=").append(j.getEstado())
              .append(", facing=").append(j.getFacing()).append(")\n");
        }

        // Obtener cocodrilos del motor
        SnapshotSistemaCocodrilos snapshot = motorCocodrilos.getSnapshot();
        sb.append("Cocodrilos: ").append(snapshot.getTotalCocodrilosActivos()).append("\n");
        for (SnapshotCocodrilo c : snapshot.getCocodrilos()) {
            if (c.isActivo()) {
                sb.append("  - ").append(c.getId()).append(" (").append(c.getTipo())
                  .append(", liana=").append(c.getLianaId())
                  .append(", y=").append(String.format("%.2f", c.getY()))
                  .append(", dir=").append(c.getDireccion() > 0 ? "↑" : "↓").append(")\n");
            }
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
    }

    public double getVelocidadMultiplicador() {
        return velocidadMultiplicador;
    }

    public long getTickActual() {
        return tickActual;
    }
}


