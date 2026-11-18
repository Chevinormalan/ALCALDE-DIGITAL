# Prompts del rediseño de la misión 3

Herramienta: `imagegen` integrada. Los PNG de esta carpeta son las imágenes completas generadas; `tools/arte/preparar_mision3.py` extrae y adapta las imágenes que carga el juego. El boceto `boceto-influencer.png` procede del usuario.

## Influencer

Referencia 1: boceto del usuario. Referencia 2: `src/main/resources/cliente/arte/mision1/parque.png`, solo para estilo. Hoja transparente de cuatro vistas de un mismo influencer en pixel art cenital. Conservar cabello en capas y mechones laterales, gafas grandes, sonrisa tranquila, cuello alto, chaqueta holgada, correa diagonal, equipo al costado y espalda y botas. Paleta morada/lila, acentos amarillos y accesorio teal. Proporciones idénticas en frente, espalda, izquierda y derecha. Sin fondo, texto ni personajes adicionales.

## Fondos

Prefijo común: fondo de juego 2D cenital, ortográfico, pixel art nítido, composición ancha 16:9 para 1280 × 720, paleta morada/lila y escala visual de la misión 1. Sin personas, personajes, interfaz, palabras ni perspectiva isométrica.

- **Atrio:** NovaMall nocturno, pantalla Civitas al fondo, kiosco izquierdo, jardinera central abajo, banco derecho, paso claro a Pixel Lab a la derecha y arena abierta para combate.
- **Pixel Lab:** laboratorio tecnológico, tres nodos de servidor del centro hacia la derecha, mesas de trabajo, pasillos transitables, salida derecha al corredor corrupto, pantallas teal y acentos dorados.
- **Pasillo:** corredor comercial nocturno con tiendas arriba, jardineras y barandas abajo y a la derecha, paneles Civitas con fallos magenta y ruta amplia desde abajo a la izquierda hacia arriba a la derecha.
- **Núcleo:** arena circular del jefe con baldosas moradas, emblema central, torres de servidores y cables en los bordes, resplandor magenta y teal y espacio libre para combatir.

## Amenazas

Cada enemigo se pidió como **un solo sprite** original de pixel art cenital, aislado sobre transparencia y legible a escala pequeña, con gama morada/lila y acentos dorados/teal. Sujetos: troll verde con auriculares morados; robot de spam formado por sobres de notificación; clickbait amarillo en forma de exclamación con teléfono; máscara deepfake con fallos cian y magenta; comentario rojo enfadado; grupo de tres drones bot morados. El Núcleo Viral se pidió como terminal CRT flotante morado con ojo magenta, circuitos teal, alertas amarillas y paneles fragmentados, aislado sobre transparencia.

La primera hoja grupal de amenazas se descartó porque dificultaba separar siluetas; las seis imágenes individuales son las fuentes activas.

## Hojas de caminata

Se generó una hoja transparente adicional para el influencer y otra para cada uno de los seis enemigos. Cada hoja tiene **4 filas × 4 columnas**: filas de frente/abajo, espalda/arriba, izquierda y derecha; columnas con cuatro poses realmente dibujadas del ciclo de caminata. El influencer conserva las gafas, el pelo del boceto, el cuello alto, la correa, el equipo teal y las botas. Cada enemigo conserva su silueta y sus colores de la imagen individual. Se solicitó fondo transparente, personaje completo, escala y pivote constantes y ninguna letra ni interfaz. Los archivos son `*-caminar.png`.

`preparar_mision3.py` separa las 112 celdas, las centra por los pies y crea los PNG que carga el juego. La primera pose de cada dirección del influencer se reutiliza como reposo y como base de LIVE. Mr. Palomo usa el atlas existente de la misión 1: cuatro direcciones en la primera fila y cuatro poses de vuelo en la segunda.

## Núcleo Viral en movimiento

`viral-core-movimiento.png` se generó usando `redisenio/viral_core.png` como referencia de identidad. Se solicitó una hoja transparente de 4 filas × 4 columnas: abajo/frente, arriba/espalda, izquierda y derecha; cuatro poses distintas de levitación por fila. Se conservaron la carcasa CRT morada, el ojo magenta, los circuitos cian, las franjas amarillas, las pantallas de alerta y los cables. Los cuadros muestran impulso y arrastre de cables y fragmentos, sin piernas ni pasos. `preparar_mision3.py` extrae los 16 fotogramas `viral_core_move_*` con el mismo pivote visual dentro de cada dirección.
