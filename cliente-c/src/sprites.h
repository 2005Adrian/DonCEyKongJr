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

// Animación multi-frame (para caminar)
typedef struct {
    Sprite frames[3];    // 3 frames de animación
    int frameCount;      // Número de frames válidos (puede ser 1-3)
} SpriteAnimation;

// Animaciones de Jr (diferentes sprites según acción)
typedef struct {
    Sprite frente;                    // Parado de frente
    Sprite subiendo;                  // Subiendo por liana
    Sprite bajando;                   // Bajando por liana
    SpriteAnimation caminandoIzq;     // Animación caminando izquierda (3 frames)
    SpriteAnimation caminandoDer;     // Animación caminando derecha (3 frames)
    Sprite saltando;                  // Saltando/colgado
    // Compatibilidad con código anterior
    Sprite izquierda;                 // Fallback: primer frame de caminandoIzq
    Sprite derecha;                   // Fallback: primer frame de caminandoDer
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
Sprite* obtenerSpriteJr(const char* estado, const char* facing, double velocidadY, double velocidadX);

// Funciones de animación
void actualizarAnimaciones(double deltaTime);
Sprite* obtenerFrameAnimacion(SpriteAnimation* anim, int frameIndex);

#endif // SPRITES_H
