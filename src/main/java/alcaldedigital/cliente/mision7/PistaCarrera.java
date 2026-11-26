package alcaldedigital.cliente.mision7;

import alcaldedigital.compartido.carrera.EstadoCarrera.Corredor;
import alcaldedigital.compartido.carrera.EstadoCarrera.Fase;
import alcaldedigital.compartido.carrera.EstadoCarrera.Objeto;
import alcaldedigital.compartido.carrera.EstadoCarrera.Vista;
import alcaldedigital.cliente.presentacion.LuzEscena;
import alcaldedigital.cliente.presentacion.PosesRemaster;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static alcaldedigital.servidor.carrera.MotorCarrera.META;
import static alcaldedigital.servidor.carrera.MotorCarrera.Y_MAX;
import static alcaldedigital.servidor.carrera.MotorCarrera.Y_MIN;

/** Proyección y dibujo de la pista a partir de la vista publicada por la carrera. */
final class PistaCarrera {
    static final int ORO = 0xfff4cb6a, CIAN = 0xff64ddd5;
    private static final int TINTA = 0xff22142f, PAPEL = 0xfff5e8fb, PELIGRO = 0xffff82ad;
    private static final int ANCHO = 1280, BORDE_SUPERIOR = 245, BORDE_INFERIOR = 502;
    private static final float TRAMO = META / 4, ANCHO_FACHADA = 1200;
    private static final float ALCANCE_DISTRACCION = 36;
    private static final String[] TRAMOS = {"Nova Prensa", "Avenida Civitas", "Pasaje Electoral", "Parque de Ciudad Nova"};
    private static final int[] ACENTOS = {ORO, CIAN, 0xffe3a3ff, 0xffa5e4a5};
    private static final int[] PISO = {0xff725881, 0xff655981, 0xff755878, 0xff555875};
    private static final int[] ICONOS = {0, 0, 12, 0, 4, 8};
    private final PApplet p;
    private final ArteCarrera arte;
    private final boolean reducido, contraste;
    private final PImage[] fachadas = new PImage[3], veredas = new PImage[3];
    private final PImage arboles, jardines;
    private final List<Figura> figuras = new ArrayList<>();
    private float izquierda, escala = 1;
    private long tickAnterior = -1;

    PistaCarrera(PApplet p, ArteCarrera arte, boolean reducido, boolean contraste) {
        this.p = p;
        this.arte = arte;
        this.reducido = reducido;
        this.contraste = contraste;
        int[] inicio = {0, 850, 335}, ancho = {940, 1100, 1070};
        for (int i = 0; i < fachadas.length; i++) {
            fachadas[i] = arte.avenida.get(inicio[i], 0, ancho[i], 282);
            veredas[i] = arte.avenida.get(inicio[i], 512, ancho[i], arte.avenida.height - 512);
        }
        arboles = arte.parque.get(0, 0, arte.parque.width, arte.parque.height * 3 / 9);
        jardines = arte.parque.get(0, arte.parque.height * 6 / 9, arte.parque.width, arte.parque.height / 3);
    }

    static int colorRol(int rol) { return rol == 0 ? ORO : CIAN; }
    static String distintivo(int rol) { return rol == 0 ? "P" : "I"; }
    static String nombreTramo(float distancia) { return TRAMOS[tramo(distancia)]; }

    private static int tramo(float distancia) {
        return PApplet.constrain((int) (distancia / TRAMO), 0, TRAMOS.length - 1);
    }

    void reiniciar() { tickAnterior = -1; }

