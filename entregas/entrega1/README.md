# Alcalde Digital — Informe de la Entrega 1

## Presentación

En el presente proyecto desarrollamos la primera entrega de **Alcalde Digital: Promesas, Palomas y Wi-Fi**, un videojuego educativo en dos dimensiones construido con Java 21 y Processing 4.5.6 para la asignatura Estructura de Datos II.

Nuestro propósito en esta etapa fue implementar un capítulo local jugable en el que las estructuras de datos no aparecieran únicamente como una representación gráfica, sino que participaran directamente en las reglas y decisiones del juego. El resultado es el capítulo **«El helipuerto de palomas»**, ambientado en el parque de Ciudad Nova.

Durante la partida, el jugador selecciona uno de cuatro roles, consulta una publicación en la red ficticia Civitas, explora el parque, obtiene evidencias y decide cómo responder al rumor. Las acciones realizadas modifican las publicaciones activas, las opciones disponibles, la puntuación y siete indicadores de la ciudad.

## Alcance realizado

En esta entrega implementamos:

- Un capítulo local completo y reproducible con cuatro variantes narrativas.
- Cuatro roles jugables: ciudadano, periodista, influencer y candidato.
- Un mapa construido con tiles, objetos, colisiones, cámara y minimapa.
- Interacción con un aviso municipal, una administradora del parque y la campaña de un candidato.
- La red social ficticia Civitas con las acciones compartir, verificar, reportar e ignorar.
- Obtención de evidencias y desbloqueo de nuevas decisiones después de contrastar dos fuentes.
- Puntuación y siete indicadores: información verificada, confianza, convivencia, bienestar, participación, desinformación y conflictos.
- Un árbol AVL para organizar publicaciones activas según riesgo e identificador.
- Un árbol n-ario para representar las rutas de decisión del capítulo.
- Una vista narrativa para el jugador y un Laboratorio técnico para consultar operaciones, recorridos e instantáneas históricas.
- Interfaz pixel, fuente Pixelify Sans, transiciones, navegación por teclado, alto contraste y movimiento reducido.
- Una sesión local que valida las acciones, evita efectos duplicados y conserva un registro técnico de las solicitudes.
- Pruebas automatizadas, una autoprueba independiente y una distribución ejecutable.

El alcance de la Entrega 1 es local. Los grafos funcionales, la comunicación mediante sockets, el multijugador, los escenarios restantes, la campaña de siete días y los finales electorales forman parte de etapas posteriores.

## Funcionamiento general

El flujo principal de la aplicación es el siguiente:

```text
ClienteApp
   └── ClienteSketch
         ├── JuegoCliente
         │     └── SesionJuego
         │            └── SesionLocal
         │                  ├── ArbolAVLPublicaciones
         │                  ├── ArbolDecisiones
         │                  ├── EstadoCiudad
         │                  └── EventoParque
         └── RenderJuego
```

`ClienteApp` inicia la aplicación. `ClienteSketch` administra la ventana y el ciclo de dibujo de Processing. `JuegoCliente` interpreta los controles y administra la navegación entre pantallas. `SesionLocal` representa la autoridad de la partida: valida proximidad, evidencia, plazo, acciones ya realizadas y consecuencias. `RenderJuego` dibuja el estado informado por la sesión.

Esta separación permite que la interfaz solicite acciones sin modificar directamente los árboles, los puntos o los indicadores. También prepara el proyecto para sustituir en el futuro la sesión local por una implementación remota.

## Estructuras de datos implementadas

### Árbol AVL de publicaciones

Implementamos `ArbolAVLPublicaciones` para mantener las publicaciones activas ordenadas por la clave compuesta `(riesgo, ID)`. El riesgo representa prioridad de atención y el identificador resuelve empates.

El árbol admite inserción, búsqueda, eliminación, actualización mediante reinserción, recorridos inorden, preorden, postorden e inverso, cálculo de alturas y factores de equilibrio, y rotaciones simples y dobles. Asimismo, conserva instantáneas de sus operaciones para que puedan consultarse en el Laboratorio.

La estructura participa en el juego. Al comenzar el capítulo se insertan tres publicaciones con riesgos 30, 20 y 10, lo que provoca una rotación y deja el árbol equilibrado. Compartir el rumor eleva su riesgo; reunir las dos evidencias lo reduce; publicar contexto retira el rumor del índice activo e inserta una corrección.

### Árbol n-ario de decisiones

