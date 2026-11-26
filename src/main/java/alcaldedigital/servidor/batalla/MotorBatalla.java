package alcaldedigital.servidor.batalla;

import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import alcaldedigital.compartido.batalla.PaqueteCruzado;
import alcaldedigital.compartido.batalla.PaqueteRastreador;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import java.util.*;

/** Simulación autoritativa a 60 pasos/s. Cinco rondas, quince patrones originales. */
public final class MotorBatalla {
    public static final float AX=280,AY=345,AW=720,AH=270;
    public static final float ENTRADA_X=640,ENTRADA_Y=185,CONVERSACION_Y=230;
    public static final float PERIODISTA_X=790,PERIODISTA_Y=555,PERIODISTA_CONVERSACION_Y=445;
    public static final String[] PATRONES={"Paquetes cruzados","Tráfico cian: quietud","Ventana cambiante","Suelo corrupto","Plataformas de respaldo","Columnas del archivo","Pulso dirigido","Cruce de servidores","Pasillo intermitente","Gravedad lateral","Escalera de caché","Ráfaga alterna","Antenas gemelas","Anillo de señal","Tormenta del cortafuegos"};
    public static final String[] CONSEJOS={"Cambia de carril cuando veas el aviso.","Quietud durante el pulso cian.","Alinea el corazón con la ventana.","Salta sobre la descarga del suelo.","Las plataformas te levantan sobre el primer pulso.","Salta ante el suelo y baja cuando se cargue el techo.","El haz fija tu posición: sal de la línea.","Las columnas libres cambian.","El último pulso cierra columnas.","Salta alejándote de la pared derecha.","Mira qué columna quedará libre al aterrizar.","Alterna esquiva y quietud según el símbolo.","Al marcarse la ruta del cuadrito, esquiva hacia un lado.","Alterna el interior y el borde.","Lee la línea, las columnas y la señal cian."};
    public final ArbolAVLPublicaciones publicaciones=new ArbolAVLPublicaciones();
    public final ArbolDecisiones decisiones=new ArbolDecisiones("m5","El servidor crítico");
    private final String id;private final long semilla;private final boolean solo;private final Dificultad dificultad;
    private EstadoCiudad base=EstadoCiudad.inicial();
    public synchronized void ciudadBase(EstadoCiudad base){this.base=base;}
    public static final int[][] GRAFO={{1,2},{3},{3,4},{5},{5},{}};
    public static java.util.List<Integer> difusionDesde(int origen){java.util.List<Integer> r=new java.util.ArrayList<>();java.util.Queue<Integer> q=new java.util.ArrayDeque<>();java.util.Set<Integer> vistos=new java.util.HashSet<>();q.add(origen);vistos.add(origen);while(!q.isEmpty()){int v=q.remove();r.add(v);for(int vecino:GRAFO[v])if(vistos.add(vecino))q.add(vecino);}return r;}
    private long tick;private Fase fase=Fase.ESPERA;private int ronda,patron,reloj,jefe=100,linea,verificados,rumores=15,gravedadDir;
    private boolean pausa,gano;private String mensaje="Conecta a ambos jugadores y marca Listo.";
    private final List<String> historial=new ArrayList<>();
    private final List<Obstaculo> obs=new ArrayList<>();
    private final J[] js={new J("Ciudadano",ENTRADA_X,ENTRADA_Y),new J("Periodista",PERIODISTA_X,PERIODISTA_Y)};
    private final int[] golpes={0,0},elecciones={-1,-1};
    private static final class J {String nombre;float x,y,vx,vy,dx,dy;boolean salto,saltoAntes,movido,listo,conectado,accion;int vidas=5,puntos,inv,recuperaciones=1,orientacion;
        J(String n,float x,float y){nombre=n;this.x=x;this.y=y;}}
    public MotorBatalla(long semilla,boolean solo){this(semilla,solo,Dificultad.NORMAL);}
    public MotorBatalla(long semilla,boolean solo,Dificultad dificultad){this(UUID.randomUUID().toString(),semilla,solo,dificultad);}
    private MotorBatalla(String id,long semilla,boolean solo,Dificultad dificultad){this.id=id;this.semilla=semilla;this.solo=solo;this.dificultad=Objects.requireNonNull(dificultad);js[0].conectado=true;js[1].orientacion=1;normalizarIndividual();if(solo)mensaje="Marca Listo para jugar con el ciudadano.";registrarEstructuras();}
    private int participantes(){return solo?1:2;}
    private void normalizarIndividual(){if(solo){js[0].recuperaciones=0;J npc=js[1];npc.vidas=0;npc.puntos=0;npc.recuperaciones=0;npc.inv=0;npc.conectado=false;npc.listo=false;npc.accion=false;npc.dx=npc.dy=npc.vx=npc.vy=0;npc.salto=false;elecciones[1]=-1;golpes[1]=0;}}
    private boolean conectados(){for(int i=0;i<participantes();i++)if(!js[i].conectado)return false;return true;}
    private boolean listos(){for(int i=0;i<participantes();i++)if(!js[i].listo)return false;return conectados();}
    private boolean confirmaron(){for(int i=0;i<participantes();i++)if(!js[i].accion)return false;return true;}
    private boolean turnoCompleto(){for(int i=0;i<participantes();i++)if(js[i].vidas>0&&!js[i].accion)return false;return true;}
    private void entrada(){js[0].x=ENTRADA_X;js[0].y=ENTRADA_Y;js[0].orientacion=0;js[1].x=PERIODISTA_X;js[1].y=PERIODISTA_Y;js[1].orientacion=1;}
    private void animarEntrada(){J ciudadano=js[0];ciudadano.movido=ciudadano.y<CONVERSACION_Y;ciudadano.y=Math.min(CONVERSACION_Y,ciudadano.y+1.5f);ciudadano.orientacion=0;J periodista=js[1];periodista.movido=linea>=1&&periodista.y>PERIODISTA_CONVERSACION_Y;if(periodista.movido)periodista.y=Math.max(PERIODISTA_CONVERSACION_Y,periodista.y-1.5f);periodista.orientacion=1;}
    private void registrarEstructuras(){for(int i=0;i<5;i++){publicaciones.insertar(new Publicacion(i+1,40+i*10,"Servidor",new String[]{"El parque desapareció","Nadie puede publicar","Todos los datos son públicos","La elección ya terminó","Compartir equivale a votar"}[i],"Rumor activo"));decisiones.insertar("m5","r"+i,"Ronda "+(i+1));for(int a=0;a<3;a++)decisiones.insertar("r"+i,"r"+i+"a"+a,new String[]{"Verificar el paquete","Atacar con precisión","Reparar al compañero"}[a]);}}
    public synchronized void conectar(int rol,String nombre,boolean conectado){if(solo&&rol==1){normalizarIndividual();return;}J j=js[rol];j.conectado=conectado;j.nombre=nombre.isBlank()?j.nombre:nombre.substring(0,Math.min(24,nombre.length()));if(!conectado){j.dx=j.dy=0;j.salto=false;j.listo=false;if(fase!=Fase.ESPERA)pausa=true;mensaje="Jugador desconectado. La partida está detenida; puede reconectarse.";}}
    public synchronized void orden(int rol,float dx,float dy,boolean salto,String accion,int valor){
        if(rol<0||rol>1||solo&&rol==1)return;J j=js[rol];
        if(accion.equals("PAUSA")){pausa=true;detenerEntradas();mensaje=solo?"Partida individual en pausa.":"Pausa compartida. Reanudar requiere conexión de ambos roles.";return;}
        if(accion.equals("REANUDAR")){if(conectados()){pausa=false;detenerEntradas();mensaje="Partida reanudada.";}return;}
        if(pausa)return;
        j.dx=finito(dx);j.dy=finito(dy);j.salto=salto;
        if(accion.equals("LISTO")&&fase==Fase.ESPERA){j.listo=true;if(listos())cambiar(Fase.INTRO);}
        if(accion.equals("CONTINUAR")&&(fase==Fase.INTRO||fase==Fase.EPILOGO)){j.accion=true;if(confirmaron()){js[0].accion=js[1].accion=false;linea++;if(fase==Fase.INTRO&&linea>=9){cambiar(Fase.OFICINA);}else if(fase==Fase.EPILOGO&&linea>=4)cambiar(Fase.FINAL);}}
        if(accion.equals("INTERACTUAR")&&fase==Fase.OFICINA&&Math.hypot(j.x-640,j.y-490)<155){j.accion=true;if(confirmaron())cambiar(Fase.ABSORCION);}
        if(accion.equals("ACCION")&&fase==Fase.TURNO&&j.vidas>0&&!j.accion&&valor>=0&&valor<=2){j.accion=true;elecciones[rol]=valor;golpes[rol]=reloj;}
        if(accion.equals("DECISION")&&fase==Fase.DECISION&&!j.accion&&valor>=0&&valor<=1){j.accion=true;elecciones[rol]=valor;}
    }
    /** Las instantáneas antiguas conservan la ronda y vuelven al aviso del patrón en curso. */
    synchronized void adaptarOleadaAnterior(){
        if(fase==Fase.ESQUIVA)cambiar(Fase.AVISO);
        else if(fase==Fase.TRAICION)cambiar(Fase.TRAICION);
    }
    private static float finito(float v){return Float.isFinite(v)?Math.max(-1,Math.min(1,v)):0;}
    private void detenerEntradas(){for(J j:js){j.dx=j.dy=0;j.salto=false;}}
    private void cambiar(Fase siguiente){if(siguiente==Fase.TURNO){var prioridad=publicaciones.recorrer("inverso");mensaje=prioridad.isEmpty()?"Todas las publicaciones fueron verificadas.":"Prioridad de Civitas: "+prioridad.getFirst().texto();}fase=siguiente;reloj=0;linea=0;obs.clear();for(J j:js){j.accion=false;j.movido=false;j.dx=j.dy=0;j.vx=j.vy=0;}if(siguiente==Fase.INTRO)entrada();if(siguiente==Fase.EPILOGO){js[0].x=515;js[0].y=530;js[0].orientacion=0;js[1].x=PERIODISTA_X;js[1].y=PERIODISTA_CONVERSACION_Y;js[1].orientacion=0;}if(siguiente==Fase.AVISO){js[0].x=500;js[1].x=770;for(int i=0;i<participantes();i++){js[i].y=550;js[i].inv=60;}normalizarIndividual();}}
    public synchronized void paso(){
        if(pausa||fase==Fase.ESPERA||fase==Fase.FINAL)return;
        tick++;reloj++;if(fase==Fase.INTRO){animarEntrada();return;}for(J j:js)if(j.inv>0)j.inv--;
        if(fase==Fase.OFICINA){for(int i=0;i<participantes();i++)moverOficina(js[i]);return;}
        if(fase==Fase.ABSORCION&&reloj>=150){patron=0;ronda=0;cambiar(Fase.AVISO);return;}
        if(fase==Fase.AVISO){for(int i=0;i<participantes();i++)moverCorazon(js[i]);if(reloj>=36)cambiar(Fase.ESQUIVA);return;}
        if(fase==Fase.ESQUIVA||fase==Fase.TRAICION){
            for(J j:js)if(j.vidas>0)moverCorazon(j);
            generar();actualizarObstaculos();colisiones();
            if(js[0].vidas<=0&&(solo||js[1].vidas<=0)){gano=false;cambiar(Fase.EPILOGO);mensaje=solo?"La señal te expulsó antes de reparar el servidor.":"La señal los expulsó antes de reparar el servidor.";return;}
            if(fase==Fase.TRAICION&&reloj>=420){gano=true;cambiar(Fase.EPILOGO);mensaje="Resistieron el último paquete y aislaron el servidor.";return;}
            if(fase==Fase.ESQUIVA&&reloj>=duracionPatron()){for(J j:js)if(j.vidas>0)j.puntos+=10;if(patron%3==2)recuperarVidaDeRonda();recuperar();if(patron%3==2)cambiar(Fase.TURNO);else{patron++;cambiar(Fase.AVISO);}}
        }
        if(fase==Fase.TURNO){if(reloj>=480)for(int i=0;i<participantes();i++)if(!js[i].accion){js[i].accion=true;elecciones[i]=0;golpes[i]=reloj;}
            if(turnoCompleto())cerrarTurno();}
        if(fase==Fase.DECISION){if(confirmaron()){if(elecciones[0]==1&&(solo||elecciones[1]==1)){historial.add("Aislaron el servidor sin difundir nuevos rumores");gano=true;cambiar(Fase.EPILOGO);mensaje="La red está aislada y recuperada.";}else{historial.add("Perdonaron la señal: último ataque anunciado");cambiar(Fase.TRAICION);mensaje="¡El servidor prepara un último pulso! Todavía pueden esquivarlo.";}}}
    }
    private void cerrarTurno(){
        // Reparar puede revivir al otro rol. Solo actúan quienes estaban vivos al cerrar el turno.
        boolean[] respondian={js[0].vidas>0,!solo&&js[1].vidas>0};
        for(int i=0;i<participantes();i++){J j=js[i];if(!respondian[i])continue;int a=elecciones[i];decisiones.ejecutar("r"+ronda+"a"+a);if(a==0){verificados++;rumores=Math.max(0,rumores-2);j.puntos+=20;var prioridad=publicaciones.recorrer("inverso");if(!prioridad.isEmpty())publicaciones.eliminar(prioridad.getFirst().id());}
            if(a==1){float precision=1-Math.min(1,Math.abs(indicador(golpes[i])-.5f)*2);int dano=4+Math.round(precision*8);j.puntos+=Math.round(precision*25);jefe=Math.max(0,jefe-dano);}
            if(a==2){J otro=solo?j:js[1-i];if(otro.vidas<5){otro.vidas=Math.min(5,otro.vidas+1);otro.inv=90;j.puntos+=12;}else j.puntos+=3;}
            historial.add("Ronda "+(ronda+1)+" · "+j.nombre+": "+new String[]{"verificar","pulso preciso","reparar"}[a]);}
        jefe=Math.max(0,jefe-12);rumores=Math.max(0,rumores-1);ronda++;
        if(ronda>=5){jefe=0;cambiar(Fase.DECISION);elecciones[0]=elecciones[1]=-1;}else{patron=ronda*3;cambiar(Fase.AVISO);elecciones[0]=elecciones[1]=-1;}}
    public static float indicador(int pasos){return (float)(.5-.5*Math.cos(pasos*.075));}
    private static final int[] DURACIONES={360,330,390,390,420,360,360,390,390,390,420,390,420,420,480};
    public static int duracion(int p){return DURACIONES[Math.floorMod(p,DURACIONES.length)];}
    private int duracionPatron(){return duracion(patron);}
    private boolean gravedad(){return fase==Fase.ESQUIVA&&(patron==3||patron==4||patron==5||patron==9||patron==10);}
    private void moverOficina(J j){float nx=j.x+j.dx*3.5f,ny=j.y+j.dy*3.5f;if(oficinaLibre(nx,j.y))j.x=nx;if(oficinaLibre(j.x,ny))j.y=ny;j.movido=Math.abs(j.dx)+Math.abs(j.dy)>.1;if(j.movido)j.orientacion=Math.abs(j.dx)>Math.abs(j.dy)?j.dx<0?2:3:j.dy<0?1:0;}
    public static boolean oficinaLibre(float x,float y){if(x<60||x>1220||y<140||y>670)return false;float[][] b={{119,150,180,94},{119,315,180,94},{119,480,180,94},{981,150,180,94},{981,315,180,94},{981,480,180,94},{550,250,180,215},{539,568,202,92}};for(float[] a:b)if(x>a[0]-10&&x<a[0]+a[2]+10&&y>a[1]-5&&y<a[1]+a[3]+5)return false;return true;}
    private void moverCorazon(J j){float ox=j.x,oy=j.y;
        if(!gravedad()){j.vx=j.dx*3.8f;j.vy=j.dy*3.8f;j.x+=j.vx;j.y+=j.vy;}
        else if(patron==9){if(j.x>=AX+AW-12&&j.salto&&!j.saltoAntes)j.vx=-8.2f;if(!j.salto&&j.vx< -1.8f)j.vx=-1.8f;j.vx+=.34f;j.x+=j.vx;j.y+=j.dy*3.8f;}
        else{boolean suelo=j.y>=AY+AH-12;for(var a:obs)if(a.tipo()==3&&j.y<=a.y()+10&&j.y>=a.y()-14&&j.x>a.x()-6&&j.x<a.x()+a.w()+6&&j.vy>=0)suelo=true;if(suelo&&j.salto&&!j.saltoAntes)j.vy=-8.2f;if(!j.salto&&j.vy< -1.8f)j.vy=-1.8f;j.vy+=.34f;j.x+=j.dx*3.8f;float antes=j.y;j.y+=j.vy;for(var a:obs)if(a.tipo()==3&&j.vy>=0&&antes<=a.y()-9&&j.y>=a.y()-9&&j.x>a.x()&&j.x<a.x()+a.w()){j.y=a.y()-10;j.vy=0;}}
        j.x=Math.max(AX+12,Math.min(AX+AW-12,j.x));j.y=Math.max(AY+12,Math.min(AY+AH-12,j.y));if(j.y>=AY+AH-12&&j.vy>0)j.vy=0;if(j.x>=AX+AW-12&&j.vx>0)j.vx=0;j.movido=Math.abs(j.x-ox)>.01||Math.abs(j.y-oy)>.01;j.saltoAntes=j.salto;}
    private void recuperar(){if(solo)return;for(int i=0;i<2;i++)if(js[i].vidas<=0&&js[i].recuperaciones>0&&js[1-i].vidas>0){js[i].vidas=2;js[i].recuperaciones--;js[i].inv=120;historial.add(js[1-i].nombre+" rescató al compañero al terminar la oleada");}}
    private void recuperarVidaDeRonda(){
        if(dificultad==Dificultad.DIFICIL)return;
        for(int i=0;i<participantes();i++){J j=js[i];if(j.vidas>0&&j.vidas<5){j.vidas++;historial.add("Recuperación de ronda "+(ronda+1)+" · "+j.nombre+": +1 corazón");}}
    }
    private void agregar(float x,float y,float w,float h,float vx,float vy,int tipo,int aviso,int dur,int obj){
        int espera=aviso==0?0:Math.max(18,Math.round(aviso*dificultad.aviso));
        float rapidez=tipo==3?1:dificultad.velocidad;
        if(obs.size()<300)obs.add(new Obstaculo(x,y,w,h,vx*rapidez,vy*rapidez,tipo,0,espera,espera+dur-aviso,obj));
    }
    private float velocidadPorVerificaciones(){
        return 1-Math.min(.25f,verificados*.025f);
    }
    private static final int[][] HUECOS = {{0,1,2},{2,1,0},{1,0,2},{1,2,0}};

