package alcaldedigital.cliente.presentacion;

import processing.core.PGraphics;
import processing.core.PImage;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/** Dibujos de perfil con pivote común. Solo las vistas eligen el cuadro. */
public enum PosesRemaster {
    PERIODISTA("periodista",8), INFLUENCER("influencer",8), JUAN("juan",8),
    SOFI("sofi",8), PALOMO("palomo",8),
    TROLL("troll",4), SPAMBOT("spambot",4), DEEPFAKE("deepfake",4);

    private static final int CELDA = 128, APOYO = 115, ALTO = 90;
    private final BufferedImage[] pixeles;
    private final PImage[] cuadros;

    PosesRemaster(String nombre,int cantidad) {
        cuadros=new PImage[cantidad];
        pixeles=new BufferedImage[cantidad];
        String ruta = "/cliente/presentacion/remaster/" + nombre + ".png";
        try (var in = PosesRemaster.class.getResourceAsStream(ruta)) {
            if (in == null) throw new IOException("Falta " + ruta);
            var atlas = ImageIO.read(in);
            if (atlas == null || atlas.getWidth() != cuadros.length * CELDA || atlas.getHeight() != CELDA)
                throw new IOException("Dimensiones inesperadas en " + ruta);
            for (int i = 0; i < cuadros.length; i++) {
                pixeles[i]=atlas.getSubimage(i * CELDA, 0, CELDA, CELDA);
                cuadros[i]=new PImage(pixeles[i]);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void dibujar(PGraphics g, int indice, float x, float pies, float alto, boolean espejo) {
        float escala = alto / ALTO;
        int cuadro = Math.clamp(indice, 0, cuadros.length - 1);
        g.pushStyle();
        g.imageMode(PGraphics.CORNER);
        g.pushMatrix();
        g.translate(x,pies);
        if (espejo) g.scale(-1,1);
        g.image(cuadros[cuadro], -CELDA * escala / 2, -APOYO * escala, CELDA * escala, CELDA * escala);
        g.popMatrix();
        g.popStyle();
    }

    public void dibujar(Graphics2D g,int indice,int x,int pies,int alto,boolean espejo) {
        float escala=alto/(float)ALTO;
        int lado=Math.round(CELDA*escala);
        int izq=x-lado/2, der=izq+lado, arriba=pies-Math.round(APOYO*escala);
        int cuadro=Math.clamp(indice,0,pixeles.length-1);
        g.drawImage(pixeles[cuadro],espejo?der:izq,arriba,espejo?izq:der,arriba+lado,
            0,0,CELDA,CELDA,null);
    }

    public static int paso(double reloj, double cuadrosPorUnidad, int longitud) {
        return Math.floorMod((long) Math.floor(reloj * cuadrosPorUnidad), longitud);
    }

    /** Cada pose tiene duración propia. El corte ya existe en el motor. */
    public static int corteJuan(float progreso, boolean reducido) {
        if (reducido) return progreso < .24f ? 1 : progreso < .73f ? 4 : 7;
        if (progreso < .10f) return 0;
        if (progreso < .18f) return 1;
        if (progreso < .24f) return 2;
        if (progreso < .33f) return 3;
        if (progreso < .55f) return 4;
        if (progreso < .69f) return 5;
        return progreso < .89f ? 6 : 7;
    }
}
