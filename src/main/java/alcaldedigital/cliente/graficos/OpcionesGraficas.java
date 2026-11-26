package alcaldedigital.cliente.graficos;

import alcaldedigital.compartido.guardado.ArchivosPartida;
import java.nio.file.Path;
import java.io.IOException;
import java.util.*;

/** Preferencias locales de presentación, independientes de los snapshots de las misiones. */
public final class OpcionesGraficas {
 public enum Filtro { NITIDO,SUAVE }
 public record Tamano(int ancho,int alto){@Override public String toString(){return ancho+" × "+alto;}}
 public static final List<Tamano> TAMANOS=List.of(new Tamano(640,360),new Tamano(960,540),new Tamano(1280,720),new Tamano(1600,900),new Tamano(1920,1080),new Tamano(2560,1440),new Tamano(3840,2160));
 public record Configuracion(boolean completa,Tamano ventana,boolean entero,Filtro filtro){
  public Configuracion{if(ventana==null||ventana.ancho<=0||ventana.alto<=0||filtro==null)throw new IllegalArgumentException("Gráficos inválidos");}
 }
 public static final Configuracion ORIGINAL=new Configuracion(false,new Tamano(1280,720),false,Filtro.NITIDO);
 private final Path archivo;
 private Configuracion actual=ORIGINAL,edicion=ORIGINAL;
 private long revision;
 private List<Tamano> compatibles=TAMANOS;
 private Tamano resolucionMonitor;
 public String mensaje="",pantalla="Detectando pantalla…",salida="1280 × 720",imagen="1280 × 720";
 public OpcionesGraficas(){this(ArchivosPartida.directorio().resolve("graficos.properties"));}
 public OpcionesGraficas(Path archivo){
  this.archivo=archivo;
  try{var p=ArchivosPartida.leer(archivo);if(p!=null){if(!"1".equals(p.getProperty("version")))throw new IllegalArgumentException();
   actual=new Configuracion(Boolean.parseBoolean(p.getProperty("completa")),new Tamano(Integer.parseInt(p.getProperty("ancho")),Integer.parseInt(p.getProperty("alto"))),Boolean.parseBoolean(p.getProperty("entero")),Filtro.valueOf(p.getProperty("filtro")));}}
  catch(IOException|RuntimeException e){mensaje="Se recuperó la configuración gráfica original.";actual=ORIGINAL;}
  edicion=actual;
 }
 public Configuracion actual(){return actual;}public Configuracion edicion(){return edicion;}public long revision(){return revision;}
 public boolean cambios(){return !edicion.equals(actual);}
 public void abrir(){edicion=actual;}
 public void detectar(int ancho,int alto,int utilAncho,int utilAlto){
  resolucionMonitor=new Tamano(ancho,alto);
  pantalla=resolucionMonitor.toString();
  var disponibles=new ArrayList<Tamano>();
  for(var tamano:TAMANOS)if(!esResolucionMonitor(tamano)&&tamano.ancho<=utilAncho&&tamano.alto<=utilAlto)disponibles.add(tamano);
  disponibles.add(resolucionMonitor);
  compatibles=List.copyOf(disponibles);
 }
 public boolean esResolucionMonitor(Tamano tamano){return tamano.equals(resolucionMonitor);}
 public List<Tamano> compatibles(){return compatibles;}
 public void modo(){edicion=new Configuracion(!edicion.completa,edicion.ventana,edicion.entero,edicion.filtro);}
 public void tamano(){int i=compatibles.indexOf(edicion.ventana);edicion=new Configuracion(edicion.completa,compatibles.get((i+1)%compatibles.size()),edicion.entero,edicion.filtro);}
 public void escalado(){edicion=new Configuracion(edicion.completa,edicion.ventana,!edicion.entero,edicion.filtro);}
 public void filtro(){edicion=new Configuracion(edicion.completa,edicion.ventana,edicion.entero,edicion.filtro==Filtro.NITIDO?Filtro.SUAVE:Filtro.NITIDO);}
 public void restaurar(){edicion=ORIGINAL;mensaje="Valores originales preparados. Pulsa Aplicar.";}
 public void aplicar(){
  boolean volverAVentana=actual.completa&&!edicion.completa&&edicion.ventana.equals(actual.ventana);
  if(volverAVentana||!esResolucionMonitor(edicion.ventana)){guardar(edicion);return;}

  // La resolución del monitor requiere toda la pantalla. Conserva el preajuste de F11.
  Tamano ventana=actual.ventana;
  if(esResolucionMonitor(ventana)&&compatibles.size()>1)ventana=compatibles.get(compatibles.size()-2);
  var completa=new Configuracion(true,ventana,edicion.entero,edicion.filtro);
  guardar(completa);
  if(actual.equals(completa))edicion=actual;
 }
 public void alternarCompleta(){guardar(new Configuracion(!actual.completa,actual.ventana,actual.entero,actual.filtro));edicion=actual;}
 public void recuperarVentana(){
  var segura=new Configuracion(false,ORIGINAL.ventana,actual.entero,actual.filtro);
  guardar(segura);
  if(!actual.equals(segura)){actual=segura;revision++;}
  edicion=actual;
  mensaje="Se recuperó el modo ventana. Puedes ajustar los gráficos.";
 }
 private void guardar(Configuracion config){
  var p=new Properties();p.setProperty("version","1");p.setProperty("completa",""+config.completa);p.setProperty("ancho",""+config.ventana.ancho);p.setProperty("alto",""+config.ventana.alto);p.setProperty("entero",""+config.entero);p.setProperty("filtro",config.filtro.name());
  try{ArchivosPartida.guardar(archivo,p);actual=config;revision++;mensaje="Gráficos aplicados y guardados.";}
  catch(IOException e){mensaje="No se pudieron guardar los gráficos: "+e.getMessage();}
 }
}
