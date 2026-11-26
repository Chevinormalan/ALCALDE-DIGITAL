# Misión 8 · El futuro de Civitas

Implementada en `entrega1-nueva`, en el mismo lienzo Processing de 1280 × 720. Referencias de interfaz: misiones 1 y 2, `TemaPixel`, `MarcosProcessing` y Pixelify Sans. Se preservan los diseños actuales de Sofi, periodista, influencer, Juan y Palomo.

## Entrada y antecedentes

En el modo «Según la campaña» se requieren siete victorias reales y los registros nuevos de decisiones. «Desbloquear todas las misiones» abre los ocho capítulos y permite elegir finales de prueba sin completar la campaña. Si un perfil antiguo carece de señales individuales, se identifica la misión que debe repetirse; no se reemplazan respuestas desconocidas por decisiones inventadas.

`AntecedentesOcho` toma una fotografía de las filas vigentes de `TablaPuntajes`, su proyección de ciudad y el estado electoral de misión 7. Repetir una victoria sustituye la contribución de esa misión; el capítulo 8 no vuelve a sumar esos cambios. El pacto de Juan permanece irreversible. Los guardados reales no se reinician para probar el capítulo.

Las señales de las misiones 1–7 se muestran en auditoría y consultas: publicaciones responsables y burlas; defensa de equipos; confianza y desinformación; respuestas favorables, desfavorables y neutrales; verificaciones y rumores; escucha y convicción; noticias y pacto. Las respuestas de misión 6 conservan dos posturas válidas.

## Escenas y elección

Obra de constructores y nave → aparición de la tarima y multitud → acusación de Sofi y revelación de Palomo → auditoría de tres evidencias → voto por participante → escrutinio → intervención viral → combate → epílogo → resultado.

Cada intervención se presenta por separado con nombre y retrato de quien habla. El texto aparece progresivamente: E, Enter, A del mando o el botón muestran primero la frase completa y después permiten continuar. No hay avance automático entre intervenciones. Las acciones escénicas deben terminar antes del siguiente paso. El contador indica la intervención dentro de cada escena.

La tarima electoral tiene espacio continuo para las cuatro candidaturas, con anclajes y nombres separados. Palomo hace desaparecer a la multitud, vuela hacia el ganador, acerca el inyector hasta el contacto, provoca la reacción y se retira antes de la transformación. En el epílogo, el equipo se acerca, el elegido se recupera, Palomo intenta escapar y Juan lo detiene con un disparo visible. Las dos rutas conservan al alcalde legítimo y distinguen la responsabilidad de un Juan que confirmó el pacto. Contraste completo con el guion y capturas: [REVISION_GUION.md](REVISION_GUION.md).

La auditoría inserta casos en el AVL por riesgo; contrastarlos los elimina. El árbol n-ario registra contrastar/omitir y cada voto. El grafo dirigido usa BFS para determinar qué grupos reciben las aclaraciones. Las consultas muestran las estructuras y las decisiones ejecutadas.

El escrutinio usa el respaldo acumulado de misión 7:

`3 × apoyo + confianza + 2 × verificadas + participación − 2 × conflictos`

Cada voto humano aporta 1 punto, sin reemplazar el respaldo ciudadano. Juan requiere además no haber confirmado el pacto, respaldo previo de al menos 200 y confianza colectiva de al menos 35. Estos umbrales son parámetros iniciales de balance del juego. Un empate exacto se resuelve con un sorteo público reproducible, registrado en el historial. El anuncio de Palomo puede proclamar a Alberto si se omiten evidencias; el escrutinio auditado conserva al ganador legítimo.

La mutación ocurre después del escrutinio por la intervención visible de Palomo. No convierte retroactivamente a un Juan honesto en autor del pacto. Si gana otra candidatura, Juan puede ayudar sin apropiarse del cargo. Los epílogos distinguen ganador y estado de ciudad: confianza cívica, reconstrucción o crisis. Esta clasificación utiliza los umbrales documentados en `docs/campana/PUNTAJES_Y_ANTECEDENTES.md`.

## Combate

Motor determinista a 60 Hz con física lateral, gravedad, salto doble, plataformas de una sola cara, esquiva, invulnerabilidad limitada y tres acciones por héroe. Sofi protege a los aliados cercanos; la periodista despeja proyectiles próximos con su protección; el influencer tiene un disparo en abanico y pulso de mayor alcance. Todos pueden terminar en solitario.

Tres formas visuales y cinco fases, con transiciones que limpian los peligros, recolocan al equipo y anuncian la nueva situación:

