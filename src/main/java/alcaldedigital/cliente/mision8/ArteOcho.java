package alcaldedigital.cliente.mision8;
import processing.core.*;import javax.imageio.ImageIO;import java.io.*;
/** Atlas nuevos y recursos vigentes. Las celdas conservan un pivote común. */
final class ArteOcho {
 final PImage[]heroes=hoja("/cliente/mision8/heroes.png",8,8),candidatos=hoja("/cliente/mision8/candidatos.png",8,8),efectos=hoja("/cliente/mision8/efectos.png",8,8),fondos=hoja("/cliente/mision8/escenarios.png",2,2),ciudadanos=hoja("/cliente/mision7/ciudadanos-estados.png",4,6),sofi=hoja("/cliente/arte/mision1/sofi-caminar.png",4,4),periodista=hoja("/cliente/mision2/periodista-correr-hoja.png",4,4);
 final PImage[][]infeccion=new PImage[4][];
 final PImage[]ataques=new PImage[96];
 final PImage[]reacciones=hoja("/cliente/mision7/reacciones.png",4,2);
 final PImage iconoCivitas=imagen("/cliente/arte/iconos/01_celular.png");
 final PImage parque=imagen("/cliente/arte/mision1/parque.png"),avenida=imagen("/cliente/mision7/avenida.png");
 final PImage[]influencer=new PImage[4],vuelo=new PImage[4];
 ArteOcho(){for(int f=1;f<=3;f++){var hoja=hoja("/cliente/mision8/ataques-forma-"+f+".png",8,4);for(int c=0;c<4;c++)for(int k=0;k<8;k++)ataques[AnimacionOcho.filaAtaque(c,f)*8+k]=hoja[c*8+k];}String[]n={"juan","petrosqui","abel","alberto"};for(int i=0;i<4;i++)infeccion[i]=hoja("/cliente/mision8/infeccion-"+n[i]+".png",4,4);fondos[0]=imagen("/cliente/mision8/tarima-cuatro.png");for(int i=0;i<4;i++){influencer[i]=imagen("/cliente/mision3/redisenio/player_right_"+(i+1)+".png");vuelo[i]=imagen("/cliente/mision3/redisenio/palomo_fly_"+(i+1)+".png");}}
 static PImage imagen(String ruta){try(var in=ArteOcho.class.getResourceAsStream(ruta)){if(in==null)throw new IOException("Falta "+ruta);return new PImage(ImageIO.read(in));}catch(IOException e){throw new IllegalStateException(e);}}
 static PImage[]hoja(String ruta,int c,int f){PImage im=imagen(ruta);PImage[]r=new PImage[c*f];for(int y=0;y<f;y++)for(int x=0;x<c;x++)r[y*c+x]=im.get(x*im.width/c,y*im.height/f,(x+1)*im.width/c-x*im.width/c,(y+1)*im.height/f-y*im.height/f);return r;}
 void sprite(PApplet p,PImage im,float x,float pies,float tam,boolean espejo){p.pushMatrix();p.translate(x,pies);if(espejo)p.scale(-1,1);float alto=tam*im.height/im.width;p.image(im,-tam/2,-alto,tam,alto);p.popMatrix();}
 PImage retrato(String quien,MotorOcho m){PImage im=switch(quien){case "SOFI"->sofi[0];case "PERIODISTA"->reacciones[4];case "INFLUENCER"->reacciones[0];case "MR. PALOMO"->efectos[24];case "JUAN"->candidatos[0];default->{int i=java.util.Arrays.asList(alcaldedigital.compartido.carrera.EstadoElectoral.NOMBRES).indexOf(quien);yield i>=0?candidatos[i*16]:iconoCivitas;}};return im;}
 PImage reposo(int rol){return heroes[rol*16];}
 PImage infeccion(int candidato,int ticks){return infeccion[candidato][AnimacionOcho.cuadroInfeccion(ticks)];}
 PImage jefe(MotorOcho m){return ataques[AnimacionOcho.filaAtaque(m.ganador,m.forma)*8+AnimacionOcho.cuadroJefe(m)];}
 PImage heroe(MotorOcho m,int i){return heroes[AnimacionOcho.cuadroHeroe(m,i)];}
}
