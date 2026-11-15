# Misión 2 — transcripción e interpretación de los bocetos de Sebastián

Fuentes: [historia y finales](referencias/boceto-historia-mision2.png) y [distribución de la red](referencias/boceto-red-mision2.png), recibidas el 7 de noviembre de 2026. Esta nota conserva el boceto; no certifica que la misión ya esté implementada.

Actualización: Sebastián entregó después la [oficina de Civitas](referencias/oficina-civitas.png) y el [diseño de la periodista](referencias/periodista-personaje.png). La propuesta integrada está en [ANALISIS_MISION2.md](ANALISIS_MISION2.md); esas referencias posteriores resuelven el escenario y la identidad del personaje que no aparecían explícitos en los bocetos iniciales.

Actualización posterior: Sebastián identificó a la paloma de las conversaciones como **Mr. Palomo** y pidió agregar al análisis el momento y guion provisional de cada encuentro. Véase la sección «Conversaciones con Mr. Palomo» del análisis.

## Transcripción de lo legible

### Foto 1: historia

- Título: «Misión 2».
- «Estás sentado en tu escritorio y se mete una paloma por la ventana y te dice:»
- Diálogo inicial parcialmente legible: «Hey papi, lo mismo que le dijiste al otro», «???», «Descarga [Civitas, lectura probable], tu mejor red». Las palabras exactas requieren confirmación.
- «La pateas y sale volando por la ventana».
- «Dices: Ush, esas plagas cada vez se extienden más por la ciudad».
- «De la nada todos los computadores de la red son hackeados».
- «La misión será: durante un tiempo de 60 segundos tendrás que proteger [los computadores].» La indicación manuscrita parece decir que se oprime E repetidamente hasta llenar la barra de un computador y luego se va al siguiente.
- «Una vez que termines»: dos ramas. Si proteges todos, entra una paloma por la ventana, hace una travesura frente al personaje y se va. El personaje queda cansado, mira la ventana y dice: «No vuelvo a patear una paloma».
- Si no proteges todos, entra una paloma, dispara un misil «por el jopo» (lectura incierta), explota todo y pronuncia una palabra final que no se distingue con seguridad.

### Foto 2: escenario y barras

- Diagrama de seis computadores numerados alrededor de una unidad central conectada con cables. La unidad central muestra «Civitas» y «100 %».
- Cada computador tiene una barra individual. Una anotación dice que las barras aparecen una vez comienza el hackeo.
- «Barra principal es la suma de los computadores protegidos».
- «Para proteger la red en cada computador se tendrá que oprimir E 3 veces». La cifra «3 veces» se distingue; la frase anterior está parcialmente tachada.

## Interpretación funcional fiel al boceto

1. Inicio en un escritorio o sala de computadores. Mr. Palomo entra por la ventana, hay un intercambio cómico y sale volando.
2. Comienza el hackeo de la red Civitas. En ese instante aparecen el reloj de 60 segundos, seis barras individuales y la barra central.
3. El jugador se desplaza entre seis terminales. Una interacción con E avanza la protección de la terminal cercana; tres interacciones exitosas completan una terminal. La foto 1 también sugiere pulsación continua, así que el gesto exacto debe cerrarse antes de programarlo.
4. La barra central representa el progreso agregado de las seis terminales, hasta el 100 % al salvarlas todas. El tiempo y la distribución de las estaciones crean la presión de la misión.
5. Al concluir el reloj o asegurar las seis terminales, se muestra uno de los dos finales dibujados: red protegida o red incompleta, ambos con remate cómico de la paloma.

## Relación con el laboratorio y con las otras misiones

- La red dibujada puede funcionar como **grafo real**: seis estaciones y un nodo central, con enlaces que transmiten el ataque y afectan la prioridad o el orden de protección. No debe ser solo una imagen de cables.
- Las acciones de protección deben modificar indicadores de Civitas y estado de la ciudad para conectar esta misión con la campaña y con el resultado general. El boceto no fija todavía los valores ni qué información se salva.
- Mantener el mismo lienzo, selector, pausa, ayuda, controles, estilo pixel y registro de resultados que usan las misiones 1 y 3. El boceto define la mecánica y el espacio; el diseño final de sprites, colisiones y escenario todavía debe construirse.
- El boceto **no identifica explícitamente el rol ni el nombre del personaje jugable**. Estar en un escritorio permite proponer al periodista, pero no confirma esa decisión. La propuesta anterior del «video de siete segundos» no describe estas fotos y no debe reemplazar su trama.

## Detalles pendientes de lectura o definición

- Texto literal del diálogo de Palomo y su frase final en el desenlace fallido.
- Si E se pulsa tres veces o se mantiene pulsada por un tiempo en cada terminal.
- Si el reloj se detiene inmediatamente al proteger los seis computadores y si una terminal puede volver a ser atacada.
- Papel exacto de la barra central: progreso de protección acumulado es la lectura más directa de la anotación.
- Rol y aspecto del protagonista, y qué consecuencias concretas tienen los resultados sobre Civitas y los indicadores de Ciudad Nova.
