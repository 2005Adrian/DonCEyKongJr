#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <windows.h>
#include <math.h>
#include "render.h"
#include "game.h"
#include "structs.h"

// Dimensiones mejoradas
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

// ==================== VARIABLES GLOBALES ====================
// Estas variables deben estar declaradas en game.c como extern en game.h

extern EstadoActual g_estadoActual;
extern int g_animacionFrame;
extern int g_efectoGolpe;
extern int g_efectoFruta;
extern int g_lianasPosX[4];
extern int g_plataformasPosY[4];
extern int g_abismoY;
extern int g_donkeyPosX;
extern int g_donkeyPosY;
extern int g_conectado;

/**
 * Dibuja el escenario completo
 */
void DibujarEscenario(HDC hdc) {
    DibujarAbismo(hdc);
    DibujarPlataformas(hdc);
    DibujarLianas(hdc);
    DibujarDonkeyKong(hdc);

    // Frutas
    for (int i = 0; i < g_estadoActual.numFrutas; i++) {
        DibujarFrutaMejorada(hdc, &g_estadoActual.frutas[i]);
    }

    // Cocodrilos
    for (int i = 0; i < g_estadoActual.numCocodrilos; i++) {
        DibujarCocodriloMejorado(hdc, &g_estadoActual.cocodrilos[i]);
    }

    // Jugadores
    for (int i = 0; i < g_estadoActual.numJugadores; i++) {
        if (g_estadoActual.jugadores[i].active) {
            DibujarJugadorMejorado(hdc, &g_estadoActual.jugadores[i]);
        }
    }
}

/**
 * Dibuja plataformas horizontales
 */
void DibujarPlataformas(HDC hdc) {
    for (int i = 0; i < 4; i++) {
        // Sombra
        HBRUSH hBrushShadow = CreateSolidBrush(RGB(60, 40, 20));
        RECT shadow = {OFFSET_X - 20, g_plataformasPosY[i] + 3, OFFSET_X + GAME_WIDTH + 20, g_plataformasPosY[i] + 28};
        FillRect(hdc, &shadow, hBrushShadow);
        DeleteObject(hBrushShadow);

        // Plataforma principal
        HBRUSH hBrush = CreateSolidBrush(COLOR_PLATAFORMA);
        RECT plat = {OFFSET_X - 20, g_plataformasPosY[i], OFFSET_X + GAME_WIDTH + 20, g_plataformasPosY[i] + 25};
        FillRect(hdc, &plat, hBrush);
        DeleteObject(hBrush);

        // Detalles de textura (tablones)
        HPEN hPen = CreatePen(PS_SOLID, 2, RGB(90, 60, 30));
        HPEN hPenOld = SelectObject(hdc, hPen);
        for (int x = OFFSET_X; x < OFFSET_X + GAME_WIDTH; x += 60) {
            MoveToEx(hdc, x, g_plataformasPosY[i], NULL);
            LineTo(hdc, x, g_plataformasPosY[i] + 25);
        }
        SelectObject(hdc, hPenOld);
        DeleteObject(hPen);
    }
}

/**
 * Dibuja lianas verticales
 */
void DibujarLianas(HDC hdc) {
    for (int i = 0; i < 4; i++) {
        int x = g_lianasPosX[i];

        // Dibujar liana trenzada
        for (int y = g_plataformasPosY[0]; y < g_plataformasPosY[3]; y += 2) {
            int offset = (int)(sin((y + g_animacionFrame * 0.1) * 0.1) * 2);

            HBRUSH hBrush1 = CreateSolidBrush(COLOR_LIANA_OSCURO);
            HBRUSH hBrush2 = CreateSolidBrush(COLOR_LIANA_CLARO);

            RECT r1 = {x - 5 + offset, y, x - 1 + offset, y + 2};
            RECT r2 = {x + 1 + offset, y, x + 5 + offset, y + 2};

            FillRect(hdc, &r1, (y / 4) % 2 == 0 ? hBrush1 : hBrush2);
            FillRect(hdc, &r2, (y / 4) % 2 == 0 ? hBrush2 : hBrush1);

            DeleteObject(hBrush1);
            DeleteObject(hBrush2);
        }
    }
}

