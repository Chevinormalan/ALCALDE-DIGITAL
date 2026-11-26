package alcaldedigital.batalla;

import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import alcaldedigital.compartido.batalla.PaqueteCruzado;
import alcaldedigital.compartido.batalla.PaqueteRastreador;
import alcaldedigital.servidor.batalla.MotorBatalla;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Rutas con controles legales y sin impactos; la curación de ronda no puede ocultar un golpe. */
class PatronesPasablesTest {
    static MotorBatalla iniciar(long semilla,int patron,Dificultad nivel,boolean solo){
        return iniciar(semilla,patron,nivel,solo,Fase.AVISO);
    }
    static MotorBatalla iniciar(long semilla,int patron,Dificultad nivel,boolean solo,Fase fase){
        Vista v=new MotorBatalla(semilla,solo,nivel).vista();Jugador[] js=v.jugadores();
        for(int i=0;i<2;i++)js[i]=new Jugador(js[i].nombre(),i==0?500:770,550,0,0,solo&&i==1?0:5,0,0,0,0,false,true,!solo||i==0,false);
        return MotorBatalla.restaurar(new Vista(v.id(),semilla,10000+semilla*97+patron*523,fase,patron/3,patron,0,100,0,v.golpes(),v.elecciones(),js,List.of(),false,false,0,solo,false,0,15,v.ciudad(),v.base(),"Comprobación",new String[0],v.publicaciones(),nivel));
    }
    static long[] semillas(){return java.util.stream.LongStream.concat(java.util.stream.LongStream.range(0,32),java.util.stream.LongStream.of(2026)).toArray();}
    private static float hacia(float diferencia){return Math.abs(diferencia)<1.9f?0:Math.signum(diferencia);}
    // El verificador reacciona a los avisos visibles y usa entradas de teclado. No ejecuta el generador
    // por adelantado ni cambia la física o los tiempos de la partida.
    static float[] control(Vista v,int rol){
        Jugador j=v.jugadores()[rol];
        for(var a:v.obstaculos())if(a.tipo()==PaqueteRastreador.TIPO)
            return esquivarRastreador(j,a,PaqueteRastreador.para(v.dificultad()));
        var cruzados=v.obstaculos().stream()
            .filter(a->a.tipo()==PaqueteCruzado.TIPO&&a.edad()<PaqueteCruzado.inicioDisipacion(a))
            .toList();
        if(!cruzados.isEmpty())return esquivarCruzados(j,cruzados);
        var peligros=v.obstaculos().stream().filter(a->a.tipo()==0||a.tipo()==2).toList();
        if(peligros.isEmpty())return new float[3];
        if(v.patron()==2&&v.fase()==Fase.ESQUIVA){
            float techo=MotorBatalla.AY,suelo=MotorBatalla.AY+MotorBatalla.AH;
            for(var a:peligros){
                if(a.y()==MotorBatalla.AY)techo=Math.max(techo,a.y()+a.h());
                else suelo=Math.min(suelo,a.y());
            }
            return new float[]{0,hacia((techo+suelo)/2-j.y()),0};
        }
        for(var a:peligros){
            boolean piso=v.gravedad()&&v.gravedadDir()==0&&a.w()==MotorBatalla.AW&&a.y()>550;
            boolean pared=v.gravedad()&&v.gravedadDir()==1&&a.h()==MotorBatalla.AH&&a.x()>950;
            if(piso||pared)return new float[]{0,0,a.aviso()-a.edad()<=10?1:0};
        }
        if(seguro(j.x(),j.y(),peligros))return new float[3];
        var xs=new ArrayList<Float>();
        var ys=new ArrayList<Float>();
        xs.add(j.x());ys.add(j.y());
        boolean moverX=!v.gravedad()||v.gravedadDir()==0;
        boolean moverY=!v.gravedad()||v.gravedadDir()==1;
        for(var a:peligros){
            if(moverX){xs.add(a.x()-12);xs.add(a.x()+a.w()+12);}
            if(moverY){ys.add(a.y()-12);ys.add(a.y()+a.h()+12);}
        }
        float tx=j.x(),ty=j.y(),distancia=Float.POSITIVE_INFINITY;
        for(float x:xs)for(float y:ys){
            if(x<MotorBatalla.AX+12||x>MotorBatalla.AX+MotorBatalla.AW-12
                    ||y<MotorBatalla.AY+12||y>MotorBatalla.AY+MotorBatalla.AH-12||!seguro(x,y,peligros))continue;
            float d=Math.max(Math.abs(x-j.x()),Math.abs(y-j.y()))+.01f*(Math.abs(x-j.x())+Math.abs(y-j.y()));
            if(d<distancia){distancia=d;tx=x;ty=y;}
        }
        return new float[]{hacia(tx-j.x()),hacia(ty-j.y()),0};
    }

