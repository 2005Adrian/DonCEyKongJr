#include "render.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "game.h"
#include "structs.h"
#include "sprites.h"

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
extern int g_lianasPosX[MAX_LIANAS];
extern int g_plataformasPosY[8];
extern int g_abismoY;
extern int g_aguaY;
extern int g_donkeyPosX;
extern int g_donkeyPosY;
extern int g_conectado;

/**
 * Dibuja el escenario completo
 */
void DibujarEscenario(HDC hdc) {
    // Fondo negro
    HBRUSH hBrushBG = CreateSolidBrush(RGB(0, 0, 0));
    RECT fondoRect = {0, OFFSET_Y, WINDOW_WIDTH, WINDOW_HEIGHT};
    FillRect(hdc, &fondoRect, hBrushBG);
    DeleteObject(hBrushBG);

    DibujarAgua(hdc);
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

    if (g_estadoActual.celebrationPending) {
        char celebracion[64];
        double tiempo = g_estadoActual.celebrationTimer;
        if (tiempo < 0) tiempo = 0;
        sprintf(celebracion, "RESCATE EN %.1fs", tiempo);
        SetBkMode(hdc, TRANSPARENT);
        SetTextColor(hdc, RGB(255, 220, 120));
        HFONT hFont = CreateFont(28, 0, 0, 0, FW_BOLD, 0, 0, 0, DEFAULT_CHARSET,
                                 OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                                 DEFAULT_PITCH | FF_SWISS, "Arial");
        HFONT oldFont = SelectObject(hdc, hFont);
        RECT msgRect = {OFFSET_X, OFFSET_Y - 50, OFFSET_X + GAME_WIDTH, OFFSET_Y - 10};
        DrawText(hdc, celebracion, -1, &msgRect, DT_CENTER | DT_VCENTER | DT_SINGLELINE);
        SelectObject(hdc, oldFont);
        DeleteObject(hFont);
    }
}

/**
 * Dibuja agua en la parte inferior del mapa
 */
void DibujarAgua(HDC hdc) {
    // Franja de agua animada
    HBRUSH hBrushAgua = CreateSolidBrush(RGB(30, 100, 180));
    RECT agua = {0, g_aguaY, WINDOW_WIDTH, g_abismoY};
    FillRect(hdc, &agua, hBrushAgua);
    DeleteObject(hBrushAgua);

    // Ondas animadas
    HPEN hPenOnda = CreatePen(PS_SOLID, 2, RGB(60, 140, 220));
    HPEN hPenOld = SelectObject(hdc, hPenOnda);
    for (int x = 0; x < WINDOW_WIDTH; x += 40) {
        int offset = (g_animacionFrame + x / 10) % 20 - 10;
        MoveToEx(hdc, x, g_aguaY + 5 + offset, NULL);
        LineTo(hdc, x + 20, g_aguaY + 5 - offset);
    }
    SelectObject(hdc, hPenOld);
    DeleteObject(hPenOnda);
}

/**
 * Dibuja plataformas según el diseño del nivel de Donkey Kong Jr.
 * - Plataforma horizontal superior muy larga
 * - Espacio vacío y luego plataforma de Donkey Kong
 * - Islas de tierra sobre el agua
 */
