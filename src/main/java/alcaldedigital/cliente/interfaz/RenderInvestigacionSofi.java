package alcaldedigital.cliente.interfaz;

import alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import alcaldedigital.cliente.recursos.PaletaMorada;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import static alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;

/** Dibuja los expedientes y sus controles en el mismo lienzo del juego. */
final class RenderInvestigacionSofi {
    private static final Color PAPEL_PERIODICO=new Color(0xF2E5CE), TINTA_PERIODICO=new Color(0x292136);
    private static final Color LINEA_PERIODICO=new Color(0xAC93A4);
    private static final Rectangle PAGINA=new Rectangle(29,139,759,528);
    private static final Rectangle[] ZONAS={
        new Rectangle(52,215,712,82),new Rectangle(52,307,382,324),
        new Rectangle(52,178,712,32),new Rectangle(450,503,314,128)
    };
    private final RenderJuego ui;
    private final PergaminoSofi pergamino;
    private final BufferedImage muestra=new BufferedImage(75,75,BufferedImage.TYPE_INT_ARGB);

    RenderInvestigacionSofi(RenderJuego ui) {this.ui=ui;pergamino=new PergaminoSofi(ui);}

    void parque(SesionMisionUno.Vista s,Vista v) {
        if(v.premium()) {parquePremium(s,v);return;}
        boolean dialogo=s.fase()==SesionMisionUno.Fase.CRISIS;
        ui.panel(912,114,344,484,PaletaMorada.PANEL);
        ui.texto(v.calmaClasica()?"CALMA A LOS VECINOS":dialogo?"ORIENTA A LOS VECINOS":"PERIÓDICOS DEL PARQUE",934,148,17,ui.oro,true);
        ui.parrafo(v.calmaClasica()?"Acércate a los vecinos alterados y escúchalos antes de que termine el tiempo.":
            dialogo?"Acércate a los vecinos con dudas y muestra una prueba de la libreta.":
            "Busca los papeles en el suelo. Recoge el primero junto a la banca.",934,181,300,17,ui.papel);
        if(v.calmaClasica()) {
            ui.texto("PERSONAS CALMADAS   "+s.calmados()+" / 8",934,300,16,ui.oro,true);
            ui.texto("PAREJAS QUE SALIERON   "+s.parejasFuera()+" / 4",934,346,14,ui.papel,true);
        } else if(dialogo) {
            for(int i=0;i<VECINOS.size();i++) {
                var vecino=VECINOS.get(i);
                fichaParque(vecino.nombre(),v.orientado(vecino.id())?"Orientado":"Tiene una duda",i,v.orientado(vecino.id()));
            }
            ui.texto("VECINOS ORIENTADOS   "+v.ciudadanos()+" / 4",934,526,15,ui.oro,true);
        } else {
            for(var caso:CASOS) {
                var ficha=v.ficha(caso.id());
                String estado=ficha.clasificada()?"Clasificado":!v.ficha(1).clasificada()&&caso.id()!=1?
                    "Después del primer caso":ficha.encontrados()>0?"Investigación en curso":rumbo(s,caso);
                fichaParque(caso.nombre(),estado,caso.id()-1,ficha.clasificada());
            }
            ui.texto("CLASIFICADOS   "+v.clasificados()+" / 4",934,526,15,ui.oro,true);
        }
        if(v.escena()==Escena.NINGUNA&&v.clasificados()==4&&!dialogo)
            ui.boton("Empezar minijuego","invest:irVecinos",934,541,300,true);
        ui.panel(24,608,1232,72,PaletaMorada.PANEL);
        if(v.escena()!=Escena.NINGUNA) {
            escena(v);
        } else if(dialogo) {
            var vecino=ui.juego.investigacionSofi.vecinoCercano(s,v);
            if(vecino!=null) {
                ui.texto(vecino.nombre()+" quiere hablar contigo.",42,648,20,ui.papel,true);
                ui.boton(ui.juego.mandoActivo?"Conversar [A]":"Conversar [E]","interactuar",918,620,288,true);
            } else ui.parrafo("Busca a los vecinos con el aviso de conversación.",42,648,842,18,ui.papel);
        } else {
            var cerca=ui.juego.investigacionSofi.papelCercano(s);
            if(cerca!=null) {
                boolean bloqueado=cerca.id()!=1&&!v.ficha(1).clasificada();
                ui.parrafo(bloqueado?"Primero investiga el rumor del agua, junto a la banca.":cerca.nombre(),
                    42,648,842,19,ui.papel);
                if(!bloqueado) ui.boton(ui.juego.mandoActivo?"Investigar [A]":"Investigar [E]",
                    "interactuar",918,620,288,true);
            } else ui.parrafo(v.clasificados()==4?"La libreta está lista. Ve con los vecinos.":
                "Acércate a un periódico. Abre la libreta para consultar el progreso.",42,648,842,18,ui.papel);
        }
        ui.pie(ui.juego.mandoActivo?"Stick / cruceta: moverte · A: interactuar · X: libreta · Menú: pausa":
            "WASD / flechas: moverte · E: interactuar · Tab: libreta · Esc/P: pausa");
    }
    private void parquePremium(SesionMisionUno.Vista s,Vista v) {
        boolean vecinos=s.fase()==SesionMisionUno.Fase.CRISIS;
        long avisos=s.resueltas().stream().filter(Boolean::booleanValue).count();
        boolean listo=avisos==CASOS.size();
        ui.panel(912,114,344,484,PaletaMorada.PANEL);
        ui.texto(vecinos?"CALMA A LOS VECINOS":"SALA DE REDACCIÓN",934,148,17,ui.oro,true);
        if(vecinos) panelDeCalma(s,v);
        else {
            ui.parrafo(listo?"Los cuatro avisos están listos. Usa el tablón junto a la fuente para reunir a los vecinos.":
            s.resueltas().getFirst()?"Explora los senderos, investiga cada expediente y prepara su aviso.":
                "Empieza por el papel que cayó junto a la banca del noroeste.",934,181,296,16,ui.papel);
            for(Caso caso:CASOS) {
                var ficha=v.ficha(caso.id());
                boolean preparado=s.resueltas().get(caso.id()-1);
                String estado=preparado?"Aviso preparado":ficha.clasificada()?"Publicación pendiente":
                    ficha.encontrados()>0?"Investigación en curso":rumbo(s,caso);
                fichaParque(caso.nombre(),estado,caso.id()-1,preparado);
            }
            ui.texto("AVISOS PREPARADOS   "+avisos+" / 4",934,535,15,ui.oro,true);
        }
        ui.panel(24,608,1232,72,PaletaMorada.PANEL);
        if(v.escena()!=Escena.NINGUNA) escena(v);
        else if(vecinos) {
            var cerca=ui.juego.investigacionSofi.vecinoCercano(s,v);
            if(cerca!=null) {
                boolean escuchado=cerca.id()==v.vecino()||s.peatones().get(cerca.id()).conversaciones()>0;
                ui.parrafo(escuchado?cerca.nombre()+" · "+cerca.pregunta():cerca.nombre()+" está preocupado. Escucha su duda.",
                    42,637,838,17,ui.papel);
                String accion=escuchado?"Calmar con el aviso":"Escuchar";
                ui.boton(accion+(ui.juego.mandoActivo?" [A]":" [E]"),"interactuar",918,620,288,true);
            } else ui.texto("Acércate a un vecino alterado para escucharlo.",42,648,17,ui.papel,false);
        } else if(listo&&ui.juego.investigacionSofi.cercaDelTablon(s)) {
            ui.texto("Tablón del parque",42,648,19,ui.papel,true);
            ui.boton(ui.juego.mandoActivo?"Convocar [A]":"Convocar [E]","interactuar",918,620,288,true);
        } else {
            var papel=ui.juego.investigacionSofi.papelCercano(s);
            if(papel!=null&&(!listo||!s.resueltas().get(papel.id()-1))) {
                boolean disponible=papel.id()==1||s.resueltas().getFirst();
                ui.texto(disponible?papel.nombre():"Prepara primero el aviso del rumor del agua.",42,648,19,ui.papel,true);
                if(disponible) ui.boton(ui.juego.mandoActivo?"Investigar [A]":"Investigar [E]","interactuar",918,620,288,true);
            } else ui.texto(listo?"Busca el tablón al este de la fuente.":"Busca un papel o consulta los casos en la libreta.",
                42,648,17,ui.papel,false);
        }
        if(vecinos) ui.pie(ui.juego.mandoActivo?"Stick / cruceta: moverte · A: escuchar y calmar · Menú: pausa":
            "WASD / flechas: moverte · E: escuchar y calmar · Esc/P: pausa");
        else ui.pie(ui.juego.mandoActivo?"Stick / cruceta: moverte · A: interactuar · X: libreta · Menú: pausa":
            "WASD / flechas: moverte · E: interactuar · Tab: libreta · Esc/P: pausa");
    }
    private void panelDeCalma(SesionMisionUno.Vista s,Vista v) {
        boolean llegando=v.escena()==Escena.VECINOS;
        boolean terminado=v.escena()==Escena.CIERRE;
        double proporcion=s.tiempo()/s.dificultad().segundos();
        Color reloj=!llegando&&proporcion<=.25?PaletaMorada.PELIGRO:ui.oro;
        ui.texto((int)Math.ceil(s.tiempo())+" s",934,196,30,reloj,true);
        ui.texto("CALMADOS  "+v.ciudadanos()+" / "+VECINOS.size(),1050,191,14,ui.papel,true);
        ui.rect(934,213,299,5,ui.tinta);
        ui.rect(934,213,(int)(299*proporcion),5,reloj);
        for(int i=0;i<VECINOS.size();i++) {
            var vecino=VECINOS.get(i);
            boolean calmado=v.orientado(vecino.id());
            boolean escuchado=s.peatones().get(vecino.id()).conversaciones()>0||v.vecino()==vecino.id();
            String estado=calmado?"En calma":llegando?"En camino":terminado?"Pendiente":escuchado?"Escuchado":"Alterado";
            int y=237+i*43;
            ui.rect(934,y,299,38,ui.tinta);
            ui.texto(calmado?"✓  "+vecino.nombre():vecino.nombre(),947,y+25,15,calmado?ui.oro:ui.papel,true);
            ui.texto(estado,1114,y+24,12,calmado?ui.oro:ui.tenue,false);
        }
        var estado=ui.juego.investigacionSofi;
        var reciente=vecino(estado.vecinoAviso);
        if(reciente!=null&&estado.finRespuestaVecino>ui.juego.animacion) pergamino.reaccionEnParque(reciente,934,417);
        else if(!terminado) ui.parrafo(llegando?"El reloj empieza cuando los vecinos llegan al tablón.":
            "Escucha con "+(ui.juego.mandoActivo?"A":"E")+" y vuelve a pulsar para tranquilizar con el aviso.",
            934,442,295,16,ui.papel);
    }
    private void fichaParque(String nombre,String estado,int fila,boolean hecha) {
        int y=246+fila*64;
        ui.rect(934,y,299,54,ui.tinta);
        ui.texto(hecha?"✓  "+nombre:nombre,946,y+22,15,hecha?ui.oro:ui.papel,true);
        ui.texto(estado,946,y+42,13,ui.tenue,false);
    }
    private static String rumbo(SesionMisionUno.Vista s,Caso caso) {
        if(s.dificultad()==SesionMisionUno.Dificultad.DIFICIL) return caso.lugar();
        double dx=caso.x()-s.x(),dy=caso.y()-s.y();
        if(Math.hypot(dx,dy)<=29) return "Al alcance";
        return Math.abs(dx)>Math.abs(dy)?dx>0?"→ Este":"← Oeste":dy>0?"↓ Sur":"↑ Norte";
    }
    private void escena(Vista v) {
        boolean tiempoAgotado=v.premium()&&v.escena()==Escena.CIERRE&&v.ciudadanos()<VECINOS.size();
        boolean inicio=v.segundosEscena()<(v.premium()&&v.escena()==Escena.VECINOS?2.9:v.escena().segundos*.53);
        String titulo=switch(v.escena()) {
            case RUMORES -> inicio?"EL RUMOR ECHA A VOLAR":"SOFI BUSCA EL ORIGEN";
            case VECINOS -> v.premium()?"SOFI CONVOCA A LOS VECINOS":inicio?"LOS TITULARES LLEGAN AL PARQUE":v.calmaClasica()?"HORA DE CALMAR A LOS VECINOS":"HORA DE MOSTRAR LAS PRUEBAS";
            case CIERRE -> inicio?(tiempoAgotado?"SE ACABÓ EL TIEMPO":"EL PARQUE RECUPERA LA CALMA"):"MR. PALOMO SE DESPIDE";
            default -> "";
        };
        String dialogo=switch(v.escena()) {
            case RUMORES -> inicio?"Cuenta Anónima: "+caso(1).titular():"Sofi: Antes de compartir, voy a buscar de dónde salió esto.";
            case VECINOS -> v.premium()?(inicio?"Sofi: ¡Vecinos! Traigan sus dudas. Tengo los avisos listos.":"Los vecinos se acercan al tablón para hablar con Sofi."):
                inicio?"Álex: ¿El agua está contaminada?  Nico: Yo vi otra versión de esa publicación.":
                v.calmaClasica()?"Sofi: Voy a escuchar a los vecinos antes de que el rumor siga creciendo.":
                    "Sofi: Tengo los documentos. Quiero escuchar sus dudas y mostrarles lo que encontré.";
            case CIERRE -> tiempoAgotado?
                (inicio?"Sofi: Me faltó hablar con algunos vecinos. Los avisos siguen en el tablón.":
                    "Mr. Palomo: ¡Cucurrucucú! Al rumor le bastó con llegar primero."):
                inicio?"Doña Luz: Ya sé dónde buscar la fuente. Primero pregunto, después comparto.":
                "Mr. Palomo: ¡Cucurrucú! Parecía más fácil antes de que apareciera esa libreta.";
            default -> "";
        };
        ui.panel(24,114,866,39,new Color(20,12,29,235));
        ui.texto(titulo,42,141,17,ui.oro,true);
        ui.parrafo(dialogo,42,637,838,18,ui.papel);
        if(!v.premium()||v.escena()==Escena.CIERRE)
            ui.boton(ui.juego.mandoActivo?"Saltar [A]":"Saltar [E]","interactuar",918,620,288,false);
    }

