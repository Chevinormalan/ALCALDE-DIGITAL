package alcaldedigital.compartido.estructuras.decisiones;

import java.util.*;

/** N-ario con IDs únicos; cada inserción crea un nodo nuevo con un solo padre. */
public final class ArbolDecisiones {
    public record Vista(String id, String texto, boolean ejecutado, List<Vista> hijos) { }
    public record Paso(String operacion, Vista raiz) { }
    private static final class Nodo {
        final String id, texto; final List<Nodo> hijos = new ArrayList<>(); boolean ejecutado;
        Nodo(String id, String texto) { this.id = id; this.texto = texto; }
    }
    private final Nodo raiz;
    private final List<Paso> pasos = new ArrayList<>();
    public ArbolDecisiones(String id, String texto) { raiz = new Nodo(Objects.requireNonNull(id), Objects.requireNonNull(texto)); registrar("Crear evento"); }
    public Vista vista() { return vista(raiz); }
    private Vista vista(Nodo n) { return new Vista(n.id, n.texto, n.ejecutado, n.hijos.stream().map(this::vista).toList()); }
    public List<Paso> pasos() { return List.copyOf(pasos); }
    private Nodo buscar(Nodo n, String id) {
        if (n.id.equals(id)) return n;
        for (Nodo h : n.hijos) { Nodo r = buscar(h, id); if (r != null) return r; }
        return null;
    }
    public Vista buscar(String id) { Nodo n = buscar(raiz, id); return n == null ? null : vista(n); }
    public void insertar(String padre, String id, String texto) {
        Objects.requireNonNull(id); Objects.requireNonNull(texto);
        if (buscar(id) != null) throw new IllegalArgumentException("ID duplicado/ciclo: " + id);
        Nodo p = buscar(raiz, padre);
        if (p == null) throw new IllegalArgumentException("Padre ausente");
        p.hijos.add(new Nodo(id, texto)); registrar("Insertar opción: " + texto);
    }
    public boolean podar(String id) {
        Nodo n = buscar(raiz, id);
        if (n == null) return false;
        if (n == raiz || tieneEjecutados(n)) throw new IllegalArgumentException("No se borra la raíz ni la ruta ejecutada");
        boolean resultado = quitar(raiz, id); registrar("Podar opción: " + n.texto); return resultado;
    }
    private boolean tieneEjecutados(Nodo n) { return n.ejecutado || n.hijos.stream().anyMatch(this::tieneEjecutados); }
    private boolean quitar(Nodo n, String id) {
        if (n.hijos.removeIf(h -> h.id.equals(id))) return true;
        for (Nodo h : n.hijos) if (quitar(h, id)) return true;
        return false;
    }
    public List<String> ruta(String id) {
        List<String> salida = new ArrayList<>(); return ruta(raiz, id, salida) ? List.copyOf(salida) : List.of();
    }
    private boolean ruta(Nodo n, String id, List<String> salida) {
        salida.add(n.id);
        if (n.id.equals(id)) return true;
        for (Nodo h : n.hijos) if (ruta(h, id, salida)) return true;
        salida.remove(salida.size() - 1); return false;
    }
    public void ejecutar(String id) {
        List<String> camino = ruta(id);
        if (camino.isEmpty()) throw new IllegalArgumentException("Opción ausente");
        camino.forEach(p -> buscar(raiz, p).ejecutado = true); registrar("Recorrer ruta: " + String.join(" → ", camino));
    }
    public List<String> recorrer(String orden) {
        List<String> salida = new ArrayList<>();
        if (orden.equals("niveles")) {
            Queue<Nodo> q = new ArrayDeque<>(); q.add(raiz);
            while (!q.isEmpty()) { Nodo n = q.remove(); salida.add(n.id); q.addAll(n.hijos); }
        } else if (orden.equals("preorden") || orden.equals("postorden")) recorrido(raiz, orden, salida);
        else throw new IllegalArgumentException("Recorrido n-ario inválido");
        return List.copyOf(salida);
    }
    private void recorrido(Nodo n, String orden, List<String> salida) {
        if (orden.equals("preorden")) salida.add(n.id);
        for (Nodo h : n.hijos) recorrido(h, orden, salida);
        if (orden.equals("postorden")) salida.add(n.id);
    }
    private void registrar(String operacion) { pasos.add(new Paso(operacion, vista())); }
}
