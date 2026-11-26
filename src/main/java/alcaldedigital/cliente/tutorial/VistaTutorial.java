package alcaldedigital.cliente.tutorial;

import alcaldedigital.cliente.entrada.MapaControles;
import alcaldedigital.cliente.interfaz.TemaPixel;
import alcaldedigital.cliente.recursos.Recursos;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/** Dibuja la práctica en el mismo lienzo lógico que el selector de misiones. */
public final class VistaTutorial {
    private static final Color FONDO = new Color(22, 15, 34), CUADRO = new Color(40, 26, 57);
    private static final Color CLARO = new Color(249, 231, 252), TENUE = new Color(216, 184, 231);
    private static final Color ORO = new Color(244, 203, 106), CIAN = new Color(100, 221, 213);
    private static final Color ROSA = new Color(250, 128, 174), LINEA = new Color(104, 73, 132);
    private static final String SOFI = "mision1/sofi-reposo", PALOMO = "mision1/palomo";
    private static final String PERIODISTA = "mision2/periodista-correr-hoja";
    private static final String INFLUENCER = "mision3/redisenio/player_right_";
    private final Recursos recursos;
    private final TemaPixel tema;
    private final Map<String, BufferedImage> sprites = new HashMap<>();

    public VistaTutorial(TemaPixel tema, Recursos recursos) {
        this.tema = tema;
        this.recursos = recursos;
    }

    public void dibujar(Graphics2D destino, Tutorial tutorial, boolean contraste, boolean reducido) {
        Graphics2D g = (Graphics2D) destino.create();
        try {
            Color fondo = contraste ? Color.BLACK : FONDO;
            Color borde = contraste ? Color.WHITE : LINEA;
            EjercicioTutorial e = tutorial.ejercicio();
            rect(g, 0, 0, 1280, 720, fondo);
            texto(g, "Práctica de controles", 44, 62, 34, CLARO, true);
            texto(g, "Misión " + e.mision + " de 8  ·  " + MapaControles.nombreMision(e.mision), 46, 93, 19, ORO, true);
            for (int n = 1; n <= 8; n++) rect(g, 46 + (n - 1) * 27, 106, 22, 4, n < e.mision ? CIAN : n == e.mision ? ORO : borde);
            omitir(g, tutorial, fondo, borde);
            marco(g, 38, 124, 1204, 382, fondo, borde);
            Graphics2D mundo = (Graphics2D) g.create(41, 127, 1198, 376);
            try {
                mundo.translate(-41, -127);
                arena(mundo, tutorial, contraste, reducido);
            } finally { mundo.dispose(); }
            dialogo(g, tutorial, contraste ? Color.BLACK : CUADRO, borde);
        } finally { g.dispose(); }
    }

    private void omitir(Graphics2D g, Tutorial t, Color fondo, Color borde) {
        Rectangle r = Tutorial.botonOmitir();
        marco(g, r.x,r.y,r.width,r.height,fondo,borde);
        texto(g, "Mantén para omitir", r.x + 13, r.y + 23, 18, CLARO, true);
        texto(g, t.omitirCon(), r.x + 13, r.y + 43, 16, ORO, false);
        rect(g, r.x + 8, r.y + r.height - 8, r.width - 16, 4, borde);
        rect(g, r.x + 8, r.y + r.height - 8, (int) ((r.width - 16) * t.progresoOmision()), 4, ORO);
    }

