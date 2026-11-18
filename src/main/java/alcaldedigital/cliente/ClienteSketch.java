package alcaldedigital.cliente;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PFont;
import alcaldedigital.cliente.graficos.*;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.cliente.interfaz.TransicionPixel;
import alcaldedigital.cliente.mision3.MisionTresSketch;
import alcaldedigital.cliente.mision2.MisionDosSketch;
import alcaldedigital.cliente.entrada.ControladorMando;
import alcaldedigital.cliente.entrada.LectorJInput;
import alcaldedigital.compartido.modelo.Rol;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

/**
 * Ventana Processing. Entrada y presentación del capítulo jugable de la entrega 1.
 */
public final class ClienteSketch extends PApplet {
    public static final int ANCHO = 1280;
    public static final int ALTO = 720;
    private final JuegoCliente juego;
    private final alcaldedigital.cliente.audio.MotorAudio audio=alcaldedigital.cliente.audio.MotorAudio.global();
    private final alcaldedigital.cliente.audio.DirectorAudio directorAudio=new alcaldedigital.cliente.audio.DirectorAudio(audio);
    public ClienteSketch(JuegoCliente juego) { this.juego=juego; }
    private final alcaldedigital.cliente.misiones.PausaUnificada pausaSistema=new alcaldedigital.cliente.misiones.PausaUnificada();
    private final RenderJuego render = new RenderJuego();
    private alcaldedigital.cliente.misiones.VistaCampana misionCampana;
    private MisionTresSketch misionTres;
    private MisionDosSketch misionDos;
    private alcaldedigital.cliente.mision5.MisionCincoVista misionCinco;
    private alcaldedigital.cliente.mision7.MisionSieteVista misionSiete;
    private alcaldedigital.cliente.mision8.MisionOchoVista misionOcho;
    private boolean misionDosPausada;
    private String tablaM5="",tablaM6="",tablaM7="";
    private final LectorJInput lectorMando=new LectorJInput();
    private final ControladorMando controladorMando=new ControladorMando();
    private PImage imagen;
    private LienzoJuego lienzo;
    private PApplet escena;
    private VentanaJuego ventana;
    private boolean f11Mantenido;
    private PFont fuenteMisionTres;
    private final TransicionPixel transicion=new TransicionPixel();
    private String claveVisual() {return juego.pantalla+":"+(juego.pantalla==JuegoCliente.Pantalla.ARBOLES?juego.pestana+":"+juego.laboratorio:"");}
    private boolean entradaLista() {return transicion.lista(claveVisual());}
    private long anterior;
    private final java.util.Set<Integer> pulsadas=new java.util.HashSet<>();

    @Override
    public void settings() {
        size(ANCHO, ALTO, JAVA2D);
        pixelDensity(1);
        noSmooth();
    }

    @Override
    public void setup() {
        surface.setTitle("Alcalde Digital — Promesas, Palomas y Wi-Fi");
        frameRate(60);
        lienzo=new LienzoJuego(this);escena=lienzo.escena();ventana=new VentanaJuego(this,juego.graficos);
        imagen = createImage(ANCHO, ALTO, ARGB);
        try(var archivo=ClienteSketch.class.getResourceAsStream("/cliente/fuentes/PixelifySans.ttf")) {
            if(archivo==null) throw new IllegalStateException("Falta PixelifySans.ttf");
            fuenteMisionTres=new PFont(Font.createFont(Font.TRUETYPE_FONT,archivo).deriveFont(18f),false);
        } catch(IOException|FontFormatException e) {throw new IllegalStateException("Fuente pixel inválida",e);}
        audio.iniciar(juego.audio);
        anterior = System.nanoTime();
    }

