package alcaldedigital.batalla;

import alcaldedigital.compartido.batalla.EstadoBatalla;
import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import alcaldedigital.servidor.batalla.*;
import alcaldedigital.cliente.mision5.MisionCincoVista;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import java.io.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class DificultadesBatallaTest {
    @TempDir Path tmp;
    @Test void normalConservaDuracionYLosAvisosPrecedenAlDano(){
        var m=PatronesPasablesTest.iniciar(0,0,Dificultad.NORMAL,true,Fase.ESQUIVA);
        m.paso();
        assertFalse(m.vista().obstaculos().isEmpty());
        assertTrue(m.vista().obstaculos().stream().allMatch(a->a.edad()<a.aviso()));
        for(int i=1;i<40;i++)m.paso();
        assertEquals(5,m.vista().jugadores()[0].vidas());
        assertEquals(360,MotorBatalla.duracion(0));
    }
    @Test void avisoCambiaConLaDificultadSinCambiarElMovimientoDelCorazon(){
        int avisoAnterior=Integer.MAX_VALUE;
        for(Dificultad d:Dificultad.values()){
            var m=PatronesPasablesTest.iniciar(0,0,d,true,Fase.ESQUIVA);
            m.orden(0,1,0,false,"",0);
            float x=m.vista().jugadores()[0].x();
            m.paso();
            assertEquals(3.8f,m.vista().jugadores()[0].x()-x,.001f);
            int aviso=m.vista().obstaculos().getFirst().aviso();
            assertTrue(aviso<avisoAnterior);
            avisoAnterior=aviso;
        }
    }
    @Test void dificultadViajaPorCodecYSeConservaAlGuardar()throws Exception{var m=PatronesPasablesTest.iniciar(4,6,Dificultad.DIFICIL,true,Fase.ESQUIVA);for(int i=0;i<10;i++)m.paso();var g=new GuardadoBatalla(tmp.resolve("dificil.bin"));g.guardar(m);var restaurado=g.cargar();assertEquals(Dificultad.DIFICIL,restaurado.vista().dificultad());assertArrayEquals(MotorBatallaTest.bytes(m.vista()),MotorBatallaTest.bytes(restaurado.vista()));}
    @Test void guardadosDeVersionAnteriorSeLeenComoNormal()throws Exception{byte[] actual=MotorBatallaTest.bytes(new MotorBatalla(2,true).vista());Path archivo=tmp.resolve("anterior.bin");try(var out=new DataOutputStream(Files.newOutputStream(archivo))){out.writeInt(0x41443542);out.writeInt(1);out.write(actual,0,actual.length-4);}assertEquals(Dificultad.NORMAL,new GuardadoBatalla(archivo).cargar().vista().dificultad());}
    @Test void unGuardadoAnteriorEnCombateReiniciaSoloElPatronEnCurso()throws Exception{
        var m=PatronesPasablesTest.iniciar(4,6,Dificultad.DIFICIL,false,Fase.ESQUIVA);
        for(int paso=0;paso<65;paso++)m.paso();
        var anterior=m.vista();
        assertFalse(anterior.obstaculos().isEmpty());
        Path ruta=tmp.resolve("oleadas-anteriores.bin");
        try(var out=new DataOutputStream(Files.newOutputStream(ruta))){
            out.writeInt(0x41443542);out.writeInt(2);EstadoBatalla.escribir(out,anterior);
        }
        var actual=new GuardadoBatalla(ruta).cargar().vista();
        assertEquals(Fase.AVISO,actual.fase());
        assertEquals(anterior.patron(),actual.patron());
        assertEquals(anterior.ronda(),actual.ronda());
        assertEquals(anterior.dificultad(),actual.dificultad());
        assertEquals(anterior.jugadores()[0].vidas(),actual.jugadores()[0].vidas());
        assertEquals(anterior.jugadores()[0].puntos(),actual.jugadores()[0].puntos());
        assertTrue(actual.obstaculos().isEmpty());
    }
    @Test void tecladoYMouseSeleccionanDificultadSinAlterarElModo()throws Exception{
        String anterior=System.getProperty("ad.guardados");System.setProperty("ad.guardados",tmp.toString());
        try{for(boolean mouse:new boolean[]{false,true})try(var vista=new MisionCincoVista(2,EstadoCiudad.inicial(),false,false)){
            vista.iniciarEn(null);if(mouse)vista.raton(800,335);else{vista.pulsar(' ',37);vista.soltar(37);}vista.pulsar(' ',10);vista.soltar(10);
            for(int i=0;i<200&&vista.vistaResultado()==null;i++){vista.actualizar(0);Thread.sleep(5);}assertNotNull(vista.vistaResultado());assertTrue(vista.vistaResultado().solo());assertEquals(mouse?Dificultad.DIFICIL:Dificultad.FACIL,vista.vistaResultado().dificultad());
        }}finally{if(anterior==null)System.clearProperty("ad.guardados");else System.setProperty("ad.guardados",anterior);}
    }
}
