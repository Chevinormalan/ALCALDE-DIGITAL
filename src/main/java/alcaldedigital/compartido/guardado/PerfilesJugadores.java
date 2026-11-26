package alcaldedigital.compartido.guardado;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

/** El directorio base conserva los usuarios. Cada campaña ocupa una carpeta independiente. */
public final class PerfilesJugadores {
    public record Perfil(UUID id, String nombre) { }

    private static final String PREFIJO = "usuario.";
    private final Path raiz;
    private final Path registro;

    public PerfilesJugadores(Path base) {
        raiz = base.toAbsolutePath().normalize();
        registro = raiz.resolve("usuarios.properties");
    }

    public Path directorio(Perfil perfil) {
        return raiz.resolve("usuarios").resolve(perfil.id().toString());
    }

    public List<Perfil> listar() throws IOException {
        return listar(leer());
    }

    public Perfil activo() throws IOException {
        Properties p = leer();
        return listar(p).stream().filter(u -> u.id().toString().equals(p.getProperty("activo")))
            .findFirst().orElse(null);
    }

    public boolean hayCampanaAnterior() throws IOException {
        return campanaAnterior(leer());
    }

    public Perfil crear(String nombre, boolean importar, boolean activar) throws IOException {
        String limpio = Normalizer.normalize(nombre.strip(), Normalizer.Form.NFC).replaceAll(" +", " ");
        if (limpio.length() > 28 || !limpio.matches("[\\p{L}\\p{N}][\\p{L}\\p{N} ._'-]*"))
            throw new IllegalArgumentException("Escribe de 1 a 28 caracteres. Usa letras, números o espacios.");
        return conRegistro(p -> {
            if (listar(p).stream().anyMatch(u -> clave(u.nombre()).equals(clave(limpio))))
                throw new IllegalArgumentException("Ese usuario ya existe. Selecciónalo en la lista.");
            if (importar && !campanaAnterior(p))
                throw new IOException("La campaña anterior ya fue asignada.");
            var nuevo = new Perfil(UUID.randomUUID(), limpio);
            Path destino = directorio(nuevo);
            Files.createDirectories(destino);
            if (importar) {
                try (var archivos = Files.list(raiz)) {
                    for (Path archivo : archivos.filter(PerfilesJugadores::archivoAnterior).toList())
                        Files.copy(archivo, destino.resolve(archivo.getFileName()));
                }
                Path tabla = TablaPuntajes.archivo(destino);
                if (Files.isRegularFile(tabla)) Files.copy(tabla, TablaPuntajes.archivoPersonal(destino));
                p.setProperty("campana.anterior", nuevo.id().toString());
            }
            p.setProperty(PREFIJO + nuevo.id(), limpio);
            if (activar) p.setProperty("activo", nuevo.id().toString());
            guardar(p);
            return nuevo;
        });
    }

    public void seleccionar(Perfil perfil) throws IOException {
        conRegistro(p -> {
            if (!listar(p).contains(perfil)) throw new IOException("El usuario ya no está disponible.");
            p.setProperty("activo", perfil.id().toString());
            guardar(p);
            return null;
        });
    }

    /** Quita el usuario del selector y archiva sus archivos de campaña para recuperación. */
    public void eliminar(Perfil perfil) throws IOException {
        conRegistro(p -> {
            String id = perfil.id().toString();
            if (!perfil.nombre().equals(p.getProperty(PREFIJO + id)))
                throw new IOException("El usuario ya no está disponible.");
            Path origen = directorio(perfil);
            Path respaldo = raiz.resolve("usuarios-eliminados").resolve(id);
            boolean movido = Files.exists(origen);
            if (movido) {
                Files.createDirectories(respaldo.getParent());
                Files.move(origen, respaldo);
            }
            p.remove(PREFIJO + id);
            if (id.equals(p.getProperty("activo"))) p.remove("activo");
            try {
                guardar(p);
            } catch (IOException e) {
                if (movido) try { Files.move(respaldo, origen); }
                catch (IOException recuperacion) { e.addSuppressed(recuperacion); }
                throw e;
            }
            return null;
        });
    }

    private static String clave(String nombre) {
        return Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private boolean campanaAnterior(Properties p) throws IOException {
        if (p.containsKey("campana.anterior") || !Files.isDirectory(raiz)) return false;
        try (var archivos = Files.list(raiz)) {
            return archivos.anyMatch(PerfilesJugadores::archivoAnterior);
        }
    }

    private static boolean archivoAnterior(Path archivo) {
        String nombre = archivo.getFileName().toString();
        return Files.isRegularFile(archivo, LinkOption.NOFOLLOW_LINKS) &&
            nombre.matches("(?:campana|progreso-misiones|tabla-puntajes|tutorial|mision[1-8][a-z0-9-]*)\\.(?:properties|bin|flag)(?:\\.bak)?");
    }

    private Properties leer() throws IOException {
        Properties p = ArchivosPartida.leer(registro);
        return p == null ? new Properties() : p;
    }

    private static List<Perfil> listar(Properties p) throws IOException {
        try {
            return p.stringPropertyNames().stream().filter(k -> k.startsWith(PREFIJO))
                .map(k -> new Perfil(UUID.fromString(k.substring(PREFIJO.length())), p.getProperty(k)))
                .sorted(Comparator.comparing(u -> clave(u.nombre()))).toList();
        } catch (IllegalArgumentException e) {
            throw new IOException("El registro de usuarios está dañado.", e);
        }
    }

    @FunctionalInterface
    private interface Cambio<T> { T aplicar(Properties p) throws IOException; }

    private void guardar(Properties p) throws IOException {
        p.setProperty("version", "1");
        ArchivosPartida.guardar(registro, p);
    }

    private synchronized <T> T conRegistro(Cambio<T> cambio) throws IOException {
        Files.createDirectories(raiz);
        try (var canal = FileChannel.open(raiz.resolve("usuarios.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             var bloqueo = canal.lock()) {
            return cambio.aplicar(leer());
        }
    }
}
