package alcaldedigital.cliente.misiones;

import alcaldedigital.compartido.guardado.ArchivosPartida;
import alcaldedigital.compartido.guardado.PerfilesJugadores;
import alcaldedigital.compartido.guardado.PerfilesJugadores.Perfil;
import alcaldedigital.compartido.guardado.ProgresoMisiones;
import alcaldedigital.compartido.guardado.TablaPuntajes;
import alcaldedigital.compartido.guardado.TablaPuntajes.Fila;
import alcaldedigital.compartido.guardado.TablaPuntajes.Resumen;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/** Selección de perfiles y resultados personales. La campaña sigue usando su tabla propia. */
public final class UsuariosPuntajes {
    @FunctionalInterface
    public interface SelectorPuestos {
        boolean elegir(String partida,int jugadores,Runnable comenzar);
    }

    public enum Panel { USUARIOS, ELIMINAR_USUARIO, PUESTOS, PUNTAJES, CIERRE }
    public record Marca(Perfil perfil, Fila fila, int anterior, int mejor, int acumulado) {
        public int mejora() { return anterior < 0 ? mejor : Math.max(0, mejor-anterior); }
    }
    public record Cierre(Fila equipo, boolean prueba, String motivo, List<Marca> marcas) { }

    private final PerfilesJugadores perfiles;
    private final Consumer<Path> cargarCampana;
    private final Runnable enfocar;
    private List<Perfil> lista=List.of();
    private final List<Perfil> participantes=new ArrayList<>();
    private Perfil activo, consultado, usuarioAEliminar;
    private Panel panel;
    private boolean creando, importar, importacionDisponible;
    private int inicioLista, paginaDetalle, marcaElegida;
    private String nombre="", error="", vinculo="", ultimoCierre="";
    private Runnable comenzar, alElegirUsuario;
    private String partidaPendiente="";
    private Cierre cierre;
    private Consumer<String> accionCierre;
    private Resumen resumen;
    private ProgresoMisiones progresoConsultado;

    public UsuariosPuntajes(Path base,Consumer<Path> cargarCampana,Runnable enfocar) {
        perfiles=new PerfilesJugadores(base);
        this.cargarCampana=cargarCampana;
        this.enfocar=enfocar;
        try {
            leerUsuarios();
            var anterior=perfiles.activo();
            if(anterior!=null) activar(anterior);
        } catch(IOException e) {
            error=e.getMessage();
        }
    }

    public Panel panel() { return panel; }
    public Perfil activo() { return activo; }
    public Perfil consultado() { return consultado; }
    public Perfil usuarioAEliminar() { return usuarioAEliminar; }
    public boolean antesDeJugar() { return alElegirUsuario!=null; }
    public boolean creando() { return creando; }
    public boolean importacionDisponible() { return importacionDisponible; }
    public boolean importar() { return importar; }
    public String nombre() { return nombre; }
    public String error() { return error; }
    public int puesto() { return participantes.indexOf(null)+1; }
    public int paginaDetalle() { return paginaDetalle; }
    public int marcaElegida() { return marcaElegida; }
    public Resumen resumen() { return resumen; }
    public ProgresoMisiones progresoConsultado() { return progresoConsultado; }
    public Cierre cierre() { return cierre; }
    public boolean hayAnterior() { return inicioLista>0; }
    public boolean haySiguiente() { return inicioLista+5<visibles().size(); }
    public List<Perfil> paginaUsuarios() {
        var usuarios=visibles();
        return usuarios.subList(Math.min(inicioLista,usuarios.size()),Math.min(inicioLista+5,usuarios.size()));
    }

    private List<Perfil> visibles() {
        return panel==Panel.PUESTOS ? lista.stream().filter(u -> !participantes.contains(u)).toList() : lista;
    }

    private void leerUsuarios() throws IOException {
        lista=perfiles.listar();
        importacionDisponible=perfiles.hayCampanaAnterior();
    }

