package alcaldedigital.cliente.mision8;

import java.awt.event.KeyEvent;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ControlesOchoTest {
    @Test void lasAccionesDeCuatroPuestosNoCompartenTeclasYConfirmarNoAtaca() {
        assertEquals(MotorOcho.TRES,MisionOchoVista.mascaraTeclado(0,Set.of(KeyEvent.VK_C)));
        assertEquals(0,MisionOchoVista.mascaraTeclado(3,Set.of(KeyEvent.VK_C)));
        assertEquals(MotorOcho.TRES,MisionOchoVista.mascaraTeclado(1,Set.of(KeyEvent.VK_O)));
        assertEquals(0,MisionOchoVista.mascaraTeclado(0,Set.of(KeyEvent.VK_O)));
        assertEquals(1<<2,MisionOchoVista.mascaraTeclado(3,Set.of(KeyEvent.VK_3)));
        assertEquals(0,MisionOchoVista.mascaraTeclado(2,Set.of(KeyEvent.VK_3)));
        assertEquals(MotorOcho.UNO,MisionOchoVista.mascaraTeclado(2,Set.of(KeyEvent.VK_NUMPAD1)));
        assertEquals(0,MisionOchoVista.mascaraTeclado(3,Set.of(KeyEvent.VK_NUMPAD1)));
        for(int jugador=0;jugador<4;jugador++)
            assertEquals(0,MisionOchoVista.mascaraTeclado(jugador,Set.of(KeyEvent.VK_E,KeyEvent.VK_ENTER)));
    }
}
