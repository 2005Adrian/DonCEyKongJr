#include "sprites.h"
#include <stdio.h>
#include <math.h>
#include <string.h>

// Sprites globales
SpritesJr sprites_jr = {{NULL, 0, 0}, {NULL, 0, 0}, {NULL, 0, 0}, {NULL, 0, 0}, {NULL, 0, 0}, {NULL, 0, 0}};
Sprite sprite_donkey = {NULL, 0, 0};
Sprite sprite_cocodrilo_rojo = {NULL, 0, 0};
Sprite sprite_cocodrilo_azul = {NULL, 0, 0};
Sprite sprite_banana = {NULL, 0, 0};
Sprite sprite_corazon = {NULL, 0, 0};

// Utilidades para resolver rutas de sprites de forma robusta
static void getExeDir(char* out, size_t outSize) {
    DWORD len = GetModuleFileNameA(NULL, out, (DWORD)outSize);
    if (len == 0 || len >= outSize) { out[0] = '\0'; return; }
    for (int i = (int)len - 1; i >= 0; --i) {
        if (out[i] == '\\' || out[i] == '/') { out[i] = '\0'; break; }
    }
}

static int tryLoadPath(const char* path, Sprite* sprite) {
    HBITMAP hBitmap = (HBITMAP)LoadImageA(NULL, path, IMAGE_BITMAP, 0, 0, LR_LOADFROMFILE);
    if (!hBitmap) return 0;
    BITMAP bm; GetObject(hBitmap, sizeof(BITMAP), &bm);
    sprite->bitmap = hBitmap; sprite->width = bm.bmWidth; sprite->height = bm.bmHeight;
    return 1;
}

static int cargarSpriteAuto(const char* filename, Sprite* sprite) {
    char exeDir[MAX_PATH] = {0};
    getExeDir(exeDir, sizeof exeDir);
    char candidate[MAX_PATH * 2];

    if (exeDir[0]) {
        // exeDir\..\sprites\filename (exe dentro de src)
        snprintf(candidate, sizeof candidate, "%s\\..\\sprites\\%s", exeDir, filename);
        if (tryLoadPath(candidate, sprite)) return 1;
        // exeDir\sprites\filename (exe y sprites juntos)
        snprintf(candidate, sizeof candidate, "%s\\sprites\\%s", exeDir, filename);
        if (tryLoadPath(candidate, sprite)) return 1;
    }

    // CWD relativos típicos
    snprintf(candidate, sizeof candidate, "..\\sprites\\%s", filename);
    if (tryLoadPath(candidate, sprite)) return 1;
    snprintf(candidate, sizeof candidate, ".\\sprites\\%s", filename);
    if (tryLoadPath(candidate, sprite)) return 1;
    // Último intento: nombre directo en CWD
    if (tryLoadPath(filename, sprite)) return 1;
    return 0;
}

// Carga un sprite desde archivo con búsqueda flexible
static int cargarSprite(const char* ruta, Sprite* sprite) {
    if (tryLoadPath(ruta, sprite)) return 1;
    const char* filename = ruta;
    const char* s1 = strrchr(ruta, '/');
    const char* s2 = strrchr(ruta, '\\');
    if (s1 || s2) filename = (s1 && (!s2 || s1 > s2)) ? s1 + 1 : s2 + 1;
    if (cargarSpriteAuto(filename, sprite)) return 1;
    printf("Error: No se pudo cargar sprite: %s\n", ruta);
    return 0;
}

