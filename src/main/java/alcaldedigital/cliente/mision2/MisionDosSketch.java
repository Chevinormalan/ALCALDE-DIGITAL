package alcaldedigital.cliente.mision2;

import alcaldedigital.cliente.audio.SonidosRemaster;

import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.presentacion.LuzEscena;
import alcaldedigital.cliente.presentacion.PosesRemaster;
import processing.core.PApplet;
import processing.core.PImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/** Misión 2 sobre el mismo lienzo Processing que las demás pantallas. */
public final class MisionDosSketch {
    private String partida=java.util.UUID.randomUUID().toString();
    public String partida(){return partida;}
    public Dificultad dificultad(){return dificultad;}
    public record Resultado(boolean terminada,boolean exito,int protegidos,float segundosRestantes,
                            boolean falloCritico,float coberturaMedia) {
        public Resultado(boolean terminada,boolean exito,int protegidos,float segundosRestantes) {
            this(terminada,exito,protegidos,segundosRestantes,false,0);
        }
        public Resultado(boolean terminada,boolean exito,int protegidos,float segundosRestantes,boolean falloCritico) {
            this(terminada,exito,protegidos,segundosRestantes,falloCritico,0);
        }
    }
    public enum Dificultad {
        FACIL(60,10), NORMAL(60,6), DIFICIL(50,4);
        private final int segundos;
        private final float intervaloAtaque;
        Dificultad(int segundos,float intervaloAtaque) {
            this.segundos=segundos;this.intervaloAtaque=intervaloAtaque;
        }
        public int segundos() {return segundos;}
        public float intervaloAtaque() {return intervaloAtaque;}
    }
    private enum Fase { ENTRADA, INTRO, SALIDA, ALARMA, DEFENSA, FALLO_CRITICO, REGRESO, FINAL, REMATE, DESPEDIDA, RESULTADO }
    private static final float ULTIMA_REPARACION=8;
    private float coberturaAcumulada,tiempoDefensa;
    private static final int W=1280,H=720;
    private static final float VELOCIDAD_CAMINAR=300,VELOCIDAD_CORRER=450;
    private static final float DURACION_SALTO=.30f,VELOCIDAD_SALTO=520,ESPERA_SALTO=.65f;
    private static final float[] TX={313,313,313,967,967,967};
    private static final float[] TY={232,400,565,232,400,565};
    private static final float[][] BLOQUEOS={
        {0,0,1280,126},{0,690,1280,30},{0,0,42,720},{1238,0,42,720},
        {119,150,180,94},{119,315,180,94},{119,480,180,94},
        {981,150,180,94},{981,315,180,94},{981,480,180,94},
        {563,256,154,203},{539,568,202,92},
        {45,268,70,145},{1168,255,70,145},{45,573,70,110},{1168,510,70,165}
    };
    private static final String[][] INTRO={
        {"MR. PALOMO","¡Ey, periodista! ¿Ya conoces Civitas? La mejor red para estar al día."},
        {"PERIODISTA","¿Una paloma acaba de entrar a mi oficina para venderme una red social?"},
        {"MR. PALOMO","La publicidad de altura llega a todas las ventanas."},
        {"PERIODISTA","Pues vuelve a salir por esta."}
    };
    private static final String[][] ALARMA={
        {"PERIODISTA","Uf, esas plagas cada vez se extienden más por la ciudad."},
        {"CIVITAS","ALERTA. La red está bajo ataque: los computadores pueden caer."},
        {"PERIODISTA","Debo mantenerlos seguros hasta que termine el plazo."}
    };
    private static final String[][] EXITO={
        {"MR. PALOMO","Veo que mi red sigue en pie."},
        {"PERIODISTA","Los seis equipos están a salvo. No pienso discutir de quién es la red."},
        {"MR. PALOMO","Entonces te dejo una pequeña reseña del servicio."},
        {"PERIODISTA","No vuelvo a patear una paloma."}
    };
    private static final String[][] FALLO={
        {"MR. PALOMO","¿Terminaste? Esa luz roja parece bastante insistente."},
        {"PERIODISTA","No. Aléjate del núcleo; todavía está inestable."},
        {"MR. PALOMO","Claro. No tocaré nada. Ni siquiera moveré la cola."},
        {"MR. PALOMO","Ups. Eso no estaba en el manual de Civitas."},
        {"PERIODISTA","Y yo que pensaba que el hackeo era mi mayor problema."}
    };
    private static final String[][] FALLO_NUCLEO={
        {"CIVITAS","FALLO CRÍTICO. El núcleo ha perdido la sincronización."},
        {"PERIODISTA","Ya no puedo recuperar esta defensa. Debo reiniciar la red."},
        {"MR. PALOMO","En el siguiente intento, el núcleo sí podrá mantenerse estable."}
    };

    private PApplet p;
    private PImage fondo;
    private final PImage[][] periodista=new PImage[4][4];
    private final PImage[][] carrera=new PImage[4][4];
    private final PImage[][] salto=new PImage[4][4];
    private final PImage[] acciones=new PImage[4];
    private PImage palomoHabla;
    private PImage fotograma;
    private Fase fase=Fase.ENTRADA;
    private final RedCivitas red=new RedCivitas();
    private final Dificultad dificultad;
    private final alcaldedigital.compartido.guardado.AzarPartida ataques;
    private final boolean falloCriticoProgramado;
    private final Set<Integer> pulsadas=new HashSet<>();
    private final alcaldedigital.cliente.presentacion.AudioEscena sonidos=new alcaldedigital.cliente.presentacion.AudioEscena("M2");
    private float px=640,py=672, palomoX=1340,palomoY=86, vuelo=0, restante;
    private boolean movimientoReducido;
    public void movimientoReducido(boolean valor) { movimientoReducido = valor; }

    private float mandoX,mandoY,animacion, avisoTiempo,interaccionTiempo,remateTiempo,proximoAtaque,tiempoFalloCritico;
    private float saltoTiempo,esperaSalto,saltoDX,saltoDY;
    private String aviso="";
    private int direccion=0,linea=0,letras=0,ultimoCuadro=-1,sorteosRealizados;
    private boolean andando,corriendo,enSalto,mandoCorriendo,terminada,exito,falloCriticoActivado,ataqueFinalActivado;
    private long anterior;

