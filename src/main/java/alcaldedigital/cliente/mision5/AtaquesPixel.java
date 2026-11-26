package alcaldedigital.cliente.mision5;

import alcaldedigital.compartido.batalla.EstadoBatalla.Obstaculo;
import alcaldedigital.compartido.batalla.PaqueteCruzado;
import alcaldedigital.compartido.batalla.PaqueteRastreador;
import alcaldedigital.servidor.batalla.MotorBatalla;
import processing.core.PApplet;
import processing.core.PImage;

/** Fotogramas de aviso, activación y recuperación dibujados desde el estado del servidor. */
final class AtaquesPixel {
    private static final int MAGENTA = 0xffeb67b1, TINTA = 0xff371531, BORDE = 0xffb54394;
    private static final int AMBAR = 0xffffd16a, LUZ = 0xffffe9ff;
    private static final int CIAN = 0xff80f9e5, PETROLEO = 0xff174b57, ESPUMA = 0xffd7fff5;
    private static final int[] INTENSIDAD = {190, 230, 255, 230, 203, 180};
    private static final int[] CARGA = {2, 3, 5, 7, 5, 3};
    // Seis siluetas de la chispa central. Cada fila contiene un motivo de 8×8 píxeles.
    private static final int[][] CHISPA = {
        {0,0,0x18,0x3c,0x3c,0x18,0,0},
        {0,0x18,0x18,0x7e,0x7e,0x18,0x18,0},
        {0x18,0x5a,0x3c,0xff,0xff,0x3c,0x5a,0x18},
        {0x81,0x42,0x24,0x18,0x18,0x24,0x42,0x81},
        {0,0x42,0,0x18,0x18,0,0x42,0},
        {0,0,0,0x18,0x18,0,0,0}
    };

    private AtaquesPixel(){}

    static void dibujar(PApplet p, Obstaculo a, int patron, boolean reducido) {
        p.pushStyle();
        p.noStroke();
        if(a.tipo()==3) plataforma(p,a,reducido);
        else if(a.edad()<a.aviso()) aviso(p,a,reducido);
        else if(a.tipo()==1) cian(p,a,reducido);
        else if(a.tipo()==2) haz(p,a,reducido);
        else descarga(p,a,patron,reducido);
        p.popStyle();
    }

    static void rastreador(PApplet p, Obstaculo a, PaqueteRastreador ritmo, PImage[] sprites, boolean reducido) {
        var fase=ritmo.fase(a);
        float x=a.x(),y=a.y(),lado=a.w(),cx=x+lado/2,cy=y+lado/2;
        p.pushStyle();
        p.noStroke();
        if (fase==PaqueteRastreador.Fase.DISIPACION) {
            disipar(p,cx,cy,a.edad()-ritmo.inicioDisipacion(a),reducido);
            p.popStyle();
            return;
        }

        float rapidez=(float)Math.hypot(a.vx(),a.vy());
        if (fase==PaqueteRastreador.Fase.FIJADO) {
            float ux=a.vx()/rapidez,uy=a.vy()/rapidez;
            float recorrido=rapidez*ritmo.embestida();
            float borde=lado/2*(Math.abs(ux)+Math.abs(uy));
            p.fill(AMBAR,165);
            for (int i=16;i<recorrido;i+=10) {
                float px=cx+ux*i,py=cy+uy*i;
                p.rect(px-uy*borde-2,py+ux*borde-2,4,4);
                p.rect(px+uy*borde-2,py-ux*borde-2,4,4);
            }
            p.fill(LUZ);
            reticula(p,cx+a.vx()*ritmo.embestida(),cy+a.vy()*ritmo.embestida());
        } else if (fase.haceDanio()&&!reducido) {
            int estela=fase==PaqueteRastreador.Fase.EMBESTIDA?5:3;
            estelaCuadrito(p,cx,cy,a.vx()/rapidez,a.vy()/rapidez,estela);
        }

        cuadroVirus(p,a,sprites,reducido,fase==PaqueteRastreador.Fase.FIJADO);
        float etiquetaY=y-MotorBatalla.AY>=17?y-15:y+lado+2;
        p.fill(TINTA);
        p.rect(cx-12,etiquetaY,24,14);
        p.fill(AMBAR);
        p.textAlign(PApplet.CENTER,PApplet.BASELINE);
        p.textSize(12);
        p.text("J"+(a.objetivo()+1),cx,etiquetaY+11);
        p.popStyle();
    }

