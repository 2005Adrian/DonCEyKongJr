#include "game.h"

// Definición de variables globales
HWND g_hwnd;
SOCKET g_sockCliente;
EstadoActual g_estadoActual = {0};
char g_miPlayerId[32];
int g_juegoActivo = 1;
int g_conectado = 0;
EstadoPantalla g_estadoPantalla = ESTADO_TITULO;
int g_animacionFrame = 0;
int g_efectoGolpe = 0;
int g_efectoFruta = 0;
HANDLE g_threadRed = NULL;

// Posiciones fijas del escenario (8 columnas/lianas)
int g_lianasPosX[MAX_LIANAS] = {100, 180, 270, 360, 450, 540, 630, 720};
int g_plataformasPosY[8] = {120, 200, 280, 360, 440, 520, 560, 100}; // Última es plataforma de victoria
int g_abismoY = 580;
int g_aguaY = 560; // Nivel del agua
int g_donkeyPosX = 720; // Extremo derecho (columna 8)
int g_donkeyPosY = 90;  // Plataforma superior

// Funciones de utilidad
int gameToScreenX(int liana) {
    if (liana < 0 || liana >= MAX_LIANAS) return -1;
    return g_lianasPosX[liana];
}

int gameToScreenY(double y) {
    // Normalizar Y del servidor (0-500) a pantalla
    // Y=0 es la parte superior (Donkey Kong), Y=500 es la parte inferior (cerca del agua)
    double normalizado = y / 500.0;
    if (normalizado > 1.0) normalizado = 1.0;
    if (normalizado < 0.0) normalizado = 0.0;

    // Mapear inversamente: Y=0 (servidor) -> plataforma superior, Y=500 -> plataforma inferior
    int yMin = g_plataformasPosY[7]; // Plataforma de victoria (parte superior)
    int yMax = g_aguaY;               // Nivel del agua (parte inferior)
    int rangoJuego = yMax - yMin;
    return yMin + (int)(normalizado * rangoJuego);
}
