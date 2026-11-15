# Análisis de misión 2 — defensa de la red Civitas

Fecha: 7 de noviembre de 2026. Estado: propuesta de diseño basada en los dos bocetos y las dos referencias visuales entregadas por Sebastián. Esta nota no representa una implementación.

**Actualización posterior:** a petición de Sebastián se incorporó la mecánica de ataques recurrentes del nivel `JUF.java` del juego anterior y se añadieron tres dificultades. Este documento conserva la propuesta original; el comportamiento jugable vigente está en [README.md](README.md).

## Referencias y prioridad

- [Historia, mecánica y finales escritos por Sebastián](referencias/boceto-historia-mision2.png).
- [Plano de seis computadores y barras](referencias/boceto-red-mision2.png).
- [Oficina y núcleo Civitas](referencias/oficina-civitas.png).
- [Diseño de la periodista](referencias/periodista-personaje.png).

La misión 2 de este boceto consiste en proteger una red hackeada en 60 segundos. El antiguo texto de «video de siete segundos» en la especificación integral es otra propuesta narrativa y no describe estas referencias. La periodista queda confirmada como protagonista por la referencia visual posterior al boceto.

## Historia y flujo

1. La periodista empieza sentada en su puesto, frente a la red de Civitas. Mr. Palomo entra volando por la ventana y hace una promoción absurda de la red. La periodista lo espanta de forma cómica; Palomo sale volando. Los textos manuscritos menos legibles se reescribirán conservando la intención, sin afirmar que son citas literales.
2. Comienza un ataque: los seis computadores periféricos aparecen comprometidos y se activan las barras de cada estación, la barra general y el reloj de 60 segundos. El reloj no corre durante el diálogo introductorio ni durante la pausa.
3. La periodista recorre el cuarto y protege cada computador. Tres interacciones válidas con E llenan su barra. Cada terminal protegida aporta al progreso general. Al alcanzar seis de seis, la barra central marca 100 % y el desafío termina.
4. Éxito: vuelve Mr. Palomo, deja una travesura frente a la protagonista y se marcha. La periodista, agotada, dice que no volverá a patear una paloma.
5. Tiempo agotado con terminales sin proteger: vuelve Mr. Palomo y ocurre la explosión absurda que muestra el boceto. El remate escrito no es completamente legible; el guion provisional de abajo conserva la intención sin fingir que transcribe esa frase.

Sebastián confirmó posteriormente que la paloma de las conversaciones es Mr. Palomo. Su llegada, conversación y partida usarán vuelo visible y los fotogramas existentes del personaje.

## Espacio jugable

La oficina de referencia aporta la composición: núcleo de Civitas en el centro, tres escritorios a la izquierda, tres a la derecha, puesto inicial abajo al centro, puerta y ventanas arriba, y muebles perimetrales. La estación inferior central es el puesto de la periodista, no un séptimo objetivo. Los seis puestos laterales tendrán pantallas visibles para identificar las interacciones.

La imagen es una guía de composición y color. Para jugar en el lienzo principal de 1280 × 720 se recompondrá en vista cenital pixelada, con rutas de paso suficientes alrededor del servidor y los escritorios. Habrá colisión con servidor, mesas, archivadores, plantas, paredes y puerta; los cables serán visibles, pero no impedirán caminar. Las zonas de interacción estarán junto a cada terminal y serán alcanzables sin atravesar muebles.

## Periodista y animación

Conservar del diseño entregado: cabello corto y desordenado verde oliva, gafas negras, ojos amarillo verdosos, saco morado, credencial colgada, falda oscura y zapatos. Crear una versión jugable pixel art con proporciones compatibles con Sofi y el influencer; la ilustración entregada servirá como referencia, no como sprite pegado al escenario.

Preparar una hoja con cuatro direcciones y cuatro cuadros de caminata por dirección (16 cuadros reales), reposo en las cuatro direcciones, interacción con terminal, postura sentada de la introducción y reacciones de tensión, alivio y cansancio para los finales. Para los diálogos, usar retratos con expresiones de calma, sospecha, urgencia y agotamiento. Los cuadros deberán conservar ropa, lentes, cabello y tamaño entre poses.

## Interfaz y conversaciones

