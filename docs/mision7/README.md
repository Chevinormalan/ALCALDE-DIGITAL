# Misión 7 · La carrera por la primicia

> Campaña vigente: esta misión requiere ganar la anterior (excepto la misión 1). Esc/P/Menú abre la pausa común y G guarda. El acceso, la recuperación y el guardado se coordinan desde el mismo sistema. Consulta [campaña y guardados](../campana/README.md); sus reglas sustituyen los menús particulares descritos en notas históricas inferiores.

Implementación integrada en **Jugar → Misión 7**, sobre el mismo lienzo Processing de 1280 × 720. No recupera el antiguo guion retirado: desarrolla el nuevo boceto y el análisis aprobado del anuncio, la rivalidad, la carrera y el descubrimiento en el parque.

## Recorrido

Una voz misteriosa anuncia que adelantaron el anuncio electoral y pretende celebrar una posesión antes de cerrar el recuento. Se muestran cuatro candidaturas y apoyos provisionales, sin botones para escoger al ganador. Periodista e influencer reaccionan simultáneamente: «Esta noticia tiene que ser mía». La periodista abandona la oficina conocida de misión 5 y el influencer sale de NovaMall. Chocan en la avenida y Mr. Palomo los envía al parque, entretenido con la rivalidad.

Después de una cuenta de tres segundos comienza la carrera. La avenida mide 9.600 unidades y sus tres momentos son salida, rivalidad y aproximación al parque. La cámara mantiene a ambos corredores visibles; los decorados conservan sus proporciones. Hay ciudadanos, vallas, baterías, escudos y distracciones. Las vallas dejan espacio vertical para esquivar. Las distracciones fijan el carril y lo anuncian durante un segundo; cambiar de carril permite evitarlas. Las penalizaciones son breves y no inmovilizan indefinidamente.

Cada corredor recibe cuatro acontecimientos de ocho segundos: mensaje de atajo, acusación contra el rival, pacto oculto de campaña y publicación que confunde compartir con votar. Se puede contrastar, aprovechar/difundir o continuar. Los rumores dan ventajas temporales a costa de reputación y ciudad. Las aclaraciones mejoran información, confianza y convivencia; también se propagan por el grafo. La periodista contrasta en 24 pasos y llega a tres grupos; el influencer emplea 45 pasos y alcanza siete. El movimiento y la velocidad máxima son comunes a personas y CPU.

La noticia identifica la candidatura de la cuenta implicada. Contrastar una ruta auténtica mejora su apoyo; descubrir una ruta falsa, un montaje contra el rival o una proclamación no oficial lo reduce. Difundir sin comprobar puede aumentar su alcance y apoyo, pero resta confianza y produce conflictos. El aviso y el historial indican el candidato y el cambio aplicado, para hacer visible cómo se construyen los apoyos electorales.

El primero obtiene la primicia; si ambos cruzan en el mismo instante, se reconoce empate. La escena de llegada reúne a ambos y descubre a una multitud ante «Comparte para confirmar al próximo alcalde». El epílogo identifica la manipulación, conserva evidencias y deja la elección definitiva para misión 8. Los puntos de llegada, la reputación y los siete indicadores colectivos se muestran por separado. El guion ejecutado está en `compartido/carrera/HistoriaCarrera.java`.

## Juan y la elección

`EstadoElectoral` conserva apoyos, confianza, publicaciones verificadas, conflictos, participación e historial para cuatro candidaturas. Los valores iniciales dependen del estado acumulado de Ciudad Nova. El respaldo provisional combina `3 × apoyo + confianza + 2 × verificadas + participación − 2 × conflictos`.

- Juan honesto puede ganar si obtiene respaldo suficiente; otro candidato puede superarlo.
- Confirmar el pacto registra que Juan autorizó un pago oculto y un montaje. Juan queda corrupto y excluido del liderazgo electoral.
- Leer la propuesta solo abre una advertencia. Se debe confirmar una segunda vez la opción que indica explícitamente que Juan no podrá ganar: 2/8, clic en ese botón, E con esa opción seleccionada o A del mando. Rechazar o dejar vencer la oferta no ejecuta el pacto. Q/K/Y usan ayudas y nunca aceptan un pacto.
- La corrupción es irreversible dentro de la campaña. Más reputación, reiniciar una carrera o cargar un checkpoint anterior no la borra.
- La CPU nunca acepta el pacto. Las decisiones humanas determinan esa condición.
- El cierre de la misión 7 muestra el resultado de la carrera, los puntajes y los indicadores de la ciudad. Los antecedentes electorales se registran para el escrutinio de la misión 8.

## Modos y controles

