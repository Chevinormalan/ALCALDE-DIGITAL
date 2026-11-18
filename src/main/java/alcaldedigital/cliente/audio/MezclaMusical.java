package alcaldedigital.cliente.audio;

/** Mezcla PCM independiente del dispositivo: capas sincronizadas, pausa y fundido por tiempo. */
public final class MezclaMusical {
 public static final int FRECUENCIA=22050;
 public record Pcm(short[] muestras){public Pcm{if(muestras.length==0||muestras.length%2!=0)throw new IllegalArgumentException("PCM estéreo vacío");}public int frames(){return muestras.length/2;}public float muestra(long frame,int canal){return muestras[(int)(frame%frames())*2+canal]/32768f;}}
 public static final class Pista {
  public final String id;public final Pcm base,ritmo,epica;public long posicion;private float ganancia,intensidad;
  public Pista(String id,Pcm base,Pcm ritmo,Pcm epica){this.id=id;this.base=base;this.ritmo=ritmo;this.epica=epica;if(ritmo!=null&&ritmo.frames()!=base.frames()||epica!=null&&epica.frames()!=base.frames())throw new IllegalArgumentException("Capas desalineadas");}
 }
 private Pista actual,saliendo;private float intensidad=.3f,volumen=1;
 public void cambiar(Pista pista){if(actual!=null&&actual.id.equals(pista.id))return;saliendo=actual;actual=pista;}
 public void objetivo(float intensidad,float volumen){this.intensidad=Math.clamp(intensidad,0,1);this.volumen=Math.clamp(volumen,0,1);}
 public String id(){return actual==null?"":actual.id;}public long posicion(){return actual==null?0:actual.posicion;}
 public int pistasCargadas(){return (actual==null?0:1)+(saliendo==null?0:1);}
 public void mezclar(float[] salida,boolean pausa){if(pausa)return;sumar(actual,salida,volumen);sumar(saliendo,salida,0);if(saliendo!=null&&saliendo.ganancia<.0001f)saliendo=null;}
 private void sumar(Pista p,float[] salida,float meta){if(p==null)return;float r=1f/(FRECUENCIA*.4f),f=1f/(FRECUENCIA*.3f);
  for(int i=0;i<salida.length;i+=2){p.ganancia+=(meta-p.ganancia)*r;p.intensidad+=(intensidad-p.intensidad)*f;
   float ritmo=Math.clamp((p.intensidad-.1f)/.55f,0,1),epica=Math.clamp((p.intensidad-.55f)/.45f,0,1);
   for(int c=0;c<2;c++){float v=p.base.muestra(p.posicion,c);if(p.ritmo!=null)v+=p.ritmo.muestra(p.posicion,c)*ritmo;if(p.epica!=null)v+=p.epica.muestra(p.posicion,c)*epica;salida[i+c]+=v*p.ganancia;}
   p.posicion=(p.posicion+1)%p.base.frames();
  }
 }
}
