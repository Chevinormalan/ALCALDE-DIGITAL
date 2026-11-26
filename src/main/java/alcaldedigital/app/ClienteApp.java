package alcaldedigital.app;

import alcaldedigital.cliente.ClienteSketch;
import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import processing.core.PApplet;

/** Punto de entrada del cliente gráfico. */
public final class ClienteApp {
    private ClienteApp() {
    }

    public static void main(String[] args) {
        JuegoCliente juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        boolean revision=Boolean.getBoolean("ad.smoke")||System.getProperties().stringPropertyNames().stream()
            .anyMatch(k -> k.startsWith("ad.") &&
                (k.endsWith(".revision") || k.endsWith(".integracion") || k.endsWith(".autoprueba")) && Boolean.getBoolean(k));
        if(revision)juego.cargarEstadoCampana();
        else juego.iniciarUsuarios();
        PApplet.runSketch(new String[]{ClienteSketch.class.getName()},
            new ClienteSketch(juego));
    }
}
