package alcaldedigital.cliente.misiones;

import alcaldedigital.cliente.entrada.MapaControles;
import alcaldedigital.cliente.interfaz.MarcosProcessing;
import alcaldedigital.cliente.recursos.Recursos;
import processing.core.PApplet;
import processing.core.PImage;

/** Lámina común de controles con ilustraciones y acciones tomadas del mapa global. */
public final class PantallaControles {
    public static final int VOLVER_X=480,VOLVER_Y=626,VOLVER_ANCHO=320,VOLVER_ALTO=45;
    private static final int FONDO=0xff21152f, MARCO=0xffb58efa, ORO=0xfff4cb6a,
        CIAN=0xff76dce6, PAPEL=0xfff5e8fb, LILA=0xffcbb1dd;
    private static PImage tecladoImagen,mandoImagen;
    private PantallaControles(){}

    public static void dibujar(PApplet p,MapaControles.Contexto c){
        if(tecladoImagen==null){var recursos=new Recursos();tecladoImagen=new PImage(recursos.imagen("controles/teclado"));mandoImagen=new PImage(recursos.imagen("controles/mando"));}
        p.pushStyle();p.noStroke();p.fill(13,8,24,245);p.rect(0,0,1280,720);
        MarcosProcessing.marco(p.g,45,35,1190,650,FONDO,ORO);
        p.textAlign(PApplet.LEFT,PApplet.BASELINE);p.fill(ORO);p.textSize(28);
        p.text("CONTROLES · MISIÓN "+c.mision()+" · "+MapaControles.nombreMision(c.mision()),70,82);
        p.fill(PAPEL);p.textSize(16);
        p.text(c.dosJugadores()?"Dos personas · mismo teclado":"Una persona",70,111);
        p.text(c.nombreMando().isBlank()?"Mando: esquema genérico":"Mando: "+c.nombreMando(),640,111);
        MarcosProcessing.marco(p.g,65,130,540,480,0xff30203e,MARCO);
        MarcosProcessing.marco(p.g,625,130,590,480,0xff30203e,MARCO);
        p.fill(ORO);p.textSize(24);p.text("TECLADO",88,166);
        p.fill(CIAN);p.text("MANDO",650,166);
        p.image(tecladoImagen,82,188,507,203);
        p.image(mandoImagen,674,182,490,260);
        if(c.dosJugadores()){
            p.fill(ORO);p.textSize(17);p.text(c.jugador1(),88,418);
            p.fill(CIAN);p.text(c.jugador2(),343,418);
            listaTeclas(p,MapaControles.teclado1(c),88,441,25,ORO,112);
            listaTeclas(p,MapaControles.teclado2(c),343,441,25,CIAN,110);
        }else listaTeclas(p,MapaControles.teclado1(c),88,421,26,ORO,150);
        p.fill(LILA);p.textSize(15);
        p.text(c.playStation()?"PlayStation · botones equivalentes":"Xbox / mando genérico",650,443);
        int y=470;for(var b:MapaControles.mando(c)){
            p.fill(CIAN);p.textSize(15);p.text(MapaControles.etiquetaBoton(c,b.boton()),650,y);
            p.fill(PAPEL);p.text(b.accion(),872,y);y+=22;
        }
        p.fill(LILA);p.textSize(14);
        p.text("Esc / P: pausa · H: ayuda · G: guardar · T: consultas",75,593);
        MarcosProcessing.marco(p.g,VOLVER_X,VOLVER_Y,VOLVER_ANCHO,VOLVER_ALTO,0xff432b5b,ORO);
        p.fill(PAPEL);p.textSize(19);p.textAlign(PApplet.CENTER,PApplet.BASELINE);
        p.text("Volver · Enter / Esc / B",VOLVER_X+VOLVER_ANCHO/2,VOLVER_Y+29);
        p.popStyle();
    }
    private static void listaTeclas(PApplet p,java.util.List<MapaControles.Tecla> teclas,
                                   int x,int y,int paso,int color,int accionX){
        p.textAlign(PApplet.LEFT,PApplet.BASELINE);p.textSize(15);
        for(var tecla:teclas){
            p.fill(color);p.text(tecla.tecla(),x,y);
            p.fill(PAPEL);p.text(tecla.accion(),x+accionX,y);y+=paso;
        }
    }
}
