package alcaldedigital.batalla;
import alcaldedigital.cliente.mision5.MisionCincoVista;
import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.servidor.sesion.*;
import alcaldedigital.compartido.batalla.EstadoBatalla.Fase;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class InterfazMisionCincoTest {
    @TempDir Path tmp;
    private void tecla(MisionCincoVista v,int k){v.pulsar(' ',k);v.soltar(k);}
    private void avanzar(MisionCincoVista v,int pasos){for(int i=0;i<pasos;i++)v.actualizar(1f/60);}
    @Test void selectorRecursosDialogosMovimientoYAbsorcionReales()throws Exception{
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");new RenderJuego().dibujar(juego);alcaldedigital.pruebas.PartidaPrueba.elegir(juego,5);assertEquals(JuegoCliente.Pantalla.MISION_CINCO,juego.pantalla);
        String anterior=System.getProperty("ad.guardados");System.setProperty("ad.guardados",tmp.toString());
        try(var vista=new MisionCincoVista(5,EstadoCiudad.inicial(),false,false)){
            vista.iniciarEn(null);tecla(vista,10);for(int i=0;i<200&&vista.vistaResultado()==null;i++){vista.actualizar(0);Thread.sleep(5);}assertNotNull(vista.vistaResultado());tecla(vista,69);avanzar(vista,1);assertEquals(Fase.INTRO,vista.vistaResultado().fase());
            tecla(vista,10);avanzar(vista,1);assertEquals(1,vista.vistaResultado().linea());
            vista.raton(1050,640);avanzar(vista,1);assertEquals(2,vista.vistaResultado().linea());
            for(int i=2;i<9;i++){tecla(vista,i%2==0?10:69);avanzar(vista,1);}assertEquals(Fase.OFICINA,vista.vistaResultado().fase());
            vista.pulsar('d',68);avanzar(vista,34);vista.soltar(68);vista.pulsar('s',83);avanzar(vista,96);vista.soltar(83);tecla(vista,69);avanzar(vista,1);assertEquals(Fase.ABSORCION,vista.vistaResultado().fase());
            avanzar(vista,151);assertEquals(Fase.AVISO,vista.vistaResultado().fase());assertTrue(Files.exists(tmp.resolve("mision5-solo.bin")));
            tecla(vista,80);long tick=vista.vistaResultado().tick();avanzar(vista,60);assertEquals(tick,vista.vistaResultado().tick());
        }finally{if(anterior==null)System.clearProperty("ad.guardados");else System.setProperty("ad.guardados",anterior);}
    }
}