1. Arena abierta.
2. Zonas de pinchos que alternan lados.
3. Jefe centrado con lluvia de mensajes y extremos peligrosos.
4. Plataformas suspendidas y desaparición de pinchos.
5. Ascenso hasta el núcleo: es necesario alcanzar altura antes de dañarlo.

Las fases se desbloquean al cruzar 85 %, 70 %, 60 % y 40 % de vida. Las formas cambian en las fases 3 y 5. Fácil y Normal recuperan una vida a cada superviviente al terminar una transición; Difícil no lo hace. El jefe aumenta su resistencia según dificultad y número de héroes.

Seis ataques: lluvia, barrido de bots, abanico, ráfagas de señales, muro con hueco y tres orbes sucesivos. Cada uno tiene aviso, actividad y recuperación. El selector considera historial para limitar repeticiones. Los orbes persiguen durante un tiempo limitado y después siguen su trayectoria. El jefe humano usa las mismas ventanas y enfriamientos; en fase 3 también queda limitado a lluvia.

Una derrota registra el intento y permite reintentar el combate conservando votos, auditoría y elegibilidad. La victoria persiste al terminar el epílogo. Puntaje: 60 por victoria, 30 por las tres auditorías y hasta 10 por vidas; el puntaje no sustituye la victoria.

## Participantes y controles

Hay individual con selección entre Sofi, periodista e influencer, cooperativo de dos o tres héroes y cuatro participantes en modalidad tres contra uno. El cuarto controla al jefe. No son cuatro héroes cooperativos.

| Puesto | Movimiento | Salto | Tres acciones | Esquiva |
|---|---|---|---|---|
| J1 | A/D | Espacio | F/Q/C | R |
| J2 | Flechas izquierda/derecha | Mayús | L/K/O | J |
| J3 | Numérico 4/6 | Numérico 0 | Numérico 1/2/3 | Decimal |
| J4 jefe | — | — | Números 1–6 de la fila superior eligen los seis ataques | — |

Mando J1: stick, A salto, X/Y ataques, LB protección y RB esquiva. Menú pausa. Configuración y diálogos: Tab o flechas para elegir y Enter/E/A para confirmar; ratón y mando también funcionan. En combate, C y O ejecutan la tercera acción de J1 y J2; E y Enter quedan reservados para confirmar fuera del combate. Los números superiores del jefe no se confunden con los del teclado numérico de J3. Esc/P abre la pausa común, G guarda, H muestra ayuda y T abre consultas. La página de controles de la misión incluye todos los puestos.

## LAN y guardados

Crear/unirse sala usa TCP **48508**. El anfitrión elige cupo de dos a cuatro y dificultad. Los invitados ven las salas de misión 8 con cupos disponibles y eligen un puesto libre. La partida empieza automáticamente al completarse el cupo. En LAN, J4 usa 1–6 del teclado o A, X, Y, LB, RB y RT del mando para los seis ataques. Una conexión queda vinculada a un puesto y no puede enviar entradas por otro. El servidor decide física, daño, azar, auditoría, escrutinio y victoria; el anfitrión controla el avance narrativo y la auditoría. Cada invitado emite su propio voto.

Desconectar pausa la sala. Un token vincula la reconexión a su puesto; se conserva en `mision8-red.properties` del invitado. El anfitrión guarda `mision8.properties`. Al continuar un guardado LAN se vuelve a la configuración para volver a alojarlo. El resultado de campaña se acredita en el perfil del anfitrión.

El snapshot versionado conserva escenas, reloj, semilla y estado del azar, votos, auditoría, antecedentes, vida y forma del jefe, plataformas derivadas de fase, héroes, proyectiles, recargas e historial. Usa escritura atómica y copia de recuperación. `mision8-resultados.properties` conserva los finales; `tabla-puntajes.properties` incluye la fila de misión 8 y `progreso-misiones.properties` su victoria. Se preservan las claves electorales de `campana.properties`.

También conserva intervención, tiempo de la acción y revelado del texto, además de inyección, recuperación y neutralización. Pausa y LAN comparten estos datos: continuar un guardado en mitad de una inyección no duplica el evento. Los guardados anteriores a esta revisión comienzan en la primera intervención de su escena, conservando el resto de la partida.

## Arte

