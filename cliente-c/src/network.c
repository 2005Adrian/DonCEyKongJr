#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#include <time.h>
#include <ctype.h>
#include "network.h"
#include "game.h"
#include "constants.h"
#include "structs.h"
#include "log_utils.h"

#define EFECTO_GOLPE_FRAMES 24
#define EFECTO_FRUTA_FRAMES 18

static int g_logEstados = 0;

// Mutex para proteger acceso a g_estadoActual
static CRITICAL_SECTION g_estadoLock;
static BOOL g_estadoLockInicializado = FALSE;

static void limpiarFinLinea(char* linea) {
    if (!linea) {
        return;
    }
    size_t len = strlen(linea);
    while (len > 0 && isspace((unsigned char)linea[len - 1])) {
        linea[--len] = '\0';
    }
}

static int extraerCadenaJson(const char* json, const char* clave, char* destino, size_t tam) {
    if (!json || !clave || !destino || tam == 0) {
        return 0;
    }

    char patron[64];
    snprintf(patron, sizeof(patron), "\"%s\":\"", clave);
    const char* inicio = strstr(json, patron);
    if (!inicio) {
        return 0;
    }

    inicio += strlen(patron);
    size_t i = 0;
    const char* cursor = inicio;
    while (*cursor && *cursor != '"' && i + 1 < tam) {
        if (*cursor == '\\' && cursor[1] != '\0') {
            ++cursor;
        }
        destino[i++] = *cursor++;
    }
    destino[i] = '\0';
    return (int)i;
}

static const char* extraerObjetoDatos(const char* json) {
    if (!json) {
        return NULL;
    }
    const char* dataKey = strstr(json, "\"data\":");
    const char* inicio = dataKey ? strchr(dataKey, '{') : strchr(json, '{');
    return inicio;
}

static void procesarEventoJSON(const char* json) {
    char nombre[32] = {0};
    if (!extraerCadenaJson(json, "name", nombre, sizeof(nombre))) {
        client_log("evento sin nombre: %s", json);
        return;
    }

    if (strcmp(nombre, "PLAYER_HIT") == 0) {
        g_efectoGolpe = EFECTO_GOLPE_FRAMES;
    } else if (strcmp(nombre, "FRUIT_TAKEN") == 0) {
        g_efectoFruta = EFECTO_FRUTA_FRAMES;
    } else if (strcmp(nombre, "PLAYER_ELIMINATED") == 0) {
        g_estadoPantalla = ESTADO_GAME_OVER;
    } else if (strcmp(nombre, "PLAYER_WIN") == 0) {
        g_estadoPantalla = ESTADO_VICTORIA;
    } else {
        client_log("evento desconocido: %s", nombre);
    }
}

int conectarServidor() {
    WSADATA wsa;
    struct sockaddr_in server;

    // Inicializar mutex para sincronización
    if (!g_estadoLockInicializado) {
        InitializeCriticalSection(&g_estadoLock);
        g_estadoLockInicializado = TRUE;
    }

    if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0) {
        client_log("WSAStartup fallo");
        return 0;
    }

    g_sockCliente = socket(AF_INET, SOCK_STREAM, 0);
    if (g_sockCliente == INVALID_SOCKET) {
        WSACleanup();
        client_log("socket invalido");
        return 0;
    }

    DWORD timeout = 100;
    setsockopt(g_sockCliente, SOL_SOCKET, SO_RCVTIMEO, (char*)&timeout, sizeof(timeout));

    server.sin_family = AF_INET;
    server.sin_port = htons(SERVER_PORT);
    inet_pton(AF_INET, SERVER_IP, &server.sin_addr);

    if (connect(g_sockCliente, (struct sockaddr*)&server, sizeof(server)) < 0) {
        closesocket(g_sockCliente);
        WSACleanup();
        client_log("connect fallo");
        return 0;
    }
    client_log("conectado a servidor");
    return 1;
}

void desconectarServidor() {
    if (g_conectado) {
        char msg[128];
        sprintf(msg, "{\"type\":\"DISCONNECT\",\"id\":\"%s\",\"playerId\":\"%s\"}", g_miPlayerId, g_miPlayerId);
        enviarMensaje(msg);
    }
    closesocket(g_sockCliente);
    WSACleanup();
    client_log("desconectado del servidor");
}

