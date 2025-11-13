#ifndef RENDER_H
#define RENDER_H

// Evita que windows.h incluya winsock.h y conflicto con winsock2
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <winsock2.h>
#include <windows.h>
#include "structs.h"

// Funciones principales de renderizado
void DibujarEscenario(HDC hdc);
void DibujarHUDVisual(HDC hdc);
void DibujarPantallaTitulo(HDC hdc);
void DibujarPantallaGameOver(HDC hdc);
void DibujarPantallaVictoria(HDC hdc);
void DibujarEfectos(HDC hdc);

// Funciones de dibujo de elementos del escenario
void DibujarPlataformas(HDC hdc);
void DibujarLianas(HDC hdc);
void DibujarLianaSegmento(HDC hdc, int x, int yInicio, int yFin, COLORREF colorOscuro, COLORREF colorClaro);
void DibujarAgua(HDC hdc);
void DibujarDonkeyKong(HDC hdc);

// Funciones de dibujo de entidades
void DibujarJugadorMejorado(HDC hdc, Jugador* j);
void DibujarCocodriloMejorado(HDC hdc, Cocodrilo* c);
void DibujarFrutaMejorada(HDC hdc, Fruta* f);

// Funciones auxiliares de dibujo
void DibujarCorazon(HDC hdc, int x, int y, int size);

#endif // RENDER_H
