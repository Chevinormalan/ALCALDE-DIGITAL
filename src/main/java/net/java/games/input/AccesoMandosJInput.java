package net.java.games.input;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Abre y libera dispositivos concretos de JInput 2.0.10 sin reinstanciar su entorno global. */
public final class AccesoMandosJInput {
    private AccesoMandosJInput() {}

    public record Dispositivo(Controller control, AutoCloseable recurso) implements AutoCloseable {
        @Override public void close() {
            try { recurso.close(); }
            catch (Exception e) { System.err.println("No se pudo cerrar un mando: " + e.getMessage()); }
        }
    }

    public static Dispositivo abrirLinux(Path ruta, boolean joystick) throws IOException {
        if (joystick) {
            var dispositivo = new LinuxJoystickDevice(ruta.toString());
            try { return new Dispositivo(FabricasLinux.crear(FabricasLinux.JOYSTICK, dispositivo), dispositivo); }
            catch (IOException | RuntimeException e) { dispositivo.close(); throw e; }
        }
        var dispositivo = new LinuxEventDevice(ruta.toString());
        try { return new Dispositivo(FabricasLinux.crear(FabricasLinux.EVENTO, dispositivo), dispositivo); }
        catch (IOException | RuntimeException e) { dispositivo.close(); throw e; }
    }

    public static void iniciarLinux() throws ClassNotFoundException {
        // Ejecuta el cargador JNI del plugin, sin abrir teclados ni ratones.
        Class.forName("net.java.games.input.LinuxEnvironmentPlugin");
    }

    private static class FabricasLinux {
        private static final Method EVENTO = fabrica("createControllerFromDevice", LinuxEventDevice.class);
        private static final Method JOYSTICK = fabrica("createJoystickFromJoystickDevice", LinuxJoystickDevice.class);

        private static Method fabrica(String nombre, Class<?> tipo) {
            try {
                Method metodo = LinuxEnvironmentPlugin.class.getDeclaredMethod(nombre, tipo);
                metodo.setAccessible(true);
                return metodo;
            } catch (ReflectiveOperationException e) { throw new IllegalStateException("Adaptador de JInput incompatible", e); }
        }

        private static Controller crear(Method metodo, Object dispositivo) throws IOException {
            try {
                var control = (Controller) metodo.invoke(null, dispositivo);
                if (control == null) throw new IOException("Dispositivo de entrada sin controles de juego");
                return control;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IOException("No se pudo abrir el mando", e);
            }
        }
    }

    public static final class Windows implements AutoCloseable {
        private final DummyWindow ventana;

        public Windows() throws IOException, ClassNotFoundException {
            Class.forName("net.java.games.input.DirectInputEnvironmentPlugin");
            ventana = new DummyWindow();
        }

        public List<Dispositivo> enumerar() throws IOException {
            var entrada = new IDirectInput(ventana);
            List<Dispositivo> controles = new ArrayList<>();
            try {
                for (IDirectInputDevice dispositivo : entrada.getDevices()) {
                    int tipo = dispositivo.getType();
                    if (tipo != IDirectInputDevice.DI8DEVTYPE_GAMEPAD && tipo != IDirectInputDevice.DI8DEVTYPE_JOYSTICK) {
                        dispositivo.close();
                        continue;
                    }
                    List<Component> componentes = new ArrayList<>();
                    for (DIDeviceObject objeto : dispositivo.getObjects()) {
                        if (objeto.getIdentifier() == null) continue;
                        var componente = new DIComponent(objeto.getIdentifier(), objeto);
                        dispositivo.registerComponent(objeto, componente);
                        componentes.add(componente);
                    }
                    var clase = tipo == IDirectInputDevice.DI8DEVTYPE_GAMEPAD ? Controller.Type.GAMEPAD : Controller.Type.STICK;
                    var mando = new DIAbstractController(dispositivo, componentes.toArray(Component[]::new),
                            new Controller[0], dispositivo.getRumblers(), clase);
                    controles.add(new Dispositivo(mando, dispositivo));
                }
                return controles;
            } catch (RuntimeException | LinkageError e) {
                entrada.releaseDevices();
                throw e;
            } finally { entrada.release(); }
        }

        @Override public void close() throws IOException { ventana.destroy(); }
    }
}
