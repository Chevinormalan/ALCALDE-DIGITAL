package alcaldedigital.cliente.entrada;

import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.cliente.mision3.MisionTresSketch;
import alcaldedigital.cliente.mision2.MisionDosSketch;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import static alcaldedigital.cliente.entrada.EstadoMando.Boton;

/** Traduce estados físicos a acciones del juego, una sola vez por pulsación. */
public final class ControladorMando {
    private alcaldedigital.cliente.misiones.VistaCampana misionCampana;
    public void misionCampana(alcaldedigital.cliente.misiones.VistaCampana m){misionCampana=m;}
    private alcaldedigital.cliente.mision5.MisionCincoVista misionCinco;
    private alcaldedigital.cliente.mision7.MisionSieteVista misionSiete;
    public void misionCinco(alcaldedigital.cliente.mision5.MisionCincoVista m){misionCinco=m;}
    public void misionSiete(alcaldedigital.cliente.mision7.MisionSieteVista m){misionSiete=m;}
    private EstadoMando anterior=EstadoMando.desconectado();
    private int direccionAnterior;
    private long proximaRepeticion;

    public void actualizar(EstadoMando estado,JuegoCliente juego,RenderJuego render,
                           MisionTresSketch misionTres,Runnable salirMisionTres) {
        actualizar(estado,juego,render,null,misionTres,salirMisionTres);
    }

