package alcaldedigital.servidor.carrera;

import alcaldedigital.compartido.carrera.EstadoCarrera;
import java.io.*;
import java.nio.file.*;

public final class GuardadoCarrera {
    private final Path archivo;
    public GuardadoCarrera(Path archivo){this.archivo=archivo;}
    public boolean existe(){return Files.isRegularFile(archivo);}
    public synchronized void guardar(MotorCarrera motor)throws IOException{
        Path dir=archivo.toAbsolutePath().getParent();Files.createDirectories(dir);Path tmp=Files.createTempFile(dir,"mision7-",".tmp");
        try{try(var out=new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))){out.writeInt(EstadoCarrera.MAGIA);out.writeInt(EstadoCarrera.VERSION);EstadoCarrera.escribir(out,motor.vista());}try{Files.move(tmp,archivo,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,archivo,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(tmp);}
    }
    public synchronized MotorCarrera cargar()throws IOException{
        try(var in=new DataInputStream(new BufferedInputStream(Files.newInputStream(archivo)))){if(in.readInt()!=EstadoCarrera.MAGIA||in.readInt()!=EstadoCarrera.VERSION)throw new IOException("Guardado de misión 7 incompatible");return MotorCarrera.restaurar(EstadoCarrera.leer(in));}
        catch(IllegalArgumentException e){throw new IOException("Guardado de misión 7 inválido",e);}
    }
}
