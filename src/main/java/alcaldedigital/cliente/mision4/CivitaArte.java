package alcaldedigital.cliente.mision4;

import alcaldedigital.cliente.presentacion.LuzEscena;

import alcaldedigital.cliente.entrada.MapaControles;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import static alcaldedigital.cliente.mision4.Civita.*;
import static alcaldedigital.cliente.mision4.MisionCuatroGame.H;
import static alcaldedigital.cliente.mision4.MisionCuatroGame.W;

/** Pixel art del laberinto y máscaras de luz recortadas por los muros reales. */
final class CivitaArte {
    private static final int ORO=0xfff4cb6a,LILA=0xffbd91d5,PAPEL=0xfff5e8fb,TINTA=0xff342336;
    private final BufferedImage sombra=new BufferedImage(W,H,BufferedImage.TYPE_INT_ARGB_PRE);
    private final BufferedImage claridad=prepararClaridad();
    private PImage escenaOriginal;
    private PImage fondoTelefono;
    private int corteTelefono;
    boolean reducido;

    void laberinto(PGraphics a,MisionCuatroGame g,Civita c,boolean interfaz) {
        a.background(13,9,23);
        a.pushMatrix();
        float sacudida=reducido?0:g.combat.screenShake*.65f;
        a.translate((float)Math.sin(g.elapsed*51)*sacudida,(float)Math.cos(g.elapsed*47)*sacudida*.54f);
        a.pushMatrix();a.translate(-c.camX,-c.camY);
        suelo(a,g);
        for(int i=0;i<ARCHIVOS.length;i++) {
            var arc=ARCHIVOS[i];
            objeto(a,arc.papel(),c.visto(i)?LILA:ORO,0);
            objeto(a,arc.terminal(),c.contrastado(i)?0xff82dbc9:0xff7488bb,1);
            if((c.fichas&(1<<i))==0) objeto(a,FICHAS[i],0xff80c1b6,2);
        }
        objeto(a,NUCLEO,c.total()==4?ORO:LILA,3);
        a.popMatrix();
        luz(a,g.hero,c,g.elapsed);
        a.pushMatrix();a.translate(-c.camX,-c.camY);
        energia(a,c,g.hero,g.elapsed);
        var enemigo=g.combat.enemy;
        if(enemigo!=null&&enemigo.y<g.hero.y) enemigo(a,g);
        if(interfaz)g.drawHero(a,enemigo!=null);
        if(enemigo!=null&&enemigo.y>=g.hero.y) enemigo(a,g);
        if(enemigo!=null)g.combat.drawEffects(a,g.hero);
        a.popMatrix();
        a.popMatrix();
        if(!interfaz)return;
        hud(a,g,c);
        if(c.combate>=0&&c.fraseTiempo>0&&enemigo!=null)frase(a,c,enemigo);
        if(c.hoja!=Hoja.NINGUNA) expediente(a,g,c);
    }
    private static void suelo(PGraphics a,MisionCuatroGame g) {
        a.image(g.sprites.image("mapa-civita"),0,0,ANCHO,ALTO);
        a.noStroke();a.fill(24,14,48,44);a.rect(0,0,ANCHO,ALTO);
        float t=g.elapsed;
        for(int i=0;i<34;i++) {
            float x=54+(i*197)%2150+(float)Math.sin(t*.82+i*1.7f)*13;
            float y=60+(i*113)%1570+(float)Math.sin(t*.69+i)*10;
            float brillo=28+(float)Math.sin(t*2.2+i)*21;
            a.fill(i%4==0?244:162,i%4==0?203:137,219,brillo);a.rect(x,y,3+i%3,2);
        }
        baliza(a,ARCHIVOS[0].papel(),"BITÁCORAS",0xffe9ba89,t);
        baliza(a,ARCHIVOS[1].papel(),"BOSQUE DE IMÁGENES",0xff9ecbd7,t);
        baliza(a,ARCHIVOS[2].papel(),"RED DE DIFUSIÓN",0xffa1cfb0,t);
        baliza(a,ARCHIVOS[3].papel(),"JARDÍN DE TESTIGOS",0xffc7a0df,t);
        baliza(a,NUCLEO,"PLAZA CENTRAL",ORO,t);
    }
    private static void baliza(PGraphics a,Punto p,String etiqueta,int color,float t) {
        float x=p.x(),y=p.y(),caida=5+(float)Math.sin(t*2+x*.02)*4;
        a.fill(25,16,38,214);a.rect(x-77,y-55,154,20,2);
        a.fill(color);a.rect(x-70,y-48,5,5);a.textSize(11);a.text(etiqueta,x-61,y-41);
        a.stroke(202,157,225,85);a.strokeWeight(1.5f);
        a.line(x+5,y-29,x+4,y-29+caida);a.noStroke();
    }

