# Misión 3 · Civitas en Rojo

> Campaña vigente: esta misión requiere ganar la anterior (excepto la misión 1). Esc/P/Menú abre la pausa común y G guarda. El acceso, la recuperación y el guardado se coordinan desde el mismo sistema. Consulta [campaña y guardados](../campana/README.md); sus reglas sustituyen los menús particulares descritos en notas históricas inferiores.

## Integración y recorrido

La versión recibida en `/home/sebas/Descargas/LabEST-Mision3/CivitasEnRojo` se incorporó al proyecto Maven como `alcaldedigital.cliente.mision3.MisionTresSketch`. Conserva la progresión por Atrio, Pixel Lab, pasillo corrupto y Núcleo Civitas, las seis clases de enemigo, el combate, los diálogos y el final del prototipo más reciente. No se incorporaron los candidatos de la versión histórica del PDF.

La selección se hace desde **Jugar → Misión 3**. `JuegoCliente` la trata como una pantalla del juego principal. `ClienteSketch` dibuja la misión directamente en su única superficie de 1280 × 720: ya no crea un lienzo interno de 1152 × 648 ni márgenes de 64 × 36. Las coordenadas lógicas del prototipo se escalan al tamaño completo de esa superficie, también para la entrada del ratón. **Esc/P** abre la pausa común. Al salir se conserva el resultado del intento, aunque si se abandona antes del epílogo queda sin completar. Se conserva la sesión de la misión 1 en caso de haberla iniciado previamente.

## Controles

- WASD o flechas: movimiento.
- F (o J anterior) o clic izquierdo: PING.
- Q (o K anterior) o clic derecho: FLASH.
- Espacio (o Mayús anterior): SIGNAL DASH.
- Control: MODO PRIVADO.
- L: LIVE BURST al completar LIVE.
- E o Enter: interactuar y avanzar diálogo.
- F3: datos y colisiones de depuración.
- P: abrir o cerrar la pausa.
- Esc/P: pausa común; desde la pausa, reanudar o volver de ayuda.
- R: volver a jugar desde el epílogo.

Con mando: stick izquierdo o cruceta para moverse, stick derecho para apuntar, A/✕ para interactuar y repetir al final, X/□ o RT/R2 para PING, LT/L2 para FLASH, RB/R1 para SIGNAL DASH, LB/L1 mantenido para MODO PRIVADO y Y/△ para LIVE BURST. Menú/Options o B/○ abren la pausa; Vista/Share abre los controles. La pausa presenta Continuar, Ver controles y Salir al menú. El mando debe estar conectado antes de abrir el juego.

## Recursos y estado técnico

El rediseño se encuentra en `src/main/resources/cliente/mision3/redisenio/`. Incluye cuatro mapas cenitales de NovaMall, el influencer basado en el boceto del usuario, seis tipos de enemigo, el Núcleo Viral y cuadros de Mr. Palomo derivados del mismo atlas que usa la misión 1. El influencer, cada enemigo móvil y el Núcleo Viral tienen **16 fotogramas dibujados**: cuatro poses para cada dirección (abajo, arriba, izquierda y derecha). La primera pose de cada dirección del influencer también se usa al detenerse, para que no cambie el perfil entre caminar y reposar. Mr. Palomo toma sus vistas de la fila superior original y los cuatro fotogramas de vuelo de la inferior; al caminar usa la vista lateral correspondiente sin girar entre perspectivas. Los 16 fotogramas nuevos del jefe muestran levitación, cables y pantallas desplazándose, sin convertirlo en un personaje que camina.

Los fondos, sprites y la interfaz usan pixel art morado con acentos amarillos; el estado corrupto añade magenta y el equipo tecnológico usa teal. La interfaz de misión 3 carga la misma `PixelifySans.ttf` que la misión 1. Pausa, panel Civitas, diálogos, HUD e indicaciones usan marcos angulares de píxel, y la pausa comparte posición y tamaño de panel y botones con la misión 1. Los recursos del prototipo siguen en `src/main/resources/cliente/mision3/` como referencia y respaldo, pero las imágenes de ejecución se cargan desde `redisenio/`. El audio original sigue activo. Los archivos fuente de la generación se guardaron en `tools/arte/referencias-mision3/` y `python3 tools/arte/preparar_mision3.py` reproduce los 163 PNG derivados.

## Jefe final móvil

La escena revela que la señal viral abandonó los servidores y adquirió cuerpo gracias a las publicaciones compartidas. Tras el diálogo, el Núcleo Viral se desprende del centro y recorre puntos dentro del círculo de combate. En fase 1 se mueve despacio; en fase 2 amplía su recorrido e invoca enemigos; en fase 3 aumenta la velocidad y, después de cada segundo ataque, anuncia con una línea y un marcador su **salto de señal** antes de ejecutarlo. Se detiene para anunciar los ataques y deja la ventana de recuperación previa. Su zona de impacto, proyectiles, sombra y distancia de LIVE BURST siguen su posición real. No atraviesa los límites circulares ni se desplaza encima del jugador sin aviso.

