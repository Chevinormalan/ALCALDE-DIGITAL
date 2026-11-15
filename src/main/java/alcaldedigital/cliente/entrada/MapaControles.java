package alcaldedigital.cliente.entrada;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Locale;

/** Acciones visibles y traducción de los dispositivos para la campaña. */
public final class MapaControles {
    public enum Accion { PRINCIPAL, SECUNDARIA, MOVILIDAD }
    public record Contexto(int mision, boolean dosJugadores, String jugador1, String jugador2,
                           String nombreMando) {
        public Contexto {
            if(mision<1||mision>7)throw new IllegalArgumentException("Misión inválida");
            if(jugador1==null)jugador1="Jugador 1";
            if(jugador2==null)jugador2="Jugador 2";
            if(nombreMando==null)nombreMando="";
        }
        public boolean playStation(){return nombreMando.toLowerCase(Locale.ROOT)
            .matches(".*(dualshock|dualsense|wireless controller|sony|playstation).*");}
    }
    public record Tecla(String tecla, String accion) {}
    public record Boton(String boton, String accion) {}
    private MapaControles(){}

    public static Accion accionTeclado(int mision, boolean dos, int codigo) {
        if(mision!=4&&mision!=6)return null;
        return switch(codigo) {
            case KeyEvent.VK_F -> Accion.PRINCIPAL;
            case KeyEvent.VK_Q -> Accion.SECUNDARIA;
            case KeyEvent.VK_SPACE -> Accion.MOVILIDAD;
            default -> null;
        };
    }
    public static Accion accionMando(EstadoMando.Boton boton) {
        return switch(boton) {
            case X, RT -> Accion.PRINCIPAL;
            case Y, LT -> Accion.SECUNDARIA;
            case RB -> Accion.MOVILIDAD;
            default -> null;
        };
    }
    /** Código interno del capítulo, independiente del dispositivo físico. */
    public static int codigoInterno(int mision, boolean dos, Accion accion) {
        return switch(mision) {
            case 4,6 -> switch(accion) {
                case PRINCIPAL -> KeyEvent.VK_F;
                case SECUNDARIA -> KeyEvent.VK_Q;
                case MOVILIDAD -> KeyEvent.VK_SPACE;
            };
            default -> throw new IllegalArgumentException("Acciones de combate no disponibles");
        };
    }
    public static String nombreMision(int n) {return switch(n){
        case 1->"El parque de Sofi";case 2->"La red Civitas";case 3->"Civitas en Rojo";
        case 4->"Juan en campaña";case 5->"Dentro del servidor";case 6->"Cielos de campaña";
        case 7->"La carrera por la primicia";default->"Misión";};}
    public static String movilidad(int n){return switch(n){
        case 2,5->"Saltar";case 4->"Esquivar";case 6->"Impulso";case 7->"Correr";
        default->"Moverse";};}
    public static List<Tecla> teclado1(Contexto c){return switch(c.mision()){
        case 1->List.of(new Tecla("W A S D","Moverse"),new Tecla("E","Hablar"),new Tecla("C","Civitas"),new Tecla("T","Consultas"));
        case 2->List.of(new Tecla("W A S D","Moverse"),new Tecla("E","Reparar"),new Tecla("ESPACIO","Saltar"),new Tecla("MAYÚS","Correr"));
        case 3->List.of(new Tecla("W A S D","Moverse"),new Tecla("E","Hablar"),new Tecla("F","PING"),new Tecla("Q","FLASH"),new Tecla("ESPACIO","DASH"),new Tecla("CTRL","Modo privado"));
        case 4->List.of(new Tecla("W A S D","Moverse"),new Tecla("E","Hablar"),new Tecla("ESPACIO","Esquivar"),new Tecla("F","Atacar"),new Tecla("Q","Escudo"),new Tecla("V / N","Investigar / responder"));
        case 5->List.of(new Tecla("W A S D","Moverse"),new Tecla("E","Confirmar"),new Tecla("ESPACIO","Saltar"),new Tecla("1 2 3","Responder"));
        case 6->List.of(new Tecla("W A S D","Moverse"),new Tecla("E","Confirmar"),new Tecla("ESPACIO","Impulso"),new Tecla("F","Disparar"),new Tecla("Q","Misil"));
        default->List.of(new Tecla("W / S","Cambiar carril"),new Tecla("E","Confirmar / ayuda"),new Tecla("ESPACIO","Correr"),new Tecla("Q","Usar ayuda"),new Tecla("1 2 3","Responder"));
    };}
    public static List<Tecla> teclado2(Contexto c){if(!c.dosJugadores())return List.of();return switch(c.mision()){
        case 5->List.of(new Tecla("FLECHAS","Moverse"),new Tecla("ENTER","Confirmar"),new Tecla("MAYÚS","Saltar"),new Tecla("7 8 9","Responder"));
        case 6->List.of(new Tecla("FLECHAS","Moverse"),new Tecla("ENTER","Confirmar"),new Tecla("MAYÚS","Impulso"),new Tecla("L","Disparar"),new Tecla("K","Misil"));
        case 7->List.of(new Tecla("FLECHAS","Cambiar carril"),new Tecla("ENTER","Confirmar"),new Tecla("MAYÚS","Correr"),new Tecla("K","Usar ayuda"),new Tecla("7 8 9","Responder"));
        default->List.of();};}
    public static List<Boton> mando(Contexto c){return switch(c.mision()){
        case 1->List.of(new Boton("STICK / CRUCETA","Moverse"),new Boton("A","Hablar"),new Boton("X","Civitas"),new Boton("Y","Consultas"));
        case 2->List.of(new Boton("STICK / CRUCETA","Moverse"),new Boton("A","Reparar"),new Boton("X","Saltar"),new Boton("RB","Correr"));
        case 3->List.of(new Boton("STICK / CRUCETA","Moverse"),new Boton("A","Hablar"),new Boton("X / RT","PING"),new Boton("Y","LIVE BURST"),new Boton("LT","FLASH"),new Boton("RB","DASH"),new Boton("LB","Modo privado"));
        case 4->List.of(new Boton("STICK / CRUCETA","Moverse"),new Boton("A","Confirmar"),new Boton("X / RT","Atacar"),new Boton("Y / LT","Escudo"),new Boton("RB","Esquivar"));
        case 5->List.of(new Boton("STICK / CRUCETA","Moverse"),new Boton("A","Confirmar"),new Boton("X","Saltar"));
        case 6->List.of(new Boton("STICK / CRUCETA","Moverse"),new Boton("A","Confirmar"),new Boton("X / RT","Disparar"),new Boton("Y / LT","Misil"),new Boton("RB","Impulso"));
        default->List.of(new Boton("STICK / CRUCETA","Cambiar carril"),new Boton("A","Confirmar"),new Boton("X","Correr"),new Boton("Y","Usar ayuda"),new Boton("LB / RB","Elegir respuesta"));
    };}
    public static String etiquetaBoton(Contexto c,String boton){if(!c.playStation())return boton;
        String[] partes=boton.split(" / ");
        for(int i=0;i<partes.length;i++)partes[i]=switch(partes[i]){
            case "A"->"Cruz";case "B"->"Círculo";case "X"->"Cuadrado";case "Y"->"Triángulo";
            case "RB"->"R1";case "LB"->"L1";case "RT"->"R2";case "LT"->"L2";
            default->partes[i];};
        return String.join(" / ",partes);
    }
}
