package alcaldedigital.batalla;

import alcaldedigital.servidor.batalla.*;
import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class IndividualYUbicacionesTest {
    @TempDir Path tmp;
    private MotorBatalla escena(Fase fase,int patron,int reloj,int vidas){var v=new MotorBatalla(9,true).vista();Jugador[] js=v.jugadores();js[0]=MotorBatallaTest.jugador(js[0],500,530,vidas,10000);return MotorBatalla.restaurar(MotorBatallaTest.escena(v,fase,patron,reloj,js,List.of()));}
    @Test void ciudadanoEntraPorPuertaSuperiorYSinRecolocacionAlTerminarDialogo(){var m=new MotorBatalla(3,true);m.orden(0,0,0,false,"LISTO",0);var j=m.vista().jugadores()[0];assertEquals(MotorBatalla.ENTRADA_X,j.x());assertEquals(MotorBatalla.ENTRADA_Y,j.y());assertTrue(j.y()<250);assertTrue(MotorBatalla.oficinaLibre(j.x(),j.y()));
        for(int i=0;i<40;i++)m.paso();assertEquals(MotorBatalla.CONVERSACION_Y,m.vista().jugadores()[0].y());
        for(int i=0;i<8;i++)m.orden(0,0,0,false,"CONTINUAR",0);var antes=m.vista().jugadores();m.orden(0,0,0,false,"CONTINUAR",0);assertEquals(Fase.OFICINA,m.vista().fase());for(int i=0;i<2;i++){assertEquals(antes[i].x(),m.vista().jugadores()[i].x());assertEquals(antes[i].y(),m.vista().jugadores()[i].y());}}
    @Test void periodistaSaleDeSuPuestoPorUnPasilloTransitable(){var m=new MotorBatalla(3,true);m.orden(0,0,0,false,"LISTO",0);assertEquals(MotorBatalla.PERIODISTA_Y,m.vista().jugadores()[1].y());m.orden(0,0,0,false,"CONTINUAR",0);for(int i=0;i<90;i++){m.paso();Jugador j=m.vista().jugadores()[1];assertTrue(MotorBatalla.oficinaLibre(j.x(),j.y()));}assertEquals(MotorBatalla.PERIODISTA_CONVERSACION_Y,m.vista().jugadores()[1].y());}
    @Test void individualNoTieneBotNiPuntosNiOrdenesDelSegundoRol(){var m=escena(Fase.ESQUIVA,0,1,5);Jugador npc=m.vista().jugadores()[1];m.orden(1,-1,-1,true,"ACCION",0);for(int i=0;i<120;i++)m.paso();Jugador despues=m.vista().jugadores()[1];assertEquals(0,despues.vidas());assertEquals(0,despues.puntos());assertEquals(0,despues.recuperaciones());assertFalse(despues.conectado());assertFalse(despues.accionLista());assertEquals(npc.x(),despues.x());assertEquals(npc.y(),despues.y());}
    @Test void turnoIndividualSoloEsperaAlCiudadanoYNoVerificaAutomaticamente(){var m=escena(Fase.TURNO,2,0,5);for(int i=0;i<120;i++)m.paso();assertEquals(Fase.TURNO,m.vista().fase());assertEquals(0,m.vista().verificados());m.orden(0,0,0,false,"ACCION",0);m.paso();assertEquals(Fase.AVISO,m.vista().fase());assertEquals(1,m.vista().verificados());assertEquals(20,m.vista().jugadores()[0].puntos());assertEquals(0,m.vista().jugadores()[1].puntos());}
    @Test void perderCiudadanoTerminaElIndividualYSaleEnUnaPosicionValida(){var m=escena(Fase.ESQUIVA,0,5,0);m.paso();assertEquals(Fase.EPILOGO,m.vista().fase());assertFalse(m.vista().gano());for(var j:m.vista().jugadores())assertTrue(MotorBatalla.oficinaLibre(j.x(),j.y()));assertEquals(0,m.vista().jugadores()[0].vidas());}
    @Test void recuperacionIndividualEsPropiaYDecisionNoEsperaAUnBot(){var m=escena(Fase.TURNO,2,0,3);m.orden(0,0,0,false,"ACCION",2);m.paso();assertEquals(4,m.vista().jugadores()[0].vidas());assertEquals(0,m.vista().jugadores()[1].vidas());m=escena(Fase.DECISION,14,0,5);m.orden(0,0,0,false,"DECISION",1);m.paso();assertEquals(Fase.EPILOGO,m.vista().fase());assertTrue(m.vista().gano());}
    @Test void hacesIndividualesNuncaApuntanAlSegundoRol(){var m=escena(Fase.ESQUIVA,12,0,5);for(int i=0;i<320;i++){m.paso();for(var a:m.vista().obstaculos())if(a.tipo()==2)assertEquals(0,a.objetivo());}}
    @Test void guardadoIndividualAnteriorSeRetomaSinAcompanante()throws Exception{var m=escena(Fase.ESQUIVA,6,5,5);Vista v=m.vista();Jugador[] js=v.jugadores();js[1]=new Jugador("Periodista",760,530,1,2,5,500,0,1,0,true,true,true,true);m=MotorBatalla.restaurar(MotorBatallaTest.escena(v,Fase.ESQUIVA,6,5,js,List.of()));var g=new GuardadoBatalla(tmp.resolve("solo.bin"));g.guardar(m);var r=g.cargar();assertTrue(r.vista().solo());assertEquals(0,r.vista().jugadores()[1].vidas());assertEquals(0,r.vista().jugadores()[1].puntos());assertFalse(r.vista().jugadores()[1].accionLista());}
    @Test void puntosIndividualesNoAcreditanAlSegundoRol()throws Exception{var m=escena(Fase.FINAL,14,0,5);var repo=new RepositorioResultados(tmp.resolve("perfil.properties"));repo.acreditar(m.vista(),alcaldedigital.compartido.modelo.EstadoCiudad.inicial());var p=new Properties();try(var in=Files.newInputStream(tmp.resolve("perfil.properties"))){p.load(in);}assertNotNull(p.getProperty("sala."+m.vista().id()+".puntos.0"));assertNull(p.getProperty("sala."+m.vista().id()+".puntos.1"));}
}
