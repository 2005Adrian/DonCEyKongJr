package cr.tec.donceykongjr.server.util;

/**
 * Configuración global centralizada del servidor DonCEy Kong Jr.
 *
 * <p>Esta clase utilitaria contiene todas las constantes de configuración del juego,
 * organizadas por categorías. Centralizar la configuración facilita el balance del juego,
 * el debugging y los ajustes de dificultad.</p>
 *
 * <p><b>Categorías de configuración:</b></p>
 * <ul>
 *   <li><b>Red:</b> Puertos, límites de conexiones</li>
 *   <li><b>Game Loop:</b> Tasa de actualización (TPS)</li>
 *   <li><b>Jugador:</b> Física, movimiento, spawn, vidas</li>
 *   <li><b>Mapa:</b> Dimensiones, nivel del agua, objetivo</li>
 *   <li><b>Dificultad:</b> Escalado de velocidad de cocodrilos</li>
 * </ul>
 *
 * <p><b>Filosofía de diseño:</b></p>
 * <ul>
 *   <li>Todas las constantes son {@code public static final}</li>
 *   <li>Valores primitivos (int, double) en lugar de wrappers</li>
 *   <li>Nombres descriptivos en mayúsculas con guiones bajos</li>
 *   <li>Documentación JavaDoc explícita para cada constante</li>
 *   <li>Agrupación lógica mediante comentarios de sección</li>
 * </ul>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * // Crear servidor en puerto configurado
 * ServidorJuego servidor = new ServidorJuego(Config.PUERTO_DEFAULT, gameManager);
 *
 * // Verificar si el jugador cayó al agua
 * if (jugador.getY() >= Config.NIVEL_AGUA) {
 *     jugador.perderVida();
 * }
 *
 * // Verificar victoria
 * if (jugador.getLianaId() == Config.OBJETIVO_LIANA &&
 *     jugador.getY() <= Config.OBJETIVO_Y) {
 *     celebrarVictoria();
 * }
 * }</pre>
 *
 * @author DonCEyKongJr Team
 * @version 1.0
 * @see GameManager
 * @see Jugador
 */
public class Config {

    // ==================== CONFIGURACIÓN DE RED ====================

    /**
     * Puerto TCP por defecto del servidor.
     *
     * <p>El servidor escucha conexiones de clientes en este puerto.
     * Puede ser sobrescrito pasando un argumento en la línea de comandos.</p>
     *
     * @see cr.tec.donceykongjr.server.Main#main(String[])
     * @see cr.tec.donceykongjr.server.network.ServidorJuego
     */
    public static final int PUERTO_DEFAULT = 5555;

    /**
     * Número máximo de jugadores activos simultáneamente.
     *
     * <p>Actualmente limitado a 1 jugador para simplificar la sincronización.
     * Los clientes adicionales pueden conectarse como espectadores.</p>
     *
     * @see #MAX_ESPECTADORES_POR_JUGADOR
     */
    public static final int MAX_JUGADORES = 1;

    /**
     * Número máximo de espectadores permitidos por jugador.
     *
     * <p>Los espectadores reciben actualizaciones del estado del juego
     * pero no pueden enviar inputs ni afectar la partida.</p>
     */
    public static final int MAX_ESPECTADORES_POR_JUGADOR = 1;

    // ==================== CONFIGURACIÓN DEL GAME LOOP ====================

    /**
     * Tasa de actualización del servidor en ticks por segundo (TPS).
     *
     * <p>El {@link cr.tec.donceykongjr.server.logic.GameLoop} actualiza
     * el estado del juego exactamente 20 veces por segundo, garantizando
     * física determinista y sincronización precisa con los clientes.</p>
     *
     * <p><b>Cálculo del intervalo:</b></p>
     * <pre>
     * intervalo = 1000ms / 20 TPS = 50ms por tick
     * </pre>
     *
     * @see #INTERVALO_TICK_MS
     */
    public static final int TICKS_POR_SEGUNDO = 20;

    /**
     * Intervalo entre ticks en milisegundos.
     *
     * <p>Calculado automáticamente como 1000 / {@link #TICKS_POR_SEGUNDO}.
     * Para 20 TPS: 1000 / 20 = 50 milisegundos.</p>
     *
     * @see cr.tec.donceykongjr.server.logic.GameLoop#iniciar()
     */
    public static final long INTERVALO_TICK_MS = 1000 / TICKS_POR_SEGUNDO;

