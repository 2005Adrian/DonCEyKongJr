#include "sprites.h"
#include <stdio.h>
#include <math.h>
#include <string.h>

// Contador global de animación (incrementa en main.c mediante g_animacionFrame++)
extern int g_animacionFrame;

// Sprites globales
SpritesJr sprites_jr = (SpritesJr){0};
Sprite sprite_donkey = {NULL, 0, 0};
Sprite sprite_cocodrilo_rojo = {NULL, 0, 0};
Sprite sprite_cocodrilo_azul = {NULL, 0, 0};
Sprite sprite_banana = {NULL, 0, 0};
Sprite sprite_corazon = {NULL, 0, 0};

// =========================
//  Utilidades de archivo
// =========================

// Obtiene el directorio del ejecutable (sin el nombre del .exe)
static void getExeDir(char* out, size_t outSize) {
    if (!out || outSize == 0) {
        return;
    }

    DWORD len = GetModuleFileNameA(NULL, out, (DWORD)outSize);
    if (len == 0 || len >= outSize) {
        out[0] = '\0';
        return;
    }

    for (int i = (int)len - 1; i >= 0; --i) {
        if (out[i] == '\\' || out[i] == '/') {
            out[i] = '\0';
            break;
        }
    }
}

// Intenta cargar una imagen desde path directo
static int tryLoadPath(const char* path, Sprite* sprite) {
    if (!path || !sprite) {
        return 0;
    }

    HBITMAP hBitmap = (HBITMAP)LoadImageA(
        NULL,
        path,
        IMAGE_BITMAP,
        0,
        0,
        LR_LOADFROMFILE | LR_CREATEDIBSECTION
    );

    if (!hBitmap) {
        hBitmap = (HBITMAP)LoadImageA(
            NULL,
            path,
            IMAGE_BITMAP,
            0,
            0,
            LR_LOADFROMFILE
        );
    }

    if (!hBitmap) {
        return 0;
    }

    BITMAP bm;
    GetObject(hBitmap, sizeof(BITMAP), &bm);
    sprite->bitmap = hBitmap;
    sprite->width = bm.bmWidth;
    sprite->height = bm.bmHeight;
    return 1;
}

// Intenta cargar un archivo asumiendo que solo se conoce el nombre (no la ruta)
static int cargarSpriteAuto(const char* filename, Sprite* sprite) {
    if (!filename || !sprite) {
        return 0;
    }

    char exeDir[MAX_PATH] = {0};
    getExeDir(exeDir, sizeof exeDir);

    char candidate[MAX_PATH * 2];

    if (exeDir[0] != '\0') {
        // exeDir\..\sprites\filename (cuando el exe está en src\)
        snprintf(candidate, sizeof candidate, "%s\\..\\sprites\\%s", exeDir, filename);
        if (tryLoadPath(candidate, sprite)) return 1;

        // exeDir\sprites\filename (cuando el exe y sprites están juntos)
        snprintf(candidate, sizeof candidate, "%s\\sprites\\%s", exeDir, filename);
        if (tryLoadPath(candidate, sprite)) return 1;
    }

    // Rutas relativas típicas desde el CWD
    snprintf(candidate, sizeof candidate, "..\\sprites\\%s", filename);
    if (tryLoadPath(candidate, sprite)) return 1;

    snprintf(candidate, sizeof candidate, ".\\sprites\\%s", filename);
    if (tryLoadPath(candidate, sprite)) return 1;

    // Último intento: nombre directo en el CWD
    if (tryLoadPath(filename, sprite)) return 1;

    return 0;
}

// Carga un sprite desde una ruta (posible con carpetas); si falla, intenta buscarlo por nombre
static int cargarSprite(const char* ruta, Sprite* sprite) {
    if (!ruta || !sprite) {
        return 0;
    }

    // Intentar ruta tal cual
    if (tryLoadPath(ruta, sprite)) {
        return 1;
    }

    // Extraer solo el nombre de archivo
    const char* filename = ruta;
    const char* s1 = strrchr(ruta, '/');
    const char* s2 = strrchr(ruta, '\\');
    if (s1 || s2) {
        filename = (s1 && (!s2 || s1 > s2)) ? s1 + 1 : s2 + 1;
    }

    if (cargarSpriteAuto(filename, sprite)) {
        return 1;
    }

    printf("Error: No se pudo cargar sprite: %s\n", ruta);
    return 0;
}

// =========================
//  Animación
// =========================

void actualizarAnimaciones(double deltaTime) {
    // Por ahora, la animación se basa en g_animacionFrame (30 FPS)
    // y no en tiempo real. Esta función se deja lista por si se
    // quiere usar deltaTime más adelante.
    (void)deltaTime;
}

Sprite* obtenerFrameAnimacion(SpriteAnimation* anim, int frameIndex) {
    if (!anim || anim->frameCount <= 0) {
        return NULL;
    }
    if (frameIndex < 0) {
        frameIndex = 0;
    }
    int idx = frameIndex % anim->frameCount;
    return &anim->frames[idx];
}