    void dibujar(Vista vista, int rolLocal, boolean dosLocales, boolean pausada) {
        Corredor[] corredores = vista.corredores();
        encuadrar(vista.tick(), corredores, rolLocal, dosLocales, pausada);
        p.noStroke();
        decorado();
        iluminacionPista(vista.tick());
        for (int rol = 0; rol < corredores.length; rol++) {
            Corredor c = corredores[rol];
            if (c.avisoAtaque() > 0) distraccion(c);
        }
        figuras.clear();
        for (Objeto objeto : vista.objetos()) {
            float x = pantallaX(objeto.x());
            if (x < -72 || x > ANCHO + 72) continue;
            int disponibles = (dosLocales ? 3 : 1 << rolLocal) & ~objeto.consumido();
            if (disponibles != 0) figuras.add(new Figura(objeto.y(), -1, objeto, disponibles));
        }
        for (int rol = 0; rol < corredores.length; rol++) {
            figuras.add(new Figura(corredores[rol].y(), rol, null, 0));
        }
        figuras.sort(Comparator.comparingDouble(Figura::y));
        for (Figura figura : figuras) {
            if (figura.objeto() != null) {
                objeto(figura.objeto(), vista.grupos(), figura.disponibles(), vista.tick(), dosLocales);
            } else {
                int rol = figura.rol();
                corredor(corredores[rol], rol, vista.fase() == Fase.CUENTA, dosLocales || rol == rolLocal);
            }
        }
        for (int rol = 0; rol < corredores.length; rol++) marcador(corredores[rol], rol);
        for (int rol = 0; rol < corredores.length; rol++) {
            Corredor c = corredores[rol];
            if (c.avisoAtaque() > 0) avisoDistraccion(c, rol);
        }
    }

    private void encuadrar(long tick, Corredor[] corredores, int rolLocal, boolean dosLocales, boolean pausada) {
        float menor = Math.min(corredores[0].distancia(), corredores[1].distancia());
        float mayor = Math.max(corredores[0].distancia(), corredores[1].distancia());
        float objetivoEscala = Math.min(1, 775 / Math.max(1, mayor - menor));
        if (!dosLocales) objetivoEscala = Math.max(.58f, objetivoEscala);
        float ancla = dosLocales ? menor : Math.max(menor, corredores[rolLocal].distancia() - 775 / objetivoEscala);
        float objetivoIzquierda = ancla - 200 / objetivoEscala;
        if (tickAnterior < 0 || tick < tickAnterior) {
            escala = objetivoEscala;
            izquierda = objetivoIzquierda;
        } else if (tick > tickAnterior && !pausada) {
            float mezcla = reducido ? 1 : 1 - (float) Math.pow(.9, Math.min(12, tick - tickAnterior));
            escala = PApplet.lerp(escala, objetivoEscala, mezcla);
            izquierda = PApplet.lerp(izquierda, objetivoIzquierda, mezcla);
            if (dosLocales) {
                escala = Math.min(escala, 1040 / Math.max(1, mayor - menor));
                izquierda = PApplet.constrain(izquierda, mayor - 1130 / escala, menor - 90 / escala);
            } else {
                float local = corredores[rolLocal].distancia();
                izquierda = PApplet.constrain(izquierda, local - 1020 / escala, local - 90 / escala);
            }
        }
        tickAnterior = tick;
    }

    private float pantallaX(float x) { return Math.round((x - izquierda) * escala); }

    private static float suelo(float y) {
        return PApplet.map(y, Y_MIN, Y_MAX, BORDE_SUPERIOR + 10, BORDE_INFERIOR - 14);
    }

