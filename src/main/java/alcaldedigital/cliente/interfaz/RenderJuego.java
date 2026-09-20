package alcaldedigital.cliente.interfaz;

import alcaldedigital.cliente.*;
import alcaldedigital.cliente.recursos.Recursos;
import alcaldedigital.cliente.visualizadores.ConsultaEstructuras;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/** Render compartido por Processing y las capturas de evidencia, sin ventanas ocultas. */
public final class RenderJuego {
    public record Boton(Rectangle area,String accion,String texto) { }
    private final Recursos recursos=new Recursos();
    private final TemaPixel tema=new TemaPixel();
    private int ratonX=-1,ratonY=-1;
    private String presionado="";
    private double finPulsacion;
    private boolean fondoMundo;
    public void apuntar(int x,int y) {ratonX=x;ratonY=y;}
    private final List<Boton> botones=new ArrayList<>();
    private final BufferedImage lienzo=new BufferedImage(1280,720,BufferedImage.TYPE_INT_ARGB);
    private Graphics2D g;
    private JuegoCliente juego;
    private Color tinta=new Color(0x18283D), papel=new Color(0xF7E7C6), tenue=new Color(0xA9BEBE), oro=new Color(0xE5AA45), teal=new Color(0x287F82);
    public List<Boton> botones() { return List.copyOf(botones); }
    public void avanzarFoco(JuegoCliente j,int paso) {
        if(botones.isEmpty()) return;
        j.foco=Math.floorMod(j.foco+paso,botones.size());
        j.editandoBusqueda=botones.get(j.foco).accion().equals("campoBusqueda");
        if(j.editandoBusqueda) j.cursorBusqueda=j.busqueda.length();
    }
    public void activarFoco(JuegoCliente j) {
        if(j.editandoBusqueda) {j.accion("buscar");return;}
        if(!botones.isEmpty()) j.accion(botones.get(Math.floorMod(j.foco,botones.size())).accion());
    }
    public void pulsar(JuegoCliente j,int x,int y) {
        for(int i=0;i<botones.size();i++) if(botones.get(i).area().contains(x,y)) {
            presionado=botones.get(i).accion();finPulsacion=j.animacion+.12;
            j.foco=i;j.editandoBusqueda=false;j.accion(botones.get(i).accion());return;
        }
        j.editandoBusqueda=false;
    }
    public BufferedImage dibujar(JuegoCliente juego) {
        this.juego=juego; botones.clear(); g=lienzo.createGraphics();
        TemaPixel.nitido(g);
        tinta=juego.contraste?Color.BLACK:TemaPixel.NOCHE; papel=juego.contraste?Color.WHITE:TemaPixel.CREMA;
        oro=TemaPixel.ORO;tenue=new Color(juego.contraste?0xFFFFFF:0xACD9DF);teal=new Color(0x197E87);
        rect(0,0,1280,720,tinta);
        if(juego.sesion!=null&&juego.pantalla!=JuegoCliente.Pantalla.MENU&&juego.pantalla!=JuegoCliente.Pantalla.PARQUE) {
            fondoMundo=true;mundo(0,0,1280,720);fondoMundo=false;
            rect(0,0,1280,720,new Color(2,18,32,juego.contraste?250:195));
        } else {
            for(int y=0;y<720;y+=8) rect(0,y,1280,2,new Color(255,255,255,4));
        }
        switch(juego.pantalla) {
            case MENU -> menu(); case ROLES -> roles(); case PARQUE -> parque();
            case CIVITAS -> civitas(); case EVIDENCIAS -> evidencias();
            case ARBOLES -> arboles(); case AYUDA -> ayuda(); case RESULTADO -> resultado();
        }
        if(!botones.isEmpty()) {
            juego.foco=Math.floorMod(juego.foco,botones.size());
            Rectangle r=botones.get(juego.foco).area();g.setStroke(new BasicStroke(2));g.setColor(oro);
            g.drawRect(r.x-4,r.y-4,r.width+8,r.height+8);
            rect(r.x-7,r.y+r.height/2-3,6,6,oro);
        }
        g.setStroke(new BasicStroke(2));g.setColor(papel);g.drawRect(10,10,1260,700);
        for(int x:new int[]{10,1258}) for(int y:new int[]{10,698}) {g.setColor(oro);g.drawRect(x,y,12,12);}
        g.dispose(); return lienzo;
    }
    private void rect(int x,int y,int w,int h,Color c) { g.setColor(c);g.fillRect(x,y,w,h); }
    private void panel(int x,int y,int w,int h,Color c) {
        boolean claro=c.getRed()>180&&c.getGreen()>160;
        if(!claro&&c.getAlpha()==255) c=juego.contraste?Color.BLACK:TemaPixel.PANEL;
        TemaPixel.marco(g,x,y,w,h,c,claro?papel:juego.contraste?Color.WHITE:TemaPixel.CIAN,w>450&&h>200);
    }
    private void texto(String t,int x,int y,int size,Color c,boolean bold) {
        if(size>=12&&size<17) size+=2;
        g.setFont(tema.fuente(size,bold));
        Font fuente=tema.fuente(size,bold);
        if(size>=28) {g.setColor(new Color(0,8,20,130));tema.escribir(g,t,x+2,y+3,fuente);}
        g.setColor(c);tema.escribir(g,t,x,y,fuente);
    }
    private int parrafo(String t,int x,int y,int w,int size,Color c) {
        g.setFont(tema.fuente(size,false));g.setColor(c);int baseline=y;String linea="";
        for(String palabra:t.split(" ")) {
            if(tema.ancho(g,linea+palabra,tema.fuente(size,false))>w&&!linea.isEmpty()) {tema.escribir(g,linea.strip(),x,baseline,tema.fuente(size,false));baseline+=size+7;linea="";}
            linea+=palabra+" ";
        }
        if(!linea.isEmpty()) tema.escribir(g,linea.strip(),x,baseline,tema.fuente(size,false));
        return baseline+size+7;
    }
    private void imagen(String recurso,int x,int y,int w,int h) {g.drawImage(recursos.imagen(recurso),x,y,w,h,null);}
    private void boton(String t,String action,int x,int y,int w,boolean principal) {
        boolean hover=new Rectangle(x,y,w,42).contains(ratonX,ratonY);
        boolean down=action.equals(presionado)&&juego.animacion<finPulsacion;
        int dy=down?2:0;
        Color fondo=principal?oro:juego.contraste?Color.BLACK:hover?new Color(0x176582):TemaPixel.AZUL;
        String icono=switch(action) {
            case "compartir" -> "07_compartir";case "verificar" -> "08_verificar";
            case "reportar" -> "09_reportar";case "ignorar" -> "10_ignorar";
            case "contraste","movimiento" -> "24_accesibilidad";case "ayuda" -> "22_ayuda";
            case "evidencias" -> "04_libreta";default -> null;
        };
        boolean accionSocial=Set.of("compartir","verificar","reportar","ignorar").contains(action);
        if(accionSocial&&!juego.contraste) fondo=switch(action) {
            case "verificar" -> new Color(0x167964);case "reportar" -> new Color(0xAD413E);
            case "ignorar" -> oro;default -> TemaPixel.AZUL;
        };
        TemaPixel.marco(g,x,y+dy,w,42-dy,fondo,principal?papel:hover?oro:TemaPixel.CIAN,false);
        rect(x+7,y+5+dy,w-14,2,new Color(255,255,255,principal?95:35));
        g.setFont(tema.fuente(17,true));
        int espacio=icono==null?28:58;
        int size=18;while(size>17&&tema.ancho(g,t,tema.fuente(size,true))>w-espacio) size--;
        int tw=tema.ancho(g,t,tema.fuente(size,true));
        if(icono!=null) imagen("iconos/"+icono,x+10,y+9+dy,24,24);
        // Los controles muy estrechos conservan el área clicable y reducen solo el rótulo.
        Font f=tema.fuente(size,true);
        while(size>12&&tema.ancho(g,t,f)>w-espacio) f=tema.fuente(--size,true);
        tw=tema.ancho(g,t,f);
        boolean claro=accionSocial?action.equals("ignorar")&&!juego.contraste:principal;
        g.setColor(claro?TemaPixel.NOCHE:papel);
        tema.escribir(g,t,x+(icono==null?(w-tw)/2:40+(w-48-tw)/2),y+27+dy,f);
        botones.add(new Boton(new Rectangle(x,y,w,42),action,t));
    }
    private void cabecera(String titulo,String detalle) {
        rect(18,18,1244,96,new Color(tinta.getRed(),tinta.getGreen(),tinta.getBlue(),235));
        texto("ALCALDE DIGITAL",28,33,16,oro,true);texto(titulo,28,77,34,papel,true);
        texto(detalle,30,101,14,tenue,false);
        rect(28,114,1224,2,TemaPixel.CIAN);
        boton("Volver [Esc]","volver",1080,35,170,false);
    }
    private void pie(String t) {texto(t,30,696,13,tenue,false);}
    private void menu() {
        imagen("portada",462,0,1080,720);
        TemaPixel.marco(g,18,18,467,683,tinta,papel,true);
        imagen("iconos/20_reloj",47,44,24,24);
        texto("CIUDAD NOVA  /  DÍA 01",84,64,17,oro,true);
        texto("ALCALDE",44,150,72,papel,true);texto("DIGITAL",44,215,72,papel,true);
        rect(48,232,384,3,oro);
        texto("PROMESAS, PALOMAS Y WI-FI",48,259,19,oro,true);
        parrafo("El parque tiene un rumor. Las palomas tienen abogado. Tú decides qué merece compartirse.",48,301,390,20,papel);
        if(juego.sesion==null) {
            boton("Entrar a Ciudad Nova  →","jugar",48,414,364,true);
            boton("Cómo jugar","ayuda",48,470,364,false);boton("Salir","salir",48,526,364,false);
        } else {
            boton(juego.sesion.vista().terminado()?"Ver mi resultado":"Continuar mi caso",juego.sesion.vista().terminado()?"resultado":"volver",48,392,364,true);
            boton("Comenzar otro caso","jugar",48,448,364,false);
            boton("Cómo jugar","ayuda",48,504,364,false);boton("Salir","salir",48,560,364,false);
            boton("Laboratorio de estructuras","laboratorio",875,536,362,false);
        }
        rect(48,610,364,2,TemaPixel.CIAN);
        texto("CAPÍTULO 1 · EL HELIPUERTO DE PALOMAS",48,639,15,oro,true);
        texto("Entrega 1  ·  Java + Processing",48,666,16,tenue,false);
        panel(917,605,320,69,new Color(24,40,61,225));
        texto("Una ciudad. Muchas versiones.",938,634,17,papel,true);
        texto("Explora · contrasta · decide",938,657,14,oro,false);
    }
    private void roles() {
        cabecera("¿Qué voz tendrá tu historia?","Un caso local de la entrega 1. Los otros personajes te acompañan como NPC.");
        String[] nombres={"Ciudadano","Periodista","Influencer","Candidato"};
        String[] textos={"Escucha a los vecinos y encuentra las fuentes. Tu voz puede frenar una cadena.","Documenta la declaración completa. Investiga antes de convertirla en titular.","Transmite junto al candidato. Dale alcance al contexto, no solo al escándalo.","Responde por tu campaña. Reconocer lo que no sabes también es una decisión."};
        for(int i=0;i<4;i++) {
            int x=28+i*312;panel(x,146,288,322,new Color(0x22394D));
            imagen("portada",x+8,154,272,114);
            rect(x+8,154,272,114,new Color(0,25,45,155));
            imagen("actores/"+nombres[i].toLowerCase(Locale.ROOT)+"_00",x+96,165,96,96);
            rect(x+10,270,268,2,TemaPixel.CIAN);
            texto(nombres[i],x+22,290,24,papel,true);parrafo(textos[i],x+22,324,245,16,tenue);
            boton("Elegir "+nombres[i],"rol:"+Rol.values()[i],x+18,408,252,true);
        }
        boton("Contraste: "+(juego.contraste?"alto":"normal"),"contraste",28,503,284,false);
        boton("Movimiento: "+(juego.movimientoReducido?"reducido":"normal"),"movimiento",340,503,286,false);
        boton("Decisión: "+(juego.tiempoAmpliado?"60 segundos":"10 segundos"),"tiempo",654,503,284,false);
        boton("Cambiar semilla +","semilla",966,503,286,false);
        parrafo("WASD o flechas para caminar · E para interactuar · C abre Civitas · T muestra los árboles. El reloj se detiene al investigar; al agotarse, se aplica Ignorar.",30,592,1130,18,papel);
        pie("Tab y Enter recorren todos los botones · Semilla "+juego.semilla+" · La verdad se descubre jugando");
    }
    private void parque() {
        var s=juego.sesion.vista();
        texto("CIUDAD NOVA",28,30,14,oro,true);texto("Parque de las Palomas Influencers",28,65,26,papel,true);
        texto("DÍA 01  /  "+s.rol()+"  /  "+s.puntos()+" puntos",28,90,14,tenue,false);
        boton("Civitas [C]","civitas",637,35,157,true);boton("Árboles [T]","arboles",805,35,157,false);
        boton("Ayuda [H]","ayuda",974,35,128,false);boton("Menú","menu",1114,35,138,false);
        mundo(24,114,866,484);
        panel(912,114,344,484,new Color(0x22394D));
        texto("TU SIGUIENTE PASO",936,148,13,oro,true);
        parrafo(s.objetivo(),936,180,291,20,papel);
        texto("EVIDENCIAS  "+s.evidencias().size()+" / 2",936,265,14,tenue,true);
        boton("Archivo de evidencias [I]","evidencias",932,281,303,false);
        texto(s.verificando()?"Investigación sin cuenta atrás":"Decisión: "+(int)Math.ceil(juego.sesion.restante())+" s",936,357,16,oro,true);
        texto("Al agotarse: Ignorar",936,381,13,tenue,false);
        int[] vals={s.ciudad().informacionVerificada(),s.ciudad().confianza(),s.ciudad().desinformacion()};
        String[] labels={"Información verificada","Confianza ciudadana","Desinformación"};
        for(int i=0;i<3;i++) barra(labels[i],vals[i],936,413+i*49,292,i==2);
        panel(24,608,1232,72,new Color(0x294357));
        parrafo(avisoVisible()?juego.aviso+" [T] Consultar":s.mensaje(),42,631,1185,14,papel);
        pie("WASD / flechas: explorar    E / Espacio: interactuar    C: Civitas    I: pruebas    T: árboles    H: ayuda");
    }
    private void barra(String nombre,int valor,int x,int y,int w,boolean peligro) {
        texto(nombre,x,y,13,papel,false);texto(""+valor,x+w-26,y,13,oro,true);
        rect(x,y+8,w,6,new Color(0x405461));rect(x,y+8,Math.max(1,w*valor/100),6,peligro?new Color(0xE97866):new Color(0x6ABCAA));
    }
    private void mundo(int vx,int vy,int vw,int vh) {
        var s=juego.sesion.vista();
        int camX=Math.max(0,Math.min(640-vw/2,Math.round(s.x())-vw/4));
        int camY=Math.max(0,Math.min(480-vh/2,Math.round(s.y())-vh/4));
        Graphics2D guard=g;g=(Graphics2D)g.create();g.clipRect(vx,vy,vw,vh);g.translate(vx-camX*2,vy-camY*2);g.scale(2,2);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
        String[] tiles={"cesped","camino","agua"};
        for(int row=0;row<MapaParque.FILAS;row++) for(int col=0;col<MapaParque.COLUMNAS;col++)
            imagen("tiles/"+tiles[juego.sesion.mapa().suelo(col,row)],col*16,row*16,16,16);
        rect(419,114,82,42,new Color(0x976646));rect(419,114,82,3,oro);
        for(int i=0;i<6;i++) { g.setColor(i%2==0?oro:teal);g.fillPolygon(new int[]{422+i*13,435+i*13,428+i*13},new int[]{109,109,119},3); }
        // El orden por coordenada de pies permite pasar detrás de árboles y delante de bancos.
        record Entidad(int y,Runnable dibujar) { }
        List<Entidad> orden=new ArrayList<>();
        for(var o:juego.sesion.mapa().objetos()) orden.add(new Entidad(o.y(),()->imagen("objetos/"+o.tipo(),o.x()-o.ancho()/2,o.y()-o.alto(),o.ancho(),o.alto())));
        orden.add(new Entidad(228,()->sprite("periodista",0,230,228)));
        orden.add(new Entidad(155,()->sprite("candidato",0,455,155)));
        orden.add(new Entidad(267,()->sprite("influencer",0,474,267)));
        orden.add(new Entidad(315,()->imagen("actores/noti_%02d".formatted(juego.movimientoReducido?0:(int)(juego.animacion*6)%4),276,287,32,32)));
        orden.add(new Entidad((int)s.y(),()->{
            g.setColor(new Color(20,35,40,70));g.fillOval((int)s.x()-10,(int)s.y()-4,20,7);
            if(juego.efecto>0&&juego.ultimaAccion.equals("rol")&&!juego.movimientoReducido)
                imagen("acciones/frame_%02d".formatted(s.rol().ordinal()*4+Math.min(3,(int)((.8-juego.efecto)*5))),(int)s.x()-24,(int)s.y()-48,48,48);
            else sprite(s.rol().name().toLowerCase(Locale.ROOT),juego.direccion*4+juego.cuadro,(int)s.x(),(int)s.y());
            if(juego.efecto>0&&!juego.movimientoReducido) imagen("efectos/"+(juego.ultimaAccion.equals("compartir")?"rumor":"verificacion")+"_%02d".formatted(Math.min(7,(int)((.8-juego.efecto)*10))), (int)s.x()-24,(int)s.y()-48,48,48);
        }));
        orden.sort(Comparator.comparingInt(Entidad::y));orden.forEach(e->e.dibujar().run());
        if(!fondoMundo) {
        marcador("1 · Aviso municipal",176,101,s.evidencias().size()>0);
        marcador("2 · Administradora",230,184,s.evidencias().size()>1);
        marcador("Campaña",455,103,false);
        if(juego.sesion.cerca("aviso")||juego.sesion.cerca("fuente")||juego.sesion.cerca("candidato")) {
            panel((int)s.x()-34,(int)s.y()-51,68,15,tinta);texto("[E] Hablar",(int)s.x()-29,(int)s.y()-40,9,papel,true);
        }
        }
        g.dispose();g=guard;
        if(fondoMundo) return;
        // Minimap basado en el mismo tilemap.
        panel(vx+12,vy+12,130,101,new Color(24,40,61,220));
        for(int row=0;row<30;row++) for(int col=0;col<40;col++) {
            int t=juego.sesion.mapa().suelo(col,row);rect(vx+17+col*3,vy+17+row*3,3,3,t==0?new Color(0x478F62):t==1?papel:teal);
        }
        rect(vx+17+(int)s.x()*3/16,vy+17+(int)s.y()*3/16,5,5,oro);
    }
    private void sprite(String rol,int frame,int x,int y) {imagen("actores/"+rol+"_%02d".formatted(frame),x-16,y-32,32,32);}
    private void marcador(String label,int x,int y,boolean listo) {
        g.setFont(tema.fuente(8,true));int w=g.getFontMetrics().stringWidth(label)+12;
        panel(x-w/2,y-10,w,14,tinta);texto((listo?"✓ ":"")+label,x-w/2+4,y,8,listo?new Color(0x8FD7B6):papel,true);
    }
    private void civitas() {
        var s=juego.sesion.vista();cabecera("Civitas / lo que está circulando","Popular no significa comprobado. La prioridad expresa urgencia, no veracidad.");
        panel(28,133,727,499,papel);
        TemaPixel.marco(g,49,151,49,49,teal,teal,false);
        imagen("actores/influencer_00",54,154,40,40);
        texto("KEVIN VIRAL",112,172,20,teal,true);
        texto("Hace un momento · Parque de las Palomas",112,196,14,tinta,false);
        imagen("portada",568,215,162,108);
        imagen("iconos/09_reportar",627,248,40,40);
        parrafo("¿Cerrarán el parque para construir un helipuerto de palomas?",54,243,494,28,tinta);
        parrafo("«Mi primo tiene una paloma que trabaja en la alcaldía». Compártelo antes de que lo borren.",54,350,640,18,tinta);
        panel(50,395,680,44,new Color(0xEAD7AF));texto("ESTADO: "+(s.evidencias().size()==2?"FUENTES CONTRASTADAS":"SIN COMPROBAR"),67,424,14,tinta,true);
        boton("Compartir","compartir",53,464,157,false);boton("Verificar","verificar",226,464,157,true);
        boton("Reportar","reportar",399,464,157,false);boton("Ignorar","ignorar",572,464,157,false);
        if(s.evidencias().size()==2) {
            boton("Publicar contexto","contexto",53,527,213,true);boton("Guardar pruebas","guardar",283,527,210,false);
            if(juego.sesion.buscarDecision("rectificar")!=null) boton("Rectificar","rectificar",510,527,219,true);
        } else texto("Publicar contexto requiere el aviso + el testimonio.",54,550,15,teal,true);
        panel(779,133,475,499,new Color(0x22394D));texto("RADAR DE PUBLICACIONES",803,169,15,oro,true);
        int y=207;
        for(Publicacion p:s.publicaciones()) {
            texto("#"+p.id()+"   PRIORIDAD "+p.riesgo(),803,y,13,oro,true);
            y=parrafo(p.texto(),803,y+28,414,17,papel)+12;
            texto(p.estado(),803,y,13,tenue,false);y+=47;
        }
        boton("Ver árbol de prioridad","arboles",803,552,425,false);
        parrafo(avisoVisible()?juego.aviso+" [T] Consultar":s.mensaje(),30,652,1210,14,oro);
        pie(s.verificando()?"Investigación activa · vuelve al parque para conseguir pruebas":"Quedan "+(int)Math.ceil(juego.sesion.restante())+" s · al agotarse se aplica Ignorar");
    }
    private void evidencias() {
        var s=juego.sesion.vista();cabecera("Archivo de evidencias","Lee la procedencia y contrasta. Una captura aislada puede perder su contexto.");
        for(int i=0;i<2;i++) {
            int x=28+i*624;panel(x,149,599,286,new Color(0x22394D));
            imagen("iconos/"+(i==0?"06_fuente":"04_libreta"),x+23,170,48,48);
            texto(i==0?"01 / Aviso municipal":"02 / Testimonio directo",x+87,202,21,papel,true);
            parrafo(s.evidencias().size()>i?s.evidencias().get(i):"Pendiente. Explora el parque y pulsa E cerca de esta fuente.",x+25,258,544,20,papel);
            texto(s.evidencias().size()>i?"Procedencia registrada · acceso público":"Fuente todavía no consultada",x+25,402,14,oro,false);
        }
        boton("Volver al parque","volver",28,472,286,true);boton("Decidir en Civitas","civitas",338,472,286,false);
        parrafo("Tu misión: leer el aviso, escuchar a la administradora y decidir qué hacer con el mensaje. La opción Publicar contexto aparece en el árbol cuando reúnes ambas fuentes.",30,577,1134,21,papel);
        pie("Las evidencias se conservan durante todo el caso. No se descartan por accidente.");
    }
    private void arboles() {
        boolean radar=juego.pestana==JuegoCliente.Pestana.RADAR;
        cabecera(juego.laboratorio?"Laboratorio de estructuras":"Lo que cambia con tus decisiones",
            juego.laboratorio?"Sustentación · una estructura y un estado a la vez · consulta sin alterar la partida.":"Estado actual de tu caso · consulta opcional · el reloj local queda pausado.");
        boton(juego.laboratorio?"AVL · Publicaciones":"Radar de publicaciones","radar",28,127,304,radar);
        boton(juego.laboratorio?"N-ario · Decisiones":"Camino de decisiones","camino",348,127,304,!radar);
        boton(juego.laboratorio?"Volver a vista de jugador":"Abrir Laboratorio",juego.laboratorio?"jugador":"laboratorio",900,127,352,false);
        if(juego.laboratorio) laboratorio(radar);else vistaJugador(radar);
    }
    private boolean avisoVisible() {return juego.avisos&&juego.segundosAviso>0&&!juego.aviso.isBlank();}
    private void vistaJugador(boolean radar) {
        var s=juego.sesion.vista();
        panel(28,190,766,483,new Color(0x22394D));panel(814,190,438,483,new Color(0x22394D));
        texto(radar?"Qué merece tu atención":"Tu camino en el rumor del parque",50,224,22,papel,true);
        if(radar) {
            int y=245;
            for(var p:s.publicaciones()) {
                panel(48,y,726,118,papel);
                imagen("portada",60,y+12,120,92);
                rect(64,y+60,34,34,TemaPixel.NOCHE);
                imagen("iconos/"+(p.id()==4?"12_contexto":p.id()==3?"20_reloj":"01_celular"),65,y+61,32,32);
                texto("#"+p.id()+" · "+p.autor(),195,y+29,18,tinta,true);
                rect(622,y+12,135,24,oro);
                texto("Prioridad "+p.riesgo(),631,y+30,16,TemaPixel.NOCHE,true);
                parrafo(p.texto(),195,y+57,558,17,tinta);
                texto(p.estado(),195,y+103,15,teal,false);y+=130;
            }
            texto("Más prioridad = más atención. No indica si algo es verdad.",50,656,14,tenue,false);
        } else {
            texto("Las acciones se eligen en Civitas. Aquí puedes consultar su estado.",50,251,14,tenue,false);
            int y=caminoJugador(s.decisiones(),0,287,s.terminado());
            if(!s.terminado()&&ConsultaEstructuras.buscar(s.decisiones(),"contexto")==null)
                parrafo("Pendiente: reúne el aviso y el testimonio para descubrir cómo aportar contexto.",51,Math.max(564,y+12),700,16,oro);
            texto(s.terminado()?"Caso cerrado: se conserva el camino que tomaste.":"Consultar una opción no la ejecuta ni cambia tus puntos.",50,656,14,tenue,false);
        }
        var evento=juego.consulta.ultimoCambio(radar);
        texto("QUÉ CAMBIÓ Y POR QUÉ",838,225,14,oro,true);
        if(evento!=null) {
            int y=parrafo(evento.causa(),838,259,388,20,papel);
            y=parrafo(radar?evento.radar():evento.camino(),838,y+14,388,18,papel);
            texto("Cambio registrado #"+evento.numero(),838,y+8,13,tenue,false);
        }
        texto("TU SIGUIENTE PASO",838,476,13,oro,true);
        parrafo(s.objetivo(),838,506,380,19,papel);
        boton(s.terminado()?"Consultar resultado":"Ir a Civitas",s.terminado()?"resultado":"civitas",837,572,392,true);
        texto("T / Esc: volver al punto de entrada",838,647,14,tenue,false);
        pie("Las dos pestañas consultan el estado actual. Los detalles técnicos y estados anteriores están en el Laboratorio.");
    }
    private int caminoJugador(ArbolDecisiones.Vista n,int nivel,int y,boolean terminado) {
        int x=52+nivel*32;
        if(nivel>0) {g.setColor(tenue);g.drawLine(x-15,y-24,x-15,y);g.drawLine(x-15,y,x-3,y);}
        panel(x,y-21,700-nivel*32,36,n.ejecutado()?teal:new Color(0x294357));
        texto((n.ejecutado()?"✓ ":"")+n.texto(),x+12,y+3,16,papel,true);
        texto(n.ejecutado()?"Elegida":nivel==0?"Tu caso":terminado?"Caso cerrado":"Disponible",635,y+3,13,papel,false);
        y+=43;
        for(var hijo:n.hijos()) y=caminoJugador(hijo,nivel+1,y,terminado);
        return y;
    }
    private void laboratorio(boolean radar) {
        int indice=radar?juego.indiceAVL():juego.indiceDecision();
        int cantidad=radar?juego.sesion.pasosAVL().size():juego.sesion.pasosDecisiones().size();
        boolean actual=(radar?juego.pasoAVL:juego.pasoDecision)<0;
        String orden=radar?new String[]{"inorden","preorden","postorden","inverso"}[juego.recorrido]:new String[]{"preorden","postorden","niveles"}[juego.recorridoNario];
        panel(28,190,788,316,new Color(0x22394D));panel(836,190,416,316,new Color(0x22394D));
        texto((actual?"ACTUAL":"HISTÓRICO")+" · Estado "+(indice+1)+" / "+cantidad,48,217,14,oro,true);
        var evento=juego.consulta.eventoDelPaso(radar,indice);
        int y=parrafo(evento==null?"Operación de la sesión":evento.titulo(),858,223,370,17,papel);
        if(evento!=null) texto("Día 1 · Parque · versión "+evento.version(),858,y+3,13,tenue,false);
        String operacion=radar?juego.sesion.pasosAVL().get(indice).operacion():juego.sesion.pasosDecisiones().get(indice).operacion();
        y=parrafo(operacion,858,y+36,368,16,oro);
        if(evento!=null) {
            texto("Al completar esta acción:",858,y+10,12,tenue,false);
            parrafo(radar?evento.radar():evento.camino(),858,y+32,368,14,papel);
        }
        texto("Cada pestaña conserva su propio estado.",858,477,12,tenue,false);
        List<String> ids;
        if(radar) {
            var recorrido=ConsultaEstructuras.publicaciones(juego.vistaAVL(),orden);
            long marcado=recorrido.isEmpty()?-1:recorrido.get(Math.floorMod(juego.visita,recorrido.size())).id();
            dibujarAVL(juego.vistaAVL(),422,263,182,0,marcado);
            ids=recorrido.stream().map(p->"#"+p.id()+" ("+p.riesgo()+")").toList();
            texto("Clave (riesgo, ID) · h = altura · FE = derecha − izquierda",48,489,13,tenue,false);
        } else {
            ids=ConsultaEstructuras.decisiones(juego.vistaDecision(),orden);
            dibujarNario(juego.vistaDecision(),44,802,258,0);
            texto("✓ ruta ejecutada · borde dorado: nodo del recorrido · selecciona un nodo para consultarlo",48,489,12,tenue,false);
        }
        boton("← Anterior",radar?"avlAnterior":"narAnterior",28,520,143,false);
        boton("Siguiente →",radar?"avlSiguiente":"narSiguiente",183,520,146,false);
        boton("Estado actual","actual",341,520,157,false);
        boton("Orden: "+orden,radar?"recorrido":"recorridoNario",510,520,228,true);
        boton("Siguiente nodo","visitar",750,520,191,false);
        boton(juego.automatico?"Detener animación":"Animar nodos","automatico",953,520,299,false);
        texto("Nodos del estado mostrado · "+orden+" · "+(ids.isEmpty()?"sin nodos":"visita "+(Math.floorMod(juego.visita,ids.size())+1)+" / "+ids.size()),30,587,14,oro,true);
        texto(String.join(" → ",ids),30,614,14,papel,false);
        if(radar) {
            String campo=juego.busqueda;
            if(juego.editandoBusqueda) campo=campo.substring(0,juego.cursorBusqueda)+"│"+campo.substring(juego.cursorBusqueda);
            boton("ID: "+(campo.isEmpty()?"escribir aquí":campo),"campoBusqueda",28,637,292,false);
            boton("Buscar ID","buscar",336,637,146,false);
            texto(juego.encontrado,504,665,14,oro,false);
        } else texto(juego.encontrado.isBlank()?"Selecciona un nodo: la consulta usa este estado, incluso si la opción ya fue podada.":juego.encontrado,30,663,16,oro,false);
        pie(juego.movimientoReducido?"Movimiento reducido: usa Siguiente nodo. T / Esc / Volver regresan al origen.":"Animar nodos recorre este estado; Anterior/Siguiente cambian operaciones. T / Esc / Volver regresan al origen.");
    }
    private void dibujarAVL(ArbolAVLPublicaciones.Vista n,int x,int y,int distancia,int nivel,long resaltado) {
        if(n==null) return;
        g.setStroke(new BasicStroke(2));g.setColor(new Color(0x69818B));
        if(n.izquierdo()!=null) g.drawLine(x,y,x-distancia,y+76);
        if(n.derecho()!=null) g.drawLine(x,y,x+distancia,y+76);
        dibujarAVL(n.izquierdo(),x-distancia,y+76,distancia/2,nivel+1,resaltado);
        dibujarAVL(n.derecho(),x+distancia,y+76,distancia/2,nivel+1,resaltado);
        TemaPixel.marco(g,x-26,y-24,52,48,n.valor().id()==resaltado?oro:teal,papel,false);
        texto(""+n.valor().riesgo(),x-13,y+6,20,n.valor().id()==resaltado?tinta:papel,true);
        texto("#"+n.valor().id()+" h"+n.altura()+" FE"+n.fe(),x-44,y+43,13,papel,false);
    }
    private void dibujarNario(ArbolDecisiones.Vista n,int left,int right,int y,int depth) {
        int x=(left+right)/2, count=n.hijos().size(),total=hojas(n),offset=0;
        if(count>0) for(int i=0;i<count;i++) {
            int peso=hojas(n.hijos().get(i));
            int l=left+(right-left)*offset/total,r=left+(right-left)*(offset+peso)/total;
            offset+=peso;
            g.setColor(new Color(0x69818B));g.drawLine(x,y,(l+r)/2,y+82);
            dibujarNario(n.hijos().get(i),l,r,y+82,depth+1);
        }
        panel(x-55,y-17,110,35,n.ejecutado()?teal:new Color(0x405461));
        texto((n.ejecutado()?"✓ ":"")+n.id(),x-49,y+5,13,papel,true);
        var ruta=ConsultaEstructuras.decisiones(juego.vistaDecision(),new String[]{"preorden","postorden","niveles"}[juego.recorridoNario]);
        if(!ruta.isEmpty()&&n.id().equals(ruta.get(Math.floorMod(juego.visita,ruta.size())))) {
            g.setColor(oro);g.setStroke(new BasicStroke(2));g.drawRect(x-57,y-19,114,39);
        }
        botones.add(new Boton(new Rectangle(x-55,y-17,110,35),"nodo:"+n.id(),n.id()));
    }
    private int hojas(ArbolDecisiones.Vista n) { return n.hijos().isEmpty()?1:n.hijos().stream().mapToInt(this::hojas).sum(); }
    private void resultado() {
        var s=juego.sesion.vista();cabecera("El parque después de tu decisión","Cierre del capítulo 1. La elección y los finales de siete días pertenecen a la entrega final.");
        panel(28,139,715,478,papel);texto(s.puntos()>=20?"CONTEXTO, BENDITO CONTEXTO":"CADA DECISIÓN DEJA HUELLA",54,179,22,tinta,true);
        parrafo(s.mensaje(),54,221,653,22,tinta);
        texto("TU RECORRIDO",54,339,13,teal,true);int y=368;
        List<String> h=s.historial();
        for(String linea:h.subList(Math.max(0,h.size()-6),h.size())) y=parrafo("• "+linea,54,y,650,14,tinta)+5;
        panel(766,139,487,478,new Color(0x22394D));texto(s.puntos()+" PUNTOS",793,180,29,oro,true);
        EstadoCiudad c=s.ciudad();String[] names={"Información verificada","Confianza","Convivencia","Bienestar digital","Participación","Desinformación","Conflictos"};
        int[] vals={c.informacionVerificada(),c.confianza(),c.convivencia(),c.bienestar(),c.participacion(),c.desinformacion(),c.conflictos()};
        for(int i=0;i<7;i++) {
            int delta=vals[i]-(i>4?20:50);
            barra(names[i]+"  ("+(delta>=0?"+":"")+delta+")",vals[i],793,224+i*51,430,i>4);
        }
        boton("Otra historia","jugar",28,639,226,true);boton("Revisar los árboles","arboles",273,639,252,false);
        boton("Evidencias","evidencias",545,639,198,false);boton("Laboratorio","laboratorio",766,639,230,false);boton("Menú principal","menu",1012,639,241,false);
        if(avisoVisible()) {panel(45,516,682,93,new Color(0xEAD7AF));parrafo(juego.aviso+" [T] Consultar",58,538,654,13,tinta);}
        pie("El historial conserva tus acciones incluso cuando una publicación sale del AVL.");
    }
    private void ayuda() {
        cabecera("Ayuda / antes de darle a compartir","Explora con calma. El panel de ayuda pausa el reloj de esta sesión local.");
        String[] titulos={"01 · Tu objetivo","02 · Cómo te mueves","03 · Qué significan las acciones","04 · Tu papel","05 · Los indicadores","06 · Accesibilidad"};
        String[] body={"Investiga el rumor del helipuerto. Encuentra el aviso municipal, contrasta con la administradora y decide qué publicar. El contexto y la convivencia importan más que el alcance.",
            "WASD/flechas: caminar. E: interactuar. C: Civitas. I/J: pruebas. T abre/cierra Radar y Camino; Laboratorio: detalle técnico. Esc vuelve al origen. Tab/Mayús+Tab + Enter: controles.",
            "Compartir puede aumentar el daño. Verificar inicia una búsqueda. Reportar requiere pruebas y validación. Ignorar evita tu retransmisión, pero otros pueden continuar. Rectificar recupera parcialmente.",
            "Ciudadano: escuchar. Periodista: documentar. Influencer: transmitir junto al candidato. Candidato: responder. En este capítulo todos investigan el parque y tienen una acción de rol en la campaña.",
            "Información, confianza, convivencia, bienestar y participación: cuidar. Desinformación y conflicto: reducir. Todos van de 0 a 100. Popularidad y verdad no son equivalentes.",
            "Activa alto contraste o movimiento reducido. Antes de empezar, amplía la decisión de 10 a 60 segundos. Hay etiquetas y formas además del color. Toda información esencial es visual y textual."};
        String[] iconos={"06_fuente","19_mapa","07_compartir","01_celular","13_confianza","24_accesibilidad"};
        for(int i=0;i<6;i++) {
            int x=28+(i%2)*624,y=135+(i/2)*163;
            panel(x,y,596,153,new Color(0x22394D));
            TemaPixel.marco(g,x+14,y+19,88,113,TemaPixel.NOCHE,TemaPixel.CIAN,false);
            if(i==3) imagen("actores/ciudadano_00",x+26,y+37,64,64);
            else imagen("iconos/"+iconos[i],x+26,y+40,64,64);
            texto(titulos[i],x+118,y+29,20,oro,true);
            parrafo(body[i],x+118,y+56,461,16,papel);
        }
        boton("Contraste: "+(juego.contraste?"alto":"normal"),"contraste",28,639,288,false);
        boton("Animaciones: "+(juego.movimientoReducido?"reducidas":"activas"),"movimiento",338,639,302,false);
        boton(juego.avisos?"Avisos: visibles":"Avisos: solo al consultar","avisos",660,639,286,false);
        if(juego.sesion!=null) boton("Laboratorio","laboratorio",964,639,289,true);
        else boton("Volver","volver",964,639,289,true);
        pie("La entrega 1 usa una sesión local. El multijugador y los grafos se integran en las siguientes entregas.");
    }
}
