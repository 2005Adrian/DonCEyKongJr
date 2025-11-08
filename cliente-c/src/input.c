#include <stdlib.h>
#include "input.h"
#include "network.h"
#include "game.h"
#include "constants.h"

void procesarTecla(WPARAM wParam) {
    char* mensaje = NULL;

    switch (wParam) {
        case 'W': mensaje = crearMensajeInput("MOVE_UP", VELOCIDAD_MOVIMIENTO); break;
        case 'S': mensaje = crearMensajeInput("MOVE_DOWN", VELOCIDAD_MOVIMIENTO); break;
        case 'A': mensaje = crearMensajeInput("LEFT", VELOCIDAD_MOVIMIENTO); break;
        case 'D': mensaje = crearMensajeInput("RIGHT", VELOCIDAD_MOVIMIENTO); break;
        case VK_SPACE: mensaje = crearMensajeInput("JUMP", 1.0); break;
        case 'E': mensaje = crearMensajeInput("GRAB", 1.0); break;
        case 'Q':
        case VK_ESCAPE:
            g_juegoActivo = 0;
            PostMessage(g_hwnd, WM_CLOSE, 0, 0);
            break;
    }

    if (mensaje) {
        enviarMensaje(mensaje);
        free(mensaje);
    }
}
