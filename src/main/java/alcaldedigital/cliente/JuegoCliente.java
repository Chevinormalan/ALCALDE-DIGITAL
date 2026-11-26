package alcaldedigital.cliente;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.compartido.modelo.InvestigacionCiudadana.Escena;
import alcaldedigital.compartido.protocolo.SesionJuego;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.cliente.visualizadores.ConsultaEstructuras;
import alcaldedigital.cliente.mision3.MisionTresSketch;
import alcaldedigital.cliente.mision2.MisionDosSketch;
import alcaldedigital.cliente.mision2.RegistroIntentosMisionDos;
import alcaldedigital.cliente.tutorial.RegistroTutorial;
import alcaldedigital.cliente.tutorial.Tutorial;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

/** Control de entrada y navegación; los efectos siempre pasan por la sesión. */
public final class JuegoCliente {
    public enum Pantalla { MENU, TUTORIAL, MISIONES, GUARDADO, DIFICULTAD, DIFICULTAD_DOS, MISION_DOS, MISION_TRES, MISION_CUATRO, MISION_CINCO, MISION_SEIS, MISION_SIETE, MISION_OCHO, ROLES, PARQUE, CIVITAS, EVIDENCIAS, ARBOLES, AYUDA, GRAFICOS, AUDIO, CONTROLES, CONFIRMAR_REINICIO, RESULTADO }
    public final alcaldedigital.cliente.audio.OpcionesAudio audio=new alcaldedigital.cliente.audio.OpcionesAudio();
    public final alcaldedigital.cliente.graficos.OpcionesGraficas graficos=new alcaldedigital.cliente.graficos.OpcionesGraficas();
    public Pantalla pantalla=Pantalla.MENU, anterior=Pantalla.MENU;
    public Pantalla origenArboles=Pantalla.PARQUE;
    public enum Pestana { RADAR, CAMINO }
    public Pestana pestana=Pestana.RADAR;
    public boolean laboratorio, editandoBusqueda, tecladoNumericoActivo, avisos=true;
    public int cursorBusqueda;
    public final ConsultaEstructuras consulta=new ConsultaEstructuras();
    public String aviso="";
    public double segundosAviso;
    public SesionJuego sesion;
    public SesionMisionUno misionUno;
    private boolean prologoPendiente;
    public final InvestigacionSofi investigacionSofi=new InvestigacionSofi();
    private final SesionJuego.Fabrica fabrica;
    private final SesionMisionUno.Fabrica fabricaMisionUno;
    private final RegistroIntentosMisionDos intentosMisionDos;
    private final DoubleSupplier azarCritico;
    public JuegoCliente(SesionJuego.Fabrica fabrica) { this(fabrica,null); }
    public JuegoCliente(SesionJuego.Fabrica fabrica,SesionMisionUno.Fabrica fabricaMisionUno) {
        this(fabrica,fabricaMisionUno,new RegistroIntentosMisionDos(),
            ()->ThreadLocalRandom.current().nextDouble());
    }
    public JuegoCliente(SesionJuego.Fabrica fabrica,SesionMisionUno.Fabrica fabricaMisionUno,
                        RegistroIntentosMisionDos intentosMisionDos,DoubleSupplier azarCritico) {
        this.fabrica=Objects.requireNonNull(fabrica);this.fabricaMisionUno=fabricaMisionUno;
        this.intentosMisionDos=Objects.requireNonNull(intentosMisionDos);
        this.azarCritico=Objects.requireNonNull(azarCritico);
    }
    public boolean misionUnoDisponible() {return fabricaMisionUno!=null;}
    public boolean contraste, movimientoReducido, tiempoAmpliado=true, salir;
    public boolean mandoActivo, pausa, controlesPausa;
    public String nombreMando="";
    public float movimientoMandoX, movimientoMandoY;
    public long semilla=20260917;
    public int direccion, cuadro, foco, pasoAVL=-1, pasoDecision=-1, recorrido, recorridoNario, visita;
    public boolean automatico;
    public String busqueda="", encontrado="";
    public String ultimaAccion="";
    public double animacion, efecto;
    private long solicitud;
    private long solicitudIdMisionUno;
    public final alcaldedigital.compartido.guardado.ProgresoMisiones progreso=new alcaldedigital.compartido.guardado.ProgresoMisiones(alcaldedigital.compartido.guardado.ArchivosPartida.directorio());
    private final RegistroTutorial registroTutorial=new RegistroTutorial(alcaldedigital.compartido.guardado.ArchivosPartida.directorio());
    private Tutorial tutorial;
    private boolean tutorialVisto;
    public Tutorial tutorial(){return pantalla==Pantalla.TUTORIAL?tutorial:null;}
    public boolean pausaUnificada;
    public boolean continuarGuardado;
    public int controlesMision=1;
    public boolean controlesDosJugadores;
    public int misionSolicitada;
    public int modoContinuacion=-1,rolContinuacion=-1;
    private boolean eligiendoPartida;
    private int ultimaMisionAbierta;
    private long consultaPartidas;
    private alcaldedigital.cliente.misiones.PartidasMenu.Partida partidaMenu;
    public alcaldedigital.cliente.misiones.PartidasMenu.Partida continuacionMenu(){
        if(ultimaMisionAbierta==1&&misionUno!=null){
            var v=misionUno.vista();
            var estado=v.fase()!=SesionMisionUno.Fase.RESULTADO?
                alcaldedigital.cliente.misiones.PartidasMenu.Estado.EN_CURSO:
                "BUENO".equals(v.desenlace())?
                    progreso.ganada(1)?alcaldedigital.cliente.misiones.PartidasMenu.Estado.VICTORIA:
                        alcaldedigital.cliente.misiones.PartidasMenu.Estado.EN_CURSO:
                    alcaldedigital.cliente.misiones.PartidasMenu.Estado.DERROTA;
            return new alcaldedigital.cliente.misiones.PartidasMenu.Partida(1,estado,-1,-1,Long.MAX_VALUE,true);
        }
        long ahora=System.nanoTime();
        if(consultaPartidas==0||ahora-consultaPartidas>1_000_000_000L){
            partidaMenu=alcaldedigital.cliente.misiones.PartidasMenu.reciente(progreso);consultaPartidas=ahora;
        }
        return partidaMenu;
    }
    public int numeroMision(){return switch(pantalla){case TUTORIAL->0;case MISION_DOS->2;case MISION_TRES->3;case MISION_CUATRO->4;case MISION_CINCO->5;case MISION_SEIS->6;case MISION_SIETE->7;case MISION_OCHO->8;default->misionUno!=null&&pantalla!=Pantalla.MENU&&pantalla!=Pantalla.MISIONES&&pantalla!=Pantalla.GUARDADO&&pantalla!=Pantalla.DIFICULTAD&&pantalla!=Pantalla.DIFICULTAD_DOS&&pantalla!=Pantalla.GRAFICOS&&pantalla!=Pantalla.AUDIO?1:0;};}
    public void victoria(int n){try{progreso.victoria(n);}catch(java.io.IOException e){aviso="No se pudo guardar el avance: "+e.getMessage();segundosAviso=9;}}
    public EstadoCiudad estadoCampana=EstadoCiudad.inicial();
    public alcaldedigital.compartido.guardado.TablaPuntajes.Fila ultimaFila;
    public void registrarFila(alcaldedigital.compartido.guardado.TablaPuntajes.Fila fila){
        if(fila==null)return;
        ultimaFila=fila;
        try{alcaldedigital.compartido.guardado.TablaPuntajes.registrar(fila);}
        catch(java.io.IOException e){aviso="No se pudo guardar la tabla: "+e.getMessage();segundosAviso=9;}
    }
    private boolean resultadoMisionUnoRegistrado;
    public alcaldedigital.cliente.mision4.MisionCuatroSketch.Resultado ultimoResultadoMisionCuatro;
    public alcaldedigital.cliente.mision6.MisionSeisMotor.Resultado ultimoResultadoMisionSeis;
    private final Set<String> misionesCuatroAcreditadas=new HashSet<>();
    public boolean misionEmbebida() {
        return Set.of(Pantalla.MISION_DOS,Pantalla.MISION_TRES,Pantalla.MISION_CUATRO,
            Pantalla.MISION_CINCO,Pantalla.MISION_SEIS,Pantalla.MISION_SIETE,Pantalla.MISION_OCHO).contains(pantalla);
    }
    public void terminarMisionCuatro(alcaldedigital.cliente.mision4.MisionCuatroSketch.Resultado r) {
        ultimoResultadoMisionCuatro=Objects.requireNonNull(r);if(r.completada())victoria(4);
        if(r.completada())registrarFila(alcaldedigital.cliente.misiones.PuntajesMision.cuatro(r));
        if(r.completada()&&!misionesCuatroAcreditadas.contains(r.partida())) {
            try {
                var repo=new alcaldedigital.servidor.carrera.RepositorioCarrera(
                    alcaldedigital.servidor.batalla.GuardadoBatalla.directorio().resolve("campana.properties"));
                if(r.respuestas().size()!=4||r.respuestas().stream().anyMatch(a->a<0))estadoCampana=repo.acreditarMisionCuatro(r.partida(),r.publicacionesValidadas(),r.respuestasResponsables(),estadoCampana);
                else{
                    int favorables=0,desfavorables=0;
                    for(int i=0;i<r.respuestas().size();i++)if(i!=2){if(r.respuestas().get(i)==0)favorables++;else if(r.respuestas().get(i)==1)desfavorables++;}
                    estadoCampana=repo.acreditarMisionCuatro(r.partida(),r.publicacionesValidadas(),favorables,desfavorables,estadoCampana);
                }
                misionesCuatroAcreditadas.add(r.partida());
            } catch(java.io.IOException e){aviso="No se pudo acreditar la misión 4: "+e.getMessage();segundosAviso=9;}
        }
        abrir(Pantalla.MISIONES);
    }
    public void terminarMisionSeis(alcaldedigital.cliente.mision6.MisionSeisMotor.Resultado r) {
        ultimoResultadoMisionSeis=Objects.requireNonNull(r);if(r.completada()){victoria(6);registrarFila(alcaldedigital.cliente.misiones.PuntajesMision.seis(r));}abrir(Pantalla.MISIONES);
    }
    public MisionTresSketch.Resultado ultimoResultadoMisionTres;
    public MisionDosSketch.Resultado ultimoResultadoMisionDos;
    public MisionDosSketch.Dificultad dificultadMisionDos=MisionDosSketch.Dificultad.NORMAL;
    public boolean falloCriticoMisionDos;
    public void terminarMisionDos(MisionDosSketch.Resultado resultado) {
        ultimoResultadoMisionDos=resultado;if(resultado.terminada()&&resultado.exito()&&!resultado.falloCritico())victoria(2);
        if(resultado.terminada()&&!resultado.falloCritico()) estadoCampana=ProgresoCampana.aplicarMisionDos(
            estadoCampana,resultado.protegidos());
        abrir(Pantalla.MISIONES);
    }
    public void terminarMisionTres(MisionTresSketch.Resultado resultado) {
        ultimoResultadoMisionTres=resultado;if(resultado.completada())victoria(3);
        if(resultado.completada()) estadoCampana=ProgresoCampana.aplicarMisionTres(
            estadoCampana,resultado.confianza(),resultado.desinformacion());
        abrir(Pantalla.MISIONES);
    }
    private void registrarResultadoMisionUno() {
        if(misionUno!=null&&!resultadoMisionUnoRegistrado&&
            misionUno.vista().fase()==SesionMisionUno.Fase.RESULTADO) {
            estadoCampana=ProgresoCampana.aplicarMisionUno(estadoCampana,misionUno.vista().ciudad());
            resultadoMisionUnoRegistrado=true;if("BUENO".equals(misionUno.vista().desenlace()))victoria(1);
            if(misionUno instanceof alcaldedigital.servidor.sesion.SesionMisionUnoLocal local)
                registrarFila(alcaldedigital.cliente.misiones.PuntajesMision.uno(local));
        }
    }
    public final Set<Integer> teclas=new HashSet<>();
    public void iniciar(Rol rol) {
        misionUno=null;
        prologoPendiente=false;
        investigacionSofi.reiniciar();
        sesion=fabrica.crear(rol,semilla,tiempoAmpliado?60:10);
        pantalla=Pantalla.PARQUE; direccion=0; cuadro=0; pasoAVL=-1; pasoDecision=-1; foco=0;
        busqueda="";encontrado="";automatico=false;visita=0;efecto=0;teclas.clear();
        laboratorio=false;pestana=Pestana.RADAR;origenArboles=Pantalla.PARQUE;
        editandoBusqueda=false;cursorBusqueda=0;consulta.iniciar(sesion);aviso="";segundosAviso=0;
    }
    public void abrir(Pantalla destino) {
        if(destino!=Pantalla.PARQUE)prologoPendiente=false;
        boolean conservarPausa=pausa&&pausaUnificada&&Set.of(Pantalla.PARQUE,Pantalla.CIVITAS,Pantalla.ARBOLES,Pantalla.AYUDA,Pantalla.RESULTADO).contains(destino);
        if(destino==Pantalla.AYUDA&&pantalla!=destino&&pantalla!=Pantalla.CONTROLES&&pantalla!=Pantalla.GRAFICOS&&pantalla!=Pantalla.AUDIO) anterior=pantalla;
        if(destino==Pantalla.ARBOLES&&pantalla!=destino&&!(pantalla==Pantalla.AYUDA&&anterior==Pantalla.ARBOLES)) origenArboles=pantalla;
        if(destino==Pantalla.TUTORIAL){if(tutorial==null)tutorial=new Tutorial();}
        else tutorial=null;
        pantalla=destino; foco=destino==Pantalla.MISIONES?1:
            destino==Pantalla.GRAFICOS?1:destino==Pantalla.DIFICULTAD||destino==Pantalla.DIFICULTAD_DOS?2:0;
        if(destino==Pantalla.MENU)consultaPartidas=0;
        if(destino==Pantalla.MISION_DOS)ultimaMisionAbierta=2;
        else if(destino==Pantalla.MISION_TRES)ultimaMisionAbierta=3;
        else if(destino==Pantalla.MISION_CUATRO)ultimaMisionAbierta=4;
        else if(destino==Pantalla.MISION_CINCO)ultimaMisionAbierta=5;
        else if(destino==Pantalla.MISION_SEIS)ultimaMisionAbierta=6;
        else if(destino==Pantalla.MISION_SIETE)ultimaMisionAbierta=7;
        else if(misionUno!=null&&Set.of(Pantalla.PARQUE,Pantalla.CIVITAS,Pantalla.RESULTADO).contains(destino))ultimaMisionAbierta=1;
        editandoBusqueda=false;tecladoNumericoActivo=false; teclas.clear();
        pausa=conservarPausa;controlesPausa=false;movimientoMandoX=movimientoMandoY=0;
    }
    public void cerrarArboles() { abrir(origenArboles); }
    boolean prologoPendiente(){return prologoPendiente;}
    boolean tomarPrologo(){
        boolean iniciar=prologoPendiente;
        prologoPendiente=false;
        return iniciar;
    }
    private void jugar(){
        if(!misionUnoDisponible()){abrir(Pantalla.ROLES);return;}
        if(!tutorialVisto){
            try{tutorialVisto=registroTutorial.visto();}
            catch(java.io.IOException e){aviso="No se pudo leer el registro del tutorial.";segundosAviso=8;}
            tutorialVisto|=progreso.todasDesbloqueadas()||
                java.util.stream.IntStream.rangeClosed(1,8).anyMatch(progreso::ganada)||continuacionMenu()!=null;
        }
        abrir(tutorialVisto?Pantalla.MISIONES:Pantalla.TUTORIAL);
    }
    private void cerrarTutorial(Tutorial.Resultado resultado){
        tutorialVisto=true;
        try{registroTutorial.guardar(resultado);}
        catch(java.io.IOException e){aviso="No se pudo guardar el tutorial: "+e.getMessage();segundosAviso=8;}
        abrir(Pantalla.MISIONES);
    }
    public int indiceAVL() { return pasoAVL<0?sesion.pasosAVL().size()-1:Math.min(pasoAVL,sesion.pasosAVL().size()-1); }
    public int indiceDecision() { return pasoDecision<0?sesion.pasosDecisiones().size()-1:Math.min(pasoDecision,sesion.pasosDecisiones().size()-1); }
    public ArbolAVLPublicaciones.Vista vistaAVL() { return pasoAVL<0?sesion.vista().avl():sesion.pasosAVL().get(indiceAVL()).raiz(); }
    public ArbolDecisiones.Vista vistaDecision() { return pasoDecision<0?sesion.vista().decisiones():sesion.pasosDecisiones().get(indiceDecision()).raiz(); }
    private void seleccionCambiada() { visita=0;automatico=false;encontrado="";editandoBusqueda=false;foco=0; }
    public void accion(String accion) {
        if(accion.equals("repetirMisionUno")&&pantalla==Pantalla.RESULTADO&&misionUno!=null){
            continuarGuardado=false;misionSolicitada=1;abrir(Pantalla.DIFICULTAD);return;
        }
        if(accion.equals("audio")&&pantalla==Pantalla.AYUDA){abrir(Pantalla.AUDIO);return;}
        if(accion.startsWith("audio:")&&pantalla==Pantalla.AUDIO){String a=accion.substring(6);if(a.equals("restaurar"))audio.restaurar();else if(a.equals("prueba"))alcaldedigital.cliente.audio.MotorAudio.global().interfaz("confirmar");else if(a.equals("mudo")||a.equals("suave")||a.equals("voces"))audio.alternar(a);else{String[] partes=a.split(":");audio.cambiar(alcaldedigital.cliente.audio.OpcionesAudio.Bus.valueOf(partes[0]),Integer.parseInt(partes[1]));}return;}

        if(accion.equals("graficos")&&pantalla==Pantalla.AYUDA){graficos.abrir();abrir(Pantalla.GRAFICOS);return;}
        if(accion.startsWith("grafico:")&&pantalla==Pantalla.GRAFICOS){switch(accion.substring(8)){
            case "modo"->graficos.modo();case "tamano"->graficos.tamano();case "escalado"->graficos.escalado();case "filtro"->graficos.filtro();case "aplicar"->graficos.aplicar();case "restaurar"->graficos.restaurar();default->{}
        }return;}

        if(accion.matches("mision[1-8]")){
            int numero=accion.charAt(6)-'0';if(!progreso.disponible(numero)){aviso="Gana la misión "+progreso.requisitoPendiente(numero)+" para desbloquear esta misión.";segundosAviso=8;return;}
            if(pantalla!=Pantalla.MISIONES&&!(eligiendoPartida&&pantalla==Pantalla.GUARDADO))return;
            if(!eligiendoPartida){misionSolicitada=numero;continuarGuardado=false;modoContinuacion=rolContinuacion=-1;
                if(alcaldedigital.compartido.guardado.GuardadoCampana.existe(numero)){abrir(Pantalla.GUARDADO);return;}}
        }
        if(accion.startsWith("partida:")&&pantalla==Pantalla.GUARDADO){
            continuarGuardado=accion.equals("partida:continuar");eligiendoPartida=true;
            if(continuarGuardado&&(misionSolicitada==5||misionSolicitada==6||misionSolicitada==7)){
                var partida=alcaldedigital.cliente.misiones.PartidasMenu.reciente(progreso,misionSolicitada);
                if(partida!=null){modoContinuacion=partida.modo();rolContinuacion=partida.rol();}
            }
            abrir(Pantalla.MISIONES);accion("mision"+misionSolicitada);eligiendoPartida=false;return;
        }
        if(tecladoNumericoActivo) {
            if(accion.startsWith("digito:")&&busqueda.length()<19) {
                busqueda=busqueda.substring(0,cursorBusqueda)+accion.charAt(7)+busqueda.substring(cursorBusqueda);
                cursorBusqueda++;
                return;
            }
            if(accion.equals("borrarDigito")) {
                if(cursorBusqueda>0) {busqueda=busqueda.substring(0,cursorBusqueda-1)+busqueda.substring(cursorBusqueda);cursorBusqueda--;}
                return;
            }
            if(accion.equals("cerrarTeclado")) {tecladoNumericoActivo=false;editandoBusqueda=false;return;}
            if(accion.equals("buscar")) tecladoNumericoActivo=false;
        }
        if(accion.equals("pausa")) {
            if(pantalla==Pantalla.MISION_DOS||pantalla==Pantalla.MISION_TRES||misionUno!=null&&
                pantalla!=Pantalla.MENU&&pantalla!=Pantalla.MISIONES&&
                pantalla!=Pantalla.DIFICULTAD&&pantalla!=Pantalla.DIFICULTAD_DOS&&pantalla!=Pantalla.GRAFICOS&&pantalla!=Pantalla.AUDIO) {
                pausa=!pausa;controlesPausa=false;foco=0;
            }
            return;
        }
        if(pausa&&!pausaUnificada) {
            if(accion.equals("reanudar")) {pausa=false;controlesPausa=false;foco=0;}
            else if(accion.equals("controlesPausa")) {controlesPausa=!controlesPausa;foco=1;}
            else if(accion.equals("salirMision")&&pantalla!=Pantalla.MISION_TRES&&pantalla!=Pantalla.MISION_DOS) abrir(Pantalla.MISIONES);
            return;
        }
        if(accion.equals("mision4")){if(pantalla==Pantalla.MISIONES)abrir(Pantalla.MISION_CUATRO);return;}
        if(accion.equals("mision6")){if(pantalla==Pantalla.MISIONES)abrir(Pantalla.MISION_SEIS);return;}
        if(accion.equals("mision5")){if(pantalla==Pantalla.MISIONES)abrir(Pantalla.MISION_CINCO);return;}
        if(accion.equals("mision8")){if(pantalla==Pantalla.MISIONES)abrir(Pantalla.MISION_OCHO);return;}
        if(accion.equals("mision7")){if(pantalla==Pantalla.MISIONES)abrir(Pantalla.MISION_SIETE);return;}
        if(accion.equals("mision3")) {
            if(pantalla==Pantalla.MISIONES) abrir(Pantalla.MISION_TRES);
            return;
        }
        if(accion.equals("mision1")&&continuarGuardado){
            try{var p=alcaldedigital.compartido.guardado.GuardadoCampana.cargar(1);misionUno=alcaldedigital.servidor.sesion.SesionMisionUnoLocal.restaurar(p);sesion=null;resultadoMisionUnoRegistrado=Boolean.parseBoolean(p.getProperty("resultadoRegistrado"));
                investigacionSofi.reiniciar();
                prepararAudioSofi();
                var investigacion=misionUno.investigacion();
                abrir(investigacion!=null&&investigacion.premium()&&misionUno.vista().fase()==SesionMisionUno.Fase.CRISIS?
                    Pantalla.PARQUE:Pantalla.valueOf(p.getProperty("pantalla","PARQUE")));return;
            }catch(Exception e){aviso="No se pudo recuperar: "+e.getMessage();segundosAviso=9;return;}
        }
        if(accion.equals("mision2")&&continuarGuardado){abrir(Pantalla.MISION_DOS);return;}
        if(accion.equals("mision2")) {
            if(pantalla==Pantalla.MISIONES) abrir(Pantalla.DIFICULTAD_DOS);
            return;
        }
        if(accion.startsWith("m2dificultad:")&&!progreso.disponible(2))return;
        if(accion.startsWith("m2dificultad:")) {
            if(pantalla!=Pantalla.DIFICULTAD_DOS) return;
            try {dificultadMisionDos=MisionDosSketch.Dificultad.valueOf(accion.substring(13));}
            catch(IllegalArgumentException e) {return;}
            falloCriticoMisionDos=dificultadMisionDos==MisionDosSketch.Dificultad.DIFICIL&&
                intentosMisionDos.sortearPrimerDificil(azarCritico.getAsDouble());
            abrir(Pantalla.MISION_DOS);
            return;
        }
        if(accion.equals("mision1")) {
            if(fabricaMisionUno==null) return;
            abrir(Pantalla.DIFICULTAD);return;
        }
        if(accion.startsWith("m1dificultad:")) {
            if(fabricaMisionUno==null||pantalla!=Pantalla.DIFICULTAD) return;
            SesionMisionUno.Dificultad dificultad;
            try {dificultad=SesionMisionUno.Dificultad.valueOf(accion.substring(13));}
            catch(IllegalArgumentException e) {return;}
            sesion=null;misionUno=fabricaMisionUno.crear(semilla++,dificultad);
            misionUno.activarInvestigacion();
            investigacionSofi.reiniciar();
            prepararAudioSofi();
            resultadoMisionUnoRegistrado=false;
            direccion=0;cuadro=0;abrir(Pantalla.PARQUE);prologoPendiente=true;return;
        }
        if(misionUno!=null&&misionUno.investigacion()!=null&&accionInvestigacionSofi(accion)) return;
        if(misionUno!=null&&(Set.of("continuar","interactuar").contains(accion)||accion.startsWith("m1:"))) {
            String solicitud=accion.startsWith("m1:")?accion.substring(3):accion;
            misionUno.solicitar("mision1-"+java.util.UUID.randomUUID(),solicitud);
            registrarResultadoMisionUno();
            var fase=misionUno.vista().fase();
            if(fase==SesionMisionUno.Fase.CIVITAS) abrir(Pantalla.CIVITAS);
            else if(fase==SesionMisionUno.Fase.CRISIS) abrir(Pantalla.PARQUE);
            else if(fase==SesionMisionUno.Fase.RESULTADO) abrir(Pantalla.RESULTADO);
            return;
        }
        if(misionUno!=null&&accion.equals("arboles")) {
            if(pantalla==Pantalla.ARBOLES) abrir(origenArboles);
            else {origenArboles=pantalla;abrir(Pantalla.ARBOLES);}
            return;
        }
        if(accion.startsWith("nodo:")) {
            String id=accion.substring(5);var nodo=ConsultaEstructuras.buscar(vistaDecision(),id);
            encontrado=nodo==null?"No existe en este estado":id+": "+nodo.texto();return;
        }
        if(accion.startsWith("rol:")) { iniciar(Rol.valueOf(accion.substring(4))); return; }
        switch(accion) {
            case "jugar" -> jugar();
            case "desbloquearTodas" -> {
                if(pantalla!=Pantalla.MISIONES)break;
                try{progreso.desbloquearTodas();aviso="Misiones 1 a 8 disponibles. Puedes elegir un final de prueba en la misión 8.";segundosAviso=8;}
                catch(java.io.IOException e){aviso="No se pudo guardar el desbloqueo: "+e.getMessage();segundosAviso=9;}
            }
            case "pedirRestablecer" -> {if(pantalla==Pantalla.MISIONES)abrir(Pantalla.CONFIRMAR_REINICIO);}
            case "cancelarRestablecer" -> {if(pantalla==Pantalla.CONFIRMAR_REINICIO)abrir(Pantalla.MISIONES);}
            case "confirmarRestablecer" -> {
                if(pantalla!=Pantalla.CONFIRMAR_REINICIO)break;
                try{
                    var respaldo=alcaldedigital.compartido.guardado.RestablecerJuego.restablecer();
                    progreso.reiniciar();intentosMisionDos.reiniciar();
                    estadoCampana=EstadoCiudad.inicial();misionUno=null;sesion=null;
                    investigacionSofi.reiniciar();
                    ultimaFila=null;ultimoResultadoMisionDos=null;ultimoResultadoMisionTres=null;
                    ultimoResultadoMisionCuatro=null;ultimoResultadoMisionSeis=null;
                    misionesCuatroAcreditadas.clear();resultadoMisionUnoRegistrado=false;
                    ultimaMisionAbierta=0;partidaMenu=null;consultaPartidas=0;
                    continuarGuardado=false;eligiendoPartida=false;modoContinuacion=rolContinuacion=-1;
                    misionSolicitada=0;falloCriticoMisionDos=false;pausa=false;pausaUnificada=false;
                    tutorialVisto=false;
                    abrir(Pantalla.MENU);
                    aviso=respaldo==null?"Juego restablecido. Pulsa Jugar para empezar.":
                        "Juego restablecido. Perfil anterior guardado en "+respaldo.getFileName()+".";
                    segundosAviso=12;
                }catch(java.io.IOException e){aviso="No se pudo restablecer: "+e.getMessage();segundosAviso=10;}
            }
            case "continuarMision" -> {
                if(pantalla!=Pantalla.MENU)break;
                var partida=continuacionMenu();if(partida==null||!progreso.disponible(partida.mision()))break;
                if(partida.estado()==alcaldedigital.cliente.misiones.PartidasMenu.Estado.VICTORIA){abrir(Pantalla.MISIONES);break;}
                if(partida.memoria()&&partida.estado()==alcaldedigital.cliente.misiones.PartidasMenu.Estado.EN_CURSO){
                    var inv=misionUno.investigacion();
                    var fase=misionUno.vista().fase();
                    abrir(fase==SesionMisionUno.Fase.RESULTADO?Pantalla.RESULTADO:
                        (inv==null?fase==SesionMisionUno.Fase.CIVITAS:inv.abierto()!=0||!inv.premium()&&inv.vecino()>=0||investigacionSofi.libreta)?
                            Pantalla.CIVITAS:Pantalla.PARQUE);
                    break;
                }
                misionSolicitada=partida.mision();continuarGuardado=partida.estado()==alcaldedigital.cliente.misiones.PartidasMenu.Estado.EN_CURSO;
                modoContinuacion=partida.modo();rolContinuacion=partida.rol();
                eligiendoPartida=true;abrir(Pantalla.MISIONES);accion("mision"+misionSolicitada);eligiendoPartida=false;
            }
            case "menu" -> abrir(Pantalla.MENU);
            case "salir" -> salir=true;
            case "contraste" -> contraste=!contraste;
            case "movimiento" -> movimientoReducido=!movimientoReducido;
            case "avisos" -> avisos=!avisos;
            case "tiempo" -> tiempoAmpliado=!tiempoAmpliado;
            case "semilla" -> semilla++;
            case "ayuda" -> abrir(Pantalla.AYUDA);
            case "verControles" -> {if(pantalla==Pantalla.AYUDA){controlesMision=1;controlesDosJugadores=false;abrir(Pantalla.CONTROLES);}}
            case "controlesAnterior" -> {if(pantalla==Pantalla.CONTROLES)controlesMision=Math.max(1,controlesMision-1);}
            case "controlesSiguiente" -> {if(pantalla==Pantalla.CONTROLES)controlesMision=Math.min(8,controlesMision+1);}
            case "controlesModo" -> {if(pantalla==Pantalla.CONTROLES&&controlesMision>=5)controlesDosJugadores=!controlesDosJugadores;}
            case "volver" -> { if(pantalla==Pantalla.CONTROLES||pantalla==Pantalla.GRAFICOS||pantalla==Pantalla.AUDIO) abrir(Pantalla.AYUDA);
                else if(pantalla==Pantalla.CONFIRMAR_REINICIO) abrir(Pantalla.MISIONES);
                else if(pantalla==Pantalla.GUARDADO||pantalla==Pantalla.DIFICULTAD||pantalla==Pantalla.DIFICULTAD_DOS) abrir(Pantalla.MISIONES);
                else if(pantalla==Pantalla.MISION_DOS||pantalla==Pantalla.MISION_TRES) abrir(Pantalla.MISIONES);
                else if(pantalla==Pantalla.MISIONES) abrir(Pantalla.MENU);
                else if(misionUno!=null) abrir(pantalla==Pantalla.ARBOLES?origenArboles:
                    pantalla==Pantalla.AYUDA?anterior:
                    pantalla==Pantalla.CIVITAS?Pantalla.PARQUE:Pantalla.MENU);
                else if(pantalla==Pantalla.ARBOLES) cerrarArboles();
                else abrir(pantalla==Pantalla.AYUDA?anterior:sesion==null?Pantalla.MENU:Pantalla.PARQUE); }
            case "civitas" -> abrir(Pantalla.CIVITAS);
            case "evidencias" -> abrir(Pantalla.EVIDENCIAS);
            case "arboles" -> { if(sesion!=null) {if(pantalla==Pantalla.ARBOLES) cerrarArboles();else abrir(Pantalla.ARBOLES);} }
            case "laboratorio" -> { if(sesion!=null) {if(pantalla!=Pantalla.ARBOLES) abrir(Pantalla.ARBOLES);laboratorio=true;seleccionCambiada();} }
            case "jugador" -> {laboratorio=false;seleccionCambiada();}
            case "radar" -> {pestana=Pestana.RADAR;seleccionCambiada();}
            case "camino" -> {pestana=Pestana.CAMINO;seleccionCambiada();}
            case "resultado" -> abrir(Pantalla.RESULTADO);
            case "avlAnterior" -> {pasoAVL=Math.max(0,indiceAVL()-1);seleccionCambiada();}
            case "avlSiguiente" -> {pasoAVL=Math.min(sesion.pasosAVL().size()-1,indiceAVL()+1);seleccionCambiada();}
            case "narAnterior" -> {pasoDecision=Math.max(0,indiceDecision()-1);seleccionCambiada();}
            case "narSiguiente" -> {pasoDecision=Math.min(sesion.pasosDecisiones().size()-1,indiceDecision()+1);seleccionCambiada();}
            case "actual" -> {if(pestana==Pestana.RADAR) pasoAVL=-1;else pasoDecision=-1;seleccionCambiada();}
            case "recorrido" -> {recorrido=(recorrido+1)%4;visita=0;}
            case "recorridoNario" -> {recorridoNario=(recorridoNario+1)%3;visita=0;}
            case "visitar" -> visita++;
            case "automatico" -> automatico=!automatico;
            case "campoBusqueda" -> {
                editandoBusqueda=true;cursorBusqueda=busqueda.length();
                tecladoNumericoActivo=mandoActivo&&pantalla==Pantalla.ARBOLES&&laboratorio;
                if(tecladoNumericoActivo) foco=0;
            }
            case "buscar" -> {
                try { long id=Long.parseLong(busqueda);var p=ConsultaEstructuras.publicaciones(vistaAVL(),"inorden").stream().filter(v->v.id()==id).findFirst().orElse(null);
                    encontrado=p==null?"No existe en este estado":"#"+p.id()+" · "+p.autor()+" · riesgo "+p.riesgo(); }
                catch(NumberFormatException e) { encontrado="Escribe un ID numérico, por ejemplo 1."; }
            }
            case "interactuar" -> {
                if(sesion.cerca("aviso")) solicitar("aviso");
                else if(sesion.cerca("fuente")) solicitar("fuente");
                else solicitar("rol");
            }
            default -> solicitar(accion);
        }
    }
    public void guardarMisionUno()throws java.io.IOException{
        if(!(misionUno instanceof alcaldedigital.servidor.sesion.SesionMisionUnoLocal local))throw new java.io.IOException("No hay partida iniciada");
        var p=local.capturar();p.setProperty("pantalla",(pantalla==Pantalla.ARBOLES?origenArboles:pantalla==Pantalla.AYUDA?anterior:pantalla).name());p.setProperty("resultadoRegistrado",Boolean.toString(resultadoMisionUnoRegistrado));
        alcaldedigital.compartido.guardado.GuardadoCampana.guardar(1,p);
    }
    private void solicitar(String accion) {
        if(sesion==null) return;
        int antes=sesion.vista().version();
        sesion.solicitar("local-"+(++solicitud),accion);
        registrarCambio(switch(accion) {
            case "fuente" -> "Contrastar ambas fuentes";case "aviso" -> "Leer aviso municipal";
            case "contexto" -> "Publicar contexto";case "guardar" -> "Guardar pruebas";
            default -> Character.toUpperCase(accion.charAt(0))+accion.substring(1);
        });
        if(sesion.vista().version()!=antes) { efecto=.8; ultimaAccion=accion; }
        if(sesion.vista().terminado()) abrir(Pantalla.RESULTADO);
        else if(accion.equals("verificar")) abrir(Pantalla.PARQUE);
    }
    private void registrarCambio(String causa) {
        if(consulta.registrar(sesion,causa)) {
            var evento=consulta.ultimo();aviso=evento.causa()+" · "+evento.resumen();segundosAviso=9;
        }
    }
    private boolean accionInvestigacionSofi(String accion) {
        if(!(accion.startsWith("invest:")||Set.of("interactuar","civitas","volver").contains(accion))) return false;
        var s=misionUno.vista();
        var antes=misionUno.investigacion();
        if(accion.equals("interactuar")&&antes.calmaClasica()&&antes.escena()==Escena.NINGUNA&&
            s.fase()==SesionMisionUno.Fase.CRISIS) return false;
        if(accion.equals("volver")&&pantalla!=Pantalla.CIVITAS) return false;
        if(s.fase()==SesionMisionUno.Fase.INTRO||s.fase()==SesionMisionUno.Fase.RESULTADO) return false;
        if(pantalla!=Pantalla.PARQUE&&pantalla!=Pantalla.CIVITAS) return true;
        if(accion.equals("civitas")) accion="invest:libreta";
        if(accion.equals("interactuar")) {
            if(pantalla!=Pantalla.PARQUE) return true;
            if(antes.escena()!=Escena.NINGUNA) accion="invest:saltarEscena";
            else if(s.fase()==SesionMisionUno.Fase.CIVITAS) {
                if(antes.premium()&&investigacionSofi.convocatoriaLista(s)&&investigacionSofi.cercaDelTablon(s))
                    accion="invest:irVecinos";
                else {
                    var caso=investigacionSofi.papelCercano(s);
                    if(caso==null) return true;
                    accion="invest:papel:"+caso.id();
                }
            } else {
                var vecino=investigacionSofi.vecinoCercano(s,antes);
                if(vecino==null) return true;
                accion=(antes.premium()?"invest:calmar:":"invest:vecino:")+vecino.id();
            }
        }
        if(accion.equals("invest:libreta")) {
            if(antes.escena()!=Escena.NINGUNA||antes.premium()&&s.fase()==SesionMisionUno.Fase.CRISIS) return true;
            boolean eraLibreta=investigacionSofi.libreta||pantalla==Pantalla.CIVITAS&&antes.abierto()==0&&antes.vecino()<0;
            investigacionSofi.libreta=!eraLibreta;
            if(!eraLibreta||antes.abierto()!=0||antes.vecino()>=0) abrir(Pantalla.CIVITAS);
            else abrir(Pantalla.PARQUE);
            sonidoSofi("civitas");
            return true;
        }
        if(accion.equals("volver")&&investigacionSofi.libreta&&(antes.abierto()!=0||antes.vecino()>=0)) {
            investigacionSofi.libreta=false;foco=0;return true;
        }
        if(accion.equals("volver")) accion="invest:cerrarExpediente";
        String solicitud=accion.substring("invest:".length());
        if(antes.premium()&&solicitud.equals("ayuda")) {
            investigacionSofi.ayuda=!investigacionSofi.ayuda;
            foco=0;return true;
        }
        if(antes.premium()&&!movimientoReducido&&antes.abierto()!=0&&
            animacion<Math.max(investigacionSofi.finRecogida,investigacionSofi.finPagina)&&
            !solicitud.equals("cerrarExpediente")) return true;
        if(solicitud.equals("cerrarExpediente")&&antes.calmaClasica()&&antes.escena()==Escena.NINGUNA) {
            investigacionSofi.libreta=false;abrir(Pantalla.PARQUE);return true;
        }
        misionUno.solicitar("m1-"+java.util.UUID.randomUUID(),solicitud);
        var despues=misionUno.investigacion();
        if(solicitud.startsWith("calmar:")&&antes.vecino()!=despues.vecino()&&despues.ciudadanos()==antes.ciudadanos())
            sonidoSofi("dialogo");
        if(solicitud.startsWith("papel:")&&despues.abierto()!=0&&despues.abierto()!=antes.abierto()) {
            investigacionSofi.reiniciar();
            investigacionSofi.finRecogida=animacion+(despues.premium()?1.12:.42);
            investigacionSofi.ultimaPista=animacion;
            abrir(Pantalla.CIVITAS);foco=1;sonidoSofi("recoger");
        } else if(solicitud.startsWith("vecino:")&&despues.vecino()>=0) {
            investigacionSofi.reiniciar();abrir(Pantalla.CIVITAS);foco=1;sonidoSofi("dialogo");
        } else if(Set.of("cerrarExpediente","irVecinos","saltarEscena").contains(solicitud)&&despues.abierto()==0&&despues.vecino()<0) {
            investigacionSofi.reiniciar();abrir(Pantalla.PARQUE);
            if(despues.escena()==Escena.VECINOS&&antes.escena()!=Escena.VECINOS) sonidoSofi(despues.premium()?"pacto":"rumor");
            if(despues.escena()==Escena.CIERRE) sonidoSofi("victoria");
        }
        if(despues.premium()&&solicitud.equals("publicacion")&&!antes.publicacion()&&despues.publicacion()) {
            investigacionSofi.finPagina=animacion+.43;foco=0;sonidoSofi("civitas");
        }
        if(despues.premium()&&solicitud.equals("comunicar")&&antes.abierto()!=0&&
            !s.resueltas().get(antes.abierto()-1)&&misionUno.vista().resueltas().get(antes.abierto()-1)) {
            investigacionSofi.finSello=animacion+.58;foco=0;sonidoSofi("confirmar");
        }
        if(solicitud.equals("contraste")&&!antes.contraste()&&despues.contraste()) sonidoSofi("civitas");
        if(solicitud.equals("lupa")&&antes.lupa()!=despues.lupa()) {
            if(despues.lupa()) investigacionSofi.inicioLupa=animacion;
            sonidoSofi("hover");
        }
        if(despues.evidencias()>antes.evidencias()) {
            investigacionSofi.finAviso=animacion+1.65;investigacionSofi.ultimaPista=animacion;
            if(despues.premium()) foco=0;
            sonidoSofi("verificar");
        }
        if(!despues.premium()&&despues.clasificados()>antes.clasificados()) {
            investigacionSofi.finAviso=animacion+.36;sonidoSofi("confirmar");
        }
        if(despues.ciudadanos()>antes.ciudadanos()) {
            investigacionSofi.finSello=animacion+.62;foco=0;sonidoSofi("confirmar");
            if(despues.premium()) {
                investigacionSofi.vecinoAviso=antes.vecino();
                investigacionSofi.finRespuestaVecino=animacion+2.0;
                if(despues.escena()==Escena.CIERRE) sonidoSofi("victoria");
            }
        }
        registrarResultadoMisionUno();
        if(misionUno.vista().fase()==SesionMisionUno.Fase.RESULTADO) abrir(Pantalla.RESULTADO);
        return true;
    }
    private void prepararAudioSofi() {
        var inv=misionUno.investigacion();
        if(inv==null||!inv.premium()) return;
        var audio=alcaldedigital.cliente.audio.MotorAudio.global();
        for(String efecto:List.of("recoger","civitas","verificar","confirmar","pacto","dialogo","viento","victoria"))
            audio.precargar("audio/fx/"+efecto);
    }
    private static void sonidoSofi(String efecto) {
        alcaldedigital.cliente.audio.MotorAudio.global().interfaz(efecto);
    }
    public void actualizar(double dt) {
        if(pausa) return;
        dt=Math.min(.1,Math.max(0,dt)); animacion+=dt;efecto=Math.max(0,efecto-dt);
        if(pantalla==Pantalla.MENU||pantalla==Pantalla.MISIONES||pantalla==Pantalla.TUTORIAL)segundosAviso=Math.max(0,segundosAviso-dt);
        if(tutorial()!=null){
            tutorial.actualizar(dt);
            if(tutorial.resultado()!=null)cerrarTutorial(tutorial.resultado());
            return;
        }
        if(misionUno!=null) {
            var fase=misionUno.vista().fase();
            if(pantalla==Pantalla.PARQUE||pantalla==Pantalla.CIVITAS) {
                var faseAnterior=fase;
                misionUno.actualizar(dt);
                registrarResultadoMisionUno();
                fase=misionUno.vista().fase();
                if(fase==SesionMisionUno.Fase.CIVITAS&&faseAnterior==SesionMisionUno.Fase.INTRO) {
                    var inv=misionUno.investigacion();
                    if(inv==null) abrir(Pantalla.CIVITAS);
                    else if(inv.premium())
                        alcaldedigital.cliente.audio.MotorAudio.global().efecto("mision1","papeles","audio/fx/viento",.3f,.25,
                            alcaldedigital.cliente.audio.OpcionesAudio.Bus.EFECTOS);
                }
                if(fase==SesionMisionUno.Fase.RESULTADO) {abrir(Pantalla.RESULTADO);return;}
            }
            if(pantalla==Pantalla.PARQUE&&(fase==SesionMisionUno.Fase.CRISIS||fase==SesionMisionUno.Fase.CIVITAS)) {
                float dx=(teclas.contains(68)||teclas.contains(39)?1:0)-(teclas.contains(65)||teclas.contains(37)?1:0);
                float dy=(teclas.contains(83)||teclas.contains(40)?1:0)-(teclas.contains(87)||teclas.contains(38)?1:0);
                if(dx==0&&dy==0) {dx=movimientoMandoX;dy=movimientoMandoY;}
                if(dx!=0||dy!=0) {
                    float n=(float)Math.hypot(dx,dy);if(n>1) {dx/=n;dy/=n;}
                    int pasos=Math.max(1,(int)Math.ceil(dt*180/5));
                    var antes=misionUno.vista();
                    for(int i=0;i<pasos;i++) misionUno.mover(dx*(float)dt*180/pasos,dy*(float)dt*180/pasos);
                    var despues=misionUno.vista();
                    if(misionUno.investigacion()!=null&&(antes.x()!=despues.x()||antes.y()!=despues.y()))
                        alcaldedigital.cliente.audio.MotorAudio.global().efecto("mision1","paso",
                            "presentacion/audio/paso_adoquin",.26f,.31,alcaldedigital.cliente.audio.OpcionesAudio.Bus.EFECTOS);
                    cuadro=movimientoReducido?0:(int)(animacion*9)%4;
                } else {misionUno.mover(0,0);cuadro=0;}
            }
            return;
        }
        if(pantalla==Pantalla.PARQUE||pantalla==Pantalla.CIVITAS||pantalla==Pantalla.RESULTADO) segundosAviso=Math.max(0,segundosAviso-dt);
        if(sesion==null) return;
        if(pantalla==Pantalla.PARQUE||pantalla==Pantalla.CIVITAS) {
            sesion.actualizar(dt);
            registrarCambio("Plazo agotado: ignorar");
            if(sesion.vista().terminado()) { abrir(Pantalla.RESULTADO);return; }
        }
        if(pantalla==Pantalla.ARBOLES&&laboratorio&&automatico&&!movimientoReducido) {
            double antes=animacion-dt;
            if((int)(antes*1.5)!=(int)(animacion*1.5)) visita++;
        }
        if(pantalla!=Pantalla.PARQUE) return;
        float dx=(teclas.contains(68)||teclas.contains(39)?1:0)-(teclas.contains(65)||teclas.contains(37)?1:0);
        float dy=(teclas.contains(83)||teclas.contains(40)?1:0)-(teclas.contains(87)||teclas.contains(38)?1:0);
        if(dx==0&&dy==0) {dx=movimientoMandoX;dy=movimientoMandoY;}
        if(dx!=0||dy!=0) {
            float n=(float)Math.hypot(dx,dy);if(n>1) {dx/=n;dy/=n;}
            direccion=Math.abs(dx)>Math.abs(dy)?(dx>0?2:1):(dy>0?0:3);
            // Subpasos para no atravesar una colisión cuando baja el framerate.
            int pasos=Math.max(1,(int)Math.ceil(dt*80/3));
            for(int i=0;i<pasos;i++) sesion.mover(dx*(float)dt*80/pasos,dy*(float)dt*80/pasos);
            cuadro=movimientoReducido?0:(int)(animacion*8)%4;
        } else cuadro=0;
    }
    public void tecla(int codigo,char caracter) {
        if(codigo==27&&(pantalla==Pantalla.CONTROLES||pantalla==Pantalla.GRAFICOS||pantalla==Pantalla.AUDIO)){abrir(Pantalla.AYUDA);return;}
        if(codigo==27&&pantalla==Pantalla.CONFIRMAR_REINICIO){abrir(Pantalla.MISIONES);return;}
        if(codigo==27&&(pantalla==Pantalla.DIFICULTAD||pantalla==Pantalla.DIFICULTAD_DOS)) {abrir(Pantalla.MISIONES);return;}
        if(codigo==27&&pantalla==Pantalla.GUARDADO){abrir(Pantalla.MISIONES);return;}
        if(codigo==27&&pantalla==Pantalla.MISIONES) {abrir(Pantalla.MENU);return;}
        if(misionUno!=null) {
            if(codigo==27) {abrir(pantalla==Pantalla.AYUDA?anterior:
                pantalla==Pantalla.ARBOLES?origenArboles:
                pantalla==Pantalla.PARQUE?Pantalla.MENU:Pantalla.PARQUE);return;}
            if(codigo==72) {accion("ayuda");return;}
            if(codigo==84) {accion("arboles");return;}
            if(codigo==67&&misionUno.vista().fase()!=SesionMisionUno.Fase.INTRO) {accion("civitas");return;}
            if((codigo==69||codigo==32)&&pantalla==Pantalla.PARQUE) {
                accion(misionUno.vista().fase()==SesionMisionUno.Fase.INTRO?"continuar":"interactuar");return;
            }
            return;
        }
        if(codigo==27) {
            if(pantalla==Pantalla.ARBOLES) {cerrarArboles();return;}
            Pantalla destino=pantalla==Pantalla.PARQUE?Pantalla.MENU:
                pantalla==Pantalla.AYUDA?anterior:
                sesion==null?Pantalla.MENU:Pantalla.PARQUE;
            abrir(destino);return;
        }
        if(pantalla==Pantalla.ARBOLES&&laboratorio&&editandoBusqueda) {
            if(caracter>='0'&&caracter<='9'&&busqueda.length()<19) {
                busqueda=busqueda.substring(0,cursorBusqueda)+caracter+busqueda.substring(cursorBusqueda);cursorBusqueda++;return;
            }
            if(codigo==8) {if(cursorBusqueda>0) {busqueda=busqueda.substring(0,cursorBusqueda-1)+busqueda.substring(cursorBusqueda);cursorBusqueda--;}return;}
            if(codigo==127) {if(cursorBusqueda<busqueda.length()) busqueda=busqueda.substring(0,cursorBusqueda)+busqueda.substring(cursorBusqueda+1);return;}
            if(codigo==37) {cursorBusqueda=Math.max(0,cursorBusqueda-1);return;}
            if(codigo==39) {cursorBusqueda=Math.min(busqueda.length(),cursorBusqueda+1);return;}
            if(codigo==36) {cursorBusqueda=0;return;}
            if(codigo==35) {cursorBusqueda=busqueda.length();return;}
            if(codigo==10||codigo==13) {accion("buscar");return;}
        }
        if(sesion==null) {if(codigo==72) accion("ayuda"); return;}
        if(codigo==72) accion("ayuda");
        if(codigo==67) accion("civitas");
        if(codigo==73||codigo==74) accion("evidencias");
        if(codigo==84) accion("arboles");
        if((codigo==69||codigo==32)&&pantalla==Pantalla.PARQUE) accion("interactuar");
    }
}
