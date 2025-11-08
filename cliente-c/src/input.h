#ifndef INPUT_H
#define INPUT_H

// Evita conflictos de headers en Windows
#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <winsock2.h>
#include <windows.h>

// Procesa teclas presionadas y envía al servidor
void procesarTecla(WPARAM wParam);

#endif // INPUT_H
