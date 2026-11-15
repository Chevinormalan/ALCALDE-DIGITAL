package alcaldedigital.pruebas;
import alcaldedigital.cliente.JuegoCliente;
/** Precondiciones explícitas de las regresiones anteriores al bloqueo de campaña. */
public final class PartidaPrueba {
 private PartidaPrueba(){}
 public static void desbloquear(JuegoCliente j){for(int n=1;n<=7;n++)j.victoria(n);}
 public static void elegir(JuegoCliente j,int n){j.accion("mision"+n);if(j.pantalla==JuegoCliente.Pantalla.GUARDADO)j.accion("partida:nueva");}
}
