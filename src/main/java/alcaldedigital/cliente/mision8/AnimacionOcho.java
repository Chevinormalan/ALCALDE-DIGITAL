package alcaldedigital.cliente.mision8;

/** Selección visual derivada del reloj de simulación: pausa, guardado y LAN conservan la pose. */
public final class AnimacionOcho {
 private AnimacionOcho(){}
 public static final int CUADROS_INFECCION=16,CUADROS_ATAQUE=8;
 public static int cuadroInfeccion(int ticks){return Math.max(0,Math.min(15,ticks/6));}
 public static double camaraVisual(MotorOcho m){
  if(m.fase!=5)return m.camara;double alto=612;for(var h:m.heroes)if(h.vida>0)alto=Math.min(alto,h.y);
  return m.camara-110*Math.max(0,Math.min(1,(-alto-250)/200));
 }
 public static int cuadroHeroe(MotorOcho m,int i){
  var h=m.heroes[i];int col=0;
  if(h.vida<=0)col=15;
  else if(h.invulnerable>55&&h.accion>0)col=14;
  else if(h.esquiva>40)col=7;
  else if(h.accion>0){int tecla=m.entradas[i];col=(tecla&MotorOcho.TRES)!=0?13:(tecla&MotorOcho.DOS)!=0?(h.accion>10?11:12):8+Math.min(2,(18-h.accion)/6);}
  else if(h.vy<-.5)col=4;
  else if(h.vy>.5)col=5;
  else if(Math.abs(h.vx)>.01)col=1+(m.ticks/8)%2;
  return m.rol(i)*16+col;
 }
 public static int aviso(MotorOcho m){return m.dificultad==2?45:60;}
 /** Instantes reales de creación de peligros en MotorOcho.ejecutarAtaque(). */
 public static int[] emisiones(MotorOcho m){
  int a=aviso(m);
  return switch(m.ataque){
   case LLUVIA->new int[]{a,a+35,a+70,a+105};
   case BOTS->new int[]{a,a+65,a+130};
   case ABANICO->new int[]{a,a+85};
   case RAYOS->new int[]{a,a+60,a+120};
   case MURO->new int[]{a};
   case ORBES->new int[]{a,a+55,a+110};
  };
 }
 public static int desdeEmision(MotorOcho m){int ultimo=-10000;for(int e:emisiones(m))if(e<=m.ataqueTicks)ultimo=e;if(m.fase==5&&m.ataqueTicks>=190)ultimo=Math.max(ultimo,190);return m.ataqueTicks-ultimo;}
 public static int cuadroJefe(MotorOcho m){
  int t=m.ataqueTicks;if(t<0||m.transicion>0)return 0;
  int edad=desdeEmision(m);if(edad<28)return 4+Math.min(3,edad/7);
  int proximo=10000;for(int e:emisiones(m))if(e>t)proximo=Math.min(proximo,e-t);
  if(t<aviso(m))return Math.min(3,t*4/aviso(m));
  if(proximo<=18)return 1+Math.min(2,(18-proximo)/6);
  return 0;
 }
 public static int filaAtaque(int candidato,int forma){if(candidato<0||candidato>3||forma<1||forma>3)throw new IllegalArgumentException("Forma inválida");return candidato*3+forma-1;}
}
