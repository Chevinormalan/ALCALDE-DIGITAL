package alcaldedigital.cliente.mision8;
import java.io.*;import java.net.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.AtomicReference;
/** TCP con simulación autoritativa. Una conexión está vinculada a un solo puesto. */
public final class RedOcho implements AutoCloseable {
 public static final int PUERTO=48508,MAX=262144;
 private ServerSocket servidor;private final Set<Socket>sockets=ConcurrentHashMap.newKeySet();private final Set<Integer>ocupados=ConcurrentHashMap.newKeySet();private final Map<Integer,String>tokens=new ConcurrentHashMap<>();
 private volatile boolean vivo=true;private Thread hilo;private final MotorOcho autoridad;private final int puerto;
 public volatile String error="",token="";public volatile int conectados=1;public final int puesto;
 public final AtomicReference<MotorOcho> vista=new AtomicReference<>();private final ConcurrentLinkedQueue<String>cola=new ConcurrentLinkedQueue<>();public volatile int mascara;
 private RedOcho(MotorOcho m,int puesto,int puerto){autoridad=m;this.puesto=puesto;this.puerto=puerto;}
 public static RedOcho alojar(MotorOcho motor,int puerto)throws IOException{
  RedOcho r=new RedOcho(motor,0,puerto);r.servidor=new ServerSocket(puerto);r.servidor.setSoTimeout(500);r.ocupados.add(0);motor.lan=true;motor.pausar(true);
  Thread accept=new Thread(()->r.aceptar(),"m8-aceptar");accept.setDaemon(true);accept.start();
  r.hilo=new Thread(()->{long ultimo=System.nanoTime(),guardar=ultimo;double acumulado=0;while(r.vivo){long ahora=System.nanoTime();acumulado+=Math.min(.15,(ahora-ultimo)/1e9);ultimo=ahora;try{synchronized(motor){if(r.ocupados.size()<motor.jugadores)motor.pausar(true);while(acumulado>=1.0/60){motor.paso();acumulado-=1.0/60;}}if(ahora-guardar>10_000_000_000L){GuardadoOcho.guardar(motor);guardar=ahora;}if(motor.escena==MotorOcho.Escena.FINAL&&!motor.credito)GuardadoOcho.acreditar(motor);Thread.sleep(4);}catch(Exception e){r.error=e.getMessage();motor.pausar(true);}}},"m8-servidor");r.hilo.setDaemon(true);r.hilo.start();return r;
 }
 public int puerto(){return servidor==null?puerto:servidor.getLocalPort();}
 public boolean lista(){return autoridad==null?conectados>=Math.max(1,vista.get()==null?4:vista.get().jugadores):ocupados.size()>=autoridad.jugadores;}
 private void aceptar(){while(vivo)try{Socket s=servidor.accept();s.setSoTimeout(3000);sockets.add(s);Thread t=new Thread(()->atender(s),"m8-puesto");t.setDaemon(true);t.start();}catch(SocketTimeoutException e){}catch(IOException e){if(vivo)error=e.getMessage();}}
 private void atender(Socket s){int rol=-1;boolean vinculado=false;try(s;var in=new DataInputStream(s.getInputStream());var out=new DataOutputStream(s.getOutputStream())){
  String[]hello=leer(in).split(" ",3);if(hello.length<2||!hello[0].equals("M8-1"))throw new IOException("Protocolo inválido");rol=Integer.parseInt(hello[1]);String recibido=hello.length>2?hello[2]:"";
  synchronized(this){if(rol<1||rol>=autoridad.jugadores||ocupados.contains(rol)||tokens.containsKey(rol)&&!tokens.get(rol).equals(recibido))throw new IOException("Puesto ocupado o token inválido");ocupados.add(rol);tokens.putIfAbsent(rol,UUID.randomUUID().toString());}
  vinculado=true;escribir(out,tokens.get(rol));while(vivo){String cmd=leer(in);if(cmd.length()>200)throw new IOException("Orden excesiva");Properties p;synchronized(autoridad){orden(rol,cmd);p=autoridad.capturar();p.setProperty("red.conectados",""+ocupados.size());}escribir(out,serializar(p));}
 }catch(Exception e){if(vivo&&rol>=0)error="Conexión J"+(rol+1)+": "+e.getMessage();}finally{if(vinculado&&rol>=1&&ocupados.remove(rol)){autoridad.pausar(true);autoridad.entrada(rol,0);}sockets.remove(s);}}
 public void orden(int rol,String cmd)throws IOException {if(autoridad==null){cola.add(cmd);return;}synchronized(autoridad){String[]a=cmd.split(" ");try{switch(a[0]){
  case "INPUT"->autoridad.entrada(rol,Integer.parseInt(a[1]));
  case "NEXT"->{if(rol==0)autoridad.avanzar();}
  case "AUDIT"->{if(rol==0)autoridad.auditar(Integer.parseInt(a[1]),Boolean.parseBoolean(a[2]));}
  case "VOTE"->autoridad.votar(rol,Integer.parseInt(a[1]));
  case "PAUSE"->autoridad.pausar(true);
  case "RESUME"->{if(lista())autoridad.pausar(false);}
  case "SAVE"->GuardadoOcho.guardar(autoridad);
  case "RETRY"->{if(rol==0){GuardadoOcho.guardar(autoridad);autoridad.reintentar();}}
  default->throw new IOException("Orden desconocida");}
 }catch(IndexOutOfBoundsException|IllegalArgumentException e){throw new IOException("Orden inválida",e);}}}
 public static RedOcho unir(String host,int puerto,int puesto,String token)throws IOException{
  RedOcho r=new RedOcho(null,puesto,puerto);r.token=token;Socket s=new Socket();s.connect(new InetSocketAddress(host,puerto),2500);s.setSoTimeout(3000);r.sockets.add(s);var in=new DataInputStream(s.getInputStream());var out=new DataOutputStream(s.getOutputStream());escribir(out,"M8-1 "+puesto+" "+token);r.token=leer(in);
  r.hilo=new Thread(()->{try(s;in;out){while(r.vivo){String cmd=r.cola.poll();escribir(out,cmd==null?"INPUT "+r.mascara:cmd);Properties p=deserializar(leer(in));r.conectados=Integer.parseInt(p.getProperty("red.conectados","1"));r.vista.set(MotorOcho.restaurar(p));Thread.sleep(25);}}catch(Exception e){if(r.vivo)r.error=e.getMessage();}},"m8-cliente");r.hilo.setDaemon(true);r.hilo.start();return r;
 }
 private static String serializar(Properties p)throws IOException{StringWriter w=new StringWriter();p.store(w,"M8");return w.toString();}
 private static Properties deserializar(String s)throws IOException{Properties p=new Properties();p.load(new StringReader(s));return p;}
 static void escribir(DataOutputStream out,String s)throws IOException{byte[]b=s.getBytes(java.nio.charset.StandardCharsets.UTF_8);if(b.length>MAX)throw new IOException("Paquete excesivo");out.writeInt(b.length);out.write(b);out.flush();}
 static String leer(DataInputStream in)throws IOException{int n=in.readInt();if(n<0||n>MAX)throw new IOException("Longitud inválida");byte[]b=in.readNBytes(n);if(b.length!=n)throw new EOFException();return new String(b,java.nio.charset.StandardCharsets.UTF_8);}
 public void close(){vivo=false;if(autoridad!=null){autoridad.pausar(true);try{GuardadoOcho.guardar(autoridad);}catch(IOException e){error=e.getMessage();}}try{if(servidor!=null)servidor.close();for(Socket s:sockets)s.close();}catch(IOException e){error=e.getMessage();}}
}
