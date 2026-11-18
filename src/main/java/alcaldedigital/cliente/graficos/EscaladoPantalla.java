package alcaldedigital.cliente.graficos;

/** Una única transformación para la imagen, los avisos del ratón y sus clics. */
public final class EscaladoPantalla {
 public static final int ANCHO=1280,ALTO=720;
 private EscaladoPantalla(){}
 public record Punto(int x,int y){public boolean dentro(){return x>=0&&y>=0;}}
 public record Marco(int x,int y,int ancho,int alto){
  public Punto aLogico(int px,int py){
   if(px<x||py<y||px>=x+ancho||py>=y+alto)return new Punto(-1,-1);
   return new Punto((int)((long)(px-x)*ANCHO/ancho),(int)((long)(py-y)*ALTO/alto));
  }
  public double factor(){return ancho/(double)ANCHO;}
 }
 public static Marco ajustar(int ancho,int alto,boolean entero){
  if(ancho<1||alto<1)throw new IllegalArgumentException("Área de pantalla inválida");
  double escala=Math.min(ancho/(double)ANCHO,alto/(double)ALTO);
  if(entero&&escala>=1)escala=Math.floor(escala);
  int w=Math.max(1,Math.min(ancho,(int)Math.floor(ANCHO*escala))),h=Math.max(1,Math.min(alto,(int)Math.floor(ALTO*escala)));
  return new Marco((ancho-w)/2,(alto-h)/2,w,h);
 }
}