    private void decorado() {
        float derecha = izquierda + ANCHO / escala;
        int primero = (int) Math.floor(izquierda / ANCHO_FACHADA);
        int ultimo = (int) Math.floor(derecha / ANCHO_FACHADA);
        for (int bloque = primero; bloque <= ultimo; bloque++) {
            float mundo = bloque * ANCHO_FACHADA;
            int zona = tramo(mundo);
            float x = pantallaX(mundo), ancho = pantallaX(mundo + ANCHO_FACHADA) - x;
            p.image(zona == 3 ? arboles : fachadas[zona], x, 0, ancho, BORDE_SUPERIOR);
            p.image(zona == 3 ? jardines : veredas[zona], x, BORDE_INFERIOR, ancho, 218);
            rect(x, BORDE_SUPERIOR, ancho, BORDE_INFERIOR - BORDE_SUPERIOR, contraste ? 0xff3e314c : PISO[zona]);
        }
        for (int y = BORDE_SUPERIOR + 18, fila = 0; y < BORDE_INFERIOR - 9; y += 22, fila++) {
            rect(0, y, ANCHO, 1, 0x1b21112f);
            int anchoBaldosa = Math.max(20, Math.round(78 * escala));
            float inicio = pantallaX((float) Math.floor(izquierda / 78) * 78) + (fila % 2) * anchoBaldosa / 2f;
            for (float x = inicio - anchoBaldosa; x < ANCHO; x += anchoBaldosa) {
                rect(x, y, 2, 22, 0x1921112f);
                rect(x + 8, y + 5, 7, 2, 0x14e4c5f5);
            }
        }
        rect(0, BORDE_SUPERIOR, ANCHO, 4, 0xff332447);
        rect(0, BORDE_INFERIOR - 4, ANCHO, 4, 0xff332447);
        rect(0, BORDE_SUPERIOR + 4, ANCHO, 2, 0xffcaaed2);
        rect(0, BORDE_INFERIOR - 6, ANCHO, 2, 0xffcaaed2);
        float calle = (Y_MAX - Y_MIN) / 3;
        for (float tramoCorto = (float) Math.floor(izquierda / 128) * 128; tramoCorto <= derecha; tramoCorto += 128) {
            float x = pantallaX(tramoCorto);
            float ancho = Math.max(3, pantallaX(tramoCorto + 43) - x);
            rect(x, suelo(Y_MIN + calle), ancho, 3, 0x9be8cbe9);
            rect(x, suelo(Y_MIN + calle * 2), ancho, 3, 0x9be8cbe9);
        }
        for (int zona = 0; zona < TRAMOS.length; zona++) {
            float x = pantallaX(zona * TRAMO + (zona == 0 ? 175 : 305));
            if (x > -135 && x < ANCHO + 135) cartel(TRAMOS[zona], x, 195, ACENTOS[zona]);
        }
        for (float mundo = Math.max(420, (float) Math.ceil((izquierda - 50) / 460) * 460); mundo < Math.min(derecha + 50, META - 400); mundo += 460) {
            int i = (int) (mundo / 460);
            p.tint(0xffa99abc);
            sprite((i % 2 == 0 ? arte.antiguosA : arte.antiguosB)[(i % 4) * 4 + 2], pantallaX(mundo), BORDE_SUPERIOR - 6, 50);
            p.noTint();
        }
        linea(0, "Salida", ORO);
        linea(META, "Meta · Parque", ACENTOS[3]);
    }

    private void iluminacionPista(long tick) {
        float tiempo = reducido ? 0 : tick / 60f;
        float derecha = izquierda + ANCHO / escala;
        float inicio = (float) Math.floor((izquierda - 250) / 320) * 320;
        for (float mundo = inicio; mundo <= derecha + 250; mundo += 320) {
            float x = pantallaX(mundo);
            if (x < -190 || x > ANCHO + 190) continue;
            int tramo = tramo(mundo);
            var luz = switch (tramo) {
                case 0 -> LuzEscena.AMBAR;
                case 1 -> LuzEscena.CIAN;
                case 2 -> LuzEscena.MAGENTA;
                default -> LuzEscena.VIOLETA;
            };
            float brillo = contraste ? 82 : 142;
            luz.dibujar(p.g,x,BORDE_SUPERIOR-18,290*escala,142,brillo);
            luz.dibujar(p.g,x,BORDE_SUPERIOR+32,183*escala,83,brillo*.55f);
            if (tramo != 3) continue;
            for (int i = 0; i < 3; i++) {
                float fase = (tiempo * .16f + i * .34f) % 1;
                float yy = BORDE_SUPERIOR - 106 + fase * 88;
                float xx = x + (i-1)*54 + 18 * PApplet.sin(fase * PApplet.TWO_PI);
                p.fill(0xffd8b9ef,70 * PApplet.sin(fase * PApplet.PI));
                p.rect(xx,yy,4,2);
            }
        }
    }

