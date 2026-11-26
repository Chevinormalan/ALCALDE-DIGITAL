package alcaldedigital.pruebas;

import alcaldedigital.compartido.red.DescubrimientoLan;
import java.net.*;
import java.util.List;
import java.util.function.BooleanSupplier;
import static org.junit.jupiter.api.Assertions.*;

public final class RedLanPrueba {
    private RedLanPrueba() {}

    public static DescubrimientoLan.Busqueda buscar(int mision) throws Exception {
        String adaptador = System.getProperty("ad.lan.interfaz");
        if (adaptador == null) return new DescubrimientoLan.Busqueda(mision, List.of(InetAddress.getByName("127.0.0.1")));
        NetworkInterface interfaz = NetworkInterface.getByName(adaptador);
        assertNotNull(interfaz, "No existe la interfaz " + adaptador);
        var destino = interfaz.getInterfaceAddresses().stream().filter(a -> a.getBroadcast() != null).findFirst().orElseThrow();
        System.out.println("LAN M" + mision + ": " + destino.getAddress().getHostAddress()
                + " UDP broadcast a " + destino.getBroadcast().getHostAddress());
        return new DescubrimientoLan.Busqueda(mision, List.of(destino.getBroadcast()));
    }

    public static void esperar(String descripcion, BooleanSupplier condicion) throws InterruptedException {
        long limite = System.nanoTime() + 6_000_000_000L;
        while (!condicion.getAsBoolean() && System.nanoTime() < limite) Thread.sleep(15);
        assertTrue(condicion.getAsBoolean(), descripcion);
    }

    public static DescubrimientoLan.Sala sala(DescubrimientoLan.Busqueda busqueda, int puerto, int ocupados) throws Exception {
        esperar("No apareció la sala con " + ocupados + " jugador(es)", () -> busqueda.salas().stream()
                .anyMatch(s -> s.puerto() == puerto && s.conectados() == ocupados));
        return busqueda.salas().stream().filter(s -> s.puerto() == puerto && s.conectados() == ocupados).findFirst().orElseThrow();
    }
}
