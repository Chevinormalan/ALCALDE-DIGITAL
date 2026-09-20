package alcaldedigital.reglas;
import alcaldedigital.servidor.sesion.*;
import alcaldedigital.servidor.eventos.*;
import alcaldedigital.compartido.modelo.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class SesionLocalTest {
    static void moverHasta(SesionLocal s,int x,int y) {
        for(int i=0;i<1000&&Math.abs(s.vista().x()-x)>.5;i++) s.mover(Math.signum(x-s.vista().x())*Math.min(2,Math.abs(x-s.vista().x())),0);
        for(int i=0;i<1000&&Math.abs(s.vista().y()-y)>.5;i++) s.mover(0,Math.signum(y-s.vista().y())*Math.min(2,Math.abs(y-s.vista().y())));
        assertEquals(x,s.vista().x(),1);assertEquals(y,s.vista().y(),1);
    }
    static void investigar(SesionLocal s) {
        s.solicitar("v","verificar");moverHasta(s,192,174);s.solicitar("a","aviso");
        moverHasta(s,230,208);s.solicitar("f","fuente");assertEquals(2,s.vista().evidencias().size());
    }
    @Test void cuatroVariantesYCuatroRolesCompletanCaso() {
        assertEquals(4,EventoParque.cargar().variantes().size());
        for(Rol r:Rol.values()) for(int i=0;i<4;i++) {
            var s=new SesionLocal(r,i,60);assertTrue(s.vista().evidencias().isEmpty());
            assertNull(s.arbolDecisiones().buscar("contexto"));investigar(s);
            assertNotNull(s.arbolDecisiones().buscar("contexto"));assertEquals(8,s.arbolAVL().buscar(1).riesgo());
            s.solicitar("c","contexto");assertTrue(s.vista().terminado());assertEquals(25,s.vista().puntos());
            assertEquals(58,s.vista().ciudad().informacionVerificada());assertEquals(10,s.vista().ciudad().desinformacion());
            assertNull(s.arbolAVL().buscar(1));assertTrue(s.archivo().containsKey(1L));assertNotNull(s.arbolAVL().buscar(4));
            assertNull(s.arbolDecisiones().buscar("compartir"));assertTrue(s.arbolDecisiones().buscar("contexto").ejecutado());
        }
    }
    @Test void compartirYRectificarRecuperaParcialmenteSinDuplicar() {
        var s=new SesionLocal(Rol.INFLUENCER,0,60);s.solicitar("c","compartir");
        int puntos=s.vista().puntos();s.solicitar("c","compartir");s.solicitar("c2","compartir");assertEquals(puntos,s.vista().puntos());
        assertEquals(75,s.arbolAVL().buscar(1).riesgo());investigar(s);s.solicitar("r","rectificar");
        assertEquals(49,s.vista().ciudad().confianza());assertEquals(25,s.vista().ciudad().desinformacion());assertEquals(8,s.vista().puntos());
    }
    @Test void reportarConYSinPruebas() {
        var sin=new SesionLocal(Rol.CIUDADANO,0,60);sin.solicitar("r","reportar");assertNotNull(sin.arbolAVL().buscar(1));assertEquals(-2,sin.vista().puntos());
        var con=new SesionLocal(Rol.CIUDADANO,0,60);investigar(con);con.solicitar("r","reportar");assertNull(con.arbolAVL().buscar(1));assertEquals(18,con.vista().puntos());
    }
    @Test void plazoAccionesBloqueadasYColisiones() {
        var s=new SesionLocal(Rol.PERIODISTA,0,10);
        s.solicitar("atajo","contexto");s.solicitar("lejos","aviso");assertEquals(0,s.vista().puntos());assertFalse(s.vista().terminado());
        s.actualizar(11);assertTrue(s.vista().terminado());assertEquals(0,s.vista().puntos());
        var mapa=new MapaParque();assertFalse(mapa.transitable(320,228));assertFalse(mapa.transitable(90,80));assertFalse(mapa.transitable(0,0));assertTrue(mapa.transitable(320,368));
    }
    @Test void investigacionSinPlazoYGuardarSoloUnaRama() {
        var s=new SesionLocal(Rol.CIUDADANO,3,10);investigar(s);s.actualizar(500);assertFalse(s.vista().terminado());
        s.solicitar("g","guardar");assertEquals(15,s.vista().puntos());assertNotNull(s.arbolAVL().buscar(1));assertNull(s.arbolAVL().buscar(4));
        var antes=s.vista().ciudad();s.solicitar("posterior","contexto");assertEquals(antes,s.vista().ciudad());
    }
    @Test void semillaEsReproducible() {
        var a=new SesionLocal(Rol.CIUDADANO,20260917L,60);var b=new SesionLocal(Rol.CIUDADANO,20260917L,60);
        investigar(a);investigar(b);assertEquals(a.vista().evidencias(),b.vista().evidencias());
    }
}
