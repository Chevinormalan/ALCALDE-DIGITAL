# Integración de misiones 4 y 6 · 12 de noviembre de 2026

Se extrajeron únicamente los paquetes `cliente/mision4`, `cliente/mision6`, su presentación/audio compartidos, sus recursos y pruebas de `/home/sebas/Descargas/alcalde_digital_Entrega 4_6.zip`. La copia recibida era anterior al proyecto actual. No se importaron su misión 5, su navegación completa, JAR, configuración Maven, perfiles ni guardados de verificación.

## Resultado

- Selector con misiones 1–7; la 8 sigue pendiente. Documentación: [misión 4](../mision4/README.md) y [misión 6](../mision6/README.md).
- `VistaCampana` adapta ambos capítulos al mismo reloj del anfitrión y la misma ventana de 1280 × 720. Los búferes Processing pertenecen al PApplet principal; no hay otro ejecutable ni ventana propia.
- Pixelify Sans y `TemaPixel.marco` compartidos en diálogos, elecciones y pausa. Juan aparece como candidato, conservando la elección final para misión 8.
- Teclado, ratón y mando para J1; la misión 6 conserva dos personas en teclado. Las salas TCP de misiones 5 y 7 se mantienen independientes.
- Pausa modal: reloj detenido, clics consumidos, entradas liberadas; repetición de tecla no alterna pausa; al entrar/reanudar se exige soltar confirmación y centrar mando. Desconexión y pérdida de foco pausan.
- Guardados individuales en el directorio del perfil existente, con redirección de pruebas a temporales. Se conservan los formatos de checkpoints recibidos.
- Resultado de misión 4 persistido con identidad de partida y crédito idempotente; las claves electorales, corrupción y salas anteriores se conservan. Misión 6 registra resultado sin inventar cambios de indicadores.

## Verificación

**184 pruebas JUnit aprobadas, cero fallos/errores/omitidas y BUILD SUCCESS.** La compilación completa y sus pruebas están en [compilacion.log](compilacion.log). Incluyen los recorridos completos de la misión 4 (conducción, colisiones, preguntas y tres combates), la misión 6 en solo/duo, las pruebas recibidas de audio/recursos, nuevas regresiones de pausa, guardado, mando simulado, botones retenidos y crédito persistido. También se ejecutan las regresiones anteriores: TCP de misiones 5 y 7, 2.970 patrones aislados, 24 recorridos de batalla y nueve recorridos de carrera.

Las doce ejecuciones gráficas del JAR comprueban entrada desde el selector, render en el lienzo principal, captura y retorno al selector. Resultados en [resultados.json](resultados.json); capturas y logs en esta carpeta: conducción/plaza/combate/pausa/rumor/cierre de misión 4; discurso/combate/entrevista/pausa/modalidad/cierre de misión 6. Son escenas controladas, no partidas humanas completas. Los recorridos completos se validan por simulación en las pruebas JUnit.

Reproducir compilación desde la raíz:

```bash
bash herramientas.sh paquete
```

Revisión gráfica opcional (abre ventanas; aísla guardados y silencia sonido):

```bash
python3 tools/verificar_misiones46.py
```

La revisión usa propiedades explícitas `ad.mision46.revision`, `ad.mision46.numero` y `ad.mision46.fase`; no se activan al jugar normalmente. El audio se valida con eventos simulados y decodificación WAV, sin una comprobación auditiva manual. No se probó mando físico ni una partida humana completa.

## Preservación y procedencia

Respaldo anterior: `/home/sebas/Documentos/LAB1/Respaldos y ProyectsAnterirores/antes-integrar-4-6-20261112-151159`. Una comparación de bytes confirmó que las fuentes de misiones 5 y 7, batalla/protocolo compartidos y recursos anteriores de misiones 1/2/3/5/7 se conservaron. En `RepositorioCarrera` únicamente se añadió el crédito de misión 4 usando la transacción existente, conservando los datos electorales.

El ZIP fuente y la carpeta original `alcalde-digital-lab1-entrega1` permanecen intactos. Los documentos del aporte se conservan como registros de procedencia en [origen](origen/LEEME.md); sus cifras y estados describen la entrega recibida. El arte y audio se reutilizan desde ese aporte; en esta integración no se generaron imágenes nuevas. No se hicieron commits ni push.

Los capítulos recibidos conservan su lógica local; no se incorporó red TCP ni nuevas operaciones de árboles/grafos a estos dos capítulos. Las mecánicas y estructuras existentes de las otras misiones siguen disponibles.