    private void generar(){
        int p=fase==Fase.TRAICION?14:patron;
        gravedadDir=p==9?1:0;
        int duracion=fase==Fase.TRAICION?420:duracionPatron();
        // El inicio de la fase se conserva al pausar, guardar o reconectar.
        long inicio=tick-reloj;
        int variante=variante(p,inicio);
        boolean cortafuegosDificil=p==14&&fase==Fase.ESQUIVA&&dificultad==Dificultad.DIFICIL;
        if(cortafuegosDificil&&reloj==84+variante)generarRastreador(variante^2,0);
        if(reloj==1&&(p==4||p==10))plataformas(p,variante);
        for(int oleada=0;oleada<3;oleada++){
            // La primera oleada de pared deja que ambos corazones se apoyen antes de pedir un salto.
            int esperaPared=p==9&&oleada==0?28:0;
            int inicioOleada=1+oleada*duracion/3+desfase(oleada,variante)+esperaPared;
            // Deja terminar al primer rastreador y al pulso cian antes de la siguiente oleada.
            if(cortafuegosDificil&&oleada>0)inicioOleada+=oleada==1?44:25;
            if(reloj==inicioOleada)oleada(p,oleada,variante,inicioOleada);
            if(p==14&&oleada==1){
                if(fase==Fase.TRAICION){
                    if(reloj==inicioOleada+78)cian(27,14);
                }else{
                    // Al continuar una partida anterior, termina la descarga que ya estaba en curso.
                    if(reloj==inicioOleada+14&&obs.stream().noneMatch(a->a.tipo()==0))
                        cuadritosDelCortafuegos(inicio,1);
                    if(reloj==inicioOleada+98&&obs.stream().noneMatch(a->a.tipo()==1))
                        cian(27,14);
                }
            }
        }
    }

