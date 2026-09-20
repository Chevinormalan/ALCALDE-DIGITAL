package alcaldedigital.cliente.escenas;

import java.util.Objects;

/** Cambia de escena sin concentrar toda la interfaz dentro de draw(). */
public final class GestorEscenas {
    private Escena actual;

    public void cambiar(Escena siguiente) {
        Objects.requireNonNull(siguiente, "La escena siguiente es obligatoria");
        if (actual != null) {
            actual.salir();
        }
        actual = siguiente;
        actual.entrar();
    }

    public Escena actual() {
        return actual;
    }
}