/**
 * Dibuja zona de abismo
 */
void DibujarAbismo(HDC hdc) {
    HBRUSH hBrush = CreateSolidBrush(COLOR_ABISMO);
    RECT abismo = {0, g_abismoY, WINDOW_WIDTH, WINDOW_HEIGHT};
    FillRect(hdc, &abismo, hBrush);
    DeleteObject(hBrush);

    // Efecto de peligro (líneas rojas parpadeantes)
    if (g_animacionFrame % 20 < 10) {
        HPEN hPen = CreatePen(PS_SOLID, 3, RGB(255, 50, 50));
        HPEN hPenOld = SelectObject(hdc, hPen);
        MoveToEx(hdc, 0, g_abismoY, NULL);
        LineTo(hdc, WINDOW_WIDTH, g_abismoY);
        SelectObject(hdc, hPenOld);
        DeleteObject(hPen);
    }
}

/**
 * Dibuja Donkey Kong en jaula
 */
void DibujarDonkeyKong(HDC hdc) {
    // Jaula (barras verticales)
    HPEN hPenJaula = CreatePen(PS_SOLID, 4, COLOR_JAULA);
    HPEN hPenOld = SelectObject(hdc, hPenJaula);
    for (int i = 0; i < 5; i++) {
        int x = g_donkeyPosX - 40 + i * 20;
        MoveToEx(hdc, x, g_donkeyPosY - 10, NULL);
        LineTo(hdc, x, g_donkeyPosY + 50);
    }
    SelectObject(hdc, hPenOld);
    DeleteObject(hPenJaula);

    // Donkey Kong (gorila grande)
    HBRUSH hBrushDonkey = CreateSolidBrush(COLOR_DONKEY);

    // Cuerpo
    Ellipse(hdc, g_donkeyPosX - 30, g_donkeyPosY, g_donkeyPosX + 30, g_donkeyPosY + 45);

    // Cabeza
    Ellipse(hdc, g_donkeyPosX - 25, g_donkeyPosY - 20, g_donkeyPosX + 25, g_donkeyPosY + 10);

    // Ojos (tristes)
    HBRUSH hBrushEyes = CreateSolidBrush(RGB(255, 255, 255));
    SelectObject(hdc, hBrushEyes);
    Ellipse(hdc, g_donkeyPosX - 15, g_donkeyPosY - 10, g_donkeyPosX - 5, g_donkeyPosY);
    Ellipse(hdc, g_donkeyPosX + 5, g_donkeyPosY - 10, g_donkeyPosX + 15, g_donkeyPosY);
    DeleteObject(hBrushEyes);

    // Pupilas
    HBRUSH hBrushPupil = CreateSolidBrush(RGB(0, 0, 0));
    SelectObject(hdc, hBrushPupil);
    Ellipse(hdc, g_donkeyPosX - 12, g_donkeyPosY - 7, g_donkeyPosX - 8, g_donkeyPosY - 3);
    Ellipse(hdc, g_donkeyPosX + 8, g_donkeyPosY - 7, g_donkeyPosX + 12, g_donkeyPosY - 3);
    DeleteObject(hBrushPupil);

    DeleteObject(hBrushDonkey);

    // Texto "HELP!"
    SetBkMode(hdc, TRANSPARENT);
    SetTextColor(hdc, RGB(255, 255, 255));
    RECT textRect = {g_donkeyPosX - 50, g_donkeyPosY + 55, g_donkeyPosX + 50, g_donkeyPosY + 75};
    DrawText(hdc, "HELP!", -1, &textRect, DT_CENTER);
}

/**
 * Dibuja Jr mejorado
 */