    void dibujar(SesionMisionUno.Vista s,Vista v) {
        if(ui.juego.investigacionSofi.libreta||(v.abierto()==0&&v.vecino()<0)) {libreta(s,v);return;}
        if(v.premium()) {
            if(v.vecino()>=0) pergamino.conversar(v);
            else pergamino.dibujar(s,v,caso(v.abierto()));
            return;
        }
        if(v.vecino()>=0) {conversacion(v);return;}
        Caso caso=caso(v.abierto());
        if(v.contraste()) contraste(s,v,caso);
        else periodico(v,caso);
    }
    private void periodico(Vista v,Caso caso) {
        ui.cabecera("Periódico del parque",caso.lugar());
        Ficha ficha=v.ficha(caso.id());
        Color hoja=ui.juego.contraste?Color.WHITE:PAPEL_PERIODICO;
        TemaPixel.marco(ui.g,PAGINA.x,PAGINA.y,PAGINA.width,PAGINA.height,hoja,LINEA_PERIODICO,true);
        ui.texto("CIVITAS  /  EDICIÓN DEL PARQUE",53,169,19,TINTA_PERIODICO,true);
        ui.texto(caso.edicion(),55,197,15,TINTA_PERIODICO,false);
        selloImpreso(caso,Zona.EDICION,55,208,698);
        ui.rect(52,211,712,2,LINEA_PERIODICO);
        ui.parrafo(caso.titular(),55,244,699,24,TINTA_PERIODICO);
        selloImpreso(caso,Zona.TITULAR,56,289,698);
        ilustracion(caso);
        ui.parrafo(caso.pieFoto(),55,574,364,15,TINTA_PERIODICO);
        selloImpreso(caso,Zona.IMAGEN,56,616,365);
        ui.rect(443,307,1,323,LINEA_PERIODICO);
        ui.texto("LA NOTA",465,331,17,TINTA_PERIODICO,true);
        ui.parrafo(caso.nota(),465,365,286,17,TINTA_PERIODICO);
        ui.rect(458,500,303,1,LINEA_PERIODICO);
        ui.texto("FUENTES Y CRÉDITOS",465,530,16,TINTA_PERIODICO,true);
        ui.parrafo(caso.credito(),465,557,285,15,TINTA_PERIODICO);
        selloImpreso(caso,Zona.FUENTES,465,617,285);
        ui.panel(810,139,442,528,PaletaMorada.PANEL);
        ui.texto("EVIDENCIAS   "+ficha.encontrados()+" / "+caso.hallazgos().size(),832,175,19,ui.oro,true);
        ui.parrafo(v.lupa()?"Explora el papel. Haz clic en los detalles que ayuden a comprobar la noticia.":
            "Activa la lupa y revisa el periódico.",832,209,392,17,ui.papel);
        ui.boton((v.lupa()?"Guardar lupa":"Activar lupa")+" ["+ui.teclaLupaSofi()+"]","invest:lupa",832,267,388,!v.lupa());
        int y=344;
        for(var hallazgo:caso.hallazgos()) if(ficha.encontro(hallazgo.zona())) {
            ui.texto("✓  "+hallazgo.zona().etiqueta,832,y,16,ui.oro,true);
            ui.parrafo(hallazgo.detalle(),851,y+19,365,13,ui.papel);
            y+=54;
        }
        if(!v.respuesta().isBlank()) ui.parrafo(v.respuesta(),832,520,390,15,ui.tenue);
        if(ui.juego.animacion<ui.juego.investigacionSofi.finAviso) {
            ui.rect(824,594,402,24,ui.tinta);
            ui.texto("EVIDENCIA ENCONTRADA   "+ficha.encontrados()+" / "+caso.hallazgos().size(),832,612,14,ui.oro,true);
        }
        if(ficha.encontrados()==caso.hallazgos().size()) ui.boton("Abrir contraste","invest:contraste",832,624,388,true);
        else ui.texto("Completa las pistas para abrir el contraste.",832,648,14,ui.tenue,false);
        dibujarZonas(v,caso,ficha,ui.juego.misionUno.vista().dificultad());
        if(ui.juego.animacion<ui.juego.investigacionSofi.finRecogida&&!ui.juego.movimientoReducido) {
            double restante=ui.juego.investigacionSofi.finRecogida-ui.juego.animacion;
            ui.imagen("iconos/06_fuente",802,452+(int)(180*restante),35,35);
        }
        ui.pie(ui.juego.mandoActivo?"Cruceta: enfocar · A: seleccionar · X: libreta · B: volver":
            "Q: lupa · Clic: examinar · Flechas y Enter: seleccionar · Tab: libreta · Esc: volver");
    }
    private void ilustracion(Caso caso) {
        if(caso.id()==3) {
            ui.rect(53,309,380,240,new Color(0x4B3157));
            ui.imagen("iconos/07_compartir",72,326,44,44);
            ui.texto("REENVÍO VECINAL",131,351,16,ui.papel,true);
            ui.rect(72,391,344,120,PAPEL_PERIODICO);
            ui.parrafo("Hay más palomas que votantes en Ciudad Nova.",88,422,311,18,TINTA_PERIODICO);
            for(int x=66;x<423;x+=12) ui.rect(x,382,7,2,LINEA_PERIODICO);
            return;
        }
        var arte=ui.arteParque();
        int x=caso.id()==2?arte.getWidth()/2:arte.getWidth()/3;
        int y=caso.id()==4?arte.getHeight()/2:arte.getHeight()/3;
        int ax=arte.getWidth()*42/100,ay=arte.getHeight()*39/100;
        ui.g.drawImage(arte,53,309,433,550,x,y,x+ax,y+ay,null);
    }
    private void selloImpreso(Caso caso,Zona zona,int x,int y,int ancho) {
        var hallazgo=caso.hallazgo(zona);
        if(hallazgo!=null) {
            Font fuente=ui.tema.fuente(8,false);
            String detalle=hallazgo.detalle();
            while(!detalle.isEmpty()&&ui.tema.ancho(ui.g,detalle,fuente)>ancho) detalle=detalle.substring(0,detalle.length()-1);
            ui.texto(detalle,x,y,8,TINTA_PERIODICO,false);
        }
    }
    private void dibujarZonas(Vista v,Caso caso,Ficha ficha,SesionMisionUno.Dificultad dificultad) {
        for(Zona zona:Zona.values()) {
            Rectangle r=ZONAS[zona.ordinal()];
            if(ficha.encontro(zona)) {
                ui.g.setColor(new Color(0x7B4D2E));ui.g.drawRect(r.x,r.y,r.width,r.height);
                ui.texto("✓",r.x+r.width-20,r.y+19,15,TINTA_PERIODICO,true);
            } else if(v.lupa()&&dificultad==SesionMisionUno.Dificultad.FACIL&&caso.hallazgo(zona)!=null) {
                ui.rect(r.x+2,r.y+2,14,3,new Color(0x845D7E));
                ui.rect(r.x+2,r.y+2,3,14,new Color(0x845D7E));
            }
            if(v.lupa()) ui.botones.add(new RenderJuego.Boton(r,"invest:hallazgo:"+zona.name(),zona.etiqueta));
        }
        if(!v.lupa()) return;
        int x=ui.ratonX,y=ui.ratonY;
        if(x<0) {
            var b=ui.botones.get(Math.floorMod(ui.juego.foco,ui.botones.size()));
            if(!b.accion().startsWith("invest:hallazgo:")) return;
            x=(int)b.area().getCenterX();y=(int)b.area().getCenterY();
        }
        if(!PAGINA.contains(x,y)) return;
        ampliar(x,y);
        for(Zona z:Zona.values()) if(ZONAS[z.ordinal()].contains(x,y)) {
            int lx=Math.clamp(x-100,37,560),ly=Math.clamp(y+76,147,620);
            String etiqueta=z.etiqueta+(dificultad!=SesionMisionUno.Dificultad.DIFICIL&&caso.hallazgo(z)!=null?
                " · detalle para revisar":"");
            ui.rect(lx,ly,208,21,ui.tinta);
            ui.texto(etiqueta,lx+6,ly+15,11,ui.papel,false);
            break;
        }
    }
    private void ampliar(int x,int y) {
        int sx=Math.clamp(x-37,PAGINA.x,PAGINA.x+PAGINA.width-75);
        int sy=Math.clamp(y-37,PAGINA.y,PAGINA.y+PAGINA.height-75);
        Graphics2D copia=muestra.createGraphics();
        copia.drawImage(ui.lienzo,0,0,75,75,sx,sy,sx+75,sy+75,null);
        copia.dispose();
        var lente=new Ellipse2D.Double(x-72,y-72,144,144);
        Graphics2D g=(Graphics2D)ui.g.create();
        g.clip(lente);
        g.drawImage(muestra,x-72,y-72,144,144,null);
        g.dispose();
        ui.g.setStroke(new BasicStroke(8));
        ui.g.setColor(ui.tinta);ui.g.drawLine(x+49,y+49,x+88,y+88);
        ui.g.setStroke(new BasicStroke(3));
        ui.g.setColor(ui.oro);ui.g.draw(lente);ui.g.drawLine(x+52,y+52,x+87,y+87);
        ui.rect(x-3,y,7,1,ui.tinta);ui.rect(x,y-3,1,7,ui.tinta);
    }