    private static float[] esquivarRastreador(Jugador j,Obstaculo a,PaqueteRastreador ritmo) {
        var fase=ritmo.fase(a);
        if(fase==PaqueteRastreador.Fase.DISIPACION)return new float[3];
        float cx=a.x()+a.w()/2,cy=a.y()+a.h()/2;
        boolean rutaVisible=fase==PaqueteRastreador.Fase.FIJADO||fase==PaqueteRastreador.Fase.EMBESTIDA;
        int pasos=fase==PaqueteRastreador.Fase.FIJADO?ritmo.embestida():ritmo.inicioDisipacion(a)-a.edad();
        float vx=rutaVisible?a.vx()*pasos:0,vy=rutaVisible?a.vy()*pasos:0;
        float[] mejor=new float[3];
        double separacion=distanciaRuta(j.x()-cx,j.y()-cy,vx,vy);
        if(separacion>(rutaVisible?32:160))return mejor;
        for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++){
            float x=Math.clamp(j.x()+dx*38,MotorBatalla.AX+12,MotorBatalla.AX+MotorBatalla.AW-12);
            float y=Math.clamp(j.y()+dy*38,MotorBatalla.AY+12,MotorBatalla.AY+MotorBatalla.AH-12);
            double margen=distanciaRuta(x-cx,y-cy,vx,vy);
            if(margen>separacion){separacion=margen;mejor[0]=dx;mejor[1]=dy;}
        }
        return mejor;
    }

    private static double distanciaRuta(float x,float y,float vx,float vy){
        float proyeccion=vx==0&&vy==0?0:Math.clamp((x*vx+y*vy)/(vx*vx+vy*vy),0,1);
        return Math.hypot(x-vx*proyeccion,y-vy*proyeccion);
    }

    private static float[] esquivarCruzados(Jugador j,List<Obstaculo> cruzados){
        float[] entrada=new float[3];
        double margen=margenEntreCruzados(j.x(),j.y(),cruzados);
        if(margen>32)return entrada;
        for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++){
            float x=Math.clamp(j.x()+dx*38,MotorBatalla.AX+12,MotorBatalla.AX+MotorBatalla.AW-12);
            float y=Math.clamp(j.y()+dy*38,MotorBatalla.AY+12,MotorBatalla.AY+MotorBatalla.AH-12);
            double candidato=margenEntreCruzados(x,y,cruzados);
            if(candidato>margen){
                margen=candidato;
                entrada[0]=dx;
                entrada[1]=dy;
            }
        }
        return entrada;
    }

    private static double margenEntreCruzados(float x,float y,List<Obstaculo> cruzados){
        double margen=Double.POSITIVE_INFINITY;
        for(var a:cruzados){
            int pasos=PaqueteCruzado.pasosRestantes(a);
            margen=Math.min(margen,distanciaRuta(x-(a.x()+a.w()/2),y-(a.y()+a.h()/2),a.vx()*pasos,a.vy()*pasos));
        }
        return margen;
    }
    private static boolean seguro(float x,float y,List<Obstaculo> peligros){
        for(var a:peligros)if(x+9>a.x()&&x-9<a.x()+a.w()&&y+9>a.y()&&y-9<a.y()+a.h())return false;
        return true;
    }
    static String reproducir(MotorBatalla motor){
        int activos=motor.vista().solo()?1:2,patronInicial=motor.vista().patron();
        while(motor.vista().fase()==Fase.ESQUIVA||motor.vista().fase()==Fase.AVISO&&motor.vista().patron()==patronInicial){Vista v=motor.vista();for(int r=0;r<activos;r++){float[] c=control(v,r);motor.orden(r,c[0],c[1],c[2]>0,"",0);}motor.paso();for(int r=0;r<activos;r++)if(recibioImpacto(motor.vista(),r))return "paso "+v.reloj()+" jugador "+r+" en "+v.jugadores()[r].x()+","+v.jugadores()[r].y();}
        return "";
    }
    private static boolean recibioImpacto(Vista v,int rol){return v.jugadores()[rol].vidas()!=5||v.fase()==Fase.TURNO&&v.jugadores()[rol].invulnerable()>0;}
    @Test void secuenciasVarianYElEstadoGuardadoConservaLaSiguienteOleada() {
        for(int p=0;p<15;p++){
            Set<String> variantes=new HashSet<>();
            for(long semilla:semillas()){
                var a=iniciar(semilla,p,Dificultad.NORMAL,true,Fase.ESQUIVA);
                for(int t=0;t<95;t++)a.paso();
                var copia=MotorBatalla.restaurar(a.vista());
                String firma="";
                for(int t=95;t<MotorBatalla.duracion(p);t++){
                    a.paso();copia.paso();
                    assertEquals(a.vista().obstaculos(),copia.vista().obstaculos());
                    if(firma.isEmpty()&&a.vista().obstaculos().stream().anyMatch(o->o.edad()==1))
                        firma=t+":"+a.vista().obstaculos();
                }
                variantes.add(firma);
            }
            assertTrue(variantes.size()>1,"Patrón "+(p+1));
        }
    }

    @Test void rutasSinImpactosEnQuincePatronesYTresDificultades(){List<String> fallos=new ArrayList<>();int casos=0;
        for(Dificultad d:Dificultad.values())for(boolean solo:new boolean[]{true,false})for(int p=0;p<15;p++)for(long semilla:semillas()){casos++;String fallo=reproducir(iniciar(semilla,p,d,solo));if(!fallo.isEmpty())fallos.add(d+" "+(solo?"solo":"coop")+" patrón "+p+" semilla "+semilla+": "+fallo);}
        System.out.println("PASABILIDAD: "+casos+" casos; fallos "+fallos.size());assertTrue(fallos.isEmpty(),String.join("\n",fallos));
    }
    @Test void recorridosCompletosConservanLasCincoVidasEnLasTresDificultades(){
        List<String> fallos=new ArrayList<>();int casos=0;
        for(Dificultad d:Dificultad.values())for(boolean solo:new boolean[]{true,false})for(long semilla:new long[]{0,1,7,2026}){
            casos++;MotorBatalla m=iniciar(semilla,0,d,solo);int activos=solo?1:2;boolean golpe=false;
            for(int t=0;t<15000&&m.vista().fase()!=Fase.DECISION;t++){
                Vista v=m.vista();for(int rol=0;rol<activos;rol++){
                    if(v.fase()==Fase.ESQUIVA){float[] c=control(v,rol);m.orden(rol,c[0],c[1],c[2]>0,"",0);}
                    else if(v.fase()==Fase.TURNO)m.orden(rol,0,0,false,"ACCION",0);
                    else m.orden(rol,0,0,false,"",0);
                }
                m.paso();for(int rol=0;rol<activos;rol++)if(recibioImpacto(m.vista(),rol)){fallos.add(d+" "+solo+" semilla "+semilla+" patrón "+v.patron()+" paso "+v.reloj()+" rol "+rol);golpe=true;break;}
                if(golpe)break;
            }
            if(!golpe&&m.vista().fase()!=Fase.DECISION)fallos.add("Recorrido sin terminar: "+d+" "+solo+" "+semilla);
        }
        System.out.println("RECORRIDOS COMPLETOS: "+casos+"; fallos "+fallos.size());assertTrue(fallos.isEmpty(),String.join("\n",fallos));
    }
    @Test void losCincoRefugiosReproducidosYaExigenMoverseDuranteElAtaque(){
        int[][] casos={{0,500,770,603},{1,500,770,603},{7,480,800,550},{11,500,770,603},{13,292,988,603}};
        for(Dificultad d:Dificultad.values())for(long semilla:semillas())for(int[] caso:casos){
            var m=iniciar(semilla,caso[0],d,false);
            boolean[] impacto=new boolean[2];
            int pasos=36+MotorBatalla.duracion(caso[0]);
            for(int i=0;i<pasos;i++){
                var v=m.vista();
                for(int rol=0;rol<2;rol++){
                    var j=v.jugadores()[rol];
                    m.orden(rol,hacia(caso[rol+1]-j.x()),hacia(caso[3]-j.y()),false,"",0);
                }
                m.paso();
                for(int rol=0;rol<2;rol++)impacto[rol]|=recibioImpacto(m.vista(),rol);
            }
            assertTrue(impacto[0]&&impacto[1],d+" semilla "+semilla+" patrón "+(caso[0]+1));
        }
    }

    @Test void elUltimoAtaqueDeSieteSegundosTieneSalidaEnIndividualYCooperativo(){
        for(Dificultad d:Dificultad.values())for(boolean solo:new boolean[]{true,false})for(long semilla:semillas()){
            var m=iniciar(semilla,14,d,solo,Fase.TRAICION);
            for(int i=0;i<420;i++){
                var v=m.vista();
                assertFalse(v.obstaculos().stream().anyMatch(a->a.tipo()==PaqueteCruzado.TIPO||a.tipo()==PaqueteRastreador.TIPO));
                for(int rol=0;rol<(solo?1:2);rol++){
                    var c=control(v,rol);
                    m.orden(rol,c[0],c[1],false,"",0);
                }
                m.paso();
                for(int rol=0;rol<(solo?1:2);rol++){
                    assertFalse(recibioImpacto(m.vista(),rol),d+" semilla "+semilla+" rol "+rol+" paso "+i);
                }
            }
            assertEquals(Fase.EPILOGO,m.vista().fase());
            assertTrue(m.vista().gano());
        }
    }

    public static void main(String[] args){List<String> fallos=new ArrayList<>();for(Dificultad d:Dificultad.values())for(boolean solo:new boolean[]{true,false})for(int p=0;p<15;p++)for(long semilla:semillas()){String f=reproducir(iniciar(semilla,p,d,solo));if(!f.isEmpty())fallos.add(d+" "+solo+" p"+p+" s"+semilla+": "+f);}fallos.forEach(System.out::println);System.out.println("Fallos "+fallos.size());}
}