    private static void objeto(PGraphics a,Punto p,int color,int tipo) {
        float x=p.x(),y=p.y();a.noStroke();a.fill(12,8,20,150);a.ellipse(x,y+3,39,11);
        a.fill(color);
        switch(tipo) {
            case 0 -> {a.rect(x-12,y-25,24,22);a.fill(TINTA);a.rect(x-8,y-20,14,2);a.rect(x-8,y-14,10,2);}
            case 1 -> {a.rect(x-18,y-32,36,25);a.fill(0xff2d2343);a.rect(x-14,y-28,28,14);a.rect(x-20,y-7,40,6);}
            case 2 -> {a.quad(x,y-23,x+10,y-12,x,y-3,x-10,y-12);}
            default -> {a.ellipse(x,y-30,60,64);a.fill(0xff362853);a.ellipse(x,y-30,48,50);a.fill(color);a.rect(x-5,y-56,10,52);}
        }
    }
    void reiniciarTelefono() {
        fondoTelefono=null;
    }

    boolean dibujarFondoTelefono(PGraphics a,int corte) {
        if (fondoTelefono==null||corteTelefono!=corte) return false;
        a.image(fondoTelefono,0,0,W,H);
        return true;
    }

    void capturarFondoTelefono(PGraphics a,int corte) {
        fondoTelefono=a.get();
        corteTelefono=corte;
    }

    private static BufferedImage prepararClaridad() {
        var capa=new BufferedImage(210,210,BufferedImage.TYPE_INT_ARGB_PRE);
        var dibujo=capa.createGraphics();
        dibujo.setPaint(new RadialGradientPaint(105,105,105,new float[]{0,.5f,1},
            new Color[]{new Color(255,255,255,150),new Color(255,255,255,52),new Color(255,255,255,0)}));
        dibujo.fillRect(0,0,210,210);
        dibujo.dispose();
        return capa;
    }

