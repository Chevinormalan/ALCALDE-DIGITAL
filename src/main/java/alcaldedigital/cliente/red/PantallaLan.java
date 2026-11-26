package alcaldedigital.cliente.red;

import alcaldedigital.cliente.audio.InterfazSonora;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.cliente.interfaz.MarcosProcessing;
import alcaldedigital.compartido.red.DescubrimientoLan;
import alcaldedigital.compartido.red.DescubrimientoLan.Sala;
import processing.core.PApplet;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static java.awt.event.KeyEvent.*;

/** Menús comunes de búsqueda, conexión y espera para las misiones con LAN. */
public final class PantallaLan implements AutoCloseable {
    private static final int ORO = 0xfff4cb6a, LILA = 0xffbd91d5, PAPEL = 0xfff5e8fb, PANEL = 0xff38264b;
    private enum Vista { MODOS, BUSCAR, ESPERAR, RECONECTAR }
    public record Destino(String ip, int puerto, int puesto) {}
    private record Boton(int x, int y, int ancho, String texto, Runnable accion) {
        boolean contiene(int px, int py) { return px >= x && px < x + ancho && py >= y && py < y + 46; }
    }

    private final int mision;
    private final int puerto;
    private final String[] puestos;
    private final List<Boton> botones = new ArrayList<>();
    private final Set<Integer> teclas = new HashSet<>();
    private EstadoMando anterior = EstadoMando.desconectado();
    private Vista vista;
    private DescubrimientoLan.Busqueda busqueda;
    private String errorBusqueda = "", ip = "";
    private Sala elegida;
    private boolean manual, editar;
    private int foco, pagina, puestoManual;
    private Consumer<Destino> conectar;
    private Runnable volver, local, crear, continuar, reconectar;
    private final InterfazSonora audio = new InterfazSonora();
    public PantallaLan(int mision, int puerto, String... puestos) {
        this.mision = mision;
        this.puerto = puerto;
        this.puestos = puestos.clone();
        this.puestoManual = mision == 7 ? 0 : 1;
    }

    public boolean activa() { return vista != null; }
    public boolean buscando() { return vista == Vista.BUSCAR; }
    public boolean esperando() { return vista == Vista.ESPERAR; }
    public boolean reconectando() { return vista == Vista.RECONECTAR; }

    public void modos(Runnable local, Runnable crear, Runnable buscar, Runnable volver) {
        cambiar(Vista.MODOS);
        this.local = local;
        this.crear = crear;
        this.continuar = buscar;
        this.volver = volver;
    }

    public void buscar(Consumer<Destino> conectar, Runnable volver) {
        cambiar(Vista.BUSCAR);
        this.conectar = conectar;
        this.volver = volver;
        manual = editar = false;
        elegida = null;
        pagina = 0;
        errorBusqueda = "";
        try { busqueda = new DescubrimientoLan.Busqueda(mision); }
        catch (IOException e) { errorBusqueda = "No se pudo buscar en la red local. Puedes conectar por IP."; }
    }

    public void esperar(Runnable cancelar) {
        cambiar(Vista.ESPERAR);
        volver = cancelar;
    }

    public void reconectar(Runnable reconectar, Runnable salir) {
        cambiar(Vista.RECONECTAR);
        this.reconectar = reconectar;
        volver = salir;
    }

    private void cambiar(Vista nueva) {
        close();
        vista = nueva;
        foco = 0;
    }

