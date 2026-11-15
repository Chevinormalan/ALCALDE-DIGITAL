package alcaldedigital.cliente.presentacion;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;
class RecursosPresentacionTest {
 @Test void todosLosWavSeDecodificanSinSaturacion()throws Exception{
  for(String n:new String[]{"carro_ralenti","carro_marcha","carro_acelerar","helicoptero_hover","helicoptero_vuelo","helicoptero_descenso","ambiente","freno","impacto","disparo_jugador","disparo_dron","cohete","explosion_cohete","destruccion_dron","dano","paso_adoquin","paso_madera","radio","encendido_helicoptero"}){
   try(var in=getClass().getResourceAsStream("/cliente/presentacion/audio/"+n+".wav");var ais=AudioSystem.getAudioInputStream(new BufferedInputStream(in))){assertEquals(16,ais.getFormat().getSampleSizeInBits());byte[] b=ais.readAllBytes();assertTrue(b.length>2000);int peak=0;for(int i=0;i<b.length;i+=2){int v=(short)((b[i]&255)|(b[i+1]<<8));peak=Math.max(peak,Math.abs(v));}assertTrue(peak>500&&peak<30000,n);}
  }
 }
 @Test void atlasDeJuanTieneContenidoYAlphaEnSusCeldas()throws Exception{
  for(String nombre:new String[]{"atlas","acciones"})try(var in=getClass().getResourceAsStream("/cliente/presentacion/juan_"+nombre+".png")){
   var im=ImageIO.read(in);assertTrue(im.getColorModel().hasAlpha());int cols=nombre.equals("atlas")?10:4,w=im.getWidth()/cols,h=im.getHeight()/4;
   for(int r=0;r<4;r++)for(int c=0;c<cols;c++){int visibles=0,vacios=0;for(int y=r*h;y<(r+1)*h;y+=2)for(int x=c*w;x<(c+1)*w;x+=2){int a=im.getRGB(x,y)>>>24;if(a>100)visibles++;if(a<10)vacios++;}assertTrue(visibles>80,nombre+":"+r+":"+c);assertTrue(vacios>80,nombre+":"+r+":"+c);}
  }
 }
}
