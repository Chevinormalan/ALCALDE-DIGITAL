package alcaldedigital.mision8;
import alcaldedigital.cliente.mision8.*;
import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.servidor.sesion.*;
import alcaldedigital.compartido.guardado.*;
import org.junit.jupiter.api.*;import java.nio.file.*;import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class FinalesPruebaOchoTest {
 @Test void desbloquearTodasAbreLasOchoSinInventarVictorias(){var j=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);j.abrir(JuegoCliente.Pantalla.MISIONES);j.accion("desbloquearTodas");for(int n=1;n<=8;n++){assertTrue(j.progreso.disponible(n));assertFalse(j.progreso.ganada(n));}var render=new RenderJuego();render.dibujar(j);assertTrue(render.botones().stream().anyMatch(b->b.accion().equals("mision8")));j.accion("mision8");assertEquals(JuegoCliente.Pantalla.MISION_OCHO,j.pantalla);}
 @Test void cadaFinalSePuedeElegirAunqueSeVotePorOtraPersona(){for(int candidato=0;candidato<4;candidato++)for(int ciudad=0;ciudad<3;ciudad++){
  MotorOcho m=new MotorOcho(FinalesPruebaOcho.antecedentes(candidato,ciudad),1,1,0,false,77);m.elegirFinalPrueba(candidato);m.escena=MotorOcho.Escena.AUDITORIA;for(int i=0;i<3;i++)m.auditar(i,true);m.votar(0,(candidato+1)%4);assertEquals(candidato,m.ganador);assertEquals(candidato,m.publicado);assertTrue(m.prueba());assertEquals(m.capturar(),MotorOcho.restaurar(m.capturar()).capturar());assertEquals(new String[]{"CONFIANZA CÍVICA","CIUDAD EN RECONSTRUCCIÓN","CIUDAD EN CRISIS"}[ciudad],m.antecedentes.estadoCiudad());
 }}
 @Test void probarFinalNoAlteraCampanaPuntajesNiGuardadoNormal()throws Exception{Path dir=ArchivosPartida.directorio();Files.createDirectories(dir);Map<Path,String>originales=new HashMap<>();for(String nombre:List.of("campana.properties","progreso-misiones.properties","tabla-puntajes.properties","mision8.properties","mision8-resultados.properties")){Path f=dir.resolve(nombre);String texto="version=1\noriginal=conservar\nelectoral.juan.corrupto=true\n";Files.writeString(f,texto);originales.put(f,texto);}
  MotorOcho m=new MotorOcho(FinalesPruebaOcho.antecedentes(0,0),1,1,0,false,1);m.elegirFinalPrueba(0);m.ganador=0;m.escena=MotorOcho.Escena.DERROTA;GuardadoOcho.guardar(m);m.escena=MotorOcho.Escena.FINAL;GuardadoOcho.acreditar(m);assertTrue(m.credito);assertTrue(Files.exists(dir.resolve("mision8-prueba.properties")));for(var e:originales.entrySet())assertEquals(e.getValue(),Files.readString(e.getKey()),e.getKey().toString());
 }
 @Test void noSeCambiaElFinalDuranteLaPartida(){MotorOcho m=new MotorOcho(FinalesPruebaOcho.antecedentes(0,0),1,1,0,false,1);m.paso();assertThrows(IllegalArgumentException.class,()->m.elegirFinalPrueba(1));}
}
