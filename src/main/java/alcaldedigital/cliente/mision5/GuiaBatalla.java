package alcaldedigital.cliente.mision5;

import alcaldedigital.servidor.batalla.MotorBatalla;
import java.util.List;

/** Ayuda consultable; describe las reglas de los ataques de esta misión. */
public final class GuiaBatalla {
    private GuiaBatalla(){}
    public record Patron(String nombre,String situacion,String respuesta){}
    private static Patron patron(int i,String situacion,String respuesta){return new Patron(MotorBatalla.PATRONES[i],situacion,respuesta);}
    public static final List<Patron> PATRONES=List.of(
        patron(0,"Paquetes rosados cruzan la arena desde la izquierda y la derecha.","Busca un espacio libre y evita cruzar su trayectoria. No necesitas saltar."),
        patron(1,"Se mezclan paquetes rosados con franjas cian que se anuncian antes de activarse.","Esquiva el rosado y detente cuando llegue el cian: solo daña si te estás moviendo."),
        patron(2,"Columnas llegan desde la derecha dejando una ventana entre el suelo y el techo.","Alinea el corazón con el hueco antes de que llegue cada columna."),
        patron(3,"La gravedad tira hacia el suelo y llegan bloques bajos desde la derecha.","Salta antes del bloque. Mantén salto para subir más o suéltalo para un salto corto."),
        patron(4,"Hay dos plataformas móviles. Una franja rosada amenaza el suelo tras su aviso.","Sube a una plataforma o salta sobre la franja. Planea dónde vas a aterrizar."),
        patron(5,"Se alternan columnas bajas y obstáculos cerca del techo, con gravedad al suelo.","Salta las columnas bajas y evita elevarte de más bajo las superiores."),
        patron(6,"El emisor fija una línea horizontal en la posición de un corazón y luego dispara.","Sal de la línea durante el aviso. Su objetivo queda fijado y no sigue tu movimiento."),
        patron(7,"Caen filas de paquetes con espacios libres entre sus columnas.","Muévete a un hueco de la fila antes de que alcance tu altura."),
        patron(8,"Otra secuencia de paredes entra desde la derecha con ventanas a distintas alturas.","Mira la siguiente ventana y ajusta la altura del corazón con anticipación."),
        patron(9,"La pared derecha actúa como suelo y los bloques caen junto a esa pared.","Al apoyarte en la derecha, pulsa salto para alejarte a la izquierda y esquivar."),
        patron(10,"La gravedad vuelve hacia abajo; las plataformas se desplazan y el suelo recibe pulsos.","Usa las plataformas como apoyo y guarda altura para el siguiente pulso."),
        patron(11,"Un pulso cian cubre toda la arena y entre pulsos pasan paquetes rosados.","Quédate inmóvil durante el cian. Aprovecha la pausa entre pulsos para esquivar."),
        patron(12,"Los haces anunciados se combinan con paquetes que caen desde posiciones variables.","Abandona la línea del haz y conserva una salida lateral para esquivar la caída."),
        patron(13,"Anillos de paquetes avanzan hacia el centro dejando una abertura que va girando.","Observa la abertura; evita quedarte encerrado en el centro cuando el anillo se cierre."),
        patron(14,"El cortafuegos mezcla haces dirigidos, lluvia de paquetes y franjas cian.","Esquiva los avisos y la lluvia; si te alcanza el cian, detén el movimiento real.")
    );
}
