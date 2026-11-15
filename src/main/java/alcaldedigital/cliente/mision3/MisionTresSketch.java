package alcaldedigital.cliente.mision3;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import java.util.*;
import java.io.*;
import javax.sound.sampled.*;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.grafo.GrafoDifusionCivitas;
import alcaldedigital.compartido.estructuras.grafo.GrafoZonasMisionTres;
import alcaldedigital.compartido.modelo.Publicacion;
import alcaldedigital.cliente.entrada.EstadoMando;

/** Lógica y dibujo de la misión 3 sobre el lienzo principal de Processing. */
public final class MisionTresSketch extends PApplet {
  private String partida=java.util.UUID.randomUUID().toString();
  public String partida(){return partida;}
  public record Resultado(boolean completada, int confianza, int desinformacion, int amenazas) {}
  private PApplet anfitrion;
  private PImage fotogramaPausa;

  public void iniciarEn(PApplet anfitrion) {
    Objects.requireNonNull(anfitrion);
    this.anfitrion = anfitrion;
    width = VW;
    height = VH;
    frameRate = 60;
    g = anfitrion.g;
    game = new GameManager();
    String vistaPrevia=System.getProperty("ad.mision3.vistaPrevia");
    if(vistaPrevia!=null) game.scenes.cut(vistaPrevia);
    if(Boolean.getBoolean("ad.mision3.colisionesDebug")){
      game.debug=true;game.dialogue.active=false;game.player.invuln=100000;
      game.camera.setMode(vistaPrevia!=null&&vistaPrevia.equals("core")?
          CameraManager.BOSS:CameraManager.EXPLORATION);
    }
    if(Boolean.getBoolean("ad.mision3.vistaPreviaJefeMovil")) {
      game.scenes.cut("core");
      game.dialogue.active=false;
      game.camera.setMode(CameraManager.BOSS);
      game.player.place(390,500);
      game.player.invuln=100000;
    }
    if(Boolean.getBoolean("ad.mision3.vistaPreviaPalomo")) {
      game.scenes.cut("atrio");
      game.player.place(400,410);
      game.player.invuln=100000;
    }
    previousFrameNanos = 0;
    simulationDebt = 0;
  }

  public void dibujarEnJuego() {
    if (anfitrion == null) throw new IllegalStateException("La misión 3 no está iniciada");
    fotogramaPausa = null;
    frameCount++;
    anfitrion.pushStyle();
    anfitrion.pushMatrix();
    anfitrion.scale(anfitrion.width / (float) VW, anfitrion.height / (float) VH);
    draw();
    anfitrion.popMatrix();
    anfitrion.popStyle();
  }

  public PImage fotograma() {
    if (fotogramaPausa == null) fotogramaPausa = anfitrion.get();
    return fotogramaPausa;
  }

  public void estadoMando(EstadoMando estado) {
    mandoActivo=estado.conectado();
    if(game==null) return;
    float x=estado.izquierdaX(),y=estado.izquierdaY();
    if(estado.pulsado(EstadoMando.Boton.IZQUIERDA)) x=-1;
    if(estado.pulsado(EstadoMando.Boton.DERECHA)) x=1;
    if(estado.pulsado(EstadoMando.Boton.ARRIBA)) y=-1;
    if(estado.pulsado(EstadoMando.Boton.ABAJO)) y=1;
    game.input.analogX=x;game.input.analogY=y;
    game.input.privatePadHeld=estado.pulsado(EstadoMando.Boton.LB);
    game.player.aimMando.set(estado.derechaX(),estado.derechaY());
  }

  public void accionMando(EstadoMando.Boton boton) {
    if(game==null) return;
    switch(boton) {
      case A -> {
        if(game.scenes.current instanceof EndingScene && ((EndingScene)game.scenes.current).dialogueDone)
          game.onKeyPressed('r',java.awt.event.KeyEvent.VK_R);
        else game.onKeyPressed('e',java.awt.event.KeyEvent.VK_E);
      }
      case X, RT -> game.onKeyPressed('j',java.awt.event.KeyEvent.VK_J);
      case LT -> game.onKeyPressed('k',java.awt.event.KeyEvent.VK_K);
      case RB -> game.onKeyPressed((char)0,java.awt.event.KeyEvent.VK_SHIFT);
      case Y -> game.onKeyPressed('l',java.awt.event.KeyEvent.VK_L);
      default -> { }
    }
  }

  public void pulsarTecla(char caracter, int codigo) {
    key = caracter;
    keyCode = codigo;
    keyPressed();
  }

  public void soltarTecla(char caracter, int codigo) {
    key = caracter;
    keyCode = codigo;
    keyReleased();
  }

  public void pulsarRaton(int boton) {
    mouseButton = boton;
    mousePressed();
  }

  public void apuntarRaton(int x, int y) {
    mouseX = x;
    mouseY = y;
  }

  public void soltarRaton(int boton) {
    mouseButton = boton;
    mouseReleased();
  }

  public void perderFoco() {
    if (game == null) return;
    game.input.up = game.input.down = game.input.left = game.input.right = false;
    game.input.privateHeld = game.input.mouseLeft = game.input.mouseRight = false;
    game.input.privatePadHeld=false;
    game.input.analogX=game.input.analogY=0;
  }

  private Properties checkpointZona;
  private void puntoControl(GameManager gm){
    var p=new Properties();p.setProperty("version","1");p.setProperty("zona",gm.scenes==null?"intro":gm.scenes.currentName);
    p.setProperty("confianza",Float.toString(gm.confidence));p.setProperty("desinformacion",Float.toString(gm.disinfo));p.setProperty("live",Float.toString(gm.live));p.setProperty("amenazas",Integer.toString(gm.totalKills));p.setProperty("primerCombate",Boolean.toString(gm.firstFightDone));p.setProperty("jefe",Boolean.toString(gm.bossDefeated));
    checkpointZona=p;
  }
  public Properties capturar(){if(checkpointZona==null)puntoControl(game);var p=new Properties();p.putAll(checkpointZona);p.setProperty("completada",Boolean.toString(resultado().completada()));p.setProperty("partida",partida);return p;}
  public static boolean guardadoValido(Properties p){
    try{
      if(p==null||!"1".equals(p.getProperty("version"))||!List.of("intro","atrio","pixel","corrupted","core","ending").contains(p.getProperty("zona")))return false;
      float confianza=Float.parseFloat(p.getProperty("confianza")),desinformacion=Float.parseFloat(p.getProperty("desinformacion"));
      if(!Float.isFinite(confianza)||!Float.isFinite(desinformacion)||confianza<0||confianza>1||desinformacion<0||desinformacion>1)return false;
      return Float.isFinite(Float.parseFloat(p.getProperty("live")))&&Integer.parseInt(p.getProperty("amenazas"))>=0;
    }catch(RuntimeException e){return false;}
  }
  public void aplicarGuardado(Properties p){
    if(!guardadoValido(p))throw new IllegalArgumentException("Punto de control de misión 3 inválido");
    float confianza=Float.parseFloat(p.getProperty("confianza")),desinformacion=Float.parseFloat(p.getProperty("desinformacion"));
    if(!Float.isFinite(confianza)||!Float.isFinite(desinformacion)||confianza<0||confianza>1||desinformacion<0||desinformacion>1)throw new IllegalArgumentException("Indicadores inválidos");
    partida=p.getProperty("partida",partida);game.scenes.cut(p.getProperty("zona"));game.confidence=confianza;game.disinfo=desinformacion;game.live=Float.parseFloat(p.getProperty("live"));game.totalKills=Integer.parseInt(p.getProperty("amenazas"));game.firstFightDone=Boolean.parseBoolean(p.getProperty("primerCombate"));game.bossDefeated=Boolean.parseBoolean(p.getProperty("jefe"));
    checkpointZona=new Properties();checkpointZona.putAll(p);
    if(Boolean.parseBoolean(p.getProperty("completada"))&&game.scenes.current instanceof EndingScene fin){fin.dialogueDone=true;fin.dialogueStarted=true;fin.cinematic.jumpTo(400);game.dialogue.active=false;}
    reanudar();
  }
  public void reanudar(){perderFoco();previousFrameNanos=0;simulationDebt=0;fotogramaPausa=null;}
  public Resultado resultado() {
    return game == null ? new Resultado(false, 0, 0, 0) :
        new Resultado(game.scenes != null && game.scenes.current instanceof EndingScene &&
            ((EndingScene) game.scenes.current).dialogueDone,
            Math.round(game.confidence * 100), Math.round(game.disinfo * 100), game.totalKills);
  }

  public void autoprobar() {
    if (game == null) throw new IllegalStateException("La misión 3 no está iniciada");
    runSelfTest();
  }

  public void cerrar() {
    if (game != null && game.audio != null) game.audio.close();
    game = null;
    anfitrion = null;
    fotogramaPausa = null;
    g = null;
  }

  private InputStream assetStream(String path) {
    InputStream stream = getClass().getResourceAsStream("/cliente/mision3/" + path);
    if (stream == null) throw new IllegalStateException("Falta recurso de misión 3: " + path);
    return stream;
  }

  private PImage loadImageResource(String path) {
    try (InputStream stream = assetStream(path)) {
      return new PImage(javax.imageio.ImageIO.read(stream));
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo leer imagen de misión 3: " + path, e);
    }
  }

  // Procedencia: CivitasEnRojo.pde

final int VW = 1152;
final int VH = 648;

GameManager game;
String snapshotScene=null;
boolean mandoActivo=false;
long previousFrameNanos=0;
double simulationDebt=0;

public void draw() {
  background(4, 14, 22);
  long now=System.nanoTime();
  if(previousFrameNanos==0)previousFrameNanos=now;
  simulationDebt+=Math.min(0.10f,(now-previousFrameNanos)/1000000000.0f);
  previousFrameNanos=now;
  int steps=0;
  while(simulationDebt>=1.0f/60.0f&&steps++<6){game.update();simulationDebt-=1.0f/60.0f;}
  game.render();
  fill(245, 232, 251); textAlign(RIGHT, BOTTOM); textSize(12);
  text(mandoActivo?"MENÚ · PAUSA / SELECTOR":"ESC / P · PAUSA", VW-18, VH-12);
}

public void keyPressed() {
  if (game != null) game.onKeyPressed(key, keyCode);
}

public void keyReleased() {
  if (game != null) game.onKeyReleased(key, keyCode);
}

public void mousePressed() {
  if (game != null) game.onMousePressed(mouseButton);
}

public void mouseReleased() {
  if (game != null) game.onMouseReleased(mouseButton);
}

void panelPixel(float x,float y,float w,float h,int fondo,int borde) {
  noStroke();fill(13,8,24,130);rect(x+4,y+5,w,h);
  fill(fondo);stroke(borde);strokeWeight(2);
  beginShape();vertex(x+6,y);vertex(x+w-6,y);vertex(x+w,y+6);
  vertex(x+w,y+h-6);vertex(x+w-6,y+h);vertex(x+6,y+h);
  vertex(x,y+h-6);vertex(x,y+6);endShape(CLOSE);
  noStroke();fill(borde);
  rect(x+3,y+3,5,5);rect(x+w-8,y+3,5,5);
  rect(x+3,y+h-8,5,5);rect(x+w-8,y+h-8,5,5);
}

void pixelRing(float cx,float cy,float rx,float ry,int col){
  noStroke();fill(col);
  int count=max(24,ceil(TWO_PI*max(rx,ry)/6f));
  for(int i=0;i<count;i++){
    float angle=TWO_PI*i/count;
    float x=round((cx+cos(angle)*rx)/6f)*6;
    float y=round((cy+sin(angle)*ry)/6f)*6;
    rect(x-3,y-3,6,6);
  }
}
void pixelLine(float x1,float y1,float x2,float y2,int col){
  noStroke();fill(col);
  int count=max(1,ceil(dist(x1,y1,x2,y2)/7f));
  for(int i=0;i<=count;i++){
    float t=i/(float)count;
    float x=round(lerp(x1,x2,t)/6f)*6;
    float y=round(lerp(y1,y2,t)/6f)*6;
    rect(x-3,y-3,6,6);
  }
}
void pixelTarget(float x,float y,int col){
  pixelRing(x,y,15,10,col);
  pixelLine(x-22,y,x-14,y,col);pixelLine(x+14,y,x+22,y,col);
  pixelLine(x,y-17,x,y-10,col);pixelLine(x,y+10,x,y+17,col);
}
void pixelBox(float x,float y,float w,float h,int fondo,int borde){
  noStroke();fill(fondo);rect(x,y,w,h);
  fill(borde);rect(x,y,w,3);rect(x,y+h-3,w,3);
  rect(x,y,3,h);rect(x+w-3,y,3,h);
  rect(x+3,y+3,6,6);rect(x+w-9,y+3,6,6);
  rect(x+3,y+h-9,6,6);rect(x+w-9,y+h-9,6,6);
}


  // Procedencia: Assets.pde
class Assets {
  PImage bgAtrio, bgPixel, bgCorrupted, bgCore, viralCore;
  PImage[][] viralCoreMove = new PImage[4][4];
  PImage troll, deepfake, spambot, clickbait, comment, botswarm;
  PImage[][][] enemyWalk = new PImage[6][4][4];
  PImage[][] playerWalk = new PImage[4][4];
  PImage[] playerIdle = new PImage[4];
  PImage[] playerLive = new PImage[4];
  PImage[] playerIdleDirection = new PImage[4];
  PImage[] palomoIdle = new PImage[4];
  PImage[] palomoWalk = new PImage[4];
  PImage[] palomoTalk = new PImage[4];
  PImage[] palomoFly = new PImage[4];

  Assets() {
    bgAtrio = loadImageResource("redisenio/fondo-atrio.png");
    bgPixel = loadImageResource("redisenio/fondo-pixel.png");
    bgCorrupted = loadImageResource("redisenio/fondo-pasillo.png");
    bgCore = loadImageResource("redisenio/fondo-nucleo.png");
    viralCore = loadImageResource("redisenio/viral_core.png");

    troll = loadImageResource("redisenio/troll.png");
    deepfake = loadImageResource("redisenio/deepfake.png");
    spambot = loadImageResource("redisenio/spambot.png");
    clickbait = loadImageResource("redisenio/clickbait.png");
    comment = loadImageResource("redisenio/comment.png");
    botswarm = loadImageResource("redisenio/botswarm.png");

    String[] dirs = {"front","back","left","right"};
    for(int direction=0;direction<4;direction++)
      for(int frame=0;frame<4;frame++)
        viralCoreMove[direction][frame]=loadImageResource(
            "redisenio/viral_core_move_"+dirs[direction]+"_"+(frame+1)+".png");
    String[] enemyNames = {"troll","spambot","clickbait","deepfake","comment","botswarm"};
    for(int type=0;type<enemyNames.length;type++)
      for(int direction=0;direction<4;direction++)
        for(int frame=0;frame<4;frame++)
          enemyWalk[type][direction][frame]=loadImageResource(
              "redisenio/enemy_"+enemyNames[type]+"_"+dirs[direction]+"_"+(frame+1)+".png");
    for (int d=0; d<4; d++) {
      for (int i=0; i<4; i++) playerWalk[d][i] = loadImageResource("redisenio/player_"+dirs[d]+"_"+(i+1)+".png");
      playerIdleDirection[d] = loadImageResource("redisenio/player_"+dirs[d]+"_idle.png");
    }
    for (int i=0; i<4; i++) {
      playerIdle[i] = loadImageResource("redisenio/player_front_idle.png");
      playerLive[i] = loadImageResource("redisenio/player_front_live.png");
      palomoIdle[i] = loadImageResource("redisenio/palomo_idle_"+(i+1)+".png");
      palomoWalk[i] = loadImageResource("redisenio/palomo_walk_"+(i+1)+".png");
      palomoTalk[i] = loadImageResource("redisenio/palomo_talk_"+(i+1)+".png");
      palomoFly[i] = loadImageResource("redisenio/palomo_fly_"+(i+1)+".png");
    }
  }
}


  // Procedencia: AudioManager.pde
class AudioManager {
  HashMap<String, Clip> clips = new HashMap<String, Clip>();
  Clip musicClip;
  String musicId="";

  AudioManager() {
    loadClip("ping","audio/ping.wav");
    loadClip("hit","audio/hit.wav");
    loadClip("flash","audio/flash.wav");
    loadClip("dash","audio/dash.wav");
    loadClip("alert","audio/alert.wav");
    loadClip("live","audio/live.wav");
    loadClip("boss","audio/boss.wav");
    loadClip("dialogue","audio/dialogue.wav");
    loadClip("private","audio/private.wav");
    loadClip("ui","audio/ui.wav");
    loadClip("ambient_music","audio/ambient_loop.wav");
    loadClip("combat_music","audio/combat_loop.wav");
    loadClip("boss_music","audio/boss_loop.wav");
  }

  void loadClip(String id, String rel) {
    try {
      AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(assetStream(rel)));
      Clip clip = AudioSystem.getClip();
      clip.open(ais);
      ais.close();
      clips.put(id, clip);
    } catch(Exception e) {
      println("Audio opcional no cargó: " + rel);
    }
  }

