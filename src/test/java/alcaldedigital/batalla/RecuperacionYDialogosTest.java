package alcaldedigital.batalla;

import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import alcaldedigital.servidor.batalla.*;
import java.util.List;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RecuperacionYDialogosTest {
    @TempDir Path tmp;
    private MotorBatalla finalDeAtaque(Dificultad nivel,boolean solo,int patron,int vidas){
        var v=new MotorBatalla(7,solo,nivel).vista();var js=v.jugadores();
        for(int i=0;i<(solo?1:2);i++)js[i]=MotorBatallaTest.jugador(js[i],500+i*200,530,vidas,1000);
        return MotorBatalla.restaurar(MotorBatallaTest.escena(v,Fase.ESQUIVA,patron,MotorBatalla.duracion(patron)-1,js,List.of()));
    }
    @Test void recuperaUnCorazonPorRondaEnFacilYNormalParaCadaSuperviviente(){
        for(var nivel:new Dificultad[]{Dificultad.FACIL,Dificultad.NORMAL})for(boolean solo:new boolean[]{true,false})for(int patron:new int[]{2,5,8,11,14}){
            var m=finalDeAtaque(nivel,solo,patron,3);m.paso();assertEquals(Fase.TURNO,m.vista().fase());
            for(int i=0;i<(solo?1:2);i++){assertEquals(4,m.vista().jugadores()[i].vidas());assertEquals(10,m.vista().jugadores()[i].puntos());}
            if(solo)assertEquals(0,m.vista().jugadores()[1].vidas());
        }
    }
    @Test void noCuraEntreAtaquesNiAutomaticamenteEnDificil(){
        for(var nivel:Dificultad.values())for(boolean solo:new boolean[]{true,false})for(int patron:new int[]{0,1,2}){
            var m=finalDeAtaque(nivel,solo,patron,3);m.paso();
            int esperado=patron==2&&nivel!=Dificultad.DIFICIL?4:3;
            assertEquals(esperado,m.vista().jugadores()[0].vidas());
        }
    }
    @Test void nuncaSuperaCincoVidasNiReviveAlCaidoConLaCuracionDeRonda(){
        var m=finalDeAtaque(Dificultad.NORMAL,false,2,5);m.paso();assertEquals(5,m.vista().jugadores()[0].vidas());
        m=finalDeAtaque(Dificultad.FACIL,false,2,3);var v=m.vista();var js=v.jugadores();var j=js[0];
        js[0]=new Jugador(j.nombre(),j.x(),j.y(),0,0,0,0,0,0,0,false,true,true,false);
        m=MotorBatalla.restaurar(MotorBatallaTest.escena(v,Fase.ESQUIVA,2,v.reloj(),js,List.of()));m.paso();
        assertEquals(0,m.vista().jugadores()[0].vidas());assertEquals(4,m.vista().jugadores()[1].vidas());
    }
    @Test void guardadoAntesYDespuesDelTurnoNoDuplicaLaCuracion()throws Exception{
        var m=finalDeAtaque(Dificultad.NORMAL,true,2,2);var g=new GuardadoBatalla(tmp.resolve("ronda.bin"));g.guardar(m);m=g.cargar();m.paso();assertEquals(3,m.vista().jugadores()[0].vidas());
        g.guardar(m);m=g.cargar();for(int i=0;i<120;i++)m.paso();assertEquals(3,m.vista().jugadores()[0].vidas());
        m.orden(0,0,0,false,"ACCION",0);m.paso();assertEquals(3,m.vista().jugadores()[0].vidas());
    }
    @Test void rescateCooperativoConservaSusDosVidasAlFinalDeRonda(){
        var m=finalDeAtaque(Dificultad.NORMAL,false,2,3);var v=m.vista();var js=v.jugadores();
        js[0]=MotorBatallaTest.jugador(js[0],500,530,0,0);
        m=MotorBatalla.restaurar(MotorBatallaTest.escena(v,Fase.ESQUIVA,2,v.reloj(),js,List.of()));m.paso();
        assertEquals(2,m.vista().jugadores()[0].vidas());assertEquals(0,m.vista().jugadores()[0].recuperaciones());assertEquals(4,m.vista().jugadores()[1].vidas());
    }
    @Test void avanzarConversacionesNoReiniciaLaAnimacionNiLasPosiciones(){
        for(boolean solo:new boolean[]{true,false})for(var fase:new Fase[]{Fase.INTRO,Fase.EPILOGO}){
            var v=new MotorBatalla(7,solo).vista();var m=MotorBatalla.restaurar(MotorBatallaTest.escena(v,fase,0,40,v.jugadores(),List.of()));var antes=m.vista();
            m.orden(0,0,0,false,"CONTINUAR",0);if(!solo)m.orden(1,0,0,false,"CONTINUAR",0);
            assertEquals(1,m.vista().linea());assertEquals(40,m.vista().reloj());
            for(int i=0;i<2;i++){assertEquals(antes.jugadores()[i].x(),m.vista().jugadores()[i].x());assertEquals(antes.jugadores()[i].y(),m.vista().jugadores()[i].y());}
            m.paso();assertEquals(41,m.vista().reloj());
        }
    }
}
