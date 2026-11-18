package alcaldedigital.cliente.misiones;

import alcaldedigital.compartido.batalla.EstadoBatalla;
import alcaldedigital.compartido.carrera.EstadoCarrera;
import alcaldedigital.compartido.guardado.ArchivosPartida;
import alcaldedigital.compartido.guardado.GuardadoCampana;
import alcaldedigital.compartido.guardado.ProgresoMisiones;
import alcaldedigital.servidor.batalla.GuardadoBatalla;
import alcaldedigital.servidor.carrera.GuardadoCarrera;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/** Resume guardados reales para el menú sin tratar un checkpoint como victoria. */
public final class PartidasMenu {
    private PartidasMenu() { }
    public enum Estado { EN_CURSO, VICTORIA, DERROTA }
    public record Partida(int mision, Estado estado, int modo, int rol, long modificada, boolean memoria) { }

    public static Partida reciente(ProgresoMisiones progreso) {
        return reciente(progreso,0);
    }

    /** El cero selecciona la partida más reciente de toda la campaña. */
    public static Partida reciente(ProgresoMisiones progreso,int mision) {
        List<Partida> partidas=new ArrayList<>();
        for(int n:new int[]{1,2,3,4,6,8})if(progreso.disponible(n)){
            try{
                Properties p=GuardadoCampana.cargar(n);
                if(p!=null)partidas.add(new Partida(n,estadoPropiedades(n,p,progreso),-1,-1,
                    modificacion(GuardadoCampana.archivo(n)),false));
            }catch(IOException|RuntimeException ignorado){/* El selector mostrará el error si se elige ese archivo. */}
        }
        if(progreso.disponible(5)){
            agregarBatalla(partidas,0,"mision5-solo.bin",progreso);
            agregarBatalla(partidas,1,"mision5-coop.bin",progreso);
            agregarBatalla(partidas,2,"mision5-lan.bin",progreso);
        }
        if(progreso.disponible(7)){
            agregarCarrera(partidas,0,0,"mision7-solo-0.bin",progreso);
            agregarCarrera(partidas,0,1,"mision7-solo-1.bin",progreso);
            agregarCarrera(partidas,1,0,"mision7-dos.bin",progreso);
            agregarCarrera(partidas,2,0,"mision7-lan.bin",progreso);
        }
        return partidas.stream().filter(p->mision==0||p.mision()==mision)
            .max(Comparator.comparingLong(Partida::modificada)
            .thenComparingInt(Partida::mision)).orElse(null);
    }

    private static Estado estadoPropiedades(int n,Properties p,ProgresoMisiones progreso){
        boolean completo=switch(n){
            case 8->"FINAL".equals(p.getProperty("escena"))||"DERROTA".equals(p.getProperty("escena"));
            case 1->"RESULTADO".equals(p.getProperty("sesion.fase"));
            case 2->"RESULTADO".equals(p.getProperty("estado.fase"));
            case 3->Boolean.parseBoolean(p.getProperty("completada"));
            case 4->"COMPLETE".equals(p.getProperty("phase"));
            case 6->"DERROTA".equals(p.getProperty("fase"))||
                "FINAL".equals(p.getProperty("fase"))&&progreso.ganada(6);
            default->false;
        };
        if(!completo)return Estado.EN_CURSO;
        boolean gano=switch(n){
            case 1->"BUENO".equals(p.getProperty("sesion.desenlace"));
            case 2->Boolean.parseBoolean(p.getProperty("estado.exito"))&&
                !Boolean.parseBoolean(p.getProperty("estado.falloCriticoActivado"));
            case 8->"FINAL".equals(p.getProperty("escena"));
            case 3,4,6->!"DERROTA".equals(p.getProperty("fase"));
            default->false;
        };
        return gano&&!progreso.ganada(n)?Estado.EN_CURSO:gano?Estado.VICTORIA:Estado.DERROTA;
    }

    private static void agregarBatalla(List<Partida> partidas,int modo,String nombre,ProgresoMisiones progreso){
        Path ruta=ArchivosPartida.directorio().resolve(nombre);
        if(!Files.isRegularFile(ruta))return;
        try{
            var vista=new GuardadoBatalla(ruta).cargar().vista();
            Estado estado=vista.fase()!=EstadoBatalla.Fase.FINAL?Estado.EN_CURSO:
                vista.gano()?(progreso.ganada(5)?Estado.VICTORIA:Estado.EN_CURSO):Estado.DERROTA;
            partidas.add(new Partida(5,estado,modo,0,modificacion(ruta),false));
        }catch(IOException|RuntimeException ignorado){ }
    }

    private static void agregarCarrera(List<Partida> partidas,int modo,int rol,String nombre,ProgresoMisiones progreso){
        Path ruta=ArchivosPartida.directorio().resolve(nombre);
        if(!Files.isRegularFile(ruta))return;
        try{
            var vista=new GuardadoCarrera(ruta).cargar().vista();
            boolean gano=vista.ganador()>=0&&vista.ganador()<2&&vista.humano(vista.ganador());
            Estado estado=vista.fase()!=EstadoCarrera.Fase.RESULTADO?Estado.EN_CURSO:
                gano?(progreso.ganada(7)?Estado.VICTORIA:Estado.EN_CURSO):Estado.DERROTA;
            partidas.add(new Partida(7,estado,modo,rol,modificacion(ruta),false));
        }catch(IOException|RuntimeException ignorado){ }
    }

    private static long modificacion(Path ruta){
        try{
            Path respaldo=ruta.resolveSibling(ruta.getFileName()+".bak");
            long principal=Files.isRegularFile(ruta)?Files.getLastModifiedTime(ruta).toMillis():0;
            long anterior=Files.isRegularFile(respaldo)?Files.getLastModifiedTime(respaldo).toMillis():0;
            return Math.max(principal,anterior);
        }catch(IOException e){return 0;}
    }
}
