package alcaldedigital.servidor.reglas;

import alcaldedigital.compartido.modelo.EstadoCiudad;

/**
 * Centraliza los cambios de puntuación e indicadores del capítulo.
 * La sesión decide cuándo aplicar una regla; esta clase solo define el cambio.
 */
public final class ReglasI1 {
    private ReglasI1() { }

    public static final int PUNTOS_EVIDENCIA = 5;
    public static final int PUNTOS_ACCION_ROL = 3;

    public static final Cambio COMPARTIR = new Cambio(0, -4, -3, -2, 1, 10, 5, -6);
    public static final Cambio REPORTAR_CON_PRUEBAS = new Cambio(5, 3, 1, 2, 2, -8, -3, 8);
    public static final Cambio REPORTAR_SIN_PRUEBAS = new Cambio(0, -2, -1, -1, -3, 0, 1, -2);
    public static final Cambio IGNORAR = new Cambio(0, 0, 0, 1, -2, 2, 0, 0);
    public static final Cambio PUBLICAR_CONTEXTO = new Cambio(8, 5, 4, 3, 4, -10, -4, 15);
    public static final Cambio RECTIFICAR = new Cambio(3, 3, 2, 2, 2, -5, -2, 4);
    public static final Cambio GUARDAR_EVIDENCIA = new Cambio(4, 1, 0, 1, 0, 0, 0, 5);

    public record Cambio(
            int informacion,
            int confianza,
            int convivencia,
            int bienestar,
            int participacion,
            int desinformacion,
            int conflictos,
            int puntos) { }

    public static EstadoCiudad aplicar(EstadoCiudad actual, Cambio cambio) {
        return new EstadoCiudad(
                actual.informacionVerificada() + cambio.informacion(),
                actual.confianza() + cambio.confianza(),
                actual.convivencia() + cambio.convivencia(),
                actual.bienestar() + cambio.bienestar(),
                actual.participacion() + cambio.participacion(),
                actual.desinformacion() + cambio.desinformacion(),
                actual.conflictos() + cambio.conflictos());
    }
}
