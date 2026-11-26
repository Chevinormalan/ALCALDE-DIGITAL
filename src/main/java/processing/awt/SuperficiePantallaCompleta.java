package processing.awt;

import java.awt.Insets;

/** Mantiene el lienzo al tamaño del monitor durante la pantalla completa de AWT. */
public final class SuperficiePantallaCompleta extends PSurfaceAWT {
    private boolean completa;

    public SuperficiePantallaCompleta(PGraphicsJava2D renderer) {
        super(renderer);
    }

    public void pantallaCompleta(boolean completa) {
        this.completa = completa;
        if (completa) setSize(frame.getWidth(), frame.getHeight());
        else currentInsets = frame.getInsets();
    }

    @Override
    public void setSize(int ancho, int alto) {
        if (!completa) {
            super.setSize(ancho, alto);
            return;
        }

        // El compositor puede colocar la ventana en un monitor distinto del solicitado.
        currentInsets = new Insets(0, 0, 0, 0);
        sketchWidth = Math.max(1, frame.getWidth());
        sketchHeight = Math.max(1, frame.getHeight());
        frame.getRootPane().setBounds(0, 0, sketchWidth, sketchHeight);
        frame.getRootPane().doLayout();
        canvas.setBounds(0, 0, sketchWidth, sketchHeight);
        if (sketch.width != sketchWidth || sketch.height != sketchHeight) {
            sketch.setSize(sketchWidth, sketchHeight);
            graphics.setSize(sketchWidth, sketchHeight);
        }
    }
}
