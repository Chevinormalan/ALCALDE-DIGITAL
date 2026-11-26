package alcaldedigital.integracion;

import alcaldedigital.cliente.*;
import alcaldedigital.cliente.graficos.*;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.servidor.sesion.*;
import processing.core.PApplet;
import processing.awt.PGraphicsJava2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class GraficosTest {
 @TempDir Path carpeta;
 private JuegoCliente juego(){return new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);}
 @Test void ampliaA1080p1440pY4kSinCambiarProporciones(){
  for(int[]t:new int[][]{{1920,1080},{2560,1440},{3840,2160}}){var m=EscaladoPantalla.ajustar(t[0],t[1],false);assertEquals(0,m.x());assertEquals(0,m.y());assertEquals(t[0],m.ancho());assertEquals(t[1],m.alto());assertEquals(new EscaladoPantalla.Punto(640,360),m.aLogico(t[0]/2,t[1]/2));}
 }
 @Test void conservaLaImagenCompletaEn16a10YUltrapanoramica(){
  var alto=EscaladoPantalla.ajustar(1920,1200,false);assertEquals(60,alto.y());assertEquals(1080,alto.alto());assertEquals(0,alto.x());
  var ancho=EscaladoPantalla.ajustar(3440,1440,false);assertEquals(440,ancho.x());assertEquals(2560,ancho.ancho());assertEquals(0,ancho.y());
 }
 @Test void losMargenesNoTienenClicsYLasEsquinasCoinciden(){
  var m=EscaladoPantalla.ajustar(3440,1440,false);assertFalse(m.aLogico(439,100).dentro());assertFalse(m.aLogico(3000,100).dentro());
  assertEquals(new EscaladoPantalla.Punto(0,0),m.aLogico(440,0));assertEquals(new EscaladoPantalla.Punto(1279,719),m.aLogico(2999,1439));
 }
 @Test void elEscaladoEnteroNoRecortaYReduceSiNoCabe(){
  var m=EscaladoPantalla.ajustar(1920,1080,true);assertEquals(1280,m.ancho());assertEquals(720,m.alto());assertEquals(320,m.x());
  var chico=EscaladoPantalla.ajustar(960,540,true);assertEquals(960,chico.ancho());assertEquals(540,chico.alto());
  for(int w:new int[]{1,600,1001,1920,3840})for(int h:new int[]{1,400,701,1200,2160}){var r=EscaladoPantalla.ajustar(w,h,true);assertTrue(r.x()>=0&&r.y()>=0&&r.x()+r.ancho()<=w&&r.y()+r.alto()<=h);}
 }
 @Test void editarNoGuardaYAplicarPersisteLaSeleccion()throws Exception{
  Path archivo=carpeta.resolve("graficos.properties");var o=new OpcionesGraficas(archivo);o.modo();o.escalado();o.filtro();o.tamano();assertFalse(Files.exists(archivo));assertEquals(OpcionesGraficas.ORIGINAL,o.actual());
  o.aplicar();var copia=new OpcionesGraficas(archivo);assertEquals(o.actual(),copia.actual());assertTrue(copia.actual().completa());assertTrue(copia.actual().entero());assertEquals(OpcionesGraficas.Filtro.SUAVE,copia.actual().filtro());
 }
 @Test void volverDescartaElBorradorYRestaurarEsExplicito(){
  var o=new OpcionesGraficas(carpeta.resolve("graficos.properties"));o.modo();o.abrir();assertFalse(o.cambios());o.modo();o.aplicar();o.restaurar();assertTrue(o.actual().completa());assertEquals(OpcionesGraficas.ORIGINAL,o.edicion());o.aplicar();assertEquals(OpcionesGraficas.ORIGINAL,o.actual());
 }
 @Test void unArchivoInvalidoRecuperaValoresSeguros()throws Exception{
  Path archivo=carpeta.resolve("graficos.properties");Files.writeString(archivo,"version=1\nancho=999999\nalto=0\nfiltro=DESCONOCIDO\n");var o=new OpcionesGraficas(archivo);assertEquals(OpcionesGraficas.ORIGINAL,o.actual());assertFalse(o.mensaje.isBlank());
 }
 @Test void lasResolucionesDisponiblesRespetanElMonitor(){
  var o=new OpcionesGraficas(carpeta.resolve("graficos.properties"));o.detectar(1920,1080,1896,1024);
  var monitor=new OpcionesGraficas.Tamano(1920,1080);
  assertTrue(o.compatibles().contains(monitor));
  assertTrue(o.compatibles().stream().allMatch(t->t.equals(monitor)||t.ancho()<=1896&&t.alto()<=1024));
  o.detectar(1024,768,1014,738);
  assertTrue(o.compatibles().contains(new OpcionesGraficas.Tamano(1024,768)));
 }
 @Test void aplicarLaResolucionDelMonitorAbrePantallaCompletaYConservaLaVentana(){
  Path archivo=carpeta.resolve("graficos.properties");
  var o=new OpcionesGraficas(archivo);
  o.detectar(1024,768,1014,738);
  var nativa=new OpcionesGraficas.Tamano(1024,768);
  for(int i=0;i<o.compatibles().size()&&!o.edicion().ventana().equals(nativa);i++)o.tamano();
  assertEquals(nativa,o.edicion().ventana());
  o.aplicar();
  assertEquals(o.actual(),o.edicion());
  assertTrue(new OpcionesGraficas(archivo).actual().completa());
  assertEquals(OpcionesGraficas.ORIGINAL.ventana(),o.actual().ventana());
  o.alternarCompleta();
  assertEquals(OpcionesGraficas.ORIGINAL,o.actual());
 }
 @Test void graficosVuelveACómoJugarYDespuesAlMenu(){
  var j=juego();j.accion("ayuda");var r=new RenderJuego();r.dibujar(j);var b=r.botones().stream().filter(x->x.accion().equals("graficos")).findFirst().orElseThrow();r.pulsar(j,b.area().x+10,b.area().y+10);assertEquals(JuegoCliente.Pantalla.GRAFICOS,j.pantalla);
  r.dibujar(j);assertTrue(r.botones().stream().anyMatch(x->x.accion().equals("grafico:aplicar")));j.tecla(27,' ');assertEquals(JuegoCliente.Pantalla.AYUDA,j.pantalla);assertEquals(JuegoCliente.Pantalla.MENU,j.anterior);j.accion("volver");assertEquals(JuegoCliente.Pantalla.MENU,j.pantalla);
 }
 @Test void graficosSigueSiendoUnMenuAunqueQuedeUnaMisionUnoEnMemoria(){
  var j=juego();j.misionUno=new SesionMisionUnoLocal(1,alcaldedigital.compartido.protocolo.SesionMisionUno.Dificultad.FACIL);j.accion("ayuda");j.accion("graficos");
  assertEquals(0,j.numeroMision());j.accion("pausa");assertFalse(j.pausa);var r=new RenderJuego();r.dibujar(j);assertTrue(r.botones().stream().anyMatch(b->b.accion().equals("grafico:modo")));
  j.accion("volver");assertEquals(JuegoCliente.Pantalla.MENU,j.anterior);j.accion("volver");assertEquals(JuegoCliente.Pantalla.MENU,j.pantalla);
 }
 private PApplet ventana(int ancho,int alto){var p=new PApplet();var g=new PGraphicsJava2D();g.setParent(p);g.setPrimary(false);g.setSize(ancho,alto);p.g=g;p.width=ancho;p.height=alto;return p;}
 @Test void todoElContenidoUsa720pYElRatonUsaLaMismaTransformacion(){
  var p=ventana(1920,1200);p.mouseX=960;p.mouseY=600;var l=new LienzoJuego(p);p.g.beginDraw();
  l.dibujar(OpcionesGraficas.ORIGINAL,()->{var a=l.escena();assertEquals(1280,a.width);assertEquals(720,a.height);assertEquals(640,a.mouseX);assertEquals(360,a.mouseY);a.background(255,0,0);});p.g.endDraw();
  assertEquals(1920,p.width);assertEquals(1200,p.height);assertEquals(0xffff0000,p.g.get(960,600));assertNotEquals(0xffff0000,p.g.get(960,20));assertNotSame(p.g,l.escena().g);
 }
 @Test void unRedimensionadoUsaElBuferVigenteYEsperaElSiguienteFotogramaParaAceptarClics(){
  var p=ventana(1280,720);var l=new LienzoJuego(p);
  p.g.beginDraw();
  p.setSize(960,600);p.g.setSize(960,600);
  l.dibujar(OpcionesGraficas.ORIGINAL,()->l.escena().background(255,0,0));
  assertEquals(1280,l.marco().ancho());assertEquals(0xffff0000,p.g.get(1279,719));
  assertFalse(l.aLogico(480,300).dentro());
  p.g.endDraw();p.g.beginDraw();
  l.dibujar(OpcionesGraficas.ORIGINAL,()->l.escena().background(255,0,0));
  assertEquals(new EscaladoPantalla.Marco(0,30,960,540),l.marco());
  assertEquals(new EscaladoPantalla.Punto(640,360),l.aLogico(480,300));
  assertFalse(l.aLogico(480,29).dentro());
  p.g.endDraw();
 }
 @Test void laReduccionMapaElCentroDelPixelMostrado(){
  var m=EscaladoPantalla.ajustar(640,360,false);
  assertEquals(new EscaladoPantalla.Punto(1,1),m.aLogico(0,0));
  assertEquals(new EscaladoPantalla.Punto(1279,719),m.aLogico(639,359));
 }
 @Test void elModoNitidoMantieneVisiblesLosTrazosAlReducir(){
  var p=ventana(640,360);var l=new LienzoJuego(p);p.g.beginDraw();
  l.dibujar(OpcionesGraficas.ORIGINAL,()->{
   var a=l.escena();a.background(0);a.noStroke();a.fill(255);a.rect(0,0,1,720);
  });
  p.g.endDraw();
  int intensidad=p.g.get(0,180)&0xff;
  assertTrue(intensidad>0&&intensidad<255);
  assertEquals(0xff000000,p.g.get(1,180));
 }
 @Test void elFiltroNitidoConservaLosColoresYElSuaveInterpola(){
  var p=ventana(1920,1080);var l=new LienzoJuego(p);var original=OpcionesGraficas.ORIGINAL;
  Runnable dibujar=()->{var a=l.escena();a.background(0);a.noStroke();a.fill(255);a.rect(0,0,641,720);};
  p.g.beginDraw();l.dibujar(original,dibujar);p.g.endDraw();int nitido=p.g.get(961,400);
  p.g.beginDraw();l.dibujar(new OpcionesGraficas.Configuracion(false,original.ventana(),false,OpcionesGraficas.Filtro.SUAVE),dibujar);p.g.endDraw();int suave=p.g.get(961,400);
  assertTrue(nitido==0xffffffff||nitido==0xff000000);assertNotEquals(nitido,suave);
 }
 @Test void laPresentacion4kIncluyeLosCuatroBordesDelLienzo(){
  var p=ventana(3840,2160);var l=new LienzoJuego(p);p.g.beginDraw();l.dibujar(OpcionesGraficas.ORIGINAL,()->{
   var a=l.escena();a.background(255,0,0);a.noStroke();a.fill(0,255,0);a.rect(0,0,1,1);a.fill(0,0,255);a.rect(1279,719,1,1);
  });p.g.endDraw();assertEquals(0xff00ff00,p.g.get(0,0));assertEquals(0xff0000ff,p.g.get(3839,2159));assertEquals(0xffff0000,p.g.get(1920,1080));
 }
}
