package alcaldedigital.cliente.misiones;

import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.entrada.EntradaMandoLocal;
import alcaldedigital.cliente.entrada.MandosJugadores;
import alcaldedigital.cliente.entrada.MapaControles;
import alcaldedigital.cliente.interfaz.MarcosProcessing;
import alcaldedigital.cliente.mision4.MisionCuatroSketch;
import alcaldedigital.cliente.mision6.MisionSeisSketch;
import alcaldedigital.cliente.mision6.MisionSeisMotor;
import alcaldedigital.cliente.mision6.RedSeis;
import alcaldedigital.cliente.red.PantallaLan;
import java.io.IOException;
import processing.core.*;
import java.util.*;
import static java.awt.event.KeyEvent.*;
import static alcaldedigital.cliente.entrada.EstadoMando.Boton;

/** Adaptación de los capítulos recibidos al reloj, pausa, entrada y campaña del anfitrión. */
public final class VistaCampana implements AutoCloseable {
    private boolean gestionExterna;
    public void gestionExterna(boolean v){gestionExterna=v;if(cuatro!=null)cuatro.gestionExterna(v);else seis.gestionExterna(v);}
    public void elegirPartida(boolean continuar){if(pantallaLan!=null&&pantallaLan.activa())return;if(cuatro!=null&&cuatro.eligiendoGuardado())cuatro.elegirGuardado(!continuar);if(seis!=null&&seis.eligiendoGuardado())seis.elegirGuardado(!continuar);}
    public void pausarComun(){pausar();}
    public void reanudarComun(){reanudar();}
    public String guardarComun()throws Exception{
        if(redSeis!=null){redSeis.guardar();return redSeis.anfitrion()?"Partida LAN guardada.":"Guardado solicitado al anfitrión.";}if(cuatro!=null)cuatro.guardarAhora();else seis.guardarAhora();return "Partida guardada.";}
    private final int numero;
    private final JuegoCliente juego;
    private final MisionCuatroSketch cuatro;
    private final MisionSeisSketch seis;
    private final PantallaLan pantallaLan;
    private RedSeis redSeis, enlacePendiente;
    private PantallaLan.Destino destinoLan;
    private String tokenLan="", mensajeLan="", falloEnlace;
    private boolean conectandoLan, cerrada, jugandoLocal, jugandoLan;
    private boolean pausaSolicitada;
    private int ocupadosLan;
    private final Set<Integer> teclasInvitado=new HashSet<>();
    private final Set<Integer> teclasSegundoMando=new HashSet<>();
    private final EntradaMandoLocal mandoDos=new EntradaMandoLocal();
    private PApplet app;
    private boolean pausa,controles,salir,terminada;
    private boolean centrarMando=true;
    private int foco;
    private final Set<Integer> pulsadas=new HashSet<>(), teclasMando=new HashSet<>();
    private EstadoMando anterior=EstadoMando.desconectado();
    public VistaCampana(int numero,JuegoCliente juego) {
        if(numero!=4&&numero!=6)throw new IllegalArgumentException("Misión no soportada");
        this.numero=numero;this.juego=juego;
        cuatro=numero==4?new MisionCuatroSketch():null;
        seis=numero==6?new MisionSeisSketch():null;
        pantallaLan=numero==6?new PantallaLan(6,RedSeis.PUERTO,"Juan","Influencer"):null;
    }
    public void bloquearTeclas(Collection<Integer> retenidas){pulsadas.addAll(retenidas);}
    public void iniciarEn(PApplet app) {
        this.app=app;
        if(cuatro!=null){cuatro.movimientoReducido(juego.movimientoReducido);cuatro.iniciarEn(app);return;}
        seis.movimientoReducido(juego.movimientoReducido);
        seis.iniciarEn(app);
        if(app==null||Boolean.getBoolean("ad.mision46.revision")||Boolean.getBoolean("ad.mision6.integracion")||Boolean.getBoolean("ad.sistema.revision")){
            jugandoLocal=true;return;
        }
        if(juego.continuarGuardado&&juego.modoContinuacion==2){
            crearLan(true);
            if(redSeis!=null)return;
        }
        modosSeis();
    }
    private void modosSeis(){
        pantallaLan.modos(()->{
            jugandoLocal=true;
            pantallaLan.close();
            boolean continuar=juego.continuarGuardado&&juego.modoContinuacion!=2;
            elegirPartida(continuar);
            if(continuar&&dosTeclados()&&juego.asignarPuestos(6,seis.motor().partida,2,()->{
                jugandoLocal=true;
                pantallaLan.close();
                centrarMando=true;
            })){
                jugandoLocal=false;
                modosSeis();
                return;
            }
            centrarMando=true;
        },()->crearLan(false),this::buscarLan,()->salir=true);
    }
    private void buscarLan(){
        mensajeLan="";
        pantallaLan.buscar(destino->{destinoLan=destino;tokenLan="";conectarLan();},this::modosSeis);
    }
    private void crearLan(boolean continuar){
        try{
            redSeis=RedSeis.alojar(RedSeis.PUERTO,continuar);
            mensajeLan="";
            try{redSeis.anunciar();}
            catch(IOException e){mensajeLan="Búsqueda automática no disponible. Los invitados pueden conectar por IP.";}
            ocupadosLan=1;
            pantallaLan.esperar(()->salir=true);
        }catch(IOException e){mensajeLan="No se pudo crear la partida: "+e.getMessage();}
    }
    private void conectarLan(){
        if(conectandoLan)return;
        conectandoLan=true;mensajeLan="Conectando con el anfitrión…";
        var destino=destinoLan;
        String reserva=tokenLan;
        Thread hilo=new Thread(()->{
            try{
                RedSeis enlace=RedSeis.unir(destino.ip(),destino.puerto(),reserva);
                synchronized(this){if(cerrada)enlace.close();else enlacePendiente=enlace;}
            }catch(IOException e){synchronized(this){falloEnlace=e.getMessage();}}
        },"m6-conectar");
        hilo.setDaemon(true);hilo.start();
    }
    private synchronized void recibirEnlace(){
        if(falloEnlace!=null){mensajeLan="Conexión: "+falloEnlace;falloEnlace=null;conectandoLan=false;}
        if(enlacePendiente==null)return;
        redSeis=enlacePendiente;enlacePendiente=null;tokenLan=redSeis.token();
        conectandoLan=false;mensajeLan="";
        pantallaLan.esperar(()->salir=true);
    }
    private void actualizarLan(){
        if(!redSeis.anfitrion()&&!redSeis.error().isBlank()){
            mensajeLan=redSeis.error();redSeis.close();redSeis=null;
            teclasInvitado.clear();pausaSolicitada=false;pausa=false;juego.pausa=false;
            pantallaLan.reconectar(this::conectarLan,()->salir=true);return;
        }
        var vista=redSeis.vista();
        if(vista==null)return;
        ocupadosLan=vista.ocupados();
        if(!vista.iniciada())return;
        if(pantallaLan.esperando()){
            pantallaLan.close();jugandoLan=true;centrarMando=true;mensajeLan="";
        }
        if(vista.pausada())pausaSolicitada=false;
        pausa=vista.pausada()||pausaSolicitada;juego.pausa=pausa;
        if(!vista.errorGuardado().isEmpty())mensajeLan=vista.errorGuardado();
        seis.verRed(vista,!redSeis.anfitrion());
        if(seis.salidaSolicitada()&&redSeis.cierreConfirmado())salir=true;
    }
    public void actualizar(float dt) {
        if(salir)return;
        if(seis!=null){
            recibirEnlace();
            if(redSeis!=null){actualizarLan();return;}
            if(pantallaLan.activa())return;
        }
        if(pausa)return;
        if(cuatro!=null)cuatro.actualizar(dt);else seis.actualizar(dt);
        if(seis!=null&&seis.salidaSolicitada())salir=true;
    }
    public boolean listaComun(){return pantallaLan==null||!pantallaLan.activa();}
    public boolean invitadoLan(){return redSeis!=null&&!redSeis.anfitrion();}
    public boolean lanPuntajes(){return redSeis!=null;}
    public String partidaPuntajes(){return seis==null||!listaComun()?null:seis.motor().partida;}
    public boolean reintentarPuntajes(){
        if(seis==null||invitadoLan()||seis.motor().fase!=MisionSeisMotor.Fase.DERROTA)return false;
        press(VK_ENTER);release(VK_ENTER);return true;
    }
    public boolean victoriaComun(){return cuatro!=null?cuatro.resultado().completada():listaComun()&&seis.resultado().completada();}
    public alcaldedigital.compartido.guardado.TablaPuntajes.Fila filaFinal(){
        if(cuatro!=null)return cuatro.resultado().completada()?PuntajesMision.cuatro(cuatro.resultado()):null;
        return listaComun()?filaSeis(seis.motor()):null;
    }
    static alcaldedigital.compartido.guardado.TablaPuntajes.Fila filaSeis(MisionSeisMotor motor){
        if(motor.fase==MisionSeisMotor.Fase.DERROTA)return PuntajesMision.seis(motor.resultado());
        if(motor.fase!=MisionSeisMotor.Fase.FINAL)return null;
        var resultado=motor.resultado();
        // En FINAL ya se ganó; Enter confirma el cierre y recién entonces acredita progreso.
        if(!resultado.completada())resultado=new MisionSeisMotor.Resultado(true,resultado.modo(),resultado.ruta(),
            resultado.integridad1(),resultado.integridad2(),resultado.partida(),resultado.respuestas());
        return PuntajesMision.seis(resultado);
    }
    public boolean pausa(){return pausa;}
    public boolean salir(){return salir;}
    public alcaldedigital.cliente.audio.Partitura.Contexto contextoAudio(){if(cuatro!=null)return cuatro.contextoAudio();var m=seis.motor();return new alcaldedigital.cliente.audio.Partitura.Contexto(6,m.fase.name(),0,m.etapa(),switch(m.fase){case COMBATE,ENTRENAMIENTO,DESPEGUE,LLEGADA->false;default->true;},m.fase==MisionSeisMotor.Fase.FINAL,"");}
    public Properties estado(){return cuatro!=null?cuatro.estado():seis.motor().capturar();}
    private void press(int k){
        if(redSeis!=null){teclaLan(k,true);return;}
        if(cuatro!=null){cuatro.pulsarTecla((char)k,k);return;}
        var motor=seis.motor();
        boolean duo=motor.fase==MisionSeisMotor.Fase.MODO&&
            (k==VK_2||((k==VK_ENTER||k==VK_SPACE)&&motor.seleccion==1));
        if(duo&&juego.asignarPuestos(2,()->{
            seis.pulsarTecla((char)k,k);
            seis.soltarTecla(k);
        }))return;
        seis.pulsarTecla((char)k,k);
    }
    private void release(int k){
        if(redSeis!=null){teclaLan(k,false);return;}
        if(cuatro!=null)cuatro.soltarTecla(k);else seis.soltarTecla(k);}
    private void teclaLan(int k,boolean pulsada){
        k=switch(k){case VK_LEFT->VK_A;case VK_RIGHT->VK_D;case VK_UP->VK_W;case VK_DOWN->VK_S;default->k;};
        if(redSeis.anfitrion()){
            redSeis.teclaAnfitrion(k,pulsada);
            return;
        }
        if(pulsada&&k==VK_Q)redSeis.especialInvitado(RedSeis.MISIL);
        if(pulsada&&k==VK_SPACE)redSeis.especialInvitado(RedSeis.IMPULSO);
        if(pulsada)teclasInvitado.add(k);else teclasInvitado.remove(k);
        int mascara=0;
        if(teclasInvitado.contains(VK_A))mascara|=RedSeis.IZQUIERDA;
        if(teclasInvitado.contains(VK_D))mascara|=RedSeis.DERECHA;
        if(teclasInvitado.contains(VK_W))mascara|=RedSeis.ARRIBA;
        if(teclasInvitado.contains(VK_S))mascara|=RedSeis.ABAJO;
        if(teclasInvitado.contains(VK_F))mascara|=RedSeis.DISPARO;
        redSeis.movimientoInvitado(mascara);
    }
    private void limpiar(){
        if(redSeis!=null){teclasInvitado.clear();redSeis.movimientoInvitado(0);}
        else if(cuatro!=null)cuatro.perderFoco();else seis.perderFoco();
        teclasMando.clear();teclasSegundoMando.clear();mandoDos.bloquear();centrarMando=true;
    }
    private void pausar(){
        limpiar();pausa=true;juego.pausa=true;controles=false;foco=0;
        if(redSeis!=null){pausaSolicitada=true;redSeis.pausar();}
    }
    private void reanudar(){
        limpiar();
        if(redSeis!=null){pausaSolicitada=false;redSeis.reanudar();return;}
        if(cuatro!=null)cuatro.reanudar();else seis.reanudar();pausa=false;juego.pausa=false;
    }
    public void perderFoco(){if(listaComun()&&!pausa)pausar();}
    public void pulsar(char ch,int k) {
        if(pantallaLan!=null&&pantallaLan.pulsar(ch,k))return;
        if(!pulsadas.add(k))return;
        if(k==VK_ESCAPE||k==VK_P){if(pausa){if(controles)controles=false;else reanudar();}else pausar();return;}
        if(pausa){
            if(k==VK_H){controles=!controles;return;}
            if(k==VK_UP)foco=Math.floorMod(foco-1,3);
            if(k==VK_DOWN||k==VK_TAB)foco=(foco+1)%3;
            if(k==VK_ENTER||k==VK_E||k==VK_SPACE)activar();return;
        }
        if(k==VK_H){pausar();controles=true;return;}
        if(cuatro!=null&&cuatro.resultado().completada()&&(k==VK_ENTER||k==VK_E||k==VK_SPACE)){salir=true;return;}
        boolean combate=cuatro!=null?cuatro.enCombate():seis.enCombate();
        var accion=combate?MapaControles.accionTeclado(numero,dosTeclados(),k):null;
        int destino=accion==null?k:MapaControles.codigoInterno(numero,dosTeclados(),accion);
        press(destino);
    }
    public void soltar(int k){if(pantallaLan!=null)pantallaLan.soltar(k);pulsadas.remove(k);if(!teclasMando.contains(k)&&!teclasSegundoMando.contains(k))release(k);}
    public boolean dosTeclados(){return seis!=null&&!jugandoLan&&redSeis==null&&seis.motor().modo==MisionSeisMotor.Modo.DUO;}
    public MandosJugadores.Contexto contextoMandos(){
        if(invitadoLan()||pantallaLan!=null&&pantallaLan.reconectando())return new MandosJugadores.Contexto(1,1);
        if(dosTeclados())return new MandosJugadores.Contexto(2,0);
        return MandosJugadores.Contexto.INDIVIDUAL;
    }

