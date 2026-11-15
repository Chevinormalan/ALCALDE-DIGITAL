package alcaldedigital.cliente.mision7;

import processing.core.*;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.interfaz.TemaPixel;
import alcaldedigital.compartido.carrera.*;
import alcaldedigital.compartido.carrera.EstadoCarrera.*;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.servidor.carrera.*;
import alcaldedigital.servidor.batalla.GuardadoBatalla;
import java.net.*;import java.io.*;import java.util.*;import java.util.concurrent.*;

/** Misión completa en la misma ventana: historia, carrera, decisiones, consultas y campaña. */
public final class MisionSieteVista implements AutoCloseable {
    private static final int FONDO=0xff21152F,PANEL=0xff38264B,LILA=0xffBD91D5,ORO=0xffF4CB6A,PAPEL=0xffF5E8FB,CIAN=0xff64DDD5,ROSA=0xffE467BE;
    private PApplet p;private ArteCarrera arte;private final EstadoCiudad ciudadInicial;private final boolean reducido,contraste;
    private EstadoCiudad ciudadCampana;
    private final RepositorioCarrera repo=new RepositorioCarrera(GuardadoBatalla.directorio().resolve("campana.properties"));
    private final Map<String,PImage> marcos=new HashMap<>();private final Set<Integer> teclas=new HashSet<>(),bloqueadas=new HashSet<>();
    private final ExecutorService tareas=Executors.newSingleThreadExecutor(r->{var t=new Thread(r,"m7-conectar");t.setDaemon(true);return t;});
    private Future<CanalCarrera> conexion;private CanalCarrera canal;private ServidorCarrera servidor;private Vista v;
    private EstadoMando mando=EstadoMando.desconectado();private boolean esperarMandoNeutro;
    private long semilla;private int modo,rolElegido,rolLocal,focoPausa,panel,paginaAyuda,tabEstructura,eleccion;
    private Dificultad dificultad=Dificultad.NORMAL;
    private boolean configurando=true,conectando,continuar,asistido=true,editarIP,pausaPendiente,salir,registrado,controlesConfiguracion;
    private volatile boolean cerrada;private String ip="127.0.0.1",mensaje="",guardado="";
    private float palomoX=1350;private final Map<String,javax.sound.sampled.Clip> sonidos=new HashMap<>();private Fase faseAnterior;private int puntosAnteriores=-1;
    public MisionSieteVista(long semilla,EstadoCiudad ciudad,boolean reducido,boolean contraste){this.semilla=semilla;ciudadInicial=ciudad;ciudadCampana=ciudad;this.reducido=reducido;this.contraste=contraste;}
    private boolean gestionExterna;
    public void gestionExterna(boolean v){gestionExterna=v;}
    public boolean listaComun(){return canal!=null&&v!=null&&!configurando;}
    public void continuarDesdeSistema(boolean seguir){continuar=seguir;}
    public void preseleccionarGuardado(int modalidad,int rol){
        if(modalidad>=0&&modalidad<=2)modo=modalidad;
        if(rol>=0&&rol<=1)rolElegido=rol;
    }
    public boolean pausaComun(){return pausada();}
    public void pausarComun(){pausar(0);}
    public void reanudarComun(){reanudar();}
    public String guardarComun()throws IOException{if(canal==null)throw new IOException("No hay partida iniciada");comando(rolLocal,"GUARDAR",0);if(!canal.error().isBlank())throw new IOException(canal.error());return modo>=2?"Guardado solicitado al servidor.":"Partida guardada.";}
    public boolean dosTeclados(){return modo==1;}
    public String personajeLocal(){return rolLocal==0?"Periodista":"Influencer";}
    public boolean consultaAbiertaComun(){return panel!=0;}
    public void abrirConsultaComun(){pausar(2);}
    public void cerrarConsultaComun(){panel=0;}
    public boolean victoriaComun(){return v!=null&&v.fase()==Fase.RESULTADO&&v.ganador()<2&&v.humano(v.ganador());}
    public void iniciarEn(PApplet p){this.p=p;arte=new ArteCarrera();
        for(String s:new String[]{"ui","alert","ping","hit"})try(var in=getClass().getResourceAsStream("/cliente/mision3/audio/"+s+".wav")){if(in!=null)try(var audio=javax.sound.sampled.AudioSystem.getAudioInputStream(new BufferedInputStream(in))){var c=javax.sound.sampled.AudioSystem.getClip();c.open(audio);sonidos.put(s,c);}}catch(Exception sinAudio){}
        String preview=System.getProperty("ad.mision7.vista","");if(!preview.isBlank())previsualizar(preview);
    }
    private void sonido(String nombre){var c=sonidos.get(nombre);if(c!=null){c.stop();c.setFramePosition(0);c.start();}}
    private GuardadoCarrera archivo(int modo){return new GuardadoCarrera(GuardadoBatalla.directorio().resolve(modo>=2?"mision7-lan.bin":modo==1?"mision7-dos.bin":"mision7-solo-"+rolElegido+".bin"));}
    private void conectar(){if(conectando)return;conectando=true;mensaje="Preparando la carrera…";
        int seleccionado=modo,rol=rolElegido;boolean cargar=continuar,mantener=asistido;Dificultad nivel=dificultad;String host=ip;var archivo=archivo(modo);EstadoCiudad baseElegida=ciudadCampana;
        conexion=tareas.submit(()->{
            if(seleccionado==3){var cliente=new ClienteCarrera(host,EstadoCarrera.PUERTO,rol);if(cerrada){cliente.close();throw new IOException("Conexión cancelada");}return cliente;}
            EstadoElectoral electoral=repo.electoral(baseElegida);MotorCarrera motor;
            int humanos=seleccionado==0?1<<rol:3;
            if(cargar){if(!archivo.existe())throw new IOException("No hay guardado para este modo y personaje");motor=archivo.cargar();if(motor.vista().humanos()!=humanos)throw new IOException("El guardado pertenece a otro modo");motor.conservarCorrupcion(electoral.juanCorrupto());}
            else motor=new MotorCarrera(semilla++,humanos,nivel,baseElegida,electoral,mantener);
            if(cerrada)throw new IOException("Conexión cancelada");
            if(seleccionado==2){servidor=new ServidorCarrera(InetAddress.getByName("0.0.0.0"),EstadoCarrera.PUERTO,motor,archivo,repo);try{var cliente=new ClienteCarrera("127.0.0.1",EstadoCarrera.PUERTO,rol);if(cerrada){cliente.close();servidor.close();throw new IOException("Conexión cancelada");}return cliente;}catch(Exception e){servidor.close();servidor=null;throw e;}}
            return new CarreraLocal(motor,archivo,repo);
        });
    }
    public void actualizar(float dt){
        if(conexion!=null&&conexion.isDone()){try{canal=conexion.get();rolLocal=rolElegido;v=canal.vista();dificultad=v.dificultad();configurando=false;mensaje="";}catch(Exception e){Throwable causa=e.getCause()==null?e:e.getCause();mensaje="No se pudo abrir: "+causa.getMessage();}conexion=null;conectando=false;}
        if(canal==null)return;boolean antes=pausada();v=canal.vista();if(v==null)return;if(v.pausa())pausaPendiente=false;
        if(antes&&!pausada()){panel=0;detenerEntrada();}
        if(esperarMandoNeutro&&Math.abs(mando.izquierdaY())<.2&&!mando.pulsado(EstadoMando.Boton.X))esperarMandoNeutro=false;
        for(int r=0;r<2;r++){
            if(!v.humano(r)||modo!=1&&r!=rolLocal)continue;
            float vertical=0;boolean correr=false;
            if(!pausada()&&v.fase()==Fase.CARRERA){boolean principal=modo!=1||r==0;
                vertical=(activa(principal?83:40)?1:0)-(activa(principal?87:38)?1:0);if(modo!=1)vertical+=(activa(40)?1:0)-(activa(38)?1:0);
                correr=activa(principal?32:16)||modo!=1&&activa(32);if(r==rolLocal&&mando.conectado()&&!esperarMandoNeutro){vertical+=mando.izquierdaY();correr|=mando.pulsado(EstadoMando.Boton.X);}
            }
            canal.movimiento(r,vertical,correr);
        }
        canal.actualizar(dt);v=canal.vista();
        if(!pausada()&&v.fase()==Fase.ENCUENTRO&&v.linea()>=3)palomoX=Math.max(820,palomoX-450*Math.min(.1f,dt));
        if(v.fase()!=faseAnterior){sonido(v.fase()==Fase.CUENTA?"alert":"ui");faseAnterior=v.fase();}
        int pts=v.corredores()[rolLocal].puntos();if(puntosAnteriores>=0&&pts>puntosAnteriores)sonido("ping");puntosAnteriores=pts;
        if(v.fase()==Fase.RESULTADO&&!registrado)try{ciudadCampana=repo.acreditar(v,ciudadCampana);registrado=true;guardado="Resultado y campaña guardados. Elección definitiva pendiente de misión 8.";}catch(IOException e){guardado="Error al guardar: "+e.getMessage();}
    }
    public void dibujar(){p.pushStyle();p.noSmooth();p.textAlign(PApplet.LEFT,PApplet.BASELINE);p.background(FONDO);
        if(configurando){p.image(arte.avenida,0,0,2160,720);configuracion();if(panel==1){velo();ayuda();}if(controlesConfiguracion)alcaldedigital.cliente.misiones.PantallaControles.dibujar(p,new alcaldedigital.cliente.entrada.MapaControles.Contexto(7,modo==1,"Periodista","Influencer",mando.nombre()));p.popStyle();return;}
        if(v==null){caja(200,220,880,180);texto("Esperando al servidor…",240,310,30,ORO);p.popStyle();return;}
        if(v.fase()==Fase.CARRERA||v.fase()==Fase.CUENTA)carrera();else escena();hud();
        if(v.fase()==Fase.ESPERA)sala();
        else if(v.fase()==Fase.CUENTA){panel("PREPARADOS",440,235,400,200);texto(Integer.toString(Math.max(1,3-v.reloj()/60)),600,365,80,ORO);}
        else if(v.fase()==Fase.CARRERA)noticias();
        else if(v.fase()==Fase.RESULTADO)resultado();
        else dialogo();
        if(pausada()&&(!gestionExterna||panel!=0)){velo();switch(panel){case 1->ayuda();case 2->estructuras();case 3->eleccion();default->pausa();}}
        if(!canal.error().isBlank()){caja(30,100,1220,68);texto(canal.error(),55,128,18,ORO);texto("La sala conserva la partida. Puedes reconectar con el mismo rol.",55,151,15,PAPEL);}
        p.popStyle();
    }
    private void marco(float x,float y,int w,int h,int fondo,int borde,boolean adornos){String key=w+":"+h+":"+fondo+":"+borde+":"+adornos;PImage im=marcos.computeIfAbsent(key,k->{var b=new java.awt.image.BufferedImage(w+12,h+14,java.awt.image.BufferedImage.TYPE_INT_ARGB);var g=b.createGraphics();g.translate(3,3);TemaPixel.nitido(g);TemaPixel.marco(g,0,0,w,h,new java.awt.Color(fondo,true),new java.awt.Color(borde,true),adornos);g.dispose();return new PImage(b);});p.image(im,x-3,y-3);}
    private void caja(int x,int y,int w,int h){marco(x,y,w,h,contraste?0xff140C1D:PANEL,LILA,true);}
    private void panel(String titulo,int x,int y,int w,int h){caja(x,y,w,h);int ancho=Math.min(w-50,Math.max(200,titulo.length()*15+32));marco(x+25,y-19,ancho,38,FONDO,ORO,false);texto(titulo,x+41,y+8,22,ORO);}
    private void boton(String s,int x,int y,int w,boolean activo){boolean hover=p.mouseX>=x&&p.mouseX<x+w&&p.mouseY>=y&&p.mouseY<y+42;marco(x,y,w,42,activo?ORO:hover?0xff604077:PANEL,activo?PAPEL:hover?ORO:LILA,false);texto(s,x+14,y+28,18,activo?FONDO:PAPEL);}
    private void texto(String s,float x,float y,int tam,int color){p.fill(color);p.textSize(tam);p.text(s,x,y);}
    private void parrafo(String s,int x,int y,int w,int h,int tam,int color){p.fill(color);p.textSize(tam);p.text(s,x,y,w,h);}
    private void velo(){p.noStroke();p.fill(13,8,24,215);p.rect(0,0,1280,720);}
    private void sprite(PImage im,float x,float suelo,float alto){float ancho=alto*im.width/im.height;p.image(im,x-ancho/2,suelo-alto,ancho,alto);}
    private PImage reaccion(int rol,int emocion){return arte.reacciones[(rol==0?4:0)+emocion];}
    private void configuracion(){panel("MISIÓN 7 · LA CARRERA POR LA PRIMICIA",100,105,1080,555);
        texto("Compite por la exclusiva. Cada publicación deja consecuencias.",140,155,20,PAPEL);
        String[] modos={"Individual contra CPU","Dos personas · mismo teclado","Crear sala LAN","Unirse a sala LAN"};for(int i=0;i<4;i++)boton(modos[i],140,180+i*53,420,modo==i);
        boton("Periodista",610,180,245,rolElegido==0);boton("Influencer",870,180,270,rolElegido==1);
        for(var d:Dificultad.values())boton(d.nombre,610+d.ordinal()*180,248,170,dificultad==d);
        boton(asistido?"Correr: mantener [F]":"Correr: pulsar [F]",610,316,530,asistido);
        texto(modo==1?"Jugador 2: usa este mismo teclado · Influencer · flechas / Enter":
            modo==3?"El anfitrión mantiene dificultad y estado de la sala.":"Misma velocidad máxima para ambos modos de control.",610,389,16,CIAN);
        if(gestionExterna){marco(140,421,420,42,FONDO,LILA,false);texto(continuar?"Continuar partida guardada":"Partida nueva",156,450,18,PAPEL);}
        else boton(continuar?"Continuar guardado [R]":"Partida nueva [R]",140,421,420,continuar);
        marco(610,421,530,42,FONDO,editarIP?ORO:LILA,false);texto(modo>=2?"IP: "+ip+(editarIP?"_":"")+" [Tab]":"C: cambiar personaje · F: modo de carrera",625,450,18,PAPEL);
        if(continuar)texto("El guardado conserva su dificultad y modos de control.",140,482,17,CIAN);
        texto("Ciudadano y Palomo conservan su historia. Juan solo gana si sigue honesto y reúne apoyo.",140,504,18,LILA);
        boton(conectando?"Conectando…":"Comenzar [Enter / A]",140,543,420,true);boton("Ayuda [H]",610,543,245,false);boton("Volver a misiones",870,543,270,false);
        parrafo(mensaje,140,607,1000,38,17,CIAN);
    }
    private void hud(){marco(12,8,425,90,FONDO,LILA,true);texto("MISIÓN 7 · LA PRIMICIA",28,36,24,ORO);texto(dificultad.nombre+" · "+(modo>=2?"SALA LAN":"CIUDAD NOVA"),28,64,16,LILA);texto("Elección definitiva: pendiente",28,86,15,PAPEL);
        for(int r=0;r<2;r++){var c=v.corredores()[r];int x=455+r*410;marco(x,8,395,90,FONDO,LILA,true);texto(MotorCarrera.ROLES[r]+(v.humano(r)?"":" · CPU"),x+15,31,19,r==0?ORO:CIAN);texto(c.puntos()+" pts · reputación "+c.reputacion(),x+15,54,17,PAPEL);
            marco(x+15,68,365,13,FONDO,LILA,false);p.noStroke();p.fill(r==0?ORO:CIAN);p.rect(x+18,71,359*Math.min(1,c.distancia()/MotorCarrera.META),7);}
        marco(12,685,1255,29,FONDO,LILA,false);texto("J1 Espacio / J2 Mayús: correr · W/S / flechas: carril · Q / K: ayuda · 1/2/3 y 7/8/9: noticia · Esc/P: pausa",27,706,15,LILA);
    }
    private void escena(){
        switch(v.fase()){
            case ANUNCIO->{p.image(arte.avenida,0,0,2160,720);panel("EDICIÓN EXTRA · EN VIVO",360,150,560,245);p.tint(FONDO);sprite(arte.candidato,640,390,210);p.noTint();for(int y=175;y<380;y+=14){p.stroke(LILA,35);p.line(390,y,890,y);}p.noStroke();for(int i=0;i<4;i++){int x=130+i*260;caja(x,430,245,80);texto(EstadoElectoral.NOMBRES[i],x+15,458,21,ORO);texto(v.electoral().elegible(i)?"Apoyo provisional: "+v.electoral().respaldo(i):"Fuera: pacto corrupto",x+15,490,16,PAPEL);}}
            case RIVALIDAD->{p.image(arte.oficina,0,90,640,440);p.image(arte.mall,640,90,640,440);p.stroke(ORO);p.strokeWeight(4);p.line(640,100,640,530);p.noStroke();sprite(reaccion(0,0),320,520,235);sprite(reaccion(1,0),960,520,235);}
            case SALIDA->{if(v.linea()<2){p.image(arte.oficina,0,0,1280,720);sprite(arte.sofi[0],515,530,94);float t=Math.min(1,v.reloj()/150f);sprite(arte.andar[0][4+(reducido?0:(int)(v.tick()/7)%4)],790-150*t,445-260*t,94);}else{p.image(arte.mall,0,0,1280,720);sprite(arte.andar[1][12+(reducido?0:(int)(v.tick()/7)%4)],640+Math.min(330,v.reloj()*1.5f),480,100);}}
            case LLEGADA,EPILOGO,RESULTADO->{p.image(arte.parque,0,0,1280,720);caja(390,117,500,150);texto("RESULTADO PROVISIONAL",425,155,26,ORO);parrafo("COMPARTE PARA CONFIRMAR AL PRÓXIMO ALCALDE",425,180,440,70,23,ROSA);for(int i=0;i<7;i++){int frame=reducido?0:(int)(v.tick()/12+i)%4;sprite(arte.ciudadanos[(i%2*3+1)*4+frame],270+i*115,380+(i%2)*25,75);}sprite(reaccion(0,v.fase()==Fase.LLEGADA&&v.linea()==0&&v.ganador()==0?1:3),520,530,145);sprite(reaccion(1,v.fase()==Fase.LLEGADA&&v.linea()==0&&v.ganador()==1?1:3),745,530,145);}
            default->{p.image(arte.avenida,0,0,2160,720);float t=Math.min(1,v.reloj()/60f);sprite(arte.andar[0][12+(reducido?0:(int)(v.tick()/7)%4)],350+190*t,420,100);sprite(arte.andar[1][8+(reducido?0:(int)(v.tick()/7)%4)],800-175*t,440,100);
                if(v.fase()==Fase.ENCUENTRO&&v.linea()>=3){boolean vuela=palomoX>825;sprite(vuela?arte.vuelo[reducido?0:(int)(v.tick()/8)%4]:arte.palomo,palomoX,350,90);}}
        }
    }
    private void dialogo(){var lineas=HistoriaCarrera.dialogo(v.fase(),v.ganador());if(lineas.isEmpty())return;var l=lineas.get(Math.min(lineas.size()-1,v.linea()));caja(65,548,1150,125);marco(90,530,Math.max(195,l.quien().length()*15+32),36,FONDO,ORO,false);texto(l.quien(),105,555,20,ORO);
        PImage retrato=switch(l.quien()){case "PERIODISTA"->reaccion(0,v.fase()==Fase.EPILOGO?3:0);case "INFLUENCER"->reaccion(1,v.fase()==Fase.EPILOGO?3:1);case "MR. PALOMO"->arte.palomo;case "CIUDADANO"->arte.sofi[0];case "VOZ MISTERIOSA"->arte.candidato;default->arte.telefono;};
        marco(88,579,70,75,FONDO,LILA,false);
        if(l.quien().equals("AMBOS")){sprite(reaccion(0,0),107,650,60);sprite(reaccion(1,0),140,650,60);}
        else{if(l.quien().equals("VOZ MISTERIOSA"))p.tint(PANEL);sprite(retrato,123,650,65);p.noTint();}
        parrafo(l.texto(),180,568,990,63,20,PAPEL);
        texto(v.humanos()==3?"E: periodista · Enter: influencer · ambos confirman":"E / Enter / A: continuar",180,653,15,CIAN);boton("Continuar",1010,625,180,true);
    }
    private void sala(){panel("CARRERA POR LA PRIMICIA",210,170,860,390);texto("Cada personaje confirma que está listo.",250,240,24,PAPEL);for(int r=0;r<2;r++){var c=v.corredores()[r];texto(MotorCarrera.ROLES[r]+": "+(!v.humano(r)?"CPU":!c.conectado()?"esperando conexión":c.listo()?"LISTO":"por confirmar"),250,310+r*50,24,r==0?ORO:CIAN);}
        texto(modo>=2?"TCP 48507 · "+ips():"Individual: E/Enter · dos personas: E y Enter",250,445,18,LILA);boton("Estoy listo [E / A]",250,485,780,true);}
    private String ips(){try{var a=new ArrayList<String>();for(var n:Collections.list(NetworkInterface.getNetworkInterfaces()))for(var ip:Collections.list(n.getInetAddresses()))if(ip instanceof Inet4Address&&!ip.isLoopbackAddress())a.add(ip.getHostAddress());return a.isEmpty()?"127.0.0.1":String.join(" / ",a);}catch(SocketException e){return "IP del anfitrión";}}
    private float suelo(float y){return 245+(y-MotorCarrera.Y_MIN)*.68f;}
    private void carrera(){
        var c0=v.corredores()[0];var c1=v.corredores()[1];float menor=Math.min(c0.distancia(),c1.distancia()),mayor=Math.max(c0.distancia(),c1.distancia());float escala=Math.min(1,1100/(mayor-menor+400));float cam=Math.max(0,menor-160);
        p.clip(0,103,1280,436);int desde=(int)(cam*escala/1800);for(int t=desde;t<desde+3;t++){float x=70+t*1800-cam*escala;if(x>1280)break;p.image(arte.avenida,x,0,1800,600);}
        if(mayor>MotorCarrera.META-1500){float x=70+(MotorCarrera.META-cam)*escala;p.image(arte.parque,x,100,1000,600);marco(x-60,170,190,42,FONDO,ORO,false);texto("PARQUE · META",x-45,198,19,ORO);}
        for(int k=0;k<4;k++){float x=70+(400+k*2400-cam)*escala;if(x>-300&&x<1280){marco(x,125,235,34,FONDO,LILA,false);texto(new String[]{"NOVA PRENSA","AVENIDA CIVITAS","PASAJE ELECTORAL","CAMINO AL PARQUE"}[k],x+12,149,16,PAPEL);}}
        for(Objeto a:objetosVisibles()){float x=70+(a.x()-cam)*escala;if(x< -80||x>1360)continue;float y=suelo(a.y());int frame=reducido?0:(int)(v.tick()/9+a.id())%4;
            if(a.tipo()==1){int estado=v.grupos()[a.grupo()];PImage im=arte.ciudadanos[(a.id()%2*3+(estado==2?2:estado==1?1:0))*4+frame];sprite(im,x,y,72);if(estado==3)texto("OK",x-10,y-74,14,CIAN);}
            else sprite(arte.objetos[(a.tipo()==2?3:a.tipo()==3?0:a.tipo()==4?1:2)*4+frame],x,y,a.tipo()==2?62:44);
        }
        for(int i=0;i<7;i++){float x=70+(900+i*1350-cam)*escala;if(x> -60&&x<1340)sprite((i%2==0?arte.antiguosA:arte.antiguosB)[(i%4)*4+2],x,218,64);}
        for(int r=0;r<2;r++){var c=v.corredores()[r];float x=70+(c.distancia()-cam)*escala,y=suelo(c.y());int frame=reducido?0:(int)(v.tick()/6)%4;
            p.noStroke();p.fill(FONDO,90);p.ellipse(x,y+2,55,12);sprite(arte.andar[r][12+frame],x,y,90);
            texto(r==0?"P":"I",x-6,y-96,17,r==0?ORO:CIAN);
            if(c.escudo()>0){p.noFill();p.stroke(CIAN);p.strokeWeight(3);p.ellipse(x,y-45,75,105);p.noStroke();}
            if(c.frenado()>0)texto("LENTO",x-25,y+22,14,ROSA);
            if(c.avisoAtaque()>0){float yy=suelo(c.objetivoAtaque());p.stroke(ROSA);p.strokeWeight(3);p.line(x-80,yy,x+80,yy);p.noStroke();texto("¡CAMBIA DE CARRIL!",x-85,yy-108,15,ORO);}
        }
        p.noClip();
        int tramo=mayor<3200?1:mayor<6400?2:3;marco(25,482,1230,49,FONDO,LILA,true);texto(new String[]{"SALIDA · consigue ventaja y observa la avenida","RIVALIDAD · las publicaciones empiezan a cambiar el recorrido","APROXIMACIÓN · algo está pasando con los ciudadanos"}[tramo-1],45,512,19,ORO);
        if(v.fase()==Fase.CARRERA&&v.corredores()[rolLocal].evento()==null){caja(25,549,1230,120);parrafo(v.mensaje(),50,570,1160,42,20,PAPEL);var c=v.corredores()[rolLocal];texto((c.asistido()?"Mantén":"Pulsa")+" "+(modo==1&&rolLocal==1?"Mayús":"Espacio / X")+" para correr · ayuda: "+nombreAyuda(c.habilidad()),50,640,19,CIAN);}
    }
    private List<Objeto> objetosVisibles(){return v.objetos().stream().filter(a->a.consumido()!=3).toList();}
    private static String nombreAyuda(int a){return a==4?"escudo":a==5?"notificaciones":"ninguna (busca los iconos)";}
    private void noticias(){
        if(modo==1){for(int r=0;r<2;r++)if(v.corredores()[r].evento()!=null)tarjetaNoticia(r,25+r*630,610);}
        else if(v.corredores()[rolLocal].evento()!=null)tarjetaNoticia(rolLocal,25,1230);
    }
    private void tarjetaNoticia(int rol,int x,int w){Evento e=v.corredores()[rol].evento();caja(x,549,w,120);texto(MotorCarrera.ROLES[rol]+" · "+HistoriaCarrera.titulo(e.tipo()),x+15,570,w<700?16:20,e.advertencia()?ROSA:ORO);texto(Math.max(0,(int)Math.ceil(e.restante()/60f))+" s",x+w-57,570,17,LILA);
        parrafo(HistoriaCarrera.cuerpo(e),x+15,579,w-30,43,w<700?14:17,PAPEL);String[] opciones=HistoriaCarrera.opciones(e);int bw=(w-40)/3;
        for(int i=0;i<3;i++){int base=modo==1&&rol==1?7:1;marco(x+10+i*(bw+8),625,bw,34,eleccion==i?0xff604077:FONDO,e.advertencia()&&i==1?ROSA:LILA,false);String s=(base+i)+" "+opciones[i];texto(s,x+20+i*(bw+8),647,w<700?12:15,i==1&&e.advertencia()?ROSA:CIAN);}
    }
    private void resultado(){velo();panel("LA PRIMICIA Y SUS CONSECUENCIAS",135,160,1010,490);texto(v.ganador()==2?"Empate: primicia compartida":MotorCarrera.ROLES[v.ganador()]+" llegó primero",175,222,29,ORO);
        for(int r=0;r<2;r++){var c=v.corredores()[r];texto(MotorCarrera.ROLES[r]+(v.humano(r)?"":" (CPU)")+": "+c.puntos()+" puntos · reputación "+c.reputacion(),175,276+r*40,23,r==0?ORO:CIAN);}
        texto("Ciudad: confianza "+v.ciudad()[1]+" · convivencia "+v.ciudad()[2]+" · desinformación "+v.ciudad()[5],175,370,21,PAPEL);
        texto(v.electoral().juanCorrupto()?"Juan quedó corrupto: no puede ganar la elección.":"Juan sigue elegible; necesita suficiente respaldo para ganar.",175,415,22,v.electoral().juanCorrupto()?ROSA:CIAN);
        texto("Líder provisional: "+EstadoElectoral.NOMBRES[v.electoral().liderProvisional()]+". Elección definitiva en misión 8.",175,458,21,PAPEL);parrafo(guardado,175,495,930,42,17,LILA);
        boton("Volver a misiones [Enter / A]",175,565,440,true);boton("Nueva carrera [R]",640,565,465,false);
    }
    private void pausa(){panel(v.humanos()==3?"PAUSA COMPARTIDA":"PAUSA",265,130,750,530);String[] o={"Continuar [Esc / P]","Ayuda de la misión [H]","Red y estructuras [T]","Campaña electoral [V]","Correr: "+(v.corredores()[rolLocal].asistido()?"mantener":"pulsar"),"Guardar ahora [G]","Guardar y salir a misiones"};
        for(int i=0;i<o.length;i++)boton(o[i],305,205+i*53,670,focoPausa==i);texto("El reloj, los rivales y las noticias están detenidos.",305,615,18,PAPEL);texto("Arriba/abajo: elegir · Enter / A: confirmar",305,642,16,LILA);
    }
    private void ayuda(){panel("AYUDA · LA CARRERA POR LA PRIMICIA",80,120,1120,540);for(int i=0;i<3;i++)boton(new String[]{"Carrera y controles","Noticias y ayudas","Elección y consecuencias"}[i],115+i*350,160,335,paginaAyuda==i);
        String[] t=switch(paginaAyuda){
            case 0->new String[]{"Objetivo: llegar primero al parque. La reputación también depende de cómo compites.","Individual / LAN: Espacio o X corre; W/S o flechas cambia de carril; Q usa la ayuda.","Dos en teclado: periodista W/S, Espacio y Q; influencer flechas, Mayús y K.","Mantener corre con un ritmo limitado. Pulsar acepta una pulsación cada 10 pasos.","Ambas opciones tienen la misma velocidad máxima. Cambia el modo desde la pausa.","Mando: stick cambia de carril; X corre; A decide; LB/RB eligen; Y usa ayuda.","La CPU tiene los mismos límites y colisiones. Las vallas siempre dejan espacio para esquivar.","Esc/P o Menú pausa. B vuelve de una consulta a pausa. Soltar teclas evita arrastres al volver."};
            case 1->new String[]{"Batería: aceleración breve. Escudo: bloquea una distracción. Notificaciones: molestan al rival.","Una distracción marca tu carril durante un segundo: muévete antes de que se active.","Las noticias tienen ocho segundos. Puedes contrastar, aprovecharlas o seguir corriendo.","1/2/3 responde a tu noticia; en dos teclados 7/8/9 responde por el influencer.","Contrastar aporta reputación e información. Compartir sin comprobar puede dar ventaja y conflictos.","Un pacto pide confirmación adicional. Leerlo no corrompe a Juan; ejecutarlo sí.","En el mando, A confirma la opción elegida con LB/RB. Y usa una ayuda recogida.","La periodista contrasta más rápido. El influencer llega a más grupos, para aclarar o difundir rumores."};
            default->new String[]{"El anuncio presenta apoyos provisionales. Ningún botón permite elegir directamente al ganador.","Juan solo puede ganar si no se corrompe y reúne suficiente respaldo ciudadano.","Aceptar y confirmar el pacto oculto registra corrupción irreversible en la campaña de Juan.","Rechazar el trato conserva su condición anterior. Mejorar puntos no borra un pacto ya ejecutado.","Apoyo, confianza, información verificada, participación y conflictos determinan el liderazgo.","Ganar la carrera da la primicia. Las decisiones dan reputación y cambian la ciudad.","La elección válida queda pendiente de misión 8. El manipulador del proceso será el conflicto.","El guardado conserva carrera, noticias, decisiones, apoyos, reputación y corrupción de Juan."};};
        for(int i=0;i<t.length;i++)texto(t[i],115,249+i*40,18,i%2==0?PAPEL:CIAN);boton("Anterior",115,605,235,false);boton(configurando?"Volver a configuración [Esc]":"Volver a pausa [Esc]",365,605,460,true);boton(configurando?"Ver controles [C]":"Siguiente",840,605,325,false);
    }
    private void estructuras(){panel("RED, AVL Y DECISIONES",80,120,1120,540);for(int i=0;i<3;i++)boton(new String[]{"Grafo de Civitas","Publicaciones (AVL)","Decisiones"}[i],115+i*350,160,335,tabEstructura==i);
        if(tabEstructura==0){texto("Periodista: origen 2 · Influencer: origen 0 · propagación por BFS",115,245,21,PAPEL);int[][] aristas={{1,2},{3},{4},{5},{6},{},{}};for(int i=0;i<7;i++){int x=170+i*150,y=315+(i%2)*50;for(int j:aristas[i]){p.stroke(LILA);p.strokeWeight(2);p.line(x,y,170+j*150,315+(j%2)*50);}p.noStroke();}
            for(int i=0;i<7;i++){int x=170+i*150,y=315+(i%2)*50;marco(x-25,y-24,50,48,FONDO,v.grupos()[i]==2?ROSA:v.grupos()[i]==3?CIAN:LILA,true);texto(Integer.toString(i),x-6,y+7,22,PAPEL);texto(new String[]{"normal","celular","alterado","aclarado"}[v.grupos()[i]],x-40,y+52,15,CIAN);}
            texto("Envíos pendientes: "+v.ondas().size()+". Cada salto llega después: la pausa también detiene la difusión.",115,470,19,PAPEL);parrafo("Los grupos alterados pueden bloquear la avenida; una aclaración reduce la aglomeración. Las rutas de la red cambian el juego.",115,510,1050,66,20,LILA);
        }else if(tabEstructura==1){texto("Índice AVL por riesgo · verificar elimina la publicación resuelta",115,245,22,ORO);for(int i=0;i<Math.min(8,v.publicaciones().size());i++){var pub=v.publicaciones().get(i);texto("#"+pub.id()+" · riesgo "+pub.riesgo()+" · "+pub.texto()+" · "+pub.estado(),115,295+i*36,17,PAPEL);}if(v.publicaciones().isEmpty())texto("No quedan publicaciones pendientes de esta carrera.",115,315,21,CIAN);
        }else{texto("Cada noticia tiene tres ramas. La ruta ejecutada queda en el guardado.",115,245,21,ORO);int inicio=Math.max(0,v.historial().size()-7);for(int i=inicio;i<v.historial().size();i++)parrafo(v.historial().get(i),115,280+(i-inicio)*42,1050,37,17,PAPEL);}
        boton("Volver a pausa [Esc / T]",115,605,520,true);
    }
    private void eleccion(){panel("CAMPAÑA ELECTORAL · PROVISIONAL",80,120,1120,540);var e=v.electoral();int[] apo=e.apoyo(),conf=e.confianza(),ver=e.verificadas(),con=e.conflictos(),part=e.participacion();
        for(int i=0;i<4;i++){int x=115+i*270;marco(x,185,250,295,FONDO,e.elegible(i)?LILA:ROSA,true);texto(EstadoElectoral.NOMBRES[i],x+18,225,27,ORO);String[] valores={"Apoyo: "+apo[i],"Confianza: "+conf[i],"Verificadas: "+ver[i],"Conflictos: "+con[i],"Participación: "+part[i],"Respaldo: "+e.respaldo(i)};for(int j=0;j<valores.length;j++)texto(valores[j],x+18,267+j*29,18,PAPEL);texto(e.elegible(i)?"ELEGIBLE":"JUAN CORRUPTO",x+18,466,16,e.elegible(i)?CIAN:ROSA);}
        texto("Líder provisional: "+EstadoElectoral.NOMBRES[e.liderProvisional()]+". No es una proclamación oficial.",115,533,22,ORO);texto("Las decisiones de campaña cambian el apoyo. La elección definitiva se resolverá en misión 8.",115,574,19,PAPEL);boton("Volver a pausa [Esc / V]",115,605,520,true);
    }
    private boolean pausada(){return pausaPendiente||v!=null&&v.pausa();}
    private boolean activa(int k){return teclas.contains(k)&&!bloqueadas.contains(k);}
    private void detenerEntrada(){bloqueadas.addAll(teclas);esperarMandoNeutro=true;if(canal!=null)for(int r=0;r<2;r++)canal.movimiento(r,0,false);}
    private void comando(int rol,String accion,int valor){if(canal==null)return;canal.accion(rol,accion,valor);v=canal.vista();sonido("ui");}
    private void pausar(int pantalla){detenerEntrada();panel=pantalla;if(!pausada()){focoPausa=0;pausaPendiente=true;comando(rolLocal,"PAUSA",0);}}
    private void reanudar(){detenerEntrada();panel=0;pausaPendiente=false;comando(rolLocal,"REANUDAR",0);}
    private void avanzar(int rol){if(v==null||pausada())return;if(v.fase()==Fase.RESULTADO)salir=true;else comando(rol,v.fase()==Fase.ESPERA?"LISTO":"CONTINUAR",0);}
    private void responder(int rol,int accion){if(v==null)return;Evento e=v.corredores()[rol].evento();if(e!=null&&e.advertencia()&&accion==1)accion=3;comando(rol,"ACCION",accion);}
    private void usarAyuda(int rol){comando(rol,"USAR",0);}
    private void activarPausa(){switch(focoPausa){case 0->reanudar();case 1->panel=1;case 2->panel=2;case 3->panel=3;case 4->comando(rolLocal,"ASISTENCIA",v.corredores()[rolLocal].asistido()?0:1);case 5->{comando(rolLocal,"GUARDAR",0);mensaje="Guardado solicitado.";}case 6->{comando(rolLocal,"GUARDAR",0);salir=true;}default->{}}}
    private void elegirModo(int nuevo){if(nuevo==3&&modo!=3)rolElegido=1;modo=nuevo;}
    public void pulsar(char c,int k){if(k==13)k=10;if(!teclas.add(k)||conectando)return;
        if(configurando){if(controlesConfiguracion){if(k==27||k==10||k==67)controlesConfiguracion=false;return;}if(panel==1){if(k==67)controlesConfiguracion=true;else if(k==27||k==72||k==10)panel=0;else if(k==37)paginaAyuda=Math.floorMod(paginaAyuda-1,3);else if(k==39)paginaAyuda=(paginaAyuda+1)%3;return;}
            if(editarIP){if(k==27||k==10||k==9){editarIP=false;return;}if(k==65&&teclas.contains(17)){ip="";return;}if(k==8&&!ip.isEmpty())ip=ip.substring(0,ip.length()-1);else if(c>=32&&c<127&&ip.length()<80)ip+=c;return;}
            if(k==27)salir=true;if(k==38)elegirModo(Math.floorMod(modo-1,4));if(k==40)elegirModo((modo+1)%4);if(k==37&&modo!=3)dificultad=Dificultad.values()[Math.floorMod(dificultad.ordinal()-1,3)];if(k==39&&modo!=3)dificultad=Dificultad.values()[(dificultad.ordinal()+1)%3];if(k==67)rolElegido=1-rolElegido;if(k==70)asistido=!asistido;if(k==82&&!gestionExterna)continuar=!continuar;if(k==9&&modo>=2)editarIP=true;if(k==72)panel=1;if(k==10||k==69)conectar();return;
        }
        if(v==null)return;
        if(pausada()){
            if(k==80){reanudar();return;}if(k==27){if(panel==0)reanudar();else panel=0;return;}
            if(k==72){panel=panel==1?0:1;return;}if(k==84){panel=panel==2?0:2;return;}if(k==86){panel=panel==3?0:3;return;}if(k==71){comando(rolLocal,"GUARDAR",0);return;}
            if(panel!=0){if(k==10||k==69){panel=0;return;}if(k==37){if(panel==1)paginaAyuda=Math.floorMod(paginaAyuda-1,3);if(panel==2)tabEstructura=Math.floorMod(tabEstructura-1,3);}if(k==39){if(panel==1)paginaAyuda=(paginaAyuda+1)%3;if(panel==2)tabEstructura=(tabEstructura+1)%3;}return;}
            if(k==38||k==87)focoPausa=Math.floorMod(focoPausa-1,7);if(k==40||k==83||k==9)focoPausa=(focoPausa+1)%7;if(k==10||k==69||k==32)activarPausa();return;
        }
        if(k==27||k==80){pausar(0);return;}if(k==72){pausar(1);return;}if(k==84){pausar(2);return;}if(k==86){pausar(3);return;}if(k==71){comando(rolLocal,"GUARDAR",0);return;}
        if(v.fase()==Fase.RESULTADO&&k==82){reiniciar();return;}
        if(v.fase()==Fase.CARRERA){
            if(k>=49&&k<=51){eleccion=k-49;responder(modo==1?0:rolLocal,eleccion);}
            if(modo==1&&k>=55&&k<=57)responder(1,k-55);
            if(k==32)comando(modo==1?0:rolLocal,"PULSO",0);if(k==16&&modo==1)comando(1,"PULSO",0);
            if(k==69||k==81)usarAyuda(modo==1?0:rolLocal);if(k==75&&modo==1)usarAyuda(1);return;
        }
        if(k==69||k==32)avanzar(modo==1?0:rolLocal);if(k==10)avanzar(modo==1?1:rolLocal);
    }
    public void soltar(int k){if(k==13)k=10;teclas.remove(k);bloqueadas.remove(k);}
    private void reiniciar(){cerrarPartida();configurando=true;continuar=false;registrado=false;guardado="";mensaje="";}
    public void raton(int x,int y){if(conectando)return;
        if(configurando){if(controlesConfiguracion){if(x>=alcaldedigital.cliente.misiones.PantallaControles.VOLVER_X&&x<alcaldedigital.cliente.misiones.PantallaControles.VOLVER_X+alcaldedigital.cliente.misiones.PantallaControles.VOLVER_ANCHO&&y>=alcaldedigital.cliente.misiones.PantallaControles.VOLVER_Y&&y<alcaldedigital.cliente.misiones.PantallaControles.VOLVER_Y+alcaldedigital.cliente.misiones.PantallaControles.VOLVER_ALTO)controlesConfiguracion=false;return;}if(panel==1){ratonAyuda(x,y);return;}if(x>=140&&x<560)for(int i=0;i<4;i++)if(y>=180+i*53&&y<222+i*53)elegirModo(i);
            if(y>=180&&y<222){if(x>=610&&x<855)rolElegido=0;if(x>=870&&x<1140)rolElegido=1;}
            if(y>=248&&y<290&&modo!=3)for(int i=0;i<3;i++)if(x>=610+i*180&&x<780+i*180)dificultad=Dificultad.values()[i];
            if(x>=610&&x<1140&&y>=316&&y<358)asistido=!asistido;
            if(y>=421&&y<463){if(!gestionExterna&&x>=140&&x<560)continuar=!continuar;if(modo>=2&&x>=610&&x<1140)editarIP=true;}
            if(y>=543&&y<585){if(x>=140&&x<560)conectar();if(x>=610&&x<855)panel=1;if(x>=870&&x<1140)salir=true;}return;
        }
        if(v==null)return;if(pausada()){
            if(panel==1)ratonAyuda(x,y);
            else if(panel==2){if(y>=160&&y<202)for(int i=0;i<3;i++)if(x>=115+i*350&&x<450+i*350)tabEstructura=i;if(x>=115&&x<635&&y>=605&&y<647)panel=0;}
            else if(panel==3){if(x>=115&&x<635&&y>=605&&y<647)panel=0;}
            else if(x>=305&&x<975)for(int i=0;i<7;i++)if(y>=205+i*53&&y<247+i*53){focoPausa=i;activarPausa();break;}return;
        }
        if(v.fase()==Fase.CARRERA){if(y>=625&&y<659){int rol=modo==1?(x>=655?1:0):rolLocal;int origen=modo==1?25+rol*630:25,w=modo==1?610:1230,bw=(w-40)/3;for(int i=0;i<3;i++)if(x>=origen+10+i*(bw+8)&&x<origen+10+i*(bw+8)+bw){eleccion=i;responder(rol,i);}}return;}
        if(v.fase()==Fase.RESULTADO&&y>=565&&y<607){if(x>=175&&x<615)salir=true;if(x>=640&&x<1105)reiniciar();return;}
        if(v.fase()==Fase.ESPERA&&x>=250&&x<1030&&y>=485&&y<527)avanzar(rolLocal);
        if(x>=1010&&x<1190&&y>=625&&y<667)avanzar(rolLocal);
    }
    private void ratonAyuda(int x,int y){if(y>=160&&y<202)for(int i=0;i<3;i++)if(x>=115+i*350&&x<450+i*350)paginaAyuda=i;if(y>=605&&y<647){if(x>=115&&x<350)paginaAyuda=Math.floorMod(paginaAyuda-1,3);if(x>=365&&x<825)panel=0;if(x>=840&&x<1165){if(configurando)controlesConfiguracion=true;else paginaAyuda=(paginaAyuda+1)%3;}}}
    public void mando(EstadoMando actual){var anterior=mando;mando=actual;if(!actual.conectado()&&anterior.conectado())perderFoco();
        for(var b:EstadoMando.Boton.values())if(actual.pulsado(b)&&!anterior.pulsado(b)){
            if(!configurando&&!pausada()&&v!=null&&v.fase()==Fase.CARRERA){switch(b){case X->comando(rolLocal,"PULSO",0);case LB->eleccion=Math.floorMod(eleccion-1,3);case RB->eleccion=(eleccion+1)%3;case A->responder(rolLocal,eleccion);case Y->usarAyuda(rolLocal);default->{}}
                if(b!=EstadoMando.Boton.MENU&&b!=EstadoMando.Boton.B&&b!=EstadoMando.Boton.VISTA)continue;
            }
            if(b==EstadoMando.Boton.A&&!configurando&&!pausada()&&v!=null){avanzar(rolLocal);continue;}
            int k=switch(b){case A->10;case B->27;case MENU->80;case VISTA->72;case ARRIBA->38;case ABAJO->40;case IZQUIERDA->37;case DERECHA->39;case Y->70;case LB->67;default->0;};if(k!=0){pulsar(' ',k);soltar(k);}
        }
    }
    public void perderFoco(){teclas.clear();bloqueadas.clear();detenerEntrada();if(canal!=null&&!pausada())pausar(0);}
    public Vista vistaResultado(){return v;}public boolean salir(){return salir;}
    public EstadoCiudad ciudadResultado(){if(v==null||v.fase()!=Fase.RESULTADO)return ciudadCampana;try{ciudadCampana=repo.acreditar(v,ciudadCampana);return ciudadCampana;}catch(IOException e){mensaje=e.getMessage();return ciudadCampana;}}
    public void cerrarPartida(){detenerEntrada();teclas.clear();bloqueadas.clear();if(canal!=null){canal.close();canal=null;}if(servidor!=null){servidor.close();servidor=null;}v=null;panel=0;focoPausa=0;pausaPendiente=false;faseAnterior=null;puntosAnteriores=-1;palomoX=1350;}
    @Override public void close(){cerrada=true;if(conexion!=null){if(conexion.isDone())try{var c=conexion.get();if(c!=canal)c.close();}catch(Exception ignored){}conexion.cancel(true);}tareas.shutdownNow();cerrarPartida();for(var c:sonidos.values())c.close();}
    private void previsualizar(String tipo){boolean dos=tipo.equals("dos");MotorCarrera m=new MotorCarrera(2026,dos?3:1,Dificultad.NORMAL,ciudadInicial,EstadoElectoral.inicial(ciudadInicial),true);
        for(int r=0;r<2;r++){m.conectar(r,true);m.orden(r,0,false,"LISTO",0);}
        Fase destino=switch(tipo){case "anuncio"->Fase.ANUNCIO;case "rivalidad"->Fase.RIVALIDAD;case "salida"->Fase.SALIDA;case "encuentro"->Fase.ENCUENTRO;default->Fase.CUENTA;};
        for(int i=0;i<40&&m.vista().fase()!=destino;i++)for(int r=0;r<2;r++)m.orden(r,0,false,"CONTINUAR",0);
        if(destino==Fase.ENCUENTRO)for(int i=0;i<4;i++)m.orden(0,0,false,"CONTINUAR",0);
        if(destino==Fase.CUENTA){for(int i=0;i<181;i++)m.paso();int duracion=switch(tipo){case "avenida"->190;case "noticia","dos"->600;case "ciudadanos"->2000;case "pacto","llegada","resultado"->18000;default->1;};
            for(int i=0;i<duracion&&m.vista().fase()==Fase.CARRERA;i++){
                boolean mostrarPacto=false;
                for(int r=0;r<2;r++)if(m.humano(r)){Evento e=m.vista().corredores()[r].evento();if(e!=null){if(tipo.equals("pacto")&&e.tipo()==Noticia.PACTO){m.orden(r,0,true,"ACCION",1);mostrarPacto=true;break;}
                    if(!tipo.equals("noticia")&&!dos)m.orden(r,0,true,"ACCION",tipo.equals("ciudadanos")?1:0);}
                    m.orden(r,0,true,"",0);
                }
                if(mostrarPacto)break;m.paso();
            }
            if(tipo.equals("resultado"))for(int i=0;i<30&&m.vista().fase()!=Fase.RESULTADO;i++)m.orden(0,0,false,"CONTINUAR",0);
        }
        canal=new CarreraLocal(m,null,null);v=m.vista();configurando=false;modo=dos?1:0;
        switch(tipo){case "ayuda"->{pausar(1);paginaAyuda=1;}case "eleccion"->pausar(3);case "estructuras"->pausar(2);case "pausa"->pausar(0);default->{}}
    }
}