Hay Individual contra CPU, Dos personas en teclado, Crear sala LAN y Unirse LAN; Fácil, Normal y Difícil. La dificultad cambia ritmo, precisión y errores de CPU, sin otorgarle velocidad superior ni inmunidad. Se puede elegir periodista o influencer. El anfitrión elige su personaje y el invitado ocupa el personaje libre al seleccionar la sala.

| Acción | Individual / LAN | Periodista en teclado compartido | Influencer en teclado compartido |
|---|---|---|---|
| Cambiar carril | W/S o flechas | W/S | Flechas arriba/abajo |
| Correr | Espacio | Espacio | Mayús |
| Usar ayuda | Q | Q | K |
| Elegir noticia | 1 / 2 / 3 | 1 / 2 / 3 | 7 / 8 / 9 |
| Confirmar noticia seleccionada | E / Enter | E | Las teclas 7 / 8 / 9 responden directamente |
| Avanzar diálogo | E / Enter | E | Enter |

«Mantener» genera un ritmo de cinco pulsaciones por segundo. «Pulsar» acepta una nueva pulsación cada diez pasos (seis por segundo como máximo). Ambos modos tienen velocidad máxima de cuatro unidades por paso. La repetición automática del teclado no crea pulsaciones adicionales. Se puede cambiar la modalidad desde la pausa. En dos personas, J2 usa este mismo teclado como influencer; el mando y el botón de asistencia corresponden al rol seleccionado en configuración.

Mando: stick cambia de carril, RB corre (X sigue como alternativa), cruceta izquierda/derecha selecciona una noticia y A la confirma; Y usa una ayuda. A avanza diálogos. Menú pausa/reanuda, B vuelve desde una consulta a pausa y Vista abre ayuda. En configuración, LB cambia el personaje e Y alterna mantener/pulsar. Se mantienen contraste alto y movimiento reducido.

Esc/P pausa; H ayuda; T estructuras; V campaña electoral; G guardar. La pausa tiene botones de continuar, ayuda, estructuras, campaña, modalidad de carrera, guardar y salir. Ayuda, estructuras y campaña mantienen la simulación detenida. Todos sus clics y teclas se consumen sin alcanzar pantallas de fondo. Al reanudar se deben soltar las teclas mantenidas y centrar el mando. Perder foco pausa la carrera.

## Estructuras y autoridad

`MotorCarrera` simula a 60 pasos/s. La escena gráfica recibe vistas; no decide posiciones, puntuaciones, noticias, azar, liderazgo o corrupción.

- AVL propio: inserta las noticias por riesgo/ID, busca publicaciones, elimina las contrastadas y reinserta las difundidas con riesgo mayor. La consulta muestra su orden de prioridad.
- Árbol n-ario: organiza las cuatro noticias por cada rol y sus tres alternativas; registra las rutas ejecutadas y las recupera al cargar.
- Grafo dirigido de Civitas: BFS determina los destinatarios. Los mensajes llegan de forma escalonada cada veinte pasos; hay ondas pendientes persistidas. Los grupos cambian entre normal, celular, alterado y aclarado. Estas reacciones afectan a las aglomeraciones del recorrido.

Crear sala inicia `ServidorCarrera` en **TCP 48507**. Se admite un cliente por rol. El servidor vincula cada rol a su conexión, rechaza secuencias repetidas y no acepta que un cliente actúe por otro. Los clientes intercambian órdenes/vistas cada unos 25 ms, fuera del hilo gráfico. Desconectar pausa la sala; se puede reconectar con el mismo rol y reanudar cuando estén ambos. Una sala de carrera es independiente de la batalla de misión 5, que usa 48505. El buscador muestra las salas de la misión con cupos libres. Una sala nueva comienza cuando entran ambos roles.

## Guardados

Se utiliza `~/.local/state/alcalde-digital/`, o el directorio indicado mediante `-Dad.guardados=RUTA`:

- `mision7-solo-0.bin` y `mision7-solo-1.bin`: individual separado por personaje.
- `mision7-dos.bin`: teclado compartido.
- `mision7-lan.bin`: sala LAN del anfitrión.
- `campana.properties`: resultados idempotentes por sala, puntuación de cada humano, reputación, mejores resultados, ciudad y antecedentes electorales. Conserva las propiedades de misión 5.

Los checkpoints son binarios propios, versionados y escritos mediante reemplazo atómico. Guardan fase, línea de diálogo, reloj, semilla, corredor, velocidad, impulso, modo de control, noticias y plazos, objetos consumidos por rol, ayudas, advertencias, ondas, grupos, estructuras, reputación y campaña. Se guardan al cambiar fase, al resolver decisiones, cada diez segundos, con G y al salir.

La corrupción se registra en campaña inmediatamente al ejecutar el pacto. El resultado final se acredita una sola vez. Repetir desde la pantalla de resultados conserva la ciudad alcanzada. Las operaciones de campaña usan bloqueo entre procesos; cerrar la sala espera que sus hilos terminen antes del último guardado. Las pruebas y capturas usan directorios temporales y no reemplazan partidas reales.

