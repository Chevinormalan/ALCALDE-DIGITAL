package alcaldedigital.cliente.mision8;

import alcaldedigital.compartido.carrera.EstadoElectoral;
import alcaldedigital.compartido.guardado.*;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.servidor.carrera.RepositorioCarrera;
import java.io.IOException;
import java.util.*;

/** Una fotografía de las siete victorias; no suma intentos ni reconstruye respuestas antiguas. */
public record AntecedentesOcho(EstadoCiudad ciudad, EstadoElectoral electoral,
                               Map<String,Integer> senales,List<String> decisiones) {
 public AntecedentesOcho {senales=Map.copyOf(senales);decisiones=List.copyOf(decisiones);}
 public static AntecedentesOcho cargar()throws IOException {
  var progreso=new ProgresoMisiones(ArchivosPartida.directorio());
  for(int m=1;m<=7;m++)if(!progreso.ganada(m))throw new IOException("Debes ganar la misión "+m+" antes del cierre electoral.");
  EstadoCiudad ciudad=TablaPuntajes.ciudadParaFinales().orElseThrow(()->new IOException("Faltan antecedentes de las siete victorias. Repite las misiones sin registro para conservar tus decisiones."));
  Map<String,Integer> s=new LinkedHashMap<>();List<String> d=new ArrayList<>();
  String[][]requeridas={{"publicaciones_responsables","burlas"},{"equipos_seguros","fallo_critico"},{"confianza_final","desinformacion_final"},{"respuestas_favorables","respuestas_desfavorables","respuestas_neutrales"},{"publicaciones_verificadas","rumores"},{"respuestas_escucha","respuestas_conviccion"},{"juan_corrupto","noticias_contrastadas"}};
  for(int m=1;m<=7;m++){var fila=TablaPuntajes.campana(m);for(String clave:requeridas[m-1])if(!fila.senales().containsKey(clave))throw new IOException("Faltan decisiones registradas de misión "+m+". Repite ese capítulo para completar los antecedentes.");final int numero=m;fila.senales().forEach((k,v)->s.put("m"+numero+"."+k,v));for(String decision:fila.decisiones())d.add("M"+m+": "+decision);}
  var e=new RepositorioCarrera(ArchivosPartida.directorio().resolve("campana.properties")).electoral(ciudad);
  return new AntecedentesOcho(ciudad,e.conservarCorrupcion(s.getOrDefault("m7.juan_corrupto",0)>0),s,d);
 }
 public int senal(int m,String nombre){return senales.getOrDefault("m"+m+"."+nombre,0);}
 public String estadoCiudad(){
  if(ciudad.confianza()<35||ciudad.desinformacion()>=60||ciudad.conflictos()>=60||senal(4,"respuestas_desfavorables")>=2&&senal(1,"publicaciones_responsables")<=1)return "CIUDAD EN CRISIS";
  if(ciudad.informacionVerificada()>=60&&ciudad.confianza()>=60&&ciudad.desinformacion()<=35&&ciudad.conflictos()<=35&&senal(1,"publicaciones_responsables")>=2&&senal(4,"respuestas_favorables")>=2&&senal(4,"respuestas_desfavorables")<=1&&senal(5,"publicaciones_verificadas")>=3&&!electoral.juanCorrupto())return "CONFIANZA CÍVICA";
  return "CIUDAD EN RECONSTRUCCIÓN";
 }
 public Properties capturar(){
  Properties p=new Properties();var c=ciudad;int[] valores={c.informacionVerificada(),c.confianza(),c.convivencia(),c.bienestar(),c.participacion(),c.desinformacion(),c.conflictos()};for(int i=0;i<7;i++)p.setProperty("ciudad."+i,""+valores[i]);
  int[][] e={electoral.apoyo(),electoral.confianza(),electoral.verificadas(),electoral.conflictos(),electoral.participacion()};for(int i=0;i<5;i++)for(int j=0;j<4;j++)p.setProperty("eleccion."+i+"."+j,""+e[i][j]);
  p.setProperty("historialElectoral",String.join("\n",electoral.historial()));p.setProperty("corrupcion",""+electoral.juanCorrupto());senales.forEach((k,v)->p.setProperty("senal."+k,""+v));p.setProperty("decisiones",String.join("\n",decisiones));return p;
 }
 public static AntecedentesOcho restaurar(Properties p){
  int[] c=new int[7];for(int i=0;i<7;i++)c[i]=entero(p,"ciudad."+i,0,100);int[][] e=new int[5][4];for(int i=0;i<5;i++)for(int j=0;j<4;j++)e[i][j]=entero(p,"eleccion."+i+"."+j,0,100000);
  Map<String,Integer>s=new HashMap<>();for(String k:p.stringPropertyNames())if(k.startsWith("senal."))s.put(k.substring(6),entero(p,k,-100000,100000));
  return new AntecedentesOcho(new EstadoCiudad(c[0],c[1],c[2],c[3],c[4],c[5],c[6]),new EstadoElectoral(Boolean.parseBoolean(p.getProperty("corrupcion")),e[0],e[1],e[2],e[3],e[4],p.getProperty("historialElectoral","").isEmpty()?List.of():List.of(p.getProperty("historialElectoral").split("\n"))),s,p.getProperty("decisiones","").isEmpty()?List.of():List.of(p.getProperty("decisiones").split("\n")));
 }
 static int entero(Properties p,String k,int min,int max){int v=Integer.parseInt(Objects.requireNonNull(p.getProperty(k),k));if(v<min||v>max)throw new IllegalArgumentException("Valor inválido: "+k);return v;}
}
