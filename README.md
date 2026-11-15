# Alcalde Digital: Promesas, Palomas y Wi-Fi

Videojuego educativo 2D top-down desarrollado en Java con Processing para el laboratorio de Estructura de Datos II.

## Campaña, pausa y guardado compartidos

En un perfil nuevo solo está disponible la misión 1. Ganar cada misión desbloquea la siguiente y el avance queda guardado; perder, abandonar o tener un checkpoint no desbloquea niveles. Las misiones 1–7 están implementadas y la 8 continúa en desarrollo.

La campaña comienza con la misión 1 disponible. Ganar una misión desbloquea la siguiente. El selector también ofrece **Desbloquear todas las misiones** como opción manual; no marca niveles como ganados y se conserva en el perfil. **Restablecer juego** pide confirmación, archiva el perfil completo en una carpeta de respaldo y vuelve a bloquear las misiones 2 a 7. El menú inicial permite continuar la partida más reciente o reintentar una misión perdida.

Todas usan la misma pausa con **Esc/P/Menú**: Continuar, Ayuda y controles, Guardar, Consultas y Guardar y salir. **G** guarda; **H/Vista** abre ayuda. Al seleccionar una misión con checkpoint se ofrece Continuar o Nueva partida. Se conservan los guardados previos y las modalidades LAN de 5/7. La misión 3 recupera desde el inicio de la zona actual de NovaMall. [Reglas de victoria, controles, guardados y verificación](docs/campana/README.md). [Tabla de puntajes y antecedentes para tres desenlaces futuros](docs/campana/PUNTAJES_Y_ANTECEDENTES.md).

Desde **Cómo jugar → Ver controles** en el menú inicial, o **Pausa → Ayuda → Ver controles** durante una misión, se muestra un mapa gráfico del teclado y del mando. En el menú inicial se puede recorrer las misiones 1 a 7 y ver el modo de dos personas de las misiones 5 a 7. J1 usa WASD, E y Espacio; J2, cuando comparte teclado, usa flechas, Enter y Mayús. F es la acción principal y Q la secundaria en los combates que las tienen. La pantalla distingue los dos personajes y cambia las etiquetas Xbox/PlayStation según el mando detectado.

## Nueva misión 1 · Sofi y Mr. Palomo

Desde el menú principal, «Jugar» abre la selección de ocho misiones. Las misiones 1 a 7 están implementadas y se desbloquean por victorias; la 8 aparece como pendiente. Después de elegir la misión 1 se selecciona Fácil, Normal o Difícil. Sofi escucha a Mr. Palomo, examina tres publicaciones en Civitas y conversa con ocho peatones mientras el rumor se propaga por un grafo social. Cada pareja calmada sale por uno de los cuatro caminos. Las publicaciones se ordenan en el AVL y las opciones forman un árbol n-ario; T muestra las tres estructuras de la misión.

La crisis dura 60 segundos en Fácil, 30 en Normal y 12 en Difícil. La introducción y Civitas no consumen ese tiempo. WASD/flechas mueven a Sofi; E escucha o continúa el diálogo; C abre Civitas; T muestra estructuras; H abre ayuda. El resultado puede ser bueno, neutral o malo según las decisiones informativas, las conversaciones y el estado de la ciudad.

El arte nuevo está en `src/main/resources/cliente/arte/mision1/` y las cinco referencias recibidas están en `tools/arte/referencias-mision1/`. [Detalles de implementación y límites](docs/mision1/README.md). El caso anterior de «El helipuerto de palomas» se conserva como resguardo en el código, recursos y pruebas, sin acceso desde el menú del juego.

## Misión 2 integrada · La red Civitas

La periodista defiende seis computadores conectados a Civitas contra oleadas que comprometen de uno a tres equipos seguros. Se elige Fácil (60 s, cada 10 s), Normal (60 s, cada 6 s) o Difícil (50 s, cada 4 s). En los últimos ocho segundos no hay oleadas regulares, pero al quedar dos segundos un computador seguro pasa a rojo una sola vez. Solo el primer intento en Difícil tiene un 30 % de probabilidad de fallo crítico del núcleo; el siguiente intento siempre puede ganarse. E repara el computador en peligro; hay que terminar con los seis seguros. Shift permite correr y Espacio lanza un salto hacia delante con cuadros propios para las cuatro direcciones. Mr. Palomo entra y sale volando en la introducción y vuelve en los dos finales. El servidor central anima sus luces cian y rojas según el estado de la red. El escenario y la protagonista siguen los bocetos aportados por Sebastián, con interfaz y colores propios. El resultado se registra en la campaña, salvo un fallo crítico inevitable. [Controles, recursos y verificación](docs/mision2/README.md).

