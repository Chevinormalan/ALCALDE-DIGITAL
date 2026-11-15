# Paleta morada del juego

Referencia: ilustración violeta aportada para Ciudad Nova. Los colores se definen en
`PaletaMorada.java` y se usan tanto en la interfaz como en el arte pixel al cargarlo.

| Uso | Color |
| --- | --- |
| Fondo profundo | `#21152F` |
| Paneles | `#38264B` |
| Violeta de controles | `#604077` |
| Bordes lila | `#BD91D5` |
| Texto claro | `#F5E8FB` |
| Acentos amarillos, botones principales y selección | `#F4CB6A` |
| Sombra | `#140C1D` |
| Texto secundario | `#D6BFE2` |
| Superficie clara | `#E8D1F2` |
| Acción positiva | `#7751A5` |
| Alerta | `#A8467B` |
| Vegetación del minimapa | `#9279AA` |
| Agua del minimapa | `#7779BF` |

Los PNG originales se conservan como fuentes. El cargador transforma sus colores una
sola vez y guarda el resultado en caché. Mantiene tamaño, transparencia, silueta y
sombras de cada cuadro; en personajes conserva tonos cálidos de piel. Así todos los
cuadros de una animación reciben exactamente la misma transformación.

El amarillo se reserva para botones principales, foco, puntos y pequeños detalles
originalmente dorados de objetos, iconos y Noti. Los caminos y fondos siguen violetas.

El modo de alto contraste conserva texto negro o blanco y sus controles habituales.
