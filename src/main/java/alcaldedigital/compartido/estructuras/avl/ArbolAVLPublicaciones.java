package alcaldedigital.compartido.estructuras.avl;

import alcaldedigital.compartido.modelo.*;
import java.util.*;

/** AVL propio. FE = altura derecha - izquierda; null=0, hoja=1. */
public final class ArbolAVLPublicaciones {
    private static final class Nodo {
        Publicacion valor; Nodo izq, der; int altura = 1;
        Nodo(Publicacion valor) { this.valor = valor; }
    }
    public record Vista(Publicacion valor, int altura, int fe, Vista izquierdo, Vista derecho) { }
    public record Paso(String operacion, Vista raiz) { }
    private Nodo raiz;
    private final Map<Long, ClavePublicacion> claves = new HashMap<>();
    private final List<Paso> pasos = new ArrayList<>();
    private final List<String> giros = new ArrayList<>();
    public int cantidad() { return claves.size(); }
    public int altura() { return altura(raiz); }
    public List<Paso> pasos() { return List.copyOf(pasos); }
    public Vista vista() { return vista(raiz); }
    private Vista vista(Nodo n) { return n == null ? null : new Vista(n.valor, n.altura, fe(n), vista(n.izq), vista(n.der)); }
    public Publicacion buscar(long id) { return buscar(claves.get(id)); }
    public Publicacion buscar(ClavePublicacion clave) {
        Nodo n = raiz;
        while (n != null && clave != null) {
            int c = clave.compareTo(n.valor.clave());
            if (c == 0) return n.valor;
            n = c < 0 ? n.izq : n.der;
        }
        return null;
    }
    public void insertar(Publicacion p) {
        Objects.requireNonNull(p);
        if (claves.containsKey(p.id())) throw new IllegalArgumentException("ID duplicado: " + p.id());
        giros.clear(); raiz = insertar(raiz, p); claves.put(p.id(), p.clave());
        registrar("Insertar #" + p.id() + " · riesgo " + p.riesgo());
    }
    private Nodo insertar(Nodo n, Publicacion p) {
        if (n == null) return new Nodo(p);
        if (p.clave().compareTo(n.valor.clave()) < 0) n.izq = insertar(n.izq, p);
        else n.der = insertar(n.der, p);
        return balancear(n);
    }
    public boolean eliminar(long id) {
        ClavePublicacion clave = claves.remove(id);
        if (clave == null) return false;
        giros.clear(); raiz = eliminar(raiz, clave); registrar("Eliminar del índice #" + id); return true;
    }
    private Nodo eliminar(Nodo n, ClavePublicacion clave) {
        if (n == null) return null;
        int c = clave.compareTo(n.valor.clave());
        if (c < 0) n.izq = eliminar(n.izq, clave);
        else if (c > 0) n.der = eliminar(n.der, clave);
        else {
            if (n.izq == null) return n.der;
            if (n.der == null) return n.izq;
            Nodo sucesor = n.der;
            while (sucesor.izq != null) sucesor = sucesor.izq;
            n.valor = sucesor.valor; // Trasladar la publicación completa, no solo su clave.
            n.der = eliminar(n.der, sucesor.valor.clave());
        }
        return balancear(n);
    }
    public void actualizar(Publicacion p) {
        if (!claves.containsKey(p.id())) throw new IllegalArgumentException("Publicación ausente");
        eliminar(p.id()); insertar(p);
    }
    private int altura(Nodo n) { return n == null ? 0 : n.altura; }
    private int fe(Nodo n) { return altura(n.der) - altura(n.izq); }
    private void recalcular(Nodo n) { n.altura = 1 + Math.max(altura(n.izq), altura(n.der)); }
    private Nodo balancear(Nodo n) {
        recalcular(n);
        if (fe(n) < -1) {
            if (fe(n.izq) > 0) n.izq = izquierda(n.izq);
            return derecha(n);
        }
        if (fe(n) > 1) {
            if (fe(n.der) < 0) n.der = derecha(n.der);
            return izquierda(n);
        }
        return n;
    }
    private Nodo derecha(Nodo n) {
        giros.add("rotación derecha en #" + n.valor.id());
        Nodo nuevo = n.izq; n.izq = nuevo.der; nuevo.der = n;
        recalcular(n); recalcular(nuevo); return nuevo;
    }
    private Nodo izquierda(Nodo n) {
        giros.add("rotación izquierda en #" + n.valor.id());
        Nodo nuevo = n.der; n.der = nuevo.izq; nuevo.izq = n;
        recalcular(n); recalcular(nuevo); return nuevo;
    }
    private void registrar(String texto) {
        pasos.add(new Paso(texto + (giros.isEmpty() ? "" : " · " + String.join("; ", giros)), vista()));
    }
    public List<Publicacion> recorrer(String orden) {
        if (!Set.of("inorden", "preorden", "postorden", "inverso").contains(orden)) throw new IllegalArgumentException("Recorrido desconocido");
        List<Publicacion> salida = new ArrayList<>(); recorrer(raiz, orden, salida); return List.copyOf(salida);
    }
    private void recorrer(Nodo n, String orden, List<Publicacion> salida) {
        if (n == null) return;
        if (orden.equals("preorden")) salida.add(n.valor);
        recorrer(orden.equals("inverso") ? n.der : n.izq, orden, salida);
        if (orden.equals("inorden") || orden.equals("inverso")) salida.add(n.valor);
        recorrer(orden.equals("inverso") ? n.izq : n.der, orden, salida);
        if (orden.equals("postorden")) salida.add(n.valor);
    }
}
