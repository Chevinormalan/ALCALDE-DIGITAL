package alcaldedigital.cliente.mision6;

import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.*;

/** Guardado asíncrono, coalescido y atómico; conserva la copia anterior válida. */
public final class AutoguardadoSeis implements AutoCloseable {
 private final Path archivo, respaldo;
 private final ExecutorService worker=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"mision6-autoguardado");t.setDaemon(true);return t;});
 private Properties pendiente;private boolean escribiendo,cerrado;
 public volatile boolean fallo,recuperado;public volatile long ultimoExito;
 public AutoguardadoSeis(Path archivo){this.archivo=archivo.toAbsolutePath();this.respaldo=this.archivo.resolveSibling(this.archivo.getFileName()+".bak");}
 public static Path ubicacion(){
  String override=System.getProperty("ad.mision6.guardar");if(override!=null&&!override.isBlank())return Path.of(override);
  return alcaldedigital.servidor.batalla.GuardadoBatalla.directorio().resolve("mision6.properties");
 }
 private Properties leer(Path p){
  if(!Files.isRegularFile(p))return null;try(var in=Files.newInputStream(p)){Properties v=new Properties();v.load(in);return MisionSeisMotor.restaurar(v)==null?null:v;}catch(IOException|RuntimeException ex){return null;}
 }
 public Properties cargar(){Properties p=leer(archivo);if(p!=null)return p;Properties b=leer(respaldo);if(b!=null){recuperado=true;return b;}if(Files.exists(archivo)||Files.exists(respaldo))fallo=true;return null;}
 public synchronized void guardar(Properties estado){
  if(cerrado)throw new IllegalStateException("Autoguardado cerrado");Properties copia=new Properties();copia.putAll(estado);pendiente=copia;
  if(!escribiendo){escribiendo=true;worker.execute(this::vaciar);}
 }
 private void vaciar(){
  while(true){Properties copia;synchronized(this){copia=pendiente;pendiente=null;if(copia==null){escribiendo=false;return;}}
   try{escribir(copia);fallo=false;ultimoExito=System.nanoTime();}catch(IOException ex){fallo=true;System.err.println("No se pudo guardar la misión 6: "+ex.getMessage());}
  }
 }
 private void escribir(Properties p)throws IOException{
  Files.createDirectories(archivo.getParent());Path tmp=Files.createTempFile(archivo.getParent(),"mision6-", ".tmp");
  try{
   try(var out=Files.newOutputStream(tmp)){p.store(out,"Alcalde Digital - Mision 6");}
   if(leer(archivo)!=null)Files.copy(archivo,respaldo,StandardCopyOption.REPLACE_EXISTING);
   try{Files.move(tmp,archivo,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
   catch(AtomicMoveNotSupportedException ex){Files.move(tmp,archivo,StandardCopyOption.REPLACE_EXISTING);}
  }finally{Files.deleteIfExists(tmp);}
 }
 public void esperar()throws Exception{worker.submit(()->{}).get(3,TimeUnit.SECONDS);if(fallo)throw new IOException("No se pudo confirmar el guardado");}
 public boolean cerrarYEsperar(){
  synchronized(this){if(!cerrado){cerrado=true;worker.shutdown();}}
  try{boolean ok=worker.awaitTermination(5,TimeUnit.SECONDS);if(!ok)fallo=true;return ok&&!fallo;}catch(InterruptedException ex){Thread.currentThread().interrupt();fallo=true;return false;}
 }
 @Override public void close(){cerrarYEsperar();}
}