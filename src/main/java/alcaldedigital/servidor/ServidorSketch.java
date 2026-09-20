package alcaldedigital.servidor;

import alcaldedigital.compartido.modelo.EstadoCiudad;
import processing.core.PApplet;

/** Panel preliminar del servidor autoritativo. */
public final class ServidorSketch extends PApplet {
    private final EstadoCiudad estadoInicial = EstadoCiudad.inicial();

    @Override
    public void settings() {
        size(960, 540, P2D);
    }

    @Override
    public void setup() {
        surface.setTitle("Alcalde Digital — Servidor");
        noSmooth();
        textFont(createFont("SansSerif", 18));
    }

    @Override
    public void draw() {
        background(20, 31, 44);
        fill(238, 242, 239);
        textSize(30);
        text("SERVIDOR · CIUDAD NOVA", 45, 65);
        fill(48, 132, 132);
        rect(45, 92, width - 90, 4);
        fill(210, 220, 220);
        textSize(16);
        text("Estado: estructura inicial · 0 clientes conectados", 45, 140);
        text("Confianza inicial: " + estadoInicial.confianza(), 45, 178);
        text("Siguiente paso: sesión local y evento del parque", 45, 216);
    }
}

