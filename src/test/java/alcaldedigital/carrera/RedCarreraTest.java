package alcaldedigital.carrera;

import alcaldedigital.cliente.mision7.ClienteCarrera;
import alcaldedigital.compartido.carrera.*;
import alcaldedigital.compartido.carrera.EstadoCarrera.*;
import alcaldedigital.servidor.carrera.*;
import java.net.*;import java.io.*;import java.nio.file.*;import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RedCarreraTest {
    @TempDir Path tmp;
    private static void esperar(BooleanSupplier condicion)throws Exception{long fin=System.nanoTime()+4_000_000_000L;while(!condicion.getAsBoolean()&&System.nanoTime()<fin)Thread.sleep(10);assertTrue(condicion.getAsBoolean(),"No llegó el estado de la sala de carrera");}
    @Test void dosClientesControlanSoloSuRolCompartenPausaYSeReconectan()throws Exception{
        var m=MotorCarreraTest.nuevo(3,Dificultad.DIFICIL);MotorCarreraTest.empezar(m);var g=new GuardadoCarrera(tmp.resolve("lan.bin"));
        try(var servidor=new ServidorCarrera(InetAddress.getLoopbackAddress(),0,m,g,null);var a=new ClienteCarrera("127.0.0.1",servidor.puerto(),0)){
            var b=new ClienteCarrera("127.0.0.1",servidor.puerto(),1);
            try{esperar(()->a.vista().corredores()[1].conectado());assertEquals(a.vista().id(),b.vista().id());assertEquals(Dificultad.DIFICIL,a.vista().dificultad());a.accion(0,"REANUDAR",0);esperar(()->!m.vista().pausa());
                float y0=m.vista().corredores()[0].y(),y1=m.vista().corredores()[1].y();a.movimiento(1,1,true);a.movimiento(0,-1,true);b.movimiento(1,1,true);esperar(()->m.vista().corredores()[0].y()<y0-10&&m.vista().corredores()[1].y()>y1+10);
                a.movimiento(0,0,false);b.movimiento(1,0,false);b.accion(1,"PAUSA",0);esperar(()->a.vista().pausa()&&b.vista().pausa());var antes=m.vista();Thread.sleep(100);assertArrayEquals(antes.corredores(),m.vista().corredores());assertEquals(antes.tick(),m.vista().tick());
                a.accion(1,"REANUDAR",0);Thread.sleep(60);assertTrue(m.vista().pausa());assertThrows(IOException.class,()->new ClienteCarrera("127.0.0.1",servidor.puerto(),0));
                b.close();esperar(()->!m.vista().corredores()[1].conectado());a.accion(0,"REANUDAR",0);Thread.sleep(70);assertTrue(m.vista().pausa());
                try(var vuelta=new ClienteCarrera("127.0.0.1",servidor.puerto(),1)){esperar(()->m.vista().corredores()[1].conectado());a.accion(0,"REANUDAR",0);esperar(()->!m.vista().pausa());a.accion(0,"GUARDAR",0);esperar(g::existe);assertEquals(m.vista().id(),g.cargar().vista().id());}
            }finally{b.close();}
        }
    }
    @Test void protocoloIncompatibleSeRechazaYNoCompartePuertoConMision5()throws Exception{
        assertNotEquals(alcaldedigital.compartido.batalla.EstadoBatalla.PUERTO,EstadoCarrera.PUERTO);
        try(var servidor=new ServidorCarrera(InetAddress.getLoopbackAddress(),0,MotorCarreraTest.nuevo(3,Dificultad.NORMAL),null,null);
            var socket=new Socket("127.0.0.1",servidor.puerto());var out=new DataOutputStream(socket.getOutputStream());var in=new DataInputStream(socket.getInputStream())){
            out.writeInt(0);out.writeInt(1);out.writeInt(0);out.flush();assertEquals(-1,in.read());
        }
    }
    @Test void cerrarServidorTrasPactoConservaLaCorrupcionEnCampanaYCheckpoint()throws Exception{
        var m=MotorCarreraTest.conEvento(Noticia.PACTO,0);var g=new GuardadoCarrera(tmp.resolve("pacto.bin"));var repo=new RepositorioCarrera(tmp.resolve("campana.properties"));
        try(var s=new ServidorCarrera(InetAddress.getLoopbackAddress(),0,m,g,repo);var a=new ClienteCarrera("127.0.0.1",s.puerto(),0);var b=new ClienteCarrera("127.0.0.1",s.puerto(),1)){
            a.accion(0,"REANUDAR",0);esperar(()->!m.vista().pausa());a.accion(0,"ACCION",1);esperar(()->m.vista().corredores()[0].evento().advertencia());a.accion(0,"ACCION",3);esperar(()->m.vista().electoral().juanCorrupto());
        }
        assertTrue(g.cargar().vista().electoral().juanCorrupto());assertTrue(repo.electoral(alcaldedigital.compartido.modelo.EstadoCiudad.inicial()).juanCorrupto());
    }
}
