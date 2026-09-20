import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Exportación mecánica de maestros: recorte alpha, nearest-neighbor, pivote fijo. */
public class PrepararArte {
    static Path origen, destino;
    static java.util.List<String> inventario=new ArrayList<>();
    public static void main(String[] args) throws Exception {
        origen=Path.of(args[0]); destino=Path.of("src/main/resources/cliente/arte");
        for(String rol:new String[]{"ciudadano","periodista","influencer","candidato"}) {
            for(int i=0;i<16;i++) exportar("08 Animaciones/"+rol+"_caminar/frame_%02d.png".formatted(i),"actores/"+rol+"_%02d.png".formatted(i),32,29);
        }
        for(int i=0;i<16;i++) {
            exportar("08 Animaciones/noti/frame_%02d.png".formatted(i),"actores/noti_%02d.png".formatted(i),32,22);
            exportar("08 Animaciones/acciones_roles/frame_%02d.png".formatted(i),"acciones/frame_%02d.png".formatted(i),64,48);
        }
        for(String efecto:new String[]{"verificacion","rumor"}) for(int i=0;i<8;i++)
            exportar("09 Efectos visuales/fx_"+efecto+"/frame_%02d.png".formatted(i),"efectos/"+efecto+"_%02d.png".formatted(i),64,60);
        try(var files=Files.list(origen.resolve("07 Iconografía/individuales"))) {
            for(Path p:files.filter(p->p.toString().endsWith(".png")).sorted().toList())
                guardar(normalizar(ImageIO.read(p.toFile()),32,28),"iconos/"+p.getFileName());
        }
        BufferedImage hoja=ImageIO.read(Path.of("tools/arte/objetos_maestro.png").toFile());
        String[] nombres={"arbol","pino","rosa","flores","banco","aviso","lampara","fuente"};
        double[][] rects={{0,0,.29,.50},{.29,0,.198,.50},{.488,0,.284,.50},{.772,0,.228,.50},
            {0,.51,.291,.49},{.291,.51,.24,.49},{.55,.51,.11,.49},{.689,.51,.311,.49}};
        for(int i=0;i<8;i++) {
            double[] r=rects[i]; int x=(int)(r[0]*hoja.getWidth()), y=(int)(r[1]*hoja.getHeight());
            int w=Math.min((int)(r[2]*hoja.getWidth()),hoja.getWidth()-x),h=Math.min((int)(r[3]*hoja.getHeight()),hoja.getHeight()-y);
            guardar(normalizar(hoja.getSubimage(x,y,w,h),96,90),"objetos/"+nombres[i]+".png");
        }
        BufferedImage materiales=ImageIO.read(origen.resolve("04 Diseño de niveles/tileset_materiales.png").toFile());
        guardar(escalar(materiales.getSubimage(27,72,94,118),16,16),"tiles/cesped.png");
        guardar(escalar(materiales.getSubimage(497,73,83,118),16,16),"tiles/camino.png");
        guardar(escalar(materiales.getSubimage(990,250,79,112),16,16),"tiles/agua.png");
        guardar(escalar(ImageIO.read(origen.resolve("03 Diseño de entornos/B_parque.png").toFile()),960,640),"portada.png");
        Files.writeString(destino.resolve("INVENTARIO.txt"),"Recursos de ejecución: "+inventario.size()+" PNG\nPivote: centro inferior; alpha real; escalado nearest-neighbor.\n"+String.join("\n",inventario)+"\n");
        StringBuilder html=new StringBuilder("<!doctype html><html lang='es'><meta charset='utf-8'><title>Alcalde Digital · Recursos de producción</title><style>body{background:#18283d;color:#f7e7c6;font:16px system-ui;margin:32px}main{display:grid;grid-template-columns:repeat(auto-fill,minmax(190px,1fr));gap:14px}figure{margin:0;padding:16px;background:#294357;border-radius:12px;overflow:hidden}img{width:128px;height:128px;object-fit:contain;image-rendering:pixelated;background:repeating-conic-gradient(#385568 0% 25%,#294357 0% 50%) 50%/16px 16px}figcaption{font-size:12px;margin-top:12px;overflow-wrap:anywhere}a{color:#e5aa45}</style><h1>Recursos listos para ejecución</h1><p>148 PNG · 4 roles · 4 direcciones · Noti · acciones · efectos · 24 iconos · objetos · tiles. Transparencia y pivotes normalizados.</p><p>Secuencias: caminar 0–3 abajo, 4–7 izquierda, 8–11 derecha, 12–15 arriba; 8 fps. Efectos 8 cuadros a 10 fps. Las acciones puntuales no se repiten.</p><main>");
        for(String entrada:inventario) {String archivo=entrada.split(" \\| ")[0];html.append("<figure><a href='").append(archivo).append("'><img loading='lazy' src='").append(archivo).append("' alt='").append(archivo).append("'></a><figcaption>").append(entrada).append("</figcaption></figure>");}
        html.append("</main></html>");Files.writeString(destino.resolve("index.html"),html);
        System.out.println("Exportados "+inventario.size()+" PNG de producción.");
    }
    static void exportar(String source,String name,int cell,int alto) throws Exception {
        guardar(normalizar(ImageIO.read(origen.resolve(source).toFile()),cell,alto),name);
    }
    static BufferedImage normalizar(BufferedImage im,int cell,int alto) {
        int x0=im.getWidth(),y0=im.getHeight(),x1=-1,y1=-1;
        for(int y=0;y<im.getHeight();y++) for(int x=0;x<im.getWidth();x++) if((im.getRGB(x,y)>>>24)>48) {
            x0=Math.min(x0,x); y0=Math.min(y0,y); x1=Math.max(x1,x); y1=Math.max(y1,y);
        }
        if(x1<0) throw new IllegalArgumentException("Recurso vacío");
        BufferedImage crop=im.getSubimage(x0,y0,x1-x0+1,y1-y0+1);
        double scale=Math.min((double)alto/crop.getHeight(),(double)(cell-2)/crop.getWidth());
        int w=Math.max(1,(int)Math.round(crop.getWidth()*scale)),h=Math.max(1,(int)Math.round(crop.getHeight()*scale));
        BufferedImage out=new BufferedImage(cell,cell,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=out.createGraphics(); g.drawImage(escalar(crop,w,h),(cell-w)/2,cell-h-1,null);g.dispose();return out;
    }
    static BufferedImage escalar(BufferedImage src,int w,int h) {
        BufferedImage out=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB); Graphics2D g=out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src,0,0,w,h,null);g.dispose();return out;
    }
    static void guardar(BufferedImage im,String name) throws Exception {
        Path p=destino.resolve(name);Files.createDirectories(p.getParent());ImageIO.write(im,"png",p.toFile());
        inventario.add(name+" | "+im.getWidth()+"x"+im.getHeight());
    }
}
