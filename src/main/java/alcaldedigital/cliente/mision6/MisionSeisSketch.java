package alcaldedigital.cliente.mision6;

import processing.core.*;
import java.awt.Font;
import javax.imageio.ImageIO;
import java.util.*;

/** Presentación de la misión sobre un búfer del mismo PApplet anfitrión. */
public final class MisionSeisSketch {
 private static final int W=1024,H=576;
 private MisionSeisMotor game=new MisionSeisMotor();
 private final AudioMisionSeis sonido=new AudioMisionSeis();
 private final alcaldedigital.cliente.presentacion.JuanSprites juanSprites=new alcaldedigital.cliente.presentacion.JuanSprites();
 private AutoguardadoSeis guardado;private Properties guardada;private boolean elegirGuardado;private int focoGuardado;private long ultimoGuardado;private String firmaGuardada="";
 private final Map<String,PImage> recursos=new HashMap<>();
 private PGraphics canvas;private PFont fuente;private long previo;private float deuda;
 private boolean gestionExterna;
 public void gestionExterna(boolean v){gestionExterna=v;}
 public void guardarAhora()throws Exception{guardarSiCorresponde(true);if(guardado!=null)guardado.esperar();}
 public MisionSeisMotor motor(){return game;}
 private PImage imagen(String path){return recursos.computeIfAbsent(path,n->{
  try(var in=getClass().getResourceAsStream("/cliente/"+n+".png")){
   if(in==null)throw new IllegalStateException("Falta recurso de la misión 6: "+n);
   var im=ImageIO.read(in);if(im==null)throw new IllegalStateException("PNG inválido: "+n);return new PImage(im);
  }catch(java.io.IOException e){throw new IllegalStateException(e);}
 });}
 public void iniciarEn(PApplet parent){if(parent!=null){canvas=parent.createGraphics(1280,720,PApplet.JAVA2D);canvas.noSmooth();}
  try(var in=getClass().getResourceAsStream("/cliente/fuentes/PixelifySans.ttf")){
   if(in==null)throw new IllegalStateException("Falta la fuente de la campaña");
   fuente=new PFont(Font.createFont(Font.TRUETYPE_FONT,in).deriveFont(18f),false);
  }catch(java.io.IOException|java.awt.FontFormatException ex){throw new IllegalStateException(ex);}
  previo=0;deuda=0;
  if((!Boolean.getBoolean("ad.mision6.integracion")||Boolean.getBoolean("ad.mision6.pruebaGuardado"))&&!Boolean.getBoolean("ad.mision6.sinGuardar")){
   guardado=new AutoguardadoSeis(AutoguardadoSeis.ubicacion());guardada=guardado.cargar();elegirGuardado=guardada!=null;
  }
 }
 public PImage dibujarEnJuego(){
  if(canvas==null)throw new IllegalStateException("Misión 6 no iniciada");
  canvas.beginDraw();canvas.pushMatrix();canvas.scale(1.25f);canvas.textFont(fuente);dibujar(canvas);
  if(elegirGuardado){
   canvas.noStroke();canvas.fill(13,8,24,225);canvas.rect(0,0,W,H);panel(canvas,210,150,604,272);
   texto(canvas,"PARTIDA GUARDADA",245,203,26,0xfff4cb6a);
   texto(canvas,guardado.recuperado?"Se recuperó la copia anterior disponible.":"Puedes continuar desde tu último punto.",245,244,17,0xfff5e8fb);
   boton(canvas,245,277,534,48,"CONTINUAR  [ENTER]",focoGuardado==0);
   boton(canvas,245,338,534,48,"NUEVA PARTIDA  [N]",focoGuardado==1);
  }else if(guardado!=null&&guardado.fallo){canvas.noStroke();canvas.fill(145,32,58,230);canvas.rect(15,62,375,28,3);texto(canvas,"No se pudo leer o guardar la partida",25,82,13,0xffffffff);}
  else if(guardado!=null&&guardado.ultimoExito>0&&System.nanoTime()-guardado.ultimoExito<1_500_000_000L){canvas.noStroke();canvas.fill(33,21,47,220);canvas.rect(846,61,160,24,3);texto(canvas,"AUTOGUARDADO",856,78,12,0xfff4cb6a);}
  canvas.popMatrix();canvas.endDraw();if(!elegirGuardado)guardarSiCorresponde(false);return canvas;
 }
 public void elegirGuardado(boolean nueva){focoGuardado=0;pulsarTecla(nueva?'n':'\n',nueva?78:10);}
 public PImage fotograma(){return canvas;}
 public boolean eligiendoGuardado(){return elegirGuardado;}
 public void actualizar(float dt){if(elegirGuardado)return;deuda+=Math.min(.1f,Math.max(0,dt));
  for(int i=0;deuda>=1f/60&&i<6;i++,deuda-=1f/60){game.actualizar(1f/60);sonido.observar(game);}}
 public void prepararRevision(String fase){if(!Boolean.getBoolean("ad.mision46.revision"))return;
  elegirGuardado=false;game.fase=MisionSeisMotor.Fase.valueOf(fase);}