    private int variante(int patron,long inicio){
        return (int)(mezclarSemilla(patron,inicio)&3);
    }

    private long mezclarSemilla(int patron,long inicio){
        long z=semilla+inicio*0x9e3779b97f4a7c15L+patron*0x632be59bd9b4e019L;
        z=(z^(z>>>30))*0xbf58476d1ce4e5b9L;
        z=(z^(z>>>27))*0x94d049bb133111ebL;
        return z^(z>>>31);
    }

    private int desfase(int oleada,int variante){
        // La dificultad modifica el momento del ataque además del aviso y la velocidad.
        int paso=variante+Math.round((dificultad.cadencia-Dificultad.DIFICIL.cadencia)*8);
        int cambio=oleada==1?switch(variante){case 1->3;case 2->-2;case 3->4;default->0;}:0;
        return oleada*paso+cambio;
    }

    private void oleada(int patron,int oleada,int variante,int inicioOleada){
        int lado=(oleada+variante)&1;
        int aviso=52;
        switch(patron){
            case 0 -> carriles(false,lado,variante,aviso,24);
            case 1 -> {
                if(oleada==1)cian(45,22);
                else carriles(false,(variante+(oleada/2))&1,variante,aviso,20);
            }
            case 2 -> ventana(HUECOS[variante][oleada],lado==0);
            case 3 -> suelo(33+4*(variante&1),aviso,13);
            case 4,10 -> {
                if(oleada==0)suelo(44,aviso,13);
                else carriles(true,lado,(variante+(patron==10?2:0))%4,aviso,21);
            }
            case 5 -> {
                if(oleada==1){
                    float x=(variante&1)==0?AX:AX+AW/3;
                    pulso(x,AY,AW*2/3,161,0,aviso,22,-1);
                }else suelo(28+7*(1+lado),aviso,14);
            }
            case 6 -> dirigido(oleada%2,false);
            case 7 -> carriles(true,lado,variante,aviso,25);
            case 8 -> carriles(oleada==2, lado, variante,aviso,21);
            case 9 -> {
                if(oleada==1)carriles(false,lado,variante,aviso,23);
                else pulso(AX+AW-36-4*(variante&1),AY,36+4*(variante&1),AH,0,aviso,13,-1);
            }
            case 11 -> {
                if(oleada==1)cian(47,22);
                else carriles((variante&2)!=0,(variante+(oleada/2))&1,variante,aviso,22);
            }
            case 12 -> {
                if(oleada==0)dirigido(0,false);
                else if(oleada==1)carriles(true,lado,variante,aviso,20);
                else generarRastreador(variante,1);
            }
            case 13 -> anillo(lado==0,variante,aviso);
            case 14 -> {
                if(oleada==1){
                    if(fase==Fase.TRAICION)carriles(true,lado,(variante+nodoDifusion(inicioOleada))%4,43,15);
                    else if(obs.stream().noneMatch(a->a.tipo()==0))cuadritosDelCortafuegos(tick-reloj,0);
                }else if(oleada==2&&fase==Fase.ESQUIVA&&dificultad==Dificultad.DIFICIL){
                    generarRastreador(variante,1);
                }else dirigido(oleada/2,oleada!=0&&(variante&1)!=0);
            }
            default -> throw new IllegalArgumentException("Patrón desconocido: "+patron);
        }
    }

