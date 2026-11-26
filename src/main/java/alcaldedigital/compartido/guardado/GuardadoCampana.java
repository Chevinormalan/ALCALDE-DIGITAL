package alcaldedigital.compartido.guardado;
import java.nio.file.*;import java.io.*;import java.util.*;
/** Punto de entrada de los guardados de misión; cada motor mantiene su codec y autoridad. */
public final class GuardadoCampana {
 private GuardadoCampana(){}
 public static Path archivo(int n){if(n<1||n>8)throw new IllegalArgumentException("Misión inválida");String externa=System.getProperty("ad.mision"+n+".guardar");return externa!=null&&!externa.isBlank()?Path.of(externa):ArchivosPartida.directorio().resolve("mision"+n+".properties");}
 public static boolean existe(int n){if(n==6){
  Path lan=ArchivosPartida.directorio().resolve("mision6-lan.properties");
  if(Files.isRegularFile(lan)||Files.isRegularFile(lan.resolveSibling(lan.getFileName()+".bak")))return true;
 }if(n==5||n==7){Path d=ArchivosPartida.directorio();if(!Files.isDirectory(d))return false;try(var f=Files.list(d)){return f.anyMatch(p->p.getFileName().toString().startsWith("mision"+n+"-")&&p.toString().endsWith(".bin"));}catch(IOException e){return false;}}return Files.isRegularFile(archivo(n))||(n!=4&&Files.isRegularFile(respaldo(n)));}
 private static Path respaldo(int n){Path f=archivo(n);return f.resolveSibling(f.getFileName()+".bak");}
 public static void guardar(int n,Properties p)throws IOException{ArchivosPartida.guardar(archivo(n),p);}
 public static Properties cargar(int n)throws IOException{
  IOException fallo=null;for(Path f:new Path[]{archivo(n),respaldo(n)})try{
   Properties p=ArchivosPartida.leer(f);if(p==null)continue;
   if(n==1)alcaldedigital.servidor.sesion.SesionMisionUnoLocal.restaurar(p);
   if(n==2)alcaldedigital.cliente.mision2.MisionDosSketch.restaurar(p);
   if(n==3&&!alcaldedigital.cliente.mision3.MisionTresSketch.guardadoValido(p))throw new IllegalArgumentException("Guardado de misión 3 inválido");
   if(n==4&&!alcaldedigital.cliente.mision4.MisionCuatroSketch.guardadoValido(p))throw new IllegalArgumentException("Guardado de misión 4 inválido");
   if(n==6&&alcaldedigital.cliente.mision6.MisionSeisMotor.restaurar(p)==null)throw new IllegalArgumentException("Guardado de misión 6 inválido");
   if(n==8)alcaldedigital.cliente.mision8.MotorOcho.restaurar(p);
   return p;
  }catch(IOException|RuntimeException e){fallo=new IOException("No se pudo recuperar la misión "+n,e);}
  if(fallo!=null)throw fallo;return null;
 }
}
