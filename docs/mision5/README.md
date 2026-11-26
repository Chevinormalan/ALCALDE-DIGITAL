# Misión 5 · El servidor crítico

> Campaña vigente: esta misión requiere ganar la anterior (excepto la misión 1). Esc/P/Menú abre la pausa común y G guarda. El acceso, la recuperación y el guardado se coordinan desde el mismo sistema. Consulta [campaña y guardados](../campana/README.md); sus reglas sustituyen los menús particulares descritos en notas históricas inferiores.

Esta es la misión del boceto del ciudadano y la periodista dentro del computador. Se integra en **Jugar → Misión 5**, en la misma aplicación Processing de 1280 × 720. Las misiones 1, 2 y 3 siguen siendo individuales. La misión 6 de la caravana y sus guiones anteriores permanecen retirados.

## Recorrido

El ciudadano entra a la oficina de la periodista. Dialogan, aparece Mr. Palomo con su celular, hackea el servidor y se marcha volando. El ciudadano entra por la puerta superior y la periodista parte de su puesto. El paso del diálogo al movimiento conserva sus posiciones. En cooperativo ambos se acercan al computador; en individual solo se absorbe al ciudadano y la periodista mantiene el enlace desde la oficina. Las luces amarillas dan paso a la absorción. La oficina, la periodista y sus 16 cuadros de caminata proceden de misión 2; el ciudadano usa los 16 cuadros actuales de Sofi de misión 1, respetando su orden de direcciones. Mr. Palomo conserva su arte y vuelo existentes.

En individual hay un solo corazón, el del ciudadano. En cooperativo hay dos corazones numerados y de colores distintos. Cada jugador tiene cinco vidas de conexión e invulnerabilidad individual tras un impacto. No chocan entre sí. Solo en cooperativo, si un jugador cae y el otro sobrevive a la oleada, vuelve con dos vidas una sola vez por partida. En individual no hay acompañante ni rescate automático: perder las cinco vidas del ciudadano termina la defensa. En cooperativo se pierde cuando ambos caen.

Son cinco rondas de tres patrones originales y un turno de respuesta conjunta. Los quince patrones combinan paquetes cruzados, quietud cian, pasillos, salto de altura variable, plataformas, gravedad lateral, columnas, haces con objetivo fijado y pulsos que alternan el borde y el centro de la arena. Entre patrones hay una transición breve de 0,6 segundos, con la arena visible y movimiento habilitado; se retiró la tarjeta «Patrón 1, 2…». Los haces conservan su aviso previo. Soltar salto limita su altura; caer por gravedad cuenta como movimiento ante un paquete cian. Los haces alternan su objetivo entre los jugadores vivos. La cantidad de obstáculos no se duplica en cooperativo.

Cada patrón contiene tres oleadas con variantes elegidas por la semilla. Los carriles libres cambian de una oleada a otra. Los paquetes, paredes, haces y plataformas tienen fotogramas pixelados. Ámbar indica el aviso y su progreso, magenta marca la descarga que se esquiva, y cian señala cuándo mantenerse quieto. Los fotogramas avanzan con el reloj de la simulación. Consulta la [comprobación de refugios y rutas](PASABILIDAD.md).

En Antenas gemelas, el haz y la lluvia de columnas van seguidos de un cuadrito rastreador. El aviso identifica al jugador objetivo, J1 en individual y J2 en cooperativo si sigue en combate. El cuadrito gira mientras lo persigue, se detiene para marcar la ruta punteada y se lanza en línea recta. Durante esa pausa puedes esquivar hacia un lado. El cuadrito reutiliza cuatro fotogramas del virus y se disipa al final del recorrido.

En Tormenta del cortafuegos, el patrón 15, dos tandas de cuadritos cruzan la arena. Sus puntos de entrada y direcciones cambian según la semilla. El aviso punteado muestra hacia dónde viajan, y la estela acompaña los cuatro fotogramas del virus. El pulso cian va después de las tandas. En Difícil aparecen dos cuadritos rastreadores, uno antes de las tandas y otro después del cian. En cooperativo avisan a J1 y J2 por turnos antes de perseguirlos.

