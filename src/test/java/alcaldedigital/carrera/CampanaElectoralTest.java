package alcaldedigital.carrera;

import alcaldedigital.compartido.carrera.*;
import alcaldedigital.compartido.carrera.EstadoCarrera.*;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.servidor.carrera.*;
import alcaldedigital.cliente.mision7.CarreraLocal;
import java.util.*;import java.nio.file.*;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class CampanaElectoralTest {
    @TempDir Path tmp;
    @Test void juanHonestoNecesitaApoyoSuficienteYCorruptoNuncaPuedeGanar(){var e=EstadoElectoral.inicial(EstadoCiudad.inicial());assertTrue(e.elegible(0));assertNotEquals(0,e.liderProvisional());e=e.aplicar(0,60,30,20,-5,20,"Campaña responsable",false);assertEquals(0,e.liderProvisional());assertNotEquals(0,e.aplicar(0,0,0,0,0,0,"Pacto confirmado",true).liderProvisional());}
    @Test void losIndicadoresDeLaCampanaInfluyenEnLosApoyosIniciales(){var bien=EstadoElectoral.inicial(new EstadoCiudad(90,90,90,90,90,0,0));var mal=EstadoElectoral.inicial(new EstadoCiudad(5,10,10,10,20,95,90));assertTrue(bien.respaldo(0)>mal.respaldo(0));assertNotEquals(bien.liderProvisional(),mal.liderProvisional());}
    @Test void arraysDelEstadoElectoralNoSePuedenModificarDesdeLaVista(){var e=EstadoElectoral.inicial(EstadoCiudad.inicial());int antes=e.apoyo()[0];int[] copia=e.apoyo();copia[0]=999;assertEquals(antes,e.apoyo()[0]);}
    @Test void aclararUnAnuncioFalsoReduceElApoyoDeLaCuentaImplicadaYExplicaLaConsecuencia(){var m=MotorCarreraTest.conEvento(Noticia.RESULTADO,0);int candidato=m.vista().corredores()[0].evento().candidato();int apoyo=m.vista().electoral().apoyo()[candidato];assertTrue(HistoriaCarrera.cuerpo(m.vista().corredores()[0].evento()).contains(EstadoElectoral.NOMBRES[candidato]));m.orden(0,0,false,"ACCION",0);assertTrue(m.vista().electoral().apoyo()[candidato]<apoyo);assertTrue(m.vista().mensaje().contains(EstadoElectoral.NOMBRES[candidato]));assertTrue(m.vista().ciudad()[0]>m.vista().base()[0]);}
    @Test void resultadoEsIdempotentePreservaMision5YSoloAcreditaAlHumano()throws Exception{
        var perfil=tmp.resolve("campana.properties");Files.writeString(perfil,"sala.anterior=victoria\nmejor.m5.0=140\n");var repo=new RepositorioCarrera(perfil);var m=MotorCarreraTest.nuevo(2,Dificultad.NORMAL);var v=m.vista();var fin=MotorCarreraTest.copiar(v,Fase.RESULTADO,0,v.corredores(),v.objetos());
        assertEquals(repo.acreditar(fin,EstadoCiudad.inicial()),repo.acreditar(fin,EstadoCiudad.inicial()));Properties p=new Properties();try(var in=Files.newInputStream(perfil)){p.load(in);}assertEquals("140",p.getProperty("mejor.m5.0"));assertEquals("victoria",p.getProperty("sala.anterior"));assertEquals("pendiente_mision8",p.getProperty("electoral.estado"));assertNotNull(p.getProperty("sala.m7."+fin.id()+".puntos.1"));assertNull(p.getProperty("sala.m7."+fin.id()+".puntos.0"));assertNull(p.getProperty("electoral.ganador.definitivo"));
    }
    @Test void corrupcionSeGuardaAntesDeTerminarYNoSeBorraAlAbrirOtraCarrera()throws Exception{
        var repo=new RepositorioCarrera(tmp.resolve("campana.properties"));var m=MotorCarreraTest.conEvento(Noticia.PACTO,0);try(var c=new CarreraLocal(m,new GuardadoCarrera(tmp.resolve("m7.bin")),repo)){
            c.accion(0,"ACCION",1);assertFalse(repo.electoral(EstadoCiudad.inicial()).juanCorrupto());c.accion(0,"ACCION",3);assertTrue(repo.electoral(EstadoCiudad.inicial()).juanCorrupto());
            assertTrue(new GuardadoCarrera(tmp.resolve("m7.bin")).cargar().vista().electoral().juanCorrupto());
        }
        var otra=repo.electoral(EstadoCiudad.inicial());assertTrue(otra.juanCorrupto());assertNotEquals(0,otra.liderProvisional());
    }
}