    public void dibujar(PApplet p, String mensaje, boolean ocupado, boolean anfitrion, int cupo, int mascara) {
        if (!activa()) return;
        audio.foco("lan-" + foco);
        audio.hover(botones.stream().filter(b -> b.contiene(p.mouseX, p.mouseY))
                .map(b -> Integer.toString(b.y())).findFirst().orElse(""));
        botones.clear();
        p.pushStyle();
        p.background(0xff20142e);
        MarcosProcessing.marco(p.g, 90, 55, 1100, 610, PANEL, ORO);
        p.textAlign(PApplet.LEFT, PApplet.BASELINE);
        texto(p, "MISIÓN " + mision + " · " + switch (vista) {
            case MODOS -> "CIELOS DE CAMPAÑA";
            case BUSCAR -> "UNIRSE A PARTIDA LAN";
            case ESPERAR -> anfitrion ? "PARTIDA CREADA" : "CONECTADO A LA PARTIDA";
            case RECONECTAR -> "PARTIDA INTERRUMPIDA";
        }, 135, 115, 32, ORO);
        switch (vista) {
            case MODOS -> {
                boton(135, 180, 1010, "Jugar local · 1 o 2 jugadores", local);
                boton(135, 245, 1010, "Crear partida LAN · 2 jugadores", crear);
                boton(135, 310, 1010, "Unirse a partida LAN", continuar);
                boton(135, 565, 320, "Volver a misiones", volver);
            }
            case BUSCAR -> dibujarBusqueda(p, ocupado);
            case ESPERAR -> {
                int conectados = Integer.bitCount(mascara);
                texto(p, conectados + " de " + cupo + " jugadores conectados"
                        + (conectados == cupo ? " · iniciando…" : " · faltan " + (cupo - conectados)), 135, 177, 26, PAPEL);
                for (int i = 0; i < cupo; i++) {
                    boolean presente = (mascara & (1 << i)) != 0;
                    texto(p, "J" + (i + 1) + " · " + puestos[i] + "  /  " + (presente ? "Conectado" : "Esperando conexión"),
                            145, 246 + i * 55, 22, presente ? ORO : LILA);
                }
                boton(135, 565, 390, anfitrion ? "Cancelar partida" : "Salir de la sala", volver);
            }
            case RECONECTAR -> {
                if (!ocupado) {
                    boton(135, 220, 480, "Reconectar al mismo puesto", reconectar);
                    boton(135, 565, 360, "Volver a misiones", volver);
                }
            }
        }
        if (mensaje != null && !mensaje.isBlank()) texto(p, mensaje, 135, 643, 17, ORO);
        foco = Math.min(foco, Math.max(0, botones.size() - 1));
        for (int i = 0; i < botones.size(); i++) {
            Boton b = botones.get(i);
            boolean activo = i == foco || b.contiene(p.mouseX, p.mouseY);
            MarcosProcessing.marco(p.g, b.x, b.y, b.ancho, 46, activo ? 0xff604077 : 0xff2b1b3c, activo ? ORO : LILA);
            texto(p, b.texto, b.x + 15, b.y + 30, 18, PAPEL);
        }
        p.popStyle();
    }

    private void dibujarBusqueda(PApplet p, boolean ocupado) {
        List<Sala> salas = busqueda == null ? List.of() : busqueda.salas();
        texto(p, salas.size() + (salas.size() == 1 ? " partida disponible" : " partidas disponibles"), 135, 163, 23, ORO);
        if (ocupado) {
            texto(p, "Conectando con el anfitrión…", 135, 220, 21, LILA);
            return;
        }
        if (manual) {
            boton(135, 200, 875, "Dirección IP o nombre del equipo: " + ip + (editar ? "_" : ""), () -> editar = true);
            if (mision != 6) boton(135, 265, 875, "Puesto: " + puestos[puestoManual], () -> {
                puestoManual = mision == 8 ? 1 + puestoManual % (puestos.length - 1) : (puestoManual + 1) % puestos.length;
            });
            if (!ip.isBlank()) boton(135, 330, 380, "Conectar", () -> conectar.accept(new Destino(ip.strip(), puerto, puestoManual)));
            boton(135, 565, 330, "Volver a salas", this::listar);
            return;
        }
        if (elegida != null) {
            UUID id = elegida.id();
            Sala vigente = salas.stream().filter(s -> s.id().equals(id)).findFirst().orElse(null);
            if (vigente == null) texto(p, "La sala se llenó o se cerró.", 135, 230, 22, LILA);
            else {
                texto(p, "Elegir puesto en " + vigente.nombre(), 135, 215, 22, PAPEL);
                for (int i = 0; i < vigente.cupos().total(); i++) if (vigente.libre(i)) {
                    int puesto = i;
                    boton(135, 245 + i * 60, 580, "J" + (i + 1) + " · " + puestos[i],
                            () -> conectar.accept(new Destino(vigente.ip(), vigente.puerto(), puesto)));
                }
            }
            boton(135, 565, 330, "Volver a salas", this::listar);
            return;
        }
        if (salas.isEmpty()) {
            String error = busqueda == null ? errorBusqueda : busqueda.error();
            texto(p, error.isBlank() ? "Buscando partidas en la red local…" : error, 135, 225, 20, LILA);
        }
        int ultima = Math.max(0, (salas.size() - 1) / 5);
        pagina = Math.min(pagina, ultima);
        for (int i = pagina * 5; i < Math.min(salas.size(), pagina * 5 + 5); i++) {
            Sala sala = salas.get(i);
            boton(135, 185 + (i % 5) * 57, 1010,
                    sala.nombre() + " · " + sala.cupos().detalle() + " · "
                            + sala.conectados() + "/" + sala.cupos().total() + " · "
                            + (sala.cupos().total() - sala.conectados()) + " libre(s)", () -> elegir(sala));
        }
        if (pagina > 0) boton(135, 487, 260, "Página anterior", () -> pagina--);
        if (pagina < ultima) boton(420, 487, 260, "Página siguiente", () -> pagina++);
        boton(135, 565, 350, "Conectar por IP", () -> { manual = true; editar = true; foco = 0; });
        boton(510, 565, 310, "Volver", volver);
    }