    private void arena(Graphics2D g, Tutorial t, boolean contraste, boolean reducido) {
        EjercicioTutorial e = t.ejercicio();
        g.setPaint(new GradientPaint(0, 130, contraste ? Color.BLACK : new Color(23, 15, 36), 0, 502,
            contraste ? new Color(18, 18, 18) : new Color(37, 25, 55)));
        g.fillRect(41, 127, 1198, 376);
        if (e.mision == 7) carretera(g,t, reducido);
        else if (e.mision == 6) cielo(g,t,reducido);
        else suelo(g, e.mision == 8);
        elementos(g, t);
        double vuelo = e == EjercicioTutorial.INICIO && !reducido ? Math.min(1, t.tiempoPaso() / .95) : 1;
        int palomoX = e.mision == 1 ? (int) (751 + 550 * (1 - vuelo)) : 1037;
        int palomoY = e.mision == 1 ? (int) (375 - 184 * (1 - vuelo)) : 219;
        if(vuelo==1) rect(g,palomoX - 23,palomoY + 1,46,3,new Color(16,8,24,124));
        dibujarSprite(g, celda(PALOMO,4,2,vuelo<1 ? 4 + (int) (t.reloj() * 8) % 4 : 1),
            palomoX,palomoY,e.mision == 1 ? 92 : 72,true);
        jugador(g,t,reducido);
        efectos(g,t);
        if (t.eligiendo()) opciones(g,t,contraste);
        else if (!t.respuesta().isBlank()) texto(g,t.respuesta(),69,478,17,ROSA,true);
        if (t.acierto()) {
            marco(g, 493, 144, 294, 35, FONDO, CIAN);
            centrado(g,"Ejercicio completado",640,168,18,CIAN,true);
        }
    }

    private void suelo(Graphics2D g, boolean lateral) {
        Color piso = new Color(150, 108, 190, 32);
        for (int y : new int[]{271, 311, 358, 414, 481}) rect(g, 65, y, 1150, 1, piso);
        if (lateral) rect(g, 100, 402, 1080, 3, new Color(154, 123, 190, 75));
        else {
            g.setColor(piso);
            for (int x = -270; x <= 1550; x += 130) g.drawLine(640 + (x - 640) / 4, 241, x, 500);
        }
    }

    private void carretera(Graphics2D g, Tutorial t, boolean reducido) {
        rect(g, 41, 264, 1198, 182, new Color(79, 56, 96));
        rect(g, 41, 264, 1198, 3, TENUE);
        rect(g, 41, 443, 1198, 3, TENUE);
        int desplazamiento = reducido ? 0 : (int) (t.tiempoPaso() * (140 + t.progresoCarrera() * 220)) % 110;
        for (int x = 43 - desplazamiento; x < 1240; x += 110) rect(g, x, 354, 56, 3, new Color(164, 132, 185));
        if (t.ejercicio() == EjercicioTutorial.ACELERAR) {
            rect(g, 432, 220, 404, 10, FONDO);
            rect(g, 432, 220, (int) (404 * t.progresoCarrera()), 10, CIAN);
        }
        if (t.ejercicio() == EjercicioTutorial.CARRIL) {
            dibujarSprite(g, celda("mision7/objetos",4,4,12), (int) t.vallaCarreraX(), 427, 63, false);
            marca(g, 550, 312, CIAN);
        }
    }

    private void cielo(Graphics2D g, Tutorial t, boolean reducido) {
        int desplazamiento = reducido ? 0 : (int) (t.reloj() * 28) % 170;
        for (int x = -140 - desplazamiento, n = 0; x < 1250; x += 170, n++) {
            int y = 229 + (n % 4) * 56;
            rect(g, x, y, 72, 3, new Color(172, 152, 219, 43));
            rect(g, x + 15, y - 5, 28, 3, new Color(172, 152, 219, 43));
        }
    }

