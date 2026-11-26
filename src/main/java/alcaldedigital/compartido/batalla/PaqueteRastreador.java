package alcaldedigital.compartido.batalla;

import alcaldedigital.compartido.batalla.EstadoBatalla.Dificultad;
import alcaldedigital.compartido.batalla.EstadoBatalla.Obstaculo;

/** Fases compartidas por el motor y el dibujo a partir de la edad del obstáculo. */
public record PaqueteRastreador(int aviso, int seguimiento, int fijado, int embestida,
                               float velocidadSeguimiento, float velocidadEmbestida) {
    public static final int TIPO = 4;
    public static final int DISIPACION = 9;
    private static final PaqueteRastreador FACIL = new PaqueteRastreador(42, 24, 27, 22, 2.2f, 4.8f);
    private static final PaqueteRastreador NORMAL = new PaqueteRastreador(36, 28, 24, 22, 2.6f, 5.1f);
    private static final PaqueteRastreador DIFICIL = new PaqueteRastreador(30, 32, 21, 24, 3.05f, 5.5f);

    public enum Fase {
        AVISO, SEGUIMIENTO, FIJADO, EMBESTIDA, DISIPACION;

        public boolean haceDanio() {
            return this == SEGUIMIENTO || this == EMBESTIDA;
        }
    }

    public static PaqueteRastreador para(Dificultad dificultad) {
        return switch (dificultad) {
            case FACIL -> FACIL;
            case NORMAL -> NORMAL;
            case DIFICIL -> DIFICIL;
        };
    }

    public int duracion() {
        return aviso + seguimiento + fijado + embestida + DISIPACION - 1;
    }

    public int inicioDisipacion(Obstaculo a) {
        return a.duracion() - DISIPACION + 1;
    }

    public Fase fase(Obstaculo a, int edad) {
        if (edad < a.aviso()) return Fase.AVISO;
        if (edad < a.aviso() + seguimiento) return Fase.SEGUIMIENTO;
        if (edad < a.aviso() + seguimiento + fijado) return Fase.FIJADO;
        if (edad < inicioDisipacion(a)) return Fase.EMBESTIDA;
        return Fase.DISIPACION;
    }

    public Fase fase(Obstaculo a) {
        return fase(a, a.edad());
    }
}
