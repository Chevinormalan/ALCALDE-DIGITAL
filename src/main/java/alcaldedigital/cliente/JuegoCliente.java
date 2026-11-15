package alcaldedigital.cliente;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.compartido.protocolo.SesionJuego;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.cliente.visualizadores.ConsultaEstructuras;
import alcaldedigital.cliente.mision3.MisionTresSketch;
import alcaldedigital.cliente.mision2.MisionDosSketch;
import alcaldedigital.cliente.mision2.RegistroIntentosMisionDos;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

/** Control de entrada y navegación; los efectos siempre pasan por la sesión. */
public final class JuegoCliente {
    public enum Pantalla { MENU, MISIONES, GUARDADO, DIFICULTAD, DIFICULTAD_DOS, MISION_DOS, MISION_TRES, MISION_CUATRO, MISION_CINCO, MISION_SEIS, MISION_SIETE, ROLES, PARQUE, CIVITAS, EVIDENCIAS, ARBOLES, AYUDA, CONTROLES, CONFIRMAR_REINICIO, RESULTADO }
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
    public int numeroMision(){return switch(pantalla){case MISION_DOS->2;case MISION_TRES->3;case MISION_CUATRO->4;case MISION_CINCO->5;case MISION_SEIS->6;case MISION_SIETE->7;default->misionUno!=null&&pantalla!=Pantalla.MENU&&pantalla!=Pantalla.MISIONES&&pantalla!=Pantalla.GUARDADO&&pantalla!=Pantalla.DIFICULTAD&&pantalla!=Pantalla.DIFICULTAD_DOS?1:0;};}
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
            Pantalla.MISION_CINCO,Pantalla.MISION_SEIS,Pantalla.MISION_SIETE).contains(pantalla);
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
        sesion=fabrica.crear(rol,semilla,tiempoAmpliado?60:10);
        pantalla=Pantalla.PARQUE; direccion=0; cuadro=0; pasoAVL=-1; pasoDecision=-1; foco=0;
        busqueda="";encontrado="";automatico=false;visita=0;efecto=0;teclas.clear();
        laboratorio=false;pestana=Pestana.RADAR;origenArboles=Pantalla.PARQUE;
        editandoBusqueda=false;cursorBusqueda=0;consulta.iniciar(sesion);aviso="";segundosAviso=0;
    }
    public void abrir(Pantalla destino) {
        boolean conservarPausa=pausa&&pausaUnificada&&Set.of(Pantalla.PARQUE,Pantalla.CIVITAS,Pantalla.ARBOLES,Pantalla.AYUDA,Pantalla.RESULTADO).contains(destino);
        if(destino==Pantalla.AYUDA&&pantalla!=destino&&pantalla!=Pantalla.CONTROLES) anterior=pantalla;
        if(destino==Pantalla.ARBOLES&&pantalla!=destino&&!(pantalla==Pantalla.AYUDA&&anterior==Pantalla.ARBOLES)) origenArboles=pantalla;
        pantalla=destino; foco=destino==Pantalla.MISIONES?1:
            destino==Pantalla.DIFICULTAD||destino==Pantalla.DIFICULTAD_DOS?2:0;
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
    public int indiceAVL() { return pasoAVL<0?sesion.pasosAVL().size()-1:Math.min(pasoAVL,sesion.pasosAVL().size()-1); }
    public int indiceDecision() { return pasoDecision<0?sesion.pasosDecisiones().size()-1:Math.min(pasoDecision,sesion.pasosDecisiones().size()-1); }
    public ArbolAVLPublicaciones.Vista vistaAVL() { return pasoAVL<0?sesion.vista().avl():sesion.pasosAVL().get(indiceAVL()).raiz(); }
    public ArbolDecisiones.Vista vistaDecision() { return pasoDecision<0?sesion.vista().decisiones():sesion.pasosDecisiones().get(indiceDecision()).raiz(); }
    private void seleccionCambiada() { visita=0;automatico=false;encontrado="";editandoBusqueda=false;foco=0; }
    public void accion(String accion) {
        if(accion.matches("mision[1-7]")){
            int numero=accion.charAt(6)-'0';if(!progreso.disponible(numero)){aviso="Gana la misión "+progreso.requisitoPendiente(numero)+" para desbloquear esta misión.";segundosAviso=8;return;}
            if(pantalla!=Pantalla.MISIONES&&!(eligiendoPartida&&pantalla==Pantalla.GUARDADO))return;
            if(!eligiendoPartida){misionSolicitada=numero;continuarGuardado=false;modoContinuacion=rolContinuacion=-1;
                if(alcaldedigital.compartido.guardado.GuardadoCampana.existe(numero)){abrir(Pantalla.GUARDADO);return;}}
        }
        if(accion.startsWith("partida:")&&pantalla==Pantalla.GUARDADO){
            continuarGuardado=accion.equals("partida:continuar");eligiendoPartida=true;
            if(continuarGuardado&&(misionSolicitada==5||misionSolicitada==7)){
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
                pantalla!=Pantalla.DIFICULTAD&&pantalla!=Pantalla.DIFICULTAD_DOS) {
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
        if(accion.equals("mision7")){if(pantalla==Pantalla.MISIONES)abrir(Pantalla.MISION_SIETE);return;}
        if(accion.equals("mision3")) {
            if(pantalla==Pantalla.MISIONES) abrir(Pantalla.MISION_TRES);
            return;
        }
        if(accion.equals("mision1")&&continuarGuardado){
            try{var p=alcaldedigital.compartido.guardado.GuardadoCampana.cargar(1);misionUno=alcaldedigital.servidor.sesion.SesionMisionUnoLocal.restaurar(p);sesion=null;resultadoMisionUnoRegistrado=Boolean.parseBoolean(p.getProperty("resultadoRegistrado"));
                abrir(Pantalla.valueOf(p.getProperty("pantalla", "PARQUE")));return;
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
            resultadoMisionUnoRegistrado=false;
            direccion=0;cuadro=0;abrir(Pantalla.PARQUE);return;
        }
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
            case "jugar" -> abrir(misionUnoDisponible()?Pantalla.MISIONES:Pantalla.ROLES);
            case "desbloquearTodas" -> {
                if(pantalla!=Pantalla.MISIONES)break;
                try{progreso.desbloquearTodas();aviso="Misiones 1 a 7 disponibles. Las victorias se registran por separado.";segundosAviso=8;}
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
                    ultimaFila=null;ultimoResultadoMisionDos=null;ultimoResultadoMisionTres=null;
                    ultimoResultadoMisionCuatro=null;ultimoResultadoMisionSeis=null;
                    misionesCuatroAcreditadas.clear();resultadoMisionUnoRegistrado=false;
                    ultimaMisionAbierta=0;partidaMenu=null;consultaPartidas=0;
                    continuarGuardado=false;eligiendoPartida=false;modoContinuacion=rolContinuacion=-1;
                    misionSolicitada=0;falloCriticoMisionDos=false;pausa=false;pausaUnificada=false;
                    abrir(Pantalla.MISIONES);
                    aviso=respaldo==null?"Juego restablecido. Comienza por la misión 1.":
                        "Juego restablecido. Perfil anterior guardado en "+respaldo.getFileName()+".";
                    segundosAviso=12;
                }catch(java.io.IOException e){aviso="No se pudo restablecer: "+e.getMessage();segundosAviso=10;}
            }
            case "continuarMision" -> {
                if(pantalla!=Pantalla.MENU)break;
                var partida=continuacionMenu();if(partida==null||!progreso.disponible(partida.mision()))break;
                if(partida.estado()==alcaldedigital.cliente.misiones.PartidasMenu.Estado.VICTORIA){abrir(Pantalla.MISIONES);break;}
                if(partida.memoria()&&partida.estado()==alcaldedigital.cliente.misiones.PartidasMenu.Estado.EN_CURSO){
                    abrir(misionUno.vista().fase()==SesionMisionUno.Fase.CIVITAS?Pantalla.CIVITAS:
                        misionUno.vista().fase()==SesionMisionUno.Fase.RESULTADO?Pantalla.RESULTADO:Pantalla.PARQUE);
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
            case "controlesSiguiente" -> {if(pantalla==Pantalla.CONTROLES)controlesMision=Math.min(7,controlesMision+1);}
            case "controlesModo" -> {if(pantalla==Pantalla.CONTROLES&&controlesMision>=5)controlesDosJugadores=!controlesDosJugadores;}
            case "volver" -> { if(pantalla==Pantalla.CONTROLES) abrir(Pantalla.AYUDA);
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
    public void actualizar(double dt) {
        if(pausa) return;
        dt=Math.min(.1,Math.max(0,dt)); animacion+=dt;efecto=Math.max(0,efecto-dt);
        if(misionUno!=null) {
            if(pantalla==Pantalla.PARQUE||pantalla==Pantalla.CIVITAS) {
                misionUno.actualizar(dt);
                registrarResultadoMisionUno();
                if(pantalla==Pantalla.PARQUE&&misionUno.vista().fase()==SesionMisionUno.Fase.CIVITAS) {
                    abrir(Pantalla.CIVITAS);return;
                }
                if(misionUno.vista().fase()==SesionMisionUno.Fase.RESULTADO) {abrir(Pantalla.RESULTADO);return;}
            }
            if(pantalla==Pantalla.PARQUE&&misionUno.vista().fase()==SesionMisionUno.Fase.CRISIS) {
                float dx=(teclas.contains(68)||teclas.contains(39)?1:0)-(teclas.contains(65)||teclas.contains(37)?1:0);
                float dy=(teclas.contains(83)||teclas.contains(40)?1:0)-(teclas.contains(87)||teclas.contains(38)?1:0);
                if(dx==0&&dy==0) {dx=movimientoMandoX;dy=movimientoMandoY;}
                if(dx!=0||dy!=0) {
                    float n=(float)Math.hypot(dx,dy);if(n>1) {dx/=n;dy/=n;}
                    int pasos=Math.max(1,(int)Math.ceil(dt*180/5));
                    for(int i=0;i<pasos;i++) misionUno.mover(dx*(float)dt*180/pasos,dy*(float)dt*180/pasos);
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
        if(codigo==27&&pantalla==Pantalla.CONTROLES){abrir(Pantalla.AYUDA);return;}
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
            if(codigo==67&&misionUno.vista().fase()!=SesionMisionUno.Fase.INTRO) {abrir(Pantalla.CIVITAS);return;}
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
