package alcaldedigital.cliente.mision8;

/** Coordenadas compartidas para sprites, contacto del inyector y revisión de las escenas. */
public final class EscenografiaOcho {
 private EscenografiaOcho(){}
 public record Punto(double x,double y){}
 public static Punto candidato(int i){if(i<0||i>3)throw new IllegalArgumentException("Candidato inválido");return new Punto(410+200*i,280);}
 public static Punto elegido(MotorOcho m){return m.escena==MotorOcho.Escena.EPILOGO||m.escena==MotorOcho.Escena.FINAL?new Punto(640,280):candidato(Math.max(0,m.ganador));}
 public static Punto contacto(MotorOcho m){Punto c=elegido(m);return new Punto(c.x+10,c.y-50);}
 public static Punto palomo(MotorOcho m){
  var linea=HistoriaOcho.actual(m);if(linea==null)return m.palomoNeutralizado?new Punto(1050,420):new Punto(1160,430);
  Punto c=elegido(m),cerca=new Punto(c.x+67,c.y-5);double t=m.ticksDialogo;
  return switch(linea.accion()){
   case VUELO->curva(new Punto(1160,430),cerca,t/120);
   case INYECTAR->cerca;
   case RETIRARSE->curva(cerca,new Punto(1160,430),t/90);
   case ESCAPAR->curva(new Punto(1160,430),new Punto(1060,200),t/120);
   case DISPARAR->new Punto(1060,m.palomoNeutralizado?200+230*Math.min(1,(t-60)/45):200);
   default->new Punto(1160,430);
  };
 }
 public static Punto puntaAguja(MotorOcho m){Punto objetivo=contacto(m);double f=Math.min(1,Math.max(0,m.ticksDialogo/35.0));return new Punto(objetivo.x+18*(1-f),objetivo.y);}
 public static Punto pulso(MotorOcho m){Punto origen=m.ganador==0?new Punto(665,230):new Punto(235,400),destino=new Punto(1060,160);return interpolar(origen,destino,Math.max(0,(m.ticksDialogo-15)/45.0));}
 public static Punto interpolar(Punto a,Punto b,double t){t=Math.max(0,Math.min(1,t));return new Punto(a.x+(b.x-a.x)*t,a.y+(b.y-a.y)*t);}
 private static Punto curva(Punto a,Punto b,double t){t=Math.max(0,Math.min(1,t));Punto p=interpolar(a,b,t*t*(3-2*t));return new Punto(p.x,p.y-100*Math.sin(Math.PI*t));}
}
