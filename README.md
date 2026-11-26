# Digital Mayor: Runaway Rumors

Videojuego educativo 2D top-down desarrollado en Java con Processing para el laboratorio de Estructura de Datos II.

## Campaña, pausa y guardado compartidos

En un perfil nuevo solo está disponible la misión 1. Ganar cada misión desbloquea la siguiente y el avance queda guardado; perder, abandonar o tener un checkpoint no desbloquea niveles. Las ocho misiones están implementadas.

La campaña comienza con la misión 1 disponible. Ganar una misión desbloquea la siguiente. El selector también ofrece **Desbloquear todas las misiones** como opción manual; no marca niveles como ganados y se conserva en el perfil. **Restablecer juego** pide confirmación, archiva el perfil completo en una carpeta de respaldo y vuelve a bloquear las misiones 2 a 8. El menú inicial permite continuar la partida más reciente o reintentar una misión perdida.

En un perfil nuevo, **Jugar → Tutorial de Mr. Palomo → Elige una misión**. El tutorial practica los controles de las ocho misiones y adapta las indicaciones al teclado o al mando conectado. Mantén **Esc**, **B** en Xbox, **Círculo** en PlayStation o el clic sobre el botón superior derecho durante 1,5 segundos para omitirlo. Al terminar u omitir, el juego recuerda la decisión. Tras restablecer el perfil, el tutorial vuelve a aparecer al pulsar **Jugar**.

Todas usan la misma pausa con **Esc/P/Menú**: Continuar, Ayuda y controles, Guardar, Consultas y Guardar y salir. **G** guarda; **H/Vista** abre ayuda. Al seleccionar una misión con checkpoint se ofrece Continuar o Nueva partida. Se conservan los guardados previos y las modalidades LAN de 5, 6, 7 y 8. La misión 3 recupera desde el inicio de la zona actual de NovaMall. [Reglas de victoria, controles, guardados y verificación](docs/campana/README.md). [Tabla de puntajes y antecedentes](docs/campana/PUNTAJES_Y_ANTECEDENTES.md).

Desde **Cómo jugar → Ver controles** en el menú inicial, o **Pausa → Ayuda → Ver controles** durante una misión, se muestra un mapa gráfico del teclado y del mando. En el menú inicial se puede recorrer las misiones 1 a 8 y ver el modo de dos personas de las misiones 5 a 8. La lámina de la misión 8 indica además las teclas de J3 y J4. J1 usa WASD, E y Espacio; J2, cuando comparte teclado, usa flechas, Enter y Mayús. F es la acción principal y Q la secundaria en los combates que las tienen. La misión 8 reserva E/Enter para confirmar fuera del combate y usa C/O para la tercera acción de J1/J2. La pantalla distingue los dos personajes y cambia las etiquetas Xbox/PlayStation según el mando detectado. [Controles globales y excepciones por misión](docs/campana/CONTROLES_GLOBALES.md).

## Gráficos y pantalla completa

**Cómo jugar → Gráficos** permite elegir ventana o pantalla completa sin bordes, tamaño compatible con el monitor, ajuste proporcional o escalado entero y filtro nítido o suave. **Aplicar** guarda las preferencias; **F11** alterna pantalla completa. Las ocho misiones y sus interfaces conservan un lienzo original de 1280 × 720; el ratón se adapta al mismo reescalado. En pantallas de otra proporción se muestran márgenes decorativos. [Uso y verificación](docs/graficos/README.md).

## Nueva misión 1 · Sofi y Mr. Palomo

El selector que sigue a «Jugar» contiene las ocho misiones. La misión 8 permite jugar según los antecedentes de siete victorias o elegir finales de prueba al desbloquear todas las misiones. Después de elegir la misión 1 se selecciona Fácil, Normal o Difícil. Sofi escucha a Mr. Palomo, recoge cuatro pergaminos y examina con la lupa sus evidencias. Prepara los avisos y reúne a cuatro vecinos en el tablón del parque. El AVL conserva las publicaciones, el árbol n-ario registra las decisiones y T abre la consulta de estructuras.

