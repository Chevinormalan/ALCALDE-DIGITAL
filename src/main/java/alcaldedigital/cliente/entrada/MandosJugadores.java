package alcaldedigital.cliente.entrada;

import java.util.*;

/** Mantiene las posiciones de los mandos aunque cambie la enumeración del sistema. */
public final class MandosJugadores {
    public static final int MAXIMO = 4;
    private static final long DURACION_AVISO = 3_000_000_000L;

    /** El primer puesto es cero para local. Un invitado LAN tiene un único puesto propio. */
    public record Contexto(int jugadoresLocales, int primerPuesto) {
        public static final Contexto INDIVIDUAL = new Contexto(1, 0);
        int jugador(int indice) { return indice < jugadoresLocales ? primerPuesto + indice + 1 : 0; }
    }

    public record Aviso(String texto, String nombre, long vence) {}

    private final String[] ids = new String[MAXIMO];
    private final EstadoMando[] lecturas = new EstadoMando[MAXIMO];
    private final EstadoMando[] estados = new EstadoMando[MAXIMO];
    private final boolean[] neutral = new boolean[MAXIMO];
    private final Aviso[] avisos = new Aviso[MAXIMO];
    private Contexto contexto = Contexto.INDIVIDUAL;

    public MandosJugadores() {
        Arrays.fill(lecturas, EstadoMando.desconectado());
        Arrays.fill(estados, EstadoMando.desconectado());
    }

    public void actualizar(Map<String, EstadoMando> dispositivos, Contexto nuevo, long ahora) {
        Set<String> asignados = new HashSet<>(Arrays.asList(ids));
        for (String id : dispositivos.keySet()) {
            if (!dispositivos.get(id).conectado() || asignados.contains(id)) continue;
            int indice = vacante(dispositivos, nuevo.jugadoresLocales());
            if (indice < 0) break;
            ids[indice] = id;
            lecturas[indice] = EstadoMando.desconectado();
            neutral[indice] = false;
        }
        for (int i = 0; i < MAXIMO; i++) {
            EstadoMando previo = lecturas[i];
            EstadoMando actual = ids[i] == null ? EstadoMando.desconectado()
                    : dispositivos.getOrDefault(ids[i], EstadoMando.desconectado());
            int anterior = contexto.jugador(i);
            int jugador = nuevo.jugador(i);
            String mensaje = null;
            if (actual.conectado() && !previo.conectado()) {
                mensaje = jugador == 0 ? "Mando detectado · disponible" : "Mando detectado · Jugador " + jugador;
                neutral[i] = false;
            } else if (!actual.conectado() && previo.conectado()) {
                mensaje = anterior == 0 ? "Mando desconectado" : "Mando desconectado · Jugador " + anterior;
                neutral[i] = false;
            } else if (actual.conectado() && jugador != anterior) {
                if (jugador > 0) mensaje = "Mando asignado · Jugador " + jugador;
                else avisos[i] = null;
                neutral[i] = false;
            }
            if (mensaje != null) avisos[i] = new Aviso(mensaje,
                    actual.conectado() ? actual.nombre() : previo.nombre(), ahora + DURACION_AVISO);
            lecturas[i] = actual;
            if (estaNeutro(actual)) neutral[i] = true;
            estados[i] = jugador == 0 ? EstadoMando.desconectado()
                    : neutral[i] ? actual : conNombreSinEntrada(actual);
        }
        contexto = nuevo;
    }

    private int vacante(Map<String, EstadoMando> dispositivos, int locales) {
        for (int i = 0; i < locales; i++) if (ids[i] == null || !dispositivos.containsKey(ids[i])) return i;
        for (int i = locales; i < MAXIMO; i++) if (ids[i] == null) return i;
        for (int i = locales; i < MAXIMO; i++) if (!dispositivos.containsKey(ids[i])) return i;
        return -1;
    }

    public EstadoMando estado(int indice) { return estados[indice]; }

    public List<Aviso> avisos(long ahora) {
        return Arrays.stream(avisos).filter(a -> a != null && a.vence() > ahora).toList();
    }

    public static boolean estaNeutro(EstadoMando mando) {
        return mando.botones().isEmpty() && Math.abs(mando.izquierdaX()) < .25 && Math.abs(mando.izquierdaY()) < .25
                && Math.abs(mando.derechaX()) < .25 && Math.abs(mando.derechaY()) < .25;
    }

    private static EstadoMando conNombreSinEntrada(EstadoMando mando) {
        return new EstadoMando(mando.nombre(), 0, 0, 0, 0, Set.of());
    }
}
