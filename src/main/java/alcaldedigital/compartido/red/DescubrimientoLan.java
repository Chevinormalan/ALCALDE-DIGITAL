package alcaldedigital.compartido.red;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** Descubrimiento IPv4 de salas abiertas. La unión efectiva siempre se confirma por TCP. */
public final class DescubrimientoLan {
    private static final String VERSION = "ALCALDE-LAN-1";
    private static final long CADUCIDAD = 3_000_000_000L;
    private static final int MAXIMO = 512;

    private DescubrimientoLan() {}

    private static int puerto(int mision) {
        return 48510 + mision;
    }

    public record Cupos(int total, int ocupados, String detalle) {}
    public record Sala(UUID id, String ip, int puerto, String nombre, Cupos cupos) {
        public int conectados() { return Integer.bitCount(cupos.ocupados()); }
        public boolean libre(int puesto) {
            return puesto >= 0 && puesto < cupos.total() && (cupos.ocupados() & (1 << puesto)) == 0;
        }
    }

    public static final class Anuncio implements AutoCloseable {
        private final DatagramSocket socket;
        private final UUID id = UUID.randomUUID();
        private final String nombre;

        public Anuncio(int mision, int puertoTcp, Supplier<Cupos> estado) throws IOException {
            String equipo = System.getenv("COMPUTERNAME");
            if (equipo == null || equipo.isBlank()) equipo = System.getenv("HOSTNAME");
            if (equipo == null || equipo.isBlank()) {
                try { equipo = InetAddress.getLocalHost().getHostName(); }
                catch (UnknownHostException e) { equipo = "Equipo anfitrión"; }
            }
            nombre = campo(equipo, 40);
            socket = new DatagramSocket(puerto(mision));
            hilo("lan-anuncio-" + mision, () -> responder(mision, puertoTcp, estado));
        }

        private void responder(int mision, int puertoTcp, Supplier<Cupos> estado) {
            var paquete = new DatagramPacket(new byte[MAXIMO], MAXIMO);
            while (!socket.isClosed()) {
                try {
                    paquete.setLength(MAXIMO);
                    socket.receive(paquete);
                    String[] solicitud = texto(paquete).split("\\|", -1);
                    if (solicitud.length != 3 || !solicitud[0].equals(VERSION)
                            || !solicitud[1].equals(Integer.toString(mision))) continue;
                    UUID nonce = UUID.fromString(solicitud[2]);
                    Cupos c = estado.get();
                    if (c == null || Integer.bitCount(c.ocupados()) >= c.total()) c = new Cupos(0, 0, "");
                    String respuesta = String.join("|", VERSION, Integer.toString(mision), nonce.toString(),
                            id.toString(), Integer.toString(puertoTcp), nombre, Integer.toString(c.total()),
                            Integer.toString(c.ocupados()), campo(c.detalle(), 60));
                    enviar(socket, respuesta, paquete.getAddress(), paquete.getPort());
                } catch (IOException | IllegalArgumentException e) {
                    if (socket.isClosed()) return;
                }
            }
        }

        @Override public void close() { socket.close(); }
    }

    public static final class Busqueda implements AutoCloseable {
        private record Registro(Sala sala, long visto) {}
        private final DatagramSocket socket;
        private final UUID nonce = UUID.randomUUID();
        private final Map<UUID, Registro> salas = new ConcurrentHashMap<>();
        private volatile String error = "";

        public Busqueda(int mision) throws IOException { this(mision, destinos()); }

        public Busqueda(int mision, Collection<InetAddress> destinos) throws IOException {
            socket = new DatagramSocket();
            try {
                socket.setBroadcast(true);
                socket.setSoTimeout(200);
            } catch (IOException e) {
                socket.close();
                throw e;
            }
            hilo("lan-buscar-" + mision, () -> buscar(mision, List.copyOf(destinos)));
        }

        public List<Sala> salas() {
            long ahora = System.nanoTime();
            salas.entrySet().removeIf(e -> ahora - e.getValue().visto() > CADUCIDAD);
            return salas.values().stream().map(Registro::sala)
                    .sorted(Comparator.comparing(Sala::nombre).thenComparing(Sala::ip)).toList();
        }

