package alcaldedigital.cliente.mision7;

import alcaldedigital.compartido.carrera.EstadoCarrera;
import alcaldedigital.compartido.carrera.EstadoCarrera.*;
import java.net.*;import java.io.*;import java.util.concurrent.*;

public final class ClienteCarrera implements CanalCarrera {
    private record Accion(String nombre,int valor){}
    private final Socket socket;private final DataInputStream in;private final DataOutputStream out;private final int rol;
    private final BlockingQueue<Accion> acciones=new LinkedBlockingQueue<>(64);private volatile Vista vista;private volatile String error="";
    private volatile boolean cerrado,correr;private volatile float vertical;private long secuencia;private final Thread hilo;
    public ClienteCarrera(String host,int puerto,int rol)throws IOException{
        this.rol=rol;socket=new Socket();try{socket.connect(new InetSocketAddress(host,puerto),2500);socket.setTcpNoDelay(true);socket.setSoTimeout(4000);
        in=new DataInputStream(new BufferedInputStream(socket.getInputStream()));out=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        out.writeInt(EstadoCarrera.MAGIA);out.writeInt(EstadoCarrera.VERSION);out.writeInt(rol);out.flush();if(!in.readBoolean())throw new IOException(in.readUTF());intercambiar(new Accion("",0));}catch(IOException e){socket.close();throw e;}
        hilo=new Thread(this::bucle,"m7-cliente-"+rol);hilo.setDaemon(true);hilo.start();
    }
    private void intercambiar(Accion a)throws IOException{EstadoCarrera.orden(out,new Orden(++secuencia,rol,vertical,correr,a.nombre,a.valor));out.flush();vista=EstadoCarrera.leer(in);error=in.readUTF();}
    private void bucle(){try{while(!cerrado){Accion a=acciones.poll();intercambiar(a==null?new Accion("",0):a);Thread.sleep(25);}}catch(Exception e){if(!cerrado)error="Conexión perdida: "+e.getMessage();cerrado=true;try{socket.close();}catch(IOException ignored){}}}
    public Vista vista(){return vista;}public String error(){return error;}
    public void movimiento(int r,float v,boolean correr){if(r==rol){vertical=v;this.correr=correr;}}
    public void accion(int r,String nombre,int valor){if(r==rol)acciones.offer(new Accion(nombre,valor));}
    public void close(){cerrado=true;try{socket.close();}catch(IOException ignored){}hilo.interrupt();}
}
