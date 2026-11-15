package alcaldedigital.cliente.interfaz;

import alcaldedigital.cliente.recursos.Recursos;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Cuatro pasos por dirección derivados de una misma vista de cada persona. */
final class SpritesTranseuntes {
    private static final int ANCHO=30, ALTO=48;
    private final BufferedImage[][][] pasos=new BufferedImage[8][4][4];

    SpritesTranseuntes(Recursos recursos) {
        for(int id=0;id<8;id++) {
            BufferedImage atlas=recursos.imagen(id<4?"mision1/transeuntes-direcciones-a":
                "mision1/transeuntes-direcciones-b");
            int fila=id%4;
            int[][] cajas=new int[4][];
            int maxAncho=1,maxAlto=1;
            for(int direccion=0;direccion<4;direccion++) {
                cajas[direccion]=limites(atlas,direccion,fila);
                maxAncho=Math.max(maxAncho,cajas[direccion][2]);
                maxAlto=Math.max(maxAlto,cajas[direccion][3]);
            }
            double escala=Math.min((ANCHO-2.0)/maxAncho,(ALTO-1.0)/maxAlto);
            for(int direccion=0;direccion<4;direccion++) {
                int[] caja=cajas[direccion];
                BufferedImage base=new BufferedImage(ANCHO,ALTO,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g=base.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                int ancho=Math.max(1,(int)Math.round(caja[2]*escala));
                int alto=Math.max(1,(int)Math.round(caja[3]*escala));
                int x=(ANCHO-ancho)/2,y=ALTO-alto;
                g.drawImage(atlas,x,y,x+ancho,y+alto,caja[0],caja[1],
                    caja[0]+caja[2],caja[1]+caja[3],null);
                g.dispose();
                for(int paso=0;paso<4;paso++) pasos[id][direccion][paso]=paso(base,id,direccion,paso);
            }
        }
    }

    BufferedImage imagen(int id,int direccion,int paso) {
        return pasos[id][Math.floorMod(direccion,4)][Math.floorMod(paso,4)];
    }

    private static int[] limites(BufferedImage atlas,int columna,int fila) {
        int x0=columna*atlas.getWidth()/4,x1=(columna+1)*atlas.getWidth()/4;
        int y0=fila*atlas.getHeight()/4,y1=(fila+1)*atlas.getHeight()/4;
        // Algunos atlas invaden la fila siguiente con zapatos. Se conserva la banda
        // vertical más larga de cada celda, que contiene a la persona completa.
        int bandaInicio=y0,bandaFin=y1,mejor=0,actual=-1;
        for(int y=y0;y<=y1;y++) {
            boolean hay=false;
            if(y<y1) for(int x=x0;x<x1;x++) if((atlas.getRGB(x,y)>>>24)>24) {hay=true;break;}
            if(hay&&actual<0) actual=y;
            if(!hay&&actual>=0) {
                if(y-actual>mejor) {mejor=y-actual;bandaInicio=actual;bandaFin=y;}
                actual=-1;
            }
        }
        int minX=x1,minY=y1,maxX=x0-1,maxY=y0-1;
        for(int y=bandaInicio;y<bandaFin;y++) for(int x=x0;x<x1;x++) if((atlas.getRGB(x,y)>>>24)>24) {
            minX=Math.min(minX,x);minY=Math.min(minY,y);
            maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);
        }
        if(maxX<minX) return new int[]{x0,y0,x1-x0,y1-y0};
        return new int[]{minX,minY,maxX-minX+1,maxY-minY+1};
    }

    private static BufferedImage paso(BufferedImage base,int id,int direccion,int indice) {
        if(indice==0) return base;
        BufferedImage imagen=new BufferedImage(ANCHO,ALTO,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=imagen.createGraphics();
        int signo=indice==1?-1:indice==3?1:0;
        int rebote=indice==2?-1:0;
        if(indice==2) {
            g.drawImage(base,0,-1,null);
            g.dispose();
            return imagen;
        }
        if(id==1||id==4||id==6||id==7) {
            // Faldas y abrigos largos conservan una sola silueta al balancearse.
            g.drawImage(base,signo,rebote,null);
            g.dispose();
            return imagen;
        }
        int cintura=35;
        if(direccion==0||direccion==3) {
            // Alternancia de piernas frontal y posterior.
            g.drawImage(base,signo-1,cintura+(signo<0?-1:0),
                ANCHO/2+signo-1,ALTO+(signo<0?-1:0),
                0,cintura,ANCHO/2,ALTO,null);
            g.drawImage(base,ANCHO/2+signo+1,cintura+(signo>0?-1:0),
                ANCHO+signo+1,ALTO+(signo>0?-1:0),
                ANCHO/2,cintura,ANCHO,ALTO,null);
        } else {
            // En perfil las piernas se superponen: el paso alterna avance y rebote.
            g.drawImage(base,signo,cintura+rebote,ANCHO+signo,ALTO+rebote,
                0,cintura,ANCHO,ALTO,null);
        }
        // El torso cubre la unión con los pasos para evitar cortes en la ropa.
        g.drawImage(base,signo,0,ANCHO+signo,cintura+3,
            0,0,ANCHO,cintura+3,null);
        g.dispose();
        return imagen;
    }
}
