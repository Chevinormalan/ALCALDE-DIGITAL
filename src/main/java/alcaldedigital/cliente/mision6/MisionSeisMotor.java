package alcaldedigital.cliente.mision6;

import java.awt.event.KeyEvent;
import java.util.*;

/** Estado de campaña, exploración, combate y entrevista; independiente de Processing. */
public final class MisionSeisMotor {
 public enum Fase { DISCURSO, BUSCAR, ENCUENTRO, CAMINAR, MODO, DESPEGUE, AVISO, ENTRENAMIENTO, COMBATE, RETIRADA, LLEGADA, EXPLORAR_SALA, SALA, ENTREVISTA, RESPUESTA, FINAL, DERROTA }
 public enum Modo { SOLO, DUO }
 public enum Ruta { ESCUCHA, CONVICCION }
 public record Resultado(boolean completada,Modo modo,Ruta ruta,int integridad1,int integridad2,String partida,List<Integer> respuestas) {
  public Resultado(boolean c,Modo m,Ruta r,int i1,int i2){this(c,m,r,i1,i2,UUID.randomUUID().toString(),List.of());}
  public Resultado {respuestas=List.copyOf(respuestas);}
 }
 public static final String[] DISCURSO={
  "JUAN: Ciudad Nova merece propuestas que podamos cumplir, no rumores que nos dividan.",
  "VECINA: ¡Queremos una campaña que también escuche al barrio!",
  "JUAN: Tienen mi palabra. Una promesa solo vale si podemos mirarnos a los ojos y responder por ella."};
 public static final String[] INTRO={
  "INFLUENCER: Juan, vi tu discurso. Después del Núcleo Viral quería saber qué viene para la ciudad.",
  "JUAN: Tú enfrentaste una amenaza invisible. Yo acabo de cruzar una campaña llena de rumores.",
  "INFLUENCER: Entonces tenemos algo en común. Traje preguntas, pero también quiero escuchar a la gente.",
  "JUAN: Vamos a la casa de campaña. Por el camino te cuento mis propuestas sin filtros.",
  "INFLUENCER: Trato hecho. Pero si ese helicóptero se cae, quiero dejar claro que la idea fue tuya."};
 public static final String[] ADVERTENCIA={
  "INFLUENCER: Cuando una ciudad entera te mira, cada palabra pesa. Tal vez este vuelo sea una prueba.",
  "DRON VIRAL: ¡SEÑAL SATURADA! ¡CAMPAÑA DETECTADA! ¡OBJETIVO: CAOS!",
  "INFLUENCER: Es un dron de vigilancia infectado por la señal residual del Núcleo Viral. Disparemos a su enlace para cortar el control.",
  "JUAN: Primero practicamos las maniobras. Después rompemos esa interferencia y seguimos a la casa de campaña."};
 public static final String[] PREGUNTAS={
  "¿Qué pesa más: lo que la gente pide hoy o lo que crees que necesita mañana?",
  "¿Hasta dónde cederías si las encuestas te empujan a prometer más de lo que puedes cumplir?",
  "Después de todo lo que pasó hoy... ¿qué clase de alcalde quieres ser?"};
 public static final String[] TRANSMISION={
  "MR. PALOMO: ¡Mi dron! Autodestrucción activada. Vamos a crear una noticia interesante: «Juan, el candidato a la alcaldía, se va a morir». A ver qué hace la ciudad cuando empiece a circular.",
  "INFLUENCER: Eso no es una noticia, es una amenaza disfrazada de rumor. No vamos a dejar que el miedo decida por Ciudad Nova.",
  "JUAN: Entonces respondamos con hechos. En la casa de campaña hablaremos de frente, sin filtros ni mentiras."};
 public static final String[][] RESPUESTAS={
  {"Primero escucho; después propongo. Las necesidades se convierten en soluciones.","Escuchar es clave, pero también debo sostener convicciones aunque no sean populares."},
  {"No prometería por likes. Prefiero construir confianza con metas realistas.","No me moveré por presión del momento. Mi deber es defender un plan serio."},
  {"Uno que escuche incluso en medio del caos.","Uno que marque el rumbo, incluso cuando el ruido quiera desviarlo."}};
 public static final class Nave {
  public float x,y,invulnerable,arma,misil,impulso,recargaImpulso,flash,herida,curacion; public int vida=5; public boolean activa=true;
  Nave(float x,float y){this.x=x;this.y=y;}
 }
 public static final class Proyectil {
  public float x,y,vx,vy,edad; public int dano,dueno; public boolean enemigo,especial;
  Proyectil(float x,float y,float vx,float vy,int dano,int dueno,boolean enemigo,boolean especial){this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.dano=dano;this.dueno=dueno;this.enemigo=enemigo;this.especial=especial;}
 }
 public static final class Corazon {
  public float x,y,edad;
  public Corazon(float x,float y){this.x=x;this.y=y;}
 }
 public Fase fase=Fase.DISCURSO; public Modo modo=Modo.SOLO; public Nave[] naves={new Nave(170,270),new Nave(170,390)};
 public final List<Proyectil> proyectiles=new ArrayList<>();private final Set<Integer> teclas=new HashSet<>();
 public final List<Corazon> corazones=new ArrayList<>();public float proximoCorazon=3,salaX=820,salaY=480;public int corazonesCreados;
 public float tiempo,global,jefeX=800,jefeY=280,ataqueJefe=1.4f,choque,impacto,impactoX,impactoY;
 public float jugadorX=600,jugadorY=435,juanX=225,juanY=355,pasos,textoVisible=0,ayudaTiempo;
 public int direccion=0,energia=180,pagina,pregunta,seleccion,escucha;public boolean terminado,practicoMover,practicoDisparo;
 public String partida=UUID.randomUUID().toString();public final int[] respuestas={-1,-1,-1};
 public int etapa(){return energia>120?1:energia>60?2:3;}
 public String linea(){
  return switch(fase){case DISCURSO->DISCURSO[Math.min(pagina,DISCURSO.length-1)];case ENCUENTRO->INTRO[Math.min(pagina,INTRO.length-1)];case AVISO->ADVERTENCIA[Math.min(pagina,ADVERTENCIA.length-1)];
   case RETIRADA->TRANSMISION[Math.min(pagina,TRANSMISION.length-1)];
   case SALA->pagina==0?"INFLUENCER: Ya estamos sentados, Juan. Después de la amenaza de Mr. Palomo, la ciudad necesita respuestas claras.":"JUAN: Haz tus preguntas. Para liderar esta ciudad también debo saber conversar cuando baja el ruido.";
   case RESPUESTA->"JUAN: "+RESPUESTAS[pregunta][seleccion];default->"";};
 }
 public String interlocutor(){String l=linea();int i=l.indexOf(':');return i<0?"NARRADOR":l.substring(0,i);}
 public String contenido(){String l=linea();int i=l.indexOf(':');return i<0?l:l.substring(i+1).trim();}
 public String textoMostrado(){String s=contenido();return s.substring(0,Math.min(s.length(),(int)textoVisible));}
 private boolean avanzarTexto(){if(textoVisible<contenido().length()){textoVisible=contenido().length();return false;}textoVisible=0;return true;}
 public void comenzar(Modo m){modo=Objects.requireNonNull(m);reiniciarVuelo();pregunta=seleccion=escucha=0;Arrays.fill(respuestas,-1);terminado=false;ir(Fase.DESPEGUE);}
 private void reiniciarVuelo(){partida=UUID.randomUUID().toString();naves=new Nave[]{new Nave(170,270),new Nave(170,390)};proyectiles.clear();corazones.clear();proximoCorazon=3;corazonesCreados=0;teclas.clear();energia=180;global=0;jefeX=800;jefeY=280;ataqueJefe=1.4f;choque=0;impacto=0;practicoMover=practicoDisparo=false;}
 private void ir(Fase f){fase=f;tiempo=0;pagina=0;textoVisible=0;teclas.clear();}
 public boolean cercaDeJuan(){return Math.hypot(jugadorX-juanX,jugadorY-juanY)<74;}
 public boolean cercaDelHelicoptero(){return Math.hypot(jugadorX-825,jugadorY-430)<100;}
 public boolean juanListo(){return Math.hypot(juanX-825,juanY-430)<150;}
 public static final float SOFA_X=650,SOFA_Y=455;
 public boolean cercaDelSofa(){return Math.hypot(salaX-SOFA_X,salaY-SOFA_Y)<80;}
 public static boolean sueloSala(float x,float y){return x>=145&&x<=900&&y>=350&&y<=520&&!(x>435&&x<830&&y<438)&&!(x>250&&x<410&&y<455)&&!(x<270&&y<420);}
 private void moverSala(float dt){
  int dx=(pulsada(KeyEvent.VK_D)||pulsada(KeyEvent.VK_RIGHT)?1:0)-(pulsada(KeyEvent.VK_A)||pulsada(KeyEvent.VK_LEFT)?1:0);
  int dy=(pulsada(KeyEvent.VK_S)||pulsada(KeyEvent.VK_DOWN)?1:0)-(pulsada(KeyEvent.VK_W)||pulsada(KeyEvent.VK_UP)?1:0);
  if(dx==0&&dy==0)return;float norm=dx!=0&&dy!=0?.7071f:1;direccion=Math.abs(dx)>Math.abs(dy)?(dx<0?2:3):(dy<0?1:0);
  float nx=salaX+dx*155*norm*dt,ny=salaY+dy*155*norm*dt;boolean movio=false;
  if(sueloSala(nx,salaY)){salaX=nx;movio=true;}if(sueloSala(salaX,ny)){salaY=ny;movio=true;}if(movio)pasos+=dt*9;
 }
 private void actualizarCorazones(float dt){
  proximoCorazon-=dt;
  if(proximoCorazon<=0){
   Nave objetivo=naves[0];for(int i=0;i<(modo==Modo.SOLO?1:2);i++)if(naves[i].activa&&(!objetivo.activa||naves[i].vida<objetivo.vida))objetivo=naves[i];
   if(corazones.size()<3&&objetivo.activa){corazones.add(new Corazon(Math.min(680,Math.max(330,objetivo.x+200)),Math.max(180,Math.min(490,objetivo.y))));corazonesCreados++;}
   proximoCorazon=6;
  }
  for(int i=corazones.size()-1;i>=0;i--){Corazon c=corazones.get(i);c.x-=62*dt;c.edad+=dt;boolean recogido=false;
   for(int j=0;j<(modo==Modo.SOLO?1:2);j++){Nave n=naves[j];if(n.activa&&n.vida<5&&Math.abs(n.x-c.x)<43&&Math.abs(n.y-c.y)<35){n.vida++;n.curacion=1.2f;recogido=true;break;}}
   if(recogido||c.x<30||c.edad>16)corazones.remove(i);
  }
 }
 private boolean libre(float x,float y){return x>=32&&x<=990&&y>=115&&y<=520&&!(x>125&&x<491&&y<306)&&!(x<118&&y<400)&&!(x>903&&y<288);}
 private void moverCampana(float dt){
  int dx=(pulsada(KeyEvent.VK_D)||pulsada(KeyEvent.VK_RIGHT)?1:0)-(pulsada(KeyEvent.VK_A)||pulsada(KeyEvent.VK_LEFT)?1:0);
  int dy=(pulsada(KeyEvent.VK_S)||pulsada(KeyEvent.VK_DOWN)?1:0)-(pulsada(KeyEvent.VK_W)||pulsada(KeyEvent.VK_UP)?1:0);
  if(dx!=0||dy!=0){float norm=dx!=0&&dy!=0?.7071f:1;float ox=jugadorX,oy=jugadorY;direccion=Math.abs(dx)>Math.abs(dy)?(dx<0?2:3):(dy<0?1:0);
   float nx=jugadorX+dx*155*norm*dt,ny=jugadorY+dy*155*norm*dt;if(libre(nx,jugadorY))jugadorX=nx;if(libre(jugadorX,ny))jugadorY=ny;
   if(ox!=jugadorX||oy!=jugadorY)pasos+=dt*9;
  }
  if(fase==Fase.CAMINAR){float dxj=jugadorX-juanX,dyj=jugadorY-juanY;float dist=(float)Math.hypot(dxj,dyj);if(dist>52){float step=Math.min(dist-52,135*dt);float nx=juanX+dxj/dist*step,ny=juanY+dyj/dist*step;if(libre(nx,juanY))juanX=nx;if(libre(juanX,ny))juanY=ny;}}
 }
 public void actualizar(float dt){
  dt=Math.max(0,Math.min(.05f,dt));tiempo+=dt;global+=dt;impacto=Math.max(0,impacto-dt);ayudaTiempo=Math.max(0,ayudaTiempo-dt);
  if(fase==Fase.DISCURSO||fase==Fase.ENCUENTRO||fase==Fase.AVISO||fase==Fase.RETIRADA||fase==Fase.SALA||fase==Fase.RESPUESTA)textoVisible=Math.min(contenido().length(),textoVisible+dt*48);
  if(fase==Fase.BUSCAR||fase==Fase.CAMINAR){moverCampana(dt);return;}
  if(fase==Fase.EXPLORAR_SALA){moverSala(dt);return;}
  if(fase==Fase.DESPEGUE&&tiempo>=5){ir(Fase.AVISO);return;}
  if(fase==Fase.LLEGADA&&tiempo>=6){salaX=800;salaY=465;direccion=2;ir(Fase.EXPLORAR_SALA);return;}
  if(fase==Fase.ENTRENAMIENTO){for(int i=0;i<(modo==Modo.SOLO?1:2);i++)actualizarNave(naves[i],i,dt);for(int i=proyectiles.size()-1;i>=0;i--){Proyectil p=proyectiles.get(i);p.x+=p.vx*dt;p.y+=p.vy*dt;p.edad+=dt;if(p.x>1074||p.edad>3)proyectiles.remove(i);}return;}
  if(fase!=Fase.COMBATE)return;
  jefeX=810+(float)Math.sin(global*.65)*65;jefeY=282+(float)Math.sin(global*(etapa()==1?1.3:2.1))*155;
  ataqueJefe-=dt;choque=Math.max(0,choque-dt);
  if(ataqueJefe<=0){dispararJefe();ataqueJefe=etapa()==1?1.55f:etapa()==2?1.1f:.85f;}
  for(int i=0;i<(modo==Modo.SOLO?1:2);i++)actualizarNave(naves[i],i,dt);
  actualizarCorazones(dt);
  for(int i=proyectiles.size()-1;i>=0;i--){
   Proyectil p=proyectiles.get(i);p.edad+=dt;p.x+=p.vx*dt;p.y+=p.vy*dt;
   if(p.x < -50||p.x>1074||p.y < 55||p.y>570||p.edad>8){proyectiles.remove(i);continue;}
   if(!p.enemigo&&Math.hypot(p.x-jefeX,p.y-jefeY)<(p.especial?80:64)){
    impactoX=p.x;impactoY=p.y;impacto=p.especial?.5f:.15f;energia=Math.max(0,energia-p.dano);proyectiles.remove(i);if(energia==0){proyectiles.clear();corazones.clear();ir(Fase.RETIRADA);return;}continue;
   }
   if(p.enemigo){for(int n=0;n<(modo==Modo.SOLO?1:2);n++){Nave v=naves[n];if(v.activa&&v.invulnerable<=0&&Math.abs(p.x-v.x)<37&&Math.abs(p.y-v.y)<19){golpear(v);proyectiles.remove(i);break;}}}
  }
  if(etapa()==3&&choque<=0){for(int i=0;i<(modo==Modo.SOLO?1:2);i++){Nave n=naves[i];if(n.activa&&n.invulnerable<=0&&Math.hypot(n.x-jefeX,n.y-jefeY)<76){golpear(n);choque=.5f;}}}
  if(!naves[0].activa&&(modo==Modo.SOLO||!naves[1].activa))ir(Fase.DERROTA);
 }
 private void actualizarNave(Nave n,int jugador,float dt){
  if(!n.activa)return;
  n.curacion=Math.max(0,n.curacion-dt);n.invulnerable=Math.max(0,n.invulnerable-dt);n.arma=Math.max(0,n.arma-dt);n.misil=Math.max(0,n.misil-dt);n.impulso=Math.max(0,n.impulso-dt);n.recargaImpulso=Math.max(0,n.recargaImpulso-dt);n.flash=Math.max(0,n.flash-dt);n.herida=Math.max(0,n.herida-dt);
  boolean uno=jugador==0,solo=modo==Modo.SOLO;
  int dx=(pulsada(uno?KeyEvent.VK_D:KeyEvent.VK_RIGHT)||(uno&&solo&&pulsada(KeyEvent.VK_RIGHT))?1:0)-(pulsada(uno?KeyEvent.VK_A:KeyEvent.VK_LEFT)||(uno&&solo&&pulsada(KeyEvent.VK_LEFT))?1:0);
  int dy=(pulsada(uno?KeyEvent.VK_S:KeyEvent.VK_DOWN)||(uno&&solo&&pulsada(KeyEvent.VK_DOWN))?1:0)-(pulsada(uno?KeyEvent.VK_W:KeyEvent.VK_UP)||(uno&&solo&&pulsada(KeyEvent.VK_UP))?1:0);
  if(fase==Fase.ENTRENAMIENTO&&jugador==0&&(dx!=0||dy!=0))practicoMover=true;
  float speed=n.impulso>0?440:220;float norma=dx!=0&&dy!=0?.7071f:1;
  n.x=Math.max(60,Math.min(930,n.x+dx*speed*norma*dt));n.y=Math.max(115,Math.min(505,n.y+dy*speed*norma*dt+(fase==Fase.COMBATE&&etapa()==2?(float)Math.sin(global*1.6f)*22*dt:0)));
  boolean disparar=uno?(pulsada(KeyEvent.VK_F)||pulsada(KeyEvent.VK_J)||(solo&&pulsada(KeyEvent.VK_Z))):pulsada(KeyEvent.VK_L);
  if(disparar&&n.arma<=0){proyectiles.add(new Proyectil(n.x+48,n.y,570,0,modo==Modo.SOLO?5:3,jugador,false,false));n.arma=.19f;n.flash=.09f;if(fase==Fase.ENTRENAMIENTO&&jugador==0)practicoDisparo=true;}
 }
 public boolean estaCaminando(){return pulsada(KeyEvent.VK_W)||pulsada(KeyEvent.VK_A)||pulsada(KeyEvent.VK_S)||pulsada(KeyEvent.VK_D)||pulsada(KeyEvent.VK_UP)||pulsada(KeyEvent.VK_DOWN)||pulsada(KeyEvent.VK_LEFT)||pulsada(KeyEvent.VK_RIGHT);}
 private boolean pulsada(int k){return teclas.contains(k);}
 private void especial(int i){Nave n=naves[i];if(!n.activa||n.misil>0||etapa()<2)return;proyectiles.add(new Proyectil(n.x+46,n.y,400,0,18,i,false,true));n.misil=4;n.flash=.18f;}
 private void impulsar(int i){Nave n=naves[i];if(!n.activa||n.recargaImpulso>0)return;n.impulso=.28f;n.invulnerable=Math.max(n.invulnerable,.33f);n.recargaImpulso=2;}
 private void golpear(Nave n){n.vida--;n.invulnerable=modo==Modo.SOLO?1.8f:1.3f;n.herida=.35f;if(n.vida<=0)n.activa=false;}
 private void dispararJefe(){int count=etapa()==1?3:etapa()==2?5:7;for(int i=0;i<count;i++){float ang=(i-(count-1)/2f)*.19f;float speed=etapa()==3?220:180;proyectiles.add(new Proyectil(jefeX-30,jefeY,(float)-Math.cos(ang)*speed,(float)Math.sin(ang)*speed,1,-1,true,false));}}
 public void pulsar(int k){
  boolean confirmar=k==KeyEvent.VK_ENTER||k==KeyEvent.VK_E||k==KeyEvent.VK_SPACE;
  if(fase==Fase.DISCURSO){if(confirmar&&avanzarTexto()&&++pagina>=DISCURSO.length)ir(Fase.BUSCAR);return;}
  if(fase==Fase.ENCUENTRO){if(confirmar&&avanzarTexto()&&++pagina>=INTRO.length)ir(Fase.CAMINAR);return;}
  if(fase==Fase.BUSCAR||fase==Fase.CAMINAR){
   if(confirmar){if(fase==Fase.BUSCAR&&cercaDeJuan())ir(Fase.ENCUENTRO);else if(fase==Fase.CAMINAR&&cercaDelHelicoptero()&&juanListo())ir(Fase.MODO);else ayudaTiempo=2.5f;return;}
   teclas.add(k);return;
  }
  if(fase==Fase.MODO){if(k==KeyEvent.VK_LEFT||k==KeyEvent.VK_RIGHT||k==KeyEvent.VK_UP||k==KeyEvent.VK_DOWN||k==KeyEvent.VK_TAB||k==KeyEvent.VK_A||k==KeyEvent.VK_D||k==KeyEvent.VK_W||k==KeyEvent.VK_S)seleccion=1-seleccion;else if(k==KeyEvent.VK_1)comenzar(Modo.SOLO);else if(k==KeyEvent.VK_2)comenzar(Modo.DUO);else if(k==KeyEvent.VK_ENTER||k==KeyEvent.VK_SPACE)comenzar(seleccion==0?Modo.SOLO:Modo.DUO);return;}
  if(fase==Fase.DERROTA){if(k==KeyEvent.VK_R||k==KeyEvent.VK_ENTER){reiniciarVuelo();ir(Fase.ENTRENAMIENTO);}return;}
  if(fase==Fase.AVISO){if(confirmar&&avanzarTexto()&&++pagina>=ADVERTENCIA.length)ir(Fase.ENTRENAMIENTO);return;}
  if(fase==Fase.RETIRADA){if(confirmar&&avanzarTexto()&&++pagina>=TRANSMISION.length)ir(Fase.LLEGADA);return;}
  if(fase==Fase.EXPLORAR_SALA){if(confirmar){if(cercaDelSofa())ir(Fase.SALA);else ayudaTiempo=2.5f;}else teclas.add(k);return;}
  if(fase==Fase.SALA){if(confirmar&&avanzarTexto()&&++pagina>=2)ir(Fase.ENTREVISTA);return;}
  if(fase==Fase.ENTREVISTA){if(k==KeyEvent.VK_LEFT||k==KeyEvent.VK_RIGHT||k==KeyEvent.VK_UP||k==KeyEvent.VK_DOWN||k==KeyEvent.VK_TAB||k==KeyEvent.VK_A||k==KeyEvent.VK_D||k==KeyEvent.VK_W||k==KeyEvent.VK_S)seleccion=1-seleccion;else if(k==KeyEvent.VK_1){seleccion=0;responder();}else if(k==KeyEvent.VK_2){seleccion=1;responder();}else if(k==KeyEvent.VK_ENTER||k==KeyEvent.VK_SPACE)responder();return;}
  if(fase==Fase.RESPUESTA){if(confirmar&&avanzarTexto()){pregunta++;if(pregunta>=PREGUNTAS.length)ir(Fase.FINAL);else ir(Fase.ENTREVISTA);}return;}
  if(fase==Fase.FINAL){if(k==KeyEvent.VK_ENTER||k==KeyEvent.VK_SPACE)terminado=true;return;}
  if(fase==Fase.ENTRENAMIENTO&&k==KeyEvent.VK_ENTER){reiniciarVuelo();ir(Fase.COMBATE);return;}
  if(fase!=Fase.COMBATE&&fase!=Fase.ENTRENAMIENTO)return;
  boolean nueva=teclas.add(k);if(!nueva)return;
  if(k==KeyEvent.VK_Q||modo==Modo.SOLO&&(k==KeyEvent.VK_K||k==KeyEvent.VK_X))especial(0);
  if(modo==Modo.DUO&&(k==KeyEvent.VK_K||k==KeyEvent.VK_M))especial(1);
  if(k==KeyEvent.VK_SPACE||modo==Modo.SOLO&&k==KeyEvent.VK_SHIFT)impulsar(0);
  if(modo==Modo.DUO&&(k==KeyEvent.VK_SHIFT||k==KeyEvent.VK_SLASH||k==KeyEvent.VK_NUMPAD0))impulsar(1);
 }
 private void responder(){respuestas[pregunta]=seleccion;if(seleccion==0)escucha++;ir(Fase.RESPUESTA);}
 public void soltar(int k){teclas.remove(k);}public void perderFoco(){teclas.clear();}
 public Ruta ruta(){return escucha>=2?Ruta.ESCUCHA:Ruta.CONVICCION;}
 public Resultado resultado(){return new Resultado(terminado,modo,ruta(),naves[0].vida,modo==Modo.DUO?naves[1].vida:-1,partida,Arrays.stream(respuestas).boxed().toList());}
 /** Captura una copia independiente; las teclas retenidas nunca se persisten. */
 public Properties capturar(){
  Properties p=new Properties();p.setProperty("version","2");p.setProperty("fase",fase.name());p.setProperty("modo",modo.name());
  p.setProperty("partida",partida);for(int i=0;i<respuestas.length;i++)p.setProperty("respuesta."+i,Integer.toString(respuestas[i]));
  poner(p,"salaX",salaX);poner(p,"salaY",salaY);poner(p,"proximoCorazon",proximoCorazon);poner(p,"corazonesCreados",corazonesCreados);
  poner(p,"corazones",corazones.size());for(int i=0;i<corazones.size();i++){var c=corazones.get(i);poner(p,"c"+i+".x",c.x);poner(p,"c"+i+".y",c.y);poner(p,"c"+i+".edad",c.edad);}
  poner(p,"tiempo",tiempo);poner(p,"global",global);poner(p,"jefeX",jefeX);poner(p,"jefeY",jefeY);poner(p,"ataque",ataqueJefe);poner(p,"choque",choque);
  poner(p,"impacto",impacto);poner(p,"impactoX",impactoX);poner(p,"impactoY",impactoY);poner(p,"jugadorX",jugadorX);poner(p,"jugadorY",jugadorY);
  poner(p,"juanX",juanX);poner(p,"juanY",juanY);poner(p,"pasos",pasos);poner(p,"textoVisible",textoVisible);poner(p,"ayudaTiempo",ayudaTiempo);
  poner(p,"direccion",direccion);poner(p,"energia",energia);poner(p,"pagina",pagina);poner(p,"pregunta",pregunta);poner(p,"seleccion",seleccion);poner(p,"escucha",escucha);
  // Un cierre ya confirmado se puede volver a contemplar al recuperar la partida.
  poner(p,"terminado",false);poner(p,"practicoMover",practicoMover);poner(p,"practicoDisparo",practicoDisparo);
  for(int i=0;i<2;i++){Nave n=naves[i];String k="n"+i+".";poner(p,k+"x",n.x);poner(p,k+"y",n.y);poner(p,k+"invulnerable",n.invulnerable);poner(p,k+"arma",n.arma);poner(p,k+"misil",n.misil);poner(p,k+"impulso",n.impulso);poner(p,k+"recarga",n.recargaImpulso);poner(p,k+"flash",n.flash);poner(p,k+"herida",n.herida);poner(p,k+"vida",n.vida);poner(p,k+"activa",n.activa);}
  poner(p,"proyectiles",proyectiles.size());for(int i=0;i<proyectiles.size();i++){Proyectil b=proyectiles.get(i);String k="p"+i+".";poner(p,k+"x",b.x);poner(p,k+"y",b.y);poner(p,k+"vx",b.vx);poner(p,k+"vy",b.vy);poner(p,k+"edad",b.edad);poner(p,k+"dano",b.dano);poner(p,k+"dueno",b.dueno);poner(p,k+"enemigo",b.enemigo);poner(p,k+"especial",b.especial);}
  return p;
 }
 private static void poner(Properties p,String k,Object v){p.setProperty(k,String.valueOf(v));}
 private static int entero(Properties p,String k,int min,int max){int n=Integer.parseInt(Objects.requireNonNull(p.getProperty(k)));if(n<min||n>max)throw new IllegalArgumentException(k);return n;}
 private static float real(Properties p,String k,float min,float max){float n=Float.parseFloat(Objects.requireNonNull(p.getProperty(k)));if(!Float.isFinite(n)||n<min||n>max)throw new IllegalArgumentException(k);return n;}
 private static boolean bandera(Properties p,String k){String n=Objects.requireNonNull(p.getProperty(k));if(!n.equals("true")&&!n.equals("false"))throw new IllegalArgumentException(k);return Boolean.parseBoolean(n);}
 public static MisionSeisMotor restaurar(Properties p){
  try{
   if(p==null||(!"1".equals(p.getProperty("version"))&&!"2".equals(p.getProperty("version"))))return null;
   MisionSeisMotor m=new MisionSeisMotor();m.fase=Fase.valueOf(p.getProperty("fase"));m.modo=Modo.valueOf(p.getProperty("modo"));
   m.partida=p.getProperty("partida",m.partida);
   for(int i=0;i<m.respuestas.length;i++){
    m.respuestas[i]=Integer.parseInt(p.getProperty("respuesta."+i,"-1"));
    if(m.respuestas[i]<-1||m.respuestas[i]>1)return null;
   }
   if("2".equals(p.getProperty("version"))){
    m.salaX=real(p,"salaX",0,1024);m.salaY=real(p,"salaY",0,576);m.proximoCorazon=real(p,"proximoCorazon",0,6);m.corazonesCreados=entero(p,"corazonesCreados",0,1000000);
    int corazones=entero(p,"corazones",0,3);for(int i=0;i<corazones;i++){var c=new Corazon(real(p,"c"+i+".x",0,1100),real(p,"c"+i+".y",0,576));c.edad=real(p,"c"+i+".edad",0,16);m.corazones.add(c);}
   }else if(m.fase==Fase.SALA){m.fase=Fase.EXPLORAR_SALA;}
   m.tiempo=real(p,"tiempo",0,1000000);m.global=real(p,"global",0,1000000);m.jefeX=real(p,"jefeX",0,1100);m.jefeY=real(p,"jefeY",0,600);m.ataqueJefe=real(p,"ataque",-2,10);m.choque=real(p,"choque",0,10);
   m.impacto=real(p,"impacto",0,5);m.impactoX=real(p,"impactoX",-100,1200);m.impactoY=real(p,"impactoY",-100,700);
   m.jugadorX=real(p,"jugadorX",0,1024);m.jugadorY=real(p,"jugadorY",0,576);m.juanX=real(p,"juanX",0,1024);m.juanY=real(p,"juanY",0,576);m.pasos=real(p,"pasos",0,1000000);m.textoVisible=real(p,"textoVisible",0,10000);m.ayudaTiempo=real(p,"ayudaTiempo",0,10);
   m.direccion=entero(p,"direccion",0,3);m.energia=entero(p,"energia",0,180);m.pagina=entero(p,"pagina",0,20);m.pregunta=entero(p,"pregunta",0,3);m.seleccion=entero(p,"seleccion",0,1);m.escucha=entero(p,"escucha",0,3);
   m.terminado=bandera(p,"terminado");m.practicoMover=bandera(p,"practicoMover");m.practicoDisparo=bandera(p,"practicoDisparo");
   if((m.fase==Fase.ENTREVISTA||m.fase==Fase.RESPUESTA)&&m.pregunta>=PREGUNTAS.length)return null;
   if((m.fase==Fase.DISCURSO&&m.pagina>=DISCURSO.length)||(m.fase==Fase.ENCUENTRO&&m.pagina>=INTRO.length)||(m.fase==Fase.AVISO&&m.pagina>=ADVERTENCIA.length)||(m.fase==Fase.SALA&&m.pagina>=2))return null;
   if(m.fase==Fase.RETIRADA&&m.pagina>=TRANSMISION.length)return null;
   for(int i=0;i<2;i++){String k="n"+i+".";Nave n=m.naves[i];n.x=real(p,k+"x",0,1024);n.y=real(p,k+"y",0,576);n.invulnerable=real(p,k+"invulnerable",0,10);n.arma=real(p,k+"arma",0,10);n.misil=real(p,k+"misil",0,10);n.impulso=real(p,k+"impulso",0,10);n.recargaImpulso=real(p,k+"recarga",0,10);n.flash=real(p,k+"flash",0,10);n.herida=real(p,k+"herida",0,10);n.vida=entero(p,k+"vida",0,5);n.activa=bandera(p,k+"activa");if(n.activa!=(n.vida>0))return null;}
   int total=entero(p,"proyectiles",0,400);m.proyectiles.clear();for(int i=0;i<total;i++){String k="p"+i+".";float x=real(p,k+"x",-100,1200),y=real(p,k+"y",-100,700),vx=real(p,k+"vx",-1000,1000),vy=real(p,k+"vy",-1000,1000);float edad=real(p,k+"edad",0,10);int dano=entero(p,k+"dano",0,100),dueno=entero(p,k+"dueno",-1,1);boolean enemigo=bandera(p,k+"enemigo"),especial=bandera(p,k+"especial");if(enemigo!=(dueno==-1))return null;Proyectil b=new Proyectil(x,y,vx,vy,dano,dueno,enemigo,especial);b.edad=edad;m.proyectiles.add(b);}
   if(m.fase==Fase.EXPLORAR_SALA&&!sueloSala(m.salaX,m.salaY)){m.salaX=800;m.salaY=465;}
   m.teclas.clear();return m;
  }catch(RuntimeException ex){return null;}
 }
}
