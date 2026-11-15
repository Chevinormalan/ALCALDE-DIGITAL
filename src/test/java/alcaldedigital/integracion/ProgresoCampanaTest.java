package alcaldedigital.integracion;

import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.compartido.modelo.ProgresoCampana;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProgresoCampanaTest {
    @Test void acumulaResultadosSinSobrescribirIndicadoresAjenos() {
        EstadoCiudad inicial=EstadoCiudad.inicial();
        EstadoCiudad trasUno=ProgresoCampana.aplicarMisionUno(inicial,
            new EstadoCiudad(60,55,48,50,53,18,22));
        assertEquals(new EstadoCiudad(60,55,48,50,53,18,22),trasUno);
        EstadoCiudad trasTres=ProgresoCampana.aplicarMisionTres(trasUno,65,12);
        assertEquals(new EstadoCiudad(60,85,48,50,53,22,22),trasTres);
        assertEquals(trasTres,ProgresoCampana.aplicarMisionTres(trasTres,35,8));
    }
}