    @Override
    public void draw() {
        ventana.actualizar();
        juego.graficos.salida=width+" × "+height;
        lienzo.dibujar(juego.graficos.actual(),()->{try{dibujarContenido();}finally{observarAudio();}});
    }
    private void observarAudio(){
        var c=new alcaldedigital.cliente.audio.Partitura.Contexto(0,"MENU",0,0,false,false,"");
        String id="menu";long tick=frameCount;boolean pausada=juego.pausa;int linea=-1;
        java.util.Map<String,Integer> marcas=new java.util.HashMap<>();
        switch(juego.pantalla){
            case MISION_DOS->{if(misionDos!=null){c=misionDos.contextoAudio();id=misionDos.partida();}}
            case MISION_TRES->{if(misionTres!=null){c=misionTres.contextoAudio();id=misionTres.partida();}}
            case MISION_CUATRO,MISION_SEIS->{if(misionCampana!=null){c=misionCampana.contextoAudio();id="campana"+c.mision();pausada=misionCampana.pausa();}}
            case MISION_CINCO->{if(misionCinco!=null){var v=misionCinco.vistaResultado();if(v!=null){c=new alcaldedigital.cliente.audio.Partitura.Contexto(5,v.fase().name(),0,v.ronda(),switch(v.fase()){case AVISO,ESQUIVA,TURNO,TRAICION->false;default->true;},v.gano(),"");id=v.id();tick=v.tick();pausada=misionCinco.pausaComun();linea=v.linea();marcas.put("fase",v.ronda()*3+v.patron());marcas.put("gravedad",v.gravedad()?1:0);}}}
            case MISION_SIETE->{if(misionSiete!=null){var v=misionSiete.vistaResultado();if(v!=null){float avance=(float)java.util.Arrays.stream(v.corredores()).mapToDouble(a->a.distancia()).max().orElse(0)/9600;c=new alcaldedigital.cliente.audio.Partitura.Contexto(7,v.fase().name(),avance,0,v.fase()!=alcaldedigital.compartido.carrera.EstadoCarrera.Fase.CARRERA,v.ganador()<2&&v.humano(v.ganador()),"");id=v.id();tick=v.tick();pausada=misionSiete.pausaComun();linea=v.linea();marcas.put("pacto",v.electoral().juanCorrupto()?1:0);if(v.fase()==alcaldedigital.compartido.carrera.EstadoCarrera.Fase.CUENTA)marcas.put("cuenta",v.reloj()/60);}}}
            case MISION_OCHO->{if(misionOcho!=null&&misionOcho.listaComun()){var m=misionOcho.motor();c=new alcaldedigital.cliente.audio.Partitura.Contexto(8,m.escena==alcaldedigital.cliente.mision8.MotorOcho.Escena.MUTACION&&!m.inyectado?"ESCRUTINIO":m.escena.name(),0,m.fase,m.escena!=alcaldedigital.cliente.mision8.MotorOcho.Escena.COMBATE,m.escena==alcaldedigital.cliente.mision8.MotorOcho.Escena.FINAL,m.antecedentes.estadoCiudad());id=m.id;tick=m.ticks;pausada=m.pausa;linea=m.lineaDialogo;
                marcas.put("inyeccion",m.inyectado?1:0);marcas.put("recuperacion",m.candidatoRecuperado?1:0);marcas.put("explosion_final",m.palomoNeutralizado?1:0);
                var l=alcaldedigital.cliente.mision8.HistoriaOcho.actual(m);if(l!=null){marcas.put("mutacion",l.accion()==alcaldedigital.cliente.mision8.HistoriaOcho.Accion.MUTAR?1:0);marcas.put("disparo_final",l.accion()==alcaldedigital.cliente.mision8.HistoriaOcho.Accion.DISPARAR&&m.ticksDialogo>=30?1:0);}
                if(m.escena==alcaldedigital.cliente.mision8.MotorOcho.Escena.COMBATE){marcas.put("fase",m.fase);marcas.put(switch(m.ataque){case LLUVIA,BOTS,ABANICO->"espadas";case RAYOS->"rayo_aviso";case MURO->"rayo";case ORBES->"orbe";},m.ataqueTicks>=0?1:0);}
            }}
            case PARQUE,CIVITAS,RESULTADO,EVIDENCIAS,ARBOLES->{if(juego.misionUno!=null){var v=juego.misionUno.vista();c=new alcaldedigital.cliente.audio.Partitura.Contexto(1,v.fase().name(),(float)(1-v.tiempo()/v.dificultad().segundos()),0,v.fase()!=alcaldedigital.compartido.protocolo.SesionMisionUno.Fase.CRISIS,!v.desenlace().isBlank()&&!v.desenlace().equals("MALO"),"");id="m1-"+System.identityHashCode(juego.misionUno);linea=v.dialogo();marcas.put("recuperacion",v.calmados());}}
            default->{}
        }
        directorAudio.observar(new alcaldedigital.cliente.audio.DirectorAudio.Estado(c,id,tick,pausada,linea,marcas));
    }
    @Override public void windowMoved(){if(ventana!=null)ventana.refrescar();}
    @Override public void windowResized(){if(ventana!=null)ventana.refrescar();}

