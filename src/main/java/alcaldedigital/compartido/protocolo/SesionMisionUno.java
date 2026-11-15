package alcaldedigital.compartido.protocolo;

import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.compartido.modelo.Publicacion;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.compartido.estructuras.grafo.GrafoSocialMisionUno;
import java.util.List;

/** Vista pública y solicitudes de la primera misión; la sesión conserva la verdad y el reloj. */
public interface SesionMisionUno {
    enum Fase { INTRO, CIVITAS, CRISIS, RESULTADO }
    enum Dificultad {
        FACIL(60), NORMAL(30), DIFICIL(12);
        private final int segundos;
        Dificultad(int segundos) {this.segundos=segundos;}
        public int segundos() {return segundos;}
    }
    enum Animo { NORMAL, ALTERADO, CALMADO, SALIENDO, FUERA }
    enum EstadoPalomo { LLEGANDO, POSADO, SALIENDO, FUERA }
    record Palomo(int lado, double progreso, EstadoPalomo estado) { }
    record Peaton(int id, float x, float y, int direccion, Animo animo, int conversaciones,
                  int necesarias, int salida) { }
    record Vista(Fase fase, Dificultad dificultad, int dialogo, Palomo palomo,
                 float x, float y, int direccion, boolean caminando,
                 double tiempo, int puntos, int calmados, int parejasFuera, String mensaje,
                 List<Peaton> peatones, List<Publicacion> publicaciones, List<String> fuentes,
                 List<Boolean> resueltas, EstadoCiudad ciudad, String desenlace,
                 ArbolAVLPublicaciones.Vista avl, ArbolDecisiones.Vista decisiones,
                 List<GrafoSocialMisionUno.Arista> conexiones) { }
    Vista vista();
    String solicitar(String id, String accion);
    void mover(float dx, float dy);
    void actualizar(double segundos);
    @FunctionalInterface interface Fabrica { SesionMisionUno crear(long semilla, Dificultad dificultad); }
}
