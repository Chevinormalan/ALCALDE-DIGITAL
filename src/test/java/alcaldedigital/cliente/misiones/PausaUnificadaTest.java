package alcaldedigital.cliente.misiones;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
import alcaldedigital.cliente.entrada.EstadoMando;import java.util.*;
class PausaUnificadaTest {
 static class Sesion implements PausaUnificada.Sesion {final int n;boolean pausa,salio,fallar,consulta;int guardados;Sesion(int n){this.n=n;}public int numero(){return n;}public boolean lista(){return true;}public boolean pausada(){return pausa;}public void pausar(){pausa=true;}public void reanudar(){pausa=false;}public String guardar()throws Exception{if(fallar)throw new java.io.IOException("disco lleno");guardados++;return "guardada";}public void salir(){salio=true;}public boolean tieneConsultas(){return true;}public void abrirConsulta(){consulta=true;}public void cerrarConsulta(){consulta=false;}}
 private void tecla(PausaUnificada p,int k){assertTrue(p.pulsar(k));p.soltar(k);}
 @Test void sieteMisionesUsanElMismoMenuYSoloSalirDespuesDeGuardar(){for(int n=1;n<=7;n++){var s=new Sesion(n);var p=new PausaUnificada();p.conectar(s);tecla(p,27);assertTrue(s.pausa);assertTrue(p.activa());assertEquals(1,s.guardados);p.raton(500,470);assertEquals(2,s.guardados);p.raton(40,40);assertTrue(s.pausa);p.raton(500,590);assertTrue(s.salio);assertEquals(3,s.guardados);}}
 @Test void discoLlenoNoCierraLaPartida(){var s=new Sesion(1);var p=new PausaUnificada();p.conectar(s);tecla(p,80);s.fallar=true;p.raton(500,590);assertFalse(s.salio);assertTrue(p.activa());}
 @Test void repeticionAyudaYConsultaNoReanudanAccidentalmente(){var s=new Sesion(5);var p=new PausaUnificada();p.conectar(s);p.pulsar(27);p.pulsar(27);assertTrue(s.pausa);p.soltar(27);tecla(p,72);assertTrue(s.pausa);tecla(p,27);assertTrue(s.pausa);tecla(p,84);assertTrue(s.consulta);tecla(p,27);assertFalse(s.consulta);assertTrue(s.pausa);tecla(p,84);tecla(p,72);assertFalse(p.consulta());assertFalse(s.consulta);tecla(p,27);assertTrue(s.pausa);tecla(p,27);assertFalse(s.pausa);}
 @Test void desconexionPausaYElStickRetenidoNoPasaAlMotorAlReanudar(){var s=new Sesion(7);var p=new PausaUnificada();p.conectar(s);var neutro=new EstadoMando("m",0,0,0,0,Set.of());p.mando(neutro);p.mando(EstadoMando.desconectado());assertTrue(s.pausa);p.mando(new EstadoMando("m",1,0,0,0,Set.of(EstadoMando.Boton.MENU)));assertFalse(s.pausa);assertTrue(p.mando(new EstadoMando("m",1,0,0,0,Set.of())));assertTrue(p.mando(neutro));assertFalse(p.mando(neutro));}
 @Test void pausaRemotaSeMuestraYAutoguardadoTieneElMismoRitmo(){var s=new Sesion(5);var p=new PausaUnificada();p.conectar(s);for(int i=0;i<101;i++)p.actualizar(.1f);assertEquals(1,s.guardados);s.pausa=true;p.actualizar(0);assertTrue(p.activa());for(int i=0;i<200;i++)p.actualizar(.1f);assertEquals(1,s.guardados);}
 @Test void reanudacionRemotaCierraElModalTambienDuranteUnaConsulta(){var s=new Sesion(7);var p=new PausaUnificada();p.conectar(s);tecla(p,84);assertTrue(p.consulta());s.pausa=false;p.actualizar(0);assertFalse(p.activa());assertFalse(p.consulta());assertFalse(s.consulta);}
 @Test void controlesGraficosPermanecenPausadosYConsumenTeclasYClics(){var s=new Sesion(6);var p=new PausaUnificada();p.conectar(s);
  tecla(p,27);assertTrue(s.pausa);p.raton(500,410);p.raton(600,570);int guardados=s.guardados;
  tecla(p,71);assertEquals(guardados,s.guardados,"G dentro del mapa no llega a la partida ni guarda");
  p.raton(80,80);assertTrue(s.pausa);tecla(p,27);assertTrue(s.pausa,"Esc solo vuelve a Ayuda");
  p.raton(600,570);p.raton(640,650);assertTrue(s.pausa,"Volver retorna a Ayuda");
  tecla(p,69);assertTrue(s.pausa);tecla(p,27);assertFalse(s.pausa);}

}
