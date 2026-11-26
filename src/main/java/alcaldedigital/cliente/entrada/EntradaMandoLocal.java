package alcaldedigital.cliente.entrada;

import static alcaldedigital.cliente.entrada.EstadoMando.Boton;

/** Entradas de un puesto local adicional, con liberación de controles al cambiar de pantalla o pausar. */
public final class EntradaMandoLocal {
    private EstadoMando estado = EstadoMando.desconectado();
    private EstadoMando anterior = EstadoMando.desconectado();
    private boolean esperarNeutro = true;

    public void leer(EstadoMando mando, boolean permiteJugar) {
        anterior = estado;
        if (!permiteJugar || !mando.conectado()) {
            bloquear();
            return;
        }
        if (esperarNeutro && MandosJugadores.estaNeutro(mando)) esperarNeutro = false;
        estado = esperarNeutro ? EstadoMando.desconectado() : mando;
    }

    public void bloquear() {
        esperarNeutro = true;
        estado = EstadoMando.desconectado();
    }

    public EstadoMando estado() { return estado; }
    public boolean pulso(Boton boton) { return estado.pulsado(boton) && !anterior.pulsado(boton); }

    public float x() { return eje(estado.izquierdaX(), Boton.IZQUIERDA, Boton.DERECHA); }
    public float y() { return eje(estado.izquierdaY(), Boton.ARRIBA, Boton.ABAJO); }
    private float eje(float eje, Boton negativo, Boton positivo) {
        if (estado.pulsado(negativo)) return -1;
        if (estado.pulsado(positivo)) return 1;
        return eje;
    }
}