void limpiarRecursosRed() {
    if (g_estadoLockInicializado) {
        DeleteCriticalSection(&g_estadoLock);
        g_estadoLockInicializado = FALSE;
        client_log("CRITICAL_SECTION de network liberado");
    }
}

int enviarMensaje(const char* json) {
    char buffer[BUFFER_SIZE];
    sprintf(buffer, "%s\n", json);
    return send(g_sockCliente, buffer, strlen(buffer), 0);
}

char* crearMensajeConexion() {
    char* msg = (char*)malloc(256);
    sprintf(msg, "{\"type\":\"CONNECT\",\"id\":\"%s\",\"playerId\":\"%s\"}", g_miPlayerId, g_miPlayerId);
    return msg;
}

char* crearMensajeInput(const char* accion, double velocidad) {
    char* msg = (char*)malloc(512);
    sprintf(msg, "{\"type\":\"INPUT\",\"id\":\"%s\",\"playerId\":\"%s\",\"action\":\"%s\",\"velocity\":%.1f}",
            g_miPlayerId, g_miPlayerId, accion, velocidad);
    return msg;
}

void parsearEstadoJSON(const char* json) {
    if (!json || !*json) {
        return;
    }

    char tipo[16] = {0};
    if (extraerCadenaJson(json, "type", tipo, sizeof(tipo))) {
        if (_stricmp(tipo, "EVENT") == 0) {
            procesarEventoJSON(json);
            return;
        }
        if (_stricmp(tipo, "ERROR") == 0) {
            client_log("mensaje de error del servidor: %s", json);
            return;
        }
        if (_stricmp(tipo, "STATE") != 0) {
            client_log("mensaje desconocido (type=%s): %s", tipo, json);
            return;
        }
    }

    const char* payload = extraerObjetoDatos(json);
    if (!payload) {
        client_log("estado sin objeto de datos: %s", json);
        return;
    }

    EstadoActual nuevo;
    memset(&nuevo, 0, sizeof(nuevo));
    nuevo.speedMultiplier = 1.0;

    const char* pausedPos = strstr(payload, "\"paused\":");
    if (pausedPos) {
        char pausedStr[6] = {0};
        sscanf(pausedPos + 9, "%5s", pausedStr);
        nuevo.paused = (strncmp(pausedStr, "true", 4) == 0);
    }

    const char* tickPos = strstr(payload, "\"tick\":");
    if (tickPos) {
        sscanf(tickPos + 7, "%ld", &nuevo.tick);
    }

    const char* speedKey = "\"speedMultiplier\":";
    const char* speedPos = strstr(payload, speedKey);
    if (speedPos) {
        sscanf(speedPos + (int)strlen(speedKey), "%lf", &nuevo.speedMultiplier);
    }

    const char* celebrationKey = "\"celebrationPending\":";
    const char* celebrationPos = strstr(payload, celebrationKey);
    if (celebrationPos) {
        char buffer[8] = {0};
        sscanf(celebrationPos + (int)strlen(celebrationKey), "%7s", buffer);
        nuevo.celebrationPending = (strncmp(buffer, "true", 4) == 0);
    }

    const char* celebrationTimerKey = "\"celebrationTimer\":";
    const char* celebrationTimerPos = strstr(payload, celebrationTimerKey);
    if (celebrationTimerPos) {
        sscanf(celebrationTimerPos + (int)strlen(celebrationTimerKey), "%lf", &nuevo.celebrationTimer);
    }

    // Jugadores
    const char* playersStart = strstr(payload, "\"players\":[");
    if (playersStart) {
        const char* cursor = playersStart + 11;
        const char* arrayEnd = strchr(playersStart, ']');
        while (nuevo.numJugadores < MAX_JUGADORES) {
            const char* objStart = strchr(cursor, '{');
            if (!objStart || (arrayEnd && objStart > arrayEnd)) break;
            const char* objEnd = strchr(objStart, '}');
            if (!objEnd) break;

            Jugador* j = &nuevo.jugadores[nuevo.numJugadores];
            memset(j, 0, sizeof(*j));
            j->liana = -1;
            j->lianaId = -1;

            const char* idPos = strstr(objStart, "\"id\":\"");
            if (idPos && idPos < objEnd) sscanf(idPos + 6, "%31[^\"]", j->id);

            const char* xPos = strstr(objStart, "\"x\":");
            if (xPos && xPos < objEnd) sscanf(xPos + 4, "%lf", &j->x);

            const char* yPos = strstr(objStart, "\"y\":");
            if (yPos && yPos < objEnd) sscanf(yPos + 4, "%lf", &j->y);

            const char* vxPos = strstr(objStart, "\"vx\":");
            if (vxPos && vxPos < objEnd) sscanf(vxPos + 5, "%lf", &j->vx);

            const char* vyPos = strstr(objStart, "\"vy\":");
            if (vyPos && vyPos < objEnd) sscanf(vyPos + 5, "%lf", &j->vy);

            const char* lianaPos = strstr(objStart, "\"liana\":");
            if (lianaPos && lianaPos < objEnd) sscanf(lianaPos + 8, "%d", &j->liana);

            const char* lianaIdPos = strstr(objStart, "\"lianaId\":");
            if (lianaIdPos && lianaIdPos < objEnd) {
                if (strncmp(lianaIdPos + 10, "null", 4) == 0) {
                    j->lianaId = -1;
                } else {
                    sscanf(lianaIdPos + 10, "%d", &j->lianaId);
                }
            }

            const char* livesPos = strstr(objStart, "\"lives\":");
            if (livesPos && livesPos < objEnd) sscanf(livesPos + 8, "%d", &j->lives);

            const char* scorePos = strstr(objStart, "\"score\":");
            if (scorePos && scorePos < objEnd) sscanf(scorePos + 8, "%d", &j->score);

            const char* statePos = strstr(objStart, "\"state\":\"");
            if (statePos && statePos < objEnd) sscanf(statePos + 9, "%15[^\"]", j->state);

            const char* facingPos = strstr(objStart, "\"facing\":\"");
            if (facingPos && facingPos < objEnd) sscanf(facingPos + 10, "%7[^\"]", j->facing);

            const char* activePos = strstr(objStart, "\"active\":");
            if (activePos && activePos < objEnd) {
                char activeStr[8] = {0};
                sscanf(activePos + 9, "%7s", activeStr);
                j->active = (strncmp(activeStr, "true", 4) == 0);
            }

            const char* celebratingPos = strstr(objStart, "\"celebrating\":");
            if (celebratingPos && celebratingPos < objEnd) {
                char celebratingStr[8] = {0};
                sscanf(celebratingPos + 14, "%7s", celebratingStr);
                j->celebrating = (strncmp(celebratingStr, "true", 4) == 0);
            }

            nuevo.numJugadores++;
            cursor = objEnd + 1;
        }
    }

    // Cocodrilos
    const char* crocsStart = strstr(payload, "\"crocodiles\":[");
    if (crocsStart) {
        const char* cursor = crocsStart + 14;
        const char* arrayEnd = strchr(crocsStart, ']');
        while (nuevo.numCocodrilos < MAX_COCODRILOS) {
            const char* objStart = strchr(cursor, '{');
            if (!objStart || (arrayEnd && objStart > arrayEnd)) break;
            const char* objEnd = strchr(objStart, '}');
            if (!objEnd) break;

            Cocodrilo* c = &nuevo.cocodrilos[nuevo.numCocodrilos];

            const char* idPos = strstr(objStart, "\"id\":\"");
            if (idPos && idPos < objEnd) sscanf(idPos + 6, "%31[^\"]", c->id);

            const char* kindPos = strstr(objStart, "\"kind\":\"");
            if (kindPos && kindPos < objEnd) sscanf(kindPos + 8, "%15[^\"]", c->kind);

            const char* lianaPos = strstr(objStart, "\"liana\":");
            if (lianaPos && lianaPos < objEnd) sscanf(lianaPos + 8, "%d", &c->liana);

            const char* yPos = strstr(objStart, "\"y\":");
            if (yPos && yPos < objEnd) sscanf(yPos + 4, "%lf", &c->y);

            nuevo.numCocodrilos++;
            cursor = objEnd + 1;
        }
    }

    // Frutas
    const char* fruitsStart = strstr(payload, "\"fruits\":[");
    if (fruitsStart) {
        const char* cursor = fruitsStart + 10;
        const char* arrayEnd = strchr(fruitsStart, ']');
        while (nuevo.numFrutas < MAX_FRUTAS) {
            const char* objStart = strchr(cursor, '{');
            if (!objStart || (arrayEnd && objStart > arrayEnd)) break;
            const char* objEnd = strchr(objStart, '}');
            if (!objEnd) break;

            Fruta* f = &nuevo.frutas[nuevo.numFrutas];

            const char* idPos = strstr(objStart, "\"id\":\"");
            if (idPos && idPos < objEnd) sscanf(idPos + 6, "%31[^\"]", f->id);

            const char* lianaPos = strstr(objStart, "\"liana\":");
            if (lianaPos && lianaPos < objEnd) sscanf(lianaPos + 8, "%d", &f->liana);

            const char* yPos = strstr(objStart, "\"y\":");
            if (yPos && yPos < objEnd) sscanf(yPos + 4, "%lf", &f->y);

            const char* pointsPos = strstr(objStart, "\"points\":");
            if (pointsPos && pointsPos < objEnd) sscanf(pointsPos + 9, "%d", &f->points);

            nuevo.numFrutas++;
            cursor = objEnd + 1;
        }
    }

    EnterCriticalSection(&g_estadoLock);
    g_estadoActual = nuevo;

    // REINICIO AUTOMÁTICO: Si estábamos en victoria/game over y el servidor reinició,
    // volver automáticamente a ESTADO_JUGANDO cuando detectemos jugadores activos no celebrando
    if (g_estadoPantalla == ESTADO_VICTORIA || g_estadoPantalla == ESTADO_GAME_OVER) {
        // Buscar si hay algún jugador activo que no esté celebrando (reinicio detectado)
        for (int i = 0; i < g_estadoActual.numJugadores; i++) {
            if (g_estadoActual.jugadores[i].active && !g_estadoActual.jugadores[i].celebrating) {
                g_estadoPantalla = ESTADO_JUGANDO;
                client_log("reinicio detectado - volviendo a ESTADO_JUGANDO");
                break;
            }
        }
    }

    LeaveCriticalSection(&g_estadoLock);
}