void DibujarJugadorMejorado(HDC hdc, Jugador* j) {
    int x = gameToScreenX(j->liana);
    int y = gameToScreenY(j->y);

    if (x < 0 || y < 0) return;

    // Efecto de golpe (parpadeo)
    if (g_efectoGolpe > 0 && g_animacionFrame % 4 < 2) return;

    // Cuerpo (camisa roja)
    HBRUSH hBrushBody = CreateSolidBrush(COLOR_JR_ROJO);
    SelectObject(hdc, hBrushBody);
    RECT body = {x - 12, y - 20, x + 12, y + 5};
    RoundRect(hdc, body.left, body.top, body.right, body.bottom, 8, 8);
    DeleteObject(hBrushBody);

    // Cabeza
    HBRUSH hBrushHead = CreateSolidBrush(COLOR_JR_PIEL);
    SelectObject(hdc, hBrushHead);
    Ellipse(hdc, x - 10, y - 32, x + 10, y - 12);
    DeleteObject(hBrushHead);

    // Gorra (parte superior de la cabeza)
    HBRUSH hBrushCap = CreateSolidBrush(COLOR_JR_ROJO);
    SelectObject(hdc, hBrushCap);
    Ellipse(hdc, x - 11, y - 34, x + 11, y - 20);
    DeleteObject(hBrushCap);

    // Ojos
    HBRUSH hBrushEye = CreateSolidBrush(RGB(0, 0, 0));
    SelectObject(hdc, hBrushEye);
    Ellipse(hdc, x - 6, y - 24, x - 2, y - 20);
    Ellipse(hdc, x + 2, y - 24, x + 6, y - 20);
    DeleteObject(hBrushEye);

    // Brazos (en posición de trepar)
    HBRUSH hBrushArm = CreateSolidBrush(COLOR_JR_PIEL);
    SelectObject(hdc, hBrushArm);
    int armOffset = (g_animacionFrame / 5) % 2 == 0 ? -3 : 3;
    Ellipse(hdc, x - 18, y - 18 + armOffset, x - 10, y - 10 + armOffset);
    Ellipse(hdc, x + 10, y - 18 - armOffset, x + 18, y - 10 - armOffset);
    DeleteObject(hBrushArm);

    // Piernas
    HBRUSH hBrushLeg = CreateSolidBrush(RGB(100, 80, 60));
    SelectObject(hdc, hBrushLeg);
    Rectangle(hdc, x - 10, y + 5, x - 5, y + 15);
    Rectangle(hdc, x + 5, y + 5, x + 10, y + 15);
    DeleteObject(hBrushLeg);
}

/**
 * Dibuja cocodrilo mejorado
 */
void DibujarCocodriloMejorado(HDC hdc, Cocodrilo* c) {
    int x = gameToScreenX(c->liana);
    int y = gameToScreenY(c->y);

    if (x < 0 || y < 0) return;

    COLORREF color = strcmp(c->kind, "ROJO") == 0 ? COLOR_CROC_ROJO : COLOR_CROC_AZUL;
    HBRUSH hBrush = CreateSolidBrush(color);
    SelectObject(hdc, hBrush);

    // Cuerpo principal
    Ellipse(hdc, x - 22, y - 12, x + 22, y + 12);

    // Cabeza con boca
    int bocaAbierta = (g_animacionFrame / 8) % 2;
    Ellipse(hdc, x + 15, y - 10, x + 30, y + 10);

    // Boca
    if (bocaAbierta) {
        HBRUSH hBrushMouth = CreateSolidBrush(RGB(255, 100, 100));
        SelectObject(hdc, hBrushMouth);
        Ellipse(hdc, x + 20, y - 3, x + 28, y + 8);
        DeleteObject(hBrushMouth);
    }

    // Ojos
    HBRUSH hBrushEye = CreateSolidBrush(RGB(255, 255, 0));
    SelectObject(hdc, hBrushEye);
    Ellipse(hdc, x + 18, y - 8, x + 23, y - 3);
    DeleteObject(hBrushEye);

    // Pupila
    HBRUSH hBrushPupil = CreateSolidBrush(RGB(0, 0, 0));
    SelectObject(hdc, hBrushPupil);
    Ellipse(hdc, x + 20, y - 7, x + 22, y - 5);
    DeleteObject(hBrushPupil);

    // Cola
    POINT cola[3] = {{x - 22, y}, {x - 32, y - 8}, {x - 32, y + 8}};
    Polygon(hdc, cola, 3);

    // Patas (pequeñas)
    Rectangle(hdc, x - 15, y + 10, x - 10, y + 18);
    Rectangle(hdc, x + 5, y + 10, x + 10, y + 18);

    DeleteObject(hBrush);
}

/**
 * Dibuja fruta mejorada
 */
