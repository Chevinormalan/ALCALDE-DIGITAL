package alcaldedigital.cliente.presentacion;
import alcaldedigital.cliente.audio.*;
import java.util.concurrent.atomic.AtomicLong;

/** Salida de escena sobre el mezclador común; el grupo conserva su pausa y aislamiento. */
public final class AudioEscena implements AudioEscenaSalida {
 private static final AtomicLong IDS=new AtomicLong();private final MotorAudio motor=MotorAudio.global();private final String grupo;private boolean cerrado;
 public AudioEscena(String nombre){grupo=nombre+"-"+IDS.incrementAndGet();}
 @Override public void bucle(String id,String recurso,float volumen){if(!cerrado)motor.bucle(grupo,id,recurso,volumen);}
 @Override public void efecto(String id,String recurso,float volumen,double espera){if(!cerrado)motor.efecto(grupo,id,recurso,volumen,espera,OpcionesAudio.Bus.EFECTOS);}
 @Override public void pausar(){if(!cerrado)motor.suspender(grupo);}
 public void reanudar(){if(!cerrado)motor.reanudar(grupo);}
 @Override public void close(){if(!cerrado){cerrado=true;motor.cerrar(grupo);}}
}
