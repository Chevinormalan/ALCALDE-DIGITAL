package alcaldedigital.reglas;

import alcaldedigital.compartido.protocolo.SesionMisionUno;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SesionMisionUnoLocalTest {
    private int solicitud;
    private void hacer(SesionMisionUnoLocal s,String accion) {s.solicitar("prueba-"+(++solicitud),accion);}
    private void completarIntroduccion(SesionMisionUnoLocal s) {
        for(int i=0;i<15;i++) s.actualizar(.1);
        assertEquals(SesionMisionUno.EstadoPalomo.POSADO,s.vista().palomo().estado());
        for(int i=0;i<4;i++) hacer(s,"continuar");
        assertEquals(SesionMisionUno.EstadoPalomo.SALIENDO,s.vista().palomo().estado());
        for(int i=0;i<14;i++) s.actualizar(.1);
        assertEquals(SesionMisionUno.Fase.CIVITAS,s.vista().fase());
    }
    private void empezarCrisis(SesionMisionUnoLocal s) {
        completarIntroduccion(s);
        hacer(s,"verificar:1");hacer(s,"compartir:1");
        hacer(s,"verificar:2");hacer(s,"compartir:2");
        hacer(s,"ignorar:3");
        assertEquals(SesionMisionUno.Fase.CRISIS,s.vista().fase());
    }
    private void moverHasta(SesionMisionUnoLocal s,float x,float y) {
        for(int i=0;i<300&&Math.hypot(s.vista().x()-x,s.vista().y()-y)>12;i++) {
            float dx=x-s.vista().x(),dy=y-s.vista().y();
            float len=(float)Math.hypot(dx,dy);
            s.mover(dx/len*Math.min(4.5f,len),dy/len*Math.min(4.5f,len));
            s.actualizar(.025); // avance de la partida mientras Sofi camina
        }
        assertTrue(Math.hypot(s.vista().x()-x,s.vista().y()-y)<38,"Sofi debe alcanzar a la persona");
    }
    @Test void laFuenteNoSeRevelaAntesDeContrastarYNoSeDuplicaLaDecision() {
        var s=new SesionMisionUnoLocal(10,SesionMisionUno.Dificultad.NORMAL);
        completarIntroduccion(s);
        assertEquals(SesionMisionUno.Fase.CIVITAS,s.vista().fase());
        assertEquals("",s.vista().fuentes().get(0));
        s.solicitar("unica","verificar:1");
        int puntos=s.vista().puntos();
        s.solicitar("unica","verificar:1");
        assertEquals(puntos,s.vista().puntos());
        assertFalse(s.vista().fuentes().get(0).isBlank());
        assertEquals(40,s.vista().publicaciones().get(0).riesgo());
    }
    @Test void elPalomoVuelaDeCadaBordeYRegresaAlMismo() {
        for(int lado=0;lado<4;lado++) {
            var s=new SesionMisionUnoLocal(lado,SesionMisionUno.Dificultad.NORMAL);
            assertEquals(lado,s.vista().palomo().lado());
            assertEquals(SesionMisionUno.EstadoPalomo.LLEGANDO,s.vista().palomo().estado());
            hacer(s,"continuar");
            assertEquals(0,s.vista().dialogo(),"el diálogo espera a que aterrice");
            completarIntroduccion(s);
            assertEquals(lado,s.vista().palomo().lado());
            assertEquals(SesionMisionUno.EstadoPalomo.FUERA,s.vista().palomo().estado());
        }
    }
    @Test void dosPersonasCalmadasSalenPorLaMismaRuta() {
        var s=new SesionMisionUnoLocal(10,SesionMisionUno.Dificultad.NORMAL);
        empezarCrisis(s);
        assertEquals(SesionMisionUno.Animo.NORMAL,s.vista().peatones().get(7).animo());
        moverHasta(s,276,137);hacer(s,"interactuar");hacer(s,"interactuar");
        assertEquals(1,s.vista().calmados());
        moverHasta(s,360,145);
        hacer(s,"interactuar");hacer(s,"interactuar");hacer(s,"interactuar");
        assertEquals(2,s.vista().calmados());
        assertEquals(SesionMisionUno.Animo.SALIENDO,s.vista().peatones().get(0).animo());
        assertEquals(SesionMisionUno.Animo.SALIENDO,s.vista().peatones().get(1).animo());
        for(int i=0;i<25;i++) s.actualizar(.1);
        assertEquals(1,s.vista().parejasFuera());
    }
    @Test void elRumorSePropagaEnOlasPorElGrafoSocial() {
        var s=new SesionMisionUnoLocal(10,SesionMisionUno.Dificultad.NORMAL);
        empezarCrisis(s);
        assertEquals(2,s.vista().peatones().stream().filter(p->p.animo()==SesionMisionUno.Animo.ALTERADO).count());
        for(int i=0;i<21;i++) s.actualizar(.1);
        assertEquals(4,s.vista().peatones().stream().filter(p->p.animo()==SesionMisionUno.Animo.ALTERADO).count());
        for(int i=0;i<40;i++) s.actualizar(.1);
        assertEquals(8,s.vista().peatones().stream().filter(p->p.animo()==SesionMisionUno.Animo.ALTERADO).count());
    }
    @Test void elEstanqueSoloBloqueaSuContornoVisible() {
        var s=new SesionMisionUnoLocal(10,SesionMisionUno.Dificultad.NORMAL);
        empezarCrisis(s);
        for(int i=0;i<4;i++) s.mover(6,0);
        for(int i=0;i<30;i++) s.mover(0,7);
        assertEquals(180,s.vista().x(),.1);
        assertEquals(320,s.vista().y(),.1,"el césped encima del lago debe ser transitable");
        for(int i=0;i<10;i++) s.mover(0,7);
        assertTrue(s.vista().y()<355,"Sofi no debe entrar al agua");

        var borde=new SesionMisionUnoLocal(10,SesionMisionUno.Dificultad.NORMAL);
        empezarCrisis(borde);
        for(int i=0;i<19;i++) borde.mover(-7,0);
        for(int i=0;i<32;i++) borde.mover(0,7);
        assertTrue(borde.vista().y()>325,"el césped a la izquierda del lago debe quedar libre");
    }
    @Test void elPlazoCierraLaMisionYConservaConsecuencias() {
        var s=new SesionMisionUnoLocal(10,SesionMisionUno.Dificultad.NORMAL);
        empezarCrisis(s);
        for(int i=0;i<300;i++) s.actualizar(.1);
        assertEquals(SesionMisionUno.Fase.RESULTADO,s.vista().fase());
        assertEquals("MALO",s.vista().desenlace());
        assertTrue(s.vista().ciudad().convivencia()<50);
    }
    @Test void cadaDificultadAplicaSuPlazoSoloAlComenzarLaCrisis() {
        for(var dificultad:SesionMisionUno.Dificultad.values()) {
            var s=new SesionMisionUnoLocal(10,dificultad);
            double tiempo=s.vista().tiempo();
            assertEquals(dificultad.segundos(),tiempo);
            completarIntroduccion(s);
            assertEquals(tiempo,s.vista().tiempo());
            hacer(s,"ignorar:1");hacer(s,"ignorar:2");hacer(s,"ignorar:3");
            s.actualizar(.1);
            assertTrue(s.vista().tiempo()<tiempo);
        }
    }
    @Test void dificilEsAjustadoPeroPermiteCompletarLaRuta() {
        var s=new SesionMisionUnoLocal(10,SesionMisionUno.Dificultad.DIFICIL);
        empezarCrisis(s);
        float[][] ruta={{276,137},{360,145},{440,206},{450,285},{365,366},
            {278,370},{250,290},{191,287},{183,205}};
        for(int paso=0;paso<ruta.length;paso++) {
            moverHasta(s,ruta[paso][0],ruta[paso][1]);
            if(paso==6) continue; // rodea el estanque
            int persona=paso<6?paso:paso-1;
            for(int espera=0;espera<480&&s.vista().peatones().get(persona).animo()==SesionMisionUno.Animo.NORMAL;espera++)
                s.actualizar(.025);
            assertEquals(SesionMisionUno.Animo.ALTERADO,s.vista().peatones().get(persona).animo());
            for(int i=0;i<s.vista().peatones().get(persona).necesarias();i++) {
                hacer(s,"interactuar");
                for(int pausa=0;pausa<6;pausa++) s.actualizar(.025);
            }
        }
        for(int i=0;i<120&&s.vista().fase()!=SesionMisionUno.Fase.RESULTADO;i++) s.actualizar(.025);
        assertEquals("BUENO",s.vista().desenlace());
        assertTrue(s.vista().tiempo()>0,"la ruta hábil debe terminar antes de los 12 segundos");
    }
    @Test void sofiaPuedeCalmarLasCuatroParejasYObtenerBuenDesenlace() {
        var s=new SesionMisionUnoLocal(10,SesionMisionUno.Dificultad.NORMAL);
        empezarCrisis(s);
        float[][] ruta={{276,137},{360,145},{440,206},{450,285},{365,366},{278,370},{250,290},{191,287},{183,205}};
        for(int paso=0;paso<ruta.length;paso++) {
            float[] destino=ruta[paso];
            moverHasta(s,destino[0],destino[1]);
            if(paso==6) continue; // punto de paso que rodea el estanque
            int persona=paso<6?paso:paso-1;
            if(persona>1) for(int i=0;i<21;i++) s.actualizar(.1);
            for(int i=0;i<s.vista().peatones().get(persona).necesarias();i++) hacer(s,"interactuar");
        }
        for(int i=0;i<40&&s.vista().fase()!=SesionMisionUno.Fase.RESULTADO;i++) s.actualizar(.1);
        assertEquals(8,s.vista().calmados());
        assertEquals(4,s.vista().parejasFuera());
        assertEquals("BUENO",s.vista().desenlace());
        assertTrue(s.vista().tiempo()>0,"la ruta completa debe caber dentro de los 30 segundos");
    }
}
