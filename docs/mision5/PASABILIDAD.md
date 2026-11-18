# Comprobación de que los patrones se pueden superar

Se reproducen controles sobre `MotorBatalla`, con su movimiento, gravedad, plataformas, generación y colisiones reales. El verificador vive exclusivamente en las pruebas: no introduce un jugador automático en el juego.

- Quince patrones, tres dificultades y modos individual/cooperativo.
- Semillas 0 a 31 y 2026, con distintos ticks iniciales para variar el azar.
- Un corazón comienza en (500,550); el segundo, cuando participa, en (770,550), como en la entrada de cada patrón.
- Cinco vidas, inmunidad inicial cero y sin rescates ni acciones de curación. Un solo impacto invalida la ruta: se detecta también la invulnerabilidad de un golpe al terminar una ronda, aunque la recuperación automática de Fácil/Normal restituya ese corazón.
- Las órdenes de desplazamiento son de teclado (-1, 0 o 1); los saltos se pulsan y se sueltan en la simulación normal.
- 198 casos por patrón; **2.970 casos** en total. A esto se añaden **24 recorridos de las cinco rondas**, usando la acción de verificación entre rondas y conservando las cinco vidas.

## Rutas de referencia

| Patrones | Movimiento que abre una salida |
| --- | --- |
| Paquetes cruzados, quietud cian y ráfaga alterna | Bajar al margen inferior y detenerse; los paquetes quedan por encima. |
| Ventana cambiante y pasillo intermitente | Ajustar la altura al hueco de la columna que se aproxima. Incluso en Difícil quedan 72 px de abertura frente a los 14 px de colisión del corazón. |
| Suelo corrupto y columnas | Saltar al aproximarse el obstáculo bajo y soltar tras superarlo. El ensayo cubre el tiempo necesario para que pase todo su ancho. |
| Plataformas y escalera | Alcanzar una plataforma, seguir su desplazamiento y saltar durante los pulsos del suelo. La plataforma admite ambos corazones. |
| Pulso dirigido | Alejarse de la altura fijada durante el aviso; conservar la posición entre haces para no atraerlos a un punto intermedio. |
| Cruce de servidores | Colocarse en un hueco entre las columnas verticales de paquetes. |
| Gravedad lateral | Esperar la aproximación y saltar alejándose de la pared derecha, con tiempo para que pase la columna completa. |
| Antenas gemelas | Salir de la línea fijada y esquivar lateralmente la lluvia de paquetes. Ir siempre a una esquina no es una ruta válida para todas las semillas. |
| Anillo de señal | Alcanzar la esquina inferior correspondiente antes de que cierre el anillo; hay espacio común sin colisión entre jugadores. |
| Tormenta del cortafuegos | Buscar el margen derecho y alternar alturas por debajo de las franjas cian, saliendo de los haces anunciados. |

Las pruebas demuestran rutas sin daño en los casos descritos. No demuestran que cualquier movimiento, posición previa o reacción tardía tenga salida; la dificultad exige anticipación. El azar se mantiene dentro de geometrías con huecos y tiempos de movimiento revisados. No se usa la prueba anterior de recorrido con inmunidad como evidencia de pasabilidad: esa prueba solo comprueba el controlador de fases.

Código: `src/test/java/alcaldedigital/batalla/PatronesPasablesTest.java`. La suite también verifica Normal respecto a la cadencia original, persistencia de dificultad, guardados anteriores, selección por teclado/ratón y transmisión del nivel por TCP.

Resultado comprobado el 12 de noviembre de 2026: 2.970 casos aislados y 24 recorridos completos aprobados; 115 pruebas JUnit totales, cero fallos y errores. Paquete generado con BUILD SUCCESS. [Selector de dificultad revisado](vista-dificultades.png).