    static void cruzado(PApplet p,Obstaculo a,PImage[] sprites,boolean reducido){
        float cx=a.x()+a.w()/2,cy=a.y()+a.h()/2;
        float rapidez=(float)Math.hypot(a.vx(),a.vy());
        float ux=a.vx()/rapidez,uy=a.vy()/rapidez;
        p.pushStyle();
        p.noStroke();
        int inicioDisipacion=PaqueteCruzado.inicioDisipacion(a);
        if(a.edad()>=inicioDisipacion){
            disipar(p,cx,cy,a.edad()-inicioDisipacion,reducido);
        }else{
            if(a.edad()<a.aviso()){
                float alcance=PaqueteCruzado.pasosRestantes(a)*rapidez;
                p.fill(AMBAR,125);
                for(int i=18;i<alcance;i+=12) p.rect(cx+ux*i-2,cy+uy*i-2,4,4);
                p.fill(LUZ);
                float puntaX=cx+ux*31,puntaY=cy+uy*31;
                p.rect(puntaX-2,puntaY-2,4,4);
                for(int signo=-1;signo<=1;signo+=2){
                    p.rect(puntaX-ux*5-signo*uy*4-2,puntaY-uy*5+signo*ux*4-2,4,4);
                }
            }else if(!reducido){
                estelaCuadrito(p,cx,cy,ux,uy,3);
            }
            cuadroVirus(p,a,sprites,reducido,false);
        }
        p.popStyle();
    }

    private static void cuadroVirus(PApplet p,Obstaculo a,PImage[] sprites,boolean reducido,boolean fijado){
        boolean aviso=a.edad()<a.aviso();
        int frame=reducido?0:a.edad()/5%sprites.length;
        p.tint(0xffffffff,aviso?145:255);
        p.image(sprites[frame],a.x(),a.y(),a.w(),a.h());
        p.noTint();
        p.fill(aviso||fijado?AMBAR:MAGENTA);
        esquinas(p,a.x(),a.y(),a.w(),a.h(),6);
        if(aviso) p.rect(a.x()+3,a.y()+a.h()-5,(a.w()-6)*a.edad()/a.aviso(),2);
    }

    private static void estelaCuadrito(PApplet p,float cx,float cy,float ux,float uy,int largo){
        for(int i=largo;i>0;i--){
            float px=cx-ux*(12+6*i),py=cy-uy*(12+6*i);
            p.fill(i==1?LUZ:AMBAR,210-i*30);
            p.rect(px-3,py-3,6,6);
        }
    }

    private static void disipar(PApplet p,float cx,float cy,int edad,boolean reducido){
        float radio=reducido?9:6+edad*.65f;
        p.fill(AMBAR,240-edad*24);
        esquinas(p,cx-radio,cy-radio,2*radio,2*radio,4);
        p.fill(LUZ,200-edad*22);
        p.rect(cx-2,cy-2,4,4);
    }

    private static int cuadro(int edad, boolean reducido) {
        return reducido?2:Math.floorMod(edad/4,INTENSIDAD.length);
    }

    private static void plataforma(PApplet p, Obstaculo a, boolean reducido){
        float x=a.x(),y=a.y(),w=a.w(),h=a.h();
        p.fill(0xff3d2857);p.rect(x,y,w,h);
        p.fill(0xff9874bc);p.rect(x,y+4,w,h-4);
        p.fill(AMBAR);p.rect(x,y,w,3);
        p.fill(0xffcbb0e5);
        for(int i=8;i+8<w;i+=16)p.rect(x+i,y+5,8,3);
        int cuadro=cuadro(a.edad(),reducido);
        p.fill(LUZ,INTENSIDAD[cuadro]);
        p.rect(x+3,y+1,7,2);p.rect(x+w-10,y+1,7,2);
    }

    private static void aviso(PApplet p, Obstaculo a, boolean reducido){
        boolean cian=a.tipo()==1;
        int color=cian?CIAN:AMBAR;
        float x=a.x(),y=a.y(),w=a.w(),h=a.h();
        if(a.vx()!=0){
            // Proyección de la pared que todavía está fuera de la arena. El hueco queda visible.
            x=MotorBatalla.AX;w=MotorBatalla.AW;
        }
        if(w<12||h<12){p.fill(color,120);p.rect(x,y,w,h);return;}
        int fotograma=cuadro(a.edad(),reducido);
        p.fill(color,cian?20:30);p.rect(x,y,w,h);
        p.fill(color,reducido?200:145+CARGA[fotograma]*14);
        esquinas(p,x,y,w,h,Math.min(18,Math.min(w,h)/3));
        float avance=(float)a.edad()/Math.max(1,a.aviso());
        if(a.tipo()==2&&h>w) p.rect(x+2,y+2,3,(h-4)*avance);
        else {
            p.rect(x+2,y+h-5,(w-4)*avance,3);
            if(w>80) for(int i=24;i<w-15;i+=32)p.rect(x+i,y+3,10,2);
        }
        if(a.vx()!=0){
            float entrada=a.vx()>0?MotorBatalla.AX+10:MotorBatalla.AX+MotorBatalla.AW-20;
            flecha(p,entrada,y+h/2,a.vx()>0?1:-1);
        }else if(cian) simboloQuietud(p,x+w/2,y+h/2);
        else if(a.tipo()==2) reticula(p,x+w/2,y+h/2);
    }

