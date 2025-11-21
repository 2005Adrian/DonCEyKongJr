#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#include <time.h>
#include "game.h"
#include "network.h"
#include "render.h"
#include "input.h"
#include "sprites.h"
#include "log_utils.h"

// Prototipo del procedimiento de ventana
LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);
static LONG WINAPI ClientUnhandledException(EXCEPTION_POINTERS* info) {
    client_log("Unhandled exception: 0x%08lX", info ? info->ExceptionRecord->ExceptionCode : 0);
    return EXCEPTION_EXECUTE_HANDLER;
}

/**
 * Punto de entrada principal
 */
int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    WNDCLASSEX wc;
    MSG msg;

    client_log("WinMain iniciado");
    SetUnhandledExceptionFilter(ClientUnhandledException);

    // Detectar modo espectador desde argumentos de línea de comandos
    if (lpCmdLine != NULL && strstr(lpCmdLine, "--spectator") != NULL) {
        strcpy(g_tipoCliente, CLIENT_TYPE_SPECTATOR);
        client_log("Modo ESPECTADOR activado");
    } else {
        strcpy(g_tipoCliente, CLIENT_TYPE_PLAYER);
        client_log("Modo JUGADOR activado");
    }

    // Detectar puerto desde argumentos (--port XXXX)
    if (lpCmdLine != NULL) {
        char* portArg = strstr(lpCmdLine, "--port");
        if (portArg != NULL) {
            int puerto = atoi(portArg + 7);
            if (puerto > 0 && puerto < 65536) {
                g_serverPort = puerto;
                client_log("Puerto configurado: %d", g_serverPort);
            }
        }
    }

    // Generar ID único para el cliente
    srand(time(NULL));
    if (strcmp(g_tipoCliente, CLIENT_TYPE_SPECTATOR) == 0) {
        sprintf(g_miPlayerId, "Spectator_%d", rand() % 10000);
    } else {
        sprintf(g_miPlayerId, "Player_%d", rand() % 10000);
    }

    // Inicializar estado del juego
    memset(&g_estadoActual, 0, sizeof(EstadoActual));

    // Registrar clase de ventana
    wc.cbSize = sizeof(WNDCLASSEX);
    wc.style = 0;
    wc.lpfnWndProc = WndProc;
    wc.cbClsExtra = 0;
    wc.cbWndExtra = 0;
    wc.hInstance = hInstance;
    wc.hIcon = LoadIcon(NULL, IDI_APPLICATION);
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wc.lpszMenuName = NULL;
    wc.lpszClassName = "DonCEyKongJrWindow";
    wc.hIconSm = LoadIcon(NULL, IDI_APPLICATION);

    if (!RegisterClassEx(&wc)) {
        MessageBox(NULL, "Error al registrar la ventana", "Error", MB_ICONEXCLAMATION | MB_OK);
        return 0;
    }

    // Crear ventana con título según el modo
    char tituloVentana[128];
    if (strcmp(g_tipoCliente, CLIENT_TYPE_SPECTATOR) == 0) {
        sprintf(tituloVentana, "DonCEy Kong Jr - ESPECTADOR");
    } else {
        sprintf(tituloVentana, "DonCEy Kong Jr - JUGADOR");
    }

    g_hwnd = CreateWindowEx(
        WS_EX_CLIENTEDGE,
        "DonCEyKongJrWindow",
        tituloVentana,
        WS_OVERLAPPEDWINDOW & ~WS_THICKFRAME & ~WS_MAXIMIZEBOX,
        CW_USEDEFAULT, CW_USEDEFAULT,
        WINDOW_WIDTH, WINDOW_HEIGHT,
        NULL, NULL, hInstance, NULL
    );

    if (g_hwnd == NULL) {
        MessageBox(NULL, "Error al crear la ventana", "Error", MB_ICONEXCLAMATION | MB_OK);
        return 0;
    }

    // Inicializar sistema de input ANTES de mostrar ventana (evita race condition)
    inicializarInput();

    // Cargar sprites (si existen)
    printf("\n");
    cargarSprites();
    printf("\n");

    ShowWindow(g_hwnd, nCmdShow);
    UpdateWindow(g_hwnd);

    // Conectar al servidor
    if (conectarServidor()) {
        g_conectado = 1;
        g_estadoPantalla = ESTADO_JUGANDO;
        char* msgConexion = crearMensajeConexion();
        enviarMensaje(msgConexion);
        free(msgConexion);

        g_threadRed = CreateThread(NULL, 0, ThreadRed, NULL, 0, NULL);
        SetTimer(g_hwnd, 1, 33, NULL); // Timer 1: 30 FPS rendering
        SetTimer(g_hwnd, 2, 40, NULL); // Timer 2: 25 Hz input (más rápido que server 20 TPS para no perder inputs)
        client_log("Conexion establecida, juego iniciado");
    } else {
        g_estadoPantalla = ESTADO_DESCONECTADO;
        SetTimer(g_hwnd, 1, 100, NULL);
        client_log("No se pudo conectar al servidor");
    }

    // Loop principal
    while (GetMessage(&msg, NULL, 0, 0) > 0) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

    // Limpiar recursos
    // Esperar a que el thread de red termine (timeout de 2 segundos)
    if (g_threadRed != NULL) {
        WaitForSingleObject(g_threadRed, 2000);
        CloseHandle(g_threadRed);
        g_threadRed = NULL;
        client_log("Thread de red cerrado");
    }

    desconectarServidor();
    limpiarRecursosRed();
    limpiarRecursosInput();
    liberarSprites();

    client_log("WinMain finalizado");
    return msg.wParam;
}

