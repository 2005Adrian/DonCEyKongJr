#ifndef SPRITES_H
#define SPRITES_H

// Asegura el orden correcto de includes en Windows
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <winsock2.h>
#include <windows.h>

// Estructura para manejar sprites
typedef struct {
    HBITMAP bitmap;
    int width;
    int height;
} Sprite;

// Animaciones de Jr (diferentes sprites según acción)
typedef struct {
    Sprite frente;       // Parado de frente
    Sprite subiendo;     // Subiendo por liana
    Sprite bajando;      // Bajando por liana
    Sprite izquierda;    // Moviéndose a la izquierda
    Sprite derecha;      // Moviéndose a la derecha
    Sprite saltando;     // Saltando/colgado
} SpritesJr;

// Sprites del juego
extern SpritesJr sprites_jr;
extern Sprite sprite_donkey;
extern Sprite sprite_cocodrilo_rojo;
extern Sprite sprite_cocodrilo_azul;
extern Sprite sprite_banana;
extern Sprite sprite_corazon;

// Funciones de sprites
int cargarSprites();
void liberarSprites();
void dibujarSprite(HDC hdc, Sprite* sprite, int x, int y);
void dibujarSpriteEscalado(HDC hdc, Sprite* sprite, int x, int y, int width, int height);

// Función para obtener el sprite correcto de Jr según su estado
Sprite* obtenerSpriteJr(double velocidadY, double velocidadX, int enLiana);

#endif // SPRITES_H
