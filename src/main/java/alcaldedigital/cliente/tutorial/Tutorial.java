package alcaldedigital.cliente.tutorial;

import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.entrada.EstadoMando.Boton;
import alcaldedigital.cliente.entrada.MapaControles;

import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import static java.awt.event.KeyEvent.*;

/** Sala de práctica local con sus propias entradas y tiempo de simulación. */
public final class Tutorial {
    public enum Resultado { COMPLETADO, OMITIDO }
    private enum Omision { TECLA, MANDO, RATON }
    private static final double TIEMPO_OMISION = 1.5, PAUSA_ACIERTO = .55;
    private static final Rectangle OMITIR = new Rectangle(944, 30, 294, 58);
    private static final Rectangle CONTINUAR = new Rectangle(957, 559, 245, 48);
    private static final int[] X_OPCION = {266, 514, 762};
    private static final EjercicioTutorial[] EJERCICIOS = EjercicioTutorial.values();
    private static final double PRINCIPIO_BARRERA = 535, FIN_BARRERA = 587;
    private final Set<Integer> teclas = new HashSet<>();
    private int paso, seleccion = 1, saltos;
    private double x = 285, y = 398, altura, velocidadVertical, reloj, tiempoPaso;
    private double acierto, progresoOmision, recorrido, impulsoCarrera;
    private double desplazamientoX, desplazamientoY;
    private boolean esperarMando = true, moviendo, izquierda, pasoBarrera;
    private EstadoMando mando = EstadoMando.desconectado();
    private int navegacionMando;
    private Omision omision;
    private Resultado resultado;
    private String respuesta = "";

    public Resultado resultado() { return resultado; }
    EjercicioTutorial ejercicio() { return EJERCICIOS[paso]; }
    double x() { return x; }
    double y() { return y; }
    double altura() { return altura; }
    double reloj() { return reloj; }
    double tiempoPaso() { return tiempoPaso; }
    double efecto() { return acierto == 0 ? 0 : 1 - acierto / PAUSA_ACIERTO; }
    double progresoOmision() { return Math.min(1, progresoOmision / TIEMPO_OMISION); }
    double progresoCarrera() { return Math.min(1, recorrido / 240); }
    double vallaCarreraX() { return 895 - tiempoPaso * 262; }
    boolean acierto() { return acierto > 0; }
    boolean moviendo() { return moviendo; }
    boolean izquierda() { return izquierda; }
    int seleccion() { return seleccion; }
    String respuesta() { return respuesta; }
    String dispositivo() { return mando.conectado() ? mando.nombre() : ""; }

    static Rectangle botonOmitir() { return new Rectangle(OMITIR); }
    static Rectangle botonContinuar() { return new Rectangle(CONTINUAR); }
    static Rectangle opcion(int n) { return new Rectangle(X_OPCION[n], 362, 228, 59); }

    String omitirCon() {
        if (!mando.conectado()) return "Esc o clic";
        return MapaControles.etiquetaBoton(new MapaControles.Contexto(ejercicio().mision,false,null,null,mando.nombre()), "B") + " o clic";
    }

    boolean eligiendo() {
        return switch (ejercicio()) { case CONTRASTAR, SERVIDOR, NOTICIA -> true; default -> false; };
    }

    boolean presentaBoton() { return ejercicio() == EjercicioTutorial.INICIO || ejercicio() == EjercicioTutorial.CIERRE; }

    public void pulsar(int codigo) {
        if (resultado != null || !teclas.add(codigo)) return;
        if (codigo == VK_ESCAPE) {
            comenzarOmision(Omision.TECLA);
            return;
        }
        if (eligiendo()) {
            if (codigo == VK_TAB || codigo == VK_RIGHT || codigo == VK_DOWN) navegar(1);
            if (codigo == VK_LEFT || codigo == VK_UP) navegar(-1);
            if (ejercicio() != EjercicioTutorial.CONTRASTAR && codigo >= VK_1 && codigo <= VK_3) elegir(codigo - VK_1);
        }
        if (ejercicio().control.teclas().contains(codigo)) actuar();
    }

    public void soltar(int codigo) {
        teclas.remove(codigo);
        if (codigo == VK_ESCAPE) cancelar(Omision.TECLA);
    }

    public void pulsarRaton(int x, int y) {
        if (resultado != null) return;
        if (OMITIR.contains(x, y)) {
            comenzarOmision(Omision.RATON);
            return;
        }
        if (presentaBoton() && CONTINUAR.contains(x, y)) actuar();
        if (eligiendo()) for (int n = 0; n < X_OPCION.length; n++) {
            if (opcion(n).contains(x, y)) { elegir(n); break; }
        }
    }

    public void apuntar(int x, int y) {
        if (!OMITIR.contains(x, y)) cancelar(Omision.RATON);
    }