// =========================
//  Carga de todos los sprites
// =========================

int cargarSprites() {
    // Sprites estáticos de Jr
    if (!cargarSprite("jr_frente.bmp", &sprites_jr.frente)) {
        cargarSprite("donkey_frente.bmp", &sprites_jr.frente);
    }

    if (!cargarSprite("jr_subiendo.bmp", &sprites_jr.subiendo)) {
        cargarSprite("donkey_liana_subir.bmp", &sprites_jr.subiendo);
    }

    if (!cargarSprite("jr_bajando.bmp", &sprites_jr.bajando)) {
        cargarSprite("donkey_liana_subir.bmp", &sprites_jr.bajando);
    }

    if (!cargarSprite("jr_saltando.bmp", &sprites_jr.saltando)) {
        cargarSprite("donkey_pasar_liana.bmp", &sprites_jr.saltando);
    }

    // Animación caminar izquierda (PNG)
    sprites_jr.caminandoIzq.frameCount = 0;
    if (cargarSprite("jr_caminando_izq1.bmp", &sprites_jr.caminandoIzq.frames[0])) {
        sprites_jr.caminandoIzq.frameCount++;
    }
    if (cargarSprite("jr_caminando_izq2.bmp", &sprites_jr.caminandoIzq.frames[1])) {
        sprites_jr.caminandoIzq.frameCount++;
    }
    if (cargarSprite("jr_caminando_izq3.bmp", &sprites_jr.caminandoIzq.frames[2])) {
        sprites_jr.caminandoIzq.frameCount++;
    }

    // Animación caminar derecha (PNG)
    sprites_jr.caminandoDer.frameCount = 0;
    if (cargarSprite("jr_caminando_der1.bmp", &sprites_jr.caminandoDer.frames[0])) {
        sprites_jr.caminandoDer.frameCount++;
    }
    if (cargarSprite("jr_caminando_der2.bmp", &sprites_jr.caminandoDer.frames[1])) {
        sprites_jr.caminandoDer.frameCount++;
    }
    if (cargarSprite("jr_caminando_der3.bmp", &sprites_jr.caminandoDer.frames[2])) {
        sprites_jr.caminandoDer.frameCount++;
    }

    // Sprites estáticos izquierda/derecha (fallback)
    if (!cargarSprite("jr_izquierda.bmp", &sprites_jr.izquierda)) {
        if (sprites_jr.caminandoIzq.frameCount > 0) {
            sprites_jr.izquierda = sprites_jr.caminandoIzq.frames[0];
        } else {
            cargarSprite("donkey_pasar_liana.bmp", &sprites_jr.izquierda);
        }
    }

    if (!cargarSprite("jr_derecha.bmp", &sprites_jr.derecha)) {
        if (sprites_jr.caminandoDer.frameCount > 0) {
            sprites_jr.derecha = sprites_jr.caminandoDer.frames[0];
        } else {
            cargarSprite("donkey_pasar_liana.bmp", &sprites_jr.derecha);
        }
    }

    // Otros sprites
    if (!cargarSprite("donkey.bmp", &sprite_donkey)) {
        cargarSprite("donkey_frente.bmp", &sprite_donkey);
    }

    if (!cargarSprite("cocodrilo_rojo.bmp", &sprite_cocodrilo_rojo)) {
        if (!cargarSprite("cocodrilo_izq_rojo.bmp", &sprite_cocodrilo_rojo)) {
            cargarSprite("cocodrilo_abajo_rojo.bmp", &sprite_cocodrilo_rojo);
        }
    }

    if (!cargarSprite("cocodrilo_azul.bmp", &sprite_cocodrilo_azul)) {
        if (!cargarSprite("cocodrilo_izq_azul.bmp", &sprite_cocodrilo_azul)) {
            cargarSprite("cocodrilo_abajo_azul.bmp", &sprite_cocodrilo_azul);
        }
    }

    if (!cargarSprite("banana.bmp", &sprite_banana)) {
        cargarSprite("fruta_banana.bmp", &sprite_banana);
    }

    cargarSprite("corazon.bmp", &sprite_corazon);

    return 1;
}

// Libera la memoria de los sprites principales
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

    // Nota: para mantener compatibilidad con la implementación original,
    // no liberamos explícitamente los frames de animación caminando*,
    // ya que izquierda/derecha pueden referenciar el mismo HBITMAP.
}

// =========================
//  Dibujo helper
// =========================

// Dibuja un sprite en su tamaño original
void dibujarSprite(HDC hdc, Sprite* sprite, int x, int y) {
    if (!sprite || !sprite->bitmap) return;

    HDC hdcMem = CreateCompatibleDC(hdc);
    HBITMAP hbmOld = (HBITMAP)SelectObject(hdcMem, sprite->bitmap);

    TransparentBlt(
        hdc,
        x,
        y,
        sprite->width,
        sprite->height,
        hdcMem,
        0,
        0,
        sprite->width,
        sprite->height,
        RGB(0, 0, 0)
    );

    SelectObject(hdcMem, hbmOld);
    DeleteDC(hdcMem);
}

