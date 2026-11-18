package alcaldedigital.servidor.batalla;

import java.nio.file.*;
import java.io.*;
import alcaldedigital.compartido.batalla.EstadoBatalla;

/** Instantáneas exactas con versión y reemplazo atómico; rutas independientes de pruebas. */
public final class GuardadoBatalla {
    private final Path archivo;
    public GuardadoBatalla(Path archivo){this.archivo=archivo;}
    public static Path directorio(){return alcaldedigital.compartido.guardado.ArchivosPartida.directorio();}
    public synchronized void guardar(MotorBatalla motor)throws IOException{
        Files.createDirectories(archivo.toAbsolutePath().getParent());Path temporal=Files.createTempFile(archivo.toAbsolutePath().getParent(),"partida-",".tmp");
        try{try(var out=new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temporal)))){out.writeInt(0x41443542);out.writeInt(EstadoBatalla.VERSION);EstadoBatalla.escribir(out,motor.vista());}try{Files.move(temporal,archivo,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temporal,archivo,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temporal);}
    }
    public synchronized MotorBatalla cargar()throws IOException{try(var in=new DataInputStream(new BufferedInputStream(Files.newInputStream(archivo)))){if(in.readInt()!=0x41443542)throw new IOException("Guardado incompatible");int version=in.readInt();if(version<1||version>EstadoBatalla.VERSION)throw new IOException("Guardado incompatible");return MotorBatalla.restaurar(EstadoBatalla.leer(in,version));}}
    public boolean existe(){return Files.isRegularFile(archivo);}
    public Path archivo(){return archivo;}
}
