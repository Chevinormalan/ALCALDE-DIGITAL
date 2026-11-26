package alcaldedigital.servidor.batalla;

import alcaldedigital.compartido.red.DescubrimientoLan;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import alcaldedigital.compartido.batalla.EstadoBatalla;
import alcaldedigital.compartido.batalla.EstadoBatalla.*;

/** Servidor TCP de sala. La única misión habilitada en esta entrega es la 5. */
public final class ServidorBatalla implements AutoCloseable {
    private final ServerSocket servidor;
    private final MotorBatalla motor;
    private final GuardadoBatalla guardado;
    private final AtomicReferenceArray<Socket> conexiones=new AtomicReferenceArray<>(2);
    private final ScheduledExecutorService reloj=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"m5-simulacion");t.setDaemon(true);return t;});
    private final ExecutorService lectores=Executors.newCachedThreadPool(r->{Thread t=new Thread(r,"m5-red");t.setDaemon(true);return t;});
    private final java.util.Set<Socket> sockets=ConcurrentHashMap.newKeySet();
    private final boolean inicioAutomatico;
    private volatile boolean iniciada;
    private DescubrimientoLan.Anuncio anuncio;
    private volatile boolean cerrado;private volatile String errorGuardado="";private int pasos;
    public ServidorBatalla(InetAddress interfaz,int puerto,MotorBatalla motor,GuardadoBatalla guardado)throws IOException{
        this(interfaz,puerto,motor,guardado,false);
    }
    public ServidorBatalla(InetAddress interfaz,int puerto,MotorBatalla motor,GuardadoBatalla guardado,boolean vestibulo)throws IOException{
        inicioAutomatico=vestibulo;
        this.motor=motor;this.guardado=guardado;servidor=new ServerSocket();servidor.setReuseAddress(true);servidor.bind(new InetSocketAddress(interfaz,puerto));
        motor.conectar(0,"Ciudadano",false);motor.conectar(1,"Periodista",false);
        if(inicioAutomatico){motor.orden(0,0,0,false,"PAUSA",0);}
        lectores.execute(this::aceptar);
        reloj.scheduleAtFixedRate(()->{try{var antes=motor.vista().fase();motor.paso();pasos++;if(pasos%600==0||antes!=motor.vista().fase())guardar();}catch(Exception e){errorGuardado=e.getMessage();}},0,16666667,TimeUnit.NANOSECONDS);
    }
    public void anunciar() throws IOException {
        anuncio = new DescubrimientoLan.Anuncio(5, puerto(), () -> {
            synchronized (motor) {
                if (iniciada || cerrado) return null;
                var jugadores = motor.vista().jugadores();
                int ocupados = (jugadores[0].conectado() ? 1 : 0) | (jugadores[1].conectado() ? 2 : 0);
                return ocupados == 0 ? null : new DescubrimientoLan.Cupos(2, ocupados, motor.vista().dificultad().nombre());
            }
        });
    }

    private void iniciarSiCompleta() {
        if (!inicioAutomatico || iniciada || conexiones.get(0) == null || conexiones.get(1) == null) return;
        motor.orden(0,0,0,false,"REANUDAR",0);
        motor.orden(0,0,0,false,"LISTO",0);
        motor.orden(1,0,0,false,"LISTO",0);
        iniciada = true;
    }

    public int puerto(){return servidor.getLocalPort();}
    public MotorBatalla motor(){return motor;}
    public String errorGuardado(){return errorGuardado;}
    public void guardar(){if(guardado!=null&&(!inicioAutomatico||iniciada))try{guardado.guardar(motor);errorGuardado="";}catch(IOException e){errorGuardado="No se pudo guardar: "+e.getMessage();}}
    private void aceptar(){while(!cerrado)try{Socket s=servidor.accept();if(cerrado){s.close();break;}s.setTcpNoDelay(true);s.setSoTimeout(8000);sockets.add(s);try{lectores.execute(()->atender(s));}catch(RejectedExecutionException cancelada){sockets.remove(s);s.close();}}catch(IOException e){if(!cerrado)errorGuardado=e.getMessage();}}
    private void atender(Socket socket){int rol=-1;boolean asignado=false;
        try(socket;var in=new DataInputStream(new BufferedInputStream(socket.getInputStream()));var out=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))){
            if(in.readInt()!=0x43495635||in.readInt()!=EstadoBatalla.VERSION)throw new IOException("Protocolo incompatible");int mision=in.readInt();rol=in.readInt();String nombre=in.readUTF();
            synchronized (motor) {
                if(mision!=5||rol<0||rol>1||!conexiones.compareAndSet(rol,null,socket)){out.writeBoolean(false);out.writeUTF("Misión no habilitada o rol ocupado");out.flush();return;}
                asignado=true;
                motor.conectar(rol,nombre,true);
                iniciarSiCompleta();
            }
            out.writeBoolean(true);out.writeUTF("Ciudad Nova · rol "+rol);out.flush();long secuencia=-1;
            while(!cerrado){Orden o=EstadoBatalla.leerOrden(in);if(o.secuencia()>secuencia){secuencia=o.secuencia();if(o.rol()==rol){if(o.accion().equals("GUARDAR"))guardar();else motor.orden(rol,o.dx(),o.dy(),o.salto(),o.accion(),o.valor());}}
                EstadoBatalla.escribir(out,motor.vista());out.writeUTF(errorGuardado);out.flush();}
        }catch(IOException e){/* Cerrar una conexión pausa la sala; el otro cliente conserva la vista. */}
        finally{
            sockets.remove(socket);
            if(asignado){
                synchronized(motor){if(conexiones.compareAndSet(rol,socket,null)){motor.conectar(rol,"",false);}}
                if(!cerrado)guardar();
            }
        }
    }
    @Override public synchronized void close(){
        if(cerrado)return;cerrado=true;if(anuncio!=null)anuncio.close();reloj.shutdownNow();
        try{servidor.close();}catch(IOException ignored){}
        for(Socket s:sockets)try{s.close();}catch(IOException ignored){}
        lectores.shutdownNow();
        try{reloj.awaitTermination(2,TimeUnit.SECONDS);lectores.awaitTermination(2,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}
        guardar();
    }
}