    private void activar(Perfil perfil) {
        activo=perfil;
        panel=null;
        nuevaMision();
        cargarCampana.accept(perfiles.directorio(perfil));
        Runnable continuar=alElegirUsuario;
        alElegirUsuario=null;
        if(continuar!=null)continuar.run();
    }

    public boolean pedirUsuarioParaJugar(Runnable continuar) {
        if(activo!=null)return false;
        abrirUsuarios();
        alElegirUsuario=continuar;
        return true;
    }

    public void abrirUsuarios() {
        alElegirUsuario=null;
        panel=Panel.USUARIOS;
        preparandoPanel();
        try {
            leerUsuarios();
            if(lista.isEmpty()) iniciarNombre();
        } catch(IOException e) { error=e.getMessage(); }
    }

    private void preparandoPanel() {
        error="";
        inicioLista=0;
        creando=false;
        enfocar.run();
    }

    private void iniciarNombre() {
        creando=true;
        nombre="";
        error="";
        importar=panel==Panel.USUARIOS&&importacionDisponible;
        enfocar.run();
    }

    public boolean escribir(int codigo,char letra) {
        if(!creando)return false;
        if(codigo==8) {
            if(!nombre.isEmpty())nombre=nombre.substring(0,nombre.offsetByCodePoints(nombre.length(),-1));
            error="";
            return true;
        }
        if(Character.isLetterOrDigit(letra)||" ._'-".indexOf(letra)>=0) {
            if(nombre.length()<28)nombre+=letra;
            error="";
            return true;
        }
        return false;
    }

    public void nuevaMision() {
        participantes.clear();
        if(activo!=null)participantes.add(activo);
        vinculo="";
        partidaPendiente="";
        ultimoCierre="";
    }

    /** Devuelve true cuando se abre el selector. En ese caso comenzar queda diferido. */
    public boolean asignarPuestos(int numero,Runnable comenzar) {
        return asignarPuestos(0,null,numero,comenzar);
    }

    public boolean asignarPuestos(int mision,String partida,int numero,Runnable comenzar) {
        if(activo==null||numero<=1)return false;
        nuevaMision();
        panel=Panel.PUESTOS;
        preparandoPanel();
        this.comenzar=comenzar;
        try {
            leerUsuarios();
            if(partida!=null){
                partidaPendiente=mision+"."+partida;
                var asignados=leerVinculo(partidaPendiente);
                if(asignados!=null&&asignados.size()==numero&&activo.equals(asignados.getFirst())){
                    participantes.clear();
                    participantes.addAll(asignados);
                    if(!participantes.contains(null)){
                        vinculo=partidaPendiente;
                        this.comenzar=null;
                        panel=null;
                        return false;
                    }
                }
            }
        } catch(IOException|IllegalArgumentException e) { error="Participantes: "+e.getMessage(); }
        while(participantes.size()<numero)participantes.add(null);
        return true;
    }

    private void elegirPuesto(Perfil perfil) throws IOException {
        if(participantes.contains(perfil))return;
        int vacante=participantes.indexOf(null);
        participantes.set(vacante,perfil);
        if(participantes.contains(null)){
            preparandoPanel();
            return;
        }
        try {
            if(!partidaPendiente.isBlank()){
                guardarVinculo(partidaPendiente);
                vinculo=partidaPendiente;
            }
        } catch(IOException e) {
            participantes.set(vacante,null);
            throw e;
        }
        panel=null;
        Runnable accion=comenzar;
        comenzar=null;
        accion.run();
    }

    public void vincular(int mision,String id,boolean local) {
        if(activo==null||!local||id==null||id.isBlank())return;
        String clave=mision+"."+id;
        if(clave.equals(vinculo))return;
        try {
            var asignados=leerVinculo(clave);
            if(asignados==null&&participantes.size()>1&&!participantes.contains(null))guardarVinculo(clave);
            else if(asignados!=null){
                participantes.clear();
                participantes.addAll(asignados);
            }
            vinculo=clave;
        } catch(IOException|IllegalArgumentException e) { error="Participantes: "+e.getMessage(); }
    }

    private Path archivoVinculos() {
        return perfiles.directorio(activo).resolve("participantes.properties");
    }

