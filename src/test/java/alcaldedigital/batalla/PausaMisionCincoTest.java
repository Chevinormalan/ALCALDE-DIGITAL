package alcaldedigital.batalla;

import alcaldedigital.cliente.mision5.*;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.servidor.batalla.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class PausaMisionCincoTest {
    @TempDir Path tmp;
    private String guardadosAntes;
    @BeforeEach void aislarGuardados(){guardadosAntes=System.getProperty("ad.guardados");System.setProperty("ad.guardados",tmp.toString());}
    @AfterEach void restaurarGuardados(){if(guardadosAntes==null)System.clearProperty("ad.guardados");else System.setProperty("ad.guardados",guardadosAntes);}
    private static Object campo(MisionCincoVista v,String nombre)throws Exception{var f=MisionCincoVista.class.getDeclaredField(nombre);f.setAccessible(true);return f.get(v);}
    private static void campo(MisionCincoVista v,String nombre,Object valor)throws Exception{var f=MisionCincoVista.class.getDeclaredField(nombre);f.setAccessible(true);f.set(v,valor);}
    private MisionCincoVista escena(Fase fase,int patron)throws Exception{
        var b=new MotorBatalla(2026,true).vista();var js=b.jugadores();js[0]=MotorBatallaTest.jugador(js[0],800,530,5,0);
        var motor=MotorBatalla.restaurar(MotorBatallaTest.escena(b,fase,patron,20,js,List.of(new Obstaculo(400,400,20,20,2,0,0,10,0,300,-1))));
        var vista=new MisionCincoVista(2026,EstadoCiudad.inicial(),false,false);
        campo(vista,"canal",new CanalLocal(motor,new GuardadoBatalla(tmp.resolve("partida.bin"))));campo(vista,"v",motor.vista());campo(vista,"configurando",false);
        return vista;
    }
    private static void tecla(MisionCincoVista v,int k){v.pulsar(' ',k);v.soltar(k);}
    private static void avanzar(MisionCincoVista v,int n){for(int i=0;i<n;i++)v.actualizar(1f/60);}
    @Test void escPausaSinSalirYLaRepeticionDeTeclaNoReanuda()throws Exception{
        try(var v=escena(Fase.ESQUIVA,6)){
            v.pulsar(' ',27);assertTrue(v.vistaResultado().pausa());var antes=v.vistaResultado();
            for(int i=0;i<30;i++){v.pulsar(' ',27);v.actualizar(1f/60);}
            assertFalse(v.salir());assertEquals(antes.tick(),v.vistaResultado().tick());assertEquals(antes.obstaculos(),v.vistaResultado().obstaculos());
            v.soltar(27);tecla(v,27);avanzar(v,1);assertFalse(v.vistaResultado().pausa());assertTrue(v.vistaResultado().tick()>antes.tick());
        }
    }
    @Test void mantenerMovimientoDurantePausaNoArrastraAlPersonajeAlReanudar()throws Exception{
        try(var v=escena(Fase.OFICINA,0)){
            v.pulsar('d',68);avanzar(v,3);float x=v.vistaResultado().jugadores()[0].x();tecla(v,80);avanzar(v,60);assertEquals(x,v.vistaResultado().jugadores()[0].x());
            tecla(v,80);avanzar(v,3);assertEquals(x,v.vistaResultado().jugadores()[0].x());
            v.pulsar('d',68);avanzar(v,1);assertEquals(x,v.vistaResultado().jugadores()[0].x());
            v.soltar(68);v.pulsar('d',68);avanzar(v,1);assertTrue(v.vistaResultado().jugadores()[0].x()>x);
        }
    }
    @Test void ayudaDelMenuIncluyeLasCincoRondasYElFinalSinMoverLaPartida()throws Exception{
        try(var v=escena(Fase.ESQUIVA,9)){
            tecla(v,80);v.raton(500,305);assertEquals("AYUDA",campo(v,"panelPausa").toString());assertEquals(4,campo(v,"paginaAyuda"));
            var antes=v.vistaResultado();for(int i=0;i<7;i++){v.raton(120+i*152,175);assertEquals(i,campo(v,"paginaAyuda"));avanzar(v,30);}
            assertEquals(antes.tick(),v.vistaResultado().tick());assertEquals(antes.jugadores()[0],v.vistaResultado().jugadores()[0]);
            tecla(v,39);assertEquals(0,campo(v,"paginaAyuda"));tecla(v,37);assertEquals(6,campo(v,"paginaAyuda"));
            v.raton(500,630);assertEquals("MENU",campo(v,"panelPausa").toString());assertTrue(v.vistaResultado().pausa());
            v.raton(500,245);assertFalse(v.vistaResultado().pausa());
        }
    }
    @Test void pausaConsumeClicsYTeclasYNoActivaResultadoNiDecisionesDeFondo()throws Exception{
        for(var fase:new Fase[]{Fase.TURNO,Fase.DECISION,Fase.FINAL})try(var v=escena(fase,14)){
            tecla(v,80);v.raton(260,570);v.raton(180,380);tecla(v,49);assertFalse(v.salir());assertFalse(v.vistaResultado().jugadores()[0].accionLista());
            tecla(v,72);v.raton(260,570);tecla(v,10);assertTrue(v.vistaResultado().pausa());assertFalse(v.salir());
            assertEquals("MENU",campo(v,"panelPausa").toString());
        }
    }
    @Test void tecladoNavegaAyudaControlesEstructurasYGuardadoSinTrampas()throws Exception{
        try(var v=escena(Fase.TURNO,5)){
            tecla(v,27);tecla(v,40);tecla(v,10);assertEquals("AYUDA",campo(v,"panelPausa").toString());assertEquals(2,campo(v,"paginaAyuda"));
            tecla(v,27);assertTrue(v.vistaResultado().pausa());tecla(v,40);tecla(v,10);assertEquals(0,campo(v,"paginaAyuda"));
            tecla(v,27);tecla(v,84);assertEquals("ESTRUCTURAS",campo(v,"panelPausa").toString());v.raton(300,625);assertEquals("MENU",campo(v,"panelPausa").toString());
            tecla(v,71);assertTrue(Files.exists(tmp.resolve("partida.bin")));assertTrue(new GuardadoBatalla(tmp.resolve("partida.bin")).cargar().vista().pausa());
            tecla(v,80);assertFalse(v.vistaResultado().pausa());assertEquals(Fase.TURNO,v.vistaResultado().fase());assertFalse(v.vistaResultado().jugadores()[0].accionLista());
        }
    }
    private static EstadoMando mando(float x,EstadoMando.Boton... botones){return new EstadoMando("Prueba",x,0,0,0,Set.of(botones));}
    private static void boton(MisionCincoVista v,EstadoMando.Boton b){v.mando(mando(0,b));v.mando(mando(0));}
    @Test void mandoPuedeVolverDeLaAyudaYDebeCentrarseParaReanudarMovimiento()throws Exception{
        try(var v=escena(Fase.OFICINA,0)){
            v.mando(mando(1));avanzar(v,2);boton(v,EstadoMando.Boton.MENU);boton(v,EstadoMando.Boton.ABAJO);boton(v,EstadoMando.Boton.A);
            assertEquals("AYUDA",campo(v,"panelPausa").toString());boton(v,EstadoMando.Boton.B);assertEquals("MENU",campo(v,"panelPausa").toString());
            float x=v.vistaResultado().jugadores()[0].x();v.mando(mando(1,EstadoMando.Boton.MENU));avanzar(v,2);assertEquals(x,v.vistaResultado().jugadores()[0].x());
            v.mando(mando(0));avanzar(v,1);v.mando(mando(1));avanzar(v,1);assertTrue(v.vistaResultado().jugadores()[0].x()>x);
        }
    }
    @Test void perderFocoPausaUnaSolaVezYNoCierraLaAyudaAbierta()throws Exception{
        try(var v=escena(Fase.ESQUIVA,12)){
            tecla(v,72);v.perderFoco();v.perderFoco();assertEquals("AYUDA",campo(v,"panelPausa").toString());var antes=v.vistaResultado();avanzar(v,120);
            assertEquals(antes.tick(),v.vistaResultado().tick());assertEquals(antes.jugadores()[0].vidas(),v.vistaResultado().jugadores()[0].vidas());
            tecla(v,27);tecla(v,27);assertFalse(v.vistaResultado().pausa());assertFalse(v.salir());
        }
    }
    @Test void motorNoAcumulaOrdenesMientrasEstaPausadoNiModificaLaVelocidadDelSalto()throws Exception{
        var m=PatronesPasablesTest.iniciar(0,3,Dificultad.NORMAL,true);m.orden(0,0,1,false,"",0);for(int i=0;i<25;i++)m.paso();m.orden(0,0,0,true,"",0);m.paso();var antes=m.vista().jugadores()[0];
        m.orden(0,1,1,true,"PAUSA",0);for(int i=0;i<100;i++){m.orden(0,1,1,true,"",0);m.paso();}
        assertEquals(antes.vy(),m.vista().jugadores()[0].vy());assertEquals(antes.x(),m.vista().jugadores()[0].x());
        m.orden(0,1,1,true,"REANUDAR",0);m.paso();assertEquals(antes.x(),m.vista().jugadores()[0].x());
    }
}
