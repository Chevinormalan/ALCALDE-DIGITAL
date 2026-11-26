package alcaldedigital.batalla;

import alcaldedigital.cliente.mision5.ClienteBatalla;
import alcaldedigital.servidor.batalla.*;
import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import alcaldedigital.compartido.batalla.PaqueteRastreador;
import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RedBatallaTest {
    @TempDir Path tmp;
    private static void esperar(BooleanSupplier condicion)throws Exception{long fin=System.nanoTime()+4_000_000_000L;while(!condicion.getAsBoolean()&&System.nanoTime()<fin)Thread.sleep(10);assertTrue(condicion.getAsBoolean(),"No llegó el estado esperado por TCP");}
    @Test void dosClientesMismaSalaControlanRolesDistintosYSeReconectan()throws Exception{
        var motor=new MotorBatalla(2026,false,Dificultad.DIFICIL);var guardado=new GuardadoBatalla(tmp.resolve("lan.bin"));
        try(var servidor=new ServidorBatalla(InetAddress.getLoopbackAddress(),0,motor,guardado);var a=new ClienteBatalla("127.0.0.1",servidor.puerto(),0,"Sofi")){
            var b=new ClienteBatalla("127.0.0.1",servidor.puerto(),1,"Periodista");
            try{esperar(()->a.vista().jugadores()[1].conectado());assertEquals(a.vista().id(),b.vista().id());assertEquals(Dificultad.DIFICIL,a.vista().dificultad());assertEquals(a.vista().dificultad(),b.vista().dificultad());a.accion(0,"LISTO",0);b.accion(1,"LISTO",0);esperar(()->a.vista().fase()==Fase.INTRO&&b.vista().fase()==Fase.INTRO);
                for(int i=0;i<9;i++){final int linea=i+1;a.accion(0,"CONTINUAR",0);b.accion(1,"CONTINUAR",0);esperar(()->motor.vista().linea()>=linea||motor.vista().fase()==Fase.OFICINA);}
                esperar(()->a.vista().fase()==Fase.OFICINA);float x=motor.vista().jugadores()[0].x(),xb=motor.vista().jugadores()[1].x();a.movimiento(1,-1,0,false);a.movimiento(0,1,0,false);b.movimiento(1,-1,0,false);esperar(()->motor.vista().jugadores()[0].x()>x+10&&motor.vista().jugadores()[1].x()<xb-10);
                a.movimiento(0,0,0,false);b.movimiento(1,0,0,false);
                assertThrows(IOException.class,()->new ClienteBatalla("127.0.0.1",servidor.puerto(),0,"Intruso"));
                b.close();esperar(()->motor.vista().pausa());long tick=motor.vista().tick();Thread.sleep(70);assertEquals(tick,motor.vista().tick());a.accion(0,"REANUDAR",0);Thread.sleep(80);assertTrue(motor.vista().pausa());
                try(var vuelta=new ClienteBatalla("127.0.0.1",servidor.puerto(),1,"Periodista")){esperar(()->motor.vista().jugadores()[1].conectado());assertEquals(a.vista().id(),vuelta.vista().id());a.accion(0,"REANUDAR",0);esperar(()->motor.vista().tick()>tick);a.accion(0,"GUARDAR",0);esperar(guardado::existe);assertEquals(motor.vista().id(),guardado.cargar().vista().id());}
            }finally{b.close();}
        }
    }
    @Test void pausaSolicitadaPorClienteCongelaLaSalaYElGuardadoHastaReanudar()throws Exception{
        var motor=PatronesPasablesTest.iniciar(2026,12,Dificultad.NORMAL,false);var guardado=new GuardadoBatalla(tmp.resolve("pausa.bin"));
        try(var servidor=new ServidorBatalla(InetAddress.getLoopbackAddress(),0,motor,guardado);
            var a=new ClienteBatalla("127.0.0.1",servidor.puerto(),0,"Ciudadano");
            var b=new ClienteBatalla("127.0.0.1",servidor.puerto(),1,"Periodista")){
            esperar(()->motor.vista().jugadores()[1].conectado());a.accion(0,"REANUDAR",0);esperar(()->!motor.vista().pausa());
            b.accion(1,"PAUSA",0);esperar(()->a.vista().pausa()&&b.vista().pausa());var antes=motor.vista();
            a.movimiento(0,1,1,true);b.movimiento(1,-1,-1,true);a.accion(0,"ACCION",1);Thread.sleep(120);
            assertEquals(antes.tick(),motor.vista().tick());assertEquals(antes.obstaculos(),motor.vista().obstaculos());assertArrayEquals(antes.jugadores(),motor.vista().jugadores());
            a.accion(0,"GUARDAR",0);esperar(guardado::existe);var guardadoPausado=guardado.cargar().vista();assertTrue(guardadoPausado.pausa());assertEquals(antes.tick(),guardadoPausado.tick());
            a.movimiento(0,0,0,false);b.movimiento(1,0,0,false);a.accion(0,"REANUDAR",0);esperar(()->!a.vista().pausa()&&!b.vista().pausa()&&motor.vista().tick()>antes.tick());
            assertEquals(antes.jugadores()[0].x(),motor.vista().jugadores()[0].x());assertEquals(antes.jugadores()[1].x(),motor.vista().jugadores()[1].x());
        }
    }
    @Test void ambosClientesRecibenElMismoRastreadorYLaMismaRutaFijada() throws Exception {
        var motor=RastreadorBatallaTest.hastaElAviso(42,Dificultad.NORMAL,false);
        var ritmo=PaqueteRastreador.para(Dificultad.NORMAL);
        motor.orden(1,0,0,false,"",0);
        do{motor.paso();}
        while(ritmo.fase(RastreadorBatallaTest.rastreador(motor.vista()))!=PaqueteRastreador.Fase.FIJADO);
        var fijado=RastreadorBatallaTest.rastreador(motor.vista());
        try(var servidor=new ServidorBatalla(InetAddress.getLoopbackAddress(),0,motor,null);
            var a=new ClienteBatalla("127.0.0.1",servidor.puerto(),0,"Ciudadano");
            var b=new ClienteBatalla("127.0.0.1",servidor.puerto(),1,"Periodista")){
            assertTrue(a.vista().pausa()&&b.vista().pausa());
            assertEquals(1,fijado.objetivo());
            assertEquals(fijado,RastreadorBatallaTest.rastreador(a.vista()));
            assertEquals(fijado,RastreadorBatallaTest.rastreador(b.vista()));
        }
    }

    @Test void protocoloSoloPermiteMisionHabilitadaYNoAdmiteElRolDelOtro()throws Exception{
        var motor=new MotorBatalla(2,false);try(var servidor=new ServidorBatalla(InetAddress.getLoopbackAddress(),0,motor,null)){
            try(var s=new Socket("127.0.0.1",servidor.puerto());var out=new DataOutputStream(s.getOutputStream());var in=new DataInputStream(s.getInputStream())){out.writeInt(0x43495635);out.writeInt(alcaldedigital.compartido.batalla.EstadoBatalla.VERSION);out.writeInt(3);out.writeInt(0);out.writeUTF("Otro");out.flush();assertFalse(in.readBoolean());}
            try(var a=new ClienteBatalla("127.0.0.1",servidor.puerto(),0,"Ciudadano")){a.accion(1,"LISTO",0);Thread.sleep(80);assertFalse(motor.vista().jugadores()[1].listo());assertEquals(0,motor.vista().jugadores()[0].puntos());}
        }
    }
}
