package alcaldedigital.servidor.batalla;

import alcaldedigital.compartido.batalla.EstadoBatalla.*;
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
    public static final String[] CONSEJOS={"Busca los huecos entre paquetes.","Los paquetes cian solo dañan si te mueves de verdad.","Sigue el hueco de la columna.","Salta desde el suelo. Soltar salto reduce la altura.","Usa las plataformas para esquivar el suelo.","Observa las columnas antes de aterrizar.","El haz fija tu posición al avisar. Sal de esa línea.","Hay huecos compartidos para los dos corazones.","El pulso se anuncia antes de cruzar.","La pared derecha será suelo. Salta hacia la izquierda.","La gravedad vuelve al suelo inferior.","Alterna movimiento y quietud según el símbolo.","El haz alterna objetivo: 1 y 2.","Rodea el centro siguiendo la abertura.","Mantén libre la salida. No te encierres en una esquina."};
    public final ArbolAVLPublicaciones publicaciones=new ArbolAVLPublicaciones();
    public final ArbolDecisiones decisiones=new ArbolDecisiones("m5","El servidor crítico");
    private final String id;private final long semilla;private final boolean solo;private final Dificultad dificultad;
    private EstadoCiudad base=EstadoCiudad.inicial();
    public synchronized void ciudadBase(EstadoCiudad base){this.base=base;}
    public static final int[][] GRAFO={{1,2},{3},{3,4},{5},{5},{}};
    public static java.util.List<Integer> difusionDesde(int origen){java.util.List<Integer> r=new java.util.ArrayList<>();java.util.Queue<Integer> q=new java.util.ArrayDeque<>();java.util.Set<Integer> vistos=new java.util.HashSet<>();q.add(origen);vistos.add(origen);while(!q.isEmpty()){int v=q.remove();r.add(v);for(int vecino:GRAFO[v])if(vistos.add(vecino))q.add(vecino);}return r;}
    private int nodoAtaque(){var r=difusionDesde(patron%6);return r.get(Math.floorMod(reloj/60,r.size()));}
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
    public static int duracion(int p){return new int[]{360,330,390,390,420,360,360,390,390,390,420,390,420,420,480}[Math.floorMod(p,15)];}
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
    private int cadencia(int base){return Math.max(12,Math.round(base*dificultad.cadencia));}
    private float apertura(float base){return base*dificultad.apertura;}
    private int azar(int sal,int limite){long z=semilla+tick*0x9e3779b97f4a7c15L+sal*31L;z=(z^(z>>>30))*0xbf58476d1ce4e5b9L;return Math.floorMod((int)(z^(z>>>27)),limite);}
    private void dirigido(int objetivo){if(solo)objetivo=0;if(!solo&&js[objetivo].vidas<=0)objetivo=1-objetivo;J j=js[objetivo];agregar(AX,j.y-13,AW,26,0,0,2,60,92,objetivo);mensaje="Pulso dirigido al jugador "+(objetivo+1)+": sal de la línea anunciada.";}
    private void generar(){int t=reloj;int p=fase==Fase.TRAICION?14:patron;gravedadDir=p==9?1:0;
        switch(p){
            case 0 -> {if(t%cadencia(42)==0){agregar(AX-35,AY+20+azar(0,195),28,28,4,0,0,0,260,-1);agregar(AX+AW+5,AY+20+azar(1,195),28,28,-4,0,0,0,260,-1);}}
            case 1 -> {if(t%cadencia(90)==0)agregar(AX,AY+25+azar(1,190),AW,26,0,0,1,40,70,-1);if(t%cadencia(60)==0)agregar(AX-30,AY+30+azar(0,175),24,24,3.5f,0,0,0,260,-1);}
            case 2,8 -> {if(t%cadencia(100)==0){float hueco=AY+50+azar(0,100);agregar(AX+AW+25,AY,28,hueco-AY,-3.5f,0,0,0,280,-1);agregar(AX+AW+25,hueco+apertura(90),28,AY+AH-hueco-apertura(90),-3.5f,0,0,0,280,-1);}}
            case 3 -> {if(t%cadencia(72)==0)agregar(AX+AW,AY+AH-35,30,35,-4.2f,0,0,0,220,-1);}
            case 4,10 -> {if(t==1){agregar(AX+120,AY+AH-65,160,12,.3f,0,3,0,500,-1);agregar(AX+410,AY+AH-115,180,12,-.25f,0,3,0,500,-1);}if(t%cadencia(100)==0)agregar(AX,AY+AH-29,AW,29,0,0,0,50,80,-1);}
            case 5 -> {if(t%cadencia(90)==0){agregar(AX+AW,AY+AH-55,40,55,-3.5f,0,0,0,260,-1);agregar(AX+AW+170,AY,40,65,-3.5f,0,0,0,260,-1);}}
            case 6,12 -> {if(t%cadencia(100)==1)dirigido((t/cadencia(100))%2);if(p==12&&t%cadencia(65)==0)agregar(AX+azar(0,660),AY-30,26,26,0,3.5f,0,0,150,-1);}
            case 7 -> {if(t%cadencia(65)==0){float hueco=AX+100+azar(0,450);for(int k=0;k<9;k++){float x=AX+k*80;if(Math.abs(x-hueco)>90)agregar(x,AY-30,24,24,0,3,0,0,160,-1);}}}
            case 9 -> {if(t%cadencia(90)==0)agregar(AX+AW-35,AY-30,35,30,0,3.3f,0,0,140,-1);}
            case 11 -> {if(t%cadencia(110)==0)agregar(AX,AY,AW,AH,0,0,1,55,75,-1);if(t%cadencia(110)==Math.round(80*dificultad.cadencia))agregar(AX-30,AY+60+azar(0,130),24,24,4.5f,0,0,0,220,-1);}
            case 13 -> {if(t%cadencia(45)==0){double abertura=t*.012;for(int k=0;k<14;k++){double a=k*Math.PI*2/14;double dif=Math.atan2(Math.sin(a-abertura),Math.cos(a-abertura));if(Math.abs(dif)<.6)continue;agregar(640+(float)Math.cos(a)*365,480+(float)Math.sin(a)*160,16,16,-(float)Math.cos(a)*2.8f,-(float)Math.sin(a)*2.0f,0,25,170,-1);}}}
            default -> {if(t%cadencia(95)==1)dirigido((t/cadencia(95))%2);if(t%cadencia(50)==0){float x=AX+50+nodoAtaque()*110;agregar(x,AY-25,20,20,0,3.8f,0,0,130,-1);}if(t%cadencia(150)==0)agregar(AX,AY+50+azar(1,140),AW,20,0,0,1,45,72,-1);}
        }
    }
    private void actualizarObstaculos(){for(int i=0;i<obs.size();i++){var a=obs.get(i);int edad=a.edad()+1;float x=a.x()+(edad>=a.aviso()?a.vx()*(1-Math.min(.25f,verificados*.025f)):0),y=a.y()+(edad>=a.aviso()?a.vy()*(1-Math.min(.25f,verificados*.025f)):0);obs.set(i,new Obstaculo(x,y,a.w(),a.h(),a.vx(),a.vy(),a.tipo(),edad,a.aviso(),a.duracion(),a.objetivo()));}obs.removeIf(a->a.edad()>a.duracion());}
    private void colisiones(){for(J j:js)if(j.vidas>0&&j.inv==0)for(var a:obs){if(a.tipo()==3||a.edad()<a.aviso()||a.tipo()==1&&!j.movido)continue;if(j.x+7>a.x()&&j.x-7<a.x()+a.w()&&j.y+7>a.y()&&j.y-7<a.y()+a.h()){j.vidas--;j.inv=75;j.puntos=Math.max(0,j.puntos-3);break;}}}
    private int[] ciudadActual(){int signo=fase==Fase.FINAL||fase==Fase.EPILOGO?(gano?1:-1):0;return new int[]{Math.min(100,base.informacionVerificada()+verificados*2),Math.max(0,Math.min(100,base.confianza()+signo*6)),Math.max(0,Math.min(100,base.convivencia()+signo*3)),Math.max(0,Math.min(100,base.bienestar()+signo*3)),Math.max(0,Math.min(100,base.participacion()+signo*3)),Math.max(0,Math.min(100,base.desinformacion()-signo*8)),Math.max(0,Math.min(100,base.conflictos()-signo*4))};}
    public synchronized Vista vista(){Jugador[] jugadores=new Jugador[2];for(int i=0;i<2;i++){J j=js[i];jugadores[i]=new Jugador(j.nombre,j.x,j.y,j.vx,j.vy,j.vidas,j.puntos,j.inv,j.recuperaciones,j.orientacion,j.movido,j.listo,j.conectado,j.accion);}return new Vista(id,semilla,tick,fase,ronda,patron,reloj,jefe,linea,golpes.clone(),elecciones.clone(),jugadores,List.copyOf(obs),pausa,gravedad(),gravedadDir,solo,gano,verificados,rumores,ciudadActual(),new int[]{base.informacionVerificada(),base.confianza(),base.convivencia(),base.bienestar(),base.participacion(),base.desinformacion(),base.conflictos()},mensaje,historial.toArray(String[]::new),publicaciones.recorrer("inverso").stream().map(a->"#"+a.id()+" · riesgo "+a.riesgo()+" · "+a.texto()).toArray(String[]::new),dificultad);}
    public static MotorBatalla restaurar(Vista v){MotorBatalla m=new MotorBatalla(v.id(),v.semilla(),v.solo(),v.dificultad());int[] c=v.base();m.base=new EstadoCiudad(c[0],c[1],c[2],c[3],c[4],c[5],c[6]);m.gravedadDir=v.gravedadDir();m.tick=v.tick();m.fase=v.fase();m.ronda=v.ronda();m.patron=v.patron();m.reloj=v.reloj();m.jefe=v.jefe();m.linea=v.linea();m.pausa=v.pausa();m.gano=v.gano();m.verificados=v.verificados();m.rumores=v.rumores();m.mensaje=v.mensaje();m.historial.addAll(List.of(v.historial()));m.obs.addAll(v.obstaculos());for(int i=0;i<2;i++){Jugador a=v.jugadores()[i];J j=m.js[i];j.nombre=a.nombre();j.x=a.x();j.y=a.y();j.vx=a.vx();j.vy=a.vy();j.vidas=a.vidas();j.puntos=a.puntos();j.inv=a.invulnerable();j.recuperaciones=a.recuperaciones();j.orientacion=a.orientacion();j.movido=a.movido();j.listo=a.listo();j.conectado=a.conectado();j.accion=a.accionLista();m.golpes[i]=v.golpes()[i];m.elecciones[i]=v.elecciones()[i];}for(int n=0;n<v.verificados();n++){var prioridad=m.publicaciones.recorrer("inverso");if(!prioridad.isEmpty())m.publicaciones.eliminar(prioridad.getFirst().id());}
        for(String h:v.historial())if(h.startsWith("Ronda "))try{int r=Integer.parseInt(h.substring(6,7))-1;int a=h.endsWith("verificar")?0:h.endsWith("pulso preciso")?1:2;m.decisiones.ejecutar("r"+r+"a"+a);}catch(RuntimeException ignorado){}
        m.normalizarIndividual();
        // Los checkpoints previos situaban la introducción debajo del servidor.
        if(m.fase==Fase.INTRO&&(m.js[0].y>CONVERSACION_Y||Math.abs(m.js[0].x-ENTRADA_X)>1))m.entrada();
        return m;}
}
