# 🚀 Instrucciones Simples - DonCEyKongJr

## 📦 Requisitos Previos

Necesitas tener instalado:

1. **Java JDK 21 o superior** ☕
   - Descarga: https://adoptium.net/
   - Verifica: `java -version`

2. **GCC (Compilador C)** 🔨
   - MinGW: https://www.mingw-w64.org/downloads/
   - TDM-GCC: https://jmeubank.github.io/tdm-gcc/
   - Verifica: `gcc --version`

---

## ✅ Verificar Instalación

Doble clic en:
```
check-versions.bat
```

Esto te dirá si tienes todo instalado correctamente.

---

## 🎮 Ejecutar el Proyecto

### Opción 1: Menú Principal (Recomendado)

1. **Doble clic** en `INICIAR.bat`

2. Verás un menú:
   ```
   [1] Servidor (Java)
   [2] Cliente (C)
   [3] Verificar Versiones
   [4] Compilar Todo
   [0] Salir
   ```

3. **Primera vez**: Presiona `4` para compilar todo

4. **Para ejecutar**:
   - Abre `INICIAR.bat` → Presiona `1` (Servidor) → Deja corriendo
   - Abre **otra ventana** de `INICIAR.bat` → Presiona `2` (Cliente)

---

### Opción 2: Scripts Directos

#### Terminal 1 - Servidor:
Doble clic en:
```
servidor-java\iniciar-servidor.bat
```
Deja esta ventana abierta.

#### Terminal 2 - Cliente:
Doble clic en:
```
cliente-c\src\iniciar-cliente.bat
```

---

## 💡 Funcionamiento

- **Servidor**: Escucha en el puerto 5000
- **Cliente**: Se conecta a `127.0.0.1:5000` (localhost)
- Todo corre en tu **misma computadora**

---

## ❌ Problemas Comunes

### "Java no está instalado"
→ Instala Java JDK 21+ desde https://adoptium.net/

### "GCC no está instalado"
→ Instala MinGW o TDM-GCC

### "Connection refused"
→ El servidor debe estar corriendo **ANTES** del cliente

### "Address already in use"
→ Ya hay un servidor corriendo. Ciérralo primero.

---

## 📝 Resumen de 3 Pasos

```
1️⃣ VERIFICAR
   check-versions.bat

2️⃣ COMPILAR (primera vez)
   INICIAR.bat → [4] Compilar Todo

3️⃣ EJECUTAR
   Terminal 1: INICIAR.bat → [1] Servidor
   Terminal 2: INICIAR.bat → [2] Cliente
```

---

**¡Listo para jugar! 🎮**
