package alcaldedigital.reglas;

import alcaldedigital.compartido.modelo.*;
import alcaldedigital.compartido.protocolo.*;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.i1.ValidadorI1;
import alcaldedigital.servidor.sesion.SesionLocal;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Comprueba consecuencias, reintentos y conservación del estado del capítulo. */
class IntegracionAportesTest {
    @Test void matrizDeConsecuenciasConservaLosSieteIndicadoresEnCadaVarianteYRol() {
        for (Rol rol : Rol.values()) for (int variante=0; variante<4; variante++) {
            comprobar(rol,variante,false,false,"compartir",-6,new EstadoCiudad(50,46,47,48,51,30,25),false);
            comprobar(rol,variante,false,false,"reportar",-2,new EstadoCiudad(50,48,49,49,47,20,21),true);
            comprobar(rol,variante,false,false,"ignorar",0,new EstadoCiudad(50,50,50,51,48,22,20),true);
            comprobar(rol,variante,true,false,"contexto",25,new EstadoCiudad(58,55,54,53,54,10,16),true);
            comprobar(rol,variante,true,false,"guardar",15,new EstadoCiudad(54,51,50,51,50,20,20),true);
            comprobar(rol,variante,true,false,"reportar",18,new EstadoCiudad(55,53,51,52,52,12,17),true);
            comprobar(rol,variante,true,true,"rectificar",8,new EstadoCiudad(53,49,49,50,53,25,23),true);
        }
    }

    private void comprobar(Rol rol,int variante,boolean investigar,boolean compartir,String accion,
                            int puntos,EstadoCiudad ciudad,boolean terminado) {
        var s=new SesionLocal(rol,variante,60);
        if(compartir) s.solicitar("previa","compartir");
        if(investigar) SesionLocalTest.investigar(s);
        var r=s.solicitarDetallado("final",accion);
        assertTrue(r.aceptada());
        assertEquals(puntos,s.vista().puntos());assertEquals(ciudad,s.vista().ciudad());
        assertEquals(terminado,s.vista().terminado());assertEquals(s.vista().version(),r.version());
        assertTrue(ValidadorI1.validarAVL(s.vista().avl()).correcto());
        assertTrue(ValidadorI1.validarNario(s.vista().decisiones()).correcto());
        var registro=s.obtenerRegistro("final");
        assertEquals(ciudad,registro.ciudadDespues());assertEquals(puntos,registro.puntosDespues());
        var vista=s.vista();var avl=s.pasosAVL();var nario=s.pasosDecisiones();var registros=s.registrosAcciones();
        assertSame(r,s.solicitarDetallado("final","otra acción"));
        assertEquals(vista,s.vista());assertEquals(avl,s.pasosAVL());assertEquals(nario,s.pasosDecisiones());
        assertEquals(registros,s.registrosAcciones());
    }

    @Test void rechazosYReintentosCompartenCacheSinEfectosNiVersionNueva() {
        var s=new SesionLocal(Rol.CIUDADANO,0,60);
        var rechazado=s.solicitarDetallado("bloqueada","contexto");
        assertFalse(rechazado.aceptada());assertEquals(0,rechazado.version());
        assertEquals(rechazado.mensaje(),s.solicitar("bloqueada","contexto"));
        s.solicitar("compartida","compartir");
        var vista=s.vista();
        assertSame(rechazado,s.solicitarDetallado("bloqueada","contexto"));
        assertEquals(vista,s.vista()); // Reintentar no restaura un mensaje antiguo en el HUD.
        assertFalse(s.solicitarDetallado("otra","compartir").aceptada());
        assertEquals(1,s.vista().version());assertEquals(-6,s.vista().puntos());
        assertEquals(List.of("bloqueada","compartida","otra"),s.registrosAcciones().stream().map(RegistroAccion::requestId).toList());
        assertThrows(UnsupportedOperationException.class,()->s.registrosAcciones().clear());
        assertThrows(IllegalArgumentException.class,()->s.solicitarDetallado("", "ignorar"));
        assertThrows(IllegalArgumentException.class,()->s.solicitarDetallado("nula", null));
        assertEquals(3,s.registrosAcciones().size());
    }