  void music(String id){
    if(id.equals(musicId))return;
    if(musicClip!=null)musicClip.stop();
    musicId=id;
    musicClip=clips.get(id+"_music");
    if(musicClip==null)return;
    try {
      if(musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN))
        ((FloatControl)musicClip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-12.0f);
      musicClip.setFramePosition(0);
      musicClip.loop(Clip.LOOP_CONTINUOUSLY);
    }catch(Exception e){println("Música opcional no disponible: "+id);}
  }

  void close() { for (Clip clip : clips.values()) clip.close(); }

  void play(String id) {
    Clip c = clips.get(id);
    if (c == null) return;
    try {
      if (c.isRunning()) c.stop();
      c.setFramePosition(0);
      c.start();
    } catch(Exception e) {}
  }
}


  // Procedencia: CameraManager.pde
class CameraManager {
  static final int EXPLORATION=0, DIALOGUE=1, CINEMATIC=2, COMBAT=3, BOSS=4, IMPACT=5;
  GameManager g;
  PVector pos=new PVector(VW/2,VH/2), target=new PVector(VW/2,VH/2);
  float zoom=1, targetZoom=1;
  float pulseZoom=0;
  int mode=EXPLORATION;
  int resumeMode=EXPLORATION,impactFrames=0;
  float shake=0;

  CameraManager(GameManager gm){g=gm;}

  void update(){
    if(mode==IMPACT){
      if(impactFrames>0)impactFrames--;
      else mode=resumeMode;
    }
    if(mode==EXPLORATION || mode==COMBAT){
      float lead=mode==COMBAT?22:12;
      target.set(constrain(g.player.pos.x+g.player.facing.x*lead,510,642),
                 constrain(g.player.pos.y-35+g.player.facing.y*lead,290,360));
      targetZoom=mode==COMBAT?1.025f:1.0f;
    }else if(mode==BOSS){target.set(VW/2,VH/2);targetZoom=1.0f;}
    float halfTargetX=VW/(2*max(1,targetZoom));
    float halfTargetY=VH/(2*max(1,targetZoom));
    target.x=constrain(target.x,halfTargetX,VW-halfTargetX);
    target.y=constrain(target.y,halfTargetY,VH-halfTargetY);
    float follow=(mode==CINEMATIC?.035f:mode==DIALOGUE?.09f:.075f);
    pos.lerp(target,follow);
    zoom=lerp(zoom,targetZoom+pulseZoom,.13f);
    pulseZoom*=.90f;
    if(pulseZoom<.001f)pulseZoom=0;
    float halfX=VW/(2*max(1,zoom)),halfY=VH/(2*max(1,zoom));
    pos.x=constrain(pos.x,halfX,VW-halfX);
    pos.y=constrain(pos.y,halfY,VH-halfY);
    shake*=.76f;
    if(shake<.15f)shake=0;
  }
  void setMode(int m){mode=m;impactFrames=0;}
  void cinematic(float x,float y,float z){mode=CINEMATIC;target.set(x,y);targetZoom=z;}
  void focus(float x,float y,float z){mode=DIALOGUE;target.set(constrain(x,440,712),constrain(y,240,400));targetZoom=z;}
  void reset(){mode=EXPLORATION;targetZoom=1;}
  void impact(float amount){
    if(mode!=IMPACT)resumeMode=mode;
    mode=IMPACT;impactFrames=max(impactFrames,4);
    shake=max(shake,amount);
  }
  void pulse(float amount){pulseZoom=max(pulseZoom,amount);}
  void begin(){pushMatrix();translate(VW/2+random(-shake,shake),VH/2+random(-shake,shake));scale(zoom);translate(-pos.x,-pos.y);}
  void end(){popMatrix();}
  PVector screenToWorld(float sx,float sy){return new PVector((sx-VW/2)/zoom+pos.x,(sy-VH/2)/zoom+pos.y);}
}


  // Procedencia: CinematicManager.pde
class CinematicManager {
  int frame=0;
  boolean running=false;
  void begin(){frame=0;running=true;}
  void update(){if(running)frame++;}
  boolean at(int cue){return running&&frame==cue;}
  float progress(int first,int last){return constrain((frame-first)/(float)(last-first),0,1);}
  void jumpTo(int cue){frame=cue;}
  void end(){running=false;}
}


  // Procedencia: CivitasSystem.pde
class CivitasSystem {
  GameManager g;
  final ArbolAVLPublicaciones publicaciones = new ArbolAVLPublicaciones();
  final List<Integer> ordenDifusion = new GrafoDifusionCivitas().ordenDesde(0);
  int cuentasAlcanzadas;
  int stage=0, anomalyTick=-1;
  int[] shares={12,53,181,502,1204,2841,7310};
  String[] trends={"#NovaMallHoy", "#AlgoPasa", "#CivitasCaido", "#NoConfiesEnNadie", "#NoConfiesEnNadie", "#Viralidad100"};
  CivitasSystem(GameManager gm){
    g=gm;
    publicaciones.insertar(new Publicacion(301,5,"@NovaMall",
        "NovaMall sigue despierto esta noche.","Normal"));
  }
  void setStage(int value){
    stage=constrain(value,0,5);
    if(stage==0) {
      publicaciones.eliminar(302);
      anomalyTick=-1;
      cuentasAlcanzadas=0;
    }
  }
  void startAnomaly(){
    anomalyTick=0;stage=1;cuentasAlcanzadas=0;
    if(publicaciones.buscar(302)==null) publicaciones.insertar(new Publicacion(302,25,
        "@Usuario_000","EL INFLUENCER ESTÁ MINTIENDO","Sin verificar"));
    g.audio.play("alert");
  }
  void update(){
    if(anomalyTick<0)return;
    anomalyTick++;
    int objetivo=min(ordenDifusion.size(),1+anomalyTick/28);
    while(cuentasAlcanzadas<objetivo){
      int cuenta=ordenDifusion.get(cuentasAlcanzadas++);
      publicaciones.actualizar(new Publicacion(302,min(100,25+cuentasAlcanzadas*10),
          "@Usuario_%03d".formatted(cuenta),"EL INFLUENCER ESTÁ MINTIENDO","Viral"));
      if(cuentasAlcanzadas>1)g.addDisinfo(.012f);
    }
  }
  int shareCount(){
    if(anomalyTick<0)return 0;
    int i=max(0,min(shares.length-1,cuentasAlcanzadas-1));
    return shares[i];
  }
  float corruption(){return stage==0?0:constrain((stage+min(1,g.disinfo))/6.0f,0,1);}
  void renderWorldSignals(int zone){
    int glow=stage==0?color(45,180,255):color(255,45,75);
    float flicker=stage==0?1:(frameCount%17<2?.35f:1);
    blendMode(ADD);noStroke();
    if(zone==0){
      fill(glow,22*flicker);ellipse(215,365,240,195);
      fill(glow,18*flicker);ellipse(810,290,190,155);
    }else if(zone==1){
      fill(glow,20*flicker);ellipse(415,260,185,135);
      ellipse(720,320,180,130);ellipse(925,250,175,130);
    }else if(zone==2){
      fill(glow,22*flicker);ellipse(195,220,355,200);
      ellipse(1000,220,285,195);
    }else{
      fill(glow,27*flicker);ellipse(575,205,520,260);
      fill(45,190,255,9);ellipse(575,460,600,210);
    }
    blendMode(BLEND);
    if(zone==0&&stage>0){
      noStroke();fill(125,0,22,min(215,stage*43)*flicker);
      rect(928,60,224,525);
      fill(255,35,65,25+stage*13);
      quad(843,267,919,286,919,342,843,322);
      quad(146,310,239,299,239,411,146,426);
    }
    if(stage>0 && frameCount%29<3){
      noStroke();fill(255,65,90,65);
      float y=random(175,430);
      rect(zone==0?125:zone==1?310:180,y,random(35,100),2);
    }
  }
  void renderScreens(){
    // La interfaz es una capa de Civitas, no un personaje superpuesto al fondo.
    float x=921,y=164,w=222,h=126;
    int accent=stage==0?color(244,203,106):color(245,108,166);
    panelPixel(x,y,w,h,color(36,21,59,235),accent);
    fill(accent);textAlign(LEFT,TOP);textSize(12);text("CIVITAS  //  "+trends[stage],x+10,y+10);
    fill(225);textSize(12);
    Publicacion principal=publicaciones.recorrer("inverso").getFirst();
    String post=principal.autor()+": "+principal.texto();
    text(post,x+10,y+37,w-20,59);
    fill(stage==0?color(244,203,106):color(255,172,85));textSize(12);
    text(stage==0?"TENDENCIAS NORMALES":"COMPARTIDOS  "+shareCount(),x+10,y+103);
    if(stage>0 && frameCount%23<2){fill(255,45,70,95);rect(x+random(w),y,random(20,80),random(2,7));}
  }
}


  // Procedencia: CollisionWorld.pde
class RectCollider {
  float x,y,w,h;
  String id;
  boolean blocksProjectiles;
  RectCollider(String id_, float x_, float y_, float w_, float h_,boolean projectiles) {
    id=id_;x=x_;y=y_;w=w_;h=h_;blocksProjectiles=projectiles;
  }
  boolean contains(float px,float py){return px>=x&&px<=x+w&&py>=y&&py<=y+h;}
  boolean overlaps(float x0,float y0,float w0,float h0) {
    return x0<x+w && x0+w0>x && y0<y+h && y0+h0>y;
  }
}

class TriggerZone extends RectCollider {
  boolean fired=false;
  TriggerZone(String id,float x,float y,float w,float h){super(id,x,y,w,h,false);}
}

class CollisionWorld {
  ArrayList<RectCollider> solids = new ArrayList<RectCollider>();
  ArrayList<TriggerZone> triggers = new ArrayList<TriggerZone>();
  float minX=48,maxX=VW-48,minY=150,maxY=VH-45;
  boolean elliptical=false;
  float ellipseX,ellipseY,ellipseRx,ellipseRy;
  boolean debug=false;

  void addSolid(String id,float x,float y,float w,float h){addSolid(id,x,y,w,h,true);}
  void addSolid(String id,float x,float y,float w,float h,boolean projectiles){
    solids.add(new RectCollider(id,x,y,w,h,projectiles));
  }
  void setEllipse(float x,float y,float rx,float ry){
    elliptical=true;ellipseX=x;ellipseY=y;ellipseRx=rx;ellipseRy=ry;
  }
  TriggerZone addTrigger(String id,float x,float y,float w,float h){
    TriggerZone t=new TriggerZone(id,x,y,w,h);triggers.add(t);return t;
  }

  void move(Player p,float dx,float dy) {
    float hw=16, hh=9;
    int steps=max(1,ceil(max(abs(dx),abs(dy))/4.0f));
    for(int i=0;i<steps;i++){
      float nx=constrain(p.pos.x+dx/steps,minX,maxX);
      if(canOccupy(nx,p.pos.y,hw,hh))p.pos.x=nx;
      else p.velocity.x=0;
      float ny=constrain(p.pos.y+dy/steps,minY,maxY);
      if(canOccupy(p.pos.x,ny,hw,hh))p.pos.y=ny;
      else p.velocity.y=0;
    }
  }

  boolean canStand(float x,float y){return canOccupy(x,y,16,9);}

  boolean canOccupy(float x,float y,float hw,float hh){
    if(x<minX||x>maxX||y<minY||y>maxY)return false;
    if(elliptical){
      float dx=(x-ellipseX)/(ellipseRx-hw),dy=(y-ellipseY)/(ellipseRy-hh);
      if(dx*dx+dy*dy>1)return false;
    }
    return !hits(x-hw,y-hh,hw*2,hh*2);
  }

  boolean hits(float x,float y,float w,float h) {
    for(RectCollider r:solids) if(r.overlaps(x,y,w,h)) return true;
    return false;
  }
  boolean hitsProjectile(float x,float y,float w,float h){
    for(RectCollider r:solids)if(r.blocksProjectiles&&r.overlaps(x,y,w,h))return true;
    return false;
  }

  boolean inside(String id,PVector p) {
    for(TriggerZone t:triggers) if(t.id.equals(id)) return t.contains(p.x,p.y);
    return false;
  }

  void renderDebug() {
    if (!game.debug) return;
    noFill(); strokeWeight(2);
    stroke(255,90,90,180);
    for(RectCollider r:solids) rect(r.x,r.y,r.w,r.h);
    if(elliptical)ellipse(ellipseX,ellipseY,ellipseRx*2,ellipseRy*2);
    stroke(80,255,170,180);
    for(TriggerZone t:triggers) rect(t.x,t.y,t.w,t.h);
    noStroke();
  }
}


  // Procedencia: CombatGeometry.pde
class Hurtbox {
  PVector center;
  float radius;
  Hurtbox(PVector p,float r){center=p;radius=r;}
}

class Hitbox {
  PVector center;
  float radius;
  Hitbox(PVector p,float r){center=p;radius=r;}
  boolean overlaps(Hurtbox target){
    float dx=center.x-target.center.x,dy=center.y-target.center.y;
    float sum=radius+target.radius;
    return dx*dx+dy*dy<sum*sum;
  }
}


  // Procedencia: DialogueManager.pde

class DialogueLine {
  String speaker;
  String text;
  float focusX, focusY;
  PVector cameraTarget;
  String animation;
  int accent;
  PImage portrait;

  DialogueLine(String s, String t, float x, float y, int a) {
    this(s,t,x,y,a,null);
  }

  DialogueLine(String s, String t, float x, float y, int a, PImage p) {
    this(s,t,x,y,a,p,"TALK");
  }

  DialogueLine(String s, String t, float x, float y, int a, PImage p,String anim) {
    speaker=s; text=t; focusX=x; focusY=y; cameraTarget=new PVector(x,y);
    accent=a; portrait=p;animation=anim;
  }
}

class DialogueManager {
  GameManager g;
  ArrayList<DialogueLine> lines = new ArrayList<DialogueLine>();
  int index=0;
  int visibleChars=0;
  int tick=0;
  int lastAdvanceFrame=-100;
  boolean active=false;
  boolean completedPulse=false;
  int previousCameraMode=CameraManager.EXPLORATION;

  DialogueManager(GameManager gm) { g=gm; }

  void start(DialogueLine... ls) {
    previousCameraMode=g.camera.mode==CameraManager.IMPACT?g.camera.resumeMode:g.camera.mode;
    lines.clear();
    for (DialogueLine l:ls) lines.add(l);
    index=0; visibleChars=0; tick=0; active=lines.size()>0; completedPulse=false;
    lastAdvanceFrame=frameCount-20;
    if (active) focusCurrent();
  }

  void add(DialogueLine l) { lines.add(l); }

  void update() {
    if (!active) return;
    tick++;
    DialogueLine l=lines.get(index);

    // Texto más rápido para evitar que parezca congelado.
    if (tick%1==0 && visibleChars<l.text.length()) {
      visibleChars=min(l.text.length(), visibleChars+2);
      if (visibleChars%8==0) g.audio.play("dialogue");
    }
    g.camera.focus(l.cameraTarget.x,l.cameraTarget.y,1.055f);
  }

  void advance() {
    if (!active) return;
    // Evita dobles avances por autorepetición de ENTER.
    if (frameCount-lastAdvanceFrame<5) return;
    lastAdvanceFrame=frameCount;
    g.audio.play("ui");

    DialogueLine l=lines.get(index);
    if (visibleChars<l.text.length()) {
      visibleChars=l.text.length();
      return;
    }

    index++;
    visibleChars=0;
    tick=0;

    if (index>=lines.size()) {
      active=false;
      completedPulse=true;
      g.camera.setMode(previousCameraMode);
    } else {
      focusCurrent();
    }
  }

  void focusCurrent() {
    if (!active) return;
    DialogueLine l=lines.get(index);
    g.camera.focus(l.cameraTarget.x,l.cameraTarget.y,1.055f);
  }

  String currentSpeaker() {
    if (!active || index<0 || index>=lines.size()) return "";
    return lines.get(index).speaker;
  }

  boolean consumeCompleted() {
    if (!completedPulse) return false;
    completedPulse=false;
    return true;
  }

  void render() {
    if (!active) return;
    DialogueLine l=lines.get(index);
    String shown=l.text.substring(0,min(visibleChars,l.text.length()));

    float x=74, y=486, w=1004, h=142;
    panelPixel(x,y,w,h,color(36,21,59,242),l.accent);

    boolean hasPortrait=l.portrait!=null;
    float textX=x+40;

    if (hasPortrait) {
      // Retrato intencional en un marco, nunca flotando sobre el mapa.
      float px=x+74, py=y+70;
      panelPixel(x+16,y+13,116,116,color(67,43,91),l.accent);

      pushMatrix();
      // Recorte visual por marco: se muestra como retrato/busto.
      imageMode(CENTER);
      float ph=110;
      float pw=ph*l.portrait.width/max(1.0f,l.portrait.height);
      image(l.portrait,px,py+6,pw,ph);
      popMatrix();

      textX=x+158;
    } else {
      fill(l.accent);
      rect(x+20,y+18,6,28);
    }

    fill(l.accent);
    textAlign(LEFT,TOP);
    textSize(18);
    text(l.speaker,textX,y+16);

    fill(225,238,245);
    textSize(18);
    text(shown,textX,y+48,w-(textX-x)-34,61);

    boolean fullyShown=visibleChars>=l.text.length();
    fill(244,203,106);
    textSize(13);
    textAlign(RIGHT,BOTTOM);
    text(fullyShown ? "ENTER / E  ·  continuar" : "ENTER / E  ·  mostrar texto",
         x+w-22,y+h-14);

    if (fullyShown && frameCount%50<25) {
      fill(l.accent);
      triangle(x+w-38,y+h-27,x+w-26,y+h-27,x+w-32,y+h-19);
    }
  }
}


  // Procedencia: Effects.pde
