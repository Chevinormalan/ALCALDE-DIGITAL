# Mejora audiovisual de misiones 4 y 6

Entrega vigente: alcalde_digital_6_sonido_suave.zip. Fecha: 12 de noviembre de 2026. Extrae todo y abre Jugar.bat; requiere Java 21 o superior.

La revisión más reciente suaviza los motores y añade movimiento al candidato en el sofá. Sus archivos, niveles y verificaciones están en [AJUSTE_SONIDO_Y_SOFA.md](AJUSTE_SONIDO_Y_SOFA.md). El resto de este documento conserva el informe de la mejora audiovisual previa, incluido el hash del JAR de aquella entrega.

## Presentación implementada

La plaza y la carretera se basan en las imágenes nuevas del Escritorio, con los personajes, HUD y letreros separados del fondo. El camino se integra en el mundo original de 2180 × 576 mediante bandas que conservan la relación de aspecto y coinciden con los bordes transitables existentes. Los obstáculos conservan sus coordenadas y huellas; su vegetación ahora comparte la paleta violeta de las referencias. El coche, la cámara y los controles siguen siendo los existentes.

Juan utiliza el mismo proveedor de sprites en ambas misiones: 40 poses procedentes de frames_alcalde y 16 celdas complementarias para ataque, defensa, daño, caminar de espaldas y sentarse. No se crea otra entidad de personaje. El ataque tiene prioridad visual durante todos los 0,16 segundos de swordTime, incluso si se pulsa movimiento o escudo y aunque exista invulnerabilidad por daño. No se cambian la duración, el alcance, el daño ni las esperas de combate. Al salir del combate se retiran la pose y el arco de ataque, evitando que un temporizador antiguo detenido por la transición los deje dibujados durante la victoria o la caminata.

Se corrigió el anclaje del dibujo de Juan durante el discurso de Misión 6: permanece dentro de la tarima. La plaza también utiliza la nueva referencia. En el sofá se muestra de frente, con una pose de descanso y las manos sobre las rodillas; la mesa pasa por delante de sus pies. Se conservan la casa y sala modernas, el dron, los corazones, el guardado, la campaña y los dos cierres.

## Imágenes utilizadas

| Original del Escritorio | Formato y tamaño | Integración |
|---|---|---|
| ejemplificacion de escenario alcalde.webp | WebP VP8, 1024 × 546 | Plaza limpia sin personajes ni interfaz incrustados. |
| Ejemplificacion_trayecto_carro.webp | WebP VP8, 1024 × 539 | Camino, bordes y árboles violetas. |
| escena_ataque_alcalde.webp | WebP VP8, 1024 × 549 | Referencia de postura de ataque y entorno de combate. |
| frames_alcalde.webp | WebP VP8, 1024 × 572 | Identidad, poses y animaciones del candidato existente. |

Los originales se incluyen en referencias_documento/mejora_audiovisual/. Los recursos finales y los prompts exactos de la herramienta integrada de imágenes están en ARTE_AUDIOVISUAL.md.

## Sonidos e interacción

Se reutilizan sword.wav, shield.wav, hit.wav y dialogue.wav de Misión 4, además de ui.wav y dash.wav de Misión 3. Sus archivos no se modifican.

Se generan 19 WAV originales para el proyecto, mono PCM de 16 bits y 44100 Hz: carro_ralenti, carro_marcha, carro_acelerar, helicoptero_hover, helicoptero_vuelo, helicoptero_descenso, ambiente, freno, impacto, disparo_jugador, disparo_dron, cohete, explosion_cohete, destruccion_dron, dano, paso_adoquin, paso_madera, radio y encendido_helicoptero. El generador reproducible es tools/GenerarAudioMisiones.java; no se descargan sonidos ni se agregan dependencias.

El motor del carro mezcla ralentí, marcha y aceleración según velocidad real y dirección de las teclas. Al soltar o invertir la entrada se retira la aceleración y se activa el frenado una sola vez al entrar en ese estado. Las hélices distinguen vuelo estacionario y desplazamiento, responden al movimiento y bajan progresivamente al aterrizar. El despegue tiene arranque de turbina; el impulso reutiliza su efecto existente. Los pasos distinguen adoquín y madera y las confirmaciones responden a interacciones aceptadas.

Los disparos se detectan tras las actualizaciones reales del motor, no por pulsar una tecla que pudiera estar bloqueada por recarga. La ráfaga del dron tiene un timbre diferente. El lanzamiento y el impacto de cohete son distintos; se detecta el misil que impacta aunque una bala actualice el destello en el mismo paso. La destrucción se emite una sola vez al comenzar la explosión visual a los 0,5 segundos. La transmisión de Mr. Palomo incluye un breve efecto de radio.

Los clips se cachean y los bucles se funden sin reiniciarse en cada fotograma. Hay límites de voces, intervalos mínimos y cancelación de eventos pendientes al pausar o cerrar. La reproducción se detiene al salir. Se sustituyó la espera serial de la clase de audio anterior, que podía retrasar efectos.

## Archivos principales modificados o añadidos

Las rutas siguientes parten de entrega1-nueva/.

