package alcaldedigital.integracion;

import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;
import static org.junit.jupiter.api.Assertions.*;

class MisionUnoInterfazTest {
    @Test void seleccionarMisionPermiteVolverYAplicarLasTresDificultades() {
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        juego.abrir(JuegoCliente.Pantalla.MISIONES);alcaldedigital.pruebas.PartidaPrueba.elegir(juego,1);
        juego.accion("volver");
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        juego.tecla(27,'\u001b');
        assertEquals(JuegoCliente.Pantalla.MENU,juego.pantalla);
        for(var dificultad:SesionMisionUno.Dificultad.values()) {
            juego.abrir(JuegoCliente.Pantalla.MISIONES);alcaldedigital.pruebas.PartidaPrueba.elegir(juego,1);
            juego.accion("m1dificultad:"+dificultad.name());
            assertEquals(JuegoCliente.Pantalla.PARQUE,juego.pantalla);
            assertEquals(dificultad,juego.misionUno.vista().dificultad());
            assertEquals(dificultad.segundos(),juego.misionUno.vista().tiempo());
            juego.accion("menu");
        }
    }

    @Test void laLupaCompletaLosCasosYLosAvisosCalmanALosVecinosATiempo() throws Exception {
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        var render=new RenderJuego();
        juego.abrir(JuegoCliente.Pantalla.MISIONES);alcaldedigital.pruebas.PartidaPrueba.elegir(juego,1);
        juego.accion("m1dificultad:DIFICIL");
        assertEquals(JuegoCliente.Pantalla.PARQUE,juego.pantalla);
        assertTrue(juego.misionUno.investigacion().premium());
        guardarImagen(render.dibujar(juego),"01-llegada");
        cuadros(juego,16);
        for(int i=0;i<4;i++) juego.tecla(69,'e');
        cuadros(juego,37);
        assertEquals(Escena.RUMORES,juego.misionUno.investigacion().escena());
        guardarImagen(render.dibujar(juego),"02-papeles");
        juego.tecla(69,'e');
        assertEquals(Escena.RUMORES,juego.misionUno.investigacion().escena());
        cuadros(juego,48);
        assertEquals(Escena.NINGUNA,juego.misionUno.investigacion().escena());
        click(juego,render,"civitas");
        assertEquals(JuegoCliente.Pantalla.CIVITAS,juego.pantalla);
        click(juego,render,"invest:libreta");

        acercar(juego,229,123);
        juego.tecla(69,'e');
        assertEquals(1,juego.misionUno.investigacion().abierto());
        guardarImagen(render.dibujar(juego),"03-recogida");
        cuadros(juego,13);
        juego.accion("invest:hallazgo:TITULAR");
        assertEquals(0,juego.misionUno.investigacion().evidencias());
        juego.accion("invest:publicacion");
        assertFalse(juego.misionUno.investigacion().publicacion());

        juego.accion("invest:lupa");
        var r=area(juego,render,"invest:hallazgo:TITULAR");
        assertTrue(r.height<35 && r.width<300);
        int x=(int)r.getCenterX(),y=(int)r.getCenterY();
        render.apuntar(-1,-1);
        int normal=render.dibujar(juego).getRGB(x+52,y+14);
        render.apuntar(x,y);
        BufferedImage lente=render.dibujar(juego);
        assertNotEquals(normal,lente.getRGB(x+52,y+14));
        guardarImagen(lente,"04-lupa");
        render.pulsar(juego,x,y);
        int primera=juego.misionUno.vista().puntos();
        juego.accion("invest:hallazgo:TITULAR");
        assertEquals(primera,juego.misionUno.vista().puntos());
        juego.guardarMisionUno();
        juego.accion("menu");juego.accion("jugar");juego.accion("mision1");
        juego.accion("partida:continuar");
        assertEquals(1,juego.misionUno.investigacion().evidencias());
        assertEquals(1,juego.misionUno.investigacion().abierto());
        click(juego,render,"volver");
        juego.tecla(69,'e');cuadros(juego,13);
        assertEquals(1,juego.misionUno.investigacion().ficha(1).encontrados());
        terminarExpediente(juego,render,1);

        acercar(juego,472,140);acercar(juego,472,219);abrirYTerminar(juego,render,2);
        acercar(juego,472,141);acercar(juego,186,145);acercar(juego,186,226);abrirYTerminar(juego,render,3);
        acercar(juego,218,274);acercar(juego,227,329);acercar(juego,258,350);acercar(juego,320,361);abrirYTerminar(juego,render,4);
        assertEquals(totalEvidencias(),juego.misionUno.investigacion().evidencias());
        assertEquals(4,juego.misionUno.vista().resueltas().stream().filter(Boolean::booleanValue).count());
        juego.accion("invest:irVecinos");
        assertEquals(Escena.NINGUNA,juego.misionUno.investigacion().escena());
        acercar(juego,422,362);acercar(juego,422,253);acercar(juego,TABLON_X,TABLON_Y);
        var antes=juego.misionUno.vista().peatones();
        click(juego,render,"interactuar");
        assertEquals(Escena.VECINOS,juego.misionUno.investigacion().escena());
        assertEquals(antes,juego.misionUno.vista().peatones());
        juego.tecla(69,'e');
        assertEquals(Escena.VECINOS,juego.misionUno.investigacion().escena());
        cuadros(juego,22);
        assertEquals(12,juego.misionUno.vista().tiempo(),1e-8);
        guardarImagen(render.dibujar(juego),"09-convocatoria");
        var sesion=(SesionMisionUnoLocal)juego.misionUno;
        juego.misionUno=SesionMisionUnoLocal.restaurar(sesion.capturar());
        var anterior=juego.misionUno.vista().peatones();
        for(int i=0;i<68&&juego.misionUno.investigacion().escena()==Escena.VECINOS;i++) {
            juego.actualizar(.1);
            var actual=juego.misionUno.vista().peatones();
            for(var vecino:VECINOS) {
                var inicio=anterior.get(vecino.id());var fin=actual.get(vecino.id());
                assertTrue(Math.hypot(inicio.x()-fin.x(),inicio.y()-fin.y())<=5.81,"El vecino da pasos visibles");
                assertTrue(Math.hypot(fin.x()-320,fin.y()-229)>47,"El vecino evita la fuente");
            }
            anterior=actual;
        }
        assertEquals(Escena.NINGUNA,juego.misionUno.investigacion().escena(),"Los cuatro llegaron");
        assertFalse(juego.misionUno.investigacion().calmaClasica());

        assertEquals(12,juego.misionUno.vista().tiempo(),1e-8);
        verificarTiempoAgotado(((SesionMisionUnoLocal)juego.misionUno).capturar());
        juego.accion("invest:libreta");
        assertEquals(JuegoCliente.Pantalla.PARQUE,juego.pantalla);

        for(var vecino:VECINOS) {
            var p=juego.misionUno.vista().peatones().get(vecino.id());
            acercar(juego,p.x(),p.y());
            var previo=juego.misionUno.investigacion().ciudadanos();
            double tiempo=juego.misionUno.vista().tiempo();
            juego.tecla(69,'e');
            assertEquals(vecino.id(),juego.misionUno.investigacion().vecino());
            assertEquals(JuegoCliente.Pantalla.PARQUE,juego.pantalla);
            assertEquals(previo,juego.misionUno.investigacion().ciudadanos());
            assertEquals(vecino.pregunta(),juego.misionUno.vista().mensaje());
            cuadros(juego,3);
            assertTrue(juego.misionUno.vista().tiempo()<tiempo);
            if(previo==0) {
                var avance=((SesionMisionUnoLocal)juego.misionUno).capturar();
                double pendiente=juego.misionUno.vista().tiempo();
                juego.misionUno=SesionMisionUnoLocal.restaurar(avance);
                assertEquals(pendiente,juego.misionUno.vista().tiempo(),1e-8);
                assertEquals(1,juego.misionUno.vista().peatones().get(vecino.id()).conversaciones());
                guardarImagen(render.dibujar(juego),"09b-escuchar");
            }
            click(juego,render,"interactuar");
            assertEquals(previo+1,juego.misionUno.investigacion().ciudadanos());
            assertEquals(vecino.reaccion(),juego.misionUno.vista().mensaje());
            juego.accion("invest:calmar:"+vecino.id());
            assertEquals(previo+1,juego.misionUno.investigacion().ciudadanos());
            guardarImagen(render.dibujar(juego),"10-vecino-"+vecino.id());
        }
        assertEquals(Escena.CIERRE,juego.misionUno.investigacion().escena());
        cuadros(juego,28);
        guardarImagen(render.dibujar(juego),"11-cierre");
        cuadros(juego,18);
        assertEquals(JuegoCliente.Pantalla.RESULTADO,juego.pantalla);
        assertTrue(juego.ultimaFila.victoria());
        assertEquals(100,juego.ultimaFila.puntaje());
        assertEquals(3,juego.ultimaFila.senales().get("publicaciones_responsables"));
        assertTrue(juego.progreso.disponible(2));
        guardarImagen(render.dibujar(juego),"12-resultado");
        click(juego,render,"repetirMisionUno");
        assertEquals(JuegoCliente.Pantalla.DIFICULTAD,juego.pantalla);
        juego.accion("volver");
        assertEquals(JuegoCliente.Pantalla.MISIONES,juego.pantalla);
        juego.accion("mision2");
        assertEquals(JuegoCliente.Pantalla.DIFICULTAD_DOS,juego.pantalla);
    }

