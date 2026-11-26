package alcaldedigital.integracion;

import alcaldedigital.cliente.mision5.ClienteBatalla;
import alcaldedigital.cliente.mision7.ClienteCarrera;
import alcaldedigital.cliente.mision8.*;
import alcaldedigital.compartido.batalla.EstadoBatalla;
import alcaldedigital.compartido.carrera.EstadoCarrera;
import alcaldedigital.compartido.carrera.EstadoElectoral;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.pruebas.PerfilAislado;
import alcaldedigital.servidor.batalla.*;
import alcaldedigital.servidor.carrera.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static alcaldedigital.pruebas.RedLanPrueba.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(PerfilAislado.class)
class VestibuloLanTest {
    private static InetAddress todas() throws Exception { return InetAddress.getByName("0.0.0.0"); }

    @Test void batallaYCarreraSoloAparecenEnSuMisionYComienzanAlLlenarse() throws Exception {
        var ciudad = EstadoCiudad.inicial();
        var carrera = new MotorCarrera(4, 3, EstadoCarrera.Dificultad.DIFICIL, ciudad, EstadoElectoral.inicial(ciudad), false);
        try (var servidor5 = new ServidorBatalla(todas(), 0, new MotorBatalla(123, false), null, true);
             var servidor7 = new ServidorCarrera(todas(), 0, carrera, null, null, true);
             var j1m5 = new ClienteBatalla("127.0.0.1", servidor5.puerto(), 0, "Ciudadano");
             var j1m7 = new ClienteCarrera("127.0.0.1", servidor7.puerto(), 1);
             var buscar5 = buscar(5);
             var buscar7 = buscar(7)) {
            servidor5.anunciar();
            servidor7.anunciar();
            var sala5 = sala(buscar5, servidor5.puerto(), 1);
            var sala7 = sala(buscar7, servidor7.puerto(), 1);
            assertEquals(1, buscar5.salas().size());
            assertEquals(1, buscar7.salas().size());
            assertTrue(sala5.libre(1));
            assertTrue(sala7.libre(0));
            assertFalse(sala7.libre(1));
            assertEquals("Difícil", sala7.cupos().detalle());
            assertEquals(EstadoBatalla.Fase.ESPERA, j1m5.vista().fase());
            assertEquals(EstadoCarrera.Fase.ESPERA, j1m7.vista().fase());
            try (var j2m5 = new ClienteBatalla(sala5.ip(), sala5.puerto(), 1, "Periodista");
                 var j2m7 = new ClienteCarrera(sala7.ip(), sala7.puerto(), 0)) {
                esperar("Misión 5 no arrancó automáticamente", () -> j1m5.vista().fase() == EstadoBatalla.Fase.INTRO);
                esperar("Misión 7 no arrancó automáticamente", () -> j1m7.vista().fase() == EstadoCarrera.Fase.ANUNCIO);
                assertEquals(j1m5.vista().id(), j2m5.vista().id());
                assertEquals(j1m7.vista().id(), j2m7.vista().id());
                assertThrows(IOException.class, () -> new ClienteCarrera(sala7.ip(), sala7.puerto(), 0));
            }
            esperar("Misión 5 siguió anunciándose tras comenzar", () -> buscar5.salas().isEmpty());
            esperar("Misión 7 siguió anunciándose tras comenzar", () -> buscar7.salas().isEmpty());
            esperar("La carrera no pausó tras desconexión", () -> j1m7.vista().pausa());
            try (var vuelta = new ClienteCarrera(sala7.ip(), sala7.puerto(), 0)) {
                vuelta.accion(0, "REANUDAR", 0);
                esperar("No se reanudó la carrera tras reconectar", () -> !j1m7.vista().pausa());
            }
        }
    }

    @ParameterizedTest @ValueSource(ints = {2, 3, 4})
    void misionOchoAnunciaCupoYEsperaAlUltimoPuesto(int total) throws Exception {
        var ciudad = EstadoCiudad.inicial();
        var motor = new MotorOcho(new AntecedentesOcho(ciudad, EstadoElectoral.inicial(ciudad), Map.of(), List.of()),
                total, 1, 0, total == 4, 42);
        try (var servidor = RedOcho.alojar(motor, 0, true); var buscar = buscar(8)) {
            servidor.anunciar();
            var sala = sala(buscar, servidor.puerto(), 1);
            assertEquals(total, sala.cupos().total());
            assertEquals("Normal · " + (total == 4 ? "3 contra 1" : "Cooperativo"), sala.cupos().detalle());
            assertTrue(motor.pausa);
            servidor.orden(0, "RESUME");
            assertTrue(motor.pausa);
            List<RedOcho> invitados = new ArrayList<>();
            try {
                for (int puesto = 1; puesto < total; puesto++) {
                    var enlace = RedOcho.unir(sala.ip(), sala.puerto(), puesto, "");
                    invitados.add(enlace);
                    if (puesto + 1 < total) {
                        var parcial = sala(buscar, servidor.puerto(), puesto + 1);
                        assertTrue(parcial.libre(puesto + 1));
                        assertFalse(parcial.libre(puesto));
                        assertTrue(motor.pausa);
                        assertThrows(IOException.class, () -> RedOcho.unir(sala.ip(), sala.puerto(), 1, ""));
                    }
                }
                var ultimo = invitados.getLast();
                esperar("No inició la misión 8", () -> ultimo.iniciada() && ultimo.vista.get() != null);
                assertEquals((1 << total) - 1, ultimo.mascaraOcupados());
                assertFalse(ultimo.vista.get().pausa);
                esperar("La sala llena siguió en el buscador", () -> buscar.salas().isEmpty());
            } finally {
                invitados.forEach(RedOcho::close);
            }
        }
    }

    @Test void cancelarRetiraAnuncioSinIniciar() throws Exception {
        var motor = new MotorBatalla(123, false);
        try (var buscar = buscar(5)) {
            try (var servidor = new ServidorBatalla(todas(), 0, motor, null, true);
                 var anfitrion = new ClienteBatalla("127.0.0.1", servidor.puerto(), 0, "Ciudadano")) {
                servidor.anunciar();
                sala(buscar, servidor.puerto(), 1);
            }
            esperar("La sala cancelada quedó visible", () -> buscar.salas().isEmpty());
            assertEquals(EstadoBatalla.Fase.ESPERA, motor.vista().fase());
        }
    }
}
