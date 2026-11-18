# Banda sonora y diseño de sonido de Alcalde Digital

Propuesta del 16 de noviembre de 2026, basada en los guiones y estados que ejecuta `entrega1-nueva`. Este documento define el trabajo; no modifica el juego ni incorpora todavía nuevas pistas. Se revisaron código, archivos y metadatos, sin presentar esta inspección como una prueba completa de escucha y mezcla.

## Dirección musical

Crear una banda instrumental original con sintetizadores, percusión y orquesta sintetizada, vinculada al pixel art morado de Ciudad Nova. Los pasajes tranquilos deben permitir leer; los desafíos con reloj deben apremiar sin convertirse en una alarma continua; los combates deben tener energía épica, con melodía y progresión propias.

Identidades que reaparecen durante la campaña:

- **Ciudad Nova:** motivo principal cálido de cuatro notas, inicialmente ligero, retomado de forma heroica en misión 8.
- **Civitas:** dos notas digitales reconocibles. La versión clara acompaña una conexión estable; la alterada aparece después de una interferencia confirmada.
- **Mr. Palomo:** figura breve, cromática y juguetona. Al principio conserva el humor; su versión amenazante se revela conforme al guion, sin anticipar secretos.
- **Sofi:** melodía cálida de campanas suaves y cuerdas; representa escucha y cooperación.
- **Periodista:** patrón preciso de teclas y pulsos, asociado al contraste de fuentes.
- **Influencer:** arpegios brillantes y percusión ágil; en los combates se integra en el motivo colectivo.
- **Juan:** metales suaves y cuerdas, sin tratarlo musicalmente como alcalde antes de la elección. Las otras candidaturas comparten el mismo marco electoral: la instrumentación identifica al personaje sin declarar honestidad o corrupción por su nombre.

Los BPM de este documento son decisiones propuestas de composición. Las cifras no son parámetros que ya existan en el juego.

## Diagnóstico de lo existente

Hay **36 archivos WAV**. Incluyen tres bucles musicales de ocho segundos (`ambient_loop`, `combat_loop`, `boss_loop`), todos mono, de 22.050 Hz y 16 bits, y efectos/ambientes a 22.050 o 44.100 Hz. Hay material de pasos, conducción, helicóptero, disparos, escudo, impactos, radio y explosiones que puede reutilizarse tras una revisión de escucha y ajuste de niveles.

| Área | Estado encontrado | Trabajo requerido |
|---|---|---|
| Menú, ayuda e interfaz general | No hay un sistema global de sonido de foco, hover y clic en `RenderJuego`/`ClienteSketch`. | Bus de interfaz común y eventos compartidos para ratón, teclado y mando. |
| Misión 1 | No tiene una banda musical propia conectada a sus estados. | Parque, lectura y crisis adaptativa. |
| Misión 2 | Reutiliza `ui`, `alert`, `ping` y `hit`; sin música propia de defensa. | Defensa cronometrada y avisos específicos por terminal. |
| Misión 3 | Tiene reproductor y los tres bucles musicales citados. Cambia de pista reiniciando el clip. | Temas más largos, identidad por zona y transiciones. |
| Misión 4 | Efectos de diálogo/combate y ambientes reactivos de coche. | Música para campaña, rumores, tres combates y plaza vacía. |
| Misión 5 | Carga bucles de misión 3, pero no aparece una reproducción musical continua de esos archivos en la vista. Usa efectos puntuales. | Música de cinco rondas y gestión de turnos/decisión final. |
| Misión 6 | Efectos reactivos de vuelo, armas, radio, pasos y destrucción. | Banda de vuelo, tres fases del dron y entrevista. |
| Misión 7 | Reutiliza cuatro efectos de misión 3, sin tema musical propio de carrera. | Cuenta atrás, carrera por tramos, noticias y llegada. |
| Misión 8 | Solicita ambiente o jefe reutilizando música de misión 3; algunos eventos ya tienen efecto. | Ceremonia, intervención, clímax por fases y cierres. |

Además, `AudioEscena.mezclar()` crea un bucle si el identificador de canal todavía no existe, pero no sustituye su clip cuando cambia el recurso solicitado para ese identificador. Misión 8 llama a `bucle("ambiente", ...)` con un recurso para historia y otro para combate: hay que corregir ese cambio de recurso antes de depender de la transición musical. Es una observación del código, no una conclusión obtenida escuchando la partida.