## Recursos

Se reutilizan la oficina, la periodista y sus dieciséis cuadros de carrera de misión 2; los dieciséis cuadros de movimiento del influencer, NovaMall cenital y vuelo de Palomo de la misión 3; Sofi, el parque y ciudadanos de misión 1. Los diálogos, botones y marcos llaman al `TemaPixel` existente; se conservan Pixelify Sans y los sonidos previos.

Nuevos PNG en `src/main/resources/cliente/mision7/`:

| Archivo | Contenido |
|---|---|
| `avenida.png` | Panorama de calle cenital con fachadas, pantallas, aceras y dos franjas transitables. |
| `ciudadanos-estados.png` | 24 cuadros: dos ciudadanos existentes, cuatro poses por estado normal/celular/alterado, mirando a la derecha. Conservan identidad al reaccionar. |
| `reacciones.png` | Ocho cuadros: cuatro expresiones por protagonista, con sus diseños existentes. |
| `objetos.png` | Dieciséis cuadros: batería, escudo, notificación y valla; cuatro estados animados de cada objeto. |

Son **48 cuadros originales nuevos y un escenario**. Las direcciones completas de los protagonistas proceden de sus recursos existentes. No se sustituyeron por personajes distintos. La extracción de celdas y márgenes se hace al cargar, sin alterar los PNG originales. Paleta: #21152F, #38264B, #604077, #BD91D5, #F5E8FB y #F4CB6A, con cian/magenta para señales.

Se usó la herramienta integrada **imagegen**. Archivos fuente y prompts exactos: [referencias de arte](../../tools/arte/referencias-mision7/PROMPTS.md). Capturas de revisión: [anuncio](vista-anuncio.png), [rivalidad](vista-rivalidad.png), [dos personas](vista-dos.png), [pacto](vista-pacto.png), [ciudadanos](vista-ciudadanos.png), [campaña](vista-eleccion.png) y [resultado](vista-resultado.png).

### Presentación de la carrera

La pista dibuja los límites de la calzada, marcas de carril, la salida y la meta. Nova Prensa, Avenida Civitas, Pasaje Electoral y el parque usan distintas fachadas, colores y señales. La cámara deja espacio por delante, encuadra a ambos corredores en teclado compartido y sigue al jugador local en individual o LAN cuando se separan mucho.

La barra de avance reúne los marcadores de la Periodista y el Influencer. Los peatones de acera y los ciudadanos de la pista se distinguen por posición y señalización. Las franjas rayadas muestran el alcance vertical y el tiempo restante de cada distracción. Las tarjetas de noticias muestran la opción enfocada y su tiempo disponible, incluida la advertencia del pacto al jugar con dos personas. La presentación usa la `Vista` existente, con las mismas reglas, guardados y protocolo de red.

## Verificación

`bash herramientas.sh paquete` compila, ejecuta JUnit y actualiza la distribución. Las pruebas incluyen recorridos completos con cualquiera de los dos personajes y ambos humanos en las tres dificultades, límites de velocidad/pulsaciones, ayudas, colisiones, aviso de sabotaje, noticias, roles, difusión, AVL, decisiones, guardado/restauración exacta, corrupción explícita e irreversible, elegibilidad y apoyo, acreditación idempotente, pausa modal, controles, teclado compartido y dos clientes reales TCP con reconexión y checkpoint.

Las capturas usan `-Dad.mision7.integracion=true`, `-Dad.mision7.vista=anuncio|rivalidad|salida|encuentro|avenida|noticia|ciudadanos|pacto|dos|llegada|resultado|ayuda|eleccion|estructuras|pausa` y `-Dad.mision7.captura=RUTA`. Son recorridos de revisión que avanzan la simulación legalmente; no añaden inmunidad ni cambian velocidad. Usar siempre un directorio temporal con `ad.guardados` para estas revisiones. Sin `ad.mision7.vista`, `ad.mision7.integracion` únicamente abre la configuración normal.

La conexión se ha comprobado con dos clientes en el mismo equipo. Un mando físico y una LAN entre dos computadores distintos requieren comprobación manual. La misión 7 conserva los datos que usa el escrutinio de la misión 8.

Validación final del 12 de noviembre de 2026: **146 pruebas JUnit, cero fallos y cero errores**, paquete generado con BUILD SUCCESS. Incluye las 115 pruebas anteriores y 31 nuevas de misión 7. Se completaron nueve recorridos con física real (tres dificultades × individual con cada rol o dos humanos), se inspeccionaron las escenas y advertencias en Processing y se comprobaron dos clientes TCP. La pausa y su [ayuda](vista-ayuda.png) también se revisaron gráficamente; [menú de pausa](vista-pausa.png).
