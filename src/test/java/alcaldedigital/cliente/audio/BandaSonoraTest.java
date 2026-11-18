package alcaldedigital.cliente.audio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import alcaldedigital.cliente.*;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.servidor.sesion.*;

class BandaSonoraTest {
 @TempDir Path dir;
 private String perfilAnterior;
 @org.junit.jupiter.api.BeforeEach void perfilTemporal(){perfilAnterior=System.getProperty("ad.guardados");System.setProperty("ad.guardados",dir.resolve("perfil").toString());}
 @org.junit.jupiter.api.AfterEach void restaurarPerfil(){if(perfilAnterior==null)System.clearProperty("ad.guardados");else System.setProperty("ad.guardados",perfilAnterior);}

 private Partitura.Contexto c(int n,String fase,float avance,int etapa,boolean victoria,String ciudad){return new Partitura.Contexto(n,fase,avance,etapa,false,victoria,ciudad);}
 private DirectorAudio.Estado estado(long tick,String fase,boolean pausa,Map<String,Integer> marcas){return new DirectorAudio.Estado(c(8,fase,0,1,fase.equals("FINAL"),"CONFIANZA CÍVICA"),"partida",tick,pausa,0,marcas);}
 @Test void lasOchoMisionesTienenMusicaPropia(){String[] fs={"CRISIS","DEFENSA","boss","BATTLE","ESQUIVA","COMBATE","CARRERA","COMBATE"};for(int i=0;i<8;i++)assertTrue(Partitura.elegir(c(i+1,fs[i],.6f,2,false,"")).pista().startsWith("M"+(i+1)));}
 @Test void urgenciaUsaLaFraccionDelRelojYNoSegundosAbsolutos(){for(int segundos:new int[]{60,30,12}){var inicio=Partitura.elegir(c(1,"CRISIS",0,1,false,""));var finalCue=Partitura.elegir(c(1,"CRISIS",1-1f/segundos,1,false,""));assertTrue(finalCue.intensidad()>inicio.intensidad());}assertEquals(1,Partitura.elegir(c(2,"FALLO_CRITICO",0,1,false,"")).intensidad());}
 @Test void lasFasesRealesAumentanLasCapasSinCambiarLaCancion(){for(int n:new int[]{5,6,8}){String f=n==5?"ESQUIVA":"COMBATE";var a=Partitura.elegir(c(n,f,0,1,false,""));var b=Partitura.elegir(c(n,f,0,n==6?3:5,false,""));assertEquals(a.pista(),b.pista());assertTrue(b.intensidad()>a.intensidad());}}
 @Test void decisionYTurnoPermitenDescansarYLaTraicionRecuperaLaTension(){assertEquals("G02",Partitura.elegir(c(5,"DECISION",0,5,false,"")).pista());assertTrue(Partitura.elegir(c(5,"TURNO",0,5,false,"")).intensidad()<Partitura.elegir(c(5,"TRAICION",0,5,false,"")).intensidad());}
 @Test void laCarreraNoProclamaAlcaldeYElCombateNoAdelantaElFinal(){assertEquals("M701",Partitura.elegir(c(7,"RESULTADO",1,1,true,"")).pista());assertEquals("G02",Partitura.elegir(c(8,"EPILOGO",1,5,true,"")).pista());assertEquals(List.of("confirmar"),DirectorAudio.eventos(new DirectorAudio.Estado(c(7,"CARRERA",0,1,false,""),"x",20,false,0,Map.of()),new DirectorAudio.Estado(c(7,"RESULTADO",1,1,true,""),"x",21,false,0,Map.of()),false));}
 @Test void losTresEstadosDeCiudadTienenCierreDistinto(){Set<String> ids=new HashSet<>();for(String ciudad:new String[]{"CONFIANZA CÍVICA","RECONSTRUCCIÓN","CIUDAD EN CRISIS"})ids.add(Partitura.elegir(c(8,"FINAL",1,5,true,ciudad)).pista());assertEquals(3,ids.size());assertEquals("G03-crisis",Partitura.elegir(c(8,"DERROTA",0,1,false,"")).pista());}
 @Test void inyeccionRecuperacionYExplosionSeOyenenSuFlancoUnaSolaVez(){var a=estado(10,"MUTACION",false,Map.of("inyeccion",0));var b=estado(11,"MUTACION",false,Map.of("inyeccion",1));assertEquals(List.of("inyeccion"),DirectorAudio.eventos(a,b,false));assertTrue(DirectorAudio.eventos(b,b,false).isEmpty());var e=estado(12,"EPILOGO",false,Map.of("inyeccion",1,"recuperacion",1));assertEquals(List.of("recuperacion"),DirectorAudio.eventos(b,e,false));var f=estado(13,"EPILOGO",false,Map.of("inyeccion",1,"recuperacion",1,"explosion_final",1));assertEquals(List.of("explosion_final"),DirectorAudio.eventos(e,f,false));}
 @Test void restaurarRetrocederRecibirDuplicadoYPausaNoRepitenEventos(){var a=estado(30,"MUTACION",false,Map.of("inyeccion",1));assertTrue(DirectorAudio.eventos(null,a,false).isEmpty());assertTrue(DirectorAudio.eventos(a,a,false).isEmpty());assertTrue(DirectorAudio.eventos(a,estado(4,"MUTACION",false,Map.of("inyeccion",1)),false).isEmpty());assertTrue(DirectorAudio.eventos(a,estado(200,"COMBATE",false,Map.of("fase",4)),false).isEmpty());assertTrue(DirectorAudio.eventos(a,estado(31,"MUTACION",true,Map.of("inyeccion",1)),false).isEmpty());}
 @Test void tonosDeDialogoSonOpcionalesYNoDependenDeLetrasNiFps(){var a=estado(5,"OBRA",false,Map.of());var b=new DirectorAudio.Estado(a.contexto(),a.partida(),6,false,1,Map.of());assertTrue(DirectorAudio.eventos(a,b,false).isEmpty());assertEquals(List.of("dialogo"),DirectorAudio.eventos(a,b,true));assertTrue(DirectorAudio.eventos(b,b,true).isEmpty());}
 @Test void configuracionSeGuardaYLimitaLosVolumenesSinTocarPartidas()throws Exception{var archivo=dir.resolve("audio.properties");var o=new OpcionesAudio(archivo);assertEquals(OpcionesAudio.ORIGINAL,o.actual());assertFalse(Files.exists(archivo));o.cambiar(OpcionesAudio.Bus.MUSICA,-100);assertEquals(0,o.actual().musica());o.cambiar(OpcionesAudio.Bus.MUSICA,110);o.alternar("mudo");assertEquals(0,o.actual().ganancia(OpcionesAudio.Bus.EFECTOS));assertEquals(o.actual(),new OpcionesAudio(archivo).actual());try(var archivos=Files.list(dir)){assertTrue(archivos.allMatch(p->p.getFileName().toString().startsWith("audio.properties")));}}
 @Test void preferenciasCorruptasRecuperanValoresYLoExplican()throws Exception{Path f=dir.resolve("audio.properties");Files.writeString(f,"version=1\ngeneral=999999\n");var o=new OpcionesAudio(f);assertEquals(OpcionesAudio.ORIGINAL,o.actual());assertFalse(o.mensaje.isBlank());}
 @Test void panelAudioEsUnMenuAunqueQuedeMisionUnoEnMemoria(){var j=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);j.misionUno=new SesionMisionUnoLocal(1,alcaldedigital.compartido.protocolo.SesionMisionUno.Dificultad.FACIL);j.accion("ayuda");var r=new RenderJuego();r.dibujar(j);var b=r.botones().stream().filter(x->x.accion().equals("audio")).findFirst().orElseThrow();r.pulsar(j,b.area().x+10,b.area().y+10);assertEquals(JuegoCliente.Pantalla.AUDIO,j.pantalla);assertEquals(0,j.numeroMision());j.accion("pausa");assertFalse(j.pausa);r.dibujar(j);assertEquals(2,r.botones().stream().filter(x->x.accion().startsWith("audio:MUSICA:")).count());j.tecla(27,' ');assertEquals(JuegoCliente.Pantalla.AYUDA,j.pantalla);j.accion("volver");assertEquals(JuegoCliente.Pantalla.MENU,j.pantalla);}
 @Test void catalogoContiene20TemasDosVariantesYCapasEnFormatoReproducible()throws Exception{
  var props=new Properties();try(var in=getClass().getResourceAsStream("/cliente/audio/catalogo.properties")){assertNotNull(in);props.load(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8));}
  var ids=props.stringPropertyNames().stream().filter(k->k.endsWith(".titulo")).map(k->k.substring(0,k.indexOf('.'))).toList();assertEquals(22,ids.size());
  for(String id:ids){long frames=validarWav("audio/musica/"+id,45);for(String capa:props.getProperty(id+".capas").split(","))if(!capa.isEmpty())assertEquals(frames,validarWav("audio/musica/"+id+"-"+capa,45));}
  for(String r:props.getProperty("precarga").split(","))assertNotNull(getClass().getResource("/cliente/"+r+".wav"),r);
 }
 private long validarWav(String recurso,int segundos)throws Exception{try(var in=getClass().getResourceAsStream("/cliente/"+recurso+".wav")){assertNotNull(in,recurso);try(var wav=AudioSystem.getAudioInputStream(new BufferedInputStream(in))){assertEquals(22050,wav.getFormat().getSampleRate());assertEquals(16,wav.getFormat().getSampleSizeInBits());assertEquals(2,wav.getFormat().getChannels());assertTrue(wav.getFrameLength()>=segundos*22050L,recurso);return wav.getFrameLength();}}}
}
