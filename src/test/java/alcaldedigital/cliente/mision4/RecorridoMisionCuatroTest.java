package alcaldedigital.cliente.mision4;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static java.awt.event.KeyEvent.*;

class RecorridoMisionCuatroTest {
 private void paso(MisionCuatroGame g,int ticks){for(int i=0;i<ticks;i++)g.update(1f/60);}
 private void tecla(MisionCuatroGame g,int k){g.press(k,(char)k);g.release(k);}
 private void dialogo(MisionCuatroGame g){var fase=g.scenes.current;for(int i=0;i<20&&g.scenes.current==fase&&g.dialogue.active();i++)tecla(g,VK_ENTER);}
 @Test void recorridoCompletoConConduccionColisionesCuatroRespuestasYTresCombates(){
  var g=new MisionCuatroGame();g.press(VK_D,'d');paso(g,600);g.release(VK_D);assertTrue(g.vehicle.x>1970);tecla(g,VK_ENTER);assertEquals(MisionCuatroGame.Phase.PLAZA,g.scenes.current);
  g.press(VK_A,'a');for(int i=0;i<400&&g.hero.x>214;i++)g.update(1f/60);g.release(VK_A);
  g.press(VK_W,'w');for(int i=0;i<100&&g.hero.y>370;i++)g.update(1f/60);g.release(VK_W);
  tecla(g,VK_ENTER);assertEquals(MisionCuatroGame.Phase.STAGE,g.scenes.current);paso(g,90);tecla(g,VK_ENTER);
  for(int i=0;i<4;i++){assertEquals(MisionCuatroGame.Phase.QUESTION,g.scenes.current);dialogo(g);assertEquals(MisionCuatroGame.Phase.ANSWER,g.scenes.current);tecla(g,VK_N);dialogo(g);}
  assertEquals(MisionCuatroGame.Phase.CONFUSED,g.scenes.current);dialogo(g);assertEquals(MisionCuatroGame.Phase.PALOMO,g.scenes.current);dialogo(g);paso(g,300);
  assertEquals(MisionCuatroGame.Phase.DOWNLOAD,g.scenes.current);tecla(g,VK_ENTER);
  for(int encuentro=0;encuentro<3;encuentro++){
   if(encuentro>0){g.press(VK_D,'d');for(int i=0;i<700&&g.scenes.current==MisionCuatroGame.Phase.ROAD;i++)g.update(1f/60);g.release(VK_D);}
   assertEquals(MisionCuatroGame.Phase.RUMOR,g.scenes.current);tecla(g,VK_V);
   for(int i=0;i<5000&&g.scenes.current==MisionCuatroGame.Phase.BATTLE;i++){
    var e=g.combat.enemy;
    float dx=e.x-g.hero.x,dy=e.y-g.hero.y;
    int k=Math.abs(dx)>Math.abs(dy)?(dx<0?VK_A:VK_D):(dy<0?VK_W:VK_S);
    g.loseFocus();if(Math.hypot(dx,dy)>43)g.press(k,(char)k);
    else g.hero.direction=Math.abs(dx)>Math.abs(dy)?(dx<0?2:3):(dy<0?1:0);
    tecla(g,VK_F);g.update(1f/60);
   }
   assertEquals(MisionCuatroGame.Phase.VICTORY,g.scenes.current,"Combate "+encuentro);paso(g,105);
  }
  paso(g,150);assertEquals(MisionCuatroGame.Phase.EPILOGUE,g.scenes.current);dialogo(g);assertTrue(g.ending());assertEquals(3,g.combat.kills);assertEquals(3,g.approval);assertEquals(4,g.answered);
  assertArrayEquals(new int[]{0,0,0,0},g.respuestas);
  assertArrayEquals(g.respuestas,MisionCuatroGame.restore(g.checkpoint()).respuestas);
 }
 @Test void restaurarConservaIdentidadDePartidaYRumoresSinMantenerTeclas(){
  var g=new MisionCuatroGame();g.set(MisionCuatroGame.Phase.RUMOR);g.press(VK_V,'v');paso(g,10);
  var copia=MisionCuatroGame.restore(g.checkpoint());assertNotNull(copia);assertEquals(g.partida,copia.partida);assertEquals(1,copia.approval);assertEquals(g.combat.enemy.hp,copia.combat.enemy.hp);for(boolean key:copia.keys)assertFalse(key);
 }
 @Test void espacioEsEsquivaYQDefiendeDuranteCombate(){
  var g=new MisionCuatroGame();g.set(MisionCuatroGame.Phase.BATTLE);g.combat.spawn(0,650,350);
  g.press(VK_SPACE,' ');assertTrue(g.hero.dodge>0);assertEquals(0,g.combat.swordTime);
  g.press(VK_Q,'q');paso(g,1);assertTrue(g.combat.guarding);
  g.release(VK_Q);assertFalse(g.combat.guarding);
 }
}
