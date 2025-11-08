# Sprites para DonCEy Kong Jr

Esta carpeta contiene los sprites (imágenes) del juego.

## 📋 Sprites Requeridos

Coloca aquí tus imágenes en formato **BMP** (24-bit) con **FONDO NEGRO**:

### 🎬 Animaciones de Jr (6 sprites):

| Archivo | Descripción | Tamaño Recomendado |
|---------|-------------|-------------------|
| `jr_frente.bmp` | Jr parado de frente | 32x32 px |
| `jr_subiendo.bmp` | Jr subiendo liana | 32x32 px |
| `jr_bajando.bmp` | Jr bajando liana | 32x32 px |
| `jr_izquierda.bmp` | Jr moviéndose izquierda | 32x32 px |
| `jr_derecha.bmp` | Jr moviéndose derecha | 32x32 px |
| `jr_saltando.bmp` | Jr saltando/colgado | 32x32 px |

### 🎮 Otros Sprites:

| Archivo | Descripción | Tamaño Recomendado |
|---------|-------------|-------------------|
| `donkey.bmp` | Donkey Kong | 64x64 px |
| `cocodrilo_rojo.bmp` | Cocodrilo rojo | 48x32 px |
| `cocodrilo_azul.bmp` | Cocodrilo azul | 48x32 px |
| `banana.bmp` | Fruta (banana) | 24x24 px |
| `corazon.bmp` | Corazón para HUD | 20x20 px |

## 🎨 Cómo Crear los Sprites

### Opción 1: Paint (Windows)

1. Abre **Paint**
2. **Archivo → Nuevo** → Cambia tamaño a 32x32 (o el tamaño que necesites)
3. **Rellena el fondo con NEGRO** (color #000000)
4. Dibuja tu sprite sobre el fondo negro
5. **Fondo NEGRO = transparente** (importante!)
6. **Guardar como → BMP → BMP de 24 bits**

### Opción 2: GIMP (Gratis)

1. Abre GIMP
2. **Archivo → Crear → Imagen** (32x32)
3. **Rellena el fondo con negro** (color #000000)
4. Dibuja tu sprite sobre el fondo negro
5. **Archivo → Exportar como** → Guarda como `.bmp` (24-bit)

### Opción 3: Usar Pixel Art Online

Visita: https://www.pixilart.com/draw
- Dibuja tu sprite pixel por pixel
- Descarga como PNG
- Convierte a BMP con Paint (Abrir PNG → Guardar como BMP)

## 🖼️ Plantillas de Ejemplo

### Jr Frente (32x32)
```
🎨 FONDO NEGRO
Gorra roja arriba
Cara piel en medio
Cuerpo rojo
Brazos y piernas pequeños
```

### Jr Subiendo (32x32)
```
🎨 FONDO NEGRO
Gorra roja
Brazos extendidos hacia arriba
Piernas dobladas
```

### Cocodrilo (48x32)
```
🎨 FONDO NEGRO
Cuerpo rojo/azul alargado
Boca abierta con dientes
Ojos amarillos
Cola puntiaguda
```

### Banana (24x24)
```
🎨 FONDO NEGRO
Forma curva amarilla
Puntas verdes/marrones
```

## 🔧 Cómo Funciona

1. El juego **intenta cargar** los sprites de esta carpeta
2. Si **encuentra** un sprite → Lo usa
3. Si **NO encuentra** → Usa gráficos dibujados (fallback)

Por ejemplo:
- Si existe `jr.bmp` → Usa el sprite
- Si NO existe `jr.bmp` → Dibuja Jr con formas GDI

**¡No te preocupes si no tienes todos los sprites!** El juego funciona igual.

## 🎯 Consejos de Diseño

### Transparencia
- **Fondo NEGRO puro (RGB 0,0,0)** será transparente
- Todo lo que NO sea negro puro se verá
- Usa colores brillantes para que contrasten con el fondo oscuro del juego

### Tamaños
- Los sprites se **escalarán automáticamente** al tamaño del juego
- Puedes usar tamaños más grandes (ej: 64x64) para más detalle

### Paleta de Colores
- **Jr**: Rojo (#DC3232), Piel (#FFC896)
- **Cocodrilo Rojo**: Rojo-naranja (#FF5014)
- **Cocodrilo Azul**: Azul (#3296FF)
- **Banana**: Amarillo (#FFDC32)
- **Donkey Kong**: Marrón (#654321)

## 📦 Sprites de Ejemplo

Si quieres sprites de prueba, crea archivos BMP simples en Paint:

**jr_frente.bmp (ejemplo rápido):**
1. Paint → 32x32
2. **Rellena todo con NEGRO**
3. Dibuja círculo piel (cabeza)
4. Rectángulo rojo arriba (gorra)
5. Rectángulo rojo abajo (cuerpo)
6. Guardar como BMP (24-bit)

## 🚀 Uso en el Código

Los sprites se cargan automáticamente en [main.c](../src/main.c):

```c
// Al iniciar el juego
cargarSprites();

// Al dibujar
if (sprite_jr.bitmap) {
    // Usa sprite
    dibujarSpriteEscalado(hdc, &sprite_jr, x, y, 40, 40);
} else {
    // Fallback: dibuja con GDI
    DibujarJugadorMejorado(hdc, jugador);
}

// Al cerrar
liberarSprites();
```

## 🎨 Recursos Útiles

- **Sprites de Donkey Kong original**: https://www.spriters-resource.com/arcade/dk/
- **Pixel Art Tutorial**: https://lospec.com/pixel-art-tutorials
- **Paleta de colores**: https://coolors.co/

## 📝 Notas

- Los sprites deben estar en **BMP de 24-bit** (no 8-bit ni 32-bit)
- El **negro puro (RGB 0,0,0)** es transparente
- Si quieres cambiar el color transparente, edita `sprites.c` líneas con `RGB(0, 0, 0)` a otro color
- **Animaciones de Jr**: El juego elige automáticamente el sprite correcto según el movimiento:
  - Subiendo → usa `jr_subiendo.bmp`
  - Bajando → usa `jr_bajando.bmp`
  - Izquierda → usa `jr_izquierda.bmp`
  - Derecha → usa `jr_derecha.bmp`
  - Saltando → usa `jr_saltando.bmp`
  - Parado → usa `jr_frente.bmp`