Existen reproductores independientes en varias misiones. Se deben coordinar para evitar música duplicada, niveles dispares o bucles que continúen al salir. Cargar un archivo no equivale a tenerlo correctamente conectado a una fase.

## Misión 1 · Sofi y el parque

Guion vigente: descanso en el parque, llegada de Palomo y presentación de Civitas, tres publicaciones, crisis del rumor, ocho peatones en cuatro oleadas, conversaciones y desenlace bueno/neutral/malo. No es un combate físico.

| Momento | Música propuesta | Sonidos y reglas |
|---|---|---|
| Banca y entrada de Palomo | «Una tarde en Ciudad Nova», 92 BPM: campanas, pizzicato y ambiente abierto. | Fuente, aves y aleteo; motivo breve de Palomo al llegar. |
| Consulta de publicaciones | Versión reducida del tema, sin percusión insistente. | Abrir Civitas, cambiar publicación, consultar fuente y confirmar acción. No indicar que una respuesta es correcta antes de contrastarla. |
| Inicio de crisis | «El rumor corre», 124 BPM, pulso claro desde el primer instante. | Golpe corto de entrada y primera señal de propagación. |
| Propagación y cuenta atrás | Tres capas: pulso; arpegio/percusión; tensión aguda moderada. | Un aviso por oleada. La urgencia depende del tiempo restante y del estado real, no de la posición del ratón. |
| Escucha y personas calmadas | Se atenúa el protagonismo de la melodía durante lectura; sigue el pulso si el reloj continúa. | Inicio de conversación, avance, señal suave por persona calmada y pequeño cierre al salir una pareja. |
| Resultado | Variación de cierre según desenlace e indicadores. | Éxito, resultado incompleto o intento fallido deben distinguirse sin castigo estridente. |

El tiempo de crisis es 60/30/12 segundos según dificultad. Los umbrales musicales deben ser proporcionales: por encima del 50 % del plazo, base activa; entre 50 % y 25 %, mayor densidad; por debajo del 25 %, urgencia. Para evitar cambios nerviosos, un nivel de urgencia no retrocede durante la misma cuenta atrás. No usar «últimos diez segundos» como regla común: abarcaría casi toda la dificultad Difícil.

La sensación de apuro debe venir de la subdivisión y las capas, manteniendo el volumen controlado. La pista no puede dedicar sus primeros quince segundos a una introducción: una partida Difícil puede terminar antes.

## Misión 2 · Defensa de la red Civitas

Guion vigente: periodista y visita cómica de Palomo, alarma, mantenimiento de seis computadores seguros durante el plazo y regreso de Palomo con cierres distintos. La versión ejecutada usa ataques recurrentes; no debe diseñarse el audio a partir de la propuesta antigua que comenzaba con todos los equipos dañados.

| Momento | Música propuesta | Sonidos y reglas |
|---|---|---|
| Oficina e introducción | «Turno de redacción», 100 BPM: teclado percusivo, bajo ligero y motivo cómico. | Zumbido de equipos, aleteo, ventana y salida de Palomo. |
| Alarma | Transición breve de deterioro digital. | Alerta de red una vez; comienza la música de presión cuando empieza la defensa, no durante el texto previo. |
| Defensa | «Seis equipos, una red», 132 BPM: secuenciador, bajo y percusión precisa. | Identidad corta de terminal comprometida, reparación y equipo protegido. |
| Varias terminales comprometidas | Añadir una capa de conflicto según cantidad real, con estabilización para que no entre y salga a cada interacción. | Panorámica moderada izquierda/derecha según ubicación, siempre acompañada de indicación visual. |
| Final de plazo | Capa de urgencia por proporción del tiempo restante. | En los últimos ocho segundos cesan las oleadas regulares; a los dos segundos sí existe un ataque final. Su aviso debe proceder de ese evento real. |
| Fallo crítico | Caída de música y avería del núcleo. | Solo después de activarse el fallo; nunca anticiparlo musicalmente. Usar cierre de avería y recuperación, no burla o castigo por un evento inevitable. |
| Éxito o fracaso y remate de Palomo | Alivio con humor o cierre incompleto con remate cómico. | Conservar espacio antes del golpe/explosión del remate. |

