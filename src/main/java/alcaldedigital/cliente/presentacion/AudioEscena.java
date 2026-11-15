package alcaldedigital.cliente.presentacion;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Clips cacheados, fundidos continuos y efectos sin reproducción atrasada. */
public final class AudioEscena implements AudioEscenaSalida {
 private record Objetivo(String recurso,float volumen){}
 private static final class Bucle {Clip clip;float volumen;Bucle(Clip c){clip=c;}}
 private final ScheduledThreadPoolExecutor worker=new ScheduledThreadPoolExecutor(1,r->{Thread t=new Thread(r,"audio-escena");t.setDaemon(true);return t;});
 private final Map<String,Clip> clips=new HashMap<>();private final Map<String,Bucle> bucles=new HashMap<>();
 private final Map<String,Objetivo> objetivos=new ConcurrentHashMap<>();private final Map<String,Long> ultimos=new HashMap<>();
 private volatile boolean pausado,cerrado,sinDispositivo=Boolean.getBoolean("ad.audio.mudo");private volatile long epoca;
 private final String nombre;private final Path registro;
 public AudioEscena(String nombre){this.nombre=nombre;String p=System.getProperty("ad.audio.registro");registro=p==null?null:Path.of(p).toAbsolutePath();
  worker.setRemoveOnCancelPolicy(true);worker.scheduleWithFixedDelay(this::mezclar,0,20,TimeUnit.MILLISECONDS);traza("INICIO mezcladores="+AudioSystem.getMixerInfo().length+" mudo="+sinDispositivo);
 }
 private synchronized void traza(String s){if(registro==null)return;try{Files.createDirectories(registro.getParent());Files.writeString(registro,nombre+" "+s+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(IOException ignored){}}
 private Clip cargar(String recurso)throws Exception{Clip c=clips.get(recurso);if(c!=null)return c;
  try(var in=getClass().getResourceAsStream("/cliente/"+recurso+".wav")){if(in==null)throw new IOException("Falta WAV: "+recurso);
   try(var ais=AudioSystem.getAudioInputStream(new BufferedInputStream(in))){c=AudioSystem.getClip();c.open(ais);clips.put(recurso,c);traza("CLIP_OK "+recurso+" frames="+c.getFrameLength());return c;}}
 }
 private void fallo(Exception ex){if(!sinDispositivo){sinDispositivo=true;traza("SIN_AUDIO "+ex.getClass().getSimpleName()+": "+ex.getMessage());for(Clip c:clips.values())c.stop();}}
 private static void volumen(Clip c,float v){if(c.isControlSupported(FloatControl.Type.MASTER_GAIN)){var g=(FloatControl)c.getControl(FloatControl.Type.MASTER_GAIN);g.setValue(Math.max(g.getMinimum(),Math.min(g.getMaximum(),v<=.0001f?g.getMinimum():(float)(20*Math.log10(v)))));}}
 @Override public void bucle(String id,String recurso,float v){if(cerrado)return;v=Math.max(0,Math.min(.5f,v));Objetivo antes=objetivos.put(id,new Objetivo(recurso,v));if(antes==null&&v>0||antes!=null&&(antes.volumen>0)!=(v>0))traza("BUCLE "+id+" volumen="+v);pausado=false;}
 @Override public void efecto(String id,String recurso,float v,double espera){if(cerrado||pausado)return;traza("EVENTO "+id);long ahora=System.nanoTime();Long anterior=ultimos.get(id);if(anterior!=null&&ahora-anterior<espera*1e9)return;ultimos.put(id,ahora);
  if(sinDispositivo||worker.getQueue().size()>20)return;long turno=epoca;
  worker.execute(()->{if(cerrado||pausado||epoca!=turno)return;try{
   if(clips.values().stream().filter(Clip::isRunning).count()>=8)return;
   Clip c=cargar(recurso);if(cerrado||pausado||epoca!=turno)return;c.stop();c.setFramePosition(0);volumen(c,Math.max(0,Math.min(.65f,v)));c.start();traza("REPRODUCE "+id);
  }catch(Exception ex){fallo(ex);}});
 }
 private void mezclar(){if(cerrado||sinDispositivo)return;try{
  for(var e:objetivos.entrySet())if(e.getValue().volumen>0&&!bucles.containsKey(e.getKey()))bucles.put(e.getKey(),new Bucle(cargar(e.getValue().recurso)));
  for(var e:bucles.entrySet()){Bucle b=e.getValue();Objetivo o=objetivos.get(e.getKey());float meta=pausado||o==null?0:o.volumen;b.volumen+=(meta-b.volumen)*.14f;
   if(b.volumen<.002f&&meta==0){b.clip.stop();b.volumen=0;continue;}volumen(b.clip,b.volumen);if(!b.clip.isRunning()&&!pausado)b.clip.loop(Clip.LOOP_CONTINUOUSLY);}
 }catch(Exception ex){fallo(ex);}}
 @Override public void pausar(){if(cerrado)return;pausado=true;epoca++;objetivos.clear();traza("PAUSA");worker.execute(()->{for(Clip c:clips.values())c.stop();for(Bucle b:bucles.values())b.volumen=0;});}
 @Override public void close(){if(cerrado)return;cerrado=true;epoca++;try{worker.submit(()->{for(Clip c:clips.values()){c.stop();c.close();}clips.clear();bucles.clear();traza("CIERRE");}).get(1500,TimeUnit.MILLISECONDS);}catch(Exception ignored){}finally{worker.shutdownNow();}}
}