    private int nodoDifusion(int paso){
        var nodos=difusionDesde(patron%6);
        return nodos.get((paso/60)%nodos.size());
    }

    private void pulso(float x,float y,float w,float h,int tipo,int aviso,int activo,int objetivo){
        agregar(x,y,w,h,0,0,tipo,aviso,aviso+activo,objetivo);
    }

    private void cian(int aviso,int activo){
        pulso(AX,AY,AW,AH,1,aviso,activo,-1);
    }

    private void suelo(float alto,int aviso,int activo){
        pulso(AX,AY+AH-alto,AW,alto,0,aviso,activo,-1);
    }

    private void dirigido(int objetivo,boolean vertical){
        if(solo)objetivo=0;
        if(!solo&&js[objetivo].vidas<=0)objetivo=1-objetivo;
        J j=js[objetivo];
        if(vertical)pulso(j.x-15,AY,30,AH,2,58,25,objetivo);
        else pulso(AX,j.y-13,AW,26,2,60,28,objetivo);
        mensaje="Pulso dirigido al jugador "+(objetivo+1)+": sal de la línea anunciada.";
    }

    private void generarRastreador(int variante,int objetivoPreferido) {
        int objetivo=solo?0:objetivoPreferido;
        if(!solo&&js[objetivo].vidas<=0)objetivo=1-objetivo;
        J j=js[objetivo];
        float lado=24, mitad=lado/2, distancia=124;
        boolean horizontal=(variante&2)==0;
        int signo=(variante&1)==0?-1:1;
        float posicion=horizontal?j.x:j.y;
        float min=(horizontal?AX:AY)+mitad, max=(horizontal?AX+AW:AY+AH)-mitad;
        float origen=posicion+signo*distancia;
        if (origen<min||origen>max) {
            origen=posicion-signo*distancia;
            signo=-signo;
        }
        origen=Math.clamp(origen,min,max);
        float x=horizontal?origen:j.x;
        float y=horizontal?j.y:origen;
        var ritmo=PaqueteRastreador.para(dificultad);
        obs.add(new Obstaculo(x-mitad,y-mitad,lado,lado,horizontal?-signo:0,horizontal?0:-signo,
            PaqueteRastreador.TIPO,0,ritmo.aviso(),ritmo.duracion(),objetivo));
    }

