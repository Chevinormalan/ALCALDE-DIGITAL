# Alcalde Digital: Promesas, Palomas y Wi-Fi

Videojuego educativo 2D top-down desarrollado en Java con Processing para el laboratorio de Estructura de Datos II.

## Entrega 1 implementada

Capítulo jugable «El helipuerto de palomas»: explora el parque, lee Civitas, encuentra el aviso y el testimonio, elige una acción y observa sus consecuencias. Puedes seleccionar uno de los cuatro roles. El caso tiene cuatro variantes por semilla.

El AVL propio organiza publicaciones por (riesgo,ID): inserción, búsqueda, eliminación, reinserción por cambio de riesgo, cuatro rotaciones y recorridos. El árbol n-ario inserta opciones al conseguir evidencia y poda alternativas al cerrar el caso. Ambos tienen historial visual de estados, búsqueda y recorridos en la GUI.

La visualización aplica el apartado 28.1 de la documentación integral: T abre una consulta opcional con **Radar de publicaciones** y **Camino de decisiones**, una pestaña a la vez y siempre con el estado actual. **Abrir Laboratorio** muestra claves, alturas, FE, operaciones, recorridos, búsqueda y estados históricos asociados a su evento. T, Esc y Volver regresan al punto de entrada y conservan la selección. También hay acceso al Laboratorio desde menú, ayuda y resultado de una sesión.

Las acciones muestran avisos explicando el cambio y su causa. Se pueden ocultar en Ayuda y siguen disponibles al consultar las estructuras. Las reglas y los algoritmos de la entrega anterior se conservaron: la prioridad de #1 baja a 8 **al reunir ambas fuentes** (30 → 8, o 75 → 8 si antes se compartió), no al comenzar la verificación.

Incluye mapa de tiles con colisiones y cámara, personajes animados, objetos independientes, ayuda, navegación por teclado, contraste alto, movimiento reducido, plazo ampliado, puntuación, siete indicadores y 148 PNG de ejecución. La sesión local mantiene la autoridad sobre reglas y evita duplicar efectos.

La red multijugador, grafos, otros escenarios jugables y finales electorales de siete días corresponden a las entregas siguientes. El cierre actual es el balance del capítulo 1.

## Requisitos previstos

- JDK 21 o compatible.
- Maven 3.9 o posterior.
- Processing Core 4.5.6, descargado por Maven.
- Visual Studio Code con Extension Pack for Java.
- Acceso inicial a Maven Central para descargar Processing Core y JUnit.

Entorno comprobado: JDK 26.0.2 compilando con `--release 21`, Maven 3.9.15 incluido en la extensión Java de VS Code y Processing Core 4.5.6. Maven no está en PATH en esta máquina; `herramientas.sh` detecta su ubicación. Las pruebas de interfaz se ejecutan sin pantalla; el juego necesita un escritorio gráfico.

## Puntos de entrada

- Cliente gráfico: `alcaldedigital.app.ClienteApp`
- Panel gráfico del servidor: `alcaldedigital.app.ServidorApp`

Para jugar el paquete preparado (JDK 21 o posterior):

```bash
bash jugar.sh
```

En Windows: `jugar.bat`. Desde VS Code: ejecutar `ClienteApp`. Para construir, probar y regenerar evidencias:

```bash
bash herramientas.sh test
bash herramientas.sh paquete
bash herramientas.sh evidencias
```

También puedes usar Maven directamente:

```bash
mvn test
mvn exec:java -Dexec.mainClass=alcaldedigital.app.ClienteApp
mvn exec:java -Dexec.mainClass=alcaldedigital.app.ServidorApp
```

`ServidorApp` conserva el panel preliminar para el futuro servidor; no se necesita para el capítulo local. El paquete de ejecución se crea en `distribucion/` y no se versiona. El ZIP de entrega incluye sus dependencias y puede jugarse sin red.

## Controles y demostración

WASD/flechas: movimiento; E/Espacio: interactuar; C: Civitas; I/J: evidencias; T: abrir/cerrar la consulta de árboles; H: ayuda; Esc: volver/menú. Tab, Mayús+Tab y Enter permiten activar los controles. En el Laboratorio AVL, enfoca el campo ID antes de escribir; admite cursor, flechas, Inicio/Fin, Retroceso, Suprimir y Enter. Busca y recorre el **estado mostrado**, incluso si es histórico. «Animar nodos» conserva ese estado; «Anterior/Siguiente» cambian la operación. Con movimiento reducido se avanza manualmente.

Selecciona Verificar para buscar el aviso al noroeste y hablar luego con la administradora. Regresa a Civitas para publicar contexto. El árbol cambia al reunir pruebas y al resolver el caso. La versión entregada incluye 40 pruebas automatizadas sin fallos y una autoprueba independiente de 49 comprobaciones.

Consulta `entregas/entrega1/README.md` para conocer el alcance, la arquitectura, el trabajo realizado por cada integrante y la validación de esta entrega. El catálogo de recursos se encuentra en `src/main/resources/cliente/arte/index.html`.

## Documento de la entrega

El informe formal consolidado se encuentra en [entregas/entrega1/README.md](entregas/entrega1/README.md). Allí presentamos el resultado , la estructura del proyecto, la función de los componentes, el trabajo por integrante, las pruebas realizadas, las instrucciones de ejecución y el alcance pendiente.

## Reglas de trabajo

- La rama principal debe permanecer ejecutable.
- Cada tarea tendrá responsable, revisor y criterio de aceptación.
- Los clientes solicitan acciones; el servidor conserva el estado oficial.
- Las estructuras resuelven mecánicas reales y no se agregan solo para mostrarlas.
- Los recursos maestros permanecen en `../Diseño Visual y Dirección de Arte/`; solo se copiarán a `src/main/resources` los archivos adaptados para ejecución.
- No se suben archivos generados por Maven, configuraciones personales ni secretos.

