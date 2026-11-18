# Infección, ataques y reposo de misión 8

Actualización del 16 de noviembre de 2026. Respaldo previo en `../Respaldos y ProyectsAnterirores/antes-animaciones-mision8-20261116/mision8.tgz`, relativo a la raíz del proyecto. El paquete distribuido contiene los cambios; una ventana abierta con una versión anterior debe reiniciarse.

## Cambios visibles

- Cada alcalde pasa por **16 cuadros propios de infección**: reacción, dolor, marcas, crecimiento de cables, formación del núcleo y levitación. Son 64 cuadros nuevos entre los cuatro candidatos. Sustituyen la alternancia de dos poses de la escena anterior.
- Cada forma de cada alcalde tiene **8 cuadros de ataque**: reposo, preparación, carga, apuntado, emisión, extensión, recuperación y regreso al reposo. Son 96 cuadros nuevos. El brillo y el retroceso acompañan las emisiones reales de cada patrón, incluidas las ráfagas repetidas.
- Lluvia de mensajes, bots, señales, muros y orbes tienen apariencias diferenciadas, estelas según su velocidad y avisos propios. Las áreas de colisión y el balance del combate se conservan.
- Sofi, periodista e influencer usan su cuadro de pie al estar quietos. Las poses de caminar o correr se muestran durante los desplazamientos de las escenas; en combate se vuelve al reposo al soltar la dirección, salvo que se esté saltando, atacando o recibiendo daño.

El encuadre del ascenso se desplaza gradualmente al acercarse al jefe y los avisos de esa fase aparecen abajo, para dejar visibles su cabeza y sus manos sin alterar la cámara usada por la simulación.

La infección se reproduce a diez cuadros por segundo hasta completar los 16 pasos y mantiene su última pose con levitación suave hasta continuar el diálogo. El reloj del motor determina los cuadros, los brillos y el retroceso: pausa, guardado y LAN conservan la pose. Movimiento reducido desactiva el balanceo y retroceso añadidos.

## Recursos guardados

Todos se generaron con `imagegen` integrado usando los cuadros vigentes como referencia. Los originales permanecen en el proyecto y la ejecución recorta sus celdas sin repintarlas.

| Recurso | Distribución |
|---|---|
| [infeccion-juan.png](../../src/main/resources/cliente/mision8/infeccion-juan.png) | 4 × 4, 16 cuadros |
| [infeccion-petrosqui.png](../../src/main/resources/cliente/mision8/infeccion-petrosqui.png) | 4 × 4, 16 cuadros |
| [infeccion-abel.png](../../src/main/resources/cliente/mision8/infeccion-abel.png) | 4 × 4, 16 cuadros |
| [infeccion-alberto.png](../../src/main/resources/cliente/mision8/infeccion-alberto.png) | 4 × 4, 16 cuadros |
| [ataques-forma-1.png](../../src/main/resources/cliente/mision8/ataques-forma-1.png) | 8 × 4, un candidato por fila |
| [ataques-forma-2.png](../../src/main/resources/cliente/mision8/ataques-forma-2.png) | 8 × 4, un candidato por fila |
| [ataques-forma-3.png](../../src/main/resources/cliente/mision8/ataques-forma-3.png) | 8 × 4, un candidato por fila |

Prompts exactos y corrección del alfa de Petrosqui: [PROMPTS_ANIMACIONES.md](../../tools/arte/referencias-mision8/PROMPTS_ANIMACIONES.md). El manifiesto registra 352 sprites distintos con transparencia y cinco fondos, incluidos los anteriores. `python3 tools/arte/preparar_mision8.py` comprueba los recursos y extrae los cuadros.

## Comprobación

**254 pruebas aprobadas**, sin fallos, errores ni omitidas. Las pruebas añadidas cubren los tres roles al soltar movimiento, los 16 pasos de infección, carga/disparo/recuperación de los seis ataques en las tres dificultades, restauración y pausa de la pose, las tres formas, el encuadre sin modificar la simulación y la transparencia y singularidad de los 160 cuadros nuevos. Se mantienen los 18 recorridos completos y las pruebas TCP anteriores.

Registro: [animaciones-pruebas.log](evidencias/animaciones-pruebas.log). Las capturas usan el renderer de Processing y perfiles temporales. Las secuencias GIF son vistas de revisión a unos 15 cuadros por segundo; el juego mantiene su simulación a 60 Hz.

- Infección: [Juan](evidencias/infeccion-0.gif), [Petrosqui](evidencias/infeccion-1.gif), [Abel](evidencias/infeccion-2.gif), [Alberto](evidencias/infeccion-3.gif).
- Ataques: [orbes](evidencias/ataque-orbes.gif), [rayos](evidencias/ataque-rayos.gif).
- [Reposo de los protagonistas](evidencias/reposo.png).

Reproducción: `python3 tools/verificar_mision8.py --animar infeccion-0 infeccion-1 infeccion-2 infeccion-3 ataque-orbes ataque-rayos reposo`. Los otros patrones y formas se capturan con `ataque-lluvia ataque-bots ataque-abanico ataque-muro fase3 fase5` sin `--animar`.
