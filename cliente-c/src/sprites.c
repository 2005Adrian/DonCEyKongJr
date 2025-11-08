#include "sprites.h"
#include <stdio.h>

// Sprites globales
Sprite sprite_jr = {NULL, 0, 0};
Sprite sprite_donkey = {NULL, 0, 0};
Sprite sprite_cocodrilo_rojo = {NULL, 0, 0};
Sprite sprite_cocodrilo_azul = {NULL, 0, 0};
Sprite sprite_banana = {NULL, 0, 0};
Sprite sprite_corazon = {NULL, 0, 0};

/**
 * Carga un sprite desde un archivo BMP
 */
static int cargarSprite(const char* ruta, Sprite* sprite) {
    HBITMAP hBitmap = (HBITMAP)LoadImage(
        NULL,
        ruta,
        IMAGE_BITMAP,
        0, 0,
        LR_LOADFROMFILE
    );

    if (!hBitmap) {
        printf("Error: No se pudo cargar sprite: %s\n", ruta);
        return 0;
    }

    // Obtener dimensiones del sprite
    BITMAP bm;
    GetObject(hBitmap, sizeof(BITMAP), &bm);

    sprite->bitmap = hBitmap;
    sprite->width = bm.bmWidth;
    sprite->height = bm.bmHeight;

    printf("✓ Sprite cargado: %s (%dx%d)\n", ruta, bm.bmWidth, bm.bmHeight);
    return 1;
}

/**
 * Carga todos los sprites del juego
 */
int cargarSprites() {
    printf("Cargando sprites...\n");

    int ok = 1;

    // Intentar cargar cada sprite
    // Si falla, continuar con sprites dibujados
    cargarSprite("../sprites/jr.bmp", &sprite_jr);
    cargarSprite("../sprites/donkey.bmp", &sprite_donkey);
    cargarSprite("../sprites/cocodrilo_rojo.bmp", &sprite_cocodrilo_rojo);
    cargarSprite("../sprites/cocodrilo_azul.bmp", &sprite_cocodrilo_azul);
    cargarSprite("../sprites/banana.bmp", &sprite_banana);
    cargarSprite("../sprites/corazon.bmp", &sprite_corazon);

    printf("Sprites cargados (los que no se carguen usarán gráficos dibujados)\n");
    return 1;
}

/**
 * Libera la memoria de los sprites
 */
void liberarSprites() {
    if (sprite_jr.bitmap) DeleteObject(sprite_jr.bitmap);
    if (sprite_donkey.bitmap) DeleteObject(sprite_donkey.bitmap);
    if (sprite_cocodrilo_rojo.bitmap) DeleteObject(sprite_cocodrilo_rojo.bitmap);
    if (sprite_cocodrilo_azul.bitmap) DeleteObject(sprite_cocodrilo_azul.bitmap);
    if (sprite_banana.bitmap) DeleteObject(sprite_banana.bitmap);
    if (sprite_corazon.bitmap) DeleteObject(sprite_corazon.bitmap);
}

/**
 * Dibuja un sprite en su tamaño original
 */
void dibujarSprite(HDC hdc, Sprite* sprite, int x, int y) {
    if (!sprite || !sprite->bitmap) return;

    HDC hdcMem = CreateCompatibleDC(hdc);
    HBITMAP hbmOld = (HBITMAP)SelectObject(hdcMem, sprite->bitmap);

    // Usar transparencia (el color blanco será transparente)
    TransparentBlt(hdc, x, y, sprite->width, sprite->height,
                   hdcMem, 0, 0, sprite->width, sprite->height,
                   RGB(255, 255, 255)); // Blanco = transparente

    SelectObject(hdcMem, hbmOld);
    DeleteDC(hdcMem);
}

/**
 * Dibuja un sprite escalado a un tamaño específico
 */
void dibujarSpriteEscalado(HDC hdc, Sprite* sprite, int x, int y, int width, int height) {
    if (!sprite || !sprite->bitmap) return;

    HDC hdcMem = CreateCompatibleDC(hdc);
    HBITMAP hbmOld = (HBITMAP)SelectObject(hdcMem, sprite->bitmap);

    // Dibujar escalado con transparencia
    TransparentBlt(hdc, x, y, width, height,
                   hdcMem, 0, 0, sprite->width, sprite->height,
                   RGB(255, 255, 255)); // Blanco = transparente

    SelectObject(hdcMem, hbmOld);
    DeleteDC(hdcMem);
}