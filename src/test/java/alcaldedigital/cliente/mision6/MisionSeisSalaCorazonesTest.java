package alcaldedigital.cliente.mision6;

import java.awt.event.KeyEvent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static alcaldedigital.cliente.mision6.MisionSeisMotor.*;

class MisionSeisSalaCorazonesTest {
 private void paso(MisionSeisMotor m,float s){for(int i=0;i<(int)(s*60);i++)m.actualizar(1f/60);}
 @Test void elDronNoHablaTrasLaVictoriaYLaTransmisionNoSeSaltaSola(){
  var m=new MisionSeisMotor();m.fase=Fase.RETIRADA;
  assertEquals("MR. PALOMO",m.interlocutor());assertTrue(m.contenido().contains("se va a morir"));assertFalse(m.contenido().contains("VOLVER"));
  paso(m,20);assertEquals(Fase.RETIRADA,m.fase);
  for(int i=0;i<TRANSMISION.length;i++){m.pulsar(KeyEvent.VK_ENTER);if(m.fase==Fase.RETIRADA&&m.pagina==i)m.pulsar(KeyEvent.VK_ENTER);}
  assertEquals(Fase.LLEGADA,m.fase);paso(m,6.1f);assertEquals(Fase.EXPLORAR_SALA,m.fase);
 }
 @Test void explorarSalaRequiereLlegarAlSofaYConservaPosicionAlRecuperar(){
  var m=new MisionSeisMotor();m.fase=Fase.LLEGADA;paso(m,6.1f);
  assertEquals(ENTRADA_SALA_X,m.salaX);assertEquals(ENTRADA_SALA_Y,m.salaY);
  m.pulsar(KeyEvent.VK_ENTER);assertEquals(Fase.EXPLORAR_SALA,m.fase,"Enter lejos del sofá no inicia la entrevista");
  m.pulsar(KeyEvent.VK_D);paso(m,3.0f);m.soltar(KeyEvent.VK_D);
  assertTrue(m.cercaDelSofa());float x=m.salaX,y=m.salaY;m.pulsar(KeyEvent.VK_D);
  var copia=MisionSeisMotor.restaurar(m.capturar());assertNotNull(copia);paso(copia,.2f);assertEquals(x,copia.salaX);assertEquals(y,copia.salaY);
  copia.pulsar(KeyEvent.VK_ENTER);assertEquals(Fase.SALA,copia.fase);
  assertFalse(sueloSala(500,330),"El sofá no es suelo");assertFalse(sueloSala(950,480),"No se puede atravesar la pared");assertTrue(sueloSala(500,470));
 }
 @Test void corazonCuraSoloAlQueLoTocaYNoSuperaCincoNiReviveNaves(){
  var m=new MisionSeisMotor();m.comenzar(Modo.DUO);m.fase=Fase.COMBATE;m.naves[0].vida=2;m.naves[1].vida=3;
  var n=m.naves[1];m.corazones.add(new Corazon(n.x,n.y));m.actualizar(.01f);
  assertEquals(4,n.vida);assertEquals(2,m.naves[0].vida);assertTrue(m.corazones.isEmpty());assertTrue(n.curacion>0);
  n.vida=5;m.corazones.add(new Corazon(n.x,n.y));m.actualizar(.01f);assertEquals(5,n.vida);assertEquals(1,m.corazones.size());
  n.vida=0;n.activa=false;m.actualizar(.01f);assertEquals(0,n.vida);assertEquals(1,m.corazones.size());
 }
 @Test void aparecenPeriodicamenteYSeConservanEnElGuardado(){
  var m=new MisionSeisMotor();m.comenzar(Modo.SOLO);m.fase=Fase.COMBATE;paso(m,3.1f);
  assertFalse(m.corazones.isEmpty());var c=m.corazones.get(0);var copia=MisionSeisMotor.restaurar(m.capturar());
  assertNotNull(copia);assertEquals(m.corazones.size(),copia.corazones.size());assertEquals(c.x,copia.corazones.get(0).x);assertEquals(m.proximoCorazon,copia.proximoCorazon);
  paso(m,6.1f);assertTrue(m.corazonesCreados>=2);
  var p=m.capturar();p.setProperty("c0.edad","NaN");assertNull(MisionSeisMotor.restaurar(p));
 }
 @Test void losGuardadosAnterioresSiguenSiendoCompatibles(){
  var m=new MisionSeisMotor();m.fase=Fase.SALA;var p=m.capturar();p.setProperty("version","1");
  for(String k:new String[]{"salaX","salaY","proximoCorazon","corazonesCreados","corazones"})p.remove(k);
  var copia=MisionSeisMotor.restaurar(p);assertNotNull(copia);assertEquals(Fase.EXPLORAR_SALA,copia.fase);assertTrue(copia.corazones.isEmpty());
 }
}