La presión musical debe combinar tiempo y terminales afectadas, sin llegar a saturar si ambas variables son críticas. Se utiliza el valor mayor de tensión, no la suma ilimitada de capas. Reparar tiene una respuesta positiva breve, pero no reinicia ni detiene la música: aún puede llegar otra oleada.

## Misión 3 · Civitas en Rojo

Guion vigente: entrada nocturna en NovaMall, encuentros con Palomo, amenazas que toman cuerpo, nodos del Pixel Lab, pasillo comprometido y Núcleo Viral de tres fases; después se aíslan copias y Palomo deja una respuesta evasiva.

- **Entrada y atrio:** «NovaMall después del cierre», 110 BPM, electrónica nocturna con espacio y luces musicales. El motivo de Palomo aparece como comentario breve, conservando su ambigüedad.
- **Amenazas y nodos:** «Publicaciones con cuerpo», 144 BPM. Percusión y bajo ganan peso cuando empieza un encuentro; las zonas de lectura reducen densidad y volumen.
- **Pasillo corrupto:** variación descompuesta del tema de exploración, con cortes rítmicos y acordes alterados. El ritmo no debe confundirse con las advertencias de ataques.
- **Núcleo Viral:** «Cortar la cadena», 152 BPM, primer gran tema épico electrónico. Tres capas corresponden a sus tres fases: base, presión adicional y melodía completa con percusión intensa. El escudo viral tiene su efecto específico.
- **Derrota del núcleo y regreso:** resolución breve al fragmentarse la señal y retorno al tema de exploración debilitado. La aparición final de Palomo conserva una nota sin resolver: el conflicto de la campaña continúa.

Reutilizar como base de revisión los efectos de PING, FLASH, DASH, LIVE y modo privado; separarlos mejor por duración y timbre para reconocer cada habilidad. No reproducir efectos pendientes después de reaparecer.

## Misión 4 · La plaza que dejó de escuchar

Guion vigente: Juan conduce, habla con vecinos, responde cuatro preguntas dominadas por rumores, descarga Civitas y afronta tres encuentros —Troll, SpamBot y Deepfake—; la plaza vacía revela que combatir no basta para restaurar confianza.

- **Conducción y llegada:** «Camino a la plaza», 112 BPM: movimiento y cierta esperanza. La música deja sitio al coche, frenado y pasos.
- **Tarima, preguntas y rumores:** variación del tema de reflexión, con interrupciones pequeñas asociadas a reacciones reales. Ambas opciones de una pregunta tienen el mismo sonido de foco; la música no adelanta qué respuesta espera el sistema.
- **Troll:** entrada directa del tema épico «Defender la palabra», 144 BPM, con percusión firme y llamadas cortas.
- **SpamBot:** misma base musical con secuencias repetitivas y mecánicas para expresar insistencia.
- **Deepfake:** armonía más inestable y ecos de motivos previos, manteniendo reconocibles los avisos de espada, guardia y daño.
- **Entre encuentros:** bajar intensidad y recuperar el ambiente de los vecinos. Un toque de salud debe oírse con claridad.
- **Epílogo en la plaza vacía:** versión incompleta del tema de Ciudad Nova, lenta y sin percusión. Evitar una gran fanfarria por haber vencido tres enemigos: el guion pide reflexión sobre una confianza que no se recuperó todavía.

## Misión 5 · Batalla del servidor

Guion vigente: ciudadano y periodista en la oficina, Palomo interviene, absorción en el servidor, cinco rondas de tres ataques y turnos de verificar/pulso/reparar. Al final se elige **Perdonar** o **Aislar**; perdonar puede activar un ataque final de siete segundos. Este cierre no es el pacto corrupto de Juan, que pertenece a misión 7.

