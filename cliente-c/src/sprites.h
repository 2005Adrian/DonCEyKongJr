#ifndef SPRITES_H
#define SPRITES_H

#include <windows.h>

// Estructura para manejar sprites
typedef struct {
    HBITMAP bitmap;
    int width;
    int height;
} Sprite;

// Sprites del juego
extern Sprite sprite_jr;
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

#endif // SPRITES_H