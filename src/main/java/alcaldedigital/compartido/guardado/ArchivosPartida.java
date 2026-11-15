package alcaldedigital.compartido.guardado;
import java.nio.file.*;
import java.io.*;
import java.util.Properties;
/** Escritura atómica compartida. El último guardado válido queda como copia de recuperación. */
public final class ArchivosPartida {
 private ArchivosPartida(){}
 public static Path directorio(){return Path.of(System.getProperty("ad.guardados",Path.of(System.getProperty("user.home"),".local","state","alcalde-digital").toString()));}
 public static synchronized void guardar(Path ruta,Properties p)throws IOException{
  Path f=ruta.toAbsolutePath(),d=f.getParent();Files.createDirectories(d);Path t=Files.createTempFile(d,"partida-",".tmp");
  try{try(var o=Files.newOutputStream(t)){p.store(o,"Alcalde Digital · partida");}
   if(Files.isRegularFile(f)){
    boolean valida=false;try{var anterior=leer(f);valida=anterior!=null&&anterior.containsKey("version");}catch(IOException corrupta){}
    if(valida)Files.copy(f,f.resolveSibling(f.getFileName()+".bak"),StandardCopyOption.REPLACE_EXISTING);
   }
   try{Files.move(t,f,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(t,f,StandardCopyOption.REPLACE_EXISTING);}
  }finally{Files.deleteIfExists(t);}
 }
 public static Properties leer(Path f)throws IOException{if(!Files.isRegularFile(f))return null;try(var i=Files.newInputStream(f)){Properties p=new Properties();p.load(i);return p;}catch(IllegalArgumentException e){throw new IOException("Guardado inválido",e);}}
}
