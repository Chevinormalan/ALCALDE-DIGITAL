package alcaldedigital.cliente.entrada;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MapaControlesTest {
    @Test void nombresDelMandoYPersonajesCambianSinAlterarLasAcciones(){
        var xbox=new MapaControles.Contexto(6,true,"Juan","Influencer","Xbox Controller");
        var sony=new MapaControles.Contexto(6,true,"Juan","Influencer","DualSense Wireless Controller");
        assertFalse(xbox.playStation());assertTrue(sony.playStation());
        assertFalse(new MapaControles.Contexto(6,false,"Juan","","Xbox Wireless Controller").playStation());
        assertEquals("X / RT",MapaControles.etiquetaBoton(xbox,"X / RT"));
        assertEquals("Cuadrado / R2",MapaControles.etiquetaBoton(sony,"X / RT"));
        assertEquals("STICK / CRUCETA",MapaControles.etiquetaBoton(sony,"STICK / CRUCETA"));
        assertEquals("Influencer",sony.jugador2());
        assertTrue(MapaControles.teclado2(sony).stream().anyMatch(t->t.tecla().equals("MAYÚS")&&t.accion().equals("Impulso")));
    }
}