La misión 1 aporta convenciones compartidas: tipografía pixel legible, controles, pausa, ayuda, navegación, estados de Civitas y resultado de campaña. La misión 2 tendrá identidad visual propia: violetas y lavandas de la oficina, fucsia para actividad de red, cian para protección y rojo de alarma para el hackeo; no copiará exactamente la distribución ni los colores de la misión 1.

Los diálogos seguirán la presentación de misión 3: panel pixelado integrado en el lienzo, nombre del hablante, retrato enmarcado cuando exista, texto que aparece progresivamente, avance con E/Enter y cámara enfocada en quien habla. La periodista y la paloma tendrán colores de acento distintos. El diálogo se cerrará antes de activar el reloj; al terminar habrá un cierre con el resultado correspondiente.

Durante el hackeo, mostrar tiempo restante, seis estados/barras, progreso general y una indicación contextual «E · proteger» cerca de la terminal alcanzable. Las barras individuales aparecen cuando empieza el ataque, como indica el boceto. Pausar detiene reloj, animaciones de ataque y entradas; Esc sigue la convención del menú de las otras misiones.

## Conversaciones con Mr. Palomo: momentos y guion provisional

Estas líneas son **propuesta de diálogo jugable**, no transcripción literal de las partes ilegibles del manuscrito. Se mantienen el humor, la entrada por la ventana, la promoción de Civitas, la reacción de la periodista y los dos remates del boceto. Palomo no afirma haber iniciado el hackeo: su responsabilidad permanece ambigua, como en las otras misiones.

### 1. Antes del hackeo: llegada a la oficina

**Momento:** al iniciar la misión, con la periodista sentada; el reloj y las barras aún no aparecen. Mr. Palomo entra volando por una ventana, aterriza cerca del escritorio y la cámara lo enfoca. Panel de conversación al estilo de misión 3, con retratos alternados.

> **MR. PALOMO:** «¡Ey, periodista! ¿Ya conoces Civitas? La mejor red para estar al día.»<br>
> **PERIODISTA:** «¿Una paloma acaba de entrar a mi oficina para venderme una red social?»<br>
> **MR. PALOMO:** «La publicidad de altura llega a todas las ventanas.»<br>
> **PERIODISTA:** «Pues vuelve a salir por esta.»

**Acción:** la periodista hace el gesto caricaturesco de espantarlo, equivalente a la patada del boceto. Palomo despega con sus cuadros de vuelo y sale por la ventana. La cámara regresa a la periodista.

> **MR. PALOMO, alejándose:** «¡Qué recibimiento! Luego hablamos.»<br>
> **PERIODISTA:** «Uf, esas plagas cada vez se extienden más por la ciudad.»

El gesto no abre una decisión ni modifica los indicadores: es la introducción cómica fijada en el boceto.

### 2. Alarma: inicio exacto del desafío

**Momento:** después de la última línea de la periodista y de que Palomo haya salido de la pantalla. Se encienden los seis computadores, aparece la alerta y se muestra la red central. El contador todavía espera a que termine esta indicación breve.

> **CIVITAS:** «ALERTA. Seis computadores comprometidos.»<br>
> **PERIODISTA:** «Tengo un minuto para proteger la red. Empiezo por el más cercano.»

Al cerrar el panel, el reloj pasa a **60 segundos**, aparecen las seis barras y se habilitan movimiento e interacción. Mr. Palomo no interrumpe durante la cuenta atrás. Los cambios de terminal y de progreso se comunican con avisos breves sobre el escenario, sin abrir conversaciones que congelen el tiempo.

### 3A. Final si protege los seis computadores

**Momento:** inmediatamente después de la tercera interacción válida en el sexto computador. Se fija el resultado 6/6, la barra central llega al 100 %, se detiene el reloj y desaparecen las alertas. La periodista respira agotada. Mr. Palomo vuelve a entrar volando por la ventana y aterriza; empieza el panel de diálogo.

> **MR. PALOMO:** «Veo que mi red sigue en pie.»<br>
> **PERIODISTA:** «Los seis equipos están a salvo. No pienso discutir de quién es la red.»<br>
> **MR. PALOMO:** «Entonces te dejo una pequeña reseña del servicio.»

**Acción:** Palomo hace la travesura dibujada en el boceto frente a la periodista y vuelve a salir volando. El efecto se presenta como humor visual pixelado, sin ocultar el resultado de la misión.

