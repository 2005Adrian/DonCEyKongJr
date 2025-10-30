🧠 GUÍA PARA EJECUTAR EL SERVIDOR Y CONECTARSE DESDE C
Proyecto: DonCEy Kong Jr – Comunicación cliente-servidor
📁 Estructura general del proyecto
Proyecto3/
│
├── servidor-java/             # Servidor (backend) en Java (Adrián)
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/
│       └── main/java/cr/tec/donceykongjr/server/
│           ├── Main.java
│           ├── network/
│           │   ├── ServidorJuego.java
│           │   └── ManejadorCliente.java
│           └── logic/
│               ├── Juego.java
│               └── entidades/...
│
└── cliente-c/                 # Cliente (frontend) en C (José Pablo y Daniel)
    └── src/
        └── cliente_prueba.c

⚙️ 1️⃣ Requisitos de instalación
🧩 Para el servidor (Java)

Tener instalado Java JDK 21 o superior
(en este proyecto se usa OpenJDK 25 Temurin)

Tener instalado Gradle 8+

Usar un editor compatible, como VS Code o IntelliJ

🧩 Para el cliente (C)

Tener instalado un compilador C para Windows, como:

MinGW

TDM-GCC

O ejecutar desde WSL/Linux

En Windows, el cliente usa la librería Winsock2 (-lws2_32).

🖥️ 2️⃣ Cómo ejecutar el servidor

1️⃣ Abrir una ventana PowerShell o terminal en la carpeta del servidor:

cd "C:\Users\<usuario>\Documentos\.Proyecto3\servidor-java"


2️⃣ Ejecutar el servidor con Gradle:

gradle run


3️⃣ Si todo está correcto, verán:

🚀 Iniciando servidor DonCEy Kong Jr...
Servidor iniciado en el puerto 5000


4️⃣ Dejar esa ventana abierta (el servidor debe seguir corriendo).
El servidor estará escuchando conexiones en el puerto 5000.

💻 3️⃣ Cómo compilar y ejecutar el cliente en C

1️⃣ Abrir otra ventana PowerShell y entrar a la carpeta del cliente:

cd "C:\Users\<usuario>\Documentos\.Proyecto3\cliente-c\src"


2️⃣ Compilar el cliente de prueba:

gcc cliente_prueba.c -o cliente_prueba.exe -lws2_32


3️⃣ Ejecutar el cliente:

.\cliente_prueba.exe


4️⃣ Si el servidor está corriendo, verán algo así:

Conectado al servidor DonCEy Kong Jr en 127.0.0.1:5000
Servidor: Conectado al servidor DonCEy Kong Jr!
Mensaje >


5️⃣ Escribir un mensaje (por ejemplo hola)
y el servidor responderá:

Respuesta: Eco: hola


6️⃣ Para cerrar la conexión, escribir:

salir

🔗 4️⃣ Cómo crear su propio cliente (C real del juego)

José Pablo y Daniel pueden partir del cliente_prueba.c y extenderlo:

🎮 En su cliente deberán implementar:

Conexión automática al servidor Java (127.0.0.1:5000 o IP de red)

Envío periódico de datos (posición del jugador, acciones, colisiones)

Recepción de eventos del servidor (movimiento de enemigos, frutas, puntaje)

Interfaz visual o textual para representar el estado del juego

Sistema de entrada de teclado para controlar al jugador

🧠 5️⃣ Cómo probar la conexión en grupo
Participante	Rol	Qué hace
Adrián	Backend (Java)	Corre el servidor (gradle run)
José Pablo	Cliente en C	Conecta al servidor y envía datos
Daniel	Cliente en C	Prueba la comunicación y lógica de juego

💡 Si están en la misma red:

Usar la IP de la máquina donde corre el servidor
(reemplazar "127.0.0.1" por la IP local del servidor)

Asegurarse de que el puerto 5000 esté abierto en el firewall

🧩 6️⃣ Verificación de funcionamiento

Servidor (Java):

🚀 Iniciando servidor DonCEy Kong Jr...
Servidor iniciado en el puerto 5000
✅ Cliente conectado desde /127.0.0.1
📩 Cliente dice: hola
📤 Respondiendo: Eco: hola


Cliente (C):

Conectado al servidor DonCEy Kong Jr en 127.0.0.1:5000
Servidor: Conectado al servidor DonCEy Kong Jr!
Mensaje > hola
Respuesta: Eco: hola

🧱 7️⃣ Posibles errores comunes
Problema	Causa	Solución
Address already in use: bind	El servidor anterior sigue ejecutándose	Cerrar el proceso Java o reiniciar el puerto
Connection refused	El servidor no está corriendo	Ejecutar gradle run antes del cliente
Cliente no recibe respuesta	Falta de \n al final del mensaje	Asegurarse de tener strcat(buffer, "\n");
Símbolos raros (Ôéà)	Codificación UTF-8 con emojis	Quitar emojis o cambiar fuente en PowerShell
✅ En resumen
Componente	Lenguaje	Estado	Responsable
Servidor (backend del juego)	Java	✅ Listo y probado	Adrián
Cliente de prueba (socket base)	C	✅ Funcional	Adrián
Cliente real (interfaz del jugador)	C	🔜 Por desarrollar	José Pablo & Daniel
