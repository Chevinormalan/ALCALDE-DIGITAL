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
    @Test void normalConservaCadenciaYVelocidadDelCombateAnterior(){var m=PatronesPasablesTest.iniciar(0,0,Dificultad.NORMAL,true);for(int i=0;i<41;i++)m.paso();assertTrue(m.vista().obstaculos().isEmpty());m.paso();assertEquals(2,m.vista().obstaculos().size());assertEquals(4,m.vista().obstaculos().getFirst().vx());assertEquals(360,MotorBatalla.duracion(0));}
    @Test void facilidadYPresionCambianSinCambiarElMovimientoDelCorazon(){for(Dificultad d:Dificultad.values()){var m=PatronesPasablesTest.iniciar(0,0,d,true);m.orden(0,1,0,false,"",0);float x=m.vista().jugadores()[0].x();m.paso();assertEquals(3.8f,m.vista().jugadores()[0].x()-x,.001f);for(int i=1;i<60;i++)m.paso();assertEquals(4*d.velocidad,m.vista().obstaculos().getFirst().vx(),.001f);}}
    @Test void dificultadViajaPorCodecYSeConservaAlGuardar()throws Exception{var m=PatronesPasablesTest.iniciar(4,6,Dificultad.DIFICIL,true);for(int i=0;i<10;i++)m.paso();var g=new GuardadoBatalla(tmp.resolve("dificil.bin"));g.guardar(m);var restaurado=g.cargar();assertEquals(Dificultad.DIFICIL,restaurado.vista().dificultad());assertArrayEquals(MotorBatallaTest.bytes(m.vista()),MotorBatallaTest.bytes(restaurado.vista()));}
    @Test void guardadosDeVersionAnteriorSeLeenComoNormal()throws Exception{byte[] actual=MotorBatallaTest.bytes(new MotorBatalla(2,true).vista());Path archivo=tmp.resolve("anterior.bin");try(var out=new DataOutputStream(Files.newOutputStream(archivo))){out.writeInt(0x41443542);out.writeInt(1);out.write(actual,0,actual.length-4);}assertEquals(Dificultad.NORMAL,new GuardadoBatalla(archivo).cargar().vista().dificultad());}
    @Test void tecladoYMouseSeleccionanDificultadSinAlterarElModo()throws Exception{
        String anterior=System.getProperty("ad.guardados");System.setProperty("ad.guardados",tmp.toString());
        try{for(boolean mouse:new boolean[]{false,true})try(var vista=new MisionCincoVista(2,EstadoCiudad.inicial(),false,false)){
            vista.iniciarEn(null);if(mouse)vista.raton(800,335);else{vista.pulsar(' ',37);vista.soltar(37);}vista.pulsar(' ',10);vista.soltar(10);
            for(int i=0;i<200&&vista.vistaResultado()==null;i++){vista.actualizar(0);Thread.sleep(5);}assertNotNull(vista.vistaResultado());assertTrue(vista.vistaResultado().solo());assertEquals(mouse?Dificultad.DIFICIL:Dificultad.FACIL,vista.vistaResultado().dificultad());
        }}finally{if(anterior==null)System.clearProperty("ad.guardados");else System.setProperty("ad.guardados",anterior);}
    }
}
