#ifndef CONSTANTS_H
#define CONSTANTS_H

// Configuracion de red
#define SERVER_IP "127.0.0.1"
#define SERVER_PORT 5555
#define BUFFER_SIZE 8192

// Tipos de cliente
#define CLIENT_TYPE_PLAYER "PLAYER"
#define CLIENT_TYPE_SPECTATOR "SPECTATOR"

// Constantes del juego
#define MAX_LIANAS 8
#define MAX_COCODRILOS 50
#define MAX_FRUTAS 20
#define MAX_JUGADORES 4

// Dimensiones de la pantalla (consola)
#define SCREEN_WIDTH 80
#define SCREEN_HEIGHT 30

// Teclas de control
#define KEY_LEFT 'A'
#define KEY_RIGHT 'D'
#define KEY_UP 'W'
#define KEY_DOWN 'S'
#define KEY_JUMP VK_SPACE
#define KEY_GRAB_SHIFT VK_SHIFT
#define KEY_GRAB_CTRL VK_CONTROL
#define KEY_QUIT 'Q'

// Velocidad de movimiento
#define VELOCIDAD_MOVIMIENTO 1.0

// Tipos de cocodrilos
#define COCODRILO_ROJO "ROJO"
#define COCODRILO_AZUL "AZUL"

// Simbolos de renderizado
#define CHAR_JUGADOR 'J'
#define CHAR_COCODRILO_ROJO 'R'
#define CHAR_COCODRILO_AZUL 'A'
#define CHAR_FRUTA 'F'
#define CHAR_LIANA '|'
#define CHAR_VACIO ' '

// Timeout para recv (milisegundos)
#define RECV_TIMEOUT_MS 100

#endif // CONSTANTS_H
