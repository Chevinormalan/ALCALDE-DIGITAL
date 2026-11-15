package alcaldedigital.compartido.carrera;

import alcaldedigital.compartido.modelo.EstadoCiudad;
import java.util.*;

/** Apoyos provisionales: ninguna pantalla permite escoger al ganador. */
public record EstadoElectoral(boolean juanCorrupto,int[] apoyo,int[] confianza,int[] verificadas,
                             int[] conflictos,int[] participacion,List<String> historial) {
    public static final String[] NOMBRES={"Juan","Petrosqui","Abel","Alberto"};
    public EstadoElectoral {
        apoyo=copia(apoyo);confianza=copia(confianza);verificadas=copia(verificadas);
        conflictos=copia(conflictos);participacion=copia(participacion);historial=List.copyOf(historial);
    }
    private static int[] copia(int[] a){if(a.length!=4)throw new IllegalArgumentException("Se requieren cuatro candidaturas");return a.clone();}
    @Override public int[] apoyo(){return apoyo.clone();}
    @Override public int[] confianza(){return confianza.clone();}
    @Override public int[] verificadas(){return verificadas.clone();}
    @Override public int[] conflictos(){return conflictos.clone();}
    @Override public int[] participacion(){return participacion.clone();}
    public static EstadoElectoral inicial(EstadoCiudad c){
        return new EstadoElectoral(false,new int[]{30+c.informacionVerificada()/5-c.desinformacion()/5,
            33+c.participacion()/6+c.desinformacion()/8,32+c.bienestar()/6+c.conflictos()/7,
            32+c.desinformacion()/4+c.participacion()/7},
            new int[]{c.confianza(),45,43,40},new int[]{c.informacionVerificada()/10,3,3,2},
            new int[]{c.conflictos()/8,5,6,7},new int[]{c.participacion()/10,5,5,5},List.of());
    }
    public boolean elegible(int candidato){return candidato>=0&&candidato<4&&!(candidato==0&&juanCorrupto);}
    public int respaldo(int i){return apoyo[i]*3+confianza[i]+verificadas[i]*2+participacion[i]-conflictos[i]*2;}
    public int liderProvisional(){int mejor=-1;for(int i=0;i<4;i++)if(elegible(i)&&(mejor<0||respaldo(i)>respaldo(mejor)))mejor=i;return mejor;}
    public EstadoElectoral aplicar(int candidato,int a,int c,int v,int f,int p,String motivo,boolean corrupcion){
        if(candidato<0||candidato>3)throw new IllegalArgumentException("Candidato desconocido");
        int[] aa=apoyo.clone(),cc=confianza.clone(),vv=verificadas.clone(),ff=conflictos.clone(),pp=participacion.clone();
        aa[candidato]=limitar(aa[candidato]+a);cc[candidato]=limitar(cc[candidato]+c);
        vv[candidato]=Math.max(0,vv[candidato]+v);ff[candidato]=Math.max(0,ff[candidato]+f);pp[candidato]=Math.max(0,pp[candidato]+p);
        var h=new ArrayList<>(historial);if(h.size()<120)h.add(motivo);
        return new EstadoElectoral(juanCorrupto||corrupcion,aa,cc,vv,ff,pp,h);
    }
    public EstadoElectoral conservarCorrupcion(boolean registrada){return registrada&&!juanCorrupto?
        aplicar(0,0,0,0,0,0,"La campaña conserva el pacto corrupto ya registrado",true):this;}
    private static int limitar(int x){return Math.max(0,Math.min(100,x));}
}