        public String error() { return error; }

        private void buscar(int mision, List<InetAddress> destinos) {
            String solicitud = String.join("|", VERSION, Integer.toString(mision), nonce.toString());
            var paquete = new DatagramPacket(new byte[MAXIMO], MAXIMO);
            long ultimoEnvio = System.nanoTime() - 1_000_000_000L;
            while (!socket.isClosed()) {
                if (System.nanoTime() - ultimoEnvio >= 750_000_000L) {
                    ultimoEnvio = System.nanoTime();
                    boolean enviado = false;
                    for (InetAddress direccion : destinos) {
                        try {
                            enviar(socket, solicitud, direccion, puerto(mision));
                            enviado = true;
                        } catch (IOException e) {
                            if (socket.isClosed()) return;
                        }
                    }
                    error = enviado ? "" : "No se pudo enviar la búsqueda en la red local.";
                }
                try {
                    paquete.setLength(MAXIMO);
                    socket.receive(paquete);
                    recibir(mision, paquete);
                } catch (SocketTimeoutException | IllegalArgumentException e) {
                    // Continuar el sondeo y descartar datagramas de otros servicios.
                } catch (IOException e) {
                    if (!socket.isClosed()) error = "Se interrumpió la búsqueda de salas.";
                    return;
                }
            }
        }

        private void recibir(int mision, DatagramPacket paquete) {
            String[] datos = texto(paquete).split("\\|", -1);
            if (datos.length != 9 || !datos[0].equals(VERSION)
                    || !datos[1].equals(Integer.toString(mision)) || !datos[2].equals(nonce.toString())) return;
            UUID id = UUID.fromString(datos[3]);
            int puertoTcp = Integer.parseInt(datos[4]);
            int total = Integer.parseInt(datos[6]);
            int ocupados = Integer.parseInt(datos[7]);
            if (total == 0 && ocupados == 0) {
                salas.remove(id);
                return;
            }
            if (puertoTcp < 1 || puertoTcp > 65535 || total < 2 || total > (mision == 8 ? 4 : 2)
                    || ocupados <= 0 || ocupados >= (1 << total) - 1) return;
            long ahora = System.nanoTime();
            if (salas.size() >= 128 && !salas.containsKey(id)) salas();
            if (salas.size() >= 128 && !salas.containsKey(id)) return;
            var sala = new Sala(id, paquete.getAddress().getHostAddress(), puertoTcp, campo(datos[5], 40),
                    new Cupos(total, ocupados, campo(datos[8], 60)));
            salas.compute(id, (k, previo) -> new Registro(
                    previo != null && previo.sala().ip().equals("127.0.0.1")
                            ? new Sala(id, "127.0.0.1", puertoTcp, sala.nombre(), sala.cupos()) : sala, ahora));
        }

        @Override public void close() { socket.close(); }
    }

    private static Collection<InetAddress> destinos() throws SocketException, UnknownHostException {
        Set<InetAddress> destinos = new LinkedHashSet<>();
        for (NetworkInterface interfaz : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!interfaz.isUp() || interfaz.isLoopback()) continue;
            for (InterfaceAddress direccion : interfaz.getInterfaceAddresses()) {
                if (direccion.getBroadcast() != null) destinos.add(direccion.getBroadcast());
            }
        }
        destinos.add(InetAddress.getByName("255.255.255.255"));
        destinos.add(InetAddress.getByName("127.0.0.1"));
        return destinos;
    }

    private static String campo(String valor, int max) {
        String limpio = valor.replaceAll("[\\p{Cntrl}|]", " ").strip();
        return limpio.substring(0, Math.min(max, limpio.length()));
    }

    private static String texto(DatagramPacket paquete) {
        return new String(paquete.getData(), paquete.getOffset(), paquete.getLength(), StandardCharsets.UTF_8);
    }

    private static void enviar(DatagramSocket socket, String mensaje, InetAddress ip, int puerto) throws IOException {
        byte[] datos = mensaje.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(datos, datos.length, ip, puerto));
    }

    private static void hilo(String nombre, Runnable tarea) {
        Thread hilo = new Thread(tarea, nombre);
        hilo.setDaemon(true);
        hilo.start();
    }
}
