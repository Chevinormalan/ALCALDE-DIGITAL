# Misión 4 · La plaza que dejó de escuchar

> Campaña vigente: esta misión requiere ganar la anterior (excepto la misión 1). Esc/P/Menú abre la pausa común y G guarda. El acceso, la recuperación y el guardado se coordinan desde el mismo sistema. Consulta [campaña y guardados](../campana/README.md); sus reglas sustituyen los menús particulares descritos en notas históricas inferiores.

Integrada en el juego principal el 12 de noviembre de 2026 y actualizada con el laberinto de Civita de `ELI1.zip`. En el selector aparece como «Juan en campaña». Conserva narrativa, escenarios, animaciones, enemigos y audio del aporte. Juan sigue siendo candidato; las elecciones definitivas pertenecen a la misión 8.

Desde **Jugar → Elegir misión 4**, conduce hasta la plaza, escucha al público y responde las cuatro preguntas de la tarima. Tras la visita de Palomo, abre Civita y sigue la animación del teléfono hasta entrar en el laberinto. La linterna revela papeles, terminales, fichas y amenazas, y la luz queda delimitada por los muros.

Recupera los cuatro expedientes: *La fuente oscura*, *La foto recortada*, *La red de los bots* y *El sospechoso*. Cada papel remite a un terminal del sector. Al recuperar la fuente, se abre una comparación ilustrada. El núcleo del corredor central se desbloquea con los cuatro expedientes contrastados. Tras el combate final, activa el núcleo para regresar a la plaza vacía.

El mapa usa la ilustración del parque de **Ajustes finales v2**. Sus senderos, setos y bordes de agua se reproducen en el plano de tránsito y delimitan el haz. Las fichas dejan durante unos segundos una ruta dorada que sigue las zonas transitables. Los expedientes mantienen el formato vertical del PDF y comparan la copia con el documento recuperado. En Civita, pulsaciones de **J** durante la recuperación encadenan hasta tres golpes. El guardián del núcleo se revela acercándose al borde este de la fuente e iluminando hacia el noroeste.

| Acción | Teclado | Mando |
|---|---|---|
| Mover / conducir | WASD o flechas | Stick izquierdo / cruceta |
| Hablar, confirmar, continuar | Enter / E | A / ✕ |
| Elegir respuesta | Izq./Der. + Enter; S/N | Cruceta + A |
| Linterna en Civita | F | LB / L1 |
| Atacar en Civita | J | X / □ o RT |
| Escudo | Q (K anterior) mantenida | Y / △ o LT |
| Esquivar | Espacio (Shift anterior) | RB |
| Pausa / reanudar | Esc / P | Menú / B |
| Controles y ayuda | H | Vista |

El ratón elige respuestas en la tarima, abre Civita y acepta el documento que se está leyendo. En el resultado, Enter regresa al selector; Esc/P abre pausa.

El capítulo usa simulación a 60 pasos por segundo conducida por `ClienteSketch`. Dibuja en un búfer de 1280 × 720 del mismo PApplet, con coordenadas del aporte escaladas proporcionalmente de 1024 × 576. `TemaPixel.marco` dibuja sus paneles y la pausa. No abre otra ventana ni otro ejecutable.

El checkpoint vive en `~/.local/state/alcalde-digital/mision4.properties` (o `ad.guardados`). Se guarda periódicamente, al cambiar de escena y al salir. Es un checkpoint narrativo del aporte: algunas conversaciones se reinician al recuperarlo, y el combate recupera enemigo, salud y ubicación, sin proyectiles ni teclas mantenidas. Al entrar ofrece Continuar o Nueva partida. `ad.mision4.guardar` permite señalar un archivo del formato recibido para importarlo explícitamente.

El guardado de Civita conserva los expedientes leídos, las fuentes contrastadas, las fichas, la linterna y las amenazas detenidas. Los checkpoints del recorrido anterior siguen siendo compatibles. Cada partida se acredita una sola vez en `campana.properties`; el resultado de la elección se determina en la misión 8.

Modalidad individual local. No añade transporte TCP ni nuevos árboles/grafos a las mecánicas del aporte. Las estructuras funcionales y consultas de las demás misiones se conservan. Validación y capturas en [integración](../integracion-misiones46/README.md).
