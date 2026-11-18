# Revisión de la misión 8 contra el guion

Revisión implementada el 16 de noviembre de 2026 en `entrega1-nueva`. Se contrastaron las tres hojas del guion y las reglas de jugabilidad acordadas con las escenas, las transiciones y su presentación. Respaldo previo: `../Respaldos y ProyectsAnterirores/antes-revision-mision8-20261116/mision8.tgz` (relativo a la raíz del proyecto).

## Problemas encontrados y correcciones

| Parte del guion | Problema encontrado | Comportamiento corregido |
|---|---|---|
| Conversaciones | Varias voces estaban reunidas en un párrafo. Construcción y mutación avanzaban por tiempo. | Una intervención por pantalla, con nombre y retrato del interlocutor. Revelado progresivo y avance manual. Las acciones deben terminar antes de continuar. |
| Nave y construcción | El paso de escenario no explicaba visualmente todas las acciones. | Entrada de la nave, desplazamiento de constructores, martillos, humo, aparición de la tarima y salida de los obreros. Sofi entra al final de la obra. |
| Tarima electoral | El fondo tenía un espacio central para un solo alcalde. | Fondo nuevo de tarima continua para cuatro candidatos, a la misma altura, con separación y nombres propios. Mantiene árboles lilas, piedra morada y detalles dorados. |
| Aparición de personas | La multitud se mostraba sin un paso narrativo propio. | Aparición con humo y «¡POF!», seguida de las intervenciones de Sofi, periodista, influencer y Palomo. |
| Elecciones | Era necesario conservar la relación con las misiones anteriores. | Se mantienen auditoría de tres evidencias, antecedentes, elegibilidad, votos por participante y recuento. El anuncio manipulado se distingue del ganador legítimo. |
| Chasquido e intervención de Palomo | No se mostraban claramente la desaparición del público, el viaje hasta el elegido ni el pinchazo. | Pose de chasquido y desaparición gradual de la multitud; vuelo hasta la posición del ganador; acercamiento del inyector, contacto visible y reacción. Palomo se retira antes de la mutación. |
| Transformación | La causa y el resultado quedaban demasiado juntos. | Intervenciones y acciones separadas: amenaza, inyección, retirada, pérdida de control y transición visual a la primera forma. El combate empieza después. |
| Recuperación tras el combate | El acercamiento y la recuperación estaban narrados. | El equipo camina hacia el elegido herido; interviene por turnos; el candidato pasa de derrotado a recuperado. |
| Huida y cierre | La huida y la neutralización de Palomo no tenían una secuencia completa. | Vuelo de escape, entrada de Juan cuando ganó otra candidatura, disparo visible, impacto y desaparición de Palomo. El resultado se presenta después de terminar la conversación. |
| Pausa, guardado y LAN | Los nuevos pasos necesitaban un estado compartido. | Se guardan y transmiten intervención, tiempo y eventos de la escena. La pausa congela la acción; reanudar no repite la inyección. Solo el anfitrión avanza la conversación en LAN. |

## Secuencia y controles

Construcción → aparición del público → revelación → auditoría → voto → escrutinio → desaparición del público → vuelo de Palomo → inyección → retirada → mutación → combate → acercamiento y recuperación → huida y disparo → conversación de cierre → resultado.

E / Enter / A del mando o el botón de la pantalla completan primero la frase que se está revelando. Cuando la frase y su acción están completas, la siguiente pulsación avanza una sola intervención. El botón muestra «En escena…» mientras la animación continúa. Una tecla o botón mantenido al avanzar no se convierte en un ataque al empezar el combate.

La inyección ocurre a los 35 pasos de su intervención: la punta del inyector llega a las coordenadas del elegido y se registra un único evento. La intervención dura al menos 105 pasos; la mutación tiene su propia duración. El mismo recorrido se calcula para Juan, Petrosqui, Abel y Alberto. Los pasos usan el reloj del motor a 60 Hz, compartido por guardados y LAN.

## Conexiones que se conservan

- Los resultados anteriores determinan respaldo, corrupción de Juan y estado de ciudad. No se inventan antecedentes para una partida de campaña incompleta.
- Final 1 corresponde a Juan; Final 2 usa al otro ganador y sus formas propias. Ayudar en el cierre no convierte a Juan en alcalde si perdió ni borra un pacto confirmado.
- El selector de finales de prueba sigue disponible antes de comenzar y sus partidas se guardan por separado.
- El combate conserva tres acciones por héroe, seis ataques del jefe, cinco fases, tres formas y modalidades de uno a cuatro participantes. Los avisos, pinchos, plataformas y ascenso siguen ligados a las fases.
- Se reutilizan personajes, poses y cuadros del juego para las animaciones. El único fondo añadido en esta revisión es la tarima amplia; su prompt y procedencia están en `tools/arte/referencias-mision8/PROMPTS.md`. La fuente, marcos, botones y paleta siguen los recursos existentes.

La adaptación mantiene el recuento legítimo y la responsabilidad por el pacto acordados en la implementación anterior. Por ello, el cierre de la segunda ruta respeta al otro ganador y no sustituye automáticamente su cargo por Juan. El disparo usa el pulso visual del juego.

## Evidencias y validación

El paquete actualizado aprobó **246 pruebas**, con cero fallos, errores u omitidas, incluidos 18 recorridos completos del combate. Las pruebas nuevas comprueban avance manual, contacto para los cuatro candidatos, pausa y restauración durante la inyección, separación de plazas, eventos del epílogo y sincronización narrativa por TCP. Registro: [revision-guion-pruebas.log](evidencias/revision-guion-pruebas.log).

Las capturas se produjeron con el renderer real de Processing y perfiles temporales. Son estados preparados por herramientas de prueba; no añaden accesos especiales al juego normal.

| Escena revisada | Evidencia |
|---|---|
| Una voz y su retrato | [Sofi](evidencias/dialogo-sofi.png) |
| Cuatro plazas en la tarima | [Tarima](evidencias/tarima.png) |
| Vuelo al elegido | [Vuelo](evidencias/vuelo-2.png) |
| Inyección por candidatura | [Juan](evidencias/inyeccion-0.png), [Petrosqui](evidencias/inyeccion-1.png), [Abel](evidencias/inyeccion-2.png), [Alberto](evidencias/inyeccion-3.png) |
| Desaparición del público | [Chasquido](evidencias/desaparicion.png) |
| Acercamiento y recuperación | [Equipo](evidencias/epilogo-acercamiento.png), [elegido recuperado](evidencias/epilogo-recuperacion.png) |
| Disparo e impacto | [Pulso](evidencias/epilogo-pulso.png), [impacto](evidencias/epilogo-neutralizacion.png) |
| Cierre de ambas rutas | [Juan](evidencias/final.png), [otro ganador](evidencias/otro-final.png) |

Resultados de las 15 capturas de esta revisión: `evidencias/revision-guion-capturas.json`. Hash del paquete y límites de validación: `evidencias/validacion.json`. Continúan pendientes las comprobaciones con mando físico, LAN entre computadores distintos y balance con jugadores humanos.
