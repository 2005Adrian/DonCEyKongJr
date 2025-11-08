# Sprites para DonCEy Kong Jr

Esta carpeta contiene los sprites (imágenes) del juego.

## 📋 Sprites Requeridos

Coloca aquí tus imágenes en formato **BMP** (24-bit):

| Archivo | Descripción | Tamaño Recomendado |
|---------|-------------|-------------------|
| `jr.bmp` | Jr con gorra roja | 32x32 px |
| `donkey.bmp` | Donkey Kong | 64x64 px |
| `cocodrilo_rojo.bmp` | Cocodrilo rojo | 48x32 px |
| `cocodrilo_azul.bmp` | Cocodrilo azul | 48x32 px |
| `banana.bmp` | Fruta (banana) | 24x24 px |
| `corazon.bmp` | Corazón para HUD | 20x20 px |

## 🎨 Cómo Crear los Sprites

### Opción 1: Paint (Windows)

1. Abre **Paint**
2. **Archivo → Nuevo** → Cambia tamaño a 32x32 (o el tamaño que necesites)
3. Dibuja tu sprite
4. **Fondo blanco = transparente** (importante!)
5. **Guardar como → BMP → BMP de 24 bits**

### Opción 2: GIMP (Gratis)

1. Abre GIMP
2. **Archivo → Crear → Imagen** (32x32)
3. Dibuja tu sprite
4. **Capa → Transparencia → Color a Alfa** (elige blanco)
5. **Archivo → Exportar como** → Guarda como `.bmp`

### Opción 3: Usar Pixel Art Online

Visita: https://www.pixilart.com/draw
- Dibuja tu sprite pixel por pixel
- Descarga como PNG
- Convierte a BMP con Paint (Abrir PNG → Guardar como BMP)

## 🖼️ Plantillas de Ejemplo

### Jr (32x32)
```
Gorra roja arriba
Cara piel en medio
Cuerpo rojo
Brazos y piernas pequeños
Fondo blanco
```

### Cocodrilo (48x32)
```
Cuerpo rojo/azul alargado
Boca abierta con dientes
Ojos amarillos
Cola puntiaguda
Fondo blanco
```

### Banana (24x24)
```
Forma curva amarilla
Puntas verdes/marrones
Fondo blanco
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
- **Fondo BLANCO** será transparente
- Todo lo que NO sea blanco puro se verá

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

**jr.bmp (ejemplo rápido):**
1. Paint → 32x32
2. Dibuja círculo piel (cabeza)
3. Rectángulo rojo arriba (gorra)
4. Rectángulo rojo abajo (cuerpo)
5. Guardar como BMP

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
- El blanco puro (RGB 255,255,255) es transparente
- Si quieres cambiar el color transparente, edita `sprites.c` línea con `RGB(255, 255, 255)`