La crisis dura 60 segundos en Fácil, 30 en Normal y 12 en Difícil. El reloj empieza cuando los vecinos llegan. WASD/flechas mueven a Sofi, E recoge e interactúa, Q activa la lupa, Tab abre la libreta, T muestra estructuras y H abre ayuda. El resultado puede ser bueno, neutral o malo según las decisiones informativas, las conversaciones y el estado de la ciudad.

El arte nuevo está en `src/main/resources/cliente/arte/mision1/` y las cinco referencias recibidas están en `tools/arte/referencias-mision1/`. [Detalles de implementación y límites](docs/mision1/README.md). El caso anterior de «El helipuerto de palomas» se conserva como resguardo en el código, recursos y pruebas, sin acceso desde el menú del juego.

## Misión 2 integrada · La red Civitas

La periodista defiende seis computadores conectados a Civitas contra oleadas que comprometen de uno a tres equipos seguros. Se elige Fácil (60 s, cada 10 s), Normal (60 s, cada 6 s) o Difícil (50 s, cada 4 s). En los últimos ocho segundos no hay oleadas regulares, pero al quedar dos segundos un computador seguro pasa a rojo una sola vez. Solo el primer intento en Difícil tiene un 30 % de probabilidad de fallo crítico del núcleo; el siguiente intento siempre puede ganarse. E repara el computador en peligro; hay que terminar con los seis seguros. Shift permite correr y Espacio lanza un salto hacia delante con cuadros propios para las cuatro direcciones. Mr. Palomo entra y sale volando en la introducción y vuelve en los dos finales. El servidor central anima sus luces cian y rojas según el estado de la red. El escenario y la protagonista siguen los bocetos aportados por Sebastián, con interfaz y colores propios. El resultado se registra en la campaña, salvo un fallo crítico inevitable. [Controles, recursos y verificación](docs/mision2/README.md).

## Misión 3 integrada · Civitas en Rojo

La misión 3 se elige desde el mismo selector y utiliza el lienzo lógico compartido de 1280 × 720, presentado según las opciones de Gráficos. Se juega con el Influencer en NovaMall: Atrio, Pixel Lab, pasillo corrupto y Núcleo Civitas, con Mr. Palomo, seis enemigos y el Núcleo Viral. El influencer está basado en el boceto aportado por Sebastián. Sus cuatro zonas, personajes y amenazas usan pixel art cenital y la paleta morada de la misión 1. Esc/P abre la pausa común; el último resultado queda registrado durante la sesión de la aplicación. La confianza y desinformación de una misión completada se acumulan en el estado de ciudad mostrado en el selector. El rumor usa el AVL de publicaciones y un grafo dirigido de difusión; otro grafo valida las rutas de NovaMall. [Controles, arte y estado técnico](docs/mision3/README.md).

## Misiones 4 y 6 integradas desde la entrega recibida

La misión 4, «La plaza que dejó de escuchar», sigue a Juan durante su campaña: conducción, preguntas en la plaza y un laberinto de Civita con linterna, cuatro expedientes y tres combates. La misión 6, «Cielos de campaña», permite uno o dos jugadores en teclado, con vuelo, dron de tres fases e entrevista. Ambas se seleccionan desde Jugar y utilizan la misma ventana de 1280 × 720, tipografía, marcos, pausa y guardados del proyecto. Esc/P pausa; H abre controles. Mando compatible para el primer jugador. La misión 6 también dispone de una sala LAN cooperativa para dos personas. [Misión 4](docs/mision4/README.md) · [Misión 6](docs/mision6/README.md) · [Integración y validación](docs/integracion-misiones46/README.md).

## Entrega 1 implementada

Caso preservado de la Entrega 1, «El helipuerto de palomas»: explora el parque, lee Civitas, encuentra el aviso y el testimonio, elige una acción y observa sus consecuencias. Conserva cuatro roles y cuatro variantes por semilla. Ya no se ofrece al jugador desde el menú principal.

