#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#include <time.h>
#include "network.h"
#include "game.h"
#include "constants.h"
#include "structs.h"

int conectarServidor() {
    WSADATA wsa;
    struct sockaddr_in server;

    if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0) return 0;

    g_sockCliente = socket(AF_INET, SOCK_STREAM, 0);
    if (g_sockCliente == INVALID_SOCKET) {
        WSACleanup();
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
        return 0;
    }

    return 1;
}

void desconectarServidor() {
    if (g_conectado) {
        char msg[128];
        sprintf(msg, "{\"type\":\"DISCONNECT\",\"playerId\":\"%s\"}", g_miPlayerId);
        enviarMensaje(msg);
    }
    closesocket(g_sockCliente);
    WSACleanup();
}

int enviarMensaje(const char* json) {
    char buffer[BUFFER_SIZE];
    sprintf(buffer, "%s\n", json);
    return send(g_sockCliente, buffer, strlen(buffer), 0);
}

char* crearMensajeConexion() {
    char* msg = (char*)malloc(256);
    sprintf(msg, "{\"type\":\"CONNECT\",\"playerId\":\"%s\"}", g_miPlayerId);
    return msg;
}

char* crearMensajeInput(const char* accion, double velocidad) {
    char* msg = (char*)malloc(512);
    sprintf(msg, "{\"type\":\"INPUT\",\"playerId\":\"%s\",\"action\":\"%s\",\"velocity\":%.1f}",
            g_miPlayerId, accion, velocidad);
    return msg;
}

