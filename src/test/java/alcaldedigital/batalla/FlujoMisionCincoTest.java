package alcaldedigital.batalla;

import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import alcaldedigital.servidor.batalla.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class FlujoMisionCincoTest {
    @Test void quincePatronesCincoTurnosEleccionYEpilogoConConservacionDePuntos(){
        var motor=new MotorBatalla(2026,false);var v=motor.vista();Jugador[] js=v.jugadores();for(int i=0;i<2;i++)js[i]=MotorBatallaTest.jugador(js[i],500+i*250,530,5,100000);
        motor=MotorBatalla.restaurar(MotorBatallaTest.escena(v,Fase.AVISO,0,0,js,List.of()));Set<Integer> patrones=new HashSet<>();int turnos=0;
        for(int pasos=0;pasos<18000&&motor.vista().fase()!=Fase.DECISION;pasos++){
            var a=motor.vista();
            // Mantener inmunidad en este caso aísla el controlador de fases; el daño se prueba aparte.
            if(a.fase()==Fase.ESQUIVA&&a.reloj()==1){Jugador[] seguros=a.jugadores();for(int i=0;i<2;i++)seguros[i]=MotorBatallaTest.jugador(seguros[i],seguros[i].x(),seguros[i].y(),5,100000);motor=MotorBatalla.restaurar(MotorBatallaTest.escena(a,Fase.ESQUIVA,a.patron(),a.reloj(),seguros,a.obstaculos()));a=motor.vista();}
            if(a.fase()==Fase.ESQUIVA)patrones.add(a.patron());if(a.fase()==Fase.TURNO){motor.orden(0,0,0,false,"ACCION",0);motor.orden(1,0,0,false,"ACCION",1);turnos++;}motor.paso();}
        assertEquals(15,patrones.size());assertEquals(5,turnos);assertEquals(Fase.DECISION,motor.vista().fase());assertEquals(5,motor.vista().verificados());assertTrue(motor.vista().jugadores()[0].puntos()>0);
        motor.orden(0,0,0,false,"DECISION",1);motor.orden(1,0,0,false,"DECISION",1);motor.paso();assertEquals(Fase.EPILOGO,motor.vista().fase());assertTrue(motor.vista().gano());
        for(int i=0;i<4;i++){motor.orden(0,0,0,false,"CONTINUAR",0);motor.orden(1,0,0,false,"CONTINUAR",0);}assertEquals(Fase.FINAL,motor.vista().fase());
    }
    @Test void perdonarDaUnaUltimaOleadaConAvisoYSalidaReal(){var v=new MotorBatalla(1,false).vista();Jugador[] js=v.jugadores();for(int i=0;i<2;i++)js[i]=MotorBatallaTest.jugador(js[i],500+i*200,530,5,1000);var m=MotorBatalla.restaurar(MotorBatallaTest.escena(v,Fase.DECISION,14,0,js,List.of()));m.orden(0,0,0,false,"DECISION",0);m.orden(1,0,0,false,"DECISION",0);m.paso();assertEquals(Fase.TRAICION,m.vista().fase());for(int i=0;i<420;i++)m.paso();assertEquals(Fase.EPILOGO,m.vista().fase());assertTrue(m.vista().gano());}
}