    private List<Perfil> leerVinculo(String clave) throws IOException {
        Properties p=ArchivosPartida.leer(archivoVinculos());
        String vinculados=p==null?null:p.getProperty(clave);
        if(vinculados==null)return null;
        Map<UUID,Perfil> porId=new HashMap<>();
        for(Perfil usuario:lista)porId.put(usuario.id(),usuario);
        var asignados=new ArrayList<Perfil>();
        for(String id:vinculados.split(","))asignados.add(porId.get(UUID.fromString(id)));
        return asignados;
    }

    private void guardarVinculo(String clave) throws IOException {
        Path archivo=archivoVinculos();
        Properties p=ArchivosPartida.leer(archivo);
        if(p==null)p=new Properties();
        p.setProperty(clave,String.join(",",participantes.stream().map(u -> u.id().toString()).toList()));
        p.setProperty("version","1");
        ArchivosPartida.guardar(archivo,p);
    }

    public void abrirPuntajes() {
        panel=Panel.PUNTAJES;
        preparandoPanel();
        consultar(activo);
    }

    private void consultar(Perfil perfil) {
        consultado=perfil;
        resumen=null;
        progresoConsultado=new ProgresoMisiones(perfiles.directorio(perfil));
        try { resumen=TablaPuntajes.resumenPersonal(perfiles.directorio(perfil)); }
        catch(IOException e) { error=e.getMessage(); }
    }

    public void presentar(Fila equipo,String motivo,boolean prueba,boolean lan,int puestoRemoto,
                          IntFunction<Fila> notaPorPuesto,Consumer<String> accion) {
        if(equipo==null||activo==null||equipo.partida().equals(ultimoCierre))return;
        ultimoCierre=equipo.partida();
        panel=Panel.CIERRE;
        preparandoPanel();
        accionCierre=accion;
        paginaDetalle=-1;
        marcaElegida=0;
        List<Marca> marcas=new ArrayList<>();
        List<Perfil> presentes=lan?List.of(activo):participantes;
        for(int i=0;i<presentes.size();i++) {
            Perfil perfil=presentes.get(i);
            if(perfil==null)continue;
            Fila fila=notaPorPuesto.apply(lan?puestoRemoto:i);
            if(fila==null){marcas.add(new Marca(perfil,null,-1,0,-1));continue;}
            if(prueba){marcas.add(new Marca(perfil,fila,-1,0,-1));continue;}
            marcas.add(guardarMarca(new Marca(perfil,fila,-1,0,-1)));
        }
        cierre=new Cierre(equipo,prueba,motivo,List.copyOf(marcas));
    }

    private Marca guardarMarca(Marca marca) {
        Perfil perfil=marca.perfil();
        Fila fila=marca.fila();
        try {
            Path directorio=perfiles.directorio(perfil);
            var antes=TablaPuntajes.resumenPersonal(directorio);
            var anterior=antes.misiones().get(fila.mision()-1).mejor();
            TablaPuntajes.registrarPersonal(directorio,fila);
            var despues=TablaPuntajes.resumenPersonal(directorio);
            var mejor=despues.misiones().get(fila.mision()-1).mejor();
            return new Marca(perfil,fila,anterior==null?-1:anterior.puntaje(),mejor==null?0:mejor.puntaje(),despues.total());
        }catch(IOException e){
            error="No se pudo guardar el resultado de "+perfil.nombre()+". "+e.getMessage();
            return marca;
        }
    }

    public boolean registroPendiente(){
        return cierre!=null&&!cierre.prueba()&&cierre.marcas().stream().anyMatch(m->m.fila()!=null&&m.acumulado()<0);
    }

    private void reintentarRegistro(){
        error="";
        var marcas=cierre.marcas().stream().map(m->m.fila()!=null&&m.acumulado()<0?guardarMarca(m):m).toList();
        cierre=new Cierre(cierre.equipo(),cierre.prueba(),cierre.motivo(),marcas);
        enfocar.run();
    }