void DibujarPlataformas(HDC hdc) {
    COLORREF colorTierra = RGB(139, 90, 43);
    COLORREF colorPasto = RGB(80, 150, 60);

    // 1. Plataforma horizontal superior muy larga (casi de lado a lado)
    HBRUSH hBrushPlat = CreateSolidBrush(colorTierra);
    RECT platSuperior = {OFFSET_X, g_plataformasPosY[7] + 20, g_donkeyPosX - 80, g_plataformasPosY[7] + 40};
    FillRect(hdc, &platSuperior, hBrushPlat);

    // Pasto sobre la plataforma superior
    HBRUSH hBrushPasto = CreateSolidBrush(colorPasto);
    RECT pastoSuperior = {OFFSET_X, g_plataformasPosY[7] + 20, g_donkeyPosX - 80, g_plataformasPosY[7] + 25};
    FillRect(hdc, &pastoSuperior, hBrushPasto);
    DeleteObject(hBrushPasto);
    DeleteObject(hBrushPlat);

    // 2. Plataforma de Donkey Kong (extremo derecho, después del hueco)
    hBrushPlat = CreateSolidBrush(colorTierra);
    RECT platDonkey = {g_donkeyPosX - 50, g_donkeyPosY + 50, g_donkeyPosX + 50, g_donkeyPosY + 70};
    FillRect(hdc, &platDonkey, hBrushPlat);

    hBrushPasto = CreateSolidBrush(colorPasto);
    RECT pastoDonkey = {g_donkeyPosX - 50, g_donkeyPosY + 50, g_donkeyPosX + 50, g_donkeyPosY + 55};
    FillRect(hdc, &pastoDonkey, hBrushPasto);
    DeleteObject(hBrushPasto);
    DeleteObject(hBrushPlat);

    // 3. Islas de tierra sobre el agua (plataformas flotantes donde el mono puede pararse)
    // Isla en columna 1 (debajo de liana 1)
    hBrushPlat = CreateSolidBrush(colorTierra);
    RECT isla1 = {g_lianasPosX[1] - 35, g_aguaY - 25, g_lianasPosX[1] + 35, g_aguaY - 5};
    FillRect(hdc, &isla1, hBrushPlat);
    hBrushPasto = CreateSolidBrush(colorPasto);
    RECT pasto1 = {g_lianasPosX[1] - 35, g_aguaY - 25, g_lianasPosX[1] + 35, g_aguaY - 20};
    FillRect(hdc, &pasto1, hBrushPasto);
    DeleteObject(hBrushPasto);

    // Isla en columna 3 (debajo de liana 3)
    RECT isla3 = {g_lianasPosX[3] - 35, g_aguaY - 25, g_lianasPosX[3] + 35, g_aguaY - 5};
    FillRect(hdc, &isla3, hBrushPlat);
    hBrushPasto = CreateSolidBrush(colorPasto);
    RECT pasto3 = {g_lianasPosX[3] - 35, g_aguaY - 25, g_lianasPosX[3] + 35, g_aguaY - 20};
    FillRect(hdc, &pasto3, hBrushPasto);
    DeleteObject(hBrushPasto);

    // Isla en columna 4 (debajo de liana 4)
    RECT isla4 = {g_lianasPosX[4] - 30, g_aguaY - 25, g_lianasPosX[4] + 30, g_aguaY - 5};
    FillRect(hdc, &isla4, hBrushPlat);
    hBrushPasto = CreateSolidBrush(colorPasto);
    RECT pasto4 = {g_lianasPosX[4] - 30, g_aguaY - 25, g_lianasPosX[4] + 30, g_aguaY - 20};
    FillRect(hdc, &pasto4, hBrushPasto);
    DeleteObject(hBrushPasto);

    DeleteObject(hBrushPlat);

    // 4. Plataforma flotante en columna 2 (a mitad de altura)
    hBrushPlat = CreateSolidBrush(colorTierra);
    int alturaMedia = (g_plataformasPosY[7] + g_aguaY) / 2;
    RECT platMedia2 = {g_lianasPosX[2] - 40, alturaMedia - 10, g_lianasPosX[2] + 40, alturaMedia + 5};
    FillRect(hdc, &platMedia2, hBrushPlat);
    hBrushPasto = CreateSolidBrush(colorPasto);
    RECT pastoMedia2 = {g_lianasPosX[2] - 40, alturaMedia - 10, g_lianasPosX[2] + 40, alturaMedia - 5};
    FillRect(hdc, &pastoMedia2, hBrushPasto);
    DeleteObject(hBrushPasto);

    // Plataforma flotante más abajo en columna 2
    int alturaBaja2 = alturaMedia + 100;
    RECT platBaja2 = {g_lianasPosX[2] - 40, alturaBaja2 - 10, g_lianasPosX[2] + 40, alturaBaja2 + 5};
    FillRect(hdc, &platBaja2, hBrushPlat);
    hBrushPasto = CreateSolidBrush(colorPasto);
    RECT pastoBaja2 = {g_lianasPosX[2] - 40, alturaBaja2 - 10, g_lianasPosX[2] + 40, alturaBaja2 - 5};
    FillRect(hdc, &pastoBaja2, hBrushPasto);
    DeleteObject(hBrushPasto);

    DeleteObject(hBrushPlat);
}

