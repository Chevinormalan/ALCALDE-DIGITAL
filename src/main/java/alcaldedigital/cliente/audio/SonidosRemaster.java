package alcaldedigital.cliente.audio;

/** Recursos de Foley y señales diseñados para la remasterización. */
public final class SonidosRemaster {
    private SonidosRemaster() {}

    public static String ruta(String nombre) {
        return "audio/remaster/" + nombre;
    }

    public static void precargar(String... sonidos) {
        MotorAudio motor = MotorAudio.global();
        for (String nombre : sonidos) motor.precargar(ruta(nombre));
    }
}
