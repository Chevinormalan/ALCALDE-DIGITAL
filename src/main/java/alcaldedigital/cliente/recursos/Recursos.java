package alcaldedigital.cliente.recursos;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Recursos empaquetados: también funciona desde un JAR fuera del repositorio. */
public final class Recursos {
    private final Map<String,BufferedImage> cache=new HashMap<>();
    public BufferedImage imagen(String nombre) {
        return cache.computeIfAbsent(nombre,n->{
            try(var in=Recursos.class.getResourceAsStream("/cliente/arte/"+n+".png")) {
                if(in==null) throw new IllegalStateException("Falta recurso: "+n);
                BufferedImage im=ImageIO.read(in);
                if(im==null) throw new IllegalStateException("PNG inválido: "+n);
                return im;
            } catch(IOException e) {throw new UncheckedIOException(e);}
        });
    }
}