    private void cuadritosDelCortafuegos(long inicio,int tanda){
        int cantidad=dificultad==Dificultad.FACIL?2:3;
        float lado=22, reduccion=velocidadPorVerificaciones();
        int aviso=24, pasosActivos=38;
        for(int i=0;i<cantidad;i++){
            long azar=mezclarSemilla(15+tanda*cantidad+i,inicio);
            int signo=(azar&1)==0?1:-1;
            float ajuste=((azar>>>8)%81)-40;
            float x=AX+(i+.5f)*(AW-lado)/cantidad+ajuste;
            float y=signo>0?AY:AY+AH-lado;
            float vx=((azar>>>16)%7)-3;
            if(i==0&&vx==0)vx=2*signo;
            float vy=signo*(7+((azar>>>24)%3));
            if(tanda==1&&i==cantidad-1){
                x=signo>0?AX:AX+AW-lado;
                y=AY+25+((azar>>>32)%155);
                vx=signo*9;
                vy=((azar>>>48)%5)-2;
            }
            agregar(x,y,lado,lado,vx*reduccion,vy*reduccion,PaqueteCruzado.TIPO,aviso,
                aviso+pasosActivos+PaqueteCruzado.DISIPACION-1,-1);
        }
    }

    private void plataformas(int patron,int variante){
        float signo=(variante&1)==0?1:-1;
        float ajuste=(variante&2)==0?0:24;
        agregar(AX+120+ajuste,AY+AH-65,160,12,.3f*signo,0,3,0,500,-1);
        agregar(AX+410-ajuste,AY+AH-(patron==10?108:115),180,12,-.25f*signo,0,3,0,500,-1);
    }

