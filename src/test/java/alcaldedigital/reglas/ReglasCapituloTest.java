package alcaldedigital.reglas;

import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.modelo.Evidencia;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.compartido.modelo.Publicacion;
import alcaldedigital.i1.ValidadorI1;
import alcaldedigital.servidor.reglas.ReglasI1;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReglasCapituloTest {
    @Test void evidenciaTieneContratoSimple() {
        var e=new Evidencia("ev-1","Aviso municipal","No existe cierre aprobado");
        assertEquals("ev-1",e.id());
        assertEquals("Aviso municipal",e.fuente());
        assertThrows(IllegalArgumentException.class,()->new Evidencia("","x","y"));
    }

    @Test void cambiosDePuntuacionEstanCentralizados() {
        EstadoCiudad inicial=EstadoCiudad.inicial();
        EstadoCiudad finalEstado=ReglasI1.aplicar(inicial,ReglasI1.PUBLICAR_CONTEXTO);
        assertEquals(58,finalEstado.informacionVerificada());
        assertEquals(55,finalEstado.confianza());
        assertEquals(10,finalEstado.desinformacion());
        assertEquals(15,ReglasI1.PUBLICAR_CONTEXTO.puntos());
    }

    @Test void validadorReconoceAVLCorrecto() {
        var a=new ArbolAVLPublicaciones();
        a.insertar(new Publicacion(1,30,"a","a","x"));
        a.insertar(new Publicacion(2,20,"b","b","x"));
        a.insertar(new Publicacion(3,10,"c","c","x"));
        var r=ValidadorI1.validarAVL(a.vista());
        assertTrue(r.correcto());
        assertEquals(3,r.nodos());
        assertEquals(2,r.altura());
    }
}
