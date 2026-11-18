package alcaldedigital.compartido.estructuras.grafo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** La dirección emisor → receptor fija el orden de llegada del rumor en Civitas. */
public final class GrafoDifusionCivitas {
    private static final int[][] CONTACTOS={{1,2},{3},{4},{5},{6},{},{}};

    public List<Integer> ordenDesde(int origen) {
        if(origen<0||origen>=CONTACTOS.length) throw new IllegalArgumentException("Cuenta desconocida");
        boolean[] visitadas=new boolean[CONTACTOS.length];
        ArrayDeque<Integer> cola=new ArrayDeque<>();
        ArrayList<Integer> orden=new ArrayList<>();
        cola.add(origen);visitadas[origen]=true;
        while(!cola.isEmpty()) {
            int actual=cola.removeFirst();orden.add(actual);
            for(int receptor:CONTACTOS[actual]) if(!visitadas[receptor]) {
                visitadas[receptor]=true;cola.addLast(receptor);
            }
        }
        return List.copyOf(orden);
    }
}