    private void carriles(boolean vertical,int lado,int variante,int aviso,int activo){
        int total=vertical?8:switch(dificultad){case FACIL->4;case NORMAL->6;case DIFICIL->8;};
        // Cada variante corta sectores distintos. Las dos polaridades juntas cubren toda la arena.
        // Los grupos tienen como máximo dos sectores contiguos, incluso en Difícil.
        int mascara=switch(variante){case 0->0b01010101;case 1->0b10011001;case 2->0b01100110;default->0b10101010;};
        float origen=vertical?AX:AY;
        float medida=(vertical?AW:AH)/total;
        for(int i=0;i<total;i++){
            if(((mascara>>>i)&1)!=lado)continue;
            float comienzo=origen+i*medida;
            if(vertical)pulso(comienzo,AY,medida,AH,0,aviso,activo,-1);
            else pulso(AX,comienzo,AW,medida,0,aviso,activo,-1);
        }
    }

    private void ventana(int hueco,boolean desdeDerecha){
        float ancho=28;
        float centro=AY+63+72*hueco;
        float apertura=90*dificultad.apertura;
        float arriba=centro-apertura/2;
        float abajo=centro+apertura/2;
        float x=desdeDerecha?AX+AW:AX-ancho;
        float vx=desdeDerecha?-24:24;
        int aviso=44;
        float reduccion=velocidadPorVerificaciones();
        int cruce=(int)Math.ceil((AW+ancho)/(24*dificultad.velocidad*reduccion))+2;
        agregar(x,AY,ancho,arriba-AY,vx,0,0,aviso,aviso+cruce,-1);
        agregar(x,abajo,ancho,AY+AH-abajo,vx,0,0,aviso,aviso+cruce,-1);
    }

