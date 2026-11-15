package alcaldedigital.cliente.mision7;
import alcaldedigital.compartido.carrera.EstadoCarrera.Vista;
public interface CanalCarrera extends AutoCloseable {
    Vista vista();void movimiento(int rol,float vertical,boolean correr);void accion(int rol,String accion,int valor);
    default void actualizar(float dt){}default String error(){return "";}@Override void close();
}
