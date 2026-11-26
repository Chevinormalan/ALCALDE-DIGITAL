# Misión 1 · Sofi y Mr. Palomo

Versión incorporada al proyecto principal desde `ELI1.zip`. Sofi comienza en la banca del parque de Ciudad Nova. Después de hablar con Mr. Palomo, sigue los papeles que arrastra el viento. El que cae junto a la banca enseña a usar la lupa. Después puedes investigar los otros tres en cualquier orden.

Al elegir la dificultad de una partida nueva se reproduce el prólogo de Sofi antes de la escena de la banca. Esc, B/Círculo o el botón **Omitir prólogo** llevan directamente a esa escena. **Continuar** abre el punto guardado.

1. **Recoge un papel con E.** Sofi lo levanta y despliega el pergamino. El parque, la fuente y los personajes se ilustran con recursos del juego. El rollo superior procede de la referencia de la misión 4.
2. **Activa la lupa con Q.** Acerca el cursor a detalles impresos y haz clic para registrar una evidencia. También puedes usar las flechas y Enter. La lupa amplía el contenido del documento y la libreta conserva los registros.
3. **Pasa a la página de publicación.** Con las evidencias completas, Sofi prepara el aviso correspondiente. El juego distingue una noticia confirmada, un boletín refutado, una frase fuera de contexto y una acusación pendiente de evidencias.
4. **Convoca en el tablón.** Una vez preparados los cuatro avisos, acércate al este de la fuente y pulsa E. Los vecinos se acercan por los caminos. Cuando llegan, el reloj marca 60 s en Fácil, 30 s en Normal o 12 s en Difícil. Pulsa E para escuchar a cada vecino y vuelve a pulsarla para calmarlo con su aviso.
5. **Cierra la investigación.** La escena final y el resumen registran los hallazgos, avisos y vecinos informados. La victoria habilita la misión 2.

WASD, flechas o stick para moverte. Tab abre la libreta, Esc vuelve al parque desde el expediente y G guarda. En mando, A/Cruz recoge y confirma, LB/L1 activa la lupa, la cruceta enfoca los detalles y X/Cuadrado abre la libreta. Las partidas nuevas usan el recorrido descrito. Los guardados de la versión anterior conservan el recorrido de la partida iniciada. Para empezar la aventura rediseñada, elige **Nueva partida** en la misión 1.

## Integración

- `SesionMisionUnoLocal` valida la distancia de interacción, registra cada evidencia una vez y decide cuándo se completa el caso.
- `ExpedientesSofi` guarda los hallazgos y el estado de cada expediente. El guardado de campaña también conserva los avisos, las posiciones y el avance de los vecinos.
- `PergaminoSofi` dibuja las ilustraciones, las zonas de inspección y la lupa sobre el lienzo existente.
- `PrologoSofi` presenta la cinemática en el mismo lienzo con audio del mezclador común. Para actualizar los recursos empaquetados a partir del MP4, ejecuta `python3 tools/arte/preparar_prologo_sofi.py /ruta/al/prologo.mp4` con FFmpeg.
- `ArbolAVLPublicaciones` conserva el estado de las publicaciones con su riesgo actualizado. `ArbolDecisiones` registra las decisiones de Sofi. El sistema de campaña acredita la victoria de la misión.

La prueba `MisionUnoInterfazTest` recorre los cuatro expedientes, usa los controles del renderizador, comprueba la ausencia de evidencias duplicadas y verifica la recuperación de guardados, la convocatoria y el cierre.