- **Oficina y conversación:** tema digital ligero, con pausas para el humor del ciudadano y la periodista.
- **Absorción:** transición original de 2–4 segundos con succión, barrido de frecuencias y golpe de llegada. No repetirla por cada diálogo.
- **Batalla:** «Dentro del servidor», 150 BPM. Tema épico electrónico de cinco niveles de densidad, uno por ronda. La tensión crece por instrumentación; no es necesario componer quince canciones completas.
- **Turnos:** conservar bajo y pulso reducidos; retirar percusión fuerte y melodía dominante para elegir. Verificar, pulso y reparar tienen confirmaciones diferentes cuando se ejecutan.
- **Decisión final:** suspender la parte heroica y mantener un fondo ambiguo. Solo después de confirmar se reproduce aislamiento o entrada del último pulso.
- **Último ataque:** variación intensa de la batalla, disponible de inmediato. Debe cubrir siete segundos sin depender de una introducción larga.
- **Regreso a la oficina:** descenso de tensión y pequeño remate cómico. La victoria se presenta después del último peligro, no antes de comprobar que los jugadores sobreviven.

Sus quince patrones requieren una familia clara de advertencias: entrada lateral, pulso cian de quietud, ventana, salto por gravedad, plataformas, cambio de gravedad, fijación del haz y anillos. Se pueden reutilizar identidades de aviso cuando enseñan la misma regla. El sonido cian debe significar «quietud» y diferenciarse del sonido rosado de peligro que se esquiva. En cooperativo, los objetivos se distinguen con timbre y señales visibles, sin depender solo del estéreo.

## Misión 6 · Cielos de campaña

Guion vigente: discurso de Juan, encuentro con el influencer, desplazamiento al helicóptero, entrenamiento, dron infectado de tres fases, autodestrucción y transmisión de Palomo, aterrizaje y entrevista. Escucha y convicción son dos rutas válidas.

- **Discurso y conversación:** tema de reflexión con instrumentación de Juan y el influencer, sin anunciar un ganador electoral.
- **Despegue:** «Por encima de Ciudad Nova», 124 BPM, ascenso armónico y ritmo de aventura. El rotor mantiene su propio canal de ambiente.
- **Entrenamiento:** versión ligera del vuelo, con suficiente espacio para disparo y confirmaciones de maniobras.
- **Dron:** «Romper la interferencia», 148 BPM, épica de combate aéreo. Las tres fases añaden capas; el desbloqueo real de misiles tiene una señal breve, sin reproducirla al cargar un guardado que ya los tiene.
- **Autodestrucción y radio:** corte de percusión, impacto y transmisión. Bajar el fondo para que la amenaza de Palomo y los textos puedan seguirse sin fatiga.
- **Aterrizaje:** descenso musical y apagado gradual de rotor, conservando los efectos reactivos existentes.
- **Sala y entrevista:** tema de reflexión en torno a 82 BPM, sin cuenta atrás ni alarma. Los cierres de escucha y convicción comparten una resolución positiva equivalente; la música no debe presentar convicción como fracaso.

## Misión 7 · La carrera por la primicia

Guion vigente: anuncio adelantado y apoyos provisionales, rivalidad periodista/influencer, choque en la avenida y provocación de Palomo; carrera de 9.600 unidades, cuatro noticias por corredor y descubrimiento de una falsa confirmación electoral en el parque.

- **Anuncio:** «Última hora», 104 BPM, cabecera de noticias reconocible que adquiere una pequeña anomalía al aparecer la transmisión manipulada.
- **Rivalidad y encuentro:** intercambio de las identidades de periodista e influencer, con humor y tensión competitiva. El golpe del encuentro tiene un efecto corto, no una explosión de combate.
- **Cuenta de tres segundos:** tres pulsos y una salida distinta, comunes a todos los métodos de control.
- **Carrera:** «La primicia no espera», 160 BPM. Bajo y percusión rápidos, melodía legible. Tres niveles por avance real: salida, rivalidad y proximidad al parque. No convertirla en una pelea épica indistinta.
- **Noticias:** breve indicación de entrada y reducción moderada de música para leer, conservando la velocidad del juego. La selección usa sonidos neutrales; una confirmación no certifica que la noticia sea verdadera.
- **Pacto:** aviso específico al abrir la advertencia explícita; el motivo se oscurece solo si se confirma de verdad. No activarlo por leer, pasar el cursor o usar una ayuda.
- **Llegada y revelación:** pequeña señal de quién ganó la carrera o de empate, seguida de una interrupción musical al descubrir la pantalla. Evitar una fanfarria de posesión: el alcalde todavía no está elegido.
- **Epílogo:** motivo de investigación con final abierto que conduce a la ceremonia de misión 8.

