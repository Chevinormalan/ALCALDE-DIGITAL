# Banda sonora integrada

La campaña utiliza 20 temas principales, dos variantes de cierre, 32 pistas de percusión/orquesta separadas y 50 nuevos efectos/ambientes. Los efectos anteriores de armas, conducción, vuelo y habilidades siguen conectados a sus acontecimientos. La música dura 23 min 34 s antes de repetir; los stems se superponen, no se cuentan como canciones adicionales.

[Montaje de escucha, 54 s](evidencias/muestra-banda-sonora.wav) · [Captura de la mezcla del juego](evidencias/mezcla-real.wav) · [Catálogo completo](../../src/main/resources/cliente/audio/catalogo.json) · [Auditoría de los WAV](evidencias/auditoria-recursos.json).

## Uso

Entra en **Cómo jugar → Audio**. Hay niveles separados de general, música, efectos, ambiente e interfaz, silenciado, intensidad dinámica/reducida y tonos de texto opcionales. Se guardan inmediatamente en `audio.properties`, dentro de la carpeta de preferencias del juego. Los tonos de texto empiezan apagados. «Probar sonido» respeta los niveles general/interfaz y el silencio.

La música baja suavemente durante los diálogos. La pausa conserva su posición, retira efectos interrumpidos y deja audible la interfaz. Volver al selector cambia al tema del menú. El sonido es local, también en LAN; no se transmite música por la red ni se modifican los snapshots o reglas de la campaña.

## Mapa musical

| Uso | Temas | Adaptación |
|---|---|---|
| Menú, investigación y cierres | G01, G02, G03 | Confianza, reconstrucción o crisis tienen arreglos distintos. |
| M1 · parque y rumor | M101, M102 | Urgencia según fracción del reloj, incluidas dificultades 60/30/12 s. |
| M2 · redacción y defensa | M201, M202 | Intensidad por reloj y equipos en peligro; fallo crítico solo después de activarse. |
| M3 · NovaMall | M301, M302, M303 | Exploración, combates y tres fases del Núcleo Viral; diálogo atenuado. |
| M4 · campaña | M401, M402 | Conducción/plaza, tres encuentros y regreso reflexivo con G02. |
| M5 · servidor | M501, M502 | Cinco rondas, descanso en turno/decisión y tensión en el último ataque. |
| M6 · vuelo | M601, M602 | Despegue/entrenamiento, tres fases del dron, aterrizaje; entrevista sin favorecer una ruta. |
| M7 · primicia | M701, M702 | Rivalidad, cuenta atrás, intensidad por distancia; llegada sin proclamar alcalde. |
| M8 · elección y clímax | M801, M802 | Ceremonia neutral; intervención tras inyección; cinco fases; recuperación y cierre según ciudad. |

Las 16 partituras locales incluyen base, ritmo y épica con idéntico largo de 32 compases y posición compartida. Las seis piezas generadas son mezclas estéreo completas: su intensidad se ajusta con volumen/atenuación; no se presentan como stems extraídos. Los BPM de estas seis pistas son indicaciones de producción, no una medición automática del resultado.

## Motor y formatos

Los recursos de ejecución son WAV PCM estéreo, 16 bits, **22.050 Hz**. Se eligió esa frecuencia para reducir tamaño y memoria; sustituye la propuesta inicial de 44.100 Hz del análisis. Una SourceDataLine mezcla música, capas, ambientes y hasta 16 efectos; un limitador suave controla la suma. Los cálculos y fundidos dependen de muestras/tiempo, no del framerate gráfico.

`MotorAudio` carga música en un hilo independiente y mantiene como máximo las dos pistas que participan en el cambio; no precarga todo el álbum. Los efectos cortos se precargan en una caché acotada. `MezclaMusical` mantiene las capas alineadas. `Partitura` decide pista/intensidad según la fase real; `DirectorAudio` compara estados y descarta acontecimientos históricos al restaurar, retroceder o saltar snapshots.

Los nuevos WAV incluyen UI, ciudad/Civitas, verificación, reparación, movimiento, ataques, transición de fase, elección, inyección, mutación, recuperación, neutralización, victoria y derrota. Algunos son recursos de la biblioteca para ampliar acontecimientos; la integración conserva efectos existentes cuando identifican mejor una habilidad. Que un WAV exista no implica que reemplace cada sonido anterior.

## Producción y fuentes

- **Seis temas generados con vidIQ:** G01, G02, G03, M101, M102 y M201. Trabajos/prompt/resultado en [generaciones.json](generaciones.json). El servicio describe sus resultados como música original libre de regalías. Se consumieron los 150 créditos disponibles; no se contrató saldo adicional.
- **Catorce temas y dos variantes compuestos en MIDI original:** partituras editables base/ritmo/épica en [fuentes](fuentes/), renderizadas con FluidSynth y **GeneralUser GS de S. Christian Collins**. Se conserva la [licencia original](fuentes/GeneralUser-LICENSE.txt). Fuentes oficiales: [GeneralUser GS](https://schristiancollins.com/generaluser/) y [repositorio del autor](https://github.com/mrbumpy409/GeneralUser-GS). Se usaron instrumentos del banco, no las canciones de demostración.
- **50 efectos/ambientes:** síntesis original reproducible con numpy; no se tomó música de Hollow Knight. La referencia de ese juego solo informa la jugabilidad del jefe.
- Las seis grabaciones originales y el SF2 se archivan fuera del JAR en `/home/sebas/Documentos/LAB1/ProduccionAudio-20261117`. Evitar depender de /tmp para reediciones.

Para reproducir el render se requieren Python/numpy, FFmpeg, FluidSynth y las fuentes de esa carpeta:

```sh
python3 tools/audio/producir.py --fuentes /home/sebas/Documentos/LAB1/ProduccionAudio-20261117
python3 tools/audio/auditar.py
```

`--solo M802` permite rehacer una partitura concreta. El juego instalado no requiere estas herramientas ni conexión a los generadores.

## Comprobación

- 285 pruebas de reglas, red e interfaz aprobadas en la campaña, incluidas 18 nuevas de audio: recursos/capas, política musical, preferencias, pausa, fundido, independencia del tamaño de bloque y deduplicación de acontecimientos.
- Revisión nativa con perfil temporal y dispositivo real: Audio abierto desde Cómo jugar, cambio de volumen con clic reescalado a 960×600, pausa/interfaz/reanudación, cambios G01→M101→M201→M801→M802 y captura PCM. [Resultado](evidencias/resultado.json), [registro](evidencias/audio.log) y [pantalla](evidencias/audio-960x600.png).
- Auditoría: 22 piezas, 32 stems alineados, 50 efectos; pico máximo de mezcla de stems 0,75 y captura del juego sin muestras saturadas. Esta medición no sustituye una valoración subjetiva de música/mezcla en distintos altavoces.
- El mando físico no se pudo probar en esta sesión porque el sistema no concede acceso a sus dispositivos de entrada. Las pruebas de lógica de mando siguen aprobadas.

La propuesta anterior se conserva en [ANALISIS_BANDA_SONORA.md](ANALISIS_BANDA_SONORA.md); describe el diseño inicial. Este README y los registros describen la implementación realizada.
