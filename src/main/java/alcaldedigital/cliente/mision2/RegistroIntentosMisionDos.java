package alcaldedigital.cliente.mision2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.FileAlreadyExistsException;
import java.util.Objects;

/** Recuerda que el primer intento en Difícil ya tuvo su sorteo crítico. */
public final class RegistroIntentosMisionDos {
    private final Path marca;
    private boolean usadoEnEstaSesion;

    public RegistroIntentosMisionDos() {
        this(alcaldedigital.compartido.guardado.ArchivosPartida.directorio().resolve("mision2-dificil-intentado.flag"));
    }

    public RegistroIntentosMisionDos(Path marca) {this.marca=Objects.requireNonNull(marca);}

    public void reiniciar(){usadoEnEstaSesion=false;}

    /** El sorteo se consume una sola vez, incluso si el juego se cierra después. */
    public boolean sortearPrimerDificil(double tirada) {
        if(!(tirada>=0&&tirada<1)) throw new IllegalArgumentException("Tirada fuera de [0,1)");
        if(usadoEnEstaSesion||Files.exists(marca)) return false;
        usadoEnEstaSesion=true;
        try {
            if(marca.getParent()!=null) Files.createDirectories(marca.getParent());
            Files.createFile(marca);
        } catch(FileAlreadyExistsException yaRegistrado) {
            return false;
        } catch(IOException sinPersistencia) {
            // Sin persistencia no se activa una derrota obligatoria que podría repetirse.
            return false;
        }
        return tirada<.30;
    }
}