abstract class Effect {
  boolean dead=false;
  abstract void update();
  abstract void render();
}

class ParticleFX extends Effect {
  PVector pos,vel;
  int col;
  float life,maxLife,size;
  ParticleFX(float x,float y,int c,float sp) {
    pos=new PVector(x,y);
    float a=random(TWO_PI);
    vel=PVector.fromAngle(a).mult(random(.4f,sp));
    col=c; maxLife=life=random(18,34); size=random(2,7);
  }
  void update(){pos.add(vel);vel.mult(.93f);life--;if(life<=0)dead=true;}
  void render(){noStroke();fill(col,255*(life/maxLife));ellipse(pos.x,pos.y,size,size);}
}

class ShockwaveFX extends Effect {
  PVector pos;
  float r=12,maxR,life,maxLife;
  int col;
  ShockwaveFX(float x,float y,float mr,int c){
    pos=new PVector(x,y);maxR=mr;col=c;maxLife=life=22;
  }
  void update(){r=lerp(r,maxR,.18f);life--;if(life<=0)dead=true;}
  void render(){pixelRing(pos.x,pos.y,r,r,color(red(col),green(col),blue(col),220*(life/maxLife)));}
}

class FlashPulseFX extends Effect {
  ActionScene scene;
  PVector center;
  float radius=10,maxRadius,damage;
  int life=12;
  HashSet<Enemy> struck=new HashSet<Enemy>();
  HashSet<ServerNode> struckServers=new HashSet<ServerNode>();
  boolean bossStruck=false;
  FlashPulseFX(ActionScene s,float x,float y,float r,float dmg){
    scene=s;center=new PVector(x,y);maxRadius=r;damage=dmg;
    for(int i=0;i<22;i++)s.effects.add(new ParticleFX(x,y-28,color(255,170,45),7));
  }
  void update(){
    radius=min(maxRadius,radius+maxRadius/10);
    for(Enemy e:scene.enemies)if(!e.dead&&!struck.contains(e)&&PVector.dist(center,e.pos)<=radius+e.hitRadius){
      PVector push=PVector.sub(e.pos,center);
      if(push.magSq()<.01f)push.set(0,-1);
      e.takeDamage(damage,push.normalize().mult(24),scene);
      if(!e.dead&&(e.state==1||e.state==2))e.recover(20);
      struck.add(e);
    }
    for(Projectile p:scene.projectiles)if(!p.fromPlayer&&!p.dead&&PVector.dist(center,p.pos)<radius){
      p.dead=true;scene.effects.add(new ParticleFX(p.pos.x,p.pos.y,color(255,180,50),5));
    }
    if(scene instanceof PixelLabScene)for(ServerNode node:((PixelLabScene)scene).servers)
      if(!node.dead&&!struckServers.contains(node)&&PVector.dist(center,node.pos)<radius+40){
        node.damage(damage);struckServers.add(node);
      }
    if(scene instanceof ViralCoreScene&&!bossStruck){
      ViralCore b=((ViralCoreScene)scene).boss;
      if(b!=null&&!b.dead&&PVector.dist(center,b.pos)<radius+b.hitRadius){
        b.takeDamage(damage*.6f,new PVector(0,0),(ViralCoreScene)scene);bossStruck=true;
      }
    }
    if(--life<=0)dead=true;
  }
  void render(){
    pixelRing(center.x,center.y,radius,radius,color(255,120,30,150+life*7));
    pixelRing(center.x,center.y,max(0,radius-7),max(0,radius-7),color(255,215,90,220));
  }
}

class FloatTextFX extends Effect {
  PVector pos;
  String textValue;
  int col;
  float life=70;
  FloatTextFX(float x,float y,String t,int c){pos=new PVector(x,y);textValue=t;col=c;}
  void update(){pos.y-=.35f;life--;if(life<=0)dead=true;}
  void render(){fill(col,255*life/70.0f);textAlign(CENTER,CENTER);textSize(13);text(textValue,pos.x,pos.y);}
}

class AfterImageFX extends Effect {
  PImage sprite;
  float x,y;
  int life=11;
  AfterImageFX(PImage image_,float x_,float y_){sprite=image_;x=x_;y=y_;}
  void update(){if(--life<=0)dead=true;}
  void render(){
    tint(55,255,210,life*13);
    imageMode(CENTER);image(sprite,x,y-32,47,64);noTint();
  }
}

class CorruptZoneFX extends Effect {
  ActionScene scene;
  PVector pos;
  float radius=55;
  int life=160;
  int tick=0;
  CorruptZoneFX(ActionScene s,float x,float y){scene=s;pos=new PVector(x,y);}
  void update(){
    tick++;life--;
    if(tick>30 && tick%30==0 && PVector.dist(scene.g.player.pos,pos)<radius) scene.g.player.hurt(.07f);
    if(life<=0)dead=true;
  }
  void render(){
    int alert=tick<=30?color(255,190,45):color(190,80,255);
    noStroke();fill(tick<=30?color(255,175,40,30):color(145,45,230,65));
    for(int dx=-42;dx<=42;dx+=14)for(int dy=-42;dy<=42;dy+=14)
      if(dx*dx+dy*dy<radius*radius)rect(pos.x+dx-4,pos.y+dy-4,8,8);
    pixelRing(pos.x,pos.y,radius,radius,alert);
    pixelRing(pos.x,pos.y,radius*.8f,radius*.8f,color(red(alert),green(alert),blue(alert),130));
  }
}

class PopupZoneFX extends Effect {
  ActionScene scene;
  PVector pos;
  int age=0;
  PopupZoneFX(ActionScene s,float x,float y){scene=s;pos=new PVector(x,y);}
  void update(){
    age++;
    if(age>30&&age<105&&age%24==0&&abs(scene.g.player.pos.x-pos.x)<55&&abs(scene.g.player.pos.y-pos.y)<36)
      scene.g.player.hurt(.055f);
    if(age>=105)dead=true;
  }
  void render(){
    boolean active=age>30;
    pixelBox(pos.x-55,pos.y-36,110,72,
      active?color(240,70,25,85):color(255,185,50,35),
      active?color(255,100,40):color(255,210,65));
    fill(255,230,180);textAlign(CENTER,CENTER);textSize(10);text("VENTANA EMERGENTE",pos.x,pos.y);
  }
}

class MimicFX extends Effect {
  ActionScene scene;
  PVector pos,target;
  int life=70;
  boolean fired=false;
  MimicFX(ActionScene s,float x,float y,PVector locked){scene=s;pos=new PVector(x,y);target=locked.copy();}
  void update(){
    life--;
    if(!fired&&life==35){
      fired=true;
      scene.projectiles.add(new Projectile(pos.x,pos.y-32,PVector.sub(target,pos),6.1f,.08f,9,color(180,95,255),false));
    }
    if(life<=0)dead=true;
  }
  void render(){
    noStroke();fill(0,75);ellipse(pos.x,pos.y+2,42,12);
    tint(185,90,255,170+parseInt(sin(life*1.2f)*55));
    imageMode(CENTER);image(scene.g.assets.playerIdle[(life/9)%4],pos.x,pos.y-32,47,64);noTint();
    pixelLine(pos.x-25,pos.y-65,pos.x+23,pos.y-7,color(230,100,255,170));
  }
}


  // Procedencia: Enemies.pde
interface DepthDrawable {
  float sortY();
  void renderDepth();
}

abstract class Enemy implements DepthDrawable {
  ActionScene scene;
  PVector pos,vel=new PVector();
  Hurtbox hurtbox;
  PImage sprite;
  PImage[][] walk;
  int facingDirection=0,walkingTick=0;
  float hp,maxHp,speed,hitRadius=25;
  boolean dead=false;
  int state=0; //0 chase 1 telegraph 2 attack 3 recovery
  int timer=0;
  int cooldown=0;
  int hitFlash=0;
  PVector lockedTarget=new PVector();
  float rewardConfidence=.02f;
  float rewardLive=.06f;
  boolean airborne=false;

  Enemy(ActionScene s,float x,float y,PImage img,float hp_,float sp,int type) {
    scene=s;pos=new PVector(x,y);hurtbox=new Hurtbox(new PVector(x,y-32),hitRadius);
    sprite=img;walk=s.g.assets.enemyWalk[type];maxHp=hp=hp_;speed=sp;cooldown=parseInt(random(22,48));
  }

  abstract void ai();
  abstract void doAttack();

  void update() {
    if(dead)return;
    if(hitFlash>0)hitFlash--;
    if(cooldown>0)cooldown--;
    ai();
    if(state==0){
      PVector separation=new PVector();
      for(Enemy other:scene.enemies)if(other!=this&&!other.dead){
        PVector away=PVector.sub(pos,other.pos);
        float d=away.mag();
        if(d>0.01f&&d<42)separation.add(away.mult((42-d)/d*.035f));
      }
      moveBy(separation.x,separation.y);
    }
  }

  void moveBy(float dx,float dy){
    float oldX=pos.x,oldY=pos.y;
    int steps=max(1,ceil(max(abs(dx),abs(dy))/4.0f));
    for(int i=0;i<steps;i++){
      float nx=constrain(pos.x+dx/steps,scene.world.minX+15,scene.world.maxX-15);
      if(scene.world.canOccupy(nx,pos.y,12,6))pos.x=nx;
      float ny=constrain(pos.y+dy/steps,scene.world.minY+9,scene.world.maxY-9);
      if(scene.world.canOccupy(pos.x,ny,12,6))pos.y=ny;
    }
    hurtbox.center.set(pos.x,pos.y-32);
    float movedX=pos.x-oldX,movedY=pos.y-oldY;
    if(abs(movedX)+abs(movedY)>.05f){
      facingDirection=abs(movedX)>abs(movedY)?(movedX<0?2:3):(movedY<0?1:0);
      walkingTick++;
    }
  }

  void moveToward(PVector target,float mult) {
    PVector d=PVector.sub(target,pos);
    if(d.magSq()>2) {
      d.normalize().mult(speed*mult);
      moveBy(d.x,d.y);
    }
  }

  void beginTelegraph(int frames) {
    if(state!=0||cooldown>0)return;
    state=1;timer=frames;
    lockedTarget.set(scene.g.player.pos);
  }

  void stateTick() {
    if(timer>0)timer--;
    if(state==1 && timer<=0) {state=2;doAttack();}
    else if(state==3 && timer<=0) state=0;
  }

  void recover(int frames){state=3;timer=frames;}

  void takeDamage(float dmg,PVector knock,ActionScene s) {
    if(dead)return;
    hp-=dmg;
    hitFlash=5;
    moveBy(knock.x,knock.y);
    s.spawnHit(pos.x,pos.y-30,color(90,235,255));
    s.g.hitFeedback(dmg>=2?3.5f:0);
    if(hp<=0) die();
  }

  void die() {
    if(dead)return;
    dead=true;
    for(int i=0;i<16;i++)scene.effects.add(new ParticleFX(pos.x,pos.y-30,color(255,75,95),7));
    scene.g.addConfidence(rewardConfidence);
    scene.g.addLive(rewardLive);
    scene.g.addDisinfo(-.018f);
    scene.onEnemyKilled(this);
  }

  public float sortY(){return pos.y+(airborne?-70:0);}

  public void renderDepth() {
    PImage frame=walk[facingDirection][(walkingTick/6)%4];
    float h=visualHeight();
    float w=h*frame.width/max(1.0f,frame.height);
    float bob=airborne?sin(frameCount*.09f+pos.x*.01f)*7:0;
    noStroke();fill(0,65);ellipse(pos.x,pos.y+5,w*.55f,14);

    if(state==1) {
      pixelRing(pos.x,pos.y-h*.35f+bob,h*.475f,h*.36f,color(255,70,75,200));
      PVector aim=PVector.sub(lockedTarget,pos);
      if(aim.magSq()>1){
        aim.limit(140);
        pixelLine(pos.x,pos.y-35,pos.x+aim.x,pos.y-35+aim.y,color(255,70,75,200));
      }
      pixelTarget(lockedTarget.x,lockedTarget.y,color(255,75,85,180));
    }

    pushMatrix();translate(pos.x,pos.y-h*.45f+bob);
    float breath=1+sin(frameCount*.09f+pos.x*.014f)*.023f;
    if(state==1)scale(1.045f,.965f);
    else if(state==3)scale(1.025f,.985f);
    else scale(breath,1/breath);
    if(hitFlash>0)tint(255);
    imageMode(CENTER);image(frame,0,0,w,h);noTint();popMatrix();

    if(hp<maxHp){
      float bw=54;
      noStroke();fill(0,170);rect(pos.x-bw/2,pos.y+12,bw,5,3);
      fill(255,70,80);rect(pos.x-bw/2,pos.y+12,bw*(hp/maxHp),5,3);
    }
  }

  float visualHeight(){return 80;}

  void shootAtPlayer(float projectileSpeed,float dmg,int col) {
    PVector d=PVector.sub(lockedTarget,pos);
    scene.projectiles.add(new Projectile(pos.x,pos.y-35,d,projectileSpeed,dmg,8,col,false));
  }
}

class TrollEnemy extends Enemy {
  int pattern=0;
  TrollEnemy(ActionScene s,float x,float y){super(s,x,y,s.g.assets.troll,8,1.15f,0);rewardConfidence=.03f;}
  void ai(){
    stateTick(); if(state!=0)return;
    float d=PVector.dist(pos,scene.g.player.pos);
    if(d>220)moveToward(scene.g.player.pos,1);
    if(cooldown<=0){pattern=(pattern+1)%4;beginTelegraph(pattern==3?22:35);cooldown=110;}
  }
  void doAttack(){
    if(pattern==0)shootAtPlayer(5.5f,.08f,color(65,255,105));
    else if(pattern==1){
      scene.effects.add(new FloatTextFX(pos.x,pos.y-105,"RATIO",color(85,255,115)));
      PVector base=PVector.sub(lockedTarget,pos).normalize();
      for(int i=-1;i<=1;i++)scene.projectiles.add(new Projectile(pos.x,pos.y-35,base.copy().rotate(i*.24f),5.0f,.065f,9,color(65,255,105),false));
    }else if(pattern==2){
      PVector base=PVector.sub(lockedTarget,pos).normalize();
      for(int i=-2;i<=2;i++)scene.projectiles.add(new Projectile(pos.x,pos.y-35,base.copy().rotate(i*.13f),4.0f,.05f,11,color(75,240,130),false));
    }else{
      PVector leap=PVector.sub(lockedTarget,pos).normalize().mult(72);
      moveBy(leap.x,leap.y);
      if(PVector.dist(pos,scene.g.player.pos)<68)scene.g.player.hurt(.1f);
    }
    recover(26);
  }
  float visualHeight(){return 94;}
}

class SpamBotEnemy extends Enemy {
  int pattern=0;
  SpamBotEnemy(ActionScene s,float x,float y){super(s,x,y,s.g.assets.spambot,6,1.65f,1);rewardConfidence=.02f;}
  void ai(){
    stateTick();if(state!=0)return;
    float d=PVector.dist(pos,scene.g.player.pos);
    if(d>270)moveToward(scene.g.player.pos,1);
    else if(d<150)moveToward(PVector.add(pos,PVector.sub(pos,scene.g.player.pos)),.85f);
    if(cooldown<=0){pattern=(pattern+1)%4;beginTelegraph(25);cooldown=105;}
  }
  void doAttack(){
    if(pattern==0){shootAtPlayer(6.1f,.065f,color(255,145,25));}
    else if(pattern==1){
      PVector base=PVector.sub(lockedTarget,pos).normalize();
      for(int i=-2;i<=2;i++)scene.projectiles.add(new Projectile(pos.x,pos.y-28,base.copy().rotate(i*.22f),5,.055f,8,color(255,145,25),false));
    }else if(pattern==2){
      scene.effects.add(new PopupZoneFX(scene,lockedTarget.x,lockedTarget.y));
    }else{
      PVector rush=PVector.sub(lockedTarget,pos).normalize().mult(85);
      moveBy(rush.x,rush.y);
      if(PVector.dist(pos,scene.g.player.pos)<65)scene.g.player.hurt(.08f);
    }
    recover(25);
  }
  float visualHeight(){return 78;}
}

