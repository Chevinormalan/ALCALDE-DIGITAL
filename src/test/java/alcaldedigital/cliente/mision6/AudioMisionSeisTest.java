package alcaldedigital.cliente.mision6;
import java.awt.event.KeyEvent;
import alcaldedigital.cliente.presentacion.SalidaAudioPrueba;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static alcaldedigital.cliente.mision6.MisionSeisMotor.*;
class AudioMisionSeisTest {
 @Test void disparosYCohetesSoloCuandoElMotorLosAcepta(){
  var m=new MisionSeisMotor();m.comenzar(Modo.SOLO);m.fase=Fase.COMBATE;m.energia=120;var s=new SalidaAudioPrueba();var a=new AudioMisionSeis(s);a.observar(m);
  m.pulsar(KeyEvent.VK_J);for(int i=0;i<30;i++){m.actualizar(1f/60);a.observar(m);}assertEquals(3,s.cuenta("disparo_jugador"));
  m.pulsar(KeyEvent.VK_K);a.observar(m);m.soltar(KeyEvent.VK_K);m.pulsar(KeyEvent.VK_K);a.observar(m);assertEquals(1,s.cuenta("cohete"));
  var antes=m.capturar();a.observar(m);assertEquals(antes,m.capturar(),"El audio no modifica el juego");
 }
 @Test void rafagaEnemigaYAutodestruccionSeEmitenUnaSolaVez(){
  var m=new MisionSeisMotor();m.comenzar(Modo.SOLO);m.fase=Fase.COMBATE;var s=new SalidaAudioPrueba();var a=new AudioMisionSeis(s);a.observar(m);
  for(int i=0;i<90;i++){m.actualizar(1f/60);a.observar(m);}assertEquals(1,s.cuenta("disparo_dron"));
  m.fase=Fase.RETIRADA;m.tiempo=0;a.observar(m);for(int i=0;i<120;i++){m.actualizar(1f/60);a.observar(m);}assertEquals(1,s.cuenta("destruccion_dron"));
 }
 @Test void aterrizajeReduceElRotorYSalaYPausaLoDetienen(){
  var m=new MisionSeisMotor();var s=new SalidaAudioPrueba();var a=new AudioMisionSeis(s);m.fase=Fase.LLEGADA;m.tiempo=0;a.observar(m);float inicio=s.bucles.get("rotor");m.tiempo=4;a.observar(m);assertTrue(s.bucles.get("rotor")<inicio);
  m.fase=Fase.EXPLORAR_SALA;a.observar(m);assertEquals(0f,s.bucles.get("rotor"));assertEquals(0f,s.bucles.get("descenso"));a.pausar();assertEquals(1,s.pausas);assertTrue(s.bucles.isEmpty());
 }
 @Test void impactoYDanioCorrespondenACambiosReales(){
  var m=new MisionSeisMotor();m.fase=Fase.COMBATE;var s=new SalidaAudioPrueba();var a=new AudioMisionSeis(s);a.observar(m);m.impacto=.5f;m.naves[0].vida--;a.observar(m);a.observar(m);
  assertEquals(1,s.cuenta("explosion_cohete"));assertEquals(1,s.cuenta("dano"));
 }
 @Test void impactoDeMisilNoSePierdeSiUnaBalaGolpeaEnElMismoPaso(){
  var m=new MisionSeisMotor();m.comenzar(Modo.SOLO);m.fase=Fase.COMBATE;m.actualizar(.01f);var s=new SalidaAudioPrueba();var a=new AudioMisionSeis(s);
  m.proyectiles.add(new Proyectil(m.jefeX,m.jefeY,0,0,5,0,false,false));m.proyectiles.add(new Proyectil(m.jefeX,m.jefeY,0,0,18,0,false,true));a.observar(m);m.actualizar(.01f);a.observar(m);a.observar(m);
  assertEquals(.15f,m.impacto);assertEquals(1,s.cuenta("explosion_cohete"));
 }
 @Test void rotorRespondeAlMovimientoYLosPasosDependenDeCaminar(){
  var m=new MisionSeisMotor();m.comenzar(Modo.SOLO);m.fase=Fase.ENTRENAMIENTO;var s=new SalidaAudioPrueba();var a=new AudioMisionSeis(s);a.observar(m);m.actualizar(1f/60);a.observar(m);float quieto=s.bucles.get("rotor");
  m.pulsar(KeyEvent.VK_D);m.actualizar(1f/60);a.observar(m);assertTrue(s.bucles.get("rotor")>quieto);
  m.fase=Fase.EXPLORAR_SALA;m.perderFoco();m.pulsar(KeyEvent.VK_D);for(int i=0;i<30;i++){m.actualizar(1f/60);a.observar(m);}assertTrue(s.cuenta("paso_madera")>0);long pasos=s.cuenta("paso_madera");m.perderFoco();for(int i=0;i<30;i++){m.actualizar(1f/60);a.observar(m);}assertEquals(pasos,s.cuenta("paso_madera"));
 }
}
