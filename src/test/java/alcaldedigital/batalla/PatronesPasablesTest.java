package alcaldedigital.batalla;

import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import alcaldedigital.servidor.batalla.MotorBatalla;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Rutas con controles legales y sin impactos; la curación de ronda no puede ocultar un golpe. */
class PatronesPasablesTest {
    static MotorBatalla iniciar(long semilla,int patron,Dificultad nivel,boolean solo){
        Vista v=new MotorBatalla(semilla,solo,nivel).vista();Jugador[] js=v.jugadores();
        for(int i=0;i<2;i++)js[i]=new Jugador(js[i].nombre(),i==0?500:770,550,0,0,solo&&i==1?0:5,0,0,0,0,false,true,!solo||i==0,false);
        return MotorBatalla.restaurar(new Vista(v.id(),semilla,10000+semilla*97+patron*523,Fase.ESQUIVA,patron/3,patron,0,100,0,v.golpes(),v.elecciones(),js,List.of(),false,false,0,solo,false,0,15,v.ciudad(),v.base(),"Comprobación",new String[0],v.publicaciones(),nivel));
    }
    static long[] semillas(){return java.util.stream.LongStream.concat(java.util.stream.LongStream.range(0,32),java.util.stream.LongStream.of(2026)).toArray();}
    private static float hacia(float diferencia){return Math.abs(diferencia)<1.9f?0:Math.signum(diferencia);}
    static float[] control(Vista v,int rol){
        Jugador j=v.jugadores()[rol];int p=v.patron();float tx=j.x(),ty=j.y();boolean salto=false;
        switch(p){
            case 0,1,11 -> ty=603;
            case 2,8 -> {
                float x=Float.POSITIVE_INFINITY;for(var a:v.obstaculos())if(a.vx()<0&&a.x()+a.w()>=j.x()-7&&a.x()<x)x=a.x();
                if(Float.isFinite(x)){float arriba=345,abajo=615;for(var a:v.obstaculos())if(Math.abs(a.x()-x)<1){if(a.y()==345)arriba=a.y()+a.h();else abajo=a.y();}ty=(arriba+abajo)/2;}
            }
            case 3,5 -> {float tiempo=Float.POSITIVE_INFINITY;for(var a:v.obstaculos())if(a.tipo()==0&&a.y()>550&&a.vx()<0&&a.x()+a.w()>j.x()-8)tiempo=Math.min(tiempo,(a.x()-j.x()-7)/-a.vx());salto=tiempo<12;}
            case 4,10 -> {tx=480;for(var a:v.obstaculos())if(a.tipo()==3&&a.y()>530){tx=a.x()+a.w()/2;break;}salto=v.reloj()%50<24;}
            case 6,12,14 -> {
                tx=p==14?988:j.x();ty=j.y();
                int masNuevo=Integer.MAX_VALUE;for(var a:v.obstaculos())if(a.tipo()==2&&a.edad()<masNuevo){masNuevo=a.edad();ty=a.y()+a.h()/2>586?570:603;}
                if(p==12)for(var a:v.obstaculos())if(a.tipo()==0&&a.vy()>0&&a.y()>j.y()-180&&a.y()<j.y()+10){
                    float centro=a.x()+a.w()/2;
                    if(Math.abs(j.x()-centro)<70){float direccion=j.x()>=centro?1:-1;if(j.x()<350)direccion=1;if(j.x()>930)direccion=-1;tx=j.x()+direccion*20;}
                }
            }
            case 7 -> tx=280+40+Math.round((j.x()-320)/80)*80;
            case 9 -> {ty=603;for(var a:v.obstaculos())if(a.tipo()==0&&a.x()>950&&a.y()>j.y()-75&&a.y()<j.y()+30)salto=true;}
            case 13 -> {tx=rol==0?292:988;ty=603;}
        }
        return new float[]{hacia(tx-j.x()),hacia(ty-j.y()),salto?1:0};
    }
    static String reproducir(MotorBatalla motor){
        int activos=motor.vista().solo()?1:2;
        while(motor.vista().fase()==Fase.ESQUIVA){Vista v=motor.vista();for(int r=0;r<activos;r++){float[] c=control(v,r);motor.orden(r,c[0],c[1],c[2]>0,"",0);}motor.paso();for(int r=0;r<activos;r++)if(recibioImpacto(motor.vista(),r))return "paso "+v.reloj()+" jugador "+r+" en "+v.jugadores()[r].x()+","+v.jugadores()[r].y();}
        return "";
    }
    private static boolean recibioImpacto(Vista v,int rol){return v.jugadores()[rol].vidas()!=5||v.fase()==Fase.TURNO&&v.jugadores()[rol].invulnerable()>0;}
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
    public static void main(String[] args){List<String> fallos=new ArrayList<>();for(Dificultad d:Dificultad.values())for(boolean solo:new boolean[]{true,false})for(int p=0;p<15;p++)for(long semilla:semillas()){String f=reproducir(iniciar(semilla,p,d,solo));if(!f.isEmpty())fallos.add(d+" "+solo+" p"+p+" s"+semilla+": "+f);}fallos.forEach(System.out::println);System.out.println("Fallos "+fallos.size());}
}
