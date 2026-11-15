package alcaldedigital.integracion;

import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MisionUnoInterfazTest {
    @Test void seleccionarMisionPermiteVolverYAplicarLasTresDificultades() {
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(juego,1);
        juego.accion("volver");
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        juego.tecla(27,'\u001b');
        assertEquals(JuegoCliente.Pantalla.MENU,juego.pantalla);
        for(var dificultad:SesionMisionUno.Dificultad.values()) {
            alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(juego,1);
            juego.accion("m1dificultad:"+dificultad.name());
            assertEquals(JuegoCliente.Pantalla.PARQUE,juego.pantalla);
            assertEquals(dificultad,juego.misionUno.vista().dificultad());
            assertEquals(dificultad.segundos(),juego.misionUno.vista().tiempo());
            juego.accion("menu");
        }
    }
    @Test void lasPantallasDeSofiDibujanUnaPartidaCompleta() {
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        var render=new RenderJuego();
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        assertEquals(1280,render.dibujar(juego).getWidth());
        assertTrue(render.botones().stream().anyMatch(b->b.accion().equals("mision1")));
        assertFalse(render.botones().stream().anyMatch(b->b.accion().startsWith("rol:")));
        alcaldedigital.pruebas.PartidaPrueba.elegir(juego,1);
        assertEquals(JuegoCliente.Pantalla.DIFICULTAD,juego.pantalla);
        assertDoesNotThrow(()->render.dibujar(juego));
        assertEquals(3,render.botones().stream().filter(b->b.accion().startsWith("m1dificultad:")).count());
        juego.accion("m1dificultad:NORMAL");
        assertEquals(JuegoCliente.Pantalla.PARQUE,juego.pantalla);
        assertEquals(30,juego.misionUno.vista().tiempo());
        assertEquals(1280,render.dibujar(juego).getWidth());
        juego.accion("arboles");
        assertEquals(JuegoCliente.Pantalla.ARBOLES,juego.pantalla);
        assertDoesNotThrow(()->render.dibujar(juego));
        juego.accion("volver");
        for(int i=0;i<15;i++) juego.actualizar(.1);
        for(int i=0;i<4;i++) juego.accion("continuar");
        for(int i=0;i<14;i++) juego.actualizar(.1);
        assertEquals(JuegoCliente.Pantalla.CIVITAS,juego.pantalla);
        assertDoesNotThrow(()->render.dibujar(juego));
        juego.accion("m1:verificar:1");juego.accion("m1:compartir:1");
        juego.accion("m1:verificar:2");juego.accion("m1:compartir:2");
        juego.accion("m1:ignorar:3");
        assertEquals(JuegoCliente.Pantalla.PARQUE,juego.pantalla);
        assertDoesNotThrow(()->render.dibujar(juego));
        for(int i=0;i<300;i++) juego.actualizar(.1);
        assertEquals(JuegoCliente.Pantalla.RESULTADO,juego.pantalla);
        assertDoesNotThrow(()->render.dibujar(juego));
    }
}
