package alcaldedigital.cliente.mision6;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.event.KeyEvent;
import java.nio.file.*;
import alcaldedigital.cliente.ClienteSketch;
import alcaldedigital.cliente.entrada.MapaControles;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import static alcaldedigital.cliente.mision6.MisionSeisMotor.*;

class MisionSeisTest {
 @TempDir Path temporal;
 private void paso(MisionSeisMotor m,float segundos){for(int i=0;i<(int)(segundos*60);i++)m.actualizar(1f/60);}
 private void texto(MisionSeisMotor m){m.pulsar(KeyEvent.VK_ENTER);m.pulsar(KeyEvent.VK_ENTER);}
 private MisionSeisMotor combate(Modo modo){
  MisionSeisMotor m=new MisionSeisMotor();m.comenzar(modo);
  assertEquals(Fase.DESPEGUE,m.fase);paso(m,5.1f);assertEquals(Fase.AVISO,m.fase);
  for(int i=0;i<ADVERTENCIA.length;i++)texto(m);
  assertEquals(Fase.ENTRENAMIENTO,m.fase);m.pulsar(KeyEvent.VK_ENTER);assertEquals(Fase.COMBATE,m.fase);return m;
 }
 @Test void selectorYPausaEscape(){
  var j=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);alcaldedigital.pruebas.PartidaPrueba.elegir(j,6);assertEquals(JuegoCliente.Pantalla.MENU,j.pantalla);
  alcaldedigital.pruebas.PartidaPrueba.desbloquear(j);j.accion("jugar");var r=new RenderJuego();r.dibujar(j);var b=r.botones().stream().filter(x->x.accion().equals("mision6")).findFirst().orElseThrow();
  r.pulsar(j,b.area().x+5,b.area().y+5);assertEquals(JuegoCliente.Pantalla.MISION_SEIS,j.pantalla);assertTrue(j.misionEmbebida());
  String old=System.getProperty("ad.guardados");System.setProperty("ad.guardados",temporal.toString());
  try(var vista=new alcaldedigital.cliente.misiones.VistaCampana(6,j)){
   vista.iniciarEn(null);vista.pulsar((char)27,27);assertTrue(j.pausa);
   vista.pulsar((char)27,27);assertTrue(j.pausa,"Una tecla mantenida no reanuda");
   vista.soltar(27);vista.pulsar((char)27,27);assertFalse(j.pausa);
  }finally{if(old==null)System.clearProperty("ad.guardados");else System.setProperty("ad.guardados",old);}
  j.terminarMisionSeis(new Resultado(false,Modo.SOLO,Ruta.ESCUCHA,5,-1));assertEquals(JuegoCliente.Pantalla.MISIONES,j.pantalla);
 }
 @Test void campanaHablaConJuanYCaminaHastaElHelicoptero(){
  var m=new MisionSeisMotor();assertEquals(Fase.DISCURSO,m.fase);
  for(int i=0;i<DISCURSO.length;i++)texto(m);assertEquals(Fase.BUSCAR,m.fase);
  assertEquals(ESCENARIO_X,m.juanX);assertEquals(ESCENARIO_Y,m.juanY);
  m.pulsar(KeyEvent.VK_ENTER);assertEquals(Fase.BUSCAR,m.fase);
  m.pulsar(KeyEvent.VK_W);paso(m,.51f);m.soltar(KeyEvent.VK_W);m.pulsar(KeyEvent.VK_A);paso(m,2.4f);m.soltar(KeyEvent.VK_A);
  assertTrue(m.cercaDeJuan(),"El influencer debe poder acercarse a Juan caminando");m.pulsar(KeyEvent.VK_ENTER);assertEquals(Fase.ENCUENTRO,m.fase);
  for(int i=0;i<INTRO.length;i++)texto(m);assertEquals(Fase.CAMINAR,m.fase);
  m.pulsar(KeyEvent.VK_ENTER);assertEquals(Fase.CAMINAR,m.fase,"No se puede abordar lejos del helicóptero");
  m.pulsar(KeyEvent.VK_D);paso(m,3.9f);m.soltar(KeyEvent.VK_D);paso(m,1.5f);
  assertTrue(m.cercaDelHelicoptero());assertTrue(m.juanListo());m.pulsar(KeyEvent.VK_ENTER);assertEquals(Fase.MODO,m.fase);
  m.pulsar(KeyEvent.VK_RIGHT);m.pulsar(KeyEvent.VK_ENTER);assertEquals(Modo.DUO,m.modo);assertEquals(Fase.DESPEGUE,m.fase);
 }
 @Test void entrenamientoSeguroYAmbosModos(){
  for(Modo modo:Modo.values()){
   var m=new MisionSeisMotor();m.comenzar(modo);paso(m,5.1f);for(int i=0;i<ADVERTENCIA.length;i++)texto(m);
   assertEquals(Fase.ENTRENAMIENTO,m.fase);m.pulsar(KeyEvent.VK_D);paso(m,.3f);m.soltar(KeyEvent.VK_D);
   int fuego=modo==Modo.SOLO?KeyEvent.VK_J:KeyEvent.VK_F;m.pulsar(fuego);paso(m,.3f);m.soltar(fuego);
   assertTrue(m.practicoMover);assertTrue(m.practicoDisparo);assertEquals(5,m.naves[0].vida);
   paso(m,1.3f);assertTrue(m.aciertosPractica>0);assertNotNull(MisionSeisMotor.restaurar(m.capturar()));
   m.pulsar(KeyEvent.VK_ENTER);assertEquals(Fase.COMBATE,m.fase);assertEquals(180,m.energia);
  }
 }
 @Test void individualSeMueveDisparaYActivaMisilEnFaseDos(){
  var m=combate(Modo.SOLO);float inicio=m.naves[0].x;m.pulsar(KeyEvent.VK_D);paso(m,.5f);m.soltar(KeyEvent.VK_D);assertTrue(m.naves[0].x>inicio);
  m.pulsar(KeyEvent.VK_K);assertTrue(m.proyectiles.stream().noneMatch(p->p.especial));m.soltar(KeyEvent.VK_K);
  m.pulsar(KeyEvent.VK_J);paso(m,.3f);m.soltar(KeyEvent.VK_J);assertTrue(m.proyectiles.stream().anyMatch(p->!p.enemigo));
  m.energia=120;m.pulsar(KeyEvent.VK_K);assertTrue(m.proyectiles.stream().anyMatch(p->p.especial));assertTrue(m.naves[0].misil>0);
  m.pulsar(KeyEvent.VK_SHIFT);assertTrue(m.naves[0].invulnerable>0);assertTrue(m.naves[0].recargaImpulso>0);
 }
 @Test void cooperativoNavesIndependientesYDanioIndependiente(){
  var m=combate(Modo.DUO);float x1=m.naves[0].x,x2=m.naves[1].x;m.pulsar(KeyEvent.VK_D);paso(m,.2f);m.soltar(KeyEvent.VK_D);assertTrue(m.naves[0].x>x1);assertEquals(x2,m.naves[1].x);
  m.pulsar(KeyEvent.VK_LEFT);paso(m,.2f);m.soltar(KeyEvent.VK_LEFT);assertTrue(m.naves[1].x<x2);
  m.energia=120;m.pulsar(KeyEvent.VK_Q);m.pulsar(KeyEvent.VK_K);assertTrue(m.proyectiles.stream().anyMatch(p->p.dueno==0&&p.especial));assertTrue(m.proyectiles.stream().anyMatch(p->p.dueno==1&&p.especial));
  var n=m.naves[1];m.proyectiles.add(new Proyectil(n.x,n.y,0,0,1,-1,true,false));m.actualizar(.01f);assertEquals(4,n.vida);assertEquals(5,m.naves[0].vida);
  m.pulsar(KeyEvent.VK_NUMPAD0);assertTrue(n.impulso>0);
 }
 @Test void controlesGlobalesSeparanMisilDeGuardarYMovilidadDeAmbosJugadores(){
  var m=combate(Modo.DUO);m.energia=120;
  m.pulsar(KeyEvent.VK_G);assertTrue(m.proyectiles.stream().noneMatch(p->p.especial),"G queda reservada para guardar");
  var secundaria=MapaControles.accionTeclado(6,true,KeyEvent.VK_Q);
  assertEquals(MapaControles.Accion.SECUNDARIA,secundaria);
  assertEquals(secundaria,MapaControles.accionMando(EstadoMando.Boton.Y));
  m.pulsar(MapaControles.codigoInterno(6,true,secundaria));
  assertTrue(m.proyectiles.stream().anyMatch(p->p.dueno==0&&p.especial));
  m.pulsar(KeyEvent.VK_SPACE);assertTrue(m.naves[0].impulso>0);
  m.pulsar(KeyEvent.VK_SHIFT);assertTrue(m.naves[1].impulso>0);
  assertEquals(KeyEvent.VK_Q,MapaControles.codigoInterno(6,true,secundaria));
 }
 @Test void interferenciaSeRompeYAmbasEntrevistasSonValidas(){
  for(int decision=0;decision<2;decision++){
   var m=combate(Modo.SOLO);m.energia=1;m.actualizar(.01f);m.proyectiles.add(new Proyectil(m.jefeX,m.jefeY,0,0,3,0,false,false));m.actualizar(.01f);assertEquals(Fase.RETIRADA,m.fase);
   for(int i=0;i<TRANSMISION.length;i++)texto(m);assertEquals(Fase.LLEGADA,m.fase);paso(m,6.1f);assertEquals(Fase.EXPLORAR_SALA,m.fase);m.pulsar(KeyEvent.VK_D);paso(m,3.0f);m.soltar(KeyEvent.VK_D);m.pulsar(KeyEvent.VK_ENTER);assertEquals(Fase.SALA,m.fase);texto(m);texto(m);assertEquals(Fase.ENTREVISTA,m.fase);
   for(int i=0;i<3;i++){m.pulsar(decision==0?KeyEvent.VK_1:KeyEvent.VK_2);assertEquals(Fase.RESPUESTA,m.fase);texto(m);}
   assertEquals(Fase.FINAL,m.fase);assertEquals(decision==0?Ruta.ESCUCHA:Ruta.CONVICCION,m.ruta());assertFalse(m.resultado().completada());assertEquals("JUAN",m.interlocutor());texto(m);assertEquals("INFLUENCER",m.interlocutor());texto(m);assertTrue(m.resultado().completada());
  }
 }
 @Test void derrotaReiniciaLaPracticaSinRepetirHistoria(){
  var m=combate(Modo.SOLO);m.naves[0].vida=1;m.naves[0].invulnerable=0;m.proyectiles.add(new Proyectil(m.naves[0].x,m.naves[0].y,0,0,1,-1,true,false));m.actualizar(.01f);
  assertEquals(Fase.DERROTA,m.fase);assertFalse(m.resultado().completada());m.pulsar(KeyEvent.VK_R);assertEquals(Fase.ENTRENAMIENTO,m.fase);assertEquals(5,m.naves[0].vida);assertEquals(180,m.energia);
 }
 @Test void laLineaDelDisparoDirigidoSeConservaAlMoverseYAlRecuperar(){
  var m=combate(Modo.DUO);m.energia=120;
  for(int i=0;i<150&&m.avisoDron==null;i++)m.actualizar(1f/60);
  assertNotNull(m.avisoDron);
  assertEquals(Ataque.DIRIGIDO,m.avisoDron.tipo());assertEquals(0,m.avisoDron.objetivo());
  var aviso=m.avisoDron;
  m.pulsar(KeyEvent.VK_S);paso(m,.24f);m.soltar(KeyEvent.VK_S);
  assertEquals(aviso,m.avisoDron);
  var copia=MisionSeisMotor.restaurar(m.capturar());assertNotNull(copia);assertEquals(aviso,copia.avisoDron);
  for(int i=0;i<60&&copia.proyectiles.isEmpty();i++)copia.actualizar(1f/60);
  assertEquals(cantidad(Ataque.DIRIGIDO),copia.proyectiles.size());
  Proyectil centro=copia.proyectiles.get(1);
  assertEquals(Math.cos(aviso.angulo())*260,centro.vx,.01);assertEquals(Math.sin(aviso.angulo())*260,centro.vy,.01);
 }
 @Test void elHaloTieneUnAvisoSinDanioAntesDeActivarse(){
  var m=combate(Modo.SOLO);m.energia=60;
  for(int i=0;i<240&&(m.avisoDron==null||m.avisoDron.tipo()!=Ataque.HALO);i++)m.actualizar(1f/60);
  assertNotNull(m.avisoDron);assertEquals(Ataque.HALO,m.avisoDron.tipo());
  var n=m.naves[0];n.x=m.avisoDron.x()+12;n.y=m.avisoDron.y();n.vida=5;n.invulnerable=0;
  m.actualizar(1f/60);assertEquals(5,n.vida);
  for(int i=0;i<60&&m.choque<=0;i++)m.actualizar(1f/60);
  assertTrue(m.choque>0);assertEquals(4,n.vida);
 }
 @Test void instantaneaConservaCombateSinTeclasFantasma(){
  var m=combate(Modo.DUO);m.energia=86;m.naves[1].vida=3;m.naves[1].misil=2.3f;m.pulsar(KeyEvent.VK_D);m.pulsar(KeyEvent.VK_F);paso(m,.15f);
  var p=m.capturar();var copia=MisionSeisMotor.restaurar(p);assertNotNull(copia);assertEquals(Fase.COMBATE,copia.fase);assertEquals(Modo.DUO,copia.modo);assertEquals(86,copia.energia);assertEquals(3,copia.naves[1].vida);assertEquals(m.naves[1].misil,copia.naves[1].misil);assertEquals(m.proyectiles.size(),copia.proyectiles.size());
  float x=copia.naves[0].x;paso(copia,.2f);assertEquals(x,copia.naves[0].x,.001f,"Recuperar no debe mantener teclas pulsadas");
  p.setProperty("energia","-1");assertNull(MisionSeisMotor.restaurar(p));p.setProperty("energia","90");p.setProperty("jugadorX","NaN");assertNull(MisionSeisMotor.restaurar(p));
 }
 @Test void guardadoRecuperaCopiaAnteriorYNoAceptaArchivosInvalidos()throws Exception{
  Path f=temporal.resolve("guardado.properties");var m=new MisionSeisMotor();
  var a=new AutoguardadoSeis(f);a.guardar(m.capturar());assertTrue(a.cerrarYEsperar());
  m.fase=Fase.BUSCAR;m.jugadorX=500;var b=new AutoguardadoSeis(f);b.guardar(m.capturar());assertTrue(b.cerrarYEsperar());
  var c=new AutoguardadoSeis(f);assertEquals(Fase.BUSCAR,MisionSeisMotor.restaurar(c.cargar()).fase);c.close();
  Files.writeString(f,"version=1\nfase=basura\n");var d=new AutoguardadoSeis(f);var recuperada=MisionSeisMotor.restaurar(d.cargar());assertTrue(d.recuperado);assertNotNull(recuperada);assertEquals(Fase.DISCURSO,recuperada.fase);d.close();
 }
}
