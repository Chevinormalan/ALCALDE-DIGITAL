package alcaldedigital.integracion;
import alcaldedigital.cliente.*;
import alcaldedigital.cliente.audio.*;
import alcaldedigital.servidor.sesion.*;
import processing.core.PApplet;
import java.nio.file.*;
import java.awt.event.KeyEvent;

/** Audio real y controles de la misma ventana, siempre en perfil aislado. */
public final class RevisionAudio {
 private final JuegoCliente juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
 private final ClienteSketch app=new ClienteSketch(juego);
 private final Path evid=Path.of(System.getProperty("ad.audio.evidencias"));
 private void exigir(boolean ok,String mensaje){if(!ok)throw new IllegalStateException(mensaje);}
 private void click(int x,int y){app.mouseX=x;app.mouseY=y;app.mouseMoved();app.mousePressed();app.mouseReleased();}
 private void tecla(int codigo){app.keyCode=codigo;app.key=(char)0;app.keyPressed();app.keyReleased();}
 public void pre(){try{int f=app.frameCount;
  if(f==10){juego.movimientoReducido=true;juego.accion("ayuda");}
  if(f==20)click(1000,660);
  if(f==30){exigir(juego.pantalla==JuegoCliente.Pantalla.AUDIO,"No abrió Audio");app.getSurface().setSize(960,600);}
  if(f==45){click(520,230);exigir(juego.audio.actual().musica()==65,"No aplicó volumen mediante clic reescalado");app.get().save(evid.resolve("audio-960x600.png").toString());}
  if(f==55)click(760,338);
  if(f==70){tecla(KeyEvent.VK_ESCAPE);exigir(juego.pantalla==JuegoCliente.Pantalla.AYUDA,"Esc no volvió a ayuda");juego.accion("volver");juego.progreso.desbloquearTodas();juego.accion("jugar");juego.accion("mision1");juego.accion("m1dificultad:FACIL");}
  if(f==80)tecla(KeyEvent.VK_ESCAPE);
  if(f==90)juego.misionUno.solicitar("audio-1","continuar");
  if(f==110)juego.accion("pausa");
  if(f==120)alcaldedigital.cliente.audio.MotorAudio.global().interfaz("hover");
  if(f==135)juego.accion("reanudar");
  if(f==160){juego.abrir(JuegoCliente.Pantalla.MISIONES);juego.accion("mision2");juego.accion("m2dificultad:NORMAL");}
  if(f==220){juego.abrir(JuegoCliente.Pantalla.MISIONES);juego.accion("mision8");}
  if(f==250){var campo=ClienteSketch.class.getDeclaredField("misionOcho");campo.setAccessible(true);var m=(alcaldedigital.cliente.mision8.MisionOchoVista)campo.get(app);m.elegirFinal(0);}
  if(f==260){click(130,490);}
  if(f==290){var campo=ClienteSketch.class.getDeclaredField("misionOcho");campo.setAccessible(true);var vista=(alcaldedigital.cliente.mision8.MisionOchoVista)campo.get(app);var m=vista.motor();if(m!=null){m.escena=alcaldedigital.cliente.mision8.MotorOcho.Escena.COMBATE;m.inyectado=true;m.ganador=0;m.publicado=0;}}
  if(f==360)app.get().save(evid.resolve("combate-audio.png").toString());
 }catch(Exception ex){ex.printStackTrace();System.exit(1);}}
 public void post(){if(app.frameCount==450){System.out.println("AUDIO_REVISION_OK");app.exit();}}
 public static void main(String[] args)throws Exception{if(System.getProperty("ad.guardados")==null)throw new IllegalStateException("Exige perfil temporal");var r=new RevisionAudio();Files.createDirectories(r.evid);r.app.registerMethod("pre",r);r.app.registerMethod("post",r);PApplet.runSketch(new String[]{ClienteSketch.class.getName()},r.app);}
}
