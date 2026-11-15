package alcaldedigital.cliente.mision4;

import processing.core.PApplet;
import processing.core.PImage;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

/** Resource cache: no disk path is needed when the game is packaged as a JAR. */
final class SpriteBank {
    final alcaldedigital.cliente.presentacion.JuanSprites juan=new alcaldedigital.cliente.presentacion.JuanSprites();
    private final Map<String,PImage> images=new HashMap<>();
    PImage image(String name) {
        return images.computeIfAbsent(name,n->{
            String path="/cliente/mision4/"+n+".png";
            try(var stream=getClass().getResourceAsStream(path)) {
                if(stream==null)throw new IllegalStateException("Falta sprite: "+path);
                return new PImage(ImageIO.read(stream));
            }catch(Exception ex){throw new IllegalStateException("Recurso de misión 4: "+path,ex);}
        });
    }
    PImage shared(String path) {
        return images.computeIfAbsent(path,n->{
            try(var stream=getClass().getResourceAsStream("/cliente/"+n+".png")) {
                if(stream==null)throw new IllegalStateException("Falta recurso: "+n);
                return new PImage(ImageIO.read(stream));
            }catch(Exception ex){throw new IllegalStateException("Recurso: "+n,ex);}
        });
    }
    PImage recortada(String name){
        String key="recorte:"+name;PImage result=images.get(key);if(result!=null)return result;PImage p=image(name);p.loadPixels();int x0=p.width,y0=p.height,x1=0,y1=0;
        for(int y=0;y<p.height;y++)for(int x=0;x<p.width;x++)if((p.pixels[y*p.width+x]>>>24)>100){x0=Math.min(x0,x);x1=Math.max(x1,x);y0=Math.min(y0,y);y1=Math.max(y1,y);}
        result=p.get(x0,y0,x1-x0+1,y1-y0+1);images.put(key,result);return result;
    }
    PImage palomo(){
        return images.computeIfAbsent("palomo",n->{
            try(var stream=getClass().getResourceAsStream("/cliente/arte/mision1/palomo.png")) {
                PImage atlas=new PImage(ImageIO.read(stream));
                return atlas.get(0,0,atlas.width/4,atlas.height/2);
            }catch(Exception ex){throw new IllegalStateException("Falta Mr. Palomo",ex);}
        });
    }
    PImage palomoVuelo(int frame){
        return images.computeIfAbsent("palomo_vuelo_"+frame,n->{
            try(var stream=getClass().getResourceAsStream("/cliente/arte/mision1/palomo.png")) {
                PImage atlas=new PImage(ImageIO.read(stream));
                return atlas.get(frame*(atlas.width/4),atlas.height/2,atlas.width/4,atlas.height/2);
            }catch(Exception ex){throw new IllegalStateException("Falta Mr. Palomo",ex);}
        });
    }
    void draw(processing.core.PGraphics a,String name,float x,float feet,float w,float h){
        if(!name.startsWith("vecino_")&&!name.startsWith("enemigo_")&&!name.equals("palomo")){
            int dir=name.contains("back")?1:name.contains("left")?2:name.contains("right")?3:0;
            String pose=name.startsWith("walk_")?"walk":name.startsWith("combat_")?"idle":name;
            float paso=name.startsWith("walk_")?Character.digit(name.charAt(name.length()-1),10):0;
            juan.dibujar(a,pose,dir,paso,0,x,feet,w,h);return;
        }
        PImage im=image(name);a.image(im,x-w/2,feet-h,w,h);
    }
}