    private void linea(float mundo, String nombre, int color) {
        float x = pantallaX(mundo);
        if (x < -165 || x > ANCHO + 165) return;
        int celda = 12;
        float ancho = Math.max(6, 24 * escala);
        for (int y = BORDE_SUPERIOR + 6, fila = 0; y < BORDE_INFERIOR - 6; y += celda, fila++) {
            rect(x - ancho, y, ancho, Math.min(celda, BORDE_INFERIOR - 6 - y), fila % 2 == 0 ? color : TINTA);
            rect(x, y, ancho, Math.min(celda, BORDE_INFERIOR - 6 - y), fila % 2 == 0 ? TINTA : color);
        }
        if (mundo == META) {
            rect(x - 98, 183, 12, 62, TINTA);
            rect(x + 86, 183, 12, 62, TINTA);
            rect(x - 98, 183, 4, 62, color);
            rect(x + 94, 183, 4, 62, color);
        }
        cartel(nombre, x, mundo == META ? 182 : 174, color);
    }

    private void cartel(String nombre, float x, float y, int color) {
        p.textSize(18);
        float ancho = p.textWidth(nombre) + 26;
        rect(x - ancho / 2 - 2, y - 22, ancho + 4, 31, TINTA);
        rect(x - ancho / 2, y + 5, ancho, 3, color);
        textoCentrado(nombre, x, y, 18, PAPEL);
    }

    private void distraccion(Corredor c) {
        float y = suelo(Math.max(Y_MIN, c.objetivoAtaque() - ALCANCE_DISTRACCION));
        float fin = suelo(Math.min(Y_MAX, c.objetivoAtaque() + ALCANCE_DISTRACCION));
        rect(0, y, ANCHO, fin - y, 0x6f681b52);
        p.stroke(0xabff82ad);
        p.strokeWeight(3);
        for (int x = -80; x < ANCHO; x += 25) p.line(x, fin - 2, x + (fin - y) * .70f, y + 2);
        p.noStroke();
        rect(0, y, ANCHO, 2, PELIGRO);
        rect(0, fin - 2, ANCHO, 2, PELIGRO);
    }

    private void avisoDistraccion(Corredor c, int rol) {
        float y = suelo(Math.max(Y_MIN, c.objetivoAtaque() - ALCANCE_DISTRACCION));
        float x = PApplet.constrain(pantallaX(c.distancia()) + (rol == 0 ? -150 : 150), 125, ANCHO - 125);
        float etiquetaY = y - 7 - rol * 28;
        rect(x - 122, etiquetaY - 22, 244, 28, TINTA);
        String restante = ((int) Math.ceil(c.avisoAtaque() / 6f) / 10f + " s").replace('.', ',');
        textoCentrado(distintivo(rol) + " · CAMBIA DE CARRIL · " + restante, x, etiquetaY - 3, 15, PELIGRO);
        rect(x - 122, etiquetaY + 3, 244 * Math.min(1, c.avisoAtaque() / 60f), 3, PELIGRO);
    }

    private void objeto(Objeto objeto, int[] grupos, int disponibles, long tick, boolean dosLocales) {
        float x = pantallaX(objeto.x()), y = suelo(objeto.y());
        int tipo = objeto.tipo();
        int cuadro = reducido ? 0 : Math.floorMod(tick / 9 + objeto.id(), 4);
        if (tipo == 1) {
            int estado = grupos[objeto.grupo()];
            int color = estado == 2 ? PELIGRO : estado == 1 ? ORO : CIAN;
            halo(x, y, 38, color);
            int fila = objeto.id() % 2 * 3 + (estado == 2 ? 2 : estado == 1 ? 1 : 0);
            sprite(arte.ciudadanos[fila * 4 + cuadro], x, y, 72);
            if (estado == 1 || estado == 2) ficha("!", x, y - 80, color);
        } else if (tipo == 2) {
            halo(x, y, 58, PELIGRO);
            sprite(arte.objetos[ICONOS[tipo] + cuadro], x, y, 62);
        } else {
            int color = tipo == 3 ? ORO : tipo == 4 ? CIAN : 0xffe3a3ff;
            p.fill(TINTA);
            p.stroke(color);
            p.strokeWeight(2);
            if (tipo == 3) p.rect(x - 23, y - 43, 46, 38, 7);
            else if (tipo == 4) {
                p.beginShape();
                p.vertex(x - 24, y - 45);
                p.vertex(x + 24, y - 45);
                p.vertex(x + 17, y - 12);
                p.vertex(x, y + 1);
                p.vertex(x - 17, y - 12);
                p.endShape(PApplet.CLOSE);
            }
            else p.ellipse(x, y - 24, 49, 44);
            p.noStroke();
            sprite(arte.objetos[ICONOS[tipo] + cuadro], x, y - 3, 42);
        }
        if (dosLocales && disponibles != 3) {
            int rol = disponibles == 1 ? 0 : 1;
            ficha(distintivo(rol), x + 23, y + 10, colorRol(rol));
        }
    }

