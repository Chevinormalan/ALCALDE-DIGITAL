package alcaldedigital.cliente.mision6;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static alcaldedigital.pruebas.RedLanPrueba.*;
import static java.awt.event.KeyEvent.*;
import static org.junit.jupiter.api.Assertions.*;

class RedSeisTest {
    @TempDir Path temporal;

    @Test void descubreRestauraControlaAmbasNavesYReconecta() throws Exception {
        var archivo = new AutoguardadoSeis(temporal.resolve("mision6-lan.properties"));
        var motor = new MisionSeisMotor();
        motor.comenzar(MisionSeisMotor.Modo.DUO);
        motor.fase = MisionSeisMotor.Fase.ENTRENAMIENTO;
        try (var servidor = RedSeis.alojar(0, motor, archivo); var buscar = buscar(6)) {
            servidor.anunciar();
            var sala = sala(buscar, servidor.puerto(), 1);
            assertTrue(sala.libre(1));
            assertFalse(servidor.vista().iniciada());
            assertEquals(0f, servidor.vista().motor().tiempo);
            String reserva;
            try (var invitado = RedSeis.unir(sala.ip(), sala.puerto(), "")) {
                esperar("El invitado no recibe la misión", () -> invitado.vista() != null && invitado.vista().iniciada());
                reserva = invitado.token();
                assertEquals(motor.partida, invitado.vista().motor().partida);
                assertThrows(IOException.class, () -> RedSeis.unir(sala.ip(), sala.puerto(), ""));
                servidor.teclaAnfitrion(VK_A, true);
                invitado.movimientoInvitado(RedSeis.DERECHA | RedSeis.DISPARO);
                esperar("No se sincronizó el control de Juan e Influencer", () -> {
                    var copia = invitado.vista().motor();
                    return copia.naves[0].x < 150 && copia.naves[1].x > 195
                            && copia.proyectiles.stream().anyMatch(p -> p.dueno == 1);
                });
                invitado.especialInvitado(RedSeis.IMPULSO);
                esperar("El invitado no pudo impulsar su nave", () -> invitado.vista().motor().naves[1].recargaImpulso > 0);
                invitado.pausar();
                esperar("La pausa remota no llegó al anfitrión", () -> servidor.vista().pausada());
                float tiempo = servidor.vista().motor().tiempo;
                Thread.sleep(100);
                assertEquals(tiempo, servidor.vista().motor().tiempo);
                servidor.guardar();
                assertEquals(motor.partida, MisionSeisMotor.restaurar(archivo.cargar()).partida);
            }
            esperar("El servidor no detectó la desconexión", () -> !servidor.vista().invitado());
            esperar("La partida en curso volvió a anunciarse", () -> buscar.salas().isEmpty());
            assertThrows(IOException.class, () -> RedSeis.unir(sala.ip(), sala.puerto(), ""));
            try (var vuelta = RedSeis.unir(sala.ip(), sala.puerto(), reserva)) {
                esperar("No regresó el puesto reservado", () -> vuelta.vista() != null && vuelta.vista().invitado());
                servidor.reanudar();
                esperar("No reanudó después de conectar", () -> !vuelta.vista().pausada());
                servidor.teclaAnfitrion(VK_ENTER, true);
                esperar("El invitado no pasó del entrenamiento al combate",
                        () -> vuelta.vista().motor().fase == MisionSeisMotor.Fase.COMBATE);
            }
        }
        try (var recuperado = new AutoguardadoSeis(temporal.resolve("mision6-lan.properties"))) {
            var estado = MisionSeisMotor.restaurar(recuperado.cargar());
            assertEquals(MisionSeisMotor.Fase.COMBATE, estado.fase);
            assertEquals(MisionSeisMotor.Modo.DUO, estado.modo);
        }
    }
}
