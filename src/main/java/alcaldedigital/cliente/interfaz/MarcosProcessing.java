package alcaldedigital.cliente.interfaz;

import processing.core.PGraphics;
import processing.awt.PGraphicsJava2D;
import java.awt.Color;

/** Usa el material del menú principal también sobre los lienzos Processing. */
public final class MarcosProcessing {
    private MarcosProcessing() {}
    public static void marco(PGraphics g,float x,float y,float w,float h,int fondo,int borde) {
        if(g instanceof PGraphicsJava2D java2d)
            TemaPixel.marco(java2d.g2,(int)x,(int)y,(int)w,(int)h,new Color(fondo,true),new Color(borde,true),true);
    }
}
