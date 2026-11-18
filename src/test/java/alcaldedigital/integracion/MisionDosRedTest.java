package alcaldedigital.integracion;

import alcaldedigital.cliente.ClienteSketch;
import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.cliente.mision2.MisionDosSketch;
import alcaldedigital.cliente.mision2.RedCivitas;
import alcaldedigital.cliente.mision2.RegistroIntentosMisionDos;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.compartido.modelo.ProgresoCampana;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class MisionDosRedTest {
    @TempDir Path carpetaTemporal;
    @Test void unAtaqueReabreElEnlaceYUnaReparacionVuelveAAislarlo() {
        RedCivitas red=new RedCivitas();
        assertEquals(6,red.protegidas());assertEquals(100,red.progreso());
        assertEquals(0,red.comprometidasAlcanzables());
        assertTrue(red.ponerEnPeligro(4));
        assertFalse(red.ponerEnPeligro(4));
        assertEquals(5,red.protegidas());
        assertEquals(1,red.comprometidasAlcanzables());
        assertTrue(red.proteger(4));
        assertEquals(6,red.protegidas());
        assertEquals(0,red.comprometidasAlcanzables());
        assertFalse(red.proteger(4));
        assertTrue(red.ponerEnPeligro(4));
        assertTrue(red.proteger(4));
        assertTrue(red.completa());assertEquals(100,red.progreso());
    }

    @Test void resultadoAfectaConfianzaYDesinformacionSinInventarVerificacion() {
        EstadoCiudad base=EstadoCiudad.inicial();
        EstadoCiudad exito=ProgresoCampana.aplicarMisionDos(base,6);
        EstadoCiudad fallo=ProgresoCampana.aplicarMisionDos(base,0);
        assertEquals(base.informacionVerificada(),exito.informacionVerificada());
        assertTrue(exito.confianza()>base.confianza());
        assertTrue(exito.desinformacion()<base.desinformacion());
        assertTrue(fallo.confianza()<base.confianza());
        assertTrue(fallo.desinformacion()>base.desinformacion());
    }

    @Test void selectorPausaYResultadoParcialConservanElFlujoDelJuego() {
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new,
            new RegistroIntentosMisionDos(carpetaTemporal.resolve("intento.flag")),()->.9);
        var sketch=new ClienteSketch(juego);sketch.width=960;sketch.height=600;
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(juego,2);
        assertEquals(JuegoCliente.Pantalla.DIFICULTAD_DOS,juego.pantalla);
        var render=new RenderJuego();render.dibujar(juego);
        assertEquals(3,render.botones().stream().filter(b->b.accion().startsWith("m2dificultad:")).count());
        juego.accion("m2dificultad:DIFICIL");
        assertEquals(JuegoCliente.Pantalla.MISION_DOS,juego.pantalla);
        assertEquals(MisionDosSketch.Dificultad.DIFICIL,juego.dificultadMisionDos);
        juego.accion("pausa");
        assertTrue(juego.pausa);
        sketch.mouseX=338;sketch.mouseY=442;sketch.mousePressed();
        assertEquals(JuegoCliente.Pantalla.MENU,juego.pantalla);
        assertFalse(juego.pausa);

        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(juego,2);juego.accion("m2dificultad:NORMAL");
        var antes=juego.estadoCampana;
        juego.terminarMisionDos(new MisionDosSketch.Resultado(true,false,2,0));
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        assertFalse(juego.ultimoResultadoMisionDos.exito());
        assertEquals(2,juego.ultimoResultadoMisionDos.protegidos());
        assertTrue(juego.estadoCampana.confianza()<antes.confianza());
        assertTrue(juego.estadoCampana.desinformacion()>antes.desinformacion());
    }

    @Test void falloCriticoSoloPuedeSalirEnElPrimerDificilYAtraviesaUnReinicio() {
        Path marca=carpetaTemporal.resolve("primer-dificil.flag");
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new,
            new RegistroIntentosMisionDos(marca),()->.1);
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(juego);juego.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(juego,2);juego.accion("m2dificultad:NORMAL");
        assertFalse(juego.falloCriticoMisionDos);
        assertFalse(java.nio.file.Files.exists(marca));
        juego.terminarMisionDos(new MisionDosSketch.Resultado(true,true,6,0));
        alcaldedigital.pruebas.PartidaPrueba.elegir(juego,2);juego.accion("m2dificultad:DIFICIL");
        assertTrue(juego.falloCriticoMisionDos);
        assertTrue(java.nio.file.Files.exists(marca));
        var antes=juego.estadoCampana;
        juego.terminarMisionDos(new MisionDosSketch.Resultado(true,false,3,38,true));
        assertEquals(antes,juego.estadoCampana);
        alcaldedigital.pruebas.PartidaPrueba.elegir(juego,2);juego.accion("m2dificultad:DIFICIL");
        assertFalse(juego.falloCriticoMisionDos);

        var reiniciado=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new,
            new RegistroIntentosMisionDos(marca),()->.1);
        alcaldedigital.pruebas.PartidaPrueba.desbloquear(reiniciado);reiniciado.accion("jugar");alcaldedigital.pruebas.PartidaPrueba.elegir(reiniciado,2);
        reiniciado.accion("m2dificultad:DIFICIL");
        assertFalse(reiniciado.falloCriticoMisionDos);
    }

    @Test void primerSorteoDificilSinAveriaTambienSeConsume() {
        Path marca=carpetaTemporal.resolve("sorteo-ganable.flag");
        var intentos=new RegistroIntentosMisionDos(marca);
        assertFalse(intentos.sortearPrimerDificil(.30));
        assertTrue(java.nio.file.Files.exists(marca));
        assertFalse(new RegistroIntentosMisionDos(marca).sortearPrimerDificil(.01));
    }
}
