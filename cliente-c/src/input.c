#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include "input.h"
#include "network.h"
#include "game.h"
#include "constants.h"

// Estado global de las teclas
static EstadoTeclas g_teclas;
static CRITICAL_SECTION g_inputLock;

void inicializarInput(void) {
    memset(&g_teclas, 0, sizeof(EstadoTeclas));
    InitializeCriticalSection(&g_inputLock);
}

void limpiarRecursosInput(void) {
    DeleteCriticalSection(&g_inputLock);
}

void limpiarEstadoTeclas(void) {
    EnterCriticalSection(&g_inputLock);
    memset(&g_teclas, 0, sizeof(EstadoTeclas));
    LeaveCriticalSection(&g_inputLock);
}

void marcarTeclaPresionada(WPARAM wParam) {
    EnterCriticalSection(&g_inputLock);

    int tecla = toupper((int)wParam);
    switch (tecla) {
        case 'W':
            g_teclas.w_pressed = TRUE;
            break;
        case 'S':
            g_teclas.s_pressed = TRUE;
            break;
        case 'A':
            g_teclas.a_pressed = TRUE;
            break;
        case 'D':
            g_teclas.d_pressed = TRUE;
            break;
    }

    switch (wParam) {
        case VK_SPACE:
            g_teclas.space_pressed = TRUE;
            break;
        case VK_SHIFT:
        case VK_LSHIFT:
        case VK_RSHIFT:
        case VK_CONTROL:
        case VK_LCONTROL:
        case VK_RCONTROL:
            g_teclas.shift_pressed = TRUE;
            break;
        case VK_ESCAPE:
        case 'Q':
            g_juegoActivo = 0;
            PostMessage(g_hwnd, WM_CLOSE, 0, 0);
            break;
    }

    LeaveCriticalSection(&g_inputLock);
}

void marcarTeclaSoltada(WPARAM wParam) {
    EnterCriticalSection(&g_inputLock);

    int tecla = toupper((int)wParam);
    switch (tecla) {
        case 'W':
            g_teclas.w_pressed = FALSE;
            break;
        case 'S':
            g_teclas.s_pressed = FALSE;
            break;
        case 'A':
            g_teclas.a_pressed = FALSE;
            break;
        case 'D':
            g_teclas.d_pressed = FALSE;
            break;
    }

    switch (wParam) {
        case VK_SPACE:
            g_teclas.space_pressed = FALSE;
            break;
        case VK_SHIFT:
        case VK_LSHIFT:
        case VK_RSHIFT:
        case VK_CONTROL:
        case VK_LCONTROL:
        case VK_RCONTROL:
            g_teclas.shift_pressed = FALSE;
            break;
    }

    LeaveCriticalSection(&g_inputLock);
}

void procesarInputsAcumulados(void) {
    if (!g_conectado) return;

    EnterCriticalSection(&g_inputLock);

    // Enviar solo cambios de estado para evitar flood
    // Para teclas de movimiento continuo, enviar si están presionadas

    if (g_teclas.w_pressed) {
        char* mensaje = crearMensajeInput("MOVE_UP", VELOCIDAD_MOVIMIENTO);
        if (mensaje) {
            enviarMensaje(mensaje);
            free(mensaje);
        }
    }

    if (g_teclas.s_pressed) {
        char* mensaje = crearMensajeInput("MOVE_DOWN", VELOCIDAD_MOVIMIENTO);
        if (mensaje) {
            enviarMensaje(mensaje);
            free(mensaje);
        }
    }

    if (g_teclas.a_pressed) {
        char* mensaje = crearMensajeInput("LEFT", VELOCIDAD_MOVIMIENTO);
        if (mensaje) {
            enviarMensaje(mensaje);
            free(mensaje);
        }
    }

    if (g_teclas.d_pressed) {
        char* mensaje = crearMensajeInput("RIGHT", VELOCIDAD_MOVIMIENTO);
        if (mensaje) {
            enviarMensaje(mensaje);
            free(mensaje);
        }
    }

    // Para acciones discretas (salto, agarre), enviar solo una vez al presionar
    if (g_teclas.space_pressed && !g_teclas.lastSentSpace) {
        char* mensaje = crearMensajeInput("JUMP", 1.0);
        if (mensaje) {
            enviarMensaje(mensaje);
            free(mensaje);
        }
        g_teclas.lastSentSpace = TRUE;
    } else if (!g_teclas.space_pressed) {
        g_teclas.lastSentSpace = FALSE;
    }

    if (g_teclas.shift_pressed && !g_teclas.lastSentShift) {
        char* mensaje = crearMensajeInput("GRAB", 1.0);
        if (mensaje) {
            enviarMensaje(mensaje);
            free(mensaje);
        }
        g_teclas.lastSentShift = TRUE;
    } else if (!g_teclas.shift_pressed) {
        g_teclas.lastSentShift = FALSE;
    }

    LeaveCriticalSection(&g_inputLock);
}

// Función legacy para compatibilidad (ahora solo marca la tecla)
void procesarTecla(WPARAM wParam) {
    marcarTeclaPresionada(wParam);
}
