# Recursos visuales de la Misión 6

Herramienta: generador de imágenes integrado, con reinterpretaciones originales. Los recursos se encuentran en entrega1-nueva/src/main/resources/cliente/mision6/.

- helicoptero.png: referencia image1.png del documento. Prompt: helicóptero de campaña de perfil hacia la derecha, cuerpo azul violeta, cabina dorada, cola y rotor visibles, acabado pixel art de juego narrativo; fondo transparente, sin texto.
- misil.png: referencia image2.png. Prompt: misil de perfil hacia la derecha con escape cálido, cuerpo violeta y detalles dorados, sprite pixel art y fondo transparente.
- impacto.png: referencia image2.png. Prompt: destello de impacto dorado y fragmentos de señal lila, sprite pixel art transparente que represente la ruptura de interferencia.
- casa.png: referencia image3.png. Prompt: fachada frontal original de residencia de campaña simétrica con columnas, escalinata, helipuerto, árboles y paleta violeta/dorada en pixel art.
- sala.png: referencia image4.png. Prompt: sala de campaña pixel art con alfombra, ventana, cortinas violetas, biblioteca, lámparas y escalinata, espacio central, sin personajes.
- cielo.png: referencia de estilo casa.png. Prompt: cielo de Ciudad Nova al atardecer con espacio despejado para juego, horizonte de edificios y ventanas iluminadas, pixel art violeta y dorado.

El documento fuente se conserva en el Escritorio. Sus referencias extraídas también están en referencias_documento/ dentro del proyecto. Los personajes se reutilizan del juego y no fueron generados de nuevo.

## Dron viral de esta entrega

Recurso: entrega1-nueva/src/main/resources/cliente/mision6/dron_atlas.png.
Referencia suministrada por el usuario: Gemini_Generated_Image_ko9ogcko9ogcko9o.jpg, guardada en el Escritorio. Se utilizó la herramienta integrada de generación/edición de imágenes, siguiendo la guía imagegen. Se conservaron el cuerpo oliva, las dos turbinas circulares, la cámara roja y las armas dobles. El renderizador usa cinco fotogramas por estado: vuelo, disparo y daño. Todos miran hacia el jugador; el atlas contiene transparencia alfa real. No se utiliza la cuadrícula impresa de la referencia como fondo.

Prompt utilizado:
Edit target: the provided drone animation sheet. Extract and clean the SAME olive metallic twin-ducted-rotor drone with red camera eye and two guns, preserving its identity and detailed game-sprite appearance. Create a production sprite atlas, EXACTLY 5 equal columns by 3 equal rows, all frames centered at identical scale, ample transparent margins within each cell. Row 1: five looping flying frames with rotor variation. Row 2: five firing frames, include muzzle flashes. Row 3: five damaged frames with sparks and a little smoke. Drone faces LEFT in every frame so its camera and weapons point left toward a player. Remove ALL checkerboard, all text and labels; output actual alpha transparency everywhere outside the fifteen sprites. No extra objects, no borders, no ground shadows, no labels. Crisp game sprite artwork, maintain olive body, red lens, dual circular rotor housings from reference. All sprites completely within their cells, no overlaps.

El generador entregó un PNG de 1619 × 971 píxeles. El juego obtiene las 15 celdas mediante división entera; los píxeles sobrantes quedan en los márgenes. Las pruebas comprueban contenido visible y fondo transparente en todas las celdas. El atlas se incluye dentro del JAR y del código fuente.

## Personajes sentados para la conferencia

Recurso: entrega1-nueva/src/main/resources/cliente/mision6/personajes_sentados.png. Herramienta: generador integrado de imágenes; guía imagegen. Referencias: mision4/front.png para Juan y mision3/redisenio/player_front_idle.png para el Influencer. La escena los dibuja sentados sobre el sofá real de la sala, sin tarjetas de personaje; durante la exploración reutiliza las animaciones direccionales originales.

Prompt utilizado:
Production 2D game sprite sheet, EXACTLY TWO equal square cells side by side in one horizontal row on actual alpha transparency. LEFT cell: Mayor Juan from image 1, preserve black/gray hair, beard, navy formal suit and tie, chibi pixel-art proportions. RIGHT cell: Influencer from image 2, preserve spiky violet hair, round glasses, purple and gold outfit, green brooch. Both are seated for an interview on an INVISIBLE sofa, bent hips and knees, visible shoes and relaxed hands on their knees, small inward turns toward one another. Keep exact recognizable character designs and clothing of references. Full seated bodies completely within respective cells, same character scale, centered each cell, generous clear margins, no overlap. Both have crisp illustrated pixel-art outlines suitable for the existing game. No chairs or sofa, no room, no panels, no names, no labels, no text, no floor, no shadows, no other subjects. Background truly transparent, not checkerboard.

Los corazones son iconos de píxel dibujados por el renderizador del juego, con el mismo símbolo en el mundo y en el indicador de vida.

## Actualización vigente de casa y sala
Los fondos casa.png y sala.png ahora se basan en CASA_MODERNA.webp y SALA_MODERNA.webp. Las referencias, los prompts exactos y las rutas se encuentran en ESCENARIOS_MODERNOS.md; las descripciones previas de esos dos fondos corresponden a la versión anterior.

