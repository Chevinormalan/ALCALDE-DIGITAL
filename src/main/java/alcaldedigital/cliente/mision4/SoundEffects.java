package alcaldedigital.cliente.mision4;

import alcaldedigital.cliente.audio.SonidosRemaster;
import java.awt.event.KeyEvent;
import alcaldedigital.cliente.presentacion.*;
/** Conserva los WAV originales y añade conducción continua, sin eventos atrasados. */
final class SoundEffects {
 private final AudioEscenaSalida salida;private boolean listo,guardia,frenando,linterna;private float espada,vx,vy,pasos;
 private int salud,vidasEnemigo,muertes,archivos,nodos,revelados,guardianes;private MisionCuatroGame.Phase fase;
 private float tiempoEscena;
 SoundEffects(){
  this(new AudioEscena("M4"));
  SonidosRemaster.precargar("civita-portal","civita-espada","civita-escudo","civita-impacto");
 }
 SoundEffects(AudioEscenaSalida s){salida=s;}
 void play(String name){
  String sonido=switch(name){case "sword"->"civita-espada";case "shield"->"civita-escudo";default->"civita-impacto";};
  salida.efecto(name,SonidosRemaster.ruta(sonido),.42f,.08);
 }
 void observar(MisionCuatroGame g){
  boolean carro=g.scenes.current==MisionCuatroGame.Phase.CAR;float rapidez=(float)Math.hypot(g.carSpeed,g.carVelocityY),normal=Math.min(1,rapidez/205);
  int dx=(g.keys[KeyEvent.VK_D]||g.keys[KeyEvent.VK_RIGHT]?1:0)-(g.keys[KeyEvent.VK_A]||g.keys[KeyEvent.VK_LEFT]?1:0);
  int dy=(g.keys[KeyEvent.VK_S]||g.keys[KeyEvent.VK_DOWN]?1:0)-(g.keys[KeyEvent.VK_W]||g.keys[KeyEvent.VK_UP]?1:0);
  boolean freno=carro&&rapidez>25&&dx*g.carSpeed+dy*g.carVelocityY<=0,acelerando=carro&&!freno&&(dx!=0||dy!=0);
  salida.bucle("ralenti","presentacion/audio/carro_ralenti",carro?.15f*(1-normal):0);
  salida.bucle("marcha","presentacion/audio/carro_marcha",carro&&!acelerando?.17f*normal:0);
  salida.bucle("acelerar","presentacion/audio/carro_acelerar",acelerando?.19f*normal:0);
  salida.bucle("ambiente","presentacion/audio/ambiente",carro?.025f:.055f);
  int hp=g.combat.enemy==null?0:g.combat.enemy.hp;
  if(listo){
   if(g.scenes.current!=fase&&g.scenes.current==MisionCuatroGame.Phase.CIVITA_ENTRADA)
       salida.efecto("vibra","audio/fx/civitas",.40f,.3);
   if(g.scenes.current==fase&&fase==MisionCuatroGame.Phase.CIVITA_ENTRADA&&tiempoEscena<2.05f&&g.sceneTime>=2.05f)
       salida.efecto("absorcion",SonidosRemaster.ruta("civita-portal"),.59f,.3);
   if(g.scenes.current!=fase&&(g.scenes.current==MisionCuatroGame.Phase.CIVITA||g.scenes.current==MisionCuatroGame.Phase.CIVITA_SALIDA))
       salida.efecto("salto_digital","presentacion/audio/radio",.30f,.3);
   if(g.civita.linterna!=linterna)salida.efecto("linterna","audio/fx/click",.22f,.13);
   if(g.civita.total()>archivos)salida.efecto("contraste","audio/fx/verificar",.35f,.18);
   if(g.civita.revelados!=revelados||g.civita.fichas!=nodos)salida.efecto("hallazgo","audio/fx/recoger",.34f,.18);
   if(g.civita.fraseTiempo>2.6f&&vidasEnemigo==0&&hp>0)
       salida.efecto("amenaza_revelada","audio/fx/rumor",.35f,.23);
   if(g.civita.total()==4&&archivos<4||(g.civita.guardianes&4)!=0&&(guardianes&4)==0)
       salida.efecto("pulso_nucleo","audio/fx/verificar",.52f,.45);
   if(freno&&!frenando)salida.efecto("freno","presentacion/audio/freno",.16f,.35);
   if(carro&&(Math.abs(vx)>45&&Math.abs(g.carSpeed)<1||Math.abs(vy)>45&&Math.abs(g.carVelocityY)<1))salida.efecto("choque","presentacion/audio/impacto",.27f,.25);
   if(g.combat.swordTime>espada+.05f)play("sword");
   if(g.combat.guarding&&!guardia)play("shield");
   if(g.combat.health<salud)play("hit");
   if(hp<vidasEnemigo&&vidasEnemigo>0||g.combat.kills>muertes)salida.efecto("golpe_enemigo",SonidosRemaster.ruta("civita-impacto"),.36f,.1);
   if(!carro&&(int)(g.hero.walk/3)>(int)(pasos/3))salida.efecto("paso_adoquin","presentacion/audio/paso_adoquin",.17f,.16);
  }
  espada=g.combat.swordTime;guardia=g.combat.guarding;salud=g.combat.health;vidasEnemigo=hp;muertes=g.combat.kills;vx=g.carSpeed;vy=g.carVelocityY;pasos=g.hero.walk;frenando=freno;listo=true;
  fase=g.scenes.current;tiempoEscena=g.sceneTime;linterna=g.civita.linterna;archivos=g.civita.total();
  nodos=g.civita.fichas;revelados=g.civita.revelados;guardianes=g.civita.guardianes;
 }
 void reset(){listo=false;}void pause(){salida.pausar();}void close(){salida.close();}
}