En Fácil y Normal, al completar cada ronda de tres ataques, cada superviviente recupera automáticamente un corazón, hasta un máximo de cinco. En Difícil no hay recuperación automática. Esta ayuda no revive a un jugador caído ni suma puntos; el rescate cooperativo y la acción de reparación mantienen sus reglas propias. El servidor aplica la recuperación una sola vez antes del turno y la incluye en el historial y los guardados.

Los cuadros de conversación de entrada, salida y decisión final usan los marcos ornamentados de misión 1. La conversación incluye una placa con el nombre, el retrato del interlocutor y el botón Continuar, además de E/Enter/A. Avanzar una frase conserva el reloj de la escena: la animación de regreso a la oficina continúa y el ciudadano no vuelve al computador al confirmar cada línea.

Cada turno permite verificar una publicación, responder con un pulso de precisión o reparar al compañero (o recuperar una vida propia en individual). La barra oscilante fija la precisión al confirmar; el servidor calcula el resultado. En individual cada turno y la decisión final dependen únicamente del ciudadano; en cooperativo las elecciones de ambos forman un solo turno. Verificar retira del AVL el rumor de mayor riesgo, deja historial y ralentiza futuros paquetes; reparar recupera una vida del compañero. Las rutas se registran en el árbol de decisiones. Al terminar cinco rondas ambos eligen entre aislar el servidor o perdonarlo; perdonar lleva a un último ataque anunciado y esquivable, no a una derrota automática. Sobrevivir permite expulsar a los personajes y cerrar la misión en la oficina.

## Modos y controles

- **Individual:** solo el ciudadano juega dentro del servidor: un corazón, cinco vidas, sus propios puntos y decisiones. La periodista es un NPC narrativo de la oficina y no combate. WASD/flechas, E/Enter y Espacio para saltar.
- **Dos en el mismo teclado:** ciudadano WASD/E/Espacio; periodista flechas/Enter/Shift. Durante la respuesta: 1/2/3 para ciudadano y 7/8/9 para periodista. Ambos usan una sola sesión local autoritativa.
- **Crear sala LAN:** el anfitrión ocupa el puesto de Ciudadano y ve «Partida creada · 1 de 2». La misión empieza al conectarse la Periodista.
- **Unirse LAN:** el buscador muestra las salas de misión 5 con cupo. Selecciona una sala para ocupar su puesto disponible. «Conectar por IP» permite indicar IPv4 o nombre del equipo y elegir puesto.
- **Mando:** stick izquierdo mueve, X salta, A confirma, cruceta navega, Menú pausa/reanuda, B vuelve desde la ayuda al menú de pausa, Vista abre ayuda, LB muestra estructuras, Y alterna continuar/nueva partida en configuración.
- **Pausa:** Esc/P abre el menú; arriba/abajo o Tab eligen y Enter/E/A confirma. Esc vuelve desde ayuda o estructuras a la pausa; Esc/P desde el menú reanuda. El ratón permite elegir todas las opciones. Guardar y salir es una opción explícita del menú; si sale el anfitrión se cierra su sala. Un invitado puede volver a unirse al mismo rol mientras el anfitrión permanezca abierto.
- **Ayuda:** el botón «Ayuda · momentos y patrones» abre la ronda actual. Siete páginas: controles/reglas, cinco rondas con tres patrones cada una y decisión/ataque final. Cada patrón explica qué sucede y cómo esquivarlo. Clic en las pestañas, flechas izquierda/derecha o botones Anterior/Siguiente cambian de página. El botón Volver o Esc regresa a la pausa sin reanudar.
- H abre la ayuda durante el juego; T muestra estructuras; G solicita un guardado inmediato. La pausa detiene reloj, ataques, vidas, invulnerabilidad, animaciones y barra de precisión, también para ambos clientes LAN. Los clics y teclas de un panel se consumen allí. Al reanudar, las teclas de movimiento que seguían pulsadas requieren soltarse; el stick debe volver al centro para evitar desplazamientos involuntarios. Perder foco pausa la partida y conserva la ayuda si ya estaba abierta.

