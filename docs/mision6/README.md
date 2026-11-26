# Misión 6 · Cielos de campaña

> Campaña vigente: esta misión requiere ganar la anterior (excepto la misión 1). Esc/P/Menú abre la pausa común y G guarda. El acceso, la recuperación y el guardado se coordinan desde el mismo sistema. Consulta [campaña y guardados](../campana/README.md); sus reglas sustituyen los menús particulares descritos en notas históricas inferiores.

Nueva integración del material recibido en `alcalde_digital_Entrega 4_6.zip`. Esta versión usa su guion de vuelo e entrevista; no restaura la antigua caravana retirada.

Desde **Jugar → Elegir misión 6 → Jugar local**, escucha a Juan, acércate a él con el influencer y acompáñalo al helicóptero. Allí eliges una o dos personas. Sigue la práctica, combate al dron durante tres fases y recoge corazones para recuperar vida. Los misiles se desbloquean en la segunda fase. Tras la autodestrucción y la transmisión de Palomo, aterriza, explora la sala y acércate al sofá para iniciar las tres preguntas de la entrevista. Sus dos cierres, escucha y convicción con diálogo, son válidos y no eligen alcalde ni alteran la corrupción de Juan.

Tras el discurso, Juan baja del escenario y acompaña al influencer por una ruta que evita los puestos y las vallas. El helicóptero tiene una zona señalizada y la práctica confirma los impactos en la diana. Durante el combate, la información de vida y recargas queda en la franja superior, con un distintivo diferente para cada nave.

El dron anuncia el abanico de la fase 1, fija una trayectoria hacia un jugador en la fase 2 y alterna ráfagas con un halo de corto alcance en la fase 3. Las líneas punteadas y el aro ámbar anuncian dónde habrá peligro. Los corazones tienen en cuenta los proyectiles que cruzan su camino. El guardado conserva un aviso en curso junto con su rumbo.

En la llegada cooperativa, las dos naves aterrizan por turnos. Un plano de la puerta conecta el aterrizaje con la sala, donde el influencer empieza junto a la entrada. Juan y el influencer cierran la entrevista por turnos, con un plano más abierto del sofá.

| Acción | Solo | Dos personas |
|---|---|---|
| Mover | WASD / flechas | J1 WASD; J2 flechas |
| Disparar (mantener) | F (J/Z anteriores) | J1 F; J2 L |
| Misil | Q (K/X anteriores) | J1 Q; J2 K / M |
| Impulso | Espacio (Shift anterior) | J1 Espacio; J2 Mayús (/ o num. 0 anteriores) |

Enter/E confirma e interactúa; las selecciones y entrevista se manejan con flechas y Enter, o con 1/2. El ratón permite elegir modalidad, respuestas, guardado y botones de pausa. Esc/P abre pausa; H muestra controles. En derrota, R/Enter vuelve a la práctica. En el cierre, Enter regresa al selector.

El mando controla J1: stick/cruceta para mover; A para hablar/confirmar; X/RT para disparar; Y/LT para misil; RB para impulso. Menú/B pausa o vuelve; Vista abre ayuda y el botón **Ver controles** muestra ambos dispositivos. En modo de dos personas, J2 es el influencer y utiliza este mismo teclado. G siempre guarda la partida; ya no dispara misiles.

## LAN cooperativa

Al entrar a la misión, **Crear partida LAN** abre una sala de dos puestos. **Unirse a partida LAN** muestra las salas de misión 6 disponibles. El anfitrión controla la introducción y la entrevista. En vuelo, controla a Juan y el invitado controla al Influencer. En cada equipo se usan WASD o flechas para mover, F para disparar, Q para misil y Espacio para impulso. La pausa se sincroniza entre ambos y ante una desconexión se puede recuperar el mismo puesto.

La conexión usa TCP **48506** y el descubrimiento usa UDP **48516**. El anfitrión guarda la partida de red en `mision6-lan.properties`. El guardado local está en `mision6.properties`.

La misión se dibuja en el mismo PApplet principal, en 1280 × 720, escalando proporcionalmente las coordenadas de 1024 × 576 del aporte. Comparte Pixelify Sans y marcos `TemaPixel`. La pausa congela simulación, diálogos, recargas y animaciones; consume los clics y limpia entradas para impedir acciones accidentales al volver. Desconexión del mando o pérdida de foco pausa la partida.

Autoguardado cada dos segundos y al cambiar de escena, pausar y salir: `~/.local/state/alcalde-digital/mision6.properties`, con copia anterior `.bak`. `ad.guardados` cambia la carpeta del perfil; `ad.mision6.guardar` permite importar expresamente un archivo externo. Recupera posiciones, modo, proyectiles, vida, recargas, corazones y entrevista. Las teclas retenidas no se guardan. Se mantienen lectores de versiones 1 y 2 del formato recibido.

Conserva escenarios, quince cuadros del dron, helicópteros, Juan, influencer sentado y sonido reactivo del ZIP. Reutiliza los recursos ya presentes de influencer y Palomo; no se sustituyeron por las copias antiguas. La narrativa sigue tratando a Juan como candidato. El resultado queda visible para el selector durante la sesión; este capítulo no concede indicadores nuevos que el aporte no definía.

Las pruebas recibidas recorren ambos modos hasta sus dos entrevistas usando daño y física reales. Las verificaciones gráficas y sus límites están en [integración](../integracion-misiones46/README.md).
