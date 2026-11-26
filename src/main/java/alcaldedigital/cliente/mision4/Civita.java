package alcaldedigital.cliente.mision4;

import static java.awt.event.KeyEvent.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import processing.core.PApplet;

/** Laberinto, alumbrado e investigación del tramo posterior a la tarima. */
final class Civita {
    static final int BALDOSA=44, COLUMNAS=52, FILAS=39, ANCHO=COLUMNAS*BALDOSA, ALTO=FILAS*BALDOSA;
    static final int RADIO_LUZ=331;
    static final float APERTURA=.68f;
    enum Hoja { NINGUNA, EXPEDIENTE, CONTRASTE }
    record Punto(int columna,int fila) {
        float x(){return columna*BALDOSA+BALDOSA/2f;}
        float y(){return fila*BALDOSA+BALDOSA/2f;}
    }
    static Punto punto(int x,int y){return new Punto(x*COLUMNAS/PlanoCivita.ANCHO,y*FILAS/PlanoCivita.ALTO);}
    record Archivo(String nombre,String titular,String indicio,String fuente,String prueba,String contraste,
                   Punto papel,Punto terminal,int guardian) {}
    static final Archivo[] ARCHIVOS={
        new Archivo("La fuente oscura","«El alcalde contaminó la fuente»",
            "La publicación acusa al alcalde. El agua oscura no muestra el origen de los residuos.",
            "Registro de mantenimiento","La bitácora registra agua turbia durante la limpieza del filtro, antes de la visita.",
            "La bitácora explica cuándo apareció el agua turbia. La imagen no demuestra quién la contaminó.",
            punto(97,145),punto(242,111),0),
        new Archivo("La foto recortada","«Esta foto lo demuestra todo»",
            "La copia deja a Juan junto a la fuente y corta el lado derecho de la imagen.",
            "Imagen original · edición de hoy","En el encuadre completo se ve una brigada recogiendo residuos. Juan observa desde el andén.",
            "El recorte eliminó a la brigada. La misma foto no respalda la acusación que le pusieron encima.",
            punto(870,203),punto(815,275),-1),
        new Archivo("La red de los bots","«Miles de vecinos publicaron lo mismo»",
            "Varios perfiles repiten exactamente la misma frase en intervalos regulares.",
            "Registro de difusión · terminal","El historial identifica una publicación inicial y tres cuentas automáticas que la copiaron.",
            "El mismo origen explica los mensajes repetidos. Las copias no son testimonios independientes.",
            punto(170,567),punto(335,620),1),
        new Archivo("El sospechoso","«Vieron a un hombre de traje azul»",
            "Este retrato es un boceto basado en relatos. Ubica a una persona, pero no muestra sus acciones.",
            "Acta de testigos · fuente y hora","Los testigos vieron a una persona junto a la fuente después del aviso. Ninguno vio quién arrojó los residuos.",
            "La presencia cerca de la fuente no demuestra responsabilidad. La acusación sigue sin comprobarse.",
            punto(878,500),punto(682,676),-1)
    };
    static final Punto[] GUARDIANES={punto(140,274),punto(221,607),punto(598,337)};
    static final Punto[] FICHAS={punto(140,339),punto(861,343),punto(315,573),punto(811,674)};
    static final Punto NUCLEO=punto(615,382), INICIO=punto(73,380);
    static final float CENTRO_X=ANCHO*.5f,CENTRO_Y=ALTO*(384f/765);
    private static final String[] PISTAS={
        "Esta marca señala la bitácora del norte. Sigue el rastro dorado.",
        "El sello coincide con el archivo original. Su terminal queda marcado.",
        "Las copias salen en ráfagas. Cúbrete y usa la espada cuando cedan.",
        "El núcleo parpadea antes de atacar. Esquiva y golpea al terminar la ráfaga."
    };
    static final String[] FRASES={
        "¡Yo no necesito pruebas, solo que me compartan!",
        "¡Si lo repito mil veces, todos creerán que es verdad!",
        "¡Si corres, publicaré que huiste por culpable!"
    };
    private final boolean[][] piso=new boolean[FILAS][COLUMNAS];
    private static final int[][] ORTOGONALES={{1,0},{-1,0},{0,1},{0,-1}};
    private final int[][] distanciaGuia=new int[FILAS][COLUMNAS];
    final CollisionWorld colisiones=new CollisionWorld(ANCHO,ALTO);
    final CivitaArte arte=new CivitaArte();
    int vistos, contrastados, guardianes, fichas, expediente, combate=-1;
    boolean linterna, activa;
    Hoja hoja=Hoja.NINGUNA;
    float tiempoHoja, camX,camY;
    float rumbo,velX,velY,pulso,fraseTiempo,guiaTiempo,nucleoTiempo;
    int guia=-1,revelados;
    Punto destello;
    String aviso="";

