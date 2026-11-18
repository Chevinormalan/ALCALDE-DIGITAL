# Ajuste de motores y conversación

Entrega: **alcalde_digital_6_sonido_suave.zip**, 12 de noviembre de 2026. Extrae el ZIP completo y abre **Jugar.bat**. Requiere Java 21 o superior. La entrega audiovisual anterior se conserva en el Escritorio.

## Cambios de esta revisión

Se reemplazaron los pulsos bruscos, el ruido agudo y la saturación sintética de los motores por armónicos redondeados y aire filtrado. El carro conserva ralentí, marcha, aceleración y frenado; el helicóptero conserva arranque, vuelo estacionario, desplazamiento y descenso. Las mezclas tienen niveles más moderados y transiciones graduales, y siguen respondiendo a los estados reales del vehículo. El frenado ahora tiene una fricción breve y discreta, sin chirrido agudo. Los bucles continúan sin reiniciarse por fotograma.

En la conversación del sofá, Juan mantiene su pose frontal, las manos sobre las rodillas y su posición en el asiento. Ahora respira y se balancea levemente desde la cadera, con un gesto algo más visible cuando responde. El movimiento solo afecta al dibujo; la mesa permanece delante de sus piernas.

Se comprobó que el combate de Juan y el recorrido del carro usan el mismo fondo morado **camino_presentacion.png**, integrado a partir de **Ejemplificacion_trayecto_carro**. No hizo falta sustituirlo nuevamente. Las imágenes de la casa, la sala, los personajes y sus proporciones se conservan.

## Archivos de implementación

Rutas relativas a entrega1-nueva/:

- `tools/GenerarAudioMisiones.java`: síntesis de motores, arranque y frenado más suaves, sin dependencias nuevas.
- `src/main/java/alcaldedigital/cliente/mision4/SoundEffects.java`: balance entre ralentí, marcha y aceleración; menor volumen de frenado.
- `src/main/java/alcaldedigital/cliente/mision6/AudioMisionSeis.java`: balance y volúmenes de hélices, descenso y arranque.
- `src/main/java/alcaldedigital/cliente/mision6/MisionSeisSketch.java`: respiración y balanceo del candidato sentado.
- `src/main/resources/cliente/presentacion/audio/`: ocho WAV actualizados — carro_ralenti, carro_marcha, carro_acelerar, freno, helicoptero_hover, helicoptero_vuelo, helicoptero_descenso y encendido_helicoptero; niveles actualizados en ORIGEN_Y_NIVELES.txt.

Los otros once WAV de presentación permanecen idénticos a la entrega anterior, incluidos disparos, cohetes, impactos, explosiones y ambiente. Tampoco se modificaron los sonidos reutilizados de otras misiones.

Como apoyo se añadió `tools/VerificarConfortAudio.java`, `tools/EmpaquetarConfort.ps1` y un filtro opcional de casos a `verificar-audiovisual.ps1`. La copia previa de los cuatro archivos de implementación y las huellas de los WAV están en `verificacion/ajuste-confort-20261112-032848/`.

## Verificación de esta revisión

- Compilación completa y **124 pruebas JUnit aprobadas**, cero fallos.
- Comparación exacta con la base: `CombatSystem.java`, `CollisionWorld.java`, `MisionSeisMotor.java` y toda la simulación de `MisionCuatroGame.java` anterior al render permanecen idénticos. No se cambiaron controles, colisiones, daño, objetivos, tiempos ni condiciones de victoria o derrota.
- Recorrido original de misión 4 aprobado, incluidas colisiones, combate, recompensas y guardado aislado.
- Cuatro ejecuciones automáticas en ventana real aprobadas: conducción/combate M4, recorrido completo individual M6, conversación del sofá y aterrizaje. Se revisaron las capturas del sofá y del combate morado.
- Comparación PCM contra el ZIP anterior: los ocho sonidos modificados tienen picos menores y variaciones entre muestras considerablemente más suaves; no hay muestras saturadas. Se comprobaron las uniones de los seis bucles. Los otros once efectos son idénticos byte por byte.
- JavaSound abrió e inició la reproducción de los **19 WAV** en el dispositivo del equipo, sin errores de audio.

Evidencias en `entrega1-nueva/verificacion/audiovisual/`: `compilacion-confort.log`, `preservacion.log`, `m4-recorrido-original.log`, `resultados-confort.json`, `confort-pcm.json`, `salida-equipo-confort.log` y `auditoria-confort.json`. Las capturas y sesiones de esta revisión están en `grafica/`; los archivos `*.confort.log` aíslan la última sesión y los registros originales conservan las anteriores.

La medición PCM y la apertura de JavaSound no sustituyen una escucha humana en tus altavoces. No se afirma haber evaluado físicamente su comodidad auditiva. Los sonidos son originales y sintetizados para el estilo del juego; no son grabaciones de vehículos reales.

SHA-256 del JAR compilado y verificado: `737C6F920BE67AD6191261210BFD2F5CCB2C5580E75C68941E4A467072588756`.
