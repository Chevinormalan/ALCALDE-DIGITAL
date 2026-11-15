# Misión 4 · La plaza que dejó de escuchar

> Campaña vigente: esta misión requiere ganar la anterior (excepto la misión 1). Esc/P/Menú abre la pausa común y G guarda. El acceso, la recuperación y el guardado se coordinan desde el mismo sistema. Consulta [campaña y guardados](../campana/README.md); sus reglas sustituyen los menús particulares descritos en notas históricas inferiores.

Integrada en el juego principal desde `alcalde_digital_Entrega 4_6.zip`, recibido el 12 de noviembre de 2026. En el selector aparece como «Juan en campaña». Conserva narrativa, escenarios, animaciones, enemigos y audio del aporte. Juan sigue siendo candidato; las elecciones definitivas pertenecen a la misión 8.

Desde **Jugar → Elegir misión 4**, conduce hasta la plaza, escucha al público y sube a la tarima. Responde cuatro preguntas y descarga Civitas tras la visita de Palomo. Investiga o responde a tres rumores y enfrenta al Troll, SpamBot y Deepfake. Puedes conversar con dos vecinos en el camino para recuperar salud. El epílogo explica que detener amenazas no restaura la confianza de inmediato.

| Acción | Teclado | Mando |
|---|---|---|
| Mover / conducir | WASD o flechas | Stick izquierdo / cruceta |
| Hablar, confirmar, continuar | Enter / E | A / ✕ |
| Elegir respuesta | Izq./Der. + Enter; S/N | Cruceta + A |
| Investigar rumor / responder | V / N | Elegir con cruceta y A |
| Atacar | F (J anterior) | X / □ o RT |
| Escudo | Q (K anterior) mantenida | Y / △ o LT |
| Esquivar | Espacio (Shift anterior) | RB |
| Pausa / reanudar | Esc / P | Menú / B |
| Controles y ayuda | H | Vista |

El ratón elige respuestas, acciones de Civitas, instalación, guardado y botones de pausa. En el resultado, Enter regresa al selector; Esc/P abre pausa.

El capítulo usa simulación a 60 pasos por segundo conducida por `ClienteSketch`. Dibuja en un búfer de 1280 × 720 del mismo PApplet, con coordenadas del aporte escaladas proporcionalmente de 1024 × 576. `TemaPixel.marco` dibuja sus paneles y la pausa. No abre otra ventana ni otro ejecutable.

El checkpoint vive en `~/.local/state/alcalde-digital/mision4.properties` (o `ad.guardados`). Se guarda periódicamente, al cambiar de escena y al salir. Es un checkpoint narrativo del aporte: algunas conversaciones se reinician al recuperarlo, y el combate recupera enemigo, salud y ubicación, sin proyectiles ni teclas mantenidas. Al entrar ofrece Continuar o Nueva partida. `ad.mision4.guardar` permite señalar un archivo del formato recibido para importarlo explícitamente.

Al completar se conservan las reglas de indicadores del aporte: fuentes investigadas y reputación modifican la ciudad. El crédito se registra una sola vez por identidad de partida en `campana.properties`, preservando las claves electorales y los resultados de otras misiones. Una partida nueva tiene identidad nueva. La misión 4 no decide ganador ni limpia la corrupción de Juan.

Modalidad individual local. No añade transporte TCP ni nuevos árboles/grafos a las mecánicas del aporte. Las estructuras funcionales y consultas de las demás misiones se conservan. Validación y capturas en [integración](../integracion-misiones46/README.md).
