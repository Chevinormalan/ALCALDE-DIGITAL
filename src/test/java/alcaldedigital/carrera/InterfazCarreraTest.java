package alcaldedigital.carrera;

import alcaldedigital.cliente.mision7.MisionSieteVista;
import alcaldedigital.cliente.JuegoCliente;
import alcaldedigital.cliente.interfaz.RenderJuego;
import alcaldedigital.cliente.entrada.EstadoMando;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.compartido.carrera.EstadoCarrera.*;
import alcaldedigital.servidor.sesion.*;
import java.util.*;import java.nio.file.*;import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class InterfazCarreraTest {
    @TempDir Path tmp;private String anterior;
    @BeforeEach void aislar(){anterior=System.getProperty("ad.guardados");System.setProperty("ad.guardados",tmp.toString());}
    @AfterEach void restaurar(){if(anterior==null)System.clearProperty("ad.guardados");else System.setProperty("ad.guardados",anterior);}
    private static void tecla(MisionSieteVista v,int k){v.pulsar(' ',k);v.soltar(k);}
    private static void avanzar(MisionSieteVista v,int pasos){for(int i=0;i<pasos;i++)v.actualizar(1f/60);}
    private static void conectar(MisionSieteVista v)throws Exception{tecla(v,10);for(int i=0;i<300&&v.vistaResultado()==null;i++){v.actualizar(0);Thread.sleep(5);}assertNotNull(v.vistaResultado());}
    private static void empezar(MisionSieteVista v){tecla(v,10);for(int i=0;i<40&&v.vistaResultado().fase()!=Fase.CUENTA;i++)tecla(v,10);assertEquals(Fase.CUENTA,v.vistaResultado().fase());avanzar(v,181);assertEquals(Fase.CARRERA,v.vistaResultado().fase());}
    @Test void selectorAbreMision7YElInfluencerPuedeJugarSoloContraPeriodistaCPU()throws Exception{
        var j=new JuegoCliente(SesionLocal::new,SesionMisionUnoLocal::new);alcaldedigital.pruebas.PartidaPrueba.desbloquear(j);j.accion("jugar");new RenderJuego().dibujar(j);alcaldedigital.pruebas.PartidaPrueba.elegir(j,7);assertEquals(JuegoCliente.Pantalla.MISION_SIETE,j.pantalla);
        try(var v=new MisionSieteVista(2026,EstadoCiudad.inicial(),false,false)){v.iniciarEn(null);v.raton(1000,200);conectar(v);assertEquals(2,v.vistaResultado().humanos());empezar(v);v.pulsar(' ',32);avanzar(v,120);v.soltar(32);assertTrue(v.vistaResultado().corredores()[1].pulsos()>0);assertTrue(v.vistaResultado().corredores()[0].distancia()>0);}
    }
    @Test void pausaAyudaYEstructurasNoActivanNoticiasNiDejanMovimientoAtascado()throws Exception{
        try(var v=new MisionSieteVista(2026,EstadoCiudad.inicial(),false,false)){v.iniciarEn(null);conectar(v);empezar(v);v.pulsar(' ',32);v.pulsar('s',83);avanzar(v,30);v.pulsar(' ',27);var antes=v.vistaResultado();
            for(int i=0;i<20;i++){v.pulsar(' ',27);v.actualizar(1f/60);}assertTrue(v.vistaResultado().pausa());assertFalse(v.salir());assertEquals(antes.tick(),v.vistaResultado().tick());v.soltar(27);
            v.raton(500,270);v.raton(820,180);tecla(v,49);avanzar(v,30);assertEquals(antes.tick(),v.vistaResultado().tick());tecla(v,27);tecla(v,84);tecla(v,39);tecla(v,39);tecla(v,27);tecla(v,86);v.raton(300,625);tecla(v,80);
            float y=v.vistaResultado().corredores()[0].y();int pulsos=v.vistaResultado().corredores()[0].pulsos();avanzar(v,10);assertEquals(y,v.vistaResultado().corredores()[0].y());assertEquals(pulsos,v.vistaResultado().corredores()[0].pulsos());v.soltar(32);v.soltar(83);
            v.pulsar('w',87);avanzar(v,1);assertTrue(v.vistaResultado().corredores()[0].y()<y);tecla(v,71);assertTrue(Files.exists(tmp.resolve("mision7-solo-0.bin")));}
    }
    @Test void mandoSePuedeUsarParaCorrerPausarYVolverDeAyuda()throws Exception{
        try(var v=new MisionSieteVista(2026,EstadoCiudad.inicial(),false,false)){v.iniciarEn(null);conectar(v);empezar(v);v.mando(new EstadoMando("Prueba",0,0,0,0,Set.of(EstadoMando.Boton.RB)));avanzar(v,120);assertTrue(v.vistaResultado().corredores()[0].pulsos()>2);
            v.mando(new EstadoMando("Prueba",0,0,0,0,Set.of(EstadoMando.Boton.MENU)));assertTrue(v.vistaResultado().pausa());v.mando(new EstadoMando("Prueba",0,0,0,0,Set.of(EstadoMando.Boton.VISTA)));v.mando(new EstadoMando("Prueba",0,0,0,0,Set.of(EstadoMando.Boton.B)));assertTrue(v.vistaResultado().pausa());v.mando(new EstadoMando("Prueba",0,0,0,0,Set.of(EstadoMando.Boton.A)));assertFalse(v.vistaResultado().pausa());}
    }
    @Test void crucetaSeleccionaLaConfirmacionDelPactoAdvertido()throws Exception{
        String preview=System.getProperty("ad.mision7.vista");System.setProperty("ad.mision7.vista","pacto");
        try(var v=new MisionSieteVista(2026,EstadoCiudad.inicial(),false,false)){
            v.iniciarEn(null);
            assertTrue(v.vistaResultado().corredores()[0].evento().advertencia());
            assertFalse(v.vistaResultado().electoral().juanCorrupto());
            v.mando(new EstadoMando("Xbox",0,0,0,0,Set.of(EstadoMando.Boton.DERECHA)));
            v.mando(new EstadoMando("Xbox",0,0,0,0,Set.of()));
            v.mando(new EstadoMando("Xbox",0,0,0,0,Set.of(EstadoMando.Boton.A)));
            assertTrue(v.vistaResultado().electoral().juanCorrupto());
        }finally{if(preview==null)System.clearProperty("ad.mision7.vista");else System.setProperty("ad.mision7.vista",preview);}
    }
    @Test void hojasNuevasTienenTransparenciaYTodosLosCuadrosSonDistintos()throws Exception{
        for(var dato:List.of(new String[]{"ciudadanos-estados", "4","6"},new String[]{"reacciones","4","2"},new String[]{"objetos","4","4"})){
            try(var in=getClass().getResourceAsStream("/cliente/mision7/"+dato[0]+".png")){assertNotNull(in);var im=ImageIO.read(in);assertTrue(im.getColorModel().hasAlpha());int columnas=Integer.parseInt(dato[1]),filas=Integer.parseInt(dato[2]);Set<Long> hashes=new HashSet<>();
                for(int r=0;r<filas;r++)for(int c=0;c<columnas;c++){long hash=1;int opacos=0;for(int y=r*im.getHeight()/filas;y<(r+1)*im.getHeight()/filas;y++)for(int x=c*im.getWidth()/columnas;x<(c+1)*im.getWidth()/columnas;x++){int color=im.getRGB(x,y);if((color>>>24)>32)opacos++;hash=hash*31+color;}assertTrue(opacos>200);hashes.add(hash);}
                assertEquals(columnas*filas,hashes.size(),dato[0]);
            }
        }
    }
    @Test void dosPersonasConfirmanYCorrenConTeclasIndependientes()throws Exception{
        try(var v=new MisionSieteVista(2026,EstadoCiudad.inicial(),false,false)){v.iniciarEn(null);v.raton(200,250);v.raton(650,200);conectar(v);assertEquals(3,v.vistaResultado().humanos());
            tecla(v,69);assertEquals(Fase.ESPERA,v.vistaResultado().fase());tecla(v,10);assertEquals(Fase.ANUNCIO,v.vistaResultado().fase());
            for(int i=0;i<40&&v.vistaResultado().fase()!=Fase.CUENTA;i++){tecla(v,69);tecla(v,10);}assertEquals(Fase.CUENTA,v.vistaResultado().fase());avanzar(v,181);
            v.pulsar(' ',32);v.pulsar(' ',16);avanzar(v,60);v.soltar(32);v.soltar(16);assertTrue(v.vistaResultado().corredores()[0].pulsos()>0);assertTrue(v.vistaResultado().corredores()[1].pulsos()>0);
            avanzar(v,20);int mandoAntes=v.vistaResultado().corredores()[0].pulsos(),tecladoAntes=v.vistaResultado().corredores()[1].pulsos();
            v.mando(new EstadoMando("Xbox",0,0,0,0,Set.of(EstadoMando.Boton.X)));v.pulsar(' ',16);avanzar(v,20);v.soltar(16);
            assertTrue(v.vistaResultado().corredores()[0].pulsos()>mandoAntes,"El mando conserva la carrera de J1");
            assertTrue(v.vistaResultado().corredores()[1].pulsos()>tecladoAntes,"J2 corre con Mayús al mismo tiempo");
            v.pulsar('w',87);v.pulsar(' ',40);float y0=v.vistaResultado().corredores()[0].y(),y1=v.vistaResultado().corredores()[1].y();avanzar(v,2);assertTrue(v.vistaResultado().corredores()[0].y()<y0);assertTrue(v.vistaResultado().corredores()[1].y()>y1);
        }
    }
    @Test void lasTarjetasCompartidasSoloActivanLaNoticiaDelCorredorElegido(){
        String anterior=System.getProperty("ad.mision7.vista");
        System.setProperty("ad.mision7.vista","dos");
        try(var vista=new MisionSieteVista(2026,EstadoCiudad.inicial(),false,false)){
            vista.iniciarEn(null);
            Evento periodista=vista.vistaResultado().corredores()[0].evento();
            Evento influencer=vista.vistaResultado().corredores()[1].evento();
            assertNotNull(periodista);
            assertNotNull(influencer);
            vista.raton(640,642);
            vista.raton(860,642);
            assertEquals(periodista,vista.vistaResultado().corredores()[0].evento());
            assertEquals(influencer,vista.vistaResultado().corredores()[1].evento());
            vista.raton(680,642);
            assertEquals(periodista,vista.vistaResultado().corredores()[0].evento());
            assertNull(vista.vistaResultado().corredores()[1].evento());
            vista.raton(50,642);
            assertNull(vista.vistaResultado().corredores()[0].evento());
        }finally{
            if(anterior==null)System.clearProperty("ad.mision7.vista");
            else System.setProperty("ad.mision7.vista",anterior);
        }
    }
    @Test void repetirUnaCarreraConservaLaCiudadDeLaAnterior()throws Exception{
        String preview=System.getProperty("ad.mision7.vista");System.setProperty("ad.mision7.vista","resultado");
        try(var v=new MisionSieteVista(2026,EstadoCiudad.inicial(),false,false)){v.iniciarEn(null);assertEquals(Fase.RESULTADO,v.vistaResultado().fase());v.actualizar(0);int[] ciudad=v.vistaResultado().ciudad();tecla(v,82);conectar(v);assertArrayEquals(ciudad,v.vistaResultado().base());}
        finally{if(preview==null)System.clearProperty("ad.mision7.vista");else System.setProperty("ad.mision7.vista",preview);}
    }
    @Test void usarAyudaNuncaConfirmaUnPactoPorAccidente()throws Exception{
        String preview=System.getProperty("ad.mision7.vista");System.setProperty("ad.mision7.vista","pacto");
        try(var v=new MisionSieteVista(2026,EstadoCiudad.inicial(),false,false)){v.iniciarEn(null);assertTrue(v.vistaResultado().corredores()[0].evento().advertencia());tecla(v,81);assertFalse(v.vistaResultado().electoral().juanCorrupto());assertNotNull(v.vistaResultado().corredores()[0].evento());tecla(v,50);assertTrue(v.vistaResultado().electoral().juanCorrupto());}
        finally{if(preview==null)System.clearProperty("ad.mision7.vista");else System.setProperty("ad.mision7.vista",preview);}
    }
    @Test void teclaEConfirmaLaNoticiaYQConservaSuUsoDeAyuda()throws Exception{
        String preview=System.getProperty("ad.mision7.vista");System.setProperty("ad.mision7.vista","pacto");
        try(var v=new MisionSieteVista(2026,EstadoCiudad.inicial(),false,false)){
            v.iniciarEn(null);tecla(v,81);
            assertNotNull(v.vistaResultado().corredores()[0].evento());
            tecla(v,69);
            assertNull(v.vistaResultado().corredores()[0].evento());
            assertFalse(v.vistaResultado().electoral().juanCorrupto());
        }finally{if(preview==null)System.clearProperty("ad.mision7.vista");else System.setProperty("ad.mision7.vista",preview);}
    }
}
