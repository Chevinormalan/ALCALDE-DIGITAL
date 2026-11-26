package alcaldedigital.app;

import alcaldedigital.servidor.batalla.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.CountDownLatch;

/** Servidor dedicado opcional. El menú Crear sala ya incorpora este mismo servidor. */
public final class ServidorSalaApp {
    private ServidorSalaApp(){}
    public static void main(String[] args)throws Exception{
        int puerto=Integer.parseInt(System.getProperty("ad.puerto","48505"));
        var g=new GuardadoBatalla(GuardadoBatalla.directorio().resolve("mision5-lan.bin"));
        MotorBatalla motor=Boolean.getBoolean("ad.continuar")&&g.existe()?g.cargar():new MotorBatalla(System.currentTimeMillis(),false);
        var servidor=new ServidorBatalla(InetAddress.getByName("0.0.0.0"),puerto,motor,g,true);
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::close));
        System.out.println("Sala misión 5 · TCP "+servidor.puerto()+" · guardado "+g.archivo());
        new CountDownLatch(1).await();
    }
}
