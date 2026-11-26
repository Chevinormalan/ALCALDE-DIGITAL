# Comprobación de refugios y rutas de esquiva

Las pruebas de `PatronesPasablesTest` reproducen entradas de teclado en el motor de batalla. Para esquivar, leen los obstáculos y avisos disponibles en cada paso. El verificador pertenece a la suite de pruebas.

- **Refugios anteriores:** 495 ensayos con las cinco rutas pasivas encontradas en Paquetes cruzados, Tráfico cian, Cruce de servidores, Ráfaga alterna y Anillo de señal. Ambos corazones salen de sus posiciones habituales, caminan hasta los refugios y esperan. La prueba exige que ambos reciban un impacto al intentar repetir la táctica.
- **Patrones individuales:** 2.970 recorridos sin perder vida. Cubren quince patrones, tres dificultades, modos individual y cooperativo y 33 semillas. Empiezan con cinco corazones y sin inmunidad inicial.
- **Campaña de la misión:** 24 recorridos completos de las cinco rondas. Incluyen el cierre de cada turno y la reducción de velocidad de las paredes al verificar publicaciones.
- **Decisión de perdonar:** 198 recorridos del último ataque, con siete segundos de combate en los dos modos y las tres dificultades.
- **Variedad y restauración:** se comprueba que cada patrón tenga distintas secuencias y que el mismo estado restaurado reproduzca las siguientes oleadas. La semilla y el tiempo de inicio del patrón son suficientes para recuperar la variante.
- **Rastreador de Antenas gemelas:** 198 ensayos verifican que alcance al jugador que se queda quieto y que su oleada esté separada de las dos anteriores. Se comprueba también el giro en persecución y la conservación de la ruta fijada al guardar y entre dos clientes TCP locales.
- **Cuadritos de Tormenta del cortafuegos:** 198 ensayos comprueban las dos tandas con entradas variadas y diagonales, los dos rastreadores exclusivos de Difícil y la separación del pulso cian. El ataque posterior a «Perdonar» conserva sus propios obstáculos.

Un impacto invalida la ruta, incluso si la recuperación automática del final de ronda repone ese corazón. Las pruebas verifican las rutas y semillas indicadas. En cooperativo los dos corazones pueden compartir el mismo hueco.

Código: [PatronesPasablesTest.java](../../src/test/java/alcaldedigital/batalla/PatronesPasablesTest.java).
