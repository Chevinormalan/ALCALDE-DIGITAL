package alcaldedigital.app;

import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.compartido.modelo.Publicacion;
import alcaldedigital.compartido.modelo.Rol;
import alcaldedigital.i1.ValidadorI1;
import alcaldedigital.servidor.sesion.SesionLocal;
import java.util.List;

/** Autoprueba independiente de árboles y sesión, sin Maven ni JUnit. */
public final class AutopruebaI1App {
    private static int correctas;

    private AutopruebaI1App() { }

    public static void main(String[] args) {
        probarAVL();
        probarNario();
        probarIntegracion();
        System.out.println("Autoprueba completada: " + correctas + " comprobaciones superadas.");
    }

    private static Publicacion publicacion(long id, int riesgo) {
        return new Publicacion(id, riesgo, "autor" + id, "texto" + id, "Sin comprobar");
    }

    private static void probarAVL() {
        int[][] rotaciones = {{30,20,10},{10,20,30},{30,10,20},{10,30,20}};
        for (int[] datos : rotaciones) {
            ArbolAVLPublicaciones arbol = new ArbolAVLPublicaciones();
            for (int i=0;i<3;i++) arbol.insertar(publicacion(i+1, datos[i]));
            comprobar(arbol.vista().valor().riesgo() == 20, "raíz correcta tras rotación");
            comprobar(ValidadorI1.validarAVL(arbol.vista()).correcto(), "AVL válido tras rotación");
        }

        ArbolAVLPublicaciones repetidos = new ArbolAVLPublicaciones();
        repetidos.insertar(publicacion(1,20));
        repetidos.insertar(publicacion(2,20));
        comprobar(repetidos.cantidad() == 2, "mismo riesgo con IDs distintos");
        comprobar(repetidos.buscar(1) != null && repetidos.buscar(2) != null, "búsqueda por ID");
        repetidos.actualizar(publicacion(1,90));
        comprobar(repetidos.buscar(1).riesgo() == 90, "reindexación por cambio de riesgo");
        comprobar(ValidadorI1.validarAVL(repetidos.vista()).correcto(), "AVL válido tras reindexar");

        ArbolAVLPublicaciones borrar = new ArbolAVLPublicaciones();
        for (int n : new int[]{50,30,70,20,40,60,80,65}) borrar.insertar(publicacion(n,n));
        comprobar(borrar.eliminar(20), "borrar hoja");
        comprobar(ValidadorI1.validarAVL(borrar.vista()).correcto(), "válido tras borrar hoja");
        comprobar(borrar.eliminar(60), "borrar nodo con un hijo");
        comprobar(ValidadorI1.validarAVL(borrar.vista()).correcto(), "válido tras borrar un hijo");
        comprobar(borrar.eliminar(70), "borrar nodo con dos hijos");
        comprobar(ValidadorI1.validarAVL(borrar.vista()).correcto(), "válido tras borrar dos hijos");
        long raiz = borrar.vista().valor().id();
        comprobar(borrar.eliminar(raiz), "borrar raíz");
        comprobar(ValidadorI1.validarAVL(borrar.vista()).correcto(), "válido tras borrar raíz");

        ArbolAVLPublicaciones recorridos = new ArbolAVLPublicaciones();
        recorridos.insertar(publicacion(20,20));
        recorridos.insertar(publicacion(10,10));
        recorridos.insertar(publicacion(30,30));
        comprobar(recorridos.recorrer("inorden").stream().map(Publicacion::id).toList().equals(List.of(10L,20L,30L)), "inorden");
        comprobar(recorridos.recorrer("preorden").stream().map(Publicacion::id).toList().equals(List.of(20L,10L,30L)), "preorden");
        comprobar(recorridos.recorrer("postorden").stream().map(Publicacion::id).toList().equals(List.of(10L,30L,20L)), "postorden");
        comprobar(recorridos.recorrer("inverso").stream().map(Publicacion::id).toList().equals(List.of(30L,20L,10L)), "inverso");
    }

