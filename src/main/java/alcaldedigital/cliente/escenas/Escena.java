package alcaldedigital.cliente.escenas;

/** Contrato común de las pantallas y escenas jugables. */
public interface Escena {
    void entrar();

    void actualizar(float deltaSegundos);

    void dibujar();

    default void teclaPresionada(char tecla, int codigo) {
    }

    default void ratonPresionado(int x, int y, int boton) {
    }

    void salir();
}