    private void luz(PGraphics a,Actor hero,Civita c,float t) {
        if(!(a instanceof PGraphicsJava2D java2d))return;
        float x=hero.x-c.camX,y=hero.y-c.camY;
        Graphics2D g=sombra.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(12,6,29,186));
            g.fillRect(0,0,W,H);
            g.setComposite(AlphaComposite.DstOut);
            g.drawImage(claridad,Math.round(x)-105,Math.round(y)-105,null);
            if(c.linterna) {
                float rumbo=c.rumbo+(reducido?0:(float)Math.sin(t*5.7)*.009f);
                cono(g,c,hero,x,y,rumbo,APERTURA+.08f,69,40);
                cono(g,c,hero,x,y,rumbo,APERTURA-.035f,139,40);
                cono(g,c,hero,x,y,rumbo,APERTURA-.15f,158,30);
            }
        } finally {
            g.dispose();
        }
        java2d.g2.drawImage(sombra,0,0,null);
    }

    private static void cono(Graphics2D g,Civita c,Actor hero,float x,float y,float rumbo,
                             float abertura,int intensidad,int radios) {
        var forma=new Polygon();
        forma.addPoint((int)x,(int)y);
        for (int i=0;i<=radios;i++) {
            float angulo=rumbo-abertura+2*abertura*i/radios;
            float d=c.alcance(hero.x,hero.y,angulo,RADIO_LUZ);
            forma.addPoint((int)(x+Math.cos(angulo)*d),(int)(y+Math.sin(angulo)*d));
        }
        g.setPaint(new RadialGradientPaint(x,y,RADIO_LUZ,new float[]{0,.42f,.76f,1},
            new Color[]{new Color(255,255,255,intensidad),new Color(255,255,255,intensidad*8/10),
                new Color(255,255,255,intensidad*4/10),new Color(255,255,255,0)}));
        g.fillPolygon(forma);
    }
    private static void energia(PGraphics a,Civita c,Actor hero,float t){
        if(c.guiaTiempo>0&&c.guia>=0){
            var objetivo=c.guia==3?NUCLEO:ARCHIVOS[c.guia].terminal();
            a.noFill();a.stroke(255,218,131,135+(float)Math.sin(t*4)*50);a.strokeWeight(2);
            a.ellipse(objetivo.x(),objetivo.y()-8,45,26);
            a.line(objetivo.x(),objetivo.y()-24,objetivo.x(),objetivo.y()-49);a.noStroke();
            a.fill(ORO);a.rect(objetivo.x()-3,objetivo.y()-53,6,6);
            int escala=0;
            for(var paso:c.rastro(hero)){
                float brillo=72+(float)Math.sin(t*5-escala++*.66f)*53;
                a.fill(255,216,129,brillo);a.ellipse(paso.x(),paso.y(),7,4);
            }
        }
        if(c.pulso>0&&c.destello!=null){
            float avance=1-c.pulso/.72f,x=c.destello.x(),y=c.destello.y()-12;
            a.noFill();a.stroke(255,219,140,205*(1-avance));a.strokeWeight(2);
            a.ellipse(x,y,20+86*avance,14+60*avance);a.noStroke();
            for(int i=0;i<8;i++){
                float angulo=i*PApplet.TWO_PI/8;
                a.fill(255,229,157,205*(1-avance));
                a.rect(x+(float)Math.cos(angulo)*avance*44,y+(float)Math.sin(angulo)*avance*44,3,3);
            }
        }
        if(c.total()!=4)return;
        float power=c.vencido(2)?1:.49f,apertura=Math.min(1,c.nucleoTiempo*.85f);
        if(c.vencido(2)){
            a.noStroke();
            float latido=.86f+.14f*(float)Math.sin(t*4.3);
            for(int i=0;i<5;i++){
                float diametro=(303-i*41)*latido;
                a.fill(245,184+i*8,123,8+i*3+apertura*5);
                a.ellipse(CENTRO_X,CENTRO_Y-18,diametro,diametro*.72f);
            }
            a.fill(255,234,164,108*latido);
            a.ellipse(CENTRO_X,CENTRO_Y-83,23,30);
        }
        for(int i=0;i<3;i++){
            float fase=(t*.48f+i/3f)%1f,radio=103+fase*158;
            a.noFill();a.stroke(244,193,109,(100*(1-fase)+apertura*51)*power);
            a.strokeWeight(2+i*.5f);a.ellipse(CENTRO_X,CENTRO_Y,radio*2,radio*1.88f);
        }
        a.noStroke();
        for(int i=0;i<12;i++){
            float r=t*(.54f+i%2*.09f)+i*.524f;
            a.fill(255,210,112,(161+60*(float)Math.sin(t*5+i))*power);
            a.rect(CENTRO_X+(float)Math.cos(r)*114,CENTRO_Y+(float)Math.sin(r)*98,4,4);
        }
        if(c.vencido(2)){
            float b=(float)Math.sin(t*4.3)*.25f+.75f;
            a.fill(255,224,125,78*b);a.ellipse(NUCLEO.x(),NUCLEO.y()-20,84*b,55*b);
        }
    }

    private static void enemigo(PGraphics a,MisionCuatroGame g) {
        var e=g.combat.enemy;
        if(e.windup>0) {
            a.noFill();a.stroke(240,108,157,180);a.strokeWeight(2);
            a.ellipse(e.targetX,e.targetY,68,68);a.line(e.x,e.y,e.targetX,e.targetY);a.noStroke();
        }
        g.drawEnemy(a);
    }
    private static void frase(PGraphics a,Civita c,CombatSystem.Enemy e){
        int x=Math.clamp((int)(e.x-c.camX)-194,27,W-417);
        int y=Math.clamp((int)(e.y-c.camY)-139,64,414);
        a.noStroke();a.fill(30,14,49,235);a.rect(x,y,390,46,4);
        a.fill(ORO);a.rect(x,y,3,46);a.textSize(12);a.text(e.pattern.name(),x+12,y+16);
        a.fill(PAPEL);a.textSize(14);a.text(FRASES[c.combate],x+12,y+37);
    }
    private static String confirmar(MisionCuatroGame g) {
        return g.nombreMando.isBlank()?"E / Enter":MapaControles.etiquetaBoton(
            new MapaControles.Contexto(4,false,"Juan",null,g.nombreMando),"A");
    }
    private static void hud(PGraphics a,MisionCuatroGame g,Civita c) {
        boolean mando=!g.nombreMando.isBlank();
        var contexto=new MapaControles.Contexto(4,false,"Juan",null,g.nombreMando);
        String luz=mando?MapaControles.etiquetaBoton(contexto,"LB"):"F";
        String investigar=mando?MapaControles.etiquetaBoton(contexto,"A"):"E";
        a.noStroke();a.fill(24,16,41,244);a.rect(0,0,W,58);a.rect(0,502,W,74);
        a.fill(LILA);a.rect(0,56,W,2);a.rect(0,502,W,2);
        a.fill(ORO);a.textSize(14);a.text("MISIÓN 4  /  CIVITA",19,22);
        a.fill(PAPEL);a.textSize(16);a.text("Expedientes "+c.total()+"/4     Fichas de red "+Integer.bitCount(c.fichas)+"/4",19,44);
        a.fill(c.linterna?ORO:LILA);a.textSize(14);a.text(luz+(c.linterna?" · encendida":" · encender linterna"),658,35);
        for(int i=0;i<5;i++){a.fill(i<g.combat.health?0xfff080ab:0xff684463);a.ellipse(897+i*21,30,11,11);}
        String contextual=c.accionCercana(g.hero,investigar,luz);
        a.fill(PAPEL);a.textSize(14);a.text(c.aviso,19,523);
        a.fill(ORO);a.textSize(15);a.text(contextual,19,543);
        a.fill(PAPEL);a.textSize(13);
        String controles=mando?"Stick: mover    "+luz+": linterna    "+investigar+": investigar    "+
            MapaControles.etiquetaBoton(contexto,"X / RT")+": espada    "+MapaControles.etiquetaBoton(contexto,"Y / LT")+
            ": escudo    "+MapaControles.etiquetaBoton(contexto,"RB")+": esquivar":
            "WASD / flechas: mover    F: linterna    E: investigar    J: espada    Q: escudo    Espacio: esquivar";
        a.text(controles,19,562);
        miniMapa(a,c,g.hero);
    }
    private static void miniMapa(PGraphics a,Civita c,Actor hero) {
        int x=905,y=70,m=2;
        a.fill(17,10,31,224);a.rect(x-5,y-5,114,94);
        for(int col=0;col<COLUMNAS;col++) for(int f=0;f<FILAS;f++) if(c.suelo(col,f)){
            a.fill(0xff625474);a.rect(x+col*m,y+f*m,m,m);
        }
        for(int i=0;i<ARCHIVOS.length;i++){
            var p=ARCHIVOS[i].papel();a.fill(c.contrastado(i)?0xff84d6ba:LILA);
            a.rect(x+p.columna()*m,y+p.fila()*m,m,m);
            if(c.visto(i)&&!c.contrastado(i)){
                var terminal=ARCHIVOS[i].terminal();a.fill(0xff89c7b9);
                a.rect(x+terminal.columna()*m,y+terminal.fila()*m,m,m);
            }
        }
        a.fill(c.total()==4?ORO:0xffbc7498);a.rect(x+NUCLEO.columna()*m,y+NUCLEO.fila()*m,m,m);
        if(c.guiaTiempo>0&&c.guia>=0){
            var meta=c.guia==3?NUCLEO:ARCHIVOS[c.guia].terminal();
            a.noFill();a.stroke(ORO);a.strokeWeight(1.4f);
            a.ellipse(x+meta.columna()*m,y+meta.fila()*m,11,11);a.noStroke();
        }
        a.fill(ORO);a.rect(x+(int)(hero.x/BALDOSA)*m-1,y+(int)(hero.y/BALDOSA)*m-1,5,5);
    }
    private void expediente(PGraphics a,MisionCuatroGame g,Civita c) {
        var arc=ARCHIVOS[c.expediente];
        boolean fuente=c.hoja==Hoja.CONTRASTE;
        a.noStroke();a.fill(11,6,22,222);a.rect(0,0,W,H);
        a.pushMatrix();a.translate(0,-(1-Math.min(1,c.tiempoHoja*4))*11);
        pergamino(a,g);
        int x=330, derecha=471;
        a.fill(TINTA);a.textSize(20);
        a.text(String.format(java.util.Locale.ROOT,"EXPEDIENTE #%02d  %s",c.expediente+1,
            arc.nombre().toUpperCase(java.util.Locale.ROOT)),x,109);
        a.fill(0xffa78259);a.rect(x,117,367,2);
        a.fill(109,71,58,95);a.rect(x-2,136,135,149);
        a.fill(0xfff9e7c4);a.rect(x-4,130,133,150);
        a.fill(0xff9c7253);a.rect(x,135,125,135);
        ilustrar(a,g,c.expediente,x+2,137,121,128,false);
        a.fill(0xff886647);a.rect(x+4,272,65,2);
        a.fill(0xffd9b682);a.rect(x+95,131,30,5);
        a.image(g.sprites.shared("arte/iconos/08_verificar"),derecha,130,20,20);
        a.fill(TINTA);a.textSize(15);a.text(fuente?"FUENTE RECUPERADA":"PISTA DETECTADA",derecha+24,146);
        a.textSize(14);g.wrapText(a,fuente?arc.prueba():arc.indicio(),derecha,169,222,17);
        if(fuente) {
            a.fill(0xffc5a175);a.rect(derecha+15,229,200,54,3);
            a.fill(0xfff8e4b9);a.rect(derecha+18,230,195,50,2);
            ilustrar(a,g,c.expediente,derecha+22,233,188,44,true);
        }
        else if(c.expediente==0||c.expediente==3){
            PImage mapa=g.sprites.image("mapa-civita");
            a.tint(235,199,147);a.image(mapa,derecha+117,224,94,56,437,310,579,451);a.noTint();
            a.fill(0xff74513b);a.textSize(12);a.text("La fuente",derecha+12,258);
        }else{
            a.image(g.sprites.shared(c.expediente==1?"arte/iconos/02_camara":"arte/iconos/23_conexion"),derecha,241,27,27);
            a.fill(0xff74513b);a.textSize(13);a.text(c.expediente==1?"Encuadre recortado":"Mensajes replicados",derecha+34,259);
        }
        a.fill(0xffa78259);a.rect(x,291,367,2);
        a.image(g.sprites.shared("arte/iconos/14_convivencia"),x,302,18,18);
        a.fill(TINTA);a.textSize(16);a.text(fuente?"INFORME CONTRASTADO":"INFORME DEL EXPEDIENTE",x+24,318);
        a.textSize(15);g.wrapText(a,fuente?arc.contraste():arc.titular(),x,341,366,18);
        a.fill(0xffa78259);a.rect(x,399,367,2);
        a.fill(TINTA);a.textSize(16);a.text("OBJETIVOS",x,424);
        a.textSize(14);a.text("[x]  Localizar el anuncio",x,448);
        g.wrapText(a,(fuente?"[x]  ":"[ ]  ")+arc.fuente(),x,469,361,17);
        a.fill(0xff4f3380);a.textSize(14);
        a.text(confirmar(g)+(fuente?" · registrar evidencia":" · volver al laberinto"),x,509);
        a.popMatrix();
    }
    private static void pergamino(PGraphics a,MisionCuatroGame g) {
        int x=300,y=70,w=424,h=470;
        a.noStroke();
        int[] xs={3,18,11,4,11,5,16,8,18,12,18,13,6,21,51,74,110,151,194,220,261,300,341,371,413,422,408,421,410,421,406,420,415};
        int[] ys={0,31,61,97,127,147,189,226,258,289,326,363,411,457,446,452,469,449,467,453,469,454,462,446,457,430,393,350,307,268,208,145,0};
        a.fill(0xff744c34);a.beginShape();for(int i=0;i<xs.length;i++)a.vertex(x+xs[i]+4,y+ys[i]+5);a.endShape(PApplet.CLOSE);
        a.fill(0xffddbc8c);a.beginShape();for(int i=0;i<xs.length;i++)a.vertex(x+xs[i],y+ys[i]);a.endShape(PApplet.CLOSE);
        a.fill(0xfff0d6a7);a.beginShape();for(int i=0;i<xs.length;i++)a.vertex(x+16+xs[i]*.925f,y+5+ys[i]*.96f);a.endShape(PApplet.CLOSE);
        a.fill(117,76,45,48);
        for(int i=0;i<58;i++)a.rect(x+23+(i*61)%378,y+13+(i*97)%416,2+i%6,1+i%2);
        PImage referencia=g.sprites.image("pergamino-referencia");
        a.image(referencia,x-18,11,w+36,79,0,0,825,165);
    }

    private void ilustrar(PGraphics a,MisionCuatroGame g,int id,int x,int y,int ancho,int alto,boolean fuente) {
        if(id==1){
            if(escenaOriginal==null){
                PGraphicsJava2D foto=new PGraphicsJava2D();foto.setParent(new PApplet());foto.setSize(360,182);foto.noSmooth();
                foto.beginDraw();foto.image(g.sprites.image("plaza_presentacion"),0,0,360,182);
                g.sprites.juan.dibujar(foto,"idle",0,0,0,110,142,41,55);
                g.sprites.draw(foto,"vecino_2",252,141,34,46);g.sprites.draw(foto,"vecino_5",289,145,34,46);
                foto.endDraw();escenaOriginal=foto.get();foto.dispose();
            }
            int origen=fuente?360:191;
            float escala=Math.min(ancho/(float)origen,alto/182f),w=origen*escala,h=182*escala;
            a.noStroke();a.fill(0xff5b4261);a.rect(x,y,ancho,alto);
            a.image(escenaOriginal,x+(ancho-w)/2,y+(alto-h)/2,w,h,0,0,origen,182);
        } else {
            a.noStroke();a.fill(id==2?0xff302a51:0xff765c73);a.rect(x,y,ancho,alto);
            if(id==0) {
                var parque=g.sprites.shared("arte/mision1/parque");
                a.image(parque,x,y,ancho,alto,parque.width*40/100,parque.height*34/100,parque.width*61/100,parque.height*56/100);
                if(!fuente){a.fill(54,42,64,95);a.rect(x,y,ancho,alto);}
            } else if(id==2) {
                a.stroke(0xffb990dc);a.strokeWeight(3);
                for(int i=0;i<3;i++){
                    int yy=y+alto*(i+1)/4;
                    a.line(x+ancho/4,y+alto/2,x+ancho*3/4,yy);
                    a.fill(0xffd4b0e8);a.rect(x+ancho*3/4-8,yy-8,16,16);
                }
                a.noStroke();a.fill(fuente?ORO:0xffb47bce);a.rect(x+ancho/4-11,y+alto/2-11,22,22);
            } else if(fuente) {
                a.fill(0xffe4c69e);a.rect(x+ancho*.25f,y+9,ancho*.5f,alto-18);
                a.fill(TINTA);for(int i=0;i<5;i++)a.rect(x+ancho*.30f,y+23+i*13,ancho*.38f,2);
            } else {
                g.sprites.juan.dibujar(a,"idle",0,0,0,x+ancho/2f,y+alto-7,alto*.63f,alto*.88f);
                a.fill(0xffd3b393);a.rect(x+12,y+12,5,alto-24);
            }
        }
    }
    void telefono(PGraphics a,MisionCuatroGame g,float t,boolean salida) {
        boolean temblor=!salida&&t<1.02f;
        if(temblor) {
            float x=376+(reducido?0:(float)Math.sin(t*61)*4);
            a.image(g.sprites.shared("arte/iconos/01_celular"),x,160,35,35);
            a.fill(ORO);a.rect(x-7,166,3,12);a.rect(x+40,166,3,12);
            return;
        }
        a.noStroke();a.fill(18,10,33,208);a.rect(0,57,W,H-57);
        float u=PApplet.constrain((t-(salida?.15f:2.05f))/2.0f,0,1);
        int x=388,y=106;
        LuzEscena.VIOLETA.dibujar(a,512,298,535,470,155);
        g.pixelPanel(a,x,y,248,345,0xff312151);
        a.fill(0xff15142d);a.rect(x+16,y+24,216,286,8);
        a.fill(LILA);a.rect(x+104,y+322,40,5);
        a.fill(ORO);a.textSize(29);a.text("CIVITA",x+61,y+103);
        a.fill(PAPEL);a.textSize(13);a.text(salida?"CONEXIÓN INTERRUMPIDA":"NOTIFICACIÓN DE CIVITA",x+38,y+143);
        if(!salida){
            float descarga=PApplet.constrain((t-1.04f)/1.32f,0,1);
            a.fill(0xff38264c);a.rect(x+33,y+159,180,8,2);
            a.fill(t<2.05f?LILA:ORO);a.rect(x+33,y+159,180*descarga,8,2);
            a.fill(PAPEL);a.textSize(12);
            a.text(t<2.05f?"Descargando  "+(int)(descarga*100)+" %":"CIVITA · abriendo enlace",x+34,y+186);
        }
        float portal=salida?1-u:u;
        float reloj=reducido?0:t;
        if(portal>.04f){
            LuzEscena.CIAN.dibujar(a,512,324,215,177,135*portal);
            a.noFill();
            for(int i=0;i<5;i++){
                float f=(reloj*.8f+i/5f)%1,radio=19+f*65;
                a.stroke(i%2==0?244:166,i%2==0?192:103,232,portal*(1-f)*179);
                a.strokeWeight(2.2f);a.ellipse(512,322,radio*2,radio*1.32f);
            }
            a.noStroke();
            for(int i=0;i<10;i++){
                float yy=y+30+(i*29+reloj*178)%270,bar=8+(i*21)%125;
                a.fill(189,122,236,portal*(28+i%3*22));a.rect(x+17+(i*11)%79,yy,bar,2,1);
            }
        }
        for(int i=0;i<30;i++) {
            float ang=i*2.39f,radio=(1-u)*112+i%9;
            float px=512+(float)Math.cos(ang+reloj*3)*radio,py=338+(float)Math.sin(ang+reloj*3)*radio*.55f;
            a.fill(i%3==0?ORO:LILA);a.rect(px,py,3+i%4,2);
        }
        if(salida||t>=2.05f){
            float k=salida?u:1-u;
            float destinoX=salida?350:g.hero.x,destinoY=salida?218:g.hero.y;
            g.sprites.juan.dibujar(a,"confused",0,0,t,512+(destinoX-512)*k,343+(destinoY-343)*k,12+40*k,14+56*k);
        }
        a.fill(PAPEL);a.textSize(16);
        a.text(salida?"Juan regresa a la plaza.":t<2.05f?"Una nueva notificación sacude el teléfono.":"La señal arrastra a Juan dentro de la pantalla.",234,501);
        a.fill(ORO);a.textSize(13);a.text(confirmar(g)+" · continuar",411,540);
    }
}
