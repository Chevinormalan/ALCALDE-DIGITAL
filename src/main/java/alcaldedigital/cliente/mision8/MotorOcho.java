package alcaldedigital.cliente.mision8;

import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.compartido.estructuras.grafo.GrafoDifusionCivitas;
import alcaldedigital.compartido.modelo.Publicacion;
import alcaldedigital.compartido.carrera.EstadoElectoral;
import java.util.*;

/** Simulación a 60 Hz. La vista y los clientes de red nunca deciden daño o escrutinio. */
public final class MotorOcho {
 public enum Escena { OBRA, REVELACION, AUDITORIA, VOTO, ESCRUTINIO, MUTACION, COMBATE, EPILOGO, FINAL, DERROTA }
 public enum Ataque { LLUVIA, BOTS, ABANICO, RAYOS, MURO, ORBES }
 public static final String[] ROLES={"Sofi","Periodista","Influencer"};
 public static final String[] DOSSIERS={"Compartir no equivale a votar", "El pacto oculto y la elegibilidad", "El anuncio adelantado de Palomo"};
 public static final int IZQ=1,DER=2,SALTO=4,UNO=8,DOS=16,TRES=32,ESQUIVA=64;
 public static final class Heroe {
  public double x,y,vx,vy;public int vida=5,invulnerable,recarga,esquiva,accion,direccion=1,saltos,danio,puntos;
  public Heroe(double x){this.x=x;y=612;}
 }
 public static final class Proyectil {
  public double x,y,vx,vy,r;public int vida,tipo,dueno,edad;public Proyectil(double x,double y,double vx,double vy,double r,int vida,int tipo,int dueno){this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.r=r;this.vida=vida;this.tipo=tipo;this.dueno=dueno;}
 }
 public record Plataforma(double x,double y,double ancho){}
 public final AntecedentesOcho antecedentes;
 public final String id;
 public final int jugadores,dificultad,rolSolo;
 public final boolean jefeHumano;
 public Escena escena=Escena.OBRA;
 public int reloj,ticks,vidaJefe,maxVida,fase=1,forma=1,transicion=0,ataqueTicks=-70,ultimo=-1,repeticiones,ganador=-1,publicado=-1,auditoria,votosEmitidos;
 public double jefeX=930,jefeY=300,camara;
 private int finalPrueba=-1;
 public boolean prueba(){return finalPrueba>=0;}
 public int finalElegido(){return finalPrueba;}
 public synchronized void elegirFinalPrueba(int candidato){if(candidato<0||candidato>3||escena!=Escena.OBRA||ticks!=0)throw new IllegalArgumentException("El final se elige antes de comenzar");finalPrueba=candidato;}
 public boolean pausa,credito,lan;public int[] entradas=new int[4],anteriores=new int[4],votos={-1,-1,-1,-1},dossiers={-1,-1,-1};
 public final Heroe[] heroes;
 public final List<Proyectil> proyectiles=new ArrayList<>();
 public final List<String> historial=new ArrayList<>();
 public final ArbolAVLPublicaciones avl=new ArbolAVLPublicaciones();
 public final ArbolDecisiones decisiones=new ArbolDecisiones("m8","El cierre de Civitas");
 public final GrafoDifusionCivitas grafo=new GrafoDifusionCivitas();
 public final List<Integer> difusion=new ArrayList<>();
 public Ataque ataque=Ataque.LLUVIA;private long azar;
 public int lineaDialogo,ticksDialogo;
 public boolean textoRevelado,inyectado,candidatoRecuperado,palomoNeutralizado;
 public String textoVisible(){var linea=HistoriaOcho.actual(this);if(linea==null)return "";int n=textoRevelado?linea.texto().length():Math.min(linea.texto().length(),(int)(ticksDialogo*.75));return linea.texto().substring(0,n);}
 public boolean textoCompleto(){var l=HistoriaOcho.actual(this);return l==null||textoRevelado||ticksDialogo*.75>=l.texto().length();}
 public boolean accionCompleta(){var l=HistoriaOcho.actual(this);return l==null||ticksDialogo>=l.duracion();}
 public MotorOcho(AntecedentesOcho a,int jugadores,int dificultad,int rolSolo,boolean jefeHumano,long semilla){this(a,jugadores,dificultad,rolSolo,jefeHumano,semilla,UUID.randomUUID().toString());}
 private MotorOcho(AntecedentesOcho a,int jugadores,int dificultad,int rolSolo,boolean jefeHumano,long semilla,String id){
  if(jugadores<1||jugadores>4||dificultad<0||dificultad>2||rolSolo<0||rolSolo>2||jugadores==4&&!jefeHumano||jefeHumano&&jugadores<2)throw new IllegalArgumentException("Configuración inválida");
  antecedentes=a;this.jugadores=jugadores;this.dificultad=dificultad;this.rolSolo=rolSolo;this.jefeHumano=jefeHumano;this.id=id;azar=semilla;
  heroes=new Heroe[jefeHumano?jugadores-1:jugadores];for(int i=0;i<heroes.length;i++)heroes[i]=new Heroe(180+i*90);
  maxVida=vidaJefe=(int)(480*(1+.55*(heroes.length-1))*(dificultad==0?.85:dificultad==2?1.2:1));
  decisiones.insertar("m8","auditoria","Auditar la elección");decisiones.insertar("m8","votacion","Votos ciudadanos");decisiones.insertar("m8","combate","Detener a Palomo");
  for(int i=0;i<3;i++){avl.insertar(new Publicacion(i+1,90-i*10,"Archivo M"+(i==0?1:i==1?7:4),DOSSIERS[i],"PENDIENTE"));decisiones.insertar("auditoria","d"+i,DOSSIERS[i]);decisiones.insertar("d"+i,"d"+i+"v","Contrastar evidencia");decisiones.insertar("d"+i,"d"+i+"o","Continuar sin contrastar");}
 }
 public int rol(int h){return heroes.length==1?rolSolo:h;}
 private int azar(int n){azar=azar*6364136223846793005L+1442695040888963407L;return (int)Long.remainderUnsigned(azar>>>1,n);}
 public synchronized void entrada(int jugador,int mascara){if(jugador<0||jugador>=jugadores)return;entradas[jugador]=mascara&127;}
 public synchronized void pausar(boolean b){pausa=b;Arrays.fill(entradas,0);Arrays.fill(anteriores,0);}
 public synchronized void avanzar(){
  if(pausa)return;var lineas=HistoriaOcho.lineas(this);if(lineas.isEmpty())return;
  if(!textoCompleto()){textoRevelado=true;return;}
  if(!accionCompleta())return;
  if(lineaDialogo+1<lineas.size()){lineaDialogo++;ticksDialogo=0;textoRevelado=false;return;}
  switch(escena){
   case OBRA->cambiar(Escena.REVELACION);
   case REVELACION->cambiar(Escena.AUDITORIA);
   case ESCRUTINIO->cambiar(Escena.MUTACION);
   case MUTACION->{if(inyectado){decisiones.ejecutar("combate");cambiar(Escena.COMBATE);}}
   case EPILOGO->{if(candidatoRecuperado&&palomoNeutralizado)cambiar(Escena.FINAL);}
   default->{}
  }
 }
 private void narrativa(){
  if(HistoriaOcho.actual(this)==null)return;ticksDialogo++;var a=HistoriaOcho.actual(this).accion();
  if(a==HistoriaOcho.Accion.INYECTAR&&ticksDialogo>=35&&!inyectado){inyectado=true;historial.add("Palomo inyectó al candidato elegido: "+ganador);}
  if(a==HistoriaOcho.Accion.RECUPERAR&&ticksDialogo>=70&&!candidatoRecuperado){candidatoRecuperado=true;historial.add("El candidato recuperó el control sin borrar los antecedentes");}
  if(a==HistoriaOcho.Accion.DISPARAR&&ticksDialogo>=60&&!palomoNeutralizado){palomoNeutralizado=true;historial.add("El pulso de Juan neutralizó a Palomo");}
 }
 public synchronized void auditar(int dossier,boolean verificar){if(pausa||escena!=Escena.AUDITORIA||dossier<0||dossier>2||dossiers[dossier]>=0)return;
  dossiers[dossier]=verificar?1:0;if(verificar){auditoria++;avl.eliminar(dossier+1);for(int n:grafo.ordenDesde(dossier==2?2:0))if(!difusion.contains(n))difusion.add(n);}
  decisiones.ejecutar("d"+dossier+(verificar?"v":"o"));historial.add(DOSSIERS[dossier]+": "+(verificar?"contrastada":"sin contrastar"));
  if(Arrays.stream(dossiers).allMatch(x->x>=0))cambiar(Escena.VOTO);
 }
 public synchronized void votar(int jugador,int candidato){if(pausa||escena!=Escena.VOTO||jugador<0||jugador>=jugadores||candidato<0||candidato>3||votos[jugador]>=0||!antecedentes.electoral().elegible(candidato))return;
  votos[jugador]=candidato;votosEmitidos++;String n="v"+jugador;decisiones.insertar("votacion",n,"J"+(jugador+1)+" vota "+candidato);decisiones.ejecutar(n);
  if(votosEmitidos==jugadores){escrutar();cambiar(Escena.ESCRUTINIO);}
 }
 public int respaldo(int candidato){if(!antecedentes.electoral().elegible(candidato)||candidato==0&&(antecedentes.electoral().respaldo(0)<200||antecedentes.ciudad().confianza()<35))return Integer.MIN_VALUE;int v=0;for(int i=0;i<jugadores;i++)if(votos[i]==candidato)v++;return antecedentes.electoral().respaldo(candidato)+v;}
 private void escrutar(){if(prueba()){ganador=publicado=finalPrueba;historial.add("Final de prueba elegido: "+EstadoElectoral.NOMBRES[ganador]+". No modifica la campaña.");return;}List<Integer> mejores=new ArrayList<>();int max=Integer.MIN_VALUE;for(int i=0;i<4;i++){int r=respaldo(i);if(r>max){max=r;mejores.clear();mejores.add(i);}else if(r==max)mejores.add(i);}
  ganador=mejores.get(azar(mejores.size()));if(mejores.size()>1)historial.add("Empate exacto: sorteo público reproducible entre "+mejores);
  publicado=auditoria==3?ganador:3;historial.add("Escrutinio auditado: "+ganador+". Anuncio de Palomo: "+publicado);}
 private void cambiar(Escena e){escena=e;reloj=0;lineaDialogo=ticksDialogo=0;textoRevelado=false;Arrays.fill(entradas,0);Arrays.fill(anteriores,0);}
 public synchronized void reintentar(){if(escena!=Escena.DERROTA)return;historial.add("Equipo derrotado: reintento de combate, escrutinio conservado");for(int i=0;i<heroes.length;i++)heroes[i]=new Heroe(180+i*90);vidaJefe=maxVida;fase=forma=1;transicion=0;ataqueTicks=-70;jefeX=930;jefeY=300;camara=0;proyectiles.clear();cambiar(Escena.COMBATE);}
 public synchronized void paso(){if(pausa)return;ticks++;reloj++;
  narrativa();
  if(escena!=Escena.COMBATE)return;
  int objetivo=vidaJefe>maxVida*.85?1:vidaJefe>maxVida*.70?2:vidaJefe>maxVida*.60?3:vidaJefe>maxVida*.40?4:5;
  if(objetivo>fase&&transicion==0&&ataqueTicks<0){fase++;forma=fase<3?1:fase<5?2:3;transicion=100;proyectiles.clear();ataqueTicks=-65;for(int i=0;i<heroes.length;i++){heroes[i].x=190+i*90;heroes[i].y=612;heroes[i].vy=0;heroes[i].invulnerable=Math.max(heroes[i].invulnerable,110);}historial.add("Fase "+fase+", forma "+forma);}
  if(transicion>0){transicion--;if(transicion==0&&dificultad<2)for(Heroe h:heroes)if(h.vida>0)h.vida=Math.min(5,h.vida+1);return;}
  if(vidaJefe<=0){vidaJefe=0;proyectiles.clear();cambiar(Escena.EPILOGO);return;}
  if(Arrays.stream(heroes).noneMatch(h->h.vida>0)){cambiar(Escena.DERROTA);return;}
  for(int i=0;i<heroes.length;i++)actualizarHeroe(i);
  if(jefeHumano){int m=entradas[jugadores-1];if(ataqueTicks<0&&++ataqueTicks>=0){ataqueTicks=-1;}if(ataqueTicks==-1&&m!=0){int eleccion=Integer.numberOfTrailingZeros(m);iniciarAtaque(Ataque.values()[fase==3?0:eleccion%6]);}}
  else if(++ataqueTicks==0){int n=fase==3?0:azar(6);if(n==ultimo&&repeticiones>=1)n=(n+1+azar(5))%6;iniciarAtaque(Ataque.values()[n]);}
  if(ataqueTicks>=0){if(jefeHumano)ataqueTicks++;ejecutarAtaque();if(ataqueTicks>=240){ataqueTicks=-(dificultad==0?100:65);}}
  actualizarProyectiles();
  if(fase==2||fase==3){int lado=fase==2?(reloj/420)%2:2;for(Heroe h:heroes)if(h.vida>0&&h.y>590&&(lado==0&&h.x<330||lado==1&&h.x>950||lado==2&&(h.x<175||h.x>1105))&&reloj%420>70)herir(h);}
  camara=0;if(fase==5){double top=Arrays.stream(heroes).filter(h->h.vida>0).mapToDouble(h->h.y).min().orElse(612);camara=Math.min(0,top-340);}
  System.arraycopy(entradas,0,anteriores,0,4);
 }
 public void iniciarAtaque(Ataque a){ataque=a;repeticiones=a.ordinal()==ultimo?repeticiones+1:0;ultimo=a.ordinal();ataqueTicks=0;Heroe h=objetivo();jefeX=fase==3?640:new int[]{420,640,900,1080}[azar(4)];jefeY=fase==5?-600:fase==4?Math.max(185,Math.min(340,h.y-120)):300;}
 private Heroe objetivo(){return Arrays.stream(heroes).filter(h->h.vida>0).min(Comparator.comparingDouble(h->Math.abs(h.x-jefeX))).orElse(heroes[0]);}
 public List<Plataforma> plataformas(){List<Plataforma>p=new ArrayList<>();p.add(new Plataforma(0,612,1280));if(fase>=4){for(int i=0;i<(fase==5?12:4);i++)p.add(new Plataforma(110+(i%3)*320,490-i*105,240));}return p;}
 private void actualizarHeroe(int i){Heroe h=heroes[i];if(h.vida<=0)return;int m=entradas[i],nuevas=m&~anteriores[i];
  h.invulnerable=Math.max(0,h.invulnerable-1);h.recarga=Math.max(0,h.recarga-1);h.esquiva=Math.max(0,h.esquiva-1);h.accion=Math.max(0,h.accion-1);
  h.vx=((m&DER)!=0?1:0)-((m&IZQ)!=0?1:0);if(h.vx!=0)h.direccion=h.vx>0?1:-1;
  if((nuevas&SALTO)!=0&&h.saltos<2){h.vy=-12.5;h.saltos++;}
  if((nuevas&ESQUIVA)!=0&&h.esquiva==0){h.esquiva=55;h.invulnerable=Math.max(h.invulnerable,16);h.accion=12;}
  h.x=Math.max(24,Math.min(1256,h.x+h.vx*(h.esquiva>40?10:4.2)));double antes=h.y;h.vy=Math.min(12,h.vy+.43);h.y+=h.vy;
  if(h.vy>=0)for(Plataforma p:plataformas())if(h.x>=p.x-12&&h.x<=p.x+p.ancho+12&&antes<=p.y+.1&&h.y>=p.y){h.y=p.y;h.vy=0;h.saltos=0;break;}
  if(h.y>camara+740){herir(h);Plataforma p=plataformas().stream().filter(q->q.y()<camara+600).max(Comparator.comparingDouble(Plataforma::y)).orElse(new Plataforma(520,612,240));h.x=p.x+p.ancho/2;h.y=p.y;h.vy=0;h.saltos=0;}
  if(h.recarga==0&&(m&(UNO|DOS|TRES))!=0){int tipo=(m&TRES)!=0?2:(m&DOS)!=0?1:0;h.recarga=tipo==2?140:tipo==1?38:24;h.accion=18;int rol=rol(i);
   if(tipo==2){h.invulnerable=Math.max(h.invulnerable,55);if(rol==0)for(Heroe aliado:heroes)if(Math.abs(aliado.x-h.x)<180)aliado.invulnerable=Math.max(aliado.invulnerable,40);if(rol==1)for(Proyectil b:proyectiles)if(b.dueno<0&&Math.hypot(b.x-h.x,b.y-h.y+40)<220)b.vida=0;if(Math.hypot(jefeX-h.x,jefeY-h.y+45)<(rol==2?340:260))danarJefe(rol==2?18:12,i);}
   else{double dx=jefeX-h.x,dy=jefeY-(h.y-40),len=Math.max(1,Math.hypot(dx,dy));double speed=tipo==0?10:8;proyectiles.add(new Proyectil(h.x,h.y-40,dx/len*speed,dy/len*speed,tipo==0?8:14,tipo==0?95:130,10+rol*2+tipo,i));if(rol==2&&tipo==1)for(double delta:new double[]{-.16,.16}){double ang=Math.atan2(dy,dx)+delta;proyectiles.add(new Proyectil(h.x,h.y-40,Math.cos(ang)*speed,Math.sin(ang)*speed,8,100,15,i));}}
  }
 }
 private void danarJefe(int n,int jugador){if(transicion>0||escena!=Escena.COMBATE||fase==5&&heroes[jugador].y> -300)return;vidaJefe=Math.max(0,vidaJefe-n);heroes[jugador].puntos+=n;}
 public void herir(Heroe h){if(h.vida<=0||h.invulnerable>0)return;h.vida--;h.danio++;h.invulnerable=dificultad==0?110:80;h.accion=22;}
 private void crear(double x,double y,double vx,double vy,int tipo){proyectiles.add(new Proyectil(x,y,vx,vy,tipo==3?15:11,240,tipo,-1));}
 private void ejecutarAtaque(){int t=ataqueTicks;int aviso=dificultad==2?45:60;
  switch(ataque){
   case LLUVIA->{if(t>=aviso&&t<=180&&(t-aviso)%35==0){int hueco=azar(5);for(int i=0;i<6;i++)if(i!=hueco)crear(100+i*210,camara+120,0,4.5+dificultad,0);}}
   case BOTS->{if(t>=aviso&&(t-aviso)%65==0&&t<210){boolean iz=(t/65)%2==0;for(int i=0;i<3;i++)crear(iz?-30:1310,camara+300+i*110,iz?5:-5,0,1);}}
   case ABANICO->{if(t==aviso||t==aviso+85){for(int i=0;i<7;i++){double a=Math.PI*.17+i*Math.PI*.11;crear(jefeX,jefeY,Math.cos(a)*5,Math.sin(a)*5,1);}}}
   case RAYOS->{int ciclo=(t-aviso)%60;if(t>=aviso&&t<aviso+180&&ciclo==0){Heroe h=objetivo();crear(h.x,camara+70,0,10,2);}}
   case MURO->{if(t==aviso){double hueco=camara+430;for(int y=180;y<650;y+=55)if(Math.abs(camara+y-hueco)>85)crear(1290,camara+y,-4.8,0,2);}}
   case ORBES->{if(t==aviso||t==aviso+55||t==aviso+110){Heroe h=objetivo();double dx=h.x-jefeX,dy=h.y-40-jefeY,len=Math.max(1,Math.hypot(dx,dy));crear(jefeX,jefeY,dx/len*3.2,dy/len*3.2,3);}}
  }
  if(fase==5&&t==190){Heroe h=objetivo();crear(h.x,camara+40,0,8,2);}
 }
 private void actualizarProyectiles(){Iterator<Proyectil>it=proyectiles.iterator();while(it.hasNext()){Proyectil b=it.next();if(b.vida<=0){it.remove();continue;}b.edad++;b.vida--;
  if(b.tipo==3&&b.edad<75){Heroe h=objetivo();double dx=h.x-b.x,dy=h.y-35-b.y,len=Math.max(1,Math.hypot(dx,dy));b.vx=b.vx*.92+dx/len*.28;b.vy=b.vy*.92+dy/len*.28;}
  b.x+=b.vx;b.y+=b.vy;
  if(b.dueno>=0&&Math.hypot(b.x-jefeX,b.y-jefeY)<75+b.r){danarJefe(b.tipo%2==0?9:16,b.dueno);b.vida=0;}
  if(b.dueno<0)for(Heroe h:heroes)if(h.vida>0&&Math.abs(b.x-h.x)<17+b.r&&Math.abs(b.y-(h.y-34))<30+b.r){herir(h);b.vida=0;}
  if(b.vida<=0||b.x< -100||b.x>1380||b.y>camara+850||b.y< -1800)it.remove();
 }}
 public String epilogo(){String elegido=alcaldedigital.compartido.carrera.EstadoElectoral.NOMBRES[Math.max(0,ganador)];String apertura=ganador==0?"Juan recupera el control: las promesas se cumplen escuchando y verificando.":elegido+" se libera de la intervención. Juan ayuda a detener a Palomo y respeta el escrutinio.";
  String ciudad=switch(antecedentes.estadoCiudad()){case "CONFIANZA CÍVICA"->"La ciudadanía conserva las pruebas y protege una elección legítima.";case "CIUDAD EN CRISIS"->"La amenaza terminó, pero quedan rumores y conflictos. La ciudad exige reparación y rendición de cuentas.";default->"La ciudad comienza a reconstruir sus redes y su confianza con ayuda de sus habitantes.";};
  return apertura+" "+ciudad+(antecedentes.electoral().juanCorrupto()?" El pacto de Juan permanece registrado; salvar la ciudad no lo vuelve elegible.":"")+" Palomo queda neutralizado y sus evidencias se conservan.";}
 public synchronized Properties capturar(){Properties p=antecedentes.capturar();p.setProperty("dialogo.linea",""+lineaDialogo);p.setProperty("dialogo.ticks",""+ticksDialogo);p.setProperty("dialogo.revelado",""+textoRevelado);p.setProperty("inyectado",""+inyectado);p.setProperty("recuperado",""+candidatoRecuperado);p.setProperty("palomoNeutralizado",""+palomoNeutralizado);p.setProperty("finalPrueba",""+finalPrueba);p.setProperty("version","m8-1");p.setProperty("id",id);p.setProperty("escena",escena.name());p.setProperty("pausa",""+pausa);p.setProperty("jefeHumano",""+jefeHumano);p.setProperty("azar",""+azar);p.setProperty("credito",""+credito);p.setProperty("lan",""+lan);p.setProperty("historial",String.join("\n",historial));
  int[] v={jugadores,dificultad,rolSolo,reloj,ticks,vidaJefe,maxVida,fase,forma,transicion,ataqueTicks,ultimo,repeticiones,ganador,publicado,auditoria,votosEmitidos,ataque.ordinal()};for(int i=0;i<v.length;i++)p.setProperty("n."+i,""+v[i]);p.setProperty("jefeX",""+jefeX);p.setProperty("jefeY",""+jefeY);p.setProperty("camara",""+camara);
  for(int i=0;i<4;i++){p.setProperty("voto."+i,""+votos[i]);p.setProperty("entrada."+i,""+entradas[i]);p.setProperty("anterior."+i,""+anteriores[i]);}for(int i=0;i<3;i++)p.setProperty("dossier."+i,""+dossiers[i]);
  for(int i=0;i<heroes.length;i++){Heroe h=heroes[i];p.setProperty("h."+i,String.join(",",Arrays.stream(new double[]{h.x,h.y,h.vx,h.vy,h.vida,h.invulnerable,h.recarga,h.esquiva,h.accion,h.direccion,h.saltos,h.danio,h.puntos}).mapToObj(Double::toString).toList()));}
  p.setProperty("balas",""+proyectiles.size());for(int i=0;i<proyectiles.size();i++){Proyectil b=proyectiles.get(i);p.setProperty("b."+i,String.join(",",Arrays.stream(new double[]{b.x,b.y,b.vx,b.vy,b.r,b.vida,b.tipo,b.dueno,b.edad}).mapToObj(Double::toString).toList()));}return p;
 }
 private static double[] numeros(Properties p,String k,int n){double[]v=Arrays.stream(Objects.requireNonNull(p.getProperty(k),k).split(",")).mapToDouble(Double::parseDouble).toArray();if(v.length!=n||Arrays.stream(v).anyMatch(x->!Double.isFinite(x)||Math.abs(x)>1000000))throw new IllegalArgumentException("Snapshot inválido: "+k);return v;}
 public static MotorOcho restaurar(Properties p){if(!"m8-1".equals(p.getProperty("version")))throw new IllegalArgumentException("Versión desconocida");int[]n=new int[18];for(int i=0;i<n.length;i++)n[i]=AntecedentesOcho.entero(p,"n."+i,-10000,10000000);
  MotorOcho m=new MotorOcho(AntecedentesOcho.restaurar(p),n[0],n[1],n[2],Boolean.parseBoolean(p.getProperty("jefeHumano")),Long.parseLong(p.getProperty("azar")),p.getProperty("id"));m.escena=Escena.valueOf(p.getProperty("escena"));m.reloj=n[3];m.ticks=n[4];m.vidaJefe=n[5];m.maxVida=n[6];m.fase=n[7];m.forma=n[8];m.transicion=n[9];m.ataqueTicks=n[10];m.ultimo=n[11];m.repeticiones=n[12];m.ganador=n[13];m.publicado=n[14];m.auditoria=n[15];m.votosEmitidos=n[16];m.ataque=Ataque.values()[n[17]];
  if(m.fase<1||m.fase>5||m.forma<1||m.forma>3||m.vidaJefe<0||m.maxVida<1||m.vidaJefe>m.maxVida||m.ganador< -1||m.ganador>3)throw new IllegalArgumentException("Estado fuera de límites");
  m.jefeX=numerosDouble(p,"jefeX");m.jefeY=numerosDouble(p,"jefeY");m.camara=numerosDouble(p,"camara");
  for(int i=0;i<3;i++){int d=AntecedentesOcho.entero(p,"dossier."+i,-1,1);if(d>=0){m.dossiers[i]=d;m.decisiones.ejecutar("d"+i+(d==1?"v":"o"));if(d==1){m.avl.eliminar(i+1);for(int x:m.grafo.ordenDesde(i==2?2:0))if(!m.difusion.contains(x))m.difusion.add(x);}}}
  for(int i=0;i<4;i++){m.votos[i]=AntecedentesOcho.entero(p,"voto."+i,-1,3);m.entradas[i]=AntecedentesOcho.entero(p,"entrada."+i,0,127);m.anteriores[i]=AntecedentesOcho.entero(p,"anterior."+i,0,127);if(m.votos[i]>=0){m.decisiones.insertar("votacion","v"+i,"J"+(i+1)+" vota "+m.votos[i]);m.decisiones.ejecutar("v"+i);}}
  if(m.escena.ordinal()>=Escena.COMBATE.ordinal())m.decisiones.ejecutar("combate");
  for(int i=0;i<m.heroes.length;i++){double[]v=numeros(p,"h."+i,13);Heroe h=m.heroes[i];h.x=v[0];h.y=v[1];h.vx=v[2];h.vy=v[3];h.vida=(int)v[4];h.invulnerable=(int)v[5];h.recarga=(int)v[6];h.esquiva=(int)v[7];h.accion=(int)v[8];h.direccion=(int)v[9];h.saltos=(int)v[10];h.danio=(int)v[11];h.puntos=(int)v[12];if(h.vida<0||h.vida>5)throw new IllegalArgumentException("Vida inválida");}
  int cantidad=AntecedentesOcho.entero(p,"balas",0,2048);for(int i=0;i<cantidad;i++){double[]v=numeros(p,"b."+i,9);Proyectil b=new Proyectil(v[0],v[1],v[2],v[3],v[4],(int)v[5],(int)v[6],(int)v[7]);b.edad=(int)v[8];m.proyectiles.add(b);}
  m.lineaDialogo=Integer.parseInt(p.getProperty("dialogo.linea","0"));m.ticksDialogo=Integer.parseInt(p.getProperty("dialogo.ticks","0"));
  if(m.lineaDialogo<0||m.lineaDialogo>=Math.max(1,HistoriaOcho.lineas(m).size())||m.ticksDialogo<0||m.ticksDialogo>10000000)throw new IllegalArgumentException("Diálogo inválido");
  m.textoRevelado=Boolean.parseBoolean(p.getProperty("dialogo.revelado"));m.inyectado=Boolean.parseBoolean(p.getProperty("inyectado",m.escena.ordinal()>=Escena.COMBATE.ordinal()?"true":"false"));m.candidatoRecuperado=Boolean.parseBoolean(p.getProperty("recuperado",m.escena==Escena.FINAL?"true":"false"));m.palomoNeutralizado=Boolean.parseBoolean(p.getProperty("palomoNeutralizado",m.escena==Escena.FINAL?"true":"false"));
  m.finalPrueba=Integer.parseInt(p.getProperty("finalPrueba","-1"));if(m.finalPrueba< -1||m.finalPrueba>3)throw new IllegalArgumentException("Final inválido");m.pausa=Boolean.parseBoolean(p.getProperty("pausa"));m.credito=Boolean.parseBoolean(p.getProperty("credito"));m.lan=Boolean.parseBoolean(p.getProperty("lan"));String hist=p.getProperty("historial","");if(!hist.isEmpty())m.historial.addAll(List.of(hist.split("\n")));return m;
 }
 private static double numerosDouble(Properties p,String k){double d=Double.parseDouble(p.getProperty(k));if(!Double.isFinite(d)||Math.abs(d)>100000)throw new IllegalArgumentException("Coordenada inválida");return d;}
}
