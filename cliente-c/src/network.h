#ifndef NETWORK_H
#define NETWORK_H

#include <winsock2.h>

// Funciones de red
int conectarServidor();
void desconectarServidor();
void limpiarRecursosRed();
int enviarMensaje(const char* json);
char* crearMensajeConexion();
char* crearMensajeInput(const char* accion, double velocidad);

// Funciones de parseo
void parsearEstadoJSON(const char* json);

// Thread de red
DWORD WINAPI ThreadRed(LPVOID lpParam);

// Funciones de sincronización para acceso thread-safe a g_estadoActual
void bloquearEstado();
void desbloquearEstado();

#endif // NETWORK_H