    private void elementos(Graphics2D g, Tutorial t) {
        switch (t.ejercicio()) {
            case ACERCARSE, LATERAL -> marca(g, 645, t.ejercicio() == EjercicioTutorial.LATERAL ? 399 : 375, t.acierto() ? ORO : CIAN);
            case PAPEL, LUPA -> dibujarSprite(g,imagen("iconos/12_contexto"),532,382,73,false);
            case LIBRETA -> dibujarSprite(g,imagen("iconos/04_libreta"),532,382,76,false);
            case CORRER -> { rect(g, 364, 256, 390, 8, FONDO);rect(g, 364, 256, (int) (390 * t.progresoCarrera()), 8, CIAN);marca(g, 620, 398, ORO); }
            case SALTO_TERMINAL -> dibujarSprite(g, celda("mision7/objetos",4,4,12), 561, 405, 63, false);
            case REPARAR -> terminal(g, 724, 388, t.acierto());
            case PING, FLASH -> dibujarSprite(g, imagen("mision3/redisenio/troll"), 694, 401, 103, true);
            case ATAQUE, GOLPE_FINAL, ALCANCE -> blanco(g, 698, 362, t.acierto());
            case ESCUDO, PROTECCION -> { blanco(g, 720, 357, false);zona(g, 357, 430, 403, .35); }
            case DASH, ESQUIVA_PLAZA, ESQUIVA_FINAL -> zona(g, 395, 541, 407, t.acierto() ? .28 : .52);
            case SALTO_PULSO -> zona(g, 536, 587, 402, .73);
            case PILOTAR -> {g.setColor(CIAN);g.drawOval(616, 245, 72, 67);g.drawOval(621, 250, 62, 57);}
            case IMPULSO -> zona(g, 473, 541, 417, .43);
            case DISPARO, MISIL -> dibujarSprite(g, celda("mision6/dron_atlas",5,3,1), 734, 317, 75, true);
            case DOBLE_SALTO -> {rect(g, 295, 240, 184, 6, LINEA);rect(g, 325, 245, 124, 3, CIAN);}
            case CIERRE -> marca(g, 638, 398, ORO);
            default -> { }
        }
    }

    private void jugador(Graphics2D g, Tutorial t, boolean reducido) {
        int mision = t.ejercicio().mision;
        int fase = reducido ? 0 : (int) (t.reloj() * 10) % 4;
        int x = (int) t.x(), y = (int) t.y();
        BufferedImage cuadro = switch (mision) {
            case 2 -> celda(PERIODISTA,4,4, t.moviendo() ? 12 + fase : 0);
            case 3 -> imagen(INFLUENCER + (t.moviendo() ? Integer.toString(fase + 1) : "idle"));
            case 4 -> imagen(t.moviendo() ? "mision4/walk_right_1" : "mision4/front");
            case 6 -> imagen("mision6/helicoptero");
            case 7 -> imagen(INFLUENCER + (fase + 1));
            case 8 -> celda("mision8/heroes",8,8, t.moviendo() ? 1 + fase % 3 : 0);
            default -> t.moviendo() ? celda("mision1/sofi-caminar",4,4,12 + fase) : celda(SOFI,4,1,0);
        };
        if (mision != 6) {
            g.setColor(new Color(8, 5, 15, 155));
            g.fillOval(x - 30,y - 4,60,12);
        }
        dibujarSprite(g, cuadro, x, y - (int) t.altura() + (mision == 6 ? 30 : 0), mision == 6 ? 76 : 96, t.izquierda());
    }

    private void efectos(Graphics2D g, Tutorial t) {
        if (!t.acierto()) return;
        int x = (int) t.x(), y = (int) (t.y() - t.altura()) - 44;
        int tiro = x + (int) ((700 - x) * Math.min(1, t.efecto() * 1.45));
        switch (t.ejercicio()) {
            case LINTERNA -> {
                g.setColor(new Color(244,203,106,105));
                g.fillPolygon(new Polygon(new int[]{x,735,735},new int[]{y,y-64,y+64},3));
                blanco(g,715,y,true);
            }
            case PING, ATAQUE, GOLPE_FINAL, ALCANCE, DISPARO, MISIL -> {
                rect(g, tiro - 32, y - 3, 51, 7, t.ejercicio() == EjercicioTutorial.MISIL ? ROSA : CIAN);
                rect(g, tiro, y - 2, 19, 4, CLARO);
            }
            case FLASH, ESCUDO, PROTECCION -> {
                int radio = 49 + (int) (t.efecto() * 28);
                g.setColor(t.ejercicio() == EjercicioTutorial.FLASH ? ORO : CIAN);
                g.drawOval(x - radio, y - radio, 2 * radio, 2 * radio);
                g.drawOval(x - radio + 4, y - radio + 4, 2 * radio - 8, 2 * radio - 8);
                if (t.ejercicio() != EjercicioTutorial.FLASH) rect(g, 509, y, Math.max(0, 200 - (int) (t.efecto() * 250)), 4, ROSA);
            }
            case DASH, IMPULSO, ESQUIVA_PLAZA, ESQUIVA_FINAL -> {
                for (int n = 0; n < 4; n++) rect(g, x - 80 - n * 10, y + n * 10, 48, 3, CIAN);
            }
            default -> { }
        }
    }