// Carga todos los sprites del juego
int cargarSprites() {
    // Animaciones de Jr
    if (!cargarSprite("jr_frente.bmp", &sprites_jr.frente))
        cargarSprite("donkey_frente.bmp", &sprites_jr.frente);
    if (!cargarSprite("jr_subiendo.bmp", &sprites_jr.subiendo))
        cargarSprite("donkey_liana_subir.bmp", &sprites_jr.subiendo);
    if (!cargarSprite("jr_bajando.bmp", &sprites_jr.bajando))
        cargarSprite("donkey_liana_subir.bmp", &sprites_jr.bajando);
    if (!cargarSprite("jr_izquierda.bmp", &sprites_jr.izquierda))
        cargarSprite("donkey_pasar_liana.bmp", &sprites_jr.izquierda);
    if (!cargarSprite("jr_derecha.bmp", &sprites_jr.derecha))
        cargarSprite("donkey_pasar_liana.bmp", &sprites_jr.derecha);
    if (!cargarSprite("jr_saltando.bmp", &sprites_jr.saltando))
        cargarSprite("donkey_pasar_liana.bmp", &sprites_jr.saltando);

    // Otros sprites
    if (!cargarSprite("donkey.bmp", &sprite_donkey))
        cargarSprite("donkey_frente.bmp", &sprite_donkey);
    if (!cargarSprite("cocodrilo_rojo.bmp", &sprite_cocodrilo_rojo)) {
        if (!cargarSprite("cocodrilo_izq_rojo.bmp", &sprite_cocodrilo_rojo))
            cargarSprite("cocodrilo_abajo_rojo.bmp", &sprite_cocodrilo_rojo);
    }
    if (!cargarSprite("cocodrilo_azul.bmp", &sprite_cocodrilo_azul)) {
        if (!cargarSprite("cocodrilo_izq_azul.bmp", &sprite_cocodrilo_azul))
            cargarSprite("cocodrilo_abajo_azul.bmp", &sprite_cocodrilo_azul);
    }
    if (!cargarSprite("banana.bmp", &sprite_banana))
        cargarSprite("fruta_banana.bmp", &sprite_banana);
    cargarSprite("corazon.bmp", &sprite_corazon);

    return 1;
}

// Libera la memoria de los sprites
void liberarSprites() {
    if (sprites_jr.frente.bitmap) DeleteObject(sprites_jr.frente.bitmap);
    if (sprites_jr.subiendo.bitmap) DeleteObject(sprites_jr.subiendo.bitmap);
    if (sprites_jr.bajando.bitmap) DeleteObject(sprites_jr.bajando.bitmap);
    if (sprites_jr.izquierda.bitmap) DeleteObject(sprites_jr.izquierda.bitmap);
    if (sprites_jr.derecha.bitmap) DeleteObject(sprites_jr.derecha.bitmap);
    if (sprites_jr.saltando.bitmap) DeleteObject(sprites_jr.saltando.bitmap);
    if (sprite_donkey.bitmap) DeleteObject(sprite_donkey.bitmap);
    if (sprite_cocodrilo_rojo.bitmap) DeleteObject(sprite_cocodrilo_rojo.bitmap);
    if (sprite_cocodrilo_azul.bitmap) DeleteObject(sprite_cocodrilo_azul.bitmap);
    if (sprite_banana.bitmap) DeleteObject(sprite_banana.bitmap);
    if (sprite_corazon.bitmap) DeleteObject(sprite_corazon.bitmap);
}

// Dibuja un sprite en su tamaño original
void dibujarSprite(HDC hdc, Sprite* sprite, int x, int y) {
    if (!sprite || !sprite->bitmap) return;
    HDC hdcMem = CreateCompatibleDC(hdc);
    HBITMAP hbmOld = (HBITMAP)SelectObject(hdcMem, sprite->bitmap);
    TransparentBlt(hdc, x, y, sprite->width, sprite->height, hdcMem, 0, 0, sprite->width, sprite->height, RGB(0,0,0));
    SelectObject(hdcMem, hbmOld);
    DeleteDC(hdcMem);
}

// Dibuja un sprite escalado
void dibujarSpriteEscalado(HDC hdc, Sprite* sprite, int x, int y, int width, int height) {
    if (!sprite || !sprite->bitmap) return;
    HDC hdcMem = CreateCompatibleDC(hdc);
    HBITMAP hbmOld = (HBITMAP)SelectObject(hdcMem, sprite->bitmap);
    TransparentBlt(hdc, x, y, width, height, hdcMem, 0, 0, sprite->width, sprite->height, RGB(0,0,0));
    SelectObject(hdcMem, hbmOld);
    DeleteDC(hdcMem);
}

// Obtiene el sprite correcto de Jr según su estado de movimiento
Sprite* obtenerSpriteJr(double velocidadY, double velocidadX, int enLiana) {
    if (velocidadY < -0.5 && sprites_jr.subiendo.bitmap) return &sprites_jr.subiendo;
    if (velocidadY > 0.5 && sprites_jr.bajando.bitmap) return &sprites_jr.bajando;
    if (velocidadX < -0.5 && sprites_jr.izquierda.bitmap) return &sprites_jr.izquierda;
    if (velocidadX > 0.5 && sprites_jr.derecha.bitmap) return &sprites_jr.derecha;
    if (!enLiana && sprites_jr.saltando.bitmap) return &sprites_jr.saltando;
    if (sprites_jr.frente.bitmap) return &sprites_jr.frente;
    return NULL;
}