> **PERIODISTA, mirando la ventana:** «No vuelvo a patear una paloma.»

Después se muestra el resultado positivo y se registran las consecuencias en Civitas.

### 3B. Final si quedan computadores sin proteger

**Momento:** cuando el reloj llega a cero. Se bloquean interacciones, se conserva el número real de computadores protegidos y la barra central queda por debajo del 100 %. Palomo vuelve a entrar volando mientras el servidor parpadea.

> **MR. PALOMO:** «¿Terminaste? Esa luz roja parece bastante insistente.»<br>
> **PERIODISTA:** «No. Aléjate del núcleo; todavía está inestable.»<br>
> **MR. PALOMO:** «Claro. No tocaré nada. Ni siquiera moveré la cola.»

**Acción:** ocurre el gag del misil y la explosión del boceto como efecto absurdo y pixelado. No se declara que Palomo haya provocado el hackeo. La escena corta a la periodista cubierta de humo digital, conservando una salida legible hacia el resultado.

> **MR. PALOMO:** «Ups. Eso no estaba en el manual de Civitas.»<br>
> **PERIODISTA:** «Y yo que pensaba que el hackeo era mi mayor problema.»

Palomo sale volando y se muestra el resultado negativo con las consecuencias de los equipos no protegidos. La palabra final tachada del manuscrito sigue pendiente de lectura y no se atribuye falsamente a este guion.

### Reglas de presentación de las conversaciones

- Intro y finales: retratos de la periodista y Palomo, nombre y acento propios, revelado de texto y avance con E/Enter; foco de cámara alternado como en misión 3.
- El personaje que habla debe haber llegado o aterrizado antes de mostrar su primer retrato; al salir, se espera a que termine el vuelo antes de pasar a la siguiente pantalla.
- No hay diálogo de Palomo durante los 60 segundos. Ningún panel narrativo consume tiempo de la prueba; el reloj solo corre mientras el jugador puede moverse y proteger computadores.
- En los finales, el estado de las seis estaciones se fija **antes** del diálogo para que la cinemática no cambie el resultado.

## Estructuras y consecuencias

Representar la red como un grafo de siete nodos (núcleo más seis terminales), con conexiones y estados reales. La propagación del ataque y el aislamiento de estaciones deben usar esos enlaces; la visualización y el estado del juego deben derivar del mismo modelo. El boceto marca todos los computadores como hackeados al inicio, así que el grafo no alterará ese hecho. Puede calcular conexiones aún comprometidas y alcance de contenido corrupto mientras la periodista protege nodos.

El éxito o fallo debe modificar indicadores de Civitas y registrar resultado de misión, sin inventar verdades o puntos que el boceto no define. La misión 2 puede registrar sus decisiones en las estructuras comunes del proyecto; no se añadirá un árbol decorativo. La infraestructura de 2 a 4 jugadores y el papel de roles ausentes se abordarán en la integración final del laboratorio, sin describir esta escena individual como multijugador ya terminado.

## Criterios para darla por terminada

- Los seis computadores son visibles, distintos, alcanzables y protegibles con E; tres interacciones completan cada uno.
- Una partida jugada a velocidad real permite completar las seis terminales dentro de 60 segundos, pero deja presión temporal. El reloj empieza después de la introducción y se congela en pausa.
- La barra central refleja exactamente el estado de las seis estaciones y llega a 100 % solo con seis protegidas.
- Los dos finales se activan según el estado real al concluir; las consecuencias llegan al progreso de campaña.
- La periodista anima arriba, abajo, izquierda y derecha sin cambios de identidad entre cuadros. Palomo entra y sale volando en el inicio y en ambos finales; cada conversación empieza después de aterrizar y termina antes de abandonar la escena.
- El nivel comparte ventana, selector, navegación, pausa y registro de resultado con las misiones 1 y 3. Se comprueban colisiones, terminales, texto, audio/avisos visuales y salida al menú.

## Detalles por cerrar antes de escribir el guion final

Confirmar el texto ilegible del inicio y del remate fallido y si E debe pulsarse tres veces (lectura explícita de la segunda foto) o mantenerse pulsada (posible lectura de la primera). La paloma ya fue identificada como Mr. Palomo por Sebastián. La mecánica central, seis estaciones, 60 segundos y dos resultados, sí queda definida.
