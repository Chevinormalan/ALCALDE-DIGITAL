package alcaldedigital.cliente.misiones;

import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.entrada.MapaControles;
import alcaldedigital.cliente.interfaz.MarcosProcessing;
import alcaldedigital.cliente.mision4.MisionCuatroSketch;
import alcaldedigital.cliente.mision6.MisionSeisSketch;
import alcaldedigital.cliente.mision6.MisionSeisMotor;
import processing.core.*;
import java.util.*;
import static java.awt.event.KeyEvent.*;
import static alcaldedigital.cliente.entrada.EstadoMando.Boton;

/** Adaptación de los capítulos recibidos al reloj, pausa, entrada y campaña del anfitrión. */
public final class VistaCampana implements AutoCloseable {
    private boolean gestionExterna;
    public void gestionExterna(boolean v){gestionExterna=v;if(cuatro!=null)cuatro.gestionExterna(v);else seis.gestionExterna(v);}
    public void elegirPartida(boolean continuar){if(cuatro!=null&&cuatro.eligiendoGuardado())cuatro.elegirGuardado(!continuar);if(seis!=null&&seis.eligiendoGuardado())seis.elegirGuardado(!continuar);}
    public void pausarComun(){pausar();}
    public void reanudarComun(){reanudar();}
    public String guardarComun()throws Exception{if(cuatro!=null)cuatro.guardarAhora();else seis.guardarAhora();return "Partida guardada.";}
    private final int numero;
    private final JuegoCliente juego;
    private final MisionCuatroSketch cuatro;
    private final MisionSeisSketch seis;
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
    }
    public void bloquearTeclas(Collection<Integer> retenidas){pulsadas.addAll(retenidas);}
    public void iniciarEn(PApplet app) {this.app=app;if(cuatro!=null)cuatro.iniciarEn(app);else seis.iniciarEn(app);}
    public void actualizar(float dt) {
        if(pausa||salir)return;
        if(cuatro!=null)cuatro.actualizar(dt);else seis.actualizar(dt);
        if(seis!=null&&seis.salidaSolicitada())salir=true;
    }
    public boolean victoriaComun(){return cuatro!=null?cuatro.resultado().completada():seis.resultado().completada();}
    public alcaldedigital.compartido.guardado.TablaPuntajes.Fila filaFinal(){
        if(cuatro!=null)return cuatro.resultado().completada()?PuntajesMision.cuatro(cuatro.resultado()):null;
        return filaSeis(seis.motor());
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
    private void press(int k){if(cuatro!=null)cuatro.pulsarTecla((char)k,k);else seis.pulsarTecla((char)k,k);}
    private void release(int k){if(cuatro!=null)cuatro.soltarTecla(k);else seis.soltarTecla(k);}
    private void limpiar(){if(cuatro!=null)cuatro.perderFoco();else seis.perderFoco();teclasMando.clear();centrarMando=true;}
    private void pausar(){limpiar();pausa=true;juego.pausa=true;controles=false;foco=0;}
    private void reanudar(){limpiar();if(cuatro!=null)cuatro.reanudar();else seis.reanudar();pausa=false;juego.pausa=false;}
    public void perderFoco(){if(!pausa)pausar();}
    public void pulsar(char ch,int k) {
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
    public void soltar(int k){pulsadas.remove(k);if(!teclasMando.contains(k))release(k);}
    public boolean dosTeclados(){return seis!=null&&seis.motor().modo==MisionSeisMotor.Modo.DUO;}
    private void activar(){switch(foco){case 0->reanudar();case 1->controles=!controles;case 2->{limpiar();salir=true;}default->{}}}
    public void raton(int x,int y){
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
            if(accion!=null)ahora.add(MapaControles.codigoInterno(numero,dosTeclados(),accion));
        }
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
        app.image(cuatro!=null?cuatro.dibujarEnJuego():seis.dibujarEnJuego(),0,0);
        if(!pausa||gestionExterna)return;
        app.noStroke();app.fill(13,8,24,225);app.rect(0,0,1280,720);
        MarcosProcessing.marco(app.g,340,118,600,482,0xff38264b,0xfff4cb6a);
        app.textAlign(PApplet.LEFT,PApplet.BASELINE);app.fill(244,203,106);app.textSize(38);app.text("PAUSA",380,171);
        app.fill(245,232,251);app.textSize(18);app.text(numero==4?"Misión 4 · Juan en campaña":"Misión 6 · Cielos de campaña",380,205);
        String[] lineas=controles?(numero==4?new String[]{"WASD / flechas: mover · Enter/E: hablar","F: ataque · Q: escudo · Espacio: esquivar","Mando: A hablar · X atacar · LT escudo · RB esquivar","Rumores: Izq./Der. y Enter · V verificar / N responder"}:
            new String[]{"Solo: WASD/flechas · F disparo · Q misil · Espacio impulso","Dos: J1 WASD + F/Q + Espacio · J2 flechas + L/K + Mayús","Mando: J1 · X/RT disparo · Y/LT misil · RB impulso","Toca corazones para curarte. Misil desde fase 2."}):
            new String[]{"El tiempo y las acciones están detenidos.","H / Vista: controles y ayuda de la misión.","Guardar y salir conserva el punto alcanzado.","Suelta teclas y centra el stick para reanudar."};
        app.textSize(16);for(int i=0;i<lineas.length;i++)app.text(lineas[i],380,255+i*30);
        String[] ops={"Continuar",controles?"Ocultar controles":"Ver controles y ayuda","Guardar y salir al selector"};
        for(int i=0;i<3;i++){MarcosProcessing.marco(app.g,380,421+i*54,520,42,foco==i?0xfff4cb6a:0xff432b5b,0xffb58efa);
            app.fill(foco==i?0xff21152f:0xfff5e8fb);app.textSize(18);app.text(ops[i],400,450+i*54);}
    }
    public void prepararRevision(String fase){if(cuatro!=null)cuatro.prepararRevision(fase);else seis.prepararRevision(fase);}
    public void terminar(){if(terminada)return;terminada=true;if(cuatro!=null)juego.terminarMisionCuatro(cuatro.resultado());else juego.terminarMisionSeis(seis.resultado());}
    @Override public void close(){if(cuatro!=null)cuatro.cerrar();else seis.cerrar();juego.pausa=false;}
}
