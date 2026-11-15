package alcaldedigital.compartido.guardado;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import alcaldedigital.compartido.modelo.EstadoCiudad;

/** Resultados de intentos. Solo una victoria puede convertirse en resultado de campaña. */
public final class TablaPuntajes {
    private TablaPuntajes() { }

    public record Fila(int mision, String partida, boolean victoria, int puntaje,
                       String dificultad, List<String> datos, List<String> decisiones,List<Integer> cambioCiudad,
                       Map<String,Integer> senales) {
        public Fila(int mision,String partida,boolean victoria,int puntaje,String dificultad,
                    List<String> datos,List<String> decisiones){
            this(mision,partida,victoria,puntaje,dificultad,datos,decisiones,List.of(0,0,0,0,0,0,0),Map.of());
        }
        public Fila(int mision,String partida,boolean victoria,int puntaje,String dificultad,
                    List<String> datos,List<String> decisiones,List<Integer> cambioCiudad){
            this(mision,partida,victoria,puntaje,dificultad,datos,decisiones,cambioCiudad,Map.of());
        }
        public Fila {
            if (mision < 1 || mision > 7 || partida == null || partida.isBlank())
                throw new IllegalArgumentException("Resultado de misión inválido");
            puntaje = Math.max(0, Math.min(100, puntaje));
            dificultad = dificultad == null ? "" : dificultad;
            datos = List.copyOf(datos);
            decisiones = List.copyOf(decisiones);
            cambioCiudad=List.copyOf(cambioCiudad);
            senales=Map.copyOf(senales);
            if(cambioCiudad.size()!=7)throw new IllegalArgumentException("Se requieren siete indicadores");
            if(senales.keySet().stream().anyMatch(k->!k.matches("[a-z_]{1,40}")))
                throw new IllegalArgumentException("Clave de antecedente inválida");
        }
    }

    public static Path archivo() { return ArchivosPartida.directorio().resolve("tabla-puntajes.properties"); }

    public static synchronized void registrar(Fila fila) throws IOException {
        Path ruta = archivo();
        Files.createDirectories(ruta.toAbsolutePath().getParent());
        try(var canal=FileChannel.open(ruta.resolveSibling("tabla-puntajes.lock"),
                StandardOpenOption.CREATE,StandardOpenOption.WRITE);var bloqueo=canal.lock()) {
            Properties p = ArchivosPartida.leer(ruta);
            if (p == null) p = new Properties();
            String prefijo = "mision." + fila.mision() + ".";
            if (p.containsKey("partida." + fila.partida())) return;
            p.setProperty("version", "1");
            p.setProperty("partida." + fila.partida(), Integer.toString(fila.mision()));
            int intento=Integer.parseInt(p.getProperty(prefijo + "intentos", "0"));
            p.setProperty(prefijo + "intentos", Integer.toString(intento+1));
            escribir(p, prefijo + "intento." + intento + ".", fila);
            escribir(p, prefijo + "ultimo.", fila);
            int mejor = Integer.parseInt(p.getProperty(prefijo + "mejor", "0"));
            p.setProperty(prefijo + "mejor", Integer.toString(Math.max(mejor, fila.puntaje())));
            if (fila.victoria()) escribir(p, prefijo + "campana.", fila);
            ArchivosPartida.guardar(ruta, p);
        } catch(RuntimeException e) { throw new IOException("No se pudo registrar el puntaje",e); }
    }

    private static void escribir(Properties p, String prefijo, Fila fila) {
        p.setProperty(prefijo + "partida", fila.partida());
        p.setProperty(prefijo + "victoria", Boolean.toString(fila.victoria()));
        p.setProperty(prefijo + "puntaje", Integer.toString(fila.puntaje()));
        p.setProperty(prefijo + "dificultad", fila.dificultad());
        p.setProperty(prefijo + "datos.cantidad", Integer.toString(fila.datos().size()));
        for (int i = 0; i < fila.datos().size(); i++) p.setProperty(prefijo + "dato." + i, fila.datos().get(i));
        p.setProperty(prefijo + "decisiones.cantidad", Integer.toString(fila.decisiones().size()));
        for (int i = 0; i < fila.decisiones().size(); i++) p.setProperty(prefijo + "decision." + i, fila.decisiones().get(i));
        for(int i=0;i<7;i++)p.setProperty(prefijo+"ciudad."+i,Integer.toString(fila.cambioCiudad().get(i)));
        p.setProperty(prefijo+"senales.cantidad",Integer.toString(fila.senales().size()));
        int s=0;
        for(var entrada:fila.senales().entrySet()){
            p.setProperty(prefijo+"senal."+s+".clave",entrada.getKey());
            p.setProperty(prefijo+"senal."+s+".valor",Integer.toString(entrada.getValue()));
            s++;
        }
    }

