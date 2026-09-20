package alcaldedigital.integracion;

import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.cliente.visualizadores.ConsultaEstructuras;
import alcaldedigital.compartido.modelo.Rol;
import alcaldedigital.servidor.sesion.SesionLocal;
import org.junit.jupiter.api.Test;
import java.util.List;
import static alcaldedigital.cliente.JuegoCliente.Pantalla.*;
import static org.junit.jupiter.api.Assertions.*;

class ExperienciaArbolesTest {
    private JuegoCliente juego() {var j=new JuegoCliente(SesionLocal::new);j.iniciar(Rol.CIUDADANO);return j;}
    private void mover(JuegoCliente j,int x,int y) {
        for(int i=0;i<1000&&Math.abs(j.sesion.vista().x()-x)>.5;i++) j.sesion.mover(Math.signum(x-j.sesion.vista().x())*2,0);
        for(int i=0;i<1000&&Math.abs(j.sesion.vista().y()-y)>.5;i++) j.sesion.mover(0,Math.signum(y-j.sesion.vista().y())*2);
        assertEquals(x,j.sesion.vista().x(),1);assertEquals(y,j.sesion.vista().y(),1);
    }
    private void investigar(JuegoCliente j) {
        j.accion("verificar");mover(j,192,174);j.accion("interactuar");
        mover(j,230,208);j.accion("interactuar");
    }
    @Test void cerrarConTVolverYEscConservaOrigenYSeleccion() {
        for(var origen:List.of(PARQUE,CIVITAS,EVIDENCIAS,RESULTADO)) for(String cierre:List.of("T","Esc","volver")) {
            var j=juego();j.abrir(origen);var antes=j.sesion.vista();j.tecla(84,'t');
            assertEquals(ARBOLES,j.pantalla);assertFalse(j.laboratorio);
            j.accion("laboratorio");j.accion("avlAnterior");j.accion("camino");j.accion("narAnterior");
            int avl=j.pasoAVL,nar=j.pasoDecision;
            if(cierre.equals("T")) j.tecla(84,'t');else if(cierre.equals("Esc")) j.tecla(27,(char)27);else j.accion("volver");
            assertEquals(origen,j.pantalla);assertEquals(antes,j.sesion.vista());
            j.tecla(84,'t');assertEquals(avl,j.pasoAVL);assertEquals(nar,j.pasoDecision);
            assertEquals(JuegoCliente.Pestana.CAMINO,j.pestana);assertTrue(j.laboratorio);
        }
    }
    @Test void ayudaAnidadaYPuntosDeAccesoNoPierdenElOrigen() {
        var j=juego();j.abrir(CIVITAS);j.accion("arboles");j.accion("ayuda");j.accion("volver");
        assertEquals(ARBOLES,j.pantalla);j.accion("volver");assertEquals(CIVITAS,j.pantalla);
        for(var origen:List.of(MENU,AYUDA,RESULTADO)) {
            j.abrir(origen);j.accion("laboratorio");assertTrue(j.laboratorio);j.accion("volver");assertEquals(origen,j.pantalla);
        }
    }
    @Test void fuentesGeneranAvisoRealSinDuplicarloNiRevelarloAntes() {
        var j=juego();j.accion("verificar");
        assertEquals(30,j.sesion.buscarPublicacion(1).riesgo());assertFalse(j.aviso.contains("30 → 8"));
        assertFalse(j.aviso.contains("está disponible"));
        mover(j,192,174);j.accion("interactuar");assertNull(j.sesion.buscarDecision("contexto"));
        mover(j,230,208);j.accion("interactuar");
        assertTrue(j.aviso.contains("30 → 8"));assertTrue(j.aviso.contains("Publicar contexto está disponible"));
        int eventos=j.consulta.eventos().size();j.accion("interactuar");assertEquals(eventos,j.consulta.eventos().size());
        j.avisos=false;j.actualizar(.1);assertTrue(j.consulta.ultimoCambio(true).radar().contains("30 → 8"));
        for(String orden:List.of("inorden","preorden","postorden","inverso"))
            assertEquals(j.sesion.recorridoAVL(orden),ConsultaEstructuras.publicaciones(j.sesion.vista().avl(),orden));
        for(String orden:List.of("preorden","postorden","niveles"))
            assertEquals(j.sesion.recorridoDecisiones(orden),ConsultaEstructuras.decisiones(j.sesion.vista().decisiones(),orden));
    }
    @Test void radarSimpleSiempreActualAunqueElLaboratorioConserveUnEstadoViejo() {
        var j=juego();j.accion("arboles");j.accion("laboratorio");j.pasoAVL=0;j.accion("volver");
        investigar(j);j.accion("contexto");j.accion("arboles");j.accion("jugador");
        assertNull(j.sesion.buscarPublicacion(1));assertNotNull(j.sesion.buscarPublicacion(4));
        assertEquals(0,j.pasoAVL);assertEquals(1,j.vistaAVL().valor().id());
        assertTrue(j.consulta.ultimoCambio(true).radar().contains("Entró #4"));
        var r=new RenderJuego();r.dibujar(j);
        assertFalse(r.botones().stream().anyMatch(b->b.accion().equals("buscar")||b.accion().equals("avlAnterior")));
        j.accion("laboratorio");j.busqueda="1";j.accion("buscar");assertTrue(j.encontrado.contains("riesgo 30"));
        j.accion("actual");j.accion("buscar");assertEquals("No existe en este estado",j.encontrado);
        j.busqueda="4";j.accion("buscar");assertTrue(j.encontrado.contains("riesgo 12"));
        assertEquals(List.of(1L),ConsultaEstructuras.publicaciones(j.sesion.pasosAVL().get(0).raiz(),"inorden").stream().map(p->p.id()).toList());
    }
    @Test void buscarNodoHistoricoPodadoYEventosCubrenTodasLasOperaciones() {
        var j=juego();investigar(j);int antes=j.sesion.pasosDecisiones().size()-1;j.accion("contexto");
        assertNull(j.sesion.buscarDecision("guardar"));j.accion("laboratorio");j.accion("camino");j.pasoDecision=antes;
        j.accion("nodo:guardar");assertEquals("guardar: Guardar evidencia",j.encontrado);
        for(int i=0;i<j.sesion.pasosAVL().size();i++) assertNotNull(j.consulta.eventoDelPaso(true,i));
        for(int i=0;i<j.sesion.pasosDecisiones().size();i++) assertNotNull(j.consulta.eventoDelPaso(false,i));
        assertEquals("Inicio del caso",j.consulta.eventoDelPaso(true,2).causa());
        assertEquals("Publicar contexto",j.consulta.eventoDelPaso(false,j.sesion.pasosDecisiones().size()-1).causa());
    }
    @Test void campoDeBusquedaFuncionaPorTecladoYMouseSinCapturaGlobal() {
        var j=juego();var r=new RenderJuego();j.accion("laboratorio");r.dibujar(j);
        j.tecla(49,'1');assertEquals("",j.busqueda);
        for(int i=0;i<r.botones().size()&&!j.editandoBusqueda;i++) r.avanzarFoco(j,1);
        assertTrue(j.editandoBusqueda);j.tecla(49,'1');j.tecla(50,'2');j.tecla(37,(char)0);j.tecla(8,'\b');
        assertEquals("2",j.busqueda);j.tecla(127,(char)127);assertEquals("",j.busqueda);
        j.tecla(49,'1');r.activarFoco(j);assertTrue(j.encontrado.contains("#1"));
        r.avanzarFoco(j,1);assertFalse(j.editandoBusqueda);r.avanzarFoco(j,-1);assertTrue(j.editandoBusqueda);
        var campo=r.botones().stream().filter(b->b.accion().equals("campoBusqueda")).findFirst().orElseThrow();
        r.pulsar(j,campo.area().x+5,campo.area().y+5);assertTrue(j.editandoBusqueda);
        j.busqueda="9999999999999999999";j.accion("buscar");assertTrue(j.encontrado.startsWith("Escribe"));
        j.tecla(84,'t');assertEquals(PARQUE,j.pantalla);
    }
    @Test void relojPausadoConsultaInocuaYMovimientoReducido() {
        var j=juego();j.accion("laboratorio");var antes=j.sesion.vista();double restante=j.sesion.restante();
        j.automatico=true;j.movimientoReducido=true;int visita=j.visita;
        for(int i=0;i<100;i++) j.actualizar(.1);
        assertEquals(visita,j.visita);assertEquals(restante,j.sesion.restante());assertEquals(antes,j.sesion.vista());
        j.accion("visitar");assertEquals(visita+1,j.visita);j.accion("volver");
        for(int i=0;i<601;i++) j.actualizar(.1);
        assertEquals(RESULTADO,j.pantalla);assertEquals("Plazo agotado: ignorar",j.consulta.ultimo().causa());
        assertTrue(j.consulta.ultimo().camino().contains("caso cerró"));
    }
    @Test void ambosModosYPestanasTienenControlesLegiblesSinSolaparse() {
        var j=juego();j.accion("compartir");investigar(j);j.accion("arboles");var r=new RenderJuego();
        for(boolean contraste:List.of(false,true)) for(boolean lab:List.of(false,true)) for(var tab:JuegoCliente.Pestana.values()) {
            j.contraste=contraste;j.laboratorio=lab;j.pestana=tab;
            var im=r.dibujar(j);var g=im.createGraphics();g.setFont(new java.awt.Font("SansSerif",java.awt.Font.BOLD,15));
            var controles=r.botones();
            for(int i=0;i<controles.size();i++) {
                var b=controles.get(i);assertTrue(b.area().x>=0&&b.area().getMaxX()<=1280);
                assertTrue(b.area().y>=0&&b.area().getMaxY()<=720);
                if(!b.accion().startsWith("nodo:")) assertTrue(g.getFontMetrics().stringWidth(b.texto())<=b.area().width-30,b.texto());
                for(int k=i+1;k<controles.size();k++) assertFalse(b.area().intersects(controles.get(k).area()),b.accion()+" / "+controles.get(k).accion());
            }
            g.dispose();
        }
    }
    @Test void nuevoCasoReiniciaSoloLaConsultaDeLaPartidaAnterior() {
        var j=juego();investigar(j);j.accion("laboratorio");j.accion("camino");j.accion("narAnterior");
        j.iniciar(Rol.PERIODISTA);assertFalse(j.laboratorio);assertEquals(JuegoCliente.Pestana.RADAR,j.pestana);
        assertEquals(-1,j.pasoDecision);assertEquals(1,j.consulta.eventos().size());assertEquals("",j.aviso);
        assertEquals(PARQUE,j.pantalla);
    }
}
