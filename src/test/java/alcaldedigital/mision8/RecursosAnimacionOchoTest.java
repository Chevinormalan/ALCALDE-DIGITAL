package alcaldedigital.mision8;

import javax.imageio.ImageIO;
import java.util.*;
import java.security.MessageDigest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecursosAnimacionOchoTest {
 @Test void cadaCandidatoTieneDieciseisPasosPropiosDeInfeccion()throws Exception{
  for(String nombre:List.of("juan","petrosqui","abel","alberto"))validar("infeccion-"+nombre,4,4);
 }
 @Test void hayOchoPosesPorCadaFormaYCandidaturaConTransparencia()throws Exception{for(int f=1;f<=3;f++)validar("ataques-forma-"+f,8,4);}
 private void validar(String nombre,int columnas,int filas)throws Exception{
  try(var in=getClass().getResourceAsStream("/cliente/mision8/"+nombre+".png")){
   assertNotNull(in);var im=ImageIO.read(in);assertTrue(im.getColorModel().hasAlpha());Set<String>hashes=new HashSet<>();
   for(int y=0;y<filas;y++)for(int x=0;x<columnas;x++){
    int alfaMin=255,opacos=0;var hash=MessageDigest.getInstance("SHA-256");
    for(int py=y*im.getHeight()/filas;py<(y+1)*im.getHeight()/filas;py++)for(int px=x*im.getWidth()/columnas;px<(x+1)*im.getWidth()/columnas;px++){
     int v=im.getRGB(px,py),a=v>>>24;alfaMin=Math.min(alfaMin,a);if(a>200)opacos++;hash.update(new byte[]{(byte)(v>>>24),(byte)(v>>>16),(byte)(v>>>8),(byte)v});
    }
    assertEquals(0,alfaMin,"Fondo sin transparencia: "+nombre+" "+x+","+y);assertTrue(opacos>100,"Cuadro vacío");assertTrue(hashes.add(HexFormat.of().formatHex(hash.digest())),"Cuadro duplicado: "+nombre);
   }
   assertEquals(columnas*filas,hashes.size());
  }
 }
}
