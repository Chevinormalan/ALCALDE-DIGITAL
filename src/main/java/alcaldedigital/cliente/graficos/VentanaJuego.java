package alcaldedigital.cliente.graficos;

import processing.awt.PSurfaceAWT;
import processing.awt.SuperficiePantallaCompleta;
import processing.core.PApplet;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

/** Aplica la geometría en AWT, antes de que Processing prepare el siguiente fotograma. */
public final class VentanaJuego {
    private record VentanaNormal(Dimension contenido, Point posicion, String monitor) {}

    private final PApplet app;
    private final OpcionesGraficas opciones;
    private long aplicada = -1;
    private long revisionFallida = -1;
    private boolean pendiente = true;
    private boolean recuperando;
    private OpcionesGraficas.Configuracion previa;
    private Rectangle pantallaAnterior;
    private GraphicsDevice pantallaCompletaNativa;
    private VentanaNormal ventanaNormal;
    private OpcionesGraficas.Tamano preajusteAlEntrar;
    private int estadoVentana;
    private Insets bordesVentana = new Insets(0, 0, 0, 0);

    public VentanaJuego(PApplet app, OpcionesGraficas opciones) {
        this.app = app;
        this.opciones = opciones;
    }

    public void refrescar() {
        pendiente = true;
    }

    public void actualizar() {
        long revision = opciones.revision();
        if ((!pendiente && aplicada == revision) || revisionFallida == revision) return;

        var nueva = opciones.actual();
        try {
            EventQueue.invokeAndWait(() -> aplicar(nueva));
            previa = nueva;
            aplicada = revision;
            pendiente = false;
            recuperando = false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            System.err.println("No se pudo cambiar la ventana: " + e.getCause());
            if (recuperando) {
                revisionFallida = revision;
            } else {
                opciones.recuperarVentana();
                previa = null;
                recuperando = true;
                pendiente = true;
            }
        }
    }

    private void aplicar(OpcionesGraficas.Configuracion nueva) {
        var canvas = (PSurfaceAWT.SmoothCanvas) app.getSurface().getNative();
        Frame frame = canvas.getFrame();
        GraphicsConfiguration monitor = pantallaCompletaNativa == null ? frame.getGraphicsConfiguration()
            : pantallaCompletaNativa.getDefaultConfiguration();

        if (previa == null || !previa.completa()) {
            bordesVentana = frame.getInsets();
            estadoVentana = frame.getExtendedState() & Frame.MAXIMIZED_BOTH;
            if (frame.getExtendedState() == Frame.NORMAL) {
                ventanaNormal = new VentanaNormal(canvas.getSize(), frame.getLocation(), monitor.getDevice().getIDstring());
            }
        }

        boolean cambioModo = previa == null || nueva.completa() != previa.completa();
        boolean cambioTamano = previa == null || !nueva.ventana().equals(previa.ventana());
        if (cambioModo && nueva.completa()) {
            preajusteAlEntrar = previa == null ? null : previa.ventana();
        }
        GraphicsConfiguration monitorAnterior = null;
        if (cambioModo && !nueva.completa() && nueva.ventana().equals(preajusteAlEntrar) && ventanaNormal != null) {
            for (var dispositivo : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                if (dispositivo.getIDstring().equals(ventanaNormal.monitor())) {
                    monitorAnterior = dispositivo.getDefaultConfiguration();
                    break;
                }
            }
        }
        if (monitorAnterior != null) monitor = monitorAnterior;
        Rectangle pantalla = monitor.getBounds();
        Rectangle util = quitarBordes(pantalla, Toolkit.getDefaultToolkit().getScreenInsets(monitor));
        if (cambioModo || (nueva.completa() ? !pantalla.equals(pantallaAnterior) : cambioTamano)) {
            ajustarVentana(frame, canvas, nueva, monitor, util, monitorAnterior != null);
        }
        pantallaAnterior = pantalla;

        Rectangle contenido = quitarBordes(util, bordesVentana);
        opciones.detectar(pantalla.width, pantalla.height, contenido.width, contenido.height);
    }

    private void ajustarVentana(Frame frame, PSurfaceAWT.SmoothCanvas canvas,
                                 OpcionesGraficas.Configuracion nueva, GraphicsConfiguration monitor,
                                 Rectangle util, boolean volver) {
        GraphicsDevice dispositivo = monitor.getDevice();
        var superficie = (SuperficiePantallaCompleta) app.getSurface();
        if (nueva.completa() && PApplet.platform == PApplet.LINUX && dispositivo.isFullScreenSupported()) {
            // El compositor conserva el monitor al reutilizar la ventana X11.
            frame.setResizable(true);
            pantallaCompletaNativa = dispositivo;
            dispositivo.setFullScreenWindow(frame);
            superficie.pantallaCompleta(true);
            canvas.requestFocusInWindow();
            return;
        }
        if (pantallaCompletaNativa != null) {
            pantallaCompletaNativa.setFullScreenWindow(null);
            pantallaCompletaNativa = null;
            superficie.pantallaCompleta(false);
        }

        Rectangle pantalla = monitor.getBounds();
        if (frame.isUndecorated() != nueva.completa()) {
            frame.dispose();
            frame.setUndecorated(nueva.completa());
            frame.setLocation(volver ? ventanaNormal.posicion() : pantalla.getLocation());
        }
        frame.setExtendedState(Frame.NORMAL);
        frame.setResizable(!nueva.completa());

        int estado = Frame.NORMAL;
        if (nueva.completa()) {
            app.getSurface().setSize(pantalla.width, pantalla.height);
            frame.setLocation(pantalla.x, pantalla.y);
        } else {
            // El peer decorado da los bordes que la ventana realmente ocupa.
            frame.addNotify();
            bordesVentana = frame.getInsets();
            Rectangle contenido = quitarBordes(util, bordesVentana);
            Dimension solicitada = volver ? ventanaNormal.contenido()
                : new Dimension(nueva.ventana().ancho(), nueva.ventana().alto());
            Dimension tamano = encajar(solicitada, contenido);
            app.getSurface().setSize(tamano.width, tamano.height);
            Point posicion = volver ? ventanaNormal.posicion() : new Point(
                util.x + (util.width - frame.getWidth()) / 2,
                util.y + (util.height - frame.getHeight()) / 2
            );
            frame.setLocation(
                Math.clamp(posicion.x, util.x, util.x + Math.max(0, util.width - frame.getWidth())),
                Math.clamp(posicion.y, util.y, util.y + Math.max(0, util.height - frame.getHeight()))
            );
            if (volver) estado = estadoVentana;
        }

        frame.setVisible(true);
        if (estado != Frame.NORMAL) frame.setExtendedState(estado);
        canvas.requestFocusInWindow();
    }

    private static Rectangle quitarBordes(Rectangle area, Insets bordes) {
        return new Rectangle(area.x + bordes.left, area.y + bordes.top,
            Math.max(1, area.width - bordes.left - bordes.right),
            Math.max(1, area.height - bordes.top - bordes.bottom));
    }

    private static Dimension encajar(Dimension solicitada, Rectangle disponible) {
        int ancho = Math.max(1, solicitada.width);
        int alto = Math.max(1, solicitada.height);
        if (ancho <= disponible.width && alto <= disponible.height) return new Dimension(ancho, alto);

        if ((long) disponible.width * alto <= (long) disponible.height * ancho) {
            return new Dimension(disponible.width, Math.max(1, (int) ((long) disponible.width * alto / ancho)));
        }
        return new Dimension(Math.max(1, (int) ((long) disponible.height * ancho / alto)), disponible.height);
    }
}