    public MisionDosSketch() {this(Dificultad.NORMAL,20260917L);}
    public MisionDosSketch(Dificultad dificultad,long semilla) {this(dificultad,semilla,false);}
    public MisionDosSketch(Dificultad dificultad,long semilla,boolean falloCriticoProgramado) {
        this.dificultad=dificultad;
        this.falloCriticoProgramado=dificultad==Dificultad.DIFICIL&&falloCriticoProgramado;
        ataques=new alcaldedigital.compartido.guardado.AzarPartida(semilla);
        restante=dificultad.segundos();
        proximoAtaque=dificultad.intervaloAtaque();
    }

    public void iniciarEn(PApplet anfitrion) {
        p=anfitrion;
        fondo=imagen("/cliente/mision2/oficina.png");
        cargarHoja("/cliente/mision2/periodista-caminar-hoja.png",periodista);
        cargarHoja("/cliente/mision2/periodista-correr-hoja.png",carrera);
        cargarHoja("/cliente/mision2/periodista-salto-hoja.png",salto);
        PImage poses=imagen("/cliente/mision2/periodista-acciones-hoja.png");
        for(int fila=0;fila<2;fila++) for(int col=0;col<2;col++) {
            int x=col*poses.width/2,y=fila*poses.height/2;
            acciones[fila*2+col]=recortar(poses.get(x,y,
                (col+1)*poses.width/2-x,(fila+1)*poses.height/2-y));
        }
        palomoHabla=imagen("/cliente/mision3/redisenio/palomo_talk_1.png");
        for(String sonido:new String[]{"ui","alert","ping","hit"}) cargarSonido(sonido);
        anterior=System.nanoTime();
        String vista=System.getProperty("ad.mision2.previsualizar","");
        if("salida".equals(vista)) {fase=Fase.SALIDA;vuelo=0;palomoX=795;palomoY=532;}
        else if("defensa".equals(vista)) comenzarDefensa();
        else if("servidor-normal".equals(vista)) {
            comenzarDefensa();
            for(int i=0;i<RedCivitas.ESTACIONES;i++) red.proteger(i);
        }
        else if("carrera".equals(vista)) {
            comenzarDefensa();px=760;py=470;pulsadas.add(68);pulsadas.add(16);
        } else if("salto".equals(vista)) {
            comenzarDefensa();px=760;py=470;direccion=3;iniciarSalto();
        }
        else if("exito".equals(vista)||"fallo".equals(vista)||"remate-exito".equals(vista)||"remate-fallo".equals(vista)) {
            if(vista.endsWith("fallo")) for(int i=0;i<6;i++) red.ponerEnPeligro(i);
            terminada=true;exito=vista.endsWith("exito");fase=vista.startsWith("remate")?Fase.REMATE:Fase.FINAL;
            palomoX=795;palomoY=532;linea=3;letras=textoActual().length();
        }
    }

    private void cargarHoja(String ruta,PImage[][] destino) {
        PImage hoja=imagen(ruta);
        for(int fila=0;fila<4;fila++) for(int col=0;col<4;col++) {
            int x=col*hoja.width/4,y=fila*hoja.height/4;
            destino[fila][col]=recortar(hoja.get(x,y,
                (col+1)*hoja.width/4-x,(fila+1)*hoja.height/4-y));
        }
    }

