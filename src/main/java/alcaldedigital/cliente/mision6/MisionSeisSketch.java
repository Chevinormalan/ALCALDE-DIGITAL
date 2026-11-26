package alcaldedigital.cliente.mision6;

import alcaldedigital.cliente.presentacion.LuzEscena;
import processing.core.*;
import java.awt.Font;
import javax.imageio.ImageIO;
import java.util.*;
import static alcaldedigital.cliente.mision6.MisionSeisMotor.*;

/** Presentación de la misión sobre un búfer del mismo PApplet anfitrión. */
public final class MisionSeisSketch {
 private static final int W=1024,H=576;
 private static final float[][] VECINOS={{461,230},{485,257},{517,225},{527,292},{466,292},{594,244},
   {631,215},{663,246},{710,212},{690,277},{630,278},{746,287},{573,324},{717,317},{514,320}};
 private MisionSeisMotor game=new MisionSeisMotor();
 private final AudioMisionSeis sonido=new AudioMisionSeis();
 private final alcaldedigital.cliente.presentacion.JuanSprites juanSprites=new alcaldedigital.cliente.presentacion.JuanSprites();
 private AutoguardadoSeis guardado;private Properties guardada;private boolean elegirGuardado;private int focoGuardado;private long ultimoGuardado;private String firmaGuardada="";
 private final Map<String,PImage> recursos=new HashMap<>();
 private PGraphics canvas;private PFont fuente;private long previo;private float deuda;
 private boolean gestionExterna, modoRed, caminandoRed, invitadoRed, movimientoReducido;
 private final PImage[] rotor = new PImage[12], explosion = new PImage[10];
 private PImage cieloIluminado;
 private final PImage[] nubes=new PImage[2];
 private final VueloVisual vueloVisual=new VueloVisual();

 private static final class VueloVisual {
  private float lectura=Float.NaN;
  private final float[] x=new float[2],y=new float[2],angulo=new float[2];
  private Fase fase;
  void observar(MisionSeisMotor motor,boolean reducido) {
   float transcurrido=motor.global-lectura;
   if(reducido||fase!=motor.fase||!Float.isFinite(transcurrido)||transcurrido<0||transcurrido>.25f) {
    for(int i=0;i<2;i++){x[i]=motor.naves[i].x;y[i]=motor.naves[i].y;angulo[i]=0;}
   } else if(transcurrido>0) {
    for(int i=0;i<2;i++){
     var nave=motor.naves[i];
     float vertical=(nave.y-y[i])/transcurrido, horizontal=(nave.x-x[i])/transcurrido;
     float destino=Math.clamp(vertical*.00032f-horizontal*.00013f,-.16f,.16f);
     angulo[i]=PApplet.lerp(angulo[i],destino,(float)(1-Math.exp(-12*transcurrido)));
     x[i]=nave.x;y[i]=nave.y;
    }
   }
   lectura=motor.global;fase=motor.fase;
  }
 }

