package alcaldedigital.i1;

import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.compartido.modelo.ClavePublicacion;
import java.util.HashSet;
import java.util.Set;

/** Comprueba las invariantes del AVL y del árbol de decisiones. */
public final class ValidadorI1 {
    private ValidadorI1() { }

    public record Resultado(boolean correcto, String mensaje, int nodos, int altura) { }

    public static Resultado validarAVL(ArbolAVLPublicaciones.Vista raiz) {
        Set<Long> ids = new HashSet<>();
        try {
            int altura = validarNodoAVL(raiz, null, null, ids);
            return new Resultado(true, "AVL correcto", ids.size(), altura);
        } catch (IllegalStateException e) {
            return new Resultado(false, e.getMessage(), ids.size(), raiz == null ? 0 : raiz.altura());
        }
    }

    private static int validarNodoAVL(
            ArbolAVLPublicaciones.Vista nodo,
            ClavePublicacion minima,
            ClavePublicacion maxima,
            Set<Long> ids) {
        if (nodo == null) return 0;

        ClavePublicacion clave = nodo.valor().clave();
        if (minima != null && clave.compareTo(minima) <= 0) {
            throw new IllegalStateException("orden BST inválido");
        }
        if (maxima != null && clave.compareTo(maxima) >= 0) {
            throw new IllegalStateException("orden BST inválido");
        }
        if (!ids.add(nodo.valor().id())) {
            throw new IllegalStateException("ID repetido en AVL");
        }

        int izquierda = validarNodoAVL(nodo.izquierdo(), minima, clave, ids);
        int derecha = validarNodoAVL(nodo.derecho(), clave, maxima, ids);
        int alturaEsperada = 1 + Math.max(izquierda, derecha);
        int feEsperado = derecha - izquierda;

        if (nodo.altura() != alturaEsperada) throw new IllegalStateException("altura incorrecta");
        if (nodo.fe() != feEsperado) throw new IllegalStateException("factor de equilibrio incorrecto");
        if (Math.abs(nodo.fe()) > 1) throw new IllegalStateException("AVL desbalanceado");
        return alturaEsperada;
    }

    public static Resultado validarNario(ArbolDecisiones.Vista raiz) {
        if (raiz == null) return new Resultado(false, "árbol n-ario vacío", 0, 0);
        Set<String> ids = new HashSet<>();
        try {
            int altura = validarNodoNario(raiz, ids);
            return new Resultado(true, "N-ario correcto", ids.size(), altura);
        } catch (IllegalStateException e) {
            return new Resultado(false, e.getMessage(), ids.size(), 0);
        }
    }

    private static int validarNodoNario(ArbolDecisiones.Vista nodo, Set<String> ids) {
        if (nodo.id() == null || nodo.id().isBlank()) throw new IllegalStateException("ID vacío en n-ario");
        if (!ids.add(nodo.id())) throw new IllegalStateException("ID repetido en n-ario");
        int mayor = 0;
        for (ArbolDecisiones.Vista hijo : nodo.hijos()) {
            mayor = Math.max(mayor, validarNodoNario(hijo, ids));
        }
        return 1 + mayor;
    }
}
