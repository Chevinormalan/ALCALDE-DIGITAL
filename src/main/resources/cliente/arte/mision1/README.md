# Recursos de la misión 1

Las referencias originales entregadas por el usuario se conservaron en `tools/arte/referencias-mision1/`. Estos PNG son derivados creados con la herramienta integrada `imagegen` y se cargan sin el recoloreado de la Entrega 1 porque ya usan la paleta morada. Ninguna referencia original se sobrescribió.

| Archivo | Dimensión de atlas | Uso y pivote |
|---|---|---|
| `parque.png` | escena 4:3 | Fondo del mundo 640 × 480 |
| `sofi-caminar.png` | 4 × 4 | Filas: abajo, izquierda, derecha, arriba. Pies al fondo de cada celda. |
| `sofi-reposo.png` | 4 × 1 | Referencia anterior de parpadeo; no se usa en pantalla porque cambia las proporciones de Sofi. Al detenerse se muestra el primer cuadro de caminata de la dirección actual. De frente, el parpadeo se dibuja sobre sus ojos cada 5,2 s. |
| `sofi-sentada.png` | 2 × 1 | Introducción en banca; segundo cuadro con viento y parpadeo. |
| `palomo.png` | 4 × 2 | Cuatro vistas de pie y cuatro poses de vuelo. Entra por un borde y regresa por el mismo. |
| `transeuntes.png` | 4 × 2 | Referencia de las ocho personas quietas. |
| `transeuntes-caminar.png` | 4 × 2 | Referencia de movimiento frontal; no se alterna en pantalla para evitar cambios de identidad. |
| `transeuntes-parpadeo.png` | 4 × 2 | Referencia de reposo; no se alterna en pantalla por el mismo motivo. |
| `transeuntes-direcciones-a.png` | 4 × 4 | Personas 1–4: frente, izquierda, derecha, espalda. Base de los cuatro pasos de cada dirección. |
| `transeuntes-direcciones-b.png` | 4 × 4 | Personas 5–8: frente, izquierda, derecha, espalda. Base de los cuatro pasos de cada dirección. |
| `fuente-agua.png` | 4 × 1 | Cuatro cuadros del agua y los chorros. |

**Conjunto final de instrucciones de generación:** convertir el parque de referencia a pixel art de vista cenital conservando fuente, banca, estanque y cuatro salidas; crear atlas transparentes de Sofi con cuatro direcciones, caminata, reposo y pose sentada; crear atlas transparentes de Mr. Palomo con cuatro vistas y vuelo; convertir los ocho transeúntes conservando identidad, ropa y orden, con reposo, paso frontal, parpadeo y cuatro vistas; aislar la fuente en cuatro cuadros con piedra fija y agua variable. Todos los prompts pidieron píxeles nítidos, fondo alfa para personajes y objetos, paleta morada con detalles amarillos y ausencia de texto.

Los transeúntes usan en ejecución cuatro pasos compuestos desde su única vista por dirección. Esto conserva color, ropa y tamaño entre fotogramas. Los demás atlas animados usan un encuadre compartido por secuencia para mantener su escala y pivote.