El escudo de fase 3 todavía exige LIVE BURST. Romperlo deja al menos un punto de vida para que el jugador pueda rematar al jefe. Al derrotarlo, fragmentos visuales regresan a las terminales, Civitas aísla las copias y comienza el final con las pantallas restauradas y Mr. Palomo.

## Colisiones, vuelo y avisos

Los cuatro escenarios tienen perfiles de colisión ajustados sobre sus objetos visibles. En Atrio bloquean kiosco, jardinera, fuente, banco y macetas; en Pixel Lab, mesas, muebles, árboles y bases de servidores; en el pasillo, escaparates, baranda y jardineras. El Núcleo limita al jugador al contorno circular de la pista, conservando su punto de entrada. Jugador y enemigos usan esas mismas huellas para moverse. Las bases de servidores bloquean el paso, pero no los proyectiles: siguen desactivándose con E, PING o FLASH. Los puntos de aparición y las rutas hacia cada salida están fuera de obstáculos.

Mr. Palomo reutiliza los cuatro cuadros de vuelo de la misión 1 al entrar y salir del Atrio, del pasillo y del epílogo. Se refleja el sprite cuando vuela hacia la izquierda, aterriza antes del diálogo y abandona la pantalla después de hablar. El pasillo espera su salida para abrir la transición; el fundido final del epílogo también espera a que termine de volar.

Los avisos de ataque usan anillos por bloques, líneas escalonadas, cruces de objetivo y paneles cuadrados. Se aplican a los seis enemigos, zonas peligrosas, Comentario Vivo, Núcleo Viral, salto de señal, proyectiles y ondas. Su tiempo y área de daño permanecen ligados a la misma lógica del combate.

La lógica de combate, escenas y diálogos sigue encapsulada en `MisionTresSketch` y todavía no está dividida en protocolo, sesión y render como la misión 1. La misión ya comparte ventana, selector, pausa, entrada y progreso de campaña con el juego principal. Su confianza, desinformación y habilidades se calculan durante la partida. Al completarla, `ProgresoCampana` aplica a `EstadoCiudad` la diferencia de confianza y desinformación respecto al inicio propio de esta misión (35 % y 8 %); un intento abandonado no cambia la ciudad. La misión 1 aplica de forma equivalente sus siete diferencias respecto a `EstadoCiudad.inicial()`. El estado acumulado se muestra en el selector mientras la aplicación permanece abierta. El guardado en disco recupera desde el inicio de la zona actual; el acumulado de campaña se persiste al guardar.

Las publicaciones de Civitas de la misión 3 usan el `ArbolAVLPublicaciones` del proyecto: el rumor se inserta, aumenta su riesgo al difundirse y el panel muestra la publicación de mayor prioridad. `GrafoDifusionCivitas` determina por BFS el orden de las cuentas que reciben el rumor; cada llegada posterior aumenta ligeramente la desinformación del combate. `GrafoZonasMisionTres` valida las conexiones entre escenarios. El árbol de decisiones de la misión 1 continúa activo allí; para la misión 3 todavía falta definir elecciones narrativas propias antes de conectarlo con una mecánica real.

Los fondos son imágenes planas cenitales. Los objetos altos siguen integrados en esas imágenes, por lo que su profundidad visual no se separa en capas del personaje. La prueba automática fuerza algunos encuentros y rutas, así que no equivale a una partida manual completa ni verifica el balance de dificultad.

## Verificación

`bash herramientas.sh paquete` compiló el juego y pasó 62 pruebas JUnit, incluidas las de Esc, salida por pausa y mando con datos simulados. Desde el JAR empaquetado, la autoprueba de la misión 3 informó `SELF TEST OK` para inicio, movimiento, colisiones, cuatro zonas, enemigos, habilidades, jefe y final. También comprueba obstáculos concretos, rutas, bases y desactivación de los tres servidores, vuelo de Palomo, movimiento del jefe y sincronización de su zona de impacto. La prueba gráfica verificó **selector → misión 3 → recorrido automático → regreso al selector** con `MISION_3_INTEGRACION_OK`. Se capturaron y revisaron visualmente las colisiones sobre los cuatro mapas, la entrada volando de Palomo y un aviso de ataque pixelado. La prueba con un mando físico queda pendiente porque este entorno negó acceso a `/dev/input/event*`. El original recibido y la carpeta `alcalde-digital-lab1-entrega1` se conservaron.

**Prompts y herramienta:** recursos generados con la herramienta integrada `imagegen`. El influencer se creó a partir del boceto del usuario y el parque de la misión 1 como referencia de estilo. Los cuatro fondos se pidieron como mapas cenitales 16:9 sin personajes ni interfaz. Los siete personajes móviles y el Núcleo Viral tienen hojas transparentes de 4 × 4 poses. Las imágenes completas y los derivados reproducibles están en las rutas indicadas arriba.
