#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>  // Biblioteca principal de sockets en Windows
#include <ws2tcpip.h>  // Funciones extra para IPv4/IPv6

#pragma comment(lib, "ws2_32.lib") // Enlaza automáticamente la librería Winsock

#define SERVER_IP "127.0.0.1"
#define SERVER_PORT 5000
#define BUFFER_SIZE 1024

int main() {
    WSADATA wsaData;
    SOCKET sock;
    struct sockaddr_in server_addr;
    char buffer[BUFFER_SIZE];

    // Inicializar Winsock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        printf("❌ Error al iniciar Winsock.\n");
        return 1;
    }

    // Crear socket
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock == INVALID_SOCKET) {
        printf("❌ Error al crear el socket: %d\n", WSAGetLastError());
        WSACleanup();
        return 1;
    }

    // Configurar dirección del servidor
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(SERVER_PORT);
    server_addr.sin_addr.s_addr = inet_addr(SERVER_IP);

    // Conectar al servidor
    if (connect(sock, (struct sockaddr*)&server_addr, sizeof(server_addr)) == SOCKET_ERROR) {
        printf("❌ Error al conectar con el servidor: %d\n", WSAGetLastError());
        closesocket(sock);
        WSACleanup();
        return 1;
    }

    printf("✅ Conectado al servidor DonCEy Kong Jr en %s:%d\n", SERVER_IP, SERVER_PORT);

    // Recibir mensaje inicial
    int len = recv(sock, buffer, sizeof(buffer) - 1, 0);
    if (len > 0) {
        buffer[len] = '\0';
        printf("Servidor: %s\n", buffer);
    }

    // Bucle de envío y recepción
    while (1) {
        printf("Mensaje > ");
        fflush(stdout);

        if (fgets(buffer, sizeof(buffer), stdin) == NULL)
            break;

        buffer[strcspn(buffer, "\n")] = '\0'; // quita el salto de línea de fgets
        if (strcmp(buffer, "salir") == 0)
            break;

        strcat(buffer, "\n"); // 🔹 agrega un salto de línea para el servidor Java

        // Enviar mensaje
        send(sock, buffer, strlen(buffer), 0);

        // Recibir respuesta
        int len = recv(sock, buffer, sizeof(buffer) - 1, 0);
        if (len > 0) {
            buffer[len] = '\0';
            printf("Respuesta: %s\n", buffer);
        } else {
            printf("❌ Error o conexión cerrada.\n");
            break;
        }
    }


    closesocket(sock);
    WSACleanup();
    printf("❌ Cliente cerrado.\n");
    return 0;
}