/**
 * Dibuja lianas según el diseño del nivel (8 columnas)
 * Cada columna tiene características específicas de altura y posición
 */
void DibujarLianas(HDC hdc) {
    COLORREF colorOscuro = RGB(101, 67, 33);
    COLORREF colorClaro = RGB(160, 110, 70);

    int platSuperior = g_plataformasPosY[7] + 40; // Debajo de la plataforma superior
    int nivelAgua = g_aguaY - 25; // Justo encima de las islas

    // Columna 0: Liana completa desde arriba hasta abajo
    DibujarLianaSegmento(hdc, g_lianasPosX[0], platSuperior, nivelAgua, colorOscuro, colorClaro);

    // Columna 1: Liana completa desde arriba hasta isla de tierra
    DibujarLianaSegmento(hdc, g_lianasPosX[1], platSuperior, nivelAgua, colorOscuro, colorClaro);

    // Columna 2: Lianas por secciones (plataforma media hacia abajo)
    int alturaMedia = (platSuperior + nivelAgua) / 2;
    int alturaBaja = alturaMedia + 100;
    DibujarLianaSegmento(hdc, g_lianasPosX[2], alturaMedia - 10, alturaBaja + 5, colorOscuro, colorClaro);
    DibujarLianaSegmento(hdc, g_lianasPosX[2], alturaBaja + 5, nivelAgua, colorOscuro, colorClaro);

    // Columna 3: Liana hasta mitad del mapa
    int mitadMapa = (platSuperior + nivelAgua) / 2;
    DibujarLianaSegmento(hdc, g_lianasPosX[3], platSuperior, mitadMapa, colorOscuro, colorClaro);

    // Columna 4: Liana corta en la parte inferior
    int inicioCorta = nivelAgua - 120;
    DibujarLianaSegmento(hdc, g_lianasPosX[4], inicioCorta, nivelAgua, colorOscuro, colorClaro);

    // Columna 5: Liana larga completa
    DibujarLianaSegmento(hdc, g_lianasPosX[5], platSuperior, nivelAgua, colorOscuro, colorClaro);

    // Columna 6: Liana normal/media
    DibujarLianaSegmento(hdc, g_lianasPosX[6], platSuperior + 60, nivelAgua, colorOscuro, colorClaro);

    // Columna 7: Liana de victoria - parte superior (sube hacia Donkey Kong)
    DibujarLianaSegmento(hdc, g_lianasPosX[7], g_donkeyPosY + 50, platSuperior, colorOscuro, colorClaro);
}

/**
 * Dibuja un segmento de liana trenzada entre dos puntos Y
 */
void DibujarLianaSegmento(HDC hdc, int x, int yInicio, int yFin, COLORREF colorOscuro, COLORREF colorClaro) {
    for (int y = yInicio; y < yFin; y += 2) {
        int offset = (int)(sin((y + g_animacionFrame * 0.1) * 0.1) * 2);

        HBRUSH hBrush1 = CreateSolidBrush(colorOscuro);
        HBRUSH hBrush2 = CreateSolidBrush(colorClaro);

        RECT r1 = {x - 5 + offset, y, x - 1 + offset, y + 2};
        RECT r2 = {x + 1 + offset, y, x + 5 + offset, y + 2};

        FillRect(hdc, &r1, (y / 4) % 2 == 0 ? hBrush1 : hBrush2);
        FillRect(hdc, &r2, (y / 4) % 2 == 0 ? hBrush2 : hBrush1);

        DeleteObject(hBrush1);
        DeleteObject(hBrush2);
    }
}


