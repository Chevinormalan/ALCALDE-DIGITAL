package alcaldedigital.cliente.tutorial;

import alcaldedigital.cliente.ClienteSketch;
import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.entrada.EstadoMando.Boton;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.compartido.guardado.ArchivosPartida;
import alcaldedigital.compartido.guardado.GuardadoCampana;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static alcaldedigital.cliente.tutorial.EjercicioTutorial.*;
import static java.awt.event.KeyEvent.*;
import static org.junit.jupiter.api.Assertions.*;

class TutorialTest {
    @TempDir Path temporal;

    @Test void realizaLasOchoPracticasYRegresaAlSelectorDesdeUnPerfilNuevo() throws Exception {
        var juego = juego();
        juego.accion("jugar");
        assertEquals(JuegoCliente.Pantalla.TUTORIAL, juego.pantalla);
        assertEquals(0, juego.numeroMision());
        var t = juego.tutorial();
        var render = new RenderJuego();
        terminar(juego, render);
        assertEquals(JuegoCliente.Pantalla.MISIONES, juego.pantalla);
        assertEquals("completado", ArchivosPartida.leer(archivo()).getProperty("estado"));
        assertTrue(juego.progreso.disponible(1));
        assertFalse(juego.progreso.disponible(2));
        assertFalse(GuardadoCampana.existe(1));
        var iniciado = juego();
        iniciado.accion("jugar");
        assertEquals(JuegoCliente.Pantalla.MISIONES, iniciado.pantalla);
        assertEquals(Tutorial.Resultado.COMPLETADO,t.resultado());
    }

    @Test void omisionRequiereUnaSolaPulsacionContinuaDeUnSegundoYMedio() throws Exception {
        var juego = juego();
        juego.accion("jugar");
        var cliente = new ClienteSketch(juego);
        cliente.keyCode=VK_ESCAPE;cliente.key=27;
        cliente.keyPressed();
        tiempo(juego,90);
        assertTrue(juego.tutorial().progresoOmision()>.5);
        cliente.keyReleased();
        tiempo(juego,80);
        assertEquals(0,juego.tutorial().progresoOmision());
        assertFalse(Files.exists(archivo()));
        cliente.keyPressed();
        tiempo(juego,149);
        assertEquals(JuegoCliente.Pantalla.TUTORIAL,juego.pantalla);
        tiempo(juego,1);
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        cliente.keyPressed();
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        assertEquals("omitido",ArchivosPartida.leer(archivo()).getProperty("estado"));
    }

    @Test void soltarElClicSalirDelBotonOPerderFocoCancelaElContador() {
        var t = new Tutorial();
        var boton = Tutorial.botonOmitir();
        int x = (int) boton.getCenterX(), y = (int) boton.getCenterY();
        t.pulsarRaton(x,y);t.actualizar(.1);t.soltarRaton();
        assertEquals(0,t.progresoOmision());
        t.pulsarRaton(x,y);t.actualizar(.1);t.apuntar(5,5);
        assertEquals(0,t.progresoOmision());
        t.pulsarRaton(x,y);t.actualizar(.1);t.perderFoco();
        assertEquals(0,t.progresoOmision());
        t.pulsarRaton(x,y);
        for(int i=0;i<15;i++)t.actualizar(.1);
        assertEquals(Tutorial.Resultado.OMITIDO,t.resultado());
    }

    @Test void mandoPlayStationSePuedeReconectarSinPerderElEjercicioYLimpiaLaOmision() {
        var t = new Tutorial();
        var neutro = mando();
        t.mando(mando(Boton.A));
        t.actualizar(.1);
        assertEquals(INICIO,t.ejercicio());
        t.mando(neutro);
        assertEquals("Cruz",INICIO.indicacion(t.dispositivo()));
        assertEquals("Círculo o clic",t.omitirCon());
        t.mando(mando(Boton.A));
        for(int i=0;i<6;i++)t.actualizar(.1);
        assertEquals(ACERCARSE,t.ejercicio());
        t.mando(mando(Boton.B));
        for(int i=0;i<12;i++)t.actualizar(.1);
        t.mando(EstadoMando.desconectado());
        assertEquals(ACERCARSE,t.ejercicio());
        assertEquals("Esc o clic",t.omitirCon());
        assertEquals(0,t.progresoOmision());
        t.mando(mando(Boton.B));
        for(int i=0;i<17;i++)t.actualizar(.1);
        assertNull(t.resultado());
        t.mando(neutro);t.mando(mando(Boton.B));
        for(int i=0;i<15;i++)t.actualizar(.1);
        assertEquals(Tutorial.Resultado.OMITIDO,t.resultado());
    }

