package alcaldedigital.cliente.interfaz;

import alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import static alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;

/** Expediente ilustrado, inspección óptica y edición del aviso de Civitas. */
final class PergaminoSofi {
    private static final int X=322,Y=112,W=580,H=555;
    private static final Rectangle HOJA=new Rectangle(X,Y,W,H);
    private static final Color FIBRA=new Color(0xF0D6A7), OCRE=new Color(0xD5AD78),
        CAFE=new Color(0x805234), TINTA=new Color(0x352439), VIOLETA=new Color(0x594067),
        SELLO=new Color(0x376555);
    private static final int[] BORDE_X={3,17,5,14,7,18,11,5,18,9,26,56,94,132,173,210,255,294,327,375,417,457,496,534,570,579,562,577,564,579,569,579,567,578};
    private static final int[] BORDE_Y={0,48,95,133,172,223,271,322,370,413,546,532,548,555,541,550,534,555,541,551,532,547,535,546,554,510,458,415,358,310,253,203,137,0};

    private final RenderJuego ui;
    private BufferedImage rollo;
    private final LupaArchivo lente = new LupaArchivo();
    private BufferedImage papel;
    private boolean contrastePapel;
    private int expedienteAnterior;
    private final EnumMap<Zona,Rectangle> zonas=new EnumMap<>(Zona.class);

    PergaminoSofi(RenderJuego ui) {this.ui=ui;}

    void dibujar(SesionMisionUno.Vista s,Vista v,Caso caso) {
        if (expedienteAnterior != caso.id() || v.publicacion()) lente.reiniciar();
        expedienteAnterior = caso.id();
        var estado=ui.juego.investigacionSofi;
        double recogida=estado.finRecogida-ui.juego.animacion;
        double giro=estado.finPagina-ui.juego.animacion;
        boolean animar=!ui.juego.movimientoReducido;
        if(animar&&recogida>.75) {
            double t=1-(recogida-.75)/.37;
            int camY=Math.clamp(Math.round(s.y())-180,0,120);
            levantarPapel(caso.x()*2,(caso.y()-camY)*2,t);
            ui.texto("Sofi recoge el expediente",49,664,17,ui.papel,true);
            return;
        }
        boolean abriendo=animar&&(recogida>0||giro>0);
        ui.texto("CIVITAS  /  INVESTIGACIÓN CIUDADANA",45,56,16,ui.oro,true);
        if(!abriendo) ui.boton("Volver al parque","volver",1030,35,205,false);
        izquierda(caso,v);
        Graphics2D original=ui.g;
        if(abriendo) {
            double progreso=recogida>0?1-recogida/.75:1-giro/.43;
            double apertura=recogida>0?.05+.95*suave(progreso):.23+.77*suave(progreso);
            ui.g=(Graphics2D)original.create();
            ui.g.translate(X+W/2,Y);
            ui.g.scale(giro>0?apertura:1,recogida>0?apertura:1);
            ui.g.translate(-X-W/2,-Y);
        }
        fondo();
        if(v.publicacion()) paginaAviso(s,caso);
        else paginaInvestigacion(caso);
        if(abriendo) {
            ui.g.dispose();
            ui.g=original;
            return;
        }
        if(v.publicacion()) accionesAviso(s,caso);
        else {
            accionesInvestigacion(s,v,caso);
            lupa(v,caso,s.dificultad());
        }
        if(v.publicacion()) ui.pie(ui.juego.mandoActivo?"X: libreta · B: volver":"Tab: libreta · Esc: volver");
        else ui.pie(ui.juego.mandoActivo?ui.teclaLupaSofi()+": lupa · Cruceta: enfocar · Botón de confirmación: examinar":
            "Q: lupa · Clic: examinar · Flechas y Enter: seleccionar · Tab: libreta · Esc: volver");
    }

    private void izquierda(Caso caso,Vista v) {
        ui.texto("EXPEDIENTE 0"+caso.id()+" / 04",48,166,15,ui.oro,true);
        ui.parrafo(caso.nombre(),48,203,232,23,ui.papel);
        ui.parrafo(caso.lugar(),48,267,224,15,ui.tenue);
        ui.rect(48,304,218,1,new Color(0x82678E));
        ui.texto(v.publicacion()?"02  PUBLICACIÓN":"01  INSPECCIÓN",48,337,17,ui.papel,true);
        ui.parrafo(v.publicacion()?"Prepara el aviso que llevarás a los vecinos.":
            "Busca en el papel los detalles que permitan sustentar la noticia.",48,367,216,16,ui.tenue);
        int y=555;
        for(Caso c:CASOS) {
            ui.rect(48+(c.id()-1)*43,y,32,5,c.id()==caso.id()?ui.oro:
                v.ficha(c.id()).clasificada()?new Color(0x77AFA0):new Color(0x63526F));
        }
    }

