package alcaldedigital.cliente.misiones;
import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.mision4.MisionCuatroSketch;
import alcaldedigital.servidor.sesion.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static alcaldedigital.cliente.entrada.EstadoMando.Boton;

class IntegracionCampanaTest {
 @TempDir Path tmp;private final Map<String,String> originales=new HashMap<>();
 @BeforeEach void aislar(){for(String k:List.of("ad.guardados","ad.audio.mudo")){originales.put(k,System.getProperty(k));}System.setProperty("ad.guardados",tmp.toString());System.setProperty("ad.audio.mudo","true");}
 @AfterEach void restaurar(){originales.forEach((k,v)->{if(v==null)System.clearProperty(k);else System.setProperty(k,v);});}
 private JuegoCliente juego(){var j=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);alcaldedigital.pruebas.PartidaPrueba.desbloquear(j);j.accion("jugar");return j;}
 private void tecla(VistaCampana v,int k){v.pulsar((char)k,k);v.soltar(k);}
 private void pasos(VistaCampana v,int n){for(int i=0;i<n;i++)v.actualizar(1f/60);}
 private EstadoMando mando(float x,Boton...b){return new EstadoMando("Simulado",x,0,0,0,Set.of(b));}
 @Test void sieteTarjetasFuncionanYLaOchoSiguePendiente(){var j=juego();var r=new RenderJuego();r.dibujar(j);for(int n=1;n<=7;n++){int m=n;assertTrue(r.botones().stream().anyMatch(b->b.accion().equals("mision"+m)));}assertFalse(r.botones().stream().anyMatch(b->b.accion().equals("mision8")));}
 @Test void pausaCongelaRelojPosicionesYEntradasDeAmbasMisiones(){
  for(int n:new int[]{4,6}){var j=juego();j.accion("mision"+n);try(var v=new VistaCampana(n,j)){
   v.iniciarEn(null);v.pulsar('d',68);pasos(v,30);tecla(v,80);var antes=v.estado();pasos(v,90);assertEquals(antes,v.estado());
   v.raton(100,100);assertTrue(v.pausa());v.pulsar('s',83);tecla(v,27);pasos(v,12);
   if(n==4)assertEquals(antes.getProperty("carY"),v.estado().getProperty("carY"));
   v.soltar(68);v.soltar(83);tecla(v,80);tecla(v,80);assertFalse(v.pausa());
  }}
 }
 @Test void autoRepeticionYPerdidaDeFocoNoReanudanNiAvanzanDialogos(){
  var j=juego();try(var v=new VistaCampana(6,j)){v.iniciarEn(null);tecla(v,10);v.pulsar((char)27,27);for(int i=0;i<8;i++)v.pulsar((char)27,27);assertTrue(v.pausa());var antes=v.estado();pasos(v,60);assertEquals(antes,v.estado());v.perderFoco();assertTrue(v.pausa());v.soltar(27);tecla(v,27);assertFalse(v.pausa());}
 }
 @Test void mandoPausaAlDesconectarYExigeCentrarParaMover(){
  var j=juego();try(var v=new VistaCampana(4,j)){v.iniciarEn(null);v.mando(mando(0));v.mando(mando(1));pasos(v,60);float x=Float.parseFloat(v.estado().getProperty("carX"));assertTrue(x>80);
   v.mando(EstadoMando.desconectado());assertTrue(v.pausa());var p=v.estado();pasos(v,20);assertEquals(p,v.estado());
   v.mando(mando(1,Boton.MENU));assertFalse(v.pausa());v.mando(mando(1));pasos(v,20); // inertia remains part of physics
   v.mando(mando(0));v.mando(mando(1));pasos(v,30);assertTrue(Float.parseFloat(v.estado().getProperty("carX"))>x);
  }
 }
 @Test void salirGuardaYRecuperarNoConservaTeclasNiSobrescribeOtrasMisiones()throws Exception{
  Files.writeString(tmp.resolve("mision7-test.bin"),"preservar");
  var j=juego();try(var v=new VistaCampana(4,j)){v.iniciarEn(null);v.pulsar('d',68);pasos(v,60);tecla(v,80);v.raton(500,550);assertTrue(v.salir());v.terminar();assertEquals(JuegoCliente.Pantalla.MISIONES,j.pantalla);}
  assertTrue(Files.isRegularFile(tmp.resolve("mision4.properties")));
  try(var v=new VistaCampana(4,j)){v.iniciarEn(null);tecla(v,10);float x=Float.parseFloat(v.estado().getProperty("carX"));pasos(v,60);assertEquals(x,Float.parseFloat(v.estado().getProperty("carX")),.001);}
  assertEquals("preservar",Files.readString(tmp.resolve("mision7-test.bin")));
 }
 @Test void resultadoCuatroAcreditaUnaVezSinCambiarEleccionNiCorromperJuan(){var j=juego();var base=j.estadoCampana;var r=new MisionCuatroSketch.Resultado(true,3,3,3,"partida-prueba");j.terminarMisionCuatro(r);var ciudad=j.estadoCampana;assertEquals(base.informacionVerificada()+12,ciudad.informacionVerificada());j.terminarMisionCuatro(r);assertEquals(ciudad,j.estadoCampana);j.terminarMisionCuatro(new MisionCuatroSketch.Resultado(false,1,0,1));assertEquals(ciudad,j.estadoCampana);}
 @Test void acreditarCuatroPreservaCorrupcionYResultadosPreviosEntreReinicios()throws Exception{
  Path perfil=tmp.resolve("campana.properties");Files.writeString(perfil,"electoral.juan.corrupto=true\nsala.m7.previa=1\n");
  var j=juego();var r=new MisionCuatroSketch.Resultado(true,3,3,3,"partida-persistida");j.terminarMisionCuatro(r);
  var p=new Properties();try(var in=Files.newInputStream(perfil)){p.load(in);}assertEquals("true",p.getProperty("electoral.juan.corrupto"));assertEquals("1",p.getProperty("sala.m7.previa"));
  var reinicio=juego();reinicio.estadoCampana=j.estadoCampana;reinicio.terminarMisionCuatro(r);assertEquals(j.estadoCampana,reinicio.estadoCampana);
 }

 @Test void entrarConConfirmacionRetenidaNoAvanzaOtraPantalla(){
  var j=juego();try(var v=new VistaCampana(6,j)){v.iniciarEn(null);v.bloquearTeclas(Set.of(10));var antes=v.estado();
   v.pulsar((char)10,10);assertEquals(antes,v.estado());v.mando(mando(0,Boton.A));assertEquals(antes,v.estado());
   v.soltar(10);v.mando(mando(0));v.mando(mando(0,Boton.A));assertNotEquals(antes.getProperty("textoVisible"),v.estado().getProperty("textoVisible"));
  }
 }

}
