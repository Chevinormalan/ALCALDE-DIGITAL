package alcaldedigital.cliente.mision5;

import alcaldedigital.compartido.batalla.EstadoBatalla;
import alcaldedigital.compartido.batalla.EstadoBatalla.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/** Red fuera del hilo gráfico; cada cliente solo puede enviar acciones de su rol. */
public final class ClienteBatalla implements CanalBatalla {
    private final Socket socket;private final DataInputStream in;private final DataOutputStream out;private final int rol;
    private final BlockingQueue<String> acciones=new LinkedBlockingQueue<>(64);
    private volatile Vista vista;private volatile String error="";private volatile boolean cerrado;
    private volatile float dx,dy;private volatile boolean salto;private long secuencia;
    private final Thread hilo;
    public ClienteBatalla(String host,int puerto,int rol,String nombre)throws IOException{
        this.rol=rol;socket=new Socket();socket.connect(new InetSocketAddress(host,puerto),2500);socket.setTcpNoDelay(true);socket.setSoTimeout(4000);
        in=new DataInputStream(new BufferedInputStream(socket.getInputStream()));out=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        out.writeInt(0x43495635);out.writeInt(EstadoBatalla.VERSION);out.writeInt(5);out.writeInt(rol);out.writeUTF(nombre);out.flush();if(!in.readBoolean()){String e=in.readUTF();socket.close();throw new IOException(e);}in.readUTF();
        intercambiar("",0);hilo=new Thread(this::bucle,"m5-cliente-"+rol);hilo.setDaemon(true);hilo.start();
    }
    private void bucle(){try{while(!cerrado){String a=acciones.poll();String nombre="";int valor=0;if(a!=null){String[] t=a.split(":",2);nombre=t[0];valor=Integer.parseInt(t[1]);}intercambiar(nombre,valor);Thread.sleep(25);}}catch(Exception e){if(!cerrado)error="Conexión perdida: "+e.getMessage();cerrado=true;try{socket.close();}catch(IOException ignored){}}}
    private void intercambiar(String a,int v)throws IOException{EstadoBatalla.escribirOrden(out,new Orden(++secuencia,rol,dx,dy,salto,a,v));out.flush();vista=EstadoBatalla.leer(in);error=in.readUTF();}
    public Vista vista(){return vista;}
    public void movimiento(int r,float x,float y,boolean s){if(r==rol){dx=x;dy=y;salto=s;}}
    public void accion(int r,String a,int v){if(r==rol)acciones.offer(a+":"+v);}
    public String error(){return error;}
    public int rol(){return rol;}
    public void close(){cerrado=true;try{socket.close();}catch(IOException ignored){}if(hilo!=null)hilo.interrupt();}
}
