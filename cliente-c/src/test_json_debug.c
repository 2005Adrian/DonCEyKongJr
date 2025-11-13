#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include <ws2tcpip.h>

#define SERVER_IP "127.0.0.1"
#define SERVER_PORT 5555

int main() {
    WSADATA wsa;
    SOCKET sock;
    struct sockaddr_in server;
    char buffer[8192];
    char playerId[32];

    sprintf(playerId, "TestPlayer_%d", rand() % 1000);

    printf("=== DEBUG JSON CLIENT ===\n");
    printf("Conectando a %s:%d...\n", SERVER_IP, SERVER_PORT);

    // Inicializar Winsock
    if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0) {
        printf("Error: WSAStartup falló\n");
        return 1;
    }

    // Crear socket
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock == INVALID_SOCKET) {
        printf("Error: No se pudo crear socket\n");
        WSACleanup();
        return 1;
    }

    // Configurar servidor
    server.sin_family = AF_INET;
    server.sin_port = htons(SERVER_PORT);
    inet_pton(AF_INET, SERVER_IP, &server.sin_addr);

    // Conectar
    if (connect(sock, (struct sockaddr*)&server, sizeof(server)) < 0) {
        printf("Error: No se pudo conectar al servidor\n");
        closesocket(sock);
        WSACleanup();
        return 1;
    }

    printf("Conectado!\n\n");

    // Enviar mensaje de conexión
    char msgConexion[256];
    sprintf(msgConexion, "{\"type\":\"CONNECT\",\"playerId\":\"%s\"}\n", playerId);
    send(sock, msgConexion, strlen(msgConexion), 0);
    printf("Enviado: %s\n", msgConexion);

    printf("\n=== RECIBIENDO MENSAJES DEL SERVIDOR ===\n");
    printf("(Presiona Ctrl+C para salir)\n\n");

    // Recibir y mostrar mensajes
    int msgCount = 0;
    while (1) {
        int recibido = recv(sock, buffer, sizeof(buffer) - 1, 0);
        if (recibido <= 0) {
            printf("\nConexión cerrada por el servidor\n");
            break;
        }

        buffer[recibido] = '\0';

        // Procesar cada línea
        char* inicio = buffer;
        char* fin;

        while ((fin = strchr(inicio, '\n')) != NULL) {
            *fin = '\0';
            if (strlen(inicio) > 0) {
                msgCount++;
                printf("\n[Mensaje #%d] (%d bytes)\n", msgCount, (int)strlen(inicio));
                printf("----------------------------------------\n");
                printf("%s\n", inicio);
                printf("----------------------------------------\n");

                // Mostrar analisis basico
                printf("Analisis:
");
                printf("  - tick: %s
", strstr(inicio, ""tick":") ? "SI" : "NO");
                printf("  - speedMultiplier: %s
", strstr(inicio, ""speedMultiplier":") ? "SI" : "NO");
                printf("  - celebrationPending: %s
", strstr(inicio, ""celebrationPending":") ? "SI" : "NO");
                printf("  - celebrationTimer: %s
", strstr(inicio, ""celebrationTimer":") ? "SI" : "NO");
                printf("  - players: %s
", strstr(inicio, ""players":[") ? "SI" : "NO");
                printf("  - crocodiles: %s
", strstr(inicio, ""crocodiles":[") ? "SI" : "NO");
                printf("  - fruits: %s
", strstr(inicio, ""fruits":[") ? "SI" : "NO");

                // Contar entidades
                if (strstr(inicio, "\"players\":[")) {
                    char* players = strstr(inicio, "\"players\":[");
                    int count = 0;
                    char* p = players;
                    while ((p = strchr(p, '{')) != NULL) {
                        char* end = strchr(p, '}');
                        if (!end) break;
                        if (p < strstr(players, "]")) {
                            count++;
                            p = end + 1;
                        } else {
                            break;
                        }
                    }
                    printf("  - Jugadores encontrados: %d\n", count);
                }

                if (strstr(inicio, "\"crocodiles\":[")) {
                    char* crocs = strstr(inicio, "\"crocodiles\":[");
                    char* crocsEnd = strchr(crocs, ']');
                    int count = 0;
                    char* p = crocs;
                    while ((p = strchr(p, '{')) != NULL && p < crocsEnd) {
                        count++;
                        p = strchr(p, '}');
                        if (!p) break;
                        p++;
                    }
                    printf("  - Cocodrilos encontrados: %d\n", count);
                }

                if (strstr(inicio, "\"fruits\":[")) {
                    char* fruits = strstr(inicio, "\"fruits\":[");
                    char* fruitsEnd = strchr(fruits, ']');
                    int count = 0;
                    char* p = fruits;
                    while ((p = strchr(p, '{')) != NULL && p < fruitsEnd) {
                        count++;
                        p = strchr(p, '}');
                        if (!p) break;
                        p++;
                    }
                    printf("  - Frutas encontradas: %d\n", count);
                }
                printf("\n");
            }
            inicio = fin + 1;
        }
    }

    closesocket(sock);
    WSACleanup();
    return 0;
}
