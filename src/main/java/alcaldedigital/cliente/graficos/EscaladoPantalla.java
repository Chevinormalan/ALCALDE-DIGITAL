package alcaldedigital.cliente.graficos;

/** Una única transformación para la imagen, los avisos del ratón y sus clics. */
public final class EscaladoPantalla {
 public static final int ANCHO=1280,ALTO=720;
 private EscaladoPantalla(){}
 public record Punto(int x,int y){public boolean dentro(){return x>=0&&y>=0;}}
 public record Marco(int x,int y,int ancho,int alto){
  public Punto aLogico(int px,int py){
   if(px<x||py<y||px>=x+ancho||py>=y+alto)return new Punto(-1,-1);
   return new Punto(coordenada(px-x,ANCHO,ancho),coordenada(py-y,ALTO,alto));
  }
  public double factor(){return ancho/(double)ANCHO;}
 }
 private static int coordenada(int pixel,int origen,int destino){
  return (int)((2L*pixel+1)*origen/(2L*destino));
 }
 public static Marco ajustar(int ancho,int alto,boolean entero){
  if(ancho<1||alto<1)throw new IllegalArgumentException("Área de pantalla inválida");
  int escala=Math.min(ancho/ANCHO,alto/ALTO);
  int w=ancho,h=alto;
  if(entero&&escala>=1){w=ANCHO*escala;h=ALTO*escala;}
  else if((long)ancho*ALTO<=(long)alto*ANCHO)h=Math.max(1,(int)((long)ancho*ALTO/ANCHO));
  else w=Math.max(1,(int)((long)alto*ANCHO/ALTO));
  return new Marco((ancho-w)/2,(alto-h)/2,w,h);
 }
}