    private void dibujarContenido() {
        long ahora = System.nanoTime();
        double dt=(ahora-anterior)/1_000_000_000.0; anterior=ahora;
        if(Boolean.getBoolean("ad.sistema.revision")&&System.getProperty("ad.guardados")!=null&&frameCount==30){
            juego.accion("jugar");int n=Integer.getInteger("ad.sistema.mision",0);
            if(n>0){juego.accion("mision"+n);if(juego.pantalla==JuegoCliente.Pantalla.GUARDADO)juego.accion(Boolean.getBoolean("ad.sistema.continuar")?"partida:continuar":"partida:nueva");
                if(n==1&&!juego.continuarGuardado)juego.accion("m1dificultad:FACIL");if(n==2&&!juego.continuarGuardado)juego.accion("m2dificultad:NORMAL");}
        }
        configurarSistema();pausaSistema.actualizar((float)Math.min(.1,dt));
        controladorMando.misionCampana(misionCampana);
        controladorMando.misionCinco(misionCinco);
        controladorMando.misionSiete(misionSiete);
        if(Boolean.getBoolean("ad.sistema.revision")&&Boolean.getBoolean("ad.sistema.dos")&&frameCount==69){
            if(misionCinco!=null)misionCinco.raton(200,258);
            if(misionSiete!=null)misionSiete.raton(200,233);
        }
        if(Boolean.getBoolean("ad.sistema.revision")&&frameCount==70){
            if(misionCinco!=null&&misionCinco.vistaResultado()==null){misionCinco.pulsar(' ',10);misionCinco.soltar(10);}
            if(misionSiete!=null&&misionSiete.vistaResultado()==null){misionSiete.pulsar(' ',10);misionSiete.soltar(10);}
        }
        if(Boolean.getBoolean("ad.sistema.revision")&&frameCount==140&&Integer.getInteger("ad.sistema.mision",0)>0){
            if(Boolean.parseBoolean(System.getProperty("ad.sistema.pausa","true"))){pausaSistema.abrir();if(Boolean.getBoolean("ad.sistema.ayuda")){pausaSistema.pulsar(72);pausaSistema.soltar(72);if(Boolean.getBoolean("ad.sistema.controles")){pausaSistema.pulsar(10);pausaSistema.soltar(10);}}}
            if(!pausaSistema.activa()&&Boolean.parseBoolean(System.getProperty("ad.sistema.pausa","true")))throw new IllegalStateException("La pausa común no se abrió");
        }
        if(Boolean.getBoolean("ad.sistema.revision")&&System.getProperty("ad.guardados")!=null&&frameCount>=210){
            String captura=System.getProperty("ad.sistema.captura");if(captura!=null&&!get().save(captura))throw new IllegalStateException("No se pudo capturar el sistema");
            System.out.println("SISTEMA_CAMPANA_OK: misión "+Integer.getInteger("ad.sistema.mision",0)+" · pausa "+pausaSistema.activa());exit();return;
        }
        var estadoMando=lectorMando.leer();
        juego.mandoActivo=estadoMando.conectado();juego.nombreMando=estadoMando.nombre();
        if(Boolean.getBoolean("ad.sistema.revision")&&System.getProperty("ad.sistema.mando")!=null)
            juego.nombreMando=System.getProperty("ad.sistema.mando");
        if(!pausaSistema.mando(estadoMando)){
            if(juego.pantalla==JuegoCliente.Pantalla.MISION_OCHO){if(misionOcho!=null)misionOcho.mando(estadoMando);}
            else controladorMando.actualizar(estadoMando,juego,render,misionDos,misionTres,
                juego.pantalla==JuegoCliente.Pantalla.MISION_DOS?this::salirMisionDosAlMenu:this::salirMisionTresAlMenu);
        }
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_OCHO){
            escena.textFont(fuenteMisionTres);
            if(misionOcho==null){misionOcho=new alcaldedigital.cliente.mision8.MisionOchoVista(juego);misionOcho.iniciarEn(escena);misionOcho.bloquearTeclas(pulsadas);}
            misionOcho.actualizar((float)dt);if(!misionOcho.listaComun())pausaSistema.desconectar();misionOcho.dibujar();pausaSistema.dibujar(escena);
            if(misionOcho.salir())cerrarMisionOcho();
            return;
        }
        int revision=Integer.getInteger("ad.mision46.numero",0);
        if(Boolean.getBoolean("ad.mision46.revision")&&revision!=0&&frameCount==30){juego.accion("jugar");juego.accion("mision"+revision);}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_CUATRO||juego.pantalla==JuegoCliente.Pantalla.MISION_SEIS){
            escena.textFont(fuenteMisionTres);
            if(misionCampana==null){
                misionCampana=new alcaldedigital.cliente.misiones.VistaCampana(juego.pantalla==JuegoCliente.Pantalla.MISION_CUATRO?4:6,juego);
                misionCampana.iniciarEn(escena);misionCampana.elegirPartida(juego.continuarGuardado);misionCampana.gestionExterna(true);misionCampana.bloquearTeclas(pulsadas);
                if(Boolean.getBoolean("ad.mision46.revision")){
                    String fase=System.getProperty("ad.mision46.fase");if(fase!=null)misionCampana.prepararRevision(fase);
                }
            }
            misionCampana.actualizar((float)dt);misionCampana.dibujar();observarVictoria();
            var filaCampana=misionCampana.filaFinal();
            if(filaCampana!=null){
                dibujarTabla(filaCampana);
                if(!filaCampana.victoria()&&!filaCampana.partida().equals(tablaM6)){
                    juego.registrarFila(filaCampana);tablaM6=filaCampana.partida();
                }
            }
            pausaSistema.dibujar(escena);
            if(misionCampana.salir()){cerrarMisionCampana();return;}
            if(Boolean.getBoolean("ad.mision46.revision")){
                if(frameCount==65&&Boolean.getBoolean("ad.mision46.pausa")){misionCampana.pulsar('p',80);misionCampana.soltar(80);}
                if(frameCount>=Integer.getInteger("ad.mision46.captura.frame",120)){
                    String captura=System.getProperty("ad.mision46.captura");
                    if(captura!=null&&!get().save(captura))throw new IllegalStateException("No se pudo guardar la captura");
                    cerrarMisionCampana();
                    if(juego.pantalla!=JuegoCliente.Pantalla.MISIONES)throw new IllegalStateException("Retorno al selector incorrecto");
                    System.out.println("MISION_"+revision+"_INTEGRACION_OK: misma ventana, render y retorno al selector");exit();
                }
            }
            return;
        }
        if(Boolean.getBoolean("ad.mision5.integracion")&&frameCount==30){juego.accion("jugar");juego.accion("mision5");}
        if(Boolean.getBoolean("ad.mision7.integracion")&&frameCount==30){juego.accion("jugar");juego.accion("mision7");}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_SIETE){
            escena.textFont(fuenteMisionTres);
            if(misionSiete==null){misionSiete=new alcaldedigital.cliente.mision7.MisionSieteVista(juego.semilla++,juego.estadoCampana,juego.movimientoReducido,juego.contraste);misionSiete.iniciarEn(escena);misionSiete.gestionExterna(true);misionSiete.continuarDesdeSistema(juego.continuarGuardado);misionSiete.preseleccionarGuardado(juego.modoContinuacion,juego.rolContinuacion);}
            misionSiete.actualizar((float)dt);misionSiete.dibujar();observarVictoria();
            var vista7=misionSiete.vistaResultado();
            if(vista7!=null&&vista7.fase()==alcaldedigital.compartido.carrera.EstadoCarrera.Fase.RESULTADO){
                var fila=alcaldedigital.cliente.misiones.PuntajesMision.siete(vista7);dibujarTabla(fila);
                if(!vista7.id().equals(tablaM7)){juego.registrarFila(fila);tablaM7=vista7.id();}
            }
            pausaSistema.actualizar(0);pausaSistema.dibujar(escena);
            if(misionSiete.salir()){cerrarMisionSiete();return;}
            String captura=System.getProperty("ad.mision7.captura");
            if(captura!=null&&frameCount>=Integer.getInteger("ad.mision7.captura.frame",120)){get().save(captura);System.out.println("MISION_7_RENDER_OK");misionSiete.close();misionSiete=null;exit();}
            return;
        }
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_CINCO){
            escena.textFont(fuenteMisionTres);
            if(misionCinco==null){misionCinco=new alcaldedigital.cliente.mision5.MisionCincoVista(juego.semilla++,juego.estadoCampana,juego.movimientoReducido,juego.contraste);misionCinco.iniciarEn(escena);misionCinco.gestionExterna(true);misionCinco.continuarDesdeSistema(juego.continuarGuardado);misionCinco.preseleccionarGuardado(juego.modoContinuacion);}
            misionCinco.actualizar((float)dt);misionCinco.dibujar();observarVictoria();
            var vista5=misionCinco.vistaResultado();
            if(vista5!=null&&vista5.fase()==alcaldedigital.compartido.batalla.EstadoBatalla.Fase.FINAL){
                var fila=alcaldedigital.cliente.misiones.PuntajesMision.cinco(vista5);dibujarTabla(fila);
                if(!vista5.id().equals(tablaM5)){juego.registrarFila(fila);tablaM5=vista5.id();}
            }
            pausaSistema.actualizar(0);pausaSistema.dibujar(escena);
            if(misionCinco.salir()){cerrarMisionCinco(false);return;}
            String captura=System.getProperty("ad.mision5.captura");
            if(captura!=null&&frameCount>=Integer.getInteger("ad.mision5.captura.frame",160)){get().save(captura);System.out.println("MISION_5_RENDER_OK");misionCinco.close();misionCinco=null;exit();}
            return;
        }
        if(Boolean.getBoolean("ad.mision2.integracion")&&frameCount==30) {
            juego.accion("jugar");juego.accion("mision2");juego.accion("m2dificultad:NORMAL");
        }
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS) {
            escena.textFont(fuenteMisionTres);
            if(misionDos==null) {
                java.util.Properties recuperada=null;
                if(juego.continuarGuardado)try{recuperada=alcaldedigital.compartido.guardado.GuardadoCampana.cargar(2);}catch(IOException e){juego.aviso=e.getMessage();}
                try{misionDos=recuperada!=null?MisionDosSketch.restaurar(recuperada):new MisionDosSketch(juego.dificultadMisionDos,juego.semilla++,juego.falloCriticoMisionDos);}
                catch(RuntimeException e){juego.aviso="Guardado inválido: "+e.getMessage();juego.abrir(JuegoCliente.Pantalla.MISIONES);return;}
                misionDos.iniciarEn(escena);if(recuperada!=null)misionDos.aplicarGuardado(recuperada);
                if(Boolean.getBoolean("ad.mision2.autoprueba")) {
                    misionDos.autoprobar();cerrarMisionDos();
                    if(Boolean.getBoolean("ad.mision2.integracion")) {
                        if(juego.pantalla!=JuegoCliente.Pantalla.MISIONES||
                           juego.ultimoResultadoMisionDos==null||!juego.ultimoResultadoMisionDos.exito())
                            throw new IllegalStateException("La misión 2 no volvió al selector con resultado");
                        System.out.println("MISION_2_INTEGRACION_OK: selector, lienzo y campaña compartidos.");exit();
                    }
                    return;
                }
            }
            if(juego.pausa) {
                misionDosPausada=true;
                escena.image(misionDos.fotograma(),0,0);
            } else {
                if(misionDosPausada) {misionDos.reanudar();misionDosPausada=false;}
                misionDos.dibujarEnJuego();
            }
            if(misionDos.pantallaResultado())dibujarTabla(alcaldedigital.cliente.misiones.PuntajesMision.dos(
                misionDos.resultado(),misionDos.partida(),misionDos.dificultad().name()));
            if(misionDos.resultadoConfirmado()) {cerrarMisionDos();return;}
            observarVictoria();pausaSistema.dibujar(escena);
            String captura=System.getProperty("ad.mision2.captura");
            if(captura!=null&&frameCount>=Integer.getInteger("ad.mision2.captura.frame",120)) {
                if(!get().save(captura)) throw new IllegalStateException("No se pudo guardar captura de misión 2");
                System.out.println("MISION_2_RENDER_OK: escena dibujada en el lienzo principal.");exit();
            }
            return;
        }
        if(Boolean.getBoolean("ad.mision3.integracion") && frameCount==30) {
            juego.accion("jugar");
            juego.accion("mision3");
        }
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_TRES) {
            escena.textFont(fuenteMisionTres);
            if(Boolean.getBoolean("ad.mision3.capturaPausa")&&frameCount==60) juego.accion("pausa");
            if(misionTres==null) {
                misionTres=new MisionTresSketch();
                misionTres.iniciarEn(escena);
                if(juego.continuarGuardado)try{var p=alcaldedigital.compartido.guardado.GuardadoCampana.cargar(3);if(p!=null)misionTres.aplicarGuardado(p);}
                catch(Exception e){juego.aviso="Guardado inválido: "+e.getMessage();misionTres.cerrar();misionTres=null;juego.abrir(JuegoCliente.Pantalla.MISIONES);return;}
                if(Boolean.getBoolean("ad.mision3.autoprueba")) {
                    misionTres.autoprobar();
                    cerrarMisionTres();
                    if(Boolean.getBoolean("ad.mision3.integracion")) {
                        if(juego.pantalla!=JuegoCliente.Pantalla.MISIONES||
                            juego.ultimoResultadoMisionTres==null||
                            !juego.ultimoResultadoMisionTres.completada())
                            throw new IllegalStateException("La misión 3 no volvió al selector con resultado");
                        System.out.println("MISION_3_INTEGRACION_OK: una ventana, selector y progreso compartido.");
                        exit();
                    }
                    return;
                }
            }
            if(juego.pausa) escena.image(misionTres.fotograma(),0,0);
            else misionTres.dibujarEnJuego();
            if(misionTres.resultado().completada())dibujarTabla(alcaldedigital.cliente.misiones.PuntajesMision.tres(misionTres.resultado(),misionTres.partida()));
            observarVictoria();pausaSistema.dibujar(escena);
            String captura=System.getProperty("ad.mision3.captura");
            if(captura!=null&&frameCount>=Integer.getInteger("ad.mision3.captura.frame",120)) {
                if(!get().save(captura)) throw new IllegalStateException("No se pudo guardar la captura de misión 3");
                cerrarMisionTres();
                System.out.println("MISION_3_RENDER_OK: escena dibujada en la ventana principal.");
                exit();
            }
            return;
        }
        // El tiempo de apertura pertenece a la interfaz: no consume el plazo de decisión.
        if(entradaLista()&&!pausaSistema.activa()) juego.actualizar(dt);
        else if(!pausaSistema.activa())juego.animacion+=Math.max(0,Math.min(.1,dt));
        BufferedImage cuadro=render.dibujar(juego);
        boolean panel=juego.pantalla!=JuegoCliente.Pantalla.MENU&&juego.pantalla!=JuegoCliente.Pantalla.ROLES&&juego.pantalla!=JuegoCliente.Pantalla.PARQUE;
        cuadro=transicion.presentar(cuadro,claveVisual(),dt,juego.movimientoReducido,panel);
        imagen.loadPixels();cuadro.getRGB(0,0,ANCHO,ALTO,imagen.pixels,0,ANCHO);imagen.updatePixels();
        escena.image(imagen,0,0);
        if(juego.pantalla==JuegoCliente.Pantalla.RESULTADO&&juego.ultimaFila!=null&&juego.ultimaFila.mision()==1)
            dibujarTabla(juego.ultimaFila);
        pausaSistema.dibujar(escena);
        if(juego.salir) exit();
        if(Boolean.getBoolean("ad.smoke")) {
            if(frameCount==30) juego.iniciar(Rol.CIUDADANO);
            if(frameCount==60) juego.accion("verificar");
            if(frameCount==90) juego.accion("arboles");
            if(frameCount==100) {
                if(juego.laboratorio) throw new IllegalStateException("T debe abrir la vista de jugador en un caso nuevo");
                juego.tecla(84,'t');
                if(juego.pantalla!=JuegoCliente.Pantalla.PARQUE) throw new IllegalStateException("T no volvió al origen");
            }
            if(frameCount==110) {juego.accion("civitas");juego.accion("arboles");juego.accion("laboratorio");}
            if(frameCount==120) {juego.accion("campoBusqueda");juego.tecla(49,'1');juego.accion("buscar");}
            if(frameCount==150) {
                if(!juego.encontrado.contains("#1")) throw new IllegalStateException("Búsqueda no resuelta");
                // PApplet.save puede diferir la escritura; guardar el frame sin salir antes de terminar.
                String destino=java.nio.file.Path.of(System.getProperty("ad.captura","entregas/entrega1/evidencias/processing-real.png")).toAbsolutePath().toString();
                if(!get().save(destino)) throw new IllegalStateException("No se pudo guardar la captura Processing");
                juego.tecla(84,'t');
                if(juego.pantalla!=JuegoCliente.Pantalla.CIVITAS) throw new IllegalStateException("Retorno incorrecto a Civitas");
                System.out.println("PROCESSING_SMOKE_OK: 150 cuadros, vista de jugador, Laboratorio, búsqueda y retorno a Civitas.");exit();
            }
        }
    }
    private void configurarSistema(){
        int n=juego.numeroMision();juego.pausaUnificada=n!=0;
        if(n==0){pausaSistema.desconectar();return;}
        pausaSistema.conectar(new alcaldedigital.cliente.misiones.PausaUnificada.Sesion(){
            public int numero(){return n;}
            public boolean lista(){return switch(n){case 1->juego.misionUno!=null;case 2->misionDos!=null;case 3->misionTres!=null;case 4,6->misionCampana!=null;case 5->misionCinco!=null&&misionCinco.listaComun();case 7->misionSiete!=null&&misionSiete.listaComun();case 8->misionOcho!=null&&misionOcho.listaComun();default->false;};}
            public boolean pausada(){return switch(n){case 4,6->misionCampana!=null&&misionCampana.pausa();case 5->misionCinco!=null&&misionCinco.pausaComun();case 7->misionSiete!=null&&misionSiete.pausaComun();case 8->misionOcho!=null&&misionOcho.pausaComun();default->juego.pausa;};}
            public void pausar(){switch(n){case 4,6->misionCampana.pausarComun();case 5->misionCinco.pausarComun();case 7->misionSiete.pausarComun();case 8->misionOcho.pausarComun();default->{juego.pausa=true;juego.teclas.clear();juego.movimientoMandoX=juego.movimientoMandoY=0;if(misionDos!=null)misionDos.perderFoco();if(misionTres!=null)misionTres.perderFoco();}}}
            public void reanudar(){switch(n){case 4,6->misionCampana.reanudarComun();case 5->misionCinco.reanudarComun();case 7->misionSiete.reanudarComun();case 8->misionOcho.reanudarComun();default->{juego.pausa=false;juego.teclas.clear();if(misionDos!=null)misionDos.reanudar();if(misionTres!=null)misionTres.reanudar();}}}
            public String guardar()throws Exception{return guardarActual();}
            public void salir(){salirActual();}
            public alcaldedigital.cliente.entrada.MapaControles.Contexto controles(){
                boolean dos=n==4||n==6?misionCampana!=null&&misionCampana.dosTeclados():
                    n==5?misionCinco!=null&&misionCinco.dosTeclados():n==7&&misionSiete!=null&&misionSiete.dosTeclados();
                String j1=switch(n){case 1->"Sofi · ciudadana";case 2->"Periodista";case 3->"Influencer";
                    case 8->"Sofi / rol elegido";case 4->"Juan · candidato";case 5->"Ciudadano";case 6->"Juan · candidato";
                    default->dos||misionSiete==null?"Periodista":misionSiete.personajeLocal();};
                String j2=switch(n){case 5->"Periodista";case 6->"Influencer";case 7->"Influencer";default->"Jugador 2";};
                return new alcaldedigital.cliente.entrada.MapaControles.Contexto(n,dos,j1,j2,juego.nombreMando);
            }
            public boolean consultaAbierta(){return n==8?misionOcho.consultaAbiertaComun():n==5?misionCinco.consultaAbiertaComun():n==7?misionSiete.consultaAbiertaComun():juego.pantalla==JuegoCliente.Pantalla.ARBOLES;}
            public boolean tieneConsultas(){return n==1||n==5||n==7||n==8;}
            public void abrirConsulta(){if(n==8)misionOcho.abrirConsultaComun();else if(n==5)misionCinco.abrirConsultaComun();else if(n==7)misionSiete.abrirConsultaComun();else if(n==1){juego.accion("arboles");juego.pausa=true;}}
            public void cerrarConsulta(){if(n==8)misionOcho.cerrarConsultaComun();else if(n==5)misionCinco.cerrarConsultaComun();else if(n==7)misionSiete.cerrarConsultaComun();else if(n==1){juego.cerrarArboles();juego.pausa=true;}}
        });
    }
    private String guardarActual()throws Exception{
        if(juego.numeroMision()!=5&&juego.numeroMision()!=7&&juego.numeroMision()!=8)new alcaldedigital.servidor.carrera.RepositorioCarrera(alcaldedigital.compartido.guardado.ArchivosPartida.directorio().resolve("campana.properties")).guardarCiudad(juego.estadoCampana);
        return switch(juego.numeroMision()){
            case 1->{juego.guardarMisionUno();yield "Partida guardada.";}
            case 2->{alcaldedigital.compartido.guardado.GuardadoCampana.guardar(2,misionDos.capturar());yield "Partida guardada.";}
            case 3->{alcaldedigital.compartido.guardado.GuardadoCampana.guardar(3,misionTres.capturar());yield "Guardado: inicio de la zona actual de NovaMall.";}
            case 8->misionOcho.guardarComun();case 4,6->misionCampana.guardarComun();case 5->misionCinco.guardarComun();case 7->misionSiete.guardarComun();
            default->throw new IOException("No hay misión iniciada");
        };
    }
    private void salirActual(){switch(juego.numeroMision()){
        case 2->cerrarMisionDos();case 3->cerrarMisionTres();case 4,6->cerrarMisionCampana();case 5->cerrarMisionCinco(false);case 7->cerrarMisionSiete();case 8->cerrarMisionOcho();default->juego.abrir(JuegoCliente.Pantalla.MISIONES);
    }pausaSistema.desconectar();anterior=System.nanoTime();}
    private void observarVictoria(){switch(juego.numeroMision()){
        case 2->{if(misionDos!=null&&misionDos.resultado().terminada()&&misionDos.resultado().exito()&&!misionDos.resultado().falloCritico())juego.victoria(2);}
        case 3->{if(misionTres!=null&&misionTres.resultado().completada())juego.victoria(3);}
        case 4,6->{if(misionCampana!=null&&misionCampana.victoriaComun())juego.victoria(juego.numeroMision());}
        case 5->{if(misionCinco!=null&&misionCinco.victoriaComun())juego.victoria(5);}
        case 7->{if(misionSiete!=null&&misionSiete.victoriaComun())juego.victoria(7);}
        default->{}
    }}
    private void cerrarMisionCampana(){
        if(misionCampana==null)return;
        misionCampana.terminar();misionCampana.close();misionCampana=null;
        pulsadas.clear();anterior=System.nanoTime();
    }
    private void cerrarMisionTres() {
        if(misionTres==null) return;
        observarVictoria();
        var resultado=misionTres.resultado();
        if(resultado.completada())juego.registrarFila(alcaldedigital.cliente.misiones.PuntajesMision.tres(resultado,misionTres.partida()));
        misionTres.cerrar();
        misionTres=null;
        juego.terminarMisionTres(resultado);
        anterior=System.nanoTime();
    }
    private void cerrarMisionCinco(boolean menu){
        observarVictoria();
        if(misionCinco!=null){juego.estadoCampana=misionCinco.ciudadResultado();misionCinco.close();misionCinco=null;}
        juego.abrir(menu?JuegoCliente.Pantalla.MENU:JuegoCliente.Pantalla.MISIONES);anterior=System.nanoTime();
    }
    private void cerrarMisionOcho(){if(misionOcho!=null){misionOcho.close();misionOcho=null;}pausaSistema.desconectar();juego.abrir(JuegoCliente.Pantalla.MISIONES);pulsadas.clear();anterior=System.nanoTime();}
    private void cerrarMisionSiete(){observarVictoria();if(misionSiete!=null){juego.estadoCampana=misionSiete.ciudadResultado();misionSiete.close();misionSiete=null;}juego.abrir(JuegoCliente.Pantalla.MISIONES);anterior=System.nanoTime();}
    private void cerrarMisionDos() {
        if(misionDos==null) return;
        var resultado=misionDos.resultado();
        if(resultado.terminada())juego.registrarFila(alcaldedigital.cliente.misiones.PuntajesMision.dos(
            resultado,misionDos.partida(),misionDos.dificultad().name()));
        misionDos.cerrar();misionDos=null;
        misionDosPausada=false;
        juego.terminarMisionDos(resultado);
        anterior=System.nanoTime();
    }
    private void salirMisionDosAlMenu() {cerrarMisionDos();juego.abrir(JuegoCliente.Pantalla.MENU);}
    private void salirMisionTresAlMenu() {
        cerrarMisionTres();
        juego.abrir(JuegoCliente.Pantalla.MENU);
    }
    private void dibujarTabla(alcaldedigital.compartido.guardado.TablaPuntajes.Fila fila){
        escena.pushStyle();
        escena.textFont(fuenteMisionTres);escena.textAlign(LEFT,BASELINE);
        final int x=28,y=4,w=1224,h=130,division=625;
        escena.noStroke();escena.fill(8,5,16,150);escena.rect(x+5,y+5,w,h);
        escena.fill(25,14,39,250);escena.rect(x,y,w,h);
        escena.stroke(244,203,106);escena.strokeWeight(2);escena.noFill();escena.rect(x,y,w,h);
        escena.stroke(187,145,218);escena.strokeWeight(1);escena.rect(x+4,y+4,w-8,h-8);
        escena.noStroke();escena.fill(244,203,106);escena.rect(x+8,y+8,5,27);
        escena.fill(245,232,251);escena.textSize(20);
        escena.text(ajustarTabla("MISIÓN "+fila.mision()+" · "+tituloTabla(fila.mision()),640),x+22,y+28);
        int estado=fila.victoria()?escena.color(115,221,203):escena.color(247,137,163);
        escena.fill(estado);escena.textSize(16);
        String dificultad=fila.dificultad().isBlank()?"":" · "+fila.dificultad();
        escena.text(ajustarTabla((fila.victoria()?"VICTORIA":"DERROTA · REPITE")+dificultad,225),x+division+22,y+28);
        escena.fill(20,12,34);escena.rect(x+w-175,y+7,164,31);
        escena.stroke(244,203,106);escena.strokeWeight(1);escena.noFill();escena.rect(x+w-175,y+7,164,31);
        escena.noStroke();escena.fill(244,203,106);escena.textSize(20);escena.text(fila.puntaje()+" / 100",x+w-158,y+29);
        escena.fill(65,42,84);escena.rect(x+5,y+40,w-10,20);
        escena.fill(244,203,106);escena.textSize(14);escena.text("INDICADORES DEL INTENTO",x+22,y+55);
        escena.text("DECISIONES REGISTRADAS",x+division+22,y+55);
        java.util.List<String> decisiones=decisionesTabla(fila);
        for(int i=0;i<4;i++){
            int superior=y+60+i*17;
            escena.fill(i%2==0?escena.color(36,22,54):escena.color(44,27,61));escena.rect(x+5,superior,w-10,17);
            escena.stroke(104,75,125);escena.strokeWeight(1);escena.line(x+5,superior+17,x+w-5,superior+17);
            escena.noStroke();escena.fill(245,232,251);escena.textSize(14);
            String dato=i<fila.datos().size()?fila.datos().get(i):"—";
            escena.text(ajustarTabla(dato,division-45),x+22,superior+13);
            String decision=i<decisiones.size()?decisiones.get(i):i==0&&decisiones.isEmpty()?"Sin preguntas en esta misión":"—";
            escena.text(ajustarTabla(decision,w-division-45),x+division+22,superior+13);
        }
        escena.stroke(187,145,218);escena.strokeWeight(1);escena.line(x+division,y+40,x+division,y+128);
        escena.popStyle();
    }
    private static String tituloTabla(int mision){return switch(mision){
        case 1->"EL PARQUE DESPUÉS DE CIVITAS";
        case 2->"LA RED CIVITAS";
        case 3->"CIVITAS EN ROJO";
        case 4->"LA PLAZA QUE DEJÓ DE ESCUCHAR";
        case 5->"BATALLA DEL SERVIDOR";
        case 6->"CIELOS DE CAMPAÑA";
        case 7->"LA CARRERA POR LA PRIMICIA";
        default->"RESULTADO";
    };}
    private String ajustarTabla(String valor,int ancho){
        if(escena.textWidth(valor)<=ancho)return valor;
        int n=valor.length();
        while(n>1&&escena.textWidth(valor.substring(0,n)+"…")>ancho)n--;
        return valor.substring(0,n)+"…";
    }
    private static java.util.List<String> decisionesTabla(alcaldedigital.compartido.guardado.TablaPuntajes.Fila fila){
        java.util.List<String> opciones=fila.decisiones();
        if(fila.mision()==5)opciones=opciones.stream().filter(s->s.startsWith("Ronda ")).toList();
        int inicio=Math.max(0,opciones.size()-4);
        return opciones.subList(inicio,opciones.size()).stream()
            .map(s->fila.mision()==7?s.replaceFirst("^r[01]e[0-3]a[0-3] · ",""):s).toList();
    }
    private void dibujarPausaMisionTres() {
        escena.noStroke();escena.fill(13,8,24,225);escena.rect(0,0,ANCHO,ALTO);
        marcoPausaPixel(340,118,600,482,escena.color(56,38,75),escena.color(244,203,106));
        escena.textAlign(LEFT,BASELINE);escena.fill(244,203,106);escena.textSize(38);escena.text("PAUSA",380,171);
        escena.fill(245,232,251);escena.textSize(18);escena.text(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS?
            "Misión 2 · La red Civitas":"Misión 3 · Civitas en Rojo",380,205);
        escena.textSize(19);
        if(juego.controlesPausa) {
            if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS) {
                escena.text("WASD: mover · Shift: correr · Espacio: salto",380,268);
                escena.text("E: defender · Mando: A / RB / X",380,297);
                escena.text("Los ataques se detienen durante la pausa.",380,326);
            } else {
                escena.text("Stick izquierdo: mover · Stick derecho: apuntar",380,268);
                escena.text("A: hablar · RT/X: PING · LT: FLASH",380,297);
                escena.text("RB: DASH · LB: privado · Y: LIVE BURST",380,326);
            }
        } else {
            escena.text("El tiempo de la misión está detenido.",380,268);
            escena.text("Puedes continuar, consultar los controles",380,297);
            escena.text("o regresar al menú.",380,326);
        }
        String[] opciones={"Continuar",juego.controlesPausa?"Ocultar controles":"Ver controles","Salir al menú"};
        for(int i=0;i<opciones.length;i++) {
            int y=421+i*54;
            marcoPausaPixel(380,y,520,42,
                juego.foco==i?escena.color(244,203,106):escena.color(67,43,91),
                juego.foco==i?escena.color(245,232,251):escena.color(181,142,250));
            escena.fill(juego.foco==i?escena.color(33,21,47):escena.color(245,232,251));
            escena.textSize(18);escena.text(opciones[i],400,y+29);
        }
    }
    private void marcoPausaPixel(int x,int y,int w,int h,int fondo,int borde) {
        escena.noStroke();escena.fill(13,8,24,180);escena.rect(x+4,y+5,w,h);
        escena.fill(fondo);escena.stroke(borde);escena.strokeWeight(2);
        escena.beginShape();escena.vertex(x+6,y);escena.vertex(x+w-6,y);escena.vertex(x+w,y+6);
        escena.vertex(x+w,y+h-6);escena.vertex(x+w-6,y+h);escena.vertex(x+6,y+h);
        escena.vertex(x,y+h-6);escena.vertex(x,y+6);escena.endShape(CLOSE);
        escena.noStroke();escena.fill(borde);escena.rect(x+3,y+3,7,7);escena.rect(x+w-10,y+3,7,7);
        escena.rect(x+3,y+h-10,7,7);escena.rect(x+w-10,y+h-10,7,7);
    }
    private void activarPausaMisionTres() {
        switch(juego.foco) {
            case 0 -> {juego.accion("reanudar");if(misionDos!=null)misionDos.reanudar();}
            case 1 -> juego.accion("controlesPausa");
            case 2 -> {if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS)salirMisionDosAlMenu();
                else salirMisionTresAlMenu();}
            default -> { }
        }
    }
    @Override public void mousePressed() {
        var punto=EscaladoPantalla.ajustar(Math.max(1,width),Math.max(1,height),juego.graficos.actual().entero()).aLogico(mouseX,mouseY);
        if(!punto.dentro())return;
        pulsarRatonLogico(punto.x(),punto.y());
    }
    private void pulsarRatonLogico(int mouseX,int mouseY) {
        if(pausaSistema.raton(mouseX,mouseY))return;
        if(misionCampana!=null){misionCampana.raton(mouseX,mouseY);return;}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_OCHO){if(misionOcho!=null)misionOcho.raton(mouseX,mouseY);return;}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_SIETE){if(misionSiete!=null)misionSiete.raton(mouseX,mouseY);return;}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_CINCO){if(misionCinco!=null)misionCinco.raton(mouseX,mouseY);return;}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS||juego.pantalla==JuegoCliente.Pantalla.MISION_TRES) {
            if(juego.pausa) {
                if(mouseX>=380&&mouseX<900)
                    for(int i=0;i<3;i++) if(mouseY>=421+i*54&&mouseY<463+i*54) {
                        juego.foco=i;activarPausaMisionTres();break;
                    }
                return;
            }
            if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS) return;
            if(misionTres!=null) {
                misionTres.apuntarRaton(mouseX*1152/ANCHO,mouseY*648/ALTO);
                misionTres.pulsarRaton(mouseButton);
            }
            return;
        }
        if(!entradaLista()) return;
        render.pulsar(juego,mouseX,mouseY);
    }
    @Override public void mouseReleased() {
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_TRES&&misionTres!=null)
            misionTres.soltarRaton(mouseButton);
    }
    @Override public void mouseMoved() {
        var punto=EscaladoPantalla.ajustar(Math.max(1,width),Math.max(1,height),juego.graficos.actual().entero()).aLogico(mouseX,mouseY);
        moverRatonLogico(punto.x(),punto.y());
    }
    private void moverRatonLogico(int mouseX,int mouseY) {
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_TRES&&misionTres!=null){
            if(mouseX>=0)misionTres.apuntarRaton(mouseX*1152/ANCHO,mouseY*648/ALTO);
        }
        else render.apuntar(mouseX,mouseY);
    }
    @Override public void mouseDragged() {mouseMoved();}
    @Override public void mouseExited() {render.apuntar(-1,-1);}
    @Override public void keyPressed() {
        int codigo=keyCode;char caracter=key;
        if(codigo==java.awt.event.KeyEvent.VK_F11){if(!f11Mantenido){f11Mantenido=true;juego.graficos.alternarCompleta();}return;}
        if(key==ESC) key=0;
        if(pausaSistema.pulsar(codigo))return;
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_OCHO){if(misionOcho!=null)misionOcho.pulsar(caracter,codigo);return;}
        if(misionCampana!=null){misionCampana.pulsar(caracter,codigo);return;}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_SIETE){if(misionSiete!=null)misionSiete.pulsar(caracter,codigo);return;}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_CINCO){if(misionCinco!=null)misionCinco.pulsar(caracter,codigo);return;}
        if(juego.pausa) {
            if(codigo==ESC||codigo==80) {juego.accion("reanudar");return;}
            if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS||juego.pantalla==JuegoCliente.Pantalla.MISION_TRES) {
                if(codigo==UP) juego.foco=Math.floorMod(juego.foco-1,3);
                else if(codigo==DOWN) juego.foco=(juego.foco+1)%3;
                else if(codigo==ENTER||codigo==RETURN) activarPausaMisionTres();
            } else {
                if(codigo==TAB) render.avanzarFoco(juego,keyEvent.isShiftDown()?-1:1);
                else if(codigo==UP) render.moverFocoDireccion(juego,0,-1);
                else if(codigo==DOWN) render.moverFocoDireccion(juego,0,1);
                else if(codigo==ENTER||codigo==RETURN) render.activarFoco(juego);
            }
            return;
        }
        if(codigo==80&&(juego.misionUno!=null||juego.pantalla==JuegoCliente.Pantalla.MISION_DOS||
            juego.pantalla==JuegoCliente.Pantalla.MISION_TRES)) {
            juego.accion("pausa");return;
        }
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS) {
            if(codigo==ESC) salirMisionDosAlMenu();
            else if(misionDos!=null) misionDos.pulsarTecla(caracter,codigo);
            return;
        }
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_TRES) {
            if(codigo==ESC) salirMisionTresAlMenu();
            else if(misionTres!=null) misionTres.pulsarTecla(caracter,codigo);
            return;
        }
        if(!entradaLista()) {if(codigo==ESC) juego.tecla(codigo,caracter);return;}
        boolean nueva=pulsadas.add(codigo);
        if(!nueva&&!juego.editandoBusqueda) return;
        if(codigo==TAB) {render.avanzarFoco(juego,keyEvent.isShiftDown()?-1:1);return;}
        if(codigo==ENTER||codigo==RETURN) {
            render.activarFoco(juego);return;
        }
        juego.teclas.add(codigo);juego.tecla(codigo,caracter);
    }
    @Override public void keyReleased() {
        if(keyCode==java.awt.event.KeyEvent.VK_F11){f11Mantenido=false;return;}
        pausaSistema.soltar(keyCode);
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_OCHO){if(misionOcho!=null)misionOcho.soltar(keyCode);return;}
        if(misionCampana!=null){misionCampana.soltar(keyCode);return;}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_SIETE){if(misionSiete!=null)misionSiete.soltar(keyCode);return;}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_CINCO){if(misionCinco!=null)misionCinco.soltar(keyCode);return;}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS) {
            if(misionDos!=null)misionDos.soltarTecla(key,keyCode);return;
        }
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_TRES) {
            if(misionTres!=null) misionTres.soltarTecla(key,keyCode);
            return;
        }
        juego.teclas.remove(keyCode);pulsadas.remove(keyCode);
    }
    @Override public void focusLost() {
        juego.teclas.clear();pulsadas.clear();pausaSistema.perderFoco();
        if(misionCampana!=null)misionCampana.perderFoco();
        if(misionCinco!=null)misionCinco.perderFoco();
        if(misionSiete!=null)misionSiete.perderFoco();
        if(misionOcho!=null)misionOcho.perderFoco();
        if(misionTres!=null) misionTres.perderFoco();
        if(misionDos!=null) misionDos.perderFoco();
    }
    @Override public void dispose() {
        audio.close();
        if(juego.numeroMision()!=0)try{guardarActual();}catch(Exception e){System.err.println("No se pudo guardar al cerrar: "+e.getMessage());}
        if(misionCampana!=null){misionCampana.close();misionCampana=null;}
        if(misionSiete!=null)misionSiete.close();
        if(misionOcho!=null)misionOcho.close();
        if(misionCinco!=null)misionCinco.close();
        if(misionTres!=null) {misionTres.cerrar();misionTres=null;}
        if(misionDos!=null) {misionDos.cerrar();misionDos=null;}
    }

}
