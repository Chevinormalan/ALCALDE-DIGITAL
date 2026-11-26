# Gráficos y reescalado

Acceso: **Menú principal → Cómo jugar → Gráficos**. Se conserva la paleta morada/lila, los marcos y la tipografía del juego. El botón nuevo tiene su propia fila junto a Ver controles, manteniendo los accesos de accesibilidad.

## Uso

- **Modo de pantalla:** ventana o pantalla completa sin bordes en el monitor donde se encuentra el juego.
- **Resolución:** recorre los tamaños de ventana que caben en el área útil del monitor y termina con la resolución del propio monitor. Aplica esa última opción para ocupar toda la pantalla sin bordes. F11 recupera la ventana anterior. Si eliges un tamaño de ventana mientras estás en pantalla completa, se usa al volver a ventana.
- **Ampliación:** ajustar proporcionalmente para aprovechar el espacio, o escalado entero para mantener píxeles uniformes. El escalado entero puede dejar márgenes mayores; en ventanas menores que 1280 × 720 se reduce para conservar toda la imagen.
- **Filtro:** al ampliar, Nítido usa vecino más cercano y Suave usa interpolación bilineal. Al reducir, ambos suavizan la imagen para mantener legibles los trazos delgados.
- **Aplicar:** activa y guarda la selección. **Restaurar valores** prepara la configuración original y se confirma con Aplicar. Volver abandona las ediciones pendientes al abrir de nuevo el panel.
- **F11:** alterna pantalla completa desde el menú o durante una misión. Una pulsación mantenida cambia el modo una sola vez. Al volver a ventana, se recuperan el tamaño y la posición anteriores si el preajuste sigue siendo el mismo y el monitor sigue conectado. Las reglas habituales de pausa por pérdida de foco siguen protegiendo la partida durante los cambios de ventana.

La ventana también puede redimensionarse libremente. Al reiniciar se recupera el tamaño elegido en Gráficos. Las preferencias se guardan en `graficos.properties`, dentro del perfil local; no forman parte de los snapshots de las misiones ni se transmiten a los compañeros de una sala LAN. Se usa la escritura atómica del proyecto y se conservan las partidas.

## Proporciones e imagen

El contenido se dibuja siempre en **1280 × 720** y se presenta una sola vez en la ventana real. Las ocho misiones, el menú, la pausa, las consultas y los controles comparten este proceso. La misión 3 conserva su transformación interna hacia ese lienzo; el reescalado final no la duplica.

| Pantalla | Imagen con ajuste proporcional | Márgenes |
|---|---|---|
| 1920 × 1080 | 1920 × 1080 | Ninguno |
| 2560 × 1440 | 2560 × 1440 | Ninguno |
| 3840 × 2160 | 3840 × 2160 | Ninguno |
| 1920 × 1200 | 1920 × 1080 | 60 arriba y abajo |
| 3440 × 1440 | 2560 × 1440 | 440 a cada lado |

Los márgenes usan un fondo decorativo morado. Se mantiene la imagen completa y sus proporciones. Ampliar los recursos conserva su detalle original; no produce arte nuevo de mayor resolución.

El panel Gráficos muestra el tamaño disponible para dibujar y el tamaño de la imagen después del reescalado. El ratón usa el mismo rectángulo que la imagen presentada. Esto incluye el resaltado de botones, los clics de menú y pausa, y el apuntado de la misión 3. Los clics sobre los márgenes se ignoran. Teclado y mando conservan sus controles; Gráficos admite navegación y retorno con ambos.

## Implementación y comprobación

- `EscaladoPantalla`: rectángulo de presentación y conversión del ratón.
- `LienzoJuego`: contexto Processing independiente de 1280 × 720 y presentación con filtro. Usa las dimensiones del búfer activo y espera el siguiente fotograma si un redimensionado dejó pendientes las nuevas coordenadas del ratón.
- `VentanaJuego`: tamaño y decoraciones mediante AWT, detectando el monitor actual. No cambia la resolución del sistema operativo. Refresca la información al mover o redimensionar la ventana.
- `OpcionesGraficas`: selección editable, persistencia local y recuperación ante preferencias inválidas.

**267 pruebas aprobadas**, cero fallos, errores u omitidas. Se añadieron 13 pruebas de gráficos y se verificaron nueve casos de ventana real: el panel y las ocho misiones.

Las pruebas unitarias comprueban escalas, proporciones, esquinas, márgenes, reducción, filtros, salida real a un búfer 4K, persistencia, recuperación y navegación. Dos regresiones de pausa de misiones 2 y 3 ahora usan coordenadas físicas de una ventana 960 × 600.

La revisión gráfica usa el `ClienteSketch` real y perfiles temporales. Verifica Cómo jugar → Gráficos mediante clic reescalado, aplicación de pantalla completa, F11 y su repetición, retorno a ventana y menú. Recorre las ocho misiones a 960 × 600 y en pantalla completa; abre pausa y comprueba su reanudación mediante clic reescalado. En misión 3 también comprueba las coordenadas de apuntado.

Reproducción: `python3 tools/verificar_graficos.py`, después de `test-compile`. Las comprobaciones de ventana real se realizaron en un monitor de **1920 × 1080**. 1440p, 4K y otras proporciones se verificaron mediante pruebas de transformación y renderizado, sin disponer de todos esos monitores físicos. Mando físico y configuraciones de varios monitores requieren revisión adicional en esos equipos.

Evidencias: [Cómo jugar](evidencias/como-jugar.png), [Gráficos en ventana](evidencias/graficos-960x600.png), [pantalla completa](evidencias/graficos-completa.png), [retorno a ventana](evidencias/graficos-restaurada.png), [pausa de misión 8](evidencias/mision8-pausa.png). Resultados y hash del paquete: `evidencias/validacion.json`; registro de compilación y pruebas: `evidencias/compilacion.log`.

Respaldo previo a la implementación: `../Respaldos y ProyectsAnterirores/antes-graficos-20261116/cliente.tgz`, relativo a la raíz del proyecto. El trabajo se realizó en `entrega1-nueva`; la carpeta original se conserva.
