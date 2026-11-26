package alcaldedigital.batalla;

import alcaldedigital.compartido.batalla.EstadoBatalla.Dificultad;
import alcaldedigital.compartido.batalla.EstadoBatalla.Obstaculo;
import alcaldedigital.compartido.batalla.EstadoBatalla.Vista;
import alcaldedigital.compartido.batalla.PaqueteCruzado;
import alcaldedigital.compartido.batalla.PaqueteRastreador;
import alcaldedigital.compartido.batalla.PaqueteRastreador.Fase;
import alcaldedigital.servidor.batalla.GuardadoBatalla;
import alcaldedigital.servidor.batalla.MotorBatalla;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RastreadorBatallaTest {
    static Obstaculo rastreador(Vista v) {
        return v.obstaculos().stream().filter(a->a.tipo()==PaqueteRastreador.TIPO)
            .findFirst().orElse(null);
    }

    private static Obstaculo rastreador(MotorBatalla m) {
        return rastreador(m.vista());
    }

    static MotorBatalla hastaElAviso(long semilla,Dificultad nivel,boolean solo) {
        var m=PatronesPasablesTest.iniciar(semilla,12,nivel,solo);
        for(int t=0;t<350;t++){
            var v=m.vista();
            for(int rol=0;rol<(solo?1:2);rol++){
                float[] c=PatronesPasablesTest.control(v,rol);
                m.orden(rol,c[0],c[1],false,"",0);
            }
            m.paso();
            if(rastreador(m)!=null)return m;
        }
        throw new AssertionError("Antenas gemelas debe crear un rastreador");
    }

    @Test void atacaAlObjetivoQueSeQuedaQuietoSinSolaparseConOtrasOleadas() {
        for(Dificultad d:Dificultad.values())for(boolean solo:new boolean[]{true,false})
            for(long semilla:PatronesPasablesTest.semillas()){
                var m=hastaElAviso(semilla,d,solo);
                int objetivo=solo?0:1;
                int vidas=m.vista().jugadores()[objetivo].vidas();
                var ritmo=PaqueteRastreador.para(d);
                var fases=EnumSet.noneOf(Fase.class);
                m.orden(objetivo,0,0,false,"",0);
                for(Obstaculo a=rastreador(m);a!=null;a=rastreador(m)){
                    assertEquals(1,m.vista().obstaculos().size());
                    assertEquals(objetivo,a.objetivo());
                    assertTrue(a.x()>=MotorBatalla.AX&&a.x()+a.w()<=MotorBatalla.AX+MotorBatalla.AW);
                    assertTrue(a.y()>=MotorBatalla.AY&&a.y()+a.h()<=MotorBatalla.AY+MotorBatalla.AH);
                    Fase fase=ritmo.fase(a);
                    fases.add(fase);
                    int antes=m.vista().jugadores()[objetivo].vidas();
                    m.paso();
                    var siguiente=rastreador(m);
                    if(siguiente!=null&&!ritmo.fase(siguiente).haceDanio())
                        assertEquals(antes,m.vista().jugadores()[objetivo].vidas());
                }
                assertEquals(EnumSet.allOf(Fase.class),fases);
                assertEquals(vidas-1,m.vista().jugadores()[objetivo].vidas(),d+" semilla "+semilla);
            }
    }

    @Test void giraAlPerseguirYConservaLaRutaFijadaTrasGuardar(@TempDir Path carpeta) throws IOException {
        var m=hastaElAviso(42,Dificultad.NORMAL,true);
        var ritmo=PaqueteRastreador.para(Dificultad.NORMAL);
        var origen=rastreador(m);
        float dx=origen.vy()==0?0:1,dy=origen.vx()==0?0:-1;
        m.orden(0,dx,dy,false,"",0);
        do{m.paso();}while(ritmo.fase(rastreador(m))==Fase.AVISO);
        var primero=rastreador(m);
        for(int t=0;t<5;t++)m.paso();
        var siguiente=rastreador(m);
        assertEquals(Fase.SEGUIMIENTO,ritmo.fase(siguiente));
        assertTrue(Math.abs(primero.vx()-siguiente.vx())+Math.abs(primero.vy()-siguiente.vy())>.05f);
        assertTrue(Math.hypot(siguiente.vx(),siguiente.vy())<3.8);

        do{m.paso();}while(ritmo.fase(rastreador(m))==Fase.SEGUIMIENTO);
        var fijado=rastreador(m);
        assertEquals(Fase.FIJADO,ritmo.fase(fijado));
        var guardado=new GuardadoBatalla(carpeta.resolve("antenas.bin"));
        guardado.guardar(m);
        var copia=guardado.cargar();
        assertEquals(fijado,rastreador(copia));

        boolean seLanzo=false;
        while(ritmo.fase(rastreador(m))!=Fase.DISIPACION){
            m.orden(0,1,0,false,"",0);
            copia.orden(0,1,0,false,"",0);
            m.paso();
            copia.paso();
            var a=rastreador(m);
            assertEquals(a,rastreador(copia));
            assertEquals(fijado.vx(),a.vx());
            assertEquals(fijado.vy(),a.vy());
            seLanzo|=ritmo.fase(a)==Fase.EMBESTIDA;
            if(ritmo.fase(a)==Fase.FIJADO){
                assertEquals(fijado.x(),a.x());
                assertEquals(fijado.y(),a.y());
            }
        }
        assertTrue(seLanzo);
    }

    @Test void cortafuegosAlternaLasTandasYReservaLaPersecucionParaDificil(){
        for(var nivel:Dificultad.values()){
            for(boolean solo:new boolean[]{true,false}){
                var entradas=new HashSet<Obstaculo>();
                for(long semilla:PatronesPasablesTest.semillas()){
                    var m=PatronesPasablesTest.iniciar(semilla,14,nivel,solo);
                    int directos=0,rastreadores=0;
                    boolean cian=false,diagonal=false;
                    for(int t=0;t<36+MotorBatalla.duracion(14);t++){
                        var v=m.vista();
                        boolean quietud=v.obstaculos().stream().anyMatch(a->a.tipo()==1);
                        cian|=quietud;
                        var seguimiento=rastreador(v);
                        for(var a:v.obstaculos())if(a.tipo()==PaqueteCruzado.TIPO){
                            assertFalse(quietud);
                            assertNull(seguimiento);
                            assertEquals(-1,a.objetivo());
                            if(a.edad()==1){
                                directos++;
                                entradas.add(a);
                                diagonal|=a.vx()!=0&&a.vy()!=0;
                            }
                        }
                        if(seguimiento!=null){
                            assertFalse(quietud);
                            assertEquals(1,v.obstaculos().size());
                            if(seguimiento.edad()==1)rastreadores++;
                            assertEquals(solo?0:rastreadores-1,seguimiento.objetivo());
                        }
                        for(int rol=0;rol<(solo?1:2);rol++){
                            float[] c=PatronesPasablesTest.control(v,rol);
                            m.orden(rol,c[0],c[1],false,"",0);
                        }
                        m.paso();
                    }
                    assertTrue(cian);
                    assertEquals(nivel==Dificultad.FACIL?4:6,directos);
                    assertEquals(nivel==Dificultad.DIFICIL?2:0,rastreadores);
                    assertTrue(diagonal);
                }
                assertTrue(entradas.size()>20);
            }
        }
    }
}