## Misión 3 integrada · Civitas en Rojo

La misión 3 se elige desde el mismo selector y se dibuja directamente sobre la ventana principal de 1280 × 720, sin un lienzo interior ni márgenes. Se juega con el Influencer en NovaMall: Atrio, Pixel Lab, pasillo corrupto y Núcleo Civitas, con Mr. Palomo, seis enemigos y el Núcleo Viral. El influencer está basado en el boceto aportado por Sebastián. Sus cuatro zonas, personajes y amenazas usan pixel art cenital y la paleta morada de la misión 1. Esc/P abre la pausa común; el último resultado queda registrado durante la sesión de la aplicación. La confianza y desinformación de una misión completada se acumulan en el estado de ciudad mostrado en el selector. El rumor usa el AVL de publicaciones y un grafo dirigido de difusión; otro grafo valida las rutas de NovaMall. [Controles, arte y estado técnico](docs/mision3/README.md).

## Misiones 4 y 6 integradas desde la entrega recibida

La misión 4, «La plaza que dejó de escuchar», sigue a Juan durante su campaña: conducción, plaza, preguntas del público, rumores y tres combates. La misión 6, «Cielos de campaña», permite uno o dos jugadores en teclado, con vuelo, dron de tres fases e entrevista. Ambas se seleccionan desde Jugar y utilizan la misma ventana de 1280 × 720, tipografía, marcos, pausa y guardados del proyecto. Esc/P pausa; H abre controles. Mando compatible para el primer jugador. Sus modalidades son locales; las salas TCP siguen disponibles en las misiones 5 y 7. [Misión 4](docs/mision4/README.md) · [Misión 6](docs/mision6/README.md) · [Integración y validación](docs/integracion-misiones46/README.md).

## Entrega 1 implementada

Caso preservado de la Entrega 1, «El helipuerto de palomas»: explora el parque, lee Civitas, encuentra el aviso y el testimonio, elige una acción y observa sus consecuencias. Conserva cuatro roles y cuatro variantes por semilla. Ya no se ofrece al jugador desde el menú principal.

El AVL propio organiza publicaciones por (riesgo,ID): inserción, búsqueda, eliminación, reinserción por cambio de riesgo, cuatro rotaciones y recorridos. El árbol n-ario inserta opciones al conseguir evidencia y poda alternativas al cerrar el caso. Ambos tienen historial visual de estados, búsqueda y recorridos en la GUI.

La visualización aplica el apartado 28.1 de la documentación integral: T abre una consulta opcional con **Radar de publicaciones** y **Camino de decisiones**, una pestaña a la vez y siempre con el estado actual. **Abrir Laboratorio** muestra claves, alturas, FE, operaciones, recorridos, búsqueda y estados históricos asociados a su evento. T, Esc y Volver regresan al punto de entrada y conservan la selección. También hay acceso al Laboratorio desde menú, ayuda y resultado de una sesión.

Las acciones muestran avisos explicando el cambio y su causa. Se pueden ocultar en Ayuda y siguen disponibles al consultar las estructuras. Las reglas y los algoritmos de la entrega anterior se conservaron: la prioridad de #1 baja a 8 **al reunir ambas fuentes** (30 → 8, o 75 → 8 si antes se compartió), no al comenzar la verificación.

Incluye mapa de tiles con colisiones y cámara, personajes animados, objetos independientes, ayuda, navegación por teclado, contraste alto, movimiento reducido, plazo ampliado, puntuación, siete indicadores y 148 PNG de ejecución. La sesión local mantiene la autoridad sobre reglas y evita duplicar efectos.

Las misiones 5 y 7 admiten dos clientes por red local en sus salas correspondientes. La sala general de 2 a 4 jugadores, el grafo territorial compartido entre escenarios, la misión 8 y los finales electorales de siete días siguen pendientes. Cada misión disponible tiene su propio cierre; la misión 7 conserva los apoyos y antecedentes para la elección definitiva de la misión 8.

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

El juego reconoce un mando Xbox o PlayStation conectado al iniciar y permite recorrer el menú, elegir dificultad, jugar las tres misiones disponibles y usar la pausa sin teclado. Conecta el mando y ejecuta `bash jugar.sh` (o `jugar.bat` en Windows) después de construir el paquete. El paquete incluye JInput y sus bibliotecas nativas; al iniciar desde Maven o el IDE, estas se preparan automáticamente desde la dependencia. El teclado y el ratón siguen disponibles.

