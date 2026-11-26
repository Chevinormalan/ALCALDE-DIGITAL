package alcaldedigital.cliente.misiones;

import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.compartido.guardado.ArchivosPartida;
import alcaldedigital.compartido.guardado.PerfilesJugadores;
import alcaldedigital.compartido.guardado.TablaPuntajes;
import alcaldedigital.compartido.guardado.TablaPuntajes.Componente;
import alcaldedigital.compartido.guardado.TablaPuntajes.Fila;
import alcaldedigital.servidor.sesion.SesionLocal;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuariosPuntajesTest {
    private static final Fila M1=nota(1,"m1",82,true);

    private static Fila nota(int mision,String partida,int nota,boolean victoria){
        return new Fila(mision,partida,victoria,nota,"NORMAL",List.of("Objetivo del intento"),List.of("Decisión real"))
            .conModalidad("Individual").conDesglose(new Componente("Objetivo",nota,100));
    }

    private static void crear(JuegoCliente juego,String nombre){
        for(char c:nombre.toCharArray())assertTrue(juego.usuarios().escribir(0,c));
        juego.accion("usuarioConfirmar");
    }

    @Test void primerInicioImportaElPerfilUnaVezYUsaElUsuarioEnReaperturas() throws Exception {
        Path raiz=ArchivosPartida.directorio();
        TablaPuntajes.registrar(M1);
        new alcaldedigital.compartido.guardado.ProgresoMisiones(raiz).victoria(1);
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        juego.iniciarUsuarios();
        assertNull(juego.usuarios().activo());
        assertNull(juego.usuarios().panel());
        assertEquals(JuegoCliente.Pantalla.MENU,juego.pantalla);
        assertNull(juego.continuacionMenu());
        juego.accion("jugar");
        assertEquals(UsuariosPuntajes.Panel.USUARIOS,juego.usuarios().panel());
        assertTrue(juego.usuarios().importar());
        var render=new RenderJuego();
        for(char c:"Sebastián Rivera".toCharArray())assertTrue(juego.usuarios().escribir(0,c));
        render.dibujar(juego);
        assertEquals("usuarioConfirmar",render.botones().getFirst().accion());
        render.activarFoco(juego);
        assertEquals("Sebastián Rivera",juego.usuarios().activo().nombre());
        assertTrue(juego.progreso.ganada(1));
        assertEquals(82,TablaPuntajes.resumenPersonal(ArchivosPartida.directorio()).total());
        assertTrue(Files.exists(raiz.resolve("tabla-puntajes.properties")));
        assertEquals(juego.usuarios().activo(),new PerfilesJugadores(raiz).activo());
        assertFalse(new PerfilesJugadores(raiz).hayCampanaAnterior());
        assertThrows(IllegalArgumentException.class,()->new PerfilesJugadores(raiz).crear("sebastian rivera",false,false));
    }

    @Test void puntajesUsaElMejorIntentoYRestablecerAislaElPerfilActivo() throws Exception {
        Path raiz=ArchivosPartida.directorio();
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        juego.iniciarUsuarios();juego.accion("jugar");crear(juego,"Sofi");
        assertEquals(JuegoCliente.Pantalla.TUTORIAL,juego.pantalla);
        var registro=new PerfilesJugadores(raiz);
        var sofi=juego.usuarios().activo();
        var juan=registro.crear("Juan",false,false);
        Path dirSofi=registro.directorio(sofi),dirJuan=registro.directorio(juan);
        TablaPuntajes.registrarPersonal(dirSofi,M1);
        TablaPuntajes.registrarPersonal(dirSofi,nota(2,"m2a",74,true));
        TablaPuntajes.registrarPersonal(dirSofi,nota(3,"m3",90,true));
        TablaPuntajes.registrarPersonal(dirSofi,nota(2,"m2b",88,true));
        TablaPuntajes.registrarPersonal(dirSofi,nota(2,"m2c",40,false));
        TablaPuntajes.registrarPersonal(dirSofi,nota(2,"m2b",100,true));
        TablaPuntajes.registrarPersonal(dirJuan,nota(1,"juan-m1",91,true));
        juego.abrir(JuegoCliente.Pantalla.MISIONES);
        juego.accion("puntajes");
        assertEquals(260,juego.usuarios().resumen().total());
        assertEquals(3,juego.usuarios().resumen().jugadas());
        var render=new RenderJuego();
        render.dibujar(juego);
        assertTrue(render.botones().stream().anyMatch(b->b.accion().equals("usuario:"+sofi.id())));
        assertEquals(40,juego.usuarios().resumen().misiones().get(1).ultimo().puntaje());
        juego.accion("volver");
        juego.accion("pedirRestablecer");juego.accion("confirmarRestablecer");
        assertEquals(sofi,juego.usuarios().activo());
        assertFalse(juego.progreso.disponible(2));
        assertEquals(0,TablaPuntajes.resumenPersonal(dirSofi).total());
        assertEquals(91,TablaPuntajes.resumenPersonal(dirJuan).total());
        try(var respaldos=Files.list(dirSofi.getParent())){
            var respaldo=respaldos.filter(p->p.getFileName().toString().contains("-respaldo-")).findFirst().orElseThrow();
            assertEquals(260,TablaPuntajes.resumenPersonal(respaldo).total());
        }
        juego.accion("jugar");assertEquals(JuegoCliente.Pantalla.TUTORIAL,juego.pantalla);
    }

    @Test void coopRegistraUnaMarcaPorUsuarioYNiPracticaNiAnfitrionRecibenPuntosAjenos() throws Exception {
        Path raiz=ArchivosPartida.directorio();
        var u=new UsuariosPuntajes(raiz,dir->{},()->{});
        u.pedirUsuarioParaJugar(()->{});
        for(char c:"Ana".toCharArray())u.escribir(0,c);
        u.accion("usuarioConfirmar");
        var registro=new PerfilesJugadores(raiz);
        var ana=u.activo();
        registro.crear("Bruno",false,false);
        // Abrir la selección al iniciar de nuevo lee los perfiles creados en el equipo.
        var perfiles=new UsuariosPuntajes(raiz,dir->{},()->{});
        var bruno=registro.listar().stream().filter(p->p.nombre().equals("Bruno")).findFirst().orElseThrow();
        assertTrue(perfiles.asignarPuestos(2,()->{}));
        perfiles.accion("usuario:"+bruno.id());
        perfiles.vincular(7,"carrera",true);
        var ganadora=nota(7,"carrera",87,true);
        perfiles.presentar(ganadora,"Meta",false,false,0,p->p==0?ganadora:nota(7,"carrera",41,false),a->{});
        assertEquals(87,TablaPuntajes.resumenPersonal(registro.directorio(ana)).total());
        assertEquals(41,TablaPuntajes.resumenPersonal(registro.directorio(bruno)).total());
        perfiles.accion("resultadoSalir");perfiles.nuevaMision();
        perfiles.vincular(7,"carrera",true);
        perfiles.presentar(ganadora,"Meta",false,false,0,p->ganadora,a->{});
        assertEquals(2,perfiles.cierre().marcas().size());
        assertEquals(41,TablaPuntajes.resumenPersonal(registro.directorio(bruno)).total());
        perfiles.accion("resultadoSalir");perfiles.nuevaMision();
        var prueba=nota(8,"prueba",100,true);
        perfiles.presentar(prueba,"Práctica",true,true,1,p->prueba,a->{});
        perfiles.accion("resultadoSalir");perfiles.nuevaMision();
        var lan=nota(7,"lan",97,true);
        perfiles.presentar(lan,"Meta",false,true,1,p->{assertEquals(1,p);return lan;},a->{});
        assertEquals(97,TablaPuntajes.resumenPersonal(registro.directorio(ana)).total());
        assertEquals(41,TablaPuntajes.resumenPersonal(registro.directorio(bruno)).total());
    }

    @Test void elMenuEsLaPrimeraPantallaYCrearDesdeJugarAbreElTutorial() throws Exception {
        Path raiz=ArchivosPartida.directorio();
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        var render=new RenderJuego();
        juego.iniciarUsuarios();
        assertNull(juego.usuarios().panel());
        render.dibujar(juego);
        assertTrue(render.botones().stream().anyMatch(b->"jugar".equals(b.accion())));
        juego.accion("jugar");
        juego.accion("volver");
        assertNull(juego.usuarios().panel());
        assertTrue(new PerfilesJugadores(raiz).listar().isEmpty());
        juego.accion("jugar");crear(juego,"María");
        assertEquals(JuegoCliente.Pantalla.TUTORIAL,juego.pantalla);
        assertEquals("María",new PerfilesJugadores(raiz).activo().nombre());
    }

    @Test void eliminarConfirmaArchivaYPermiteCrearDeNuevoSinMezclarPuntajes() throws Exception {
        Path raiz=ArchivosPartida.directorio();
        var registro=new PerfilesJugadores(raiz);
        var ana=registro.crear("Ana",false,true);
        var luis=registro.crear("Luis",false,false);
        TablaPuntajes.registrarPersonal(registro.directorio(luis),M1);
        var juego=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
        juego.iniciarUsuarios();
        juego.accion("usuarios");
        juego.accion("eliminarUsuario:"+luis.id());
        assertEquals(UsuariosPuntajes.Panel.ELIMINAR_USUARIO,juego.usuarios().panel());
        juego.accion("volver");
        assertEquals(2,registro.listar().size());
        juego.accion("eliminarUsuario:"+luis.id());juego.accion("confirmarEliminarUsuario");
        assertEquals(List.of(ana),registro.listar());
        assertEquals(ana,juego.usuarios().activo());
        assertFalse(Files.exists(registro.directorio(luis)));
        assertEquals(82,TablaPuntajes.resumenPersonal(raiz.resolve("usuarios-eliminados").resolve(luis.id().toString())).total());
        juego.accion("eliminarUsuario:"+ana.id());juego.accion("confirmarEliminarUsuario");
        assertNull(juego.usuarios().panel());
        assertNull(registro.activo());
        assertEquals(JuegoCliente.Pantalla.MENU,juego.pantalla);
        juego.accion("jugar");crear(juego,"Luis");
        assertNotEquals(luis.id(),juego.usuarios().activo().id());
        assertEquals(0,TablaPuntajes.resumenPersonal(ArchivosPartida.directorio()).total());
        assertEquals(JuegoCliente.Pantalla.TUTORIAL,juego.pantalla);
    }

    @Test void reanudarCooperativoReemplazaSolamenteElPuestoDelUsuarioEliminado() throws Exception {
        Path raiz=ArchivosPartida.directorio();
        var registro=new PerfilesJugadores(raiz);
        var ana=registro.crear("Ana",false,true);
        var luis=registro.crear("Luis",false,false);
        var sara=registro.crear("Sara",false,false);
        var ivan=registro.crear("Iván",false,false);
        var u=new UsuariosPuntajes(raiz,dir->{},()->{});
        assertTrue(u.asignarPuestos(3,()->{}));
        u.accion("usuario:"+luis.id());u.accion("usuario:"+sara.id());
        u.vincular(8,"guardado",true);
        u.abrirUsuarios();
        u.accion("eliminarUsuario:"+luis.id());u.accion("confirmarEliminarUsuario");
        u.accion("volver");
        assertTrue(u.asignarPuestos(8,"guardado",3,()->{}));
        assertEquals(2,u.puesto());
        assertEquals(List.of(ivan),u.paginaUsuarios());
        u.accion("usuario:"+ivan.id());
        var nuevo=new UsuariosPuntajes(raiz,dir->{},()->{});
        assertFalse(nuevo.asignarPuestos(8,"guardado",3,()->fail("El guardado ya tiene los tres usuarios")));
        var fila=nota(8,"guardado",51,true);
        nuevo.presentar(fila,"Cierre",false,false,0,p->fila,a->{});
        assertEquals(List.of(ana,ivan,sara),nuevo.cierre().marcas().stream().map(UsuariosPuntajes.Marca::perfil).toList());
        assertFalse(Files.exists(registro.directorio(luis)));
    }

}
