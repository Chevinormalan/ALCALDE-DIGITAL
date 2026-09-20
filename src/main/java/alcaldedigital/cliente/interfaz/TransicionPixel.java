package alcaldedigital.cliente.interfaz;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Composición visual independiente de la sesión. No ejecuta acciones de juego. */
public final class TransicionPixel {
    private String clave;
    private BufferedImage anterior, destino, salida;
    private double transcurrido=1;
    private static final double DURACION=.24;
    public boolean activa() {return transcurrido<DURACION;}
    public boolean lista(String nueva) {return !activa()&&nueva.equals(clave);}
    private static BufferedImage copiar(BufferedImage imagen) {
        var copia=new BufferedImage(imagen.getWidth(),imagen.getHeight(),BufferedImage.TYPE_INT_ARGB);
        var g=copia.createGraphics();g.drawImage(imagen,0,0,null);g.dispose();return copia;
    }
    public BufferedImage presentar(BufferedImage imagen,String nueva,double dt,boolean reducido,boolean panel) {
        if(clave==null) {clave=nueva;destino=copiar(imagen);return imagen;}
        if(!clave.equals(nueva)) {
            anterior=copiar(activa()&&salida!=null?salida:destino);
            clave=nueva;transcurrido=0;
        }
        if(destino==null) destino=copiar(imagen);
        else {var d=destino.createGraphics();d.drawImage(imagen,0,0,null);d.dispose();}
        if(reducido) transcurrido=DURACION;
        else transcurrido+=Math.max(0,Math.min(.1,dt));
        if(!activa()) return imagen;
        int w=imagen.getWidth(),h=imagen.getHeight();
        if(salida==null) salida=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        var g=salida.createGraphics();TemaPixel.nitido(g);g.drawImage(anterior,0,0,null);
        double t=Math.min(1,transcurrido/DURACION),ease=1-Math.pow(1-t,3);
        g.setColor(new Color(0,12,25,(int)(110*ease)));g.fillRect(0,0,w,h);
        if(panel) {
            int visible=(int)(h*ease)/4*4;
            g.clipRect(0,0,w,visible);
            int desplazamiento=-(int)(24*(1-ease))/2*2;
            g.drawImage(destino,0,desplazamiento,null);
            g.setClip(null);g.setColor(TemaPixel.ORO);g.fillRect(12,Math.max(0,visible-2),w-24,2);
        } else {
            for(int y=0;y<h;y+=24) {
                int visible=(int)(w*Math.min(1,Math.max(0,ease*1.2-(y/24%3)*.07)))/8*8;
                g.setClip(0,y,visible,24);g.drawImage(destino,0,0,null);
            }
        }
        g.dispose();return salida;
    }
}
