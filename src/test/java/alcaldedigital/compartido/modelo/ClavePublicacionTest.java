 package alcaldedigital.compartido.modelo;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ClavePublicacionTest {
    @Test
    void desempataElMismoRiesgoPorId() {
        ClavePublicacion primera = new ClavePublicacion(50, 7);
        ClavePublicacion segunda = new ClavePublicacion(50, 9);
        assertTrue(primera.compareTo(segunda) < 0);
    }
}