En LAN cada jugador controla únicamente su rol. Las confirmaciones de ambos solo se requieren en cooperativo; en individual no se espera ni se simula una respuesta del segundo rol. Hay ocho segundos de espera máxima de lectura en una conexión: una desconexión detectada pausa la simulación y conserva el estado. Tras reconectar ambos, P reanuda.

## Sockets y autoridad

El protocolo de batalla está reservado a la misión **5**. El servidor acepta dos roles y utiliza el puerto **TCP 48505**. El servidor simula 60 pasos por segundo y mantiene reloj, azar con semilla, jugadores, obstáculos, objetivos, colisiones, publicaciones, relaciones de difusión, puntuaciones y turnos. Los clientes intercambian órdenes y vistas aproximadamente cada 25 ms; no envían valores de vida, puntos, resultado ni posición absoluta. El rol pertenece a la conexión y las secuencias repetidas se descartan.

La prueba automática usa dos clientes TCP en el mismo equipo; una sesión entre dos computadores físicos depende de que ambos estén en la misma red y de que el puerto del anfitrión sea accesible. No se requieren internet, cuentas externas ni dependencias nuevas.

Servidor dedicado opcional desde la carpeta del juego:

```bash
java -cp 'distribucion/alcalde-digital.jar:distribucion/lib/*' alcaldedigital.app.ServidorSalaApp
```

En ese caso ambos jugadores eligen Unirse → Conectar por IP y seleccionan roles distintos. `-Dad.continuar=true` carga el guardado LAN disponible al iniciar el servidor dedicado.

## Guardados y campaña

Por defecto se escriben en `~/.local/state/alcalde-digital/`:

- `mision5-solo.bin`, `mision5-coop.bin`, `mision5-lan.bin`: modos separados. Guardan identificador de sala, semilla, fase, ronda, patrón, reloj, posiciones, velocidades, vidas, puntos, recuperación, elecciones, historial, publicaciones, obstáculos y ciudad inicial. Se reemplazan de forma atómica en cada cambio de fase y cada diez segundos. G y salir también guardan.
- `campana.properties`: resultado por sala, puntuación de cada rol, mejores puntuaciones y siete indicadores. Acreditar una sala ya registrada no vuelve a sumar sus cambios. La ciudad del resultado proviene de la sesión autoritativa del anfitrión y se replica a ambos clientes.

En la configuración, R alterna partida nueva o continuar. Continuar restaura la instantánea. Si el guardado es anterior a este cambio y estaba en pleno combate, vuelve al inicio del patrón actual, conservando ronda, vidas, puntaje y decisiones. En LAN el anfitrión continúa y el otro jugador vuelve a unirse. Una partida nueva reemplaza el checkpoint de su modo, conservando los resultados ya registrados. Los errores de lectura/escritura se muestran en pantalla. `-Dad.guardados=RUTA` permite un directorio independiente; las pruebas usan carpetas temporales y no alteran el perfil del usuario. El guardado de campañas aquí se realiza al completar esta misión; las sesiones antiguas de las otras misiones siguen con su comportamiento previo.

## Arte y accesibilidad

Fondo #21152F, panel #38264B, violeta #604077, lila #BD91D5, texto #F5E8FB y acento amarillo #F4CB6A, con cian para el segundo corazón y la quietud. Se reutilizan Pixelify Sans, los sonidos, personajes y oficina existentes. El nuevo interior del servidor y la hoja de 12 cuadros (servidor, virus, emisor; cuatro por fila) están en `src/main/resources/cliente/mision5/`. Fuentes y prompts en `tools/arte/referencias-mision5/`. La animación de los obstáculos se muestra sobre su huella real de colisión.

Se conservan contraste alto y movimiento reducido en la presentación; los avisos de sonido también son visuales. Los corazones llevan números y el aviso explica la regla de quietud, para no depender solo del color. Los patrones son propios: no se incorporan sprites, música ni código del combate de Sans.

## Verificación

