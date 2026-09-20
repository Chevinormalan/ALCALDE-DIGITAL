package alcaldedigital.compartido.modelo;

/** Valores colectivos limitados al intervalo 0–100. */
public record EstadoCiudad(
        int informacionVerificada,
        int confianza,
        int convivencia,
        int bienestar,
        int participacion,
        int desinformacion,
        int conflictos) {

    public EstadoCiudad {
        informacionVerificada = limitar(informacionVerificada);
        confianza = limitar(confianza);
        convivencia = limitar(convivencia);
        bienestar = limitar(bienestar);
        participacion = limitar(participacion);
        desinformacion = limitar(desinformacion);
        conflictos = limitar(conflictos);
    }

    public static EstadoCiudad inicial() {
        return new EstadoCiudad(50, 50, 50, 50, 50, 20, 20);
    }

    private static int limitar(int valor) {
        return Math.max(0, Math.min(100, valor));
    }
}

