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
            String ruta=n.startsWith("mision4/")||n.startsWith("mision6/")||n.startsWith("mision7/")||n.startsWith("mision5/")||n.startsWith("mision2/")||n.startsWith("mision3/")?
                "/cliente/"+n+".png":"/cliente/arte/"+n+".png";
            try(var in=Recursos.class.getResourceAsStream(ruta)) {
                if(in==null) throw new IllegalStateException("Falta recurso: "+n);
                BufferedImage im=ImageIO.read(in);
                if(im==null) throw new IllegalStateException("PNG inválido: "+n);
                return n.startsWith("controles/")||n.startsWith("mision4/")||n.startsWith("mision6/")||n.startsWith("mision7/")||n.startsWith("mision1/")||n.startsWith("mision5/")||n.startsWith("mision2/")||n.startsWith("mision3/")?
                    im:PaletaMorada.recolorear(n,im);
            } catch(IOException e) {throw new UncheckedIOException(e);}
        });
    }
}