La herramienta integrada `imagegen` produjo tres atlas transparentes de 8 × 8: 64 poses de héroes, 64 poses de candidatos normales y sus tres formas, y 64 cuadros de efectos/personajes auxiliares. Un cuarto atlas contiene cuatro escenarios. La revisión añade una tarima electoral amplia en un archivo separado. Las nuevas animaciones incorporan cuatro hojas de infección de 4 × 4 y tres hojas de ataque de 8 × 4. Son **352 sprites distintos y cinco fondos**, con transparencia real en los sprites. Las escenas combinan estas poses con desplazamientos, contacto del inyector, humo, reacción, transformación y recuperación.

Cada alcalde tiene 16 pasos de infección, desde el primer síntoma hasta la aparición del núcleo y los cables. Cada una de sus tres formas dispone de ocho poses de carga, emisión y recuperación, sincronizadas con las ráfagas reales. Los tres protagonistas usan su cuadro de pie cuando se detienen, tanto en las escenas como en el combate. Detalle y animaciones de revisión: [ANIMACIONES.md](ANIMACIONES.md).

Se reutilizan el parque y Sofi de misión 1, las hojas de periodista de misión 2, influencer y vuelo de Palomo del rediseño de misión 3 y los ciudadanos de misión 7. Los atlas nuevos completan poses de combate, candidaturas faltantes, constructores, nave, humo, Palomo y peligros. Se conservan la paleta morada/lila/dorada, fuente y marcos ornamentados. Hay señales visuales para el audio y se evita el parpadeo de daño con movimiento reducido.

Recursos: `src/main/resources/cliente/mision8/`. Prompts, manifiesto SHA-256 y cuadros extraídos: `tools/arte/referencias-mision8/`. `python3 tools/arte/preparar_mision8.py` valida y extrae las celdas sin alterar las imágenes originales.

## Verificación

Las pruebas cubren los cuatro ganadores, corrupción irreversible, voto limitado por respaldo, tres estados de ciudad, snapshot determinista, pausa, transiciones, seis ataques, salto doble, derrota y reintento, victoria idempotente, bloqueo de acceso y paquetes inválidos. Hay 18 recorridos completos con física real: los tres roles individuales y modalidades de 2, 3 y 4 participantes en Fácil/Normal/Difícil. Las pruebas TCP usan cuatro conexiones reales en el mismo equipo y comprueban pausa, vinculación de puestos y reconexión.

`python3 tools/verificar_mision8.py` captura las pantallas en Processing con perfiles temporales. Las escenas de revisión se preparan desde código de pruebas y no incorporan ventajas o accesos de prueba al juego normal. Evidencias en `docs/mision8/evidencias/`.

La prueba de red entre computadores distintos, el mando físico y el balance de partidas humanas siguen requiriendo comprobación manual.

Verificación del paquete tras mejorar las animaciones: **254 pruebas aprobadas**, cero fallos, errores u omitidas. Se conservaron los 18 recorridos completos y las pruebas de cuatro conexiones. Se comprobaron además diálogos sin avance automático, vuelo e inyección en los cuatro candidatos, pausa y guardado durante el contacto, cierre del epílogo, sincronización narrativa entre clientes, reposo tras soltar movimiento, poses de disparo y transparencia de todos los cuadros nuevos. Registro actual: `evidencias/animaciones-pruebas.log`; hash del JAR y límites comprobados: `evidencias/validacion.json`.

La entrada desde el selector de `ClienteSketch` se comprobó con el JAR final y un perfil temporal con siete victorias explícitas. Reproducción: `python3 tools/verificar_integracion_m8.py`. Evidencia: `evidencias/integracion.png`.

## Selector de finales y acceso libre

Antes de comenzar, el botón **Final** recorre «Según la campaña», «Final 1 · Juan», «Final 2 · Petrosqui», «Final 2 · Abel» y «Final 2 · Alberto». El botón **Ciudad** permite probar confianza cívica, reconstrucción o crisis. Con acceso libre y sin antecedentes completos se selecciona inicialmente el Final 1 de prueba.

La ruta elegida se mantiene durante escenas, votos, combate, epílogo, reintentos y conexión LAN. Los finales manuales utilizan datos de ensayo y guardan solamente `mision8-prueba.properties`; no sustituyen la partida de campaña, sus puntajes, decisiones, corrupción ni victorias. Para seguir la historia real se elige «Según la campaña». Continuar una partida también presenta la configuración antes de entrar.

Actualización del selector: **238 pruebas aprobadas**, sin fallos, y capturas con un perfil sin victorias (`libre.png` y `libre-petrosqui.png`). Los cuatro candidatos y los tres estados de ciudad se verifican como escenarios de prueba independientes de la campaña.
