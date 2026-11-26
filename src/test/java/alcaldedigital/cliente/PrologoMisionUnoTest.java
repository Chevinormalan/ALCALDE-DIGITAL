package alcaldedigital.cliente;

import alcaldedigital.cliente.JuegoCliente.Pantalla;
import alcaldedigital.compartido.protocolo.SesionMisionUno.Fase;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class PrologoMisionUnoTest {
    @Test void partidaNuevaSolicitaElPrologoYContinuarRecuperaElCheckpoint() throws Exception {
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        juego.abrir(Pantalla.MISIONES);
        juego.accion("mision1");
        assertFalse(juego.prologoPendiente());
        juego.accion("m1dificultad:FACIL");
        assertTrue(juego.prologoPendiente());
        assertTrue(juego.tomarPrologo());
        assertFalse(juego.tomarPrologo());
        assertEquals(Fase.INTRO,juego.misionUno.vista().fase());

        juego.guardarMisionUno();
        juego.abrir(Pantalla.MISIONES);
        juego.accion("mision1");
        assertEquals(Pantalla.GUARDADO,juego.pantalla);
        juego.accion("partida:continuar");
        assertEquals(Pantalla.PARQUE,juego.pantalla);
        assertFalse(juego.tomarPrologo());

        juego.abrir(Pantalla.MISIONES);
        juego.accion("mision1");
        juego.accion("partida:nueva");
        juego.accion("m1dificultad:NORMAL");
        assertTrue(juego.tomarPrologo());
    }
}