    private static PImage recortar(PImage imagen) {
        imagen.loadPixels();int minX=imagen.width,minY=imagen.height,maxX=-1,maxY=-1;
        for(int y=0;y<imagen.height;y++) for(int x=0;x<imagen.width;x++)
            if((imagen.pixels[y*imagen.width+x]>>>24)>10) {
                minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);
            }
        if(maxX<0) return imagen;
        return imagen.get(Math.max(0,minX-3),Math.max(0,minY-3),
            Math.min(imagen.width,maxX+4)-Math.max(0,minX-3),
            Math.min(imagen.height,maxY+4)-Math.max(0,minY-3));
    }

    private PImage imagen(String ruta) {
        try(InputStream entrada=MisionDosSketch.class.getResourceAsStream(ruta)) {
            if(entrada==null) throw new IllegalStateException("Falta recurso: "+ruta);
            return new PImage(ImageIO.read(entrada));
        } catch(IOException e) {throw new IllegalStateException("Imagen inválida: "+ruta,e);}
    }

    private static String rutaSonido(String nombre) {
        return switch(nombre) {
            case "alert"->SonidosRemaster.ruta("red-alarma");
            case "ping"->SonidosRemaster.ruta("red-estacion");
            case "hit"->SonidosRemaster.ruta("red-fallo");
            default->"mision3/audio/"+nombre;
        };
    }
    private void cargarSonido(String nombre) {alcaldedigital.cliente.audio.MotorAudio.global().precargar(rutaSonido(nombre));}
    private void sonar(String nombre) {sonidos.efecto(nombre,rutaSonido(nombre),.52f,.08);}
    public alcaldedigital.cliente.audio.Partitura.Contexto contextoAudio(){return new alcaldedigital.cliente.audio.Partitura.Contexto(2,fase.name(),Math.max(1-restante/dificultad.segundos(),(6-red.protegidas())/6f),0,fase!=Fase.DEFENSA,exito,"");}

    public java.util.Properties capturar(){
        var p=new java.util.Properties();p.setProperty("version","1");p.setProperty("dificultad",dificultad.name());p.setProperty("critico",Boolean.toString(falloCriticoProgramado));p.setProperty("azar",Long.toString(ataques.estado()));
        alcaldedigital.compartido.guardado.CamposEstado.escribir(this,p,"estado.");
        for(int i=0;i<6;i++)p.setProperty("equipo."+i,Boolean.toString(red.protegida(i)));return p;
    }
    public static MisionDosSketch restaurar(java.util.Properties p){
        if(!"1".equals(p.getProperty("version")))throw new IllegalArgumentException("Versión de misión 2 inválida");
        var m=new MisionDosSketch(Dificultad.valueOf(p.getProperty("dificultad")),0,Boolean.parseBoolean(p.getProperty("critico")));
        alcaldedigital.compartido.guardado.CamposEstado.leer(m,p,"estado.");m.ataques.restaurar(Long.parseLong(p.getProperty("azar")));
        for(int i=0;i<6;i++)if(Boolean.parseBoolean(p.getProperty("equipo."+i)))m.red.proteger(i);else m.red.ponerEnPeligro(i);
        if(m.restante<0||m.restante>m.dificultad.segundos()||m.px<0||m.px>1280||m.py<0||m.py>720)throw new IllegalArgumentException("Estado de misión 2 inválido");
        m.perderFoco();m.anterior=0;return m;
    }
    public void aplicarGuardado(java.util.Properties p){alcaldedigital.compartido.guardado.CamposEstado.leer(this,p,"estado.");ataques.restaurar(Long.parseLong(p.getProperty("azar")));for(int i=0;i<6;i++)if(Boolean.parseBoolean(p.getProperty("equipo."+i)))red.proteger(i);else red.ponerEnPeligro(i);perderFoco();reanudar();}
    public Resultado resultado() {return new Resultado(terminada,exito,red.protegidas(),restante,
        falloCriticoActivado,tiempoDefensa==0?0:coberturaAcumulada/tiempoDefensa);}
    public boolean pantallaResultado(){return fase==Fase.RESULTADO;}
    public PImage fotograma() {return fotograma==null?p.get():fotograma;}
    public void cerrar() {
        sonidos.close();
        p=null;fotograma=null;pulsadas.clear();
    }
    public void reanudar() {anterior=System.nanoTime();}
    public void perderFoco() {pulsadas.clear();mandoX=mandoY=0;mandoCorriendo=false;}

    public void dibujarEnJuego() {
        long ahora=System.nanoTime();
        float dt=PApplet.constrain((ahora-anterior)/1_000_000_000f,0,.08f);
        anterior=ahora;
        actualizar(dt);
        dibujar();
        fotograma=p.get();
    }

    public void actualizarEstado(float dt){actualizar(Math.max(0,Math.min(.08f,dt)));}
    private void actualizar(float dt) {
        animacion+=dt;
        esperaSalto=Math.max(0,esperaSalto-dt);
        interaccionTiempo=Math.max(0,interaccionTiempo-dt);
        if(avisoTiempo>0) avisoTiempo=Math.max(0,avisoTiempo-dt);
        switch(fase) {
            case ENTRADA -> {
                vuelo=Math.min(1,vuelo+dt/1.25f);
                palomoX=PApplet.lerp(1340,795,vuelo);
                palomoY=PApplet.lerp(85,532,vuelo);
                if(vuelo>=1) {fase=Fase.INTRO;linea=letras=0;}
            }
            case INTRO,ALARMA,FINAL -> {letras=Math.min(textoActual().length(),letras+Math.max(1,(int)(dt*82)));}
            case SALIDA -> {
                vuelo=Math.min(1,vuelo+dt/1.0f);
                palomoX=PApplet.lerp(795,1340,vuelo);
                palomoY=PApplet.lerp(532,80,vuelo);
                if(vuelo>=1) {fase=Fase.ALARMA;linea=letras=0;sonar("alert");}
            }
            case DEFENSA -> {
                mover(dt);
                tiempoDefensa+=dt;coberturaAcumulada+=dt*red.protegidas()/6f;
                restante=Math.max(0,restante-dt);
                proximoAtaque-=dt;
                if(falloCriticoProgramado&&restante<=dificultad.segundos()-12) {
                    falloCriticoActivado=true;fase=Fase.FALLO_CRITICO;
                    tiempoFalloCritico=0;
                    if(red.completa()) red.ponerEnPeligro(ataques.nextInt(RedCivitas.ESTACIONES));
                    sonar("hit");
                    break;
                }
                if(restante>ULTIMA_REPARACION) while(proximoAtaque<=0) {
                    atacarOleada(tamanoOleada());proximoAtaque+=dificultad.intervaloAtaque();
                }
                if(restante>0&&restante<=2&&!ataqueFinalActivado) {
                    ataqueFinalActivado=true;
                    atacarOleada(1,true);
                }
                if(restante<=0) finalizar(red.completa());
            }
            case FALLO_CRITICO -> {
                tiempoFalloCritico+=dt;
                if(tiempoFalloCritico>=2.4f) finalizar(false);
            }
            case REGRESO -> {
                vuelo=Math.min(1,vuelo+dt/1.15f);
                palomoX=PApplet.lerp(1340,795,vuelo);
                palomoY=PApplet.lerp(80,532,vuelo);
                if(vuelo>=1) {fase=Fase.FINAL;linea=letras=0;}
            }
            case REMATE -> {
                remateTiempo+=dt;
                if(remateTiempo>=1.5f) {fase=Fase.FINAL;letras=0;}
            }
            case DESPEDIDA -> {
                vuelo=Math.min(1,vuelo+dt/1.0f);
                palomoX=PApplet.lerp(795,1340,vuelo);
                palomoY=PApplet.lerp(532,75,vuelo);
                if(vuelo>=1) fase=Fase.RESULTADO;
            }
            case RESULTADO -> { }
        }
    }

    private void mover(float dt) {
        if(enSalto) {avanzarSalto(dt);return;}
        float dx=(pulsadas.contains(68)||pulsadas.contains(39)?1:0)-
            (pulsadas.contains(65)||pulsadas.contains(37)?1:0);
        float dy=(pulsadas.contains(83)||pulsadas.contains(40)?1:0)-
            (pulsadas.contains(87)||pulsadas.contains(38)?1:0);
        if(dx==0&&dy==0) {dx=mandoX;dy=mandoY;}
        float longitud=PApplet.sqrt(dx*dx+dy*dy);
        andando=longitud>.05f;
        corriendo=andando&&(pulsadas.contains(16)||mandoCorriendo);
        if(!andando) return;
        if(longitud>1) {dx/=longitud;dy/=longitud;}
        direccion=Math.abs(dx)>Math.abs(dy)?(dx<0?2:3):(dy<0?1:0);
        moverConColision(dx,dy,dt*(corriendo?VELOCIDAD_CORRER:VELOCIDAD_CAMINAR));
    }

    private void moverConColision(float dx,float dy,float distancia) {
        int pasos=Math.max(1,(int)Math.ceil(distancia/5));
        for(int i=0;i<pasos;i++) {
            float nx=px+dx*distancia/pasos, ny=py+dy*distancia/pasos;
            if(libre(nx,py)) px=nx;
            if(libre(px,ny)) py=ny;
        }
    }

    private void iniciarSalto() {
        if(fase!=Fase.DEFENSA||enSalto||esperaSalto>0) return;
        float dx=(pulsadas.contains(68)||pulsadas.contains(39)?1:0)-
            (pulsadas.contains(65)||pulsadas.contains(37)?1:0);
        float dy=(pulsadas.contains(83)||pulsadas.contains(40)?1:0)-
            (pulsadas.contains(87)||pulsadas.contains(38)?1:0);
        if(dx==0&&dy==0) {dx=mandoX;dy=mandoY;}
        if(PApplet.abs(dx)+PApplet.abs(dy)<.05f) {
            dx=direccion==2?-1:direccion==3?1:0;
            dy=direccion==1?-1:direccion==0?1:0;
        }
        float longitud=PApplet.sqrt(dx*dx+dy*dy);
        saltoDX=dx/longitud;saltoDY=dy/longitud;
        direccion=Math.abs(saltoDX)>Math.abs(saltoDY)?(saltoDX<0?2:3):(saltoDY<0?1:0);
        saltoTiempo=0;esperaSalto=ESPERA_SALTO;
        enSalto=true;andando=false;corriendo=false;
    }

    private void avanzarSalto(float dt) {
        float paso=Math.min(dt,DURACION_SALTO-saltoTiempo);
        moverConColision(saltoDX,saltoDY,paso*VELOCIDAD_SALTO);
        saltoTiempo+=paso;
        if(saltoTiempo>=DURACION_SALTO-.0001f) {saltoTiempo=DURACION_SALTO;enSalto=false;}
    }

    private boolean libre(float x,float y) {
        for(float[] r:BLOQUEOS)
            if(x+14>r[0]&&x-14<r[0]+r[2]&&y+8>r[1]&&y-8<r[1]+r[3]) return false;
        return true;
    }

    public void pulsarTecla(char tecla,int codigo) {
        if(codigo==32) {
            if(!pulsadas.add(codigo)) return;
            if(fase==Fase.DEFENSA) iniciarSalto();else avanzar();
        } else if(codigo==69||codigo==10||codigo==13) {
            if(!pulsadas.add(codigo)) return;
            avanzar();
        } else pulsadas.add(codigo);
    }
    public void soltarTecla(char tecla,int codigo) {pulsadas.remove(codigo);}
    public void estadoMando(EstadoMando estado) {
        mandoX=estado.izquierdaX();mandoY=estado.izquierdaY();
        mandoCorriendo=estado.pulsado(EstadoMando.Boton.RB);
        if(estado.pulsado(EstadoMando.Boton.IZQUIERDA)) mandoX=-1;
        if(estado.pulsado(EstadoMando.Boton.DERECHA)) mandoX=1;
        if(estado.pulsado(EstadoMando.Boton.ARRIBA)) mandoY=-1;
        if(estado.pulsado(EstadoMando.Boton.ABAJO)) mandoY=1;
    }
    public void accionMando(EstadoMando.Boton boton) {
        if(boton==EstadoMando.Boton.A) avanzar();
        else if(boton==EstadoMando.Boton.X) iniciarSalto();
    }

    private void avanzar() {
        if(fase==Fase.RESULTADO) {confirmarResultado();return;}
        if(fase==Fase.DEFENSA) {interactuar();return;}
        if(fase!=Fase.INTRO&&fase!=Fase.ALARMA&&fase!=Fase.FINAL) return;
        sonar("ui");
        String actual=textoActual();
        if(letras<actual.length()) {letras=actual.length();return;}
        if(fase==Fase.FINAL&&linea==2&&!falloCriticoActivado) {
            linea=3;fase=Fase.REMATE;remateTiempo=0;
            sonar(exito?"ui":"hit");return;
        }
        linea++;letras=0;
        if(linea<dialogo().length) return;
        linea=0;
        if(fase==Fase.INTRO) {fase=Fase.SALIDA;vuelo=0;sonar("hit");}
        else if(fase==Fase.ALARMA) comenzarDefensa();
        else {fase=Fase.DESPEDIDA;vuelo=0;}
    }

    private void interactuar() {
        if(enSalto) return;
        int cercano=-1;float distancia=Float.MAX_VALUE;
        for(int i=0;i<6;i++) {
            float d=PApplet.dist(px,py,TX[i],TY[i]);
            if(d<distancia&&d<91&&red.enPeligro(i)) {cercano=i;distancia=d;}
        }
        if(cercano<0) {avisar("Acércate a un computador en peligro.");return;}
        red.proteger(cercano);
        interaccionTiempo=.28f;
        sonar("ping");
        avisar("PC "+(cercano+1)+" a salvo · "+red.protegidas()+"/6");
    }

    private void comenzarDefensa() {
        fase=Fase.DEFENSA;restante=dificultad.segundos();
        proximoAtaque=dificultad.intervaloAtaque();
        ataqueFinalActivado=false;
        atacarOleada(1);
    }
    private int tamanoOleada() {
        int tirada=ataques.nextInt(100);
        return switch(dificultad) {
            case FACIL -> tirada<75?1:tirada<95?2:3;
            case NORMAL -> tirada<55?1:tirada<90?2:3;
            case DIFICIL -> tirada<40?1:tirada<80?2:3;
        };
    }
    private void atacarOleada(int cantidad) {atacarOleada(cantidad,false);}
    private void atacarOleada(int cantidad,boolean finalDelReloj) {
        sorteosRealizados++;
        List<Integer> seguros=new ArrayList<>();
        for(int pc=0;pc<RedCivitas.ESTACIONES;pc++) if(red.protegida(pc)) seguros.add(pc);
        Collections.shuffle(seguros,ataques);
        int nuevos=Math.min(cantidad,seguros.size());
        if(nuevos==0) {
            if(finalDelReloj) avisar("¡ALERTA FINAL! Todos los PC ya están en peligro.");
            return;
        }
        StringBuilder alerta=new StringBuilder(finalDelReloj?"¡ATAQUE FINAL: ":"¡ATAQUE: ");
        for(int i=0;i<nuevos;i++) {
            int pc=seguros.get(i);
            red.ponerEnPeligro(pc);
            if(i>0) alerta.append(", ");
            alerta.append("PC ").append(pc+1);
        }
        avisar(alerta.append("! Pulsa E para defender.").toString());
        sonar("alert");
    }

    private void finalizar(boolean ganada) {
        terminada=true;exito=ganada;fase=Fase.REGRESO;vuelo=0;andando=false;corriendo=false;enSalto=false;
        sonar(ganada?"ping":"hit");
        pulsadas.clear();mandoX=mandoY=0;
    }
    private void avisar(String mensaje) {aviso=mensaje;avisoTiempo=2.1f;}

    private String[][] dialogo() {return fase==Fase.INTRO?INTRO:fase==Fase.ALARMA?ALARMA:
        falloCriticoActivado?FALLO_NUCLEO:exito?EXITO:FALLO;}
    private String textoActual() {return dialogo()[Math.min(linea,dialogo().length-1)][1];}

    private void dibujar() {
        p.background(29,13,48);
        boolean hablando=fase==Fase.INTRO||fase==Fase.ALARMA||fase==Fase.FINAL;
        float focoX=hablando&&dialogo()[linea][0].equals("MR. PALOMO")?palomoX:px;
        float focoY=hablando&&dialogo()[linea][0].equals("MR. PALOMO")?palomoY:py;
        p.pushMatrix();
        if(hablando) {
            float z=1.025f;
            p.translate(W/2f-z*W/2f+(W/2f-focoX)*.045f,
                H/2f-z*H/2f+(H/2f-focoY)*.045f);
            p.scale(z);
        }
        p.image(fondo,0,0,W,H);
        if(fase==Fase.DEFENSA||fase==Fase.FALLO_CRITICO||terminada) {
            dibujarEnlaces();
            dibujarServidor();dibujarEstaciones();
        }
        dibujarPeriodista();
        if(fase==Fase.ENTRADA||fase==Fase.INTRO||fase==Fase.SALIDA||fase==Fase.REGRESO||fase==Fase.FINAL||fase==Fase.REMATE||fase==Fase.DESPEDIDA)
            dibujarPalomo();
        dibujarEspanto();
        dibujarRemate();
        p.popMatrix();
        if(fase==Fase.DEFENSA||fase==Fase.FALLO_CRITICO||terminada) dibujarHUD();
        else if(fase!=Fase.RESULTADO) dibujarRotulo();
        if(hablando) dibujarDialogo();
        if(fase==Fase.FALLO_CRITICO) dibujarFalloCritico();
        if(fase==Fase.RESULTADO) dibujarResultado();
        if(avisoTiempo>0&&fase==Fase.DEFENSA) {
            marco(320,90,640,42,p.color(43,25,66),p.color(102,232,248));
            p.fill(244,235,250);p.textAlign(PApplet.CENTER,PApplet.CENTER);p.textSize(17);
            p.text(aviso,640,111);
        }
    }

    private void dibujarPeriodista() {
        if (fase==Fase.DEFENSA && interaccionTiempo<=0 && corriendo && !enSalto && direccion>=2) {
            int cuadro = movimientoReducido ? 0 : PosesRemaster.paso(animacion,24,8);
            PosesRemaster.PERIODISTA.dibujar(p.g,cuadro,px,py,82,direccion==2);
            return;
        }
        PImage sprite;
        if(fase==Fase.ENTRADA||fase==Fase.INTRO) sprite=acciones[0];
        else if(fase==Fase.SALIDA) sprite=acciones[1];
        else if(fase==Fase.ALARMA) sprite=acciones[2];
        else if(fase==Fase.DEFENSA&&enSalto)
            sprite=salto[direccion][Math.min(3,(int)(saltoTiempo*4/DURACION_SALTO))];
        else if(fase==Fase.DEFENSA&&interaccionTiempo>0) sprite=acciones[1];
        else if(terminada) sprite=acciones[exito?3:2];
        else sprite=andando&&!movimientoReducido?(corriendo?carrera[direccion][(int)(animacion*14)%4]:
            periodista[direccion][(int)(animacion*10)%4]):periodista[direccion][0];
        float ancho=sprite==acciones[0]?87:72, alto=sprite==acciones[0]?87:85;
        float altura=enSalto?18*PApplet.sin(PApplet.PI*saltoTiempo/DURACION_SALTO):0;
        if(enSalto) {p.noStroke();p.fill(27,13,45,95);p.rect(px-18,py-7,36,6);}
        p.image(sprite,px-ancho/2,py-alto-altura,ancho,alto);
    }
    private void dibujarPalomo() {
        if(fase==Fase.INTRO||fase==Fase.FINAL) p.image(palomoHabla,palomoX-35,palomoY-58,70,70);
        else {
            int pose=movimientoReducido?2:PosesRemaster.paso(animacion,23,8);
            boolean izquierda=fase==Fase.ENTRADA||fase==Fase.REGRESO;
            PosesRemaster.PALOMO.dibujar(p.g,pose,palomoX,palomoY+10,60,izquierda);
        }
    }

    private void dibujarEspanto() {
        if(fase!=Fase.SALIDA||vuelo>.35f) return;
        p.noStroke();p.fill(255,220,103);
        p.rect(744,514,10,32);p.rect(733,525,32,10);
        p.fill(255,111,171);p.rect(749,505,7,7);p.rect(766,528,7,7);
        p.fill(255);p.textAlign(PApplet.CENTER,PApplet.CENTER);p.textSize(18);
        p.text("¡FUERA!",747,488);
    }

    private void dibujarRemate() {
        if(falloCriticoActivado) return;
        if(fase!=Fase.REMATE&&fase!=Fase.FINAL&&fase!=Fase.DESPEDIDA) return;
        if(exito&&(fase==Fase.REMATE||fase==Fase.FINAL&&linea>=3||fase==Fase.DESPEDIDA)) {
            float x=px+47,y=py-9;
            if(fase==Fase.REMATE&&remateTiempo<.72f) {
                float t=remateTiempo/.72f;
                x=PApplet.lerp(palomoX-10,x,t);
                y=PApplet.lerp(palomoY+8,y,t*t);
                p.noStroke();p.fill(137,91,110);p.rect(x-7,y-7,14,14);
                p.fill(94,61,88);p.rect(x-4,y+6,8,5);
            } else {
                p.noStroke();p.fill(67,41,75,170);p.rect(x-23,y-1,46,6);
                p.fill(111,72,92);p.rect(x-18,y-8,36,10);
                p.fill(141,93,107);p.rect(x-11,y-17,22,12);
                p.fill(174,125,137);p.rect(x-4,y-23,8,8);
                p.fill(227,175,180);p.rect(x-5,y-18,6,5);
            }
        }
        if(!exito&&fase==Fase.REMATE&&remateTiempo<1) {
            float t=remateTiempo;
            float x=PApplet.lerp(palomoX,640,t),y=PApplet.lerp(palomoY-25,355,t);
            p.noStroke();p.fill(255,216,100);p.rect(x-13,y-5,26,10);
            p.fill(251,91,125);p.rect(x+10,y-8,9,16);
            p.fill(255,147,56);p.rect(x-20,y-4,8,8);
        }
        if(!exito&&fase==Fase.REMATE&&remateTiempo>=1) {
            float pulso=Math.min(1,(remateTiempo-1)*3f);
            p.noStroke();p.fill(255,76,142,55);p.rect(42,126,1196,564);
            for(int i=0;i<20;i++) {
                float a=i*PApplet.TWO_PI/20f;
                float r=40+pulso*(i%2==0?100:145);
                p.fill(i%2==0?p.color(255,191,71):p.color(255,67,133));
                p.rect(634+PApplet.cos(a)*r,354+PApplet.sin(a)*r,14,14);
            }
            p.fill(255,217,99,210);p.rect(614,334,52,52);
            p.fill(255,104,86,190);p.rect(626,322,28,76);
            p.fill(255,104,86,190);p.rect(602,346,76,28);
            p.fill(255,248,198);p.rect(630,350,20,20);
        }
    }

    // Puntos medidos en los cables del fondo, del servidor a las estaciones.
    private static final int[][] CABLES = {
        {552,345, 480,345, 397,317, 364,252, 341,207, 323,204, 280,204},
        {552,375, 510,375, 465,375, 417,375, 370,375, 327,375, 280,373},
        {558,409, 481,426, 414,445, 375,498, 343,542, 324,545, 280,545}
    };

    private void dibujarEnlaces() {
        boolean alarma = falloCriticoActivado;
        (alarma ? LuzEscena.MAGENTA : LuzEscena.CIAN).dibujar(p.g,640,355,325,254,120);
        for (int i = 0; i < RedCivitas.ESTACIONES; i++) {
            boolean activo = red.enPeligro(i);
            int color = activo ? 0xfffa588e : 0xff5de2dd;
            int[] cable = CABLES[i % 3];
            boolean derecha = i >= 3;
            float x = derecha ? 1280-cable[12] : cable[12], y = cable[13];
            (activo ? LuzEscena.MAGENTA : LuzEscena.CIAN).dibujar(p.g,x,y,116,70,activo?180:81);
            if (!activo) continue;
            float fase = movimientoReducido ? .5f : (animacion * .64f + i * .29f) % 1;
            p.noStroke();
            for (int j = 6; j >= 0; j--) {
                float t = fase - j*.029f;
                if (t < 0) continue;
                int k = t < .55f ? 0 : 6;
                float u = k == 0 ? t/.55f : (t-.55f)/.45f;
                float xx = p.bezierPoint(cable[k],cable[k+2],cable[k+4],cable[k+6],u);
                float yy = p.bezierPoint(cable[k+1],cable[k+3],cable[k+5],cable[k+7],u);
                if (derecha) xx = 1280-xx;
                p.fill(j==0 ? 0xffffd4e0 : color,240-j*32);
                p.rect(xx-4,yy-2,8,4);
            }
        }
    }

    private void dibujarServidor() {
        boolean rojo=red.protegidas()<RedCivitas.ESTACIONES||falloCriticoActivado;
        int paso=movimientoReducido?0:(int)(animacion*(rojo?9:5))%4;
        int luz=rojo?p.color(255,75,111):p.color(78,241,253);
        int tenue=rojo?p.color(178,47,90,125):p.color(67,216,244,115);
        p.noStroke();
        // La carcasa está en el fondo; estos píxeles iluminan sus pantallas y conectores.
        p.fill(tenue);
        p.rect(613,317,54,23);p.rect(617,382,46,18);
        p.fill(luz);
        for(int i=0;i<5;i++) {
            p.rect(617+i*10,323,6,3);
            if((i+paso)%3!=0) p.rect(617+i*10,330,6,3);
        }
        for(int i=0;i<5;i++) {
            int alto=4+((i+paso)%4)*3;
            p.rect(621+i*9,396-alto,6,alto);
        }
        int[][] conectores={{547,337},{547,371},{558,403},{716,337},{716,371},{705,403},{632,421}};
        for(int i=0;i<conectores.length;i++) {
            p.fill((i+paso)%3==0?tenue:luz);
            p.rect(conectores[i][0],conectores[i][1],8,6);
        }
        p.fill(luz,rojo?180:130);
        p.rect(526+paso*11,327,8,5);
        p.rect(744-paso*11,393,8,5);
        if(rojo) {
            p.fill(255,48,91,110+paso*30);
            p.rect(579,348+paso*8,20,4);p.rect(681,373-paso*7,20,4);
            p.fill(255,200,214,175);
            p.rect(611+paso*8,341,12,3);
        }
    }

    private void dibujarFalloCritico() {
        p.noStroke();p.fill(27,5,34,185);p.rect(0,86,W,83);
        marco(275,91,730,72,p.color(64,21,58),p.color(255,72,111));
        p.fill(255,105,136);p.textAlign(PApplet.CENTER,PApplet.CENTER);p.textSize(24);
        p.text("FALLO CRÍTICO DEL NÚCLEO",640,116);
        p.fill(252);p.textSize(16);
        p.text("La red quedó inestable. El próximo intento Difícil será recuperable.",640,146);
    }

    private void dibujarEstaciones() {
        for(int i=0;i<6;i++) {
            float x=i<3?145:1004,y=TY[i]-87;
            int color=red.protegida(i)?p.color(65,226,223):p.color(250,75,127);
            marco(x,y,130,29,p.color(38,21,67,225),color);
            p.noStroke();p.fill(color);
            p.rect(x+5,y+22,120*red.avance(i),4);
            p.fill(250);p.textAlign(PApplet.LEFT,PApplet.TOP);p.textSize(12);
            p.text("PC "+(i+1)+(red.protegida(i)?"  SEGURO":"  EN PELIGRO"),x+8,y+6);
            if(fase==Fase.DEFENSA&&PApplet.dist(px,py,TX[i],TY[i])<91&&red.enPeligro(i)) {
                marco(TX[i]-45,TY[i]+8,90,25,p.color(43,25,66),p.color(102,232,248));
                p.fill(255);p.textAlign(PApplet.CENTER,PApplet.CENTER);p.text("E · proteger",TX[i],TY[i]+20);
            }
        }
        p.fill(35,20,66,225);p.noStroke();p.rect(586,352,108,24);
        p.fill(red.completa()?p.color(85,234,230):p.color(255,117,184));
        p.textAlign(PApplet.CENTER,PApplet.CENTER);p.textSize(15);p.text(red.progreso()+" %",640,364);
    }

    private void dibujarRotulo() {
        marco(18,15,385,43,p.color(37,21,64,230),p.color(243,129,231));
        p.fill(255);p.textAlign(PApplet.LEFT,PApplet.CENTER);p.textSize(17);
        p.text("MISIÓN 2  ·  LA RED CIVITAS",33,36);
    }
    private void dibujarHUD() {
        marco(18,14,1244,69,p.color(37,21,64,238),p.color(239,100,219));
        p.textAlign(PApplet.LEFT,PApplet.CENTER);p.fill(250);p.textSize(18);
        p.text("MISIÓN 2  ·  DEFENDER CIVITAS  ["+dificultad.name()+"]",35,38);
        p.textSize(15);p.fill(128,238,246);
        p.text("EQUIPOS "+red.protegidas()+"/6  ·  EN RIESGO "+red.comprometidasAlcanzables(),36,65);
        p.fill(255);p.textAlign(PApplet.CENTER,PApplet.CENTER);p.textSize(23);
        p.text(String.format(java.util.Locale.ROOT,"%02d s",(int)Math.ceil(restante)),640,37);
        p.textSize(14);p.text(fase==Fase.FALLO_CRITICO?"NÚCLEO DESCONECTADO":
            ataqueFinalActivado&&restante>0&&red.protegidas()<6?
                "RED "+red.progreso()+" % · ATAQUE FINAL":
            restante<=ULTIMA_REPARACION?"RED "+red.progreso()+" % · ÚLTIMA REPARACIÓN":
            "RED "+red.progreso()+" % · ATAQUE EN "+
                (int)Math.ceil(Math.max(0,proximoAtaque))+" s",640,64);
        p.textAlign(PApplet.RIGHT,PApplet.CENTER);p.textSize(14);
        p.text("SHIFT · CORRER    ESPACIO · SALTO",1245,39);
        p.text("E · DEFENDER    P · PAUSA",1245,65);
    }

    private void dibujarDialogo() {
        String[] actual=dialogo()[linea];
        boolean palomo=actual[0].equals("MR. PALOMO");
        int acento=palomo?p.color(181,137,253):actual[0].equals("CIVITAS")?
            p.color(247,87,134):p.color(112,233,242);
        marco(65,517,1150,181,p.color(36,20,58,246),acento);
        marco(82,529,130,148,p.color(64,38,91),acento);
        if(!actual[0].equals("CIVITAS")) {
            PImage retrato=palomo?palomoHabla:
                fase==Fase.ALARMA||!exito&&fase==Fase.FINAL?acciones[2]:
                exito&&fase==Fase.FINAL?acciones[3]:periodista[0][0];
            p.image(retrato,99,533,96,126);
        } else {
            p.fill(acento);p.textAlign(PApplet.CENTER,PApplet.CENTER);p.textSize(35);p.text("!",147,602);
        }
        p.fill(acento);p.textAlign(PApplet.LEFT,PApplet.TOP);p.textSize(20);
        p.text(actual[0],234,540);
        p.fill(250,242,255);p.textSize(20);
        p.text(actual[1].substring(0,Math.min(letras,actual[1].length())),234,579,943,73);
        p.fill(247,202,118);p.textAlign(PApplet.RIGHT,PApplet.BOTTOM);p.textSize(14);
        p.text(letras<actual[1].length()?"E / ENTER · mostrar":"E / ENTER · continuar",1180,681);
    }

    private void dibujarResultado() {
        p.noStroke();p.fill(17,9,33,220);p.rect(0,0,W,H);
        int borde=exito?p.color(104,230,237):p.color(247,86,132);
        marco(259,164,762,392,p.color(50,30,77),borde);
        p.textAlign(PApplet.CENTER,PApplet.CENTER);
        p.fill(borde);p.textSize(37);p.text(falloCriticoActivado?"FALLO CRÍTICO":
            exito?"RED PROTEGIDA":"RED COMPROMETIDA",640,228);
        p.fill(247);p.textSize(22);p.text(red.protegidas()+" de 6 computadores protegidos",640,293);
        p.textSize(18);
        p.text(falloCriticoActivado?"El núcleo se desincronizó y esta defensa no podía completarse.":
            exito?"Civitas conserva su servicio y la ciudad recupera confianza.":
                "La infección alcanzó las estaciones que quedaron abiertas.",640,349);
        p.text(falloCriticoActivado?"El próximo intento Difícil será posible. La campaña no recibe penalización.":
            "La decisión queda registrada para la campaña.",640,391);
        p.fill(250,203,112);p.textSize(18);p.text("E / ENTER · volver a misiones",640,492);
    }

    private void marco(float x,float y,float w,float h,int fondo,int borde) {
        p.noStroke();p.fill(16,8,32,150);p.rect(x+4,y+5,w,h);
        p.fill(fondo);p.stroke(borde);p.strokeWeight(2);
        p.beginShape();p.vertex(x+6,y);p.vertex(x+w-6,y);p.vertex(x+w,y+6);
        p.vertex(x+w,y+h-6);p.vertex(x+w-6,y+h);p.vertex(x+6,y+h);
        p.vertex(x,y+h-6);p.vertex(x,y+6);p.endShape(PApplet.CLOSE);
        p.noStroke();p.fill(borde);p.rect(x+3,y+3,6,6);p.rect(x+w-9,y+3,6,6);
    }

    public boolean resultadoConfirmado() {return fase==Fase.RESULTADO&&ultimoCuadro==1;}
    public void confirmarResultado() {if(fase==Fase.RESULTADO) ultimoCuadro=1;}

    /** Verificación determinista de rutas, ataques, defensa y ambos resultados. */
    public void autoprobar() {
        MisionDosSketch paso=new MisionDosSketch();
        paso.fase=Fase.DEFENSA;paso.px=400;paso.py=440;paso.pulsadas.add(68);
        paso.mover(.1f);
        MisionDosSketch carreraPrueba=new MisionDosSketch();
        carreraPrueba.fase=Fase.DEFENSA;carreraPrueba.px=400;carreraPrueba.py=440;
        carreraPrueba.pulsadas.add(68);carreraPrueba.pulsadas.add(16);
        carreraPrueba.mover(.1f);
        if(carreraPrueba.px-paso.px<10)
            throw new IllegalStateException("Shift no aumentó la velocidad de carrera");
        MisionDosSketch impulso=new MisionDosSketch();
        impulso.fase=Fase.DEFENSA;impulso.px=400;impulso.py=440;impulso.direccion=3;
        impulso.pulsarTecla(' ',32);impulso.mover(.02f);
        float tiempoInicio=impulso.saltoTiempo;
        impulso.pulsarTecla(' ',32);
        if(!impulso.enSalto||impulso.saltoTiempo!=tiempoInicio)
            throw new IllegalStateException("Espacio repitió el salto al mantenerse pulsado");
        for(int pasoSalto=0;pasoSalto<14;pasoSalto++) impulso.mover(.02f);
        if(impulso.enSalto||impulso.px<500||impulso.px>549)
            throw new IllegalStateException("El salto atravesó el servidor o no avanzó");
        MisionDosSketch reparacion=new MisionDosSketch();
        reparacion.fase=Fase.DEFENSA;reparacion.px=TX[0];reparacion.py=TY[0];
        reparacion.red.ponerEnPeligro(0);
        reparacion.pulsarTecla(' ',32);reparacion.pulsarTecla('E',69);
        if(!reparacion.red.enPeligro(0))
            throw new IllegalStateException("Espacio o E repararon durante el salto");
        reparacion.soltarTecla('E',69);
        for(int pasoSalto=0;pasoSalto<15;pasoSalto++) reparacion.mover(.02f);
        reparacion.px=TX[0];reparacion.py=TY[0];
        reparacion.pulsarTecla('E',69);
        if(!reparacion.red.protegida(0))
            throw new IllegalStateException("E no reparó el computador al aterrizar");
        boolean[][] visitado=new boolean[128][72];
        ArrayDeque<int[]> cola=new ArrayDeque<>();
        cola.add(new int[]{64,67});visitado[64][67]=true;
        boolean[] alcanzable=new boolean[6];
        int[][] pasos={{1,0},{-1,0},{0,1},{0,-1}};
        while(!cola.isEmpty()) {
            int[] c=cola.remove();float x=c[0]*10,y=c[1]*10;
            for(int i=0;i<6;i++) if(PApplet.dist(x,y,TX[i],TY[i])<80) alcanzable[i]=true;
            for(int[] d:pasos) {
                int nx=c[0]+d[0],ny=c[1]+d[1];
                if(nx>=0&&nx<128&&ny>=0&&ny<72&&!visitado[nx][ny]&&libre(nx*10,ny*10)) {
                    visitado[nx][ny]=true;cola.add(new int[]{nx,ny});
                }
            }
        }
        comenzarDefensa();
        for(int i=0;i<6;i++) {
            if(!alcanzable[i]||!libre(TX[i],TY[i]))
                throw new IllegalStateException("Terminal inaccesible: "+i);
            px=TX[i];py=TY[i];
            red.ponerEnPeligro(i);
            interactuar();
        }
        if(!red.completa()||red.comprometidasAlcanzables()!=0)
            throw new IllegalStateException("La red no quedó protegida");
        restante=.03f;proximoAtaque=1;
        actualizar(.03f);
        if(!resultado().terminada()||!resultado().exito())
            throw new IllegalStateException("Resultado de misión 2 incorrecto");
        MisionDosSketch fallida=new MisionDosSketch();
        fallida.fase=Fase.DEFENSA;fallida.restante=.02f;fallida.proximoAtaque=1;
        fallida.red.ponerEnPeligro(0);
        fallida.actualizar(.03f);
        if(!fallida.resultado().terminada()||fallida.resultado().exito()||
           fallida.resultado().protegidos()!=5||fallida.resultado().segundosRestantes()!=0)
            throw new IllegalStateException("El tiempo agotado no produjo el resultado parcial");
        MisionDosSketch oleada=new MisionDosSketch(Dificultad.DIFICIL,33);
        oleada.fase=Fase.DEFENSA;oleada.proximoAtaque=.02f;
        oleada.actualizar(.03f);
        if(oleada.red.protegidas()<3||oleada.red.protegidas()>5||
           oleada.red.comprometidasAlcanzables()!=6-oleada.red.protegidas())
            throw new IllegalStateException("La oleada no alcanzó de uno a tres equipos distintos");
        MisionDosSketch multiples=new MisionDosSketch(Dificultad.DIFICIL,33);
        multiples.atacarOleada(3);multiples.atacarOleada(2);
        if(multiples.red.protegidas()!=1||multiples.red.comprometidasAlcanzables()!=5)
            throw new IllegalStateException("La oleada repitió un equipo ya comprometido");
        MisionDosSketch gracia=new MisionDosSketch(Dificultad.DIFICIL,33);
        gracia.fase=Fase.DEFENSA;gracia.restante=8.01f;gracia.proximoAtaque=.02f;
        gracia.actualizar(.03f);
        if(gracia.sorteosRealizados!=0||!gracia.red.completa())
            throw new IllegalStateException("La última reparación recibió un nuevo ataque");
        MisionDosSketch cierre=new MisionDosSketch(Dificultad.NORMAL,33);
        cierre.fase=Fase.DEFENSA;cierre.restante=2.01f;cierre.proximoAtaque=100;
        cierre.actualizar(.02f);
        if(!cierre.ataqueFinalActivado||cierre.red.protegidas()!=5||
           cierre.sorteosRealizados!=1)
            throw new IllegalStateException("No apareció un PC rojo al quedar dos segundos");
        cierre.actualizar(.05f);
        if(cierre.sorteosRealizados!=1||cierre.red.protegidas()!=5)
            throw new IllegalStateException("El ataque final se repitió");
        for(int i=0;i<6;i++) if(cierre.red.enPeligro(i)) {
            cierre.px=TX[i];cierre.py=TY[i];cierre.interactuar();break;
        }
        cierre.actualizar(2);
        if(!cierre.resultado().exito())
            throw new IllegalStateException("El PC del ataque final no se pudo reparar");
        for(Dificultad nivel:Dificultad.values()) {
            MisionDosSketch simulada=new MisionDosSketch(nivel,33);
            simulada.comenzarDefensa();
            for(int segundo=0;segundo<nivel.segundos();segundo++) simulada.actualizar(1);
            int esperados=switch(nivel) {case FACIL->7;case NORMAL->10;case DIFICIL->12;};
            if(!simulada.terminada||simulada.sorteosRealizados!=esperados)
                throw new IllegalStateException("Ritmo de ataques incorrecto en "+nivel);
        }
        MisionDosSketch critica=new MisionDosSketch(Dificultad.DIFICIL,33,true);
        critica.comenzarDefensa();critica.restante=38.01f;
        critica.actualizar(.02f);critica.actualizar(2.4f);
        if(!critica.resultado().falloCritico()||!critica.resultado().terminada()||
           critica.resultado().exito())
            throw new IllegalStateException("El fallo crítico Difícil no cerró el intento");
        MisionDosSketch normal=new MisionDosSketch(Dificultad.NORMAL,33,true);
        normal.comenzarDefensa();normal.restante=48.01f;normal.actualizar(.02f);
        if(normal.falloCriticoActivado)
            throw new IllegalStateException("El fallo crítico se aplicó fuera de Difícil");
        System.out.println("MISION_2_SELFTEST_OK: carrera, salto, rutas, oleadas, ataque final, fallo crítico y tres ritmos.");
    }
}
