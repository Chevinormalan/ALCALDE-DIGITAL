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
        try{juego.estadoCampana=new alcaldedigital.servidor.batalla.RepositorioResultados(alcaldedigital.servidor.batalla.GuardadoBatalla.directorio().resolve("campana.properties")).ciudad(juego.estadoCampana);}catch(java.io.IOException e){System.err.println("No se pudo cargar la campaña: "+e.getMessage());}
        PApplet.runSketch(new String[]{ClienteSketch.class.getName()},
            new ClienteSketch(juego));
    }
}
