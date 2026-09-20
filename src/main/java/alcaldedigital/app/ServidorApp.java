package alcaldedigital.app;

import alcaldedigital.servidor.ServidorSketch;
import processing.core.PApplet;

/** Punto de entrada del panel gráfico del servidor. */
public final class ServidorApp {
    private ServidorApp() {
    }

    public static void main(String[] args) {
        PApplet.main(ServidorSketch.class.getName());
    }
}

