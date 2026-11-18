package alcaldedigital.cliente.audio;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import alcaldedigital.cliente.audio.MezclaMusical.*;
class MezclaMusicalTest {
 private Pcm pcm(int frames,int valor){short[] v=new short[frames*2];java.util.Arrays.fill(v,(short)valor);return new Pcm(v);}
 @Test void cambiarRecursoConMismoCanalSustituyeLaPistaYTerminaElFundido(){var m=new MezclaMusical();m.cambiar(new Pista("M801",pcm(500000,8000),null,null));m.objetivo(.2f,1);m.mezclar(new float[44100],false);m.cambiar(new Pista("M802",pcm(500000,-8000),null,null));assertEquals("M802",m.id());assertEquals(2,m.pistasCargadas());float[] out=new float[300000];m.mezclar(out,false);assertTrue(out[out.length-1]<0);assertEquals(1,m.pistasCargadas());}
 @Test void pausaCongelaPosicionYLasMuestrasDeMusica(){var m=new MezclaMusical();m.cambiar(new Pista("x",pcm(50000,8000),null,null));m.objetivo(.2f,1);m.mezclar(new float[1000],false);long antes=m.posicion();float[] out=new float[4000];m.mezclar(out,true);assertEquals(antes,m.posicion());for(float n:out)assertEquals(0,n);m.mezclar(out,false);assertEquals(antes+2000,m.posicion());}
 @Test void lasCapasCompartenPosicionYLaIntensidadAumentaLaEnergia(){var m=new MezclaMusical();m.cambiar(new Pista("x",pcm(1000,2000),pcm(1000,3000),pcm(1000,4000)));m.objetivo(0,1);float[] bajo=new float[200000];m.mezclar(bajo,false);long pos=m.posicion();m.objetivo(1,1);float[] alto=new float[200000];m.mezclar(alto,false);assertTrue(alto[alto.length-1]>bajo[bajo.length-1]*3);assertEquals(pos,m.posicion());}
 @Test void laMezclaEsIndependienteDelTamanoDelBloqueYNoReiniciaUnaPistaIgual(){var a=new MezclaMusical();var b=new MezclaMusical();for(var m:new MezclaMusical[]{a,b}){m.cambiar(new Pista("x",pcm(22050,4000),null,null));m.objetivo(.4f,.6f);}float[] todo=new float[20480];a.mezclar(todo,false);for(int i=0;i<10;i++){float[] parte=new float[2048];b.mezclar(parte,false);for(int k=0;k<2048;k++)assertEquals(todo[i*2048+k],parte[k]);}long antes=a.posicion();a.cambiar(new Pista("x",pcm(22050,4000),null,null));assertEquals(antes,a.posicion());}
 @Test void seRechazanCapasDesalineadas(){assertThrows(IllegalArgumentException.class,()->new Pista("x",pcm(100,1),pcm(101,1),null));}
}
