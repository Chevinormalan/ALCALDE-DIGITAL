# Puntajes y antecedentes de las misiones 1 a 7

La misma tabla ocupa la franja superior del cierre de cada capítulo, con columnas
para indicadores y decisiones, cuatro filas y un marco morado y dorado. Los
paneles propios de cada misión y los controles quedan visibles debajo. Muestra
un puntaje de 0 a 100 y el resultado del intento. El puntaje describe
el intento: **solo la condición de victoria de cada motor desbloquea la misión
siguiente**. Una derrota conserva su fila y permite reintentar. El botón manual
del selector abre capítulos para revisión, pero no registra victorias.

[Vista del cierre de la misión 1 con tabla y paneles visibles](evidencias/tabla-mision1.png).

| Misión | Victoria | Base del puntaje |
| --- | --- | --- |
| 1 | Desenlace BUENO | Hasta 40 por vecinos calmados, 40 por publicaciones responsables y 20 por puntos. |
| 2 | Seis equipos seguros y sin fallo crítico | Hasta 75 por equipos seguros y 25 por cobertura media durante la defensa. El fallo crítico inevitable recibe 0 y no acredita ciudad. |
| 3 | Epílogo completado | 60 por completar, hasta 20 por confianza y hasta 20 por desinformación baja. |
| 4 | Tres combates y epílogo | 40 por completar, 30 por fuentes investigadas, 20 por respuestas favorables y 10 por enemigos. |
| 5 | Fase FINAL con victoria | 50 por victoria, hasta 25 por verificaciones y 25 por vidas restantes. Se muestran los puntos de cada participante. |
| 6 | Dron vencido y entrevista terminada | 70 por completar y hasta 30 por integridad de las naves. Escucha y convicción no son aciertos o errores. |
| 7 | Un corredor humano llega primero | 35 por victoria, hasta 25 por reputación, 20 por noticias contrastadas y 20 por puntos. El pacto no puede compensarse con puntaje. |

La escala es inicial y debe balancearse con recorridos humanos. Dificultad,
puntos originales y datos de cada participante permanecen visibles por separado.
El puntaje no sustituye las reglas de victoria: una derrota con muchos puntos
no abre el capítulo siguiente.

## Preguntas y decisiones

- Misión 1 guarda, por publicación, si se contrastó y cómo se trató; también
  cuenta las burlas. El AVL, árbol de decisiones y grafo social siguen
  determinando sus consecuencias durante el juego.
- Misión 4 guarda las cuatro respuestas. Según sus reglas actuales, las
  preguntas 1, 2 y 4 valoran «No» como favorable y «Sí» como desfavorable;
  la 3 es neutral. Sus diálogos de «Sí» se ajustaron para que la opción y la
  consecuencia expresen la misma decisión. Las respuestas favorables y
  desfavorables afectan confianza, convivencia, desinformación y conflictos.
- Misión 5 conserva las elecciones por ronda en el historial autoritativo;
  verificar afecta el AVL y perdonar exige sobrevivir al último ataque.
- Misión 6 guarda cada una de las tres opciones de entrevista. Las dos rutas
  son válidas y no modifican la corrupción de Juan.
- Misión 7 guarda las elecciones de noticias y el historial electoral. El
  grafo propaga sus efectos; aceptar el pacto mantiene la corrupción irreversible.

Las misiones 2 y 3 no reciben preguntas artificiales para aumentar su
puntaje. En ellas se muestran medidas de defensa y recuperación de la red.

## Señales para tres desenlaces futuros

Además del texto visible, cada fila guarda `senales` numéricas con nombre
estable. Así se pueden evaluar decisiones sin leer frases de la interfaz ni
usar el puntaje total como sustituto de una respuesta concreta.

| Misión | Señales registradas para el análisis |
| --- | --- |
| 1 | `vecinos_calmados`, `publicaciones_responsables`, `burlas` |
| 2 | `equipos_seguros`, `cobertura_media`, `fallo_critico` |
| 3 | `confianza_final`, `desinformacion_final`, `amenazas_vencidas` |
| 4 | `fuentes_validadas`, `respuestas_favorables`, `respuestas_desfavorables`, `respuestas_neutrales` |
| 5 | `publicaciones_verificadas`, `rumores`, `vidas_restantes` |
| 6 | `respuestas_escucha`, `respuestas_conviccion`, `integridad_restante` |
| 7 | `noticias_contrastadas`, `juan_corrupto`, `reputacion_media` |

Propuesta de **clasificación de antecedentes**, pendiente de diseño y pruebas
de balance. Se aplicaría solo tras siete victorias registradas. Las categorías
se comprueban en este orden para que sean excluyentes:

1. **Ciudad en crisis:** confianza menor que 35, desinformación de 60 o más,
   conflictos de 60 o más, o bien dos o más respuestas desfavorables en la
   misión 4 junto con una o ninguna publicación responsable en la misión 1.
2. **Ciudad con confianza cívica:** si no hay crisis, información verificada y
   confianza de 60 o más, desinformación y conflictos de 35 o menos, dos o más
   publicaciones responsables en la misión 1, dos o más respuestas favorables
   y una o ninguna desfavorable en la misión 4, tres o más verificaciones en
   la misión 5 y ningún pacto corrupto de Juan en la misión 7.
3. **Ciudad en reconstrucción:** los demás casos, incluidos los de buen
   puntaje con señales contradictorias. El pacto corrupto impide la categoría
   de confianza cívica y sigue excluyendo a Juan como candidato elegible.

La entrevista de la misión 6 conserva las dos formas de responder como
antecedente narrativo; ninguna se considera negativa. El líder electoral de
la misión 7 es provisional: la clasificación propuesta no declara ganador ni
resuelve una elección. Estos umbrales son una **propuesta de análisis**; no
existen escenas, lógica de finales ni código de la misión 8.

## Archivo e intentos

`tabla-puntajes.properties`, en el directorio de guardados habitual o en
`ad.guardados`, conserva cada intento terminal, el último y el mejor puntaje.
Solo una victoria actualiza `campana` para esa misión. Repetir una victoria
reemplaza su fila vigente; una derrota posterior no la sustituye. El identificador
de partida impide acreditar dos veces el mismo cierre. La escritura es atómica
y usa un cerrojo entre procesos. El historial visible de la misión 5 conserva
hasta sus 128 entradas más recientes; sus recuentos permanecen completos en
las señales numéricas. No se modifican partidas antiguas para
inventar respuestas que no se registraron en su momento.

Cada victoria guarda además su cambio de siete indicadores. La consulta
`TablaPuntajes.ciudadParaFinales()` solo entrega una proyección cuando hay
siete victorias registradas; en una repetición sustituye el cambio de esa
misión, sin sumar dos veces. Esta consulta prepara datos y **no implementa la
misión 8 ni decide sus finales**. Los resultados electorales siguen siendo
provisionales y la corrupción de Juan conserva su regla independiente.

La ciudad que usa actualmente el juego para las escenas sigue en
`campana.properties` y conserva su comportamiento histórico. En perfiles
anteriores a esta tabla no pueden reconstruirse respuestas individuales ni
el cambio aislado de cada misión; por eso la proyección para finales se ofrece
solo cuando las siete victorias están registradas en el formato nuevo.