// Dibuja un sprite escalado a un tamaño específico
void dibujarSpriteEscalado(HDC hdc, Sprite* sprite, int x, int y, int width, int height) {
    if (!sprite || !sprite->bitmap) return;

    HDC hdcMem = CreateCompatibleDC(hdc);
    HBITMAP hbmOld = (HBITMAP)SelectObject(hdcMem, sprite->bitmap);

    TransparentBlt(
        hdc,
        x,
        y,
        width,
        height,
        hdcMem,
        0,
        0,
        sprite->width,
        sprite->height,
        RGB(0, 0, 0)
    );

    SelectObject(hdcMem, hbmOld);
    DeleteDC(hdcMem);
}

// =========================
//  Selección de sprite de Jr
// =========================

Sprite* obtenerSpriteJr(const char* estado, const char* facing, double velocidadY, double velocidadX) {
    const double LIMITE_VERTICAL = 5.0;
    const double LIMITE_HORIZONTAL = 0.3;  // Umbral de movimiento horizontal

    // Índice de frame para caminar (cambia cada 4 frames de animación global)
    int frameWalkIndex = g_animacionFrame / 4;
    Sprite* walkIzq = obtenerFrameAnimacion(&sprites_jr.caminandoIzq, frameWalkIndex);
    Sprite* walkDer = obtenerFrameAnimacion(&sprites_jr.caminandoDer, frameWalkIndex);

    if (estado) {
        // EN_LIANA: usar sprites de subir / bajar
        if (strcmp(estado, "EN_LIANA") == 0) {
            if (velocidadY < -LIMITE_VERTICAL && sprites_jr.subiendo.bitmap) {
                return &sprites_jr.subiendo;
            }
            if (velocidadY > LIMITE_VERTICAL && sprites_jr.bajando.bitmap) {
                return &sprites_jr.bajando;
            }
            if (sprites_jr.saltando.bitmap) {
                return &sprites_jr.saltando;
            }
        }

        // SALTANDO: usar sprite de salto, pero orientado según velocidadX
        if (strcmp(estado, "SALTANDO") == 0) {
            if (velocidadX < -LIMITE_HORIZONTAL && sprites_jr.izquierda.bitmap) {
                return &sprites_jr.izquierda;
            }
            if (velocidadX > LIMITE_HORIZONTAL && sprites_jr.derecha.bitmap) {
                return &sprites_jr.derecha;
            }
            if (sprites_jr.saltando.bitmap) {
                return &sprites_jr.saltando;
            }
        }

        // CELEBRANDO: usar sprite de frente
        if (strcmp(estado, "CELEBRANDO") == 0 && sprites_jr.frente.bitmap) {
            return &sprites_jr.frente;
        }

        // MUERTO: reutilizar sprite de salto
        if (strcmp(estado, "MUERTO") == 0 && sprites_jr.saltando.bitmap) {
            return &sprites_jr.saltando;
        }

        // SUELO: animación de caminar + idle mirando a un lado
        if (strcmp(estado, "SUELO") == 0) {
            if (velocidadX < -LIMITE_HORIZONTAL) {
                if (walkIzq && walkIzq->bitmap) {
                    return walkIzq;
                }
                if (sprites_jr.izquierda.bitmap) {
                    return &sprites_jr.izquierda;
                }
            }
            if (velocidadX > LIMITE_HORIZONTAL) {
                if (walkDer && walkDer->bitmap) {
                    return walkDer;
                }
                if (sprites_jr.derecha.bitmap) {
                    return &sprites_jr.derecha;
                }
            }

            // Sin velocidad horizontal: usar sprite estático según facing
            if (facing && strcmp(facing, "LEFT") == 0 && sprites_jr.izquierda.bitmap) {
                return &sprites_jr.izquierda;
            }
            if (facing && strcmp(facing, "RIGHT") == 0 && sprites_jr.derecha.bitmap) {
                return &sprites_jr.derecha;
            }
        }
    }

    // Fallback genérico: si hay movimiento horizontal en cualquier otro estado
    if (velocidadX < -LIMITE_HORIZONTAL) {
        if (walkIzq && walkIzq->bitmap) {
            return walkIzq;
        }
        if (sprites_jr.izquierda.bitmap) {
            return &sprites_jr.izquierda;
        }
    }
    if (velocidadX > LIMITE_HORIZONTAL) {
        if (walkDer && walkDer->bitmap) {
            return walkDer;
        }
        if (sprites_jr.derecha.bitmap) {
            return &sprites_jr.derecha;
        }
    }

    // Último recurso: sprite de frente
    if (sprites_jr.frente.bitmap) {
        return &sprites_jr.frente;
    }

    return NULL;
}