class ClickBaitEnemy extends Enemy {
  int pattern=0;
  ClickBaitEnemy(ActionScene s,float x,float y){super(s,x,y,s.g.assets.clickbait,4,2.1f,2);airborne=true;rewardConfidence=.015f;}
  void ai(){
    stateTick();if(state!=0)return;
    PVector target=scene.g.player.pos.copy();
    PVector d=PVector.sub(target,pos);
    if(d.mag()>170){d.normalize();d.rotate(sin(frameCount*.07f+pos.x)*.5f);d.mult(speed);moveBy(d.x,d.y);}
    if(cooldown<=0){pattern=(pattern+1)%2;beginTelegraph(22);cooldown=95;}
  }
  void doAttack(){
    if(pattern==0){
      PVector d=PVector.sub(lockedTarget,pos).normalize().mult(95);
      moveBy(d.x,d.y);
      if(PVector.dist(pos,scene.g.player.pos)<75)scene.g.player.hurt(.085f);
    }else shootAtPlayer(6.7f,.06f,color(255,195,60));
    recover(30);
  }
  public void renderDepth(){
    super.renderDepth();
    if(state==1){
      fill(255,225,70);textAlign(CENTER,CENTER);textSize(14);
      text("¡MIRA ESTO!",pos.x,pos.y-88);
    }
  }
  float visualHeight(){return 58;}
}

class DeepfakeEnemy extends Enemy {
  int pattern=0;
  DeepfakeEnemy(ActionScene s,float x,float y){super(s,x,y,s.g.assets.deepfake,12,1.3f,3);rewardConfidence=.05f;}
  void ai(){
    stateTick();if(state!=0)return;
    float d=PVector.dist(pos,scene.g.player.pos);
    if(d>260)moveToward(scene.g.player.pos,.9f);
    if(cooldown<=0){pattern=(pattern+1)%4;beginTelegraph(38);cooldown=120;}
  }
  void doAttack(){
    if(pattern==0){shootAtPlayer(6,.095f,color(160,95,255));}
    else if(pattern==1){
      float nx=constrain(lockedTarget.x+90,scene.world.minX+20,scene.world.maxX-20);
      float ny=constrain(lockedTarget.y-55,scene.world.minY+20,scene.world.maxY-20);
      if(scene.world.hits(nx-12,ny-6,24,12)){nx=pos.x;ny=pos.y;}
      scene.effects.add(new MimicFX(scene,nx,ny,lockedTarget));
    }else if(pattern==2){
      PVector d=PVector.sub(lockedTarget,pos).normalize().mult(125);
      moveBy(d.x,d.y);
      if(PVector.dist(pos,scene.g.player.pos)<85)scene.g.player.hurt(.14f);
    }else{
      scene.effects.add(new CorruptZoneFX(scene,lockedTarget.x,lockedTarget.y));
      scene.g.addDisinfo(.01f);
    }
    recover(35);
  }
  float visualHeight(){return 88;}
}

class CommentEnemy extends Enemy {
  String rumor="";
  int rumorType=-1;
  int nextRumor=0;
  CommentEnemy(ActionScene s,float x,float y){super(s,x,y,s.g.assets.comment,5,1.5f,4);rewardConfidence=.02f;}
  void ai(){
    stateTick();
    if(state==1)return;
    if(state!=0)return;
    if(PVector.dist(scene.g.player.pos,pos)>125)moveToward(scene.g.player.pos,.7f);
    if(cooldown<=0){
      String[] r={"EL INFLUENCER COMPRÓ SUS SEGUIDORES.","CIVITAS PUBLICA POR TI.","EL SUELO YA NO ES SEGURO."};
      rumorType=nextRumor++%3;rumor=r[rumorType];
      beginTelegraph(42);cooldown=120;
    }
  }
  void doAttack(){
    if(rumorType==0){
      PVector d=PVector.sub(lockedTarget,pos).normalize().mult(120);
      moveBy(d.x,d.y);
      if(PVector.dist(pos,scene.g.player.pos)<65)scene.g.player.hurt(.12f);
    }else if(rumorType==1){
      PVector d=PVector.sub(lockedTarget,pos);
      scene.projectiles.add(new Projectile(pos.x,pos.y-32,d,6.5f,.09f,10,color(255,200,60),false));
    }else scene.effects.add(new CorruptZoneFX(scene,lockedTarget.x,lockedTarget.y));
    recover(32);
  }
  public void renderDepth(){
    super.renderDepth();
    if(state==1){
      float bw=230,bh=50;
      int telegraphColor=rumorType==0?color(255,70,90):rumorType==1?color(255,210,60):color(180,80,255);
      pixelBox(pos.x-bw/2,pos.y-150,bw,bh,color(5,15,22,238),telegraphColor);
      fill(255);textAlign(CENTER,CENTER);textSize(11);text(rumor,pos.x-bw/2+10,pos.y-146,bw-20,bh-6);
    }
  }
  float visualHeight(){return 76;}
}

class BotSwarmEnemy extends Enemy {
  BotSwarmEnemy(ActionScene s,float x,float y){super(s,x,y,s.g.assets.botswarm,3,2.55f,5);airborne=true;rewardConfidence=.012f;rewardLive=.04f;cooldown=25;}
  int swarmIndex(){
    int i=0;
    for(Enemy e:scene.enemies){if(e==this)break;if(e instanceof BotSwarmEnemy&&!e.dead)i++;}
    return i;
  }
  boolean amplifier(){return swarmIndex()==0&&scene.swarmDisrupted==0;}
  void ai(){
    stateTick();if(state!=0)return;
    int count=0;for(Enemy e:scene.enemies)if(e instanceof BotSwarmEnemy&&!e.dead)count++;
    float angle=TWO_PI*swarmIndex()/max(1,count)+frameCount*.006f;
    PVector target=PVector.add(scene.g.player.pos,PVector.fromAngle(angle).mult(145));
    PVector d=PVector.sub(target,pos);
    if(d.mag()>14){d.limit(speed*(scene.swarmDisrupted>0?.65f:1));moveBy(d.x,d.y);}
    if(cooldown<=0){beginTelegraph(scene.swarmDisrupted>0?28:20);cooldown=70+swarmIndex()*18;}
    if(amplifier()&&frameCount%150==0)scene.g.addDisinfo(.009f);
  }
  void doAttack(){
    shootAtPlayer(6.4f,.045f,color(220,70,255));
    recover(18);
  }
  void die(){
    if(amplifier()){
      scene.swarmDisrupted=150;
      scene.effects.add(new FloatTextFX(pos.x,pos.y-65,"SEÑAL ROTA",color(80,245,255)));
    }
    super.die();
  }
  public void renderDepth(){
    super.renderDepth();
    if(amplifier()){
      noFill();stroke(210,70,255,175);strokeWeight(2);
      ellipse(pos.x,pos.y-43,62,62);noStroke();
      fill(235,190,255);textAlign(CENTER,CENTER);textSize(12);text("◆",pos.x,pos.y-79);
    }
  }
  float visualHeight(){return 52;}
}


  // Procedencia: GameManager.pde
class GameManager {
  Assets assets;
  InputManager input;
  AudioManager audio;
  CameraManager camera;
  CivitasSystem civitas;
  DialogueManager dialogue;
  HUD hud;
  SceneManager scenes;
  Player player;

  int hitStop = 0;
  float screenShake = 0;
  int liveOverlayFrames=0;
  boolean debug = false;

  float confidence = 0.35f;
  float disinfo = 0.18f;
  float live = 0.0f;
  int totalKills=0;
  boolean firstFightDone=false, bossDefeated=false;

  GameManager() {
    assets = new Assets();
    input = new InputManager();
    audio = new AudioManager();
    player = new Player(this, 165, 500);
    camera = new CameraManager(this);
    civitas = new CivitasSystem(this);
    dialogue = new DialogueManager(this);
    hud = new HUD(this);
    scenes = new SceneManager(this);
    scenes.start("intro");
  }

  void update() {
    camera.update();
    if(liveOverlayFrames>0)liveOverlayFrames--;

    if (hitStop > 0) {
      hitStop--;
      return;
    }

    dialogue.update();
    civitas.update();
    scenes.update();
    if(screenShake>0){camera.impact(screenShake);screenShake=0;}
  }

  void render() {
    camera.begin();
    scenes.renderWorld();
    if(debug && scenes.current instanceof ActionScene)
      ((ActionScene)scenes.current).world.renderDebug();
    camera.end();

    if(liveOverlayFrames>0){
      noStroke();fill(5,3,14,min(95,liveOverlayFrames*4));rect(0,0,VW,VH);
    }

    scenes.renderOverlay();
    if(!scenes.currentName.equals("intro")&&!scenes.currentName.equals("ending"))civitas.renderScreens();
    dialogue.render();
    scenes.renderTransition();

    if (debug) {
      fill(0,180); noStroke(); rect(8,VH-62,390,54,8);
      fill(80,240,255); textAlign(LEFT,TOP); textSize(12);
      text("F3 debug | Escena: "+scenes.currentName+" | FPS "+nf(frameRate,0,0),18,VH-52);
    }
  }

  void hitFeedback(float strength) {
    if(strength>=3.5f)hitStop=max(hitStop,strength>=6?3:2);
    if(strength>=5)screenShake=max(screenShake,strength);
    audio.play("hit");
  }

  void addConfidence(float v) { confidence = constrain(confidence+v,0,1); }
  void addDisinfo(float v) { disinfo = constrain(disinfo+v,0,1); }
  void addLive(float v) { live = constrain(live+v,0,1); }

  void onKeyPressed(char k, int kc) {
    input.keyDown(k,kc);

    if (dialogue.active) {
      if (k=='e'||k=='E'||k==' '||kc==ENTER) dialogue.advance();
      return;
    }

    if (k=='e'||k=='E'||kc==ENTER) scenes.interact();
    if (k=='j'||k=='J'||k=='f'||k=='F') player.tryPing();
    if (k=='k'||k=='K'||k=='q'||k=='Q') player.tryFlash();
    if (k=='l'||k=='L') player.tryLiveBurst();
    if (kc==SHIFT||kc==java.awt.event.KeyEvent.VK_SPACE) player.tryDash();
    if (kc==CONTROL) input.privateHeld = true;
    if (kc==java.awt.event.KeyEvent.VK_F3) debug=!debug;
    if (k=='r'||k=='R') {
      if (scenes.currentName.equals("ending")) scenes.start("intro");
    }
  }

  void onKeyReleased(char k, int kc) {
    input.keyUp(k,kc);
  }

  void onMousePressed(int b) {
    if (dialogue.active) {
      if (b==LEFT) dialogue.advance();
      return;
    }
    if (b==LEFT) {
      input.mouseLeft=true;
      player.tryPing(true);
    }
    if (b==RIGHT) {
      input.mouseRight=true;
      player.tryFlash();
    }
  }

  void onMouseReleased(int b) {
    if (b==LEFT) input.mouseLeft=false;
    if (b==RIGHT) input.mouseRight=false;
  }

  PVector mouseWorld() {
    return camera.screenToWorld(mouseX,mouseY);
  }
}


  // Procedencia: HUD.pde
class HUD {
  GameManager g;
  HUD(GameManager gm){g=gm;}

  void render(String objective,int remaining) {
    float x=18,y=15,w=1116,h=92;
    panelPixel(x,y,w,h,color(36,21,59,235),color(244,203,106));

    metric(42,38,230,"CONCENTRACIÓN",g.player.concentration,color(126,224,191));
    metric(312,38,230,"CONFIANZA CIVITAS",g.confidence,color(181,142,250));
    metric(582,38,230,"LIVE",g.live,color(244,203,106));
    metric(852,38,230,"DESINFORMACIÓN",g.disinfo,color(245,108,166));

    if(objective!=null && objective.length()>0){
      panelPixel(18,116,450,35,color(36,21,59,235),color(244,203,106));
      fill(255,210,80);textAlign(LEFT,CENTER);textSize(14);
      String objetivoVisible=mandoActivo?objective.replace("PING J  FLASH K  DASH SHIFT","PING RT  FLASH LT  DASH RB"):objective;
      text(objetivoVisible + (remaining>=0?"  ·  "+remaining:""),32,133);
    }
    if(g.player.privateMode){
      fill(175,75,255);textAlign(CENTER,CENTER);textSize(14);
      text("🔒 MODO PRIVADO",VW/2,128);
    }
  }

  void metric(float x,float y,float w,String label,float val,int col){
    fill(col);textAlign(LEFT,CENTER);textSize(13);text(label,x,y-9);
    fill(67,43,91);rect(x,y,w,20);
    fill(col);rect(x+2,y+2,(w-4)*constrain(val,0,1),16);
    fill(220);textAlign(RIGHT,CENTER);textSize(12);text(parseInt(val*100)+"%",x+w,y+31);
  }
}


  // Procedencia: InputManager.pde
class InputManager {
  boolean up, down, left, right;
  boolean privateHeld;
  boolean privatePadHeld;
  float analogX,analogY;
  boolean mouseLeft, mouseRight;

  void keyDown(char k, int kc) {
    if (k=='w'||k=='W'||kc==UP) up=true;
    if (k=='s'||k=='S'||kc==DOWN) down=true;
    if (k=='a'||k=='A'||kc==LEFT) left=true;
    if (k=='d'||k=='D'||kc==RIGHT) right=true;
    if (kc==CONTROL) privateHeld=true;
  }

  void keyUp(char k, int kc) {
    if (k=='w'||k=='W'||kc==UP) up=false;
    if (k=='s'||k=='S'||kc==DOWN) down=false;
    if (k=='a'||k=='A'||kc==LEFT) left=false;
    if (k=='d'||k=='D'||kc==RIGHT) right=false;
    if (kc==CONTROL) privateHeld=false;
  }

  PVector moveVector() {
    float x=(right?1:0)-(left?1:0),y=(down?1:0)-(up?1:0);
    if(x==0&&y==0) {x=analogX;y=analogY;}
    PVector v = new PVector(x,y);
    if (v.magSq()>1) v.normalize();
    return v;
  }
}


  // Procedencia: InteractionPrompt.pde
class InteractionPrompt {
  void render(String label){
    float w=310,h=38,x=(VW-w)/2,y=VH-58;
    panelPixel(x,y,w,h,color(36,21,59,235),color(244,203,106));
    fill(244,203,106);textAlign(CENTER,CENTER);textSize(15);
    text(label,VW/2,y+h/2);
  }
}


  // Procedencia: NPCs.pde
class PalomoNPC implements DepthDrawable {
  GameManager g;
  PVector pos;
  int mode=0; // 0 posado, 1 diálogo, 2 salida, 3 caminar, 4 llegada, 5 fuera
  float targetX;
  PVector flightStart=new PVector(),flightEnd=new PVector();
  int flightTick,flightDuration;
  PalomoNPC(GameManager gm,float x,float y){g=gm;pos=new PVector(x,y);targetX=x;}
  void walkTo(float x){targetX=x;mode=3;}
  void flyIn(float fromX,float fromY,float landX,float landY,int frames){
    pos.set(fromX,fromY);flightStart.set(pos);flightEnd.set(landX,landY);
    flightDuration=frames;flightTick=0;mode=4;
  }
  void flyAway(float exitX,float exitY,int frames){
    flightStart.set(pos);flightEnd.set(exitX,exitY);
    flightDuration=frames;flightTick=0;mode=2;
  }
  boolean landed(){return mode==0;}
  boolean gone(){return mode==5;}
  boolean visible(){return mode!=5;}
  void update(){
    if(mode==2||mode==4){
      flightTick++;
      float t=constrain(flightTick/(float)flightDuration,0,1);
      float eased=t*t*(3-2*t);
      pos.set(lerp(flightStart.x,flightEnd.x,eased),lerp(flightStart.y,flightEnd.y,eased));
      if(t>=1)mode=mode==4?0:5;
    }else if(mode==3){
      float d=targetX-pos.x;
      if(abs(d)>1.5f)pos.x+=constrain(d,-2.1f,2.1f);
      else mode=0;
    }
  }
  public float sortY(){return pos.y;}
  public void renderDepth(){
    if(!visible())return;
    int f=(frameCount/7)%4;
    boolean flying=mode==2||mode==4;
    // La primera fila del atlas de la misión 1 son vistas, no cuadros consecutivos.
    PImage im=flying?g.assets.palomoFly[f]:
        mode==3?g.assets.palomoWalk[targetX<pos.x?1:2]:
        mode==1?g.assets.palomoTalk[0]:g.assets.palomoIdle[0];
    float paso=mode==3&&f%2==1?2:0;
    float t=flightDuration==0?0:constrain(flightTick/(float)flightDuration,0,1);
    float altura=mode==4?(1-t)*18:mode==2?t*18:0;
    pushMatrix();translate(pos.x,pos.y-29-paso-altura+sin(frameCount*.06f)*2);
    if(flying&&flightEnd.x<flightStart.x)scale(-1,1);
    imageMode(CENTER);image(im,0,0,57,60);popMatrix();
  }
}


  // Procedencia: Player.pde
class Player implements DepthDrawable {
  static final int IDLE=0,WALK=1,ATTACK=2,DASH=3,HIT=4,LIVE=5,DEAD=6;
  GameManager g;
  PVector pos=new PVector();
  Hurtbox hurtbox=new Hurtbox(new PVector(),20);
  PVector facing=new PVector(0,1);
  PVector aimMando=new PVector();
  PVector velocity=new PVector();
  float speed=3.65f;
  float concentration=1.0f;
  int invuln=0;
  int pingCd=0,flashCd=0,dashCd=0;
  int dashFrames=0;
  int dashElapsed=0;
  PVector dashDir=new PVector();
  boolean privateMode=false;
  float privacy=1;
  int shieldFlash=0;
  int bufferedPing=0,bufferedFlash=0,bufferedDash=0;
  boolean bufferedMouseAim=false;
  int flashWindup=0;
  int animTick=0;
  int liveFrames=0;
  int attackFrames=0,hitFrames=0,deathFrames=0;
  int animState=IDLE;

