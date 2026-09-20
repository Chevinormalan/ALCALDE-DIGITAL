package alcaldedigital.compartido.protocolo;

import alcaldedigital.compartido.modelo.Rol;
import java.util.Objects;
import java.util.Random;

/** Configuración local: conserva la semilla y permite fijar una variante en pruebas. */
public record ConfiguracionSesion(Rol rol, long semilla, int plazo, Integer varianteFijada) {
    public ConfiguracionSesion {
        Objects.requireNonNull(rol, "rol");
        if (plazo < 10) throw new IllegalArgumentException("Plazo mínimo: 10 segundos");
        if (varianteFijada != null && (varianteFijada < 0 || varianteFijada >= 4))
            throw new IllegalArgumentException("Variante fuera de rango: 0–3");
    }

    public ConfiguracionSesion(Rol rol, long semilla, int plazo) {
        this(rol, semilla, plazo, null);
    }

    public int indiceVariante() {
        return varianteFijada == null ? new Random(semilla).nextInt(4) : varianteFijada;
    }
}
