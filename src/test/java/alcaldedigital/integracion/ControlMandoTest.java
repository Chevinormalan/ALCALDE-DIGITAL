package alcaldedigital.integracion;

import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.entrada.ControladorMando;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.compartido.modelo.Rol;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import static alcaldedigital.cliente.entrada.EstadoMando.Boton;
import static org.junit.jupiter.api.Assertions.*;

class ControlMandoTest {
    private final JuegoCliente juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
    private final RenderJuego render=new RenderJuego();
    private final ControladorMando mando=new ControladorMando();

    private void enviar(Boton... botones) {
        var estado=new EstadoMando("Mando de prueba",0,0,0,0,
            botones.length==0?EnumSet.noneOf(Boton.class):EnumSet.of(botones[0],botones));
        mando.actualizar(estado,juego,render,null,()->juego.abrir(JuegoCliente.Pantalla.MENU));
    }

    @Test void eligeLasTresMisionesYRequiereUnaPulsacionNueva() {
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);
        render.dibujar(juego);
        enviar(Boton.A);
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        render.dibujar(juego);
        enviar(Boton.A); // mantener A no selecciona accidentalmente una misión
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        enviar();enviar(Boton.A);
        assertEquals(JuegoCliente.Pantalla.DIFICULTAD,juego.pantalla);
        render.dibujar(juego);
        assertEquals(2,juego.foco); // Normal: el primer botón es Volver.
        enviar();enviar(Boton.A);
        assertEquals(JuegoCliente.Pantalla.PARQUE,juego.pantalla);
        assertEquals(30,juego.misionUno.vista().tiempo());

        juego.abrir(JuegoCliente.Pantalla.MISIONES);
        render.dibujar(juego);
        enviar();enviar(Boton.DERECHA);
        assertEquals(2,juego.foco);
        enviar();enviar(Boton.A);
        assertEquals(JuegoCliente.Pantalla.DIFICULTAD_DOS,juego.pantalla);
        render.dibujar(juego);
        enviar();enviar(Boton.A);
        assertEquals(JuegoCliente.Pantalla.MISION_DOS,juego.pantalla);
        juego.abrir(JuegoCliente.Pantalla.MISIONES);
        render.dibujar(juego);
        enviar();enviar(Boton.DERECHA);
        enviar();enviar(Boton.DERECHA);
        assertEquals(3,juego.foco);
        enviar();enviar(Boton.A);
        assertEquals(JuegoCliente.Pantalla.MISION_TRES,juego.pantalla);
    }

    @Test void pausaSeReanudaYSalirRequiereElegirLaOpcion() {
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(juego,1);juego.accion("m1dificultad:NORMAL");
        render.dibujar(juego);
        enviar(Boton.MENU);
        assertTrue(juego.pausa);
        render.dibujar(juego);
        double animacion=juego.animacion;
        juego.actualizar(1);
        assertEquals(animacion,juego.animacion);
        enviar();enviar(Boton.A);
        assertFalse(juego.pausa);
        assertEquals(JuegoCliente.Pantalla.PARQUE,juego.pantalla);
        enviar();enviar(Boton.MENU);
        render.dibujar(juego);
        enviar();enviar(Boton.ABAJO);
        assertEquals(1,juego.foco);
        enviar();enviar(Boton.A);
        assertTrue(juego.controlesPausa);
        render.dibujar(juego);
        enviar();enviar(Boton.ABAJO);
        assertEquals(2,juego.foco);
        enviar();enviar(Boton.A);
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
    }

    @Test void desconectarDuranteMisionPausaYElTecladoNumericoFunciona() {
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(juego,3);
        enviar();
        mando.actualizar(EstadoMando.desconectado(),juego,render,null,()->{});
        assertTrue(juego.pausa);
        juego.abrir(JuegoCliente.Pantalla.MENU);
        juego.iniciar(Rol.CIUDADANO);
        juego.accion("arboles");juego.accion("laboratorio");
        enviar();juego.accion("campoBusqueda");
        assertTrue(juego.tecladoNumericoActivo);
        render.dibujar(juego);
        enviar(Boton.A);
        assertEquals("7",juego.busqueda);
        enviar();enviar(Boton.X);
        assertEquals("",juego.busqueda);
        enviar();enviar(Boton.B);
        assertFalse(juego.tecladoNumericoActivo);
    }

    @Test void laPausaDeMisionTresSaleAlMenuConMando() {
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(juego,3);
        enviar(Boton.MENU);
        assertTrue(juego.pausa);
        enviar();enviar(Boton.ABAJO);
        enviar();enviar(Boton.ABAJO);
        assertEquals(2,juego.foco);
        enviar();enviar(Boton.A);
        assertEquals(JuegoCliente.Pantalla.MENU,juego.pantalla);
    }
}
