package alcaldedigital.cliente.audio;
/** Cambio de foco, no fotogramas: una señal por entrada o selección nueva. */
public final class InterfazSonora {
 private String hover="",foco="";
 public void hover(String clave){if(!clave.equals(hover)){if(!clave.isEmpty())MotorAudio.global().interfaz("hover");hover=clave;}}
 public void foco(String clave){if(!clave.equals(foco)){if(!clave.isEmpty())MotorAudio.global().interfaz("hover");foco=clave;}}
 public static void activar(String accion){MotorAudio.global().interfaz(accion.equals("volver")||accion.equals("menu")?"volver":accion.contains("guardar")?"guardar":accion.equals("reanudar")?"reanudar":accion.contains("confirm")||accion.contains("aplicar")?"confirmar":"click");}
}
