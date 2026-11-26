package alcaldedigital.mision8;

import alcaldedigital.cliente.mision8.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AnimacionOchoTest {
 @Test void elEncuadreDelAscensoDejaVisibleLaCabezaSinAlterarLaSimulacion(){
  var m=MisionOchoTest.combate(0,false,1,1,0);m.fase=5;m.forma=3;m.jefeY=-600;m.heroes[0].y=-450;m.camara=-790;
  var estado=m.capturar();double camara=AnimacionOcho.camaraVisual(m);
  assertTrue(m.jefeY-camara>=300);assertTrue(m.heroes[0].y-camara<600);assertEquals(estado,m.capturar());
  m.heroes[0].y=612;assertEquals(m.camara,AnimacionOcho.camaraVisual(m));m.fase=4;assertEquals(m.camara,AnimacionOcho.camaraVisual(m));
 }
 @Test void losTresRolesVuelvenAReposoAlSoltarMovimiento(){
  for(int rol=0;rol<3;rol++){
   var m=MisionOchoTest.combate(0,false,1,1,rol);m.entrada(0,MotorOcho.DER);m.paso();
   assertNotEquals(rol*16,AnimacionOcho.cuadroHeroe(m,0));
   m.entrada(0,0);m.paso();assertEquals(rol*16,AnimacionOcho.cuadroHeroe(m,0));
   for(int i=0;i<20;i++)m.paso();assertEquals(rol*16,AnimacionOcho.cuadroHeroe(m,0));
  }
 }
 @Test void elAtaqueConservaSuFamiliaAlSoltarYTrasRestaurar(){
  for(int tecla:new int[]{MotorOcho.DOS,MotorOcho.TRES}){
   var m=MisionOchoTest.combate(0,false,1,1,1);
   m.entrada(0,tecla);m.paso();
   int cuadro=tecla==MotorOcho.DOS?27:29;
   assertEquals(cuadro,AnimacionOcho.cuadroHeroe(m,0));
   m.entrada(0,0);m.paso();
   var recuperado=MotorOcho.restaurar(m.capturar());
   assertEquals(cuadro,AnimacionOcho.cuadroHeroe(recuperado,0));
   for(int i=0;i<19;i++)recuperado.paso();
   assertEquals(16,AnimacionOcho.cuadroHeroe(recuperado,0));
  }
 }

 @Test void laInfeccionRecorreLosDieciseisCuadrosYSostieneElUltimo(){
  Set<Integer>vistos=new HashSet<>();int anterior=0;
  for(int t=0;t<=150;t++){int cuadro=AnimacionOcho.cuadroInfeccion(t);assertTrue(cuadro>=anterior&&cuadro-anterior<=1);vistos.add(cuadro);anterior=cuadro;}
  assertEquals(16,vistos.size());assertEquals(15,AnimacionOcho.cuadroInfeccion(10000));
 }
 @Test void cadaEmisionTieneDisparoYRecuperacionConAvisoPrevio(){
  for(var ataque:MotorOcho.Ataque.values())for(int dificultad=0;dificultad<3;dificultad++){
   var m=MisionOchoTest.combate(0,false,1,dificultad,0);m.iniciarAtaque(ataque);
   m.ataqueTicks=AnimacionOcho.aviso(m)-1;assertEquals(3,AnimacionOcho.cuadroJefe(m));
   for(int e:AnimacionOcho.emisiones(m)){
    m.ataqueTicks=e;assertEquals(4,AnimacionOcho.cuadroJefe(m),ataque.name());
    m.ataqueTicks=e+7;assertEquals(5,AnimacionOcho.cuadroJefe(m));
    m.ataqueTicks=e+14;assertEquals(6,AnimacionOcho.cuadroJefe(m));
    m.ataqueTicks=e+21;assertEquals(7,AnimacionOcho.cuadroJefe(m));
   }
  }
 }
 @Test void recuperarUnaPartidaYPausarlaNoCambiaSuPose(){
  var m=MisionOchoTest.combate(2,false,3,1,0);m.iniciarAtaque(MotorOcho.Ataque.ORBES);for(int i=0;i<69;i++)m.paso();m.pausar(true);
  var copia=MotorOcho.restaurar(m.capturar());int cuadro=AnimacionOcho.cuadroJefe(m);
  for(int i=0;i<120;i++){m.paso();copia.paso();}
  assertEquals(cuadro,AnimacionOcho.cuadroJefe(m));assertEquals(cuadro,AnimacionOcho.cuadroJefe(copia));assertEquals(m.capturar(),copia.capturar());
 }
 @Test void lasTresFormasDeLosCuatroCandidatosUsanFilasPropias(){
  Set<Integer>filas=new HashSet<>();for(int c=0;c<4;c++)for(int f=1;f<=3;f++)assertTrue(filas.add(AnimacionOcho.filaAtaque(c,f)));
  assertEquals(12,filas.size());
 }
}
