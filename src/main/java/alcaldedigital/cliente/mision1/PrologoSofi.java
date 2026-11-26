package alcaldedigital.cliente.mision1;

import alcaldedigital.cliente.audio.MotorAudio;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.interfaz.MarcosProcessing;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import processing.awt.PGraphicsJava2D;
import processing.core.PApplet;
import processing.core.PFont;

/** Presenta el prólogo en el lienzo del juego. Los JPEG se descodifican fuera del hilo de dibujo. */
public final class PrologoSofi implements AutoCloseable {
    private static final String VIDEO="/cliente/mision1/prologo.zip";
    private static final String AUDIO="audio/cinematica/prologo-sofi";
    private static final int ANCHO=640, ALTO=360, MAX_BYTES_JPEG=256*1024;
    private static final int X_OMITIR=1004, Y_OMITIR=10, ANCHO_OMITIR=260, ALTO_OMITIR=39;

    private record Datos(int fps,int fotogramas) {}
    private record Fotograma(int indice,BufferedImage imagen) {}
    private final ArrayBlockingQueue<Fotograma> cola=new ArrayBlockingQueue<>(9);
    private final MotorAudio.Cinematica sonido;
    private final Thread lector;
    private volatile Datos datos;
    private volatile boolean error;
    private volatile int buscado;
    private BufferedImage imagen;
    private double segundos;
    private boolean reproduciendo,omitir,atrasMantenido;

    public PrologoSofi(MotorAudio motor,EstadoMando mando) {
        sonido=motor.prepararCinematica(AUDIO);
        atrasMantenido=mando.pulsado(EstadoMando.Boton.B);
        lector=new Thread(this::leer,"prologo-sofi-video");
        lector.setDaemon(true);
        lector.start();
    }

    private void leer() {
        var lectorJpeg=ImageIO.getImageReadersByFormatName("JPEG").next();
        try(var recurso=PrologoSofi.class.getResourceAsStream(VIDEO)) {
            if(recurso==null)throw new IOException("Falta "+VIDEO);
            try(var archivo=new ZipInputStream(new BufferedInputStream(recurso))) {
                var entrada=archivo.getNextEntry();
                if(entrada==null||!entrada.getName().equals("reproduccion.properties"))
                    throw new IOException("Faltan los datos del prólogo");
                var propiedades=new Properties();
                propiedades.load(archivo);
                int fps=Integer.parseInt(propiedades.getProperty("fps","0"));
                int total=Integer.parseInt(propiedades.getProperty("fotogramas","0"));
                if(fps<1||fps>60||total<1)throw new IOException("Duración del prólogo inválida");
                datos=new Datos(fps,total);
                for(int indice=0;indice<total&&!Thread.currentThread().isInterrupted();indice++) {
                    entrada=archivo.getNextEntry();
                    if(entrada==null)throw new IOException("Faltan fotogramas del prólogo");
                    if(indice<buscado)continue;
                    byte[] jpeg=archivo.readNBytes(MAX_BYTES_JPEG+1);
                    if(jpeg.length>MAX_BYTES_JPEG)throw new IOException("Fotograma demasiado grande");
                    try(var bytes=new MemoryCacheImageInputStream(new ByteArrayInputStream(jpeg))) {
                        lectorJpeg.setInput(bytes,true,true);
                        if(lectorJpeg.getWidth(0)!=ANCHO||lectorJpeg.getHeight(0)!=ALTO)
                            throw new IOException("Tamaño de fotograma incorrecto");
                        cola.put(new Fotograma(indice,lectorJpeg.read(0)));
                    }
                }
            }
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch(IOException|RuntimeException ex) {
            error=true;
            System.err.println("No se pudo reproducir el prólogo de Sofi: "+ex.getMessage());
        } finally {
            lectorJpeg.dispose();
        }
    }

    /** Devuelve falso al terminar, omitir o si los recursos no se pueden abrir. */
    public boolean actualizar(double dt,boolean enfocada,EstadoMando mando) {
        boolean atras=mando.pulsado(EstadoMando.Boton.B);
        if(enfocada&&atras&&!atrasMantenido)omitir();
        atrasMantenido=atras;
        if(omitir||error)return false;
        if(reproduciendo&&enfocada)segundos+=Math.max(0,dt);
        Datos video=datos;
        if(video!=null) {
            int indice=(int)(segundos*video.fps());
            if(indice>=video.fotogramas())return false;
            buscado=indice;
            Fotograma candidato;
            while((candidato=cola.peek())!=null&&candidato.indice()<=indice) {
                imagen=cola.remove().imagen();
            }
        }
        reproduciendo=enfocada&&imagen!=null&&sonido.lista();
        sonido.reproducir(reproduciendo);
        return true;
    }

    public void dibujar(PApplet escena,PFont fuente,boolean mando) {
        if(imagen==null)escena.background(0xff130c20);
        else {
            var graficos=(PGraphicsJava2D)escena.g;
            graficos.g2.drawImage(imagen,0,0,escena.width,escena.height,null);
        }
        escena.pushStyle();
        boolean encima=boton(escena.mouseX,escena.mouseY);
        MarcosProcessing.marco(escena.g,X_OMITIR,Y_OMITIR,ANCHO_OMITIR,ALTO_OMITIR,
            encima?0xff412856:0xff241631,0xffcbaade);
        escena.textFont(fuente);
        escena.textSize(19);
        escena.textAlign(PApplet.CENTER,PApplet.CENTER);
        escena.fill(0xfff5e8fb);
        escena.text(mando?"Omitir · B / Círculo":"Omitir prólogo · Esc",
            X_OMITIR+ANCHO_OMITIR/2f,Y_OMITIR+ALTO_OMITIR/2f);
        escena.popStyle();
    }

    public static boolean boton(int x,int y) {
        return x>=X_OMITIR&&x<X_OMITIR+ANCHO_OMITIR&&y>=Y_OMITIR&&y<Y_OMITIR+ALTO_OMITIR;
    }

    public void omitir(){omitir=true;pausar();}

    public void pausar(){
        reproduciendo=false;
        sonido.reproducir(false);
    }

    @Override public void close() {
        sonido.close();
        lector.interrupt();
    }
}
