package alcaldedigital.cliente.mision5;

import alcaldedigital.compartido.batalla.EstadoBatalla.Vista;

/** Canal común para la sesión individual, cooperativa en teclado y TCP. */
public interface CanalBatalla extends AutoCloseable {
    Vista vista();
    void movimiento(int rol,float dx,float dy,boolean salto);
    void accion(int rol,String accion,int valor);
    default String error(){return "";}
    default void actualizar(float segundos){}
    @Override void close();
}