    public void soltarRaton() { cancelar(Omision.RATON); }

    public void perderFoco() {
        teclas.clear();
        cancelar(omision);
        esperarMando = true;
        navegacionMando = 0;
        desplazamientoX = desplazamientoY = 0;
    }

    public void mando(EstadoMando estado) {
        EstadoMando anterior = mando;
        mando = estado;
        if (!anterior.nombre().equals(estado.nombre())) {
            perderFoco();
            if (!estado.conectado()) return;
        }
        if (!estado.conectado() || resultado != null) return;
        if (esperarMando) {
            if (estado.botones().isEmpty() && Math.abs(estado.izquierdaX()) < .25 && Math.abs(estado.izquierdaY()) < .25)
                esperarMando = false;
            return;
        }
        if (!estado.pulsado(Boton.B)) cancelar(Omision.MANDO);
        else if (!anterior.pulsado(Boton.B)) comenzarOmision(Omision.MANDO);
        if (eligiendo()) {
            int direccion = direccionMenu(estado);
            if (direccion != 0 && direccion != navegacionMando) navegar(direccion);
            navegacionMando = direccion;
        }
        for (Boton b : ejercicio().control.botones()) {
            if (estado.pulsado(b) && !anterior.pulsado(b)) { actuar(); break; }
        }
    }

    private static int direccionMenu(EstadoMando e) {
        if (e.pulsado(Boton.DERECHA) || e.pulsado(Boton.ABAJO) || e.izquierdaX() > .58 || e.izquierdaY() > .58) return 1;
        if (e.pulsado(Boton.IZQUIERDA) || e.pulsado(Boton.ARRIBA) || e.izquierdaX() < -.58 || e.izquierdaY() < -.58) return -1;
        return 0;
    }

    private void comenzarOmision(Omision entrada) {
        if (omision != null) return;
        omision = entrada;
        progresoOmision = 0;
    }

    private void cancelar(Omision entrada) {
        if (omision != entrada) return;
        omision = null;
        progresoOmision = 0;
    }

    private void navegar(int direccion) {
        if (acierto > 0) return;
        seleccion = Math.floorMod(seleccion + direccion, X_OPCION.length);
        respuesta = "";
    }

    private void elegir(int n) {
        if (acierto > 0) return;
        seleccion = n;
        if (n == 0) aprobar();
        else respuesta = switch (ejercicio()) {
            case CONTRASTAR -> "Qué fácil sería, ¿eh? Prueba contrastar las fuentes.";
            case SERVIDOR -> "Un aviso no se arregla solo. Verifica la señal.";
            default -> "Podrías acabar en un callejón. Contrasta la ruta.";
        };
    }

    private void actuar() {
        if (acierto > 0 || resultado != null) return;
        switch (ejercicio()) {
            case CONTRASTAR, SERVIDOR, NOTICIA -> elegir(seleccion);
            case SALTO_TERMINAL, SALTO_PULSO -> saltar(false);
            case DOBLE_SALTO -> saltar(true);
            case ACELERAR -> impulsoCarrera = .22;
            case CIERRE -> resultado = Resultado.COMPLETADO;
            case ACERCARSE, CORRER, CARRIL, PILOTAR, LATERAL -> { }
            default -> aprobar();
        }
    }

    private void saltar(boolean doble) {
        if (saltos == 0) {
            saltos = 1;
            velocidadVertical = doble ? 350 : 385;
        } else if (doble && saltos == 1 && altura > 2) {
            saltos = 2;
            velocidadVertical = 365;
            aprobar();
        }
    }