    @Test void configuracionConservaConstructoresSemillaYPlazo() {
        var cfg=new ConfiguracionSesion(Rol.PERIODISTA,20260917L,60);
        var a=new SesionLocal(cfg);var b=new SesionLocal(Rol.PERIODISTA,20260917L,60);
        SesionLocalTest.investigar(a);SesionLocalTest.investigar(b);
        assertEquals(a.vista(),b.vista());assertEquals(cfg,a.configuracion());
        assertEquals(60,a.restante());
        for(int i=0;i<4;i++) {
            var fija=new SesionLocal(new ConfiguracionSesion(Rol.CIUDADANO,777L,10,i));
            var anterior=new SesionLocal(Rol.CIUDADANO,i,10);
            SesionLocalTest.investigar(fija);SesionLocalTest.investigar(anterior);
            assertEquals(anterior.vista(),fija.vista());
        }
        assertThrows(IllegalArgumentException.class,()->new ConfiguracionSesion(Rol.CIUDADANO,1L,9));
        assertThrows(IllegalArgumentException.class,()->new ConfiguracionSesion(Rol.CIUDADANO,1L,10,4));
    }

    @Test void evidenciasSoloSeExponenDespuesDeObtenerlasYConservanElTexto() {
        var s=new SesionLocal(Rol.CIUDADANO,3,60);
        assertTrue(s.evidenciasObtenidas().isEmpty());
        s.solicitar("lejos","aviso");assertTrue(s.evidenciasObtenidas().isEmpty());
        SesionLocalTest.investigar(s);
        assertEquals(List.of("ev-aviso","ev-testimonio"),s.evidenciasObtenidas().stream().map(Evidencia::id).toList());
        assertEquals(s.vista().evidencias(),s.evidenciasObtenidas().stream().map(Evidencia::contenido).toList());
        assertTrue(s.vista().historial().contains("Evidencia: aviso · +5 puntos"));
        assertTrue(s.vista().historial().contains("Evidencia: fuente · +5 puntos"));
        assertThrows(UnsupportedOperationException.class,()->s.evidenciasObtenidas().clear());
    }

    @Test void plazoYRechazoTrasCierreQuedanRegistradosSinCambiarConsecuencias() {
        var s=new SesionLocal(Rol.CANDIDATO,0,10);s.actualizar(11);
        var registro=s.obtenerRegistro("plazo-agotado");
        assertTrue(registro.resultado().aceptada());assertEquals("ignorar",registro.accion());
        var antes=s.vista();var avl=s.pasosAVL();var nar=s.pasosDecisiones();
        assertFalse(s.solicitarDetallado("tarde","verificar").aceptada());
        assertEquals(antes.puntos(),s.vista().puntos());assertEquals(antes.ciudad(),s.vista().ciudad());
        assertEquals(antes.version(),s.vista().version());
        assertEquals(avl,s.pasosAVL());assertEquals(nar,s.pasosDecisiones());
    }

    @Test void validadoresDetectanEstadosInvalidosYNoModificanLosArboles() {
        var p=new Publicacion(1,20,"a","texto","Sin comprobar");
        assertFalse(ValidadorI1.validarAVL(new ArbolAVLPublicaciones.Vista(p,9,0,null,null)).correcto());
        var hijo=new ArbolAVLPublicaciones.Vista(new Publicacion(2,30,"b","t","x"),1,0,null,null);
        assertFalse(ValidadorI1.validarAVL(new ArbolAVLPublicaciones.Vista(p,2,-1,hijo,null)).correcto());
        var n=new ArbolDecisiones.Vista("raiz","texto",false,List.of());
        assertFalse(ValidadorI1.validarNario(new ArbolDecisiones.Vista("raiz","texto",false,List.of(n))).correcto());
        var s=new SesionLocal(Rol.CIUDADANO,0,60);var antes=s.vista();
        for(var paso:s.pasosAVL()) assertTrue(ValidadorI1.validarAVL(paso.raiz()).correcto());
        for(var paso:s.pasosDecisiones()) assertTrue(ValidadorI1.validarNario(paso.raiz()).correcto());
        assertEquals(antes,s.vista());
    }
}