    public void actualizar(EstadoMando estado,JuegoCliente juego,RenderJuego render,
                           MisionDosSketch misionDos,MisionTresSketch misionTres,Runnable salirMisionActual) {
        juego.mandoActivo=estado.conectado();juego.nombreMando=estado.nombre();
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_CUATRO||juego.pantalla==JuegoCliente.Pantalla.MISION_SEIS){
            if(misionCampana!=null)misionCampana.mando(estado);anterior=estado;return;
        }
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_SIETE){if(misionSiete!=null)misionSiete.mando(estado);anterior=estado;return;}
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_CINCO){if(misionCinco!=null)misionCinco.mando(estado);anterior=estado;return;}
        boolean seDesconecto=anterior.conectado()&&!estado.conectado();
        juego.mandoActivo=estado.conectado();juego.nombreMando=estado.nombre();
        if(seDesconecto&&(juego.misionUno!=null||juego.pantalla==JuegoCliente.Pantalla.MISION_DOS||
            juego.pantalla==JuegoCliente.Pantalla.MISION_TRES)&&!juego.pausa)
            juego.accion("pausa");
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS&&misionDos!=null)
            misionDos.estadoMando(estado);
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_TRES&&misionTres!=null)
            misionTres.estadoMando(estado);
        juego.movimientoMandoX=juego.movimientoMandoY=0;
        if(!estado.conectado()) {anterior=estado;direccionAnterior=0;return;}

        if((juego.pantalla==JuegoCliente.Pantalla.GRAFICOS||juego.pantalla==JuegoCliente.Pantalla.AUDIO)){
            if(nuevo(estado,Boton.B)||nuevo(estado,Boton.VISTA))juego.accion("volver");
            else if(nuevo(estado,Boton.A))render.activarFoco(juego);
            else navegar(estado,juego,render);
            anterior=estado;return;
        }

        if(juego.pausa) {
            if(nuevo(estado,Boton.MENU)||nuevo(estado,Boton.B)) juego.accion("reanudar");
            else if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS||juego.pantalla==JuegoCliente.Pantalla.MISION_TRES) {
                int vertical=estado.pulsado(Boton.ARRIBA)||estado.izquierdaY()<-.65f?-1:
                    estado.pulsado(Boton.ABAJO)||estado.izquierdaY()>.65f?1:0;
                long ahora=System.nanoTime();
                if(vertical!=0&&(vertical!=direccionAnterior||ahora>=proximaRepeticion)) {
                    juego.foco=Math.floorMod(juego.foco+vertical,3);
                    proximaRepeticion=ahora+(vertical!=direccionAnterior?300_000_000L:130_000_000L);
                }
                direccionAnterior=vertical;
                if(nuevo(estado,Boton.A)) activarPausa(juego,salirMisionActual);
            } else {
                navegar(estado,juego,render);
                if(nuevo(estado,Boton.A)) render.activarFoco(juego);
            }
            anterior=estado;return;
        }

        if(juego.tecladoNumericoActivo) {
            if(nuevo(estado,Boton.B)) juego.accion("cerrarTeclado");
            else if(nuevo(estado,Boton.X)) juego.accion("borrarDigito");
            else if(nuevo(estado,Boton.A)) render.activarFoco(juego);
            else navegar(estado,juego,render);
            anterior=estado;return;
        }

        if(juego.pantalla==JuegoCliente.Pantalla.MISION_DOS) {
            if(nuevo(estado,Boton.MENU)||nuevo(estado,Boton.B)) juego.accion("pausa");
            else if(nuevo(estado,Boton.VISTA)) {juego.accion("pausa");juego.controlesPausa=true;}
            else if(misionDos!=null&&nuevo(estado,Boton.A)) misionDos.accionMando(Boton.A);
            else if(misionDos!=null&&nuevo(estado,Boton.X)) misionDos.accionMando(Boton.X);
            anterior=estado;return;
        }
        if(juego.pantalla==JuegoCliente.Pantalla.MISION_TRES) {
            if(nuevo(estado,Boton.MENU)||nuevo(estado,Boton.B)) juego.accion("pausa");
            else if(nuevo(estado,Boton.VISTA)) {juego.accion("pausa");juego.controlesPausa=true;}
            else if(misionTres!=null) for(Boton boton:new Boton[]{Boton.A,Boton.X,Boton.Y,Boton.RB,Boton.LT,Boton.RT})
                if(nuevo(estado,boton)) misionTres.accionMando(boton);
            anterior=estado;return;
        }

        boolean parque=juego.pantalla==JuegoCliente.Pantalla.PARQUE;
        if(parque&&juego.misionUno!=null&&juego.misionUno.vista().fase()==SesionMisionUno.Fase.CRISIS) {
            float x=estado.izquierdaX(),y=estado.izquierdaY();
            if(estado.pulsado(Boton.IZQUIERDA)) x=-1;
            if(estado.pulsado(Boton.DERECHA)) x=1;
            if(estado.pulsado(Boton.ARRIBA)) y=-1;
            if(estado.pulsado(Boton.ABAJO)) y=1;
            juego.movimientoMandoX=x;juego.movimientoMandoY=y;
        }
        if(nuevo(estado,Boton.MENU)) juego.accion("pausa");
        else if(nuevo(estado,Boton.B)) {
            if(parque&&juego.misionUno!=null) juego.accion("pausa");
            else juego.accion("volver");
        } else if(nuevo(estado,Boton.VISTA)) {
            juego.accion(juego.pantalla==JuegoCliente.Pantalla.AYUDA?"volver":"ayuda");
        } else if(nuevo(estado,Boton.X)&&juego.misionUno!=null&&parque) {
            if(juego.misionUno.vista().fase()!=SesionMisionUno.Fase.INTRO) juego.accion("civitas");
        } else if(nuevo(estado,Boton.Y)&&juego.misionUno!=null) juego.accion("arboles");
        else if(nuevo(estado,Boton.A)) {
            if(parque&&juego.misionUno!=null) juego.accion(juego.misionUno.vista().fase()==SesionMisionUno.Fase.INTRO?"continuar":"interactuar");
            else render.activarFoco(juego);
        } else if(nuevo(estado,Boton.LB)) render.moverFocoDireccion(juego,0,-1);
        else if(nuevo(estado,Boton.RB)) render.moverFocoDireccion(juego,0,1);
        else if(!parque||juego.misionUno==null) navegar(estado,juego,render);
        anterior=estado;
    }

    private void activarPausa(JuegoCliente juego,Runnable salirMisionTres) {
        switch(juego.foco) {
            case 0 -> juego.accion("reanudar");
            case 1 -> juego.accion("controlesPausa");
            case 2 -> salirMisionTres.run();
            default -> { }
        }
    }

    private void navegar(EstadoMando estado,JuegoCliente juego,RenderJuego render) {
        int dx=estado.pulsado(Boton.IZQUIERDA)?-1:estado.pulsado(Boton.DERECHA)?1:
            estado.izquierdaX()<-.65f?-1:estado.izquierdaX()>.65f?1:0;
        int dy=estado.pulsado(Boton.ARRIBA)?-1:estado.pulsado(Boton.ABAJO)?1:
            estado.izquierdaY()<-.65f?-1:estado.izquierdaY()>.65f?1:0;
        int direccion=Math.abs(estado.izquierdaY())>Math.abs(estado.izquierdaX())&&dy!=0?
            (dy<0?1:2):dx!=0?(dx<0?3:4):dy!=0?(dy<0?1:2):0;
        long ahora=System.nanoTime();
        if(direccion!=0&&(direccion!=direccionAnterior||ahora>=proximaRepeticion)) {
            switch(direccion) {
                case 1 -> render.moverFocoDireccion(juego,0,-1);
                case 2 -> render.moverFocoDireccion(juego,0,1);
                case 3 -> render.moverFocoDireccion(juego,-1,0);
                case 4 -> render.moverFocoDireccion(juego,1,0);
                default -> { }
            }
            proximaRepeticion=ahora+(direccion!=direccionAnterior?300_000_000L:130_000_000L);
        }
        direccionAnterior=direccion;
    }

    private boolean nuevo(EstadoMando actual,Boton boton) {
        return actual.pulsado(boton)&&!anterior.pulsado(boton);
    }
}
