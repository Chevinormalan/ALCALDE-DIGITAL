package alcaldedigital.cliente.mision6;

import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static alcaldedigital.cliente.mision6.MisionSeisMotor.*;

class MisionSeisEntregaTest {
 private void paso(MisionSeisMotor m,float segundos){for(int i=0;i<(int)(segundos*60);i++)m.actualizar(1f/60);}
 private void confirmar(MisionSeisMotor m){m.pulsar(KeyEvent.VK_ENTER);m.pulsar(KeyEvent.VK_ENTER);}
 private void mantener(MisionSeisMotor m,int k,boolean activa){if(activa)m.pulsar(k);else m.soltar(k);}

 @Test void atlasDelDronContieneQuinceFotogramasConTransparenciaReal()throws Exception{
  try(var in=getClass().getResourceAsStream("/cliente/mision6/dron_atlas.png")){
   assertNotNull(in);var im=ImageIO.read(in);assertTrue(im.getColorModel().hasAlpha());
   int w=im.getWidth()/5,h=im.getHeight()/3;
   for(int fila=0;fila<3;fila++)for(int columna=0;columna<5;columna++){
    int visible=0,transparente=0;
    for(int y=fila*h;y<(fila+1)*h;y++)for(int x=columna*w;x<(columna+1)*w;x++){
     int alpha=im.getRGB(x,y)>>>24;if(alpha>160)visible++;if(alpha<10)transparente++;
    }
    assertTrue(visible>w*h/20,"Fotograma vacío");assertTrue(transparente>w*h/5,"El fondo debe ser transparente");
   }
  }
 }

 @Test void partidaCompletaEnAmbosModosSinReducirArtificialmenteLaVidaDelJefe(){
  for(Modo modo:Modo.values()){
   var m=new MisionSeisMotor();
   for(int i=0;i<DISCURSO.length;i++)confirmar(m);
   m.pulsar(KeyEvent.VK_W);paso(m,.51f);m.soltar(KeyEvent.VK_W);
   m.pulsar(KeyEvent.VK_A);paso(m,2.4f);m.soltar(KeyEvent.VK_A);confirmar(m);
   for(int i=0;i<INTRO.length;i++)confirmar(m);
   m.pulsar(KeyEvent.VK_D);paso(m,3.9f);m.soltar(KeyEvent.VK_D);paso(m,1.5f);m.pulsar(KeyEvent.VK_ENTER);
   assertEquals(Fase.MODO,m.fase);m.pulsar(modo==Modo.SOLO?KeyEvent.VK_1:KeyEvent.VK_2);
   paso(m,5.1f);for(int i=0;i<ADVERTENCIA.length;i++)confirmar(m);
   assertEquals(Fase.ENTRENAMIENTO,m.fase);m.pulsar(KeyEvent.VK_ENTER);
   int fases=0;
   for(int i=0;i<60*100&&m.fase==Fase.COMBATE;i++){
    fases|=1<<m.etapa();var n=m.naves[0];
    float objetivo=282+(float)Math.sin((m.global+1.05f)*(m.etapa()==1?1.3:2.1))*155;
    mantener(m,KeyEvent.VK_W,n.y>objetivo+12);mantener(m,KeyEvent.VK_S,n.y<objetivo-12);
    m.pulsar(modo==Modo.SOLO?KeyEvent.VK_J:KeyEvent.VK_F);
    if(modo==Modo.DUO)m.pulsar(KeyEvent.VK_L);
    if(m.etapa()>1&&n.misil<=0){int k=modo==Modo.SOLO?KeyEvent.VK_K:KeyEvent.VK_G;m.pulsar(k);m.soltar(k);}
    for(var b:m.proyectiles)if(b.enemigo&&b.x>n.x&&b.x-n.x<110&&Math.abs(b.y-n.y)<40){m.pulsar(KeyEvent.VK_SHIFT);m.soltar(KeyEvent.VK_SHIFT);break;}
    m.actualizar(1f/60);
    if(i==120){m=MisionSeisMotor.restaurar(m.capturar());assertNotNull(m);}
   }
   assertEquals(14,fases,"El combate debe recorrer sus tres fases");assertEquals(Fase.RETIRADA,m.fase,modo.toString());
   for(int i=0;i<TRANSMISION.length;i++)confirmar(m);assertEquals(Fase.LLEGADA,m.fase);paso(m,6.1f);assertEquals(Fase.EXPLORAR_SALA,m.fase);m.pulsar(KeyEvent.VK_A);paso(m,.98f);m.soltar(KeyEvent.VK_A);m.pulsar(KeyEvent.VK_W);paso(m,.6f);m.soltar(KeyEvent.VK_W);m.pulsar(KeyEvent.VK_ENTER);confirmar(m);confirmar(m);
   assertEquals(Fase.ENTREVISTA,m.fase);
   for(int i=0;i<3;i++){m.pulsar(modo==Modo.SOLO?KeyEvent.VK_1:KeyEvent.VK_2);confirmar(m);}
   assertEquals(Fase.FINAL,m.fase);assertEquals(modo==Modo.SOLO?Ruta.ESCUCHA:Ruta.CONVICCION,m.ruta());
   m.pulsar(KeyEvent.VK_ENTER);assertTrue(m.resultado().completada());
  }
 }
}
