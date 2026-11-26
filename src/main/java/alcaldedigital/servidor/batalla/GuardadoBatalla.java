package alcaldedigital.servidor.batalla;

import java.nio.file.*;
import java.io.*;
import alcaldedigital.compartido.batalla.EstadoBatalla;

/** Instantáneas exactas con versión y reemplazo atómico; rutas independientes de pruebas. */
public final class GuardadoBatalla {
    private static final int VERSION_ARCHIVO=3;
    private final Path archivo;
    public GuardadoBatalla(Path archivo){this.archivo=archivo;}
    public static Path directorio(){return alcaldedigital.compartido.guardado.ArchivosPartida.directorio();}
    public synchronized void guardar(MotorBatalla motor)throws IOException{
        Files.createDirectories(archivo.toAbsolutePath().getParent());Path temporal=Files.createTempFile(archivo.toAbsolutePath().getParent(),"partida-",".tmp");
        try{try(var out=new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temporal)))){out.writeInt(0x41443542);out.writeInt(VERSION_ARCHIVO);EstadoBatalla.escribir(out,motor.vista());}try{Files.move(temporal,archivo,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temporal,archivo,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(temporal);}
    }
    public synchronized MotorBatalla cargar()throws IOException{
        try(var in=new DataInputStream(new BufferedInputStream(Files.newInputStream(archivo)))){
            if(in.readInt()!=0x41443542)throw new IOException("Guardado incompatible");
            int version=in.readInt();
            if(version<1||version>VERSION_ARCHIVO)throw new IOException("Guardado incompatible");
            var estado=EstadoBatalla.leer(in,version==1?1:EstadoBatalla.VERSION);
            var motor=MotorBatalla.restaurar(estado);
            if(version<VERSION_ARCHIVO)motor.adaptarOleadaAnterior();
            return motor;
        }
    }
    public boolean existe(){return Files.isRegularFile(archivo);}
    public Path archivo(){return archivo;}
}
