package alcaldedigital.cliente.entrada;

import java.util.EnumSet;
import java.util.Set;

/** Estado normalizado de un mando durante un fotograma. */
public record EstadoMando(String nombre, float izquierdaX, float izquierdaY,
                          float derechaX, float derechaY, Set<Boton> botones) {
    public enum Boton { A, B, X, Y, LB, RB, LT, RT, MENU, VISTA,
        ARRIBA, ABAJO, IZQUIERDA, DERECHA }

    public EstadoMando {
        botones=botones.isEmpty()?Set.of():Set.copyOf(botones);
    }

    public static EstadoMando desconectado() {
        return new EstadoMando("",0,0,0,0,EnumSet.noneOf(Boton.class));
    }

    public boolean conectado() { return !nombre.isBlank(); }
    public boolean pulsado(Boton boton) { return botones.contains(boton); }
}
