# Objetos del parque

`objetos_maestro.png` fue generado con la herramienta integrada image_gen el 17/09/2026 para este proyecto. No se usó CLI ni API externa.

Prompt: production sprite atlas for Alcalde Digital, transparent alpha, eight isolated objects: green deciduous tree, conifer, pink tree, flowering bush, bench, public notice board, dark teal lamp, stone fountain with dove. Overhead pixel art, ink #18283D, cream #F7E7C6, teal #287F82, green #478F62, gold #E5AA45, coral #E97866. No text, grid, characters or ground; consistent upper-left lighting.

El resultado no respetó una cuadrícula perfectamente uniforme. `PrepararArte.java` conserva los rectángulos de extracción reales y exporta ocho objetos independientes con transparencia. Los maestros previos de LAB1 aportan los cuatro personajes, Noti, acciones, efectos, iconos, materiales y portada.

El exportador realiza ajustes mecánicos de escala, límites alpha y pivote; no sobrescribe los maestros. Las caminatas usan celdas 32×32, pies centrados abajo, 4 cuadros por dirección y 8 fps. La interfaz se dibuja con texto real. Los objetos tienen cuerpos de colisión independientes de su imagen.
