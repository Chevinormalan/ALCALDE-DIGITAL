package alcaldedigital.compartido.estructuras.grafo;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Conexiones jugables entre las zonas de NovaMall. */
public final class GrafoZonasMisionTres {
    private static final Map<String,Set<String>> SALIDAS=Map.of(
        "intro",Set.of("atrio"), "atrio",Set.of("pixel"),
        "pixel",Set.of("corrupted"), "corrupted",Set.of("core"),
        "core",Set.of("ending"), "ending",Set.of());

    public boolean conectadas(String origen,String destino) {
        return SALIDAS.getOrDefault(origen,Set.of()).contains(destino);
    }

    public List<String> ruta(String origen,String destino) {
        if(!SALIDAS.containsKey(origen)||!SALIDAS.containsKey(destino)) return List.of();
        ArrayDeque<List<String>> cola=new ArrayDeque<>();
        cola.add(List.of(origen));
        while(!cola.isEmpty()) {
            List<String> ruta=cola.removeFirst();
            String actual=ruta.getLast();
            if(actual.equals(destino)) return ruta;
            for(String vecino:SALIDAS.get(actual)) if(!ruta.contains(vecino)) {
                var siguiente=new java.util.ArrayList<>(ruta);
                siguiente.add(vecino);
                cola.addLast(List.copyOf(siguiente));
            }
        }
        return List.of();
    }
}