    /**
     * Velocidad base de movimiento de entidades (multiplicador global).
     *
     * <p>Este valor actúa como multiplicador base para todas las velocidades.
     * Actualmente establecido en 1.0 (sin modificación).</p>
     */
    public static final double VELOCIDAD_BASE = 1.0;

    // ==================== CONFIGURACIÓN DEL JUGADOR ====================

    /**
     * Número de vidas iniciales del jugador al comenzar la partida.
     *
     * <p>Cada colisión con cocodrilos o caída al agua reduce una vida.
     * Cuando las vidas llegan a 0, el jugador es eliminado.</p>
     */
    public static final int VIDAS_INICIALES = 3;

    /**
     * Puntos base otorgados por recoger una fruta.
     *
     * <p>Las frutas individuales pueden tener valores diferentes,
     * pero este es el valor por defecto sugerido.</p>
     */
    public static final int PUNTOS_FRUTA_BASE = 50;

    // ==================== LÍMITES VERTICALES DEL MAPA ====================

    /**
     * Coordenada Y mínima permitida para el jugador.
     *
     * <p>Representa el límite superior del mapa (cielo).
     * Si el jugador supera este límite hacia arriba, cae al abismo.</p>
     */
    public static final double JUGADOR_Y_MIN = 0.0;

    /**
     * Coordenada Y máxima del mapa (suelo más bajo).
     *
     * <p>Este es el límite inferior absoluto del área jugable.
     * Más allá de {@link #NIVEL_AGUA} se considera caída mortal.</p>
     */
    public static final double JUGADOR_Y_MAX = 500.0;

    /**
     * Nivel del agua (umbral de muerte por caída).
     *
     * <p>Si el jugador cae por debajo de esta coordenada Y,
     * muere por ahogamiento y pierde una vida.</p>
     *
     * <p><b>Relación con el suelo:</b></p>
     * <pre>
     * NIVEL_AGUA = 485.0   (agua peligrosa)
     * ALTURA_SUELO = 475.0 (plataforma segura)
     * </pre>
     */
    public static final double NIVEL_AGUA = 485.0;

    /**
     * Altura de la plataforma de suelo inferior (coordenada Y).
     *
     * <p>Esta es la plataforma base donde el jugador spawn y puede caminar.
     * Está ubicada justo por encima del {@link #NIVEL_AGUA}.</p>
     *
     * @see #JUGADOR_SPAWN_Y
     */
    public static final double ALTURA_SUELO = 475.0;

    // ==================== FÍSICA HORIZONTAL DEL JUGADOR ====================

    /**
     * Velocidad horizontal máxima del jugador en unidades/segundo.
     *
     * <p>Esta velocidad fue balanceada para alta dificultad, proporcionando
     * control preciso pero requiriendo reflejos rápidos del jugador.</p>
     *
     * <p><b>Nota de balance:</b> Reducida de 1.5 a 1.2 para mejorar control.</p>
     */
    public static final double JUGADOR_VEL_HORIZONTAL_MAX = 1.2;

    /**
     * Aceleración horizontal en unidades/segundo².
     *
     * <p>Controla qué tan rápido el jugador alcanza la velocidad máxima.
     * Una aceleración de 8.0 permite alcanzar velocidad máxima en ~0.15 segundos.</p>
     *
     * <p><b>Balance:</b> Aceleración más suave para control preciso.</p>
     */
    public static final double JUGADOR_ACELERACION_HORIZONTAL = 8.0;

    /**
     * Fricción aplicada cuando no hay input horizontal (desaceleración).
     *
     * <p>Controla qué tan rápido el jugador se detiene al soltar las teclas.
     * Una fricción de 15.0 detiene al jugador en ~0.08 segundos.</p>
     *
     * <p><b>Balance:</b> Mayor fricción para mejor capacidad de frenado.</p>
     */
    public static final double JUGADOR_FRICCION = 15.0;

