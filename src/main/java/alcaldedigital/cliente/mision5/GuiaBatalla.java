package alcaldedigital.cliente.mision5;

import alcaldedigital.servidor.batalla.MotorBatalla;
import java.util.List;

/** Ayuda consultable; describe las reglas de los ataques de esta misión. */
public final class GuiaBatalla {
    private GuiaBatalla(){}
    public record Patron(String nombre,String situacion,String respuesta){}
    private static Patron patron(int i,String situacion,String respuesta){return new Patron(MotorBatalla.PATRONES[i],situacion,respuesta);}
    public static final List<Patron> PATRONES=List.of(
        patron(0,"Se cargan carriles de paquetes a distintas alturas. Los carriles libres alternan.","Pasa a un carril sin marcas ámbar antes de la descarga."),
        patron(1,"Las descargas rosadas cambian de carril y un pulso cian cruza toda la arena entre ambas.","Cambia de carril durante los avisos ámbar. Cuando se active el cian, quédate inmóvil."),
        patron(2,"Una pared entra por uno de los costados y deja una ventana a una altura distinta.","La guía ámbar marca las zonas macizas. Alinea el corazón con la ventana y espera a que pase."),
        patron(3,"La gravedad tira hacia el suelo y el servidor carga descargas bajas.","Salta cerca del final de la carga. Mantén salto para pasar por encima de toda la descarga."),
        patron(4,"Dos plataformas ofrecen apoyo. Primero se carga el suelo y luego se cierran columnas.","Salta o sube a una plataforma durante el primer pulso. Cambia de columna cuando veas las siguientes marcas."),
        patron(5,"Dos descargas del suelo rodean un pulso que cae desde el techo sobre parte de la arena.","Salta sobre las descargas bajas y aprovecha el suelo cuando esté marcado el techo."),
        patron(6,"El emisor fija un haz horizontal a la altura de un corazón. Con dos jugadores alterna el objetivo.","Sal de la línea durante el aviso. El haz permanece en la posición marcada."),
        patron(7,"La lluvia llena columnas enteras. Entre oleadas cambian las columnas que quedan libres.","Busca un hueco en cada aviso, aunque estés en el mismo carril que te protegió antes."),
        patron(8,"Dos descargas cierran distintos carriles horizontales. La última cierra columnas.","Sigue los huecos de cada aviso; para el último ataque, muévete de lado."),
        patron(9,"La pared derecha actúa como suelo. Entre dos pulsos de pared se cierran carriles horizontales.","Salta hacia la izquierda cuando cargue la pared y cambia de altura cuando se marquen carriles."),
        patron(10,"Las plataformas se desplazan mientras el suelo y los sectores verticales se activan sucesivamente.","Elige dónde aterrizar mirando las columnas. Una plataforma también puede quedar dentro de una descarga."),
        patron(11,"Los carriles rosados cambian de orientación entre partidas. Un pulso cian separa las dos descargas.","Sal de la zona ámbar y detente cuando se active el cian."),
        patron(12,"Primero llega un haz, luego llueven paquetes y al final un cuadrito persigue al jugador marcado.","Busca una columna libre. Cuando el cuadrito marque su ruta, esquiva hacia un lado."),
        patron(13,"Se alterna un anillo alrededor de la arena con una descarga en el centro.","Entra al interior cuando se marque el borde. Sal hacia un lateral cuando se cargue el centro."),
        patron(14,"Tras el haz llegan dos tandas de cuadritos y un pulso cian. En Difícil aparecen también dos rastreadores, uno a la vez.","Esquiva las rutas punteadas de los cuadritos. Quédate quieto durante el pulso cian.")
    );
}
