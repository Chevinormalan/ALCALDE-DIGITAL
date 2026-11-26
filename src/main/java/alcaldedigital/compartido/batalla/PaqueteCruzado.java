package alcaldedigital.compartido.batalla;

import alcaldedigital.compartido.batalla.EstadoBatalla.Obstaculo;

/** Cuadrito que conserva la dirección anunciada al entrar en la arena. */
public final class PaqueteCruzado {
    public static final int TIPO=5;
    public static final int DISIPACION=8;

    private PaqueteCruzado(){}

    public static int inicioDisipacion(Obstaculo a){
        return a.duracion()-DISIPACION+1;
    }

    public static int pasosRestantes(Obstaculo a){
        return Math.max(0,inicioDisipacion(a)-Math.max(a.edad()+1,a.aviso()));
    }
}
