package alcaldedigital.cliente.entrada;

import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import static alcaldedigital.cliente.entrada.EstadoMando.Boton.A;
import static org.junit.jupiter.api.Assertions.*;

class MandosJugadoresTest {
    private EstadoMando mando(String nombre, EstadoMando.Boton... botones) {
        return new EstadoMando(nombre,0,0,0,0,Set.of(botones));
    }

    @Test void detectaSinRepetirYAsignaElSegundoAlElegirCooperativo() {
        var mandos = new MandosJugadores();
        var dispositivos = new LinkedHashMap<String,EstadoMando>();
        dispositivos.put("usb-1", mando("DualSense", A));
        dispositivos.put("usb-2", mando("Xbox"));
        mandos.actualizar(dispositivos, MandosJugadores.Contexto.INDIVIDUAL, 0);
        assertEquals("Mando detectado · Jugador 1",mandos.avisos(0).getFirst().texto());
        assertEquals("Mando detectado · disponible",mandos.avisos(0).getLast().texto());
        assertFalse(mandos.estado(0).pulsado(A), "Conectar con ✕ pulsado no confirma el menú");
        assertFalse(mandos.estado(1).conectado());
        dispositivos.put("usb-1",mando("DualSense"));
        mandos.actualizar(dispositivos,new MandosJugadores.Contexto(2,0),100);
        assertEquals("Xbox",mandos.estado(1).nombre());
        assertEquals("Mando asignado · Jugador 2",mandos.avisos(100).getLast().texto());
        mandos.actualizar(dispositivos,new MandosJugadores.Contexto(2,0),4_000_000_000L);
        assertTrue(mandos.avisos(4_000_000_000L).isEmpty());
        dispositivos.put("usb-1",mando("DualSense", A));
        mandos.actualizar(dispositivos,new MandosJugadores.Contexto(2,0),4_000_000_001L);
        assertTrue(mandos.estado(0).pulsado(A));
    }

    @Test void mandaAvisoDePuestoLanYConservaIdentidadesAunqueTenganElMismoNombre() {
        var mandos = new MandosJugadores();
        var locales = new MandosJugadores.Contexto(2,0);
        var dispositivos = new LinkedHashMap<String,EstadoMando>();
        dispositivos.put("usb-1",mando("DualSense"));
        dispositivos.put("usb-2",mando("DualSense"));
        mandos.actualizar(dispositivos,locales,0);
        mandos.actualizar(Map.of("usb-2",mando("DualSense", A)),locales,100);
        assertFalse(mandos.estado(0).conectado());
        assertTrue(mandos.estado(1).pulsado(A));
        assertEquals("Mando desconectado · Jugador 1", mandos.avisos(100).getFirst().texto());
        mandos.actualizar(dispositivos,locales,200);
        assertEquals("Mando detectado · Jugador 1", mandos.avisos(200).getFirst().texto());
        mandos.actualizar(dispositivos,new MandosJugadores.Contexto(1,2),300);
        assertEquals("Mando asignado · Jugador 3",mandos.avisos(300).getFirst().texto());
        assertTrue(mandos.estado(0).conectado());
        assertFalse(mandos.estado(1).conectado());
    }

    @Test void descartaSensoresQueNoTienenBotonesDeMando() {
        assertTrue(DispositivosMandos.tieneBotonesDeJuego("7fdb000000000000 0 0 0 0"));
        assertTrue(DispositivosMandos.tieneBotonesDeJuego("100000000 0 0 0 0"));
        assertFalse(DispositivosMandos.tieneBotonesDeJuego("0"));
        assertFalse(DispositivosMandos.tieneBotonesDeJuego("70000 0 0 0 0"));
    }
}
