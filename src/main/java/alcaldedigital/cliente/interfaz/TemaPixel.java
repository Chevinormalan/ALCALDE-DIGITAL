package alcaldedigital.cliente.interfaz;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Materiales compartidos de la interfaz. Coordenadas enteras y fuente empaquetada. */
public final class TemaPixel {
    public static final Color NOCHE=new Color(0x06263C), PANEL=new Color(0x09364F),
        AZUL=new Color(0x104E6C), CIAN=new Color(0x54BDCF), CREMA=new Color(0xFFF0CE),
        ORO=new Color(0xF4BD4F), SOMBRA=new Color(0x031522);
    private final Font base;
    private final Map<String,Font> fuentes=new HashMap<>();
    public TemaPixel() {
        try(var in=TemaPixel.class.getResourceAsStream("/cliente/fuentes/PixelifySans.ttf")) {
            if(in==null) throw new IllegalStateException("Falta PixelifySans.ttf");
            base=Font.createFont(Font.TRUETYPE_FONT,in);
        } catch(IOException|FontFormatException e) {throw new IllegalStateException("Fuente pixel inválida",e);}
    }
    public Font fuente(int size,boolean bold) {
        return fuentes.computeIfAbsent(size+":"+bold,k->base.deriveFont(bold?Font.BOLD:Font.PLAIN,(float)size));
    }
    private Font glifo(Font fuente,char c) {
        return fuente.canDisplay(c)?fuente:fuentes.computeIfAbsent("simbolo:"+fuente.getSize()+":"+fuente.getStyle(),
            k->new Font(Font.DIALOG,fuente.getStyle(),fuente.getSize()));
    }
    public int ancho(Graphics2D g,String texto,Font fuente) {
        int ancho=0;
        for(char c:texto.toCharArray()) ancho+=g.getFontMetrics(glifo(fuente,c)).charWidth(c);
        return ancho;
    }
    public void escribir(Graphics2D g,String texto,int x,int y,Font fuente) {
        for(char c:texto.toCharArray()) {
            Font f=glifo(fuente,c);g.setFont(f);g.drawString(String.valueOf(c),x,y);x+=g.getFontMetrics(f).charWidth(c);
        }
        g.setFont(fuente);
    }
    public static void nitido(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }
    public static void marco(Graphics2D g,int x,int y,int w,int h,Color fondo,Color borde,boolean adorno) {
        g.setColor(SOMBRA);g.fillRect(x+4,y+5,w,h);
        Polygon p=new Polygon(new int[]{x+6,x+w-6,x+w,x+w,x+w-6,x+6,x,x},
            new int[]{y,y,y+6,y+h-6,y+h,y+h,y+h-6,y+6},8);
        g.setColor(fondo);g.fillPolygon(p);g.setStroke(new BasicStroke(2));g.setColor(borde);g.drawPolygon(p);
        g.setColor(new Color(borde.getRed(),borde.getGreen(),borde.getBlue(),65));
        g.drawLine(x+7,y+4,x+w-7,y+4);g.drawLine(x+4,y+7,x+4,y+h-7);
        if(adorno) for(int dx:new int[]{0,w-13}) for(int dy:new int[]{0,h-13}) {
            g.setColor(borde);g.drawRect(x+dx+3,y+dy+3,7,7);g.fillRect(x+dx+6,y+dy+6,3,3);
        }
    }
}