Las ayudas, batería, escudo, vallas y distracciones necesitan respuestas propias. Las notificaciones de noticias deben distinguirse de los avisos de una distracción que fija un carril. Las decisiones sobre difundir no deben premiarse con la misma señal que una evidencia contrastada.

## Misión 8 · El futuro de Civitas

Guion vigente: nave y construcción de tarima para cuatro candidaturas, aparición del público, acusación a Palomo, auditoría, voto, anuncio y recuento, desaparición de personas, vuelo, inyección y mutación; combate de cinco fases/tres formas; recuperación, huida, disparo de Juan y cierre según ganador y estado de ciudad.

| Momento | Música y sincronización |
|---|---|
| Nave y construcción | «La plaza decide», 100 BPM, ceremonia todavía incompleta. Martillos, nave y humo en un canal de efectos; no convertir cada golpe en percusión musical obligatoria. |
| POF y acusación | Golpe de aparición de multitud; la música baja y deja el motivo de Palomo en una versión claramente amenazante después de la revelación. |
| Auditoría y voto | Base sobria del tema de reflexión. Evidencia contrastada y voto registrado son sonidos distintos. Pasar sobre un candidato no indica aprobación o corrupción. |
| Anuncio y recuento | Golpe breve ceremonial; si existe discrepancia, señalarla cuando el recuento la revela. La música sigue al ganador legítimo, no al anuncio manipulado. |
| Chasquido, vuelo e inyección | Retirar el pulso, introducir tensión y dar espacio a chasquido, alas e inyector. La punzada debe coincidir con el contacto real del paso 35, no con el clic para avanzar el diálogo. |
| Mutación | Transición de contagio: núcleo grave, crecimiento de cables y culminación. Sus 16 cuadros por alcalde se sincronizan como una sola acción, no como 16 sonidos repetidos. |
| Combate | «El futuro de Civitas», 156 BPM: clímax épico que reúne Ciudad Nova, héroes y la versión corrompida de Palomo. |
| Recuperación | La música de combate termina; quedan armonía y motivo de cooperación. La recuperación tiene un sonido propio. |
| Huida, disparo e impacto | Reentrada breve de tensión; aleteo y disparo claramente separados; impacto real cuando el pulso alcanza a Palomo. |
| Resultado | Confianza cívica: resolución luminosa; reconstrucción: alivio con final abierto; crisis: calma seria y reparación pendiente. |

Las cinco fases deben escucharse como una obra continua: fase 1 presenta tema y bajo; fase 2 añade presión; fase 3 subraya la segunda forma; fase 4 abre el espacio armónico de las plataformas y reduce capas que tapen los avisos; fase 5 recupera la melodía completa para el ascenso y el núcleo. Mantener tempo y compás facilita las transiciones entre capas. La intensidad no depende de que una fase dure una cantidad fija de segundos.

Los cuatro alcaldes conservan el tema central con colores instrumentales distintos y equivalentes en importancia. No hacen falta cuatro bandas completas separadas, ni usar un tema inherentemente maligno para quien no sea Juan. La infección es una intervención de Palomo. Ayudar en el epílogo no borra el pacto confirmado ni convierte a Juan en alcalde si ganó otra candidatura.

Las seis familias de ataque necesitan aviso y emisión identificables: lluvia de mensajes, barrido de bots, abanico, ráfaga de señales, muro y tres orbes. El aviso precede al peligro según el motor; la emisión coincide con su creación. El clímax musical deja espacio a estas señales.

## Interfaz, diálogos y sonidos globales

