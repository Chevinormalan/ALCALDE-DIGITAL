package alcaldedigital.cliente.misiones;

import alcaldedigital.cliente.mision4.MisionCuatroSketch;
import alcaldedigital.cliente.mision6.MisionSeisMotor;
import alcaldedigital.compartido.guardado.TablaPuntajes;
import org.junit.jupiter.api.Test;
import java.awt.event.KeyEvent;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PuntajesMisionTest {
    @Test void derrotaTienePuntajePeroNoResultadoCanonicoYRepetirNoDuplica()throws Exception{
        var derrota=new TablaPuntajes.Fila(2,"intento-derrota",false,73,"NORMAL",
            List.of("Equipos seguros: 5/6"),List.of());
        TablaPuntajes.registrar(derrota);
        assertEquals(derrota,TablaPuntajes.ultimo(2));
        assertNull(TablaPuntajes.campana(2));
        var victoria=new TablaPuntajes.Fila(2,"intento-victoria",true,91,"NORMAL",
            List.of("Equipos seguros: 6/6"),List.of());
        TablaPuntajes.registrar(victoria);
        TablaPuntajes.registrar(victoria);
        assertEquals(victoria,TablaPuntajes.campana(2));
        assertEquals(2,TablaPuntajes.historial(2).size());
        assertEquals(derrota,TablaPuntajes.historial(2).getFirst());
    }

    @Test void preguntasCuatroConservanPositivasNegativasYNeutral(){
        var r=new MisionCuatroSketch.Resultado(true,3,1,3,"m4-preguntas",List.of(0,1,1,0));
        var fila=PuntajesMision.cuatro(r);
        assertTrue(fila.victoria());
        assertTrue(fila.datos().get(1).contains("2 favorables, 1 desfavorable, 1 neutral"));
        assertEquals(4,fila.decisiones().size());
        assertEquals("Pregunta 3: Sí · neutral",fila.decisiones().get(2));
        assertEquals(2,fila.senales().get("respuestas_favorables"));
        assertEquals(1,fila.senales().get("respuestas_desfavorables"));
        assertEquals(1,fila.senales().get("respuestas_neutrales"));
        assertEquals(4,fila.cambioCiudad().get(1));
        assertEquals(-2,fila.cambioCiudad().get(5));
    }

    @Test void entrevistaSeisGuardaLasTresRespuestasSinPenalizarConviccion(){
        var m=new MisionSeisMotor();m.modo=MisionSeisMotor.Modo.SOLO;
        for(int i=0;i<3;i++){
            m.fase=MisionSeisMotor.Fase.ENTREVISTA;m.pregunta=i;
            m.pulsar(i==1?KeyEvent.VK_2:KeyEvent.VK_1);
        }
        var restaurada=MisionSeisMotor.restaurar(m.capturar());
        assertNotNull(restaurada);
        assertEquals(List.of(0,1,0),restaurada.resultado().respuestas());
        var fila=PuntajesMision.seis(restaurada.resultado());
        assertEquals(3,fila.decisiones().size());
        assertEquals("Pregunta 2: convicción y diálogo",fila.decisiones().get(1));
        assertEquals(2,fila.senales().get("respuestas_escucha"));
        assertEquals(1,fila.senales().get("respuestas_conviccion"));
        assertEquals(0,fila.puntaje()-Math.round(30f*restaurada.naves[0].vida/5));
    }

    @Test void tablaSeisMuestraVictoriaAntesDeConfirmarSinAcreditarla(){
        var motor=new MisionSeisMotor();
        motor.fase=MisionSeisMotor.Fase.FINAL;
        assertFalse(motor.resultado().completada());
        assertTrue(VistaCampana.filaSeis(motor).victoria());
        assertFalse(motor.resultado().completada());
        motor.fase=MisionSeisMotor.Fase.DERROTA;
        assertFalse(VistaCampana.filaSeis(motor).victoria());
    }

    @Test void ciudadCanonicaExigeSieteVictoriasYReemplazaLaMisionRepetida()throws Exception{
        for(int n=1;n<=7;n++)TablaPuntajes.registrar(new TablaPuntajes.Fila(n,"primera-"+n,true,80,"",
            List.of(),List.of(),List.of(0,1,0,0,0,0,0)));
        assertEquals(57,TablaPuntajes.ciudadParaFinales().orElseThrow().confianza());
        TablaPuntajes.registrar(new TablaPuntajes.Fila(4,"repetida-4",true,95,"",
            List.of(),List.of(),List.of(0,-2,0,0,0,0,0)));
        assertEquals(54,TablaPuntajes.ciudadParaFinales().orElseThrow().confianza());
        TablaPuntajes.registrar(new TablaPuntajes.Fila(4,"derrota-4",false,20,"",
            List.of(),List.of(),List.of(0,-50,0,0,0,0,0)));
        assertEquals(54,TablaPuntajes.ciudadParaFinales().orElseThrow().confianza());
        assertEquals(3,TablaPuntajes.historial(4).size());
    }

    @Test void antecedentesTipadosSobrevivenAlGuardado()throws Exception{
        var fila=new TablaPuntajes.Fila(4,"antecedentes",true,75,"NORMAL",
            List.of(),List.of(),List.of(0,0,0,0,0,0,0),
            java.util.Map.of("respuestas_favorables",2,"respuestas_desfavorables",1));
        TablaPuntajes.registrar(fila);
        assertEquals(fila.senales(),TablaPuntajes.campana(4).senales());
    }
}
