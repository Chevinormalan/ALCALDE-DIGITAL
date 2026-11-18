package alcaldedigital.integracion;

import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.ClienteSketch;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.cliente.mision3.MisionTresSketch;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MisionTresMenuTest {
    @Test void seleccionaMisionTresYRegresaConResultadoCompartido() {
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        var render=new RenderJuego();
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");
        render.dibujar(juego);
        var boton=render.botones().stream().filter(b->b.accion().equals("mision3"))
            .findFirst().orElseThrow();
        render.pulsar(juego,boton.area().x+20,boton.area().y+20);
        assertEquals(JuegoCliente.Pantalla.MISION_TRES,juego.pantalla);
        juego.terminarMisionTres(new MisionTresSketch.Resultado(true,100,0,23));
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        assertEquals(100,juego.estadoCampana.confianza());
        assertEquals(12,juego.estadoCampana.desinformacion());
        assertDoesNotThrow(()->render.dibujar(juego));
    }

    @Test void escYBotonDePausaRegresanAlMenuPrincipal() {
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        var sketch=new ClienteSketch(juego);sketch.width=960;sketch.height=600;
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(juego,3);
        sketch.keyCode=27;sketch.key=27;
        sketch.keyPressed();
        assertEquals(JuegoCliente.Pantalla.MENU,juego.pantalla);

        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(juego,3);juego.accion("pausa");
        sketch.mouseX=338;sketch.mouseY=442;
        sketch.mousePressed();
        assertEquals(JuegoCliente.Pantalla.MENU,juego.pantalla);
        assertFalse(juego.pausa);
    }
}
