package alcaldedigital.cliente.visualizadores;

import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.compartido.modelo.Publicacion;
import alcaldedigital.compartido.protocolo.SesionJuego;
import java.util.*;

/** Proyección de lectura: relaciona operaciones existentes con acciones y recorre sus snapshots. */
public final class ConsultaEstructuras {
    public record Evento(int numero, String causa, int version, int avlDesde, int avlHasta,
                         int decisionesDesde, int decisionesHasta, String radar, String camino) {
        public String titulo() { return "Evento %02d · %s".formatted(numero, causa); }
        public String resumen() { return String.join(" ", List.of(radar,camino).stream().filter(s->!s.isBlank()).toList()); }
    }
    private final List<Evento> eventos=new ArrayList<>();
    private SesionJuego.Vista anterior;
    private int cantidadAVL, cantidadDecisiones;

    public List<Evento> eventos() { return List.copyOf(eventos); }
    public Evento ultimo() { return eventos.isEmpty()?null:eventos.get(eventos.size()-1); }
    public void iniciar(SesionJuego sesion) {
        eventos.clear(); anterior=null; cantidadAVL=0; cantidadDecisiones=0;
        registrar(sesion,"Inicio del caso");
    }
    public boolean registrar(SesionJuego sesion,String causa) {
        var actual=sesion.vista();
        int avl=sesion.pasosAVL().size(), nar=sesion.pasosDecisiones().size();
        if(anterior!=null&&actual.version()==anterior.version()&&avl==cantidadAVL&&nar==cantidadDecisiones) return false;
        String radar="",camino="";
        if(anterior==null) {
            radar="El radar organiza tres publicaciones por prioridad. Prioridad no significa veracidad.";
            camino="Hay cuatro decisiones disponibles. Tú eliges qué hacer en Civitas.";
        } else {
            List<String> cambios=new ArrayList<>();
            for(Publicacion p:anterior.publicaciones()) {
                var nueva=actual.publicaciones().stream().filter(q->q.id()==p.id()).findFirst();
                if(nueva.isEmpty()) cambios.add("La publicación #"+p.id()+" salió del radar; su historia se conserva.");
                else if(nueva.get().riesgo()!=p.riesgo()) cambios.add("Prioridad de #"+p.id()+": "+p.riesgo()+" → "+nueva.get().riesgo()+".");
            }
            for(Publicacion p:actual.publicaciones()) if(anterior.publicaciones().stream().noneMatch(q->q.id()==p.id()))
                cambios.add("Entró #"+p.id()+" al radar con prioridad "+p.riesgo()+".");
            radar=String.join(" ",cambios);
            List<String> opciones=new ArrayList<>();
            for(String id:decisiones(actual.decisiones(),"preorden")) {
                var n=buscar(actual.decisiones(),id);var antes=buscar(anterior.decisiones(),id);
                if(antes==null) opciones.add(n.texto()+" está disponible.");
            }
            if(!opciones.isEmpty()) camino="Conseguiste ambas fuentes: "+String.join(" ",opciones);
            else if(actual.terminado()&&!anterior.terminado()) camino="El caso cerró. Tu camino se conserva y se retiran las alternativas pendientes.";
            else if(nar!=cantidadDecisiones) camino="Tu camino registró la decisión: "+causa.toLowerCase(Locale.ROOT)+".";
        }
        // También se conserva la obtención de la primera fuente aunque no cambie un árbol.
        eventos.add(new Evento(eventos.size()+1,causa,actual.version(),cantidadAVL,avl,cantidadDecisiones,nar,radar,camino));
        anterior=actual;cantidadAVL=avl;cantidadDecisiones=nar;
        return !radar.isBlank()||!camino.isBlank();
    }
    public Evento eventoDelPaso(boolean avl,int paso) {
        return eventos.stream().filter(e->paso>=(avl?e.avlDesde():e.decisionesDesde())
            &&paso<(avl?e.avlHasta():e.decisionesHasta())).findFirst().orElse(null);
    }
    public Evento ultimoCambio(boolean avl) {
        for(int i=eventos.size()-1;i>=0;i--) if(!(avl?eventos.get(i).radar():eventos.get(i).camino()).isBlank()) return eventos.get(i);
        return null;
    }
    public static List<Publicacion> publicaciones(ArbolAVLPublicaciones.Vista raiz,String orden) {
        List<Publicacion> salida=new ArrayList<>();recorrer(raiz,orden,salida);return List.copyOf(salida);
    }
    private static void recorrer(ArbolAVLPublicaciones.Vista n,String orden,List<Publicacion> salida) {
        if(n==null) return;
        if(orden.equals("preorden")) salida.add(n.valor());
        recorrer(orden.equals("inverso")?n.derecho():n.izquierdo(),orden,salida);
        if(orden.equals("inorden")||orden.equals("inverso")) salida.add(n.valor());
        recorrer(orden.equals("inverso")?n.izquierdo():n.derecho(),orden,salida);
        if(orden.equals("postorden")) salida.add(n.valor());
    }
    public static List<String> decisiones(ArbolDecisiones.Vista raiz,String orden) {
        if(raiz==null) return List.of();
        List<String> salida=new ArrayList<>();
        if(orden.equals("niveles")) {
            Queue<ArbolDecisiones.Vista> cola=new ArrayDeque<>();cola.add(raiz);
            while(!cola.isEmpty()) {var n=cola.remove();salida.add(n.id());cola.addAll(n.hijos());}
        } else recorrer(raiz,orden,salida);
        return List.copyOf(salida);
    }
    private static void recorrer(ArbolDecisiones.Vista n,String orden,List<String> salida) {
        if(orden.equals("preorden")) salida.add(n.id());
        n.hijos().forEach(h->recorrer(h,orden,salida));
        if(orden.equals("postorden")) salida.add(n.id());
    }
    public static ArbolDecisiones.Vista buscar(ArbolDecisiones.Vista n,String id) {
        if(n==null||n.id().equals(id)) return n;
        for(var hijo:n.hijos()) {var r=buscar(hijo,id);if(r!=null) return r;}
        return null;
    }
}
