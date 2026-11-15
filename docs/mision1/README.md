# Misión 1 · Sofi y Mr. Palomo

> Campaña vigente: esta misión requiere ganar la anterior (excepto la misión 1). Esc/P/Menú abre la pausa común y G guarda. El acceso, la recuperación y el guardado se coordinan desde el mismo sistema. Consulta [campaña y guardados](../campana/README.md); sus reglas sustituyen los menús particulares descritos en notas históricas inferiores.

Capturas de revisión: [introducción](vista-intro.png), [crisis](vista-crisis.png) y [estructuras](vista-estructuras.png).

## Recorrido implementado

1. El menú abre la lista de ocho misiones y ofrece Fácil (60 s), Normal (30 s) y Difícil (12 s) para la misión 1. Sofi comienza sentada en la banca noroeste. Mr. Palomo llega volando desde uno de cuatro bordes, le presenta Civitas con un desliz sobre «manipular» a la gente y regresa por el mismo borde.
2. El jugador consulta tres publicaciones. Contrastar revela su procedencia; compartir, reportar e ignorar dejan consecuencias. La verdad del evento permanece en la sesión.
3. Comienza una crisis con el plazo de la dificultad elegida. El grafo social dirige la llegada del rumor a los ocho peatones en cuatro oleadas.
4. Sofi se acerca a cada persona y escucha dos o tres veces. Burlarse empeora confianza y convivencia. Cada pareja calmada sale por un camino cardinal.
5. El resultado combina personas calmadas, publicaciones tratadas, puntos e indicadores. La interfaz muestra un desenlace bueno, neutral o malo.

## Código y estructuras

- `SesionMisionUnoLocal` valida acciones, proximidad, plazo y consecuencias; el cliente recibe una vista filtrada mediante `SesionMisionUno`.
- `GrafoSocialMisionUno` usa BFS sobre relaciones dirigidas para fijar las oleadas de difusión. La vista de estructuras dibuja las aristas del mismo grafo.
- `ArbolAVLPublicaciones` cambia el riesgo de publicaciones contrastadas o compartidas y retira una que se reporta con pruebas.
- `ArbolDecisiones` registra rutas separadas para las tres publicaciones.
- El parque ocupa un mundo lógico de 640 × 480, dibujado a escala 2. Los sprites se leen de atlas con pivotes de pies; la fuente tiene cuatro cuadros de agua. Los transeúntes generan cuatro pasos por dirección desde una única vista para mantener su identidad entre cuadros.

## Alcance actual

La misión funciona como sesión local de un jugador. Todavía faltan la sala de 2 a 4 jugadores, transporte cliente-servidor real, el grafo territorial entre escenarios y la misión 8. Los transeúntes usan cuatro pasos por dirección compuestos a partir de sus vistas consistentes; el parpadeo y el viento individuales en reposo quedan pendientes de una solución que no cambie su identidad entre fotogramas. Las acciones ambientales se representan mediante basura y agua alterada; se pueden ampliar con objetos persistentes y rutas más complejas.

## Validación

`bash herramientas.sh test` comprueba el caso anterior, la nueva sesión, confidencialidad de fuentes, idempotencia, propagación social, salida de parejas, plazo y desenlace favorable. `bash herramientas.sh paquete` prepara `distribucion/alcalde-digital.jar`.