    public void segundoMando(EstadoMando estado){
        mandoDos.leer(estado,dosTeclados()&&listaComun()&&!pausa);
        if(mandoDos.pulso(Boton.MENU)){pausar();return;}
        Set<Integer> ahora=new HashSet<>();
        if(dosTeclados()&&seis.enCombate()){
            if(mandoDos.x()<-.4)ahora.add(VK_LEFT);
            if(mandoDos.x()>.4)ahora.add(VK_RIGHT);
            if(mandoDos.y()<-.4)ahora.add(VK_UP);
            if(mandoDos.y()>.4)ahora.add(VK_DOWN);
            var mando=mandoDos.estado();
            if(mando.pulsado(Boton.X)||mando.pulsado(Boton.RT))ahora.add(VK_L);
            if(mando.pulsado(Boton.Y)||mando.pulsado(Boton.LT))ahora.add(VK_K);
            if(mando.pulsado(Boton.RB))ahora.add(VK_SHIFT);
        }
        for(int k:new HashSet<>(teclasSegundoMando))if(!ahora.contains(k)){
            if(!pulsadas.contains(k))release(k);
            teclasSegundoMando.remove(k);
        }
        for(int k:ahora)if(teclasSegundoMando.add(k))press(k);
    }
    private void activar(){switch(foco){case 0->reanudar();case 1->controles=!controles;case 2->{limpiar();salir=true;}default->{}}}
    public void raton(int x,int y){
        if(pantallaLan!=null&&pantallaLan.raton(x,y))return;
        if(pausa){if(x>=380&&x<=900)for(int i=0;i<3;i++)if(y>=421+i*54&&y<463+i*54){foco=i;activar();break;}return;}
        float xx=x/1.25f, yy=y/1.25f;
        boolean guardado=cuatro!=null?cuatro.eligiendoGuardado():seis.eligiendoGuardado();
        if(guardado){
            if(xx>=205&&xx<=819&&yy>=260&&yy<320){if(cuatro!=null)cuatro.elegirGuardado(false);else seis.elegirGuardado(false);}
            else if(xx>=205&&xx<=819&&yy>=320&&yy<400){if(cuatro!=null)cuatro.elegirGuardado(true);else seis.elegirGuardado(true);}return;
        }
        if(cuatro!=null){
            if(cuatro.resultado().completada()&&yy>=380&&yy<=435){salir=true;return;}
            cuatro.raton(xx,yy);return;
        }
        if(seis!=null&&seis.motor().fase==MisionSeisMotor.Fase.ENTREVISTA&&xx>=65&&xx<=957&&yy>=442&&yy<=520){
            press(yy<480?VK_1:VK_2);release(yy<480?VK_1:VK_2);return;
        }
        if(seis!=null&&seis.motor().fase==MisionSeisMotor.Fase.MODO&&yy>=386&&yy<=436){
            if(xx>=116&&xx<=496){press(VK_1);release(VK_1);}else if(xx>=526&&xx<=906){press(VK_2);release(VK_2);}return;
        }
        if(seis!=null&&seis.motor().fase==MisionSeisMotor.Fase.DERROTA&&yy>=354&&yy<=407){press(VK_ENTER);release(VK_ENTER);}
    }
    private boolean nuevo(EstadoMando e,Boton b){return e.pulsado(b)&&!anterior.pulsado(b);}
    public void mando(EstadoMando e){
        if(cuatro!=null)cuatro.nombreMando(e.nombre());
        if(pantallaLan!=null&&pantallaLan.mando(e)){anterior=e;return;}
        if(anterior.conectado()&&!e.conectado())pausar();
        boolean neutral=Math.abs(e.izquierdaX())<.3&&Math.abs(e.izquierdaY())<.3&&e.botones().isEmpty();
        if(!e.conectado()){anterior=e;return;}
        if(nuevo(e,Boton.MENU)||nuevo(e,Boton.B)){
            if(pausa){if(controles)controles=false;else reanudar();}else pausar();anterior=e;return;
        }
        if(pausa){
            if(nuevo(e,Boton.VISTA))controles=!controles;
            if(nuevo(e,Boton.ARRIBA)||e.izquierdaY()<-.6&&anterior.izquierdaY()>=-.6)foco=Math.floorMod(foco-1,3);
            if(nuevo(e,Boton.ABAJO)||e.izquierdaY()>.6&&anterior.izquierdaY()<=.6)foco=(foco+1)%3;
            if(nuevo(e,Boton.A))activar();anterior=e;return;
        }
        if(centrarMando){if(neutral)centrarMando=false;anterior=e;return;}
        if(nuevo(e,Boton.VISTA)){pausar();controles=true;anterior=e;return;}
        boolean guardado=cuatro!=null?cuatro.eligiendoGuardado():seis.eligiendoGuardado();
        Set<Integer> ahora=new HashSet<>();
        if(e.izquierdaX()<-.4||e.pulsado(Boton.IZQUIERDA))ahora.add(VK_A);
        if(e.izquierdaX()>.4||e.pulsado(Boton.DERECHA))ahora.add(VK_D);
        if(e.izquierdaY()<-.4||e.pulsado(Boton.ARRIBA))ahora.add(guardado?VK_UP:VK_W);
        if(e.izquierdaY()>.4||e.pulsado(Boton.ABAJO))ahora.add(guardado?VK_DOWN:VK_S);
        for(var b:new Boton[]{Boton.X,Boton.RT,Boton.Y,Boton.LT,Boton.RB})if(e.pulsado(b)){
            var accion=MapaControles.accionMando(b);
            if(accion!=null)ahora.add(cuatro!=null&&cuatro.enCivita()&&(b==Boton.X||b==Boton.RT)?VK_J:
                MapaControles.codigoInterno(numero,dosTeclados(),accion));
        }
        if(cuatro!=null&&cuatro.enCivita()&&e.pulsado(Boton.LB))ahora.add(VK_F);
        for(int k:new HashSet<>(teclasMando))if(!ahora.contains(k)){if(!pulsadas.contains(k))release(k);teclasMando.remove(k);}
        for(int k:ahora)if(teclasMando.add(k))press(k);
        if(nuevo(e,Boton.A)){
            if(cuatro!=null&&cuatro.resultado().completada())salir=true;
            else {press(VK_ENTER);release(VK_ENTER);}
        }
        anterior=e;
    }
    public void dibujar(){
        if(app==null)return;
        if(pantallaLan!=null&&pantallaLan.activa()){
            pantallaLan.dibujar(app,mensajeLan,conectandoLan,redSeis!=null&&redSeis.anfitrion(),2,ocupadosLan);return;
        }
        app.image(cuatro!=null?cuatro.dibujarEnJuego():seis.dibujarEnJuego(),0,0);
        if(!pausa||gestionExterna)return;
        app.noStroke();app.fill(13,8,24,225);app.rect(0,0,1280,720);
        MarcosProcessing.marco(app.g,340,118,600,482,0xff38264b,0xfff4cb6a);
        app.textAlign(PApplet.LEFT,PApplet.BASELINE);app.fill(244,203,106);app.textSize(38);app.text("PAUSA",380,171);
        app.fill(245,232,251);app.textSize(18);app.text(numero==4?"Misión 4 · Juan en campaña":"Misión 6 · Cielos de campaña",380,205);
        String[] lineas=controles?(numero==4?new String[]{"WASD / flechas: mover · Enter/E: interactuar","En Civita: F linterna · J espada · Q escudo · Espacio esquivar","Mando: A interactuar · LB linterna · X espada","Tarima: flechas y Enter o S/N para responder"}:
            new String[]{"Solo: WASD/flechas · F disparo · Q misil · Espacio impulso","Dos: J1 WASD + F/Q + Espacio · J2 flechas + L/K + Mayús","Mando: J1 · X/RT disparo · Y/LT misil · RB impulso","Toca corazones para curarte. Misil desde fase 2."}):
            new String[]{"El tiempo y las acciones están detenidos.","H / Vista: controles y ayuda de la misión.","Guardar y salir conserva el punto alcanzado.","Suelta teclas y centra el stick para reanudar."};
        app.textSize(16);for(int i=0;i<lineas.length;i++)app.text(lineas[i],380,255+i*30);
        String[] ops={"Continuar",controles?"Ocultar controles":"Ver controles y ayuda","Guardar y salir al selector"};
        for(int i=0;i<3;i++){MarcosProcessing.marco(app.g,380,421+i*54,520,42,foco==i?0xfff4cb6a:0xff432b5b,0xffb58efa);
            app.fill(foco==i?0xff21152f:0xfff5e8fb);app.textSize(18);app.text(ops[i],400,450+i*54);}
    }
    public void prepararRevision(String fase){if(cuatro!=null)cuatro.prepararRevision(fase);else seis.prepararRevision(fase);}
    public void terminar(){
        if(terminada)return;terminada=true;
        if(cuatro!=null)juego.terminarMisionCuatro(cuatro.resultado());
        else if(jugandoLocal||jugandoLan)juego.terminarMisionSeis(seis.resultado());
        else juego.abrir(JuegoCliente.Pantalla.MISIONES);
    }
    @Override public synchronized void close(){
        cerrada=true;
        if(cuatro!=null)cuatro.cerrar();
        else{
            pantallaLan.close();
            if(enlacePendiente!=null)enlacePendiente.close();
            if(redSeis!=null)redSeis.close();
            seis.cerrar(jugandoLocal);
        }
        juego.pausa=false;
    }
}