    private void contraste(SesionMisionUno.Vista s,Vista v,Caso caso) {
        ui.cabecera("Libreta / Comprobación rápida","Compara el titular con la fuente y elige una conclusión.");
        Ficha ficha=v.ficha(caso.id());
        ui.panel(29,139,487,527,PaletaMorada.PANEL);
        ui.texto("LO QUE CIRCULA",49,173,17,ui.oro,true);
        ui.parrafo(caso.titular(),49,209,442,21,ui.papel);
        ui.texto("PISTAS DE TU LIBRETA",49,290,16,ui.oro,true);
        int y=323;
        for(var h:caso.hallazgos()) y=ui.parrafo("✓ "+h.detalle(),49,y,435,16,ui.papel)+12;
        if(!ficha.clasificada()&&!v.respuesta().isBlank()) {
            ui.rect(43,489,457,100,ui.tinta);
            ui.parrafo(v.respuesta(),58,518,424,16,ui.tenue);
        }
        ui.boton("Revisar periódico","invest:inspeccion",49,608,210,false);
        ui.boton("Libreta [Tab]","invest:libreta",270,608,217,false);
        ui.panel(535,139,716,527,PaletaMorada.PANEL);
        ui.texto("FUENTES DE CONTRASTE",555,170,17,ui.oro,true);
        for(int i=0;i<caso.documentos().size();i++) {
            var documento=caso.documentos().get(i);
            int fila=187+i*106;
            ui.rect(556,fila,672,96,ui.tinta);
            ui.imagen("iconos/04_libreta",568,fila+11,27,27);
            ui.texto(documento.nombre(),606,fila+28,16,ui.oro,true);
            ui.parrafo(documento.detalle(),570,fila+52,630,15,ui.papel);
        }
        if(ficha.clasificada()) {
            clasificacionLista(s,v,caso);
        } else {
            ui.texto("¿QUÉ DEMUESTRA LA FUENTE?",556,422,16,ui.oro,true);
            for(int i=0;i<caso.razones().size();i++)
                ui.boton(caso.razones().get(i),"invest:comprobar:"+i,555,440+i*62,674,false);
        }
        ui.pie("Elige la conclusión que se sostiene con lo que acabas de leer.");
    }
    private void clasificacionLista(SesionMisionUno.Vista s,Vista v,Caso caso) {
        double golpe=ui.juego.movimientoReducido?0:
            Math.max(0,(ui.juego.investigacionSofi.finAviso-ui.juego.animacion)/.36);
        int sello=402-(int)Math.round(10*golpe*golpe);
        TemaPixel.marco(ui.g,555,sello,308,31,ui.tinta,ui.oro,false);
        ui.texto("✓  "+caso.veredicto().etiqueta.toUpperCase(java.util.Locale.ROOT),569,sello+23,17,ui.oro,true);
        ui.parrafo(caso.conclusion(),555,450,658,17,ui.papel);
        if(!s.resueltas().get(caso.id()-1)) {
            int ancho=213;
            ui.boton("Compartir contexto","invest:compartir:"+caso.id(),555,523,ancho,false);
            if(caso.veredicto()!=Veredicto.VERDADERA)
                ui.boton("Reportar con pruebas","invest:reportar:"+caso.id(),782,523,ancho,false);
            ui.boton("Ignorar","invest:ignorar:"+caso.id(),1009,523,ancho,false);
        } else ui.texto("Decisión de difusión registrada",555,547,15,ui.tenue,false);
        ui.boton(v.clasificados()==4?"Empezar minijuego":"Buscar otro periódico",
            v.clasificados()==4?"invest:irVecinos":"volver",555,608,674,true);
    }
    private void libreta(SesionMisionUno.Vista s,Vista v) {
        long preparados=s.resueltas().stream().filter(Boolean::booleanValue).count();
        ui.cabecera("Libreta de Sofi",v.premium()?preparados+" de 4 avisos preparados":v.clasificados()+" de 4 casos clasificados");
        for(Caso caso:CASOS) {
            var ficha=v.ficha(caso.id());
            int y=138+(caso.id()-1)*116;
            ui.panel(29,y,1220,104,PaletaMorada.PANEL);
            ui.texto(caso.nombre(),48,y+31,19,ui.oro,true);
            String estado=ficha.clasificada()?caso.veredicto().etiqueta:ficha.encontrados()+" / "+caso.hallazgos().size()+" evidencias";
            ui.texto(estado,962,y+31,16,ui.papel,true);
            String detalle=v.premium()&&s.resueltas().get(caso.id()-1)?aviso(caso):ficha.clasificada()?caso.conclusion():caso.lugar();
            ui.parrafo(detalle,48,y+62,1135,17,ui.papel);
        }
        if(!v.premium()&&v.clasificados()==4&&s.fase()==SesionMisionUno.Fase.CIVITAS)
            ui.boton("Empezar minijuego","invest:irVecinos",29,610,320,true);
        ui.boton("Cerrar libreta","invest:libreta",910,610,339,false);
        ui.pie(ui.juego.mandoActivo?"X: abrir o cerrar libreta":"Tab: abrir o cerrar libreta");
    }
    private void conversacion(Vista v) {
        Vecino vecino=vecino(v.vecino());
        ui.cabecera("Conversación en el parque",v.ciudadanos()+" de 4 vecinos orientados");
        ui.panel(29,139,1223,506,PaletaMorada.PANEL);
        ui.retratoVecino(vecino.id(),54,151,43,66);
        ui.texto(vecino.nombre(),104,193,23,ui.oro,true);
        ui.parrafo(vecino.pregunta(),57,234,1155,22,ui.papel);
        if(v.orientado(vecino.id())) {
            ui.texto("CIUDADANO INFORMADO   "+v.ciudadanos()+" / 4",57,331,19,ui.oro,true);
            ui.parrafo(vecino.reaccion(),57,378,1125,22,ui.papel);
            ui.rect(57,454,1080,75,ui.tinta);
            ui.imagen("iconos/04_libreta",73,470,35,35);
            ui.texto("Prueba presentada",123,481,16,ui.oro,true);
            ui.texto(vecino.prueba(),123,507,16,ui.papel,false);
            ui.boton("Continuar","volver",57,576,437,true);
        } else {
            var alternativas=new Vecino[]{VECINOS.get((VECINOS.indexOf(vecino)+1)%VECINOS.size()),vecino};
            if(vecino.id()%2==1) {alternativas[1]=alternativas[0];alternativas[0]=vecino;}
            ui.texto("¿QUÉ LE MUESTRAS?",57,307,17,ui.oro,true);
            for(int i=0;i<alternativas.length;i++) ui.boton(alternativas[i].prueba(),
                "invest:mostrar:"+alternativas[i].caso(),57,324+i*58,824,false);
            ui.boton("Repetir el titular","invest:mostrar:0",57,440,824,false);
            if(!v.respuesta().isBlank()) ui.parrafo(v.respuesta(),57,537,1141,18,ui.tenue);
            ui.boton("Consultar libreta","invest:libreta",57,576,437,false);
        }
        ui.pie("Relaciona la duda del vecino con la evidencia de tu investigación.");
    }
    void resultado(SesionMisionUno.Vista s,Vista v) {
        ui.cabecera("BUENO".equals(s.desenlace())?"El parque recupera la calma":"El rumor sigue circulando",
            v.calmaClasica()||v.premium()?"Resumen de la investigación y de la convocatoria":"Investigación ciudadana completada");
        ui.panel(29,139,1223,489,PaletaMorada.PANEL);
        ui.parrafo(s.mensaje(),51,185,1153,22,ui.papel);
        ui.texto("CASOS  "+v.clasificados()+"/4",52,268,20,ui.oro,true);
        ui.texto("EVIDENCIAS  "+v.evidencias()+"/"+totalEvidencias(),343,268,20,ui.oro,true);
        ui.texto(v.calmaClasica()?"CALMADOS  "+s.calmados()+"/8":"CALMADOS  "+v.ciudadanos()+"/4",679,268,20,ui.oro,true);
        ui.texto("PUNTOS  "+s.puntos(),960,268,20,ui.oro,true);
        for(Caso caso:CASOS) {
            int y=319+(caso.id()-1)*67;
            ui.rect(52,y,1175,54,ui.tinta);
            ui.texto(caso.nombre(),68,y+33,17,ui.papel,true);
            ui.texto(caso.veredicto().etiqueta,749,y+33,17,ui.oro,true);
        }
        ui.boton("Elegir otra misión","jugar",29,641,340,true);
        ui.boton("Repetir misión 1","repetirMisionUno",390,641,340,false);
        ui.boton("Menú principal","menu",751,641,340,false);
    }
}
