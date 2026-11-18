package alcaldedigital.estructuras;

import alcaldedigital.compartido.estructuras.grafo.GrafoDifusionCivitas;
import alcaldedigital.compartido.estructuras.grafo.GrafoZonasMisionTres;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GrafosMisionTresTest {
    @Test void conservaRutaRealDelCentroComercial() {
        var grafo=new GrafoZonasMisionTres();
        assertEquals(List.of("atrio","pixel","corrupted","core"),grafo.ruta("atrio","core"));
        assertTrue(grafo.conectadas("pixel","corrupted"));
        assertFalse(grafo.conectadas("atrio","core"));
    }
    @Test void rumorSigueContactosDirigidosPorBfs() {
        var grafo=new GrafoDifusionCivitas();
        assertEquals(List.of(0,1,2,3,4,5,6),grafo.ordenDesde(0));
        assertEquals(List.of(2,4,6),grafo.ordenDesde(2));
    }
}
