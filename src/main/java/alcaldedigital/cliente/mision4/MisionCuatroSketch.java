package alcaldedigital.cliente.mision4;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import java.util.Objects;
import java.util.Properties;

/** Rendered inside Alcalde Digital's existing Processing window. */
public final class MisionCuatroSketch {
    public record Resultado(boolean completada,int publicacionesValidadas,int respuestasResponsables,int enemigos,String partida,java.util.List<Integer> respuestas){
        public Resultado(boolean c,int v,int r,int e){this(c,v,r,e,java.util.UUID.randomUUID().toString(),java.util.List.of());}
        public Resultado(boolean c,int v,int r,int e,String partida){this(c,v,r,e,partida,java.util.List.of());}
        public Resultado {respuestas=java.util.List.copyOf(respuestas);}
    }
    private PGraphics canvas;
    private MisionCuatroGame game;
    private boolean pruebaSinGuardar=Boolean.getBoolean("ad.mision4.sinGuardar");
    private long previous;
    private float debt;
    private final SoundEffects sound=new SoundEffects();
    private final Autoguardado saves=new Autoguardado(Autoguardado.defaultPath());
    private Properties saved;
    private boolean gestionExterna;
    public void gestionExterna(boolean v){gestionExterna=v;}
    public void guardarAhora()throws Exception{saveIfNeeded(true);saves.esperar();}
    private boolean chooseSave;
    private int focoGuardado;
    private String lastCheckpoint="";
    private long lastSaveNanos;
    public void iniciarEn(PApplet parent){
        if(parent!=null){canvas=parent.createGraphics(1280,720,PApplet.JAVA2D);canvas.noSmooth();}
        game=new MisionCuatroGame();saved=pruebaSinGuardar?null:saves.load();chooseSave=saved!=null;
        if(chooseSave&&MisionCuatroGame.restore(saved)==null){saved=null;chooseSave=false;}
        previous=0;debt=0;
        if(!chooseSave)saveIfNeeded(true);
        if(!chooseSave)sound.observar(game);
    }
    public PImage dibujarEnJuego(){
        if(canvas==null)throw new IllegalStateException("Misión 4 sin iniciar");
        canvas.beginDraw();canvas.pushMatrix();canvas.scale(1.25f);
        game.draw(canvas);
        if(chooseSave){
            canvas.noStroke();canvas.fill(15,10,25,225);canvas.rect(0,0,1024,576);
            canvas.fill(48,32,65);canvas.stroke(244,203,106);canvas.strokeWeight(3);canvas.rect(205,165,614,245,12);
            canvas.noStroke();canvas.fill(244,203,106);canvas.textAlign(PApplet.CENTER);canvas.textSize(27);canvas.text("PARTIDA GUARDADA",512,222);
            canvas.fill(focoGuardado==0?0xfff4cb6a:0xfff5e8fb);canvas.textSize(17);canvas.text("ENTER · continuar desde el último punto",512,287);
            canvas.fill(focoGuardado==1?0xfff4cb6a:0xfff5e8fb);canvas.text("N · comenzar una partida nueva",512,329);canvas.textAlign(PApplet.LEFT);
        }
        if(saves.failed){canvas.fill(150,25,45);canvas.rect(12,70,365,30,4);canvas.fill(255);canvas.textSize(13);canvas.text("No se pudo guardar la partida",22,90);}
        else if(!chooseSave&&saves.lastSuccess>0&&System.nanoTime()-saves.lastSuccess<1_500_000_000L){
            canvas.noStroke();canvas.fill(33,21,47,220);canvas.rect(846,65,165,27,4);canvas.fill(244,203,106);canvas.textSize(12);canvas.text("✓ AUTOGUARDADO",856,83);
        }
        canvas.popMatrix();canvas.endDraw();
        if(!chooseSave)saveIfNeeded(false);
        return canvas;
    }
    public void elegirGuardado(boolean nueva){focoGuardado=0;pulsarTecla(nueva?'n':'\n',nueva?78:10);}
    public void raton(float x,float y){
        String fase=game.scenes.current.name();
        if(fase.equals("ANSWER")&&y>=261&&y<=319){if(x>=661&&x<=806)pulsarTecla('s',83);else if(x>=817&&x<=962)pulsarTecla('n',78);}
        else if(fase.equals("RUMOR")&&y>=365&&y<=413){if(x>=228&&x<=506)pulsarTecla('v',86);else if(x>=518&&x<=796)pulsarTecla('n',78);}
        else if(fase.equals("DOWNLOAD")&&x>=322&&x<=702&&y>=329&&y<=384)pulsarTecla('\n',10);
    }
    public PImage fotograma(){return canvas;}
    public void actualizar(float dt){if(game==null||chooseSave)return;debt+=Math.min(.1f,Math.max(0,dt));
        for(int i=0;debt>=1f/60&&i<6;i++,debt-=1f/60){game.update(1f/60);sound.observar(game);}}
    public Properties estado(){return game.checkpoint();}
    public static boolean guardadoValido(Properties p){return p!=null&&MisionCuatroGame.restore(p)!=null;}
    public boolean eligiendoGuardado(){return chooseSave;}
    public void prepararRevision(String fase){if(!Boolean.getBoolean("ad.mision46.revision"))return;
        chooseSave=false;game.set(MisionCuatroGame.Phase.valueOf(fase));
        if(fase.equals("BATTLE")){game.hero.x=500;game.hero.y=350;game.combat.spawn(0,650,350);}}

