## Nombres alternativos soportados

Además de los nombres clásicos en esta carpeta, el cliente intenta cargar también estos nombres alternativos (si existen):

- `jr_frente.bmp` ⇄ `donkey_frente.bmp`
- `jr_subiendo.bmp` ⇄ `donkey_liana_subir.bmp`
- `jr_bajando.bmp` ⇄ `donkey_liana_subir.bmp` (reutilizado)
- `jr_izquierda.bmp` ⇄ `donkey_pasar_liana.bmp`
- `jr_derecha.bmp` ⇄ `donkey_pasar_liana.bmp`
- `jr_saltando.bmp` ⇄ `donkey_pasar_liana.bmp`
- `donkey.bmp` ⇄ `donkey_frente.bmp`
- `cocodrilo_rojo.bmp` ⇄ `cocodrilo_izq_rojo.bmp` (o `cocodrilo_abajo_rojo.bmp`)
- `cocodrilo_azul.bmp` ⇄ `cocodrilo_izq_azul.bmp` (o `cocodrilo_abajo_azul.bmp`)
- `banana.bmp` ⇄ `fruta_banana.bmp`

Notas:
- El motor carga archivos BMP (24‑bit, fondo negro como transparente).
- Si tienes sprites en PNG, conviértelos a BMP. Ya se generaron copias `.bmp` desde tus PNG con los nombres que el código espera.