    private void anillo(boolean exterior,int variante,int aviso){
        float lado=82+14*variante;
        float borde=42+6*variante;
        if(exterior){
            pulso(AX,AY,AW,borde,0,aviso,25,-1);
            pulso(AX,AY+AH-borde,AW,borde,0,aviso,25,-1);
            pulso(AX,AY+borde,lado,AH-2*borde,0,aviso,25,-1);
            pulso(AX+AW-lado,AY+borde,lado,AH-2*borde,0,aviso,25,-1);
        }else{
            float x=lado-15,y=borde-12;
            pulso(AX+x,AY+y,AW-2*x,AH-2*y,0,aviso,22,-1);
        }
    }
    private void actualizarObstaculos() {
        float reduccion=velocidadPorVerificaciones();
        for (int i=0;i<obs.size();i++) {
            var a=obs.get(i);
            if (a.tipo()==PaqueteRastreador.TIPO) {
                obs.set(i,actualizarRastreador(a,reduccion));
                continue;
            }
            int edad=a.edad()+1;
            boolean cruzado=a.tipo()==PaqueteCruzado.TIPO;
            if(cruzado&&edad>=a.aviso()&&edad<PaqueteCruzado.inicioDisipacion(a)){
                obs.set(i,desplazarPaquete(a,edad,a.vx(),a.vy(),PaqueteCruzado.inicioDisipacion(a),true));
                continue;
            }
            float avance=!cruzado&&edad>=a.aviso()?reduccion:0;
            obs.set(i,new Obstaculo(a.x()+a.vx()*avance,a.y()+a.vy()*avance,a.w(),a.h(),
                a.vx(),a.vy(),a.tipo(),edad,a.aviso(),a.duracion(),a.objetivo()));
        }
        obs.removeIf(a->a.edad()>a.duracion());
    }

    private Obstaculo actualizarRastreador(Obstaculo a,float reduccion) {
        var ritmo=PaqueteRastreador.para(dificultad);
        int edad=a.edad()+1;
        J objetivo=js[a.objetivo()];
        if (objetivo.vidas<=0 || !objetivo.conectado) edad=Math.max(edad,ritmo.inicioDisipacion(a));
        var fase=ritmo.fase(a,edad);
        float x=a.x(),y=a.y(),vx=a.vx(),vy=a.vy();
        boolean persigue=fase==PaqueteRastreador.Fase.SEGUIMIENTO;
        boolean empiezaFijado=fase==PaqueteRastreador.Fase.FIJADO&&ritmo.fase(a)!=fase;
        if (persigue||empiezaFijado) {
            double dx=objetivo.x-(x+a.w()/2),dy=objetivo.y-(y+a.h()/2);
            double rumbo=Math.atan2(vy,vx);
            double direccion=dx==0&&dy==0?rumbo:Math.atan2(dy,dx);
            if (persigue) {
                double giro=Math.IEEEremainder(direccion-rumbo,2*Math.PI);
                direccion=rumbo+Math.clamp(giro,-.09,.09);
            }
            float rapidez=reduccion*(persigue?ritmo.velocidadSeguimiento():ritmo.velocidadEmbestida());
            vx=(float)Math.cos(direccion)*rapidez;
            vy=(float)Math.sin(direccion)*rapidez;
        }
        if (fase.haceDanio())
            return desplazarPaquete(a,edad,vx,vy,ritmo.inicioDisipacion(a),!persigue);
        return new Obstaculo(x,y,a.w(),a.h(),vx,vy,a.tipo(),edad,a.aviso(),a.duracion(),a.objetivo());
    }

    private static Obstaculo desplazarPaquete(Obstaculo a,int edad,float vx,float vy,int disipacion,boolean detenerAlBorde){
        float siguienteX=a.x()+vx,siguienteY=a.y()+vy;
        float x=Math.clamp(siguienteX,AX,AX+AW-a.w());
        float y=Math.clamp(siguienteY,AY,AY+AH-a.h());
        if(detenerAlBorde&&(siguienteX!=x||siguienteY!=y)){
            edad=disipacion;
            x=a.x();
            y=a.y();
        }
        return new Obstaculo(x,y,a.w(),a.h(),vx,vy,a.tipo(),edad,a.aviso(),a.duracion(),a.objetivo());
    }

