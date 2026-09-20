package alcaldedigital.cliente;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.compartido.protocolo.SesionJuego;
import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.cliente.visualizadores.ConsultaEstructuras;
import java.util.*;

/** Control de entrada y navegación; los efectos siempre pasan por la sesión. */
public final class JuegoCliente {
    public enum Pantalla { MENU, ROLES, PARQUE, CIVITAS, EVIDENCIAS, ARBOLES, AYUDA, RESULTADO }
    public Pantalla pantalla=Pantalla.MENU, anterior=Pantalla.MENU;
    public Pantalla origenArboles=Pantalla.PARQUE;
    public enum Pestana { RADAR, CAMINO }
    public Pestana pestana=Pestana.RADAR;
    public boolean laboratorio, editandoBusqueda, avisos=true;
    public int cursorBusqueda;
    public final ConsultaEstructuras consulta=new ConsultaEstructuras();
    public String aviso="";
    public double segundosAviso;
    public SesionJuego sesion;
    private final SesionJuego.Fabrica fabrica;
    public JuegoCliente(SesionJuego.Fabrica fabrica) { this.fabrica=Objects.requireNonNull(fabrica); }
    public boolean contraste, movimientoReducido, tiempoAmpliado=true, salir;
    public long semilla=20260917;
    public int direccion, cuadro, foco, pasoAVL=-1, pasoDecision=-1, recorrido, recorridoNario, visita;
    public boolean automatico;
    public String busqueda="", encontrado="";
    public String ultimaAccion="";
    public double animacion, efecto;
    private long solicitud;
    public final Set<Integer> teclas=new HashSet<>();
    public void iniciar(Rol rol) {
        sesion=fabrica.crear(rol,semilla,tiempoAmpliado?60:10);
        pantalla=Pantalla.PARQUE; direccion=0; cuadro=0; pasoAVL=-1; pasoDecision=-1; foco=0;
        busqueda="";encontrado="";automatico=false;visita=0;efecto=0;teclas.clear();
        laboratorio=false;pestana=Pestana.RADAR;origenArboles=Pantalla.PARQUE;
        editandoBusqueda=false;cursorBusqueda=0;consulta.iniciar(sesion);aviso="";segundosAviso=0;
    }
    public void abrir(Pantalla destino) {
        if(destino==Pantalla.AYUDA&&pantalla!=destino) anterior=pantalla;
        if(destino==Pantalla.ARBOLES&&pantalla!=destino&&!(pantalla==Pantalla.AYUDA&&anterior==Pantalla.ARBOLES)) origenArboles=pantalla;
        pantalla=destino; foco=0; editandoBusqueda=false; teclas.clear();
    }
    public void cerrarArboles() { abrir(origenArboles); }
    public int indiceAVL() { return pasoAVL<0?sesion.pasosAVL().size()-1:Math.min(pasoAVL,sesion.pasosAVL().size()-1); }
    public int indiceDecision() { return pasoDecision<0?sesion.pasosDecisiones().size()-1:Math.min(pasoDecision,sesion.pasosDecisiones().size()-1); }
    public ArbolAVLPublicaciones.Vista vistaAVL() { return pasoAVL<0?sesion.vista().avl():sesion.pasosAVL().get(indiceAVL()).raiz(); }
    public ArbolDecisiones.Vista vistaDecision() { return pasoDecision<0?sesion.vista().decisiones():sesion.pasosDecisiones().get(indiceDecision()).raiz(); }
    private void seleccionCambiada() { visita=0;automatico=false;encontrado="";editandoBusqueda=false;foco=0; }
    public void accion(String accion) {
        if(accion.startsWith("nodo:")) {
            String id=accion.substring(5);var nodo=ConsultaEstructuras.buscar(vistaDecision(),id);
            encontrado=nodo==null?"No existe en este estado":id+": "+nodo.texto();return;
        }
        if(accion.startsWith("rol:")) { iniciar(Rol.valueOf(accion.substring(4))); return; }
        switch(accion) {
            case "jugar" -> abrir(Pantalla.ROLES);
            case "menu" -> abrir(Pantalla.MENU);
            case "salir" -> salir=true;
            case "contraste" -> contraste=!contraste;
            case "movimiento" -> movimientoReducido=!movimientoReducido;
            case "avisos" -> avisos=!avisos;
            case "tiempo" -> tiempoAmpliado=!tiempoAmpliado;
            case "semilla" -> semilla++;
            case "ayuda" -> abrir(Pantalla.AYUDA);
            case "volver" -> { if(pantalla==Pantalla.ARBOLES) cerrarArboles();
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
            case "campoBusqueda" -> { editandoBusqueda=true;cursorBusqueda=busqueda.length(); }
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
        dt=Math.min(.1,Math.max(0,dt)); animacion+=dt;efecto=Math.max(0,efecto-dt);
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
        if(dx!=0||dy!=0) {
            float n=(float)Math.hypot(dx,dy);dx/=n;dy/=n;
            direccion=Math.abs(dx)>Math.abs(dy)?(dx>0?2:1):(dy>0?0:3);
            // Subpasos para no atravesar una colisión cuando baja el framerate.
            int pasos=Math.max(1,(int)Math.ceil(dt*80/3));
            for(int i=0;i<pasos;i++) sesion.mover(dx*(float)dt*80/pasos,dy*(float)dt*80/pasos);
            cuadro=movimientoReducido?0:(int)(animacion*8)%4;
        } else cuadro=0;
    }
    public void tecla(int codigo,char caracter) {
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
