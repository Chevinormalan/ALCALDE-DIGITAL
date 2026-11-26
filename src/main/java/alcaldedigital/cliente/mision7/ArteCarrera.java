package alcaldedigital.cliente.mision7;

import processing.core.PImage;
import javax.imageio.ImageIO;
import java.io.*;

/** Recursos existentes sin recolorear; atlas nuevos con márgenes y escala uniformes. */
final class ArteCarrera {
    final PImage avenida=imagen("/cliente/mision7/avenida.png"),parque=imagen("/cliente/arte/mision1/parque.png"),
        oficina=imagen("/cliente/mision2/oficina.png"),mall=imagen("/cliente/mision3/redisenio/fondo-atrio.png"),
        palomo=imagen("/cliente/mision3/redisenio/palomo_talk_1.png"),
        candidato=imagen("/cliente/arte/actores/candidato_00.png"),telefono=imagen("/cliente/arte/iconos/01_celular.png");
    final PImage[][] andar=new PImage[2][16];
    final PImage[] marchaPeriodista=hoja("/cliente/mision2/periodista-caminar-hoja.png",4,4);
    final PImage influencerQuieto=imagen("/cliente/mision3/redisenio/player_right_idle.png");
    final PImage[] reacciones=hoja("/cliente/mision7/reacciones.png",4,2),ciudadanos=hoja("/cliente/mision7/ciudadanos-estados.png",4,6),
        antiguosA=hoja("/cliente/arte/mision1/transeuntes-direcciones-a.png",4,4),antiguosB=hoja("/cliente/arte/mision1/transeuntes-direcciones-b.png",4,4),
        objetos=hoja("/cliente/mision7/objetos.png",4,4),sofi=hoja("/cliente/arte/mision1/sofi-caminar.png",4,4);
    final PImage[] vuelo=new PImage[4];
    ArteCarrera(){
        andar[0]=hoja("/cliente/mision2/periodista-correr-hoja.png",4,4);
        String[] dir={"front","back","left","right"};for(int r=0;r<4;r++)for(int f=0;f<4;f++)andar[1][r*4+f]=imagen("/cliente/mision3/redisenio/player_"+dir[r]+"_"+(f+1)+".png");
        for(int i=0;i<4;i++)vuelo[i]=imagen("/cliente/mision3/redisenio/palomo_fly_"+(i+1)+".png");
    }
    static PImage imagen(String ruta){try(var in=ArteCarrera.class.getResourceAsStream(ruta)){if(in==null)throw new IOException("Falta recurso "+ruta);var b=ImageIO.read(in);if(b==null)throw new IOException("PNG inválido "+ruta);return new PImage(b);}catch(IOException e){throw new IllegalStateException(e);}}
    static PImage[] hoja(String ruta,int columnas,int filas){PImage im=imagen(ruta);im.loadPixels();int minX=im.width,minY=im.height,maxX=0,maxY=0;
        for(int r=0;r<filas;r++)for(int c=0;c<columnas;c++){
            int x0=c*im.width/columnas,y0=r*im.height/filas,x1=(c+1)*im.width/columnas,y1=(r+1)*im.height/filas;
            for(int y=y0;y<y1;y++)for(int x=x0;x<x1;x++)if((im.pixels[y*im.width+x]>>>24)>32){minX=Math.min(minX,x-x0);minY=Math.min(minY,y-y0);maxX=Math.max(maxX,x-x0);maxY=Math.max(maxY,y-y0);}
        }
        if(minX>maxX||minY>maxY)throw new IllegalStateException("Atlas vacío: "+ruta);
        PImage[] frames=new PImage[columnas*filas];for(int r=0;r<filas;r++)for(int c=0;c<columnas;c++){
            int x=c*im.width/columnas,y=r*im.height/filas;int w=Math.min(maxX-minX+1,(c+1)*im.width/columnas-x-minX),h=Math.min(maxY-minY+1,(r+1)*im.height/filas-y-minY);
            frames[r*columnas+c]=im.get(x+minX,y+minY,w,h);
        }return frames;
    }
}