El AVL propio organiza publicaciones por (riesgo,ID): inserción, búsqueda, eliminación, reinserción por cambio de riesgo, cuatro rotaciones y recorridos. El árbol n-ario inserta opciones al conseguir evidencia y poda alternativas al cerrar el caso. Ambos tienen historial visual de estados, búsqueda y recorridos en la GUI.

La visualización aplica el apartado 28.1 de la documentación integral: T abre una consulta opcional con **Radar de publicaciones** y **Camino de decisiones**, una pestaña a la vez y siempre con el estado actual. **Abrir Laboratorio** muestra claves, alturas, FE, operaciones, recorridos, búsqueda y estados históricos asociados a su evento. T, Esc y Volver regresan al punto de entrada y conservan la selección. También hay acceso al Laboratorio desde menú, ayuda y resultado de una sesión.

Las acciones muestran avisos explicando el cambio y su causa. Se pueden ocultar en Ayuda y siguen disponibles al consultar las estructuras. Las reglas y los algoritmos de la entrega anterior se conservaron: la prioridad de #1 baja a 8 **al reunir ambas fuentes** (30 → 8, o 75 → 8 si antes se compartió), no al comenzar la verificación.

Incluye mapa de tiles con colisiones y cámara, personajes animados, objetos independientes, ayuda, navegación por teclado, contraste alto, movimiento reducido, plazo ampliado, puntuación, siete indicadores y 148 PNG de ejecución. La sesión local mantiene la autoridad sobre reglas y evita duplicar efectos.

Las misiones 5 y 7 admiten dos clientes por red local en sus salas correspondientes. La misión 8 incorpora una sala TCP de hasta cuatro participantes y el cierre electoral. El vestíbulo general y el grafo territorial compartido entre escenarios siguen pendientes. Cada misión disponible tiene su propio cierre; la misión 7 conserva los apoyos y antecedentes para la elección definitiva.

## Requisitos previstos

- JDK 21 o compatible.
- Maven 3.9 o posterior.
- Processing Core 4.5.6, descargado por Maven.
- Visual Studio Code con Extension Pack for Java.
- Acceso inicial a Maven Central para descargar Processing Core, JInput y JUnit.

Entorno comprobado: JDK 26.0.2 compilando con `--release 21`, Maven 3.9.15 incluido en la extensión Java de VS Code y Processing Core 4.5.6. Maven no está en PATH en esta máquina; `herramientas.sh` detecta su ubicación. Las pruebas de interfaz se ejecutan sin pantalla; el juego necesita un escritorio gráfico.

## Puntos de entrada

- Cliente gráfico: `alcaldedigital.app.ClienteApp`
- Panel gráfico del servidor: `alcaldedigital.app.ServidorApp`

Para jugar el paquete preparado (JDK 21 o posterior):

```bash
bash jugar.sh
```

En Windows: `jugar.bat`. Desde VS Code: ejecutar `ClienteApp`. Para construir, probar y regenerar evidencias:

```bash
bash herramientas.sh test
bash herramientas.sh paquete
bash herramientas.sh evidencias
```

También puedes usar Maven directamente:

```bash
mvn test
mvn exec:java -Dexec.mainClass=alcaldedigital.app.ClienteApp
mvn exec:java -Dexec.mainClass=alcaldedigital.app.ServidorApp
```

`ServidorApp` conserva el panel preliminar para el futuro servidor; no se necesita para el capítulo local. El paquete de ejecución se crea en `distribucion/` y no se versiona. El ZIP de entrega incluye sus dependencias y puede jugarse sin red.

## Controles y demostración

El juego reconoce mandos Xbox y PlayStation. En Linux y Windows busca dispositivos mientras el juego está abierto. El menú muestra «Mando detectado · Jugador 1» al conectar el primero. Los mandos adicionales quedan disponibles hasta elegir cooperativo local. En las misiones 5, 6, 7 y 8 cada mando asignado controla su puesto. En LAN, el aviso muestra el puesto que la sala asignó a ese equipo. **Cómo jugar → Ver controles** muestra el esquema de botones. El paquete incluye JInput y sus bibliotecas nativas.

