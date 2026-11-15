package alcaldedigital.servidor.carrera;

import alcaldedigital.compartido.carrera.*;
import alcaldedigital.compartido.carrera.EstadoCarrera.*;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.compartido.estructuras.grafo.GrafoDifusionCivitas;
import java.util.*;

/** Autoridad de la carrera: mismo movimiento, límites y colisiones para humanos y CPU. */
public final class MotorCarrera {
    public static final float META=9600,Y_MIN=320,Y_MAX=530,VELOCIDAD_MAX=4;
    public static final int[] HITOS={1400,3400,5600,7800};
    public static final String[] ROLES={"Periodista","Influencer"};
    public final ArbolAVLPublicaciones publicaciones=new ArbolAVLPublicaciones();
    public final ArbolDecisiones decisiones=new ArbolDecisiones("m7","La carrera por la primicia");
    private final GrafoDifusionCivitas red=new GrafoDifusionCivitas();
    private final String id;private final long semilla;private final int humanos;private final Dificultad dificultad;
    private EstadoElectoral electoral;private final int[] base,ciudad,grupos=new int[7];
    private final List<Objeto> objetos=new ArrayList<>();private final List<Onda> ondas=new ArrayList<>();private final List<String> historial=new ArrayList<>();
    private long tick;private Fase fase=Fase.ESPERA;private int reloj,linea,ganador=-1;private boolean pausa;
    private String mensaje="Confirma que estás listo. La elección sigue pendiente.";
    private static final class C {
        float distancia,y,impulso,velocidad,vertical,objetivoAtaque;boolean correr,conectado,listo,confirma,asistido;
        int puntos,reputacion=50,escudo,frenado,turbo,habilidad,avisoAtaque,pulsos,siguienteEvento;long ultimoPulso=-100;Evento evento;
    }
    private final C[] cs={new C(),new C()};
    public MotorCarrera(long semilla,int humanos,Dificultad d,EstadoCiudad ciudad,EstadoElectoral electoral,boolean asistido){this(UUID.randomUUID().toString(),semilla,humanos,d,vector(ciudad),electoral,asistido);}
    private MotorCarrera(String id,long semilla,int humanos,Dificultad d,int[] base,EstadoElectoral electoral,boolean asistido){
        if(humanos<1||humanos>3)throw new IllegalArgumentException("Roles humanos inválidos");
        this.id=id;this.semilla=semilla;this.humanos=humanos;dificultad=Objects.requireNonNull(d);this.base=base.clone();ciudad=base.clone();this.electoral=Objects.requireNonNull(electoral);
        for(int r=0;r<2;r++){cs[r].y=r==0?380:480;cs[r].asistido=asistido;cs[r].conectado=!humano(r);}
        for(int r=0;r<2;r++)for(int e=0;e<4;e++){String padre="r"+r+"e"+e;decisiones.insertar("m7",padre,ROLES[r]+": "+Noticia.values()[e]);for(int a=0;a<3;a++)decisiones.insertar(padre,padre+"a"+a,new String[]{"Contrastar / rechazar","Difundir / aceptar","Continuar"}[a]);}
        generarRecorrido();
    }
    public boolean humano(int r){return r>=0&&r<2&&(humanos&(1<<r))!=0;}
    public synchronized void conservarCorrupcion(boolean registrada){electoral=electoral.conservarCorrupcion(registrada);}
    public synchronized void conectar(int r,boolean conectado){if(!humano(r))return;cs[r].conectado=conectado;if(!conectado){detenerEntradas();cs[r].listo=false;if(fase!=Fase.ESPERA&&fase!=Fase.RESULTADO)pausa=true;mensaje="Conexión perdida. La carrera está detenida hasta que vuelvan ambos roles.";}}
    private boolean todos(boolean confirmacion){for(int r=0;r<2;r++)if(humano(r)&&(!cs[r].conectado||!(confirmacion?cs[r].confirma:cs[r].listo)))return false;return true;}
    private boolean conectados(){for(int r=0;r<2;r++)if(humano(r)&&!cs[r].conectado)return false;return true;}
    private void detenerEntradas(){for(var c:cs){c.vertical=0;c.correr=false;}}
    public synchronized void orden(int rol,float vertical,boolean correr,String accion,int valor){
        if(!humano(rol))return;C c=cs[rol];
        if(accion.equals("PAUSA")){pausa=true;detenerEntradas();mensaje="La carrera y la propagación están en pausa.";return;}
        if(accion.equals("REANUDAR")){if(conectados()){pausa=false;detenerEntradas();mensaje="Carrera reanudada.";}return;}
        if(accion.equals("ASISTENCIA")&&(pausa||fase==Fase.ESPERA)){c.asistido=valor!=0;detenerEntradas();return;}
        if(pausa)return;c.vertical=Float.isFinite(vertical)?Math.max(-1,Math.min(1,vertical)):0;c.correr=correr;
        switch(accion){
            case "LISTO"->{if(fase==Fase.ESPERA){c.listo=true;if(todos(false))cambiar(Fase.ANUNCIO);}}
            case "CONTINUAR"->{if(!HistoriaCarrera.dialogo(fase,ganador).isEmpty()){c.confirma=true;if(todos(true)){for(var j:cs)j.confirma=false;linea++;if(linea>=HistoriaCarrera.dialogo(fase,ganador).size())cambiar(siguiente(fase));}}}
            case "PULSO"->{if(fase==Fase.CARRERA)pulso(c);}
            case "USAR"->{if(fase==Fase.CARRERA)usar(rol);}
            case "ACCION"->{if(fase==Fase.CARRERA)resolver(rol,valor);}
            default->{}
        }
    }
    private static Fase siguiente(Fase f){return switch(f){case ANUNCIO->Fase.RIVALIDAD;case RIVALIDAD->Fase.SALIDA;case SALIDA->Fase.ENCUENTRO;case ENCUENTRO->Fase.CUENTA;case LLEGADA->Fase.EPILOGO;case EPILOGO->Fase.RESULTADO;default->f;};}
    private void cambiar(Fase f){fase=f;reloj=linea=0;detenerEntradas();for(var c:cs)c.confirma=false;}
    private int azar(long sal,int limite){long z=semilla+sal*0x9e3779b97f4a7c15L;z=(z^(z>>>30))*0xbf58476d1ce4e5b9L;return Math.floorMod((int)(z^(z>>>27)),limite);}
    private void generarRecorrido(){
        int id=1;
        for(int x=750;x<9400;x+=550){int fila=azar(x,3);float y=Y_MIN+30+fila*70;int grupo=Math.min(6,x/1400);
            objetos.add(new Objeto(id++,x,y,1,0,grupo));
            if(x%2==0)objetos.add(new Objeto(id++,x+140,Y_MIN+30+((fila+1)%3)*70,2,0,grupo));
            objetos.add(new Objeto(id++,x+280,Y_MIN+30+((fila+2)%3)*70,3+azar(x+7,3),0,grupo));
        }
    }
    public synchronized void paso(){
        if(pausa||fase==Fase.ESPERA||fase==Fase.RESULTADO)return;tick++;reloj++;
        if(fase==Fase.CUENTA){if(reloj>=180)cambiar(Fase.CARRERA);return;}
        if(fase!=Fase.CARRERA)return;
        actualizarOndas();float[] antes={cs[0].distancia,cs[1].distancia};
        for(int r=0;r<2;r++){
            C c=cs[r];if(!humano(r))cpu(r);
            if(c.asistido&&c.correr&&tick%12==0)pulso(c);
            if(c.escudo>0)c.escudo--;if(c.frenado>0)c.frenado--;if(c.turbo>0)c.turbo--;
            if(c.avisoAtaque>0&&--c.avisoAtaque==0){if(Math.abs(c.y-c.objetivoAtaque)<36)golpe(c,90);else c.puntos+=5;}
            c.impulso=Math.max(0,c.impulso-.05f);c.velocidad=Math.min(VELOCIDAD_MAX,1.3f+c.impulso+(c.turbo>0?.7f:0));if(c.frenado>0)c.velocidad*=.48f;
            c.y=Math.max(Y_MIN,Math.min(Y_MAX,c.y+c.vertical*3.2f));c.distancia+=c.velocidad;colisiones(r,antes[r]);
            if(c.evento!=null){var e=c.evento;c.evento=new Evento(e.indice(),e.tipo(),e.restante()-1,e.advertencia(),e.candidato(),e.verdadera());if(c.evento.restante()<=0)resolver(r,2);}
            if(c.evento==null&&c.siguienteEvento<4&&c.distancia>=HITOS[c.siguienteEvento])abrirEvento(r);
        }
        if(cs[0].distancia>=META||cs[1].distancia>=META){
            float t0=cs[0].distancia>=META?(META-antes[0])/cs[0].velocidad:Float.POSITIVE_INFINITY;
            float t1=cs[1].distancia>=META?(META-antes[1])/cs[1].velocidad:Float.POSITIVE_INFINITY;
            ganador=Math.abs(t0-t1)<.0001?2:t0<t1?0:1;
            for(int r=0;r<2;r++){if(cs[r].evento!=null)resolver(r,2);cs[r].puntos+=ganador==2?25:ganador==r?35:10;}
            mensaje=ganador==2?"Primicia compartida. La reputación refleja las decisiones.":ROLES[ganador]+" consiguió la primicia. Falta investigar el anuncio.";
            historial.add(mensaje);cambiar(Fase.LLEGADA);
        }
    }
    private void pulso(C c){if(tick-c.ultimoPulso<10)return;c.ultimoPulso=tick;c.impulso=Math.min(2.7f,c.impulso+.8f);c.pulsos++;}
    private void cpu(int r){C c=cs[r];c.asistido=false;c.vertical=0;c.correr=false;
        int ritmo=dificultad.ritmoCPU;if(tick%ritmo==0&&azar(tick+r,100)>=dificultad.errorCPU*100)pulso(c);
        Objeto proximo=null;for(var a:objetos)if(a.x()>c.distancia&&a.x()<c.distancia+210&&a.tipo()<=2&&(a.consumido()&(1<<r))==0&&(proximo==null||a.x()<proximo.x()))proximo=a;
        if(proximo!=null&&Math.abs(proximo.y()-c.y)<48&&azar(proximo.id()*997L+r,100)>=dificultad.errorCPU*100)c.vertical=c.y<425?1:-1;
        if(c.avisoAtaque>0)c.vertical=c.y<425?1:-1;
        if(c.habilidad!=0&&tick%90==0)usar(r);
        if(c.evento!=null&&c.evento.restante()<350){int accion=c.evento.tipo()==Noticia.PACTO?0:azar(c.evento.indice()*31L+semilla+r,100)<18?1:0;resolver(r,accion);}
    }
    private void usar(int r){C c=cs[r];if(c.habilidad==4)c.escudo=240;else if(c.habilidad==5){C otro=cs[1-r];otro.avisoAtaque=60;otro.objetivoAtaque=otro.y;mensaje="Distracción anunciada: cambia de carril para evitarla.";}else return;c.habilidad=0;}
    private void golpe(C c,int duracion){if(c.escudo>0){c.escudo=0;mensaje="El escudo bloqueó una distracción.";}else c.frenado=Math.max(c.frenado,duracion);}
    private void colisiones(int r,float antes){C c=cs[r];for(int i=0;i<objetos.size();i++){
        Objeto a=objetos.get(i);if((a.consumido()&(1<<r))!=0||antes>a.x()+26||c.distancia<a.x()-26||Math.abs(c.y-a.y())>33)continue;
        objetos.set(i,new Objeto(a.id(),a.x(),a.y(),a.tipo(),a.consumido()|(1<<r),a.grupo()));
        if(a.tipo()==1){if(grupos[a.grupo()]==2)golpe(c,110);else if(grupos[a.grupo()]==1)golpe(c,45);else c.puntos+=2;}
        else if(a.tipo()==2)golpe(c,85);else if(a.tipo()==3){c.turbo=150;c.puntos+=3;}else c.habilidad=a.tipo();
    }}
    private void abrirEvento(int r){C c=cs[r];int e=c.siguienteEvento;Noticia tipo=Noticia.values()[e];c.evento=new Evento(e,tipo,480,false,e==2?0:azar(semilla+e*11+r,4),tipo==Noticia.ATAJO&&azar(e*17L+r,2)==0);
        publicaciones.insertar(new Publicacion(idEvento(r,e),50+e*10,ROLES[r],HistoriaCarrera.titulo(tipo),"Sin contrastar"));}
    private static long idEvento(int r,int e){return 700+e*2L+r;}
    private void resolver(int r,int accion){C c=cs[r];Evento e=c.evento;if(e==null||accion<0||accion>3)return;
        if(e.tipo()==Noticia.PACTO&&accion==1&&!e.advertencia()){c.evento=new Evento(e.indice(),e.tipo(),e.restante(),true,0,e.verdadera());mensaje="Advertencia: confirmar el pacto corrompe a Juan y lo excluye de la elección.";return;}
        if(accion==3){if(e.tipo()!=Noticia.PACTO||!e.advertencia()||!humano(r))return;accion=1;}
        else if(e.tipo()==Noticia.PACTO&&accion==1)return;
        int candidato=e.candidato();String motivo;
        if(accion==0){
            c.reputacion=Math.min(100,c.reputacion+8);c.puntos+=15;c.frenado=Math.max(c.frenado,r==0?24:45);efectoCiudad(2,2,2,1,1,-2,-1);difundir(r,3);
            int cambioApoyo=e.tipo()==Noticia.PACTO?4:e.tipo()==Noticia.ATAJO&&e.verdadera()?3:-4;
            int cambioConfianza=cambioApoyo>0?3:-5;
            String efectoElectoral=EstadoElectoral.NOMBRES[candidato]+": apoyo "+(cambioApoyo>0?"+":"")+cambioApoyo;
            electoral=electoral.aplicar(candidato,cambioApoyo,cambioConfianza,1,-1,1,ROLES[r]+" contrastó "+e.tipo()+" · "+efectoElectoral,false);
            publicaciones.eliminar(idEvento(r,e.indice()));
            if(e.tipo()==Noticia.ATAJO)c.turbo=e.verdadera()?210:80;
            motivo=(e.tipo()==Noticia.PACTO?"Rechazó el pacto. Se conserva la condición de Juan.":e.tipo()==Noticia.ATAJO?(e.verdadera()?"La ruta tiene una fuente válida.":"El supuesto atajo era falso."):e.tipo()==Noticia.RUMOR?"La acusación era un montaje sin pruebas.":"Compartir no equivale a votar. El anuncio no era oficial.")+" "+efectoElectoral+".";
        }else if(accion==1){
            c.reputacion=Math.max(0,c.reputacion-12);c.turbo=180;efectoCiudad(-1,-3,-3,-2,0,5,4);difundir(r,2);
            if(e.tipo()==Noticia.PACTO){electoral=electoral.aplicar(0,8,-15,0,8,0,"Pacto ejecutado: Juan autorizó el pago oculto y el montaje",true);motivo="Juan autorizó el pago oculto. Queda corrupto y no puede ganar.";}
            else{electoral=electoral.aplicar(candidato,5,-2,0,3,0,ROLES[r]+" difundió sin comprobar "+e.tipo()+" · "+EstadoElectoral.NOMBRES[candidato]+": apoyo +5",false);motivo="Publicó sin contrastar: ventaja temporal y más rumores. "+EstadoElectoral.NOMBRES[candidato]+": apoyo +5, confianza -2.";}
            if(e.tipo()==Noticia.ATAJO&&!e.verdadera())c.frenado=170;
            if(e.tipo()==Noticia.RUMOR){cs[1-r].avisoAtaque=60;cs[1-r].objetivoAtaque=cs[1-r].y;}
            Publicacion p=publicaciones.buscar(idEvento(r,e.indice()));if(p!=null){publicaciones.eliminar(p.id());publicaciones.insertar(p.conRiesgo(90,"Difundida sin verificar"));}
        }else{c.puntos+=2;motivo=e.tipo()==Noticia.PACTO?"Continuó sin ejecutar el pacto.":"Continuó la carrera sin difundir la publicación.";}
        decisiones.ejecutar("r"+r+"e"+e.indice()+"a"+accion);historial.add("r"+r+"e"+e.indice()+"a"+accion+" · "+ROLES[r]+": "+motivo);mensaje=motivo;c.siguienteEvento++;c.evento=null;
    }
    private void difundir(int r,int estado){int origen=r==0?2:0;int demora=0;for(int nodo:red.ordenDesde(origen)){ondas.add(new Onda(nodo,estado,demora));demora+=20;}}
    private void actualizarOndas(){for(int i=ondas.size()-1;i>=0;i--){Onda o=ondas.get(i);if(o.demora()<=0){grupos[o.nodo()]=o.estado();ondas.remove(i);}else ondas.set(i,new Onda(o.nodo(),o.estado(),o.demora()-1));}
        for(int i=0;i<7;i++)if(grupos[i]==0&&Math.max(cs[0].distancia,cs[1].distancia)>3200+i*350)grupos[i]=1;}
    private void efectoCiudad(int... cambio){for(int i=0;i<7;i++)ciudad[i]=Math.max(0,Math.min(100,ciudad[i]+cambio[i]));}
    public synchronized Vista vista(){Corredor[] a=new Corredor[2];for(int i=0;i<2;i++){C c=cs[i];a[i]=new Corredor(c.distancia,c.y,c.impulso,c.velocidad,c.puntos,c.reputacion,c.escudo,c.frenado,c.turbo,c.habilidad,c.avisoAtaque,c.objetivoAtaque,c.ultimoPulso,c.pulsos,c.siguienteEvento,c.conectado,c.listo,c.confirma,c.asistido,c.evento);}
        return new Vista(id,semilla,tick,fase,reloj,linea,dificultad,humanos,pausa,ganador,a,List.copyOf(objetos),grupos.clone(),electoral,ciudad.clone(),base.clone(),List.copyOf(historial),publicaciones.recorrer("inverso"),List.copyOf(ondas),mensaje);}
    public static MotorCarrera restaurar(Vista v){
        MotorCarrera m=new MotorCarrera(v.id(),v.semilla(),v.humanos(),v.dificultad(),v.base(),v.electoral(),true);m.tick=v.tick();m.fase=v.fase();m.reloj=v.reloj();m.linea=v.linea();m.ganador=v.ganador();m.pausa=v.pausa();m.mensaje=v.mensaje();
        System.arraycopy(v.ciudad(),0,m.ciudad,0,7);System.arraycopy(v.grupos(),0,m.grupos,0,7);m.objetos.clear();m.objetos.addAll(v.objetos());m.ondas.addAll(v.ondas());m.historial.addAll(v.historial());
        for(int r=0;r<2;r++){C c=m.cs[r];var a=v.corredores()[r];c.distancia=a.distancia();c.y=a.y();c.impulso=a.impulso();c.velocidad=a.velocidad();c.puntos=a.puntos();c.reputacion=a.reputacion();c.escudo=a.escudo();c.frenado=a.frenado();c.turbo=a.turbo();c.habilidad=a.habilidad();c.avisoAtaque=a.avisoAtaque();c.objetivoAtaque=a.objetivoAtaque();c.ultimoPulso=a.ultimoPulso();c.pulsos=a.pulsos();c.siguienteEvento=a.siguienteEvento();c.conectado=a.conectado();c.listo=a.listo();c.confirma=a.confirma();c.asistido=a.asistido();c.evento=a.evento();}
        for(var p:v.publicaciones())m.publicaciones.insertar(p);
        for(String h:v.historial())if(h.matches("r[01]e[0-3]a[0-2] · .*"))m.decisiones.ejecutar(h.substring(0,6));return m;
    }
    public static int[] vector(EstadoCiudad c){return new int[]{c.informacionVerificada(),c.confianza(),c.convivencia(),c.bienestar(),c.participacion(),c.desinformacion(),c.conflictos()};}
}