    private void colisiones() {
        var ritmo=PaqueteRastreador.para(dificultad);
        for (J j:js) {
            if (j.vidas<=0||j.inv>0) continue;
            for (var a:obs) {
                if (a.tipo()==3||a.edad()<a.aviso()||a.tipo()==1&&!j.movido) continue;
                if (a.tipo()==PaqueteRastreador.TIPO&&!ritmo.fase(a).haceDanio()) continue;
                if (a.tipo()==PaqueteCruzado.TIPO&&a.edad()>=PaqueteCruzado.inicioDisipacion(a)) continue;
                if (j.x+7>a.x()&&j.x-7<a.x()+a.w()&&j.y+7>a.y()&&j.y-7<a.y()+a.h()) {
                    j.vidas--;
                    j.inv=75;
                    j.puntos=Math.max(0,j.puntos-3);
                    break;
                }
            }
        }
    }
    private int[] ciudadActual(){int signo=fase==Fase.FINAL||fase==Fase.EPILOGO?(gano?1:-1):0;return new int[]{Math.min(100,base.informacionVerificada()+verificados*2),Math.max(0,Math.min(100,base.confianza()+signo*6)),Math.max(0,Math.min(100,base.convivencia()+signo*3)),Math.max(0,Math.min(100,base.bienestar()+signo*3)),Math.max(0,Math.min(100,base.participacion()+signo*3)),Math.max(0,Math.min(100,base.desinformacion()-signo*8)),Math.max(0,Math.min(100,base.conflictos()-signo*4))};}
    public synchronized Vista vista(){Jugador[] jugadores=new Jugador[2];for(int i=0;i<2;i++){J j=js[i];jugadores[i]=new Jugador(j.nombre,j.x,j.y,j.vx,j.vy,j.vidas,j.puntos,j.inv,j.recuperaciones,j.orientacion,j.movido,j.listo,j.conectado,j.accion);}return new Vista(id,semilla,tick,fase,ronda,patron,reloj,jefe,linea,golpes.clone(),elecciones.clone(),jugadores,List.copyOf(obs),pausa,gravedad(),gravedadDir,solo,gano,verificados,rumores,ciudadActual(),new int[]{base.informacionVerificada(),base.confianza(),base.convivencia(),base.bienestar(),base.participacion(),base.desinformacion(),base.conflictos()},mensaje,historial.toArray(String[]::new),publicaciones.recorrer("inverso").stream().map(a->"#"+a.id()+" · riesgo "+a.riesgo()+" · "+a.texto()).toArray(String[]::new),dificultad);}
    public static MotorBatalla restaurar(Vista v){MotorBatalla m=new MotorBatalla(v.id(),v.semilla(),v.solo(),v.dificultad());int[] c=v.base();m.base=new EstadoCiudad(c[0],c[1],c[2],c[3],c[4],c[5],c[6]);m.gravedadDir=v.gravedadDir();m.tick=v.tick();m.fase=v.fase();m.ronda=v.ronda();m.patron=v.patron();m.reloj=v.reloj();m.jefe=v.jefe();m.linea=v.linea();m.pausa=v.pausa();m.gano=v.gano();m.verificados=v.verificados();m.rumores=v.rumores();m.mensaje=v.mensaje();m.historial.addAll(List.of(v.historial()));m.obs.addAll(v.obstaculos());for(int i=0;i<2;i++){Jugador a=v.jugadores()[i];J j=m.js[i];j.nombre=a.nombre();j.x=a.x();j.y=a.y();j.vx=a.vx();j.vy=a.vy();j.vidas=a.vidas();j.puntos=a.puntos();j.inv=a.invulnerable();j.recuperaciones=a.recuperaciones();j.orientacion=a.orientacion();j.movido=a.movido();j.listo=a.listo();j.conectado=a.conectado();j.accion=a.accionLista();m.golpes[i]=v.golpes()[i];m.elecciones[i]=v.elecciones()[i];}for(int n=0;n<v.verificados();n++){var prioridad=m.publicaciones.recorrer("inverso");if(!prioridad.isEmpty())m.publicaciones.eliminar(prioridad.getFirst().id());}
        for(String h:v.historial())if(h.startsWith("Ronda "))try{int r=Integer.parseInt(h.substring(6,7))-1;int a=h.endsWith("verificar")?0:h.endsWith("pulso preciso")?1:2;m.decisiones.ejecutar("r"+r+"a"+a);}catch(RuntimeException ignorado){}
        m.normalizarIndividual();
        // Los checkpoints previos situaban la introducción debajo del servidor.
        if(m.fase==Fase.INTRO&&(m.js[0].y>CONVERSACION_Y||Math.abs(m.js[0].x-ENTRADA_X)>1))m.entrada();
        return m;}
}