| Mando | Menús y misión 1 | Misión 3 |
| --- | --- | --- |
| Stick izquierdo / cruceta | Navegar; mover a Sofi | Mover al Influencer |
| Stick derecho | — | Apuntar PING |
| A / ✕ | Confirmar; hablar | Interactuar, avanzar diálogo; repetir al final |
| B / ○ | Volver; abrir o cerrar pausa durante la misión | Abrir o cerrar pausa |
| X / □ | Abrir Civitas en el parque | PING |
| Y / △ | Abrir estructuras | LIVE BURST |
| LB / L1 | Mover el foco hacia arriba | MODO PRIVADO mientras se mantenga |
| RB / R1 | Mover el foco hacia abajo | SIGNAL DASH |
| LT / L2 | — | FLASH |
| RT / R2 | — | PING |
| Menú / Options | Pausa | Pausa |
| Vista / Share | Ayuda | Pausa con controles |

En la misión 2, el stick izquierdo mueve a la periodista, RB/R1 permite correr, X/□ lanza el salto hacia delante y A/✕ avanza los diálogos o repara la terminal cercana con una pulsación. Menú/Options abre la pausa. B/○ también abre la pausa; allí se puede volver al menú principal.

En el Laboratorio, el campo de búsqueda abre un teclado numérico manejable con la cruceta o el stick y A; X borra y B cierra. En pausa, elige «Continuar», «Ver controles» o «Salir al selector». Si el mando se desconecta durante una misión, el juego se pausa. La detección de un mando nuevo se realiza al iniciar la aplicación. En Linux, la cuenta que ejecuta el juego necesita acceso a los dispositivos `/dev/input/event*`.

WASD/flechas: movimiento; E: interactuar; en misión 2, Shift corre y Espacio salta durante la defensa. C: Civitas; I/J: evidencias; T: abrir/cerrar la consulta de árboles; H: ayuda; Esc: volver/menú. Tab, Mayús+Tab y Enter permiten activar los controles. En el Laboratorio AVL, enfoca el campo ID antes de escribir; admite cursor, flechas, Inicio/Fin, Retroceso, Suprimir y Enter. Busca y recorre el **estado mostrado**, incluso si es histórico. «Animar nodos» conserva ese estado; «Anterior/Siguiente» cambian la operación. Con movimiento reducido se avanza manualmente.

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


## Misión 5 · El servidor crítico y sala LAN

Ciudadano y periodista entran al servidor en una batalla de dos corazones: cinco rondas con quince patrones, salto y gravedad, haces dirigidos, vida individual, precisión, publicaciones y decisiones compartidas. Usa la oficina y los fotogramas de las misiones 1/2, más escenario y sprites nuevos con la paleta actual. Admite individual con solo el ciudadano (un corazón), dos personas en un teclado o dos clientes por sockets TCP, puerto 48505. Hay checkpoints por modo y registro duradero de puntos y campaña por sala. [Instrucciones, controles y guardados](docs/mision5/README.md).

## Misión 7 · La carrera por la primicia

Periodista e influencer persiguen un anuncio electoral sospechoso hasta el parque. Historia, rivalidad, cuenta atrás, carrera de avenida, noticias con decisiones, ayudas, sabotajes anunciados, llegada y desenlace funcionan en la misma ventana. Admite individual con cualquiera de los dos roles frente a CPU, dos personas en teclado o sala competitiva TCP 48507. Hay tres dificultades y control por pulsaciones o manteniendo el botón, con igual velocidad máxima. La periodista contrasta más rápido; el influencer alcanza a más grupos del grafo social.

Se reutilizan oficina, NovaMall, parque, protagonista ciudadano, Palomo y los movimientos de los personajes existentes. Se crearon una avenida y 48 cuadros originales de reacciones, ciudadanos y objetos con la paleta actual. El AVL organiza noticias; el árbol registra decisiones y el grafo propaga aclaraciones y rumores que cambian los ciudadanos y obstáculos. Los puntos por la primicia se separan de la reputación y el resultado colectivo. Juan queda excluido si se confirma un pacto corrupto; permanecer honesto y reunir apoyo le permite competir. La misión 7 guarda apoyos provisionales y antecedentes, sin proclamar un ganador definitivo. [Controles, arte, red, guardados y verificación](docs/mision7/README.md).
