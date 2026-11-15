package alcaldedigital.cliente.mision2;

import java.util.ArrayDeque;
import java.util.Arrays;

/** Red en estrella: los ataques reabren enlaces y la defensa vuelve a aislarlos. */
public final class RedCivitas {
    public static final int ESTACIONES=6;
    private final int[] avances=new int[ESTACIONES];
    private final boolean[][] enlaces=new boolean[ESTACIONES+1][ESTACIONES+1];

    public RedCivitas() {
        Arrays.fill(avances,1);
    }

    public int avance(int estacion) {validar(estacion);return avances[estacion];}
    public boolean protegida(int estacion) {return avance(estacion)==1;}
    public boolean enPeligro(int estacion) {return !protegida(estacion);}
    public int protegidas() {return (int)Arrays.stream(avances).filter(v->v==1).count();}
    public int progreso() {return Math.round(protegidas()*100f/ESTACIONES);}
    public boolean completa() {return protegidas()==ESTACIONES;}

    public boolean ponerEnPeligro(int estacion) {
        validar(estacion);
        if(enPeligro(estacion)) return false;
        avances[estacion]=0;
        enlaces[0][estacion+1]=enlaces[estacion+1][0]=true;
        return true;
    }

    public boolean proteger(int estacion) {
        validar(estacion);
        if(protegida(estacion)) return false;
        avances[estacion]=1;
        enlaces[0][estacion+1]=enlaces[estacion+1][0]=false;
        return true;
    }

    /** BFS sobre los enlaces que todavía permiten llegar al ataque desde el núcleo. */
    public int comprometidasAlcanzables() {
        boolean[] visto=new boolean[ESTACIONES+1];
        ArrayDeque<Integer> cola=new ArrayDeque<>();
        visto[0]=true;cola.add(0);int total=0;
        while(!cola.isEmpty()) {
            int nodo=cola.remove();
            for(int siguiente=1;siguiente<=ESTACIONES;siguiente++)
                if(enlaces[nodo][siguiente]&&!visto[siguiente]) {
                    visto[siguiente]=true;cola.add(siguiente);total++;
                }
        }
        return total;
    }

    private static void validar(int estacion) {
        if(estacion<0||estacion>=ESTACIONES) throw new IllegalArgumentException("Estación inválida: "+estacion);
    }
}
