package alcaldedigital.compartido.modelo;

import java.util.Objects;

/** Acumula cambios de cada misión respecto a su propio estado inicial. */
public final class ProgresoCampana {
    private static final int MAX_FUENTES_MISION_CUATRO=4;
    private ProgresoCampana() { }

    public static EstadoCiudad aplicarMisionUno(EstadoCiudad anterior, EstadoCiudad resultado) {
        Objects.requireNonNull(anterior);
        Objects.requireNonNull(resultado);
        EstadoCiudad base=EstadoCiudad.inicial();
        return new EstadoCiudad(
            anterior.informacionVerificada()+resultado.informacionVerificada()-base.informacionVerificada(),
            anterior.confianza()+resultado.confianza()-base.confianza(),
            anterior.convivencia()+resultado.convivencia()-base.convivencia(),
            anterior.bienestar()+resultado.bienestar()-base.bienestar(),
            anterior.participacion()+resultado.participacion()-base.participacion(),
            anterior.desinformacion()+resultado.desinformacion()-base.desinformacion(),
            anterior.conflictos()+resultado.conflictos()-base.conflictos());
    }

    public static EstadoCiudad aplicarMisionTres(EstadoCiudad anterior, int confianzaFinal,
                                                  int desinformacionFinal) {
        Objects.requireNonNull(anterior);
        if(confianzaFinal<0||confianzaFinal>100||desinformacionFinal<0||desinformacionFinal>100)
            throw new IllegalArgumentException("Indicadores de misión 3 fuera de rango");
        // La misión 3 inicia en 35 % de confianza y 8 % de desinformación.
        return new EstadoCiudad(anterior.informacionVerificada(),
            anterior.confianza()+confianzaFinal-35,anterior.convivencia(),anterior.bienestar(),
            anterior.participacion(),anterior.desinformacion()+desinformacionFinal-8,
            anterior.conflictos());
    }

    public static EstadoCiudad aplicarMisionDos(EstadoCiudad anterior,int protegidos) {
        Objects.requireNonNull(anterior);
        if(protegidos<0||protegidos>6) throw new IllegalArgumentException("Equipos protegidos fuera de rango");
        int confianza=protegidos==6?6:protegidos>=4?1:-6;
        int desinformacion=protegidos==6?-8:12-2*protegidos;
        return new EstadoCiudad(anterior.informacionVerificada(),
            anterior.confianza()+confianza,anterior.convivencia(),anterior.bienestar(),
            anterior.participacion(),anterior.desinformacion()+desinformacion,
            anterior.conflictos());
    }
    public static EstadoCiudad aplicarMisionCuatro(EstadoCiudad anterior,int validadas,int respuestasResponsables) {
        Objects.requireNonNull(anterior);
        if(validadas<0||validadas>MAX_FUENTES_MISION_CUATRO||respuestasResponsables<0||respuestasResponsables>4)
            throw new IllegalArgumentException("Resultado de misión 4 fuera de rango");
        // Contrastar los expedientes reduce el efecto de los rumores en la campaña.
        return new EstadoCiudad(anterior.informacionVerificada()+validadas*4,
            anterior.confianza()+respuestasResponsables+validadas,
            anterior.convivencia()+respuestasResponsables,
            anterior.bienestar(),anterior.participacion(),
            anterior.desinformacion()+6-validadas*3,
            anterior.conflictos()+3-validadas);
    }
    /** Balance de respuestas explícitas de la plaza; la tercera pregunta no tiene polaridad. */
    public static EstadoCiudad aplicarMisionCuatro(EstadoCiudad anterior,int validadas,int favorables,int desfavorables){
        Objects.requireNonNull(anterior);
        if(validadas<0||validadas>MAX_FUENTES_MISION_CUATRO||favorables<0||desfavorables<0||favorables+desfavorables>3)
            throw new IllegalArgumentException("Respuestas de misión 4 fuera de rango");
        int balance=favorables-desfavorables;
        return new EstadoCiudad(anterior.informacionVerificada()+validadas*4,
            anterior.confianza()+validadas+balance,
            anterior.convivencia()+balance,
            anterior.bienestar(),anterior.participacion(),
            anterior.desinformacion()+6-validadas*3+desfavorables,
            anterior.conflictos()+3-validadas+desfavorables);
    }
}
