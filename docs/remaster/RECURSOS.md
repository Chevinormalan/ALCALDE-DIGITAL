# Recursos incorporados

| Recurso | Procedencia y uso |
|---|---|
| [Sofi recoge el expediente](../../src/main/resources/cliente/arte/mision1/sofi-recogida.png) | Seis poses nuevas del personaje, producidas con ImageGen integrado usando el [atlas de caminata del juego](../../src/main/resources/cliente/arte/mision1/sofi-caminar.png) como referencia. Se corrigieron el pivote de cada dibujo y el alfa residual. La [hoja fuente](../../tools/arte/referencias-remaster/sofi-recogida.png) conserva las celdas originales de 512 × 512. |
| [Fuselaje](../../src/main/resources/cliente/mision6/helicoptero-cuerpo.png) y [rotor](../../src/main/resources/cliente/mision6/helicoptero-rotor.png) | Fuselaje del ZIP con las aspas fijas retiradas. Doce fotogramas originales de las aspas en perspectiva, con un anclaje común al buje. |
| [Explosión](../../src/main/resources/cliente/mision6/explosion-remaster.png) | Diez cuadros originales de núcleo, fragmentos y disipación. Los impactos del dron y la autodestrucción usan los eventos que ya expone la misión. |
| [Hallazgo](../../src/main/resources/cliente/audio/fx/lupa-hallazgo.wav) | Timbre PCM mono a 22.050 Hz de 310 ms, sintetizado para la confirmación de evidencia. Se entrega mediante el mezclador y el volumen de interfaz del juego. |
| Lupa, papel e iluminación | Composición Java2D. El marco de la lupa y las capas de iluminación se precalculan en memoria. El documento se amplía a partir de su dibujo en el lienzo y la página conserva sus coordenadas de interacción. |

Las fuentes de preparación están en [ArteRemaster.cs](../../tools/arte/ArteRemaster.cs) y [preparar_remaster.ps1](../../tools/arte/preparar_remaster.ps1). La distribución incluye los PNG y WAV listos para usar.

## Hojas de personajes y sonidos de la edición empaquetada

ImageGen integrado produjo hojas de referencia para Sofi, Periodista, Influencer, Juan, Palomo y los tres rivales. Se conservaron las referencias del juego como modelos de identidad. La preparación recorta cada pose con su anclaje y quita el alfa residual de alrededor de la silueta. [Fuentes de las hojas](../../tools/arte/referencias-remaster/) · [Atlas de ejecución](../../src/main/resources/cliente/presentacion/remaster/) · [Preparación de arte](../../tools/arte/ArteRemaster.cs)

| Hoja | Poses activas |
|---|---|
| Sofi | 8 pasos laterales, además de los 6 dibujos de recogida |
| Periodista | 8 cuadros de carrera, con contacto, apoyo, paso y vuelo |
| Influencer | 4 cuadros de carrera, alcance con teléfono, daño, esquiva y postura de agotamiento |
| Juan | 8 cuadros de preparación, corte y vuelta a la guardia |
| Palomo | 8 aleteos que recorren las posiciones alta, intermedia y baja |
| Troll, SpamBot y Deepfake | 4 dibujos de preparación, ataque, recobro y daño por rival |

Se sintetizaron 32 efectos PCM adicionales a 22.050 Hz. Cubren los documentos, estados de red, acciones de NovaMall, portal y combate de Civita, servidor crítico, cañón y dron, pasos y prensa, y carga y emisiones del encuentro final. Cada vista usa el estado existente para elegir el sonido. [WAV de ejecución](../../src/main/resources/cliente/audio/remaster/) · [Código de síntesis](../../tools/arte/AudioRemaster.cs)

## Prompt de Sofi

Herramienta ImageGen integrada, hoja transparente. La imagen de entrada fue el atlas `sofi-caminar.png`.

> Produce a game-ready transparent pixel-art sprite sheet for Sofi from the immediately preceding input, which is the strict character model reference (not a layout template). Six actually different drawn poses, arranged in exactly 3 columns × 2 rows with equal regular square cells. Each pose centered horizontally, shoe soles precisely aligned near 84% of cell height with broad transparent padding around each sprite; all faces three-quarter facing screen RIGHT. Read in row-major order: 1 Sofi starting to bend with bent knees, right arm down; 2 deep squat leaning forward, arm down towards ground, left arm balancing; 3 deepest squat, right hand almost touches ground at her front right; 4 starting rise with right hand and bent elbow lifting, head still leaning down; 5 rising mostly upright with arm drawing toward chest; 6 upright finishing with both elbows subtly bent. DRAW EMPTY HANDS; the game composites the collectible separately. Each drawing distinct in joints, silhouette, shoulders and hair. Preserve reference EXACT original big violet tousled hair silhouette, round dark spectacles, golden eyes, purple sweater, black pleated skirt, dark boots, tiny black crossbody bag, golden buckle, chibi proportions, outline and deep plum pixel clusters. Match pixel resolution and no smooth gradients. Native game sprite aesthetic. No divider lines, shadows, captions, margins painted, motion trails, no background. Strict transparent RGBA across the full sheet. No redesign or additions.
