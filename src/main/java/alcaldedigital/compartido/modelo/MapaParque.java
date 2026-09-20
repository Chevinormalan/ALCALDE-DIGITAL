package alcaldedigital.compartido.modelo;
import java.util.*;

/** Tilemap 40x30 de 16px: suelo, sólidos y props ordenados por pies. */
public final class MapaParque {
    public static final int TILE=16, COLUMNAS=40, FILAS=30;
    public record Objeto(String tipo,int x,int y,int ancho,int alto) { }
    private final List<Objeto> objetos=new ArrayList<>();
    public MapaParque() {
        for(int x=16;x<640;x+=48) {
            objetos.add(new Objeto(x%3==0?"pino":"arbol",x,38,40,52));
            if(x<280||x>360) objetos.add(new Objeto("arbol",x,478,40,52));
        }
        for(int y=100;y<450;y+=55) {
            objetos.add(new Objeto("arbol",20,y,44,54)); objetos.add(new Objeto("pino",620,y,40,54));
        }
        objetos.add(new Objeto("fuente",320,240,88,90));
        objetos.add(new Objeto("aviso",176,150,38,42));
        objetos.add(new Objeto("banco",238,310,38,22)); objetos.add(new Objeto("banco",398,310,38,22));
        for(int x:new int[]{100,530}) for(int y:new int[]{180,350}) objetos.add(new Objeto("rosa",x,y,48,58));
        for(int x:new int[]{266,374}) for(int y:new int[]{146,298}) objetos.add(new Objeto("flores",x,y,26,22));
        for(int x:new int[]{144,450}) for(int y:new int[]{190,360}) objetos.add(new Objeto("lampara",x,y,18,42));
    }
    public List<Objeto> objetos() { return List.copyOf(objetos); }
    public int suelo(int columna,int fila) {
        if(columna<0||fila<0||columna>=COLUMNAS||fila>=FILAS) return 2;
        if(columna>=4&&columna<=9&&fila>=4&&fila<=7) return 2;
        if(columna>=18&&columna<=21||fila>=12&&fila<=15
            ||columna>=10&&columna<=12&&fila>=8&&fila<=22
            ||columna>=27&&columna<=29&&fila>=8&&fila<=22
            ||fila>=21&&fila<=23&&columna>=10&&columna<=29
            ||fila>=8&&fila<=10&&columna>=10&&columna<=29) return 1;
        return 0;
    }
    public boolean transitable(float x,float y) {
        if(x<38||x>598||y<56||y>454) return false;
        for(int dx:new int[]{-5,5}) for(int dy:new int[]{-3,3})
            if(suelo((int)(x+dx)/TILE,(int)(y+dy)/TILE)==2) return false;
        for(Objeto o:objetos) {
            int w=o.tipo.equals("fuente")?37:o.tipo.equals("aviso")?15:o.tipo.equals("banco")?17:7;
            int h=o.tipo.equals("fuente")?32:7;
            if(Math.abs(x-o.x)<w+5&&y>o.y-h&&y<o.y+6) return false;
        }
        return true;
    }
}
