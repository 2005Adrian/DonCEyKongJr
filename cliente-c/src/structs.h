#ifndef STRUCTS_H
#define STRUCTS_H

#include "constants.h"

// Estructura para representar un jugador
typedef struct {
    char id[32];
    double x;
    double y;
    int liana;
    int lives;
    int score;
    int active;
} Jugador;

// Estructura para representar un cocodrilo
typedef struct {
    char id[32];
    char kind[16];  // "ROJO" o "AZUL"
    int liana;
    double y;
} Cocodrilo;

// Estructura para representar una fruta
typedef struct {
    char id[32];
    int liana;
    double y;
    int points;
} Fruta;

// Estructura para representar el estado completo del juego
typedef struct {
    long tick;
    int level;
    double speedMultiplier;
    int paused;

    Jugador jugadores[MAX_JUGADORES];
    int numJugadores;

    Cocodrilo cocodrilos[MAX_COCODRILOS];
    int numCocodrilos;

    Fruta frutas[MAX_FRUTAS];
    int numFrutas;
} EstadoJuego;

// Alias para mantener compatibilidad
typedef EstadoJuego EstadoActual;

// Estructura para mensajes JSON que se envian al servidor
typedef struct {
    char type[32];      // "INPUT", "CONNECT", "DISCONNECT"
    char playerId[32];
    char action[32];    // "MOVE_UP", "MOVE_DOWN", "LEFT", "RIGHT", "JUMP", "GRAB"
    double velocity;
} MensajeCliente;

#endif // STRUCTS_H
