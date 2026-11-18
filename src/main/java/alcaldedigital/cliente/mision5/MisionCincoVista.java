package alcaldedigital.cliente.mision5;

import processing.core.*;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import alcaldedigital.servidor.batalla.*;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;

/** Misión 5 en el lienzo principal. Toda regla reside en el motor/servidor. */
public final class MisionCincoVista implements AutoCloseable {
    private static final int FONDO=0xff21152F,PANEL=0xff38264B,LILA=0xffBD91D5,ORO=0xffF4CB6A,PAPEL=0xffF5E8FB,CIAN=0xff64DDD5,ROSA=0xffE467BE;
    private static final String[][] INTRO={
        {"CIUDADANO","Están pasando cosas muy raras en Ciudad Nova."},
        {"PERIODISTA","¿Tú crees? Un momento… ¿quién eres y qué haces aquí?"},
        {"CIUDADANO","No sé, pero la trama de este juego me trajo acá."},
        {"PERIODISTA","Okay, eso es válido para mí, creo."},
        {"PERIODISTA","No, porfis, no otra vez. ¡Mr. Palomo, baja ese celular!"},
        {"MR. PALOMO","Pobre cosita… esta vez será hasta peor."},
        {"CIUDADANO","¿Alguien me puede explicar qué está pasando?"},
        {"CIVITAS","SERVIDOR EN ESTADO CRÍTICO. Publicaciones sin verificar en propagación."},
        {"PERIODISTA","Acércate al computador central. ¡Tenemos que reaccionar juntos!"}
    };
    private PApplet p;private final EstadoCiudad ciudadInicial;private final boolean reducido,contraste;
    private PImage oficina,fondo;private final PImage[][] personajes=new PImage[2][16];private final PImage[][] enemigos=new PImage[3][4];private final PImage[] palomoVuelo=new PImage[4];private PImage palomo,telefono;
    private CanalBatalla canal;private ServidorBatalla servidor;private Vista v;
    private final Set<Integer> teclas=new HashSet<>(),teclasBloqueadas=new HashSet<>();
    private enum PanelPausa { MENU, AYUDA, ESTRUCTURAS }
    private PanelPausa panelPausa=PanelPausa.MENU;
    private boolean pausaPendiente,esperarMandoNeutro;
    private int focoPausa,paginaAyuda;
    private final alcaldedigital.cliente.presentacion.AudioEscena sonidos=new alcaldedigital.cliente.presentacion.AudioEscena("M5");
    private final ExecutorService tareas=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"m5-conectar");t.setDaemon(true);return t;});
    private Future<CanalBatalla> conexion;
    private volatile boolean cerrada;private int rolInvitado=1;private Fase ultimaFase;private int ultVidas=-1;
    private boolean configurando=true,continuarGuardado,salir,resultadoRegistrado,conectando,seleccionIP,controlesConfiguracion;
    private int modo,rolLocal,eleccion;private Dificultad dificultad=Dificultad.NORMAL;
    private final Map<String,PImage> marcos=new HashMap<>();private String direccion="127.0.0.1",mensaje="",estadoGuardado="";private long semilla;private float animacion,palomoX=1340;private EstadoMando mando=EstadoMando.desconectado();
    private final RepositorioResultados resultados=new RepositorioResultados(GuardadoBatalla.directorio().resolve("campana.properties"));
    public MisionCincoVista(long semilla,EstadoCiudad ciudad,boolean reducido,boolean contraste){this.semilla=semilla;ciudadInicial=ciudad;this.reducido=reducido;this.contraste=contraste;}
    private boolean gestionExterna;
    public void gestionExterna(boolean v){gestionExterna=v;}
    public boolean listaComun(){return canal!=null&&v!=null&&!configurando;}
    public void continuarDesdeSistema(boolean continuar){continuarGuardado=continuar;}
    public void preseleccionarGuardado(int modalidad){if(modalidad>=0&&modalidad<=2)modo=modalidad;}
    public boolean pausaComun(){return pausaVisible();}
    public void pausarComun(){pausar();}
    public void reanudarComun(){reanudar();}
    public String guardarComun()throws IOException{if(canal==null)throw new IOException("No hay partida iniciada");comando(rolLocal,"GUARDAR",0);if(!canal.error().isBlank())throw new IOException(canal.error());return modo>=2?"Guardado solicitado al servidor.":"Partida guardada.";}
    public boolean dosTeclados(){return modo==1;}
    public boolean consultaAbiertaComun(){return panelPausa!=PanelPausa.MENU;}
    public void abrirConsultaComun(){pausar();panelPausa=PanelPausa.ESTRUCTURAS;}
    public void cerrarConsultaComun(){panelPausa=PanelPausa.MENU;}
    public boolean victoriaComun(){return v!=null&&v.fase()==Fase.FINAL&&v.gano();}
    public void iniciarEn(PApplet host){p=host;oficina=imagen("/cliente/mision2/oficina.png");fondo=imagen("/cliente/mision5/servidor-interior.png");hoja("/cliente/arte/mision1/sofi-caminar.png",personajes[0],new int[]{0,3,1,2});hoja("/cliente/mision2/periodista-caminar-hoja.png",personajes[1],new int[]{0,1,2,3});
        PImage atlas=imagen("/cliente/mision5/servidor-ataques.png");for(int r=0;r<3;r++)for(int c=0;c<4;c++)enemigos[r][c]=recorte(atlas.get(c*atlas.width/4,r*atlas.height/3,atlas.width/4,atlas.height/3));
        telefono=imagen("/cliente/arte/iconos/01_celular.png");palomo=imagen("/cliente/mision3/redisenio/palomo_talk_1.png");for(int i=0;i<4;i++)palomoVuelo[i]=imagen("/cliente/mision3/redisenio/palomo_fly_"+(i+1)+".png");
        for(String s:new String[]{"ui","alert","ping","hit"})alcaldedigital.cliente.audio.MotorAudio.global().precargar("mision3/audio/"+s);
        String preview=System.getProperty("ad.mision5.vista","");if(!preview.isBlank())previsualizar(preview);
    }
    private PImage imagen(String ruta){try(var in=getClass().getResourceAsStream(ruta)){if(in==null)throw new IOException("Falta "+ruta);return new PImage(ImageIO.read(in));}catch(IOException e){throw new IllegalStateException(e);}}
    private PImage recorte(PImage im){im.loadPixels();int x0=im.width,y0=im.height,x1=-1,y1=-1;for(int y=0;y<im.height;y++)for(int x=0;x<im.width;x++)if((im.pixels[y*im.width+x]>>>24)>24){x0=Math.min(x0,x);y0=Math.min(y0,y);x1=Math.max(x1,x);y1=Math.max(y1,y);}return x1>=x0?im.get(x0,y0,x1-x0+1,y1-y0+1):im;}
    private void hoja(String ruta,PImage[] destino,int[] filas){PImage h=imagen(ruta);for(int r=0;r<4;r++)for(int c=0;c<4;c++)destino[r*4+c]=recorte(h.get(c*h.width/4,filas[r]*h.height/4,h.width/4,h.height/4));}
    private void sonido(String s){sonidos.efecto(s,"mision3/audio/"+s,.42f,.12);}
    private GuardadoBatalla archivo(){return new GuardadoBatalla(GuardadoBatalla.directorio().resolve(modo==2||modo==3?"mision5-lan.bin":modo==1?"mision5-coop.bin":"mision5-solo.bin"));}
    private void conectar(){if(conectando)return;conectando=true;mensaje="Preparando la partida…";final Dificultad nivel=dificultad;final int elegido=modo;final String ip=direccion;final boolean cargar=continuarGuardado;final int rolElegido=rolInvitado;final GuardadoBatalla guardado=archivo();
        conexion=tareas.submit(()->{MotorBatalla motor;if(elegido==3){var cliente=new ClienteBatalla(ip,48505,rolElegido,rolElegido==0?"Ciudadano":"Periodista");if(cerrada){cliente.close();throw new IOException("Conexión cancelada");}return cliente;}
            if(cargar){if(!guardado.existe())throw new IOException("No existe un guardado para este modo");motor=guardado.cargar();if(motor.vista().solo()!=(elegido==0))throw new IOException("Guardado de otro modo");}
            else{motor=new MotorBatalla(semilla++,elegido==0,nivel);motor.ciudadBase(ciudadInicial);}
            if(cerrada)throw new IOException("Conexión cancelada");
            if(elegido==2){servidor=new ServidorBatalla(InetAddress.getByName("0.0.0.0"),48505,motor,guardado);try{var cliente=new ClienteBatalla("127.0.0.1",48505,0,"Ciudadano");if(cerrada){cliente.close();servidor.close();throw new IOException("Conexión cancelada");}return cliente;}catch(Exception e){servidor.close();servidor=null;throw e;}}
            return new CanalLocal(motor,guardado);
        });
    }
    public void actualizar(float dt){
        if(conexion!=null&&conexion.isDone()){try{canal=conexion.get();rolLocal=modo==3?rolInvitado:0;configurando=false;mensaje="";v=canal.vista();dificultad=v.dificultad();}catch(Exception e){Throwable causa=e.getCause()==null?e:e.getCause();mensaje="No se pudo abrir: "+causa.getMessage();}conexion=null;conectando=false;}
        if(canal==null)return;boolean estabaPausada=pausaVisible();v=canal.vista();if(v==null)return;
        if(v.pausa())pausaPendiente=false;
        if(estabaPausada&&!pausaVisible()){panelPausa=PanelPausa.MENU;detenerEntrada();}
        if(esperarMandoNeutro&&Math.abs(mando.izquierdaX())<.2f&&Math.abs(mando.izquierdaY())<.2f&&!mando.pulsado(EstadoMando.Boton.X))esperarMandoNeutro=false;
        boolean jugable=!pausaVisible()&&Set.of(Fase.OFICINA,Fase.AVISO,Fase.ESQUIVA,Fase.TRAICION).contains(v.fase());
        for(int r=0;r<2;r++){
            if(modo!=1&&r!=rolLocal)continue;
            float dx=0,dy=0;boolean salto=false;
            if(jugable){boolean primero=modo!=1||r==0;int l=primero?65:37,rr=primero?68:39,u=primero?87:38,d=primero?83:40;
                dx=(activa(rr)?1:0)-(activa(l)?1:0);dy=(activa(d)?1:0)-(activa(u)?1:0);
                if(modo!=1){dx+=(activa(39)?1:0)-(activa(37)?1:0);dy+=(activa(40)?1:0)-(activa(38)?1:0);}
                salto=activa(primero?32:16);
                if(r==rolLocal&&mando.conectado()&&!esperarMandoNeutro){dx+=mando.izquierdaX();dy+=mando.izquierdaY();salto|=mando.pulsado(EstadoMando.Boton.X);}
            }
            canal.movimiento(r,dx,dy,salto);
        }
        canal.actualizar(dt);v=canal.vista();
        float pasoVisual=pausaVisible()?0:Math.min(.1f,dt);animacion+=pasoVisual;
        if(v.fase()==Fase.INTRO&&v.linea()>=4){float destino=v.linea()>=7?1340:870;palomoX+=Math.max(-500*pasoVisual,Math.min(500*pasoVisual,destino-palomoX));}
        else if(v.fase()==Fase.OFICINA&&palomoX<1340)palomoX=Math.min(1340,palomoX+500*pasoVisual);else palomoX=1340;
        if(ultimaFase!=null&&v.fase()!=ultimaFase)sonido(v.fase()==Fase.AVISO?"alert":"ui");ultimaFase=v.fase();
        int vidas=v.jugadores()[0].vidas()+v.jugadores()[1].vidas();if(ultVidas>=0&&vidas<ultVidas)sonido("hit");ultVidas=vidas;
        if(v.fase()==Fase.FINAL&&!resultadoRegistrado)try{resultados.acreditar(v,ciudadInicial);resultadoRegistrado=true;estadoGuardado="Resultado y puntos guardados.";}catch(IOException e){estadoGuardado="Error al guardar resultado: "+e.getMessage();}
    }
    public void dibujar(){revisarBotonesAudio();p.pushStyle();p.noSmooth();p.textAlign(PApplet.LEFT,PApplet.BASELINE);p.background(FONDO);
        if(configurando){p.image(oficina,0,0,1280,720);configuracion();if(controlesConfiguracion)alcaldedigital.cliente.misiones.PantallaControles.dibujar(p,new alcaldedigital.cliente.entrada.MapaControles.Contexto(5,modo==1,"Ciudadano","Periodista",mando.nombre()));p.popStyle();return;}
        if(v==null){caja(180,235,920,140);texto("Esperando al servidor…",220,310,28,ORO);p.popStyle();return;}
        boolean exterior=Set.of(Fase.INTRO,Fase.OFICINA,Fase.ABSORCION,Fase.EPILOGO,Fase.FINAL,Fase.ESPERA).contains(v.fase());
        if(exterior)oficina();else combate();hud();
        switch(v.fase()){
            case ESPERA->sala();case INTRO->dialogo(INTRO[Math.min(8,v.linea())][0],v.solo()&&v.linea()==8?"Acércate al computador. Yo mantendré la conexión desde aquí.":INTRO[Math.min(8,v.linea())][1],v.solo()?"E / Enter: continuar":"E / Enter: continuar · ambos jugadores confirman");
            case OFICINA->{caja(75,620,1130,50);texto(v.solo()?"Acércate al servidor y pulsa E / Enter. La periodista mantiene el enlace desde la oficina.":"Acerca ambos personajes al servidor y pulsa E / Enter. Sus luces se vuelven amarillas.",100,652,18,PAPEL);}
            case ABSORCION->{float t=Math.min(1,v.reloj()/150f);p.fill(ORO,80+t*130);p.rect(0,0,1280,720);texto(v.solo()?"¡EL SERVIDOR TE ESTÁ ABSORBIENDO!":"¡EL SERVIDOR LOS ESTÁ ABSORBIENDO!",280,350,28,FONDO);}
            case AVISO->{}
            case TURNO->turno();case DECISION->decision();case EPILOGO->epilogo();case FINAL->resultado();default->{}
        }
        if(pausaVisible()&&(!gestionExterna||panelPausa!=PanelPausa.MENU)){
            p.noStroke();p.fill(13,8,24,210);p.rect(0,0,1280,720);
            switch(panelPausa){case MENU->pausa();case AYUDA->ayuda();case ESTRUCTURAS->estructuras();}
        }
        String error=canal.error();if(!error.isBlank()){caja(35,100,1210,75);texto(error,55,130,18,ORO);texto("Esc: salir · Unirse LAN permite reconectar al mismo rol.",55,157,16,PAPEL);}
        p.popStyle();
    }
    private void marco(float x,float y,float w,float h,int fondo,int borde,boolean adorno){
        String clave=(int)w+":"+(int)h+":"+fondo+":"+borde+":"+adorno;
        PImage im=marcos.computeIfAbsent(clave,k->{
            var b=new java.awt.image.BufferedImage((int)w+12,(int)h+14,java.awt.image.BufferedImage.TYPE_INT_ARGB);
            var g=b.createGraphics();g.translate(3,3);alcaldedigital.cliente.interfaz.TemaPixel.nitido(g);
            alcaldedigital.cliente.interfaz.TemaPixel.marco(g,0,0,(int)w,(int)h,new java.awt.Color(fondo,true),new java.awt.Color(borde,true),adorno);g.dispose();return new PImage(b);
        });p.image(im,x-3,y-3);
    }
    private void caja(float x,float y,float w,float h){marco(x,y,w,h,contraste?0xff140C1D:PANEL,LILA,true);}
    private void panel(String titulo,int x,int y,int w,int h){
        caja(x,y,w,h);int ancho=Math.min(w-50,Math.max(200,titulo.length()*16+32));
        marco(x+25,y-19,ancho,38,FONDO,ORO,false);texto(titulo,x+41,y+8,23,ORO);
    }
    private final alcaldedigital.cliente.audio.InterfazSonora audioUI=new alcaldedigital.cliente.audio.InterfazSonora();
    private final java.util.List<java.awt.Rectangle> botonesAudio=new java.util.ArrayList<>();
    private void revisarBotonesAudio(){if(configurando)audioUI.foco("m5:"+modo+":"+dificultad+":"+eleccion);String clave="";for(var r:botonesAudio)if(r.contains(p.mouseX,p.mouseY)){clave=r.x+":"+r.y;break;}audioUI.hover(clave);botonesAudio.clear();}
    private void boton(String titulo,int x,int y,int w,boolean activo){botonesAudio.add(new java.awt.Rectangle(x,y,w,42));
        boolean hover=p.mouseX>=x&&p.mouseX<x+w&&p.mouseY>=y&&p.mouseY<y+42;
        marco(x,y,w,42,activo?ORO:hover?0xff604077:PANEL,activo?PAPEL:hover?ORO:LILA,false);
        texto(titulo,x+16,y+28,18,activo?FONDO:PAPEL);
    }
    private void texto(String s,float x,float y,int size,int color){p.fill(color);p.textSize(size);p.text(s,x,y);}
    private void configuracion(){
        panel("MISIÓN 5 · EL SERVIDOR CRÍTICO",135,100,1010,560);
        texto(modo==0?"Solo el ciudadano · un corazón y cinco vidas":"Ciudadano + periodista · dos corazones independientes",170,177,18,PAPEL);
        String[] modos={"Individual · ciudadano","Dos personas · mismo teclado","Crear sala LAN","Unirse a sala LAN"};
        for(int i=0;i<4;i++)boton(modos[i],170,205+i*53,425,modo==i);
        for(Dificultad d:Dificultad.values())boton(d.nombre(),640,205+d.ordinal()*58,450,dificultad==d);
        texto(modo==3?"Dificultad elegida por el anfitrión":dificultad==Dificultad.FACIL?"Más margen, avisos largos y huecos amplios":dificultad==Dificultad.DIFICIL?"Más velocidad y menos margen":"Ritmo original de la misión",640,405,16,CIAN);
        texto("Arriba/abajo: modo · izquierda/derecha: dificultad",170,435,17,LILA);
        if(gestionExterna){marco(170,455,340,42,FONDO,LILA,false);texto(continuarGuardado?"Continuar partida guardada":"Partida nueva",186,483,17,PAPEL);}
        else boton(continuarGuardado?"Continuar guardado [R]":"Partida nueva [R]",170,455,340,continuarGuardado);
        marco(535,455,555,42,contraste?0xff140C1D:FONDO,seleccionIP?ORO:LILA,false);
        texto("IP: "+direccion+(seleccionIP?"_":"")+" [Tab]",550,483,17,PAPEL);
        texto(modo==1?"Jugador 2: usa este mismo teclado · Periodista · flechas / Enter":
            continuarGuardado?"El guardado conserva su dificultad.":"TCP 48505 · F cambia rol al unirse: "+(rolInvitado==0?"Ciudadano":"Periodista"),170,527,17,LILA);
        boton(conectando?"Conectando…":"Comenzar [Enter / A]",170,550,425,true);
        boton("Controles [H]",640,550,210,false);boton("Volver a misiones",870,550,220,false);
        texto(mensaje,170,629,16,CIAN);
    }
    private int participantes(){return v.solo()?1:2;}
    private void oficina(){
        p.image(oficina,0,0,1280,720);
        for(int i=0;i<2;i++){
            Jugador j=v.jugadores()[i];int f=j.movido()&&!reducido?(int)(v.tick()/8)%4:0;
            PImage a=personajes[i][j.orientacion()*4+f];float h=92,w=h*a.width/a.height;
            float px=j.x(),py=j.y();
            if(v.fase()==Fase.ABSORCION&&(!v.solo()||i==0)){
                float k=Math.max(.08f,1-v.reloj()/150f);h*=k;w*=k;
                px=640+(px-640)*k;py=400+(py-400)*k;
            }
            if(v.fase()==Fase.EPILOGO&&i==0){float k=Math.min(1,v.reloj()/75f);px=640+(px-640)*k;py=400+(py-400)*k;}
            p.image(a,px-w/2,py-h,w,h);
            texto(i==0?"CIUDADANO":v.solo()?"PERIODISTA · APOYO":"PERIODISTA",px-45,py+16,13,i==0?ORO:CIAN);
        }
        if((v.fase()==Fase.INTRO&&v.linea()>=4||v.fase()==Fase.OFICINA)&&palomoX<1320){
            boolean saliendo=v.fase()!=Fase.INTRO||v.linea()>=7;boolean volando=saliendo||Math.abs(palomoX-870)>2;
            PImage a=volando?palomoVuelo[reducido?0:(int)(animacion*7)%4]:palomo;
            p.pushMatrix();p.translate(palomoX,350);
            if(volando&&!saliendo){p.translate(80,0);p.scale(-1,1);}
            p.image(a,0,0,80,74);p.popMatrix();
            if(v.fase()==Fase.INTRO&&v.linea()==6&&!volando)p.image(telefono,palomoX+55,385,25,36);
        }
        if(v.fase()==Fase.OFICINA||v.fase()==Fase.ABSORCION){p.fill(ORO);for(int i=0;i<4;i++)p.rect(585+i*26,370,14,7);}
    }
    private void corazon(float x,float y,int color,int numero,float tam){p.noStroke();p.fill(color);float q=tam/8;int[][] bandas={{1,0,2},{5,0,2},{0,1,8},{0,2,8},{1,3,6},{2,4,4},{3,5,2}};for(int[] a:bandas)p.rect(x-4*q+a[0]*q,y-3*q+a[1]*q,a[2]*q,q);if(numero>0)texto(""+numero,x-3,y+4,12,FONDO);}
    private void combate(){p.image(fondo,0,0,1280,720);int frame=reducido?0:(int)(v.tick()/10)%4;p.image(enemigos[0][frame],545,90,190,195);marco(520,294,240,17,FONDO,LILA,false);p.noStroke();p.fill(ORO);p.rect(524,298,232*v.jefe()/100f,9);texto("DEFENSAS DE LA SEÑAL · "+v.jefe()+"%",515,329,14,PAPEL);
        caja(MotorBatalla.AX-4,MotorBatalla.AY-4,MotorBatalla.AW+8,MotorBatalla.AH+8);p.noStroke();p.fill(FONDO);p.rect(MotorBatalla.AX,MotorBatalla.AY,MotorBatalla.AW,MotorBatalla.AH);
        p.clip((int)MotorBatalla.AX,(int)MotorBatalla.AY,(int)MotorBatalla.AW,(int)MotorBatalla.AH);
        for(Obstaculo a:v.obstaculos()){boolean aviso=a.edad()<a.aviso();int color=a.tipo()==1?CIAN:a.tipo()==3?ORO:ROSA;p.noStroke();p.fill(color,aviso?75:235);p.rect(a.x(),a.y(),a.w(),a.h());if(a.tipo()==2){p.stroke(aviso?ORO:ROSA);p.strokeWeight(aviso?2:5);p.line(a.x(),a.y()+a.h()/2,a.x()+a.w(),a.y()+a.h()/2);p.noStroke();}else if(a.tipo()!=3&&a.w()<60&&a.h()<75)p.image(enemigos[1][frame],a.x()-3,a.y()-3,a.w()+6,a.h()+6);}
        for(int i=0;i<participantes();i++){Jugador j=v.jugadores()[i];if(j.vidas()>0&&(reducido||j.invulnerable()==0||v.tick()%8<5))corazon(j.x(),j.y(),i==0?ORO:CIAN,i+1,22);}
        p.noClip();for(var a:v.obstaculos())if(a.tipo()==2){p.image(enemigos[2][a.edad()<a.aviso()?1:2],205,a.y()-18,64,64);texto("OBJ "+(a.objetivo()+1),185,a.y()-23,14,a.objetivo()==0?ORO:CIAN);}
        texto(v.gravedad()?v.gravedadDir()==1?"GRAVEDAD: PARED DERECHA · salto hacia la izquierda":"GRAVEDAD: SUELO · salto de altura variable":(v.solo()?"MOVIMIENTO LIBRE · solo el ciudadano":"MOVIMIENTO LIBRE · los corazones no chocan entre sí"),280,649,17,CIAN);
    }
    private void hud(){marco(10,8,680,70,FONDO,LILA,true);texto("MISIÓN 5 · EL SERVIDOR CRÍTICO",25,32,22,ORO);texto("RONDA "+Math.min(5,v.ronda()+1)+"/5 · "+v.dificultad().nombre()+" · "+(modo>=2?"SALA LAN":"PARTIDA LOCAL"),25,61,17,LILA);
        for(int i=0;i<participantes();i++){Jugador j=v.jugadores()[i];float x=720+i*275;marco(x-10,8,260,70,FONDO,LILA,true);texto((i+1)+" "+j.nombre()+" · "+j.puntos()+" pts",x,27,17,i==0?ORO:CIAN);for(int k=0;k<5;k++)corazon(x+12+k*25,48,k<j.vidas()?(i==0?ORO:CIAN):0xff604077,0,18);texto(v.solo()?"INDIVIDUAL · 5 vidas":j.recuperaciones()+" rescate · "+(j.conectado()?"conectado":"ausente"),x,71,13,PAPEL);}
        marco(10,684,1256,30,FONDO,LILA,false);texto("WASD / flechas: mover · Espacio / X: salto · E / Enter: acción · Esc / P: pausa · H: ayuda · T: estructuras",25,705,15,LILA);
    }
    private void dialogo(String nombre,String texto,String pie){
        marco(75,550,1130,120,contraste?0xff140C1D:PANEL,LILA,true);
        marco(100,532,Math.max(195,nombre.length()*15+32),36,FONDO,ORO,false);
        texto(nombre,116,557,20,ORO);
        marco(98,580,64,72,FONDO,LILA,false);
        PImage retrato=switch(nombre){case "CIUDADANO"->personajes[0][0];case "PERIODISTA"->personajes[1][0];case "MR. PALOMO"->palomo;default->telefono;};
        float alto=60,ancho=Math.min(50,alto*retrato.width/retrato.height);
        p.image(retrato,130-ancho/2,586,ancho,alto);
        p.fill(PAPEL);p.textSize(20);p.text(texto,185,576,980,47);
        p.stroke(LILA);p.strokeWeight(1);p.line(185,631,950,631);p.noStroke();
        texto(pie,185,653,14,CIAN);boton("Continuar [Enter]",975,620,200,true);
    }
    private void sala(){panel("SALA DE MISIÓN 5",180,180,920,420);texto(modo>=2?"Anfitrión: "+ips()+" · puerto 48505":"Confirma que estás listo para entrar.",220,282,18,PAPEL);for(int i=0;i<participantes();i++)texto((i+1)+" · "+v.jugadores()[i].nombre()+" · "+(v.jugadores()[i].conectado()?v.jugadores()[i].listo()?"LISTO":"conectado, falta confirmar":"esperando conexión"),220,345+i*50,23,i==0?ORO:CIAN);texto((v.solo()?"E / A: comenzar con el ciudadano":"E / A: Listo · segundo teclado: Enter"),220,510,22,PAPEL);texto("H: ayuda · Esc / P: pausa y opciones de salida",220,553,18,LILA);}
    private String ips(){try{List<String> r=new ArrayList<>();for(var i:Collections.list(NetworkInterface.getNetworkInterfaces()))for(var a:Collections.list(i.getInetAddresses()))if(a instanceof Inet4Address&&!a.isLoopbackAddress())r.add(a.getHostAddress());return r.isEmpty()?"127.0.0.1":String.join(" / ",r);}catch(Exception e){return "IP de este equipo";}}
    private void turno(){panel(v.solo()?"TU TURNO · CIUDADANO":"TURNO CONJUNTO",140,185,1000,470);texto(v.mensaje(),175,270,17,PAPEL);marco(200,300,880,35,FONDO,LILA,false);p.noStroke();p.fill(ORO);p.rect(615,302,50,31);p.fill(CIAN);p.rect(200+MotorBatalla.indicador(v.reloj())*870,293,10,49);
        String[] opciones={"Verificar una publicación: baja la velocidad de los paquetes","Pulso de precisión: responde al servidor y suma puntos",(v.solo()?"Recuperar una vida del ciudadano":"Reparar al compañero: recupera una vida")};for(int i=0;i<3;i++)boton((i+1)+". "+opciones[i],175,360+i*48,930,i==eleccion);
        for(int i=0;i<participantes();i++)if(v.jugadores()[i].accionLista()){p.fill(i==0?ORO:CIAN);p.rect(200+MotorBatalla.indicador(v.golpes()[i])*870,288+i*7,8,53);}
        texto("Ciudadano: "+(v.jugadores()[0].accionLista()?"listo":"eligiendo")+(v.solo()?"":" · Periodista: "+(v.jugadores()[1].accionLista()?"listo":"eligiendo")),175,535,17,ORO);
        texto(v.dificultad()==Dificultad.DIFICIL?"Difícil: sin recuperación automática de corazones.":"Ronda superada: +1 corazón por superviviente, hasta cinco vidas.",175,584,15,CIAN);
        texto((v.solo()?"1 / 2 / 3: elige la acción del ciudadano":"1 / 2 / 3: ciudadano · 7 / 8 / 9: periodista en teclado compartido"),175,564,17,CIAN);texto((v.solo()?"E / A: confirmar selección · solo cuenta tu respuesta.":"E / A: confirmar selección · no hay nuevo turno hasta que ambos respondan."),175,605,17,LILA);
    }
    private void decision(){
        marco(75,480,1130,185,contraste?0xff140C1D:PANEL,LILA,true);marco(100,461,230,36,FONDO,ORO,false);texto("EL SERVIDOR",116,487,22,ORO);
        texto("Puedo volver a ser útil. ¿Me dejarán conectado? La señal todavía fluctúa.",105,545,20,PAPEL);
        boton("Perdonar · afrontar último pulso [1]",105,570,510,Math.min(1,eleccion)==0);
        boton("Aislar · recuperar la red [2]",645,570,530,Math.min(1,eleccion)==1);
        texto(v.solo()?"Tú decides · arriba/abajo y A también funcionan":"Ambos confirman su elección · segundo teclado 7 / 8",105,645,16,CIAN);
    }
    private void epilogo(){if(v.solo()){
        String[][] l={{"CIVITAS",v.gano()?"El enlace del ciudadano se estabilizó. Has salido del servidor.":"La señal te expulsó antes de completar la defensa."},{"PERIODISTA",v.gano()?"¡Volviste! Mantuve la conexión desde aquí.":"Estás fuera. Guardamos lo que alcanzaste a verificar."},{"CIUDADANO","En definitiva, la época de elecciones sí es rara."},{"PERIODISTA","Sí. Y todavía falta la elección."}};
        dialogo(l[Math.min(3,v.linea())][0],l[Math.min(3,v.linea())][1],"E / Enter / A: continuar");return;
    }String[][] l=v.gano()?new String[][]{{"CIVITAS","Señal aislada. Las publicaciones verificadas se conservan."},{"PERIODISTA","Estamos fuera. Ahora sí, no vuelvas a pegarle al computador."},{"CIUDADANO","En definitiva, la época de elecciones sí es rara."},{"PERIODISTA","Sí. Y todavía falta la elección."}}:new String[][]{{"CIVITAS","La defensa no resistió. Los dos personajes fueron expulsados."},{"CIUDADANO","¿El servidor nos acaba de escupir?"},{"PERIODISTA","Guardamos lo que alcanzamos a verificar. Tendremos otra oportunidad."},{"AMBOS","En definitiva, la época de elecciones sí es rara."}};dialogo(l[Math.min(3,v.linea())][0],l[Math.min(3,v.linea())][1],"E / Enter / A: continuar · ambos confirman");}
    private void resultado(){panel(v.gano()?"SERVIDOR RECUPERADO":"LA SEÑAL RESISTIÓ",210,175,860,450);texto(v.mensaje(),250,282,20,PAPEL);for(int i=0;i<participantes();i++)texto(v.jugadores()[i].nombre()+": "+v.jugadores()[i].puntos()+" puntos · "+v.jugadores()[i].vidas()+" vidas restantes",250,345+i*43,22,i==0?ORO:CIAN);texto("Publicaciones verificadas: "+v.verificados(),250,450,21,PAPEL);texto(estadoGuardado,250,500,18,CIAN);boton("Volver a misiones [Enter / A]",250,550,385,true);boton("Nueva partida [R]",655,550,375,false);}
    private void pausa(){
        panel(v.solo()?"PAUSA":"PAUSA COMPARTIDA",270,126,740,535);
        texto("La partida está detenida. Continúa cuando estés listo.",310,203,19,PAPEL);
        String[] opciones={"Continuar [Esc / P]","Ayuda · momentos y patrones [H]","Controles y reglas","Guardar ahora [G]","Ver estructuras [T]","Guardar y salir a misiones"};
        for(int i=0;i<opciones.length;i++)boton(opciones[i],310,230+i*58,660,focoPausa==i);
        p.fill(ORO);p.textSize(15);p.text(mensaje.isBlank()?v.mensaje():mensaje,310,593,650,32);
        texto("Arriba / abajo: elegir · Enter / A: confirmar",310,642,16,LILA);
    }
    private void ayuda(){
        panel("AYUDA · MOMENTOS Y PATRONES",80,115,1120,545);
        for(int i=0;i<7;i++)boton(i==0?"Controles":i==6?"Final":"Ronda "+i,110+i*152,155,140,paginaAyuda==i);
        if(paginaAyuda>=1&&paginaAyuda<=5){
            texto("MOMENTO "+paginaAyuda+" · supera sus tres ataques para llegar al turno",115,224,20,ORO);
            for(int i=0;i<3;i++){
                int indice=(paginaAyuda-1)*3+i,y=242+i*108;var guia=GuiaBatalla.PATRONES.get(indice);
                marco(110,y,1060,96,FONDO,LILA,true);texto((i+1)+". "+guia.nombre(),130,y+24,20,ORO);
                texto(guia.situacion(),130,y+48,17,PAPEL);texto(guia.respuesta(),130,y+73,17,CIAN);
            }
            texto(v.dificultad()==Dificultad.DIFICIL?"Difícil: más presión y sin curación automática de ronda.":"Al superar la ronda: +1 corazón por superviviente, hasta cinco vidas.",115,587,17,PAPEL);
        }else if(paginaAyuda==0){
            String[] reglas={"Individual / LAN: WASD o flechas para mover; E / Enter para confirmar; Espacio para saltar.","Dos en un teclado: ciudadano WASD / E / Espacio; periodista flechas / Enter / Shift.","Mando: stick para mover; X para saltar; A para confirmar; Menú para pausar; B para volver.",v.solo()?"Juegas solo con el ciudadano: un corazón y cinco vidas. La periodista permanece en la oficina.":"Dos corazones sin colisión. Si uno cae, el otro puede rescatarlo una vez al superar un ataque.","Cinco rondas de tres ataques. Rosado: esquiva. Cian: detén el movimiento real, incluida la caída.","Turno: verificar frena paquetes; pulso suma puntos; reparar recupera una vida.","Fácil / Normal: +1 corazón por superviviente al superar una ronda. Máximo cinco. Difícil: sin ayuda.","Esc / P: pausa. H: esta ayuda. T: estructuras. La consulta detiene la partida, también en LAN.","En LAN, ambos roles deben estar conectados para reanudar. G solicita guardar la partida."};
            for(int i=0;i<reglas.length;i++)texto(reglas[i],115,239+i*36,18,i%2==0?PAPEL:CIAN);
        }else{
            texto("DESPUÉS DE LAS CINCO RONDAS",115,239,24,ORO);
            String[] finalGuia={"Aislar: recupera la red y termina el combate con éxito.","Perdonar: activa un último ataque de siete segundos; todavía puedes ganar si sobrevives.","Se combinan haces dirigidos, lluvia de paquetes y franjas cian.","Sal de la línea anunciada antes del disparo, guarda una salida y detente ante el cian.","En cooperativo los haces apuntan por turnos a corazones vivos. No hace falta separarse siempre.","Después del regreso a la oficina, E / Enter / A avanza las frases sin reiniciar la animación."};
            for(int i=0;i<finalGuia.length;i++)texto(finalGuia[i],115,292+i*46,18,i%2==0?PAPEL:CIAN);
        }
        boton("Anterior [Izq.]",110,610,235,false);boton("Volver a pausa [Esc]",360,610,360,true);boton("Siguiente [Der.]",735,610,250,false);
        texto((paginaAyuda+1)+" / 7",1050,638,18,ORO);
    }
    private void estructuras(){panel("RED Y ESTRUCTURAS",80,115,1120,545);texto("Grafo de difusión · origen "+v.patron()%6+" · BFS "+MotorBatalla.difusionDesde(v.patron()%6),115,211,20,CIAN);for(int i=0;i<6;i++){float x=180+i*170,y=270+(i%2)*35;for(int j:MotorBatalla.GRAFO[i]){p.stroke(LILA);p.line(x,y,180+j*170,270+(j%2)*35);}p.noStroke();corazon(x,y,ORO,i+1,22);}texto("AVL: "+v.publicaciones().length+" publicaciones activas · "+(v.publicaciones().length==0?"índice vacío":v.publicaciones()[0]),115,370,16,PAPEL);texto("Árbol: la ruta de cada ronda registra verificar, pulso o reparar.",115,414,18,PAPEL);String[] hist=v.historial();for(int i=Math.max(0,hist.length-4),y=465;i<hist.length;i++,y+=33)texto(hist[i],115,y,17,LILA);boton("Volver a pausa [Esc / T]",115,605,450,true);}
    private void comando(int rol,String a,int n){if(canal==null)return;canal.accion(rol,a,n);v=canal.vista();alcaldedigital.cliente.audio.InterfazSonora.activar(a.equals("GUARDAR")?"guardar":a.equals("REANUDAR")?"reanudar":"click");}
    private boolean pausaVisible(){return pausaPendiente||v!=null&&v.pausa();}
    private boolean activa(int k){return teclas.contains(k)&&!teclasBloqueadas.contains(k);}
    private void detenerEntrada(){teclasBloqueadas.addAll(teclas);esperarMandoNeutro=true;if(canal!=null)for(int i=0;i<2;i++)canal.movimiento(i,0,0,false);}
    private void pausar(){if(canal==null)return;detenerEntrada();if(!pausaVisible()){focoPausa=0;pausaPendiente=true;comando(rolLocal,"PAUSA",0);}}
    private void reanudar(){detenerEntrada();panelPausa=PanelPausa.MENU;pausaPendiente=false;mensaje="";comando(rolLocal,"REANUDAR",0);}
    private void abrirAyuda(boolean controles){
        pausar();panelPausa=PanelPausa.AYUDA;
        paginaAyuda=controles?0:switch(v.fase()){case AVISO,ESQUIVA,TURNO->Math.min(5,v.ronda()+1);case DECISION,TRAICION->6;default->0;};
    }
    private void activarPausa(){switch(focoPausa){
        case 0->reanudar();case 1->abrirAyuda(false);case 2->abrirAyuda(true);
        case 3->{comando(rolLocal,"GUARDAR",0);mensaje="Guardado solicitado.";}
        case 4->panelPausa=PanelPausa.ESTRUCTURAS;
        case 5->{comando(rolLocal,"GUARDAR",0);salir=true;}
        default->{}
    }}
    private void teclaPausa(int k){
        if(k==80){reanudar();return;}
        if(k==27){if(panelPausa==PanelPausa.MENU)reanudar();else panelPausa=PanelPausa.MENU;return;}
        if(k==71){comando(rolLocal,"GUARDAR",0);mensaje="Guardado solicitado.";return;}
        if(k==72){if(panelPausa==PanelPausa.AYUDA)panelPausa=PanelPausa.MENU;else abrirAyuda(false);return;}
        if(k==84){panelPausa=panelPausa==PanelPausa.ESTRUCTURAS?PanelPausa.MENU:PanelPausa.ESTRUCTURAS;return;}
        if(panelPausa==PanelPausa.AYUDA){
            if(k==37)paginaAyuda=Math.floorMod(paginaAyuda-1,7);
            if(k==39)paginaAyuda=(paginaAyuda+1)%7;
            if(k>=48&&k<=54)paginaAyuda=k-48;
            if(k==10||k==69)panelPausa=PanelPausa.MENU;
            return;
        }
        if(panelPausa==PanelPausa.ESTRUCTURAS){if(k==10||k==69)panelPausa=PanelPausa.MENU;return;}
        if(k==38||k==87)focoPausa=Math.floorMod(focoPausa-1,6);
        if(k==40||k==83||k==9)focoPausa=(focoPausa+1)%6;
        if(k==10||k==69||k==32)activarPausa();
    }
    private void continuar(int rol){if(v==null||pausaVisible())return;switch(v.fase()){case ESPERA->comando(rol,"LISTO",0);case INTRO,EPILOGO->comando(rol,"CONTINUAR",0);case OFICINA->comando(rol,"INTERACTUAR",0);case TURNO->comando(rol,"ACCION",eleccion);case FINAL->salir=true;default->{}}}
    public void pulsar(char c,int k){if(k==13)k=10;if(!teclas.add(k))return;if(configurando&&conectando)return;
        if(configurando){if(controlesConfiguracion){if(k==27||k==10||k==72)controlesConfiguracion=false;return;}if(seleccionIP){if(k==10||k==9||k==27){seleccionIP=false;return;}if(k==65&&teclas.contains(17)){direccion="";return;}if(k==8&& !direccion.isEmpty())direccion=direccion.substring(0,direccion.length()-1);else if(c>=32&&c<127&&direccion.length()<80)direccion+=c;return;}if(k==27)salir=true;if(k==38)modo=Math.floorMod(modo-1,4);if(k==40)modo=(modo+1)%4;if((k==37||k==39)&&modo!=3)dificultad=Dificultad.values()[Math.floorMod(dificultad.ordinal()+(k==37?-1:1),3)];if(k>=49&&k<=51&&modo!=3)dificultad=Dificultad.values()[k-49];if(k==70&&modo==3)rolInvitado=1-rolInvitado;if(k==82&&!gestionExterna)continuarGuardado=!continuarGuardado;if(k==9)seleccionIP=true;if(k==10||k==69)conectar();if(k==72)controlesConfiguracion=true;return;}
        if(v==null)return;
        if(pausaVisible()){teclaPausa(k);return;}
        if(k==27||k==80){panelPausa=PanelPausa.MENU;pausar();return;}
        if(k==72){abrirAyuda(false);return;}
        if(k==84){pausar();panelPausa=PanelPausa.ESTRUCTURAS;return;}
        if(k==71){comando(rolLocal,"GUARDAR",0);mensaje="Guardado solicitado al servidor.";return;}
        if(v.fase()==Fase.FINAL&&k==82){cerrarPartida();configurando=true;continuarGuardado=false;resultadoRegistrado=false;estadoGuardado="";return;}
        if(k>=49&&k<=51){int op=k-49;if(v.fase()==Fase.DECISION)comando(modo==1?0:rolLocal,"DECISION",Math.min(1,op));else if(v.fase()==Fase.TURNO){eleccion=op;comando(modo==1?0:rolLocal,"ACCION",op);}}
        if(modo==1&&k>=55&&k<=57){int op=k-55;if(v.fase()==Fase.DECISION)comando(1,"DECISION",Math.min(1,op));else if(v.fase()==Fase.TURNO)comando(1,"ACCION",op);}
        if(v.fase()==Fase.DECISION){if(k==38||k==40)eleccion=1-Math.min(1,eleccion);if(k==10||k==69)comando(modo==1&&k==10?1:rolLocal,"DECISION",Math.min(1,eleccion));}
        if(v.fase()==Fase.TURNO){if(k==38)eleccion=Math.floorMod(eleccion-1,3);if(k==40)eleccion=(eleccion+1)%3;}
        if(k==69)continuar(modo==1?0:rolLocal);if(k==10)continuar(modo==1?1:rolLocal);
    }
    public void soltar(int k){if(k==13)k=10;teclas.remove(k);teclasBloqueadas.remove(k);}
    public void limpiar(){teclas.clear();teclasBloqueadas.clear();detenerEntrada();}
    public void perderFoco(){limpiar();if(canal!=null&&!pausaVisible()){panelPausa=PanelPausa.MENU;pausar();}}
    private void reiniciar(){cerrarPartida();configurando=true;continuarGuardado=false;resultadoRegistrado=false;estadoGuardado="";}
    public void raton(int x,int y){if(botonesAudio.stream().anyMatch(r->r.contains(x,y)))alcaldedigital.cliente.audio.InterfazSonora.activar("click");
        if(conectando)return;
        if(configurando){
            if(controlesConfiguracion){if(x>=alcaldedigital.cliente.misiones.PantallaControles.VOLVER_X&&x<alcaldedigital.cliente.misiones.PantallaControles.VOLVER_X+alcaldedigital.cliente.misiones.PantallaControles.VOLVER_ANCHO&&y>=alcaldedigital.cliente.misiones.PantallaControles.VOLVER_Y&&y<alcaldedigital.cliente.misiones.PantallaControles.VOLVER_Y+alcaldedigital.cliente.misiones.PantallaControles.VOLVER_ALTO)controlesConfiguracion=false;return;}
            if(x>=170&&x<595)for(int i=0;i<4;i++)if(y>=205+i*53&&y<247+i*53)modo=i;
            if(x>=640&&x<1090&&modo!=3)for(int i=0;i<3;i++)if(y>=205+i*58&&y<247+i*58)dificultad=Dificultad.values()[i];
            if(y>=455&&y<497){if(!gestionExterna&&x>=170&&x<510)continuarGuardado=!continuarGuardado;if(x>=535&&x<1090)seleccionIP=true;}
            if(y>=550&&y<592){if(x>=170&&x<595)conectar();else if(x>=640&&x<850)controlesConfiguracion=true;else if(x>=870&&x<1090)salir=true;}
            return;
        }
        if(v==null)return;
        if(pausaVisible()){
            if(panelPausa==PanelPausa.AYUDA){
                if(y>=155&&y<197)for(int i=0;i<7;i++)if(x>=110+i*152&&x<250+i*152)paginaAyuda=i;
                if(y>=610&&y<652){if(x>=110&&x<345)paginaAyuda=Math.floorMod(paginaAyuda-1,7);else if(x>=360&&x<720)panelPausa=PanelPausa.MENU;else if(x>=735&&x<985)paginaAyuda=(paginaAyuda+1)%7;}
            }else if(panelPausa==PanelPausa.ESTRUCTURAS){if(x>=115&&x<565&&y>=605&&y<647)panelPausa=PanelPausa.MENU;}
            else if(x>=310&&x<970)for(int i=0;i<6;i++)if(y>=230+i*58&&y<272+i*58){focoPausa=i;activarPausa();break;}
            return;
        }
        if((v.fase()==Fase.INTRO||v.fase()==Fase.EPILOGO)&&x>=975&&x<1175&&y>=620&&y<662){continuar(rolLocal);return;}
        if(v.fase()==Fase.TURNO&&x>=175&&x<1105)for(int i=0;i<3;i++)if(y>=360+i*48&&y<402+i*48)comando(rolLocal,"ACCION",i);
        if(v.fase()==Fase.DECISION&&y>=570&&y<612){if(x>=105&&x<615)comando(rolLocal,"DECISION",0);if(x>=645&&x<1175)comando(rolLocal,"DECISION",1);}
        if(v.fase()==Fase.FINAL&&y>=550&&y<592){if(x>=250&&x<635)salir=true;if(x>=655&&x<1030)reiniciar();}
    }
    public void mando(EstadoMando estado){
        EstadoMando anterior=mando;mando=estado;if(!estado.conectado()&&anterior.conectado())perderFoco();
        if(estado.pulsado(EstadoMando.Boton.MENU)&&!anterior.pulsado(EstadoMando.Boton.MENU)){pulsar(' ',80);soltar(80);return;}
        if(estado.pulsado(EstadoMando.Boton.B)&&!anterior.pulsado(EstadoMando.Boton.B)){pulsar(' ',27);soltar(27);return;}
        for(var b:EstadoMando.Boton.values())if(estado.pulsado(b)&&!anterior.pulsado(b)){
            int k=switch(b){case A->10;case VISTA->72;case LB->84;case Y->82;case ARRIBA->38;case ABAJO->40;case IZQUIERDA->37;case DERECHA->39;default->0;};
            if(b==EstadoMando.Boton.A&&!configurando&&v!=null&&!pausaVisible()&&v.fase()!=Fase.DECISION)continuar(rolLocal);
            else if(k!=0){pulsar(' ',k);soltar(k);}
        }
    }
    public boolean salir(){return salir;}
    public Vista vistaResultado(){return v;}
    public EstadoCiudad ciudadResultado(){if(v==null||v.fase()!=Fase.FINAL)return ciudadInicial;try{return resultados.acreditar(v,ciudadInicial);}catch(IOException e){mensaje=e.getMessage();return ciudadInicial;}}
    public void cerrarPartida(){limpiar();if(canal!=null){canal.close();canal=null;}if(servidor!=null){servidor.close();servidor=null;}v=null;pausaPendiente=false;panelPausa=PanelPausa.MENU;focoPausa=0;ultimaFase=null;ultVidas=-1;}
    @Override public void close(){cerrada=true;if(conexion!=null){if(conexion.isDone())try{var c=conexion.get();if(c!=canal)c.close();}catch(Exception ignored){}conexion.cancel(true);}tareas.shutdownNow();cerrarPartida();sonidos.close();}
    private void previsualizar(String tipo){
        boolean coop=tipo.equals("coop");MotorBatalla m=new MotorBatalla(2026,!coop,Dificultad.valueOf(System.getProperty("ad.mision5.dificultad","NORMAL")));
        m.conectar(1,"Periodista",true);m.orden(0,0,0,false,"LISTO",0);if(coop)m.orden(1,0,0,false,"LISTO",0);
        if(tipo.equals("entrada")){canal=new CanalLocal(m,null);v=m.vista();configurando=false;return;}
        for(int i=0;i<9;i++){m.orden(0,0,0,false,"CONTINUAR",0);if(coop)m.orden(1,0,0,false,"CONTINUAR",0);}
        Vista a=m.vista();Jugador[] js=a.jugadores();
        js[0]=new Jugador("Ciudadano",500,530,0,0,5,40,99999,0,0,false,true,true,false);
        js[1]=new Jugador("Periodista",760,530,0,0,coop?5:0,0,99999,coop?1:0,0,false,coop,coop,false);
        int pat=tipo.equals("haces")?12:tipo.equals("gravedad")?9:tipo.equals("decision")||tipo.equals("final")?14:0;
        Fase fase=switch(tipo){case "turno"->Fase.TURNO;case "oficina"->Fase.INTRO;case "decision"->Fase.DECISION;case "final"->Fase.TRAICION;default->Fase.ESQUIVA;};
        Vista escena=new Vista(a.id(),a.semilla(),100,fase,pat/3,pat,0,100,4,new int[]{0,0},new int[]{-1,-1},js,List.of(),false,false,0,!coop,false,0,15,a.ciudad(),a.base(),"Vista de revisión",new String[0],a.publicaciones(),a.dificultad());
        canal=new CanalLocal(MotorBatalla.restaurar(escena),null);v=canal.vista();modo=coop?1:0;configurando=false;
        switch(System.getProperty("ad.mision5.panel","")){
            case "pausa"->{pulsar('p',80);soltar(80);}
            case "ayuda"->{pulsar('h',72);soltar(72);}
            case "controles"->abrirAyuda(true);
            case "estructuras"->{pulsar('t',84);soltar(84);}
            default->{}
        }
    }
}
