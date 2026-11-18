package alcaldedigital.cliente.graficos;

import processing.core.PApplet;
import processing.awt.PSurfaceAWT;
import java.awt.*;

/** Cambia decoraciones y tamaño en el hilo AWT, sin alterar el modo del monitor. */
public final class VentanaJuego {
 private final PApplet app;private final OpcionesGraficas opciones;
 private long aplicada=-1;private OpcionesGraficas.Configuracion previa;private Rectangle monitorAnterior;
 public VentanaJuego(PApplet app,OpcionesGraficas opciones){this.app=app;this.opciones=opciones;}
 public void refrescar(){aplicada=-1;}
 public void actualizar(){
  if(aplicada==opciones.revision())return;var nueva=opciones.actual();
  try{EventQueue.invokeAndWait(()->{
   var canvas=(PSurfaceAWT.SmoothCanvas)app.getSurface().getNative();Frame frame=canvas.getFrame();
   var monitor=frame.getGraphicsConfiguration();Rectangle pantalla=monitor.getBounds();Insets barra=Toolkit.getDefaultToolkit().getScreenInsets(monitor);
   Rectangle util=new Rectangle(pantalla.x+barra.left,pantalla.y+barra.top,pantalla.width-barra.left-barra.right,pantalla.height-barra.top-barra.bottom);
   opciones.detectar(pantalla.width,pantalla.height,util.width-24,util.height-56);
   boolean geometria=previa==null||nueva.completa()!=previa.completa()||!nueva.ventana().equals(previa.ventana())||nueva.completa()&&!pantalla.equals(monitorAnterior);
   monitorAnterior=new Rectangle(pantalla);
   if(!geometria)return;
   if(frame.isUndecorated()!=nueva.completa()){frame.dispose();frame.setUndecorated(nueva.completa());}
   frame.setExtendedState(Frame.NORMAL);app.getSurface().setResizable(!nueva.completa());
   int ancho=nueva.ventana().ancho(),alto=nueva.ventana().alto();
   if(nueva.completa()){ancho=pantalla.width;alto=pantalla.height;}
   else{double factor=Math.min(1,Math.min(Math.max(1,util.width-24)/(double)ancho,Math.max(1,util.height-56)/(double)alto));ancho=Math.max(1,(int)(ancho*factor));alto=Math.max(1,(int)(alto*factor));}
   app.getSurface().setSize(ancho,alto);
   if(nueva.completa())frame.setLocation(pantalla.x,pantalla.y);
   else frame.setLocation(util.x+Math.max(0,(util.width-frame.getWidth())/2),util.y+Math.max(0,(util.height-frame.getHeight())/2));
   frame.setVisible(true);canvas.requestFocusInWindow();
  });previa=nueva;aplicada=opciones.revision();}
  catch(Exception e){opciones.recuperarVentana();aplicada=-1;}
 }
}