    private static void probarNario() {
        ArbolDecisiones arbol = new ArbolDecisiones("parque", "Rumor del parque");
        arbol.insertar("parque", "compartir", "Compartir");
        arbol.insertar("parque", "verificar", "Verificar");
        arbol.insertar("parque", "reportar", "Reportar");
        arbol.insertar("parque", "ignorar", "Ignorar");
        comprobar(arbol.buscar("verificar") != null, "buscar opción n-aria");
        comprobar(arbol.recorrer("preorden").get(0).equals("parque"), "preorden n-ario");
        comprobar(arbol.recorrer("niveles").size() == 5, "recorrido por niveles");
        arbol.insertar("verificar", "contexto", "Publicar contexto");
        arbol.ejecutar("contexto");
        comprobar(arbol.buscar("verificar").ejecutado(), "ruta ejecutada");
        comprobar(arbol.buscar("contexto").ejecutado(), "decisión final ejecutada");
        comprobar(arbol.podar("ignorar"), "podar rama no ejecutada");
        comprobar(ValidadorI1.validarNario(arbol.vista()).correcto(), "n-ario válido");
        esperarError(() -> arbol.insertar("verificar", "contexto", "Duplicado"), "rechazar ID repetido");
        esperarError(() -> arbol.insertar("no-existe", "x", "Huérfano"), "rechazar padre inexistente");
        esperarError(() -> arbol.podar("verificar"), "no podar ruta ejecutada");
    }

    private static void probarIntegracion() {
        SesionLocal sesion = new SesionLocal(Rol.CIUDADANO, 0, 60);
        comprobar(sesion.arbolAVL().vista().valor().riesgo() == 20, "inicio 30,20,10 balanceado");
        comprobar(sesion.solicitar("v", "verificar").contains("aviso"), "verificar inicia investigación");
        moverHasta(sesion,192,174);
        sesion.solicitar("a","aviso");
        comprobar(sesion.vista().puntos() == 5, "primera evidencia suma 5");
        moverHasta(sesion,230,208);
        sesion.solicitar("f","fuente");
        comprobar(sesion.vista().evidencias().size() == 2, "dos evidencias registradas");
        comprobar(sesion.buscarPublicacion(1).riesgo() == 8, "riesgo baja a 8 al contrastar");
        comprobar(sesion.buscarDecision("contexto") != null, "evidencia habilita contexto");
        sesion.solicitar("c","contexto");
        comprobar(sesion.vista().terminado(), "caso cerrado");
        comprobar(sesion.vista().puntos() == 25, "puntuación final 25");
        comprobar(sesion.buscarPublicacion(1) == null, "rumor sale del AVL activo");
        comprobar(sesion.buscarPublicacion(4) != null, "corrección entra al AVL");
        comprobar(ValidadorI1.validarAVL(sesion.arbolAVL().vista()).correcto(), "AVL final válido");
        comprobar(ValidadorI1.validarNario(sesion.arbolDecisiones().vista()).correcto(), "n-ario final válido");

        SesionLocal duplicado = new SesionLocal(Rol.INFLUENCER,0,60);
        duplicado.solicitar("uno","compartir");
        int puntos = duplicado.vista().puntos();
        duplicado.solicitar("uno","compartir");
        duplicado.solicitar("dos","compartir");
        comprobar(duplicado.vista().puntos() == puntos, "acción repetida no duplica puntos");
    }

    private static void moverHasta(SesionLocal sesion, int x, int y) {
        for (int i=0;i<1000 && Math.abs(sesion.vista().x()-x)>.5;i++) {
            sesion.mover(Math.signum(x-sesion.vista().x()) * Math.min(2,Math.abs(x-sesion.vista().x())),0);
        }
        for (int i=0;i<1000 && Math.abs(sesion.vista().y()-y)>.5;i++) {
            sesion.mover(0,Math.signum(y-sesion.vista().y()) * Math.min(2,Math.abs(y-sesion.vista().y())));
        }
        comprobar(Math.abs(sesion.vista().x()-x) <= 1 && Math.abs(sesion.vista().y()-y) <= 1, "movimiento hasta evidencia");
    }

    private static void comprobar(boolean condicion, String nombre) {
        if (!condicion) throw new IllegalStateException("Falló la comprobación: " + nombre);
        correctas++;
    }

    private static void esperarError(Runnable accion, String nombre) {
        try {
            accion.run();
            throw new IllegalStateException("Falló la comprobación: " + nombre);
        } catch (IllegalArgumentException esperada) {
            correctas++;
        }
    }
}
