package alcaldedigital.cliente.presentacion;
import java.util.*;
public final class SalidaAudioPrueba implements AudioEscenaSalida {
 public final List<String> efectos=new ArrayList<>();public final Map<String,Float> bucles=new HashMap<>();public int pausas;
 public void bucle(String id,String recurso,float v){bucles.put(id,v);}
 public void efecto(String id,String recurso,float v,double espera){efectos.add(id);}
 public void pausar(){pausas++;bucles.clear();}public void close(){bucles.clear();}
 public long cuenta(String s){return efectos.stream().filter(s::equals).count();}
}
