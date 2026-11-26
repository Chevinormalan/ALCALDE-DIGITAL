package alcaldedigital.cliente.mision8;
import alcaldedigital.compartido.guardado.*;
import java.io.*;import java.nio.file.*;import java.nio.channels.FileChannel;import java.util.*;
public final class GuardadoOcho {
 private GuardadoOcho(){}
 public static TablaPuntajes.Fila fila(MotorOcho m){
  boolean gano=m.escena==MotorOcho.Escena.FINAL;
  int vidas=Arrays.stream(m.heroes).mapToInt(h->h.vida).sum();
  int victoria=gano?60:0,auditoria=m.auditoria*10,supervivencia=Math.round(10f*vidas/(5*m.heroes.length));
  var historial=m.historial.subList(Math.max(0,m.historial.size()-128),m.historial.size());
  return new TablaPuntajes.Fila(8,m.id+"-"+m.historial.stream().filter(s->s.startsWith("Equipo derrotado")).count(),gano,
   victoria+auditoria+supervivencia,new String[]{"FACIL","NORMAL","DIFICIL"}[m.dificultad],
   List.of("Alcalde legítimo: "+EstadoNombre(m.ganador),"Evidencias: "+m.auditoria+"/3","Vidas: "+vidas,"Ciudad: "+m.antecedentes.estadoCiudad()),
   historial,List.of(0,0,0,0,0,0,0),Map.of("auditorias",m.auditoria,"vida_jefe",m.vidaJefe,"vidas_restantes",vidas,
   "juan_corrupto",m.antecedentes.electoral().juanCorrupto()?1:0))
   .conDesglose(new TablaPuntajes.Componente("Victoria",victoria,60),new TablaPuntajes.Componente("Auditoría",auditoria,30),
    new TablaPuntajes.Componente("Vidas restantes",supervivencia,10));
 }
 private static String EstadoNombre(int i){return i<0?"pendiente":alcaldedigital.compartido.carrera.EstadoElectoral.NOMBRES[i];}
 public static void guardar(MotorOcho m)throws IOException{synchronized(m){if(m.prueba()){ArchivosPartida.guardar(ArchivosPartida.directorio().resolve("mision8-prueba.properties"),m.capturar());return;}if(m.escena==MotorOcho.Escena.DERROTA)TablaPuntajes.registrar(fila(m));GuardadoCampana.guardar(8,m.capturar());}}
 public static MotorOcho cargar()throws IOException{Properties p=GuardadoCampana.cargar(8);if(p==null)throw new IOException("No existe guardado de misión 8");try{return MotorOcho.restaurar(p);}catch(RuntimeException e){throw new IOException("Guardado de misión 8 inválido",e);}}
 public static void acreditar(MotorOcho m)throws IOException{synchronized(m){if(!m.credito)acreditarBloqueado(m);}}
 private static void acreditarBloqueado(MotorOcho m)throws IOException{
  if(m.escena!=MotorOcho.Escena.FINAL)return;
  if(m.prueba()){m.credito=true;guardar(m);return;}
  if(m.ganador==0&&new alcaldedigital.servidor.carrera.RepositorioCarrera(ArchivosPartida.directorio().resolve("campana.properties")).electoral(m.antecedentes.ciudad()).juanCorrupto())throw new IOException("Se confirmó un pacto en la campaña. Repite el escrutinio: Juan ya no es elegible.");
  Path dir=ArchivosPartida.directorio();Files.createDirectories(dir);
  synchronized(GuardadoOcho.class){try(var c=FileChannel.open(dir.resolve("mision8-resultados.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);var lock=c.lock()){
   Path archivo=dir.resolve("mision8-resultados.properties");Properties p=ArchivosPartida.leer(archivo);if(p==null)p=new Properties();
   String pref="partida."+m.id+".";if(!p.containsKey(pref+"ganador")){
    p.setProperty(pref+"ganador",""+m.ganador);p.setProperty(pref+"ciudad",m.antecedentes.estadoCiudad());p.setProperty(pref+"epilogo",m.epilogo());p.setProperty(pref+"historial",String.join("\n",m.historial));p.setProperty(pref+"auditorias",""+m.auditoria);p.setProperty(pref+"jugadores",""+m.jugadores);p.setProperty("ultima",m.id);p.setProperty("version","1");ArchivosPartida.guardar(archivo,p);
   }
   TablaPuntajes.registrar(fila(m));new ProgresoMisiones(dir).victoria(8);m.credito=true;guardar(m);
  }}
 }
}
