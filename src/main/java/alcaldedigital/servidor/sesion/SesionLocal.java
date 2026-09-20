package alcaldedigital.servidor.sesion;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.compartido.estructuras.avl.*;
import alcaldedigital.compartido.estructuras.decisiones.*;
import alcaldedigital.servidor.eventos.EventoParque;
import alcaldedigital.compartido.protocolo.*;
import alcaldedigital.servidor.reglas.ReglasI1;
import java.util.*;

/** Autoridad de la entrega 1: posición, reloj, permisos, efectos e idempotencia. */
public final class SesionLocal implements SesionJuego {
    private final Rol rol;
    private final EventoParque.Variante variante;
    private final ArbolAVLPublicaciones avl=new ArbolAVLPublicaciones();
    private final ArbolDecisiones decisiones=new ArbolDecisiones("parque","¿Cerrarán el parque?");
    private final List<Evidencia> evidencias=new ArrayList<>();
    private final List<String> historial=new ArrayList<>();
    private final Map<String,ResultadoAccion> peticiones=new HashMap<>();
    private final Map<String,RegistroAccion> registros=new LinkedHashMap<>();
    private final ConfiguracionSesion configuracion;
    private final Set<String> realizadas=new HashSet<>();
    private final Map<Long,Publicacion> archivo=new LinkedHashMap<>();
    private final MapaParque mapa=new MapaParque();
    private EstadoCiudad ciudad=EstadoCiudad.inicial();
    private int puntos,version;
    private float x=320,y=368;
    private boolean terminado,verificando;
    private double transcurrido;
    private final int plazo;
    private String mensaje="Civitas tiene un rumor nuevo. Pulsa C para leerlo.";
    public SesionLocal(Rol rol,long semilla,int plazo) { this(new ConfiguracionSesion(rol,semilla,plazo)); }
    public SesionLocal(Rol rol,int indiceVariante,int plazo) {
        this(new ConfiguracionSesion(rol,0L,plazo,indiceVariante));
    }
    public SesionLocal(ConfiguracionSesion configuracion) {
        this.configuracion=Objects.requireNonNull(configuracion);
        this.rol=configuracion.rol();
        this.plazo=configuracion.plazo();
        EventoParque evento=EventoParque.cargar(); variante=evento.variantes().get(configuracion.indiceVariante());
        agregar(new Publicacion(1,30,"Kevin Viral",evento.rumor(),"Sin comprobar"));
        agregar(new Publicacion(2,20,"Cleta comenta","Dicen que las palomas tendrán parqueadero privado.","Opinión / rumor"));
        agregar(new Publicacion(3,10,"Agenda del parque","Hay campaña en el pabellón. Entrada libre.","Agenda pública"));
        for(String opcion:List.of("compartir","verificar","reportar","ignorar"))
            decisiones.insertar("parque",opcion,Character.toUpperCase(opcion.charAt(0))+opcion.substring(1));
        historial.add("Inicio: insertar 30,20,10 provoca rotación derecha.");
    }
    private void agregar(Publicacion p) { avl.insertar(p); archivo.put(p.id(),p); }
    public ArbolAVLPublicaciones arbolAVL() { return avl; }
    public ArbolDecisiones arbolDecisiones() { return decisiones; }
    @Override public List<ArbolAVLPublicaciones.Paso> pasosAVL() { return avl.pasos(); }
    @Override public List<ArbolDecisiones.Paso> pasosDecisiones() { return decisiones.pasos(); }
    @Override public List<Publicacion> recorridoAVL(String orden) { return avl.recorrer(orden); }
    @Override public List<String> recorridoDecisiones(String orden) { return decisiones.recorrer(orden); }
    @Override public Publicacion buscarPublicacion(long id) { return avl.buscar(id); }
    @Override public ArbolDecisiones.Vista buscarDecision(String id) { return decisiones.buscar(id); }
    public MapaParque mapa() { return mapa; }
    public Map<Long,Publicacion> archivo() { return Map.copyOf(archivo); }
    public double restante() { return Math.max(0,plazo-transcurrido); }
    public int plazo() { return plazo; }
    public ConfiguracionSesion configuracion() { return configuracion; }
    public List<Evidencia> evidenciasObtenidas() { return List.copyOf(evidencias); }
    public List<RegistroAccion> registrosAcciones() { return List.copyOf(registros.values()); }
    public RegistroAccion obtenerRegistro(String requestId) { return registros.get(requestId); }
    public boolean cerca(String punto) {
        float px=punto.equals("aviso")?176:punto.equals("fuente")?230:455;
        float py=punto.equals("aviso")?150:punto.equals("fuente")?228:155;
        return Math.hypot(x-px,y-py)<47;
    }
    public String objetivo() {
        if(terminado) return "Caso cerrado · consulta tu balance";
        if(!verificando) return "Abre Civitas [C] y decide qué hacer";
        if(evidencias.isEmpty()) return "Busca el aviso municipal al noroeste [E]";
        if(evidencias.size()==1) return "Contrasta con la administradora [E]";
        return "Publica contexto en Civitas [C]";
    }
    @Override public Vista vista() {
        return new Vista(rol,ciudad,puntos,version,objetivo(),mensaje,evidencias.stream().map(Evidencia::contenido).toList(),List.copyOf(historial),
            terminado,avl.recorrer("inverso"),avl.vista(),decisiones.vista(),x,y,verificando);
    }
    @Override public String solicitar(String requestId,String accion) {
        return solicitarDetallado(requestId,accion).mensaje();
    }
    /** Extensión local compatible: la interfaz SesionJuego conserva su contrato de texto. */
    public ResultadoAccion solicitarDetallado(String requestId,String accion) {
        if(requestId==null||requestId.isBlank()||accion==null) throw new IllegalArgumentException("Petición inválida");
        if(peticiones.containsKey(requestId)) return peticiones.get(requestId);
        int versionAntes=version, puntosAntes=puntos;
        EstadoCiudad ciudadAntes=ciudad;
        String texto=resolver(accion);
        // Todas las acciones ejecutadas del capítulo incrementan la versión, incluso las perjudiciales.
        ResultadoAccion resultado=new ResultadoAccion(version!=versionAntes,texto,version);
        peticiones.put(requestId,resultado);
        registros.put(requestId,new RegistroAccion(requestId,accion,resultado,versionAntes,
            puntosAntes,puntos,ciudadAntes,ciudad));
        mensaje=texto;
        return resultado;
    }
    private String resolver(String accion) {
        if(!Set.of("aviso","fuente","rol","compartir","verificar","reportar","ignorar","contexto","guardar","rectificar").contains(accion)) return "Acción desconocida.";
        if(terminado) return "El caso terminó. Revisa el balance o inicia otra partida.";
        if(realizadas.contains(accion)) return "Esta acción ya fue registrada. No duplica puntos.";
        if(accion.equals("aviso")||accion.equals("fuente")) {
            if(!cerca(accion)) return "Acércate a la fuente para interactuar.";
            if(!verificando) return "Primero selecciona Verificar en Civitas [C].";
            if(accion.equals("fuente")&&!realizadas.contains("aviso")) return "Lee primero el aviso municipal.";
            evidencias.add(accion.equals("aviso")
                ? new Evidencia("ev-aviso","Aviso municipal",variante.aviso())
                : new Evidencia("ev-testimonio","Administradora del parque",variante.testimonio()));
            realizadas.add(accion); version++; puntos+=ReglasI1.PUNTOS_EVIDENCIA; historial.add("Evidencia: "+accion+" · +5 puntos");
            if(evidencias.size()==2) {
                decisiones.insertar("verificar","contexto","Publicar contexto");
                decisiones.insertar("verificar","guardar","Guardar evidencia");
                if(realizadas.contains("compartir")) decisiones.insertar("verificar","rectificar","Rectificar");
                reindexar(8,"Fuentes contrastadas");
            }
            return evidencias.get(evidencias.size()-1).contenido();
        }
        if(accion.equals("rol")) {
            if(!cerca("candidato")) return "Acércate a la campaña del candidato.";
            realizadas.add(accion); puntos+=ReglasI1.PUNTOS_ACCION_ROL; version++;
            String texto=switch(rol) {
                case CIUDADANO -> "Escuchaste sin insultar. Un vecino acepta buscar fuentes contigo.";
                case PERIODISTA -> "Grabaste la declaración completa: revisemos el acta antes de publicar.";
                case INFLUENCER -> "Transmitiste el discurso completo con el candidato presente.";
                case CANDIDATO -> "Admitiste que necesitas consultar el acta. Tu campaña gana coherencia.";
            };
            historial.add(texto+" · +3 puntos"); return texto;
        }
        if(decisiones.buscar(accion)==null) return "Acción bloqueada: consigue primero las evidencias.";
        realizadas.add(accion); decisiones.ejecutar(accion);
        switch(accion) {
            case "verificar" -> { verificando=true; version++; historial.add("Verificar: búsqueda presencial."); return "Busca el aviso municipal y después contrasta con la administradora."; }
            case "compartir" -> {
                reindexar(75,"Compartida sin verificar"); aplicar(ReglasI1.COMPARTIR);
                historial.add("Compartir: riesgo 30→75; desinformación +10; puntos -6.");
                return "El rumor ganó alcance. Todavía puedes verificar y rectificar.";
            }
            case "reportar" -> {
                boolean valido=evidencias.size()==2;
                if(valido) { avl.eliminar(1); aplicar(ReglasI1.REPORTAR_CON_PRUEBAS); }
                else aplicar(ReglasI1.REPORTAR_SIN_PRUEBAS);
                terminado=true; cerrarRamas();
                historial.add(valido?"Reporte validado: retirar del AVL; preservar archivo.":"Reporte sin pruebas: el mensaje permanece activo.");
                return valido?"Reporte validado. El rumor sale del índice activo.":"Reporte sin pruebas: no demuestra falsedad ni elimina automáticamente.";
            }
            case "ignorar" -> {
                aplicar(ReglasI1.IGNORAR); terminado=true; cerrarRamas();
                historial.add("Ignorar: tú no retransmites; Kevin sigue difundiendo.");
                return "No compartiste. Kevin continúa: el silencio no resuelve todo.";
            }
            case "contexto", "rectificar" -> {
                boolean rectificacion=realizadas.contains("compartir");
                avl.eliminar(1); agregar(new Publicacion(4,12,rectificacion?"Tu rectificación":"Fuentes del parque",variante.conclusion(),"Contextualizada"));
                if(rectificacion) aplicar(ReglasI1.RECTIFICAR); else aplicar(ReglasI1.PUBLICAR_CONTEXTO);
                terminado=true; cerrarRamas();
                historial.add(rectificacion?"Rectificación: recuperación parcial; original en historial.":"Contexto: retirar rumor, insertar corrección. +15 puntos.");
                return variante.conclusion()+(rectificacion?" Rectificar recupera parte de la confianza.":"");
            }
            case "guardar" -> {
                aplicar(ReglasI1.GUARDAR_EVIDENCIA); terminado=true; cerrarRamas();
                historial.add("Guardar: conocimiento personal, sin corrección pública.");
                return "Conservaste las pruebas. La ciudad aún necesita el contexto.";
            }
            default -> throw new IllegalArgumentException("Acción no ejecutable");
        }
    }
    private void cerrarRamas() {
        for(String id:List.of("compartir","reportar","ignorar","contexto","guardar","rectificar")) {
            var nodo=decisiones.buscar(id);
            if(nodo!=null&&!nodo.ejecutado()) decisiones.podar(id);
        }
    }
    private void reindexar(int riesgo,String estado) {
        Publicacion p=avl.buscar(1);
        if(p!=null) { Publicacion nueva=p.conRiesgo(riesgo,estado); avl.actualizar(nueva); archivo.put(1L,nueva); }
    }
    private void aplicar(ReglasI1.Cambio cambio) {
        ciudad=ReglasI1.aplicar(ciudad,cambio);
        puntos+=cambio.puntos(); version++;
    }
    @Override public void mover(float dx,float dy) {
        if(terminado||!Float.isFinite(dx)||!Float.isFinite(dy)) return;
        float largo=(float)Math.hypot(dx,dy);
        if(largo>4) { dx*=4/largo; dy*=4/largo; }
        if(mapa.transitable(x+dx,y)) x+=dx;
        if(mapa.transitable(x,y+dy)) y+=dy;
    }
    @Override public void actualizar(double segundos) {
        if(!Double.isFinite(segundos)||segundos<0) throw new IllegalArgumentException("Tiempo inválido");
        if(terminado||verificando) return;
        transcurrido+=segundos;
        if(transcurrido>=plazo) solicitar("plazo-agotado","ignorar");
    }
}