    private static void descarga(PApplet p, Obstaculo a, int patron, boolean reducido){
        int edad=a.edad()-a.aviso();
        int f=cuadro(edad,reducido);
        float x=a.x(),y=a.y(),w=a.w(),h=a.h();
        boolean vertical=h>w;
        int tono=patron==7||patron==12?0xffe75aa3:MAGENTA;
        if(w<18||h<18){p.fill(tono);p.rect(x,y,w,h);return;}
        p.fill(TINTA);p.rect(x,y,w,h);
        p.fill(tono,110+INTENSIDAD[f]/4);p.rect(x+2,y+2,w-4,h-4);
        p.fill(tono,225);esquinas(p,x,y,w,h,Math.min(14,Math.min(w,h)/3));
        p.fill(BORDE);
        if(vertical){
            for(float i=6;i+7<h;i+=18)p.rect(x+4,y+i,w-8,7);
        }else{
            for(float i=6;i+8<w;i+=24)p.rect(x+i,y+4,9,h-8);
        }
        p.fill(LUZ,180);
        if(vertical){
            p.rect(x+3,y+2,2,h-4);
            for(float i=10+f;i+16<h;i+=48)fragmento(p,x+w/2-8,y+i,f,patron);
        }else{
            p.rect(x+2,y+3,w-4,2);
            int sentido=patron==0&&(int)y%90>45?-1:1;
            for(float i=12+f*sentido;i+16<w;i+=44)fragmento(p,x+i,y+h/2-8,f,patron);
        }
        if(patron==13){p.fill(LUZ);esquinas(p,x,y,w,h,Math.min(12,Math.min(w,h)/3));}
    }

    private static void fragmento(PApplet p,float x,float y,int f,int patron){
        if(patron==13){
            p.fill(TINTA);p.rect(x+2,y,12,16);p.rect(x,y+2,16,12);
            p.fill(AMBAR,INTENSIDAD[f]);esquinas(p,x+2,y+2,12,12,4);
        }else if(patron==7||patron==12||patron==14){
            p.fill(TINTA);p.rect(x+4,y+1,8,8);p.rect(x+2,y+8,12,6);
            p.fill(LUZ,INTENSIDAD[f]);p.rect(x+6,y+5+f%4,4,6);
        }else{
            p.fill(TINTA);p.rect(x+1,y+3,14,10);
            p.fill(AMBAR,100+CARGA[f]*17);p.rect(x+3,y+5,6,2);p.rect(x+3,y+9,10,2);
            p.fill(LUZ);p.rect(x+10,y+5,2,2);
        }
    }

    private static void cian(PApplet p, Obstaculo a, boolean reducido){
        float x=a.x(),y=a.y(),w=a.w(),h=a.h();
        int f=cuadro(a.edad()-a.aviso(),reducido);
        p.fill(PETROLEO,140);p.rect(x,y,w,h);
        p.fill(CIAN,48+CARGA[f]*5);
        for(float i=5;i+2<h;i+=14)p.rect(x+2,y+i,w-4,2);
        p.fill(CIAN);esquinas(p,x,y,w,h,18);
        p.fill(ESPUMA);simboloQuietud(p,x+w/2,y+22);
    }

    private static void haz(PApplet p, Obstaculo a, boolean reducido){
        float x=a.x(),y=a.y(),w=a.w(),h=a.h();
        int f=cuadro(a.edad()-a.aviso(),reducido);
        p.fill(0xff5a1f50);p.rect(x,y,w,h);
        p.fill(MAGENTA,INTENSIDAD[f]);
        p.rect(x+2,y+2,w-4,h-4);
        p.fill(LUZ,210);
        int nucleo=4+CARGA[f];
        if(w>h){
            p.rect(x+2,y+(h-nucleo)/2,w-4,nucleo);
            for(float i=28;i+16<w;i+=86)chispa(p,x+i,y+h/2-8,f);
        }else{
            p.rect(x+(w-nucleo)/2,y+2,nucleo,h-4);
            for(float i=28;i+16<h;i+=69)chispa(p,x+w/2-8,y+i,f);
        }
    }

    private static void chispa(PApplet p,float x,float y,int f){
        p.fill(AMBAR,INTENSIDAD[f]);
        int[] silueta=CHISPA[f];
        for(int fila=0;fila<silueta.length;fila++)
            for(int bit=0;bit<8;bit++)if((silueta[fila]&(1<<bit))!=0)p.rect(x+bit*2,y+fila*2,2,2);
    }

    private static void esquinas(PApplet p,float x,float y,float w,float h,float largo){
        p.rect(x,y,largo,3);p.rect(x,y,3,largo);
        p.rect(x+w-largo,y,largo,3);p.rect(x+w-3,y,3,largo);
        p.rect(x,y+h-3,largo,3);p.rect(x,y+h-largo,3,largo);
        p.rect(x+w-largo,y+h-3,largo,3);p.rect(x+w-3,y+h-largo,3,largo);
    }

    private static void flecha(PApplet p,float x,float y,int sentido){
        p.rect(x-5,y-2,12,4);
        p.rect(x+(sentido>0?3:-7),y-6,4,12);
        p.rect(x+(sentido>0?7:-11),y-3,4,6);
    }

    private static void simboloQuietud(PApplet p,float x,float y){
        p.rect(x-8,y-7,5,14);p.rect(x+3,y-7,5,14);
    }

    private static void reticula(PApplet p,float x,float y){
        p.rect(x-12,y-1,8,2);p.rect(x+4,y-1,8,2);
        p.rect(x-1,y-10,2,6);p.rect(x-1,y+4,2,6);
    }
}
