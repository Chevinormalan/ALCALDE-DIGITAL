package alcaldedigital.cliente.mision6;

import java.awt.event.KeyEvent;
import java.util.*;

/** Estado de campaña, exploración, combate y entrevista; independiente de Processing. */
public final class MisionSeisMotor {
 public enum Fase { DISCURSO, BUSCAR, ENCUENTRO, CAMINAR, MODO, DESPEGUE, AVISO, ENTRENAMIENTO, COMBATE, RETIRADA, LLEGADA, EXPLORAR_SALA, SALA, ENTREVISTA, RESPUESTA, FINAL, DERROTA }
 public enum Modo { SOLO, DUO }
 public enum Ruta { ESCUCHA, CONVICCION }
 public enum Ataque { ABANICO, DIRIGIDO, RAFAGA, HALO }
 public record Aviso(Ataque tipo,float x,float y,float angulo,int objetivo) {}
 static final float ESCENARIO_X=330,ESCENARIO_Y=220;
 static final float HELICOPTERO_X=741,HELICOPTERO_Y=408,EMBARQUE_X=775,EMBARQUE_Y=401;
 static final float ENTRADA_SALA_X=174,ENTRADA_SALA_Y=475,JUAN_SALA_X=708,JUAN_SALA_Y=455;
 static final float DIANA_X=810,DIANA_Y=300,RADIO_HALO=98;
 private static final float[][] DESCENSO={{257,259},{188,324},{225,355}};
 private static final float[][] PASEO={{246,357},{415,365},{592,379},{740,388}};
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
 public final List<Corazon> corazones=new ArrayList<>();public float proximoCorazon=3,salaX=ENTRADA_SALA_X,salaY=ENTRADA_SALA_Y;public int corazonesCreados;
 public float tiempo,global,jefeX=800,jefeY=280,ataqueJefe=1.4f,choque,impacto,impactoX,impactoY;
 public float jugadorX=600,jugadorY=435,juanX=ESCENARIO_X,juanY=ESCENARIO_Y,pasos,textoVisible=0,ayudaTiempo;
 public int direccion=0,energia=180,pagina,pregunta,seleccion,escucha;public boolean terminado,practicoMover,practicoDisparo;
 float pasosJuan;
 int tramoJuan=DESCENSO.length,direccionJuan,aciertosPractica;
 boolean juanCaminando;
 private boolean influencerCaminando;
 int ataquesLanzados,etapaAtaque=1;
 Aviso avisoDron;
 public String partida=UUID.randomUUID().toString();public final int[] respuestas={-1,-1,-1};
 public int etapa(){return energia>120?1:energia>60?2:3;}
 public String linea(){
  return switch(fase){case DISCURSO->DISCURSO[Math.min(pagina,DISCURSO.length-1)];case ENCUENTRO->INTRO[Math.min(pagina,INTRO.length-1)];case AVISO->ADVERTENCIA[Math.min(pagina,ADVERTENCIA.length-1)];
   case RETIRADA->TRANSMISION[Math.min(pagina,TRANSMISION.length-1)];
   case SALA->pagina==0?"INFLUENCER: Ya estamos sentados, Juan. Después de la amenaza de Mr. Palomo, la ciudad necesita respuestas claras.":"JUAN: Haz tus preguntas. Para liderar esta ciudad también debo saber conversar cuando baja el ruido.";
   case RESPUESTA->"JUAN: "+RESPUESTAS[pregunta][seleccion];
   case FINAL->pagina==1?"INFLUENCER: Una ciudad no se salva solo peleando contra el caos. También se salva decidiendo qué hacer cuando baja el ruido.":
    ruta()==Ruta.ESCUCHA?"JUAN: Gobernar es escuchar, ajustar y construir con la gente. Las propuestas deben crecer con la ciudad.":
     "JUAN: Gobernar también exige sostener ideas firmes y explicarlas con valentía. Escuchar no significa renunciar al rumbo.";
   default->"";};
 }
 public String interlocutor(){String l=linea();int i=l.indexOf(':');return i<0?"NARRADOR":l.substring(0,i);}
 public String contenido(){String l=linea();int i=l.indexOf(':');return i<0?l:l.substring(i+1).trim();}
 public String textoMostrado(){String s=contenido();return s.substring(0,Math.min(s.length(),(int)textoVisible));}
 private boolean avanzarTexto(){if(textoVisible<contenido().length()){textoVisible=contenido().length();return false;}textoVisible=0;return true;}
 public void comenzar(Modo m){modo=Objects.requireNonNull(m);reiniciarVuelo();pregunta=seleccion=escucha=0;Arrays.fill(respuestas,-1);terminado=false;ir(Fase.DESPEGUE);}
 private void reiniciarVuelo(){partida=UUID.randomUUID().toString();naves=new Nave[]{new Nave(170,270),new Nave(170,390)};proyectiles.clear();corazones.clear();proximoCorazon=3;corazonesCreados=0;teclas.clear();energia=180;global=0;jefeX=800;jefeY=280;ataqueJefe=1.4f;choque=0;impacto=0;avisoDron=null;ataquesLanzados=0;etapaAtaque=1;aciertosPractica=0;practicoMover=practicoDisparo=false;}
 private void ir(Fase f){fase=f;tiempo=0;pagina=0;textoVisible=0;juanCaminando=false;perderFoco();}
 public boolean cercaDeJuan(){return tramoJuan>=DESCENSO.length&&Math.hypot(jugadorX-juanX,jugadorY-juanY)<74;}
 public boolean cercaDelHelicoptero(){return Math.hypot(jugadorX-EMBARQUE_X,jugadorY-EMBARQUE_Y)<76;}
 public boolean juanListo(){return tramoJuan>=PASEO.length;}
 int tramoCharla(){return tramoJuan<2?0:tramoJuan<3?1:2;}
 public static final float SOFA_X=650,SOFA_Y=455;
 public boolean cercaDelSofa(){return Math.hypot(salaX-SOFA_X,salaY-SOFA_Y)<80;}
 public static boolean sueloSala(float x,float y){
  return x>=145&&x<=900&&y>=350&&y<=520
    &&!dentro(x,y,145,355,238,422)&&!dentro(x,y,250,353,410,451)
    &&!dentro(x,y,435,350,616,430)&&!dentro(x,y,455,350,824,437)
    &&!dentro(x,y,820,350,900,387);
 }
 private static boolean dentro(float x,float y,float izquierda,float arriba,float derecha,float abajo){
  return x>=izquierda&&x<=derecha&&y>=arriba&&y<=abajo;
 }
 private void moverSala(float dt){
  int dx=(pulsada(KeyEvent.VK_D)||pulsada(KeyEvent.VK_RIGHT)?1:0)-(pulsada(KeyEvent.VK_A)||pulsada(KeyEvent.VK_LEFT)?1:0);
  int dy=(pulsada(KeyEvent.VK_S)||pulsada(KeyEvent.VK_DOWN)?1:0)-(pulsada(KeyEvent.VK_W)||pulsada(KeyEvent.VK_UP)?1:0);
  float ox=salaX,oy=salaY,norm=dx!=0&&dy!=0?.7071f:1;
  float nx=salaX+dx*155*norm*dt,ny=salaY+dy*155*norm*dt;
  if(sueloSala(nx,salaY))salaX=nx;if(sueloSala(salaX,ny))salaY=ny;
  animarInfluencer(salaX-ox,salaY-oy,dt);
 }
 private void actualizarCorazones(float dt){
  proximoCorazon-=dt;
  if(proximoCorazon<=0&&(etapa()==1||avisoDron==null)&&choque<=0){
   Nave objetivo=naves[0];for(int i=0;i<(modo==Modo.SOLO?1:2);i++)if(naves[i].activa&&(!objetivo.activa||naves[i].vida<objetivo.vida))objetivo=naves[i];
   if(corazones.size()<3&&objetivo.activa){
    float x=Math.clamp(objetivo.x+200,330,680);
    corazones.add(new Corazon(x,alturaCorazon(x,Math.clamp(objetivo.y,180,490))));
    corazonesCreados++;
   }
   proximoCorazon=6;
  }else proximoCorazon=Math.max(0,proximoCorazon);
  for(int i=corazones.size()-1;i>=0;i--){Corazon c=corazones.get(i);c.x-=62*dt;c.edad+=dt;boolean recogido=false;
   for(int j=0;j<(modo==Modo.SOLO?1:2);j++){Nave n=naves[j];if(n.activa&&n.vida<5&&Math.abs(n.x-c.x)<43&&Math.abs(n.y-c.y)<35){n.vida++;n.curacion=1.2f;recogido=true;break;}}
   if(recogido||c.x<30||c.edad>16)corazones.remove(i);
  }
 }
 private float alturaCorazon(float x,float base){
  float elegida=base,mejor=-1;
  for(int i=-1;i<=1;i++){
   float y=Math.clamp(base+i*68,180,490),margen=55;
   for(Proyectil p:proyectiles){
    if(!p.enemigo||Math.abs(p.vx+62)<1)continue;
    float cruce=(x-p.x)/(p.vx+62);
    if(cruce>=0&&cruce<=2)margen=Math.min(margen,Math.abs(y-p.y-p.vy*cruce));
   }
   float valor=margen-Math.abs(y-base)*.18f;
   if(valor>mejor){mejor=valor;elegida=y;}
  }
  return elegida;
 }
 private boolean libre(float x,float y){
  return x>=32&&x<=990&&y>=115&&y<=520
    &&!dentro(x,y,130,115,471,302)&&!dentro(x,y,32,115,118,400)
    &&!dentro(x,y,801,139,935,284)&&!dentro(x,y,835,304,968,455)
    &&!dentro(x,y,354,454,607,484)&&!dentro(x,y,750,454,856,486)
    &&!dentro(x,y,444,223,545,315)&&!dentro(x,y,606,210,712,307);
 }
 private void moverCampana(float dt){
  int dx=(pulsada(KeyEvent.VK_D)||pulsada(KeyEvent.VK_RIGHT)?1:0)-(pulsada(KeyEvent.VK_A)||pulsada(KeyEvent.VK_LEFT)?1:0);
  int dy=(pulsada(KeyEvent.VK_S)||pulsada(KeyEvent.VK_DOWN)?1:0)-(pulsada(KeyEvent.VK_W)||pulsada(KeyEvent.VK_UP)?1:0);
  float ox=jugadorX,oy=jugadorY,norm=dx!=0&&dy!=0?.7071f:1;
  float nx=jugadorX+dx*155*norm*dt,ny=jugadorY+dy*155*norm*dt;
  if(libre(nx,jugadorY))jugadorX=nx;if(libre(jugadorX,ny))jugadorY=ny;
  animarInfluencer(jugadorX-ox,jugadorY-oy,dt);
  actualizarCaminoJuan(dt);
 }
 private void animarInfluencer(float dx,float dy,float dt){
  influencerCaminando=dx!=0||dy!=0;
  if(!influencerCaminando)return;
  direccion=Math.abs(dx)>Math.abs(dy)?(dx<0?2:3):(dy<0?1:0);
  pasos+=dt*9;
 }
 private void actualizarCaminoJuan(float dt){
  boolean baja=fase==Fase.BUSCAR;
  float[][] camino=baja?DESCENSO:PASEO;
  juanCaminando=false;
  if(tramoJuan>=camino.length||!baja&&Math.hypot(jugadorX-juanX,jugadorY-juanY)>235)return;
  float dx=camino[tramoJuan][0]-juanX,dy=camino[tramoJuan][1]-juanY;
  float distancia=(float)Math.hypot(dx,dy),paso=Math.min(distancia,(baja?146:145)*dt);
  if(distancia>0){
   juanX+=dx*paso/distancia;juanY+=dy*paso/distancia;
   direccionJuan=Math.abs(dx)>Math.abs(dy)?(dx<0?2:3):(dy<0?1:0);
   pasosJuan+=paso/15;
   juanCaminando=paso>0;
  }
  if(paso>=distancia)tramoJuan++;
 }
 public void actualizar(float dt){
  dt=Math.max(0,Math.min(.05f,dt));tiempo+=dt;global+=dt;impacto=Math.max(0,impacto-dt);ayudaTiempo=Math.max(0,ayudaTiempo-dt);
  if(fase==Fase.DISCURSO||fase==Fase.ENCUENTRO||fase==Fase.AVISO||fase==Fase.RETIRADA||fase==Fase.SALA||fase==Fase.RESPUESTA||fase==Fase.FINAL)textoVisible=Math.min(contenido().length(),textoVisible+dt*48);
  if(fase==Fase.BUSCAR||fase==Fase.CAMINAR){moverCampana(dt);return;}
  if(fase==Fase.EXPLORAR_SALA){moverSala(dt);return;}
  if(fase==Fase.DESPEGUE&&tiempo>=5){ir(Fase.AVISO);return;}
  if(fase==Fase.LLEGADA&&tiempo>=6){salaX=ENTRADA_SALA_X;salaY=ENTRADA_SALA_Y;direccion=3;ir(Fase.EXPLORAR_SALA);return;}
  if(fase==Fase.ENTRENAMIENTO){actualizarPractica(dt);return;}
  if(fase!=Fase.COMBATE)return;
  actualizarDron(dt);
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
  if(choque>0){for(int i=0;i<(modo==Modo.SOLO?1:2);i++){Nave n=naves[i];if(n.activa&&n.invulnerable<=0&&Math.hypot(n.x-jefeX,n.y-jefeY)<RADIO_HALO)golpear(n);}}
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
 public boolean estaCaminando(){return influencerCaminando;}
 private boolean pulsada(int k){return teclas.contains(k);}
 private void especial(int i){Nave n=naves[i];if(!n.activa||n.misil>0||etapa()<2)return;proyectiles.add(new Proyectil(n.x+46,n.y,400,0,18,i,false,true));n.misil=4;n.flash=.18f;}
 private void impulsar(int i){Nave n=naves[i];if(!n.activa||n.recargaImpulso>0)return;n.impulso=.28f;n.invulnerable=Math.max(n.invulnerable,.33f);n.recargaImpulso=2;}
 private void golpear(Nave n){n.vida--;n.invulnerable=modo==Modo.SOLO?1.8f:1.3f;n.herida=.35f;if(n.vida<=0)n.activa=false;}
 private void actualizarPractica(float dt){
  for(int i=0;i<(modo==Modo.SOLO?1:2);i++)actualizarNave(naves[i],i,dt);
  for(int i=proyectiles.size()-1;i>=0;i--){
   Proyectil p=proyectiles.get(i);p.x+=p.vx*dt;p.y+=p.vy*dt;p.edad+=dt;
   if(Math.hypot(p.x-DIANA_X,p.y-DIANA_Y)<33){
    aciertosPractica=Math.min(999,aciertosPractica+1);
    impacto=.24f;impactoX=DIANA_X;impactoY=DIANA_Y;proyectiles.remove(i);
   }else if(p.x>1074||p.edad>3)proyectiles.remove(i);
  }
 }
 private void actualizarDron(float dt){
  choque=Math.max(0,choque-dt);
  if(etapaAtaque!=etapa()){
   etapaAtaque=etapa();ataquesLanzados=0;avisoDron=null;ataqueJefe=Math.max(.85f,ataqueJefe);
  }
  if(avisoDron==null&&choque<=0){
   jefeX=810+(float)Math.sin(global*.65)*65;
   jefeY=282+(float)Math.sin(global*(etapa()==1?1.3:2.1))*155;
  }
  ataqueJefe-=dt;
  Ataque tipo=siguienteAtaque();
  if(avisoDron==null&&ataqueJefe<=duracionAviso(tipo))prepararAtaque(tipo);
  if(ataqueJefe>0)return;
  dispararJefe(avisoDron);
  avisoDron=null;
  ataquesLanzados++;
  ataqueJefe=switch(tipo){case ABANICO->1.55f;case DIRIGIDO->1.50f;case RAFAGA->1.30f;case HALO->1.75f;};
 }
 private Ataque siguienteAtaque(){
  return switch(etapa()){case 1->Ataque.ABANICO;case 2->Ataque.DIRIGIDO;default->ataquesLanzados%2==0?Ataque.RAFAGA:Ataque.HALO;};
 }
 static float duracionAviso(Ataque a){
  return switch(a){case ABANICO->.32f;case DIRIGIDO->.64f;case RAFAGA->.45f;case HALO->.68f;};
 }
 static int cantidad(Ataque a){return switch(a){case ABANICO,DIRIGIDO->3;case RAFAGA->5;case HALO->0;};}
 static float angulo(Aviso a,int i){
  float separacion=switch(a.tipo()){case ABANICO->.23f;case DIRIGIDO->.075f;case RAFAGA->.19f;case HALO->0;};
  return a.angulo()+(i-(cantidad(a.tipo())-1)/2f)*separacion;
 }
 private void prepararAtaque(Ataque tipo){
  int objetivo=-1;
  float x=jefeX-45,y=jefeY+16;
  float angulo=(float)Math.PI+(ataquesLanzados%2==0?.09f:-.09f);
  if(tipo==Ataque.DIRIGIDO){
   objetivo=modo==Modo.SOLO?0:ataquesLanzados%2;
   if(!naves[objetivo].activa)objetivo=1-objetivo;
   Nave n=naves[objetivo];
   angulo=(float)Math.atan2(n.y-y,n.x-x);
  }
  if(tipo==Ataque.HALO){x=jefeX;y=jefeY;}
  avisoDron=new Aviso(tipo,x,y,angulo,objetivo);
 }
 private void dispararJefe(Aviso a){
  if(a.tipo()==Ataque.HALO){choque=.34f;return;}
  float velocidad=switch(a.tipo()){case ABANICO->180;case DIRIGIDO->260;case RAFAGA->235;case HALO->0;};
  for(int i=0;i<cantidad(a.tipo());i++){
   float angulo=angulo(a,i);
   proyectiles.add(new Proyectil(a.x(),a.y(),(float)Math.cos(angulo)*velocidad,(float)Math.sin(angulo)*velocidad,1,-1,true,false));
  }
 }
 public void pulsar(int k){
  boolean confirmar=k==KeyEvent.VK_ENTER||k==KeyEvent.VK_E||k==KeyEvent.VK_SPACE;
  if(fase==Fase.DISCURSO){
   if(confirmar&&avanzarTexto()&&++pagina>=DISCURSO.length){
    juanX=ESCENARIO_X;juanY=ESCENARIO_Y;tramoJuan=0;ir(Fase.BUSCAR);
   }
   return;
  }
  if(fase==Fase.ENCUENTRO){if(confirmar&&avanzarTexto()&&++pagina>=INTRO.length){tramoJuan=0;ir(Fase.CAMINAR);}return;}
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
  if(fase==Fase.FINAL){if(confirmar&&avanzarTexto()){if(pagina==0)pagina=1;else terminado=true;}return;}
  if(fase==Fase.ENTRENAMIENTO&&k==KeyEvent.VK_ENTER){reiniciarVuelo();ir(Fase.COMBATE);return;}
  if(fase!=Fase.COMBATE&&fase!=Fase.ENTRENAMIENTO)return;
  boolean nueva=teclas.add(k);if(!nueva)return;
  if(k==KeyEvent.VK_Q||modo==Modo.SOLO&&(k==KeyEvent.VK_K||k==KeyEvent.VK_X))especial(0);
  if(modo==Modo.DUO&&(k==KeyEvent.VK_K||k==KeyEvent.VK_M))especial(1);
  if(k==KeyEvent.VK_SPACE||modo==Modo.SOLO&&k==KeyEvent.VK_SHIFT)impulsar(0);
  if(modo==Modo.DUO&&(k==KeyEvent.VK_SHIFT||k==KeyEvent.VK_SLASH||k==KeyEvent.VK_NUMPAD0))impulsar(1);
 }
 private void responder(){respuestas[pregunta]=seleccion;if(seleccion==0)escucha++;ir(Fase.RESPUESTA);}
 public void soltar(int k){teclas.remove(k);}public void perderFoco(){teclas.clear();influencerCaminando=false;}
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
  poner(p,"tramoJuan",tramoJuan);poner(p,"direccionJuan",direccionJuan);poner(p,"pasosJuan",pasosJuan);poner(p,"juanCaminando",juanCaminando);
  poner(p,"aciertosPractica",aciertosPractica);poner(p,"etapaAtaque",etapaAtaque);poner(p,"ataquesLanzados",ataquesLanzados);
  if(avisoDron!=null){
   poner(p,"aviso.tipo",avisoDron.tipo());poner(p,"aviso.x",avisoDron.x());poner(p,"aviso.y",avisoDron.y());
   poner(p,"aviso.angulo",avisoDron.angulo());poner(p,"aviso.objetivo",avisoDron.objetivo());
  }
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
   if(m.fase==Fase.RETIRADA&&m.pagina>=TRANSMISION.length||m.fase==Fase.FINAL&&m.pagina>=2)return null;
   if(p.containsKey("tramoJuan")){
    m.tramoJuan=entero(p,"tramoJuan",0,PASEO.length);m.direccionJuan=entero(p,"direccionJuan",0,3);
    m.pasosJuan=real(p,"pasosJuan",0,1000000);m.juanCaminando=bandera(p,"juanCaminando");
   }else if(m.fase==Fase.CAMINAR){
    m.tramoJuan=0;
    while(m.tramoJuan<PASEO.length&&m.juanX>PASEO[m.tramoJuan][0])m.tramoJuan++;
   }
   if(p.containsKey("etapaAtaque")){
    m.aciertosPractica=entero(p,"aciertosPractica",0,999);m.etapaAtaque=entero(p,"etapaAtaque",1,3);
    m.ataquesLanzados=entero(p,"ataquesLanzados",0,1000000);
    if(p.containsKey("aviso.tipo")){
     m.avisoDron=new Aviso(Ataque.valueOf(p.getProperty("aviso.tipo")),real(p,"aviso.x",0,1100),
       real(p,"aviso.y",0,600),real(p,"aviso.angulo",-8,8),entero(p,"aviso.objetivo",-1,1));
    }
   }else{
    m.etapaAtaque=m.etapa();
    m.choque=0;
   }
   for(int i=0;i<2;i++){String k="n"+i+".";Nave n=m.naves[i];n.x=real(p,k+"x",0,1024);n.y=real(p,k+"y",0,576);n.invulnerable=real(p,k+"invulnerable",0,10);n.arma=real(p,k+"arma",0,10);n.misil=real(p,k+"misil",0,10);n.impulso=real(p,k+"impulso",0,10);n.recargaImpulso=real(p,k+"recarga",0,10);n.flash=real(p,k+"flash",0,10);n.herida=real(p,k+"herida",0,10);n.vida=entero(p,k+"vida",0,5);n.activa=bandera(p,k+"activa");if(n.activa!=(n.vida>0))return null;}
   int total=entero(p,"proyectiles",0,400);m.proyectiles.clear();for(int i=0;i<total;i++){String k="p"+i+".";float x=real(p,k+"x",-100,1200),y=real(p,k+"y",-100,700),vx=real(p,k+"vx",-1000,1000),vy=real(p,k+"vy",-1000,1000);float edad=real(p,k+"edad",0,10);int dano=entero(p,k+"dano",0,100),dueno=entero(p,k+"dueno",-1,1);boolean enemigo=bandera(p,k+"enemigo"),especial=bandera(p,k+"especial");if(enemigo!=(dueno==-1))return null;Proyectil b=new Proyectil(x,y,vx,vy,dano,dueno,enemigo,especial);b.edad=edad;m.proyectiles.add(b);}
   if(m.fase==Fase.EXPLORAR_SALA&&!sueloSala(m.salaX,m.salaY)){m.salaX=ENTRADA_SALA_X;m.salaY=ENTRADA_SALA_Y;}
   m.teclas.clear();return m;
  }catch(RuntimeException ex){return null;}
 }
}
