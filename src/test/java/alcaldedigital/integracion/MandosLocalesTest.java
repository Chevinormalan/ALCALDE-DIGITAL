package alcaldedigital.integracion;

import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.mision5.MisionCincoVista;
import alcaldedigital.cliente.mision7.MisionSieteVista;
import alcaldedigital.cliente.mision8.*;
import alcaldedigital.cliente.misiones.VistaCampana;
import alcaldedigital.compartido.batalla.EstadoBatalla;
import alcaldedigital.compartido.carrera.EstadoCarrera;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.pruebas.PerfilAislado;
import alcaldedigital.servidor.sesion.SesionLocal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;

import java.util.Set;

import static alcaldedigital.cliente.entrada.EstadoMando.Boton.*;
import static java.awt.event.KeyEvent.VK_2;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(PerfilAislado.class)
class MandosLocalesTest {
    private static EstadoMando mando(float x, float y, EstadoMando.Boton... botones) {
        return new EstadoMando("Simulado",x,y,0,0,Set.of(botones));
    }
    private static final EstadoMando NEUTRO = mando(0,0);
    private static final float PASO = 1f/60;

    @Test void misionCincoMueveCadaCorazonConSuPropioMando() {
        String propiedad="ad.mision5.vista";
        System.setProperty(propiedad,"coop");
        try(var vista=new MisionCincoVista(1,EstadoCiudad.inicial(),false,false)){
            vista.iniciarEn(null);
            var antes=vista.vistaResultado();
            assertEquals(EstadoBatalla.Fase.ESQUIVA,antes.fase());
            vista.mando(NEUTRO);vista.segundoMando(NEUTRO);
            for(int i=0;i<20;i++){
                vista.mando(mando(-1,0));vista.segundoMando(mando(1,0));vista.actualizar(PASO);
            }
            assertTrue(vista.vistaResultado().jugadores()[0].x()<antes.jugadores()[0].x());
            assertTrue(vista.vistaResultado().jugadores()[1].x()>antes.jugadores()[1].x());
        }finally{System.clearProperty(propiedad);}
    }

    @Test void misionSeisElSegundoMandoMueveYDisparaSoloConLaNaveDelInfluencer(){
        var juego=new JuegoCliente(SesionLocal::new);
        String revision="ad.mision46.revision";
        System.setProperty(revision,"true");
        try(var vista=new VistaCampana(6,juego)){
            vista.iniciarEn(null);
            vista.prepararRevision("MODO");
            vista.pulsar('2',VK_2);vista.soltar(VK_2);
            vista.prepararRevision("ENTRENAMIENTO");
            var antes=vista.estado();
            vista.segundoMando(NEUTRO);
            for(int i=0;i<20;i++){
                vista.segundoMando(mando(1,0,X));vista.actualizar(PASO);
            }
            var despues=vista.estado();
            assertEquals(antes.getProperty("n0.x"),despues.getProperty("n0.x"));
            assertTrue(Float.parseFloat(despues.getProperty("n1.x"))>Float.parseFloat(antes.getProperty("n1.x")));
            assertEquals("1",despues.getProperty("p0.dueno"));
        }finally{System.clearProperty(revision);}
    }

    @Test void misionSieteElSegundoMandoCambiaSoloElCarrilDelInfluencer(){
        String propiedad="ad.mision7.vista";
        System.setProperty(propiedad,"dos");
        try(var vista=new MisionSieteVista(1,EstadoCiudad.inicial(),false,false)){
            vista.iniciarEn(null);
            var antes=vista.vistaResultado();
            assertEquals(EstadoCarrera.Fase.CARRERA,antes.fase());
            vista.segundoMando(NEUTRO);
            for(int i=0;i<12;i++){
                vista.segundoMando(mando(0,1,RB));vista.actualizar(PASO);
            }
            assertEquals(antes.corredores()[0].y(),vista.vistaResultado().corredores()[0].y());
            assertTrue(vista.vistaResultado().corredores()[1].y()>antes.corredores()[1].y());
        }finally{System.clearProperty(propiedad);}
    }

    @Test void misionOchoUsaLosTresHeroesYElCuartoMandoActivaElAtaqueDelJefe()throws Exception{
        var juego=new JuegoCliente(SesionLocal::new);
        juego.progreso.desbloquearTodas();
        var p=new PApplet();
        var g=new PGraphicsJava2D();
        g.setParent(p);g.setPrimary(false);g.setSize(1280,720);
        p.g=g;p.width=1280;p.height=720;
        try(var vista=new MisionOchoVista(juego)){
            vista.iniciarEn(p);
            g.beginDraw();
            try{vista.dibujar();}finally{g.endDraw();}
            vista.raton(940,258);
            vista.raton(230,620);
            var motor=vista.motor();
            motor.escena=MotorOcho.Escena.COMBATE;
            assertTrue(vista.listaComun());
            motor.ataqueTicks=-1;
            double x0=motor.heroes[0].x,x1=motor.heroes[1].x,x2=motor.heroes[2].x;
            vista.mando(NEUTRO);
            for(int i=1;i<4;i++)vista.mandoLocal(i,NEUTRO);
            vista.mandoLocal(1,mando(-1,0));vista.mandoLocal(2,mando(1,0));vista.mandoLocal(3,mando(0,0,RT));
            vista.actualizar(PASO*2);
            assertEquals(x0,motor.heroes[0].x);
            assertTrue(motor.heroes[1].x<x1);
            assertTrue(motor.heroes[2].x>x2);
            assertEquals(MotorOcho.Ataque.ORBES,motor.ataque);
        }
    }
}