  Player(GameManager gm,float x,float y){g=gm;pos.set(x,y);syncHurtbox();}

  void syncHurtbox(){hurtbox.center.set(pos.x,pos.y-32);}

  void reset(float x,float y){
    place(x,y);concentration=1;invuln=0;
    deathFrames=0;hitFrames=0;attackFrames=0;liveFrames=0;animState=IDLE;privateMode=false;
    privacy=1;shieldFlash=0;bufferedPing=bufferedFlash=bufferedDash=flashWindup=0;
  }

  void place(float x,float y){pos.set(x,y);velocity.set(0,0);dashFrames=0;dashElapsed=0;syncHurtbox();}
  void respawn(float x,float y){reset(x,y);invuln=90;}

  void update(ActionScene s) {
    animTick++;
    if(deathFrames>0){
      animState=DEAD;deathFrames--;
      if(deathFrames==0){
        g.addConfidence(-.10f);g.addDisinfo(.08f);
        s.respawnPlayer();
      }
      return;
    }
    if(attackFrames>0)attackFrames--;
    if(hitFrames>0)hitFrames--;
    if(liveFrames>0)liveFrames--;
    if(invuln>0)invuln--;
    if(pingCd>0)pingCd--;
    if(flashCd>0)flashCd--;
    if(dashCd>0)dashCd--;
    if(shieldFlash>0)shieldFlash--;
    if(bufferedPing>0)bufferedPing--;
    if(bufferedFlash>0)bufferedFlash--;
    if(bufferedDash>0)bufferedDash--;
    if(flashWindup>0){
      flashWindup--;
      if(flashWindup==0){
        s.effects.add(new FlashPulseFX(s,pos.x,pos.y,150,2.6f));
        g.screenShake=max(g.screenShake,5);
        g.audio.play("flash");
      }
    }

    boolean wasPrivate=privateMode;
    privateMode=(g.input.privateHeld||g.input.privatePadHeld) && privacy>0.01f;
    if(privateMode&&!wasPrivate)g.audio.play("private");
    if(privateMode)privacy=max(0,privacy-.0012f);
    else privacy=min(1,privacy+.0028f);

    if(bufferedDash>0&&dashCd==0){bufferedDash=0;startDash();}
    if(bufferedFlash>0&&flashCd==0&&dashFrames==0){bufferedFlash=0;startFlash();}
    if(bufferedPing>0&&pingCd==0&&dashFrames==0){bufferedPing=0;firePing(bufferedMouseAim);}

    if(dashFrames>0) {
      animState=DASH;
      dashElapsed++;
      if(dashElapsed%2==1){
        int direction=abs(facing.x)>abs(facing.y)?(facing.x<0?2:3):(facing.y<0?1:0);
        s.effects.add(new AfterImageFX(g.assets.playerWalk[direction][(animTick/4)%4],pos.x,pos.y));
      }
      s.world.move(this,dashDir.x*12.5f,dashDir.y*12.5f);
      syncHurtbox();
      s.addParticleTrail(pos.x,pos.y-38,color(40,255,200));
      dashFrames--;
      if(dashFrames==0)velocity.set(dashDir.copy().mult(speed*.65f));
      return;
    }

    PVector mv=g.input.moveVector();
    if(mv.magSq()>0){
      facing.set(mv);
      velocity.add(mv.mult(1.1f));
      velocity.limit(speed);
    }else velocity.mult(.76f);
    if(velocity.magSq()<.01f)velocity.set(0,0);
    s.world.move(this,velocity.x,velocity.y);
    syncHurtbox();
    animState=liveFrames>0?LIVE:hitFrames>0?HIT:attackFrames>0?ATTACK:
              velocity.magSq()>.08f?WALK:IDLE;
  }

  void tryPing(){tryPing(false);}
  void tryPing(boolean mouseAim) {
    if(g.dialogue.active||deathFrames>0)return;
    ActionScene s=g.scenes.actionScene();
    if(s==null||!s.allowCombat())return;
    if(pingCd>0||dashFrames>0){bufferedPing=8;bufferedMouseAim=mouseAim;return;}
    firePing(mouseAim);
  }

  void firePing(boolean mouseAim){
    ActionScene s=g.scenes.actionScene();
    if(s==null)return;
    pingCd=11;
    attackFrames=9;animState=ATTACK;
    PVector aim=mouseAim?PVector.sub(g.mouseWorld(),new PVector(pos.x,pos.y-42)):
      aimMando.magSq()>.04f?aimMando.copy():facing.copy();
    if(aim.magSq()<.01f)aim=facing.copy();
    s.projectiles.add(new Projectile(pos.x,pos.y-42,aim,10.5f,1.35f,8,color(65,225,255),true));
    for(int i=0;i<5;i++)s.effects.add(new ParticleFX(pos.x,pos.y-42,color(45,225,255),4));
    g.audio.play("ping");
  }

  void tryFlash() {
    if(g.dialogue.active||deathFrames>0)return;
    ActionScene s=g.scenes.actionScene();
    if(s==null||!s.allowCombat())return;
    if(flashCd>0||dashFrames>0){bufferedFlash=8;return;}
    startFlash();
  }

  void startFlash(){
    flashCd=75;
    attackFrames=17;flashWindup=14;animState=ATTACK;
  }

  void tryDash() {
    if(g.dialogue.active||deathFrames>0)return;
    ActionScene s=g.scenes.actionScene();
    if(s==null||!s.allowCombat())return;
    if(dashCd>0){bufferedDash=8;return;}
    startDash();
  }

  void startDash(){
    PVector mv=g.input.moveVector();
    if(mv.magSq()==0)mv=facing.copy();
    if(mv.magSq()==0)mv.set(0,1);
    dashDir=mv.normalize();
    facing.set(dashDir);
    dashFrames=9;
    dashElapsed=0;
    animState=DASH;
    dashCd=32;
    g.audio.play("dash");
  }

  void tryLiveBurst() {
    if(g.dialogue.active||g.live<0.999f||deathFrames>0)return;
    ActionScene s=g.scenes.actionScene();
    if(s==null||!s.allowCombat())return;
    g.live=0;
    liveFrames=36;
    animState=LIVE;
    s.effects.add(new ShockwaveFX(pos.x,pos.y-30,330,color(255,45,70)));
    for(int i=0;i<70;i++)s.effects.add(new ParticleFX(pos.x,pos.y-30,color(255,45,70),11));
    s.liveBurst();
    g.addDisinfo(-.18f);
    g.addConfidence(.08f);
    g.screenShake=15;
    g.hitStop=4;
    g.liveOverlayFrames=25;
    g.camera.pulse(.10f);
    g.audio.play("live");
  }

  void hurt(float dmg) {
    if(invuln>0||deathFrames>0||(dashFrames>0&&dashElapsed>=2&&dashElapsed<=7))return;
    if(privateMode){
      privacy=max(0,privacy-dmg*2.2f);
      shieldFlash=12;
      dmg*=.25f;
      g.audio.play("private");
      if(privacy<=0)privateMode=false;
    }
    concentration-=dmg;
    hitFrames=12;animState=HIT;
    invuln=34;
    g.addDisinfo(.02f);
    g.hitFeedback(6);
    if(concentration<=0) {
      concentration=0;deathFrames=24;invuln=28;animState=DEAD;privateMode=false;
    }
  }

  public float sortY(){return pos.y;}

  public void renderDepth() {
    int dir=0;
    if(abs(facing.x)>abs(facing.y))dir=facing.x<0?2:3;
    else dir=facing.y<0?1:0;
    boolean moving=animState==WALK||animState==ATTACK||animState==DASH;
    int f=(animTick/7)%4;
    PImage im=animState==LIVE?g.assets.playerLive[(animTick/5)%4]:
              (moving||animState==HIT||animState==DEAD)?g.assets.playerWalk[dir][animState==HIT||animState==DEAD?0:f]:
              g.assets.playerIdleDirection[dir];

    noStroke();fill(0,70);ellipse(pos.x,pos.y+2,42,12);
    noFill();stroke(65,225,255,130);strokeWeight(1.5f);ellipse(pos.x,pos.y+2,45,15);noStroke();
    pushMatrix();translate(pos.x,pos.y-(animState==DEAD?20:32));
    if(animState==DEAD)rotate(HALF_PI);
    if(animState==HIT)tint(255,160,160);
    else if(invuln>0&&frameCount%4<2)tint(255,130);
    imageMode(CENTER);image(im,0,0,47,64);noTint();
    if(animState==LIVE){
      noFill();stroke(244,203,106,225);strokeWeight(2);
      rect(19,-26,9,14,2);point(23,-15);noStroke();
    }
    popMatrix();

    if(privateMode){
      blendMode(ADD);noFill();stroke(190,70,255,190);strokeWeight(4);
      ellipse(pos.x,pos.y-32,82,100);
      stroke(230,160,255,100);ellipse(pos.x,pos.y-32,98,116);
      noStroke();blendMode(BLEND);
      fill(235,180,255);textAlign(CENTER,CENTER);textSize(13);text("▣",pos.x,pos.y-93);
      noStroke();fill(10,15,30,190);rect(pos.x-24,pos.y+18,48,4,2);
      fill(180,85,255);rect(pos.x-24,pos.y+18,48*privacy,4,2);
      if(shieldFlash>0){noFill();stroke(255,210,255,shieldFlash*18);strokeWeight(3);
        ellipse(pos.x,pos.y-32,95+shieldFlash,110+shieldFlash);noStroke();}
    }
  }
}


  // Procedencia: Projectile.pde
class Projectile {
  PVector pos,vel;
  Hitbox hitbox;
  float radius,damage;
  int col;
  int life;
  boolean fromPlayer;
  boolean dead=false;

  Projectile(float x,float y,PVector dir,float speed,float dmg,float r,int c,boolean fp) {
    pos=new PVector(x,y);
    hitbox=new Hitbox(pos,r);
    vel=dir.copy().normalize().mult(speed);
    damage=dmg;radius=r;col=c;fromPlayer=fp;life=180;
  }

  void update(ActionScene s) {
    PVector previous=pos.copy();
    pos.add(vel); life--;
    if(life<=0||pos.x<-30||pos.x>VW+30||pos.y<100||pos.y>VH+30){dead=true;return;}
    if(s.world.hitsProjectile(pos.x-radius*.6f,pos.y+28-radius*.6f,radius*1.2f,radius*1.2f)){
      dead=true;
      s.effects.add(new ParticleFX(previous.x,previous.y,col,3));
      return;
    }

    if(fromPlayer) {
      for(Enemy e:s.enemies) {
        if(!e.dead && hitbox.overlaps(e.hurtbox)) {
          e.takeDamage(damage,vel.copy().normalize().mult(18),s);
          dead=true; return;
        }
      }
      if(s instanceof PixelLabScene) {
        if(((PixelLabScene)s).hitServer(pos,radius,damage)) {dead=true;return;}
      }
      if(s instanceof ViralCoreScene) {
        ViralCore b=((ViralCoreScene)s).boss;
        if(b!=null && !b.dead && hitbox.overlaps(b.hurtbox)) {
          b.takeDamage(damage,vel.copy().normalize().mult(12),(ViralCoreScene)s);
          dead=true;return;
        }
      }
    } else {
      if(hitbox.overlaps(s.g.player.hurtbox)) {
        if(s.g.player.privateMode){
          s.effects.add(new ParticleFX(pos.x,pos.y,color(190,85,255),5));
          s.g.player.privacy=max(0,s.g.player.privacy-damage*2.4f);
          s.g.player.shieldFlash=12;
          if(s.g.player.privacy<=0)s.g.player.privateMode=false;
          s.g.audio.play("private");dead=true;return;
        }
        s.g.player.hurt(damage);
        dead=true;return;
      }
    }
  }

  void render() {
    blendMode(ADD);
    noStroke();
    float x=round(pos.x/4f)*4,y=round(pos.y/4f)*4;
    fill(col,45);rect(x-radius*1.5f,y-radius*1.5f,radius*3,radius*3);
    fill(col,110);rect(x-radius,y-radius,radius*2,radius*2);
    fill(255,230);rect(x-radius*.45f,y-radius*.45f,radius*.9f,radius*.9f);
    blendMode(BLEND);
  }
}


  // Procedencia: SceneBase.pde
abstract class SceneBase {
  GameManager g;
  SceneBase(GameManager gm){g=gm;}
  abstract void onEnter();
  void onExit(){}
  abstract void update();
  abstract void renderWorld();
  void renderOverlay(){}
  void interact(){}
}

abstract class ActionScene extends SceneBase {
  CollisionWorld world=new CollisionWorld();
  ArrayList<Enemy> enemies=new ArrayList<Enemy>();
  ArrayList<Projectile> projectiles=new ArrayList<Projectile>();
  ArrayList<Effect> effects=new ArrayList<Effect>();
  ArrayList<PVector> spawnPoints=new ArrayList<PVector>();
  WaveManager wave;
  boolean combatLocked=false;
  boolean controlsLocked=false;
  String objective="";
  int objectiveRemaining=-1;
  int swarmDisrupted=0;

  ActionScene(GameManager gm){super(gm);wave=new WaveManager(this);}

  boolean allowCombat(){return true;}

  void updateCommon() {
    if(swarmDisrupted>0)swarmDisrupted--;
    if(!g.dialogue.active&&!controlsLocked)g.player.update(this);
    if(g.dialogue.active)return;

    wave.update();

    for(int i=enemies.size()-1;i>=0;i--){
      if(i>=enemies.size())continue;
      Enemy e=enemies.get(i);
      e.update();
      if(e.dead && frameCount%2==0 && i<enemies.size() && enemies.get(i)==e) enemies.remove(i);
    }

    for(int i=projectiles.size()-1;i>=0;i--){
      if(i>=projectiles.size())continue;
      Projectile p=projectiles.get(i);p.update(this);
      if(p.dead && i<projectiles.size() && projectiles.get(i)==p)projectiles.remove(i);
    }

    for(int i=effects.size()-1;i>=0;i--){
      Effect e=effects.get(i);e.update();if(e.dead)effects.remove(i);
    }
  }

  void renderActors(ArrayList<DepthDrawable> extra) {
    ArrayList<DepthDrawable> all=new ArrayList<DepthDrawable>();
    if(extra!=null)all.addAll(extra);
    for(Enemy e:enemies)if(!e.dead)all.add(e);
    all.add(g.player);
    Collections.sort(all,new Comparator<DepthDrawable>(){
      public int compare(DepthDrawable a,DepthDrawable b){return Float.compare(a.sortY(),b.sortY());}
    });
    for(DepthDrawable d:all)d.renderDepth();
    for(Projectile p:projectiles)p.render();
    for(Effect e:effects)e.render();
  }

  void spawnEnemy(int type,float x,float y){
    Enemy e=null;
    if(type==0)e=new TrollEnemy(this,x,y);
    if(type==1)e=new SpamBotEnemy(this,x,y);
    if(type==2)e=new ClickBaitEnemy(this,x,y);
    if(type==3)e=new DeepfakeEnemy(this,x,y);
    if(type==4)e=new CommentEnemy(this,x,y);
    if(type==5)e=new BotSwarmEnemy(this,x,y);
    if(e!=null)enemies.add(e);
  }

  void onEnemyKilled(Enemy e){g.totalKills++;if(wave.active)wave.killed();}

  PVector randomSpawnPoint(){
    if(spawnPoints.size()==0)return new PVector(random(120,VW-120),random(220,VH-80));
    return spawnPoints.get(parseInt(random(spawnPoints.size()))).copy();
  }

  void spawnHit(float x,float y,int col){
    for(int i=0;i<10;i++)effects.add(new ParticleFX(x,y,col,6));
  }

  void addParticleTrail(float x,float y,int col){
    effects.add(new ParticleFX(x,y,col,2.2f));
  }

  void flashDamage(PVector center,float radius,float dmg){
    for(Enemy e:enemies)if(!e.dead&&PVector.dist(center,e.pos)<radius){
      e.takeDamage(dmg,PVector.sub(e.pos,center).normalize().mult(24),this);
      if(!e.dead && (e.state==1||e.state==2))e.recover(20);
    }
    for(int i=projectiles.size()-1;i>=0;i--){
      Projectile p=projectiles.get(i);
      if(!p.fromPlayer&&PVector.dist(center,p.pos)<radius)projectiles.remove(i);
    }
    if(this instanceof PixelLabScene)((PixelLabScene)this).flashServers(center,radius,dmg);
    if(this instanceof ViralCoreScene){
      ViralCore b=((ViralCoreScene)this).boss;
      if(b!=null&&!b.dead&&PVector.dist(center,b.pos)<radius+40)b.takeDamage(dmg*.6f,PVector.sub(b.pos,center).normalize().mult(12),(ViralCoreScene)this);
    }
  }

