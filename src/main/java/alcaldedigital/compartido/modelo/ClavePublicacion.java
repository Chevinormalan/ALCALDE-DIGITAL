package alcaldedigital.compartido.modelo;

/** Clave lexicográfica del AVL: primero riesgo y luego ID estable. */
public record ClavePublicacion(int riesgo, long id) implements Comparable<ClavePublicacion> {
    @Override
    public int compareTo(ClavePublicacion otra) {
        int porRiesgo = Integer.compare(riesgo, otra.riesgo);
        return porRiesgo != 0 ? porRiesgo : Long.compare(id, otra.id);
    }
}

