package alcaldedigital.compartido.guardado;
import java.nio.file.*;import java.nio.channels.*;import java.io.*;import java.util.*;
/** Una victoria desbloquea la siguiente misión; el acceso libre es opcional. */
public final class ProgresoMisiones {
 private static final Object CERROJO=new Object();
 private final Path archivo;private final boolean[] ganadas=new boolean[8];private boolean desbloqueoTotal;private String error="";private long modificado=-1;
 public ProgresoMisiones(Path dir){archivo=dir.resolve("progreso-misiones.properties").toAbsolutePath();recargar();}
 private void recargar(){try{
  long fecha=Files.exists(archivo)?Files.getLastModifiedTime(archivo).toMillis():0;if(fecha==modificado)return;
  Properties p;try{p=ArchivosPartida.leer(archivo);if(p!=null&&!"1".equals(p.getProperty("version")))throw new IOException("Versión del progreso inválida");}
  catch(IOException e){p=ArchivosPartida.leer(archivo.resolveSibling(archivo.getFileName()+".bak"));if(p==null||!"1".equals(p.getProperty("version")))throw e;}
  if(p!=null){desbloqueoTotal|=Boolean.parseBoolean(p.getProperty("desbloqueo.total"));
   for(int i=1;i<=7;i++)ganadas[i]|=Boolean.parseBoolean(p.getProperty("victoria."+i));}modificado=fecha;
 }catch(IOException e){error=e.getMessage();}}
 public synchronized boolean ganada(int n){recargar();return n>=1&&n<=7&&ganadas[n];}
 public synchronized boolean todasDesbloqueadas(){recargar();return desbloqueoTotal;}
 public synchronized int requisitoPendiente(int n){recargar();if(n<1||n>7)return -1;
  if(desbloqueoTotal)return 0;
  for(int i=1;i<n;i++)if(!ganadas[i])return i;return 0;}
 public synchronized boolean disponible(int n){return requisitoPendiente(n)==0;}
 public void desbloquearTodas()throws IOException{
  synchronized(CERROJO){synchronized(this){
   Files.createDirectories(archivo.getParent());
   try(var c=FileChannel.open(archivo.resolveSibling("progreso-misiones.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);var bloqueo=c.lock()){
    modificado=-1;recargar();if(desbloqueoTotal)return;
    Properties p=new Properties();p.setProperty("version","1");p.setProperty("desbloqueo.total","true");
    for(int i=1;i<=7;i++)p.setProperty("victoria."+i,Boolean.toString(ganadas[i]));
    ArchivosPartida.guardar(archivo,p);desbloqueoTotal=true;modificado=-1;error="";
   }
  }}
 }
 public void victoria(int n)throws IOException{
  if(n<1||n>7||ganada(n))return;
  synchronized(CERROJO){synchronized(this){
   if(n<1||n>7)return;Files.createDirectories(archivo.getParent());
   try(var c=FileChannel.open(archivo.resolveSibling("progreso-misiones.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);var bloqueo=c.lock()){
    modificado=-1;recargar();if(!disponible(n)||ganadas[n])return;
    Properties p=new Properties();p.setProperty("version","1");if(desbloqueoTotal)p.setProperty("desbloqueo.total","true");for(int i=1;i<=7;i++)p.setProperty("victoria."+i,Boolean.toString(ganadas[i]||i==n));
    ArchivosPartida.guardar(archivo,p);ganadas[n]=true;modificado=-1;error="";
   }
  }}
 }
 public String error(){return error;}
 /** Descarta el progreso en memoria tras archivar el perfil completo. */
 public synchronized void reiniciar(){Arrays.fill(ganadas,false);desbloqueoTotal=false;modificado=-1;error="";recargar();}
}