| Mando | Menús y misión 1 | Misión 3 |
| --- | --- | --- |
| Stick izquierdo / cruceta | Navegar; mover a Sofi | Mover al Influencer |
| Stick derecho | — | Apuntar PING |
| A / ✕ | Confirmar; hablar | Interactuar, avanzar diálogo; repetir al final |
| B / ○ | Volver; abrir o cerrar pausa durante la misión | Abrir o cerrar pausa |
| X / □ | Abrir la libreta de Sofi | PING |
| Y / △ | Abrir estructuras | LIVE BURST |
| LB / L1 | Activar la lupa del expediente | MODO PRIVADO mientras se mantenga |
| RB / R1 | Mover el foco hacia abajo | SIGNAL DASH |
| LT / L2 | — | FLASH |
| RT / R2 | — | PING |
| Menú / Options | Pausa | Pausa |
| Vista / Share | Ayuda | Pausa con controles |

En la misión 2, el stick izquierdo mueve a la periodista, RB/R1 permite correr, X/□ lanza el salto hacia delante y A/✕ avanza los diálogos o repara la terminal cercana con una pulsación. Menú/Options abre la pausa. B/○ también abre la pausa; allí se puede volver al menú principal.

En el Laboratorio, el campo de búsqueda abre un teclado numérico manejable con la cruceta o el stick y A; X borra y B cierra. En pausa, elige «Continuar», «Ver controles» o «Salir al selector». Si el mando se desconecta durante una misión, el juego se pausa. Al reconectar, el juego recupera el puesto si sigue libre. En Linux, la cuenta necesita acceso al dispositivo del mando en `/dev/input`.

WASD/flechas: movimiento; E: interactuar; en misión 2, Shift corre y Espacio salta durante la defensa. En la misión 1, Q activa la lupa, Tab abre la libreta y las flechas con Enter permiten examinar pistas del expediente. T abre o cierra estructuras, H abre ayuda y Esc vuelve o abre el menú según la escena. En el Laboratorio AVL, enfoca el campo ID antes de escribir; admite cursor, flechas, Inicio/Fin, Retroceso, Suprimir y Enter. Busca y recorre el **estado mostrado**, incluso si es histórico. «Animar nodos» conserva ese estado; «Anterior/Siguiente» cambian la operación. Con movimiento reducido se avanza manualmente.

Las pruebas y capturas de la Entrega 1 siguen comprobando el caso preservado. La misión 1 tiene pruebas propias de vuelo, difusión, desenlaces, interfaz y viabilidad en Difícil. La misión 3 conserva su autoprueba integrada y se comprobó el flujo selector → misión 3 → regreso al selector desde el JAR.

Consulta `entregas/entrega1/README.md` para conocer el alcance, la arquitectura, el trabajo realizado por cada integrante y la validación de esta entrega. El catálogo de recursos se encuentra en `src/main/resources/cliente/arte/index.html`.

## Documento de la entrega

El informe formal consolidado se encuentra en [entregas/entrega1/README.md](entregas/entrega1/README.md). Allí presentamos el resultado , la estructura del proyecto, la función de los componentes, el trabajo por integrante, las pruebas realizadas, las instrucciones de ejecución y el alcance pendiente.

## Reglas de trabajo

- La rama principal debe permanecer ejecutable.
- Cada tarea tendrá responsable, revisor y criterio de aceptación.
- Los clientes solicitan acciones; el servidor conserva el estado oficial.
- Las estructuras resuelven mecánicas reales y no se agregan solo para mostrarlas.
- Los recursos maestros permanecen en `../Diseño Visual y Dirección de Arte/`; solo se copiarán a `src/main/resources` los archivos adaptados para ejecución.
- No se suben archivos generados por Maven, configuraciones personales ni secretos.


