package alcaldedigital.servidor.carrera;

import alcaldedigital.compartido.carrera.EstadoCarrera;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReferenceArray;

/** Sala competitiva TCP. Ningún cliente decide posiciones, colisiones ni puntuaciones. */
public final class ServidorCarrera implements AutoCloseable {
    private final ServerSocket servidor;private final MotorCarrera motor;private final GuardadoCarrera guardado;private final RepositorioCarrera repo;
    private final AtomicReferenceArray<Socket> roles=new AtomicReferenceArray<>(2);
    private final java.util.Set<Socket> sockets=ConcurrentHashMap.newKeySet();
    private final ExecutorService lectores=Executors.newCachedThreadPool(r->{var t=new Thread(r,"m7-red");t.setDaemon(true);return t;});
    private final ScheduledExecutorService reloj=Executors.newSingleThreadScheduledExecutor(r->{var t=new Thread(r,"m7-simulacion");t.setDaemon(true);return t;});
    private volatile boolean cerrado;private volatile String error="";private int pasos;private volatile boolean corrupcionGuardada,acreditada;
    public ServidorCarrera(InetAddress direccion,int puerto,MotorCarrera m,GuardadoCarrera g,RepositorioCarrera repo)throws IOException{
        if(m.vista().humanos()!=3)throw new IllegalArgumentException("LAN requiere dos roles humanos");motor=m;guardado=g;this.repo=repo;
        servidor=new ServerSocket();servidor.setReuseAddress(true);servidor.bind(new InetSocketAddress(direccion,puerto));for(int r=0;r<2;r++)m.conectar(r,false);
        lectores.execute(this::aceptar);reloj.scheduleAtFixedRate(this::paso,0,16666667,TimeUnit.NANOSECONDS);
    }
    public int puerto(){return servidor.getLocalPort();}public MotorCarrera motor(){return motor;}
    private void paso(){try{var antes=motor.vista();motor.paso();var v=motor.vista();if(++pasos%600==0||antes.fase()!=v.fase()||antes.historial().size()!=v.historial().size()||v.electoral().juanCorrupto()&&!corrupcionGuardada||v.fase()==EstadoCarrera.Fase.RESULTADO&&!acreditada)guardar();
    }catch(Exception e){error="La sala se detuvo: "+e.getMessage();motor.orden(0,0,false,"PAUSA",0);}}
    public synchronized void guardar(){try{var v=motor.vista();if(guardado!=null)guardado.guardar(motor);
        if(repo!=null&&v.electoral().juanCorrupto()&&!corrupcionGuardada){repo.registrarCorrupcion(v.electoral());corrupcionGuardada=true;}
        if(repo!=null&&!acreditada&&v.fase()==EstadoCarrera.Fase.RESULTADO){repo.acreditar(v,new alcaldedigital.compartido.modelo.EstadoCiudad(v.base()[0],v.base()[1],v.base()[2],v.base()[3],v.base()[4],v.base()[5],v.base()[6]));acreditada=true;}
    }catch(IOException e){error="Guardado: "+e.getMessage();}}
    private void aceptar(){while(!cerrado)try{Socket s=servidor.accept();if(cerrado){s.close();break;}s.setTcpNoDelay(true);s.setSoTimeout(8000);sockets.add(s);try{lectores.execute(()->atender(s));}catch(RejectedExecutionException e){sockets.remove(s);s.close();}}catch(IOException e){if(!cerrado)error=e.getMessage();}}
    private void atender(Socket socket){int rol=-1;boolean asignado=false;try(socket;var in=new DataInputStream(new BufferedInputStream(socket.getInputStream()));var out=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))){
        if(in.readInt()!=EstadoCarrera.MAGIA||in.readInt()!=EstadoCarrera.VERSION)throw new IOException("Protocolo incompatible");rol=in.readInt();
        if(rol<0||rol>1||!roles.compareAndSet(rol,null,socket)){out.writeBoolean(false);out.writeUTF("Rol ocupado o desconocido");out.flush();return;}
        asignado=true;motor.conectar(rol,true);out.writeBoolean(true);out.flush();long secuencia=-1;
        while(!cerrado){var a=EstadoCarrera.orden(in);if(a.secuencia()>secuencia){secuencia=a.secuencia();if(a.rol()==rol){if(a.accion().equals("GUARDAR"))guardar();else{var antes=motor.vista();motor.orden(rol,a.vertical(),a.correr(),a.accion(),a.valor());var despues=motor.vista();if(antes.fase()!=despues.fase()||antes.historial().size()!=despues.historial().size()||antes.pausa()!=despues.pausa())guardar();}}}
            EstadoCarrera.escribir(out,motor.vista());out.writeUTF(error);out.flush();}
    }catch(IOException e){/* La desconexión conserva y pausa la carrera. */}finally{sockets.remove(socket);if(asignado&&roles.compareAndSet(rol,socket,null)){motor.conectar(rol,false);guardar();}}}
    @Override public void close(){synchronized(this){if(cerrado)return;cerrado=true;}reloj.shutdownNow();try{servidor.close();}catch(IOException ignored){}for(var s:sockets)try{s.close();}catch(IOException ignored){}lectores.shutdownNow();try{reloj.awaitTermination(2,TimeUnit.SECONDS);lectores.awaitTermination(2,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}guardar();}
}
