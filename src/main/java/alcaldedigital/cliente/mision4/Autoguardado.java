package alcaldedigital.cliente.mision4;

import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.*;

/** Checkpoints are written on a worker and replaced atomically when supported. */
final class Autoguardado {
    private final Path file;
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"mision4-guardado");t.setDaemon(true);return t;});
    volatile boolean failed;
    volatile long lastSuccess;
    Autoguardado(Path file){this.file=file;}
    static Path defaultPath(){
        String prueba=System.getProperty("ad.mision4.guardar");if(prueba!=null&&!prueba.isBlank())return Path.of(prueba);
        return alcaldedigital.servidor.batalla.GuardadoBatalla.directorio().resolve("mision4.properties");
    }
    Properties load(){
        if(!Files.isRegularFile(file))return null;
        try(var in=Files.newInputStream(file)){Properties p=new Properties();p.load(in);if(!"1".equals(p.getProperty("version")))return null;return p;}
        catch(IOException ex){failed=true;System.err.println("No se pudo leer el autoguardado: "+ex.getMessage());return null;}
    }
    void save(Properties snapshot){
        worker.execute(()->{try{write(snapshot);failed=false;lastSuccess=System.nanoTime();}catch(IOException ex){failed=true;System.err.println("No se pudo guardar la misión 4: "+ex.getMessage());}});
    }
    private void write(Properties snapshot)throws IOException{
        Files.createDirectories(file.getParent());Path tmp=Files.createTempFile(file.getParent(),"mision4-", ".tmp");
        try{
            try(var out=Files.newOutputStream(tmp)){snapshot.store(out,"Alcalde Digital - Mision 4");}
            try{Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException ex){Files.move(tmp,file,StandardCopyOption.REPLACE_EXISTING);}
        }finally{Files.deleteIfExists(tmp);}
    }
    void esperar()throws Exception{worker.submit(()->{}).get(3,TimeUnit.SECONDS);if(failed)throw new IOException("No se pudo confirmar el guardado");}
    void close(){worker.shutdown();try{if(!worker.awaitTermination(2,TimeUnit.SECONDS))worker.shutdownNow();}catch(InterruptedException ex){Thread.currentThread().interrupt();}}
}
