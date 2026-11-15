package alcaldedigital.cliente.misiones;
import alcaldedigital.cliente.*;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.cliente.mision2.MisionDosSketch;
import alcaldedigital.cliente.mision3.MisionTresSketch;
import alcaldedigital.cliente.mision4.MisionCuatroSketch;
import alcaldedigital.cliente.mision6.MisionSeisMotor;
import alcaldedigital.compartido.guardado.*;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.compartido.batalla.EstadoBatalla;
import alcaldedigital.compartido.carrera.EstadoCarrera;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import alcaldedigital.servidor.batalla.GuardadoBatalla;
import alcaldedigital.servidor.batalla.MotorBatalla;
import alcaldedigital.servidor.carrera.GuardadoCarrera;
import alcaldedigital.servidor.carrera.MotorCarrera;
import alcaldedigital.servidor.sesion.*;
import org.junit.jupiter.api.*;import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;import java.util.*;
import java.nio.file.attribute.FileTime;
import static org.junit.jupiter.api.Assertions.*;

class ProgresionYGuardadoTest {
 @TempDir Path tmp;
 private final Path perfilAlConstruir=ArchivosPartida.directorio();
 private final JuegoCliente juegoAlConstruir=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);
 private JuegoCliente juego(){return new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);}
 @Test void perfilSeAislaAntesDeInicializarLosCamposDelTest(){
  assertNotEquals(Path.of(System.getProperty("user.home"),".local","state","alcalde-digital"),perfilAlConstruir);
  assertEquals(perfilAlConstruir,ArchivosPartida.directorio());juegoAlConstruir.victoria(1);
  assertTrue(Files.isRegularFile(perfilAlConstruir.resolve("progreso-misiones.properties")));
 }
 @Test void perfilNuevoSoloAbreLaUnoYNoPermiteSaltarPorAccionDirecta(){var j=juego();j.accion("jugar");var r=new RenderJuego();r.dibujar(j);assertEquals(List.of("mision1"),r.botones().stream().map(b->b.accion()).filter(a->a.matches("mision[1-7]")).toList());for(int n=2;n<=7;n++){j.accion("mision"+n);assertEquals(JuegoCliente.Pantalla.MISIONES,j.pantalla);}j.accion("m2dificultad:NORMAL");assertEquals(JuegoCliente.Pantalla.MISIONES,j.pantalla);}
 @Test void victoriasPersistenEnOrdenYUnaDerrotaNoDesbloquea(){var j=juego();j.victoria(4);assertFalse(j.progreso.ganada(4));j.victoria(1);assertTrue(juego().progreso.disponible(2));j.terminarMisionDos(new MisionDosSketch.Resultado(true,false,5,0));assertFalse(j.progreso.disponible(3));j.terminarMisionDos(new MisionDosSketch.Resultado(true,true,6,0));assertTrue(juego().progreso.disponible(3));assertFalse(j.progreso.disponible(4));}
 @Test void tenerCheckpointNoEsHaberGanado(){var p=new Properties();p.setProperty("version","1");assertDoesNotThrow(()->GuardadoCampana.guardar(6,p));var j=juego();j.accion("jugar");j.accion("mision6");assertEquals(JuegoCliente.Pantalla.MISIONES,j.pantalla);assertFalse(j.progreso.disponible(6));}
 @Test void botonOpcionalDesbloqueaTodasSinInventarVictoriasYPersiste()throws Exception{
  var j=juego();j.accion("desbloquearTodas");assertFalse(j.progreso.disponible(7));
  j.accion("jugar");var r=new RenderJuego();r.dibujar(j);
  var boton=r.botones().stream().filter(b->b.accion().equals("desbloquearTodas")).findFirst().orElseThrow();
  assertEquals("Desbloquear todas las misiones",boton.texto());
  r.pulsar(j,(int)boton.area().getCenterX(),(int)boton.area().getCenterY());
  assertEquals(JuegoCliente.Pantalla.MISIONES,j.pantalla);
  var reiniciado=juego();for(int n=1;n<=7;n++){assertTrue(reiniciado.progreso.disponible(n));assertFalse(reiniciado.progreso.ganada(n));}
  assertFalse(reiniciado.progreso.disponible(8));reiniciado.victoria(6);
  var despuesDeGanar=juego();assertTrue(despuesDeGanar.progreso.ganada(6));assertFalse(despuesDeGanar.progreso.ganada(5));
  assertTrue(despuesDeGanar.progreso.todasDesbloqueadas());
  reiniciado.accion("jugar");r.dibujar(reiniciado);
  assertEquals(7,r.botones().stream().filter(b->b.accion().matches("mision[1-7]")).count());
  assertTrue(r.botones().stream().noneMatch(b->b.accion().equals("desbloquearTodas")));
 }
 @Test void perfilConDesbloqueoAnteriorMantieneElAcceso()throws Exception{
  var legado=new Properties();legado.setProperty("version","1");legado.setProperty("desbloqueo.total","true");
  legado.setProperty("victoria.4","true");ArchivosPartida.guardar(ArchivosPartida.directorio().resolve("progreso-misiones.properties"),legado);
  var j=juego();assertTrue(j.progreso.todasDesbloqueadas());
  for(int n=1;n<=7;n++)assertTrue(j.progreso.disponible(n));
  assertTrue(j.progreso.ganada(4));assertFalse(j.progreso.ganada(1));
 }
 @Test void restablecerArchivaPerfilYLimpiaBloqueosYContinuacion()throws Exception{
  String anterior=System.getProperty("ad.guardados");Path perfil=tmp.resolve("perfil");
  System.setProperty("ad.guardados",perfil.toString());
  try{
   var j=juego();j.accion("jugar");j.accion("desbloquearTodas");j.victoria(1);
   var guardado=new Properties();guardado.setProperty("version","1");
   GuardadoCampana.guardar(1,guardado);
   assertTrue(j.progreso.disponible(7));
   j.accion("pedirRestablecer");assertEquals(JuegoCliente.Pantalla.CONFIRMAR_REINICIO,j.pantalla);
   j.accion("confirmarRestablecer");assertEquals(JuegoCliente.Pantalla.MISIONES,j.pantalla);
   assertTrue(j.progreso.disponible(1));assertFalse(j.progreso.disponible(2));
   assertFalse(j.progreso.ganada(1));assertFalse(j.progreso.todasDesbloqueadas());
   assertFalse(Files.exists(GuardadoCampana.archivo(1)));
   try(var archivos=Files.list(tmp)){
    Path respaldo=archivos.filter(p->p.getFileName().toString().startsWith("perfil-respaldo-")).findFirst().orElseThrow();
    assertTrue(Files.exists(respaldo.resolve("progreso-misiones.properties")));
    assertTrue(Files.exists(respaldo.resolve(GuardadoCampana.archivo(1).getFileName())));
   }
   j.accion("menu");var render=new RenderJuego();render.dibujar(j);
   assertTrue(render.botones().stream().noneMatch(b->b.accion().equals("continuarMision")));
  }finally{if(anterior==null)System.clearProperty("ad.guardados");else System.setProperty("ad.guardados",anterior);}
 }
 @Test void menuRetomaMisionUnoGuardada()throws Exception{
  var sesion=new SesionMisionUnoLocal(2026,SesionMisionUno.Dificultad.FACIL);
  GuardadoCampana.guardar(1,sesion.capturar());var j=juego();var r=new RenderJuego();r.dibujar(j);
  assertTrue(r.botones().stream().anyMatch(b->b.accion().equals("continuarMision")&&b.texto().equals("Continuar misión 1")));
  j.accion("continuarMision");assertEquals(JuegoCliente.Pantalla.PARQUE,j.pantalla);
  assertEquals(SesionMisionUno.Dificultad.FACIL,j.misionUno.vista().dificultad());
 }
 @Test void menuRetomaUltimaMisionDisponibleSinSaltarBloqueos()throws Exception{
  var p=new MisionDosSketch(MisionDosSketch.Dificultad.NORMAL,9817).capturar();
  GuardadoCampana.guardar(2,p);var j=juego();var r=new RenderJuego();r.dibujar(j);
  assertTrue(r.botones().stream().noneMatch(b->b.accion().equals("continuarMision")));
  j.victoria(1);Files.setLastModifiedTime(GuardadoCampana.archivo(2),FileTime.fromMillis(System.currentTimeMillis()+2000));
  j.abrir(JuegoCliente.Pantalla.MENU);r.dibujar(j);
  assertTrue(r.botones().stream().anyMatch(b->b.accion().equals("continuarMision")&&b.texto().equals("Continuar misión 2")));
  j.accion("continuarMision");assertEquals(JuegoCliente.Pantalla.MISION_DOS,j.pantalla);assertTrue(j.continuarGuardado);
 }
 @Test void menuDetectaGuardadosTresACuatroCincoSeisYSiete()throws Exception{
  var j=juego();j.victoria(1);j.victoria(2);
  var tres=new Properties();tres.setProperty("version","1");tres.setProperty("zona","atrio");
  tres.setProperty("confianza","0.5");tres.setProperty("desinformacion","0.2");tres.setProperty("live","0.0");tres.setProperty("amenazas","0");
  GuardadoCampana.guardar(3,tres);assertEquals(3,PartidasMenu.reciente(j.progreso).mision());
  j.victoria(3);var cuatro=new MisionCuatroSketch();cuatro.iniciarEn(null);var p4=cuatro.estado();cuatro.cerrar();
  GuardadoCampana.guardar(4,p4);assertEquals(4,PartidasMenu.reciente(j.progreso).mision());
  j.victoria(4);new GuardadoBatalla(ArchivosPartida.directorio().resolve("mision5-coop.bin"))
      .guardar(new MotorBatalla(2026,false,EstadoBatalla.Dificultad.NORMAL));
  var cinco=PartidasMenu.reciente(j.progreso,5);assertNotNull(cinco);assertEquals(1,cinco.modo());
  j.abrir(JuegoCliente.Pantalla.MENU);j.accion("continuarMision");
  assertEquals(JuegoCliente.Pantalla.MISION_CINCO,j.pantalla);assertEquals(1,j.modoContinuacion);
  j.victoria(5);GuardadoCampana.guardar(6,new MisionSeisMotor().capturar());
  assertNotNull(PartidasMenu.reciente(j.progreso,6));
  j.victoria(6);var ciudad=EstadoCiudad.inicial();
  new GuardadoCarrera(ArchivosPartida.directorio().resolve("mision7-solo-1.bin"))
      .guardar(new MotorCarrera(2026,2,EstadoCarrera.Dificultad.NORMAL,ciudad,
          alcaldedigital.compartido.carrera.EstadoElectoral.inicial(ciudad),true));
  var siete=PartidasMenu.reciente(j.progreso,7);assertNotNull(siete);assertEquals(0,siete.modo());assertEquals(1,siete.rol());
  j.abrir(JuegoCliente.Pantalla.MENU);j.accion("continuarMision");
  assertEquals(JuegoCliente.Pantalla.MISION_SIETE,j.pantalla);assertEquals(1,j.rolContinuacion);
 }
 @Test void selectorDePartidaConservaVictoriasYDejaElegirNueva(){var j=juego();j.accion("jugar");j.accion("mision1");j.accion("m1dificultad:FACIL");assertDoesNotThrow(j::guardarMisionUno);j.abrir(JuegoCliente.Pantalla.MISIONES);j.accion("mision1");assertEquals(JuegoCliente.Pantalla.GUARDADO,j.pantalla);new RenderJuego().dibujar(j);j.accion("partida:continuar");assertEquals(JuegoCliente.Pantalla.PARQUE,j.pantalla);assertEquals(SesionMisionUno.Dificultad.FACIL,j.misionUno.vista().dificultad());j.abrir(JuegoCliente.Pantalla.MISIONES);j.accion("mision1");j.accion("partida:nueva");assertEquals(JuegoCliente.Pantalla.DIFICULTAD,j.pantalla);}
 @Test void guardarUnoRecuperaFasePosicionesFuentesYArboles(){var m=new SesionMisionUnoLocal(2026,SesionMisionUno.Dificultad.FACIL);m.actualizar(2);for(int i=0;i<4;i++)m.solicitar("intro"+i,"continuar");m.actualizar(2);m.solicitar("ver","verificar:1");var copia=SesionMisionUnoLocal.restaurar(m.capturar());assertEquals(m.vista(),copia.vista());m.actualizar(.1);copia.actualizar(.1);assertEquals(m.vista(),copia.vista());}
 @Test void guardarDosRecuperaRedTiempoSaltoYLaSecuenciaDeAtaques(){var m=new MisionDosSketch(MisionDosSketch.Dificultad.NORMAL,9817);var p=m.capturar();p.setProperty("estado.fase","DEFENSA");p.setProperty("estado.proximoAtaque","0.2");m=MisionDosSketch.restaurar(p);for(int i=0;i<30;i++)m.actualizarEstado(1f/60);var copia=MisionDosSketch.restaurar(m.capturar());for(int i=0;i<650;i++){m.actualizarEstado(1f/60);copia.actualizarEstado(1f/60);}assertEquals(m.resultado(),copia.resultado());assertEquals(m.capturar(),copia.capturar());}
 @Test void azarRestauradoProduceLasMismasOleadasQueRandomJava(){var a=new AzarPartida(2026);var b=new java.util.Random(2026);for(int i=0;i<50;i++)assertEquals(b.nextInt(6),a.nextInt(6));long estado=a.estado();var c=new AzarPartida(0);c.restaurar(estado);for(int i=0;i<50;i++)assertEquals(a.nextDouble(),c.nextDouble());}
 @Test void escrituraAtomicaPreservaLaCopiaAnteriorYArchivosDeOtrosModos()throws Exception{Path f=tmp.resolve("mision1.properties");var p=new Properties();p.setProperty("version","1");p.setProperty("fase","INTRO");ArchivosPartida.guardar(f,p);p.setProperty("fase","CRISIS");ArchivosPartida.guardar(f,p);assertEquals("INTRO",ArchivosPartida.leer(tmp.resolve("mision1.properties.bak")).getProperty("fase"));assertEquals("CRISIS",ArchivosPartida.leer(f).getProperty("fase"));}
}
