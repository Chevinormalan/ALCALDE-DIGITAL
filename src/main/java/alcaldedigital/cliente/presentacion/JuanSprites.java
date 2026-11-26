package alcaldedigital.cliente.presentacion;
import processing.core.*;
import javax.imageio.ImageIO;
import java.util.*;

/** Un solo Juan, dos atlas; pivote de pies y escala uniforme por secuencia. */
public final class JuanSprites {
 private record Frame(PImage imagen,float pivote,float referencia){}
 private final Map<String,PImage> atlas=new HashMap<>();private final Map<String,Frame> frames=new HashMap<>();
 private PImage atlas(String n){return atlas.computeIfAbsent(n,k->{try(var in=getClass().getResourceAsStream("/cliente/presentacion/juan_"+k+".png")){if(in==null)throw new IllegalStateException("Falta atlas de Juan: "+k);return new PImage(ImageIO.read(in));}catch(java.io.IOException ex){throw new IllegalStateException(ex);}});}
 private Frame frame(String nombre,int col,int fila){String key=nombre+":"+fila+":"+col;Frame cached=frames.get(key);if(cached!=null)return cached;
  PImage source=atlas(nombre);int cols=nombre.equals("atlas")?10:4,rows=4,cw=source.width/cols,ch=source.height/rows;
  PImage celda=source.get(col*cw,fila*ch,cw,ch);celda.loadPixels();int[] xx=new int[cw],yy=new int[ch];
  for(int y=0;y<ch;y++)for(int x=0;x<cw;x++)if((celda.pixels[y*cw+x]>>>24)>80){xx[x]++;yy[y]++;}
  int x0=0,x1=cw-1,y0=0,y1=ch-1;while(x0<x1&&xx[x0]<3)x0++;while(x1>x0&&xx[x1]<3)x1--;while(y0<y1&&yy[y0]<3)y0++;while(y1>y0&&yy[y1]<3)y1--;
  if(x1-x0<3||y1-y0<3)throw new IllegalStateException("Celda vacía de Juan: "+key);
  Frame f=new Frame(celda.get(x0,y0,x1-x0+1,y1-y0+1),cw/2f-x0,ch*(nombre.equals("atlas")?.90f:.82f));frames.put(key,f);return f;
 }
 public String seleccion(String estado,int dir,float pasos,float tiempo){
  if(estado.equals("anticipacion"))return "acciones:"+(dir==1?2:dir>=2?1:0)+":1";
  if(estado.equals("recobro"))return dir==1?"acciones:3:2":dir>=2?"atlas:2:6":"atlas:2:2";
  if(estado.equals("attack")||estado.equals("defend")||estado.equals("hurt")||estado.equals("seated"))return "acciones:"+(dir==1?2:dir>=2?1:0)+":"+(estado.equals("attack")?0:estado.equals("defend")?1:estado.equals("hurt")?2:3);
  if(estado.equals("walk")){if(dir==1)return "acciones:3:"+Math.floorMod((int)pasos,4);return "atlas:2:"+(dir>=2?4+Math.floorMod((int)pasos,6):Math.floorMod((int)pasos,4));}
  if(estado.equals("speech"))return "atlas:1:"+new int[]{1,2,5,6,8}[Math.floorMod((int)(tiempo*2),5)];
  if(estado.equals("confused"))return "atlas:0:3";
  return dir==1?"acciones:3:0":dir>=2?"atlas:0:8":"atlas:0:"+((int)(tiempo*10)%45==0?5:0);
 }
 public void dibujar(PGraphics g,String estado,int dir,float pasos,float tiempo,float x,float pies,float ancho,float alto){
  String[] key=seleccion(estado,dir,pasos,tiempo).split(":");Frame f=frame(key[0],Integer.parseInt(key[2]),Integer.parseInt(key[1]));
  float escala=alto/f.referencia;float limite=ancho*(estado.equals("attack")?1.8f:1.6f);escala=Math.min(escala,limite/f.imagen.width);
  g.pushMatrix();g.translate(x,pies);if(dir==2)g.scale(-1,1);g.image(f.imagen,-f.pivote*escala,-f.imagen.height*escala,f.imagen.width*escala,f.imagen.height*escala);g.popMatrix();
 }
}
