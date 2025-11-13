#ifndef GAME_H
#define GAME_H

#include <winsock2.h>
#include "constants.h"
#include "structs.h"

// Dimensiones de ventana
#define WINDOW_WIDTH 900
#define WINDOW_HEIGHT 700
#define GAME_WIDTH 800
#define GAME_HEIGHT 600
#define OFFSET_X 50
#define OFFSET_Y 80

// Colores del juego
#define COLOR_BG RGB(20, 20, 40)
#define COLOR_PLATAFORMA RGB(139, 90, 43)
#define COLOR_LIANA_OSCURO RGB(101, 67, 33)
#define COLOR_LIANA_CLARO RGB(160, 110, 70)
#define COLOR_ABISMO RGB(10, 10, 20)
#define COLOR_JR_ROJO RGB(220, 50, 50)
#define COLOR_JR_PIEL RGB(255, 200, 150)
#define COLOR_DONKEY RGB(101, 67, 33)
#define COLOR_JAULA RGB(120, 120, 120)
#define COLOR_CROC_ROJO RGB(255, 80, 20)
#define COLOR_CROC_AZUL RGB(50, 150, 255)
#define COLOR_FRUTA RGB(255, 220, 50)
#define COLOR_HUD_BG RGB(30, 30, 60)
#define COLOR_TEXT RGB(255, 255, 255)
#define COLOR_VIDA RGB(255, 50, 50)

// Estados de la pantalla del juego
typedef enum {
    ESTADO_TITULO,
    ESTADO_JUGANDO,
    ESTADO_GAME_OVER,
    ESTADO_VICTORIA,
    ESTADO_DESCONECTADO
} EstadoPantalla;

// Variables globales del juego
extern HWND g_hwnd;
extern SOCKET g_sockCliente;
extern EstadoActual g_estadoActual;
extern char g_miPlayerId[32];
extern int g_juegoActivo;
extern int g_conectado;
extern EstadoPantalla g_estadoPantalla;
extern int g_animacionFrame;
extern int g_efectoGolpe;
extern int g_efectoFruta;
extern HANDLE g_threadRed;

// Posiciones fijas del escenario
extern int g_lianasPosX[MAX_LIANAS];
extern int g_plataformasPosY[4];
extern int g_abismoY;
extern int g_donkeyPosX;
extern int g_donkeyPosY;

// Funciones de utilidad
int gameToScreenX(int liana);
int gameToScreenY(double y);

#endif // GAME_H