  void liveBurst(){
    for(int i=projectiles.size()-1;i>=0;i--)if(!projectiles.get(i).fromPlayer){
      Projectile p=projectiles.remove(i);
      effects.add(new ParticleFX(p.pos.x,p.pos.y,color(255,65,95),5));
    }
    for(Enemy e:enemies)if(!e.dead&&PVector.dist(e.pos,g.player.pos)<330)
      e.takeDamage(7,PVector.sub(e.pos,g.player.pos).normalize().mult(32),this);
    if(this instanceof PixelLabScene)((PixelLabScene)this).liveServers();
    if(this instanceof ViralCoreScene)((ViralCoreScene)this).boss.onLiveBurst();
  }

  void respawnPlayer(){
    enemies.clear();projectiles.clear();
    if(wave.active)wave.recoverAfterRespawn();
    onPlayerRespawn();
  }

  void onPlayerRespawn(){}

  void renderOverlay(){
    g.hud.render(objective,objectiveRemaining);
  }
}


  // Procedencia: SceneHelpers.pde
void drawExitMarker(float x,float y,String labelText){
  noFill();stroke(60,235,255,170);strokeWeight(3);
  ellipse(x,y,68+sin(frameCount*.08f)*8,68+sin(frameCount*.08f)*8);noStroke();
  fill(0,180);rect(x-55,y-58,110,25,8);
  fill(80,235,255);textAlign(CENTER,CENTER);textSize(11);text(labelText,x,y-46);
}


  // Procedencia: SceneManager.pde
class SceneManager {
  GameManager g;
  GrafoZonasMisionTres rutas=new GrafoZonasMisionTres();
  SceneBase current;
  String currentName="";
  String pending="";
  float fade=255;
  int transitionState=0; //0 none 1 fadeout 2 fadein

  SceneManager(GameManager gm){g=gm;}

  void start(String name) {
    currentName=name;
    current=create(name);
    current.onEnter();puntoControl(g);
    fade=255;
    transitionState=2;
  }

  void go(String name) {
    if (transitionState!=0) return;
    if (!rutas.conectadas(currentName,name))
      throw new IllegalArgumentException("Ruta de NovaMall no conectada: "+currentName+" → "+name);
    pending=name;
    transitionState=1;
  }

  void cut(String name){
    if(current!=null)current.onExit();
    currentName=name;current=create(name);current.onEnter();puntoControl(g);
    fade=0;transitionState=0;
  }

  void update() {
    if (transitionState==1) {
      fade=min(255,fade+18);
      if (fade>=255) {
        if (current!=null) current.onExit();
        currentName=pending;
        current=create(pending);
        current.onEnter();puntoControl(g);
        transitionState=2;
      }
    } else if (transitionState==2) {
      fade=max(0,fade-18);
      if (fade<=0) transitionState=0;
    }

    if (current!=null && transitionState!=1) current.update();
  }

  void renderWorld() {
    if (current!=null) current.renderWorld();
  }

  void renderOverlay() {
    if (current!=null) current.renderOverlay();
  }

  void renderTransition() {
    if (fade>0) {
      noStroke(); fill(0,fade);
      rect(0,0,VW,VH);
    }
  }

  void interact(){ if(current!=null) current.interact(); }

  ActionScene actionScene() {
    if (current instanceof ActionScene) return (ActionScene) current;
    return null;
  }

  SceneBase create(String n) {
    if (n.equals("intro")) return new IntroScene(g);
    if (n.equals("atrio")) return new AtrioScene(g);
    if (n.equals("pixel")) return new PixelLabScene(g);
    if (n.equals("corrupted")) return new CorruptedMallScene(g);
    if (n.equals("core")) return new ViralCoreScene(g);
    return new EndingScene(g);
  }
}


  // Procedencia: WaveManager.pde
class WaveManager {
  ActionScene scene;
  int total=0,maxAlive=3,spawned=0,defeated=0;
  int spawnCooldown=0;
  boolean active=false;
  int[] pool;

  WaveManager(ActionScene s){scene=s;}

  void configure(int total_,int max_,int[] pool_) {
    total=total_;maxAlive=max_;pool=pool_;
    spawned=0;defeated=0;spawnCooldown=1;active=true;
  }

  void update() {
    if(!active)return;
    spawnCooldown--;
    int alive=0;
    for(Enemy e:scene.enemies)if(!e.dead)alive++;
    if(spawned<total && alive<maxAlive && spawnCooldown<=0){
      int type=pool[parseInt(random(pool.length))];
      PVector p=scene.randomSpawnPoint();
      scene.spawnEnemy(type,p.x,p.y);
      spawned++;
      spawnCooldown=parseInt(random(28,55));
    }
  }

  void killed(){defeated++;}
  void recoverAfterRespawn(){spawned=defeated;spawnCooldown=25;}
  boolean complete(){
    int alive=0;for(Enemy e:scene.enemies)if(!e.dead)alive++;
    return active && defeated>=total && alive==0;
  }
  int remaining(){return max(0,total-defeated);}
}


  // Procedencia: IntroScene.pde
class IntroScene extends ActionScene {
  int timer=0;
  CinematicManager cinematic=new CinematicManager();
  IntroScene(GameManager gm){super(gm);}
  boolean allowCombat(){return false;}
  void onEnter(){
    g.confidence=.35f;g.disinfo=.08f;g.live=0;g.totalKills=0;
    g.firstFightDone=false;g.bossDefeated=false;
    g.civitas.anomalyTick=-1;g.civitas.setStage(0);
    g.player.reset(-60,510);
    controlsLocked=true;
    g.camera.cinematic(690,300,1.07f);
    g.audio.music("ambient");
    cinematic.begin();
  }
  void update(){
    cinematic.update();timer=cinematic.frame;
    if(timer<72)g.camera.cinematic(lerp(690,555,timer/72.0f),315,1.04f);
    else if(timer<150)g.player.pos.x=lerp(-60,170,(timer-72)/78.0f);
    else g.scenes.cut("atrio");
  }
  void renderWorld(){
    imageMode(CENTER);image(g.assets.bgAtrio,VW/2,VH/2,VW,VH);
    if(timer>72)g.player.renderDepth();
  }
  void renderOverlay(){
    float a=timer<30?map(timer,0,30,0,255):timer>120?map(timer,120,150,255,0):255;
    noStroke();fill(2,8,18,95);rect(0,0,VW,VH);
    fill(230,245,255,a);textAlign(CENTER,CENTER);textSize(32);
    text("NOVAMALL  ·  20:37",VW/2,150);
    fill(110,220,250,a);textSize(13);text("Una noche cualquiera en Civitas",VW/2,185);
  }
}


  // Procedencia: AtrioScene.pde
class AtrioScene extends ActionScene {
  TriggerZone anomalyZone,exitZone;
  InteractionPrompt prompt=new InteractionPrompt();
  PalomoNPC palomo;
  int state=0,eventTimer=0;
  AtrioScene(GameManager gm){super(gm);}
  void onEnter(){
    g.camera.reset();g.civitas.setStage(0);
    world.minX=48;world.maxX=1110;world.minY=170;world.maxY=605;
    // Huellas en coordenadas lógicas del mapa 1280 × 720 (escala 0,9).
    world.addSolid("kiosco_izquierdo",70,205,175,155);
    world.addSolid("jardin_central",418,420,310,145);
    world.addSolid("banco_derecho",830,280,105,65);
    world.addSolid("maceta_banco_izquierda",790,275,40,72);
    world.addSolid("maceta_banco_derecha",935,275,35,72);
    world.addSolid("fuente_superior",495,170,190,55);
    world.addSolid("jardinera_inferior_izquierda",48,500,34,105);
    world.addSolid("jardinera_inferior_derecha",1078,500,32,105);
    anomalyZone=world.addTrigger("pantalla",365,235,405,255);
    exitZone=world.addTrigger("pixel_lab",1000,250,110,225);
    spawnPoints.add(new PVector(385,260));spawnPoints.add(new PVector(750,305));
    spawnPoints.add(new PVector(340,515));spawnPoints.add(new PVector(875,525));
    objective="EXPLORA EL ATRIO  ·  OBSERVA LAS PANTALLAS";
    if(!world.canStand(g.player.pos.x,g.player.pos.y))g.player.place(170,510);
  }
  void update(){
    updateCommon();
    if(state==0 && anomalyZone.contains(g.player.pos.x,g.player.pos.y)){
      state=1;eventTimer=0;controlsLocked=true;
      g.civitas.startAnomaly();g.camera.cinematic(680,310,1.07f);
      g.audio.music("combat");
      objective="LA PANTALLA ESTÁ FALLANDO";
    }
    if(state==1){
      eventTimer++;
      if(eventTimer==65){
        palomo=new PalomoNPC(g,VW+70,245);
        palomo.flyIn(VW+70,245,835,405,76);
      }
      if(palomo!=null)palomo.update();
      if(eventTimer==150){
        state=2;palomo.mode=1;
        g.dialogue.start(
          new DialogueLine("INFLUENCER","¿Una paloma acaba de hablar?",g.player.pos.x,g.player.pos.y,color(55,220,255),g.assets.playerIdle[0]),
          new DialogueLine("MR. PALOMO","Eso no es relevante.",palomo.pos.x,palomo.pos.y,color(190,120,255),g.assets.palomoTalk[0]),
          new DialogueLine("INFLUENCER","Para mí sí es bastante relevante.",g.player.pos.x,g.player.pos.y,color(55,220,255),g.assets.playerIdle[0]),
          new DialogueLine("MR. PALOMO","Civitas tiene problemas más importantes.",palomo.pos.x,palomo.pos.y,color(190,120,255),g.assets.palomoTalk[0])
        );
      }
    }else if(state==2 && g.dialogue.consumeCompleted()){
      state=3;controlsLocked=false;palomo.flyAway(VW+90,205,82);
      g.camera.setMode(CameraManager.COMBAT);
      g.civitas.setStage(2);g.audio.music("combat");
      wave.configure(7,3,new int[]{0,2,4});
      objective="DEFIÉNDETE  ·  PING J  FLASH K  DASH SHIFT";
      objectiveRemaining=7;
    }else if(state==3){
      if(palomo!=null)palomo.update();
      objectiveRemaining=wave.remaining();
      if(wave.complete()){
        state=4;g.firstFightDone=true;g.civitas.setStage(2);
        g.camera.reset();objective="ACCESO A PIXEL LAB ABIERTO";
        objectiveRemaining=-1;
        g.dialogue.start(new DialogueLine("INFLUENCER","Esas publicaciones... se volvieron reales. Tengo que encontrar su origen.",g.player.pos.x,g.player.pos.y,color(55,220,255)));
      }
    }else if(state==4 && !g.dialogue.active && exitZone.contains(g.player.pos.x,g.player.pos.y))g.scenes.go("pixel");
  }
  void renderWorld(){
    imageMode(CENTER);image(g.assets.bgAtrio,VW/2,VH/2,VW,VH);
    g.civitas.renderWorldSignals(0);
    if(state>=1){noStroke();fill(255,18,42,20+min(50,g.civitas.anomalyTick/5));rect(0,0,VW,VH);}
    ArrayList<DepthDrawable> extra=new ArrayList<DepthDrawable>();
    if(palomo!=null&&palomo.visible())extra.add(palomo);
    renderActors(extra);
    if(state==4)drawExitMarker(1060,355,"PIXEL LAB");
  }
  void renderOverlay(){
    super.renderOverlay();
    if(state==0)prompt.render(mandoActivo?"STICK IZQUIERDO  ·  EXPLORA EL ATRIO":"WASD / FLECHAS  ·  EXPLORA EL ATRIO");
  }
  void onPlayerRespawn(){g.player.respawn(170,510);}
}


  // Procedencia: PixelLabScene.pde
class ServerNode implements DepthDrawable {
  PixelLabScene scene;PVector pos;float hp=10,maxHp=10;boolean dead=false;int tick=0;
  ServerNode(PixelLabScene s,float x,float y){scene=s;pos=new PVector(x,y);}
  public float sortY(){return pos.y;}
  void update(){
    if(dead||scene.g.dialogue.active)return;
    tick++;
    if(tick%225==0){scene.spawnEnemy(random(1)<.5f?1:5,pos.x+random(-40,40),pos.y+random(35,70));scene.g.addDisinfo(.006f);}
  }
  void damage(float d){
    if(dead)return;hp-=d;scene.spawnHit(pos.x,pos.y-35,color(55,230,255));
    if(hp<=0){dead=true;scene.g.addConfidence(.05f);scene.g.addDisinfo(-.06f);
      for(int i=0;i<22;i++)scene.effects.add(new ParticleFX(pos.x,pos.y-35,color(50,245,230),7));}
  }
  public void renderDepth(){
    // El servidor forma parte del mapa; el marcador indica su estado sin duplicar el objeto.
    noFill();stroke(dead?color(80,64,95):color(244,203,106),dead?150:230);
    strokeWeight(3);ellipse(pos.x,pos.y-42,88,88);noStroke();
    if(dead){fill(36,21,59,155);ellipse(pos.x,pos.y-42,76,76);}
    else {fill(36,21,59,220);rect(pos.x-37,pos.y+8,74,9,3);
      fill(244,203,106);rect(pos.x-35,pos.y+10,70*(hp/maxHp),5,2);}
  }
}

class PixelLabScene extends ActionScene {
  ArrayList<ServerNode> servers=new ArrayList<ServerNode>();
  InteractionPrompt prompt=new InteractionPrompt();
  TriggerZone exitZone;boolean cleared=false;
  PixelLabScene(GameManager gm){super(gm);}
  void onEnter(){
    g.player.place(150,510);g.camera.reset();g.civitas.setStage(3);g.audio.music("combat");
    world.minY=170;world.maxY=595;world.maxX=1110;
    world.addSolid("mesa_central",220,285,230,110);
    world.addSolid("mesa_derecha",760,285,180,112);
    world.addSolid("nodo_central",520,260,105,120,false);
    world.addSolid("nodo_superior",750,170,95,85,false);
    world.addSolid("nodo_inferior",750,410,95,115,false);
    world.addSolid("muebles_inferiores_izquierda",230,535,185,60);
    world.addSolid("jardinera_inferior",430,545,155,50);
    world.addSolid("muebles_inferiores_derecha",675,535,90,60);
    world.addSolid("arbol_inferior_izquierdo",50,505,60,90);
    world.addSolid("arbol_inferior_derecho",945,505,85,90);
    exitZone=world.addTrigger("pasillo",1015,230,95,300);
    servers.add(new ServerNode(this,565,365));servers.add(new ServerNode(this,795,228));servers.add(new ServerNode(this,795,508));
    spawnPoints.add(new PVector(310,260));spawnPoints.add(new PVector(950,490));spawnPoints.add(new PVector(645,505));
    wave.configure(7,4,new int[]{0,1,5});
    objective="DESACTIVA 3 NODOS  ·  E O ATAQUES";objectiveRemaining=3;
    g.dialogue.start(new DialogueLine("INFLUENCER","Las cuentas falsas salen de estos nodos. Puedo desconectarlos de cerca.",610,300,color(55,220,255)));
  }
  void update(){
    updateCommon();for(ServerNode s:servers)s.update();
    int alive=0;for(ServerNode s:servers)if(!s.dead)alive++;
    objectiveRemaining=alive;
    if(alive==0&&!cleared){
      cleared=true;objective="NODOS DESCONECTADOS  ·  SAL AL PASILLO";objectiveRemaining=-1;
      g.dialogue.start(new DialogueLine("CIVITAS","El origen se ha desplazado. Ruta del pasillo comprometida.",680,230,color(70,225,255)));
    }
    if(cleared&&!g.dialogue.active&&exitZone.contains(g.player.pos.x,g.player.pos.y))g.scenes.go("corrupted");
  }
  void interact(){
    if(g.dialogue.active)return;
    for(ServerNode s:servers)if(!s.dead&&PVector.dist(g.player.pos,s.pos)<105){s.damage(10);g.audio.play("flash");return;}
  }
  void renderWorld(){
    imageMode(CENTER);image(g.assets.bgPixel,VW/2,VH/2,VW,VH);
    g.civitas.renderWorldSignals(1);
    noStroke();fill(110,0,32,25);rect(0,0,VW,VH);
    ArrayList<DepthDrawable> extra=new ArrayList<DepthDrawable>();extra.addAll(servers);
    renderActors(extra);if(cleared)drawExitMarker(1060,350,"PASILLO");
  }
  void renderOverlay(){
    super.renderOverlay();
    if(!g.dialogue.active)for(ServerNode node:servers)
      if(!node.dead&&PVector.dist(g.player.pos,node.pos)<105){
        prompt.render("E  ·  DESACTIVAR NODO");
        break;
      }
  }
  boolean hitServer(PVector p,float r,float dmg){
    for(ServerNode s:servers)if(!s.dead&&PVector.dist(p,s.pos)<r+42){s.damage(dmg);return true;}
    return false;
  }
  void flashServers(PVector p,float r,float dmg){for(ServerNode s:servers)if(!s.dead&&PVector.dist(p,s.pos)<r+40)s.damage(dmg);}
  void liveServers(){for(ServerNode s:servers)if(!s.dead)s.damage(6);}
  void onPlayerRespawn(){g.player.respawn(150,510);}
}


  // Procedencia: CorruptedMallScene.pde