| Evento | Sonido propuesto | Regla de reproducción |
|---|---|---|
| Entrar en un botón con el ratón | Toque suave de 35–60 ms, agudo moderado. | Una vez al cambiar de botón; permanecer encima no repite. |
| Mover foco con teclado/mando | La misma familia del hover. | No duplicar si ratón y foco cambian al mismo botón en el mismo instante. |
| Pulsar un botón | Clic de 60–100 ms con más cuerpo. | Una vez por activación aceptada, no por cada cuadro de la animación. |
| Confirmar una selección | Dos notas ascendentes de 100–180 ms. | Confirma que se ejecutó una orden; no que una respuesta narrativa sea correcta. |
| Volver o cancelar | Variante descendente de 80–140 ms. | Diferente de error y de derrota. |
| Botón bloqueado | Toque seco de 80–120 ms. | Solo al intentar activarlo; un hover no debe martillear una alarma. |
| Cambiar pestaña o página | Deslizamiento corto. | Uno por cambio aceptado. |
| Aplicar gráficos o audio | Confirmación discreta. | Distinguir aplicar de seleccionar una opción; no sonar al iniciar y cargar preferencias. |
| Guardado manual | Tono suave. | Autoguardados silenciosos salvo fallo, evitando avisos periódicos molestos. |
| Nueva intervención de diálogo | Pequeña señal de avance. | No hacer clic por cada letra. El revelado puede tener blips escasos y desactivables. |
| Resultado o desbloqueo | Firma breve de logro. | Una sola vez; no al redibujar, consultar o cargar resultados antiguos. |
| Unirse, desconectar o reconectar LAN | Firmas distintas. | La desconexión no se confunde con derrota. |

Añadir sonidos globales de pausa/reanudación, error de conexión, publicación, verificación, reporte, información nueva, escudo, curación y daño. Un motivo común une acciones equivalentes en varias misiones, mientras sus ataques y entornos conservan identidad.

Los efectos repetidos de pasos, armas y alas pueden tener tres o cuatro variantes leves para evitar monotonía, con volumen y tono controlados. El paneo nunca sustituye una señal visual o un timbre identificable.

## Mezcla y comportamiento dinámico

Proponer controles de **volumen general, música, efectos, ambiente e interfaz**, más silenciar y reducir intensidad sonora. Un acceso «Audio» junto a los ajustes existentes permitiría probar un clic y una muestra musical al ajustar niveles. La prueba se reproduce al soltar el control, no por cada paso del deslizador.

Valores iniciales de diseño, pendientes de escucha: general 80 %, música 55 %, efectos 70 %, ambiente 35 % e interfaz 50 %. Son posiciones de controles, no una garantía de volumen físico en los altavoces. Reducir intensidad limita alarmas repetidas y retira capas agudas sin eliminar las advertencias esenciales.

- Bajar la música aproximadamente 4–6 dB al mostrar diálogos o una señal crítica; recuperar nivel con suavidad. No bajar y subir por cada proyectil de un arma automática.
- Cambiar música con fundidos de aproximadamente 0,5–1,5 segundos. Las capas de una misma composición comparten tempo y punto de bucle; los cambios no urgentes pueden entrar al siguiente compás.
- El motor decide cuándo existe un ataque. La música se adapta a ese hecho; no se retrasa una advertencia para hacerla coincidir con un compás.
- Pausar congela el audio de juego y detiene efectos pendientes; el bus de interfaz sigue funcionando para navegar la pausa. Reanudar recupera la sección musical, sin superponer otra copia.
- Al salir de una misión se cierran sus ambientes y música. Solo queda el tema correspondiente al menú o al selector.
- El sistema limita voces simultáneas y prioriza avisos críticos, acciones directas y diálogo sobre ambiente y hover. Varios impactos cercanos pueden agruparse, preservando el primer ataque y los cambios de estado.
- En LAN se transmiten estados/eventos, no audio en vivo. Cada equipo reproduce sus recursos; no se repiten efectos históricos al recibir snapshots o reconectar. Los avisos comunes se deduplican por evento y los sonidos locales mantienen su prioridad.
- La generación o interpolación gráfica de fotogramas no debe generar sonidos adicionales. El audio responde a eventos y reloj de simulación, no al número de veces que se dibuja una imagen.

## Catálogo propuesto y producción

Primera entrega completa: **20 composiciones base**, antes de variantes, capas, transiciones y firmas breves. No son 20 copias del mismo bucle ni 20 bandas aisladas sin relación.

