package alcaldedigital.cliente.mision6;

import alcaldedigital.compartido.guardado.ArchivosPartida;
import alcaldedigital.compartido.red.DescubrimientoLan;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.awt.event.KeyEvent.*;

/** El anfitrión ejecuta la simulación. El invitado envía únicamente los controles de la segunda nave. */
public final class RedSeis implements AutoCloseable {
    public static final int PUERTO = 48506;
    private static final int MAGIA = 0x43495636, VERSION = 1;
    private static final int MAX_SNAPSHOT = 131072;
    private static final int NO_OP = 0, PAUSAR = 1, REANUDAR = 2, GUARDAR = 3;
    private static final int[] TECLAS_INVITADO = {VK_LEFT, VK_RIGHT, VK_UP, VK_DOWN, VK_L};
    public static final int IZQUIERDA = 1, DERECHA = 2, ARRIBA = 4, ABAJO = 8, DISPARO = 16, MISIL = 32, IMPULSO = 64;

    public record Vista(MisionSeisMotor motor, boolean iniciada, boolean pausada, boolean invitado,
                        boolean caminando, String errorGuardado) {
        public int ocupados() { return invitado ? 3 : 1; }
    }

    private final MisionSeisMotor autoridad;
    private final AutoguardadoSeis guardado;
    private final ServerSocket servidor;
    private final Socket cliente;
    private final BlockingQueue<Integer> ordenesRemotas = new ArrayBlockingQueue<>(16);
    private final AtomicInteger accionesInvitado = new AtomicInteger();
    private volatile int entradaInvitado;
    private volatile boolean cerrado;
    private final ExecutorService red = Executors.newCachedThreadPool(r -> hilo(r, "m6-red"));
    private final ScheduledExecutorService reloj = Executors.newSingleThreadScheduledExecutor(r -> hilo(r, "m6-reloj"));
    private final java.util.Set<Socket> sockets = ConcurrentHashMap.newKeySet();
    private DescubrimientoLan.Anuncio anuncio;
    private Socket invitado;
    private volatile Vista vista;
    private boolean iniciada, pausa, cierreEntregado;
    private String token = "";
    private volatile String error = "";
    private int pasos;

    private RedSeis(MisionSeisMotor motor, AutoguardadoSeis guardado, ServerSocket servidor, Socket cliente) {
        this.autoridad = motor;
        this.guardado = guardado;
        this.servidor = servidor;
        this.cliente = cliente;
    }

    public static Path archivo() { return ArchivosPartida.directorio().resolve("mision6-lan.properties"); }

    public static RedSeis alojar(int puerto, boolean continuar) throws IOException {
        var guardado = new AutoguardadoSeis(archivo());
        try {
            MisionSeisMotor motor = continuar ? MisionSeisMotor.restaurar(guardado.cargar()) : new MisionSeisMotor();
            if (motor == null || continuar && motor.modo != MisionSeisMotor.Modo.DUO) throw new IOException("No hay un guardado LAN válido.");
            return alojar(puerto, motor, guardado);
        } catch (IOException | RuntimeException e) {
            guardado.close();
            throw e;
        }
    }

    static RedSeis alojar(int puerto, MisionSeisMotor motor, AutoguardadoSeis guardado) throws IOException {
        var servidor = new ServerSocket();
        try { servidor.bind(new InetSocketAddress(puerto)); }
        catch (IOException e) { servidor.close(); throw e; }
        motor.modo = MisionSeisMotor.Modo.DUO;
        var sala = new RedSeis(motor, guardado, servidor, null);
        sala.red.execute(sala::aceptar);
        sala.reloj.scheduleAtFixedRate(sala::paso, 0, 16_666_667, TimeUnit.NANOSECONDS);
        return sala;
    }

    public void anunciar() throws IOException {
        anuncio = new DescubrimientoLan.Anuncio(6, puerto(), () -> {
            synchronized (autoridad) {
                return cerrado || iniciada ? null : new DescubrimientoLan.Cupos(2, 1, "Cooperativo");
            }
        });
    }

    public int puerto() { return servidor.getLocalPort(); }
    public String token() { return token; }
    public String error() { return error; }
    public boolean anfitrion() { return autoridad != null; }

    public boolean cierreConfirmado() {
        if (!anfitrion()) return true;
        synchronized (autoridad) { return cierreEntregado || invitado == null; }
    }

