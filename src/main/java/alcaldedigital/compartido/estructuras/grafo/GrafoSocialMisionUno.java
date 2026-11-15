package alcaldedigital.compartido.estructuras.grafo;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

/** Relaciones dirigidas de los ocho peatones; BFS determina olas de difusión. */
public final class GrafoSocialMisionUno {
    public record Arista(int origen,int destino) { }
    private static final int[][] SIGUIENTES={
        {2},{3},{4},{5},{6},{7},{},{}};

    public List<Arista> aristas() {
        List<Arista> salida=new ArrayList<>();
        for(int i=0;i<SIGUIENTES.length;i++) for(int destino:SIGUIENTES[i]) salida.add(new Arista(i,destino));
        return List.copyOf(salida);
    }

    public int[] distanciasDesde(int... origenes) {
        int[] distancia=new int[SIGUIENTES.length];Arrays.fill(distancia,-1);
        Queue<Integer> cola=new ArrayDeque<>();
        for(int origen:origenes) {
            if(origen<0||origen>=SIGUIENTES.length) throw new IllegalArgumentException("Peatón desconocido");
            if(distancia[origen]>=0) continue;
            distancia[origen]=0;cola.add(origen);
        }
        while(!cola.isEmpty()) {
            int actual=cola.remove();
            for(int siguiente:SIGUIENTES[actual]) if(distancia[siguiente]<0) {
                distancia[siguiente]=distancia[actual]+1;cola.add(siguiente);
            }
        }
        return distancia;
    }
}