class CorruptedMallScene extends ActionScene {
  TriggerZone exitZone;PalomoNPC palomo;
  boolean started=false,cleared=false,palomoTalked=false;
  CorruptedMallScene(GameManager gm){super(gm);}
  void onEnter(){
    g.player.place(200,470);g.camera.reset();g.civitas.setStage(4);g.audio.music("combat");
    world.minX=65;world.maxX=1110;world.minY=145;world.maxY=575;
    world.addSolid("escaparates",65,145,890,35);
    world.addSolid("baranda_inferior",260,505,840,70);
    world.addSolid("jardinera_izquierda",65,440,60,135);
    world.addSolid("jardinera_derecha",1080,145,72,315);
    exitZone=world.addTrigger("nucleo",1000,155,110,175);
    spawnPoints.add(new PVector(385,300));spawnPoints.add(new PVector(600,240));
    spawnPoints.add(new PVector(750,335));spawnPoints.add(new PVector(330,475));
    objective="ATRAVIESA EL PASILLO CORRUPTO";objectiveRemaining=-1;
    g.dialogue.start(new DialogueLine("INFLUENCER","Las pantallas repiten frases que nadie recuerda haber escrito.",600,260,color(55,220,255)));
  }
  void update(){
    updateCommon();
    if(!started&&!g.dialogue.active&&g.player.pos.x>290){
      started=true;g.camera.setMode(CameraManager.COMBAT);
      wave.configure(10,4,new int[]{2,3,4,5,0});
      objective="ROMPE LA OLEADA DE RUMORES";objectiveRemaining=10;
    }
    if(started&&!cleared){
      objectiveRemaining=wave.remaining();
      if(wave.complete()){
        cleared=true;objective="EL NÚCLEO ESTÁ AL FONDO";objectiveRemaining=-1;
        palomo=new PalomoNPC(g,VW+70,115);
        palomo.flyIn(VW+70,115,800,265,76);
      }
    }
    if(cleared&&palomo!=null){
      palomo.update();
      if(!palomoTalked&&palomo.landed()&&!g.dialogue.active&&g.player.pos.x>660){
        palomoTalked=true;palomo.mode=1;
        g.dialogue.start(
          new DialogueLine("INFLUENCER","¿Tú sabes quién empezó esto?",g.player.pos.x,g.player.pos.y,color(55,220,255)),
          new DialogueLine("MR. PALOMO","Un rumor no necesita autor. Solo público.",palomo.pos.x,palomo.pos.y,color(190,120,255),g.assets.palomoTalk[0])
        );
      }
      if(palomoTalked&&g.dialogue.consumeCompleted())palomo.flyAway(VW+90,75,80);
      if(palomoTalked&&palomo.gone()&&!g.dialogue.active&&
          exitZone.contains(g.player.pos.x,g.player.pos.y))g.scenes.go("core");
    }
  }
  void renderWorld(){
    imageMode(CENTER);image(g.assets.bgCorrupted,VW/2,VH/2,VW,VH);
    g.civitas.renderWorldSignals(2);
    noStroke();fill(110,0,30,26);rect(0,0,VW,VH);
    ArrayList<DepthDrawable> extra=new ArrayList<DepthDrawable>();
    if(palomo!=null&&palomo.visible())extra.add(palomo);
    renderActors(extra);if(cleared)drawExitMarker(1050,225,"NÚCLEO");
  }
  void onPlayerRespawn(){g.player.respawn(200,470);}
}


  // Procedencia: ViralCore.pde
class ViralCore implements DepthDrawable {
  ViralCoreScene scene;
  PVector pos=new PVector(785,350);
  Hurtbox hurtbox=new Hurtbox(new PVector(pos.x,pos.y-70),59);
  PVector destination=new PVector(700,325),dashTarget=new PVector();
  PVector[] route={new PVector(785,350),new PVector(700,325),
    new PVector(820,425),new PVector(690,470),new PVector(525,455),
    new PVector(435,365),new PVector(560,320),new PVector(720,330)};
  float hp=100,maxHp=100,hitRadius=59;
  int phase=1,state=0,timer=65,attack=-1,hitFlash=0,invuln=0;
  int routeIndex=0,facingDirection=0,movementTick=0,movementHold=0,awakening=70;
  int attackCount=0;
  PVector lockedTarget=new PVector();
  boolean shield=false,dead=false,shieldBroken=false;
  ViralCore(ViralCoreScene s){scene=s;}

  void update(){
    if(dead)return;
    if(hitFlash>0)hitFlash--;
    if(invuln>0)invuln--;
    refreshPhase();
    if(movementHold>0)movementHold--;
    if(awakening>0){awakening--;return;}
    timer--;
    if(state==0){
      if(PVector.dist(pos,destination)<10)chooseDestination(false);
      moveToward(destination,phase==1?.85f:phase==2?1.25f:1.65f);
      if(timer<=0){
        attack=(attack+1)%(phase==1?2:3);
        attackCount++;
        lockedTarget.set(scene.g.player.pos);
        state=1;timer=phase==3?31:43;
        scene.g.audio.play("alert");
      }
    }else if(state==1&&timer<=0){
      executeAttack();state=2;timer=phase==3?28:40;
    }else if(state==2&&timer<=0){
      if(phase>=2&&attackCount%3==0)scene.spawnBossMinions(phase==3?2:1);
      if(phase==3&&attackCount%2==0){
        chooseDestination(true);state=3;timer=24;scene.g.audio.play("alert");
      }else{state=0;timer=phase==3?31:46;}
    }else if(state==3&&timer<=0){
      state=4;timer=28;
    }else if(state==4){
      moveToward(dashTarget,11.5f);
      if(timer<=0||PVector.dist(pos,dashTarget)<8){state=5;timer=20;}
    }else if(state==5&&timer<=0){
      state=0;timer=20;
    }
  }

  void chooseDestination(boolean dash){
    int count=phase==1?4:route.length;
    PVector chosen=pos;
    for(int attempt=0;attempt<count;attempt++){
      routeIndex=(routeIndex+1)%count;
      PVector candidate=route[routeIndex];
      float playerDistance=PVector.dist(candidate,scene.g.player.pos);
      if(PVector.dist(candidate,pos)<100||playerDistance<145)continue;
      if(phase==3&&shield&&playerDistance>390)continue;
      chosen=candidate;break;
    }
    if(dash)dashTarget.set(chosen);
    else destination.set(chosen);
  }

  void moveToward(PVector target,float speed){
    PVector step=PVector.sub(target,pos);
    if(step.magSq()<.25f)return;
    step.limit(speed);
    float nextX=pos.x+step.x,nextY=pos.y+step.y;
    // El cuerpo flota sobre la pista circular; la sombra y la hitbox quedan dentro.
    float nx=(nextX-576)/335f,ny=(nextY-365)/180f;
    if(nx*nx+ny*ny>1||PVector.dist(new PVector(nextX,nextY),scene.g.player.pos)<112){
      if(state==0)chooseDestination(false);
      else if(state==4){state=5;timer=20;}
      return;
    }
    pos.set(nextX,nextY);
    hurtbox.center.set(pos.x,pos.y-70);
    facingDirection=abs(step.x)>abs(step.y)?(step.x<0?2:3):(step.y<0?1:0);
    movementTick++;
    movementHold=8;
  }

  void refreshPhase(){
    if(hp<=68&&phase<2)phase=2;
    if(hp<=30&&phase<3){
      phase=3;shield=true;
      scene.g.disinfo=1;scene.g.civitas.setStage(5);
      scene.g.audio.play("alert");
    }
  }

  void executeAttack(){
    PVector to=PVector.sub(lockedTarget,pos).normalize();
    if(attack==0){
      for(int i=-2;i<=2;i++)scene.projectiles.add(new Projectile(pos.x,pos.y-65,to.copy().rotate(i*.19f),5.5f,.065f,9,color(255,65,90),false));
    }else if(attack==1){
      for(int i=-1;i<=1;i++)scene.projectiles.add(new Projectile(pos.x,pos.y-65,to.copy().rotate(i*.085f),7,.09f,9,color(255,100,45),false));
    }else if(attack==2){
      for(int i=0;i<12;i++)scene.projectiles.add(new Projectile(pos.x,pos.y-65,PVector.fromAngle(TWO_PI*i/12.0f),phase==3?6.0f:4.8f,.06f,8,color(235,65,140),false));
    }
  }

  void takeDamage(float dmg,PVector knock,ViralCoreScene s){
    if(dead||invuln>0)return;
    if(shield)dmg*=.04f;
    else if(state!=2)dmg*=.72f;
    hp=shield?max(1,hp-dmg):hp-dmg;
    invuln=3;hitFlash=6;
    if(hp>0)refreshPhase();
    // El retroceso no saca al jefe del círculo ni desincroniza su zona de impacto.
    moveToward(PVector.add(pos,knock.copy().mult(.24f)),knock.mag()*.24f);
    s.spawnHit(pos.x,pos.y-70,color(255,115,110));
    s.g.hitFeedback(dmg>2?5:2.5f);
    if(hp<=0)die();
  }

  void onLiveBurst(){
    if(dead||PVector.dist(scene.g.player.pos,pos)>400)return;
    if(phase==3&&shield){
      shield=false;shieldBroken=true;hp=max(1,hp-8);
      scene.g.addDisinfo(-.36f);
      for(int i=0;i<28;i++)scene.effects.add(new ParticleFX(pos.x,pos.y-75,color(255,70,120),9));
      scene.g.camera.impact(12);
    }else hp-=13;
    hitFlash=10;
    if(hp<=0)die();
  }

  void die(){
    if(dead)return;
    dead=true;
    for(int i=0;i<80;i++)scene.effects.add(new ParticleFX(pos.x,pos.y-70,color(255,70,100),11));
    scene.g.camera.impact(15);scene.g.audio.play("boss");
  }
  public float sortY(){return pos.y;}
  public void renderDepth(){
    float bob=sin(frameCount*.05f)*5;
    noStroke();fill(0,95);ellipse(pos.x,pos.y+4,115,21);
    if(state==3){
      pixelLine(pos.x,pos.y-75,dashTarget.x,dashTarget.y-75,color(255,95,170,200));
      pixelRing(dashTarget.x,dashTarget.y-55,48,16,color(255,95,170,200));
      pixelTarget(dashTarget.x,dashTarget.y,color(255,170,220,200));
      fill(255,170,220);textAlign(CENTER,CENTER);textSize(14);
      text("SALTO DE SEÑAL",pos.x,pos.y-192);
    }
    if(state==1){
      int cue=attack==0?color(255,75,100):attack==1?color(255,170,60):color(220,75,220);
      pixelRing(pos.x,pos.y-78,87,94,color(red(cue),green(cue),blue(cue),210));
      fill(cue);textAlign(CENTER,CENTER);textSize(12);
      text(attack==0?"PROPAGACIÓN":attack==1?"RÁFAGA":"ONDA VIRAL",pos.x,pos.y-190);
      if(attack<2){
        pixelLine(pos.x,pos.y-65,lockedTarget.x,lockedTarget.y-32,
          color(red(cue),green(cue),blue(cue),160));
        pixelTarget(lockedTarget.x,lockedTarget.y,color(red(cue),green(cue),blue(cue),200));
      }
    }
    if(shield){
      pixelRing(pos.x,pos.y-82,102,112,color(205,75,255,210));
    }
    int frame=movementHold>0?(movementTick/(state==4?3:7))%4:0;
    PImage sprite=scene.g.assets.viralCoreMove[facingDirection][frame];
    pushMatrix();translate(pos.x,pos.y-80+bob);
    if(hitFlash>0)tint(255,185,185);
    imageMode(CENTER);image(sprite,0,0,190,205);
    noTint();popMatrix();
  }
}


  // Procedencia: ViralCoreScene.pde
class ViralCoreScene extends ActionScene {
  ViralCore boss;boolean finished=false,finalDialogue=false;int timer=0;
  ViralCoreScene(GameManager gm){super(gm);}
  void onEnter(){
    g.player.place(295,500);g.camera.setMode(CameraManager.BOSS);
    g.civitas.setStage(4);g.audio.music("boss");
    world.minX=105;world.maxX=1040;world.minY=205;world.maxY=570;
    world.setEllipse(576,355,375,255);
    boss=new ViralCore(this);objective="NÚCLEO VIRAL";objectiveRemaining=-1;
    g.dialogue.start(
      new DialogueLine("CIVITAS","La señal dejó los servidores. Se mueve entre las pantallas.",575,255,color(255,80,100)),
      new DialogueLine("NÚCLEO VIRAL","ME COMPARTIERON HASTA DARME CUERPO.",boss.pos.x,boss.pos.y,color(255,80,150)),
      new DialogueLine("INFLUENCER","Entonces voy a cortar la cadena.",g.player.pos.x,g.player.pos.y,color(55,220,255))
    );
  }
  void update(){
    updateCommon();
    if(!g.dialogue.active&&!finished)boss.update();
    if(boss.dead&&!finished){
      finished=true;timer=0;g.bossDefeated=true;
      g.addDisinfo(-.7f);g.addConfidence(.2f);g.civitas.setStage(2);
    }
    if(finished){
      timer++;
      if(timer==70){finalDialogue=true;g.dialogue.start(
        new DialogueLine("CIVITAS","La señal se fragmentó. Estoy aislando cada copia.",575,250,color(70,220,255)),
        new DialogueLine("INFLUENCER","Devuelve las pantallas. Que todos vean qué ocurrió.",g.player.pos.x,g.player.pos.y,color(55,220,255))
      );}
      if(finalDialogue&&g.dialogue.consumeCompleted())g.scenes.go("ending");
    }
  }
  void renderWorld(){
    imageMode(CENTER);image(g.assets.bgCore,VW/2,VH/2,VW,VH);
    g.civitas.renderWorldSignals(3);
    noStroke();fill(120,0,25,boss.phase==3?45:20);rect(0,0,VW,VH);
    ArrayList<DepthDrawable> extra=new ArrayList<DepthDrawable>();
    if(!boss.dead)extra.add(boss);
    renderActors(extra);
    if(finished&&timer<70){
      // Los fragmentos vuelven a las terminales antes de restaurar Civitas.
      float progress=constrain(timer/70.0f,0,1);
      float[][] terminals={{170,225},{970,215},{175,505},{965,510}};
      for(float[] terminal:terminals){
        float x=lerp(boss.pos.x,terminal[0],progress);
        float y=lerp(boss.pos.y-70,terminal[1],progress);
        noStroke();fill(95,240,230,210*(1-progress));rect(x-5,y-5,10,10);
        fill(244,203,106,160*(1-progress));rect(x+8,y-9,4,4);
      }
    }
  }
  void renderOverlay(){
    super.renderOverlay();
    if(!boss.dead){
      float x=250,y=158,w=652;
      panelPixel(x,y,w,26,color(36,21,59,235),color(255,70,90));
      fill(255,70,90);noStroke();rect(x+4,y+4,(w-8)*max(0,boss.hp/boss.maxHp),18);
      fill(255);textAlign(CENTER,CENTER);textSize(13);
      text("NÚCLEO VIRAL  ·  FASE "+boss.phase,VW/2,y+13);
      if(boss.phase==3&&boss.shield){fill(215,110,255);textSize(12);
        text("ESCUDO VIRAL  ·  CARGA LIVE Y PULSA L",VW/2,y+47);}
    }
  }
  void spawnBossMinions(int n){
    int alive=0;for(Enemy e:enemies)if(!e.dead)alive++;
    PVector[] points={new PVector(430,320),new PVector(685,460),
      new PVector(480,470),new PVector(795,345),new PVector(615,280)};
    for(int i=0;i<n&&alive+i<6;i++){
      PVector point=points[(boss.attackCount+i)%points.length];
      if(!world.canOccupy(point.x,point.y,12,6))continue;
      spawnEnemy(random(1)<.5f?0:random(1)<.5f?1:5,point.x,point.y);
    }
  }
  void onPlayerRespawn(){g.player.respawn(295,500);boss.hp=min(boss.maxHp,boss.hp+8);}
}


  // Procedencia: EndingScene.pde