    /**
     * Multiplicador de control horizontal cuando el jugador está en el aire.
     *
     * <p>Reduce el control horizontal a 60% del normal durante saltos,
     * simulando física realista y aumentando dificultad.</p>
     *
     * <p><b>Uso:</b></p>
     * <pre>
     * aceleracionEnAire = JUGADOR_ACELERACION_HORIZONTAL * JUGADOR_CONTROL_AEREO_MULT
     *                   = 8.0 * 0.6 = 4.8 unidades/s²
     * </pre>
     */
    public static final double JUGADOR_CONTROL_AEREO_MULT = 0.6;

    // ==================== FÍSICA VERTICAL DEL JUGADOR ====================

    /**
     * Velocidad de subida/bajada en lianas en unidades/segundo.
     *
     * <p>Esta es la velocidad vertical cuando el jugador está agarrado a una liana
     * y presiona las teclas arriba/abajo.</p>
     */
    public static final double JUGADOR_VEL_LIANA = 180.0;

    /**
     * Velocidad vertical inicial del salto desde el suelo.
     *
     * <p>Valor negativo porque el eje Y aumenta hacia abajo.
     * Un salto de -220 permite alcanzar aproximadamente 120 unidades de altura.</p>
     *
     * <p><b>Balance:</b> Reducida de 260.0 para mejorar control en espacios cerrados.</p>
     */
    public static final double JUGADOR_VEL_SALTO = 220.0;

    /**
     * Velocidad vertical inicial del salto desde una liana.
     *
     * <p>Actualmente igual a {@link #JUGADOR_VEL_SALTO}.
     * Podría ser diferente para mecánicas avanzadas.</p>
     */
    public static final double JUGADOR_VEL_SALTO_LIANA = 220.0;

    /**
     * Aceleración gravitacional en unidades/segundo².
     *
     * <p>Valor positivo porque el eje Y aumenta hacia abajo.
     * La gravedad se aplica continuamente durante saltos y caídas:</p>
     * <pre>
     * vy += JUGADOR_GRAVEDAD * deltaTime
     * </pre>
     *
     * <p><b>Física:</b> Con gravedad 520 y salto inicial -220,
     * el jugador alcanza el pico del salto en ~0.42 segundos.</p>
     */
    public static final double JUGADOR_GRAVEDAD = 520.0;

    // ==================== MECÁNICAS DE AGARRE Y COLISIÓN ====================

    /**
     * Distancia máxima horizontal para agarrar una liana.
     *
     * <p>El jugador puede agarrar una liana si está dentro de 0.35 unidades
     * de la coordenada X de la liana.</p>
     *
     * <p><b>Uso:</b> Permite "perdón" en el input del jugador,
     * facilitando el agarre sin precisión pixel-perfect.</p>
     */
    public static final double JUGADOR_DISTANCIA_ENGANCHE = 0.35;

    /**
     * Duración del "buffer de agarre" en segundos.
     *
     * <p>Si el jugador presiona "agarrar" hasta 0.30 segundos antes
     * de estar cerca de una liana, el agarre se ejecuta automáticamente
     * cuando esté en rango.</p>
     *
     * <p><b>Mecánica de calidad de vida:</b> Mejora la experiencia
     * permitiendo input anticipado.</p>
     */
    public static final double JUGADOR_GRAB_BUFFER = 0.30;

    /**
     * Cooldown entre cambios de liana en segundos.
     *
     * <p>Después de cambiar de liana horizontalmente, el jugador debe
     * esperar 0.25 segundos antes de poder cambiar nuevamente.</p>
     *
     * <p><b>Prevención de bugs:</b> Evita cambios múltiples accidentales
     * por mantener presionada una tecla.</p>
     */
    public static final double JUGADOR_CAMBIO_LIANA_COOLDOWN = 0.25;

    /**
     * Diferencia vertical máxima para colisión con cocodrilos (radio de hitbox).
     *
     * <p>Si la distancia absoluta entre el jugador y un cocodrilo en la misma
     * liana es menor o igual a 30 unidades, se detecta colisión:</p>
     * <pre>
     * Math.abs(jugador.getY() - cocodrilo.getY()) <= JUGADOR_DELTA_Y_COCODRILO
     * </pre>
     */
    public static final double JUGADOR_DELTA_Y_COCODRILO = 30.0;