    Civita() {
        for(int y=0;y<FILAS;y++) for(int x=0;x<COLUMNAS;x++){
            piso[y][x]=PlanoCivita.transitable((x+.5f)*PlanoCivita.ANCHO/COLUMNAS,(y+.5f)*PlanoCivita.ALTO/FILAS);
            if(!piso[y][x])colisiones.add(x*BALDOSA,y*BALDOSA,BALDOSA,BALDOSA);
        }
    }
    boolean suelo(int x,int y){return y>=0&&y<FILAS&&x>=0&&x<COLUMNAS&&piso[y][x];}
    private boolean suelo(float x,float y){return suelo((int)Math.floor(x/BALDOSA),(int)Math.floor(y/BALDOSA));}
    int total(){return Integer.bitCount(contrastados);}
    boolean visto(int id){return (vistos&(1<<id))!=0;}
    boolean contrastado(int id){return (contrastados&(1<<id))!=0;}
    boolean vencido(int id){return (guardianes&(1<<id))!=0;}
    static float angulo(Actor actor){return switch(actor.direction){case 0->PApplet.HALF_PI;case 1->-PApplet.HALF_PI;case 2->PApplet.PI;default->0;};}

    float alcance(float x,float y,float angulo,float maximo) {
        float dx=(float)Math.cos(angulo),dy=(float)Math.sin(angulo);
        for(float d=8;d<maximo;d+=8) if(!suelo(x+dx*d,y+dy*d))return d-3;
        return maximo;
    }
    boolean iluminado(Actor hero,Punto punto) {
        if(!linterna)return false;
        float dx=punto.x()-hero.x,dy=punto.y()-12-hero.y;
        float distancia=(float)Math.hypot(dx,dy);
        if(distancia>RADIO_LUZ)return false;
        float a=(float)Math.atan2(dy,dx), delta=a-rumbo;
        if(distancia>36&&Math.cos(delta)<Math.cos(APERTURA))return false;
        return alcance(hero.x,hero.y,a,distancia)>=distancia-1;
    }
    boolean cerca(Actor hero,Punto punto){return Math.hypot(hero.x-punto.x(),hero.y-punto.y())<=56;}

