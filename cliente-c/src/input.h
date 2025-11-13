#ifndef INPUT_H
#define INPUT_H

// Evita conflictos de headers en Windows
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <winsock2.h>
#include <windows.h>

// Estados de las teclas
typedef struct {
    BOOL w_pressed;
    BOOL s_pressed;
    BOOL a_pressed;
    BOOL d_pressed;
    BOOL space_pressed;
    BOOL shift_pressed;
    BOOL lastSentW;
    BOOL lastSentS;
    BOOL lastSentA;
    BOOL lastSentD;
    BOOL lastSentSpace;
    BOOL lastSentShift;
} EstadoTeclas;

// Inicializa el sistema de input
void inicializarInput(void);

// Limpia recursos del sistema de input
void limpiarRecursosInput(void);

// Actualiza el estado de una tecla cuando se presiona
void marcarTeclaPresionada(WPARAM wParam);

// Actualiza el estado de una tecla cuando se suelta
void marcarTeclaSoltada(WPARAM wParam);

// Procesa y envía inputs acumulados al servidor (llamar cada frame)
void procesarInputsAcumulados(void);

// Procesa teclas presionadas (legacy - mantener compatibilidad)
void procesarTecla(WPARAM wParam);

#endif // INPUT_H
