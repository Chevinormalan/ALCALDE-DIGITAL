package alcaldedigital.cliente.tutorial;

import alcaldedigital.compartido.guardado.ArchivosPartida;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

/** El estado viaja con el perfil de campaña al restablecer el juego. */
public final class RegistroTutorial {
    private final Path archivo;

    public RegistroTutorial(Path perfil) { archivo = perfil.resolve("tutorial.properties"); }

    public boolean visto() throws IOException {
        Properties datos = ArchivosPartida.leer(archivo);
        if (datos == null) return false;
        return "1".equals(datos.getProperty("version")) &&
            ("completado".equals(datos.getProperty("estado")) || "omitido".equals(datos.getProperty("estado")));
    }

    public void guardar(Tutorial.Resultado resultado) throws IOException {
        Properties datos = new Properties();
        datos.setProperty("version", "1");
        datos.setProperty("estado", resultado == Tutorial.Resultado.COMPLETADO ? "completado" : "omitido");
        ArchivosPartida.guardar(archivo, datos);
    }
}