    public static synchronized Fila ultimo(int mision) throws IOException {
        return leer(mision, "ultimo.");
    }

    public static synchronized List<Fila> historial(int mision) throws IOException {
        if(mision<1||mision>7)throw new IllegalArgumentException("Misión inválida");
        Properties p=ArchivosPartida.leer(archivo());
        if(p==null)return List.of();
        int n;
        try{n=Integer.parseInt(p.getProperty("mision."+mision+".intentos","0"));}
        catch(NumberFormatException e){throw new IOException("Historial de puntajes inválido",e);}
        if(n<0||n>100000)throw new IOException("Historial de puntajes inválido");
        List<Fila> filas=new ArrayList<>();
        for(int i=0;i<n;i++)filas.add(leerFila(p,mision,"intento."+i+"."));
        return List.copyOf(filas);
    }

    public static synchronized Fila campana(int mision) throws IOException {
        return leer(mision, "campana.");
    }

    /** No inventa antecedentes para partidas antiguas: solo se ofrece tras siete victorias registradas. */
    public static synchronized java.util.Optional<EstadoCiudad> ciudadParaFinales()throws IOException{
        int[] n={50,50,50,50,50,20,20};
        for(int m=1;m<=7;m++){
            Fila fila=campana(m);if(fila==null||!fila.victoria())return java.util.Optional.empty();
            for(int i=0;i<7;i++)n[i]+=fila.cambioCiudad().get(i);
        }
        return java.util.Optional.of(new EstadoCiudad(n[0],n[1],n[2],n[3],n[4],n[5],n[6]));
    }

    private static Fila leer(int mision, String tipo) throws IOException {
        if (mision < 1 || mision > 7) throw new IllegalArgumentException("Misión inválida");
        Properties p = ArchivosPartida.leer(archivo());
        return p==null?null:leerFila(p,mision,tipo);
    }
    private static Fila leerFila(Properties p,int mision,String tipo)throws IOException{
        String prefijo = "mision." + mision + "." + tipo;
        if (p.getProperty(prefijo + "partida") == null) return null;
        try {
            List<String> datos = new ArrayList<>(), decisiones = new ArrayList<>();
            int nd = Integer.parseInt(p.getProperty(prefijo + "datos.cantidad", "0"));
            int ne = Integer.parseInt(p.getProperty(prefijo + "decisiones.cantidad", "0"));
            int ns = Integer.parseInt(p.getProperty(prefijo + "senales.cantidad", "0"));
            if (nd < 0 || nd > 64 || ne < 0 || ne > 128 || ns<0 || ns>32) throw new IllegalArgumentException("Tamaño inválido");
            for (int i = 0; i < nd; i++) datos.add(p.getProperty(prefijo + "dato." + i, ""));
            for (int i = 0; i < ne; i++) decisiones.add(p.getProperty(prefijo + "decision." + i, ""));
            List<Integer> cambios=new ArrayList<>();
            for(int i=0;i<7;i++)cambios.add(Integer.parseInt(p.getProperty(prefijo+"ciudad."+i,"0")));
            Map<String,Integer> senales=new LinkedHashMap<>();
            for(int i=0;i<ns;i++)senales.put(p.getProperty(prefijo+"senal."+i+".clave"),
                Integer.parseInt(p.getProperty(prefijo+"senal."+i+".valor")));
            return new Fila(mision, p.getProperty(prefijo + "partida"),
                    Boolean.parseBoolean(p.getProperty(prefijo + "victoria")),
                    Integer.parseInt(p.getProperty(prefijo + "puntaje")),
                    p.getProperty(prefijo + "dificultad", ""), datos, decisiones,cambios,senales);
        } catch (RuntimeException e) { throw new IOException("Tabla de puntajes inválida", e); }
    }
}
