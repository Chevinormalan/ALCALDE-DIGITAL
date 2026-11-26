package alcaldedigital.cliente;

import alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import java.util.Comparator;
import static alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;

/** Preferencias de la pantalla de investigación. El progreso pertenece a la sesión. */
public final class InvestigacionSofi {
    public double finAviso, finRecogida, finPagina, finSello, ultimaPista, inicioLupa;
    public double finRespuestaVecino;
    public int vecinoAviso=-1;
    public boolean ayuda;
    public boolean libreta;

    public void reiniciar() {
        libreta=false;ayuda=false;finAviso=0;finRecogida=0;finPagina=0;finSello=0;ultimaPista=0;inicioLupa=0;
        finRespuestaVecino=0;vecinoAviso=-1;
    }
    public Caso papelCercano(SesionMisionUno.Vista s) {
        return CASOS.stream().filter(c->distancia(s,c)<=29)
            .min(Comparator.comparingDouble(c->distancia(s,c))).orElse(null);
    }
    public boolean convocatoriaLista(SesionMisionUno.Vista s) {
        return s.resueltas().stream().allMatch(Boolean::booleanValue);
    }
    public boolean cercaDelTablon(SesionMisionUno.Vista s) {
        return Math.hypot(s.x()-TABLON_X,s.y()-TABLON_Y)<=ALCANCE_TABLON;
    }
    private static double distancia(SesionMisionUno.Vista s,Caso c) {
        return Math.hypot(s.x()-c.x(),s.y()-c.y());
    }
    public Vecino vecinoCercano(SesionMisionUno.Vista s,Vista investigacion) {
        return VECINOS.stream().filter(v->!investigacion.orientado(v.id())&&distancia(s,v)<=38)
            .min(Comparator.comparingDouble(v->distancia(s,v))).orElse(null);
    }
    private static double distancia(SesionMisionUno.Vista s,Vecino v) {
        var p=s.peatones().get(v.id());
        return Math.hypot(s.x()-p.x(),s.y()-p.y());
    }
}