Implementamos `ArbolDecisiones` porque una situación narrativa puede ofrecer más de dos alternativas. Cada nodo posee un identificador, texto, estado de ejecución y una lista de hijos.

La estructura admite inserción, búsqueda, cálculo de rutas, ejecución de una ruta, poda de alternativas y recorridos en preorden, postorden y por niveles. Al verificar y reunir las dos fuentes, el juego inserta las opciones de publicar contexto y guardar evidencia. Si previamente se compartió el rumor, también habilita la rectificación. Al cerrar el caso se podan las alternativas no seleccionadas, mientras se conserva la ruta ejecutada.

## Modelo, reglas y sesión

Modelamos las publicaciones, sus claves, los roles, el estado de la ciudad, el mapa y las evidencias mediante clases específicas. Cada evidencia conserva identificador, fuente y contenido, mientras la interfaz continúa mostrando solamente la información que el jugador obtuvo.

Centralizamos los cambios de puntos e indicadores en `ReglasI1`. `SesionLocal` determina cuándo puede aplicarse cada regla. La sesión también mantiene la variante narrativa en privado, controla el plazo, valida colisiones y proximidad, archiva publicaciones retiradas y evita aplicar dos veces una solicitud con el mismo identificador.

Incorporamos `ConfiguracionSesion`, `ResultadoAccion` y `RegistroAccion`. Estos contratos permiten configurar rol, semilla y plazo; representar de forma estructurada el resultado y la versión de una acción; y registrar los cambios producidos. El método utilizado por el cliente conserva su respuesta textual para mantener la compatibilidad con la interfaz actual.

## Interfaz y experiencia de juego

Construimos una interfaz de 1280 × 720 píxeles con estética pixel. El parque utiliza tiles de 16 × 16 píxeles, sprites animados y objetos independientes. La cámara sigue al personaje y el orden de dibujo considera la posición de los pies para representar correctamente la profundidad.

La interfaz incluye menú, selección de roles, parque, Civitas, archivo de evidencias, consulta de árboles, ayuda y resultado. La vista del jugador presenta el AVL como **Radar de publicaciones** y el árbol n-ario como **Camino de decisiones**. El Laboratorio conserva la información académica: claves, alturas, factores de equilibrio, operaciones, recorridos, búsqueda por ID y estados históricos.

También implementamos navegación por teclado, foco visible, contraste alto, movimiento reducido, ampliación del plazo y transiciones pixel. La fuente Pixelify Sans y su licencia están empaquetadas con el programa.

## Estructura del proyecto entregado

```text
alcalde-digital-lab1-entrega1/
├── pom.xml                         Configuración Maven, Java, Processing y JUnit
├── README.md                       Descripción general e instrucciones de uso
├── herramientas.sh                Pruebas, empaquetado y generación de evidencias
├── jugar.sh / jugar.bat            Inicio del juego en Linux/macOS y Windows
├── distribucion/
│   ├── alcalde-digital.jar         Aplicación preparada
│   └── lib/                        Processing Core
├── entregas/
│   └── entrega1/
│       └── README.md               Informe formal de esta entrega
├── src/
│   ├── main/
│   │   ├── java/alcaldedigital/
│   │   │   ├── app/                Puntos de entrada y utilidades
│   │   │   ├── cliente/            Control, renderizado y recursos
│   │   │   ├── compartido/         Modelos, estructuras y protocolo
│   │   │   ├── i1/                 Validación académica de estructuras
│   │   │   └── servidor/           Sesión, reglas y evento
│   │   └── resources/              Arte, fuente, textos y archivos JSON
│   └── test/java/alcaldedigital/   Pruebas automatizadas
├── target/                          Resultado reproducible de Maven
└── tools/                           Preparación de recursos visuales
```

Los archivos fuente mantenidos se encuentran en `src`. La carpeta `target` contiene productos generados por Maven, y `distribucion` contiene el paquete preparado para ejecutar el juego.

## Trabajo realizado por integrante

El trabajo se organizó por áreas de responsabilidad. Además de desarrollar cada componente, integramos los módulos sobre una misma base y comprobamos conjuntamente su compatibilidad.

### I1 — Forero: dominio, árboles y reglas

En esta área desarrollamos y consolidamos el modelo de publicaciones, claves, evidencias e indicadores de la ciudad. Implementamos el árbol AVL de publicaciones y el árbol n-ario de decisiones, junto con sus operaciones, recorridos e invariantes.

