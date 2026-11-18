package alcaldedigital.cliente.audio;

import alcaldedigital.compartido.guardado.ArchivosPartida;
import java.nio.file.Path;
import java.util.Properties;
import java.io.IOException;

/** Preferencias locales: no viajan en partidas ni en el protocolo LAN. */
public final class OpcionesAudio {
 public enum Bus { GENERAL,MUSICA,EFECTOS,AMBIENTE,INTERFAZ }
 public record Configuracion(int general,int musica,int efectos,int ambiente,int interfaz,boolean mudo,boolean suave,boolean voces){
  public Configuracion {for(int n:new int[]{general,musica,efectos,ambiente,interfaz})if(n<0||n>100)throw new IllegalArgumentException("Volumen fuera de rango");}
  public int valor(Bus b){return switch(b){case GENERAL->general;case MUSICA->musica;case EFECTOS->efectos;case AMBIENTE->ambiente;case INTERFAZ->interfaz;};}
  public float ganancia(Bus b){return mudo?0:(general/100f)*(b==Bus.GENERAL?1:valor(b)/100f);}
 }
 public static final Configuracion ORIGINAL=new Configuracion(80,55,70,35,50,false,false,false);
 private final Path archivo;private volatile Configuracion actual=ORIGINAL;
 public String mensaje="";
 public OpcionesAudio(){this(ArchivosPartida.directorio().resolve("audio.properties"));}
 public OpcionesAudio(Path archivo){this.archivo=archivo;try{var p=ArchivosPartida.leer(archivo);if(p!=null){if(!"1".equals(p.getProperty("version")))throw new IllegalArgumentException();actual=new Configuracion(n(p,"general"),n(p,"musica"),n(p,"efectos"),n(p,"ambiente"),n(p,"interfaz"),Boolean.parseBoolean(p.getProperty("mudo")),Boolean.parseBoolean(p.getProperty("suave")),Boolean.parseBoolean(p.getProperty("voces")));}}catch(IOException|RuntimeException ex){mensaje="Se recuperaron los valores originales de audio.";}}
 private static int n(Properties p,String k){return Integer.parseInt(p.getProperty(k));}
 public Configuracion actual(){return actual;}
 public void cambiar(Bus b,int paso){var c=actual;int[] v={c.general,c.musica,c.efectos,c.ambiente,c.interfaz};v[b.ordinal()]=Math.clamp(v[b.ordinal()]+paso,0,100);guardar(new Configuracion(v[0],v[1],v[2],v[3],v[4],c.mudo,c.suave,c.voces));}
 public void alternar(String nombre){var c=actual;guardar(new Configuracion(c.general,c.musica,c.efectos,c.ambiente,c.interfaz,nombre.equals("mudo")?!c.mudo:c.mudo,nombre.equals("suave")?!c.suave:c.suave,nombre.equals("voces")?!c.voces:c.voces));}
 public void restaurar(){guardar(ORIGINAL);}
 private void guardar(Configuracion c){Properties p=new Properties();p.setProperty("version","1");for(Bus b:Bus.values())p.setProperty(b.name().toLowerCase(),""+c.valor(b));p.setProperty("mudo",""+c.mudo);p.setProperty("suave",""+c.suave);p.setProperty("voces",""+c.voces);try{ArchivosPartida.guardar(archivo,p);actual=c;mensaje="Audio guardado.";}catch(IOException ex){mensaje="No se pudo guardar el audio: "+ex.getMessage();}}
}
