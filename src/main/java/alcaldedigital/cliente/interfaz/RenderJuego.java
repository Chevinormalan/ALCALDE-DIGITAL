package alcaldedigital.cliente.interfaz;

import alcaldedigital.cliente.*;
import alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;
import static alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;
import alcaldedigital.cliente.mision2.MisionDosSketch;
import alcaldedigital.cliente.misiones.PartidasMenu;
import alcaldedigital.cliente.entrada.MapaControles;
import alcaldedigital.cliente.recursos.Recursos;
import alcaldedigital.cliente.recursos.PaletaMorada;
import alcaldedigital.cliente.tutorial.VistaTutorial;
import alcaldedigital.compartido.NombreJuego;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import alcaldedigital.cliente.visualizadores.ConsultaEstructuras;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/** Render compartido por Processing y las capturas de evidencia, sin ventanas ocultas. */
public final class RenderJuego {
    public record Boton(Rectangle area,String accion,String texto) { }
    private static final String ICONO_RUMOR_FUENTE="iconos/06_fuente";
    private static final double DURACION_REVELADO=.32, DURACION_AVISO_RUMOR=1.4;
    private final Recursos recursos=new Recursos();
    private final PortadasMisiones portadasMisiones=new PortadasMisiones();
    private final RenderInvestigacionSofi renderInvestigacion=new RenderInvestigacionSofi(this);
    private Vista investigacion;
    BufferedImage arteParque() {return recursos.imagen("mision1/parque");}
    BufferedImage arte(String recurso) {return recursos.imagen(recurso);}
    String teclaLupaSofi() {
        return juego.mandoActivo?MapaControles.etiquetaBoton(new MapaControles.Contexto(1,false,null,null,juego.nombreMando),"LB"):"Q";
    }
    void personajeSofi(String recurso,int columnas,int filas,int cuadro,int x,int y,int w,int h) {
        celda(recurso,columnas,filas,cuadro,x,y,w,h);
    }
    void retratoVecino(int id,int x,int y,int ancho,int alto) {
        retratoVecino(id,0,0,x,y,ancho,alto);
    }
    void retratoVecino(int id,int direccion,int pose,int x,int y,int ancho,int alto) {
        if(spritesTranseuntes==null) spritesTranseuntes=new SpritesTranseuntes(recursos);
        g.drawImage(spritesTranseuntes.imagen(id,direccion,pose),x,y,ancho,alto,null);
    }
    private final Map<String,Rectangle> limitesCelda=new HashMap<>();
    private final Map<String,Rectangle> limitesGrupo=new HashMap<>();
    private SpritesTranseuntes spritesTranseuntes;
    final TemaPixel tema=new TemaPixel();
    private final VistaTutorial vistaTutorial=new VistaTutorial(tema,recursos);
    private final alcaldedigital.cliente.audio.InterfazSonora audioUI=new alcaldedigital.cliente.audio.InterfazSonora();
    int ratonX=-1,ratonY=-1;
    private String presionado="";
    private double finPulsacion;
    private boolean fondoMundo;
    private SesionMisionUno sesionVisualMisionUno;
    private SesionMisionUno.Vista vistaAnteriorMisionUno;
    private final Map<Long,Double> reveladosMisionUno=new HashMap<>();
    private final Map<Integer,Double> avisosRumorMisionUno=new HashMap<>();
    public void apuntar(int x,int y) {ratonX=x;ratonY=y;audioUI.hover(botones.stream().filter(b->b.area.contains(x,y)).map(b->b.accion).findFirst().orElse(""));}
    final List<Boton> botones=new ArrayList<>();
    final BufferedImage lienzo=new BufferedImage(1280,720,BufferedImage.TYPE_INT_ARGB);
    Graphics2D g;
    JuegoCliente juego;
    Color tinta=PaletaMorada.FONDO, papel=PaletaMorada.PAPEL,
        tenue=PaletaMorada.TEXTO_SUAVE, oro=PaletaMorada.ACENTO, teal=PaletaMorada.VIOLETA;
    public List<Boton> botones() { return List.copyOf(botones); }
    public void avanzarFoco(JuegoCliente j,int paso) {
        if(botones.isEmpty()) return;
        if(investigacion!=null&&investigacion.lupa()) {ratonX=-1;ratonY=-1;}
        j.foco=Math.floorMod(j.foco+paso,botones.size());audioUI.foco(botones.get(j.foco).accion);
        j.editandoBusqueda=botones.get(j.foco).accion().equals("campoBusqueda");
        if(j.editandoBusqueda) j.cursorBusqueda=j.busqueda.length();
    }
    public void moverFocoDireccion(JuegoCliente j,int dx,int dy) {
        if(botones.isEmpty()) return;
        if(investigacion!=null&&investigacion.lupa()) {ratonX=-1;ratonY=-1;}
        Rectangle actual=botones.get(Math.floorMod(j.foco,botones.size())).area();
        double cx=actual.getCenterX(),cy=actual.getCenterY(),mejor=Double.POSITIVE_INFINITY;
        int elegido=j.foco;
        for(int i=0;i<botones.size();i++) {
            if(i==j.foco) continue;
            Rectangle r=botones.get(i).area();
            double x=r.getCenterX()-cx,y=r.getCenterY()-cy;
            double avance=x*dx+y*dy;
            if(avance<=1) continue;
            double lado=Math.abs(x*dy-y*dx);
            double puntaje=avance+lado*2.5;
            if(puntaje<mejor) {mejor=puntaje;elegido=i;}
        }
        if(j.foco!=elegido)audioUI.foco(botones.get(elegido).accion);j.foco=elegido;
        j.editandoBusqueda=botones.get(elegido).accion().equals("campoBusqueda");
    }
    public void activarFoco(JuegoCliente j) {
        if(j.editandoBusqueda&&!j.tecladoNumericoActivo) {j.accion("buscar");return;}
        if(!botones.isEmpty()) {String a=botones.get(Math.floorMod(j.foco,botones.size())).accion();alcaldedigital.cliente.audio.InterfazSonora.activar(a);j.accion(a);}
    }
    public void pulsar(JuegoCliente j,int x,int y) {
        for(int i=0;i<botones.size();i++) if(botones.get(i).area().contains(x,y)) {
            alcaldedigital.cliente.audio.InterfazSonora.activar(botones.get(i).accion());presionado=botones.get(i).accion();finPulsacion=j.animacion+.12;
            j.foco=i;j.editandoBusqueda=false;j.accion(botones.get(i).accion());return;
        }
        j.editandoBusqueda=false;
    }
    public BufferedImage dibujar(JuegoCliente juego) {
        this.juego=juego; botones.clear(); g=lienzo.createGraphics();
        TemaPixel.nitido(g);
        tinta=juego.contraste?Color.BLACK:TemaPixel.NOCHE; papel=juego.contraste?Color.WHITE:TemaPixel.CREMA;
        oro=TemaPixel.ORO;tenue=juego.contraste?Color.WHITE:PaletaMorada.TEXTO_SUAVE;teal=PaletaMorada.VIOLETA;
        if(juego.tutorial()!=null){
            investigacion=null;
            vistaTutorial.dibujar(g,juego.tutorial(),juego.contraste,juego.movimientoReducido);
            g.dispose();return lienzo;
        }
        rect(0,0,1280,720,tinta);
        var vistaMisionUno=juego.misionUno==null?null:juego.misionUno.vista();
        investigacion=juego.misionUno==null?null:juego.misionUno.investigacion();
        actualizarPresentacionMisionUno(vistaMisionUno);
        boolean seleccion=juego.pantalla==JuegoCliente.Pantalla.GUARDADO||juego.pantalla==JuegoCliente.Pantalla.MISIONES||
            juego.pantalla==JuegoCliente.Pantalla.CONTROLES||juego.pantalla==JuegoCliente.Pantalla.GRAFICOS||juego.pantalla==JuegoCliente.Pantalla.AUDIO||juego.pantalla==JuegoCliente.Pantalla.CONFIRMAR_REINICIO||
            juego.pantalla==JuegoCliente.Pantalla.DIFICULTAD||
            juego.pantalla==JuegoCliente.Pantalla.DIFICULTAD_DOS;
        if(juego.misionUno!=null&&!seleccion&&juego.pantalla!=JuegoCliente.Pantalla.MENU&&juego.pantalla!=JuegoCliente.Pantalla.PARQUE) {
            mundoMisionUno(vistaMisionUno,0,0,1280,720,true);
            boolean recogiendo=investigacion!=null&&investigacion.premium()&&investigacion.abierto()!=0&&
                !juego.movimientoReducido&&juego.investigacionSofi.finRecogida-juego.animacion>.75;
            rect(0,0,1280,720,new Color(33,21,47,juego.contraste?250:recogiendo?82:210));
        } else if(juego.sesion!=null&&!seleccion&&juego.pantalla!=JuegoCliente.Pantalla.MENU&&juego.pantalla!=JuegoCliente.Pantalla.PARQUE) {
            fondoMundo=true;mundo(0,0,1280,720);fondoMundo=false;
            rect(0,0,1280,720,new Color(33,21,47,juego.contraste?250:195));
        } else {
            for(int y=0;y<720;y+=8) rect(0,y,1280,2,new Color(255,255,255,4));
        }
        if(seleccion) {
            if(juego.pantalla==JuegoCliente.Pantalla.GUARDADO) guardado();
            else if(juego.pantalla==JuegoCliente.Pantalla.MISIONES) misiones();
            else if(juego.pantalla==JuegoCliente.Pantalla.CONTROLES) controles();
            else if(juego.pantalla==JuegoCliente.Pantalla.GRAFICOS) graficos();
            else if(juego.pantalla==JuegoCliente.Pantalla.AUDIO) audio();
            else if(juego.pantalla==JuegoCliente.Pantalla.CONFIRMAR_REINICIO) confirmarReinicio();
            else if(juego.pantalla==JuegoCliente.Pantalla.DIFICULTAD_DOS) dificultadDos();
            else dificultad();
        } else if(juego.misionUno!=null&&juego.pantalla!=JuegoCliente.Pantalla.MENU&&juego.pantalla!=JuegoCliente.Pantalla.ROLES&&
            !(juego.pantalla==JuegoCliente.Pantalla.AYUDA&&juego.anterior==JuegoCliente.Pantalla.MENU)) {
            switch(juego.pantalla) {
                case PARQUE -> parqueMisionUno(vistaMisionUno);
                case CIVITAS -> {if(investigacion==null) civitasMisionUno(vistaMisionUno);else renderInvestigacion.dibujar(vistaMisionUno,investigacion);}
                case RESULTADO -> {if(investigacion==null) resultadoMisionUno();else renderInvestigacion.resultado(vistaMisionUno,investigacion);}
                case AYUDA -> ayudaMisionUno();
                case ARBOLES -> estructurasMisionUno();
                default -> parqueMisionUno(vistaMisionUno);
            }
        } else switch(juego.pantalla) {
            case MENU -> menu(); case MISIONES -> misiones(); case GUARDADO -> guardado();
            case DIFICULTAD -> dificultad(); case DIFICULTAD_DOS -> dificultadDos();
            case ROLES -> roles(); case PARQUE -> parque();
            case CIVITAS -> civitas(); case EVIDENCIAS -> evidencias();
            case ARBOLES -> arboles(); case AYUDA -> ayuda(); case RESULTADO -> resultado();
            case CONTROLES -> controles(); case GRAFICOS -> graficos(); case AUDIO -> audio(); case CONFIRMAR_REINICIO -> confirmarReinicio();
        }
        if(juego.pausa&&!juego.pausaUnificada) pausa();
        else if(juego.tecladoNumericoActivo) tecladoNumerico();
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
    private void pausa() {
        botones.clear();
        rect(0,0,1280,720,new Color(13,8,24,220));
        panel(340,118,600,482,PaletaMorada.PANEL);
        texto("PAUSA",380,171,38,oro,true);
        texto("Misión 1 · Sofi y Mr. Palomo",380,205,18,papel,true);
        if(juego.controlesPausa)
            parrafo("Stick izquierdo o cruceta: moverse. A: hablar y confirmar. X: Civitas. Y: estructuras. LB y RB: cambiar publicación. B: volver. Menú: pausa.",380,265,515,19,papel);
        else
            parrafo("El tiempo de la misión está detenido. Puedes continuar, consultar los controles o regresar al selector.",380,265,515,19,papel);
        boton("Continuar","reanudar",380,421,520,true);
        boton(juego.controlesPausa?"Ocultar controles":"Ver controles","controlesPausa",380,475,520,false);
        boton("Salir al selector","salirMision",380,529,520,false);
    }
    private void tecladoNumerico() {
        botones.clear();
        rect(0,0,1280,720,new Color(13,8,24,220));
        panel(412,76,456,568,PaletaMorada.PANEL);
        texto("BUSCAR ID",450,127,31,oro,true);
        texto(juego.busqueda.isBlank()?"Escribe un número":juego.busqueda,450,177,22,papel,true);
        String[][] teclas={{"7","8","9"},{"4","5","6"},{"1","2","3"}};
        for(int fila=0;fila<3;fila++) for(int columna=0;columna<3;columna++) {
            String digito=teclas[fila][columna];
            boton(digito,"digito:"+digito,450+columna*126,213+fila*65,108,false);
        }
        boton("Borrar","borrarDigito",450,408,108,false);
        boton("0","digito:0",576,408,108,false);
        boton("Buscar","buscar",702,408,108,true);
        boton("Cancelar [B]","cerrarTeclado",450,503,360,false);
    }
    void rect(int x,int y,int w,int h,Color c) { g.setColor(c);g.fillRect(x,y,w,h); }
    void panel(int x,int y,int w,int h,Color c) {
        boolean claro=c.getRed()>180&&c.getGreen()>160;
        if(!claro&&c.getAlpha()==255) c=juego.contraste?Color.BLACK:TemaPixel.PANEL;
        TemaPixel.marco(g,x,y,w,h,c,claro?papel:juego.contraste?Color.WHITE:TemaPixel.CIAN,w>450&&h>200);
    }
    void texto(String t,int x,int y,int size,Color c,boolean bold) {
        if(size>=12&&size<17) size+=2;
        g.setFont(tema.fuente(size,bold));
        Font fuente=tema.fuente(size,bold);
        if(size>=28) {g.setColor(new Color(20,12,29,130));tema.escribir(g,t,x+2,y+3,fuente);}
        g.setColor(c);tema.escribir(g,t,x,y,fuente);
    }
    int parrafo(String t,int x,int y,int w,int size,Color c) {
        g.setFont(tema.fuente(size,false));g.setColor(c);int baseline=y;String linea="";
        for(String palabra:t.split(" ")) {
            if(tema.ancho(g,linea+palabra,tema.fuente(size,false))>w&&!linea.isEmpty()) {tema.escribir(g,linea.strip(),x,baseline,tema.fuente(size,false));baseline+=size+7;linea="";}
            linea+=palabra+" ";
        }
        if(!linea.isEmpty()) tema.escribir(g,linea.strip(),x,baseline,tema.fuente(size,false));
        return baseline+size+7;
    }
    void imagen(String recurso,int x,int y,int w,int h) {g.drawImage(recursos.imagen(recurso),x,y,w,h,null);}
    void boton(String t,String action,int x,int y,int w,boolean principal) {
        boolean hover=new Rectangle(x,y,w,42).contains(ratonX,ratonY);
        boolean down=action.equals(presionado)&&juego.animacion<finPulsacion;
        int dy=down?2:0;
        Color fondo=principal?oro:juego.contraste?Color.BLACK:hover?PaletaMorada.VIOLETA.brighter():TemaPixel.AZUL;
        String icono=switch(action) {
            case "compartir" -> "07_compartir";case "verificar" -> "08_verificar";
            case "reportar" -> "09_reportar";case "ignorar" -> "10_ignorar";
            case "contraste","movimiento" -> "24_accesibilidad";case "ayuda" -> "22_ayuda";
            case "evidencias" -> "04_libreta";default -> null;
        };
        boolean accionSocial=Set.of("compartir","verificar","reportar","ignorar").contains(action);
        if(accionSocial&&!juego.contraste) fondo=switch(action) {
            case "verificar" -> PaletaMorada.EXITO;case "reportar" -> PaletaMorada.PELIGRO;
            default -> TemaPixel.AZUL;
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
        boolean claro=principal&&!accionSocial;
        g.setColor(claro?TemaPixel.NOCHE:papel);
        tema.escribir(g,t,x+(icono==null?(w-tw)/2:40+(w-48-tw)/2),y+27+dy,f);
        botones.add(new Boton(new Rectangle(x,y,w,42),action,t));
    }
    void cabecera(String titulo,String detalle) {
        rect(18,18,1244,96,new Color(tinta.getRed(),tinta.getGreen(),tinta.getBlue(),235));
        texto(NombreJuego.TITULO,28,33,16,oro,true);texto(titulo,28,77,34,papel,true);
        texto(detalle,30,101,14,tenue,false);
        rect(28,114,1224,2,TemaPixel.CIAN);
        boton(juego.mandoActivo?"Volver [B]":"Volver [Esc]","volver",1080,35,170,false);
    }
    void pie(String t) {texto(t,30,696,13,tenue,false);}
    private void menu() {
        imagen("portada",462,0,1080,720);
        TemaPixel.marco(g,18,18,467,683,tinta,papel,true);
        imagen("iconos/20_reloj",47,44,24,24);
        int victorias=0;for(int n=1;n<=7;n++)if(juego.progreso.ganada(n))victorias++;
        texto("CIUDAD NOVA  /  "+(victorias==0?"NUEVA CAMPAÑA":victorias+" MISIONES GANADAS"),84,64,17,oro,true);
        texto(NombreJuego.NOMBRE+":",44,150,54,papel,true);
        texto(NombreJuego.SUBTITULO,44,207,43,oro,true);
        rect(48,232,384,3,oro);
        parrafo(victorias==0?"Sofi conoce a Mr. Palomo. Una publicación altera el parque y tus decisiones pueden calmarlo.":
            "Tu campaña continúa en Ciudad Nova. Retoma una partida o elige una misión disponible.",48,282,390,20,papel);
        if(juego.misionUnoDisponible()) {
            var partida=juego.continuacionMenu();
            boolean ganada=partida!=null&&partida.estado()==PartidasMenu.Estado.VICTORIA;
            boolean accesoDirecto=partida!=null&&!ganada;
            boton(ganada?"Continuar campaña":"Jugar · elegir misión","jugar",48,397,364,true);
            if(accesoDirecto)boton(partida.estado()==PartidasMenu.Estado.DERROTA?
                "Reintentar misión "+partida.mision():"Continuar misión "+partida.mision(),
                "continuarMision",48,453,364,false);
            boton("Cómo jugar","ayuda",48,accesoDirecto?509:453,364,false);
            boton("Salir","salir",48,accesoDirecto?565:509,364,false);
        } else if(juego.sesion==null) {
            boton("Entrar a Ciudad Nova  →","jugar",48,414,364,true);
            boton("Cómo jugar","ayuda",48,470,364,false);boton("Salir","salir",48,526,364,false);
        } else {
            boton(juego.sesion.vista().terminado()?"Ver mi resultado":"Continuar mi caso",juego.sesion.vista().terminado()?"resultado":"volver",48,392,364,true);
            boton("Comenzar otro caso","jugar",48,448,364,false);
            boton("Cómo jugar","ayuda",48,504,364,false);boton("Salir","salir",48,560,364,false);
            boton("Laboratorio de estructuras","laboratorio",875,536,362,false);
        }
        rect(48,610,364,2,TemaPixel.CIAN);
        int disponibles=0;for(int n=1;n<=7;n++)if(juego.progreso.disponible(n))disponibles++;
        texto("MISIONES 1–7 · "+disponibles+" DISPONIBLES",48,639,15,oro,true);
        texto("Ciudad Nova · campaña local",48,666,16,tenue,false);
        panel(917,605,320,69,new Color(56,38,75,225));
        texto(juego.mandoActivo?"Mando conectado":"Una ciudad. Muchas versiones.",938,634,17,papel,true);
        texto(juego.mandoActivo?"A: elegir · B: volver":"Explora · contrasta · decide",938,657,14,oro,false);
        if(juego.segundosAviso>0&&!juego.aviso.isBlank())pie(juego.aviso);
    }
    private void guardado(){
        cabecera("Misión "+juego.misionSolicitada,"Hay una partida guardada para esta misión.");
        panel(300,200,680,350,PaletaMorada.PANEL);
        texto("¿Cómo quieres continuar?",340,260,29,papel,true);
        boton("Continuar partida","partida:continuar",340,310,600,true);
        boton("Nueva partida","partida:nueva",340,375,600,false);
        boton("Volver a misiones","volver",340,440,600,false);
        pie("Nueva partida conserva las victorias y los antecedentes de campaña.");
    }
    private void elegirMision(int n,int x,int y){
        if(juego.progreso.disponible(n))boton("Elegir misión "+n,"mision"+n,x,y,258,true);
        else {panel(x,y,258,42,new Color(40,29,54));texto("Gana la misión "+juego.progreso.requisitoPendiente(n),x+15,y+28,17,tenue,true);}
    }
    private void misiones() {
        cabecera("Elige una misión","Gana misiones para avanzar o activa el acceso libre. Las misiones 5 a 8 admiten LAN.");
        for(int n=1;n<=portadasMisiones.cantidad();n++) {
            int x=28+((n-1)%4)*312,y=145+((n-1)/4)*230;
            panel(x,y,288,213,PaletaMorada.PANEL);
            g.drawImage(portadasMisiones.imagen(n),x+9,y+9,null);
            rect(x+9,y+9+PortadasMisiones.ALTO,PortadasMisiones.ANCHO,1,juego.contraste?papel:PaletaMorada.VIOLETA);
            boolean disponible=juego.progreso.disponible(n);
            String estado=!disponible?"BLOQUEADA":juego.progreso.ganada(n)?"GANADA":"DISPONIBLE";
            texto("MISIÓN "+n+" · "+estado,x+16,y+128,13,disponible?oro:tenue,true);
            texto(portadasMisiones.titulo(n),x+16,y+149,n==7?18:20,papel,true);
            elegirMision(n,x+15,y+157);
        }
        if(juego.progreso.todasDesbloqueadas()){
            panel(28,615,350,44,PaletaMorada.PANEL);
            texto("TODAS LAS MISIONES DISPONIBLES",42,643,16,oro,true);
        }else boton("Desbloquear todas las misiones","desbloquearTodas",28,615,350,false);
        texto("Acceso libre",396,643,15,tenue,false);
        boton("Restablecer juego","pedirRestablecer",952,615,300,false);
        pie(juego.segundosAviso>0?juego.aviso:(juego.mandoActivo?"Stick/cruceta: elegir · A: confirmar · B: volver  |  ":"")+
            "Ciudad: confianza "+juego.estadoCampana.confianza()+" · desinformación "+
            juego.estadoCampana.desinformacion()+"  |  Misión 8: campaña o final de prueba con acceso libre.");
    }
    private void confirmarReinicio(){
        cabecera("Restablecer juego","Comenzar otra vez desde la misión 1.");
        panel(240,175,800,370,PaletaMorada.PANEL);
        texto("¿Restablecer todo el progreso?",285,245,30,oro,true);
        parrafo("Se archivarán las partidas guardadas, victorias, puntajes, decisiones de campaña y el desbloqueo total. El perfil anterior quedará en una carpeta de respaldo.",285,285,710,19,papel);
        boton("Cancelar y volver","cancelarRestablecer",285,405,340,true);
        boton("Sí, restablecer juego","confirmarRestablecer",650,405,345,false);
        pie("Después del reinicio, la misión 1 estará disponible y las misiones 2 a 7 volverán a bloquearse.");
    }
    private void controles(){
        int n=juego.controlesMision;boolean dos=juego.controlesDosJugadores&&n>=5;
        cabecera("Controles · misión "+n,MapaControles.nombreMision(n)+" · "+(dos?"dos personas en un teclado":"una persona"));
        panel(28,137,596,482,PaletaMorada.PANEL);
        panel(652,137,596,482,PaletaMorada.PANEL);
        texto("TECLADO",52,174,24,oro,true);texto("MANDO",676,174,24,TemaPixel.CIAN,true);
        imagen("controles/teclado",52,187,548,203);
        imagen("controles/mando",694,183,505,240);
        var c=new MapaControles.Contexto(n,dos,"Jugador 1","Jugador 2",juego.nombreMando);
        if(dos){
            texto("J1",52,419,17,oro,true);texto("J2",338,419,17,TemaPixel.CIAN,true);
            dibujarListaTeclas(MapaControles.teclado1(c),52,442,26,oro);
            dibujarListaTeclas(MapaControles.teclado2(c),338,442,26,TemaPixel.CIAN);
        }else dibujarListaTeclas(MapaControles.teclado1(c),52,418,27,oro);
        if(n==8){
            texto("J3: num. 4/6 · 0 · 1/2/3 · decimal",52,558,14,papel,false);
            texto("J4 jefe: números superiores 1–6",52,584,14,papel,false);
        }
        texto(c.playStation()?"PlayStation · compatibilidad de botones":"Esquema Xbox / mando genérico",676,443,16,papel,false);
        int y=468;for(var b:MapaControles.mando(c)){
            texto(MapaControles.etiquetaBoton(c,b.boton()),678,y,15,TemaPixel.CIAN,true);
            texto(b.accion(),900,y,15,papel,false);y+=22;
        }
        if(n>1)boton("Misión anterior","controlesAnterior",28,638,238,false);
        else {panel(28,638,238,42,PaletaMorada.PANEL);texto("Primera misión",52,666,15,tenue,false);}
        if(n<8)boton("Misión siguiente","controlesSiguiente",280,638,238,false);
        else {panel(280,638,238,42,PaletaMorada.PANEL);texto("Última misión",304,666,15,tenue,false);}
        if(n>=5)boton(dos?"Dos personas":"Una persona","controlesModo",532,638,258,false);
        else {panel(532,638,258,42,PaletaMorada.PANEL);texto("Una persona",555,666,15,tenue,false);}
        boton("Volver a Ayuda","volver",804,638,448,true);
        pie("Esc / P / Menú: pausa · H / Vista: ayuda · G: guardar · T: consultas disponibles");
    }
    private void dibujarListaTeclas(List<MapaControles.Tecla> teclas,int x,int y,int paso,Color acento){
        for(var t:teclas){
            texto(t.tecla(),x,y,15,acento,true);
            texto(t.accion(),x+dosColumnas(x),y,15,papel,false);y+=paso;
        }
    }
    private int dosColumnas(int x){return x>=338?112:120;}
    private void dificultad() {
        cabecera("Elige la dificultad","Misión 1 · Sofi y Mr. Palomo");
        String[] nombres={"FÁCIL","NORMAL","DIFÍCIL"};
        String[] ayudas={"Detalles marcados","Ayudas al explorar","Por tu cuenta"};
        String[] descripciones={
            "La brújula orienta por el parque. Al activar la lupa, el papel marca las zonas con pistas.",
            "Sigue las indicaciones del parque. La lupa te avisa cuando enfocas un detalle útil.",
            "La libreta indica el sector de cada papel. Tú decides qué partes del periódico revisar."
        };
        SesionMisionUno.Dificultad[] opciones=SesionMisionUno.Dificultad.values();
        for(int i=0;i<opciones.length;i++) {
            int x=28+i*417;
            panel(x,157,390,405,PaletaMorada.PANEL);
            texto(nombres[i],x+25,207,27,i==2?PaletaMorada.PELIGRO:oro,true);
            rect(x+25,224,340,3,oro);
            imagen("iconos/"+(i==0?"06_fuente":i==1?"04_libreta":"12_contexto"),x+25,248,54,54);
            texto(opciones[i].segundos()+" s",x+93,289,37,papel,true);
            texto(ayudas[i],x+25,330,16,tenue,true);
            parrafo(descripciones[i],x+25,385,333,19,papel);
            if(juego.misionUnoDisponible()) boton("Jugar en "+nombres[i],
                "m1dificultad:"+opciones[i].name(),x+21,502,348,i==1);
        }
        parrafo("Investiga a tu ritmo. El reloj empieza cuando los vecinos se reúnen junto a la fuente.",31,610,1180,17,tenue);
        pie(juego.mandoActivo?"Stick: caminar · A: interactuar · X: libreta · Menú: pausa":
            "WASD o flechas: caminar · E: interactuar · Q: lupa · Tab: libreta · Esc/P: pausa");
    }
    private void dificultadDos() {
        cabecera("Dificultad de la misión 2","Defiende la red Civitas hasta que termine el reloj.");
        String[] nombres={"FÁCIL","NORMAL","DIFÍCIL"};
        String[] descripciones={
            "6 oleadas de 1 a 3 equipos y una alerta final a los 2 s.",
            "9 oleadas de 1 a 3 equipos y una alerta final a los 2 s.",
            "11 oleadas y alerta final. Solo el primer intento tiene 30 % de fallo crítico."
        };
        MisionDosSketch.Dificultad[] opciones=MisionDosSketch.Dificultad.values();
        for(int i=0;i<opciones.length;i++) {
            int x=28+i*417;
            panel(x,157,390,405,PaletaMorada.PANEL);
            texto(nombres[i],x+25,207,27,i==2?PaletaMorada.PELIGRO:oro,true);
            rect(x+25,224,340,3,oro);
            texto(opciones[i].segundos()+" s",x+25,307,60,papel,true);
            texto("ataque cada "+NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO"))
                .format(opciones[i].intervaloAtaque())+" s",x+25,340,17,tenue,false);
            parrafo(descripciones[i],x+25,385,333,19,papel);
            boton("Jugar en "+nombres[i],"m2dificultad:"+opciones[i].name(),
                x+21,502,348,i==1);
        }
        parrafo("Cada oleada compromete entre 1 y 3 equipos seguros. Pulsa E junto a cada uno para repararlo. A los 2 s restantes, un equipo seguro se pondrá rojo. Ganas si todos quedan seguros.",31,610,1180,17,tenue);
        pie(juego.mandoActivo?"Stick: mover · RB: correr · X: salto · A: defender · Menú: pausa":
            "WASD o flechas: mover · Shift: correr · Espacio: salto · E: defender · P: pausa");
    }
    private void roles() {
        cabecera("¿Qué voz tendrá tu historia?","Un caso local de la entrega 1. Los otros personajes te acompañan como NPC.");
        String[] nombres={"Ciudadano","Periodista","Influencer","Candidato"};
        String[] textos={"Escucha a los vecinos y encuentra las fuentes. Tu voz puede frenar una cadena.","Documenta la declaración completa. Investiga antes de convertirla en titular.","Transmite junto al candidato. Dale alcance al contexto, no solo al escándalo.","Responde por tu campaña. Reconocer lo que no sabes también es una decisión."};
        for(int i=0;i<4;i++) {
            int x=28+i*312;panel(x,146,288,322,PaletaMorada.PANEL);
            imagen("portada",x+8,154,272,114);
            rect(x+8,154,272,114,new Color(33,21,47,155));
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
        panel(912,114,344,484,PaletaMorada.PANEL);
        texto("TU SIGUIENTE PASO",936,148,13,oro,true);
        parrafo(s.objetivo(),936,180,291,20,papel);
        texto("EVIDENCIAS  "+s.evidencias().size()+" / 2",936,265,14,tenue,true);
        boton("Archivo de evidencias [I]","evidencias",932,281,303,false);
        texto(s.verificando()?"Investigación sin cuenta atrás":"Decisión: "+(int)Math.ceil(juego.sesion.restante())+" s",936,357,16,oro,true);
        texto("Al agotarse: Ignorar",936,381,13,tenue,false);
        int[] vals={s.ciudad().informacionVerificada(),s.ciudad().confianza(),s.ciudad().desinformacion()};
        String[] labels={"Información verificada","Confianza ciudadana","Desinformación"};
        for(int i=0;i<3;i++) barra(labels[i],vals[i],936,413+i*49,292,i==2);
        panel(24,608,1232,72,PaletaMorada.PANEL);
        parrafo(avisoVisible()?juego.aviso+" [T] Consultar":s.mensaje(),42,631,1185,14,papel);
        pie("WASD / flechas: explorar    E / Espacio: interactuar    C: Civitas    I: pruebas    T: árboles    H: ayuda");
    }
    private void actualizarPresentacionMisionUno(SesionMisionUno.Vista vista) {
        if(sesionVisualMisionUno!=juego.misionUno) {
            sesionVisualMisionUno=juego.misionUno;
            vistaAnteriorMisionUno=null;
            reveladosMisionUno.clear();
            avisosRumorMisionUno.clear();
        }
        var anterior=vistaAnteriorMisionUno;
        vistaAnteriorMisionUno=vista;
        if(vista==null||anterior==null) return;

        if(juego.pantalla==JuegoCliente.Pantalla.CIVITAS) {
            for(int i=0;i<vista.publicaciones().size();i++) {
                if(anterior.fuentes().get(i).isBlank()&&!vista.fuentes().get(i).isBlank())
                    reveladosMisionUno.put(vista.publicaciones().get(i).id(),juego.animacion);
            }
        }
        if(vista.fase()!=SesionMisionUno.Fase.CRISIS) return;
        for(int i=0;i<vista.peatones().size();i++) {
            var peaton=vista.peatones().get(i);
            if(peaton.animo()==SesionMisionUno.Animo.ALTERADO&&
                anterior.peatones().get(i).animo()==SesionMisionUno.Animo.NORMAL)
                avisosRumorMisionUno.put(peaton.id(),vista.tiempo());
        }
    }
    private void parqueMisionUno(SesionMisionUno.Vista s) {
        texto("CIUDAD NOVA / MISIÓN 1",28,30,14,oro,true);
        texto("Sofi y Mr. Palomo",28,65,29,papel,true);
        if(investigacion==null||!investigacion.premium()) texto("El parque escucha lo que compartes",28,92,14,tenue,false);
        if(s.fase()!=SesionMisionUno.Fase.INTRO&&(investigacion==null||investigacion.escena()==Escena.NINGUNA&&
            !(investigacion.premium()&&s.fase()==SesionMisionUno.Fase.CRISIS))) {
            String nombre=investigacion==null?"Civitas":"Libreta";
            String tecla=juego.mandoActivo?"X":investigacion==null?"C":"Tab";
            boton(nombre+" ["+tecla+"]","civitas",635,35,143,true);
        }
        boton(juego.mandoActivo?"Árboles [Y]":"Árboles [T]","arboles",788,35,143,false);
        boton(juego.mandoActivo?"Ayuda [View]":"Ayuda [H]","ayuda",941,35,143,false);
        boton("Menú","menu",1094,35,158,false);
        mundoMisionUno(s,24,114,866,484,false);
        if(investigacion!=null&&s.fase()!=SesionMisionUno.Fase.INTRO&&!calmaClasicaEnCurso(s)) {
            renderInvestigacion.parque(s,investigacion);return;
        }
        if(investigacion!=null&&investigacion.premium()&&s.fase()==SesionMisionUno.Fase.INTRO) {
            introPremium(s);
            return;
        }
        panel(912,114,344,484,PaletaMorada.PANEL);
        texto("TU MISIÓN · "+nombreDificultad(s.dificultad()),936,148,15,oro,true);
        String objetivo=switch(s.fase()) {
            case INTRO -> s.palomo().estado()==SesionMisionUno.EstadoPalomo.LLEGANDO?
                "Mr. Palomo llega volando. Espera a que aterrice.":
                s.palomo().estado()==SesionMisionUno.EstadoPalomo.SALIENDO?
                "Mr. Palomo vuela de regreso por el mismo camino.":
                juego.mandoActivo?"Escucha a Mr. Palomo. Pulsa A o Continuar.":
                    "Escucha a Mr. Palomo. Pulsa E o Continuar.";
            case CIVITAS -> "Examina las tres publicaciones en Civitas.";
            case CRISIS -> "Habla con las ocho personas alteradas. Cada pareja saldrá por un camino.";
            case RESULTADO -> "Consulta el resultado de Sofi.";
        };
        parrafo(objetivo,936,181,292,18,papel);
        texto("PERSONAS CALMADAS   "+s.calmados()+" / 8",936,271,15,oro,true);
        texto("PAREJAS FUERA   "+s.parejasFuera()+" / 4",936,303,15,papel,true);
        if(s.fase()==SesionMisionUno.Fase.CRISIS) {
            texto("TIEMPO   "+(int)Math.ceil(s.tiempo())+" s",936,351,22,s.tiempo()<8?PaletaMorada.PELIGRO:oro,true);
            texto(juego.mandoActivo?"Stick para moverte; A para hablar.":"WASD para moverte; E para hablar.",936,382,13,tenue,false);
        } else if(s.fase()==SesionMisionUno.Fase.INTRO&&
            s.palomo().estado()==SesionMisionUno.EstadoPalomo.POSADO) {
            boton(juego.mandoActivo?"Continuar [A]":"Continuar [E]","continuar",936,348,292,true);
        }
        barra("Convivencia",s.ciudad().convivencia(),936,427,282,false);
        barra("Información verificada",s.ciudad().informacionVerificada(),936,482,282,false);
        barra("Desinformación",s.ciudad().desinformacion(),936,537,282,true);
        panel(24,608,1232,72,PaletaMorada.PANEL);
        boolean cerca=s.fase()==SesionMisionUno.Fase.CRISIS&&s.peatones().stream().anyMatch(p->
            p.animo()==SesionMisionUno.Animo.ALTERADO&&Math.hypot(s.x()-p.x(),s.y()-p.y())<38);
        parrafo(s.mensaje(),42,636,cerca?650:1180,16,papel);
        if(cerca) {
            boton(juego.mandoActivo?"Escuchar [A]":"Escuchar [E]","interactuar",721,619,220,true);
            boton("Burlarse","m1:burlarse",956,619,268,false);
        }
        if(calmaClasicaEnCurso(s))
            pie(juego.mandoActivo?"Sofi: stick / cruceta · A: escuchar · X: libreta · Y: árboles · Menú: pausa":
                "Sofi: WASD / flechas · E / Espacio: escuchar · Tab: libreta · Esc/P: pausa · G: guardar");
        else
            pie(juego.mandoActivo?"Sofi: stick / cruceta · A: hablar · X: Civitas · Y: árboles · Menú: pausa":
                "Sofi: WASD / flechas · E / Espacio: hablar · C: Civitas · Esc/P: pausa · G: guardar");
    }
    private void introPremium(SesionMisionUno.Vista s) {
        panel(912,114,344,484,PaletaMorada.PANEL);
        texto("JUNTO A LA BANCA",934,154,15,oro,true);
        celda("mision1/sofi-sentada",2,1,0,943,207,108,149);
        int palomo=juego.movimientoReducido?0:(int)(juego.animacion*2)%2;
        celda("mision1/palomo",4,2,palomo,1072,225,144,131);
        texto("Sofi",961,391,16,papel,true);texto("Mr. Palomo",1074,391,16,oro,true);
        panel(24,608,1232,72,PaletaMorada.PANEL);
        parrafo(s.mensaje(),42,635,838,16,papel);
        if(s.palomo().estado()==SesionMisionUno.EstadoPalomo.POSADO)
            boton(juego.mandoActivo?"Continuar [A]":"Continuar [E]","continuar",918,620,288,true);
        pie("MISIÓN 1   /   El parque escucha lo que compartes");
    }
    private void mundoMisionUno(SesionMisionUno.Vista s,int vx,int vy,int vw,int vh,boolean fondo) {
        float centroX=s.x(),centroY=s.y();
        if(!fondo&&investigacion!=null&&investigacion.escena()!=Escena.NINGUNA&&!juego.movimientoReducido) {
            double u=investigacion.segundosEscena()/investigacion.escena().segundos;
            float viaje=(float)Math.sin(Math.PI*Math.clamp(u,0,1));
            centroX+=(320-centroX)*viaje;
            centroY+=(234-centroY)*viaje;
        }
        double escala=2;
        if(!fondo&&investigacion!=null&&investigacion.premium()) {
            double t=investigacion.segundosEscena();
            double amplitud=switch(investigacion.escena()) {
                case RUMORES -> juego.movimientoReducido?0:Math.min(transicion(t/.55),transicion((Escena.RUMORES.segundos-t)/.65));
                case VECINOS -> juego.movimientoReducido?0:Math.min(transicion(t/.5),transicion((DURACION_CONVOCATORIA-t)/.6));
                default -> 0;
            };
            escala-=.635*amplitud;
            centroX=(float)(centroX+(320-centroX)*amplitud);
            centroY=(float)(centroY+(228-centroY)*amplitud);
        }
        int anchoVisible=(int)Math.ceil(vw/escala),altoVisible=(int)Math.ceil(vh/escala);
        int camX=Math.max(0,Math.min(640-anchoVisible,Math.round(centroX)-anchoVisible/2));
        int camY=Math.max(0,Math.min(480-altoVisible,Math.round(centroY)-altoVisible/2));
        Graphics2D previo=g;g=(Graphics2D)g.create();g.clipRect(vx,vy,vw,vh);
        g.translate(vx-camX*escala,vy-camY*escala);g.scale(escala,escala);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        imagen("mision1/parque",0,0,640,480);
        int agua=juego.movimientoReducido?0:(int)(juego.animacion*5)%4;
        celda("mision1/fuente-agua",4,1,agua,276,164,88,90);
        if(!fondo&&investigacion!=null) ambienteInvestigacion(s,investigacion);
        if(s.fase()==SesionMisionUno.Fase.CRISIS&&s.ciudad().conflictos()>=29) {
            rect(295,207,5,3,new Color(0x675B8B));rect(340,226,7,2,new Color(0x675B8B));
            rect(317,238,4,2,new Color(0x675B8B));
        }
        List<SesionMisionUno.Peaton> peatones=new ArrayList<>(s.peatones());
        peatones.sort(Comparator.comparingDouble(SesionMisionUno.Peaton::y));
        for(var p:peatones) {
            if(p.animo()==SesionMisionUno.Animo.FUERA) continue;
            boolean moviendo=p.animo()==SesionMisionUno.Animo.ALTERADO||p.animo()==SesionMisionUno.Animo.SALIENDO
                ||p.animo()==SesionMisionUno.Animo.NORMAL;
            if(investigacion!=null&&investigacion.premium()&&s.fase()==SesionMisionUno.Fase.CRISIS&&vecino(p.id())!=null)
                moviendo=p.animo()==SesionMisionUno.Animo.SALIENDO;
            int paso=moviendo&&!juego.movimientoReducido?(int)(juego.animacion*8+p.id()*.7)%4:0;
            if(spritesTranseuntes==null) spritesTranseuntes=new SpritesTranseuntes(recursos);
            g.drawImage(spritesTranseuntes.imagen(p.id(),p.direccion(),paso),
                (int)p.x()-15,(int)p.y()-48,null);
            if(investigacion!=null&&!calmaClasicaEnCurso(s)) {
                reaccionVecino(s,p,fondo);
                continue;
            }
            if(p.animo()==SesionMisionUno.Animo.ALTERADO) {
                if(p.id()!=3) {
                    rect((int)p.x()+12,(int)p.y()+4,6,3,new Color(0x8E6387));
                    rect((int)p.x()+17,(int)p.y()+3,2,2,oro);
                }
                rect((int)p.x()-9,(int)p.y()-57,18,3,PaletaMorada.PELIGRO);
                Double inicio=avisosRumorMisionUno.get(p.id());
                if(!fondo&&inicio!=null&&inicio-s.tiempo()<DURACION_AVISO_RUMOR&&
                    p.x()>=camX-12&&p.x()<=camX+vw/2+12)
                    burbujaRumor((int)p.x(),(int)p.y()-61,camX,camX+vw/2);
                else texto("!",(int)p.x()-3,(int)p.y()-61,12,PaletaMorada.PELIGRO,true);
            } else if(p.animo()==SesionMisionUno.Animo.CALMADO||p.animo()==SesionMisionUno.Animo.SALIENDO) {
                texto("✓",(int)p.x()-4,(int)p.y()-54,11,oro,true);
            }
        }
        if(s.fase()==SesionMisionUno.Fase.INTRO) dibujarPalomo(s.palomo());
        else if(!fondo&&investigacion!=null&&investigacion.premium()&&investigacion.escena()==Escena.CIERRE) {
            double t=investigacion.segundosEscena();
            if(t>1.55) {
                double trayecto=Math.clamp((t-1.55)/2.65,0,1);
                int x=473-(int)(trayecto*184),y=117+(int)(Math.sin(Math.PI*trayecto)*53);
                int pose=juego.movimientoReducido?4:4+(int)(juego.animacion*11)%4;
                celda("mision1/palomo",4,2,pose,x,y,39,47);
            }
        } else if(!fondo&&investigacion!=null&&(investigacion.escena()==Escena.RUMORES||investigacion.escena()==Escena.CIERRE))
            dibujarPalomo(new SesionMisionUno.Palomo(s.palomo().lado(),
                Math.min(1,investigacion.segundosEscena()/investigacion.escena().segundos),SesionMisionUno.EstadoPalomo.SALIENDO));
        int sofiFrame=s.direccion()*4+(s.caminando()?juego.cuadro:0);
        if(s.fase()==SesionMisionUno.Fase.INTRO) {
            int quieta=juego.movimientoReducido?0:(juego.animacion%5.2)<.28?1:0;
            celda("mision1/sofi-sentada",2,1,quieta,(int)s.x()-16,(int)s.y()-42,32,42);
        } else {
            int x=(int)s.x()-16,y=(int)s.y()-47;
            if(investigacion!=null&&investigacion.premium()&&!juego.movimientoReducido) {
                double r=juego.investigacionSofi.finRecogida-juego.animacion;
                if(fondo&&investigacion.abierto()!=0&&r>.75) {
                    double gesto=Math.sin(Math.PI*Math.clamp((1.12-r)/.37,0,1));
                    int bajar=(int)(gesto*13);
                    celda("mision1/sofi-caminar",4,4,sofiFrame,x+(int)(gesto*5),y+bajar,32,47-bajar);
                    papelSuelo((int)(s.x()+13),(int)(s.y()-gesto*22),false);
                    g.dispose();g=previo;
                    return;
                }
                if(investigacion.escena()==Escena.VECINOS&&investigacion.segundosEscena()<1.4) {
                    int alzar=(int)(Math.sin(investigacion.segundosEscena()*5)*3);
                    celda("mision1/sofi-caminar",4,4,2,x,y-alzar,32,47);
                    texto("♪",x+29,y+4,13,oro,true);
                } else celda("mision1/sofi-caminar",4,4,sofiFrame,x,y,32,47);
            } else celda("mision1/sofi-caminar",4,4,sofiFrame,x,y,32,47);
            if(!s.caminando()&&s.direccion()==0&&!juego.movimientoReducido) {
                double t=juego.animacion%5.2;
                if(t<.22) parpadeoSofi(x,y,t<.10);
            }
        }
        g.dispose();g=previo;
        if(!fondo&&(investigacion==null||investigacion.escena()==Escena.NINGUNA)) {
            panel(vx+11,vy+11,128,38,new Color(56,38,75,220));
            texto("N  ↑  E  →",vx+21,vy+34,13,oro,true);
        }
    }
    private boolean calmaClasicaEnCurso(SesionMisionUno.Vista s) {
        return investigacion!=null&&investigacion.calmaClasica()&&investigacion.escena()==Escena.NINGUNA&&
            s.fase()==SesionMisionUno.Fase.CRISIS;
    }
    private static double transicion(double tiempo) {
        double u=Math.clamp(tiempo,0,1);
        return u*u*(3-2*u);
    }
    private void ambienteInvestigacion(SesionMisionUno.Vista s,Vista inv) {
        if(inv.premium()) {
            ambientePremium(s,inv);
            return;
        }
        if(!juego.movimientoReducido) for(int i=0;i<10;i++) {
            double t=juego.animacion*(9+i%3)+i*67;
            int x=(int)(t%630),y=65+(i*79)%365+(int)(Math.sin(t/21)*7);
            rect(x,y,3,1,(i%2==0?PaletaMorada.LILA:PaletaMorada.ACENTO));
            rect(x+1,y-1,2,3,PaletaMorada.LILA);
        }
        if(inv.escena()==Escena.RUMORES&&!juego.movimientoReducido) {
            for(int i=0;i<5;i++) {
                double t=inv.segundosEscena()-i*.19;
                if(t<0) continue;
                int x=85+(int)(130*t)+i*27,y=84+i*28+(int)(Math.sin(t*5+i)*18);
                papelSuelo(x,y,false);
            }
        }
        if(s.fase()==SesionMisionUno.Fase.CIVITAS&&inv.escena()==Escena.NINGUNA) for(var caso:CASOS) {
            boolean cerca=Math.hypot(caso.x()-s.x(),caso.y()-s.y())<=29;
            papelSuelo(caso.x(),caso.y(),cerca);
        }
    }
    private void ambientePremium(SesionMisionUno.Vista s,Vista inv) {
        if(!juego.movimientoReducido) {
            for(int i=0;i<12;i++) {
                double t=juego.animacion*(12+i%3)+i*67;
                int x=(int)(t%638),y=62+(i*79)%367+(int)(Math.sin(t/21)*7);
                rect(x,y,3,1,i%2==0?new Color(0x83AC8A):new Color(0xCDB674));
                rect(x+1,y-1,1,3,new Color(0xC2BF8A));
            }
            for(int i=0;i<3;i++) {
                double t=juego.animacion*21+i*17;
                int x=(int)(t%700)-27,y=74+i*6+(int)(Math.sin(t*.025)*9);
                int ala=(int)(Math.sin(juego.animacion*8+i)*3);
                rect(x,y,5,2,new Color(0xDEC6D9));
                rect(x-2,y+ala,3,1,new Color(0xAB88B1));
                rect(x+4,y-ala,3,1,new Color(0xAB88B1));
            }
        }
        if(s.fase()==SesionMisionUno.Fase.CIVITAS) {
            for(Caso caso:CASOS) {
                if(inv.escena()==Escena.RUMORES&&!juego.movimientoReducido) papelEnElViento(caso,inv.segundosEscena());
                else {
                    boolean cerca=Math.hypot(caso.x()-s.x(),caso.y()-s.y())<=29;
                    papelSuelo(caso.x(),caso.y(),cerca);
                    if(s.resueltas().get(caso.id()-1)) rect(caso.x()+3,caso.y()-5,4,4,new Color(0x478676));
                    else if(cerca&&!juego.movimientoReducido)
                        rect(caso.x()+3,caso.y()-7-(int)(Math.sin(juego.animacion*6)+1),4,1,oro);
                }
            }
        }
        boolean listo=juego.investigacionSofi.convocatoriaLista(s);
        dibujarTablon(listo,inv.escena()==Escena.VECINOS,s);
    }
    private void papelEnElViento(Caso caso,double segundos) {
        int i=caso.id()-1;
        double viaje=Math.clamp((segundos-.15-i*.21)/(2.8+i*.15),0,1);
        double avance=transicion(viaje);
        int sx=156+i*12,sy=126+i*4;
        double x=sx+(caso.x()-sx)*avance+Math.sin(viaje*16+i)*29*Math.sin(Math.PI*viaje);
        double y=sy+(caso.y()-sy)*avance;
        double altura=51*Math.sin(Math.PI*viaje)+8*Math.sin(viaje*18)*Math.sin(Math.PI*viaje);
        double tiempoSuelo=segundos-(.15+i*.21+2.8+i*.15);
        if(tiempoSuelo>0&&tiempoSuelo<.38) altura=4*Math.sin(Math.PI*tiempoSuelo/.38);
        if(viaje>=1&&altura<=.2) {papelSuelo(caso.x(),caso.y(),false);return;}
        g.setColor(new Color(25,15,34,110));
        g.fillOval((int)x-6,(int)y-4,12,5);
        Graphics2D anterior=g;g=(Graphics2D)anterior.create();
        g.translate(x,y-altura);
        g.rotate(Math.sin(viaje*14+i)*.78*Math.sin(Math.PI*viaje));
        g.scale(1,.79+.21*Math.cos(viaje*20));
        papelSuelo(0,0,false);
        g.dispose();g=anterior;
    }
    private void dibujarTablon(boolean listo,boolean convocando,SesionMisionUno.Vista s) {
        int x=TABLON_X,y=TABLON_Y;
        rect(x-16,y-5,32,5,new Color(0x4A314D));
        rect(x-13,y-16,4,15,new Color(0x5A3B43));rect(x+9,y-16,4,15,new Color(0x5A3B43));
        rect(x-17,y-37,34,23,new Color(0x432840));
        rect(x-15,y-35,30,19,new Color(0x93624B));
        rect(x-12,y-32,24,14,new Color(0xBB875E));
        rect(x-19,y-39,38,4,listo?oro:new Color(0x664655));
        for(int i=0;i<4;i++) if(s.resueltas().get(i)) {
            int xx=x-11+i*6;
            rect(xx,y-31,5,10,papel);rect(xx+1,y-28,3,1,new Color(0x795468));
        }
        if(listo&&s.fase()==SesionMisionUno.Fase.CIVITAS) {
            int dy=juego.movimientoReducido?0:(int)(Math.sin(juego.animacion*4)*3);
            texto("!",x-3,y-47+dy,15,oro,true);
        }
        if(convocando&&!juego.movimientoReducido&&investigacion.segundosEscena()<1.4) {
            int r=(int)(investigacion.segundosEscena()*24)%19+13;
            g.setColor(new Color(247,210,130,128));g.setStroke(new BasicStroke(1));
            g.drawOval(x-r,y-28-r,r*2,r*2);
        }
    }
    private void papelSuelo(int x,int y,boolean cerca) {
        if(cerca) {
            g.setStroke(new BasicStroke(1));g.setColor(oro);g.drawOval(x-13,y-6,26,12);
        }
        rect(x-6,y-5,13,8,PaletaMorada.SOMBRA);
        rect(x-6,y-7,12,8,papel);
        rect(x-4,y-5,7,1,PaletaMorada.LINEA);
        rect(x-4,y-3,5,1,PaletaMorada.LINEA);
        rect(x+4,y-7,2,2,oro);
    }
    private void reaccionVecino(SesionMisionUno.Vista s,SesionMisionUno.Peaton p,boolean fondo) {
        if(fondo||s.fase()!=SesionMisionUno.Fase.CRISIS) return;
        int x=(int)p.x(),y=(int)p.y()-52;
        if(investigacion.escena()==Escena.VECINOS&&p.animo()==SesionMisionUno.Animo.ALTERADO) {
            imagen(ICONO_RUMOR_FUENTE,x-8,y-20,16,16);
        } else if(vecino(p.id())!=null) {
            boolean informado=investigacion.orientado(p.id());
            TemaPixel.marco(g,x-9,y-16,18,15,tinta,informado?oro:TemaPixel.CIAN,false);
            texto(informado?"✓":"?",x-4,y-5,10,papel,true);
            var aviso=juego.investigacionSofi;
            if(investigacion.premium()&&p.id()==aviso.vecinoAviso&&aviso.finRespuestaVecino>juego.animacion) {
                int elevacion=juego.movimientoReducido?0:(int)(3*(2+Math.sin(juego.animacion*6)));
                imagen("iconos/04_libreta",x-8,y-38-elevacion,16,18);
            }
        }
    }
    private void burbujaRumor(int x,int y,int bordeIzquierdo,int bordeDerecho) {
        int centro=Math.clamp(x,bordeIzquierdo+36,bordeDerecho-36);
        int punta=Math.clamp(x,centro-24,centro+24);
        Color borde=juego.contraste?papel:oro;
        TemaPixel.marco(g,centro-32,y-22,64,20,tinta,borde,false);
        g.setColor(borde);
        g.fillPolygon(new int[]{punta-3,punta+3,punta},new int[]{y-2,y-2,y+2},3);
        imagen(ICONO_RUMOR_FUENTE,centro-27,y-19,14,14);
        texto("¿Agua?",centro-10,y-8,11,papel,true);
    }
    private void parpadeoSofi(int x,int y,boolean cerrado) {
        Color piel=new Color(0xF5B8A8),parpado=new Color(0x39213E);
        for(int ojo:new int[]{14,20}) {
            if(cerrado) rect(x+ojo,y+22,4,3,piel);
            rect(x+ojo,y+23,4,1,parpado);
        }
    }
    private void dibujarPalomo(SesionMisionUno.Palomo palomo) {
        if(palomo.estado()==SesionMisionUno.EstadoPalomo.FUERA) return;
        if(palomo.estado()==SesionMisionUno.EstadoPalomo.POSADO) {
            int gesto=investigacion!=null&&investigacion.premium()&&!juego.movimientoReducido&&
                juego.animacion%1.2<.35?1:0;
            celda("mision1/palomo",4,2,gesto,153,84,40,40);return;
        }
        int[][] origenes={{24,-49},{450,65},{320,263},{-52,65}};
        int lado=palomo.lado();
        double recorrido=palomo.estado()==SesionMisionUno.EstadoPalomo.LLEGANDO?
            palomo.progreso():1-palomo.progreso();
        double suavizado=recorrido*recorrido*(3-2*recorrido);
        int x=(int)Math.round(origenes[lado][0]+(148-origenes[lado][0])*suavizado);
        int y=(int)Math.round(origenes[lado][1]+(78-origenes[lado][1])*suavizado
            -12*Math.sin(Math.PI*recorrido));
        BufferedImage atlas=recursos.imagen("mision1/palomo");
        int frame=4+(juego.movimientoReducido?0:(int)(juego.animacion*11)%4);
        int sx=(frame%4)*atlas.getWidth()/4,sy=atlas.getHeight()/2+48;
        int ex=(frame%4+1)*atlas.getWidth()/4,ey=atlas.getHeight();
        boolean haciaIzquierda=palomo.estado()==SesionMisionUno.EstadoPalomo.LLEGANDO?
            lado==1:lado!=1;
        g.drawImage(atlas,haciaIzquierda?x+48:x,y,
            haciaIzquierda?x:x+48,y+48,sx,sy,ex,ey,null);
    }
    private void celda(String nombre,int columnas,int filas,int indice,int x,int y,int w,int h) {
        BufferedImage im=recursos.imagen(nombre);
        int col=indice%columnas,row=indice/columnas;
        Rectangle r=limiteCelda(nombre,im,columnas,filas,indice);
        if(nombre.startsWith("mision1/")) {
            int inicio=nombre.equals("mision1/palomo")?row*columnas:0;
            int fin=nombre.equals("mision1/palomo")?inicio+columnas:columnas*filas;
            String grupo=nombre+":"+columnas+":"+filas+":"+inicio;
            Rectangle fijo=limitesGrupo.computeIfAbsent(grupo,k->{
                int minX=Integer.MAX_VALUE,minY=Integer.MAX_VALUE,maxX=0,maxY=0;
                for(int i=inicio;i<fin;i++) {
                    int cx=(i%columnas)*im.getWidth()/columnas;
                    int cy=(i/columnas)*im.getHeight()/filas;
                    Rectangle cel=limiteCelda(nombre,im,columnas,filas,i);
                    minX=Math.min(minX,cel.x-cx);minY=Math.min(minY,cel.y-cy);
                    maxX=Math.max(maxX,cel.x-cx+cel.width);
                    maxY=Math.max(maxY,cel.y-cy+cel.height);
                }
                return new Rectangle(minX,minY,maxX-minX,maxY-minY);
            });
            int origenX=col*im.getWidth()/columnas,origenY=row*im.getHeight()/filas;
            g.drawImage(im,x,y,x+w,y+h,origenX+fijo.x,origenY+fijo.y,
                origenX+fijo.x+fijo.width,origenY+fijo.y+fijo.height,null);
        } else g.drawImage(im,x,y,x+w,y+h,r.x,r.y,r.x+r.width,r.y+r.height,null);
    }
    private Rectangle limiteCelda(String nombre,BufferedImage im,int columnas,int filas,int indice) {
        return limitesCelda.computeIfAbsent(nombre+":"+indice,k->{
            int col=indice%columnas,row=indice/columnas;
            int x0=col*im.getWidth()/columnas,x1=(col+1)*im.getWidth()/columnas;
            int y0=row*im.getHeight()/filas,y1=(row+1)*im.getHeight()/filas;
            int minX=x1,minY=y1,maxX=x0-1,maxY=y0-1;
            for(int py=y0;py<y1;py++) for(int px=x0;px<x1;px++) if((im.getRGB(px,py)>>>24)>24) {
                minX=Math.min(minX,px);minY=Math.min(minY,py);
                maxX=Math.max(maxX,px);maxY=Math.max(maxY,py);
            }
            return maxX<minX?new Rectangle(x0,y0,x1-x0,y1-y0):
                new Rectangle(minX,minY,maxX-minX+1,maxY-minY+1);
        });
    }
    private void civitasMisionUno(SesionMisionUno.Vista s) {
        cabecera("Civitas / ¿qué merece compartirse?","Comprueba la procedencia y observa sus efectos en el parque.");
        for(int i=0;i<s.publicaciones().size();i++) {
            var p=s.publicaciones().get(i);int y=130+i*174;
            panel(28,y,786,164,PaletaMorada.PANEL);
            imagen(p.id()==1?ICONO_RUMOR_FUENTE:"iconos/01_celular",48,y+10,26,26);
            texto("#"+p.id()+"  "+p.autor(),82,y+29,16,oro,true);
            parrafo(p.texto(),48,y+58,352,18,papel);
            String fuente=s.fuentes().get(i);
            evidenciaCivitas(p,fuente,y);
            if(s.resueltas().get(i)) texto("DECISIÓN REGISTRADA",48,y+139,14,oro,true);
            else {
                if(fuente.isBlank()) boton("Contrastar","m1:verificar:"+p.id(),48,y+113,170,true);
                else texto("CONTRASTADA",56,y+139,15,oro,true);
                boton("Compartir","m1:compartir:"+(i+1),234,y+113,170,false);
                boton("Reportar","m1:reportar:"+(i+1),420,y+113,170,false);
                boton("Ignorar","m1:ignorar:"+(i+1),606,y+113,170,false);
            }
            if(s.fase()==SesionMisionUno.Fase.CRISIS&&fuente.isBlank())
                boton("Consultar fuente","m1:verificar:"+(i+1),48,y+113,250,false);
        }
        panel(834,130,418,512,PaletaMorada.PANEL);
        texto("EFECTO DE TUS DECISIONES",854,165,17,oro,true);
        parrafo(s.mensaje(),854,205,370,18,papel);
        texto(s.fase()==SesionMisionUno.Fase.CRISIS?"CRISIS: "+(int)Math.ceil(s.tiempo())+" s":"PUBLICACIONES: 3",854,307,19,oro,true);
        texto("Puntos: "+s.puntos(),854,343,17,papel,true);
        barra("Información",s.ciudad().informacionVerificada(),854,395,366,false);
        barra("Convivencia",s.ciudad().convivencia(),854,450,366,false);
        barra("Desinformación",s.ciudad().desinformacion(),854,505,366,true);
        boton("Volver al parque","volver",853,572,179,true);
        boton("Árboles [T]","arboles",1045,572,179,false);
        pie("Compara la publicación con la evidencia antes de decidir qué hacer.");
    }
    private void evidenciaCivitas(Publicacion publicacion,String fuente,int y) {
        int x=420, ancho=372, alto=94;
        boolean revelada=!fuente.isBlank();
        Double inicio=reveladosMisionUno.get(publicacion.id());
        double avance=!revelada?0:inicio==null||juego.movimientoReducido?1:
            Math.clamp((juego.animacion-inicio)/DURACION_REVELADO,0,1);
        TemaPixel.marco(g,x,y+12,ancho,alto,tinta,juego.contraste?papel:TemaPixel.CIAN,false);
        if(avance<1) {
            imagen("iconos/21_candado",x+13,y+24,24,24);
            texto("Procedencia pendiente",x+46,y+42,16,tenue,true);
            if(!revelada) texto("Riesgo "+publicacion.riesgo(),x+46,y+71,14,tenue,false);
        }
        if(avance==0) return;
        Graphics2D previo=g;
        g=(Graphics2D)g.create();
        g.clipRect(x,y+12,ancho+2,(int)Math.round((alto+2)*avance));
        TemaPixel.marco(g,x,y+12,ancho,alto,juego.contraste?tinta:PaletaMorada.SOMBRA,oro,false);
        String titulo=switch((int)publicacion.id()) {
            case 1 -> "Aviso del parque";
            case 2 -> "Agenda municipal";
            case 3 -> "Contexto del chiste";
            default -> "Fuente consultada";
        };
        imagen("iconos/04_libreta",x+13,y+18,24,24);
        texto(titulo,x+46,y+36,17,oro,true);
        parrafo(fuente,x+16,y+57,ancho-34,15,papel);
        g.dispose();
        g=previo;
    }
    private void resultadoMisionUno() {
        var s=juego.misionUno.vista();
        panel(28,138,712,478,PaletaMorada.PANEL);
        texto("DESENLACE "+s.desenlace(),52,185,29,oro,true);
        parrafo(s.mensaje(),52,230,650,21,papel);
        texto("PERSONAS CALMADAS  "+s.calmados()+" / 8",52,345,20,papel,true);
        texto("PAREJAS QUE SALIERON  "+s.parejasFuera()+" / 4",52,385,20,papel,true);
        parrafo("Sofi observa lo que cambió fuera de la pantalla. Sus decisiones en Civitas y sus conversaciones cuentan para el resultado.",52,462,644,17,tenue);
        if(juego.ultimaFila!=null&&juego.ultimaFila.mision()==1){
            texto("PUBLICACIONES RESPONSABLES  "+juego.ultimaFila.senales().getOrDefault("publicaciones_responsables",0)+
                " / 3    ·    BURLAS  "+juego.ultimaFila.senales().getOrDefault("burlas",0),52,541,16,papel,true);
        }
        panel(760,138,492,478,PaletaMorada.PANEL);
        texto(s.puntos()+" PUNTOS",786,184,29,oro,true);
        var c=s.ciudad();String[] nombres={"Información verificada","Confianza","Convivencia","Bienestar digital","Participación","Desinformación","Conflictos"};
        int[] valores={c.informacionVerificada(),c.confianza(),c.convivencia(),c.bienestar(),c.participacion(),c.desinformacion(),c.conflictos()};
        for(int i=0;i<7;i++) barra(nombres[i],valores[i],786,217+i*53,435,i>4);
        boton("Repetir / dificultad","mision1",28,638,290,true);
        boton("Menú principal","menu",335,638,290,false);
        boton("Elegir otra misión","jugar",642,638,290,false);
        boton(juego.mandoActivo?"Volver [B]":"Volver [Esc]","volver",960,638,290,false);
        pie("El desenlace combina las conversaciones con las publicaciones que decidiste difundir o verificar.");
    }
    private static String nombreDificultad(SesionMisionUno.Dificultad dificultad) {
        return switch(dificultad) {
            case FACIL -> "FÁCIL"; case NORMAL -> "NORMAL"; case DIFICIL -> "DIFÍCIL";
        };
    }
    private void estructurasMisionUno() {
        var s=juego.misionUno.vista();
        cabecera("Estructuras de la misión 1","Los cambios de Civitas y la difusión alteran el estado real del parque.");
        panel(28,140,389,479,PaletaMorada.PANEL);
        panel(429,140,397,479,PaletaMorada.PANEL);
        panel(838,140,414,479,PaletaMorada.PANEL);
        texto("AVL · PUBLICACIONES",47,174,17,oro,true);
        if(s.avl()!=null) dibujarAVL(s.avl(),223,283,82,0,-1);
        texto("Clave: riesgo + ID",47,560,14,tenue,false);
        texto("Verificar cambia el riesgo; reportar",47,582,13,papel,false);
        texto("con pruebas retira una publicación.",47,602,13,papel,false);
        texto("ÁRBOL · DECISIONES",448,174,17,oro,true);
        if(investigacion==null) decisionesMisionUno(s.decisiones(),0,211);
        else {
            for(var caso:CASOS) {
                var nodo=s.decisiones().hijos().get(caso.id()-1);
                int y=214+(caso.id()-1)*97;
                texto(nodo.texto(),448,y,17,oro,true);
                int i=0;
                for(var accion:nodo.hijos()) {
                    texto((accion.ejecutado()?"✓ ":"• ")+accion.texto(),448+(i%2)*159,y+23+(i/2)*21,
                        13,accion.ejecutado()?oro:papel,accion.ejecutado());
                    i++;
                }
            }
        }
        texto("GRAFO · DIFUSIÓN",857,174,17,oro,true);
        texto("Dos cadenas propagan el rumor",857,209,14,papel,false);
        texto("en oleadas entre ocho personas.",857,231,14,papel,false);
        int[] posX=new int[8],posY=new int[8];
        for(int i=0;i<8;i++) {posX[i]=i%2==0?922:1135;posY[i]=285+i/2*70;}
        g.setColor(PaletaMorada.LINEA);g.setStroke(new BasicStroke(2));
        for(var arista:s.conexiones()) {
            int a=arista.origen(),b=arista.destino();
            g.drawLine(posX[a],posY[a]+13,posX[b],posY[b]-13);
            g.drawLine(posX[b]-4,posY[b]-19,posX[b],posY[b]-13);
            g.drawLine(posX[b]+4,posY[b]-19,posX[b],posY[b]-13);
        }
        for(int i=0;i<8;i++) {
            var estado=s.peatones().get(i).animo();
            Color color=estado==SesionMisionUno.Animo.ALTERADO?PaletaMorada.PELIGRO:
                estado==SesionMisionUno.Animo.CALMADO||estado==SesionMisionUno.Animo.SALIENDO||
                    estado==SesionMisionUno.Animo.FUERA?oro:PaletaMorada.VIOLETA;
            g.setColor(color);g.fillOval(posX[i]-18,posY[i]-18,36,36);
            texto(""+(i+1),posX[i]-5,posY[i]+6,15,estado==SesionMisionUno.Animo.NORMAL?papel:tinta,true);
        }
        texto("Lila: sin llegar · Rosa: alterado",857,586,13,tenue,false);
        texto("Amarillo: calmado",857,606,13,tenue,false);
        boton(juego.mandoActivo?"Volver [B / Y]":"Volver [T]","volver",28,640,258,true);
        pie("AVL: orden por riesgo · Árbol n-ario: opciones elegidas · Grafo: conexiones reales del rumor");
    }
    private int decisionesMisionUno(ArbolDecisiones.Vista nodo,int nivel,int y) {
        if(y>594) return y;
        texto((nodo.ejecutado()?"✓ ":"• ")+nodo.texto(),448+nivel*25,y,14,nodo.ejecutado()?oro:papel,nodo.ejecutado());
        y+=23;
        for(var hijo:nodo.hijos()) y=decisionesMisionUno(hijo,nivel+1,y);
        return y;
    }
    private void ayudaMisionUno() {
        cabecera("Ayuda / misión 1","Sofi escucha, contrasta y conversa con la gente del parque.");
        panel(28,138,1224,464,PaletaMorada.PANEL);
        if(investigacion!=null&&investigacion.premium()) {
            parrafo(juego.mandoActivo?
                "Usa el stick para buscar papeles y A para recogerlos. Activa la lupa en el menú del expediente y recorre los detalles con la cruceta.":
                "Usa WASD o las flechas para caminar. Pulsa E junto a un periódico y Q para activar la lupa. Enfoca la letra pequeña y haz clic para registrar los detalles.",
                54,192,1140,22,papel);
            parrafo("Prepara cada aviso y convoca junto a la fuente. Cuando empiece el reloj, escucha a cada vecino y vuelve a interactuar para calmarlo con su aviso.",
                54,348,1140,20,tenue);
        } else if(investigacion!=null) {
            parrafo(juego.mandoActivo?
                "Muévete con el stick o la cruceta y pulsa A junto al periódico. Activa la lupa y enfoca detalles con la cruceta. Compara el titular con la fuente y elige una conclusión. Después, acércate a los vecinos alterados y pulsa A para escucharlos.":
                "Usa WASD o las flechas para moverte, E para investigar y Q para activar la lupa. Selecciona los detalles con clic. Compara el titular con la fuente y elige una conclusión. Después, acércate a los vecinos alterados y pulsa E para escucharlos.",
                54,188,1140,22,papel);
            parrafo("Empieza por el periódico de la banca. Tab abre la libreta. El reloj corre cuando empieza el minijuego de calmar a los vecinos. En el contraste puedes volver a intentar una respuesta.",54,362,1140,20,tenue);
        } else {
            parrafo("Escucha a Mr. Palomo y revisa las publicaciones en Civitas. Contrasta antes de compartir o reportar. Durante la crisis, acércate a los vecinos y usa el botón de conversación.",54,188,1140,23,papel);
            parrafo("Durante la crisis el reloj sigue corriendo si abres Civitas. Algunas personas necesitan varias conversaciones. Cada pareja calmada toma uno de los cuatro caminos.",54,362,1140,20,tenue);
        }
        boton("Volver a la misión","volver",54,524,344,true);
        if(investigacion!=null&&investigacion.premium()) pie("Tab abre la libreta · Esc vuelve al parque · G guarda la partida");
    }
    private void barra(String nombre,int valor,int x,int y,int w,boolean peligro) {
        texto(nombre,x,y,13,papel,false);texto(""+valor,x+w-26,y,13,oro,true);
        rect(x,y+8,w,6,PaletaMorada.VIOLETA);rect(x,y+8,Math.max(1,w*valor/100),6,peligro?PaletaMorada.PELIGRO:PaletaMorada.EXITO);
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
        rect(419,114,82,42,PaletaMorada.VIOLETA);rect(419,114,82,3,oro);
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
            g.setColor(new Color(33,21,47,70));g.fillOval((int)s.x()-10,(int)s.y()-4,20,7);
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
        panel(vx+12,vy+12,130,101,new Color(56,38,75,220));
        for(int row=0;row<30;row++) for(int col=0;col<40;col++) {
            int t=juego.sesion.mapa().suelo(col,row);rect(vx+17+col*3,vy+17+row*3,3,3,t==0?PaletaMorada.CESPED:t==1?PaletaMorada.SUPERFICIE_CLARA:PaletaMorada.AGUA);
        }
        rect(vx+17+(int)s.x()*3/16,vy+17+(int)s.y()*3/16,5,5,oro);
    }
    private void sprite(String rol,int frame,int x,int y) {imagen("actores/"+rol+"_%02d".formatted(frame),x-16,y-32,32,32);}
    private void marcador(String label,int x,int y,boolean listo) {
        g.setFont(tema.fuente(8,true));int w=g.getFontMetrics().stringWidth(label)+12;
        panel(x-w/2,y-10,w,14,tinta);texto((listo?"✓ ":"")+label,x-w/2+4,y,8,listo?PaletaMorada.EXITO:papel,true);
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
        panel(50,395,680,44,PaletaMorada.SUPERFICIE_CLARA);texto("ESTADO: "+(s.evidencias().size()==2?"FUENTES CONTRASTADAS":"SIN COMPROBAR"),67,424,14,tinta,true);
        boton("Compartir","compartir",53,464,157,false);boton("Verificar","verificar",226,464,157,true);
        boton("Reportar","reportar",399,464,157,false);boton("Ignorar","ignorar",572,464,157,false);
        if(s.evidencias().size()==2) {
            boton("Publicar contexto","contexto",53,527,213,true);boton("Guardar pruebas","guardar",283,527,210,false);
            if(juego.sesion.buscarDecision("rectificar")!=null) boton("Rectificar","rectificar",510,527,219,true);
        } else texto("Publicar contexto requiere el aviso + el testimonio.",54,550,15,teal,true);
        panel(779,133,475,499,PaletaMorada.PANEL);texto("RADAR DE PUBLICACIONES",803,169,15,oro,true);
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
            int x=28+i*624;panel(x,149,599,286,PaletaMorada.PANEL);
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
        panel(28,190,766,483,PaletaMorada.PANEL);panel(814,190,438,483,PaletaMorada.PANEL);
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
        panel(x,y-21,700-nivel*32,36,n.ejecutado()?teal:PaletaMorada.PANEL);
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
        panel(28,190,788,316,PaletaMorada.PANEL);panel(836,190,416,316,PaletaMorada.PANEL);
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
        g.setStroke(new BasicStroke(2));g.setColor(PaletaMorada.LINEA);
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
            g.setColor(PaletaMorada.LINEA);g.drawLine(x,y,(l+r)/2,y+82);
            dibujarNario(n.hijos().get(i),l,r,y+82,depth+1);
        }
        panel(x-55,y-17,110,35,n.ejecutado()?teal:PaletaMorada.VIOLETA);
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
        panel(766,139,487,478,PaletaMorada.PANEL);texto(s.puntos()+" PUNTOS",793,180,29,oro,true);
        EstadoCiudad c=s.ciudad();String[] names={"Información verificada","Confianza","Convivencia","Bienestar digital","Participación","Desinformación","Conflictos"};
        int[] vals={c.informacionVerificada(),c.confianza(),c.convivencia(),c.bienestar(),c.participacion(),c.desinformacion(),c.conflictos()};
        for(int i=0;i<7;i++) {
            int delta=vals[i]-(i>4?20:50);
            barra(names[i]+"  ("+(delta>=0?"+":"")+delta+")",vals[i],793,224+i*51,430,i>4);
        }
        boton("Otra historia","jugar",28,639,226,true);boton("Revisar los árboles","arboles",273,639,252,false);
        boton("Evidencias","evidencias",545,639,198,false);boton("Laboratorio","laboratorio",766,639,230,false);boton("Menú principal","menu",1012,639,241,false);
        if(avisoVisible()) {panel(45,516,682,93,PaletaMorada.SUPERFICIE_CLARA);parrafo(juego.aviso+" [T] Consultar",58,538,654,13,tinta);}
        pie("El historial conserva tus acciones incluso cuando una publicación sale del AVL.");
    }
    private void audio() {
        var opciones=juego.audio;var c=opciones.actual();
        cabecera("Audio","Ajusta la banda sonora y las señales del juego. Los cambios se escuchan y guardan al instante.");
        panel(28,122,754,454,PaletaMorada.PANEL);panel(806,122,446,454,PaletaMorada.PANEL);
        String[] nombres={"Volumen general","Música","Efectos del juego","Ambiente","Botones e interfaz"};
        var buses=alcaldedigital.cliente.audio.OpcionesAudio.Bus.values();
        for(int i=0;i<5;i++){int y=170+i*79;texto(nombres[i],52,y+24,21,papel,true);boton("−","audio:"+buses[i]+":-10",407,y,73,false);texto(c.valor(buses[i])+" %",518,y+29,22,oro,true);boton("+","audio:"+buses[i]+":10",660,y,73,false);}
        texto("MEZCLA Y ACCESIBILIDAD",831,159,22,oro,true);
        boton(c.mudo()?"Sonido: silenciado":"Sonido: activo","audio:mudo",831,192,396,false);
        boton(c.suave()?"Intensidad: reducida":"Intensidad: dinámica","audio:suave",831,257,396,false);
        boton(c.voces()?"Tonos de texto: activos":"Tonos de texto: desactivados","audio:voces",831,322,396,false);
        boton("Probar sonido","audio:prueba",831,387,396,true);
        parrafo("La música baja durante los diálogos. La pausa conserva la posición de la canción y permite escuchar los botones.",831,471,391,17,papel);
        texto(opciones.mensaje.isBlank()?"La música y los efectos también tienen señales visuales.":opciones.mensaje,40,608,17,oro,false);
        boton("Restaurar valores","audio:restaurar",28,638,390,false);boton("Volver a Cómo jugar","volver",848,638,404,true);
        pie("Esc / B: volver · preferencias de este equipo, independientes de la partida.");
    }
    private void graficos() {
        var opciones=juego.graficos;var c=opciones.edicion();
        cabecera("Gráficos","Ajusta el juego a tu pantalla. Los personajes, textos y controles conservan sus proporciones.");
        panel(28,122,754,454,PaletaMorada.PANEL);
        texto("PANTALLA Y REESCALADO",52,155,23,oro,true);
        texto("Modo de pantalla",52,203,17,papel,true);
        boton(c.completa()?"Pantalla completa sin bordes":"Ventana","grafico:modo",52,215,706,false);
        texto("Resolución · según tu monitor",52,290,17,papel,true);
        String resolucion=c.ventana().toString();
        if(opciones.esResolucionMonitor(c.ventana())||c.completa()&&c.ventana().equals(opciones.actual().ventana()))
            resolucion=opciones.pantalla+" · monitor en pantalla completa";
        else if(c.completa())resolucion+=" · se usa al volver a ventana";
        boton(resolucion,"grafico:tamano",52,302,706,false);
        texto("Ampliación",52,376,17,papel,true);
        boton(c.entero()?"Píxeles uniformes · escalado entero":"Ajustar a la pantalla · proporcional","grafico:escalado",52,388,706,false);
        texto("Filtro de imagen",52,463,17,papel,true);
        boton(c.filtro()==alcaldedigital.cliente.graficos.OpcionesGraficas.Filtro.NITIDO?"Nítido · pixel art":"Suave · bordes suavizados","grafico:filtro",52,475,706,false);
        panel(806,122,446,454,PaletaMorada.PANEL);
        texto("VISTA DE PANTALLA",831,155,23,oro,true);
        texto("Resolución original: 1280 × 720",831,195,17,papel,true);
        texto("Monitor: "+opciones.pantalla,831,225,17,papel,false);
        texto("Área disponible: "+opciones.salida,831,255,17,papel,false);
        texto("Imagen del juego: "+opciones.imagen,831,285,17,papel,false);
        parrafo(c.entero()?"El escalado entero mantiene píxeles del mismo tamaño. Puede dejar más márgenes. Si la ventana es pequeña, el juego se reduce para caber.":"El ajuste proporcional aprovecha el espacio disponible. En pantallas de otra proporción se muestran márgenes decorativos.",831,324,393,18,papel);
        parrafo("F11 alterna pantalla completa durante el juego. Las preferencias se guardan en este equipo.",831,446,393,17,oro);
        if(opciones.cambios())texto("Cambios preparados. Pulsa Aplicar.",40,608,17,oro,false);
        else if(!opciones.mensaje.isBlank())texto(opciones.mensaje,40,608,16,oro,false);
        else texto("Selecciona una opción para modificarla.",40,608,17,papel,false);
        boton("Aplicar","grafico:aplicar",28,638,390,true);
        boton("Restaurar valores","grafico:restaurar",438,638,390,false);
        boton("Volver a Cómo jugar","volver",848,638,404,false);
        pie("Esc / B: volver · F11: pantalla completa · la partida conserva sus datos.");
    }
    private void ayuda() {
        cabecera("Cómo jugar","Controles, accesibilidad y opciones de pantalla para acompañarte en la ciudad.");
        String[] titulos={"01 · Tu objetivo","02 · Cómo te mueves","03 · Qué significan las acciones","04 · Tu papel","05 · Los indicadores","06 · Accesibilidad"};
        String[] body={"Acompaña a Sofi y a los demás personajes por Ciudad Nova. Las ocho misiones se abren según tus victorias. Cada capítulo tiene ayuda durante la partida.",
            "J1: WASD, E y Espacio. J2 local: flechas, Enter y Mayús. Esc/P pausa; H ayuda; G guarda; T consultas. Ver controles muestra las acciones de cada misión.",
            "Compartir puede aumentar el daño. Verificar inicia una búsqueda. Reportar requiere pruebas y validación. Ignorar evita tu retransmisión, pero otros pueden continuar. Rectificar recupera parcialmente.",
            "Sofi escucha a sus vecinos; la periodista contrasta; el influencer publica; Juan responde por su campaña. Cada misión indica sus acciones propias y sus condiciones de victoria.",
            "Información, confianza, convivencia, bienestar y participación: cuidar. Desinformación y conflicto: reducir. Todos van de 0 a 100. Popularidad y verdad no son equivalentes.",
            "Activa alto contraste o movimiento reducido. Cada misión ofrece sus dificultades. Las señales combinan colores, formas y texto; la información esencial también aparece en pantalla."};
        String[] iconos={"06_fuente","19_mapa","07_compartir","01_celular","13_confianza","24_accesibilidad"};
        for(int i=0;i<6;i++) {
            int x=28+(i%2)*624,y=123+(i/2)*154;
            panel(x,y,596,144,PaletaMorada.PANEL);
            TemaPixel.marco(g,x+14,y+15,88,108,TemaPixel.NOCHE,TemaPixel.CIAN,false);
            if(i==3) imagen("actores/ciudadano_00",x+26,y+37,64,64);
            else imagen("iconos/"+iconos[i],x+26,y+40,64,64);
            texto(titulos[i],x+118,y+29,20,oro,true);
            parrafo(body[i],x+118,y+56,461,16,papel);
        }
        boton("Contraste: "+(juego.contraste?"alto":"normal"),"contraste",28,584,288,false);
        boton("Animaciones: "+(juego.movimientoReducido?"reducidas":"activas"),"movimiento",338,584,302,false);
        boton(juego.avisos?"Avisos: visibles":"Avisos: solo al consultar","avisos",660,584,286,false);
        boton("Ver controles","verControles",28,638,390,true);
        boton("Gráficos","graficos",438,638,390,false);
        boton("Audio","audio",848,638,404,false);
        pie("Esc / B: volver · F11: pantalla completa · Gráficos conserva las proporciones del juego.");
    }
}
