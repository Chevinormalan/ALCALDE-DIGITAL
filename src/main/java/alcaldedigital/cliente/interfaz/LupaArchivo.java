package alcaldedigital.cliente.interfaz;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/** Óptica y seguimiento de la lupa. La selección sigue usando el puntero original. */
final class LupaArchivo {
    private static final int RADIO = 88, MUESTRA = 96, CENTRO = 108, MARCO = 264;
    private final BufferedImage muestra = new BufferedImage(MUESTRA, MUESTRA, BufferedImage.TYPE_INT_RGB);
    private static final BufferedImage LATON = marco(false), ENFOCADO = marco(true);
    private double tiempo = Double.NaN;
    private float x, y, apertura;
    private boolean objetivo;

    void reiniciar() {
        tiempo = Double.NaN;
        apertura = 0;
        objetivo = false;
    }

    void dibujar(RenderJuego ui, int px, int py, boolean visible, boolean fijar, boolean objetivo) {
        double ahora = ui.juego.animacion;
        boolean reducido = ui.juego.movimientoReducido;
        boolean corte = Double.isNaN(tiempo) || ahora < tiempo || ahora - tiempo > .24;
        float dt = corte ? 0 : (float) (ahora - tiempo);
        tiempo = ahora;
        if (corte) apertura = 0;
        if (visible) {
            if (corte || fijar || reducido || apertura == 0) {
                x = px;
                y = py;
            } else {
                float seguir = (float) (1 - Math.exp(-26 * dt));
                x = px + Math.clamp((x - px) * (1 - seguir), -12, 12);
                y = py + Math.clamp((y - py) * (1 - seguir), -12, 12);
            }
            this.objetivo = objetivo;
        }
        apertura = reducido ? (visible ? 1 : 0) : Math.clamp(apertura + (visible ? dt / .15f : -dt / .11f), 0, 1);
        if (!visible && apertura == 0) return;
        float fase = visible ? Math.max(.25f, apertura) : apertura;
        float escala = .76f + .24f * fase * (2 - fase);
        Graphics2D copia = muestra.createGraphics();
        int sx = Math.round(x) - MUESTRA / 2, sy = Math.round(y) - MUESTRA / 2;
        copia.drawImage(ui.lienzo, 0, 0, MUESTRA, MUESTRA, sx, sy, sx + MUESTRA, sy + MUESTRA, null);
        copia.dispose();

        Graphics2D g = (Graphics2D) ui.g.create();
        try {
            g.translate(x, y);
            g.scale(escala, escala);
            g.setComposite(AlphaComposite.SrcOver.derive(visible ? 1 : fase));
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            vidrio(g, RADIO, 1.9);
            vidrio(g, RADIO - 9, 2.48);
            g.drawImage(this.objetivo ? ENFOCADO : LATON, -CENTRO, -CENTRO, null);
            if (this.objetivo) {
                g.setColor(new Color(0x186B5D));
                for (int lado = -1; lado <= 1; lado += 2) {
                    g.fillRect(lado * 13 - 1, -7, 2, 15);
                    g.fillRect(lado * 9 - 4, -7, 9, 1);
                    g.fillRect(lado * 9 - 4, 7, 9, 1);
                }
            }
        } finally {
            g.dispose();
        }
    }

    private void vidrio(Graphics2D destino, int radio, double aumento) {
        Graphics2D g = (Graphics2D) destino.create();
        g.clip(new Ellipse2D.Double(-radio, -radio, 2 * radio, 2 * radio));
        AffineTransform transformacion = AffineTransform.getTranslateInstance(
            -aumento * MUESTRA / 2, -aumento * MUESTRA / 2);
        transformacion.scale(aumento, aumento);
        g.drawImage(muestra, transformacion, null);
        g.setPaint(new GradientPaint(0, -radio, new Color(225, 247, 234, 61),
            0, radio, new Color(52, 71, 107, 44)));
        g.fillRect(-radio, -radio, 2 * radio, 2 * radio);
        g.dispose();
    }

    private static BufferedImage marco(boolean objetivo) {
        var imagen = new BufferedImage(MARCO, MARCO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.translate(CENTRO, CENTRO);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        var mango = (Graphics2D) g.create();
        mango.rotate(Math.PI / 4);
        mango.setColor(new Color(28, 15, 34, 105));
        mango.fillRoundRect(72, -11, 83, 30, 15, 15);
        mango.setColor(new Color(0x281B30));
        mango.fillRoundRect(73, -13, 82, 26, 12, 12);
        mango.setPaint(new GradientPaint(76, -10, new Color(0xAC704D), 76, 11, new Color(0x432734)));
        mango.fillRoundRect(80, -9, 71, 18, 8, 8);
        mango.setColor(new Color(0xD2A368));
        mango.fillRoundRect(83, -11, 13, 22, 5, 5);
        mango.fillRoundRect(140, -10, 8, 20, 4, 4);
        mango.setColor(new Color(249, 196, 135, 130));
        mango.drawLine(98, -6, 138, -6);
        mango.dispose();

        g.setColor(new Color(24, 16, 36, 84));
        g.setStroke(new BasicStroke(12));
        g.drawOval(-RADIO + 2, -RADIO + 6, 2 * RADIO, 2 * RADIO);
        g.setColor(new Color(0x2B232D));
        g.setStroke(new BasicStroke(14));
        g.drawOval(-RADIO, -RADIO, 2 * RADIO, 2 * RADIO);
        g.setPaint(new GradientPaint(-68, -68, new Color(0xFFE1A6),
            74, 68, new Color(objetivo ? 0x548D75 : 0x99603E)));
        g.setStroke(new BasicStroke(9));
        g.drawOval(-RADIO, -RADIO, 2 * RADIO, 2 * RADIO);
        g.setStroke(new BasicStroke(1.4f));
        g.setColor(new Color(0x4B3546));
        g.drawOval(-RADIO - 2, -RADIO - 2, 2 * RADIO + 4, 2 * RADIO + 4);
        g.setColor(new Color(objetivo ? 0xAAE8BE : 0xE6B977));
        g.drawOval(-RADIO + 3, -RADIO + 3, 2 * RADIO - 6, 2 * RADIO - 6);
        g.setColor(new Color(245, 255, 243, 155));
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(-77, -77, 154, 154, 109, 41);
        g.setColor(new Color(247, 255, 245, 85));
        g.drawArc(-71, -71, 142, 142, 117, 19);
        g.drawArc(-79, -79, 158, 158, 289, 47);
        g.dispose();
        return imagen;
    }
}
