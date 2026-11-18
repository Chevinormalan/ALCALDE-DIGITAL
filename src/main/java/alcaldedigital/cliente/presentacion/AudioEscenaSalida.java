package alcaldedigital.cliente.presentacion;
/** Permite comprobar eventos sin exigir una tarjeta de sonido. */
public interface AudioEscenaSalida extends AutoCloseable {
 void bucle(String id,String recurso,float volumen);
 void efecto(String id,String recurso,float volumen,double intervaloMinimo);
 void pausar();
 @Override void close();
}