    @Test void losGuardadosAnterioresMantienenSuRuta() {
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        juego.abrir(JuegoCliente.Pantalla.MISIONES);alcaldedigital.pruebas.PartidaPrueba.elegir(juego,1);
        juego.accion("m1dificultad:NORMAL");
        var estado=((SesionMisionUnoLocal)juego.misionUno).capturar();
        estado.remove("investigacion.premium");
        juego.misionUno=SesionMisionUnoLocal.restaurar(estado);
        assertFalse(juego.misionUno.investigacion().premium());
        cuadros(juego,16);
        for(int i=0;i<4;i++) juego.accion("continuar");
        cuadros(juego,15);
        juego.accion("interactuar");
        acercar(juego,229,123);
        juego.accion("interactuar");juego.accion("invest:lupa");
        for(var pista:caso(1).hallazgos()) juego.accion("invest:hallazgo:"+pista.zona());
        assertFalse(juego.misionUno.investigacion().ficha(1).clasificada());
        juego.accion("invest:contraste");
        juego.accion("invest:comprobar:"+caso(1).razonCorrecta());
        assertTrue(juego.misionUno.investigacion().ficha(1).clasificada());
    }

    private static void verificarTiempoAgotado(java.util.Properties previo) {
        var sesion=SesionMisionUnoLocal.restaurar(previo);
        for(int i=0;i<122;i++) sesion.actualizar(.1);
        assertEquals(0,sesion.vista().tiempo(),1e-8);
        assertEquals(Escena.CIERRE,sesion.investigacion().escena());
        sesion.solicitar("demasiado-tarde","calmar:0");
        assertEquals(0,sesion.vista().calmados());
        for(int i=0;i<44;i++) sesion.actualizar(.1);
        assertEquals(SesionMisionUno.Fase.RESULTADO,sesion.vista().fase());
        assertEquals("NEUTRAL",sesion.vista().desenlace());
        assertEquals(4,sesion.investigacion().clasificados());
        assertFalse(alcaldedigital.cliente.misiones.PuntajesMision.uno(sesion).victoria());
    }

