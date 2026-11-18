package alcaldedigital.servidor.batalla;

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
    private volatile boolean cerrado;private volatile String errorGuardado="";private int pasos;
    public ServidorBatalla(InetAddress interfaz,int puerto,MotorBatalla motor,GuardadoBatalla guardado)throws IOException{
        this.motor=motor;this.guardado=guardado;servidor=new ServerSocket();servidor.setReuseAddress(true);servidor.bind(new InetSocketAddress(interfaz,puerto));
        motor.conectar(0,"Ciudadano",false);motor.conectar(1,"Periodista",false);
        lectores.execute(this::aceptar);
        reloj.scheduleAtFixedRate(()->{try{var antes=motor.vista().fase();motor.paso();pasos++;if(pasos%600==0||antes!=motor.vista().fase())guardar();}catch(Exception e){errorGuardado=e.getMessage();}},0,16666667,TimeUnit.NANOSECONDS);
    }
    public int puerto(){return servidor.getLocalPort();}
    public MotorBatalla motor(){return motor;}
    public String errorGuardado(){return errorGuardado;}
    public void guardar(){if(guardado!=null)try{guardado.guardar(motor);errorGuardado="";}catch(IOException e){errorGuardado="No se pudo guardar: "+e.getMessage();}}
    private void aceptar(){while(!cerrado)try{Socket s=servidor.accept();if(cerrado){s.close();break;}s.setTcpNoDelay(true);s.setSoTimeout(8000);sockets.add(s);try{lectores.execute(()->atender(s));}catch(RejectedExecutionException cancelada){sockets.remove(s);s.close();}}catch(IOException e){if(!cerrado)errorGuardado=e.getMessage();}}
    private void atender(Socket socket){int rol=-1;boolean asignado=false;
        try(socket;var in=new DataInputStream(new BufferedInputStream(socket.getInputStream()));var out=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))){
            if(in.readInt()!=0x43495635||in.readInt()!=EstadoBatalla.VERSION)throw new IOException("Protocolo incompatible");int mision=in.readInt();rol=in.readInt();String nombre=in.readUTF();
            if(mision!=5||rol<0||rol>1||!conexiones.compareAndSet(rol,null,socket)){out.writeBoolean(false);out.writeUTF("Misión no habilitada o rol ocupado");out.flush();return;}
            asignado=true;motor.conectar(rol,nombre,true);out.writeBoolean(true);out.writeUTF("Ciudad Nova · rol "+rol);out.flush();long secuencia=-1;
            while(!cerrado){Orden o=EstadoBatalla.leerOrden(in);if(o.secuencia()>secuencia){secuencia=o.secuencia();if(o.rol()==rol){if(o.accion().equals("GUARDAR"))guardar();else motor.orden(rol,o.dx(),o.dy(),o.salto(),o.accion(),o.valor());}}
                EstadoBatalla.escribir(out,motor.vista());out.writeUTF(errorGuardado);out.flush();}
        }catch(IOException e){/* Cerrar una conexión pausa la sala; el otro cliente conserva la vista. */}
        finally{sockets.remove(socket);if(asignado&&conexiones.compareAndSet(rol,socket,null)){motor.conectar(rol,"",false);guardar();}}
    }
    @Override public synchronized void close(){
        if(cerrado)return;cerrado=true;reloj.shutdownNow();
        try{servidor.close();}catch(IOException ignored){}
        for(Socket s:sockets)try{s.close();}catch(IOException ignored){}
        lectores.shutdownNow();
        try{reloj.awaitTermination(2,TimeUnit.SECONDS);lectores.awaitTermination(2,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}
        guardar();
    }
}
