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

// Prototipo del procedimiento de ventana
LRESULT CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);

/**
 * Punto de entrada principal
 */
int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    WNDCLASSEX wc;
    MSG msg;

    // Generar ID único para el jugador
    srand(time(NULL));
    sprintf(g_miPlayerId, "Player_%d", rand() % 10000);

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

    // Crear ventana
    g_hwnd = CreateWindowEx(
        WS_EX_CLIENTEDGE,
        "DonCEyKongJrWindow",
        "DonCEy Kong Jr - El Juego Completo",
        WS_OVERLAPPEDWINDOW & ~WS_THICKFRAME & ~WS_MAXIMIZEBOX,
        CW_USEDEFAULT, CW_USEDEFAULT,
        WINDOW_WIDTH, WINDOW_HEIGHT,
        NULL, NULL, hInstance, NULL
    );

    if (g_hwnd == NULL) {
        MessageBox(NULL, "Error al crear la ventana", "Error", MB_ICONEXCLAMATION | MB_OK);
        return 0;
    }

    ShowWindow(g_hwnd, nCmdShow);
    UpdateWindow(g_hwnd);

    // Conectar al servidor
    if (conectarServidor()) {
        g_conectado = 1;
        g_estadoPantalla = ESTADO_JUGANDO;
        char* msgConexion = crearMensajeConexion();
        enviarMensaje(msgConexion);
        free(msgConexion);

        CreateThread(NULL, 0, ThreadRed, NULL, 0, NULL);
        SetTimer(g_hwnd, 1, 33, NULL); // 30 FPS
    } else {
        g_estadoPantalla = ESTADO_DESCONECTADO;
        SetTimer(g_hwnd, 1, 100, NULL);
    }

    // Loop principal
    while (GetMessage(&msg, NULL, 0, 0) > 0) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

    desconectarServidor();
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
            g_animacionFrame++;
            if (g_efectoGolpe > 0) g_efectoGolpe--;
            if (g_efectoFruta > 0) g_efectoFruta--;
            InvalidateRect(hwnd, NULL, FALSE);
            break;

        case WM_KEYDOWN:
            if (g_estadoPantalla == ESTADO_TITULO) {
                g_estadoPantalla = ESTADO_JUGANDO;
            } else if (g_estadoPantalla == ESTADO_JUGANDO) {
                procesarTecla(wParam);
            }
            break;

        case WM_CLOSE:
            g_juegoActivo = 0;
            DestroyWindow(hwnd);
            break;

        case WM_DESTROY:
            PostQuitMessage(0);
            break;

        default:
            return DefWindowProc(hwnd, msg, wParam, lParam);
    }
    return 0;
}
