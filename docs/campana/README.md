# Campaña, pausa y guardado comunes

Estado vigente: 14 de noviembre de 2026. Sustituye los comportamientos de pausa y acceso libre descritos en notas históricas de cada misión.

## Avance por victorias

Un perfil nuevo solo tiene disponible la misión 1. Para abrir una misión se debe ganar la anterior. Los niveles bloqueados muestran «Gana la misión N» y no tienen un botón activo. El control de navegación también comprueba el bloqueo; no basta con enviar una acción de selección. Las misiones ganadas se pueden repetir. La misión 8 sigue sin implementar.

El selector ofrece **Desbloquear todas las misiones** como opción manual. Un perfil nuevo empieza con la misión 1 disponible y el botón no se activa por sí solo. Al pulsarlo, abre las misiones 1 a 7 y guarda `desbloqueo.total=true`; no registra victorias. Los perfiles antiguos con ese indicador conservan el acceso. Sin acceso libre, una victoria guardada fuera de orden permanece registrada, pero su misión y las posteriores siguen bloqueadas hasta completar las anteriores.

| Misión | Victoria que desbloquea la siguiente |
|---|---|
| 1 | Resultado BUENO; un resultado neutral o malo no desbloquea |
| 2 | Defensa terminada con los seis equipos seguros; excluye fallo crítico |
| 3 | Completar el epílogo tras vencer al Núcleo Viral |
| 4 | Superar los tres combates y completar el epílogo |
| 5 | Resultado FINAL con victoria del motor autoritativo |
| 6 | Vencer al dron y terminar cualquiera de las dos entrevistas válidas |
| 7 | Un corredor humano llega primero; perder contra CPU o empatar no cuenta |

En dos jugadores de misión 7 gana uno de los roles humanos. Esto registra la victoria de esa misión para la campaña; no proclama alcalde ni cambia la condición de Juan. La elección definitiva sigue reservada a misión 8.

`ProgresoMisiones` conserva victorias y el acceso libre en `progreso-misiones.properties`, con escritura atómica, copia anterior y cerrojo entre procesos. No se interpretan como victorias los checkpoints, archivos de prueba ni el mero hecho de haber entrado a un nivel. Los guardados existentes permanecen y se pueden recuperar cuando la misión correspondiente esté disponible.

El menú inicial muestra **Continuar misión N** para la partida más reciente aún en curso y **Reintentar misión N** si terminó en derrota. Si la última partida fue una victoria, ofrece continuar la campaña desde el selector. Las misiones 5 y 7 recuperan también la modalidad y el personaje del archivo elegido. Un guardado de una misión bloqueada no permite saltar a ella; el acceso libre se activa únicamente desde su botón.

## Un menú para todas las misiones

Esc, P o Menú del mando abren la misma pausa:

Desde **Ayuda y controles → Ver controles** se abre un mapa gráfico con teclado y mando, específico de la misión y modalidad. En los modos de dos personas de 5, 6 y 7 muestra a J1 en dorado y J2 en cian con sus personajes. El mando usa nombres Xbox o PlayStation según el dispositivo detectado. Volver, Esc y B regresan a Ayuda sin reanudar; el mapa consume las demás entradas mientras está abierto. Las acciones globales son WASD/E/Espacio/F/Q para J1 y flechas/Enter/Mayús/L/K para J2 cuando esas acciones existen en la misión. G solo guarda.

[Captura de controles de la misión 5 con dos personas](evidencias/controles-mision5-dos.png).

1. Continuar.
2. Ayuda y controles.
3. Guardar partida.
4. Consultas de la misión.
5. Guardar y salir a misiones.

Arriba/abajo/Tab y Enter/E/A seleccionan; también funciona el ratón. G guarda sin salir. H/Vista abre ayuda. Las flechas cambian página dentro de la ayuda, incluidos los quince patrones de misión 5. Las consultas existentes de misión 1 y las redes/estructuras de 5 y 7 se conservan bajo pausa. Los niveles sin consultas adicionales remiten a su ayuda.

El modal consume clics y bloquea las teclas retenidas al reanudar; el stick debe volver al centro. La repetición automática no alterna la pausa. Perder foco o desconectar un mando pausa. En LAN la solicitud se envía al servidor y todos los clientes muestran la pausa común; la reanudación remota cierra también las consultas. El servidor mantiene sus condiciones de conexión de ambos roles.

La lógica común está en `cliente/misiones/PausaUnificada.java`; `ClienteSketch` conecta adaptadores a las sesiones locales o de red. Las vistas conservan sus funciones de ayuda y consulta, pero sus menús de pausa particulares no se presentan dentro del juego principal.

