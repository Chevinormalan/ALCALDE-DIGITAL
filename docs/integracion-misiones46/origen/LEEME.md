# Alcalde Digital — entrega completa con Misión 6

Extrae todo el ZIP en una carpeta y abre Jugar.bat. Requiere Java 21 o superior. El selector contiene las misiones 1 a 6; la nueva misión se llama Cielos de campaña.

La Misión 6 comienza con el discurso de Juan. Controla al Influencer, acércate a Juan y pulsa Enter. Camina con él hasta el helicóptero; cuando ambos estén cerca, pulsa Enter para abordar. Allí eliges uno o dos jugadores. Después de la práctica se combate contra el dron viral de la referencia, con tres fases, misil desde la fase 2 e impulso con invulnerabilidad breve. Al derrotar al dron, este se autodestruye y desaparece mientras Mr. Palomo transmite una amenaza disfrazada de noticia; avanza sus mensajes con Enter. Después de aterrizar controlas al Influencer libremente por la sala. Acércate al sofá y pulsa Enter: ambos personajes se sientan para una entrevista de tres preguntas con dos rutas de cierre válidas.

## Controles

- Plaza y sala: WASD o flechas para caminar; Enter para hablar, subir o sentarse junto al sofá.
- Diálogos: Enter completa la frase; otra pulsación continúa.
- Solo: WASD o flechas; J o Z dispara; K o X lanza misil; Shift impulsa.
- Parejas, J1: WASD, F dispara, G misil, Shift impulsa.
- Parejas, J2: flechas, L dispara, K o M misil, / o 0 del teclado numérico impulsa.
- Selecciones y entrevista: flechas y Enter, o teclas 1 y 2.
- Esc o P abre la pausa: Continuar, Ver controles y Salir al selector. También se puede usar el ratón en este menú.
- Durante el combate: toca los corazones rosas para recuperar un punto de vida (máximo cinco). En parejas, cura a la nave que lo recoge; una nave con la vida completa no lo consume.
- Si pierdes: R o Enter reintenta desde la práctica sin repetir la campaña.

## Autoguardado

La Misión 6 guarda cada dos segundos y al cambiar de escena, pausar o salir. Al volver ofrece Continuar o Nueva partida. Conserva posiciones en plaza y sala, corazones, diálogos, proyectiles, integridad, recargas y decisiones; no conserva teclas retenidas. El archivo está en %LOCALAPPDATA%\AlcaldeDigital\mision6.properties y se mantiene una copia anterior .bak. La nueva versión puede recuperar los guardados de la Misión 6 anterior.

## Contenido

- entrega1-nueva/src: código Java, pruebas y recursos de las seis misiones.
- entrega1-nueva/distribucion: JAR ejecutable, bibliotecas y archivos nativos.
- entrega1-nueva/compilar.ps1: compila con un JDK 21 o superior y ejecuta JUnit sin descargar dependencias.
- entrega1-nueva/verificar-entrega6.ps1: batería gráfica opcional; abre ventanas de prueba y guarda evidencias sin modificar la partida real.
- VERIFICACION_MISION6.md: resultados y límites de las verificaciones de esta entrega.
- ARTE_MISION6.md: referencias y preparación del dron.

La entrega conserva el proyecto integrado completo. Los archivos originales en ED2.DANI no se modificaron.
Última actualización: casa y sala modernas basadas en las nuevas referencias del usuario, helipuerto sin helicóptero dibujado y mensaje que identifica a Juan como candidato. Los detalles del arte están en ESCENARIOS_MODERNOS.md.


Mejora audiovisual vigente: nuevas plaza, carretera y animaciones de Juan en misiones 4 y 6; pose frontal en el sofá y audio reactivo de vehículos, disparos y efectos. La última revisión suaviza los motores del carro y el helicóptero y añade respiración y balanceo al candidato sentado. Consulta AJUSTE_SONIDO_Y_SOFA.md para esta revisión, y MEJORA_AUDIOVISUAL.md y ARTE_AUDIOVISUAL.md para los recursos anteriores.

