package alcaldedigital.integracion;
import alcaldedigital.cliente.*;
import alcaldedigital.cliente.interfaz.*;
import alcaldedigital.cliente.recursos.*;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InterfazTest {
    @Test void ayudaDelMenuSigueDisponibleTrasJugarMisionUno(){
        var j=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);var r=new RenderJuego();
        j.accion("jugar");j.accion("mision1");j.accion("m1dificultad:FACIL");
        j.abrir(JuegoCliente.Pantalla.MENU);j.accion("ayuda");r.dibujar(j);
        assertTrue(r.botones().stream().anyMatch(b->b.accion().equals("verControles")));
        j.accion("verControles");assertEquals(JuegoCliente.Pantalla.CONTROLES,j.pantalla);
        j.accion("volver");j.accion("volver");assertEquals(JuegoCliente.Pantalla.MENU,j.pantalla);
    }
    @Test void pantallasBotonesYRecursos() {
        var j=new JuegoCliente(SesionLocal::new);var r=new RenderJuego();
        for(var pantalla:JuegoCliente.Pantalla.values()) {
            if(j.sesion==null&&pantalla!=JuegoCliente.Pantalla.MENU&&pantalla!=JuegoCliente.Pantalla.ROLES) j.iniciar(Rol.CIUDADANO);
            j.pantalla=pantalla;var img=r.dibujar(j);assertEquals(1280,img.getWidth());assertEquals(720,img.getHeight());
            for(var b:r.botones()) {assertTrue(b.area().x>=0&&b.area().y>=0);assertTrue(b.area().getMaxX()<=1280&&b.area().getMaxY()<=720);}
        }
    }
    @Test void todosLosFotogramasYTransparencia() {
        var r=new Recursos();
        for(String rol:new String[]{"ciudadano","periodista","influencer","candidato","noti"}) for(int i=0;i<16;i++) {
            var im=r.imagen("actores/"+rol+"_%02d".formatted(i));assertEquals(32,im.getWidth());assertEquals(32,im.getHeight());
            assertEquals(0,im.getRGB(0,0)>>>24);assertTrue(im.getColorModel().hasAlpha());
        }
    }
    @Test void movimientoEntradaYReglas() {
        var j=new JuegoCliente(SesionLocal::new);j.accion("rol:CIUDADANO");float x=j.sesion.vista().x();
        j.teclas.add(68);j.actualizar(.05);assertTrue(j.sesion.vista().x()>x);
        j.accion("civitas");assertTrue(j.teclas.isEmpty());j.accion("verificar");assertEquals(JuegoCliente.Pantalla.PARQUE,j.pantalla);
        j.accion("ayuda");double tiempo=j.sesion.restante();j.actualizar(1);assertEquals(tiempo,j.sesion.restante());
    }
}