`bash herramientas.sh paquete` compila, prueba y genera el JAR. Las pruebas de red requieren permiso para abrir sockets locales. La suite incluye vida/invulnerabilidad independientes, quietud durante caída, salto variable, gravedad lateral, objetivo fijado, patrones, rescates, cinco rondas, elección final, codec y restauración exacta, resultado idempotente, roles TCP, desconexión/reconexión y una entrada completa desde configuración hasta absorción usando los controles de la interfaz.

Para inspección gráfica desde el JAR: `-Dad.mision5.integracion=true`, `-Dad.mision5.vista=oficina|haces|gravedad|turno|decision|final`, `-Dad.mision5.panel=pausa|ayuda|controles|estructuras`, `-Dad.mision5.captura=RUTA`. Estas vistas son fixtures de revisión con invulnerabilidad y no representan el balance de una partida normal. Una partida humana completa, un mando físico y una conexión entre dos computadores distintos requieren comprobación manual.

Validación del 12 de noviembre de 2026: **115 pruebas JUnit, cero fallos y cero errores**, paquete generado con BUILD SUCCESS. Incluye repetición de teclas al pausar, retorno desde ayuda, bloqueo de clics hacia el fondo, liberación del movimiento al reanudar, mando simulado, pérdida de foco, guardados pausados y congelación de la sala con dos clientes TCP. También se corrigió el bloqueo al reparar a un compañero caído: solo actúan quienes estaban vivos al confirmar el turno, aunque la reparación reviva al otro rol. La prueba cubre ambos roles y la continuación desde un guardado. Se mantienen las 2.970 rutas aisladas y 24 recorridos completos de pasabilidad; un impacto en el último paso de una ronda también se detecta aunque la recuperación restituya ese corazón. Se revisaron capturas Processing de [pausa](vista-pausa-renovada.png), [guía de patrones](vista-ayuda-patrones.png), [controles](vista-ayuda-controles.png) y [ayuda final](vista-ayuda-final.png). Los doce cuadros de la hoja nueva son distintos y tienen alfa real. El mando físico y una LAN entre dos computadores siguen pendientes de comprobación manual.


Corrección de ubicaciones e individual: el render usa las posiciones de la sesión, sin colocar a ambos personajes abajo ni devolver al ciudadano a la puerta al acabar el diálogo. Entrada, conversación, absorción y regreso tienen posiciones transitables. Los checkpoints individuales anteriores se normalizan al cargarlos para no reactivar al acompañante. Capturas revisadas: [entrada superior](vista-entrada-corregida.png) e [individual con un corazón](vista-individual.png). Las pruebas nuevas cubren posiciones, continuidad del diálogo, ausencia de bot/puntos/confirmaciones del segundo rol, recuperación propia, derrota individual, objetivos de los haces y guardado.


## Dificultades y estilo

En la configuración se elige **Fácil, Normal o Difícil** con clic, izquierda/derecha, 1/2/3 o cruceta del mando. Normal es la selección inicial y ofrece el aviso y la separación de oleadas intermedios. La dificultad del anfitrión se transmite a ambos clientes y se conserva en el guardado. Al continuar se respeta el nivel guardado. Los archivos del formato anterior se cargan como Normal.

| Nivel | Velocidad de obstáculos | Intervalo entre llegadas | Aviso previo | Ancho de los pasillos |
| --- | ---: | ---: | ---: | ---: |
| Fácil | 75 % | 125 % | 135 % | 130 % |
| Normal | 100 % | 100 % | 100 % | 100 % |
| Difícil | 125 % | 85 % | 80 % | 80 % |

Se conservan velocidad del corazón, gravedad y plataformas para que los saltos sigan siendo controlables. La duración de los patrones y las cinco vidas se mantienen. Los recuadros usan directamente `TemaPixel.marco`, el mismo dibujo de bordes angulares, sombras, líneas interiores y adornos de misión 1. Se añadieron botones con selección, respuesta al ratón y acciones reales para modo, dificultad, turno, decisión, pausa y resultado.

[Verificación de rutas y límites de la comprobación](PASABILIDAD.md).
