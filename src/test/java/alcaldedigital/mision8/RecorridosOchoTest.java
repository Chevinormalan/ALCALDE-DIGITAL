package alcaldedigital.mision8;
import alcaldedigital.cliente.mision8.*;import org.junit.jupiter.api.*;import java.util.*;import static org.junit.jupiter.api.Assertions.*;
/** Recorridos con las mismas entradas y colisiones disponibles a una persona. */
class RecorridosOchoTest {
 private static final Map<MotorOcho.Heroe,MotorOcho.Plataforma> destinos=new WeakHashMap<>();
 static int controlar(MotorOcho m,int i){var h=m.heroes[i];double destino=640;
  if(m.fase>=4){var siguiente=destinos.get(h);if(h.saltos==0||siguiente==null){siguiente=m.plataformas().stream().filter(p->p.y()<h.y-12).min(Comparator.comparingDouble(p->Math.abs(p.y()-h.y))).orElse(null);if(siguiente!=null)destinos.put(h,siguiente);}if(siguiente!=null)destino=siguiente.x()+siguiente.ancho()/2;}
  int mascara=MotorOcho.UNO;if(h.x<destino-25)mascara|=MotorOcho.DER;else if(h.x>destino+25)mascara|=MotorOcho.IZQ;
  if(m.ticks%32==0&&h.saltos<2)mascara|=MotorOcho.SALTO;
  double riesgo=99999;MotorOcho.Proyectil amenaza=null;for(var b:m.proyectiles)if(b.dueno<0){double dist=Math.hypot(b.x+b.vx*10-h.x,b.y+b.vy*10-(h.y-34));if(dist<riesgo){riesgo=dist;amenaza=b;}}
  if(riesgo<85&&h.invulnerable==0){if(h.esquiva==0)mascara|=MotorOcho.ESQUIVA;if(amenaza!=null&&m.fase<4){mascara&=~(MotorOcho.IZQ|MotorOcho.DER);mascara|=amenaza.x<h.x?MotorOcho.DER:MotorOcho.IZQ;}if(riesgo<45&&h.recarga==0)mascara|=MotorOcho.TRES;}
  return mascara;
 }
 @Test void todosLosRolesYModalidadesCompletanLasCincoFasesConFisicaReal(){int aprobados=0;
  for(int n=1;n<=4;n++)for(int d=0;d<3;d++)for(int rol=0;rol<(n==1?3:1);rol++){
   MotorOcho m=MisionOchoTest.combate(rol,false,n,d,rol);
   for(int t=0;t<18000&&m.escena==MotorOcho.Escena.COMBATE;t++){
    for(int h=0;h<m.heroes.length;h++)m.entrada(h,controlar(m,h));
    if(m.jefeHumano)m.entrada(n-1,1<<((t/350)%6));m.paso();
   }
   System.out.println("M8 recorrido jugadores="+n+" dificultad="+d+" rol="+rol+" escena="+m.escena+" fase="+m.fase+" vidaJefe="+m.vidaJefe+" vidas="+m.heroes[0].vida+" y="+m.heroes[0].y);
   assertEquals(MotorOcho.Escena.EPILOGO,m.escena,"jugadores "+n+" dificultad "+d+" rol "+rol);aprobados++;
  }
  assertEquals(18,aprobados);
 }
}
