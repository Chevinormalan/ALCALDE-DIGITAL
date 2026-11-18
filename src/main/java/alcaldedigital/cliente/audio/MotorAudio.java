package alcaldedigital.cliente.audio;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import alcaldedigital.cliente.audio.OpcionesAudio.Bus;
import alcaldedigital.cliente.audio.MezclaMusical.*;

/** Una sola salida estéreo. Carga asíncrona, PCM acotado, mezcla y canales independientes. */
public final class MotorAudio implements AutoCloseable {
 private static final class Holder {static final MotorAudio VALOR=new MotorAudio();}
 public static MotorAudio global(){return Holder.VALOR;}
 private final ExecutorService carga=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"audio-carga");t.setDaemon(true);return t;});
 private final ConcurrentLinkedQueue<Runnable> ordenes=new ConcurrentLinkedQueue<>();
 private final Map<String,Pcm> cache=new ConcurrentHashMap<>();private final Set<String> cargando=ConcurrentHashMap.newKeySet();
 private final Map<String,Voz> bucles=new HashMap<>();private final List<Voz> efectos=new ArrayList<>();
 private final Map<String,Long> ultimos=new ConcurrentHashMap<>();private final Set<String> suspendidos=ConcurrentHashMap.newKeySet();
 private final MezclaMusical musica=new MezclaMusical();private final Properties catalogo=new Properties();
 private final AtomicLong revision=new AtomicLong(),turnoEfectos=new AtomicLong();private volatile String pedido="";private volatile boolean cerrado,pausa,activo,sinSalida;
 private volatile float intensidad=.3f,atenuacion=1;private volatile OpcionesAudio opciones;
 private final ByteArrayOutputStream captura=System.getProperty("ad.audio.captura")==null?null:new ByteArrayOutputStream();
 private SourceDataLine linea;private Thread hilo;private final Path registro;
 private static final AudioFormat FORMATO=new AudioFormat(22050,16,2,true,false);
 private static final class Voz {final String grupo,recurso;final Pcm pcm;final Bus bus;final boolean repetir;long posicion;volatile float volumen;float suavizado;Voz(String grupo,String recurso,Pcm pcm,Bus bus,boolean repetir,float volumen){this.grupo=grupo;this.recurso=recurso;this.pcm=pcm;this.bus=bus;this.repetir=repetir;this.volumen=volumen;}}
 private MotorAudio(){String r=System.getProperty("ad.audio.registro");registro=r==null?null:Path.of(r);try(var in=getClass().getResourceAsStream("/cliente/audio/catalogo.properties")){if(in!=null)catalogo.load(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8));}catch(IOException ex){traza("CATALOGO_ERROR "+ex.getMessage());}}
 public synchronized void iniciar(OpcionesAudio opciones){this.opciones=opciones;if(activo||cerrado)return;activo=true;if(Boolean.getBoolean("ad.audio.mudo")){sinSalida=true;traza("MUDO revision sin dispositivo");return;}hilo=new Thread(this::ejecutar,"audio-mezcla");hilo.setDaemon(true);hilo.start();}
 public boolean activo(){return activo;}
 public void pausa(boolean valor){if(pausa==valor)return;traza(valor?"PAUSA_GLOBAL":"REANUDAR_GLOBAL");pausa=valor;if(valor){turnoEfectos.incrementAndGet();ordenes.add(()->efectos.removeIf(v->v.bus!=Bus.INTERFAZ));}}
 public void musica(String id,float intensidad,boolean dialogo){this.intensidad=intensidad;atenuacion=dialogo?.44f:1;if(cerrado||id.equals(pedido))return;pedido=id;long rev=revision.incrementAndGet();traza("MUSICA "+id);if(!activo||sinSalida||Boolean.getBoolean("ad.audio.mudo"))return;
  carga.execute(()->{if(rev!=revision.get()||cerrado)return;try{Pcm base=leer("audio/musica/"+id),ritmo=null,epica=null;String capas=catalogo.getProperty(id+".capas","");if(capas.contains("ritmo"))ritmo=leer("audio/musica/"+id+"-ritmo");if(capas.contains("epica"))epica=leer("audio/musica/"+id+"-epica");Pista pista=new Pista(id,base,ritmo,epica);ordenes.add(()->{if(rev==revision.get()){musica.cambiar(pista);traza("MUSICA_OK "+id+" frames="+base.frames());}});}catch(Exception ex){traza("PISTA_ERROR "+id+" "+ex.getMessage());}});
 }
 public void efecto(String grupo,String id,String recurso,float volumen,double intervalo,Bus bus){if(cerrado||suspendidos.contains(grupo)||pausa&&bus!=Bus.INTERFAZ)return;long ahora=System.nanoTime();String clave=grupo+":"+id;Long antes=ultimos.putIfAbsent(clave,ahora);if(antes!=null){if(ahora-antes<intervalo*1e9)return;ultimos.put(clave,ahora);}traza("EVENTO "+grupo+" "+id);
  if(!activo||sinSalida||Boolean.getBoolean("ad.audio.mudo")||ordenes.size()>64)return;
  long turno=turnoEfectos.get();Pcm pcm=cache.get(recurso);if(pcm==null){precargar(recurso);return;}ordenes.add(()->{if(suspendidos.contains(grupo)||bus!=Bus.INTERFAZ&&(pausa||turnoEfectos.get()!=turno))return;efectos.removeIf(v->v.grupo.equals(grupo)&&v.recurso.equals(recurso));if(efectos.size()<16)efectos.add(new Voz(grupo,recurso,pcm,bus,false,Math.clamp(volumen,0,1)));});
 }
 public void fx(String id){efecto("guion",id,"audio/fx/"+id,.75f,.12,Bus.EFECTOS);}
 public void interfaz(String id){efecto("interfaz",id,"audio/fx/"+id,.7f,.055,Bus.INTERFAZ);}
 public boolean voces(){return opciones!=null&&opciones.actual().voces();}
 public void precargar(String recurso){if(!activo||sinSalida||cache.containsKey(recurso)||!cargando.add(recurso)||cerrado)return;carga.execute(()->{try{if(cache.size()<100)cache.put(recurso,leer(recurso));}catch(Exception ex){traza("EFECTO_ERROR "+recurso+" "+ex.getMessage());}finally{cargando.remove(recurso);}});}
 public void bucle(String grupo,String id,String recurso,float v){if(cerrado)return;suspendidos.remove(grupo);if(!activo||sinSalida||Boolean.getBoolean("ad.audio.mudo"))return;String clave=grupo+":"+id;Pcm pcm=cache.get(recurso);if(pcm==null){precargar(recurso);return;}ordenes.add(()->{Voz voz=bucles.get(clave);if(voz==null||!voz.recurso.equals(recurso)){voz=new Voz(grupo,recurso,pcm,Bus.AMBIENTE,true,v);bucles.put(clave,voz);traza("BUCLE "+clave+" "+recurso);}voz.volumen=Math.clamp(v,0,1);});}
 public void suspender(String grupo){suspendidos.add(grupo);ordenes.add(()->efectos.removeIf(v->v.grupo.equals(grupo)));}
 public void reanudar(String grupo){suspendidos.remove(grupo);}
 public void cerrar(String grupo){suspendidos.add(grupo);ordenes.add(()->{efectos.removeIf(v->v.grupo.equals(grupo));bucles.entrySet().removeIf(e->e.getValue().grupo.equals(grupo));});ultimos.keySet().removeIf(k->k.startsWith(grupo+":"));}
 private Pcm leer(String recurso)throws Exception{try(var in=getClass().getResourceAsStream("/cliente/"+recurso+".wav")){if(in==null)throw new IOException("Falta "+recurso);try(var original=AudioSystem.getAudioInputStream(new BufferedInputStream(in));var convertido=AudioSystem.getAudioInputStream(FORMATO,original)){byte[] bytes=convertido.readAllBytes();short[] samples=new short[bytes.length/2];ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);return new Pcm(samples);}}}
 private void ejecutar(){try{linea=AudioSystem.getSourceDataLine(FORMATO);linea.open(FORMATO,1024*4);linea.start();traza("DISPOSITIVO_OK "+linea.getLineInfo());for(String r:catalogo.getProperty("precarga","").split(","))if(!r.isBlank())precargar(r);
  float[] mezcla=new float[1024];byte[] bytes=new byte[2048];
  while(!cerrado){Runnable r;while((r=ordenes.poll())!=null)r.run();Arrays.fill(mezcla,0);var c=opciones.actual();musica.objetivo(c.suave()?Math.min(.35f,intensidad):intensidad,c.ganancia(Bus.MUSICA)*atenuacion*(.8f+.2f*intensidad));musica.mezclar(mezcla,pausa);
   for(Voz v:bucles.values())sumar(v,mezcla,c);for(var it=efectos.iterator();it.hasNext();)if(!sumar(it.next(),mezcla,c))it.remove();
   for(int i=0;i<mezcla.length;i++){float v=mezcla[i];float limitado=Math.abs(v)<=.85f?v:Math.copySign((float)(.85+.15*Math.tanh((Math.abs(v)-.85)/.15)),v);short n=(short)(Math.clamp(limitado,-.999f,.999f)*32767);bytes[i*2]=(byte)n;bytes[i*2+1]=(byte)(n>>8);}if(captura!=null&&captura.size()<22050*4*30)captura.write(bytes);linea.write(bytes,0,bytes.length);
  }
 }catch(Exception ex){sinSalida=true;ordenes.clear();cache.clear();if(!cerrado)traza("SIN_DISPOSITIVO "+ex.getClass().getSimpleName()+" "+ex.getMessage());}finally{if(linea!=null){linea.stop();linea.flush();linea.close();}if(captura!=null){try{Path destino=Path.of(System.getProperty("ad.audio.captura"));Files.createDirectories(destino.toAbsolutePath().getParent());byte[] datos=captura.toByteArray();try(var in=new AudioInputStream(new ByteArrayInputStream(datos),FORMATO,datos.length/4)){AudioSystem.write(in,AudioFileFormat.Type.WAVE,destino.toFile());}}catch(Exception ex){traza("CAPTURA_ERROR "+ex.getMessage());}}}}
 private boolean sumar(Voz v,float[] mezcla,OpcionesAudio.Configuracion c){if((pausa&&v.bus!=Bus.INTERFAZ)||suspendidos.contains(v.grupo))return true;float meta=v.volumen*c.ganancia(v.bus);for(int i=0;i<mezcla.length;i+=2){if(v.posicion>=v.pcm.frames()){if(v.repetir)v.posicion=0;else return false;}v.suavizado+=(meta-v.suavizado)*.01f;mezcla[i]+=v.pcm.muestra(v.posicion,0)*v.suavizado;mezcla[i+1]+=v.pcm.muestra(v.posicion,1)*v.suavizado;v.posicion++;}return true;}
 private synchronized void traza(String texto){if(registro==null)return;try{Files.createDirectories(registro.toAbsolutePath().getParent());Files.writeString(registro,"audio "+texto+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(IOException ignored){}}
 public String titulo(){return catalogo.getProperty(pedido+".titulo",pedido);}
 @Override public void close(){if(cerrado)return;cerrado=true;revision.incrementAndGet();carga.shutdownNow();if(linea!=null)linea.close();if(hilo!=null)try{hilo.join(1000);}catch(InterruptedException ex){Thread.currentThread().interrupt();}cache.clear();ordenes.clear();ultimos.clear();}
}