    void entrar(MisionCuatroGame g) {
        activa=true;g.hero.x=INICIO.x();g.hero.y=INICIO.y();g.hero.direction=3;
        g.loseFocus();camara(g.hero,true,0);
        aviso="Enciende la linterna y busca los expedientes pegados a las paredes.";
        g.set(MisionCuatroGame.Phase.CIVITA);
    }
    private void camara(Actor hero,boolean exacta,float dt) {
        float x=PApplet.constrain(hero.x-512,0,ANCHO-1024),y=PApplet.constrain(hero.y-290,0,ALTO-576);
        float k=exacta?1:Math.min(1,dt*7);
        camX+=(x-camX)*k;camY+=(y-camY)*k;
    }
    void actualizar(MisionCuatroGame g,float dt) {
        pulso=Math.max(0,pulso-dt);fraseTiempo=Math.max(0,fraseTiempo-dt);
        guiaTiempo=Math.max(0,guiaTiempo-dt);nucleoTiempo=Math.max(0,nucleoTiempo-dt);
        if(hoja!=Hoja.NINGUNA){tiempoHoja+=dt;return;}
        var hero=g.hero;var sistema=g.combat;
        if(hero.knockX!=0||hero.knockY!=0){colisiones.move(hero,hero.knockX,hero.knockY);hero.knockX=hero.knockY=0;}
        registrarVictoria(sistema);
        if(combate>=0&&sistema.health<=0) {
            sistema.health=5;spawn(g,combate);hero.invulnerable=1.5f;
            aviso="Juan se recuperó. La investigación sigue guardada.";
        }
        sistema.defend(combate>=0&&(g.keys[VK_K]||g.keys[VK_Q]));
        float dx=g.axis(VK_D,VK_RIGHT)-g.axis(VK_A,VK_LEFT),dy=g.axis(VK_S,VK_DOWN)-g.axis(VK_W,VK_UP);
        if(hero.dodge>0&&dx==0&&dy==0){dx=(float)Math.cos(angulo(hero));dy=(float)Math.sin(angulo(hero));}
        mover(hero,dx,dy,dt,sistema.guarding?.38f:sistema.golpeCivitaActivo()?.72f:1);
        if(fraseTiempo<1.65f)sistema.update(dt,hero,colisiones);
        registrarVictoria(sistema);
        camara(hero,false,dt);
        for(int i=0;i<ARCHIVOS.length;i++) if((revelados&(1<<i))==0&&iluminado(hero,ARCHIVOS[i].papel())){
            revelados|=1<<i;resaltar(ARCHIVOS[i].papel());
        }
        if(combate<0) for(int i=0;i<2;i++) if(!vencido(i)&&iluminado(hero,GUARDIANES[i])) {
            spawn(g,i);aviso=i==0?"¡Troll revelado! Usa la espada, el escudo y la esquiva.":
                "¡SpamBot revelado! Esquiva las ráfagas y acércate.";break;
        }
    }
    private void mover(Actor hero,float dx,float dy,float dt,float factor) {
        float n=(float)Math.hypot(dx,dy);
        if(n>0) {
            hero.direction=Math.abs(dx)>Math.abs(dy)?(dx<0?2:3):(dy<0?1:0);
            dx/=n;dy/=n;
        }
        float k=Math.min(1,dt*(n>0?24:31));
        velX+=(dx*hero.speed()*factor-velX)*k;velY+=(dy*hero.speed()*factor-velY)*k;
        float ox=hero.x,oy=hero.y;
        colisiones.move(hero,velX*dt,velY*dt);
        if(ox==hero.x)velX=0;if(oy==hero.y)velY=0;
        hero.walk+=(float)Math.hypot(hero.x-ox,hero.y-oy)/14;
        float delta=angulo(hero)-rumbo;
        rumbo+=(float)Math.atan2(Math.sin(delta),Math.cos(delta))*Math.min(1,dt*13);
    }
    private void resaltar(Punto punto){destello=punto;pulso=.72f;}
    private void spawn(MisionCuatroGame g,int id) {
        var punto=GUARDIANES[id];
        combate=id;g.encounter=id;g.combat.spawn(id,punto.x(),punto.y());
        fraseTiempo=2.7f;resaltar(punto);
    }
    void tecla(MisionCuatroGame g,int code) {
        boolean aceptar=code==VK_E||code==VK_ENTER;
        if(hoja!=Hoja.NINGUNA) {
            if(aceptar&&tiempoHoja>=.28f) {
                if(hoja==Hoja.CONTRASTE){
                    int antes=total();contrastados|=1<<expediente;g.approval=total();
                    if(antes<4&&total()==4)nucleoTiempo=2.4f;
                    aviso=total()==4?"Cuatro expedientes contrastados. Ve al núcleo del corredor central.":"Evidencia registrada en el expediente.";
                }
                hoja=Hoja.NINGUNA;g.loseFocus();
            }
            return;
        }
        if(code==VK_F){linterna=!linterna;rumbo=angulo(g.hero);aviso=linterna?"Linterna activa. La luz se orienta con Juan.":"Linterna apagada.";return;}
        if(code==VK_J&&combate>=0&&fraseTiempo<1.65f)g.combat.ataqueCivita(g.hero);
        if((code==VK_SPACE||code==VK_SHIFT)&&combate>=0)g.combat.dodge(g.hero);
        if(aceptar)interactuar(g);
    }
    private void registrarVictoria(CombatSystem sistema) {
        if(combate>=0&&sistema.enemy==null){
            guardianes|=1<<combate;combate=-1;
            if(vencido(2)){nucleoTiempo=3;aviso="El núcleo está encendido. Acércate al anillo dorado para volver.";}
            else aviso="Amenaza detenida. Continúa la investigación.";
        }
    }
    private void interactuar(MisionCuatroGame g) {
        var hero=g.hero;
        if(cerca(hero,NUCLEO)){
            if(total()<4){aviso="El núcleo pide los cuatro expedientes contrastados.";return;}
            if(vencido(2)){g.loseFocus();g.set(MisionCuatroGame.Phase.CIVITA_SALIDA);return;}
            if(combate>=0)return;
            if(!iluminado(hero,GUARDIANES[2])){
                aviso="Enciende F y apunta al borde noroeste del anillo para revelar al guardián.";return;
            }
            spawn(g,2);aviso="Deepfake bloquea la salida. Detecta el aviso y esquiva antes de atacar.";return;
        }
        if(combate>=0){aviso="Detén la amenaza para consultar el material.";return;}
        for(int i=0;i<ARCHIVOS.length;i++) {
            Archivo a=ARCHIVOS[i];
            if(cerca(hero,a.papel())){
                if(!iluminado(hero,a.papel())){aviso="Ilumina el papel con F y mira en su dirección.";return;}
                vistos|=1<<i;expediente=i;hoja=Hoja.EXPEDIENTE;tiempoHoja=0;velX=velY=0;g.loseFocus();
                aviso="Busca el terminal de este sector.";return;
            }
            if(cerca(hero,a.terminal())){
                if(!iluminado(hero,a.terminal())){aviso="Ilumina el terminal para leerlo.";return;}
                if(!visto(i)){aviso="Localiza primero el expediente de este sector.";return;}
                if(a.guardian()>=0&&!vencido(a.guardian())){
                    aviso="Ilumina y derrota al guardián que protege este registro.";return;
                }
                expediente=i;hoja=Hoja.CONTRASTE;tiempoHoja=0;velX=velY=0;g.loseFocus();return;
            }
            if((fichas&(1<<i))==0&&cerca(hero,FICHAS[i])&&iluminado(hero,FICHAS[i])){
                fichas|=1<<i;guiar(i);aviso=PISTAS[i];resaltar(FICHAS[i]);return;
            }
        }
    }
    private void guiar(int pista) {
        guia=pista;guiaTiempo=18;
        var meta=pista==3?NUCLEO:ARCHIVOS[pista].terminal();
        for(var fila:distanciaGuia)Arrays.fill(fila,-1);
        var cola=new ArrayDeque<Punto>();cola.add(meta);
        distanciaGuia[meta.fila()][meta.columna()]=0;
        while(!cola.isEmpty()){
            var p=cola.removeFirst();
            for(var delta:ORTOGONALES){
                int x=p.columna()+delta[0],y=p.fila()+delta[1];
                if(!suelo(x,y)||distanciaGuia[y][x]>=0)continue;
                distanciaGuia[y][x]=distanciaGuia[p.fila()][p.columna()]+1;
                cola.addLast(new Punto(x,y));
            }
        }
    }
    List<Punto> rastro(Actor hero){
        if(guiaTiempo<=0)return List.of();
        int x=(int)(hero.x/BALDOSA),y=(int)(hero.y/BALDOSA);
        if(!suelo(x,y))return List.of();
        var pasos=new ArrayList<Punto>(7);
        while(pasos.size()<7&&distanciaGuia[y][x]>0){
            int siguiente=distanciaGuia[y][x]-1;
            boolean encontrado=false;
            for(var delta:ORTOGONALES){
                int xx=x+delta[0],yy=y+delta[1];
                if(!suelo(xx,yy)||distanciaGuia[yy][xx]!=siguiente)continue;
                x=xx;y=yy;pasos.add(new Punto(x,y));encontrado=true;break;
            }
            if(!encontrado)break;
        }
        return pasos;
    }
    String accionCercana(Actor hero,String investigar,String iluminar) {
        if(cerca(hero,NUCLEO))return investigar+" · examinar el núcleo";
        for(int i=0;i<ARCHIVOS.length;i++){
            Archivo a=ARCHIVOS[i];
            if(cerca(hero,a.papel()))return iluminado(hero,a.papel())?investigar+" · leer expediente "+(i+1):iluminar+" · ilumina el papel";
            if(cerca(hero,a.terminal()))return iluminado(hero,a.terminal())?investigar+" · consultar terminal "+(i+1):iluminar+" · ilumina el terminal";
            if((fichas&(1<<i))==0&&cerca(hero,FICHAS[i])&&iluminado(hero,FICHAS[i]))return investigar+" · recoger ficha de red";
        }
        return "";
    }
    void guardar(Properties p) {
        if(!activa)return;
        p.setProperty("civita.activa","true");
        p.setProperty("civita.mapa","2");
        p.setProperty("civita.vistos",""+vistos);p.setProperty("civita.verificados",""+contrastados);
        p.setProperty("civita.guardianes",""+guardianes);p.setProperty("civita.fichas",""+fichas);
        p.setProperty("civita.linterna",""+linterna);p.setProperty("civita.combate",""+combate);
    }
    void restaurar(MisionCuatroGame g,Properties p) {
        activa=Boolean.parseBoolean(p.getProperty("civita.activa"));
        if(!activa)return;
        vistos=Integer.parseInt(p.getProperty("civita.vistos"))&15;contrastados=Integer.parseInt(p.getProperty("civita.verificados"))&vistos;
        guardianes=Integer.parseInt(p.getProperty("civita.guardianes"))&7;fichas=Integer.parseInt(p.getProperty("civita.fichas"))&15;
        linterna=Boolean.parseBoolean(p.getProperty("civita.linterna"));
        boolean anterior=!"2".equals(p.getProperty("civita.mapa"));
        if((g.scenes.current==MisionCuatroGame.Phase.CIVITA||g.scenes.current==MisionCuatroGame.Phase.CIVITA_SALIDA)&&
            (anterior||colisiones.blocked(g.hero.x,g.hero.y,g.hero.radius))) {
            Punto regreso=g.scenes.current==MisionCuatroGame.Phase.CIVITA?INICIO:NUCLEO;
            g.hero.x=regreso.x();g.hero.y=regreso.y();
        }
        rumbo=angulo(g.hero);revelados=vistos;
        int id=Integer.parseInt(p.getProperty("civita.combate"));
        if(id>=0&&id<3&&!vencido(id)&&g.scenes.current==MisionCuatroGame.Phase.CIVITA)spawn(g,id);
        camara(g.hero,true,0);aviso="Continúa la investigación. El progreso quedó guardado.";
    }
}
