package alcaldedigital.cliente.interfaz;

import alcaldedigital.cliente.recursos.Recursos;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/** Encuadres del arte del juego para las tarjetas del selector. */
final class PortadasMisiones {
    static final int ANCHO = 270, ALTO = 101;
    private static final String SOFI = "mision1/sofi-reposo";
    private static final String PERIODISTA = "mision2/periodista-correr-hoja";
    private static final String INFLUENCER = "mision3/redisenio/player_";
    private static final String CANDIDATOS = "mision8/candidatos";

    private static final List<Portada> PORTADAS = List.of(
        new Portada("Sofi y Mr. Palomo", "mision1/parque", .49,
            cuadro(SOFI, 4, 1, 0, 58, 95, 61),
            cuadro("mision1/palomo", 4, 2, 0, 213, 87, 49)),
        new Portada("La red Civitas", "mision2/oficina", .51,
            cuadro(PERIODISTA, 4, 4, 0, 72, 96, 67)),
        new Portada("Civitas en Rojo", "mision3/redisenio/fondo-atrio", .43,
            sprite(INFLUENCER + "front_live", 65, 95, 67),
            sprite("mision3/redisenio/troll", 217, 92, 52)),
        new Portada("Juan en campaña", "mision4/plaza_presentacion", .40,
            sprite("mision4/speech", 83, 57, 46),
            sprite("mision4/vecino_0", 152, 94, 35),
            sprite("mision4/vecino_3", 197, 90, 35)),
        new Portada("El servidor crítico", "mision5/servidor-interior", .48,
            cuadro("mision5/servidor-ataques", 4, 3, 0, 138, 72, 49),
            cuadro(SOFI, 4, 1, 0, 68, 97, 50),
            cuadro(PERIODISTA, 4, 4, 13, 209, 96, 47)),
        new Portada("Cielos de campaña", "mision6/cielo", .50,
            sprite("mision6/helicoptero", 85, 67, 50),
            cuadro("mision6/dron_atlas", 5, 3, 1, 211, 60, 38)),
        new Portada("La carrera por la primicia", "mision7/avenida", .50,
            cuadro(PERIODISTA, 4, 4, 13, 84, 88, 61),
            sprite(INFLUENCER + "right_3", 166, 94, 65)),
        new Portada("El futuro de Civitas", "mision8/tarima-cuatro", .39,
            cuadro(CANDIDATOS, 8, 8, 0, 63, 71, 44),
            cuadro(CANDIDATOS, 8, 8, 16, 111, 71, 44),
            cuadro(CANDIDATOS, 8, 8, 32, 159, 71, 44),
            cuadro(CANDIDATOS, 8, 8, 48, 207, 71, 44))
    );

    private List<BufferedImage> miniaturas;

    int cantidad() { return PORTADAS.size(); }

    String titulo(int mision) { return PORTADAS.get(mision - 1).titulo(); }

    BufferedImage imagen(int mision) {
        if (miniaturas == null) {
            Recursos recursos = new Recursos();
            miniaturas = PORTADAS.stream().map(portada -> componer(recursos, portada)).toList();
        }
        return miniaturas.get(mision - 1);
    }

    private static BufferedImage componer(Recursos recursos, Portada portada) {
        BufferedImage miniatura = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = miniatura.createGraphics();
        try {
            TemaPixel.nitido(g);
            fondo(g, recursos.imagen(portada.fondo()), portada.centroVertical());
            g.setPaint(new GradientPaint(0, 28, new Color(19, 10, 37, 12),
                0, ALTO, new Color(19, 10, 37, 88)));
            g.fillRect(0, 0, ANCHO, ALTO);
            for (Figura figura : portada.figuras()) dibujar(g, recursos.imagen(figura.recurso()), figura);
        } finally {
            g.dispose();
        }
        return miniatura;
    }

    private static void fondo(Graphics2D g, BufferedImage fondo, double centroVertical) {
        int ancho = fondo.getWidth(), alto = fondo.getHeight();
        int corteX = Math.min(ancho, alto * ANCHO / ALTO);
        int corteY = Math.min(alto, ancho * ALTO / ANCHO);
        int x = (ancho - corteX) / 2;
        int y = Math.clamp((int) Math.round(alto * centroVertical - corteY / 2.0), 0, alto - corteY);
        g.drawImage(fondo, 0, 0, ANCHO, ALTO, x, y, x + corteX, y + corteY, null);
    }

    private static void dibujar(Graphics2D g, BufferedImage hoja, Figura figura) {
        int columna = figura.cuadro() % figura.columnas(), fila = figura.cuadro() / figura.columnas();
        int x0 = columna * hoja.getWidth() / figura.columnas();
        int y0 = fila * hoja.getHeight() / figura.filas();
        int x1 = (columna + 1) * hoja.getWidth() / figura.columnas();
        int y1 = (fila + 1) * hoja.getHeight() / figura.filas();
        int izq = x1, arriba = y1, der = x0, abajo = y0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                if ((hoja.getRGB(x, y) >>> 24) <= 32) continue;
                izq = Math.min(izq, x);
                arriba = Math.min(arriba, y);
                der = Math.max(der, x + 1);
                abajo = Math.max(abajo, y + 1);
            }
        }
        if (izq >= der || arriba >= abajo) throw new IllegalStateException("Cuadro vacío: " + figura.recurso());
        int ancho = Math.round(figura.alto() * (der - izq) / (float) (abajo - arriba));
        int x = figura.centroX() - ancho / 2, y = figura.baseY() - figura.alto();
        g.drawImage(hoja, x, y, x + ancho, figura.baseY(), izq, arriba, der, abajo, null);
    }

    private static Figura sprite(String recurso, int x, int y, int alto) {
        return cuadro(recurso, 1, 1, 0, x, y, alto);
    }

    private static Figura cuadro(String recurso, int columnas, int filas, int indice, int x, int y, int alto) {
        return new Figura(recurso, columnas, filas, indice, x, y, alto);
    }

    private record Portada(String titulo, String fondo, double centroVertical, Figura... figuras) {}

    private record Figura(String recurso, int columnas, int filas, int cuadro, int centroX, int baseY, int alto) {}
}