    private static void abrirYTerminar(JuegoCliente j,RenderJuego r,int id) throws Exception {
        j.tecla(69,'e');cuadros(j,13);terminarExpediente(j,r,id);
    }

    private static void terminarExpediente(JuegoCliente j,RenderJuego r,int id) throws Exception {
        assertEquals(id,j.misionUno.investigacion().abierto());
        j.accion("invest:lupa");
        guardarImagen(r.dibujar(j),"05-expediente-"+id);
        for(var pista:caso(id).hallazgos())
            if(!j.misionUno.investigacion().ficha(id).encontro(pista.zona()))
                click(j,r,"invest:hallazgo:"+pista.zona());
        assertTrue(j.misionUno.investigacion().ficha(id).clasificada());
        r.dibujar(j);
        assertFalse(r.botones().stream().anyMatch(b->b.accion().contains("contraste")||b.accion().contains("comprobar")));
        click(j,r,"invest:publicacion");
        cuadros(j,6);
        assertTrue(j.misionUno.investigacion().publicacion());
        assertFalse(j.misionUno.vista().resueltas().get(id-1));
        guardarImagen(r.dibujar(j),"06-publicacion-"+id);
        click(j,r,"invest:comunicar");
        assertTrue(j.misionUno.vista().resueltas().get(id-1));
        int puntos=j.misionUno.vista().puntos();
        j.accion("invest:comunicar");
        assertEquals(puntos,j.misionUno.vista().puntos());
        cuadros(j,7);
        guardarImagen(r.dibujar(j),"07-sello-"+id);
        click(j,r,"volver");
        assertEquals(JuegoCliente.Pantalla.PARQUE,j.pantalla);
    }

    private static java.awt.Rectangle area(JuegoCliente j,RenderJuego r,String accion) {
        r.dibujar(j);
        return r.botones().stream().filter(b->b.accion().equals(accion)).findFirst()
            .orElseThrow(()->new AssertionError("Control ausente: "+accion+" en "+j.pantalla)).area();
    }

    private static void click(JuegoCliente j,RenderJuego r,String accion) {
        var a=area(j,r,accion);
        r.pulsar(j,(int)a.getCenterX(),(int)a.getCenterY());
    }

    private static void guardarImagen(BufferedImage imagen,String nombre) throws Exception {
        String directorio=System.getProperty("ad.capturas.mision1");
        if(directorio==null) return;
        Path dir=Files.createDirectories(Path.of(directorio));
        ImageIO.write(imagen,"png",dir.resolve(nombre+".png").toFile());
    }

    private static void acercar(JuegoCliente j,float x,float y) {
        for(int i=0;i<190;i++) {
            var s=j.misionUno.vista();
            float dx=x-s.x(),dy=y-s.y(),d=(float)Math.hypot(dx,dy);
            if(d<=4) return;
            j.misionUno.mover(dx/d*4,dy/d*4);
            j.actualizar(.025);
        }
        fail("No se alcanzó el punto del parque "+x+", "+y);
    }

    private static void cuadros(JuegoCliente j,int cantidad) {
        for(int i=0;i<cantidad;i++) j.actualizar(.1);
    }
}
