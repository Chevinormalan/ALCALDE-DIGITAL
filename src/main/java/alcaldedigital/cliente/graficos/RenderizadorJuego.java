package alcaldedigital.cliente.graficos;

import processing.awt.PGraphicsJava2D;
import processing.awt.SuperficiePantallaCompleta;
import processing.core.PSurface;

public final class RenderizadorJuego extends PGraphicsJava2D {
    @Override
    public PSurface createSurface() {
        return surface = new SuperficiePantallaCompleta(this);
    }
}
