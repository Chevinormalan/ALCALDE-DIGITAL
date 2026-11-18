package alcaldedigital.cliente.graficos;

import processing.core.*;
import processing.awt.PGraphicsJava2D;
import java.awt.*;

/** El contenido siempre dibuja a 720p; la ventana real solamente presenta ese resultado. */
public final class LienzoJuego {
 private final PApplet ventana,escena;
 private final PGraphicsJava2D lienzo;
 public LienzoJuego(PApplet ventana){
  this.ventana=ventana;lienzo=(PGraphicsJava2D)ventana.createGraphics(1280,720,PApplet.JAVA2D);lienzo.noSmooth();
  escena=new PApplet(){@Override public void exit(){ventana.exit();}@Override public int millis(){return ventana.millis();}};
  escena.g=lienzo;escena.width=escena.pixelWidth=1280;escena.height=escena.pixelHeight=720;escena.pixelDensity=1;
 }
 public PApplet escena(){return escena;}
 public void dibujar(OpcionesGraficas.Configuracion config,Runnable contenido){
  var marco=EscaladoPantalla.ajustar(Math.max(1,ventana.width),Math.max(1,ventana.height),config.entero());
  var raton=marco.aLogico(ventana.mouseX,ventana.mouseY);var anterior=marco.aLogico(ventana.pmouseX,ventana.pmouseY);
  escena.mouseX=raton.x();escena.mouseY=raton.y();escena.pmouseX=anterior.x();escena.pmouseY=anterior.y();
  escena.frameCount=ventana.frameCount;escena.frameRate=ventana.frameRate;escena.focused=ventana.focused;
  escena.mousePressed=ventana.mousePressed;escena.mouseButton=ventana.mouseButton;escena.key=ventana.key;escena.keyCode=ventana.keyCode;escena.keyPressed=ventana.keyPressed;
  lienzo.beginDraw();try{contenido.run();}finally{lienzo.endDraw();}
  presentar((PGraphicsJava2D)ventana.g,lienzo.image,marco,config.filtro());
 }
 public static void presentar(PGraphicsJava2D destino,Image imagen,EscaladoPantalla.Marco marco,OpcionesGraficas.Filtro filtro){
  var g=(Graphics2D)destino.g2.create();try{
   g.setColor(new Color(33,21,47));g.fillRect(0,0,destino.width,destino.height);
   g.setColor(new Color(56,38,75));for(int x=0;x<destino.width;x+=32)g.fillRect(x,0,2,destino.height);
   g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,filtro==OpcionesGraficas.Filtro.NITIDO?RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR:RenderingHints.VALUE_INTERPOLATION_BILINEAR);
   g.drawImage(imagen,marco.x(),marco.y(),marco.ancho(),marco.alto(),null);
  }finally{g.dispose();}
 }
}
