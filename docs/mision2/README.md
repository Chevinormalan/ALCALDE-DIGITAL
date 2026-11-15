# Misión 2 — La red Civitas

> Campaña vigente: esta misión requiere ganar la anterior (excepto la misión 1). Esc/P/Menú abre la pausa común y G guarda. El acceso, la recuperación y el guardado se coordinan desde el mismo sistema. Consulta [campaña y guardados](../campana/README.md); sus reglas sustituyen los menús particulares descritos en notas históricas inferiores.

Misión jugable integrada en el selector y en la ventana principal Processing de 1280 × 720. Basada en los dos bocetos de Sebastián, la oficina de referencia y el diseño de la periodista guardados en [`referencias/`](referencias/). El análisis inicial está en [ANALISIS_MISION2.md](ANALISIS_MISION2.md). La defensa continua se incorporó después a partir del nivel `JUF.java` de `Lab2_FERIA_GAMER (2).zip`.

## Recorrido

La periodista comienza frente a su escritorio. Mr. Palomo entra volando, conversa y sale. Civitas anuncia el ataque; al cerrar el aviso comienza la defensa. Los seis computadores empiezan seguros y uno cae al comenzar. Después llegan oleadas que comprometen uno, dos o tres equipos seguros distintos, según la dificultad. Acercarse y pulsar E una vez repara un computador, que puede ser atacado de nuevo. La barra central representa el porcentaje seguro actual. En los últimos ocho segundos no llegan oleadas regulares; cuando quedan dos segundos, un computador seguro elegido al azar se pone rojo una sola vez. Sigue siendo reparable con E antes de que termine el reloj. La victoria se decide al terminar el tiempo: los seis deben estar seguros. Después regresa Palomo y se representa uno de los dos finales del boceto: una deposición frente a la periodista en el éxito, o un proyectil y explosión pixelados sobre el núcleo en el fallo normal. E/Enter en el resultado vuelve al selector.

| Dificultad | Tiempo | Intervalo de ataque | Oleadas regulares | Probabilidad de 1/2/3 equipos |
| --- | ---: | ---: | ---: | ---: |
| Fácil | 60 s | 10 s | 6 | 75/20/5 % |
| Normal | 60 s | 6 s | 9 | 55/35/10 % |
| Difícil | 50 s | 4 s | 11 | 40/40/20 % |

Normal y Difícil aceleran las amenazas para compensar la nueva movilidad. Difícil conserva los 50 segundos, una pulsación de reparación y la condición final del juego anterior; su intervalo se redujo de 5 a 4 segundos a petición posterior de Sebastián. La oficina de misión 2 conserva sus seis computadores, en vez de los cinco de aquel juego. Las oleadas están ligadas a la semilla del juego para reproducir una partida. Una oleada toma solo equipos seguros; si quedan menos de los sorteados, afecta únicamente a los disponibles.

Además de las oleadas de la tabla, todas las dificultades tienen una alerta final al quedar dos segundos: un único equipo seguro pasa a rojo. Si todos ya están en peligro, se informa del estado sin añadir otro. El aviso final aparece una sola vez y se detiene durante la pausa.

Solo en el primer intento de **Difícil** se realiza un sorteo independiente con un 30 % de probabilidad de fallo crítico inevitable del servidor. Si ocurre, se ve la avería y se explica que la defensa no se puede completar; ese resultado no penaliza la campaña. La marca del sorteo se guarda en `~/.local/state/alcalde-digital/mision2-dificil-intentado.flag` antes de entrar a la misión. Los siguientes intentos en Difícil siempre son **ganables**, aunque se puede perder por dejar computadores sin reparar. Fácil y Normal no participan en ese sorteo.

WASD/flechas o stick izquierdo mueven; mantener Shift o RB hace correr; Espacio o X del mando lanza un salto hacia delante en la dirección de movimiento o, si no la hay, en la última dirección mirada. E/Enter o A del mando avanzan diálogo y reparan la estación próxima en peligro. Durante la defensa, Espacio se reserva al salto. Esc/P u Options abren la pausa común con Continuar, Ayuda, Guardar, Consultas y Guardar y salir. El reloj, los ataques y el salto no avanzan durante diálogos ni pausa.

Caminar recorre 300 px/s, correr 450 px/s y el salto dura 0,30 s con hasta 156 px de impulso horizontal y una elevación visual de 18 px. El impulso tiene una espera total de 0,65 s desde su inicio, se activa una sola vez por pulsación y no permite reparar hasta aterrizar. El punto de colisión permanece en el suelo y se comprueba en pasos de hasta 5 px: no atraviesa paredes, muebles ni el servidor.

## Arte, escenario y sonido

`src/main/resources/cliente/mision2/` contiene el fondo cenital recompuesto y hojas separadas de 16 cuadros de caminata, 16 de carrera, 16 de salto y cuatro poses de acción. Cada hoja de movimiento tiene cuatro direcciones y cuatro fotogramas por dirección. Las hojas completas de origen están en `tools/arte/referencias-mision2/`; las referencias entregadas por Sebastián están en `docs/mision2/referencias/`. El personaje se recorta por alfa por celda y conserva orientación y escala. Palomo reutiliza los cuadros de vuelo y diálogo de misión 3, derivados de la misión 1. Los sonidos de interfaz, alarma, protección y fallo reutilizan los WAV de misión 3; si no hay dispositivo de audio, se mantienen las señales visuales.

El servidor central, los seis escritorios, el puesto inicial, paredes y muebles periféricos bloquean el paso. Los cables son visuales. Los seis puntos de interacción son accesibles desde los pasillos. Los indicadores de terminal son rojos mientras están en peligro y cian cuando están seguros; incluyen texto para no depender del color. El servidor añade fotogramas de luces, pantallas, conectores y fallos pixelados cian o rojos sobre la carcasa dibujada en el escenario.

## Estado compartido

`RedCivitas` contiene un grafo en estrella con núcleo y seis estaciones. Un ataque reabre el enlace de una estación; repararla lo aísla. Una búsqueda en anchura calcula cuántas estaciones están expuestas desde el núcleo. La barra general se deriva de los equipos seguros. `JuegoCliente` conserva la dificultad y el último resultado de la misión; `ProgresoCampana` modifica confianza y desinformación según los equipos que sigan protegidos al final. La misión no declara información verificada por el solo hecho de proteger infraestructura.

## Verificación

`bash herramientas.sh paquete` compila y ejecuta las pruebas JUnit. `MisionDosRedTest` cubre ataques y reparaciones repetidos, aislamiento del grafo, selección de tres dificultades, pausa, regreso al menú, persistencia del primer intento difícil y consecuencias de un resultado parcial. La autoprueba gráfica se ejecuta con `-Dad.mision2.integracion=true -Dad.mision2.autoprueba=true` y comprueba carrera, salto, colisiones, seis rutas transitables, oleadas distintas, aparición única y reparación del ataque final a los dos segundos, fallo crítico solo en Difícil, los tres ritmos y ambos resultados. Una partida manual completa y un mando físico requieren una sesión gráfica con acceso al dispositivo.