    public void actualizar(double dt) {
        if (resultado != null) return;
        dt = Math.clamp(dt, 0, .1);
        reloj += dt;
        tiempoPaso += dt;
        if (omision != null) {
            progresoOmision += dt;
            if (progresoOmision + 1e-9 >= TIEMPO_OMISION) {
                resultado = Resultado.OMITIDO;
                return;
            }
        }
        if (saltos > 0) {
            altura = Math.max(0, altura + velocidadVertical * dt);
            velocidadVertical -= 1080 * dt;
            if (altura == 0 && velocidadVertical <= 0) { saltos = 0; velocidadVertical = 0; }
        }
        if (acierto > 0) {
            actualizarEfecto(dt);
            acierto -= dt;
            if (acierto <= 0) siguiente();
            return;
        }
        moviendo = false;
        switch (ejercicio()) {
            case ACERCARSE -> {
                mover(dt, 242, true);
                if (Math.hypot(x - 645, y - 375) < 37) aprobar();
            }
            case CORRER -> {
                boolean corre = activaControl();
                mover(dt, corre ? 350 : 180, false);
                if (corre) recorrido += Math.abs(desplazamientoX);
                if (recorrido >= 240) aprobar();
            }
            case SALTO_TERMINAL, SALTO_PULSO -> {
                double anterior = x;
                mover(dt, 245, false);
                if (altura < 18 && anterior <= FIN_BARRERA && x > anterior && x > PRINCIPIO_BARRERA)
                    x = anterior <= PRINCIPIO_BARRERA ? PRINCIPIO_BARRERA : anterior;
                if (altura >= 18 && x > FIN_BARRERA && anterior <= FIN_BARRERA) pasoBarrera = true;
                if (x > FIN_BARRERA + 9 && pasoBarrera) aprobar();
            }
            case PILOTAR -> {
                mover(dt, 245, true);
                if (Math.hypot(x - 652, y - 280) < 40) aprobar();
            }
            case ACELERAR -> {
                impulsoCarrera = Math.max(0, impulsoCarrera - dt);
                if (activaControl() || impulsoCarrera > 0) recorrido += dt * 218;
                if (recorrido >= 240) aprobar();
            }
            case CARRIL -> {
                y = Math.clamp(y + eje(false, false) * dt * 195, 299, 432);
                if (vallaCarreraX() <= x + 34) {
                    if (y < 345) aprobar();
                    else {
                        tiempoPaso = 0;
                        respuesta = "La valla te frenó. Prueba el carril superior.";
                    }
                }
            }
            case LATERAL -> {
                mover(dt, 245, false);
                if (Math.abs(x - 645) < 34) aprobar();
            }
            default -> { }
        }
    }

    private void mover(double dt, double velocidad, boolean vertical) {
        double dx = eje(true, true), dy = vertical ? eje(false, true) : 0;
        double factor = Math.max(1, Math.hypot(dx, dy));
        double anteriorX = x, anteriorY = y;
        x = Math.clamp(x + dx * velocidad * dt / factor, 148, 1020);
        y = Math.clamp(y + dy * velocidad * dt / factor, 219, 450);
        desplazamientoX = x - anteriorX;
        desplazamientoY = y - anteriorY;
        moviendo = Math.abs(desplazamientoX) + Math.abs(desplazamientoY) > .01;
        if (dx != 0) izquierda = dx < 0;
    }

    private double eje(boolean horizontal, boolean usaCruceta) {
        int negativo = horizontal ? VK_A : VK_W, positivo = horizontal ? VK_D : VK_S;
        int flechaNegativa = horizontal ? VK_LEFT : VK_UP, flechaPositiva = horizontal ? VK_RIGHT : VK_DOWN;
        boolean flechas = ejercicio() != EjercicioTutorial.LATERAL;
        int teclado = (teclas.contains(positivo) || flechas && teclas.contains(flechaPositiva) ? 1 : 0)
            - (teclas.contains(negativo) || flechas && teclas.contains(flechaNegativa) ? 1 : 0);
        if (teclado != 0 || esperarMando) return teclado;
        if (usaCruceta && mando.pulsado(horizontal ? Boton.IZQUIERDA : Boton.ARRIBA)) return -1;
        if (usaCruceta && mando.pulsado(horizontal ? Boton.DERECHA : Boton.ABAJO)) return 1;
        double valor = horizontal ? mando.izquierdaX() : mando.izquierdaY();
        return Math.abs(valor) > .25 ? valor : 0;
    }

    private boolean activaControl() {
        var control = ejercicio().control;
        return control.teclas().stream().anyMatch(teclas::contains) ||
            !esperarMando && control.botones().stream().anyMatch(mando::pulsado);
    }

    private void actualizarEfecto(double dt) {
        switch (ejercicio()) {
            case DASH, ESQUIVA_PLAZA, IMPULSO, ESQUIVA_FINAL -> x += dt * 465;
            default -> { }
        }
    }

    private void aprobar() {
        if (acierto > 0) return;
        acierto = PAUSA_ACIERTO;
        respuesta = "";
    }

    private void siguiente() {
        paso++;
        acierto = 0;
        tiempoPaso = 0;
        seleccion = 1;
        respuesta = "";
        navegacionMando = 0;
        recorrido = impulsoCarrera = 0;
        altura = velocidadVertical = 0;
        saltos = 0;
        pasoBarrera = false;
        moviendo = izquierda = false;
        x = switch (ejercicio()) {
            case HABLAR, PAPEL, LUPA, CONTRASTAR, LIBRETA -> 645;
            case SALTO_TERMINAL, SALTO_PULSO -> 457;
            case REPARAR -> 607;
            case FLASH, ATAQUE, GOLPE_FINAL -> 525;
            case CORRER, PILOTAR -> 280;
            default -> 380;
        };
        y = switch (ejercicio()) {
            case HABLAR, PAPEL, LUPA, CONTRASTAR, LIBRETA -> 375;
            case ACELERAR, CARRIL -> 410;
            case IMPULSO, DISPARO, MISIL -> 308;
            default -> 398;
        };
    }
}