void parsearEstadoJSON(const char* json) {
    // Parsear campos básicos
    char* levelPos = strstr(json, "\"level\":");
    if (levelPos) sscanf(levelPos + 8, "%d", &g_estadoActual.level);

    char* pausedPos = strstr(json, "\"paused\":");
    if (pausedPos) {
        char pausedStr[16];
        sscanf(pausedPos + 9, "%s", pausedStr);
        g_estadoActual.paused = (strncmp(pausedStr, "true", 4) == 0);
    }

    char* tickPos = strstr(json, "\"tick\":");
    if (tickPos) sscanf(tickPos + 7, "%ld", &g_estadoActual.tick);

    // Parsear jugadores
    g_estadoActual.numJugadores = 0;
    char* playersStart = strstr(json, "\"players\":[");
    if (playersStart) {
        char* cursor = playersStart + 11;
        while (g_estadoActual.numJugadores < MAX_JUGADORES) {
            char* objStart = strchr(cursor, '{');
            if (!objStart) break;
            char* objEnd = strchr(objStart, '}');
            if (!objEnd) break;

            // Verificar que no hemos llegado al final del array
            char* arrayEnd = strchr(playersStart, ']');
            if (arrayEnd && objStart > arrayEnd) break;

            Jugador* j = &g_estadoActual.jugadores[g_estadoActual.numJugadores];

            char* idPos = strstr(objStart, "\"id\":\"");
            if (idPos && idPos < objEnd) sscanf(idPos + 6, "%31[^\"]", j->id);

            char* xPos = strstr(objStart, "\"x\":");
            if (xPos && xPos < objEnd) sscanf(xPos + 4, "%lf", &j->x);

            char* yPos = strstr(objStart, "\"y\":");
            if (yPos && yPos < objEnd) sscanf(yPos + 4, "%lf", &j->y);

            char* lianaPos = strstr(objStart, "\"liana\":");
            if (lianaPos && lianaPos < objEnd) sscanf(lianaPos + 8, "%d", &j->liana);

            char* livesPos = strstr(objStart, "\"lives\":");
            if (livesPos && livesPos < objEnd) sscanf(livesPos + 8, "%d", &j->lives);

            char* scorePos = strstr(objStart, "\"score\":");
            if (scorePos && scorePos < objEnd) sscanf(scorePos + 8, "%d", &j->score);

            char* activePos = strstr(objStart, "\"active\":");
            if (activePos && activePos < objEnd) {
                char activeStr[16];
                sscanf(activePos + 9, "%s", activeStr);
                j->active = (strncmp(activeStr, "true", 4) == 0);
            }

            g_estadoActual.numJugadores++;
            cursor = objEnd + 1;
        }
    }

    // Parsear cocodrilos
    g_estadoActual.numCocodrilos = 0;
    char* crocsStart = strstr(json, "\"crocodiles\":[");
    if (crocsStart) {
        char* cursor = crocsStart + 14;
        while (g_estadoActual.numCocodrilos < MAX_COCODRILOS) {
            char* objStart = strchr(cursor, '{');
            if (!objStart) break;
            char* objEnd = strchr(objStart, '}');
            if (!objEnd) break;

            // Verificar que no hemos llegado al final del array
            char* arrayEnd = strchr(crocsStart, ']');
            if (arrayEnd && objStart > arrayEnd) break;

            Cocodrilo* c = &g_estadoActual.cocodrilos[g_estadoActual.numCocodrilos];

            char* idPos = strstr(objStart, "\"id\":\"");
            if (idPos && idPos < objEnd) sscanf(idPos + 6, "%31[^\"]", c->id);

            char* kindPos = strstr(objStart, "\"kind\":\"");
            if (kindPos && kindPos < objEnd) sscanf(kindPos + 8, "%15[^\"]", c->kind);

            char* lianaPos = strstr(objStart, "\"liana\":");
            if (lianaPos && lianaPos < objEnd) sscanf(lianaPos + 8, "%d", &c->liana);

            char* yPos = strstr(objStart, "\"y\":");
            if (yPos && yPos < objEnd) sscanf(yPos + 4, "%lf", &c->y);

            g_estadoActual.numCocodrilos++;
            cursor = objEnd + 1;
        }
    }

    // Parsear frutas
    g_estadoActual.numFrutas = 0;
    char* fruitsStart = strstr(json, "\"fruits\":[");
    if (fruitsStart) {
        char* cursor = fruitsStart + 10;
        while (g_estadoActual.numFrutas < MAX_FRUTAS) {
            char* objStart = strchr(cursor, '{');
            if (!objStart) break;
            char* objEnd = strchr(objStart, '}');
            if (!objEnd) break;

            // Verificar que no hemos llegado al final del array
            char* arrayEnd = strchr(fruitsStart, ']');
            if (arrayEnd && objStart > arrayEnd) break;

            Fruta* f = &g_estadoActual.frutas[g_estadoActual.numFrutas];

            char* idPos = strstr(objStart, "\"id\":\"");
            if (idPos && idPos < objEnd) sscanf(idPos + 6, "%31[^\"]", f->id);

            char* lianaPos = strstr(objStart, "\"liana\":");
            if (lianaPos && lianaPos < objEnd) sscanf(lianaPos + 8, "%d", &f->liana);

            char* yPos = strstr(objStart, "\"y\":");
            if (yPos && yPos < objEnd) sscanf(yPos + 4, "%lf", &f->y);

            char* pointsPos = strstr(objStart, "\"points\":");
            if (pointsPos && pointsPos < objEnd) sscanf(pointsPos + 9, "%d", &f->points);

            g_estadoActual.numFrutas++;
            g_efectoFruta = 10;
            cursor = objEnd + 1;
        }
    }
}

DWORD WINAPI ThreadRed(LPVOID lpParam) {
    char buffer[8192] = {0};
    int bufferPos = 0;

    while (g_juegoActivo && g_conectado) {
        char temp[4096];
        int recibido = recv(g_sockCliente, temp, sizeof(temp) - 1, 0);

        if (recibido <= 0) {
            int error = WSAGetLastError();
            // Solo desconectar si es un error real (no timeout)
            if (error != WSAETIMEDOUT && error != WSAEWOULDBLOCK) {
                g_conectado = 0;
                g_estadoPantalla = ESTADO_DESCONECTADO;
                break;
            }
            // Si es timeout, continuar esperando
            Sleep(10);
            continue;
        }

        temp[recibido] = '\0';

        if (bufferPos + recibido < sizeof(buffer)) {
            strcpy(buffer + bufferPos, temp);
            bufferPos += recibido;
        }

        char* inicio = buffer;
        char* fin;

        while ((fin = strchr(inicio, '\n')) != NULL) {
            *fin = '\0';
            if (strlen(inicio) > 0) {
                parsearEstadoJSON(inicio);
            }
            inicio = fin + 1;
        }

        if (inicio != buffer) {
            bufferPos = strlen(inicio);
            memmove(buffer, inicio, bufferPos + 1);
        }
    }

    return 0;
}
