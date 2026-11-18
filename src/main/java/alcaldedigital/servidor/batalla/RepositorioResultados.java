package alcaldedigital.servidor.batalla;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.compartido.batalla.EstadoBatalla.*;

/** Campaña y puntuación duraderas; una sala terminada se acredita una sola vez. */
public final class RepositorioResultados {
    private final Path archivo;
    public RepositorioResultados(Path archivo){this.archivo=archivo;}
    private Properties leer()throws IOException{Properties p=new Properties();if(Files.exists(archivo))try(var in=Files.newInputStream(archivo)){p.load(in);}return p;}
    public synchronized EstadoCiudad ciudad(EstadoCiudad defecto)throws IOException{Properties p=leer();String v=p.getProperty("ciudad");if(v==null)return defecto;try{int[] n=Arrays.stream(v.split(",")).mapToInt(Integer::parseInt).toArray();if(n.length!=7)return defecto;return new EstadoCiudad(n[0],n[1],n[2],n[3],n[4],n[5],n[6]);}catch(RuntimeException corrupto){throw new IOException("Campaña guardada inválida",corrupto);}}
    public synchronized EstadoCiudad acreditar(Vista v,EstadoCiudad anterior)throws IOException{if(v.fase()!=Fase.FINAL)return anterior;Properties p=leer();if(p.containsKey("sala."+v.id()))return ciudad(anterior);
        int[] n=v.ciudad();EstadoCiudad c=new EstadoCiudad(n[0],n[1],n[2],n[3],n[4],n[5],n[6]);
        p.setProperty("sala."+v.id()+".dificultad",v.dificultad().name());p.setProperty("sala."+v.id(),v.gano()?"victoria":"derrota");p.setProperty("ciudad",c.informacionVerificada()+","+c.confianza()+","+c.convivencia()+","+c.bienestar()+","+c.participacion()+","+c.desinformacion()+","+c.conflictos());
        for(int i=0;i<(v.solo()?1:2);i++){p.setProperty("sala."+v.id()+".puntos."+i,Integer.toString(v.jugadores()[i].puntos()));p.setProperty("mejor.m5."+i,Integer.toString(Math.max(Integer.parseInt(p.getProperty("mejor.m5."+i,"0")),v.jugadores()[i].puntos())));}
        Files.createDirectories(archivo.toAbsolutePath().getParent());Path tmp=Files.createTempFile(archivo.toAbsolutePath().getParent(),"perfil-",".tmp");try{try(var out=Files.newOutputStream(tmp)){p.store(out,"Alcalde Digital · campaña y resultados por sala");}try{Files.move(tmp,archivo,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,archivo,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(tmp);}return c;
    }
}