    public void accion(String accion) {
        if(panel==null)return;
        if(accion.equals("volver")){volver();return;}
        try {
            if(accion.startsWith("usuario:")) { seleccionar(buscarUsuario(accion.substring(8)));return; }
            if(panel==Panel.USUARIOS&&!creando&&accion.startsWith("eliminarUsuario:")) {
                usuarioAEliminar=buscarUsuario(accion.substring("eliminarUsuario:".length()));
                panel=Panel.ELIMINAR_USUARIO;
                preparandoPanel();
                return;
            }
            switch(accion) {
                case "usuarioNuevo" -> iniciarNombre();
                case "usuarioConfirmar" -> crearUsuario();
                case "usuarioImportar" -> importar=!importar;
                case "usuariosAnterior" -> {inicioLista=Math.max(0,inicioLista-5);enfocar.run();}
                case "usuariosSiguiente" -> {if(haySiguiente())inicioLista+=5;enfocar.run();}
                case "confirmarEliminarUsuario" -> {if(panel==Panel.ELIMINAR_USUARIO)eliminarUsuario();}
                case "resultadoDetalles" -> {paginaDetalle=0;enfocar.run();}
                case "resultadoResumen" -> {paginaDetalle=-1;enfocar.run();}
                case "resultadoAnterior" -> paginaDetalle=Math.max(0,paginaDetalle-1);
                case "resultadoSiguiente" -> paginaDetalle++;
                case "resultadoGuardar" -> reintentarRegistro();
                case "resultadoJugador" -> {marcaElegida=(marcaElegida+1)%cierre.marcas().size();paginaDetalle=-1;enfocar.run();}
                case "resultadoSalir", "resultadoReintentar", "resultadoSiguienteMision", "resultadoDesenlace" -> terminarCierre(accion);
                default -> { }
            }
        } catch(IOException|IllegalArgumentException e) { error=e.getMessage(); }
    }

    private void crearUsuario() throws IOException {
        Panel origen=panel;
        var creado=perfiles.crear(nombre,origen==Panel.USUARIOS&&importar,origen==Panel.USUARIOS);
        leerUsuarios();
        if(origen==Panel.PUESTOS){preparandoPanel();elegirPuesto(creado);}
        else activar(creado);
    }

    private Perfil buscarUsuario(String id) throws IOException {
        var uuid=UUID.fromString(id);
        return lista.stream().filter(p->p.id().equals(uuid)).findFirst().orElseThrow(()->new IOException("Usuario desconocido."));
    }

    private void eliminarUsuario() throws IOException {
        Perfil eliminado=usuarioAEliminar;
        perfiles.eliminar(eliminado);
        usuarioAEliminar=null;
        if(eliminado.equals(activo)){
            activo=null;
            nuevaMision();
            cargarCampana.accept(null);
        }
        lista=lista.stream().filter(u->!u.equals(eliminado)).toList();
        panel=lista.isEmpty()?null:Panel.USUARIOS;
        preparandoPanel();
        if(panel==null)alElegirUsuario=null;
    }

    private void seleccionar(Perfil perfil) throws IOException {
        switch(panel) {
            case PUESTOS -> elegirPuesto(perfil);
            case PUNTAJES -> consultar(perfil);
            case USUARIOS -> {perfiles.seleccionar(perfil);activar(perfil);}
            default -> { }
        }
    }

    private void terminarCierre(String accion) {
        panel=null;
        var respuesta=accionCierre;
        accionCierre=null;
        respuesta.accept(accion);
    }

    private void volver() {
        if(panel==Panel.ELIMINAR_USUARIO){
            usuarioAEliminar=null;
            panel=Panel.USUARIOS;
            preparandoPanel();
        } else if(panel==Panel.CIERRE) {
            if(paginaDetalle>=0){paginaDetalle=-1;enfocar.run();}else terminarCierre("resultadoDesenlace");
        } else if(creando&&(activo!=null||!lista.isEmpty())) {
            preparandoPanel();
        } else {
            if(panel==Panel.PUESTOS){comenzar=null;nuevaMision();}
            panel=null;
            alElegirUsuario=null;
            enfocar.run();
        }
    }
}