    private void fondo() {
        boolean contraste = ui.juego.contraste;
        if (papel == null || contrastePapel != contraste) {
            contrastePapel = contraste;
            papel = new BufferedImage(W + 44, H + 54, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = papel.createGraphics();
            g.translate(22, 39);
            Polygon borde = new Polygon(BORDE_X, BORDE_Y, BORDE_X.length);
            g.setColor(new Color(18, 10, 27, 140));
            g.translate(8, 10);
            g.fill(borde);
            g.translate(-8, -10);
            g.setPaint(new GradientPaint(0, 0, new Color(0xF2D09D), W, H, new Color(0xA96D48)));
            g.fill(borde);
            g.setStroke(new BasicStroke(2));
            g.setColor(CAFE);
            g.draw(borde);
            g.clip(borde);
            g.setPaint(new RadialGradientPaint(W / 2f, H * .48f, W * .85f,
                new float[]{0, .55f, 1}, new Color[]{contraste ? Color.WHITE : new Color(0xFAE8C0),
                contraste ? new Color(0xFFF9E9) : FIBRA, OCRE}));
            g.fillRoundRect(21, 5, W - 37, H - 16, 9, 9);
            g.setColor(new Color(126, 82, 52, 71));
            g.drawRect(29, 63, W - 56, H - 93);
            g.drawRect(32, 68, W - 62, H - 103);
            var azar = new java.util.Random(10231);
            for (int i = 0; i < 1100; i++) {
                g.setColor(i % 5 == 0 ? new Color(255, 252, 233, 90) : new Color(124, 79, 49, i % 7 == 0 ? 39 : 14));
                g.fillRect(32 + azar.nextInt(W - 64), 52 + azar.nextInt(H - 81), 1 + azar.nextInt(5), 1);
            }
            g.dispose();
        }
        ui.g.drawImage(papel, X - 22, Y - 39, null);
        dibujarRollo(X-17,Y-33,W+34,94);
    }

    private void dibujarRollo(int x,int y,int ancho,int alto) {
        if(rollo==null) {
            var referencia=ui.arte("mision4/pergamino-referencia");
            rollo=new BufferedImage(referencia.getWidth(),165,BufferedImage.TYPE_INT_ARGB);
            for(int py=0;py<rollo.getHeight();py++) for(int px=0;px<rollo.getWidth();px++) {
                int color=referencia.getRGB(px,py);
                int rojo=(color>>>16)&255,verde=(color>>>8)&255,azul=color&255;
                if(rojo>=42||verde>=38||azul<=rojo) rollo.setRGB(px,py,color);
            }
        }
        ui.g.drawImage(rollo,x,y,ancho,alto,null);
    }



    private void paginaInvestigacion(Caso caso) {
        zonas.clear();
        int x=X+37;
        ui.texto("CIVITAS",x,194,23,TINTA,true);
        ui.texto("ARCHIVO DEL PARQUE  •  CIUDAD NOVA",x+147,192,12,CAFE,true);
        ui.rect(x,204,505,2,CAFE);
        ui.parrafo(caso.titular(),x,230,506,21,TINTA);
        ui.texto(caso.edicion(),x,283,12,VIOLETA,false);
        ui.rect(x,297,505,1,CAFE);
        switch(caso.id()) {
            case 1 -> agua(caso);
            case 2 -> jornada(caso);
            case 3 -> palomas();
            case 4 -> boletin(caso);
            default -> throw new IllegalArgumentException("Expediente desconocido");
        }
    }

    private void agua(Caso caso) {
        fotoParque(360,320,224,170,263,162,377,249);
        ui.texto("FUENTE DE CIUDAD NOVA",363,511,12,CAFE,true);
        ui.texto("EL LLAMADO",604,331,15,TINTA,true);
        ui.parrafo(caso.nota(),604,354,245,15,TINTA);
        detalle(Zona.TITULAR,603,430,190,"«Hay que cerrarla ahora»");
        tarjeta(359,531,504,90,"AVISO DE LA ADMINISTRACIÓN DEL PARQUE");
        ui.texto("Actividad de hoy · limpieza del parque",370,578,15,TINTA,true);
        detalle(Zona.FUENTES,370,594,267,"Cuenta anónima · Sin informe del agua");
    }

    private void jornada(Caso caso) {
        fotoParque(360,313,500,171,190,129,498,303);
        ui.rect(365,453,493,29,new Color(48,29,60,213));
        ui.texto("Parque de Ciudad Nova  /  Archivo municipal",376,472,13,ui.papel,false);
        tarjeta(360,506,500,112,"AGENDA MUNICIPAL  •  CIUDAD NOVA");
        ui.parrafo(caso.nota(),372,551,465,14,TINTA);
        detalle(Zona.EDICION,372,578,151,"Edición de hoy");
        detalle(Zona.FUENTES,536,578,280,"Esta tarde · Limpieza del parque");
    }

    private void palomas() {
        tarjeta(360,322,233,215,"CAPTURA REENVIADA");
        tarjeta(615,322,247,215,"PUBLICACIÓN COMPLETA");
        for(int i=371;i<583;i+=10) ui.rect(i,375,6,2,CAFE);
        detalle(Zona.IMAGEN,371,381,193,"La captura empieza aquí");
        detalle(Zona.EDICION,627,365,215,"CHISTE DEL BARRIO · HUMOR");
        palomo(428,409);palomo(693,409);
        ui.parrafo("Hay más palomas que votantes en Ciudad Nova.",372,489,211,13,TINTA);
        ui.parrafo("Hay más palomas que votantes en Ciudad Nova.",627,489,218,13,TINTA);
        ui.texto("Reenvío vecinal",361,563,13,VIOLETA,true);
        ui.texto("Original · Sección de humor",615,563,13,VIOLETA,true);
        ui.parrafo("La captura y el original conservan la misma frase.",360,608,497,15,TINTA);
    }

    private void boletin(Caso caso) {
        fotoParque(360,322,159,113,264,164,382,253);
        ui.texto("BOLETÍN REENVIADO",537,333,15,TINTA,true);
        ui.parrafo(caso.nota(),537,359,317,14,TINTA);
        detalle(Zona.TITULAR,537,416,242,"«La agenda no incluye ninguna limpieza»");
        tarjeta(360,464,246,151,"AGENDA MUNICIPAL");
        tarjeta(617,464,245,151,"AVISO DEL PARQUE");
        detalle(Zona.EDICION,372,507,208,"Hoy · Ciudad Nova");
        ui.parrafo("Esta tarde · Jornada de limpieza del parque.",372,558,223,14,TINTA);
        ui.parrafo("Convocatoria de hoy a la jornada de limpieza del parque.",629,514,220,14,TINTA);
        detalle(Zona.FUENTES,629,586,208,"Administración · Misma jornada");
    }

    private void tarjeta(int x,int y,int w,int h,String titulo) {
        ui.rect(x+2,y+3,w,h,new Color(124,75,47,68));
        ui.rect(x,y,w,h,new Color(0xF6E1B9));
        ui.rect(x,y,w,2,CAFE);
        ui.texto(titulo,x+11,y+23,12,VIOLETA,true);
        ui.rect(x+10,y+31,w-20,1,OCRE);
    }

    private void fotoParque(int x,int y,int w,int h,int x1,int y1,int x2,int y2) {
        var parque=ui.arteParque();
        ui.rect(x-3,y-3,w+6,h+6,CAFE);
        ui.g.drawImage(parque,x,y,x+w,y+h,
            x1*parque.getWidth()/640,y1*parque.getHeight()/480,
            x2*parque.getWidth()/640,y2*parque.getHeight()/480,null);
        ui.rect(x+4,y+4,12,2,FIBRA);ui.rect(x+w-16,y+h-6,12,2,FIBRA);
    }

    private void palomo(int x,int y) {
        ui.personajeSofi("mision1/palomo",4,2,0,x,y,69,55);
    }

    private void detalle(Zona zona,int x,int y,int ancho,String texto) {
        zonas.put(zona,new Rectangle(x-3,y-4,ancho,23));
        ui.rect(x,y+16,ancho-8,1,OCRE);
        ui.g.setColor(TINTA);
        ui.g.setFont(new Font(Font.MONOSPACED,Font.BOLD,10));
        ui.g.drawString(texto,x+1,y+11);
    }

    private void accionesInvestigacion(SesionMisionUno.Vista s,Vista v,Caso caso) {
        Ficha ficha=v.ficha(caso.id());
        int total=caso.hallazgos().size();
        int x=938;
        ui.texto("EVIDENCIAS  "+ficha.encontrados()+" / "+total,x,164,18,ui.oro,true);
        for(int i=0;i<total;i++)
            ui.rect(x+i*42,180,33,5,i<ficha.encontrados()?ui.oro:new Color(0x65556D));
        ui.parrafo(ficha.clasificada()?"La investigación está lista para convertirse en un aviso.":
            v.lupa()?"Enfoca la letra pequeña y selecciona el detalle que quieras examinar.":
                "Pulsa "+ui.teclaLupaSofi()+" para activar la lupa y examinar el documento.",
            x,215,285,16,ui.papel);
        if(!ficha.clasificada()) {
            ui.boton((v.lupa()?"Guardar lupa":"Activar lupa")+" ["+ui.teclaLupaSofi()+"]","invest:lupa",x,283,286,!v.lupa());
            ui.boton(ui.juego.investigacionSofi.ayuda?"Ocultar ayuda":"Pedir una pista","invest:ayuda",x,620,286,false);
        } else {
            ui.boton("Pasar a publicación","invest:publicacion",x,620,286,true);
            ui.texto("INVESTIGACIÓN COMPLETADA",x,307,13,ui.oro,true);
        }
        double desde=ui.juego.animacion-ui.juego.investigacionSofi.ultimaPista;
        if(ui.juego.investigacionSofi.ayuda&&!ficha.clasificada()) {
            Zona z=siguiente(caso,ficha);
            ui.parrafo(orientacion(caso,z),x,565,282,14,ui.oro);
        } else if(!ficha.clasificada()&&desde>18&&!s.dificultad().equals(SesionMisionUno.Dificultad.DIFICIL)) {
            ui.texto("Hay una pista disponible.",x,583,13,ui.tenue,false);
        }
        int y=356;
        for(Hallazgo hallazgo:caso.hallazgos()) if(ficha.encontro(hallazgo.zona())) {
            boolean reciente=hallazgo.zona().ordinal()==v.ultimoHallazgo();
            ui.texto("✓  "+hallazgo.zona().etiqueta,x,y,15,ui.oro,true);
            ui.parrafo(reciente?hallazgo.explicacion():hallazgo.detalle(),x+17,y+21,268,13,ui.papel);
            y+=65;
        }
        if(ui.juego.animacion<ui.juego.investigacionSofi.finAviso)
            ui.texto("EVIDENCIA DESCUBIERTA",x,338,12,new Color(0xAFDBC5),true);
    }

    private static Zona siguiente(Caso caso,Ficha ficha) {
        return caso.hallazgos().stream().map(Hallazgo::zona).filter(z->!ficha.encontro(z)).findFirst().orElse(null);
    }

    private static String orientacion(Caso caso,Zona z) {
        return switch(z) {
            case TITULAR -> "Busca la frase subrayada en el recuadro que describe la nota.";
            case EDICION -> caso.id()==3?"Acerca la lupa al encabezado de la publicación completa.":
                "Busca la fecha en el recuadro de la agenda.";
            case IMAGEN -> "Examina el borde donde arranca la captura reenviada.";
            case FUENTES -> caso.id()==1?"Busca qué falta al pie del aviso de limpieza.":
                "Examina la letra pequeña de la actividad o del aviso que la acompaña.";
        };
    }

    private void lupa(Vista v,Caso caso,SesionMisionUno.Dificultad dificultad) {
        Ficha ficha=v.ficha(caso.id());
        double aviso=ui.juego.investigacionSofi.finAviso-ui.juego.animacion;
        for(var elemento:zonas.entrySet()) {
            Zona zona=elemento.getKey();
            Rectangle r=elemento.getValue();
            boolean encontrada=ficha.encontro(zona);
            boolean orientada=ui.juego.investigacionSofi.ayuda&&siguiente(caso,ficha)==zona;
            if(encontrada) {
                ui.g.setColor(SELLO);ui.g.setStroke(new BasicStroke(1));
                ui.g.drawRoundRect(r.x,r.y,r.width,r.height,6,6);
                ui.texto("✓",r.x+r.width-13,r.y+15,11,SELLO,true);
                if(zona.ordinal()==v.ultimoHallazgo()&&aviso>1.02&&!ui.juego.movimientoReducido)
                    destello(r.x+r.width,r.y+2,aviso);
            } else if(v.lupa()&&(orientada||dificultad==SesionMisionUno.Dificultad.FACIL)) {
                ui.rect(r.x-10,r.y-4,4,30,CAFE);
                ui.rect(r.x-10,r.y-4,10,3,CAFE);
            }
            if(v.lupa()&&!ficha.clasificada()&&!encontrada)
                ui.botones.add(new RenderJuego.Boton(r,"invest:hallazgo:"+zona.name(),zona.etiqueta));
        }
        if (!v.lupa() || ficha.clasificada()) {
            lente.dibujar(ui, 0, 0, false, false, false);
            return;
        }
        int x=ui.ratonX,y=ui.ratonY;
        boolean teclado = x < 0;
        if(teclado) {
            var b=ui.botones.get(Math.floorMod(ui.juego.foco,ui.botones.size()));
            if(!b.accion().startsWith("invest:hallazgo:")) {
                lente.dibujar(ui, 0, 0, false, false, false);
                return;
            }
            x=(int)b.area().getCenterX();y=(int)b.area().getCenterY();
        }
        if (!HOJA.contains(x,y)) {
            lente.dibujar(ui, 0, 0, false, false, false);
            return;
        }
        boolean objetivo=false;
        for(var z:zonas.entrySet()) if(!ficha.encontro(z.getKey())&&z.getValue().contains(x,y)) objetivo=true;
        lente.dibujar(ui,x,y,true,teclado||objetivo,objetivo);
    }

    private void destello(int x,int y,double t) {
        int r=2+(int)((1.65-t)*22);
        ui.rect(x-r,y,2*r+1,2,ui.oro);ui.rect(x,y-r,2,2*r+1,ui.oro);
        ui.rect(x+13,y-8,3,3,ui.papel);ui.rect(x-14,y+9,3,3,ui.papel);
    }



    private void paginaAviso(SesionMisionUno.Vista s,Caso caso) {
        ui.texto("CIVITAS  /  AVISO A LA COMUNIDAD",359,198,17,TINTA,true);
        ui.rect(359,212,506,2,CAFE);
        ui.parrafo(caso.nombre(),359,252,500,23,TINTA);
        ui.texto(caso.veredicto().etiqueta,359,288,15,VIOLETA,true);
        tarjeta(359,317,506,173,"MENSAJE DE SOFI");
        ui.parrafo(aviso(caso),372,373,478,22,TINTA);
        ui.personajeSofi("mision1/sofi-caminar",4,4,0,374,518,58,90);
        ui.texto("Sofi  /  Ciudad Nova",444,579,15,VIOLETA,true);
        if(s.resueltas().get(caso.id()-1)) sello(caso,584,524);
    }

    private void accionesAviso(SesionMisionUno.Vista s,Caso caso) {
        boolean publicada=s.resueltas().get(caso.id()-1);
        ui.texto(publicada?"AVISO PREPARADO":"PUBLICACIÓN",938,164,18,ui.oro,true);
        ui.parrafo(publicada?"Sofi lleva el aviso en su libreta. Regresa al parque para continuar.":
            "El aviso recoge lo que encontraste con la lupa. Podrás compartirlo con la comunidad.",
            938,210,286,17,ui.papel);
        if(!publicada) ui.boton(accionAviso(caso),"invest:comunicar",938,317,286,true);
        else ui.boton("Guardar y continuar","volver",938,317,286,true);
        ui.boton("Ver el expediente","invest:inspeccion",938,384,286,false);
    }

    private void sello(Caso caso,int x,int y) {
        double r=ui.juego.movimientoReducido?0:Math.clamp((ui.juego.investigacionSofi.finSello-ui.juego.animacion)/.58,0,1);
        Graphics2D previo=ui.g;
        ui.g=(Graphics2D)previo.create();
        ui.g.translate(x+124,y+24-27*r);
        ui.g.rotate(-.1-r*.08);
        ui.g.scale(1+r*.28,1+r*.28);
        ui.g.setColor(SELLO);ui.g.setStroke(new BasicStroke(2));
        ui.g.drawRoundRect(-125,-24,250,48,10,10);
        ui.g.drawRoundRect(-120,-19,240,38,6,6);
        ui.texto(selloAviso(caso),-110,5,14,SELLO,true);
        ui.g.dispose();ui.g=previo;
    }

    void conversar(Vista v) {
        Vecino vecino=vecino(v.vecino());
        Caso caso=caso(vecino.caso());
        boolean informado=v.orientado(vecino.id());
        ui.cabecera("En el tablón del parque","Ciudadanos informados  "+v.ciudadanos()+" / "+VECINOS.size());
        ui.panel(41,157,361,480,ui.tinta);
        ui.texto(vecino.nombre(),69,207,26,ui.oro,true);
        double t=ui.juego.movimientoReducido?0:ui.juego.animacion;
        reaccion(vecino,informado,t,147,247,109,151);
        ui.parrafo(informado?vecino.reaccion():vecino.pregunta(),68,455,306,18,ui.papel);
        tarjeta(452,187,743,360,"AVISO DE SOFI  /  "+caso.nombre().toUpperCase(java.util.Locale.ROOT));
        ui.parrafo(aviso(caso),481,285,660,23,TINTA);
        if(informado) ui.personajeSofi("mision1/sofi-caminar",4,4,2,1109,352,49,77);
        ui.texto("Lo que revisó Sofi",481,391,15,VIOLETA,true);
        ui.parrafo(vecino.prueba(),481,419,622,16,TINTA);
        if(informado) sello(caso,910,467);
        ui.boton(informado?"Continuar":"Mostrar el aviso a "+vecino.nombre(),
            informado?"volver":"invest:presentar",452,574,427,true);
        ui.pie("Acércate a cada vecino para conversar sobre lo que averiguaste.");
    }

    void reaccionEnParque(Vecino vecino,int x,int y) {
        Caso caso=caso(vecino.caso());
        double t=ui.juego.movimientoReducido?0:ui.juego.animacion;
        ui.rect(x,y,299,162,ui.tinta);
        reaccion(vecino,true,t,x+7,y+19,58,85);
        ui.texto(vecino.nombre()+" se calmó",x+73,y+25,14,ui.oro,true);
        tarjeta(x+72,y+38,219,36,selloAviso(caso));
        ui.parrafo(vecino.reaccion(),x+73,y+94,210,13,ui.papel);
    }

    private void reaccion(Vecino vecino,boolean informado,double t,int x,int y,int ancho,int alto) {
        double avance=informado&&t>0?1-Math.clamp((ui.juego.investigacionSofi.finSello-t)/.62,0,1):1;
        int dx=0,dy=0,pose=0,direccion=0;
        if(informado) {
            double pulso=Math.sin(Math.PI*avance);
            switch(vecino.id()) {
                case 0 -> {dy=-(int)(pulso*19);pose=(int)(avance*3)%3;}
                case 3 -> {direccion=avance<.53?1:0;dx=(int)(pulso*10);}
                case 4 -> {dy=(int)(pulso*7);pose=avance<.73?2:0;}
                case 7 -> {direccion=avance<.6?2:0;pose=(int)(avance*4)%4;}
                default -> { }
            }
        }
        ui.retratoVecino(vecino.id(),direccion,pose,x+dx,y+dy*alto/151,ancho,alto);
    }

    private void levantarPapel(int origenX,int origenY,double t) {
        double u=suave(t);
        int x=(int)(origenX+(612-origenX)*u);
        int y=(int)(origenY+(111-origenY)*u-37*Math.sin(t*Math.PI));
        Graphics2D previo=ui.g;
        ui.g=(Graphics2D)previo.create();
        ui.g.translate(x,y);
        ui.g.rotate(Math.sin(t*8)*.23*(1-u));
        if(t<.6) {
            int w=24+(int)(suave(t/.6)*100);
            ui.rect(-w/2,-w/5,w,w*2/5,CAFE);
            ui.rect(-w/2+2,-w/5-2,w-4,w*2/5,FIBRA);
            ui.rect(-w/3,-2,w*2/3,2,OCRE);
        } else {
            int w=124+(int)(suave((t-.6)/.4)*490);
            dibujarRollo(-w/2,-12,w,24+(int)(13*u));
        }
        ui.g.dispose();ui.g=previo;
    }

    private static double suave(double t) {
        double u=Math.clamp(t,0,1);
        return u*u*(3-2*u);
    }
}