## Guardado y recuperación

El sistema usa el directorio del perfil ya existente:

```text
~/.local/state/alcalde-digital/
```

`ad.guardados` permite redirigirlo a otra carpeta. Al elegir un nivel con guardado se ofrece la misma pantalla **Continuar partida / Nueva partida / Volver**. Nueva partida no borra las victorias ni limpia los antecedentes electorales. Las modalidades de 5 y 7 siguen eligiéndose en su configuración, porque tienen checkpoints distintos para individual, teclado y LAN.

Se guarda al pausar, mediante G o Guardar, al usar Guardar y salir, al cerrar la ventana con una misión activa y automáticamente cada diez segundos de juego. Los motores de red conservan además su guardado autoritativo y de desconexión. Un fallo de guardado mantiene abierta la partida y muestra el error; no se confirma un guardado inexistente. En LAN se informa «Guardado solicitado al servidor».

| Misión | Recuperación |
|---|---|
| 1 | Fase, diálogo, Palomo, posición, reloj, peatones, fuentes, publicaciones, árboles e indicadores |
| 2 | Fase, red de seis equipos, reloj, salto, oleadas, decisiones de fallo crítico y estado exacto del generador aleatorio |
| 3 | Punto de control al inicio de la zona actual de NovaMall; conserva las zonas superadas y sus indicadores, y reinicia los desafíos de la zona actual |
| 4 | Checkpoint narrativo del aporte: ubicación, salud, enemigo y decisiones; algunas conversaciones se reinician y no conserva todos los proyectiles |
| 5 | Snapshot autoritativo por modalidad, incluido combate, dificultad y pausa |
| 6 | Snapshot recibido de vuelo, combate, proyectiles, recargas, corazones y entrevista, con copia de recuperación |
| 7 | Snapshot autoritativo de carrera, noticias, objetos, difusión, reputación y antecedentes, separado por modalidad y personaje |

Los codecs existentes de 4/5/6/7 se conservan; un sistema común coordina la entrada, el guardado y el directorio. Los nuevos archivos `mision1.properties`, `mision2.properties` y `mision3.properties` son versionados. No se deserializan clases indicadas por un archivo. El generador persistente de misión 2 reproduce la misma secuencia que `java.util.Random`. El acumulado de ciudad se persiste al guardar preservando las claves electorales de otras misiones.

## Validación

- Paquete integrado construido el 15 de noviembre de 2026 con Maven: **216 pruebas JUnit**, cero fallos, errores u omitidas y BUILD SUCCESS. [Log completo](compilacion-completa.log).
- El registro de [verificación final anterior](verificacion-final.log) documenta 14 pruebas afectadas por un ajuste previo; el paquete integrado se volvió a comprobar completo.
- Se mantienen los sockets reales de 5 y 7, los 2.970 casos aislados y 24 recorridos completos de batalla, y los nueve recorridos de carrera.
- Las pruebas nuevas verifican perfil bloqueado, selección directa, persistencia de victorias, derrotas, checkpoints sin victoria, pantalla común de recuperación, estados de 1/2, azar, escritura atómica, pausa, errores de guardado, mando simulado y reanudación remota.
- [Catorce revisiones gráficas](evidencias/resultados.json): selector bloqueado; pausa de las siete misiones; recuperación de 1/2/3/4/6 entre procesos; ayuda de misión 5. Las capturas están en `evidencias/`.
- JUnit aísla el perfil mediante `PerfilAislado`; los fixtures que necesitan un nivel avanzado declaran las victorias previas en sus temporales. La revisión gráfica también crea perfiles temporales y no desbloquea la campaña real.

Reproducir desde la raíz:

```bash
bash herramientas.sh paquete
python3 tools/verificar_sistema_campana.py
```

La segunda orden abre ventanas de prueba y silencia el audio. Las propiedades `ad.sistema.revision` se utilizan solo para esas escenas explícitas. No se verificó mando físico ni una LAN entre dos computadores; las comprobaciones de red usan clientes reales en el mismo equipo.

Respaldo anterior a este cambio: `LAB1/Respaldos y ProyectsAnterirores/antes-campana-pausa-guardado-20261112-155345`. No se eliminaron partidas anteriores ni se modificó la carpeta original `alcalde-digital-lab1-entrega1`.


## Cambio de navegación, 14 de noviembre de 2026

Se unificó la continuación de las partidas 1 a 7 en el menú inicial. Se recuperó el botón de acceso libre como opción manual; un perfil nuevo comienza en la misión 1 y una derrota no se registra como victoria.
