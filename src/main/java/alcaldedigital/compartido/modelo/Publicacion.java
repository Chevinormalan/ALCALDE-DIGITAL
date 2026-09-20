package alcaldedigital.compartido.modelo;

import java.util.Objects;

/** Solo información pública: la verdad del evento pertenece a la sesión. */
public record Publicacion(long id, int riesgo, String autor, String texto, String estado) {
    public Publicacion {
        if (id < 1 || riesgo < 0 || riesgo > 100) throw new IllegalArgumentException("ID/riesgo inválido");
        Objects.requireNonNull(autor); Objects.requireNonNull(texto); Objects.requireNonNull(estado);
    }
    public ClavePublicacion clave() { return new ClavePublicacion(riesgo, id); }
    public Publicacion conRiesgo(int valor, String estadoNuevo) {
        return new Publicacion(id, valor, autor, texto, estadoNuevo);
    }
}