/**
 * Dibuja Donkey Kong en jaula
 */
void DibujarDonkeyKong(HDC hdc) {
    // Usar sprite si está disponible
    if (sprite_donkey.bitmap) {
        dibujarSpriteEscalado(hdc, &sprite_donkey, g_donkeyPosX - 30, g_donkeyPosY - 20, 60, 70);
    }

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

    // Si no hay sprite, dibujar forma básica
    if (!sprite_donkey.bitmap) {
        HBRUSH hBrushDonkey = CreateSolidBrush(COLOR_DONKEY);
        Ellipse(hdc, g_donkeyPosX - 30, g_donkeyPosY, g_donkeyPosX + 30, g_donkeyPosY + 45);
        Ellipse(hdc, g_donkeyPosX - 25, g_donkeyPosY - 20, g_donkeyPosX + 25, g_donkeyPosY + 10);
        HBRUSH hBrushEyes = CreateSolidBrush(RGB(255, 255, 255));
        SelectObject(hdc, hBrushEyes);
        Ellipse(hdc, g_donkeyPosX - 15, g_donkeyPosY - 10, g_donkeyPosX - 5, g_donkeyPosY);
        Ellipse(hdc, g_donkeyPosX + 5, g_donkeyPosY - 10, g_donkeyPosX + 15, g_donkeyPosY);
        DeleteObject(hBrushEyes);
        HBRUSH hBrushPupil = CreateSolidBrush(RGB(0, 0, 0));
        SelectObject(hdc, hBrushPupil);
        Ellipse(hdc, g_donkeyPosX - 12, g_donkeyPosY - 7, g_donkeyPosX - 8, g_donkeyPosY - 3);
        Ellipse(hdc, g_donkeyPosX + 8, g_donkeyPosY - 7, g_donkeyPosX + 12, g_donkeyPosY - 3);
        DeleteObject(hBrushPupil);
        DeleteObject(hBrushDonkey);
    }

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

    if (g_efectoGolpe > 0 && g_animacionFrame % 4 < 2) return;

    int enLiana = (j->lianaId >= 0);
    Sprite* spriteActual = obtenerSpriteJr(j->state, j->facing, j->vy, j->vx);

    if (spriteActual && spriteActual->bitmap) {
        dibujarSpriteEscalado(hdc, spriteActual, x - 20, y - 35, 40, 50);
        return;
    }

    int estaCelebrando = j->celebrating || (j->state[0] && strcmp(j->state, "CELEBRANDO") == 0);
    if (estaCelebrando) {
        HPEN penGlow = CreatePen(PS_DOT, 2, RGB(255, 215, 0));
        HPEN oldPen = SelectObject(hdc, penGlow);
        HBRUSH oldBrush = SelectObject(hdc, GetStockObject(HOLLOW_BRUSH));
        Ellipse(hdc, x - 28, y - 50, x + 28, y + 10);
        SelectObject(hdc, oldBrush);
        SelectObject(hdc, oldPen);
        DeleteObject(penGlow);
    }

    HBRUSH hBrushBody = CreateSolidBrush(COLOR_JR_ROJO);
    SelectObject(hdc, hBrushBody);
    RECT body = {x - 12, y - 20, x + 12, y + 5};
    RoundRect(hdc, body.left, body.top, body.right, body.bottom, 8, 8);
    DeleteObject(hBrushBody);

    HBRUSH hBrushHead = CreateSolidBrush(COLOR_JR_PIEL);
    SelectObject(hdc, hBrushHead);
    Ellipse(hdc, x - 10, y - 32, x + 10, y - 12);
    DeleteObject(hBrushHead);

    HBRUSH hBrushCap = CreateSolidBrush(COLOR_JR_ROJO);
    SelectObject(hdc, hBrushCap);
    Ellipse(hdc, x - 11, y - 34, x + 11, y - 20);
    DeleteObject(hBrushCap);

    int miraIzquierda = (j->facing[0] && strcmp(j->facing, "LEFT") == 0);
    HBRUSH hBrushEye = CreateSolidBrush(RGB(0, 0, 0));
    SelectObject(hdc, hBrushEye);
    if (miraIzquierda) {
        Ellipse(hdc, x - 8, y - 24, x - 4, y - 20);
        Ellipse(hdc, x - 2, y - 24, x + 2, y - 20);
    } else {
        Ellipse(hdc, x - 6, y - 24, x - 2, y - 20);
        Ellipse(hdc, x + 2, y - 24, x + 6, y - 20);
    }
    DeleteObject(hBrushEye);

    HBRUSH hBrushArm = CreateSolidBrush(COLOR_JR_PIEL);
    SelectObject(hdc, hBrushArm);
    int armOffset = (g_animacionFrame / 5) % 2 == 0 ? -3 : 3;
    if (enLiana) {
        Ellipse(hdc, x - 18, y - 18 + armOffset, x - 10, y - 10 + armOffset);
        Ellipse(hdc, x + 10, y - 18 - armOffset, x + 18, y - 10 - armOffset);
    } else if (miraIzquierda) {
        Ellipse(hdc, x - 20, y - 15 + armOffset, x - 12, y - 7 + armOffset);
        Ellipse(hdc, x + 8, y - 10 - armOffset, x + 16, y - 2 - armOffset);
    } else {
        Ellipse(hdc, x - 16, y - 10 + armOffset, x - 8, y - 2 + armOffset);
        Ellipse(hdc, x + 12, y - 15 - armOffset, x + 20, y - 7 - armOffset);
    }
    DeleteObject(hBrushArm);

    HBRUSH hBrushLeg = CreateSolidBrush(RGB(100, 80, 60));
    SelectObject(hdc, hBrushLeg);
    if (miraIzquierda) {
        Rectangle(hdc, x - 12, y + 5, x - 6, y + 17);
        Rectangle(hdc, x + 2, y + 5, x + 8, y + 17);
    } else {
        Rectangle(hdc, x - 10, y + 5, x - 4, y + 17);
        Rectangle(hdc, x + 4, y + 5, x + 10, y + 17);
    }
    DeleteObject(hBrushLeg);
}


