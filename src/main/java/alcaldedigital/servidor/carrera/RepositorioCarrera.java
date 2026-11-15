package alcaldedigital.servidor.carrera;

import alcaldedigital.compartido.carrera.*;
import alcaldedigital.compartido.carrera.EstadoCarrera.*;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import java.nio.file.*;
import java.nio.channels.*;
import java.io.*;
import java.util.*;
import java.util.function.Function;

/** Comparte campana.properties sin borrar las claves de las misiones anteriores. */
public final class RepositorioCarrera {
    private static final Object CERROJO=new Object();private final Path archivo;
    public RepositorioCarrera(Path archivo){this.archivo=archivo.toAbsolutePath();}
    private <T>T transaccion(boolean escribir,Function<Properties,T> funcion)throws IOException{
        synchronized(CERROJO){Files.createDirectories(archivo.getParent());
            try(var canal=FileChannel.open(archivo.resolveSibling("campana-m7.lock"),StandardOpenOption.CREATE,StandardOpenOption.WRITE);var bloqueo=canal.lock()){
                Properties p=new Properties();if(Files.exists(archivo))try(var in=Files.newInputStream(archivo)){p.load(in);}T resultado=funcion.apply(p);
                if(escribir){Path tmp=Files.createTempFile(archivo.getParent(),"campana-m7-",".tmp");try{try(var out=Files.newOutputStream(tmp)){p.store(out,"Alcalde Digital · campaña compartida");}try{Files.move(tmp,archivo,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,archivo,StandardCopyOption.REPLACE_EXISTING);}}finally{Files.deleteIfExists(tmp);}}
                return resultado;
            }catch(IllegalArgumentException e){throw new IOException("Campaña electoral inválida",e);}
        }
    }
    public EstadoElectoral electoral(EstadoCiudad ciudad)throws IOException{return transaccion(false,p->leerElectoral(p,ciudad));}
    private static EstadoElectoral leerElectoral(Properties p,EstadoCiudad ciudad){
        var defecto=EstadoElectoral.inicial(ciudad);
        return new EstadoElectoral(Boolean.parseBoolean(p.getProperty("electoral.juan.corrupto","false")),numeros(p,"apoyo",defecto.apoyo()),numeros(p,"confianza",defecto.confianza()),numeros(p,"verificadas",defecto.verificadas()),numeros(p,"conflictos",defecto.conflictos()),numeros(p,"participacion",defecto.participacion()),
            p.getProperty("electoral.historial","").isEmpty()?List.of():Arrays.asList(p.getProperty("electoral.historial").split("\n")));
    }
    private static int[] numeros(Properties p,String clave,int[] defecto){String valor=p.getProperty("electoral."+clave);return valor==null?defecto:Arrays.stream(valor.split(",")).mapToInt(Integer::parseInt).toArray();}
    private static String unir(int[] a){return String.join(",",Arrays.stream(a).mapToObj(Integer::toString).toList());}
    private static EstadoCiudad ciudad(Properties p,EstadoCiudad defecto){String s=p.getProperty("ciudad");if(s==null)return defecto;int[] n=Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray();if(n.length!=7)throw new IllegalArgumentException("Ciudad inválida");return new EstadoCiudad(n[0],n[1],n[2],n[3],n[4],n[5],n[6]);}
    public void registrarCorrupcion(EstadoElectoral e)throws IOException{if(!e.juanCorrupto())return;transaccion(true,p->{p.setProperty("electoral.juan.corrupto","true");return null;});}
    /** Conserva resultados electorales y evita acreditar de nuevo un checkpoint completado. */
    public void guardarCiudad(EstadoCiudad c)throws IOException{transaccion(true,p->{p.setProperty("ciudad",unir(new int[]{c.informacionVerificada(),c.confianza(),c.convivencia(),c.bienestar(),c.participacion(),c.desinformacion(),c.conflictos()}));return null;});}
    public EstadoCiudad acreditarMisionCuatro(String partida,int validadas,int responsables,EstadoCiudad anterior)throws IOException{
        return transaccion(true,p->{
            String clave="mision4."+partida;
            if(p.containsKey(clave))return anterior;
            EstadoCiudad c=alcaldedigital.compartido.modelo.ProgresoCampana.aplicarMisionCuatro(anterior,validadas,responsables);
            p.setProperty(clave,validadas+","+responsables);
            p.setProperty("ciudad",unir(new int[]{c.informacionVerificada(),c.confianza(),c.convivencia(),c.bienestar(),c.participacion(),c.desinformacion(),c.conflictos()}));
            return c;
        });
    }
    public EstadoCiudad acreditarMisionCuatro(String partida,int validadas,int favorables,int desfavorables,EstadoCiudad anterior)throws IOException{
        return transaccion(true,p->{
            String clave="mision4."+partida;
            if(p.containsKey(clave))return anterior;
            EstadoCiudad c=alcaldedigital.compartido.modelo.ProgresoCampana.aplicarMisionCuatro(anterior,validadas,favorables,desfavorables);
            p.setProperty(clave,validadas+","+favorables+","+desfavorables);
            p.setProperty("ciudad",unir(new int[]{c.informacionVerificada(),c.confianza(),c.convivencia(),c.bienestar(),c.participacion(),c.desinformacion(),c.conflictos()}));
            return c;
        });
    }
    public EstadoCiudad acreditar(Vista v,EstadoCiudad anterior)throws IOException{
        if(v.fase()!=Fase.RESULTADO)return anterior;
        return transaccion(true,p->{
            String prefijo="sala.m7."+v.id();if(p.containsKey(prefijo))return ciudad(p,anterior);
            var e=v.electoral().conservarCorrupcion(Boolean.parseBoolean(p.getProperty("electoral.juan.corrupto","false")));
            p.setProperty("electoral.juan.corrupto",Boolean.toString(e.juanCorrupto()));p.setProperty("electoral.apoyo",unir(e.apoyo()));p.setProperty("electoral.confianza",unir(e.confianza()));p.setProperty("electoral.verificadas",unir(e.verificadas()));p.setProperty("electoral.conflictos",unir(e.conflictos()));p.setProperty("electoral.participacion",unir(e.participacion()));p.setProperty("electoral.historial",String.join("\n",e.historial()));
            p.setProperty("electoral.estado","pendiente_mision8");p.setProperty("electoral.lider.provisional",Integer.toString(e.liderProvisional()));
            p.setProperty(prefijo,Integer.toString(v.ganador()));p.setProperty(prefijo+".dificultad",v.dificultad().name());p.setProperty("ciudad",unir(v.ciudad()));
            for(int r=0;r<2;r++)if(v.humano(r)){p.setProperty(prefijo+".puntos."+r,Integer.toString(v.corredores()[r].puntos()));p.setProperty(prefijo+".reputacion."+r,Integer.toString(v.corredores()[r].reputacion()));int mejor=Integer.parseInt(p.getProperty("mejor.m7."+r,"0"));p.setProperty("mejor.m7."+r,Integer.toString(Math.max(mejor,v.corredores()[r].puntos())));}
            return ciudad(p,anterior);
        });
    }
}