    private void opciones(Graphics2D g, Tutorial t, boolean contraste) {
        EjercicioTutorial e = t.ejercicio();
        Color panel = contraste ? Color.BLACK : FONDO;
        marco(g, 241,236,798,238,panel,CIAN);
        texto(g, switch(e){ case CONTRASTAR -> "Civitas de práctica"; case SERVIDOR -> "Aviso del servidor"; default -> "Noticia durante la carrera"; }, 264,277,25,ORO,true);
        texto(g, switch(e){ case CONTRASTAR -> "«Cierran el parque porque la fuente aprendió a hablar»";
            case SERVIDOR -> "«La alerta viene de una cuenta sin identificar»"; default -> "«Hay un atajo al parque. Comparte el mensaje para verlo»"; }, 264,321,19,CLARO,false);
        for (int n = 0; n < e.opciones().length; n++) {
            Rectangle r = Tutorial.opcion(n);
            Color borde = n == t.seleccion() ? ORO : LINEA;
            marco(g,r.x,r.y,r.width,r.height,n == t.seleccion() ? ORO : panel,borde);
            String prefijo = t.dispositivo().isBlank() && e != EjercicioTutorial.CONTRASTAR ? (n+1) + " · " : "";
            centrado(g,prefijo+e.opciones()[n],(int) r.getCenterX(),r.y+36,17,n == t.seleccion() ? FONDO : CLARO,true);
        }
        if (!t.respuesta().isBlank()) texto(g,t.respuesta(),264,451,17,ROSA,false);
    }

    private void dialogo(Graphics2D g, Tutorial t, Color fondo, Color borde) {
        EjercicioTutorial e = t.ejercicio();
        marco(g,38,528,1204,150,fondo,borde);
        marco(g,56,548,110,107,FONDO,LINEA);
        dibujarSprite(g,celda(PALOMO,4,2,0),111,646,85,false);
        texto(g,"MR. PALOMO",184,564,18,ORO,true);
        parrafo(g,e.frase,184,592,728,19,CLARO);
        rect(g,184,622,1017,1,borde);
        texto(g,e.objetivo,184,653,19,CLARO,true);
        if (t.presentaBoton()) {
            Rectangle r = Tutorial.botonContinuar();
            marco(g,r.x,r.y,r.width,r.height,ORO,CLARO);
            centrado(g,e==EjercicioTutorial.INICIO ? "Empezar" : "Elegir misión", (int) r.getCenterX(),r.y+31,19,FONDO,true);
        }
        String pista = e.indicacion(t.dispositivo());
        Font fuente = tema.fuente(19,true);
        int anchoPista = Math.max(94,tema.ancho(g,pista,fuente)+30);
        marco(g,1202-anchoPista,629,anchoPista,34,FONDO,CIAN);
        centrado(g,pista,1202-anchoPista/2,652,19,CIAN,true);
    }

