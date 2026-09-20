package alcaldedigital.compartido.protocolo;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import java.util.List;

/** Frontera local sustituible por transporte en la entrega de red. */
public interface SesionJuego {
    record Vista(Rol rol, EstadoCiudad ciudad, int puntos, int version, String objetivo,
                 String mensaje, List<String> evidencias, List<String> historial, boolean terminado,
                 List<Publicacion> publicaciones, ArbolAVLPublicaciones.Vista avl,
                 ArbolDecisiones.Vista decisiones, float x, float y, boolean verificando) { }
    Vista vista();
    String solicitar(String requestId, String accion);
    void mover(float dx,float dy);
    void actualizar(double segundos);
    MapaParque mapa();
    double restante();
    boolean cerca(String punto);
    List<ArbolAVLPublicaciones.Paso> pasosAVL();
    List<ArbolDecisiones.Paso> pasosDecisiones();
    List<Publicacion> recorridoAVL(String orden);
    List<String> recorridoDecisiones(String orden);
    Publicacion buscarPublicacion(long id);
    ArbolDecisiones.Vista buscarDecision(String id);
    @FunctionalInterface interface Fabrica { SesionJuego crear(Rol rol,long semilla,int plazo); }
}