| ID | Composición | Uso / BPM propuesto |
|---|---|---|
| G01 | Ciudad Nova despierta | Menú y selector, 104 |
| G02 | Escuchar antes de publicar | Diálogo, investigación, decisiones e entrevista, 82 |
| G03 | Lo que queda de la ciudad | Cierres, con variantes de confianza/reconstrucción/crisis |
| M101 | Una tarde en Ciudad Nova | Parque de misión 1, 92 |
| M102 | El rumor corre | Crisis de misión 1, 124 |
| M201 | Turno de redacción | Oficina de misión 2, 100 |
| M202 | Seis equipos, una red | Defensa de misión 2, 132 |
| M301 | NovaMall después del cierre | Exploración de misión 3, 110 |
| M302 | Publicaciones con cuerpo | Combates de misión 3, 144 |
| M303 | Cortar la cadena | Núcleo Viral, 152 |
| M401 | Camino a la plaza | Conducción y campaña de misión 4, 112 |
| M402 | Defender la palabra | Tres combates de misión 4, 144 |
| M501 | Dentro de la conexión | Oficina/entrada al servidor, 96 |
| M502 | Dentro del servidor | Cinco rondas y último pulso de misión 5, 150 |
| M601 | Por encima de Ciudad Nova | Despegue y vuelo de misión 6, 124 |
| M602 | Romper la interferencia | Dron de misión 6, 148 |
| M701 | Última hora | Anuncio y rivalidad de misión 7, 104 |
| M702 | La primicia no espera | Carrera de misión 7, 160 |
| M801 | La plaza decide | Ceremonia y elección de misión 8, 100 |
| M802 | El futuro de Civitas | Combate final, 156 |

Los cierres de entrevista reutilizan G02/G03 de forma equivalente. La plaza vacía de misión 4 usa una variación incompleta de G03. La mutación de misión 8 es una transición original corta con efectos, no necesita sumar otra canción larga. El motivo de Palomo y las firmas de victoria/derrota pueden reaparecer en distintas orquestaciones.

Producir bucles de acción de aproximadamente 40–90 segundos y fondos tranquilos de 60–100 segundos, ajustados a compases completos y con entradas inmediatas para escenas cortas. Los avisos, impactos y remates quedan en archivos separados de la música. La partitura debe poder mantener una sección indefinidamente si el jugador tarda en leer o en vencer al jefe.

Formato inicial de ejecución: WAV PCM de 16 bits, 44.100 Hz; música estéreo y efectos mono cuando se necesite posicionarlos. El reproductor actual busca WAV; adoptar otro formato exigiría comprobar o añadir su decodificación. Cargar solo los paquetes de audio necesarios para la misión activa. Un minuto estéreo PCM de ese formato ocupa unos 10,6 MB; precargar todas las pistas y capas multiplicaría la memoria sin necesidad.

La música puede componerse de forma original mediante síntesis y programación. El objetivo aquí es un estilo electrónico con orquesta sintetizada; el realismo de una orquesta grabada requeriría otro proceso de producción. Cada recurso debe tener procedencia, versión, duración, tempo y punto de bucle documentados.

## Orden de implementación y aceptación

1. Unificar buses, controles y eventos de interfaz; corregir la sustitución de recursos de `AudioEscena`, la pausa y los reproductores que quedan activos al salir.
2. Preparar muestras de menú, crisis de misión 1, defensa de misión 2 y clímax de misión 8. Comprobar que la urgencia y la épica resultan agradables antes de producir el catálogo completo.
3. Incorporar música de misiones 3–7, turnos, entrevistas y variantes de cierre.
4. Integrar avisos sincronizados con las animaciones de ataques y acciones narrativas, con deduplicación en LAN.
5. Revisar mezcla y transiciones mediante partidas y escucha real.

Criterios de aceptación: hover una vez por entrada; clic una vez por activación; cero señales engañosas sobre preguntas, pactos o elecciones; presión solo cuando corresponde; pausa sin ataques sonoros atrasados; música correcta al guardar/cargar/reintentar; avisos comprensibles sobre música épica; un solo resultado sonoro por evento; mismos criterios en individual, teclado compartido y LAN. Comprobar dispositivos sin salida de audio, silenciado y retorno al menú, sin usar perfiles reales para las pruebas.

La validación técnica automatizada no sustituye la escucha de la mezcla. Primero se debe escuchar una muestra representativa de cada estilo, y después probar las escenas donde se acumulan música, diálogos y varias amenazas.