void DibujarFrutaMejorada(HDC hdc, Fruta* f) {
    int x = gameToScreenX(f->liana);
    int y = gameToScreenY(f->y);

    if (x < 0 || y < 0) return;

    // Efecto brillo cuando está cerca
    if (g_efectoFruta > 0) {
        int size = 15 + (g_efectoFruta / 2);
        HBRUSH hBrushGlow = CreateSolidBrush(RGB(255, 255, 200));
        SelectObject(hdc, hBrushGlow);
        Ellipse(hdc, x - size, y - size, x + size, y + size);
        DeleteObject(hBrushGlow);
    }

    // Banana (forma curvada)
    HBRUSH hBrushFruit = CreateSolidBrush(COLOR_FRUTA);
    SelectObject(hdc, hBrushFruit);

    // Cuerpo de banana (varios círculos)
    for (int i = 0; i < 5; i++) {
        int offsetX = (int)(i * 3 - 6);
        int offsetY = (int)(sin(i * 0.5) * 4);
        Ellipse(hdc, x + offsetX - 5, y + offsetY - 6, x + offsetX + 5, y + offsetY + 6);
    }

    DeleteObject(hBrushFruit);

    // Puntos
    char puntosText[16];
    sprintf(puntosText, "+%d", f->points);
    SetBkMode(hdc, TRANSPARENT);
    SetTextColor(hdc, RGB(255, 255, 0));
    HFONT hFont = CreateFont(12, 0, 0, 0, FW_BOLD, 0, 0, 0, DEFAULT_CHARSET,
                              OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                              DEFAULT_PITCH | FF_SWISS, "Arial");
    HFONT hOldFont = SelectObject(hdc, hFont);
    TextOut(hdc, x + 15, y - 5, puntosText, strlen(puntosText));
    SelectObject(hdc, hOldFont);
    DeleteObject(hFont);
}

/**
 * Dibuja corazón
 */
void DibujarCorazon(HDC hdc, int x, int y, int size) {
    HBRUSH hBrush = CreateSolidBrush(COLOR_VIDA);
    SelectObject(hdc, hBrush);

    // Corazón simple con círculos y triángulo
    Ellipse(hdc, x - size/2, y - size/2, x + size/4, y + size/2);
    Ellipse(hdc, x - size/4, y - size/2, x + size/2, y + size/2);

    POINT triangle[3] = {
        {x - size/2, y},
        {x + size/2, y},
        {x, y + size}
    };
    Polygon(hdc, triangle, 3);

    DeleteObject(hBrush);
}

/**
 * HUD visual completo con debug info de entidades
 */
