package alcaldedigital.cliente.misiones;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.entrada.MapaControles;
import alcaldedigital.cliente.interfaz.MarcosProcessing;
import processing.core.*;import java.util.*;
import static java.awt.event.KeyEvent.*;
/** Un único menú y protocolo de entrada para todas las misiones. La sesión conserva la autoridad. */
public final class PausaUnificada {
 public interface Sesion {
  int numero();boolean lista();boolean pausada();void pausar();void reanudar();String guardar()throws Exception;void salir();
  default boolean consultaAbierta(){return true;}default boolean tieneConsultas(){return false;}default void abrirConsulta(){}default void cerrarConsulta(){}
  default MapaControles.Contexto controles(){return new MapaControles.Contexto(numero(),false,"Jugador 1","Jugador 2","");}
 }
 private final alcaldedigital.cliente.audio.InterfazSonora audioUI=new alcaldedigital.cliente.audio.InterfazSonora();
 private Sesion sesion;private boolean visible,ayuda,consulta,controles,esperarNeutro;private int foco,pagina;
 private String mensaje="";private float segundos,autoguardado;private final Set<Integer> retenidas=new HashSet<>(),bloqueadas=new HashSet<>();
 private EstadoMando anterior=EstadoMando.desconectado();
 public void conectar(Sesion s){if(sesion==null||sesion.numero()!=s.numero()){visible=ayuda=consulta=controles=false;foco=pagina=0;mensaje="";esperarNeutro=true;autoguardado=0;}sesion=s;}
 public void desconectar(){sesion=null;visible=ayuda=consulta=controles=false;}
 public boolean activa(){return visible;}public boolean consulta(){return consulta;}
 public void actualizar(float dt){if(sesion==null)return;segundos=Math.max(0,segundos-dt);if(sesion.lista()&&!visible){autoguardado+=dt;if(autoguardado>=10){autoguardado=0;guardar(false);}}if(consulta&&!sesion.consultaAbierta())consulta=false;if(visible&&!sesion.pausada()){if(consulta)sesion.cerrarConsulta();visible=ayuda=consulta=controles=false;bloqueadas.addAll(retenidas);esperarNeutro=true;}if(sesion.lista()&&sesion.pausada()&&!visible){visible=true;bloqueadas.addAll(retenidas);esperarNeutro=true;}}
 public void abrir(){if(sesion==null||!sesion.lista())return;sesion.pausar();visible=true;ayuda=consulta=controles=false;foco=0;bloqueadas.addAll(retenidas);esperarNeutro=true;guardar(false);}
 private void continuar(){sesion.reanudar();if(!sesion.pausada()){visible=ayuda=consulta=controles=false;}bloqueadas.addAll(retenidas);esperarNeutro=true;}
 public void guardar(){guardar(true);}
 private void guardar(boolean feedback){if(sesion==null||!sesion.lista())return;try{mensaje=sesion.guardar();if(feedback)alcaldedigital.cliente.audio.MotorAudio.global().interfaz("guardar");}catch(Exception e){mensaje="No se pudo guardar: "+e.getMessage();}segundos=5;}
 private void activar(){alcaldedigital.cliente.audio.InterfazSonora.activar(foco==0?"reanudar":foco==2?"guardar":"click");switch(foco){case 0->continuar();case 1->{ayuda=true;pagina=0;}case 2->guardar();case 3->{if(sesion.tieneConsultas()){consulta=true;ayuda=false;sesion.abrirConsulta();}else{mensaje="Esta misión explica sus mecánicas en Ayuda.";segundos=4;}}case 4->{try{mensaje=sesion.guardar();sesion.salir();visible=false;}catch(Exception e){mensaje="No se pudo guardar: "+e.getMessage();segundos=8;}}default->{}}}
 public boolean pulsar(int k){boolean nueva=retenidas.add(k);if(sesion==null||!sesion.lista())return false;
  if(!nueva||bloqueadas.contains(k))return true;
  if(controles){if(k==VK_ESCAPE||k==VK_ENTER||k==VK_E||k==VK_H||k==VK_P){controles=false;}return true;}
  if(k==VK_ESCAPE||k==VK_P){if(consulta){sesion.cerrarConsulta();consulta=false;}else if(ayuda)ayuda=false;else if(visible)continuar();else abrir();return true;}
  if(k==VK_G){guardar();return true;}
  if(k==VK_H){if(consulta){sesion.cerrarConsulta();consulta=false;}if(!visible)abrir();ayuda=!ayuda;return true;}
  if(consulta)return false;
  if(k==VK_T||k==VK_V){if(sesion.tieneConsultas()){if(!visible)abrir();consulta=true;sesion.abrirConsulta();return true;}}
  if(!visible)return false;
  if(consulta)return false;
  if(ayuda){if(k==VK_LEFT)pagina=Math.max(0,pagina-1);if(k==VK_RIGHT)pagina++;if(k==VK_ENTER||k==VK_C)controles=true;else if(k==VK_E)ayuda=false;return true;}
  int focoAntes=foco;if(k==VK_UP||k==VK_W)foco=Math.floorMod(foco-1,5);if(k==VK_DOWN||k==VK_S||k==VK_TAB)foco=(foco+1)%5;
  if(foco!=focoAntes)audioUI.foco("pausa:"+foco);if(k==VK_ENTER||k==VK_E||k==VK_SPACE)activar();return true;
 }
 public void soltar(int k){retenidas.remove(k);bloqueadas.remove(k);}
 public boolean raton(int x,int y){if(!visible||consulta)return false;
  if(controles){if(x>=PantallaControles.VOLVER_X&&x<PantallaControles.VOLVER_X+PantallaControles.VOLVER_ANCHO&&y>=PantallaControles.VOLVER_Y&&y<PantallaControles.VOLVER_Y+PantallaControles.VOLVER_ALTO)controles=false;return true;}
  if(ayuda){if(x>=480&&x<800&&y>=555&&y<600)controles=true;else if(y>=605&&y<=655){if(x<400)pagina=Math.max(0,pagina-1);else if(x>900)pagina++;else ayuda=false;}return true;}
  if(x>=380&&x<900)for(int i=0;i<5;i++)if(y>=340+i*57&&y<384+i*57){foco=i;activar();break;}return true;
 }
 private void teclaMando(int k){pulsar(k);soltar(k);}
 public boolean mando(EstadoMando e){
  if(sesion==null||!sesion.lista()){anterior=e;return false;}
  if(anterior.conectado()&&!e.conectado())abrir();
  for(var b:new EstadoMando.Boton[]{EstadoMando.Boton.MENU,EstadoMando.Boton.B,EstadoMando.Boton.VISTA})if(e.pulsado(b)&&!anterior.pulsado(b)){
   teclaMando(b==EstadoMando.Boton.VISTA?VK_H:VK_ESCAPE);anterior=e;return true;}
  if(visible){
   if(e.pulsado(EstadoMando.Boton.A)&&!anterior.pulsado(EstadoMando.Boton.A))teclaMando(VK_ENTER);
   if(e.pulsado(EstadoMando.Boton.ARRIBA)&&!anterior.pulsado(EstadoMando.Boton.ARRIBA)||e.izquierdaY()<-.6&&anterior.izquierdaY()>=-.6)teclaMando(VK_UP);
   if(e.pulsado(EstadoMando.Boton.ABAJO)&&!anterior.pulsado(EstadoMando.Boton.ABAJO)||e.izquierdaY()>.6&&anterior.izquierdaY()<=.6)teclaMando(VK_DOWN);
   if(e.pulsado(EstadoMando.Boton.IZQUIERDA)&&!anterior.pulsado(EstadoMando.Boton.IZQUIERDA))teclaMando(VK_LEFT);
   if(e.pulsado(EstadoMando.Boton.DERECHA)&&!anterior.pulsado(EstadoMando.Boton.DERECHA))teclaMando(VK_RIGHT);
   anterior=e;return !consulta;
  }
  if(esperarNeutro){if(e.botones().isEmpty()&&Math.abs(e.izquierdaX())<.3&&Math.abs(e.izquierdaY())<.3)esperarNeutro=false;anterior=e;return true;}
  anterior=e;return false;
 }
 public void perderFoco(){if(sesion!=null&&sesion.lista())abrir();}
 public void dibujar(PApplet p){if(sesion==null)return;String hover="";if(visible&&!consulta&&!ayuda&&!controles&&p.mouseX>=380&&p.mouseX<900)for(int i=0;i<5;i++)if(p.mouseY>=340+i*57&&p.mouseY<384+i*57)hover="pausa:"+i;audioUI.hover(hover);
  if(visible&&controles){PantallaControles.dibujar(p,sesion.controles());return;}
  if(visible&&!consulta){p.pushStyle();p.noStroke();p.fill(13,8,24,225);p.rect(0,0,1280,720);
   MarcosProcessing.marco(p.g,320,105,640,555,0xff38264b,0xfff4cb6a);p.textAlign(PApplet.LEFT,PApplet.BASELINE);p.fill(244,203,106);p.textSize(34);p.text("PAUSA · MISIÓN "+sesion.numero(),355,155);
   p.fill(245,232,251);p.textSize(17);
   if(ayuda){dibujarAyuda(p);}else{
    p.text("La partida está detenida. G guarda sin salir.",355,200);p.text("Suelta las teclas y centra el mando para continuar.",355,235);
    String[] opciones={"Continuar","Ayuda y controles","Guardar partida","Consultas de la misión","Guardar y salir a misiones"};
    for(int i=0;i<5;i++){MarcosProcessing.marco(p.g,380,340+i*57,520,44,i==foco?0xfff4cb6a:0xff432b5b,0xffbd91d5);p.fill(i==foco?0xff21152f:0xfff5e8fb);p.textSize(18);p.text(opciones[i],400,370+i*57);}
   }p.popStyle();
  }
  if(segundos>0&&!mensaje.isBlank()){p.pushStyle();MarcosProcessing.marco(p.g,230,20,820,55,0xff21152f,0xfff4cb6a);p.fill(245,232,251);p.textAlign(PApplet.LEFT,PApplet.BASELINE);p.textSize(17);p.text(mensaje,250,55);p.popStyle();}
 }
 private List<String[]> ayuda(){
  var paginas=new ArrayList<String[]>();paginas.add(new String[]{"Esc / P / Menú: pausa y reanuda.","H / Vista: ayuda · G: guardar partida.","La victoria desbloquea la siguiente misión.","Guardar y salir conserva el progreso alcanzado.","En LAN, guarda el servidor y la pausa es compartida."});
  String[] controles=switch(sesion.numero()){
   case 1->new String[]{"WASD / flechas: mover · E/A: hablar.","C/X: Civitas · T/Y: árbol y red.","Comprueba las fuentes y calma a los ocho vecinos.","Un desenlace BUENO cuenta como victoria."};
   case 2->new String[]{"WASD / stick: mover · E/A: reparar.","Shift/RB: correr · Espacio/X: salto.","Termina con los seis equipos seguros para ganar.","El guardado conserva reloj, ataques y salto."};
   case 3->new String[]{"WASD / stick: mover · E/A: hablar.","F/X/RT: PING · Q/LT: FLASH · Espacio/RB: DASH.","L/Y: LIVE BURST · Ctrl/LB: modo privado.","Se guarda al inicio de la zona actual.","Las zonas completadas permanecen superadas."};
   case 4->new String[]{"WASD / stick: mover y conducir · E/A: hablar.","F/X/RT: ataque · Q/Y/LT: escudo · Espacio/RB: esquiva.","V: investigar rumor · N: responder sin investigar.","Supera los tres combates y termina el epílogo."};
   case 5->new String[]{"Ciudadano: WASD/E/Espacio.","Periodista en dos: flechas/Enter/Shift.","Cinco rondas, quince ataques; sobrevive para ganar.","Cian: detén el movimiento real; rosado: esquiva.","Consulta las páginas siguientes para cada patrón."};
   case 8->new String[]{"A/D + Espacio: mover y salto doble · F/Q/E: ataques.","R: esquiva · J2: flechas, Mayús, L/K/Enter, J.","J3: numérico 4/6, 0, 1/2/3 y decimal.","J4 jefe: Z/X/C/B/N/M eligen los seis patrones.","T abre antecedentes, AVL, decisiones, grafo y controles."};
   case 6->new String[]{"Solo: WASD/flechas, F disparo, Q misil, Espacio impulso.","Dos: J1 Juan · WASD + F/Q + Espacio.","J2 Influencer · flechas + L/K + Mayús.","Mando J1: X/RT disparo, Y/LT misil, RB impulso.","Vence al dron y termina cualquiera de las entrevistas."};
   default->new String[]{"Espacio/Mayús/X: correr · W/S/flechas: carril.","Q/K/Y: ayuda · 1/2/3 o 7/8/9: noticia.","En solo debes llegar antes que la CPU.","La reputación y los apoyos siguen afectando la ciudad.","El empate no cuenta como victoria de campaña."};};
  paginas.add(controles);if(sesion.numero()==5)for(var a:alcaldedigital.cliente.mision5.GuiaBatalla.PATRONES)paginas.add(new String[]{a.nombre(),a.situacion(),a.respuesta()});return paginas;
 }
 private void dibujarAyuda(PApplet p){var paginas=ayuda();pagina=Math.floorMod(pagina,paginas.size());p.textSize(20);p.text("AYUDA · "+(pagina+1)+" / "+paginas.size(),355,210);p.textSize(17);int y=260;
  for(String l:paginas.get(pagina)){p.text(l,355,y,570,85);y+=62;}
  MarcosProcessing.marco(p.g,480,555,320,45,0xff432b5b,0xfff4cb6a);
  p.fill(245,232,251);p.textSize(19);p.text("Ver controles · Enter / A",505,585);
  p.fill(244,203,106);p.textSize(15);p.text("Izq.: anterior     E/Esc: volver     Der.: siguiente",355,630);
 }
}