/**
 * Dibuja cocodrilo mejorado
 */
void DibujarCocodriloMejorado(HDC hdc, Cocodrilo* c) {
    int x = gameToScreenX(c->liana);
    int y = gameToScreenY(c->y);

    if (x < 0 || y < 0) return;

    // Usar sprite si está disponible
    Sprite* spr = (strcmp(c->kind, "ROJO") == 0) ? &sprite_cocodrilo_rojo : &sprite_cocodrilo_azul;
    if (spr->bitmap) {
        dibujarSpriteEscalado(hdc, spr, x - 25, y - 20, 50, 40);
        return;
    }

    // Fallback con GDI
    COLORREF color = strcmp(c->kind, "ROJO") == 0 ? COLOR_CROC_ROJO : COLOR_CROC_AZUL;
    HBRUSH hBrush = CreateSolidBrush(color);
    SelectObject(hdc, hBrush);
    Ellipse(hdc, x - 22, y - 12, x + 22, y + 12);
    int bocaAbierta = (g_animacionFrame / 8) % 2;
    Ellipse(hdc, x + 15, y - 10, x + 30, y + 10);
    if (bocaAbierta) {
        HBRUSH hBrushMouth = CreateSolidBrush(RGB(255, 100, 100));
        SelectObject(hdc, hBrushMouth);
        Ellipse(hdc, x + 20, y - 3, x + 28, y + 8);
        DeleteObject(hBrushMouth);
    }
    HBRUSH hBrushEye = CreateSolidBrush(RGB(255, 255, 0));
    SelectObject(hdc, hBrushEye);
    Ellipse(hdc, x + 18, y - 8, x + 23, y - 3);
    DeleteObject(hBrushEye);
    HBRUSH hBrushPupil = CreateSolidBrush(RGB(0, 0, 0));
    SelectObject(hdc, hBrushPupil);
    Ellipse(hdc, x + 20, y - 7, x + 22, y - 5);
    DeleteObject(hBrushPupil);
    POINT cola[3] = {{x - 22, y}, {x - 32, y - 8}, {x - 32, y + 8}};
    Polygon(hdc, cola, 3);
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

    // Usar sprite si está disponible
    if (sprite_banana.bitmap) {
        dibujarSpriteEscalado(hdc, &sprite_banana, x - 10, y - 10, 20, 20);
    } else {
        // Fallback: banana geométrica
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
    }

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
    if (sprite_corazon.bitmap) {
        dibujarSpriteEscalado(hdc, &sprite_corazon, x - size/2, y - size/2, size, size);
        return;
    }
    HBRUSH hBrush = CreateSolidBrush(COLOR_VIDA);
    SelectObject(hdc, hBrush);
    Ellipse(hdc, x - size/2, y - size/2, x + size/4, y + size/2);
    Ellipse(hdc, x - size/4, y - size/2, x + size/2, y + size/2);
    POINT triangle[3] = {{x - size/2, y}, {x + size/2, y}, {x, y + size}};
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

    // Velocidad de cocodrilos y estado del jugador
    HFONT hFontSmall = CreateFont(20, 0, 0, 0, FW_NORMAL, 0, 0, 0, DEFAULT_CHARSET,
                                   OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                                   DEFAULT_PITCH | FF_SWISS, "Arial");
    SelectObject(hdc, hFontSmall);

    double factor = g_estadoActual.speedMultiplier > 0.0 ? g_estadoActual.speedMultiplier : 1.0;
    char velocidadText[32];
    sprintf(velocidadText, "CROCS x%.2f", factor);
    RECT levelRect = {WINDOW_WIDTH - 170, 10, WINDOW_WIDTH - 20, 35};
    DrawText(hdc, velocidadText, -1, &levelRect, DT_CENTER | DT_VCENTER | DT_SINGLELINE);

    const char* estadoJugador = "SIN_JUGADOR";
    if (g_estadoActual.numJugadores > 0 && g_estadoActual.jugadores[0].state[0]) {
        estadoJugador = g_estadoActual.jugadores[0].state;
    }
    char estadoText[48];
    sprintf(estadoText, "ESTADO %s", estadoJugador);
    RECT estadoRect = {WINDOW_WIDTH - 200, 30, WINDOW_WIDTH - 20, 60};
    DrawText(hdc, estadoText, -1, &estadoRect, DT_CENTER | DT_VCENTER | DT_SINGLELINE);

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
    sprintf(info, "P:%d C:%d F:%d | Score:%d | Vidas:%d | Estado:%s | Crocs:%.2f | Tick:%ld | Celebracion:%s(%.1fs)",
            g_estadoActual.numJugadores, g_estadoActual.numCocodrilos, g_estadoActual.numFrutas,
            g_estadoActual.numJugadores > 0 ? g_estadoActual.jugadores[0].score : 0,
            g_estadoActual.numJugadores > 0 ? g_estadoActual.jugadores[0].lives : 0,
            estadoJugador,
            factor,
            g_estadoActual.tick,
            g_estadoActual.celebrationPending ? "SI" : "NO",
            g_estadoActual.celebrationTimer);

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
