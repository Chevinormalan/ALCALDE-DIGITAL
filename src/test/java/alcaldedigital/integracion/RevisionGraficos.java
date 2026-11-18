package alcaldedigital.integracion;

import alcaldedigital.cliente.*;
import alcaldedigital.servidor.sesion.*;
import processing.core.PApplet;
import processing.awt.PSurfaceAWT;
import java.awt.event.KeyEvent;
import java.nio.file.*;

/** Comprueba la ventana real y las mismas rutas de entrada, usando exclusivamente un perfil temporal. */
public final class RevisionGraficos {
 private final JuegoCliente juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
 private final ClienteSketch app=new ClienteSketch(juego);
 private final String caso=System.getProperty("ad.graficos.caso","panel");
 private final Path evid=Path.of(System.getProperty("ad.graficos.evidencias"));
 private void exigir(boolean b,String mensaje){if(!b)throw new IllegalStateException(mensaje);}
 private void click(int x,int y){app.mouseX=x;app.mouseY=y;app.mousePressed();app.mouseReleased();}
 private void tecla(int codigo){app.keyCode=codigo;app.key=(char)0;app.keyPressed();app.keyReleased();}
 private void captura(String nombre){exigir(app.get().save(evid.resolve(nombre+".png").toString()),"Captura fallida");}
 public void pre(){try{
  int f=app.frameCount;
  if(f==10){juego.movimientoReducido=true;if(caso.equals("panel"))juego.accion("ayuda");else{int n=Integer.parseInt(caso.substring(6));juego.progreso.desbloquearTodas();juego.accion("jugar");juego.accion("mision"+n);if(n==1)juego.accion("m1dificultad:FACIL");if(n==2)juego.accion("m2dificultad:NORMAL");}}
  if(f==20)app.getSurface().setSize(960,600);
  if(caso.equals("panel")){
   if(f==34){exigir(app.width==960&&app.height==600,"Tamaño de ventana no aplicado");captura("como-jugar");click(600,528);}
   if(f==36)exigir(juego.pantalla==JuegoCliente.Pantalla.GRAFICOS,"El clic reescalado no abrió Gráficos");
   if(f==40){var antes=juego.graficos.edicion();click(5,5);exigir(antes.equals(juego.graficos.edicion()),"El margen recibió un clic");captura("graficos-960x600");}
   if(f==45)click(300,207);
   if(f==47)exigir(juego.graficos.edicion().completa(),"El clic reescalado no cambió el modo");
   if(f==50)click(150,528);
   if(f==70){var frame=((PSurfaceAWT.SmoothCanvas)app.getSurface().getNative()).getFrame();exigir(frame.isUndecorated(),"No se activó sin bordes");var r=frame.getGraphicsConfiguration().getBounds();exigir(app.width==r.width&&app.height==r.height,"No ocupa el monitor");captura("graficos-completa");}
   if(f==75){app.keyCode=KeyEvent.VK_F11;app.keyPressed();app.keyPressed();exigir(!juego.graficos.actual().completa(),"F11 repitió el cambio al mantener la tecla");app.keyReleased();}
   if(f==95){exigir(!((PSurfaceAWT.SmoothCanvas)app.getSurface().getNative()).getFrame().isUndecorated(),"F11 no recuperó ventana");captura("graficos-restaurada");}
   if(f==100){tecla(KeyEvent.VK_ESCAPE);exigir(juego.pantalla==JuegoCliente.Pantalla.AYUDA,"Esc no volvió a Cómo jugar");tecla(KeyEvent.VK_ESCAPE);exigir(juego.pantalla==JuegoCliente.Pantalla.MENU,"No volvió al menú");}
  }else{
   int n=Integer.parseInt(caso.substring(6));
   if(f==45&&(n==5||n==7))tecla(KeyEvent.VK_ENTER);
   if(f==45&&n==8)click(150,495);
   if(f==60&&n==3){app.mouseX=480;app.mouseY=300;app.mouseMoved();var campo=ClienteSketch.class.getDeclaredField("misionTres");campo.setAccessible(true);var m=(PApplet)campo.get(app);exigir(m!=null&&m.mouseX==576&&m.mouseY==324,"Apuntado duplicado o descentrado en misión 3");}
   if(f==65)captura(caso+"-ventana");
   if(f==75)tecla(KeyEvent.VK_F11);
   if(f==95){var campo=ClienteSketch.class.getDeclaredField("pausaSistema");campo.setAccessible(true);var pausa=(alcaldedigital.cliente.misiones.PausaUnificada)campo.get(app);if(!pausa.activa())tecla(KeyEvent.VK_P);}
   if(f==100){var campo=ClienteSketch.class.getDeclaredField("pausaSistema");campo.setAccessible(true);var pausa=(alcaldedigital.cliente.misiones.PausaUnificada)campo.get(app);exigir(pausa.activa(),"La pausa no apareció en pantalla completa");captura(caso+"-pausa");}
   if(f==102){double s=Math.min(app.width/1280d,app.height/720d);click((int)((app.width-1280*s)/2+500*s),(int)((app.height-720*s)/2+360*s));}
   if(f==104){var campo=ClienteSketch.class.getDeclaredField("pausaSistema");campo.setAccessible(true);exigir(!((alcaldedigital.cliente.misiones.PausaUnificada)campo.get(app)).activa(),"El clic reescalado no reanudó la misión");}
  }
 }catch(Exception e){e.printStackTrace();System.exit(1);}}
 public void post(){try{if(app.frameCount==110){captura(caso+"-final");System.out.println("GRAFICOS_REVISION_OK "+caso+" "+app.width+"x"+app.height);app.exit();}}catch(Exception e){e.printStackTrace();System.exit(1);}}
 public static void main(String[]args)throws Exception{
  if(System.getProperty("ad.guardados")==null)throw new IllegalStateException("La revisión exige perfil temporal");var revision=new RevisionGraficos();Files.createDirectories(revision.evid);revision.app.registerMethod("pre",revision);revision.app.registerMethod("post",revision);PApplet.runSketch(new String[]{ClienteSketch.class.getName()},revision.app);
 }
}