    /** Escenas de verificación optativas; no se ejecutan al jugar normalmente. */
    public void pruebaAudiovisual(int cuadro){
        if(!Boolean.getBoolean("ad.mision4.audiovisual")||game==null)return;
        if(cuadro==10||cuadro==180)pulsarTecla('d',java.awt.event.KeyEvent.VK_D);
        if(cuadro==120||cuadro==290)soltarTecla(java.awt.event.KeyEvent.VK_D);
        if(cuadro==310){game.set(MisionCuatroGame.Phase.PLAZA);game.hero.x=600;game.hero.y=420;}
        if(cuadro==370){game.set(MisionCuatroGame.Phase.BATTLE);game.hero.x=500;game.hero.y=350;game.hero.direction=3;game.combat.spawn(0,545,350);game.hero.invulnerable=2;}
        if(cuadro==380||cuadro==405||cuadro==470)pulsarTecla('j',java.awt.event.KeyEvent.VK_J);
        if(cuadro==381||cuadro==406||cuadro==471)soltarTecla(java.awt.event.KeyEvent.VK_J);
        if(cuadro==430)pulsarTecla('k',java.awt.event.KeyEvent.VK_K);if(cuadro==450)soltarTecla(java.awt.event.KeyEvent.VK_K);
        if(cuadro==100||cuadro==155||cuadro==320||cuadro==383){
            var dir=java.nio.file.Path.of("verificacion/audiovisual/grafica").toAbsolutePath();try{java.nio.file.Files.createDirectories(dir);}catch(java.io.IOException ex){throw new IllegalStateException(ex);}
            canvas.save(dir.resolve("m4-"+cuadro+".png").toString());
        }
    }
    public void pulsarTecla(char ch,int code){
        if(game==null)return;
        if(chooseSave){
            if(code==38||code==40||code==9){focoGuardado=1-focoGuardado;return;}
            if((code==10||code==69)&&focoGuardado==1)code=78;
            if(code==java.awt.event.KeyEvent.VK_ENTER||code==java.awt.event.KeyEvent.VK_E){MisionCuatroGame restored=MisionCuatroGame.restore(saved);if(restored!=null)game=restored;chooseSave=false;previous=0;saveIfNeeded(true);}
            else if(code==java.awt.event.KeyEvent.VK_N){game=new MisionCuatroGame();chooseSave=false;previous=0;saveIfNeeded(true);}
            sound.reset();if(!chooseSave)sound.observar(game);return;
        }
        var phase=game.scenes.current;
        var line=game.dialogue.line();int visible=game.dialogue.visible();
        game.press(code,ch);
        saveIfNeeded(false);
        sound.observar(game);
        if(phase!=MisionCuatroGame.Phase.BATTLE&&(line!=game.dialogue.line()||visible!=game.dialogue.visible()||phase!=game.scenes.current))sound.play("dialogue");
    }
    public void soltarTecla(int code){if(game!=null)game.release(code);}
    public boolean enCombate(){return game!=null&&game.scenes.current==MisionCuatroGame.Phase.BATTLE;}
    public void perderFoco(){if(game!=null)game.loseFocus();sound.pause();reanudar();} public void reanudar(){previous=0;debt=0;}
    public Resultado resultado(){return game==null?new Resultado(false,0,0,0):
        new Resultado(game.ending(),game.approval,Math.max(0,game.reputation),game.combat.kills,game.partida,
            java.util.Arrays.stream(game.respuestas).boxed().toList());}
    private void saveIfNeeded(boolean force){
        if(game==null||chooseSave||pruebaSinGuardar||gestionExterna&&!force)return;
        Properties p=game.checkpoint();
        String signature=p.getProperty("phase")+":"+p.getProperty("question")+":"+p.getProperty("encounter")+":"+
            p.getProperty("approval")+":"+p.getProperty("answered")+":"+p.getProperty("kills")+":"+p.getProperty("citizen0")+":"+p.getProperty("citizen1");
        long now=System.nanoTime();
        if(force||!signature.equals(lastCheckpoint)||now-lastSaveNanos>=5_000_000_000L){
            saves.save(p);lastCheckpoint=signature;lastSaveNanos=now;
        }
    }
    public void cerrar(){if(!chooseSave)saveIfNeeded(true);saves.close();sound.close();game=null;canvas=null;}
}
