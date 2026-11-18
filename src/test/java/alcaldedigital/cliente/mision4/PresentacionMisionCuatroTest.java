package alcaldedigital.cliente.mision4;
import java.awt.event.KeyEvent;
import alcaldedigital.cliente.presentacion.SalidaAudioPrueba;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PresentacionMisionCuatroTest {
 @Test void laPoseDeAtaqueNoQuedaPegadaDespuesDeLaVictoria(){
  var g=new MisionCuatroGame();g.combat.swordTime=.14f;g.set(MisionCuatroGame.Phase.VICTORY);assertNotEquals("attack",g.heroVisualPose(true));
  g.set(MisionCuatroGame.Phase.ROAD);g.keys[KeyEvent.VK_D]=true;assertTrue(g.heroVisualPose(true).startsWith("walk_"));assertEquals(.14f,g.combat.swordTime,"La presentación no toca el temporizador de combate");
 }
 @Test void ataqueVisibleDuranteTodaLaVentanaInclusoMoviendoseConDanioYEscudo(){
  var g=new MisionCuatroGame();g.set(MisionCuatroGame.Phase.BATTLE);g.combat.spawn(0,600,300);g.hero.x=300;g.hero.y=300;g.hero.direction=3;g.press(KeyEvent.VK_J,'j');g.release(KeyEvent.VK_J);
  float duracion=g.combat.swordTime;assertEquals(.16f,duracion);g.keys[KeyEvent.VK_D]=true;g.hero.invulnerable=1;g.combat.guarding=true;
  for(int i=0;i<9;i++){assertEquals("attack",g.heroVisualPose(true));g.combat.update(1f/60,g.hero,g.road);}
  g.combat.update(1f/60,g.hero,g.road);assertNotEquals("attack",g.heroVisualPose(true));
 }
 @Test void sonidoSoloAcompanaAtaquesAceptados(){
  var g=new MisionCuatroGame();g.set(MisionCuatroGame.Phase.BATTLE);g.combat.spawn(0,600,300);var s=new SalidaAudioPrueba();var a=new SoundEffects(s);a.observar(g);
  g.press(KeyEvent.VK_J,'j');a.observar(g);g.release(KeyEvent.VK_J);g.press(KeyEvent.VK_J,'j');a.observar(g);assertEquals(1,s.cuenta("sword"));
  var antes=g.checkpoint();a.observar(g);assertEquals(antes,g.checkpoint());
 }
 @Test void motorCambiaEntreAceleracionMarchaYFrenadoSinAlterarLaConduccion(){
  var g=new MisionCuatroGame();var s=new SalidaAudioPrueba();var a=new SoundEffects(s);a.observar(g);g.press(KeyEvent.VK_D,'d');
  for(int i=0;i<60;i++){g.update(1f/60);a.observar(g);}assertTrue(s.bucles.get("acelerar")>0);g.release(KeyEvent.VK_D);g.update(1f/60);a.observar(g);
  assertEquals(0f,s.bucles.get("acelerar"));assertTrue(s.bucles.get("marcha")>0);assertEquals(1,s.cuenta("freno"));for(int i=0;i<10;i++){g.update(1f/60);a.observar(g);}assertEquals(1,s.cuenta("freno"));
  a.pause();assertTrue(s.bucles.isEmpty());
 }
}
