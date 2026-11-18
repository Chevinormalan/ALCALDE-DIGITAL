package alcaldedigital.cliente.mision8;

import processing.core.*;
import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.MarcosProcessing;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.compartido.carrera.EstadoElectoral;
import java.awt.event.KeyEvent;
import java.util.*;import java.io.*;

/** Presentación de la misión en el mismo lienzo, con la fuente y marcos de 1 y 2. */
public final class MisionOchoVista implements AutoCloseable {
 private final JuegoCliente juego;private PApplet p;private ArteOcho arte;private MotorOcho motor;private AntecedentesOcho antecedentes;private volatile RedOcho red;
 private final Set<Integer> teclas=new HashSet<>(),bloqueadas=new HashSet<>();private EstadoMando anteriorMando=EstadoMando.desconectado();private int mascaraMando;
 private final alcaldedigital.cliente.presentacion.AudioEscena audio=new alcaldedigital.cliente.presentacion.AudioEscena("mision8");private MotorOcho.Escena escenaAudio;private int danioAudio,ataqueAudio=-1;private boolean audioInyeccion,audioNeutralizacion;
 private boolean esperarMando=true;private boolean previaControles;private boolean salir,consulta,editarHost;private volatile boolean config=true,conectando;private int jugadores=1,dificultad=1,rolSolo,modo,puesto=1,pagina,foco;
 private int finalElegido=-1,ciudadPrueba;
 private String host="127.0.0.1",error="",token="";private double acumulado,tiempo;private float autoguardado;
 private record Boton(int x,int y,int w,int h,String texto,Runnable accion){}
 private final List<Boton> botones=new ArrayList<>();
 public MisionOchoVista(JuegoCliente j){juego=j;}
 public void iniciarEn(PApplet p){this.p=p;arte=new ArteOcho();try{var enlace=alcaldedigital.compartido.guardado.ArchivosPartida.leer(alcaldedigital.compartido.guardado.ArchivosPartida.directorio().resolve("mision8-red.properties"));if(enlace!=null){host=enlace.getProperty("host",host);puesto=Integer.parseInt(enlace.getProperty("puesto","1"));token=enlace.getProperty("token","");}try{antecedentes=AntecedentesOcho.cargar();}catch(IOException falta){if(!juego.progreso.todasDesbloqueadas())throw falta;finalElegido=0;}if(juego.continuarGuardado){motor=GuardadoOcho.cargar();if(!motor.prueba()&&(antecedentes==null||antecedentes.electoral().juanCorrupto()!=motor.antecedentes.electoral().juanCorrupto()))throw new IOException("La elegibilidad cambió en la campaña. Inicia una nueva misión 8.");jugadores=motor.jugadores;dificultad=motor.dificultad;rolSolo=motor.rolSolo;modo=motor.lan?1:0;finalElegido=motor.finalElegido();ciudadPrueba=motor.antecedentes.estadoCiudad().equals("CIUDAD EN CRISIS")?2:motor.antecedentes.estadoCiudad().equals("CONFIANZA CÍVICA")?0:1;motor.pausar(false);config=true;}}catch(Exception e){motor=null;juego.continuarGuardado=false;error=e.getMessage();}}
 public MotorOcho motor(){return motor;}
 public boolean salir(){return salir;}public boolean listaComun(){return !config&&!previaControles&&motor!=null;}
 public boolean pausaComun(){return motor!=null&&motor.pausa;}
 public void pausarComun(){teclas.clear();mascaraMando=0;orden("PAUSE");}
 public void reanudarComun(){bloqueadas.addAll(teclas);teclas.clear();mascaraMando=0;orden("RESUME");}
 public boolean consultaAbiertaComun(){return consulta;}
 public void abrirConsultaComun(){consulta=true;pagina=0;}
 public void cerrarConsultaComun(){consulta=false;}
 public String guardarComun()throws IOException{if(motor==null)return "Configura la partida para guardar.";if(red!=null){red.orden(red.puesto,"SAVE");return red.puesto==0?"Partida LAN guardada.":"Guardado solicitado al anfitrión.";}GuardadoOcho.guardar(motor);return "Misión 8 guardada.";}
 public void salirComun(){salir=true;}
 public void perderFoco(){teclas.clear();mascaraMando=0;}
 public void bloquearTeclas(Set<Integer>k){bloqueadas.addAll(k);}
 private void orden(String cmd){if(cmd.equals("NEXT")||cmd.equals("RETRY")){bloqueadas.addAll(teclas);teclas.clear();mascaraMando=0;esperarMando=true;}try{if(red!=null)red.orden(red.puesto,cmd);else if(motor!=null){String[]a=cmd.split(" ");switch(a[0]){case "PAUSE"->motor.pausar(true);case "RESUME"->motor.pausar(false);case "NEXT"->motor.avanzar();case "AUDIT"->motor.auditar(Integer.parseInt(a[1]),Boolean.parseBoolean(a[2]));case "VOTE"->motor.votar(0,Integer.parseInt(a[1]));case "RETRY"->{GuardadoOcho.guardar(motor);motor.reintentar();}default->{}}}}catch(Exception e){error=e.getMessage();}}
 public void elegirFinal(int candidato){if(candidato< -1||candidato>3)throw new IllegalArgumentException("Final inválido");finalElegido=candidato;juego.continuarGuardado=false;motor=null;error="";}
 private void iniciar(){if(antecedentes==null&&finalElegido<0&&modo!=2){error="Elige un final de prueba para jugar sin completar la campaña.";return;}error="";if(modo==2){if(conectando)return;conectando=true;Thread t=new Thread(()->{try{RedOcho conexion=RedOcho.unir(host,RedOcho.PUERTO,puesto,token);red=conexion;token=conexion.token;var enlace=new Properties();enlace.setProperty("version","1");enlace.setProperty("host",host);enlace.setProperty("puesto",""+puesto);enlace.setProperty("token",token);alcaldedigital.compartido.guardado.ArchivosPartida.guardar(alcaldedigital.compartido.guardado.ArchivosPartida.directorio().resolve("mision8-red.properties"),enlace);config=false;}catch(Exception e){error="Conexión: "+e.getMessage();}finally{conectando=false;}},"m8-conectar");t.setDaemon(true);t.start();return;}
  try{if(juego.continuarGuardado&&motor!=null){config=false;motor.pausar(false);}else{motor=new MotorOcho(finalElegido<0?antecedentes:FinalesPruebaOcho.antecedentes(finalElegido,ciudadPrueba),jugadores,dificultad,rolSolo,jugadores==4,juego.semilla++);if(finalElegido>=0)motor.elegirFinalPrueba(finalElegido);}if(modo==1)red=RedOcho.alojar(motor,RedOcho.PUERTO);config=false;guardarComun();}catch(Exception e){error=e.getMessage();}}
 public void actualizar(float dt){tiempo+=Math.min(.1,dt);if(config||previaControles)return;if(red!=null&&red.puesto!=0){MotorOcho remoto=red.vista.get();if(remoto!=null)motor=remoto;if(!red.error.isBlank()){error="Conexión perdida: "+red.error+". Pulsa Comenzar para reconectar.";red.close();red=null;modo=2;config=true;return;}red.mascara=mascara(0)|mascaraMando;return;}if(motor==null)return;
  synchronized(motor){for(int i=0;i<motor.jugadores;i++)if(red==null||i==0)motor.entrada(i,mascara(i)|(i==0?mascaraMando:0));if(red==null){acumulado+=Math.min(.15,dt);while(acumulado>=1d/60){motor.paso();acumulado-=1d/60;}}if(motor.escena==MotorOcho.Escena.FINAL&&!motor.credito)try{GuardadoOcho.acreditar(motor);if(!motor.prueba())juego.victoria(8);}catch(IOException e){error=e.getMessage();}}
  if(!motor.pausa){autoguardado+=dt;if(autoguardado>=10){autoguardado=0;try{guardarComun();}catch(IOException e){error=e.getMessage();}}}
 }
 private static final int[][] CONTROLES={{65,68,32,70,81,69,82},{37,39,16,76,75,10,74},{100,102,96,97,98,99,110}};
 private int mascara(int jugador){if(jugador>=3){int mask=0;int[]k={90,88,67,66,78,77};for(int i=0;i<6;i++)if(teclas.contains(k[i]))mask|=1<<i;return mask;}int mask=0;int[]b={1,2,4,8,16,32,64};for(int i=0;i<7;i++)if(teclas.contains(CONTROLES[jugador][i]))mask|=b[i];return mask;}
 public void pulsar(char c,int k){if(bloqueadas.contains(k)||!teclas.add(k))return;if(editarHost){if(k==10)editarHost=false;else if(k==8&&!host.isEmpty())host=host.substring(0,host.length()-1);else if(host.length()<100&&(Character.isLetterOrDigit(c)||c=='.'||c=='-'||c==':'))host+=c;return;}
  if(k==27&&config){salir=true;return;}if(k==9){foco=(foco+1)%Math.max(1,botones.size());return;}if(consulta){if(k==37)pagina=Math.floorMod(pagina-1,6);if(k==39)pagina=(pagina+1)%6;return;}
  if((config||motor!=null&&motor.escena!=MotorOcho.Escena.COMBATE)&&(k==37||k==38||k==39||k==40)){foco=Math.floorMod(foco+(k==37||k==38?-1:1),Math.max(1,botones.size()));return;}
  if((config||motor!=null&&motor.escena!=MotorOcho.Escena.COMBATE)&&(k==10||k==32||k==69)){if(!botones.isEmpty())activarBotonAudio();}
 }
 private void activarBotonAudio(){alcaldedigital.cliente.audio.InterfazSonora.activar("click");botones.get(Math.min(foco,botones.size()-1)).accion.run();}
 public void soltar(int k){teclas.remove(k);bloqueadas.remove(k);}
 public void raton(int x,int y){for(Boton b:List.copyOf(botones))if(x>=b.x&&x<b.x+b.w&&y>=b.y&&y<b.y+b.h){alcaldedigital.cliente.audio.InterfazSonora.activar("click");b.accion.run();foco=0;break;}}
 public void mando(EstadoMando e){mascaraMando=0;if(esperarMando){if(e.botones().isEmpty()&&Math.abs(e.izquierdaX())<.3&&Math.abs(e.izquierdaY())<.3)esperarMando=false;else{anteriorMando=e;return;}}if(e.izquierdaX()<-.3)mascaraMando|=MotorOcho.IZQ;if(e.izquierdaX()>.3)mascaraMando|=MotorOcho.DER;
  if(e.pulsado(EstadoMando.Boton.A))mascaraMando|=MotorOcho.SALTO;if(e.pulsado(EstadoMando.Boton.X))mascaraMando|=MotorOcho.UNO;if(e.pulsado(EstadoMando.Boton.Y))mascaraMando|=MotorOcho.DOS;if(e.pulsado(EstadoMando.Boton.LB))mascaraMando|=MotorOcho.TRES;if(e.pulsado(EstadoMando.Boton.RB))mascaraMando|=MotorOcho.ESQUIVA;
  if(e.pulsado(EstadoMando.Boton.DERECHA)&&!anteriorMando.pulsado(EstadoMando.Boton.DERECHA))foco=(foco+1)%Math.max(1,botones.size());if(e.pulsado(EstadoMando.Boton.IZQUIERDA)&&!anteriorMando.pulsado(EstadoMando.Boton.IZQUIERDA))foco=Math.floorMod(foco-1,Math.max(1,botones.size()));
  if((config||motor!=null&&motor.escena!=MotorOcho.Escena.COMBATE)&&e.pulsado(EstadoMando.Boton.A)&&!anteriorMando.pulsado(EstadoMando.Boton.A)&&!botones.isEmpty())activarBotonAudio();anteriorMando=e;
 }
 private static final int FONDO=0xff21152f,PANEL=0xff38264b,ORO=0xfff4cb6a,LILA=0xffbd91d5,PAPEL=0xfff5e8fb,CIAN=0xff73ddcb,ROSA=0xfff789a3;
 private void marco(float x,float y,float w,float h){MarcosProcessing.marco(p.g,x,y,w,h,PANEL,ORO);}
 private void texto(String s,float x,float y,float tam,int color){p.fill(color);p.textSize(tam);p.text(s,x,y);}
 private void parrafo(String s,float x,float y,float w,float h,float tam){p.fill(PAPEL);p.textSize(tam);p.textLeading(tam+5);p.text(s,x,y,w,h);}
 private void boton(String s,int x,int y,int w,Runnable a){int h=43;boolean focoActual=botones.size()==foco;boolean dentro=p.mouseX>=x&&p.mouseX<x+w&&p.mouseY>=y&&p.mouseY<y+h;MarcosProcessing.marco(p.g,x,y,w,h,dentro||focoActual?0xff604077:PANEL,dentro||focoActual?ORO:LILA);texto(s,x+14,y+29,17,PAPEL);botones.add(new Boton(x,y,w,h,s,a));}
 private final alcaldedigital.cliente.audio.InterfazSonora audioUI=new alcaldedigital.cliente.audio.InterfazSonora();
 public void dibujar(){audioUI.foco("m8:"+foco);audioUI.hover(botones.stream().filter(b->p.mouseX>=b.x&&p.mouseX<b.x+b.w&&p.mouseY>=b.y&&p.mouseY<b.y+b.h).map(b->b.x+":"+b.y).findFirst().orElse(""));p.pushStyle();p.textAlign(PApplet.LEFT,PApplet.BASELINE);p.noStroke();botones.clear();if(config){configuracion();p.popStyle();return;}if(motor==null){p.background(FONDO);texto("Conectando con el anfitrión…",400,340,24,ORO);p.popStyle();return;}
  synchronized(motor){sonido();if(motor.escena==MotorOcho.Escena.COMBATE)combate();else escena();if(consulta)consulta();if(motor.prueba())texto("PRUEBA · "+FinalesPruebaOcho.nombre(motor.finalElegido()),370,26,15,ORO);if(red!=null){texto("LAN · J"+(red.puesto+1)+" · "+(red.lista()?"conectados":"esperando participantes"),35,30,15,CIAN);}
   if(!error.isEmpty()){marco(70,650,1140,55);texto(error,90,684,16,ROSA);}}
  p.popStyle();
 }
 private void configuracion(){p.image(arte.fondos[0],0,0,1280,720);p.fill(18,10,30,205);p.rect(0,0,1280,720);marco(85,55,1110,615);texto("MISIÓN 8 · EL FUTURO DE CIVITAS",125,110,31,ORO);parrafo("La carrera ha terminado. Ahora la ciudad debe distinguir votos, noticias y promesas. Tres héroes pueden cooperar; el cuarto participante controla al jefe.",125,132,1000,65,18);
  texto("PARTICIPANTES",125,225,18,LILA);for(int i=1;i<=4;i++){final int n=i;boton((jugadores==i?"> ":"")+i+(i==4?" · 3 contra 1":""),125+(i-1)*245,240,230,()->jugadores=n);}
  texto("DIFICULTAD",125,315,18,LILA);for(int i=0;i<3;i++){final int d=i;boton((dificultad==i?"> ":"")+new String[]{"Fácil","Normal","Difícil"}[i],125+i*240,330,220,()->dificultad=d);}
  boton("Rol solo: "+MotorOcho.ROLES[rolSolo],855,330,285,()->rolSolo=(rolSolo+1)%3);
  boton("Modo: "+new String[]{"Local","Crear LAN","Unirse LAN"}[modo],125,400,320,()->modo=(modo+1)%3);
  boton((editarHost?"IP: ":"Servidor: ")+host,465,400,390,()->editarHost=true);boton("Puesto LAN: J"+(puesto+1),875,400,265,()->puesto=puesto%3+1);
  boton("Final: "+FinalesPruebaOcho.nombre(finalElegido),125,465,640,()->elegirFinal((finalElegido+2)%5-1));
  boton("Ciudad: "+(finalElegido<0?"según campaña":FinalesPruebaOcho.ciudad(ciudadPrueba)),785,465,355,()->{elegirFinal(finalElegido<0?0:finalElegido);ciudadPrueba=(ciudadPrueba+1)%3;});
  texto(finalElegido>=0?"FINAL DE PRUEBA · no cambia tus decisiones ni acredita victorias.":antecedentes!=null?antecedentes.estadoCiudad()+" · según tus siete victorias.":"Acceso libre: elige un final de prueba para comenzar.",125,540,17,CIAN);
  parrafo(error,125,553,990,38,15);boton(conectando?"Conectando…":"Comenzar",125,600,280,this::iniciar);boton("Ver controles",425,600,280,()->{previaControles=true;consulta=true;pagina=4;config=false;motor=new MotorOcho(antecedentes!=null?antecedentes:referencia(),1,1,0,false,1);motor.pausar(true);});boton("Volver a misiones",725,600,390,()->salir=true);
 }
 private static AntecedentesOcho referencia(){var c=alcaldedigital.compartido.modelo.EstadoCiudad.inicial();return new AntecedentesOcho(c,EstadoElectoral.inicial(c),Map.of(),List.of());}
 private HistoriaOcho.Accion accionEscena(){var linea=HistoriaOcho.actual(motor);return linea==null?HistoriaOcho.Accion.HABLAR:linea.accion();}
 private void escena(){
  var e=motor.escena;var accion=accionEscena();boolean obra=e==MotorOcho.Escena.OBRA;
  boolean construida=!obra||motor.lineaDialogo>=3||accion==HistoriaOcho.Accion.CONSTRUIR&&motor.ticksDialogo>=125;
  p.image(construida?arte.fondos[0]:arte.parque,0,0,1280,720);
  if(motor.antecedentes.estadoCiudad().equals("CIUDAD EN CRISIS")){p.fill(70,12,60,50);p.rect(0,0,1280,530);}
  if(e==MotorOcho.Escena.MUTACION&&accion==HistoriaOcho.Accion.MUTAR){p.fill(15,5,30,Math.min(170,motor.ticksDialogo*1.7f));p.rect(0,0,1280,530);}
  boolean epilogo=e==MotorOcho.Escena.EPILOGO||e==MotorOcho.Escena.FINAL;
  if(construida){for(int i=0;i<4;i++){
   if(epilogo&&i!=motor.ganador)continue;
   var lugar=epilogo?EscenografiaOcho.elegido(motor):EscenografiaOcho.candidato(i);
   boolean elegido=i==motor.ganador;int pose=0;
   if(e==MotorOcho.Escena.ESCRUTINIO&&elegido)pose=1;
   if(e==MotorOcho.Escena.MUTACION&&elegido&&motor.inyectado)pose=2;
   if(epilogo&&elegido)pose=motor.candidatoRecuperado?1:3;
   if(accion==HistoriaOcho.Accion.DISPARAR&&i==0&&motor.ticksDialogo<60){arte.sprite(p,arte.heroes[60],(float)lugar.x(),(float)lugar.y(),100,false);}
   else if(e==MotorOcho.Escena.MUTACION&&elegido&&accion==HistoriaOcho.Accion.MUTAR){
    float progreso=PApplet.constrain(motor.ticksDialogo/90f,0,1);float flota=juego.movimientoReducido?0:(float)Math.sin(motor.ticksDialogo*.09)*2*progreso;
    arte.sprite(p,arte.infeccion(i,motor.ticksDialogo),(float)lugar.x(),(float)lugar.y()+flota,100+progreso*25,false);
    if(motor.ticksDialogo>=20&&motor.ticksDialogo<110){p.noFill();p.stroke(115,221,203,100);p.strokeWeight(2);float radio=25+progreso*70;p.ellipse((float)lugar.x(),(float)lugar.y()-55,radio,radio/2);p.noStroke();}

   }else arte.sprite(p,arte.candidatos[i*16+pose],(float)lugar.x(),(float)lugar.y(),100,false);
   MarcosProcessing.marco(p.g,(float)lugar.x()-62,(float)lugar.y()+9,124,25,FONDO,LILA);texto(EstadoElectoral.NOMBRES[i],(float)lugar.x()-43,(float)lugar.y()+28,16,ORO);
  }}
  if(obra)dibujarObra();
  if(!obra&&!epilogo)dibujarMultitud(accion);
  dibujarProtagonistas(epilogo,accion);
  if(!obra)dibujarPalomo(accion);
  if(e==MotorOcho.Escena.AUDITORIA){auditoria();return;}
  if(e==MotorOcho.Escena.VOTO){votacion();return;}
  if(e==MotorOcho.Escena.FINAL||e==MotorOcho.Escena.DERROTA){
   String titulo=e==MotorOcho.Escena.FINAL?"VICTORIA · "+motor.antecedentes.estadoCiudad():"DERROTA · REINTENTA";
   String resumen=e==MotorOcho.Escena.FINAL?"Alcalde legítimo: "+EstadoElectoral.NOMBRES[motor.ganador]+". Evidencias contrastadas: "+motor.auditoria+"/3. Puntaje: "+GuardadoOcho.fila(motor).puntaje()+"/100.":"El equipo ha caído. Reintenta el combate conservando los votos y antecedentes de esta partida.";
   cajaDialogo(new HistoriaOcho.Linea("CIVITAS",resumen),resumen,titulo,false);
   if(e==MotorOcho.Escena.FINAL){boton("Volver",1030,600,190,()->salir=true);boton("Antecedentes",1030,650,190,()->{consulta=true;pagina=0;});}
   else{boton("Reintentar",1030,600,190,()->orden("RETRY"));boton("Salir",1030,650,190,()->salir=true);}return;
  }
  var linea=HistoriaOcho.actual(motor);if(linea!=null){
   cajaDialogo(linea,motor.textoVisible(),"MISIÓN 8",true);
   String etiqueta=!motor.textoCompleto()?"Mostrar texto":!motor.accionCompleta()?"En escena…":"Continuar";
   if(red==null||red.puesto==0)boton(etiqueta,1030,650,190,()->orden("NEXT"));
   else texto("El anfitrión continúa",1000,684,14,LILA);
  }
 }
 private void cajaDialogo(HistoriaOcho.Linea linea,String texto,String titulo,boolean secuencia){
  marco(28,530,1224,180);MarcosProcessing.marco(p.g,48,548,135,145,FONDO,LILA);
  PImage retrato=arte.retrato(linea.quien(),motor);arte.sprite(p,retrato,115,686,115,false);
  texto(linea.quien(),210,568,23,ORO);parrafo(texto,210,583,790,108,19);
  texto(titulo,1030,565,14,LILA);
  if(secuencia){texto((motor.lineaDialogo+1)+" / "+HistoriaOcho.lineas(motor).size(),1090,596,16,PAPEL);texto("E / Enter / A",1035,632,14,LILA);}
 }
 private void dibujarObra(){
  if(accionEscena()!=HistoriaOcho.Accion.CONSTRUIR)return;int t=motor.ticksDialogo;
  var nave=t<85?EscenografiaOcho.interpolar(new EscenografiaOcho.Punto(-180,180),new EscenografiaOcho.Punto(430,165),t/85.0):t<155?new EscenografiaOcho.Punto(430,165):EscenografiaOcho.interpolar(new EscenografiaOcho.Punto(430,165),new EscenografiaOcho.Punto(1450,130),(t-155)/85.0);
  if(t<240)arte.sprite(p,arte.efectos[8+Math.min(7,t/30)],(float)nave.x(),(float)nave.y(),240,false);
  if(t<235)for(int i=0;i<4;i++){float x=t<85?-160+t*8-i*70:t<170?520+i*130:520+i*130+(t-170)*13;int frame=t<85||t>=170?(t/7+i)%3:4+(t/9+i)%4;arte.sprite(p,arte.efectos[frame],x,365,85,false);}
  if(t>=80&&t<200)for(int i=0;i<6;i++)arte.sprite(p,arte.efectos[16+Math.min(7,(t-80)/15)],160+i*210,395,280,false);
 }
 private void dibujarMultitud(HistoriaOcho.Accion accion){
  float aparicion=accion==HistoriaOcho.Accion.APARECER?PApplet.constrain(motor.ticksDialogo/55f,0,1):accion==HistoriaOcho.Accion.DESAPARECER?1-PApplet.constrain(motor.ticksDialogo/65f,0,1):motor.escena==MotorOcho.Escena.MUTACION&&motor.lineaDialogo>2?0:1;
  p.tint(255,255*aparicion);for(int i=0;i<18;i++){int estado=motor.antecedentes.ciudad().conflictos()>60?2:1;int frame=juego.movimientoReducido?0:(motor.ticks/18+i)%4;int idx=(i%2)*12+estado*4+frame;arte.sprite(p,arte.ciudadanos[idx],345+(i%9)*90,425+(i/9)*65,62,false);}p.noTint();
  if(accion==HistoriaOcho.Accion.APARECER&&motor.ticksDialogo<80){arte.sprite(p,arte.efectos[16+Math.min(7,motor.ticksDialogo/10)],710,470,330,false);texto("¡POF!",640,360,45,ORO);}
 }
 private void dibujarProtagonistas(boolean epilogo,HistoriaOcho.Accion accion){
  for(int i=0;i<3;i++){float x=new float[]{95,185,270}[i],y=475;boolean mueve=false;
   if(motor.escena==MotorOcho.Escena.OBRA&&i==0){float t=motor.lineaDialogo>=3?Math.min(1,motor.ticksDialogo/85f):0;x=-90+185*t;mueve=t<1;}
   if(epilogo){float t=accion==HistoriaOcho.Accion.ACERCARSE?Math.min(1,motor.ticksDialogo/100f):1;x+=(new float[]{480,620,760}[i]-x)*t;y-=45*t;mueve=t<1;}
   int f=mueve?(motor.ticks/7)%4:0;PImage im=mueve?(i==0?arte.sofi[8+f]:i==1?arte.periodista[12+f]:arte.influencer[f]):arte.reposo(i);arte.sprite(p,im,x,y,mueve?(i==2?52:110):100,false);
  }
  if(epilogo&&motor.ganador!=0&&(motor.lineaDialogo>=5||motor.escena==MotorOcho.Escena.FINAL)){float llegada=accion==HistoriaOcho.Accion.DISPARAR?Math.min(1,motor.ticksDialogo/15f):1;arte.sprite(p,arte.heroes[accion==HistoriaOcho.Accion.DISPARAR&&motor.ticksDialogo<60?60:48],-70+270*llegada,430,100,false);}
 }
 private void dibujarPalomo(HistoriaOcho.Accion accion){
  int t=motor.ticksDialogo;var lugar=EscenografiaOcho.palomo(motor);if(accion==HistoriaOcho.Accion.DISPARAR&&t>=100)return;
  if(motor.palomoNeutralizado&&accion!=HistoriaOcho.Accion.DISPARAR)return;
  boolean vuela=accion==HistoriaOcho.Accion.VUELO||accion==HistoriaOcho.Accion.RETIRARSE||accion==HistoriaOcho.Accion.ESCAPAR;
  PImage im=vuela?arte.vuelo[(motor.ticks/7)%4]:arte.efectos[24+(accion==HistoriaOcho.Accion.INYECTAR?(t<35?4:t<80?5:6):accion==HistoriaOcho.Accion.DESAPARECER?3:1)];
  if(accion==HistoriaOcho.Accion.DISPARAR&&motor.palomoNeutralizado)im=arte.efectos[31];
  arte.sprite(p,im,(float)lugar.x(),(float)lugar.y(),110,accion!=HistoriaOcho.Accion.RETIRARSE&&accion!=HistoriaOcho.Accion.ESCAPAR);
  if(accion==HistoriaOcho.Accion.INYECTAR){var punta=EscenografiaOcho.puntaAguja(motor);p.stroke(235,230,240);p.strokeWeight(2);p.line((float)lugar.x()-35,(float)punta.y(),(float)punta.x(),(float)punta.y());p.noStroke();if(t>=35&&t<65){p.fill(115,221,203,170);p.ellipse((float)punta.x(),(float)punta.y(),14,14);}}
  if(accion==HistoriaOcho.Accion.DISPARAR){if(t>=15&&t<60){var pulso=EscenografiaOcho.pulso(motor);arte.sprite(p,arte.efectos[32+(t/5)%6],(float)pulso.x(),(float)pulso.y()+12,42,false);}if(t>=60&&t<100){arte.sprite(p,arte.efectos[46+(t/10)%2],(float)lugar.x(),(float)lugar.y(),140,false);arte.sprite(p,arte.efectos[16+Math.min(7,(t-60)/5)],(float)lugar.x(),(float)lugar.y()+30,180,false);}}
 }
 private void auditoria(){marco(60,75,1160,565);texto("ANTES DE VOTAR · CONTRASTA LAS EVIDENCIAS",95,120,27,ORO);for(int i=0;i<3;i++){final int d=i;int y=160+i*135;texto(MotorOcho.DOSSIERS[i],95,y+5,21,PAPEL);parrafo(evidencia(i),95,y+15,680,75,17);if(motor.dossiers[i]<0&&(red==null||red.puesto==0)){boton("Contrastar",820,y,170,()->orden("AUDIT "+d+" true"));boton("Omitir",1005,y,155,()->orden("AUDIT "+d+" false"));}else texto(motor.dossiers[i]==1?"CONTRASTADA":motor.dossiers[i]==0?"OMITIDA":"El anfitrión revisa",830,y+30,18,CIAN);}
  texto("El AVL ordena los casos por riesgo. Las aclaraciones se propagan por el grafo.",95,610,16,LILA);}
 private String evidencia(int i){var a=motor.antecedentes;return switch(i){case 0->"M1: "+a.senal(1,"publicaciones_responsables")+" publicaciones responsables. M5: "+a.senal(5,"publicaciones_verificadas")+" verificaciones. Compartir no acredita un voto.";case 1->"M7: pacto de Juan "+(a.electoral().juanCorrupto()?"CONFIRMADO: queda excluido.":"no confirmado.")+" M4: "+a.senal(4,"respuestas_favorables")+" respuestas favorables, "+a.senal(4,"respuestas_desfavorables")+" desfavorables.";default->"M7: "+a.senal(7,"noticias_contrastadas")+" noticias contrastadas. Los apoyos eran provisionales. M6: escucha y convicción son rutas válidas.";};}
 private void votacion(){marco(75,80,1130,490);texto("ES HORA DE VOTAR · EL RESPALDO DE TODA LA CIUDAD CUENTA",105,130,25,ORO);int votante=red==null?motor.votosEmitidos:red.puesto;for(int i=0;i<4;i++){final int c=i;int x=110+i*275;arte.sprite(p,arte.candidatos[i*16],x+105,310,135,false);texto(EstadoElectoral.NOMBRES[i],x+25,345,22,ORO);texto("Respaldo: "+motor.antecedentes.electoral().respaldo(i),x+12,380,17,PAPEL);if(motor.antecedentes.electoral().elegible(i)){if(votante<jugadores&&motor.votos[votante]<0)boton("Votar · J"+(votante+1),x,420,245,()->{if(red==null)motor.votar(votante,c);else orden("VOTE "+c);});}else texto("EXCLUIDO · PACTO",x,445,17,ROSA);}
  texto("Votos emitidos: "+motor.votosEmitidos+" / "+motor.jugadores+" · cada voto aporta 1 al respaldo",110,525,18,CIAN);}
 private double camaraVisual(){return AnimacionOcho.camaraVisual(motor);}
 private void combate(){int bg=motor.fase<4?1:motor.fase==4?2:3;p.image(arte.fondos[bg],0,0,1280,720);p.fill(20,8,32,60);p.rect(0,0,1280,720);
  for(var plat:motor.plataformas()){float y=(float)(plat.y()-camaraVisual());if(y< -50||y>760)continue;if(plat.ancho()>1000){p.fill(96,64,119);p.rect(0,y,1280,18);p.fill(244,203,106);p.rect(0,y,1280,3);}else{p.image(arte.efectos[58+(motor.ticks/24)%2],(float)plat.x(),y-18,(float)plat.ancho(),55);}}
  if(motor.fase==2||motor.fase==3){int lado=motor.fase==2?(motor.reloj/420)%2:2;for(int x=0;x<1280;x+=65)if(lado==0&&x<330||lado==1&&x>950||lado==2&&(x<175||x>1105))arte.sprite(p,arte.efectos[48+Math.min(4,(motor.reloj%420)/18)],x+30,(float)(612-camaraVisual()),65,false);}
  dibujarJefe();

  if(motor.fase==5&&(motor.ataqueTicks<0||motor.ataqueTicks>=(dificultad==2?45:60))){marco(350,580,580,48);texto("ASCIENDE · ALCANZA EL NÚCLEO VIRAL",375,612,20,ORO);}
  dibujarAvisos();
  for(var b:motor.proyectiles)dibujarProyectil(b);

  for(int i=0;i<motor.heroes.length;i++){var h=motor.heroes[i];if(!juego.movimientoReducido&&h.invulnerable>0&&motor.ticks%8<3)continue;arte.sprite(p,arte.heroe(motor,i),(float)h.x,(float)(h.y-camaraVisual()+3),100,h.direccion<0);texto("J"+(i+1),(float)h.x-15,(float)(h.y-camaraVisual()-100),14,ORO);}
  marco(20,35,1240,90);texto("MR. CORRUPTO · "+EstadoElectoral.NOMBRES[motor.ganador]+" · FORMA "+motor.forma+" / 3",40,65,21,ORO);p.fill(FONDO);p.rect(40,80,1190,22);p.fill(ROSA);p.rect(40,80,1190f*motor.vidaJefe/motor.maxVida,22);texto("FASE "+motor.fase+" / 5",1040,65,17,CIAN);
  for(int i=0;i<motor.heroes.length;i++){var h=motor.heroes[i];marco(20+i*305,650,295,55);texto("J"+(i+1)+" "+MotorOcho.ROLES[motor.rol(i)]+" · "+h.vida+" / 5",35+i*305,684,17,h.vida>0?PAPEL:ROSA);}
  if(motor.jefeHumano)texto("J"+motor.jugadores+" · JEFE: Z X C B N M",925,680,16,ORO);
  if(motor.transicion>0){marco(320,270,640,120);texto("FASE "+motor.fase+" · FORMA "+motor.forma,355,315,30,ORO);parrafo(motor.fase>=4?"Las plataformas abren una nueva ruta. Salto doble para ascender.":"Observa los avisos del suelo y conserva una ruta de escape.",355,333,570,55,18);}
 }
 private void dibujarJefe(){
  float x=(float)motor.jefeX,y=(float)(motor.jefeY-camaraVisual()+90);
  float flota=juego.movimientoReducido?0:3*PApplet.sin(motor.ticks*.07f);int edad=AnimacionOcho.desdeEmision(motor);
  float retroceso=juego.movimientoReducido||edad>=14?0:5*(1-edad/14f);
  int cuadro=AnimacionOcho.cuadroJefe(motor);float carga=cuadro>=1&&cuadro<=3?(cuadro/3f):0;
  p.fill(115,221,203,22+45*carga);p.ellipse(x,y-92,105+30*carga,105+30*carga);
  if(motor.transicion>0&&(motor.fase==3||motor.fase==5)){
   float f=1-motor.transicion/100f;int anterior=motor.ganador*16+(motor.forma==2?4:8);
   p.tint(255,255*(1-f));arte.sprite(p,arte.candidatos[anterior],x,y+flota,190+(motor.forma-1)*22,false);p.tint(255,255*f);
  }
  arte.sprite(p,arte.jefe(motor),x+retroceso,y+flota,190+motor.forma*22,false);p.noTint();
  if(edad<12){p.noFill();p.stroke(115,221,203,180*(1-edad/12f));p.strokeWeight(3);float r=24+edad*4;p.ellipse(x,(float)(motor.jefeY-camaraVisual()),r,r);p.noStroke();}
 }
 private void dibujarAvisos(){
  int t=motor.ataqueTicks;if(t<0||motor.transicion>0)return;int aviso=AnimacionOcho.aviso(motor);
  int siguiente=10000;for(int e:AnimacionOcho.emisiones(motor))if(e>t)siguiente=Math.min(siguiente,e-t);
  if(motor.fase==5&&t<190)siguiente=Math.min(siguiente,190-t);
  if(t<aviso){int y=motor.fase==5?580:145;marco(385,y,510,55);texto("AVISO · "+nombreAtaque(),410,y+36,21,ORO);}
  if(t>=aviso&&siguiente>18)return;
  p.stroke(244,203,106,150);p.strokeWeight(2);p.noFill();
  var patron=motor.fase==5&&siguiente==190-t?MotorOcho.Ataque.RAYOS:motor.ataque;
  switch(patron){
   case LLUVIA->{for(int x=105;x<1280;x+=210){p.line(x,205,x,232);p.line(x-5,225,x,232);p.line(x+5,225,x,232);}}
   case BOTS->{for(int y=295;y<600;y+=110){p.line(20,y,55,y);p.line(1225,y,1260,y);}}
   case ABANICO->{for(int i=0;i<7;i++){float a=PApplet.PI*.17f+i*PApplet.PI*.11f;float x=(float)motor.jefeX,y=(float)(motor.jefeY-camaraVisual());p.line(x+PApplet.cos(a)*45,y+PApplet.sin(a)*45,x+PApplet.cos(a)*78,y+PApplet.sin(a)*78);}}
   case RAYOS->{var objetivo=Arrays.stream(motor.heroes).filter(h->h.vida>0).min(Comparator.comparingDouble(h->Math.abs(h.x-motor.jefeX))).orElse(motor.heroes[0]);float x=(float)objetivo.x;for(int y=210;y<612;y+=25)p.line(x,y,x,y+12);}
   case MURO->{p.line(1240,215,1240,330);p.line(1240,525,1240,612);}
   case ORBES->{float x=(float)motor.jefeX,y=(float)(motor.jefeY-camaraVisual());for(int i=0;i<3;i++)p.ellipse(x-38+i*38,y-85,18,18);}
  }p.noStroke();
 }
 private String nombreAtaque(){return switch(motor.ataque){case LLUVIA->"LLUVIA DE MENSAJES";case BOTS->"BARRIDO DE BOTS";case ABANICO->"ABANICO VIRAL";case RAYOS->"RÁFAGA DE SEÑALES";case MURO->"MURO DE DESINFORMACIÓN";case ORBES->"ORBES VIRALES";};}
 private void dibujarProyectil(MotorOcho.Proyectil b){
  float x=(float)b.x,y=(float)(b.y-camaraVisual()),r=(float)b.r;
  p.pushStyle();p.pushMatrix();p.translate(x,y);p.noStroke();
  int color=b.dueno>=0?CIAN:b.tipo==2?ORO:b.tipo==3?CIAN:ROSA;
  p.fill(color,35);p.ellipse(0,0,r*3.2f,r*3.2f);
  // La estela nace detrás de la velocidad real; no se añaden colisiones visuales falsas.
  p.stroke(color,100);p.strokeWeight(Math.max(2,r*.7f));p.line(0,0,(float)-b.vx*3,(float)-b.vy*3);p.noStroke();
  if(b.dueno>=0){arte.sprite(p,arte.efectos[32+(b.edad/4)%6],0,r,r*2.5f,false);}
  else switch(b.tipo){
   case 0->{p.rotate((float)Math.atan2(b.vy,b.vx)-PApplet.HALF_PI);p.fill(ROSA);p.beginShape();p.vertex(0,r*1.25f);p.vertex(-r*.65f,-r*.4f);p.vertex(0,-r);p.vertex(r*.65f,-r*.4f);p.endShape(PApplet.CLOSE);p.fill(PAPEL);p.rect(-2,-r*.55f,4,r);}
   case 1->{p.rotate((float)Math.atan2(b.vy,b.vx));arte.sprite(p,arte.efectos[40+(b.edad/5)%6],0,r*1.2f,r*2.5f,false);}
   case 2->{p.rotate((float)Math.atan2(b.vy,b.vx));p.fill(ORO);p.rect(-r,-r*.65f,r*2,r*1.3f,4);p.fill(PAPEL);p.rect(-r,-r*.22f,r*2,r*.44f,2);}
   default->{arte.sprite(p,arte.efectos[32+(b.edad/5)%6],0,r*1.3f,r*2.6f,false);p.noFill();p.stroke(CIAN,160);p.strokeWeight(1.5f);float pulso=1+.08f*PApplet.sin(b.edad*.2f);p.ellipse(0,0,r*2*pulso,r*2*pulso);}
  }
  p.popMatrix();p.popStyle();
 }
 private void consulta(){p.fill(15,8,25,235);p.rect(0,0,1280,720);marco(50,65,1180,590);String[]titulos={"ANTECEDENTES DE CAMPAÑA","AVL · EVIDENCIAS POR RIESGO","ÁRBOL DE DECISIONES","GRAFO DE DIFUSIÓN","CONTROLES Y PATRONES","HISTORIAL DEL ESCRUTINIO"};texto(titulos[pagina],85,110,27,ORO);
  if(pagina==0){var a=motor.antecedentes;texto(a.estadoCiudad(),90,160,23,CIAN);var c=a.ciudad();String[]n={"Información verificada","Confianza","Convivencia","Bienestar","Participación","Desinformación","Conflictos"};int[]v={c.informacionVerificada(),c.confianza(),c.convivencia(),c.bienestar(),c.participacion(),c.desinformacion(),c.conflictos()};for(int i=0;i<7;i++){texto(n[i]+": "+v[i],90,205+i*43,18,PAPEL);p.fill(i<5?CIAN:ROSA);p.rect(365,189+i*43,v[i]*3,12);}parrafo(String.join("\n",a.decisiones().stream().filter(x->x.startsWith("M4")||x.startsWith("M6")).limit(7).toList()),740,175,430,330,17);}
  else if(pagina==1){dibujarAVL(motor.avl.vista(),640,210,270);texto("Pendientes: "+motor.avl.cantidad()+" · altura: "+motor.avl.altura(),90,155,19,CIAN);parrafo("Contrastar elimina el caso del AVL. Las decisiones permanecen en el árbol y las aclaraciones alcanzan el grafo.",90,505,1080,70,19);}
  else if(pagina==2){var lista=motor.decisiones.recorrer("preorden");int i=0;for(String id:lista){var n=motor.decisiones.buscar(id);texto((n.ejecutado()?"> ":"  ")+n.texto(),95+(i/9)*550,170+(i%9)*43,17,n.ejecutado()?CIAN:PAPEL);i++;}}
  else if(pagina==3){int[][]xy={{200,280},{450,190},{450,410},{700,190},{700,410},{980,190},{980,410}};int[][]ar={{0,1},{0,2},{1,3},{2,4},{3,5},{4,6}};p.stroke(LILA);for(int[]a:ar){p.line(xy[a[0]][0],xy[a[0]][1],xy[a[1]][0],xy[a[1]][1]);texto(">",(xy[a[0]][0]+xy[a[1]][0])/2f,(xy[a[0]][1]+xy[a[1]][1])/2f,24,ORO);}p.noStroke();for(int i=0;i<7;i++){p.fill(motor.difusion.contains(i)?CIAN:LILA);p.ellipse(xy[i][0],xy[i][1],56,56);texto(""+i,xy[i][0]-7,xy[i][1]+7,20,FONDO);}texto("BFS: "+motor.difusion+" · grupos alcanzados por la aclaración",100,520,20,PAPEL);}
  else if(pagina==4){parrafo("J1: A/D mover · Espacio salto doble · F ataque · Q alcance · E protección · R esquiva\nJ2: flechas mover · Mayús salto · L ataque · K alcance · Enter protección · J esquiva\nJ3: numérico 4/6 mover · 0 salto · 1/2/3 ataques · decimal esquiva\nJ4 jefe: Z lluvia · X bots · C abanico · B rayos · N muro · M orbes\nMando J1: stick · A salto · X/Y ataques · LB protección · RB esquiva\nEsc/P pausa · G guardar · H ayuda · T consultas\nLee el aviso dorado, esquiva el ataque activo y aprovecha la recuperación. Los pinchos alternan. En plataformas usa salto doble.",95,160,1080,420,20);}
  else parrafo(String.join("\n",motor.historial.stream().skip(Math.max(0,motor.historial.size()-12)).toList()),95,160,1080,390,18);
  boton("Anterior",90,590,235,()->pagina=Math.floorMod(pagina-1,6));boton("Volver",520,590,235,()->{consulta=false;if(previaControles){previaControles=false;config=true;motor=null;}});boton("Siguiente",950,590,235,()->pagina=(pagina+1)%6);
 }
 private void dibujarAVL(alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones.Vista n,int x,int y,int ancho){if(n==null)return;p.stroke(LILA);if(n.izquierdo()!=null)p.line(x,y,x-ancho,y+110);if(n.derecho()!=null)p.line(x,y,x+ancho,y+110);p.noStroke();marco(x-105,y-27,210,65);texto("#"+n.valor().id()+" · riesgo "+n.valor().riesgo(),x-90,y,17,PAPEL);texto("altura "+n.altura()+" · FE "+n.fe(),x-90,y+22,14,CIAN);dibujarAVL(n.izquierdo(),x-ancho,y+110,ancho/2);dibujarAVL(n.derecho(),x+ancho,y+110,ancho/2);}
 private void sonido(){if(motor.pausa){audio.pausar();return;}audio.reanudar();boolean combate=motor.escena==MotorOcho.Escena.COMBATE;int dano=Arrays.stream(motor.heroes).mapToInt(h->h.danio).sum();if(dano>danioAudio)audio.efecto("dano","mision3/audio/hit",.3f,.2);danioAudio=dano;if(combate&&motor.ataqueTicks>=0&&motor.ataqueTicks<3&&ataqueAudio!=motor.ticks){audio.efecto("aviso","mision3/audio/alert",.3f,2);ataqueAudio=motor.ticks;}}
 public void close(){audio.close();if(red==null&&motor!=null&&!previaControles)try{GuardadoOcho.guardar(motor);}catch(IOException e){error=e.getMessage();}if(red!=null){red.close();red=null;}}
}
