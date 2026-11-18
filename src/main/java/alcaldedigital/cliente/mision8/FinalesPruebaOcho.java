package alcaldedigital.cliente.mision8;

import alcaldedigital.compartido.carrera.EstadoElectoral;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import java.util.*;

/** Escenarios de ensayo: no leen ni escriben decisiones reales del perfil. */
public final class FinalesPruebaOcho {
 private FinalesPruebaOcho(){}
 public static String nombre(int candidato){return candidato<0?"Según la campaña":(candidato==0?"Final 1 · Juan":"Final 2 · "+EstadoElectoral.NOMBRES[candidato]);}
 public static String ciudad(int estado){return switch(estado){case 0->"Confianza cívica";case 1->"Reconstrucción";case 2->"Crisis";default->throw new IllegalArgumentException("Estado inválido");};}
 public static AntecedentesOcho antecedentes(int candidato,int estado){
  if(candidato<0||candidato>3)throw new IllegalArgumentException("Final inválido");
  ciudad(estado);
  EstadoCiudad c=estado==0?new EstadoCiudad(80,80,75,75,75,15,15):estado==1?EstadoCiudad.inicial():new EstadoCiudad(30,25,30,30,30,75,70);
  int[] apoyo={30,30,30,30};apoyo[candidato]=95;
  var e=new EstadoElectoral(false,apoyo,new int[]{80,65,65,65},new int[]{10,10,10,10},new int[]{0,0,0,0},new int[]{10,10,10,10},List.of("Escenario de prueba: "+nombre(candidato)));
  return new AntecedentesOcho(c,e,Map.of("m1.publicaciones_responsables",3,"m4.respuestas_favorables",3,"m4.respuestas_desfavorables",0,"m5.publicaciones_verificadas",4),List.of("Datos de ensayo: no son respuestas del jugador."));
 }
}