 public void movimientoReducido(boolean valor) { movimientoReducido = valor; }
 public void verRed(RedSeis.Vista vista, boolean invitado){
  if(!modoRed){modoRed=true;elegirGuardado=false;sonido.restablecer();}
  game=vista.motor();caminandoRed=vista.caminando();invitadoRed=invitado;
  if(vista.pausada())sonido.pausar();else sonido.observar(game);
 }
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
  if(modoRed||guardado==null||elegirGuardado||gestionExterna&&!forzar)return;
  String firma=game.fase+":"+game.pagina+":"+game.pregunta+":"+game.seleccion+":"+game.etapa()+":"+game.practicoMover+":"+game.practicoDisparo+":"+game.naves[0].vida+":"+game.naves[1].vida+":"+game.corazones.size();
  long ahora=System.nanoTime();if(forzar||!firma.equals(firmaGuardada)||ahora-ultimoGuardado>=2_000_000_000L){guardado.guardar(game.capturar());ultimoGuardado=ahora;firmaGuardada=firma;}
 }
 public void cerrar(){cerrar(true);}
 public void cerrar(boolean guardarLocal){
  if(guardado!=null){if(guardarLocal&&!modoRed&&!elegirGuardado)guardarSiCorresponde(true);if(!guardado.cerrarYEsperar())System.err.println("No se confirmó el autoguardado de la misión 6");guardado=null;}
  sonido.close();game.perderFoco();recursos.clear();cieloIluminado=null;canvas=null;
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
  vueloVisual.observar(game,movimientoReducido);
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
   case LLEGADA -> dibujarLlegada(g);
   case EXPLORAR_SALA -> dibujarSalaLibre(g);
   case SALA -> {escenaSofa(g);cuadroDialogo(g,game.interlocutor(),game.textoMostrado(),"ENTER · continuar la charla");}
   case ENTREVISTA -> dibujarEntrevista(g,false);
   case RESPUESTA -> dibujarEntrevista(g,true);
   case FINAL -> dibujarFinal(g);
   case DERROTA -> {cielo(g);panel(g,185,155,654,290);texto(g,"LA SEÑAL SIGUE ACTIVA",235,213,30,0xfff4cb6a);parrafo(g,"La interferencia ha obligado a los helicópteros a retirarse. Pueden intentarlo de nuevo: el objetivo es desactivar el enlace del dron.",235,246,555,19,0xfff5e8fb);boton(g,322,354,380,53,"REINTENTAR  [R / ENTER]",true);}
  }
  if(game.fase!=Fase.COMBATE&&game.fase!=Fase.ENTRENAMIENTO)cabecera(g);
 }
 private void fondo(PGraphics g,String name){g.image(imagen(name),0,0,W,H);}
 private void cabecera(PGraphics g){g.noStroke();g.fill(25,15,38,225);g.rect(0,0,W,54);g.fill(189,145,213);g.rect(0,53,W,2);texto(g,"CIUDAD NOVA  /  MISIÓN 6",20,21,13,0xfff4cb6a);texto(g,"Cielos de campaña",20,43,18,0xfff5e8fb);texto(g,"ESC / P · pausa y controles",780,33,14,0xfff4cb6a);}
 private void cielo(PGraphics g){
  float t=movimientoReducido?0:game.global;
  float deriva=(float)Math.sin(t*.13)*16;
  if(cieloIluminado==null) cieloIluminado=prepararCielo();
  g.image(cieloIluminado,Math.round(-20+deriva),0,W+40,H);
  for(int i=0;i<4;i++) {
   float x=(79+i*276-t*(7+i%3*3))%1320;
   if(x < -160) x+=1320;
   float ancho=103+i%3*26;
   PImage nube=nubes[i%2];
   g.image(nube,x,95+i%3*81,ancho,ancho*nube.height/nube.width);
  }
 }
 private PImage prepararCielo(){
  var capa=new java.awt.image.BufferedImage(1330,720,java.awt.image.BufferedImage.TYPE_INT_RGB);
  var g=capa.createGraphics();
  g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
  var original=(java.awt.image.BufferedImage)imagen("mision6/cielo").getNative();
  g.drawImage(original,0,0,capa.getWidth(),capa.getHeight(),null);
  nubes[0]=recorteNube(original,78,145,402,82);
  nubes[1]=recorteNube(original,1730,137,442,96);
  g.scale(1.25,1.25);
  g.translate(20,0);
  LuzEscena.AMBAR.dibujar(g,755,408,900,244,.48f);
  LuzEscena.VIOLETA.dibujar(g,278,119,675,255,.38f);
  g.setColor(new java.awt.Color(28,18,51,32));
  g.fillRect(-20,0,W+40,H);
  g.dispose();
  return new PImage(capa);
 }
 private PImage recorteNube(java.awt.image.BufferedImage original,int ox,int oy,int w,int h) {
  var imagen=new java.awt.image.BufferedImage(w,h,java.awt.image.BufferedImage.TYPE_INT_ARGB);
  for(int y=0;y<h;y++) {
   int fondo=(original.getRGB(0,oy+y)>>>16)&255;
   for(int x=0;x<w;x++) {
    int pixel=original.getRGB(x+ox,y+oy);
    int distancia=((pixel>>>16)&255)-fondo;
    int alfa=Math.clamp((distancia-14)*2,0,100);
    if(alfa>0) imagen.setRGB(x,y,(alfa<<24)|(pixel&0xffffff));
   }
  }
  return new PImage(imagen);
 }
 private PImage celda(String nombre,PImage[] celdas,int columnas,int i) {
  if(celdas[i]==null){
   PImage hoja=imagen(nombre);
   int w=hoja.width/columnas,h=hoja.height/(celdas.length/columnas);
   celdas[i]=hoja.get(i%columnas*w,i/columnas*h,w,h);
  }
  return celdas[i];
 }
 private void explosion(PGraphics g,float x,float y,float tam,float progreso) {
  float t=Math.clamp(progreso,0,1);
  int i=movimientoReducido?2:Math.min(9,(int)(t*10));
  LuzEscena.AMBAR.dibujar(g,x,y,tam*1.22f,tam*1.1f,210*(1-t));
  g.tint(255,Math.min(255,330*(1-t)));
  g.image(celda("mision6/explosion-remaster",explosion,5,i),x-tam/2,y-tam/2,tam,tam);
  g.noTint();
 }
 private void helicoptero(PGraphics g,float x,float y,float size,boolean dano,boolean fuego){
  helicoptero(g,x,y,size,dano,fuego,-1,1);
 }
 private void helicoptero(PGraphics g,float x,float y,float size,boolean dano,boolean fuego,int rol,float rotorActivo){
  float t=game.global;
  g.pushMatrix();
  g.translate(x,y+(movimientoReducido?0:(float)Math.sin(t*4+x*.01)*2*rotorActivo));
  g.scale(size);
  if(rol>=0) {
   if(game.naves[rol].vida<=2&&game.fase==Fase.COMBATE) humo(g,rol);
   g.rotate(vueloVisual.angulo[rol]);
  }
  if(dano&&(movimientoReducido||(int)(t*12)%2==0))g.tint(255,175,188);
  g.image(imagen("mision6/helicoptero-cuerpo"),-85,-48,170,96);
  g.noTint();
  float frenado=1-rotorActivo;
  int cuadro=movimientoReducido||rotorActivo==0?0:
   Math.floorMod((int)((t-.225f*frenado*frenado)*33),rotor.length);
  g.image(celda("mision6/helicoptero-rotor",rotor,6,cuadro),-70,-39,158,24);
  g.noStroke();
  if(rol>=0){
   g.fill(0xff241530);g.rect(-24,8,24,10);
   g.fill(colorNave(rol));g.rect(-23,9,22,8);
   g.fill(0xff241530);
   for(int i=0;i<=rol;i++)g.rect(-19+i*7,10,4,6);
  }
  if(fuego){
   LuzEscena.AMBAR.dibujar(g,88,-2,50,30,202);
   g.fill(255,151,77,219);g.triangle(78,-5,109,-2,80,5);
   g.fill(255,239,185);g.triangle(78,-2,98,-2,78,3);
  }
  g.popMatrix();
 }
 private void humo(PGraphics g,int rol) {
  float reloj=movimientoReducido?.1f:game.global;
  g.noStroke();
  for(int i=0;i<7;i++){
   float u=(reloj*1.15f+i/7f)%1;
   float dx=-52-u*91,dy=-2-u*(12+i%3*2);
   g.fill(28,20,46,137*(1-u));
   g.ellipse(dx,dy,11+u*20,8+u*14);
   g.fill(111,74,120,65*(1-u));
   g.ellipse(dx,dy-3,7+u*16,5+u*9);
  }
  float pulso=movimientoReducido?95:90+58*(float)Math.sin(reloj*17+rol);
  LuzEscena.AMBAR.dibujar(g,-43,-3,33,33,pulso);
 }

 private static int colorNave(int rol){return rol==0?0xfff4cb6a:0xff8bdcec;}
 private void marcaSuelo(PGraphics g,float x,float y,float ancho){
  g.noStroke();g.fill(244,203,106,37);g.ellipse(x,y,ancho,ancho*.28f);
  g.noFill();g.stroke(244,203,106,210);g.strokeWeight(2);g.ellipse(x,y,ancho,ancho*.28f);
  g.noStroke();
 }
 private void dibujarLlegada(PGraphics g){
  if(game.tiempo<4.45f){
   fondo(g,"mision6/casa");
   aterrizar(g,0,512,108,.68f);
   if(game.modo==Modo.DUO)aterrizar(g,.68f,391,117,.55f);
   g.noStroke();g.fill(25,15,38,225);g.rect(23,512,190,37,3);
   texto(g,"Casa de campaña",34,536,16,0xfff4cb6a);
   oscurecer(g,(game.tiempo-4.15f)/.30f);
  }else{
   fondo(g,"mision6/entrada");
   dibujarEntrada(g);
   oscurecer(g,Math.max(1-(game.tiempo-4.45f)/.26f,(game.tiempo-5.75f)/.25f));
  }
 }
 private void dibujarEntrada(PGraphics g){
  float t=Math.clamp((game.tiempo-4.45f)/1.43f,0,1);
  float pies=521-124*t,escala=1-.30f*t;
  float xJuan=484+19*t,xInfluencer=548-18*t,piesInfluencer=pies+18;
  g.noStroke();g.fill(30,17,35,105);g.ellipse(xJuan,pies,30*escala,8*escala);
  g.ellipse(xInfluencer,piesInfluencer,30*escala,8*escala);
  juanSprites.dibujar(g,"walk",1,game.tiempo*9,game.global,xJuan,pies,52*escala,73*escala);
  PImage influencer=imagen("mision3/redisenio/player_back_"+(1+(int)(game.tiempo*9)%4));
  g.image(influencer,xInfluencer-26*escala,piesInfluencer-70*escala,52*escala,70*escala);
 }
 private void aterrizar(PGraphics g,float retraso,float destinoX,float destinoY,float escala){
  float t=Math.clamp((game.tiempo-retraso)/3.25f,0,1);
  float avance=t*t*(3-2*t);
  float x=-95+(destinoX+95)*avance,y=31+(destinoY-31)*avance;
  float sombra=Math.clamp((t-.55f)/.45f,0,1);
  g.noStroke();g.fill(23,15,32,115*sombra);
  g.ellipse(x,destinoY+24,104*escala*(.7f+.3f*sombra),19*escala);
  float rotor=1-Math.clamp((game.tiempo-retraso-3.25f)/.45f,0,1);
  helicoptero(g,x,y,escala,false,false,-1,rotor);
 }
 private void oscurecer(PGraphics g,float cantidad){
  if(cantidad<=0)return;
  g.noStroke();g.fill(19,13,30,255*Math.clamp(cantidad,0,1));g.rect(0,0,W,H);
 }
 private void dron(PGraphics g,float x,float y,float size){
  // Atlas de 5 columnas y 3 filas: vuelo, disparo y daño. Mirada hacia el jugador.
  int fila=game.fase==MisionSeisMotor.Fase.RETIRADA||game.impacto>0?2:
      game.fase==Fase.COMBATE&&game.avisoDron!=null&&game.ataqueJefe<.14f?1:0;
  int frame=movimientoReducido?0:(int)(game.global*10)%5;String clave="dron-"+fila+"-"+frame;
  PImage sprite=recursos.get(clave);
  if(sprite==null){PImage atlas=imagen("mision6/dron_atlas");int cw=atlas.width/5,ch=atlas.height/3;
   sprite=atlas.get(frame*cw,fila*ch,cw,ch);recursos.put(clave,sprite);}
  float ancho=size*1.42f,alto=ancho*sprite.height/sprite.width;
  g.image(sprite,x-ancho/2,y-alto/2,ancho,alto);
 }
 private void dibujarAutodestruccion(PGraphics g){
  float t=game.tiempo,x=game.jefeX,y=game.jefeY;
  if(t<.75f){if((int)(t*22)%2==0)g.tint(255,130,120);dron(g,x+(float)Math.sin(t*70)*4,y,150);g.noTint();}
  if(t>=.5f&&t<2.3f){float p=(t-.5f)/1.8f,size=165+165*p;explosion(g,x,y,size,p);
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
  for(int i=0;i<VECINOS.length;i++){
   float x=VECINOS[i][0],y=VECINOS[i][1];
   if(game.fase==Fase.CAMINAR&&i>=12){
    float distancia=(float)Math.hypot(game.jugadorX-x,game.jugadorY-y);
    y-=15*Math.clamp(1-distancia/100,0,1);
   }
   g.noStroke();g.fill(20,12,29,85);g.ellipse(x,y,22,7);
   g.image(imagen("mision4/vecino_"+(i%8)),x-18,y-45+(float)Math.sin(game.global*2+i)*.8f,36,45);
  }
  marcaSuelo(g,HELICOPTERO_X,HELICOPTERO_Y+25,123);
  helicoptero(g,HELICOPTERO_X,HELICOPTERO_Y,.82f,false,false,-1,0);
  float jx=discurso?ESCENARIO_X:game.juanX,jy=discurso?ESCENARIO_Y:game.juanY;
  g.noStroke();g.fill(20,12,29,110);g.ellipse(jx,jy,31,9);g.ellipse(game.jugadorX,game.jugadorY,31,9);
  if(jy<=game.jugadorY){dibujarJuanPlaza(g,jx,jy,discurso);dibujarInfluencer(g);}
  else{dibujarInfluencer(g);dibujarJuanPlaza(g,jx,jy,discurso);}
  if(game.fase==MisionSeisMotor.Fase.CAMINAR){
   String nombre=game.tramoCharla()==1?"INFLUENCER":"JUAN";
   String comentario=switch(game.tramoCharla()){
    case 0->"Quiero que esta campaña vuelva a escuchar al barrio.";
    case 1->"Después del Núcleo Viral, verificar importa más que nunca.";
    default->"La casa está cerca. Allí responderé todas tus preguntas.";
   };
   float bx=Math.max(20,Math.min(690,(game.jugadorX+game.juanX)/2-135));float by=Math.max(90,Math.min(game.jugadorY,game.juanY)-124);
   panel(g,bx,by,310,57);texto(g,nombre,bx+13,by+17,12,0xfff4cb6a);parrafo(g,comentario,bx+13,by+21,285,13,0xfff5e8fb);
  }
  if(!discurso&&game.fase!=MisionSeisMotor.Fase.ENCUENTRO){
   boolean buscar=game.fase==MisionSeisMotor.Fase.BUSCAR;
   boolean cerca=buscar?game.cercaDeJuan():game.cercaDelHelicoptero()&&game.juanListo();
   float x=buscar?game.juanX:EMBARQUE_X,y=buscar?game.juanY-86:EMBARQUE_Y-64;
   if(cerca){panel(g,x-105,y-26,210,35);texto(g,buscar?"ENTER · hablar":"ENTER · subir",x-72,y-3,15,0xfff4cb6a);}
   else {g.noStroke();g.fill(244,203,106,225);float saltito=(float)Math.sin(game.global*5)*3;g.triangle(x-8,y-20+saltito,x+8,y-20+saltito,x,y-9+saltito);}
   g.noStroke();g.fill(25,15,38,232);g.rect(0,538,W,38);
   String meta=buscar?game.juanCaminando?"Juan baja del escenario. Reúnete con él al pie de la escalera.":"Acércate a Juan para hablar con él.":game.cercaDelHelicoptero()&&!game.juanListo()?"Espera a Juan junto al helicóptero.":"Acompaña a Juan al helicóptero y pulsa ENTER para subir.";
   texto(g,meta,20,562,14,0xfff5e8fb);texto(g,"WASD / flechas · caminar",780,562,13,0xffd6bfe2);
  }
 }
 private void dibujarJuanPlaza(PGraphics g,float x,float y,boolean discurso){
  juanSprites.dibujar(g,discurso?"speech":game.juanCaminando?"walk":"idle",discurso?0:game.direccionJuan,
    game.pasosJuan,game.global,x,y,50,68);
 }
 private void dibujarInfluencer(PGraphics g){
  dibujarInfluencerEn(g,game.jugadorX,game.jugadorY,game.fase==MisionSeisMotor.Fase.BUSCAR||game.fase==MisionSeisMotor.Fase.CAMINAR);
 }
 private void dibujarInfluencerEn(PGraphics g,float x,float y,boolean libre){
  String[] dirs={"front","back","left","right"};String dir=dirs[Math.max(0,Math.min(3,game.direccion))];
  String sprite=libre&&(modoRed?caminandoRed:game.estaCaminando())?"player_"+dir+"_"+(1+(int)game.pasos%4):"player_"+dir+"_idle";
  g.image(imagen("mision3/redisenio/"+sprite),x-26,y-70,52,70);
 }
 private void dibujarEntrenamiento(PGraphics g){
  cielo(g);for(int i=0;i<(game.modo==Modo.SOLO?1:2);i++){var n=game.naves[i];helicoptero(g,n.x,n.y,.68f,false,n.flash>0,i,1);}
  for(var p:game.proyectiles){g.noStroke();g.fill(244,203,106);g.rect(p.x-8,p.y-2,17,4);}
  g.noFill();g.stroke(game.impacto>0?0xff9cf2c5:0xfff4cb6a);g.strokeWeight(game.impacto>0?5:2);
  g.ellipse(DIANA_X,DIANA_Y,66,66);g.line(DIANA_X-14,DIANA_Y,DIANA_X+14,DIANA_Y);
  g.line(DIANA_X,DIANA_Y-14,DIANA_X,DIANA_Y+14);g.noStroke();
  if(game.aciertosPractica>0)texto(g,"ACERTADOS "+game.aciertosPractica,DIANA_X-51,DIANA_Y+56,13,0xff9cf2c5);
  g.noStroke();g.fill(25,15,38,245);g.rect(0,0,W,62);
  g.fill(189,145,213);g.rect(0,60,W,2);
  texto(g,"PRÁCTICA SEGURA · APUNTA A LA DIANA",19,22,16,0xfff4cb6a);
  texto(g,modoRed?(invitadoRed?"Influencer":"Juan")+" · WASD / flechas · F: disparar":game.modo==Modo.SOLO?"Mover: WASD / flechas · Disparar: F":"J1 Juan: WASD + F · J2 Influencer: flechas + L",19,47,14,0xfff5e8fb);
  texto(g,"ENTER · iniciar combate",800,33,15,0xfff4cb6a);
  g.fill(25,15,38,225);g.rect(0,540,W,36);
  texto(g,!modoRed&&game.modo==Modo.DUO?"Impulso: J1 Espacio · J2 Mayús · Esc / P: pausa":"Espacio: impulso · Esc / P: pausa",20,563,14,0xfff5e8fb);
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
  cielo(g);
  g.clip(0,62,W,480);
  var aviso=game.avisoDron;
  if(aviso!=null)dibujarAvisoDron(g,aviso);
  if(game.choque>0)dibujarHalo(g,game.jefeX,game.jefeY,true);
  if(game.etapa()==2&&Math.sin(game.global*1.6)>.2){
   señuelo(g,Math.min(943,game.jefeX+61),Math.min(485,game.jefeY+74));
   señuelo(g,game.jefeX-54,Math.max(126,game.jefeY-75));
  }
  dron(g,game.jefeX,game.jefeY,150);
  for(var c:game.corazones){float y=c.y+(float)Math.sin(game.global*5)*3;corazon(g,c.x,y,29);texto(g,"+1 VIDA",c.x-23,y-23,11,0xffffbed7);}
  for(var p:game.proyectiles){
   g.noStroke();if(p.enemigo){dibujarDisparoDron(g,p);}
   else if(p.especial){
    g.noStroke();g.fill(215,133,199,72);g.ellipse(p.x-36,p.y,38,7);
    g.fill(255,192,107,175);g.triangle(p.x-19,p.y-3,p.x-55,p.y,p.x-19,p.y+3);
    g.image(imagen("mision6/misil"),p.x-32,p.y-14,64,28);
   }
   else{g.fill(244,203,106);g.rect(p.x-8,p.y-2,17,4);g.fill(255,246,194);g.rect(p.x-5,p.y-1,12,2);}
  }
  if(game.impacto>0){
   float t=1-Math.clamp(game.impacto/.5f,0,1);
   explosion(g,game.impactoX,game.impactoY,71+56*t,t);
  }
  if(game.etapa()==2){g.stroke(226,207,255,80);g.strokeWeight(2);for(int i=0;i<5;i++){float wx=(i*240-game.global*90)%1200;if(wx<0)wx+=1200;float y=170+i*55;g.line(wx,y,wx+65,y+18*(float)Math.sin(game.global*1.6));}g.noStroke();}
  for(int i=0;i<(game.modo==Modo.SOLO?1:2);i++){
   var n=game.naves[i];
   if(!n.activa)continue;
   if(n.impulso>0){g.noFill();g.stroke(126,218,245,190);g.strokeWeight(3);g.ellipse(n.x,n.y,110,68);g.noStroke();}
   if(n.invulnerable>0)g.tint(255,(int)(game.global*9)%2==0?255:155);
   helicoptero(g,n.x,n.y,.68f,n.herida>0,n.flash>0,i,1);
   if(game.modo==Modo.DUO)texto(g,i==0?"J1 · JUAN":"J2 · INFLUENCER",n.x-40,n.y-41,11,colorNave(i));
  }
  for(int i=0;i<(game.modo==MisionSeisMotor.Modo.SOLO?1:2);i++){var n=game.naves[i];if(n.curacion>0)texto(g,"+1 VIDA",n.x-22,n.y-45,14,0xff9cf2c5);}
  g.noClip();
  dibujarHudVuelo(g);
  g.fill(25,15,38,230);g.rect(0,542,W,34);texto(g,modoRed?(invitadoRed?"LAN · Influencer":"LAN · Juan")+" · WASD / flechas · F disparo · Q misil · Espacio impulso":game.modo==MisionSeisMotor.Modo.SOLO?"WASD / flechas: mover · F: disparo · Q: misil · Espacio: impulso":"J1 WASD + F/Q + Espacio   ·   J2 flechas + L/K + Mayús",16,565,14,0xfff5e8fb);
 }
 private void dibujarHudVuelo(PGraphics g){
  g.noStroke();g.fill(25,15,38,245);g.rect(0,0,W,62);
  g.fill(189,145,213);g.rect(0,60,W,2);
  texto(g,"INTEGRIDAD",17,16,12,0xffd6bfe2);
  texto(g,game.etapa()<2?"MISIL · FASE 2":"MISIL",247,16,10,0xffd6bfe2);texto(g,"IMPULSO",331,16,10,0xffd6bfe2);
  for(int i=0;i<(game.modo==MisionSeisMotor.Modo.SOLO?1:2);i++){
   var n=game.naves[i];float y=24+i*18;texto(g,game.modo==Modo.SOLO?"JUAN + INFLUENCER":i==0?"J1 · JUAN":"J2 · INFLUENCER",17,y+8,11,colorNave(i));
   for(int j=0;j<5;j++){if(j<n.vida)corazon(g,156+j*16,y+5,12);else{g.fill(105,68,94);g.rect(150+j*16,y+1,12,8,2);}}
   g.fill(83,58,105);g.rect(247,y,66,9);g.rect(331,y,60,9);
   g.fill(244,203,106);g.rect(247,y,game.etapa()<2?0:66*(1-Math.min(1,n.misil/4)),9);
   g.fill(139,220,236);g.rect(331,y,60*(1-Math.min(1,n.recargaImpulso/2)),9);
  }
  texto(g,"DRON VIRAL · FASE "+game.etapa()+" / 3",555,17,13,0xfff4cb6a);
  texto(g,"ESC / P · pausa",882,17,12,0xffd6bfe2);
  g.fill(82,55,98);g.rect(555,24,453,9);g.fill(222,114,176);g.rect(555,24,453*game.energia/180f,9);
  g.fill(244,203,106);g.rect(706,22,2,13);g.rect(857,22,2,13);
  String consejo=switch(game.etapa()){
   case 1->"Busca un hueco entre los disparos.";
   case 2->game.avisoDron==null||game.avisoDron.objetivo()<0?"Misil disponible.":(
       game.modo==Modo.DUO?"Objetivo J"+(game.avisoDron.objetivo()+1)+" · sal de la línea marcada.":"Sal de la línea marcada.");
   default->"Aléjate del aro ámbar antes de que se active.";
  };
  texto(g,consejo,555,51,12,0xfff5e8fb);
 }
 private void dibujarAvisoDron(PGraphics g,Aviso a){
  if(a.tipo()==Ataque.HALO){dibujarHalo(g,a.x(),a.y(),false);return;}
  float opacidad=125+85*(1-Math.clamp(game.ataqueJefe/duracionAviso(a.tipo()),0,1));
  g.noStroke();g.fill(244,203,106,opacidad);g.rect(a.x()-5,a.y()-5,10,10);
  for(int i=0;i<cantidad(a.tipo());i++){
   float ang=angulo(a,i),dx=(float)Math.cos(ang),dy=(float)Math.sin(ang);
   for(int j=16;j<1100;j+=21){
    float x=a.x()+dx*j,y=a.y()+dy*j;
    if(x<0||x>W||y<62||y>542)break;
    g.rect(x-2,y-2,4,4);
   }
  }
 }
 private void dibujarHalo(PGraphics g,float x,float y,boolean activo){
  g.noStroke();g.fill(activo?0xffec6bb2:0xfff4cb6a,activo?60:27);
  g.ellipse(x,y,2*RADIO_HALO,2*RADIO_HALO);
  g.noFill();g.stroke(activo?0xffec6bb2:0xfff4cb6a);g.strokeWeight(activo?5:2);
  g.ellipse(x,y,2*RADIO_HALO,2*RADIO_HALO);
  g.noStroke();
 }
 private void señuelo(PGraphics g,float x,float y){
  g.tint(153,178,255,69);dron(g,x,y,116);g.noTint();
  g.stroke(170,190,255,100);g.strokeWeight(1);
  for(int i=-20;i<=20;i+=10)g.line(x-40,y+i,x+40,y+i);
  g.noStroke();
 }
 private void dibujarDisparoDron(PGraphics g,Proyectil p){
  g.pushMatrix();g.translate(p.x,p.y);g.rotate((float)Math.atan2(p.vy,p.vx));
  g.fill(239,102,171,105);g.rect(-19,-2,13,4);
  g.fill(239,102,171);g.rect(-8,-4,13,8);g.triangle(4,-5,10,0,4,5);
  g.fill(255,221,238);g.rect(-5,-1,10,2);g.popMatrix();
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
  fondo(g,"mision6/sala");
  marcaSuelo(g,SOFA_X,SOFA_Y,57);
  g.noStroke();g.fill(20,12,29,90);g.ellipse(game.salaX,game.salaY,30,8);g.ellipse(JUAN_SALA_X,JUAN_SALA_Y,30,8);
  int mirada=game.salaX<JUAN_SALA_X?2:3;
  if(game.salaY<JUAN_SALA_Y)dibujarInfluencerEn(g,game.salaX,game.salaY,true);
  juanSprites.dibujar(g,"idle",mirada,0,game.global,JUAN_SALA_X,JUAN_SALA_Y,50,68);
  if(game.salaY>=JUAN_SALA_Y)dibujarInfluencerEn(g,game.salaX,game.salaY,true);
  if(game.cercaDelSofa()){panel(g,524,389,252,30);texto(g,"ENTER · sentarse y conversar",540,410,14,0xfff4cb6a);}
  g.fill(25,15,38,235);g.rect(0,536,W,40);
  texto(g,game.cercaDelSofa()?"Pulsa ENTER para iniciar la entrevista.":"Cruza la sala y reúnete con Juan junto al sofá.",20,560,14,0xfff5e8fb);
  if(game.ayudaTiempo>0&&!game.cercaDelSofa()){panel(g,260,70,504,44);texto(g,"Acércate a la marca junto al sofá.",278,98,16,0xfff4cb6a);}
  oscurecer(g,1-game.tiempo/.30f);
 }
 private void escenaSofa(PGraphics g){
  boolean cierre=game.fase==Fase.FINAL;
  float escala=cierre?1.58f-.37f*Math.clamp(game.tiempo/.75f,0,1):1.58f;
  boolean hablaJuan=game.fase==Fase.RESPUESTA||cierre&&game.pagina==0||game.fase==Fase.SALA&&game.pagina==1;
  float foco=cierre?566:hablaJuan?554:581;
  g.pushMatrix();g.translate(512,cierre?300:280);g.scale(escala);g.translate(-foco,-345);fondo(g,"mision6/sala");
  // El gesto se aplica desde la cadera para mantener al personaje apoyado en el sofá.
  g.pushMatrix();g.translate(510,355);
  g.rotate(hablaJuan?.025f+(float)Math.sin(game.global*6)*.017f:-.012f);
  g.scale(1,1+(float)Math.sin(game.global*(hablaJuan?5:2.1f))*.012f);
  juanSprites.dibujar(g,"seated",0,0,game.global,0,10,64,110);
  g.popMatrix();
  PImage influencer=recursos.get("influencer-sentado");
  if(influencer==null){
   PImage atlas=imagen("mision6/personajes_sentados");
   influencer=atlas.get(atlas.width/2,0,atlas.width/2,atlas.height);
   recursos.put("influencer-sentado",influencer);
  }
  g.pushMatrix();g.translate(645,353);
  g.rotate(hablaJuan?.015f:-.03f+(float)Math.sin(game.global*6)*.017f);
  g.image(influencer,-60,-101,120,120);g.popMatrix();
  PImage mesa=recursos.get("mesa-delante");if(mesa==null){PImage sala=imagen("mision6/sala");mesa=sala.get(sala.width*435/1024,sala.height*358/576,sala.width*175/1024,sala.height*73/576);recursos.put("mesa-delante",mesa);}
  g.image(mesa,435,358,175,73);
  g.popMatrix();
 }
 private void dibujarFinal(PGraphics g){
  escenaSofa(g);
  String ruta=game.ruta()==Ruta.ESCUCHA?"Escuchar y adaptar":"Convicción y diálogo";
  texto(g,ruta,38,84,15,0xfff4cb6a);
  cuadroDialogo(g,game.interlocutor(),game.textoMostrado(),game.pagina==0?"ENTER · continuar":"ENTER · volver al selector");
 }
}