    @Test void lasOchoPracticasSePuedenCompletarSoloConMando() {
        var juego=juego();juego.accion("jugar");
        var t=juego.tutorial();t.mando(mando());
        for(EjercicioTutorial e:EjercicioTutorial.values()){
            assertEquals(e,t.ejercicio());
            switch(e){
                case ACERCARSE -> mantenerMando(juego,140,1,0);
                case CORRER -> mantenerMando(juego,100,1,0,Boton.RB);
                case SALTO_TERMINAL,SALTO_PULSO -> {
                    mantenerMando(juego,45,1,0);
                    mantenerMando(juego,45,1,0,Boton.X);
                }
                case CONTRASTAR,SERVIDOR,NOTICIA -> {
                    mantenerMando(juego,1,0,0,Boton.IZQUIERDA);
                    mantenerMando(juego,1,0,0,Boton.A);
                }
                case PILOTAR -> {mantenerMando(juego,48,0,-1);mantenerMando(juego,150,1,0);}
                case ACELERAR -> mantenerMando(juego,120,0,0,Boton.RB);
                case CARRIL -> {mantenerMando(juego,50,0,-1);tiempo(juego,150);}
                case LATERAL -> mantenerMando(juego,115,1,0);
                case DOBLE_SALTO -> {
                    mantenerMando(juego,14,0,0,Boton.A);
                    assertFalse(t.acierto());
                    mantenerMando(juego,1,0,0,Boton.A);
                }
                default -> mantenerMando(juego,1,0,0,e.control.botones().iterator().next());
            }
            tiempo(juego,60);
        }
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        assertEquals(Tutorial.Resultado.COMPLETADO,t.resultado());
    }

    @Test void losPerfilesAnterioresConPartidaAbrenElSelector() throws Exception {
        var m = new SesionMisionUnoLocal(2026,alcaldedigital.compartido.protocolo.SesionMisionUno.Dificultad.NORMAL);
        GuardadoCampana.guardar(1,m.capturar());
        var juego=juego();juego.accion("jugar");
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        assertFalse(Files.exists(archivo()));
    }

    @Test void restablecerDevuelveAlMenuYLaSiguienteVezMuestraElTutorial() throws Exception {
        String anterior=System.getProperty("ad.guardados");
        Path perfil=temporal.resolve("perfil");
        System.setProperty("ad.guardados",perfil.toString());
        try {
            var juego=juego();juego.accion("jugar");juego.tutorial().pulsar(VK_ESCAPE);tiempo(juego,150);
            assertTrue(Files.exists(archivo()));
            juego.accion("pedirRestablecer");juego.accion("confirmarRestablecer");
            assertEquals(JuegoCliente.Pantalla.MENU,juego.pantalla);
            assertFalse(Files.exists(archivo()));
            try(var archivos=Files.list(temporal)){
                assertTrue(archivos.anyMatch(p->p.getFileName().toString().startsWith("perfil-respaldo-")&& Files.isRegularFile(p.resolve("tutorial.properties"))));
            }
            juego.accion("jugar");
            assertEquals(JuegoCliente.Pantalla.TUTORIAL,juego.pantalla);
            assertEquals(INICIO,juego.tutorial().ejercicio());
        } finally {
            if(anterior==null)System.clearProperty("ad.guardados");else System.setProperty("ad.guardados",anterior);
        }
    }

    private static JuegoCliente juego() { return new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new); }
    private static Path archivo() { return ArchivosPartida.directorio().resolve("tutorial.properties"); }
    private static EstadoMando mando(Boton... botones) {return new EstadoMando("DualSense Wireless Controller",0,0,0,0,Set.of(botones));}

    static void terminar(JuegoCliente juego,RenderJuego render) {
        var t=juego.tutorial();
        for(EjercicioTutorial e:EjercicioTutorial.values()){
            assertEquals(e,t.ejercicio());
            render.dibujar(juego);
            ejecutar(juego,e);
            tiempo(juego,60);
        }
    }

    static void ejecutar(JuegoCliente juego,EjercicioTutorial e) {
        var t=juego.tutorial();
        switch(e){
            case ACERCARSE -> mantener(juego,140,VK_D);
            case CORRER -> mantener(juego,100,VK_D,VK_SHIFT);
            case SALTO_TERMINAL,SALTO_PULSO -> {
                mantener(juego,45,VK_D);
                assertFalse(t.acierto());
                t.pulsar(VK_D);pulso(t,VK_SPACE);tiempo(juego,45);t.soltar(VK_D);
            }
            case CONTRASTAR -> {
                pulso(t,VK_ENTER);assertFalse(t.acierto());assertFalse(t.respuesta().isBlank());
                pulso(t,VK_LEFT);pulso(t,VK_ENTER);
            }
            case SERVIDOR,NOTICIA -> pulso(t,VK_1);
            case PILOTAR -> {mantener(juego,48,VK_W);mantener(juego,150,VK_D);}
            case ACELERAR -> mantener(juego,120,VK_SPACE);
            case CARRIL -> {mantener(juego,50,VK_W);tiempo(juego,150);}
            case LATERAL -> mantener(juego,115,VK_D);
            case DOBLE_SALTO -> {
                pulso(t,VK_SPACE);tiempo(juego,14);assertFalse(t.acierto());
                pulso(t,VK_SPACE);
            }
            default -> pulso(t,e.control.teclas().iterator().next());
        }
    }

    static void tiempo(JuegoCliente juego,int pasos) {for(int i=0;i<pasos;i++)juego.actualizar(.01);}
    private static void pulso(Tutorial t,int tecla) {t.pulsar(tecla);t.soltar(tecla);}
    private static void mantener(JuegoCliente juego,int pasos,int... teclas) {
        var t=juego.tutorial();for(int tecla:teclas)t.pulsar(tecla);tiempo(juego,pasos);for(int tecla:teclas)t.soltar(tecla);
    }
    private static void mantenerMando(JuegoCliente juego,int pasos,float x,float y,Boton... botones){
        var t=juego.tutorial();t.mando(new EstadoMando("DualSense Wireless Controller",x,y,0,0,Set.of(botones)));
        tiempo(juego,pasos);t.mando(mando());
    }
}