void DibujarHUDVisual(HDC hdc) {
    // Fondo del HUD
    HBRUSH hBrushHUD = CreateSolidBrush(COLOR_HUD_BG);
    RECT hudRect = {0, 0, WINDOW_WIDTH, 60};
    FillRect(hdc, &hudRect, hBrushHUD);
    DeleteObject(hBrushHUD);

    // Línea separadora
    HPEN hPen = CreatePen(PS_SOLID, 2, RGB(100, 100, 150));
    HPEN hPenOld = SelectObject(hdc, hPen);
    MoveToEx(hdc, 0, 60, NULL);
    LineTo(hdc, WINDOW_WIDTH, 60);
    SelectObject(hdc, hPenOld);
    DeleteObject(hPen);

    SetBkMode(hdc, TRANSPARENT);
    SetTextColor(hdc, COLOR_TEXT);

    // Vidas (corazones)
    for (int i = 0; i < 3; i++) {
        if (g_estadoActual.numJugadores > 0 && i < g_estadoActual.jugadores[0].lives) {
            DibujarCorazon(hdc, 50 + i * 35, 30, 20);
        } else {
            // Corazón vacío
            HPEN hPenGray = CreatePen(PS_SOLID, 2, RGB(80, 80, 80));
            HPEN hPenOld2 = SelectObject(hdc, hPenGray);
            HBRUSH hBrushOld = SelectObject(hdc, GetStockObject(NULL_BRUSH));
            Ellipse(hdc, 40 + i * 35, 20, 55 + i * 35, 35);
            SelectObject(hdc, hBrushOld);
            SelectObject(hdc, hPenOld2);
            DeleteObject(hPenGray);
        }
    }

    // Puntos (grande)
    HFONT hFontBig = CreateFont(36, 0, 0, 0, FW_BOLD, 0, 0, 0, DEFAULT_CHARSET,
                                 OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                                 DEFAULT_PITCH | FF_SWISS, "Arial");
    HFONT hOldFont = SelectObject(hdc, hFontBig);

    char scoreText[32];
    if (g_estadoActual.numJugadores > 0) {
        sprintf(scoreText, "%06d", g_estadoActual.jugadores[0].score);
    } else {
        sprintf(scoreText, "000000");
    }

    RECT scoreRect = {WINDOW_WIDTH / 2 - 100, 10, WINDOW_WIDTH / 2 + 100, 50};
    DrawText(hdc, scoreText, -1, &scoreRect, DT_CENTER | DT_VCENTER | DT_SINGLELINE);

    SelectObject(hdc, hOldFont);
    DeleteObject(hFontBig);

    // Nivel
    HFONT hFontSmall = CreateFont(20, 0, 0, 0, FW_NORMAL, 0, 0, 0, DEFAULT_CHARSET,
                                   OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                                   DEFAULT_PITCH | FF_SWISS, "Arial");
    SelectObject(hdc, hFontSmall);

    char levelText[32];
    sprintf(levelText, "NIVEL %d", g_estadoActual.level);
    RECT levelRect = {WINDOW_WIDTH - 150, 15, WINDOW_WIDTH - 20, 45};
    DrawText(hdc, levelText, -1, &levelRect, DT_CENTER | DT_VCENTER | DT_SINGLELINE);

    // Indicador de conexión
    if (g_conectado) {
        HBRUSH hBrushConn = CreateSolidBrush(RGB(50, 255, 50));
        Ellipse(hdc, WINDOW_WIDTH - 30, 25, WINDOW_WIDTH - 10, 45);
        DeleteObject(hBrushConn);
    } else {
        HBRUSH hBrushDisc = CreateSolidBrush(RGB(255, 50, 50));
        Ellipse(hdc, WINDOW_WIDTH - 30, 25, WINDOW_WIDTH - 10, 45);
        DeleteObject(hBrushDisc);
    }

    SelectObject(hdc, hOldFont);
    DeleteObject(hFontSmall);

    // Debug info - Mostrando cantidad de entidades y tick
    char info[128];
    sprintf(info, "P:%d C:%d F:%d | Nivel:%d | Score:%d | Vidas:%d | Tick:%ld",
            g_estadoActual.numJugadores, g_estadoActual.numCocodrilos, g_estadoActual.numFrutas,
            g_estadoActual.level,
            g_estadoActual.numJugadores > 0 ? g_estadoActual.jugadores[0].score : 0,
            g_estadoActual.numJugadores > 0 ? g_estadoActual.jugadores[0].lives : 0,
            g_estadoActual.tick);

    RECT debugRect = {10, WINDOW_HEIGHT - 25, 500, WINDOW_HEIGHT - 5};
    HFONT hFontDebug = CreateFont(14, 0, 0, 0, FW_NORMAL, 0, 0, 0, DEFAULT_CHARSET,
                                   OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                                   DEFAULT_PITCH | FF_SWISS, "Courier New");
    SelectObject(hdc, hFontDebug);
    SetTextColor(hdc, RGB(150, 150, 150));
    DrawText(hdc, info, -1, &debugRect, DT_LEFT);
    SelectObject(hdc, hOldFont);
    DeleteObject(hFontDebug);
}

/**
 * Pantalla de título
 */
