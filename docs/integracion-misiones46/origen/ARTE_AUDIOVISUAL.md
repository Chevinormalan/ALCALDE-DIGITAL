# Arte de las misiones 4 y 6

Se usó la herramienta integrada de generación/edición de imágenes, siguiendo la guía imagegen. Los originales se conservan en referencias_documento/mejora_audiovisual/.

| Archivo del usuario | Formato y dimensiones | Uso |
|---|---|---|
| ejemplificacion de escenario alcalde.webp | WebP VP8, 1024 × 546 | Plaza vacía con decoración; personajes e interfaz se dibujan por separado. |
| Ejemplificacion_trayecto_carro.webp | WebP VP8, 1024 × 539 | Suelo, bordes, vegetación de la carretera y árbol violeta de los obstáculos existentes. |
| escena_ataque_alcalde.webp | WebP VP8, 1024 × 549 | Referencia del ataque y coherencia de calle; no se incrusta la captura ni su interfaz. |
| frames_alcalde.webp | WebP VP8, 1024 × 572 | Atlas limpio de 40 poses del mismo candidato y atlas complementario de acciones de 16 celdas. |

Los atlas PNG usan alfa real. JuanSprites recorta celdas, conserva la relación de aspecto y alinea los pies; las imágenes no determinan las colisiones. El renderizador prioriza ataque durante swordTime sin cambiar sus 0,16 segundos. El candidato del sofá se muestra de frente con las manos sobre las rodillas y la mesa en primer plano; durante el discurso de Misión 6 su dibujo permanece en la tarima. La plaza renovada también se usa en ese discurso, manteniendo la casa y sala modernas anteriores.

Recursos finales: entrega1-nueva/src/main/resources/cliente/presentacion/juan_atlas.png, juan_acciones.png; cliente/mision4/plaza_presentacion.png, camino_presentacion.png y arbol_violeta.png. Los recursos originales siguen presentes.

## juanAtlas

Edit this supplied sprite sheet by cleaning it for actual game use. Preserve the SAME 40 poses of the SAME silver-gray-haired man with thick gray moustache, navy suit, blue tie, white shirt, black shoes, friendly chibi pixel-art proportions. Preserve pose order: EXACT 10 equal columns by 4 equal rows. All cells identical size, each figure centered with consistent scale and foot baseline. Remove all flat gray and green cell backgrounds and replace with REAL alpha transparency, without removing any hair or clothing pixels. No checkerboard, no labels, no new characters, no text, no ground shadows. The ten third-row frames must remain the reference's walking cycle progressing from front to right profile. Retain all speaking, waving, pointing and open-hand gestures in their original cell order. This is background extraction and sprite cleanup, not character redesign. Full bodies fully within cells with 8% clear margins, output wide landscape 10 by 4 production atlas.

## juanAcciones

Create a production 2D pixel-art action sprite atlas for the EXACT man in input 1: silver-gray slicked hair, thick GRAY MOUSTACHE, NO beard, navy suit, blue tie, black shoes, matching the reference's pixel-art proportions. Input 2 shows the existing short sword attack. EXACT 4 columns by 4 rows, equal square cells, fixed foot baseline and same scale, generous padding, each whole figure and weapon within own cell. Row1 four FRONT-facing poses: sword attack extending blade, guard with small golden shield, recoiling when hit, seated on invisible sofa with knees bent. Row2 same four poses facing RIGHT. Row3 same four poses facing BACK. Row4 four WALKING BACK-facing poses making a seamless walking cycle. Keep attacks readable, not violent/gory; the game's existing golden arc will be separate. Do not add terrain, furniture, effects, words, labels or shadows. True transparent background. Preserve identity of the user's man, not the old black-haired bearded mayor.

## plazaVisual

Clean this exact 2D pixel-art game background into an EMPTY SCENERY layer. Preserve the exact map layout and proportions of the reference image, violet-magenta-gold palette and crisp pixel art. Remove every character, NPC, crowd, mayor and placard held by a person; reconstruct ground behind them seamlessly. Remove the entire top UI header and all text and UI overlays; reconstruct scene behind it. Keep stage/podium at upper left, stairs, tents, trees, fences, lights, balloons and decorations in their same coordinates. No new furniture or obstacles on the open ground. Output full-bleed 16:9 opaque background, matching the reference camera. Critical for existing collisions: stage footprint must be around x142..485, y55..295 on a 1024x576 frame, stairs around x212,y330; open crowd area right of stage remains empty for game NPCs. Preserve clear walking area through center and to the lower-right car parking. No characters baked into scenery, no words, no extra overlays.

## caminoVisual

Produce a SEAMLESS horizontally tileable 16:9 EMPTY scenery strip based faithfully on these two supplied 2D pixel-art game screenshots. Use their magenta-lilac cobblestone avenue, purple flowering trees, dark violet vegetation, thin gold/purple edging and crisp chibi-RPG pixel style. Exact layout for 1024x576 gameplay: upper greenery/tree band y0..148, straight wide FLAT OPEN cobblestone avenue y150..510, bottom greenery band y512..576. Keep BOTH left and right image edges tileable with continuous path and compatible greenery; don't place cut-off large trees straddling edges. Remove all characters, car, NPCs, UI, text panels, floating signs, health bars and attack effects. Do not place any objects or obstacles on the open central avenue: the game will draw collision-matched obstacles separately. Decorative trees remain entirely in the upper and lower borders. No signs, no benches on walkable path, no lamps on walkable path, no text or checkerboard. Opaque full bleed background. This is a road layer, not a whole screenshot.

## Árbol de los obstáculos

Extract ONE purple-magenta flowering tree from this pixel-art game reference as a clean reusable obstacle sprite. Preserve the reference's violet bark, rounded pink/purple canopy, crisp 2D game pixel-art and dark outline. Full tree from top canopy to trunk base, centered, no clipping, only a small root base, NO ground tile, no shadow oval, no UI, no text, no car, no other objects. Real transparent alpha background. Keep the exact reference style; this replaces green placeholder trees at existing collision positions, not a new obstacle.

