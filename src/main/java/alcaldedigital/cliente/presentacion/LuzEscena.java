package alcaldedigital.cliente.presentacion;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import processing.core.PGraphics;
import processing.awt.PGraphicsJava2D;

/** Luz precalculada que se compone detrás de los actores y de la interfaz. */
public final class LuzEscena {
    private static final int TAMANO = 128;
    public static final LuzEscena AMBAR = new LuzEscena(0xFFD690);
    public static final LuzEscena CIAN = new LuzEscena(0x78E8E8);
    public static final LuzEscena MAGENTA = new LuzEscena(0xE888CE);
    public static final LuzEscena VIOLETA = new LuzEscena(0x9589F4);
    private final BufferedImage imagen;

    private LuzEscena(int rgb) {
        imagen = new BufferedImage(TAMANO, TAMANO, BufferedImage.TYPE_INT_ARGB_PRE);
        var g = imagen.createGraphics();
        g.setPaint(new RadialGradientPaint(TAMANO / 2f, TAMANO / 2f, TAMANO / 2f,
            new float[]{0, .28f, .64f, 1}, new Color[]{new Color(0xB0000000 | rgb, true),
            new Color(0x6B000000 | rgb, true), new Color(0x29000000 | rgb, true), new Color(rgb, true)}));
        g.fillRect(0, 0, TAMANO, TAMANO);
        g.dispose();
    }

    public void dibujar(PGraphics g, float x, float y, float ancho, float alto, float intensidad) {
        dibujar(((PGraphicsJava2D)g).g2, Math.round(x), Math.round(y),
            Math.round(ancho), Math.round(alto), intensidad / 255);
    }

    public void dibujar(java.awt.Graphics2D g, int x, int y, int ancho, int alto, float intensidad) {
        var antes = g.getComposite();
        g.setComposite(AlphaComposite.SrcOver.derive(Math.clamp(intensidad, 0, 1)));
        try {
            g.drawImage(imagen, x - ancho / 2, y - alto / 2, ancho, alto, null);
        } finally {
            g.setComposite(antes);
        }
    }
}