También centralizamos las consecuencias numéricas del capítulo, incorporamos la evidencia tipada y añadimos un validador académico y una autoprueba para comprobar el equilibrio del AVL, la unicidad de identificadores y la estructura del árbol n-ario. Las pruebas específicas de esta área verifican orden, rotaciones, eliminaciones, reindexación, recorridos y reglas.

### I2 — Sebastián Rivera Valbuena: cliente, interfaz e integración general

En esta área desarrollamos el cliente Processing, la navegación, los controles, el mapa por tiles, la cámara, las animaciones, la carga de recursos, la interfaz pixel, las transiciones y las opciones de accesibilidad. También construimos la visualización narrativa y técnica de los árboles, incluyendo su historial, recorridos y búsqueda.

Adicionalmente, coordinamos la preparación de la base común y realizamos trabajo transversal de integración: revisamos los aportes de las demás áreas, adaptamos las reglas y evidencias a la sesión vigente, completamos los contratos de configuración y registro, incorporamos los casos de contexto a las pruebas, verificamos que la jugabilidad no cambiara, generamos la distribución y consolidamos la documentación de la entrega.

### I3 — Gaby: sesión y contratos de comunicación

En esta área definimos la separación entre el cliente y la autoridad de la partida mediante `SesionJuego` y `SesionLocal`. Implementamos el control de versiones, la idempotencia por identificador de solicitud y los contratos `ConfiguracionSesion`, `ResultadoAccion` y `RegistroAccion`.

La integración mantiene el método textual consumido por el cliente y añade un resultado estructurado para futuras extensiones. Los registros permiten consultar la acción solicitada, su aceptación, la versión y los puntos e indicadores antes y después de resolverla.

### I4 — Said: contenido, variantes y validación del capítulo

En esta área organizamos el contenido del evento del parque y sus cuatro variantes narrativas. El cargador `EventoParque` obtiene desde JSON el aviso municipal, el testimonio y la conclusión correspondiente a cada variante.

También incorporamos criterios de acción y contexto a las pruebas de regresión. Estos casos comprueban que compartir sin verificar, reportar con o sin evidencia, ignorar, publicar contexto, guardar evidencia y rectificar produzcan las consecuencias definidas en los cuatro roles y las cuatro variantes.

## Validación

Compilamos el proyecto desde cero y ejecutamos **40 pruebas JUnit**, con cero fallos, cero errores y cero pruebas omitidas. La distribución actual también supera una autoprueba independiente de **49 comprobaciones**.

Las nueve suites verifican:

- Orden compuesto de publicaciones.
- Rotaciones, eliminación, alturas, factores de equilibrio y mutaciones del AVL.
- Inserción, búsqueda, rutas, recorridos y poda del árbol n-ario.
- Reglas de sesión, evidencias, variantes, roles, plazo, colisiones e idempotencia.
- Consecuencias en los siete indicadores y la puntuación.
- Pantallas, recursos y renderizado reproducible.
- Vista narrativa, Laboratorio, historial, búsqueda y navegación.
- Fuente, transiciones y movimiento reducido.
- Configuración, resultados estructurados, registros y reintentos.

La integración fue comparada con el comportamiento anterior mediante 448 recorridos de acciones. Coincidieron respuestas, puntos, indicadores, estados de ambos árboles, archivo e historial. También se compararon las capturas del render antes y después de integrar las mejoras, sin encontrar cambios visuales no previstos.

## Ejecución

La clase principal del juego es:

```text
alcaldedigital.app.ClienteApp
```

Desde la raíz del proyecto podemos ejecutar:

```bash
bash jugar.sh
```

En Windows utilizamos `jugar.bat`. Para compilar, probar y preparar nuevamente la distribución usamos:

```bash
bash herramientas.sh test
bash herramientas.sh paquete
```

La ejecución desde el código fuente requiere JDK 21 o posterior y Maven 3.9 o posterior. Processing Core y JUnit se administran mediante Maven.

## Resultado de la entrega

Con esta entrega obtuvimos un capítulo jugable en el que las estructuras de datos están vinculadas a decisiones observables. El AVL representa la prioridad de publicaciones y el árbol n-ario representa las rutas disponibles y ejecutadas. La sesión mantiene las reglas oficiales y la interfaz permite comprender tanto la consecuencia narrativa como la operación académica correspondiente.

El proyecto queda preparado para continuar con grafos, propagación social, rutas territoriales y comunicación multijugador en las siguientes entregas, manteniendo los árboles y contratos desarrollados en esta primera etapa.
