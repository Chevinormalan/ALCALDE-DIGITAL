package alcaldedigital.compartido.modelo;

import java.util.Objects;

/** Evidencia que el jugador obtiene durante la verificación del evento. */
public record Evidencia(String id, String fuente, String contenido) {
    public Evidencia {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID de evidencia vacío");
        Objects.requireNonNull(fuente, "fuente");
        Objects.requireNonNull(contenido, "contenido");
    }
}