    public Vista vista() {
        if (!anfitrion()) return vista;
        synchronized (autoridad) { return leerVista(instantanea()); }
    }

    private void aceptar() {
        while (!cerrado) {
            try {
                Socket socket = servidor.accept();
                sockets.add(socket);
                if (cerrado) { socket.close(); sockets.remove(socket); break; }
                try { red.execute(() -> atender(socket)); }
                catch (RejectedExecutionException e) { socket.close(); sockets.remove(socket); }
            } catch (IOException e) {
                if (!cerrado) error = "Se interrumpió la escucha de la sala.";
                return;
            }
        }
    }

    private void atender(Socket socket) {
        try (socket; var in = new DataInputStream(socket.getInputStream()); var out = new DataOutputStream(socket.getOutputStream())) {
            socket.setSoTimeout(4000);
            socket.setTcpNoDelay(true);
            if (in.readInt() != MAGIA || in.readInt() != VERSION) throw new IOException("Protocolo de misión 6 incompatible");
            String reserva = in.readUTF();
            boolean admitido;
            synchronized (autoridad) {
                admitido = invitado == null && (!iniciada || token.equals(reserva));
                if (admitido) {
                    invitado = socket;
                    if (!iniciada) {
                        token = UUID.randomUUID().toString();
                        iniciada = true;
                    }
                }
            }
            out.writeBoolean(admitido);
            out.writeUTF(admitido ? token : "Puesto ocupado o reservado");
            out.flush();
            if (!admitido) return;
            while (!cerrado) {
                int entradas = in.readUnsignedByte();
                int orden = in.readUnsignedByte();
                Properties estado;
                synchronized (autoridad) {
                    ejecutar(orden);
                    if (!pausa && (autoridad.fase == MisionSeisMotor.Fase.ENTRENAMIENTO || autoridad.fase == MisionSeisMotor.Fase.COMBATE)) {
                        entradaSegundoJugador(entradas);
                    }
                    estado = instantanea();
                }
                escribir(out, estado);
                if (bandera(estado, "terminado")) synchronized (autoridad) { cierreEntregado = true; }
            }
        } catch (IOException e) {
            // El bucle de simulación conserva la partida cuando queda un puesto ausente.
        } finally {
            sockets.remove(socket);
            synchronized (autoridad) {
                if (invitado == socket) {
                    invitado = null;
                    ejecutar(PAUSAR);
                    if (!cerrado) programarGuardado();
                }
            }
        }
    }