    private void corredor(Corredor c, int rol, boolean cuenta, boolean local) {
        float x = pantallaX(c.distancia()), y = suelo(c.y());
        if (x < -65 || x > ANCHO + 65) return;
        int color = colorRol(rol);
        halo(x, y, 58, local ? color : 0xffb39cbe);
        if (c.turbo() > 0) {
            if (reducido) ficha("+", x - 40, y - 29, ORO);
            else for (int i = 0; i < 3; i++) rect(x - 76 + i * 9, y - 25 - i * 13, 28, 3, ORO);
        }
        if (c.escudo() > 0) {
            p.noFill();
            p.stroke(CIAN);
            p.strokeWeight(3);
            p.ellipse(x, y - 46, 78, 105);
            p.noStroke();
        }
        boolean parado = cuenta || c.velocidad() <= 0;
        int cuadro = parado || reducido ? 0 : (int) (c.distancia() / 12) % 4;
        if (!parado && c.velocidad()>=2.35f) {
            var hoja = rol==0 ? PosesRemaster.PERIODISTA : PosesRemaster.INFLUENCER;
            int fotograma = reducido ? 0 : PosesRemaster.paso(c.distancia(),rol==0?.08:.04,rol==0?8:4);
            hoja.dibujar(p.g,fotograma,x,y,82,false);
        } else {
            PImage pose = rol==0 ? arte.marchaPeriodista[12+cuadro] : parado ? arte.influencerQuieto : arte.andar[rol][12+cuadro];
            sprite(pose,x,y,90);
        }
        if (c.frenado() > 0) {
            rect(x - 22, y + 7, 44, 15, TINTA);
            textoCentrado("FRENO", x, y + 19, 12, PELIGRO);
        }
    }

    private void marcador(Corredor c, int rol) {
        float x = pantallaX(c.distancia());
        if (x < 22 || x > ANCHO - 22) {
            boolean delante = x > ANCHO / 2f;
            String indicador = delante ? distintivo(rol) + " >" : "< " + distintivo(rol);
            float lado = delante ? ANCHO - 28 : 28;
            rect(lado - 22, suelo(c.y()) - 42, 44, 24, TINTA);
            textoCentrado(indicador, lado, suelo(c.y()) - 24, 16, colorRol(rol));
        } else {
            ficha(distintivo(rol), x + (rol == 0 ? -17 : 17), suelo(c.y()) - 97, colorRol(rol));
        }
    }

    private void ficha(String texto, float x, float y, int color) {
        rect(x - 11, y - 15, 22, 21, TINTA);
        rect(x - 11, y + 4, 22, 2, color);
        textoCentrado(texto, x, y, 16, color);
    }

    private void halo(float x, float y, int ancho, int color) {
        p.fill(TINTA, 210);
        p.ellipse(x, y + 1, ancho, 12);
        rect(x - ancho / 2f + 5, y + 3, ancho - 10, 2, color);
    }

    private void rect(float x, float y, float w, float h, int color) {
        p.fill(color);
        p.rect(x, y, w, h);
    }

    private void sprite(PImage im, float x, float suelo, float alto) {
        float ancho = alto * im.width / im.height;
        p.image(im, x - ancho / 2, suelo - alto, ancho, alto);
    }

    private void textoCentrado(String texto, float x, float y, int tam, int color) {
        p.textAlign(PApplet.CENTER, PApplet.BASELINE);
        p.fill(color);
        p.textSize(tam);
        p.text(texto, x, y);
        p.textAlign(PApplet.LEFT, PApplet.BASELINE);
    }

    private record Figura(float y, int rol, Objeto objeto, int disponibles) {}
}