// Funciones helper para acceso thread-safe desde render.c
void bloquearEstado() {
    if (g_estadoLockInicializado) {
        EnterCriticalSection(&g_estadoLock);
    }
}

void desbloquearEstado() {
    if (g_estadoLockInicializado) {
        LeaveCriticalSection(&g_estadoLock);
    }
}

DWORD WINAPI ThreadRed(LPVOID lpParam) {
    char buffer[8192] = {0};
    int bufferPos = 0;

    client_log("ThreadRed iniciado");

    while (g_juegoActivo && g_conectado) {
        char temp[4096];
        int recibido = recv(g_sockCliente, temp, sizeof(temp) - 1, 0);

        if (recibido <= 0) {
            int error = WSAGetLastError();
            // Solo desconectar si es un error real (no timeout)
            if (error != WSAETIMEDOUT && error != WSAEWOULDBLOCK) {
                client_log("recv error %d -> desconectando", error);
                g_conectado = 0;
                g_estadoPantalla = ESTADO_DESCONECTADO;
                break;
            }
            // Si es timeout, continuar esperando
            Sleep(10);
            continue;
        }

        temp[recibido] = '\0';

        if (bufferPos + recibido < (int)sizeof(buffer)) {
            memcpy(buffer + bufferPos, temp, recibido);
            bufferPos += recibido;
            buffer[bufferPos] = '\0';
        } else {
            client_log("buffer saturado, se limpia");
            bufferPos = 0;
            buffer[0] = '\0';
        }

        char* inicio = buffer;
        char* fin;

        while ((fin = strchr(inicio, '\n')) != NULL) {
            *fin = '\0';
            limpiarFinLinea(inicio);
            if (strlen(inicio) > 0) {
                if (g_logEstados < 5) {
                    client_log("estado crudo: %s", inicio);
                    g_logEstados++;
                }
                parsearEstadoJSON(inicio);
            }
            inicio = fin + 1;
        }

        if (inicio != buffer) {
            bufferPos = strlen(inicio);
            memmove(buffer, inicio, bufferPos + 1);
        }
    }

    client_log("ThreadRed finalizado");
    return 0;
}