void DibujarPantallaTitulo(HDC hdc) {
    SetBkMode(hdc, TRANSPARENT);

    // Título grande
    HFONT hFontTitle = CreateFont(72, 0, 0, 0, FW_BOLD, 0, 0, 0, DEFAULT_CHARSET,
                                   OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                                   DEFAULT_PITCH | FF_SWISS, "Arial");
    SelectObject(hdc, hFontTitle);

    SetTextColor(hdc, RGB(255, 200, 50));
    RECT titleRect = {0, 150, WINDOW_WIDTH, 250};
    DrawText(hdc, "DonCEy Kong Jr", -1, &titleRect, DT_CENTER | DT_VCENTER | DT_SINGLELINE);

    DeleteObject(hFontTitle);

    // Subtítulo
    HFONT hFontSub = CreateFont(24, 0, 0, 0, FW_NORMAL, 0, 0, 0, DEFAULT_CHARSET,
                                 OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                                 DEFAULT_PITCH | FF_SWISS, "Arial");
    SelectObject(hdc, hFontSub);

    SetTextColor(hdc, RGB(255, 255, 255));
    RECT subRect = {0, 280, WINDOW_WIDTH, 320};
    DrawText(hdc, "Presiona cualquier tecla para comenzar", -1, &subRect, DT_CENTER);

    DeleteObject(hFontSub);

    // Controles
    HFONT hFontControls = CreateFont(18, 0, 0, 0, FW_NORMAL, 0, 0, 0, DEFAULT_CHARSET,
                                      OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                                      DEFAULT_PITCH | FF_SWISS, "Arial");
    SelectObject(hdc, hFontControls);

    SetTextColor(hdc, RGB(200, 200, 200));
    char* controles[] = {
        "W/S - Subir/Bajar",
        "A/D - Izquierda/Derecha",
        "ESPACIO - Saltar",
        "E - Agarrar liana",
        "ESC - Salir"
    };

    int startY = 400;
    for (int i = 0; i < 5; i++) {
        RECT controlRect = {0, startY + i * 25, WINDOW_WIDTH, startY + (i + 1) * 25};
        DrawText(hdc, controles[i], -1, &controlRect, DT_CENTER);
    }

    DeleteObject(hFontControls);
}

/**
 * Pantalla Game Over
 */
void DibujarPantallaGameOver(HDC hdc) {
    // Overlay oscuro
    HBRUSH hBrushOverlay = CreateSolidBrush(RGB(0, 0, 0));
    RECT overlay = {0, 0, WINDOW_WIDTH, WINDOW_HEIGHT};
    FillRect(hdc, &overlay, hBrushOverlay);
    DeleteObject(hBrushOverlay);

    SetBkMode(hdc, TRANSPARENT);
    SetTextColor(hdc, RGB(255, 50, 50));

    HFONT hFont = CreateFont(64, 0, 0, 0, FW_BOLD, 0, 0, 0, DEFAULT_CHARSET,
                              OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                              DEFAULT_PITCH | FF_SWISS, "Arial");
    SelectObject(hdc, hFont);

    RECT textRect = {0, WINDOW_HEIGHT / 2 - 50, WINDOW_WIDTH, WINDOW_HEIGHT / 2 + 50};
    DrawText(hdc, "GAME OVER", -1, &textRect, DT_CENTER | DT_VCENTER | DT_SINGLELINE);

    DeleteObject(hFont);
}

/**
 * Pantalla Victoria
 */
void DibujarPantallaVictoria(HDC hdc) {
    SetBkMode(hdc, TRANSPARENT);
    SetTextColor(hdc, RGB(255, 215, 0));

    HFONT hFont = CreateFont(64, 0, 0, 0, FW_BOLD, 0, 0, 0, DEFAULT_CHARSET,
                              OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                              DEFAULT_PITCH | FF_SWISS, "Arial");
    SelectObject(hdc, hFont);

    RECT textRect = {0, WINDOW_HEIGHT / 2 - 50, WINDOW_WIDTH, WINDOW_HEIGHT / 2 + 50};
    DrawText(hdc, "¡VICTORIA!", -1, &textRect, DT_CENTER | DT_VCENTER | DT_SINGLELINE);

    DeleteObject(hFont);
}

/**
 * Efectos visuales
 */
void DibujarEfectos(HDC hdc) {
    // Efecto de golpe (pantalla roja)
    if (g_efectoGolpe > 0) {
        HBRUSH hBrush = CreateSolidBrush(RGB(255, 0, 0));
        SetROP2(hdc, R2_MERGEPEN);
        RECT screen = {0, 0, WINDOW_WIDTH, WINDOW_HEIGHT};
        FrameRect(hdc, &screen, hBrush);
        DeleteObject(hBrush);
    }
}
