package alcaldedigital.cliente.audio;
import java.util.*;

/** Observa snapshots; no altera reglas, votos, tiempos, guardados ni órdenes LAN. */
public final class DirectorAudio {
 public record Estado(Partitura.Contexto contexto,String partida,long tick,boolean pausa,int linea,Map<String,Integer> marcas){public Estado{marcas=Map.copyOf(marcas);}}
 private final MotorAudio motor;private Estado anterior;private String ambiente="";
 public DirectorAudio(MotorAudio motor){this.motor=motor;}
 public void observar(Estado actual){
  motor.pausa(actual.pausa);var cue=Partitura.elegir(actual.contexto);motor.musica(cue.pista(),cue.intensidad(),cue.dialogo());
  if(!ambiente.equals(cue.ambiente())){motor.cerrar("ambiente-guion");ambiente=cue.ambiente();}
  if(!ambiente.isEmpty())motor.bucle("ambiente-guion","fondo","audio/fx/"+ambiente,.4f);
  boolean misma=mismaPartida(anterior,actual);
  if(misma&&actual.pausa!=anterior.pausa)motor.interfaz(actual.pausa?"pausa":"reanudar");
  for(String id:eventos(anterior,actual,motor.voces())){
   if(id.equals("dialogo"))motor.efecto("guion","dialogo","audio/fx/dialogo",.18f,.18,OpcionesAudio.Bus.EFECTOS);
   else if(actual.contexto.mision()==5&&(id.equals("rayo_aviso")||id.equals("turno")))
    motor.efecto("guion",id,SonidosRemaster.ruta(id.equals("turno")?"servidor-turno":"servidor-alarma"),.52f,.12,OpcionesAudio.Bus.EFECTOS);
   else motor.fx(id);
  }
  anterior=actual;
 }
 private static boolean mismaPartida(Estado a,Estado b){return a!=null&&b.partida.equals(a.partida)&&b.contexto.mision()==a.contexto.mision();}
 /** Solo cambios nuevos: una restauración o un salto de snapshots no repite acontecimientos anteriores. */
 public static List<String> eventos(Estado antes,Estado ahora,boolean voces){
  if(!mismaPartida(antes,ahora)||ahora.pausa||antes.pausa||ahora.tick<antes.tick||ahora.tick-antes.tick>90)return List.of();
  var salida=new ArrayList<String>();
  if(!ahora.contexto.fase().equals(antes.contexto.fase())){String e=transicion(ahora.contexto);if(!e.isEmpty())salida.add(e);}
  if(ahora.linea!=antes.linea&&voces)salida.add("dialogo");
  for(var e:ahora.marcas.entrySet())if(e.getValue()>antes.marcas.getOrDefault(e.getKey(),0))salida.add(e.getKey());
  return List.copyOf(salida);
 }
 private static String transicion(Partitura.Contexto c){return switch(c.fase()){
  case "CRISIS","ALARMA","FALLO_CRITICO"->"alarma";
  case "ABSORCION"->"absorcion";case "AVISO"->"rayo_aviso";case "TURNO"->"turno";case "TRAICION"->"alarma";
  case "CUENTA"->"cuenta";case "CARRERA"->"salida";case "VOTO"->"urna";case "ESCRUTINIO"->"escrutinio";
  case "DERROTA"->"derrota";case "RESULTADO"->c.mision()==7?"confirmar":c.victoria()?"victoria":"derrota";
  case "FINAL"->c.mision()==8||c.mision()==5?(c.victoria()?"victoria":"derrota"):"confirmar";
  default->"";};}
}