    public static RedSeis unir(String ip, int puerto, String token) throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, puerto), 2500);
            socket.setSoTimeout(4000);
            socket.setTcpNoDelay(true);
            var in = new DataInputStream(socket.getInputStream());
            var out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(MAGIA);
            out.writeInt(VERSION);
            out.writeUTF(token);
            out.flush();
            boolean aceptado = in.readBoolean();
            String respuesta = in.readUTF();
            if (!aceptado) throw new IOException(respuesta);
            var enlace = new RedSeis(null, null, null, socket);
            enlace.token = respuesta;
            enlace.red.execute(() -> enlace.intercambiar(in, out));
            return enlace;
        } catch (IOException e) {
            socket.close();
            throw e;
        }
    }

    private void intercambiar(DataInputStream in, DataOutputStream out) {
        try (cliente; in; out) {
            while (!cerrado) {
                out.writeByte(entradaInvitado | accionesInvitado.getAndSet(0));
                Integer orden = ordenesRemotas.poll();
                out.writeByte(orden == null ? NO_OP : orden);
                out.flush();
                vista = leerVista(leer(in));
                Thread.sleep(25);
            }
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            if (!cerrado) error = "Conexión perdida con el anfitrión.";
        }
    }

    private void paso() {
        synchronized (autoridad) {
            if (!iniciada || pausa || cerrado) return;
            var anterior = autoridad.fase;
            autoridad.actualizar(1f / 60);
            if (autoridad.fase == MisionSeisMotor.Fase.MODO) autoridad.comenzar(MisionSeisMotor.Modo.DUO);
            if (++pasos % 120 == 0 || anterior != autoridad.fase) programarGuardado();
        }
    }

    public void teclaAnfitrion(int tecla, boolean pulsada) {
        if (!anfitrion()) return;
        synchronized (autoridad) {
            if (iniciada && !pausa) {
                if (pulsada) autoridad.pulsar(tecla); else autoridad.soltar(tecla);
            }
        }
    }

    public void movimientoInvitado(int mascara) { entradaInvitado = mascara & 31; }
    public void especialInvitado(int accion) { accionesInvitado.getAndUpdate(a -> a | (accion & 96)); }

    private void entradaSegundoJugador(int mascara) {
        for (int i = 0; i < TECLAS_INVITADO.length; i++) {
            int tecla = TECLAS_INVITADO[i];
            if ((mascara & (1 << i)) != 0) autoridad.pulsar(tecla); else autoridad.soltar(tecla);
        }
        if ((mascara & MISIL) != 0) { autoridad.pulsar(VK_K); autoridad.soltar(VK_K); }
        if ((mascara & IMPULSO) != 0) { autoridad.pulsar(VK_SHIFT); autoridad.soltar(VK_SHIFT); }
    }

    public void pausar() { orden(PAUSAR); }
    public void reanudar() { orden(REANUDAR); }
    public void guardar() throws Exception {
        orden(GUARDAR);
        if (guardado != null) guardado.esperar();
    }

    private void orden(int orden) {
        movimientoInvitado(0);
        accionesInvitado.set(0);
        if (!anfitrion()) { ordenesRemotas.offer(orden); return; }
        synchronized (autoridad) { ejecutar(orden); }
    }

    private void ejecutar(int orden) {
        if (!iniciada) return;
        switch (orden) {
            case PAUSAR -> { pausa = true; autoridad.perderFoco(); }
            case REANUDAR -> { if (invitado != null) { autoridad.perderFoco(); pausa = false; } }
            case GUARDAR -> programarGuardado();
            default -> { }
        }
    }

    private void programarGuardado() {
        if (guardado != null) guardado.guardar(autoridad.capturar());
    }

    private Properties instantanea() {
        Properties p = autoridad.capturar();
        p.setProperty("terminado", Boolean.toString(autoridad.terminado));
        p.setProperty("red.iniciada", Boolean.toString(iniciada));
        p.setProperty("red.pausa", Boolean.toString(pausa));
        p.setProperty("red.invitado", Boolean.toString(invitado != null));
        p.setProperty("red.caminando", Boolean.toString(autoridad.estaCaminando()));
        p.setProperty("red.error", guardado != null && guardado.fallo ? "No se pudo guardar la partida LAN." : "");
        return p;
    }

    private static Vista leerVista(Properties p) {
        MisionSeisMotor motor = MisionSeisMotor.restaurar(p);
        if (motor == null) throw new IllegalArgumentException("Estado inválido de misión 6");
        return new Vista(motor, bandera(p, "red.iniciada"), bandera(p, "red.pausa"),
                bandera(p, "red.invitado"), bandera(p, "red.caminando"), p.getProperty("red.error", ""));
    }

    private static boolean bandera(Properties p, String clave) { return Boolean.parseBoolean(p.getProperty(clave)); }

    private static void escribir(DataOutputStream out, Properties p) throws IOException {
        var bytes = new ByteArrayOutputStream();
        p.store(bytes, null);
        if (bytes.size() > MAX_SNAPSHOT) throw new IOException("Instantánea demasiado grande");
        out.writeInt(bytes.size());
        bytes.writeTo(out);
        out.flush();
    }

    private static Properties leer(DataInputStream in) throws IOException {
        int n = in.readInt();
        if (n <= 0 || n > MAX_SNAPSHOT) throw new IOException("Tamaño de instantánea inválido");
        byte[] bytes = in.readNBytes(n);
        if (bytes.length != n) throw new EOFException();
        var p = new Properties();
        p.load(new ByteArrayInputStream(bytes));
        return p;
    }

    private static Thread hilo(Runnable tarea, String nombre) {
        Thread hilo = new Thread(tarea, nombre);
        hilo.setDaemon(true);
        return hilo;
    }

    @Override public synchronized void close() {
        if (cerrado) return;
        cerrado = true;
        if (anuncio != null) anuncio.close();
        if (servidor != null) cerrar(servidor);
        if (cliente != null) cerrar(cliente);
        for (Socket socket : sockets) cerrar(socket);
        red.shutdownNow();
        reloj.shutdownNow();
        if (guardado != null) {
            synchronized (autoridad) { if (iniciada) programarGuardado(); }
            guardado.close();
        }
    }

    private static void cerrar(Closeable recurso) {
        try { recurso.close(); } catch (IOException ignored) { }
    }
}
