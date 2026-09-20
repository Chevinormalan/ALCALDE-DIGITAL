package alcaldedigital.estructuras;
import alcaldedigital.compartido.estructuras.decisiones.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ArbolDecisionesTest {
    @Test void insercionBusquedaRecorridosYRuta() {
        var a=new ArbolDecisiones("r","Rumor");a.insertar("r","v","Verificar");a.insertar("r","i","Ignorar");a.insertar("v","c","Contexto");
        assertEquals(List.of("r","v","c","i"),a.recorrer("preorden"));
        assertEquals(List.of("c","v","i","r"),a.recorrer("postorden"));
        assertEquals(List.of("r","v","i","c"),a.recorrer("niveles"));
        assertEquals(List.of("r","v","c"),a.ruta("c"));assertNull(a.buscar("ausente"));
        a.ejecutar("c");assertTrue(a.buscar("v").ejecutado());assertFalse(a.buscar("i").ejecutado());
        assertTrue(a.podar("i"));assertFalse(a.podar("i"));
        assertThrows(IllegalArgumentException.class,()->a.podar("v"));
    }
    @Test void validaUnicidadPadresYRaiz() {
        var a=new ArbolDecisiones("r","Rumor");a.insertar("r","v","Verificar");
        assertThrows(IllegalArgumentException.class,()->a.insertar("v","r","Ciclo"));
        assertThrows(IllegalArgumentException.class,()->a.insertar("nada","x","Huérfano"));
        assertThrows(IllegalArgumentException.class,()->a.podar("r"));
        assertThrows(IllegalArgumentException.class,()->a.ejecutar("nada"));
    }
}