    private void elegir(Sala sala) {
        if (sala.cupos().total() - sala.conectados() == 1) {
            for (int i = 0; i < sala.cupos().total(); i++) if (sala.libre(i)) {
                conectar.accept(new Destino(sala.ip(), sala.puerto(), i));
                return;
            }
        }
        elegida = sala;
        foco = 0;
    }

    private void listar() { manual = editar = false; elegida = null; foco = 0; }
    private void boton(int x, int y, int ancho, String texto, Runnable accion) {
        botones.add(new Boton(x, y, ancho, texto, accion));
    }
    private static void texto(PApplet p, String texto, int x, int y, int tamano, int color) {
        p.fill(color);
        p.textSize(tamano);
        p.text(texto, x, y);
    }

    public boolean pulsar(char c, int k) {
        if (!activa()) return false;
        if (editar) {
            if (k == VK_ENTER || k == VK_ESCAPE || k == VK_TAB) editar = false;
            else if (k == VK_BACK_SPACE && !ip.isEmpty()) ip = ip.substring(0, ip.length() - 1);
            else if (ip.length() < 80 && (Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == ':')) ip += c;
            return true;
        }
        if (!teclas.add(k)) return true;
        if (k == VK_ESCAPE) {
            if (vista == Vista.BUSCAR && (manual || elegida != null)) listar();
            else if (!botones.isEmpty()) volver.run();
        } else if (!botones.isEmpty()) {
            if (k == VK_UP || k == VK_LEFT) foco = Math.floorMod(foco - 1, botones.size());
            if (k == VK_DOWN || k == VK_RIGHT || k == VK_TAB) foco = (foco + 1) % botones.size();
            if (k == VK_ENTER || k == VK_E || k == VK_SPACE) activar(botones.get(foco));
        }
        return true;
    }

    public void soltar(int k) { teclas.remove(k); }
    public boolean raton(int x, int y) {
        if (!activa()) return false;
        for (Boton b : botones) if (b.contiene(x, y)) {
            activar(b);
            break;
        }
        return true;
    }

    private void activar(Boton b) {
        InterfazSonora.activar("click");
        b.accion.run();
    }

    public boolean mando(EstadoMando actual) {
        EstadoMando previo = anterior;
        anterior = actual;
        if (!activa()) return false;
        if (actual.pulsado(EstadoMando.Boton.B) && !previo.pulsado(EstadoMando.Boton.B)) toque(VK_ESCAPE);
        else if (actual.pulsado(EstadoMando.Boton.A) && !previo.pulsado(EstadoMando.Boton.A)) toque(VK_ENTER);
        else if (actual.pulsado(EstadoMando.Boton.ARRIBA) && !previo.pulsado(EstadoMando.Boton.ARRIBA)
                || actual.izquierdaY() < -.6 && previo.izquierdaY() >= -.6) toque(VK_UP);
        else if (actual.pulsado(EstadoMando.Boton.ABAJO) && !previo.pulsado(EstadoMando.Boton.ABAJO)
                || actual.izquierdaY() > .6 && previo.izquierdaY() <= .6) toque(VK_DOWN);
        return true;
    }

    private void toque(int k) { pulsar((char) 0, k); soltar(k); }

    @Override public void close() {
        if (busqueda != null) busqueda.close();
        busqueda = null;
        vista = null;
        botones.clear();
        teclas.clear();
    }
}
