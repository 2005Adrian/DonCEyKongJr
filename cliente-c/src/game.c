#include "game.h"

// Definición de variables globales
HWND g_hwnd;
SOCKET g_sockCliente;
EstadoActual g_estadoActual;
char g_miPlayerId[32];
int g_juegoActivo = 1;
int g_conectado = 0;
EstadoPantalla g_estadoPantalla = ESTADO_TITULO;
int g_animacionFrame = 0;
int g_efectoGolpe = 0;
int g_efectoFruta = 0;

// Posiciones fijas del escenario
int g_lianasPosX[4] = {150, 300, 450, 600};
int g_plataformasPosY[4] = {150, 280, 410, 540};
int g_abismoY = 580;
int g_donkeyPosX = 400;
int g_donkeyPosY = 50;

// Funciones de utilidad
int gameToScreenX(int liana) {
    if (liana < 0 || liana >= 4) return -1;
    return g_lianasPosX[liana];
}

int gameToScreenY(double y) {
    // Normalizar Y del servidor (0-500) a pantalla
    double normalizado = y / 500.0;
    if (normalizado > 1.0) normalizado = 1.0;
    if (normalizado < 0.0) normalizado = 0.0;

    // Mapear a plataformas
    int rangoJuego = g_plataformasPosY[3] - g_plataformasPosY[0];
    return g_plataformasPosY[0] + (int)(normalizado * rangoJuego);
}