/**
 * Procedimiento de ventana
 */
LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    switch (msg) {
        case WM_PAINT: {
            PAINTSTRUCT ps;
            HDC hdc = BeginPaint(hwnd, &ps);

            // Crear buffer para double buffering
            HDC hdcMem = CreateCompatibleDC(hdc);
            HBITMAP hbmMem = CreateCompatibleBitmap(hdc, WINDOW_WIDTH, WINDOW_HEIGHT);
            HBITMAP hbmOld = (HBITMAP)SelectObject(hdcMem, hbmMem);

            // Limpiar con color de fondo
            HBRUSH hBrushBg = CreateSolidBrush(COLOR_BG);
            RECT rect = {0, 0, WINDOW_WIDTH, WINDOW_HEIGHT};
            FillRect(hdcMem, &rect, hBrushBg);
            DeleteObject(hBrushBg);

            // Proteger lectura de g_estadoActual con mutex
            bloquearEstado();

            // Dibujar según estado
            switch (g_estadoPantalla) {
                case ESTADO_TITULO:
                    DibujarPantallaTitulo(hdcMem);
                    break;
                case ESTADO_JUGANDO:
                    DibujarEscenario(hdcMem);
                    DibujarEfectos(hdcMem);
                    DibujarHUDVisual(hdcMem);
                    break;
                case ESTADO_GAME_OVER:
                    DibujarEscenario(hdcMem);
                    DibujarPantallaGameOver(hdcMem);
                    break;
                case ESTADO_VICTORIA:
                    DibujarEscenario(hdcMem);
                    DibujarPantallaVictoria(hdcMem);
                    break;
                case ESTADO_DESCONECTADO:
                    SetBkMode(hdcMem, TRANSPARENT);
                    SetTextColor(hdcMem, RGB(255, 0, 0));
                    HFONT hFont = CreateFont(32, 0, 0, 0, FW_BOLD, 0, 0, 0, DEFAULT_CHARSET,
                                              OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS, DEFAULT_QUALITY,
                                              DEFAULT_PITCH | FF_SWISS, "Arial");
                    SelectObject(hdcMem, hFont);
                    RECT textRect = {0, WINDOW_HEIGHT / 2, WINDOW_WIDTH, WINDOW_HEIGHT / 2 + 50};
                    DrawText(hdcMem, "DESCONECTADO DEL SERVIDOR", -1, &textRect,
                             DT_CENTER | DT_VCENTER | DT_SINGLELINE);
                    DeleteObject(hFont);
                    break;
            }

            // Liberar mutex después del renderizado
            desbloquearEstado();

            // Copiar buffer a pantalla
            BitBlt(hdc, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, hdcMem, 0, 0, SRCCOPY);

            // Limpiar
            SelectObject(hdcMem, hbmOld);
            DeleteObject(hbmMem);
            DeleteDC(hdcMem);

            EndPaint(hwnd, &ps);
            return 0;
        }

        case WM_TIMER:
            if (wParam == 1) {
                // Timer 1: Renderizado a 30 FPS
                g_animacionFrame++;
                if (g_efectoGolpe > 0) g_efectoGolpe--;
                if (g_efectoFruta > 0) g_efectoFruta--;
                InvalidateRect(hwnd, NULL, FALSE);
            } else if (wParam == 2) {
                // Timer 2: Input polling a 25 Hz (más rápido que server 20 TPS para no perder inputs)
                if (g_estadoPantalla == ESTADO_JUGANDO && g_conectado) {
                    procesarInputsAcumulados();
                }
            }
            break;

        case WM_KEYDOWN:
            if (g_estadoPantalla == ESTADO_TITULO) {
                g_estadoPantalla = ESTADO_JUGANDO;
            } else if (g_estadoPantalla == ESTADO_JUGANDO) {
                marcarTeclaPresionada(wParam);
            }
            break;

        case WM_KEYUP:
            if (g_estadoPantalla == ESTADO_JUGANDO) {
                marcarTeclaSoltada(wParam);
            }
            break;

        case WM_CLOSE:
            g_juegoActivo = 0;
            client_log("WM_CLOSE recibido");
            // Matar timers antes de destruir ventana
            KillTimer(hwnd, 1);
            KillTimer(hwnd, 2);
            DestroyWindow(hwnd);
            break;

        case WM_DESTROY:
            client_log("WM_DESTROY recibido");
            PostQuitMessage(0);
            break;

        default:
            return DefWindowProc(hwnd, msg, wParam, lParam);
    }
    return 0;
}
