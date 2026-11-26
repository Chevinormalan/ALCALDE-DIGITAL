package alcaldedigital.cliente.entrada;

import java.lang.reflect.Proxy;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import static alcaldedigital.cliente.entrada.EstadoMando.Boton;
import static org.junit.jupiter.api.Assertions.*;

class LectorJInputTest {
    private Component componente(Component.Identifier id,float valor) {
        return (Component)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Component.class},
            (proxy,metodo,args)->switch(metodo.getName()) {
                case "getIdentifier" -> id;
                case "getPollData" -> valor;
                default -> throw new UnsupportedOperationException(metodo.getName());
            });
    }

    private Controller mando(String nombre,Component... componentes) {
        return (Controller)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Controller.class},
            (proxy,metodo,args)->switch(metodo.getName()) {
                case "getName" -> nombre;
                case "getComponents" -> componentes;
                default -> throw new UnsupportedOperationException(metodo.getName());
            });
    }

    @Test void traduceMandoXboxConZonaMuertaCrucetaYDisparadores() {
        var estado=LectorJInput.convertir(mando("Xbox Controller",
            componente(Component.Identifier.Axis.X,.1f),
            componente(Component.Identifier.Axis.Y,-.6f),
            componente(Component.Identifier.Axis.RX,.8f),
            componente(Component.Identifier.Axis.Z,.9f),
            componente(Component.Identifier.Axis.RZ,.95f),
            componente(Component.Identifier.Axis.POV,Component.POV.UP_RIGHT),
            componente(Component.Identifier.Button._0,1),
            componente(Component.Identifier.Button._6,1),
            componente(Component.Identifier.Button._7,1),
            componente(Component.Identifier.Button._8,1)));
        assertEquals(0,estado.izquierdaX());
        assertTrue(estado.izquierdaY()<0);
        assertTrue(estado.derechaX()>0);
        for(var boton:new Boton[]{Boton.A,Boton.ARRIBA,Boton.DERECHA,Boton.LT,Boton.RT,Boton.VISTA,Boton.MENU})
            assertTrue(estado.pulsado(boton),boton.name());
        assertFalse(estado.pulsado(Boton.X));
    }

    @Test void traduceIndicesPlayStationSinConfundirGatillosConMenu() {
        var estado=LectorJInput.convertir(mando("Sony DualSense Wireless Controller",
            componente(Component.Identifier.Button._0,1),
            componente(Component.Identifier.Button._1,1),
            componente(Component.Identifier.Button._6,1),
            componente(Component.Identifier.Button._7,1),
            componente(Component.Identifier.Button._8,1),
            componente(Component.Identifier.Button._9,1)));
        for(var boton:new Boton[]{Boton.A,Boton.X,Boton.LT,Boton.RT,Boton.VISTA,Boton.MENU})
            assertTrue(estado.pulsado(boton),boton.name());
        assertFalse(estado.pulsado(Boton.B));
    }

    @Test @EnabledOnOs(OS.LINUX)
    void traduceBotonesLinuxDePlayStationPorPosicion() {
        String nombre="Sony DualSense Wireless Controller";
        assertEquals(Boton.A,unBoton(nombre,Component.Identifier.Button.A));
        assertEquals(Boton.B,unBoton(nombre,Component.Identifier.Button.B));
        assertEquals(Boton.Y,unBoton(nombre,Component.Identifier.Button.X));
        assertEquals(Boton.X,unBoton(nombre,Component.Identifier.Button.Y));
        assertEquals(Boton.LB,unBoton(nombre,Component.Identifier.Button.LEFT_THUMB));
        assertEquals(Boton.RB,unBoton(nombre,Component.Identifier.Button.RIGHT_THUMB));
        assertEquals(Boton.LT,unBoton(nombre,Component.Identifier.Button.LEFT_THUMB2));
        assertEquals(Boton.RT,unBoton(nombre,Component.Identifier.Button.RIGHT_THUMB2));
        assertEquals(Boton.MENU,unBoton(nombre,Component.Identifier.Button.START));
        assertEquals(Boton.VISTA,unBoton(nombre,Component.Identifier.Button.SELECT));
    }

    @Test void xboxWirelessNoUsaElPerfilDePlayStation() {
        String nombre="Xbox Wireless Controller";
        assertFalse(LectorJInput.esPlayStation(nombre));
        assertEquals(Boton.A,unBoton(nombre,Component.Identifier.Button._0));
        assertEquals(Boton.X,unBoton(nombre,Component.Identifier.Button.X));
        assertEquals(Boton.Y,unBoton(nombre,Component.Identifier.Button.Y));
        assertEquals(Boton.LB,unBoton(nombre,Component.Identifier.Button.LEFT_THUMB));
        assertEquals(Boton.RT,unBoton(nombre,Component.Identifier.Button.RIGHT_THUMB2));
    }

    private Boton unBoton(String nombre,Component.Identifier.Button identificador) {
        var estado=LectorJInput.convertir(mando(nombre,componente(identificador,1)));
        assertEquals(1,estado.botones().size(),identificador.getName());
        return estado.botones().iterator().next();
    }
}
