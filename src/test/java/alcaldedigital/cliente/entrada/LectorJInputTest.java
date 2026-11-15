package alcaldedigital.cliente.entrada;

import java.lang.reflect.Proxy;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import org.junit.jupiter.api.Test;
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
}