    private void marca(Graphics2D g, int x, int y, Color color) {
        g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),40));g.fillOval(x-35,y-18,70,36);
        g.setColor(color);g.drawOval(x-35,y-18,70,36);
        g.fillPolygon(new Polygon(new int[]{x-8,x+8,x},new int[]{y-40,y-40,y-29},3));
    }

    private void terminal(Graphics2D g, int x, int y, boolean reparada) {
        marco(g,x-41,y-59,82,53,CUADRO,reparada ? CIAN : ROSA);
        rect(g,x-22,y-43,44,18,reparada ? CIAN : ROSA);
        rect(g,x-12,y-5,24,12,TENUE);
    }

    private void blanco(Graphics2D g, int x, int y, boolean golpeado) {
        Color color = golpeado ? CIAN : ORO;
        rect(g,x-3,y,6,37,LINEA);
        g.setColor(color);g.fillOval(x-30,y-30,60,60);
        g.setColor(FONDO);g.fillOval(x-21,y-21,42,42);
        g.setColor(color);g.fillOval(x-11,y-11,22,22);
    }

    private void zona(Graphics2D g, int desde, int hasta, int base, double intensidad) {
        Color aviso = new Color(250,128,174,(int) (255*intensidad));
        rect(g,desde,base-41,hasta-desde,52,aviso);
        rect(g,desde,base-41,hasta-desde,2,ROSA);
        for (int x=desde+4;x<hasta-6;x+=12) rect(g,x,base+5,6,5,ROSA);
    }

    private BufferedImage imagen(String recurso) { return celda(recurso,1,1,0); }

    private BufferedImage celda(String recurso,int columnas,int filas,int cuadro) {
        return sprites.computeIfAbsent(recurso+":"+cuadro,k->{
            BufferedImage hoja = recursos.imagen(recurso);
            int columna = cuadro % columnas, fila = cuadro / columnas;
            int x0 = columna * hoja.getWidth() / columnas, y0 = fila * hoja.getHeight() / filas;
            int x1 = (columna+1) * hoja.getWidth() / columnas, y1 = (fila+1) * hoja.getHeight() / filas;
            int izq=x1, arriba=y1, der=x0, abajo=y0;
            for (int y=y0;y<y1;y++) for (int x=x0;x<x1;x++) if ((hoja.getRGB(x,y) >>> 24) > 32) {
                izq=Math.min(izq,x);der=Math.max(der,x+1);arriba=Math.min(arriba,y);abajo=Math.max(abajo,y+1);
            }
            return izq < der && arriba < abajo ? hoja.getSubimage(izq,arriba,der-izq,abajo-arriba) : hoja.getSubimage(x0,y0,x1-x0,y1-y0);
        });
    }

    private static void dibujarSprite(Graphics2D g, BufferedImage imagen, int x, int y, int alto, boolean espejo) {
        int ancho = alto * imagen.getWidth() / imagen.getHeight(), izq = x - ancho / 2;
        g.drawImage(imagen,izq,y-alto,izq+ancho,y,espejo?imagen.getWidth():0,0,espejo?0:imagen.getWidth(),imagen.getHeight(),null);
    }

    private static void rect(Graphics2D g,int x,int y,int w,int h,Color c) { g.setColor(c);g.fillRect(x,y,w,h); }
    private static void marco(Graphics2D g,int x,int y,int w,int h,Color fondo,Color borde) { TemaPixel.marco(g,x,y,w,h,fondo,borde,false); }

    private void texto(Graphics2D g,String s,int x,int y,int tam,Color color,boolean negrita) {
        g.setColor(color);tema.escribir(g,s,x,y,tema.fuente(tam,negrita));
    }

    private void centrado(Graphics2D g,String s,int x,int y,int tam,Color color,boolean negrita) {
        texto(g,s,x-tema.ancho(g,s,tema.fuente(tam,negrita))/2,y,tam,color,negrita);
    }

    private void parrafo(Graphics2D g,String frase,int x,int y,int ancho,int tam,Color color) {
        Font fuente=tema.fuente(tam,false);
        String linea="";
        for(String palabra:frase.split(" ")) {
            if(!linea.isEmpty()&&tema.ancho(g,linea+" "+palabra,fuente)>ancho) {
                texto(g,linea,x,y,tam,color,false);y+=22;linea="";
            }
            linea=linea.isEmpty()?palabra:linea+" "+palabra;
        }
        texto(g,linea,x,y,tam,color,false);
    }
}
