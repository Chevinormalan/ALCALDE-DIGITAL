package alcaldedigital.mision8;
import alcaldedigital.cliente.mision8.*;import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;
public class RedOchoTest {
 @Test void dialogoInyeccionYPausaSeCompartenEntreClientes()throws Exception{
  MotorOcho m=MisionOchoTest.combate(2,false,2,1,0);m.escena=MotorOcho.Escena.MUTACION;m.inyectado=false;
  var lineas=HistoriaOcho.lineas(m);for(int i=0;i<lineas.size();i++)if(lineas.get(i).accion()==HistoriaOcho.Accion.INYECTAR)m.lineaDialogo=i;m.ticksDialogo=0;
  try(RedOcho servidor=RedOcho.alojar(m,0);RedOcho cliente=RedOcho.unir("127.0.0.1",servidor.puerto(),1,"")){
   esperar(servidor::lista);servidor.orden(0,"RESUME");esperar(()->m.inyectado);servidor.orden(0,"PAUSE");
   esperar(()->cliente.vista.get()!=null&&cliente.vista.get().pausa&&cliente.vista.get().ticksDialogo==m.ticksDialogo);
   var copia=cliente.vista.get();assertTrue(copia.inyectado);assertEquals(m.lineaDialogo,copia.lineaDialogo);assertEquals("Abel",HistoriaOcho.actual(copia).quien());assertEquals(EscenografiaOcho.contacto(copia),EscenografiaOcho.puntaAguja(copia));
  }
 }
 private static void esperar(java.util.function.BooleanSupplier b)throws Exception{long limite=System.nanoTime()+4_000_000_000L;while(!b.getAsBoolean()&&System.nanoTime()<limite)Thread.sleep(10);assertTrue(b.getAsBoolean());}
 @Test void cuatroPuestosCompartenEstadoPausaYReconectanSinSuplantar()throws Exception{
  MotorOcho m=MisionOchoTest.combate(1,false,4,1,0);
  try(RedOcho server=RedOcho.alojar(m,0);RedOcho j2=RedOcho.unir("127.0.0.1",server.puerto(),1,"");RedOcho j3=RedOcho.unir("127.0.0.1",server.puerto(),2,"");RedOcho j4=RedOcho.unir("127.0.0.1",server.puerto(),3,"")){
   esperar(()->server.lista()&&j4.vista.get()!=null);assertThrows(java.io.IOException.class,()->RedOcho.unir("127.0.0.1",server.puerto(),2,"incorrecto"));assertTrue(server.lista());
   server.orden(0,"RESUME");j2.mascara=MotorOcho.DER;j3.mascara=MotorOcho.IZQ;j4.mascara=1;
   esperar(()->j2.vista.get()!=null&&j2.vista.get().heroes[1].x>270);assertTrue(m.heroes[0].x<250);assertTrue(m.heroes[2].x<360);
   j3.orden(2,"PAUSE");esperar(()->m.pausa);int tick=m.ticks;Thread.sleep(80);assertEquals(tick,m.ticks);String token=j3.token;j3.close();esperar(()->!server.lista());
   assertThrows(java.io.IOException.class,()->RedOcho.unir("127.0.0.1",server.puerto(),2,""));
   try(RedOcho nuevo=RedOcho.unir("127.0.0.1",server.puerto(),2,token)){esperar(()->server.lista());server.orden(0,"RESUME");esperar(()->m.ticks>tick);}
  }
 }
 @Test void invitadoNoAvanzaDialogoNiAuditaPorElAnfitrion()throws Exception{
  MotorOcho m=new MotorOcho(MisionOchoTest.antecedentes(0,false),2,1,0,false,42);
  try(RedOcho server=RedOcho.alojar(m,0);RedOcho cliente=RedOcho.unir("127.0.0.1",server.puerto(),1,"")){
   esperar(server::lista);server.orden(0,"RESUME");m.reloj=200;cliente.orden(1,"NEXT");Thread.sleep(80);assertEquals(MotorOcho.Escena.OBRA,m.escena);server.orden(0,"NEXT");assertTrue(m.textoRevelado);m.ticksDialogo=200;server.orden(0,"NEXT");assertEquals(1,m.lineaDialogo);assertEquals(MotorOcho.Escena.OBRA,m.escena);
  }
 }
}
