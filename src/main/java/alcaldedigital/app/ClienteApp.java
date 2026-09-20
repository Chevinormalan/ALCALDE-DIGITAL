package alcaldedigital.app;

import alcaldedigital.cliente.ClienteSketch;
import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.servidor.sesion.SesionLocal;
import processing.core.PApplet;

/** Punto de entrada del cliente gráfico. */
public final class ClienteApp {
    private ClienteApp() {
    }

    public static void main(String[] args) {
        PApplet.runSketch(new String[]{ClienteSketch.class.getName()},new ClienteSketch(new JuegoCliente(SesionLocal::new)));
    }
}
