package alcaldedigital.compartido.guardado;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Archiva el perfil completo para iniciar una campaña limpia. */
public final class RestablecerJuego {
    private RestablecerJuego(){}

    public static Path restablecer() throws IOException {
        Path perfil=ArchivosPartida.directorio().toAbsolutePath().normalize();
        if(!Files.exists(perfil)) {Files.createDirectories(perfil);return null;}
        Path padre=perfil.getParent();
        String fecha=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path respaldo=padre.resolve(perfil.getFileName()+"-respaldo-"+fecha);
        int sufijo=2;
        while(Files.exists(respaldo))respaldo=padre.resolve(perfil.getFileName()+"-respaldo-"+fecha+"-"+sufijo++);
        Files.move(perfil,respaldo);
        try{Files.createDirectories(perfil);}catch(IOException e){Files.move(respaldo,perfil);throw e;}
        return respaldo;
    }
}