## Partidas LAN

Las misiones 5, 6 y 7 tienen salas para dos personas. La misión 8 permite crear una sala de dos, tres o cuatro. En la misma red local, el anfitrión elige **Crear partida LAN** y el resto elige **Unirse a partida LAN** dentro de la misma misión. La lista muestra el número de salas, sus cupos libres y los puestos disponibles. La misión arranca automáticamente cuando se completa la sala.

El buscador usa difusión IPv4 por UDP en los puertos 48515–48518. Cada misión juega por TCP en su puerto 48505–48508. Si el buscador muestra cero salas y el anfitrión sigue esperando, **Conectar por IP** permite indicar su dirección directamente. Ambos equipos necesitan una versión del juego que tenga las salas LAN. Tras perder la conexión aparece **Reconectar al mismo puesto**.

## Misión 5 · El servidor crítico y sala LAN

Ciudadano y periodista entran al servidor en una batalla de dos corazones: cinco rondas con quince patrones, salto y gravedad, haces dirigidos, vida individual, precisión, publicaciones y decisiones compartidas. Usa la oficina y los fotogramas de las misiones 1/2, más escenario y sprites nuevos con la paleta actual. Admite individual con solo el ciudadano (un corazón), dos personas en un teclado o dos clientes por sockets TCP, puerto 48505. Hay checkpoints por modo y registro duradero de puntos y campaña por sala. [Instrucciones, controles y guardados](docs/mision5/README.md).

## Misión 7 · La carrera por la primicia

Periodista e influencer persiguen un anuncio electoral sospechoso hasta el parque. Historia, rivalidad, cuenta atrás, carrera de avenida, noticias con decisiones, ayudas, sabotajes anunciados, llegada y desenlace funcionan en la misma ventana. Admite individual con cualquiera de los dos roles frente a CPU, dos personas en teclado o sala competitiva TCP 48507. Hay tres dificultades y control por pulsaciones o manteniendo el botón, con igual velocidad máxima. La periodista contrasta más rápido; el influencer alcanza a más grupos del grafo social.

Se reutilizan oficina, NovaMall, parque, protagonista ciudadano, Palomo y los movimientos de los personajes existentes. Se crearon una avenida y 48 cuadros originales de reacciones, ciudadanos y objetos con la paleta actual. El AVL organiza noticias; el árbol registra decisiones y el grafo propaga aclaraciones y rumores que cambian los ciudadanos y obstáculos. Los puntos por la primicia se separan de la reputación y el resultado colectivo. Juan queda excluido si se confirma un pacto corrupto; permanecer honesto y reunir apoyo le permite competir. La misión 7 guarda apoyos provisionales y antecedentes, sin proclamar un ganador definitivo. [Controles, arte, red, guardados y verificación](docs/mision7/README.md).


## Misión 8 · El futuro de Civitas

Cierre electoral conectado a las siete victorias y sus decisiones. Incluye construcción animada de la tarima, auditoría con AVL/árbol/grafo, votación y escrutinio, mutación de cualquiera de los cuatro candidatos, combate lateral de cinco fases y tres formas, seis ataques y tres variantes del estado de la ciudad. Hay uno, dos o tres héroes cooperativos; cuatro personas juegan tres contra uno. LAN TCP 48508, pausa común, guardado y reconexión por puesto.

Se reutilizan personajes, colores, botones y marcos de las primeras misiones. El arte nuevo contiene 192 sprites y cuatro fondos. [Jugabilidad, controles, decisiones y verificación](docs/mision8/README.md).

## Banda sonora y sonido

Las ocho misiones y el menú tienen música integrada: 20 temas principales, dos variantes de cierre, 32 capas y 50 nuevos efectos/ambientes. Cómo jugar → Audio permite ajustar cinco niveles, silenciar y reducir intensidad. La música acompaña reloj, rondas y fases, baja durante diálogos y conserva su posición al pausar. [Catálogo, muestra, fuentes y validación](docs/audio/README.md).