class EndingScene extends SceneBase {
  PalomoNPC palomo;int timer=0;boolean dialogueStarted=false,dialogueDone=false;
  CinematicManager cinematic=new CinematicManager();
  EndingScene(GameManager gm){super(gm);}
  void onEnter(){
    g.player.place(490,500);g.camera.cinematic(575,330,1.02f);
    g.audio.music("ambient");palomo=new PalomoNPC(g,VW+70,300);
    cinematic.begin();
  }
  void update(){
    cinematic.update();timer=cinematic.frame;
    if(timer==145){
      g.civitas.setStage(0);g.player.liveFrames=1;
      palomo.flyIn(VW+70,300,660,460,78);
    }
    if(timer==205)g.player.liveFrames=0;
    if(timer>=145)palomo.update();
    if(timer==230){
      dialogueStarted=true;palomo.mode=1;
      g.dialogue.start(
        new DialogueLine("INFLUENCER","¿Sabías que esto iba a pasar?",g.player.pos.x,g.player.pos.y,color(55,220,255),g.assets.playerIdle[0]),
        new DialogueLine("MR. PALOMO","Sabía que algo iba a pasar.",palomo.pos.x,palomo.pos.y,color(190,120,255),g.assets.palomoTalk[0]),
        new DialogueLine("INFLUENCER","Eso no responde nada.",g.player.pos.x,g.player.pos.y,color(55,220,255),g.assets.playerIdle[0]),
        new DialogueLine("MR. PALOMO","Correcto.",palomo.pos.x,palomo.pos.y,color(190,120,255),g.assets.palomoTalk[0])
      );
    }
    if(dialogueStarted&&g.dialogue.consumeCompleted()){
      dialogueDone=true;palomo.flyAway(VW+90,245,85);
      cinematic.jumpTo(300);timer=300;
    }
  }
  void renderWorld(){
    imageMode(CENTER);
    if(timer<100){
      image(g.assets.bgCore,VW/2,VH/2,VW,VH);
      noStroke();fill(190,10,30,120);rect(0,0,VW,VH);
      if(timer>80&&frameCount%8<4){fill(0,220);rect(0,0,VW,VH);}
    }
    else{image(g.assets.bgAtrio,VW/2,VH/2,VW,VH);noStroke();
      if(timer<145){fill(0,0,0,220-(timer-100)*3);rect(0,0,VW,VH);}
      else{fill(20,105,255,30);rect(0,0,VW,VH);}}
    if(timer>=100){g.player.renderDepth();if(timer>=145&&palomo.visible())palomo.renderDepth();}
  }
  void renderOverlay(){
    if(timer>=100&&timer<230){fill(95,225,255);textAlign(CENTER,CENTER);textSize(24);text("CIVITAS RESTAURADO",VW/2,160);}
    if(dialogueDone&&palomo.gone()&&timer>370){
      float a=min(230,(timer-370)*5);
      noStroke();fill(2,8,18,a);rect(0,0,VW,VH);
      fill(255,215,95);textAlign(CENTER,CENTER);textSize(30);text("MISIÓN COMPLETADA",VW/2,235);
      fill(155,225,255);textSize(15);
      text("Confianza "+parseInt(g.confidence*100)+"%   ·   Amenazas "+g.totalKills+"   ·   Desinformación "+parseInt(g.disinfo*100)+"%",VW/2,287);
      textSize(12);text("R  ·  VOLVER A JUGAR",VW/2,343);
    }
  }
}


  // Procedencia: Validation.pde
void checkGame(boolean condition,String message){
  if(!condition)throw new RuntimeException("SELF TEST: "+message);
}

void closeAllDialogue(){
  int guard=0;
  while(game.dialogue.active&&guard++<30){
    frameCount+=6;
    game.dialogue.advance();
  }
  checkGame(!game.dialogue.active,"el diálogo no se cierra");
}

void tickGame(){frameCount++;game.update();}

void waitForScene(String name,int limit){
  for(int i=0;i<limit&&!game.scenes.currentName.equals(name);i++)tickGame();
  checkGame(game.scenes.currentName.equals(name),"no se alcanzó "+name);
}

void killActiveEnemies(ActionScene s){
  ArrayList<Enemy> copy=new ArrayList<Enemy>(s.enemies);
  for(Enemy e:copy)if(!e.dead)e.takeDamage(100,new PVector(0,0),s);
}

boolean routeExists(CollisionWorld w,float sx,float sy,float tx,float ty){
  int step=16,cols=VW/step+1,rows=VH/step+1;
  boolean[][] seen=new boolean[cols][rows];
  ArrayDeque<int[]> queue=new ArrayDeque<int[]>();
  int ix=constrain(parseInt(sx/step),0,cols-1),iy=constrain(parseInt(sy/step),0,rows-1);
  queue.add(new int[]{ix,iy});seen[ix][iy]=true;
  int[][] dirs={{1,0},{-1,0},{0,1},{0,-1}};
  while(!queue.isEmpty()){
    int[] cell=queue.removeFirst();
    float x=cell[0]*step+step/2,y=cell[1]*step+step/2;
    if(dist(x,y,tx,ty)<28)return true;
    for(int[] d:dirs){
      int nx=cell[0]+d[0],ny=cell[1]+d[1];
      if(nx<0||ny<0||nx>=cols||ny>=rows||seen[nx][ny])continue;
      float px=nx*step+step/2,py=ny*step+step/2;
      if(w.canStand(px,py)){seen[nx][ny]=true;queue.add(new int[]{nx,ny});}
    }
  }
  return false;
}

void runSelfTest(){
  try{
    waitForScene("atrio",180);
    AtrioScene atrio=(AtrioScene)game.scenes.current;
    checkGame(atrio.world.canStand(game.player.pos.x,game.player.pos.y),"inicio dentro de colisión");
    checkGame(!atrio.world.canStand(160,300)&&!atrio.world.canStand(570,480)&&
      !atrio.world.canStand(875,310),"faltan objetos sólidos en el atrio");
    checkGame(atrio.world.canStand(370,450),"el atrio bloquea una baldosa libre");
    for(PVector p:atrio.spawnPoints)checkGame(atrio.world.canStand(p.x,p.y),"enemigo aparece dentro de objeto del atrio");
    checkGame(routeExists(atrio.world,170,510,400,410),"no hay ruta a la pantalla del atrio");
    checkGame(routeExists(atrio.world,400,410,1060,355),"no hay ruta a Pixel Lab");
    float x0=game.player.pos.x;
    game.input.right=true;
    for(int i=0;i<20;i++)tickGame();
    game.input.right=false;
    checkGame(game.player.pos.x>x0+15,"movimiento horizontal");
    game.player.place(50,315);
    atrio.world.move(game.player,100,0);
    checkGame(game.player.pos.x<=54.1f,"el jugador atravesó el kiosco");
    game.player.place(400,410);game.player.invuln=100000;
    tickGame();
    checkGame(atrio.state==1,"no comenzó la anomalía");
    for(int i=0;i<70;i++)tickGame();
    checkGame(atrio.palomo!=null&&atrio.palomo.mode==4,"Palomo no llega volando al atrio");
    for(int i=0;i<82;i++)tickGame();
    checkGame(game.dialogue.active,"Mr. Palomo no abrió diálogo");
    closeAllDialogue();tickGame();
    checkGame(atrio.palomo.mode==2,"Palomo no despega después del diálogo del atrio");
    checkGame(atrio.state==3,"no empezó el primer combate");
    for(int i=0;i<1200&&atrio.state!=4;i++){tickGame();killActiveEnemies(atrio);}
    checkGame(atrio.state==4,"la oleada del atrio no termina");
    closeAllDialogue();
    game.player.place(1060,355);waitForScene("pixel",45);

    PixelLabScene pixel=(PixelLabScene)game.scenes.current;
    checkGame(pixel.world.canStand(game.player.pos.x,game.player.pos.y),"Pixel Lab inicia dentro de colisión");
    checkGame(!pixel.world.canStand(560,350)&&!pixel.world.canStand(795,220)&&
      !pixel.world.canStand(795,490),"faltan bases sólidas de nodos");
    checkGame(pixel.world.canStand(700,350),"Pixel Lab bloquea un pasillo libre");
    for(PVector p:pixel.spawnPoints)checkGame(pixel.world.canStand(p.x,p.y),"enemigo aparece dentro de objeto de Pixel Lab");
    checkGame(routeExists(pixel.world,150,510,1040,350),"no hay ruta por Pixel Lab");
    closeAllDialogue();
    game.player.place(490,350);
    pixel.interact();
    checkGame(pixel.servers.getFirst().dead,"la base sólida impide desactivar el nodo central");
    game.player.place(700,235);pixel.interact();
    checkGame(pixel.servers.get(1).dead,"la base sólida impide desactivar el nodo superior");
    game.player.place(700,500);pixel.interact();
    checkGame(pixel.servers.get(2).dead,"la base sólida impide desactivar el nodo inferior");
    game.player.place(150,510);game.player.invuln=0;
    float before=game.player.concentration;
    game.player.hurt(.1f);
    checkGame(game.player.concentration<before,"el jugador no recibe daño");
    game.player.invuln=100000;
    for(int type=0;type<6;type++){
      pixel.spawnEnemy(type,800+type*15,460);
      Enemy sample=pixel.enemies.get(pixel.enemies.size()-1);
      sample.takeDamage(100,new PVector(0,0),pixel);
      checkGame(sample.dead,"enemigo "+type+" no puede morir");
    }
    game.player.place(150,510);game.player.facing.set(1,0);
    pixel.spawnEnemy(1,240,468);
    Enemy target=pixel.enemies.get(pixel.enemies.size()-1);
    game.player.pingCd=0;game.player.tryPing();
    for(int i=0;i<15&&!target.dead;i++){
      ArrayList<Projectile> shots=new ArrayList<Projectile>(pixel.projectiles);
      for(Projectile shot:shots)if(shot.fromPlayer&&!shot.dead)shot.update(pixel);
    }
    checkGame(target.hp<target.maxHp,"PING no daña al objetivo");
    game.hitStop=0;
    game.player.place(150,510);
    pixel.spawnEnemy(0,220,510);
    Enemy flashTarget=pixel.enemies.get(pixel.enemies.size()-1);
    game.player.flashCd=0;game.player.tryFlash();
    checkGame(flashTarget.hp==flashTarget.maxHp,"FLASH golpea antes de la preparación");
    for(int i=0;i<13;i++)game.player.update(pixel);
    checkGame(flashTarget.hp==flashTarget.maxHp,"FLASH no respeta su preparación");
    game.player.update(pixel);
    for(int i=0;i<7;i++){
      ArrayList<Effect> active=new ArrayList<Effect>(pixel.effects);
      for(Effect effect:active)if(effect instanceof FlashPulseFX&&!effect.dead)effect.update();
    }
    checkGame(flashTarget.hp<flashTarget.maxHp,"la onda de FLASH no alcanza al enemigo");
    game.player.place(150,510);game.player.dashCd=0;game.input.right=true;
    game.player.tryDash();
    game.player.update(pixel);
    checkGame(game.player.dashElapsed==1,"el dash no comienza en el primer frame");
    game.player.invuln=0;
    float beforeDashHit=game.player.concentration;
    game.player.hurt(.01f);
    checkGame(game.player.concentration<beforeDashHit,"el inicio del dash debe ser vulnerable");
    game.player.invuln=0;
    for(int i=0;i<4;i++)game.player.update(pixel);
    float middleDashLife=game.player.concentration;
    game.player.hurt(.01f);
    checkGame(game.player.concentration==middleDashLife,"el centro del dash no concede invulnerabilidad");
    for(int i=0;i<4;i++)game.player.update(pixel);
    checkGame(abs(game.player.pos.x-262.5f)<1,"la distancia del dash no es fija");
    game.input.right=false;
    game.player.place(150,510);
    game.player.privateMode=true;game.player.invuln=0;game.live=.5f;
    float protectedLife=game.player.concentration;
    Projectile weak=new Projectile(game.player.pos.x,game.player.pos.y-32,new PVector(1,0),0,.05f,8,color(255),false);
    weak.update(pixel);
    checkGame(weak.dead&&game.player.concentration==protectedLife,"MODO PRIVADO no bloquea proyectil débil");
    game.player.privateMode=false;
    for(ServerNode node:pixel.servers)node.damage(100);
    for(int i=0;i<5;i++)tickGame();
    checkGame(pixel.cleared,"servidores no completan objetivo");
    closeAllDialogue();
    game.player.place(1040,350);waitForScene("corrupted",45);

    CorruptedMallScene corridor=(CorruptedMallScene)game.scenes.current;
    checkGame(corridor.world.canStand(game.player.pos.x,game.player.pos.y),"pasillo inicia dentro de colisión");
    checkGame(!corridor.world.canStand(400,530)&&!corridor.world.canStand(100,500),
      "faltan baranda o jardinera del pasillo");
    for(PVector p:corridor.spawnPoints)checkGame(corridor.world.canStand(p.x,p.y),"enemigo aparece dentro de objeto del pasillo");
    checkGame(routeExists(corridor.world,200,470,1050,225),"no hay ruta al núcleo");
    closeAllDialogue();game.player.invuln=100000;
    game.player.place(320,470);tickGame();
    checkGame(corridor.started,"oleada del pasillo no comienza");
    for(int i=0;i<50;i++)tickGame();
    killActiveEnemies(corridor);
    game.player.invuln=0;game.player.concentration=.05f;
    game.player.hurt(.1f);
    checkGame(game.player.animState==Player.DEAD,"no se muestra el estado de derrota");
    for(int i=0;i<80&&game.player.deathFrames>0;i++)tickGame();
    checkGame(game.player.deathFrames==0,"el jugador no reaparece");
    checkGame(corridor.wave.spawned==corridor.wave.defeated,"la oleada no se recupera tras morir");
    game.player.invuln=100000;
    for(int i=0;i<1800&&!corridor.cleared;i++){tickGame();killActiveEnemies(corridor);}
    checkGame(corridor.cleared,"oleada del pasillo no termina");
    game.player.place(700,260);
    for(int i=0;i<95&&!game.dialogue.active;i++)tickGame();
    checkGame(game.dialogue.active,"falta segundo encuentro con Palomo");
    closeAllDialogue();tickGame();
    checkGame(corridor.palomo.mode==2,"Palomo no despega del pasillo");
    game.player.place(1050,225);waitForScene("core",130);

    ViralCoreScene core=(ViralCoreScene)game.scenes.current;
    checkGame(core.world.canStand(game.player.pos.x,game.player.pos.y),"núcleo inicia dentro de colisión");
    checkGame(!core.world.canStand(1020,500)&&core.world.canStand(620,420),
      "el límite de la arena no coincide con la pista circular");
    closeAllDialogue();
    PVector bossStart=core.boss.pos.copy();
    for(int i=0;i<190;i++)tickGame();
    checkGame(PVector.dist(bossStart,core.boss.pos)>25,"el Núcleo Viral no se mueve en fase 1");
    checkGame(PVector.dist(core.boss.hurtbox.center,new PVector(core.boss.pos.x,core.boss.pos.y-70))<1,
      "la zona de impacto del jefe no sigue su movimiento");
    core.boss.state=0;core.boss.invuln=0;
    core.boss.takeDamage(100,new PVector(0,0),core);
    core.boss.update();
    checkGame(core.boss.phase==3&&core.boss.shield,"fase 3 sin escudo");
    core.boss.invuln=0;
    core.boss.takeDamage(100,new PVector(0,0),core);
    checkGame(!core.boss.dead&&core.boss.hp>0&&core.boss.shield,"el boss muere sin LIVE BURST");
    for(int i=0;i<700&&core.boss.state!=3;i++)tickGame();
    checkGame(core.boss.state==3,"la fase 3 no anuncia el salto de señal");
    PVector beforeDash=core.boss.pos.copy();
    for(int i=0;i<56;i++)tickGame();
    checkGame(PVector.dist(beforeDash,core.boss.pos)>25,"el salto de señal no desplaza al jefe");
    float arenaX=(core.boss.pos.x-576)/335f,arenaY=(core.boss.pos.y-365)/180f;
    checkGame(arenaX*arenaX+arenaY*arenaY<=1,"el Núcleo Viral salió de la arena");
    checkGame(PVector.dist(core.boss.hurtbox.center,new PVector(core.boss.pos.x,core.boss.pos.y-70))<1,
      "la zona de impacto se desincronizó tras el salto");
    game.player.place(620,430);game.live=1;
    game.player.tryLiveBurst();
    checkGame(core.boss.shieldBroken&&!core.boss.shield,"LIVE BURST no rompe el escudo");
    checkGame(game.live==0,"LIVE no se consumió");
    core.boss.invuln=0;
    core.boss.takeDamage(100,new PVector(0,0),core);
    checkGame(core.boss.dead,"el boss no puede morir");
    for(int i=0;i<180&&!game.dialogue.active;i++)tickGame();
    checkGame(game.dialogue.active,"falta diálogo posterior al boss");
    closeAllDialogue();waitForScene("ending",45);
    for(int i=0;i<150;i++)tickGame();
    checkGame(((EndingScene)game.scenes.current).palomo.mode==4,"Palomo no llega volando al epílogo");
    for(int i=0;i<110&&!game.dialogue.active;i++)tickGame();
    checkGame(game.dialogue.active,"falta diálogo final");
    closeAllDialogue();
    for(int i=0;i<90;i++)tickGame();
    checkGame(((EndingScene)game.scenes.current).dialogueDone,"el final no se completa");
    checkGame(((EndingScene)game.scenes.current).palomo.gone(),"Palomo no sale volando del epílogo");
    println("SELF TEST OK: inicio, movimiento, colisión, anomalía, diálogos, 6 enemigos, PING, FLASH, dash, privacidad, reaparición, nodos, LIVE, boss y final.");
  }catch(Exception e){
    println("SELF TEST FALLÓ: "+e.getMessage());
    e.printStackTrace();
    throw new IllegalStateException("Autoprueba de misión 3 falló", e);
  }
}

}