 public void pulsarTecla(char ch,int code){
  if(elegirGuardado){
   if(code==java.awt.event.KeyEvent.VK_UP||code==java.awt.event.KeyEvent.VK_DOWN||code==java.awt.event.KeyEvent.VK_TAB)focoGuardado=1-focoGuardado;
   else if(code==java.awt.event.KeyEvent.VK_N){game=new MisionSeisMotor();elegirGuardado=false;guardada=null;reanudar();guardarSiCorresponde(true);}
   else if(code==java.awt.event.KeyEvent.VK_ENTER||code==java.awt.event.KeyEvent.VK_SPACE){
    if(focoGuardado==1)game=new MisionSeisMotor();else {MisionSeisMotor restaurada=MisionSeisMotor.restaurar(guardada);if(restaurada!=null)game=restaurada;}
    elegirGuardado=false;guardada=null;reanudar();guardarSiCorresponde(true);
   }
   sonido.restablecer();return;
  }
  var antesFase=game.fase;int antesPagina=game.pagina,antesSeleccion=game.seleccion;float antesTexto=game.textoVisible;
  game.pulsar(code);sonido.observar(game);if(antesFase!=game.fase||antesPagina!=game.pagina||antesSeleccion!=game.seleccion||antesTexto!=game.textoVisible)sonido.confirmar();guardarSiCorresponde(false);
 }
 public void soltarTecla(int code){game.soltar(code);}
 public boolean enCombate(){return game.fase==MisionSeisMotor.Fase.COMBATE||game.fase==MisionSeisMotor.Fase.ENTRENAMIENTO;}
 public void perderFoco(){game.perderFoco();sonido.pausar();previo=0;deuda=0;if(!elegirGuardado)guardarSiCorresponde(true);} public void reanudar(){game.perderFoco();previo=0;deuda=0;}
 public boolean salidaSolicitada(){return game.terminado;} public MisionSeisMotor.Resultado resultado(){return game.resultado();}
 private void guardarSiCorresponde(boolean forzar){
  if(guardado==null||elegirGuardado||gestionExterna&&!forzar)return;
  String firma=game.fase+":"+game.pagina+":"+game.pregunta+":"+game.seleccion+":"+game.etapa()+":"+game.practicoMover+":"+game.practicoDisparo+":"+game.naves[0].vida+":"+game.naves[1].vida+":"+game.corazones.size();
  long ahora=System.nanoTime();if(forzar||!firma.equals(firmaGuardada)||ahora-ultimoGuardado>=2_000_000_000L){guardado.guardar(game.capturar());ultimoGuardado=ahora;firmaGuardada=firma;}
 }
 public void cerrar(){
  if(guardado!=null){if(!elegirGuardado)guardarSiCorresponde(true);if(!guardado.cerrarYEsperar())System.err.println("No se confirmó el autoguardado de la misión 6");guardado=null;}
  sonido.close();game.perderFoco();recursos.clear();canvas=null;
 }
 private void panel(PGraphics g,float x,float y,float w,float h){
  alcaldedigital.cliente.interfaz.MarcosProcessing.marco(g,x,y,w,h,0xff2f1e40,0xffbe91da);
 }
 private void texto(PGraphics g,String s,float x,float y,int tam,int color){g.fill(color);g.textAlign(PApplet.LEFT,PApplet.BASELINE);g.textSize(tam);g.text(s,x,y);}
 private void parrafo(PGraphics g,String s,float x,float y,float ancho,int tam,int color){
  g.fill(color);g.textSize(tam);g.textAlign(PApplet.LEFT,PApplet.TOP);g.text(s,x,y,ancho,200);g.textAlign(PApplet.LEFT,PApplet.BASELINE);
 }
 private void boton(PGraphics g,float x,float y,float w,float h,String s,boolean activo){
  alcaldedigital.cliente.interfaz.MarcosProcessing.marco(g,x,y,w,h,activo?0xfff4cb6a:0xff604077,0xffbd91d5);g.fill(activo?0xff21152f:0xfff5e8fb);g.textSize(16);g.textAlign(PApplet.CENTER,PApplet.CENTER);g.text(s,x+w/2,y+h/2);g.textAlign(PApplet.LEFT,PApplet.BASELINE);
 }
 private void dibujar(PGraphics g){
  switch(game.fase){
   case DISCURSO -> {dibujarPlaza(g,true);cuadroDialogo(g,game.interlocutor(),game.textoMostrado(),"ENTER · continuar");}
   case BUSCAR -> dibujarPlaza(g,false);
   case CAMINAR -> dibujarPlaza(g,false);
   case MODO -> dibujarModo(g);
   case ENCUENTRO -> {dibujarPlaza(g,false);cuadroDialogo(g,game.interlocutor(),game.textoMostrado(),"ENTER · continuar");}
   case DESPEGUE -> {cielo(g);float x=110+game.tiempo*110,y=420-game.tiempo*45;helicoptero(g,x,y,1,false,false);if(game.modo==MisionSeisMotor.Modo.DUO)helicoptero(g,x-100,y+55,.75f,false,false);cuadroDialogo(g,"INFLUENCER", "La gente cree que transmitir es fácil. Pero cuando una ciudad entera te mira, cada palabra pesa. Tal vez este vuelo sea una prueba.","Rumbo a la casa de campaña");}
      case AVISO -> {cielo(g);helicoptero(g,200,315,1,false,false);if(game.modo==MisionSeisMotor.Modo.DUO)helicoptero(g,150,225,.75f,false,false);dron(g,760,250,150);cuadroDialogo(g,game.interlocutor(),game.textoMostrado(),"ENTER · continuar");}
   case ENTRENAMIENTO -> dibujarEntrenamiento(g);
   case COMBATE -> dibujarCombate(g);
   case RETIRADA -> {cielo(g);helicoptero(g,205,355,1,false,false);if(game.modo==MisionSeisMotor.Modo.DUO)helicoptero(g,150,245,.75f,false,false);dibujarAutodestruccion(g);cuadroDialogo(g,game.interlocutor(),game.textoMostrado(),"TRANSMISIÓN INTERCEPTADA · ENTER para continuar");}
   case LLEGADA -> {fondo(g,"mision6/casa");float t=Math.min(1,game.tiempo/4);t=t*t*(3-2*t);float x=-90+602*t;float y=32+76*t;helicoptero(g,x,y,.68f,false,false);if(game.modo==MisionSeisMotor.Modo.DUO)helicoptero(g,x-105,y+9,.55f,false,false);panel(g,185,475,654,66);texto(g,"La casa de campaña · Ciudad Nova",220,516,20,0xfff4cb6a);}
   case EXPLORAR_SALA -> dibujarSalaLibre(g);
   case SALA -> {escenaSofa(g);cuadroDialogo(g,game.interlocutor(),game.textoMostrado(),"ENTER · continuar la charla");}
   case ENTREVISTA -> dibujarEntrevista(g,false);
   case RESPUESTA -> dibujarEntrevista(g,true);
   case FINAL -> dibujarFinal(g);
   case DERROTA -> {cielo(g);panel(g,185,155,654,290);texto(g,"LA SEÑAL SIGUE ACTIVA",235,213,30,0xfff4cb6a);parrafo(g,"La interferencia ha obligado a los helicópteros a retirarse. Pueden intentarlo de nuevo: el objetivo es desactivar el enlace del dron.",235,246,555,19,0xfff5e8fb);boton(g,322,354,380,53,"REINTENTAR  [R / ENTER]",true);}
  }
  cabecera(g);
 }
 private void fondo(PGraphics g,String name){g.image(imagen(name),0,0,W,H);}
 private void cabecera(PGraphics g){g.noStroke();g.fill(25,15,38,225);g.rect(0,0,W,54);g.fill(189,145,213);g.rect(0,53,W,2);texto(g,"CIUDAD NOVA  /  MISIÓN 6",20,21,13,0xfff4cb6a);texto(g,"Cielos de campaña",20,43,18,0xfff5e8fb);texto(g,"ESC / P · pausa y controles",780,33,14,0xfff4cb6a);}
 private void cielo(PGraphics g){
  float deriva=(float)Math.sin(game.global*.13)*16;
  g.image(imagen("mision6/cielo"),-20+deriva,0,W+40,H);
  g.noStroke();g.fill(28,18,51,42);g.rect(0,0,W,H);
 }
 private void fondoAzotea(PGraphics g){
  cielo(g);g.noStroke();g.fill(52,37,76);g.rect(0,345,W,231);g.fill(84,62,104);g.rect(0,343,W,12);
  for(int x=0;x<W;x+=52){g.fill(35,24,54);g.rect(x,326,7,35);g.fill(243,201,104);g.rect(x+1,326,5,4);}
  helicop(g,760,372,.9f);texto(g,"AZOTEA DE CAMPAÑA",35,86,15,0xfff4cb6a);
 }
 private void helicop(PGraphics g,float x,float y,float size){helicoptero(g,x,y,size,false,false);}
 private void helicoptero(PGraphics g,float x,float y,float size,boolean dano,boolean fuego){
  g.pushMatrix();g.translate(x,y+(float)Math.sin(game.global*4+x*.01)*2);g.scale(size);
  if(dano&&((int)(game.global*12)%2==0))g.tint(255,150,150);g.image(imagen("mision6/helicoptero"),-85,-48,170,96);g.noTint();
  g.stroke(216,207,235,190);g.strokeWeight(3);float rotor=70+(float)Math.sin(game.global*35)*16;g.line(-rotor,-34,rotor,-34);g.strokeWeight(1);g.line(-rotor*.8f,-38,rotor*.8f,-38);g.noStroke();
  if(fuego){g.fill(255,219,113,230);g.triangle(78,-3,108,-9,94,3);}
  g.popMatrix();
 }
 private void dron(PGraphics g,float x,float y,float size){
  // Atlas de 5 columnas y 3 filas: vuelo, disparo y daño. Mirada hacia el jugador.
  int fila=game.fase==MisionSeisMotor.Fase.RETIRADA||game.impacto>0?2:
      game.fase==MisionSeisMotor.Fase.COMBATE&&game.ataqueJefe>(game.etapa()==1?1.55f:game.etapa()==2?1.1f:.85f)-.28f?1:0;
  int frame=(int)(game.global*10)%5;String clave="dron-"+fila+"-"+frame;
  PImage sprite=recursos.get(clave);
  if(sprite==null){PImage atlas=imagen("mision6/dron_atlas");int cw=atlas.width/5,ch=atlas.height/3;
   sprite=atlas.get(frame*cw,fila*ch,cw,ch);recursos.put(clave,sprite);}
  float ancho=size*1.42f,alto=ancho*sprite.height/sprite.width;
  g.image(sprite,x-ancho/2,y-alto/2,ancho,alto);
 }
 private void dibujarAutodestruccion(PGraphics g){
  float t=game.tiempo,x=game.jefeX,y=game.jefeY;
  if(t<.75f){if((int)(t*22)%2==0)g.tint(255,130,120);dron(g,x+(float)Math.sin(t*70)*4,y,150);g.noTint();}
  if(t>=.5f&&t<2.3f){float p=(t-.5f)/1.8f,size=90+210*p;g.tint(255,255*(1-p));g.image(imagen("mision6/impacto"),x-size/2,y-size/2,size,size);g.noTint();
   g.noStroke();for(int i=0;i<9;i++){float ang=i*PApplet.TWO_PI/9;g.fill(244,203,106,255*(1-p));g.rect(x+(float)Math.cos(ang)*p*145,y+(float)Math.sin(ang)*p*100+35*p*p,6,4);}
  }
 }
 private void personajes(PGraphics g){
  g.noStroke();g.fill(20,12,29,125);g.ellipse(350,455,70,16);g.ellipse(530,455,70,16);
  juanSprites.dibujar(g,"idle",0,0,game.global,350,455,80,155);g.image(imagen("mision3/redisenio/player_front_idle"),480,315,95,140);
 }
 /** Mismo retrato lateral, nombre y texto progresivo utilizado por la misión 4. */
 private void cuadroDialogo(PGraphics g,String nombre,String contenido,String pie){
  panel(g,42,401,940,151);g.noStroke();g.fill(56,38,75);g.rect(59,416,92,99,3);
  if(nombre.equals("INFLUENCER"))g.image(imagen("mision3/redisenio/player_front_idle"),65,422,80,90);
  else if(nombre.equals("JUAN"))juanSprites.dibujar(g,"speech",0,0,game.global,105,513,80,92);
  else if(nombre.equals("MR. PALOMO"))g.image(imagen("mision3/redisenio/palomo_idle_1"),65,425,80,80);
  else if(nombre.equals("DRON VIRAL"))dron(g,105,465,65);
  else if(nombre.equals("VECINA"))g.image(imagen("mision4/vecino_4"),65,421,80,92);
  else g.image(imagen("arte/iconos/04_libreta"),74,433,60,60);
  texto(g,nombre,170,435,17,0xfff4cb6a);parrafo(g,contenido,170,448,780,18,0xfff5e8fb);texto(g,pie,170,535,12,0xffd6bfe2);
 }
 private void dibujarPlaza(PGraphics g,boolean discurso){
  fondo(g,"mision4/plaza_presentacion");
  for(int i=0;i<15;i++){
   float x=470+(i%5)*55+(i/5%2)*10,y=215+(i/5)*56;
   g.noStroke();g.fill(20,12,29,85);g.ellipse(x,y,22,7);
   g.image(imagen("mision4/vecino_"+(i%8)),x-18,y-45+(float)Math.sin(game.global*2+i)*.8f,36,45);
  }
  helicoptero(g,825,427,.9f,false,false);
  float jx=discurso?330:game.juanX,jy=discurso?220:game.juanY;
  g.noStroke();g.fill(20,12,29,110);g.ellipse(jx,jy,31,9);g.ellipse(game.jugadorX,game.jugadorY,31,9);
  if(jy<=game.jugadorY){dibujarJuanPlaza(g,jx,jy,discurso);dibujarInfluencer(g);}
  else{dibujarInfluencer(g);dibujarJuanPlaza(g,jx,jy,discurso);}
  if(game.fase==MisionSeisMotor.Fase.CAMINAR){
   String nombre=game.jugadorX<470?"JUAN":game.jugadorX<650?"INFLUENCER":"JUAN";
   String comentario=game.jugadorX<470?"Quiero que esta campaña vuelva a escuchar al barrio.":game.jugadorX<650?"Después del Núcleo Viral, verificar importa más que nunca.":"La casa está cerca. Allí responderé todas tus preguntas.";
   float bx=Math.max(20,Math.min(690,(game.jugadorX+game.juanX)/2-135));float by=Math.max(90,Math.min(game.jugadorY,game.juanY)-124);
   panel(g,bx,by,310,57);texto(g,nombre,bx+13,by+17,12,0xfff4cb6a);parrafo(g,comentario,bx+13,by+21,285,13,0xfff5e8fb);
  }
  if(!discurso&&game.fase!=MisionSeisMotor.Fase.ENCUENTRO){
   boolean buscar=game.fase==MisionSeisMotor.Fase.BUSCAR;
   boolean cerca=buscar?game.cercaDeJuan():game.cercaDelHelicoptero();
   float x=buscar?game.juanX:825,y=buscar?game.juanY-86:354;
   if(cerca){panel(g,x-105,y-26,210,35);texto(g,buscar?"ENTER · hablar":"ENTER · subir",x-72,y-3,15,0xfff4cb6a);}
   else {g.noStroke();g.fill(244,203,106,225);float saltito=(float)Math.sin(game.global*5)*3;g.triangle(x-8,y-20+saltito,x+8,y-20+saltito,x,y-9+saltito);}
   g.noStroke();g.fill(25,15,38,232);g.rect(0,538,W,38);
   String meta=buscar?"Acércate a Juan para hablar con él.":game.cercaDelHelicoptero()&&!game.juanListo()?"Espera a Juan junto al helicóptero.":"Acompaña a Juan al helicóptero y pulsa ENTER para subir.";
   texto(g,meta,20,562,14,0xfff5e8fb);texto(g,"WASD / flechas · caminar",780,562,13,0xffd6bfe2);
  }
 }
 private void dibujarJuanPlaza(PGraphics g,float x,float y,boolean discurso){
  boolean camina=game.fase==MisionSeisMotor.Fase.CAMINAR&&Math.hypot(game.jugadorX-game.juanX,game.jugadorY-game.juanY)>56;
  int dir=camina?(game.jugadorX<game.juanX?2:3):0;
  juanSprites.dibujar(g,discurso?"speech":camina?"walk":"idle",dir,game.global*7,game.global,x,y,50,68);
 }
 private void dibujarInfluencer(PGraphics g){
  dibujarInfluencerEn(g,game.jugadorX,game.jugadorY,game.fase==MisionSeisMotor.Fase.BUSCAR||game.fase==MisionSeisMotor.Fase.CAMINAR);
 }
 private void dibujarInfluencerEn(PGraphics g,float x,float y,boolean libre){
  String[] dirs={"front","back","left","right"};String dir=dirs[Math.max(0,Math.min(3,game.direccion))];
  String sprite=libre&&game.estaCaminando()?"player_"+dir+"_"+(1+(int)game.pasos%4):"player_"+dir+"_idle";
  g.image(imagen("mision3/redisenio/"+sprite),x-26,y-70,52,70);
 }
 private void dibujarEntrenamiento(PGraphics g){
  cielo(g);for(int i=0;i<(game.modo==MisionSeisMotor.Modo.SOLO?1:2);i++){var n=game.naves[i];helicoptero(g,n.x,n.y,.68f,false,n.flash>0);}
  for(var p:game.proyectiles){g.noStroke();g.fill(244,203,106);g.rect(p.x-8,p.y-2,17,4);}
  g.noFill();g.stroke(244,203,106,180);g.strokeWeight(3);g.ellipse(810,280,90,90);g.line(790,280,830,280);g.line(810,260,810,300);g.noStroke();
  panel(g,35,68,954,155);texto(g,"PRÁCTICA SEGURA · MOVERSE Y DISPARAR",58,98,20,0xfff4cb6a);
  texto(g,game.modo==MisionSeisMotor.Modo.SOLO?"Mueve: WASD / flechas    ·    Mantén F: disparar":"J1 Juan: WASD + F    ·    J2 Influencer: flechas + L",58,128,17,0xfff5e8fb);
  texto(g,"Toca corazones para curarte. El misil se habilita en la fase 2.",58,158,16,0xffd6bfe2);
  texto(g,(game.practicoMover?"✓":"○")+" mover   "+(game.practicoDisparo?"✓":"○")+" disparar   ·   ENTER: iniciar combate",58,194,16,0xfff4cb6a);
  g.fill(25,15,38,225);g.rect(0,540,W,36);texto(g,"Shift: impulso e invulnerabilidad breve · P / ESC: pausa",20,563,14,0xfff5e8fb);
 }
 private void dibujarModo(PGraphics g){
  cielo(g);helicoptero(g,300,176,1.05f,false,false);helicoptero(g,723,176,1.05f,false,false);
  panel(g,85,241,854,264);texto(g,"CIELOS DE CAMPAÑA",116,282,27,0xfff4cb6a);
  parrafo(g,"Ya están a bordo. Elige cómo jugar: juntos en un helicóptero o cada uno en su propia nave.",116,302,790,17,0xfff5e8fb);
  boton(g,116,386,380,50,"1 JUGADOR · una nave",game.seleccion==0);boton(g,526,386,380,50,"2 JUGADORES · dos naves",game.seleccion==1);
  texto(g,"Jugador 2: usa este mismo teclado · Influencer · flechas / Enter",116,460,14,0xffd6bfe2);
  texto(g,"Flechas / TAB para elegir · ENTER para comenzar",116,482,14,0xffd6bfe2);
 }
 private void dibujarCombate(PGraphics g){
  cielo(g);int etapa=game.etapa();if(etapa==3){g.noFill();g.stroke(220,121,255,160);g.strokeWeight(5);g.ellipse(game.jefeX,game.jefeY,168+(float)Math.sin(game.global*6)*9,168+(float)Math.sin(game.global*6)*9);g.noStroke();}
  if(etapa==2 && Math.sin(game.global*1.6)>.2){g.tint(190,150,255,65);dron(g,game.jefeX+60,game.jefeY+80,120);dron(g,game.jefeX-50,game.jefeY-80,120);g.noTint();}
  dron(g,game.jefeX,game.jefeY,150);
  for(var c:game.corazones){float y=c.y+(float)Math.sin(game.global*5)*3;corazon(g,c.x,y,29);texto(g,"+1 VIDA",c.x-23,y-23,11,0xffffbed7);}
  for(var p:game.proyectiles){
   g.noStroke();if(p.enemigo){g.fill(239,102,171);g.pushMatrix();g.translate(p.x,p.y);g.rotate(.5f);g.ellipse(0,0,17,8);g.fill(255,221,238);g.rect(-4,-1,8,2);g.popMatrix();}
   else if(p.especial){g.image(imagen("mision6/misil"),p.x-32,p.y-14,64,28);}
   else{g.fill(244,203,106);g.rect(p.x-8,p.y-2,17,4);g.fill(255,246,194);g.rect(p.x-5,p.y-1,12,2);}
  }
  if(game.impacto>0){float size=65+(1-game.impacto/.5f)*45;g.tint(255,Math.min(255,game.impacto*510));g.image(imagen("mision6/impacto"),game.impactoX-size/2,game.impactoY-size/2,size,size);g.noTint();}
  if(etapa==2){g.stroke(226,207,255,80);g.strokeWeight(2);for(int i=0;i<5;i++){float wx=(i*240-game.global*90)%1200;if(wx<0)wx+=1200;g.line(wx,170+i*55,wx+65,170+i*55);}g.noStroke();}
  for(int i=0;i<(game.modo==MisionSeisMotor.Modo.SOLO?1:2);i++){var n=game.naves[i];if(n.activa&&(n.invulnerable<=0||(int)(game.global*9)%2==0)){if(n.impulso>0){g.noFill();g.stroke(126,218,245,190);g.strokeWeight(3);g.ellipse(n.x,n.y,110,68);g.noStroke();}helicoptero(g,n.x,n.y,.68f,n.herida>0,n.flash>0);if(game.modo==MisionSeisMotor.Modo.DUO)texto(g,i==0?"J1 · JUAN":"J2 · INFLUENCER",n.x-40,n.y-46,11,0xfff5e8fb);}}
  for(int i=0;i<(game.modo==MisionSeisMotor.Modo.SOLO?1:2);i++){var n=game.naves[i];if(n.curacion>0)texto(g,"+1 VIDA",n.x-22,n.y-45,14,0xff9cf2c5);}
  panel(g,15,67,327,87);texto(g,"INTEGRIDAD",27,87,12,0xffd6bfe2);
  texto(g,game.etapa()<2?"MISIL F2":"MISIL",236,87,10,0xffd6bfe2);texto(g,"IMP.",294,87,10,0xffd6bfe2);
  for(int i=0;i<(game.modo==MisionSeisMotor.Modo.SOLO?1:2);i++){
   var n=game.naves[i];float y=100+i*24;texto(g,game.modo==MisionSeisMotor.Modo.SOLO?"JUAN + INFLUENCER":i==0?"JUAN":"INFLUENCER",27,y+8,11,0xfff5e8fb);
   for(int j=0;j<5;j++){if(j<n.vida)corazon(g,156+j*16,y+5,12);else{g.fill(105,68,94);g.rect(150+j*16,y+1,12,8,2);}}
   g.fill(83,58,105);g.rect(236,y,49,9);g.rect(293,y,34,9);
   g.fill(244,203,106);g.rect(236,y,game.etapa()<2?0:49*(1-Math.min(1,n.misil/4)),9);
   g.fill(139,220,236);g.rect(293,y,34*(1-Math.min(1,n.recargaImpulso/2)),9);
  }
  panel(g,570,67,438,76);texto(g,"DRON VIRAL · FASE "+etapa+" / 3",585,87,13,0xfff4cb6a);
  g.fill(82,55,98);g.rect(585,96,399,10);g.fill(222,114,176);g.rect(585,96,399*game.energia/180f,10);
  g.fill(244,203,106);g.rect(718,94,2,14);g.rect(851,94,2,14);
  String consejo=etapa==1?"Alinea tu nave con el dron y mantén disparo.":etapa==2?"Turbulencia y señuelos · ¡misil desbloqueado!":"Halo activo · evita tocar el dron.";
  texto(g,consejo,585,128,12,0xfff5e8fb);
  texto(g,"Corazones: tócalos para recuperar vida",24,175,13,0xffffbed7);
  g.fill(25,15,38,230);g.rect(0,542,W,34);texto(g,game.modo==MisionSeisMotor.Modo.SOLO?"WASD / flechas: mover · F: disparo · Q: misil · Espacio: impulso":"J1 WASD + F/Q + Espacio   ·   J2 flechas + L/K + Mayús",16,565,14,0xfff5e8fb);
 }
 private void dibujarEntrevista(PGraphics g,boolean respuesta){
  escenaSofa(g);
  if(respuesta){cuadroDialogo(g,game.interlocutor(),game.textoMostrado(),"ENTER · continuar");return;}
  panel(g,42,360,940,196);texto(g,"INFLUENCER · PREGUNTA "+(game.pregunta+1)+" / 3",65,389,17,0xfff4cb6a);
  parrafo(g,MisionSeisMotor.PREGUNTAS[game.pregunta],65,398,892,17,0xfff5e8fb);
  for(int i=0;i<2;i++){String prefijo=game.seleccion==i?"> ":"  ";parrafo(g,prefijo+(i+1)+" · "+MisionSeisMotor.RESPUESTAS[game.pregunta][i],65,448+i*38,892,14,game.seleccion==i?0xfff4cb6a:0xffd6bfe2);}
  texto(g,"Flechas: elegir · ENTER: responder · 1 / 2: respuesta directa",65,541,12,0xffd6bfe2);
 }
 private void corazon(PGraphics g,float x,float y,float size){
  String[] filas={"0110110","1111111","1111111","0111110","0011100","0001000"};float u=size/7;
  g.noStroke();for(int r=0;r<filas.length;r++)for(int c=0;c<7;c++)if(filas[r].charAt(c)=='1'){
   g.fill(75,25,66);g.rect(x-size/2+c*u,y-size*.4f+r*u+u*.5f,u,u);
   g.fill(r<2?0xffffa4cb:0xffef649a);g.rect(x-size/2+c*u,y-size*.4f+r*u,u,u);
  }
 }
 private void dibujarSalaLibre(PGraphics g){
  fondo(g,"mision6/sala");g.noStroke();g.fill(20,12,29,90);g.ellipse(game.salaX,game.salaY,30,8);g.ellipse(710,455,30,8);
  if(game.salaY<455){dibujarInfluencerEn(g,game.salaX,game.salaY,true);juanSprites.dibujar(g,"idle",0,0,game.global,710,455,50,68);}
  else{juanSprites.dibujar(g,"idle",0,0,game.global,710,455,50,68);dibujarInfluencerEn(g,game.salaX,game.salaY,true);}
  texto(g,"JUAN",691,380,12,0xfff4cb6a);
  float y=424+(float)Math.sin(game.global*4)*3;
  if(game.cercaDelSofa()){panel(g,524,389,252,30);texto(g,"ENTER · sentarse y conversar",540,410,14,0xfff4cb6a);}
  else {g.fill(244,203,106);g.triangle(642,y,658,y,650,y+12);}
  g.fill(25,15,38,235);g.rect(0,536,W,40);texto(g,"Explora la sala. Acércate al sofá y pulsa ENTER para iniciar la conferencia.",20,560,14,0xfff5e8fb);
  if(game.ayudaTiempo>0){panel(g,260,70,504,44);texto(g,"Acércate al sofá para hablar con Juan.",278,98,16,0xfff4cb6a);}
 }
 private void escenaSofa(PGraphics g){
  g.pushMatrix();g.translate(512,280);g.scale(1.6f);g.translate(-575,-345);fondo(g,"mision6/sala");
  PImage atlas=imagen("mision6/personajes_sentados");
  // Respiración y balanceo desde la cadera: conserva la pose frontal y el asiento.
  float hablando=game.fase==MisionSeisMotor.Fase.RESPUESTA?1f:.45f;
  g.pushMatrix();g.translate(510,355);
  g.rotate((float)Math.sin(game.global*1.8f)*.004f*hablando);
  g.scale(1,1+(float)Math.sin(game.global*2.1f)*.005f);
  juanSprites.dibujar(g,"seated",0,0,game.global,0,10,64,110);
  g.popMatrix();
  for(int i=1;i<2;i++){String clave="sentado-"+i;PImage sprite=recursos.get(clave);if(sprite==null){sprite=atlas.get(i*(atlas.width/2),0,atlas.width/2,atlas.height);recursos.put(clave,sprite);}
   float x=i==0?510:645;float dy=(game.fase==MisionSeisMotor.Fase.RESPUESTA&&i==0||game.fase==MisionSeisMotor.Fase.ENTREVISTA&&i==1)?(float)Math.sin(game.global*3)*.6f:0;
   g.image(sprite,x-60,252+dy,120,120);
  }
  PImage mesa=recursos.get("mesa-delante");if(mesa==null){PImage sala=imagen("mision6/sala");mesa=sala.get(sala.width*435/1024,sala.height*358/576,sala.width*175/1024,sala.height*73/576);recursos.put("mesa-delante",mesa);}
  g.image(mesa,435,358,175,73);
  g.popMatrix();
 }
 private void dibujarFinal(PGraphics g){
  escenaSofa(g);String s=game.ruta()==MisionSeisMotor.Ruta.ESCUCHA?
   "JUAN: Gobernar es escuchar, ajustar y construir con la gente. Las propuestas deben crecer con la ciudad.":
   "JUAN: Gobernar también exige sostener ideas firmes y explicarlas con valentía. Escuchar no significa renunciar al rumbo.";
  cuadroDialogo(g,"JUAN",(game.ruta()==MisionSeisMotor.Ruta.ESCUCHA?"ESCUCHAR Y ADAPTAR. ":"CONVICCIÓN Y DIÁLOGO. ")+s.replace("JUAN: ","")+"  INFLUENCER: Una ciudad no se salva solo peleando contra el caos. También se salva decidiendo qué hacer cuando baja el ruido.","ENTER · volver al selector");
 }
}
