package alcaldedigital.integracion;

import alcaldedigital.cliente.interfaz.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InterfazPixelTest {
    private BufferedImage cuadro(Color color) {
        var b=new BufferedImage(1280,720,BufferedImage.TYPE_INT_ARGB);
        var g=b.createGraphics();g.setColor(color);g.fillRect(0,0,1280,720);g.dispose();return b;
    }
    @Test void fuenteEmpaquetadaTieneEspanolYMedidasParaSimbolos() {
        var tema=new TemaPixel();var f=tema.fuente(18,false);
        assertEquals(-1,f.canDisplayUpTo("áéíóúüñÁÉÍÓÚÜÑ¿¡"));
        var g=cuadro(Color.BLACK).createGraphics();
        assertTrue(tema.ancho(g,"← Verificación → ✓",f)>100);
        tema.escribir(g,"← Verificación → ✓",4,24,f);g.dispose();
        assertSame(f,tema.fuente(18,false));
    }
    @Test void desplegableBloqueaEntradaHastaTerminarYConservaOrigen() {
        var t=new TransicionPixel();var origen=cuadro(Color.RED);
        t.presentar(origen,"parque",0,false,false);assertTrue(t.lista("parque"));
        assertFalse(t.lista("ayuda"));
        var b=t.presentar(cuadro(Color.BLUE),"ayuda",.05,false,true);
        assertTrue(t.activa());assertFalse(t.lista("ayuda"));
        assertEquals(Color.BLUE.getRGB(),b.getRGB(600,50));
        assertNotEquals(Color.BLUE.getRGB(),b.getRGB(600,700));
        for(int i=0;i<4;i++) t.presentar(cuadro(Color.BLUE),"ayuda",.1,false,true);
        assertTrue(t.lista("ayuda"));
    }
    @Test void movimientoReducidoPresentaDestinoSinEspera() {
        var t=new TransicionPixel();t.presentar(cuadro(Color.RED),"menu",0,false,false);
        var destino=cuadro(Color.BLUE);
        assertSame(destino,t.presentar(destino,"roles",0,true,false));
        assertTrue(t.lista("roles"));assertFalse(t.activa());
    }
    @Test void cierreRapidoDuranteDesplieguePuedeVolverAlOrigen() {
        var t=new TransicionPixel();t.presentar(cuadro(Color.RED),"parque",0,false,false);
        t.presentar(cuadro(Color.BLUE),"ayuda",.05,false,true);
        t.presentar(cuadro(Color.RED),"parque",.05,false,false);
        for(int i=0;i<4;i++) t.presentar(cuadro(Color.RED),"parque",.1,false,false);
        assertTrue(t.lista("parque"));
    }
}