    /**
     * Diferencia vertical máxima para recoger frutas (radio de hitbox).
     *
     * <p>Similar a {@link #JUGADOR_DELTA_Y_COCODRILO} pero ligeramente más pequeño
     * para que las frutas requieran más precisión.</p>
     */
    public static final double JUGADOR_DELTA_Y_FRUTA = 24.0;

    /**
     * Duración de la animación de celebración al ganar, en segundos.
     *
     * <p>Después de alcanzar el objetivo, el jugador celebra durante 1.5 segundos
     * antes de que el mapa se reinicie para el siguiente nivel.</p>
     *
     * @see cr.tec.donceykongjr.server.logic.GameManager#iniciarCelebracion
     */
    public static final double JUGADOR_TIEMPO_CELEBRACION = 1.5;

    // ==================== PUNTO DE SPAWN ====================

    /**
     * Coordenada Y del spawn del jugador.
     *
     * <p>El jugador aparece en el suelo seguro ({@link #ALTURA_SUELO})
     * al inicio del juego y después de perder una vida.</p>
     */
    public static final double JUGADOR_SPAWN_Y = ALTURA_SUELO;

    /**
     * Índice de la liana de spawn del jugador.
     *
     * <p>El jugador aparece en la columna 1 (segunda liana desde la izquierda),
     * que es una liana completa de 0-500 unidades de altura.</p>
     */
    public static final int JUGADOR_SPAWN_LIANA = 1;

    /**
     * Coordenada X del spawn del jugador.
     *
     * <p>Por simplicidad, la coordenada X coincide con el índice de la liana.
     * Spawn en liana 1 = X=1.0</p>
     */
    public static final double JUGADOR_SPAWN_X = JUGADOR_SPAWN_LIANA;

    // ==================== OBJETIVO Y VICTORIA ====================

    /**
     * Coordenada Y del objetivo (plataforma de victoria).
     *
     * <p>El jugador debe alcanzar Y ≤ 60 en la liana de objetivo para ganar.
     * Representa la plataforma superior donde está Donkey Kong padre.</p>
     */
    public static final double OBJETIVO_Y = 60.0;

    /**
     * Índice de la liana de objetivo (plataforma de victoria).
     *
     * <p>Liana 7 (octava columna) es la liana de victoria final.
     * Esta liana solo existe en la parte superior (Y: 0-100).</p>
     *
     * <p><b>Ruta sugerida para ganar:</b></p>
     * <pre>
     * Spawn (liana 1) → Subir → Cruzar a liana 6 → Subir → Liana 7 → ¡VICTORIA!
     * </pre>
     */
    public static final int OBJETIVO_LIANA = 7;

    // ==================== ESCALADO DE DIFICULTAD ====================

    /**
     * Factor de incremento de dificultad tras cada rescate exitoso.
     *
     * <p>Cada vez que el jugador alcanza el objetivo, la velocidad de los
     * cocodrilos se multiplica por este factor:</p>
     * <pre>
     * nuevaVelocidad = velocidadActual * COCODRILO_INCREMENTO_DIFICULTAD
     * Ejemplo: Nivel 1 (x1.0) → Nivel 2 (x1.10) → Nivel 3 (x1.21) → ...
     * </pre>
     *
     * <p><b>Progresión de dificultad:</b></p>
     * <ul>
     *   <li>Rescate 1: Factor 1.0 (velocidad base)</li>
     *   <li>Rescate 2: Factor 1.10 (+10%)</li>
     *   <li>Rescate 3: Factor 1.21 (+21%)</li>
     *   <li>Rescate 5: Factor 1.46 (+46%)</li>
     *   <li>Rescate 10: Factor 2.59 (+159%)</li>
     * </ul>
     *
     * @see cr.tec.donceykongjr.server.logic.MotorCocodrilos#incrementarDificultad(double)
     */
    public static final double COCODRILO_INCREMENTO_DIFICULTAD = 1.10;

    /**
     * Constructor privado para prevenir instanciación.
     *
     * <p>Esta es una clase utilitaria que solo contiene constantes estáticas.
     * No debe ser instanciada.</p>
     *
     * @throws AssertionError Si se intenta instanciar mediante reflexión
     */
    private Config() {
        throw new AssertionError("No se debe instanciar la clase Config");
    }
}