| Archivo | Cambio |
|---|---|
| src/main/java/alcaldedigital/cliente/mision4/MisionCuatroGame.java | Solo render: plaza, camino, obstáculos, poses y prioridad visual del ataque. |
| src/main/java/alcaldedigital/cliente/mision4/SpriteBank.java | Atlas compartido y recorte alfa de los recursos decorativos. |
| src/main/java/alcaldedigital/cliente/mision4/MisionCuatroSketch.java | Observación del audio por paso, eventos aceptados, pausa/cierre y prueba visual opcional. |
| src/main/java/alcaldedigital/cliente/mision4/SoundEffects.java | Mantiene el acceso a los efectos originales y añade conducción reactiva. |
| src/main/java/alcaldedigital/cliente/mision4/Autoguardado.java | Ruta de prueba opcional para no tocar la partida personal; formato y ruta normal conservados. |
| src/main/java/alcaldedigital/cliente/mision6/MisionSeisSketch.java | Sprites de Juan, tarima, sofá frontal, mesa en primer plano y audio. |
| src/main/java/alcaldedigital/cliente/mision6/AudioMisionSeis.java | Observador sonoro sin mutaciones del motor. |
| src/main/java/alcaldedigital/cliente/presentacion/JuanSprites.java | Selección de celdas, direcciones, pivotes y escala. |
| src/main/java/alcaldedigital/cliente/presentacion/AudioEscena.java y AudioEscenaSalida.java | Caché de clips, fundidos, control de voces y salida comprobable. |
| src/main/java/alcaldedigital/cliente/ClienteSketch.java | Activación de pruebas opcionales de la Misión 4; ejecución normal conservada. |
| tools/GenerarAudioMisiones.java y tools/VerificarSalidaAudio.java | Síntesis original y comprobación del dispositivo. |
| tools/Mision4VisualChecks.java | Coloca al actor en posiciones coherentes para capturas de las escenas narrativas. |
| verificar-audiovisual.ps1 | Compilación, protección de lógica, recorridos y registros. |

También se añaden pruebas de presentación/audio en src/test/java/alcaldedigital/cliente/{mision4,mision6,presentacion}, cinco PNG en src/main/resources/cliente/{mision4,presentacion} y los 19 WAV en cliente/presentacion/audio/. Los recursos anteriores no se eliminan.

## Verificación realizada

- Compilación completa y 124 pruebas JUnit aprobadas, cero fallos. Incluyen la permanencia de ataque durante su ventana, audio de ataques aceptados, cadencias, proyectiles simultáneos, motor/frenado, pasos, aterrizaje, pausa, alfa en las 56 celdas y decodificación completa de los 19 WAV con comprobación de niveles.
- Comparación exacta con las copias previas: CombatSystem.java, CollisionWorld.java y MisionSeisMotor.java permanecen idénticos. También permanece idéntico todo MisionCuatroGame anterior al método draw, incluyendo entradas, física, condiciones y progresión.
- El recorrido original Mision4Checks pasa conducción, colisiones, escudo, esquiva, enemigos, recompensas, guardado y render. Mision4VisualChecks carga y captura las fases.
- Doce ejecuciones gráficas aprobadas (once de la batería y una revisión final adicional de Misión 4): conducción/combate M4, recorridos completos M6 individual y cooperativo, discurso, caminata, sofá, aterrizaje, pausa y regresiones de misiones 2, 3 y 5.
- Se inspeccionaron capturas de carretera, ataque, discurso sobre la tarima y sofá frontal.
- JavaSound abrió e inició las 19 muestras originales en el dispositivo del equipo, sin errores. En la última sesión registrada de cada modo, la destrucción se reproduce una vez. Después de la pausa registrada no hay nuevas reproducciones.

Evidencias: verificacion/audiovisual/compilacion.log, preservacion.log, m4-recorrido-original.log, m4-escenas.log, resultados.json, resultados-m4-final.json, auditoria-audio.json y salida-equipo.log. En grafica/ se incluyen capturas y registros. Los archivos *.ultimo.log contienen exclusivamente la sesión final; los registros sin ese sufijo conservan también ejecuciones anteriores. Los informes JUnit se guardan en verificacion/junit/.

## Límites

Los recorridos fueron automatizados en una ventana real. No se hizo una partida humana exhaustiva ni una prueba con mando físico. La comprobación de JavaSound demuestra apertura e inicio de reproducción, y la inspección PCM comprueba niveles de los archivos; no equivale a una escucha física ni a una evaluación de la mezcla en tus altavoces. Los bucles de la prueba del dispositivo se detienen después de una muestra breve. Los efectos son una síntesis original con carácter mecánico para este juego, no grabaciones de vehículos reales.

El ZIP anterior se conserva en ED2.DANI/alcalde_antes_audiovisual_20261112_022603.zip. Las pruebas usan guardados aislados y no reemplazan los avances personales. No se han cambiado los archivos de las misiones 1, 2, 3 o 5.
SHA-256 del JAR verificado: 4CAEE8304A72F08079582F901C1A4D611E2B73A23ACA70D5A825FCF8DCE9D67E
