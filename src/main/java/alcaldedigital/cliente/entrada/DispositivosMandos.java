package alcaldedigital.cliente.entrada;

import net.java.games.input.AccesoMandosJInput;
import net.java.games.input.AccesoMandosJInput.Dispositivo;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Inventario de mandos del sistema. Todas sus llamadas se realizan en el hilo de lectura. */
final class DispositivosMandos implements AutoCloseable {
    private static final Path DISPOSITIVOS = Path.of("/dev/input");
    private static final Path SYS = Path.of("/sys/class/input");
    private final String sistema = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    private final Map<String, Conexion> controles = new LinkedHashMap<>();
    private final Map<String, String> rutas = new HashMap<>();
    private AccesoMandosJInput.Windows windows;
    private Controller[] otros;

    private record Conexion(Controller control, Dispositivo recurso) {
        Conexion(Dispositivo dispositivo) { this(dispositivo.control(), dispositivo); }
        void cerrar() { if (recurso != null) recurso.close(); }
    }
    private record Rutas(Path evento, Path joystick) {}

    DispositivosMandos() throws Exception {
        if (sistema.contains("linux")) AccesoMandosJInput.iniciarLinux();
        else if (sistema.startsWith("windows")) windows = new AccesoMandosJInput.Windows();
        else otros = ControllerEnvironment.getDefaultEnvironment().getControllers();
    }

    void actualizar() throws IOException {
        if (sistema.contains("linux")) abrirLinux();
        else if (windows != null) actualizarWindows();
        else {
            for (int i=0;i<otros.length;i++) if (esMando(otros[i]))
                controles.putIfAbsent("jinput:"+i, new Conexion(otros[i], null));
        }
    }

    Map<String, EstadoMando> leer() {
        Map<String, EstadoMando> estados = new LinkedHashMap<>();
        var it = controles.entrySet().iterator();
        while (it.hasNext()) {
            var entrada = it.next();
            Conexion c = entrada.getValue();
            try {
                if (c.control.poll()) {
                    estados.put(entrada.getKey(), LectorJInput.convertir(c.control));
                    continue;
                }
            } catch (RuntimeException e) {
                // Un mando puede desaparecer mientras el sistema entrega su último estado.
            }
            c.cerrar();
            it.remove();
        }
        return Collections.unmodifiableMap(estados);
    }

    private void abrirLinux() throws IOException {
        Map<String, Rutas> disponibles = new TreeMap<>();
        if (Files.isDirectory(SYS)) {
            try (var archivos = Files.list(SYS)) {
                for (Path evento : archivos.filter(p -> p.getFileName().toString().matches("event[0-9]+")).toList()) {
                    Path dispositivo = evento.resolve("device");
                    if (!tieneBotonesDeJuego(atributo(dispositivo.resolve("capabilities/key")))) continue;
                    try {
                        Path ruta = dispositivo.toRealPath();
                        Path joystick;
                        try (var nodos = Files.list(dispositivo)) {
                            joystick = nodos.map(Path::getFileName).filter(p -> p.toString().matches("js[0-9]+"))
                                    .findFirst().map(DISPOSITIVOS::resolve).orElse(null);
                        }
                        String serie = atributo(dispositivo.resolve("uniq"));
                        String modelo = atributo(dispositivo.resolve("name"));
                        // El kernel cambia eventN/inputN al reconectar. La serie o el puerto físico perduran.
                        String ubicacion = ruta.toString().replaceFirst("/input/input[0-9]+$", "")
                                .replaceFirst("\\.[0-9a-fA-F]+$", "");
                        String id = "linux:" + modelo + ":" + (serie.isEmpty() ? ubicacion : serie);
                        disponibles.putIfAbsent(id, new Rutas(DISPOSITIVOS.resolve(evento.getFileName()), joystick));
                    } catch (NoSuchFileException e) { /* Se retiró durante la lectura del inventario. */ }
                }
            }
        }
        controles.entrySet().removeIf(e -> {
            Rutas nodos = disponibles.get(e.getKey());
            if (nodos != null && nodos.toString().equals(rutas.get(e.getKey()))
                    && (Files.exists(nodos.evento) || nodos.joystick != null && Files.exists(nodos.joystick))) return false;
            e.getValue().cerrar();
            return true;
        });
        rutas.keySet().retainAll(disponibles.keySet());
        for (var item : disponibles.entrySet()) {
            if (controles.containsKey(item.getKey())) continue;
            Rutas nodos = item.getValue();
            Dispositivo dispositivo = abrir(nodos.evento, false);
            if (dispositivo == null) dispositivo = abrir(nodos.joystick, true);
            if (dispositivo != null) {
                controles.put(item.getKey(), new Conexion(dispositivo));
                rutas.put(item.getKey(), nodos.toString());
            }
        }
    }

    private static Dispositivo abrir(Path ruta, boolean joystick) {
        if (ruta == null || !Files.isReadable(ruta)) return null;
        try {
            Dispositivo d = AccesoMandosJInput.abrirLinux(ruta, joystick);
            if (esMando(d.control())) return d;
            d.close();
        } catch (IOException e) { /* Un nodo puede desaparecer o negar acceso mientras se abre. */ }
        return null;
    }

    private void actualizarWindows() throws IOException {
        // Conserva los dispositivos que aún responden, aunque DirectInput reordene los índices.
        leer();
        Map<String, List<Dispositivo>> porModelo = new LinkedHashMap<>();
        for (Dispositivo d : windows.enumerar()) {
            if (esMando(d.control())) porModelo.computeIfAbsent(d.control().getName(), k -> new ArrayList<>()).add(d);
            else d.close();
        }
        controles.entrySet().removeIf(e -> {
            if (porModelo.containsKey(e.getValue().control.getName())) return false;
            e.getValue().cerrar();
            return true;
        });
        for (var modelo : porModelo.entrySet()) {
            String nombre = modelo.getKey();
            int activos = (int) controles.values().stream().filter(c -> nombre.equals(c.control.getName())).count();
            List<Dispositivo> candidatos = modelo.getValue();
            for (int i = 0; i < candidatos.size(); i++) {
                Dispositivo candidato = candidatos.get(i);
                String id = "windows:"+nombre+":"+(i+1);
                if (activos >= candidatos.size() || controles.containsKey(id)) candidato.close();
                else {
                    controles.put(id, new Conexion(candidato));
                    activos++;
                }
            }
        }
    }

    private static boolean esMando(Controller c) {
        if (c.getType() != Controller.Type.GAMEPAD && c.getType() != Controller.Type.STICK) return false;
        return Arrays.stream(c.getComponents()).filter(x -> x.getIdentifier() instanceof Component.Identifier.Button).count() >= 2;
    }

    static boolean tieneBotonesDeJuego(String bits) {
        String[] palabras = bits.strip().split("\\s+");
        for (int codigo = 0x120; codigo <= 0x13f; codigo++) {
            int indice = palabras.length - 1 - codigo / 64;
            if (indice >= 0 && (Long.parseUnsignedLong(palabras[indice], 16) & (1L << (codigo % 64))) != 0) return true;
        }
        return false;
    }

    private static String atributo(Path archivo) {
        try { return Files.readString(archivo).strip(); }
        catch (IOException e) { return ""; }
    }

    @Override public void close() throws IOException {
        controles.values().forEach(Conexion::cerrar);
        controles.clear();
        if (windows != null) windows.close();
    }
}
