package alcaldedigital.cliente.interfaz;

import alcaldedigital.cliente.entrada.MapaControles;
import alcaldedigital.cliente.misiones.UsuariosPuntajes;
import alcaldedigital.cliente.misiones.PuntajesMision;
import alcaldedigital.cliente.misiones.UsuariosPuntajes.Panel;
import alcaldedigital.cliente.recursos.PaletaMorada;
import alcaldedigital.compartido.guardado.TablaPuntajes.Fila;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

final class RenderPuntajes {
    private static final int DECISIONES_POR_PAGINA=5;
    private final RenderJuego ui;
    private final DecimalFormat puntos=new DecimalFormat("0.##",DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es")));

    RenderPuntajes(RenderJuego ui) { this.ui=ui; }

    void dibujar(UsuariosPuntajes usuarios) {
        switch(usuarios.panel()) {
            case USUARIOS, PUESTOS -> usuarios(usuarios);
            case ELIMINAR_USUARIO -> confirmarEliminacion(usuarios);
            case PUNTAJES -> puntajes(usuarios);
            case CIERRE -> cierre(usuarios);
        }
        if(!usuarios.error().isBlank())ui.parrafo(usuarios.error(),34,683,1210,15,PaletaMorada.TEXTO_SUAVE);
    }

    private void usuarios(UsuariosPuntajes u) {
        boolean puestos=u.panel()==Panel.PUESTOS;
        String titulo=u.creando()?"Crea tu usuario":puestos?"Selecciona el usuario de J"+u.puesto():u.antesDeJugar()?"Elige tu usuario para jugar":"Usuarios";
        if(u.creando())ui.texto(titulo,96,116,40,ui.papel,true);
        else ui.cabecera(titulo,puestos?"Esta partida se registrará en los perfiles seleccionados.":"Cada usuario tiene su propia campaña y sus puntajes.");
        ui.panel(93,153,1094,492,PaletaMorada.PANEL);
        if(u.creando()) {
            ui.texto("Nombre de usuario",140,220,23,ui.oro,true);
            ui.panel(140,246,670,62,PaletaMorada.FONDO);
            ui.texto(u.nombre().isEmpty()?"Escribe tu nombre":u.nombre()+"_",162,286,25,u.nombre().isEmpty()?ui.tenue:ui.papel,false);
            ui.boton(u.antesDeJugar()?"Crear y jugar":"Crear usuario","usuarioConfirmar",140,347,270,true);
            ui.boton("Volver","volver",1060,65,160,false);
            if(!puestos&&u.importacionDisponible()) {
                ui.boton(u.importar()?"Guardar campaña anterior   Sí":"Guardar campaña anterior   No","usuarioImportar",140,420,460,false);
                ui.parrafo("Vincula el progreso y los puntajes anteriores a este usuario.",140,492,800,17,ui.tenue);
            }
            return;
        }
        int y=188;
        for(var jugador:u.paginaUsuarios()) {
            ui.boton(jugador.nombre(),"usuario:"+jugador.id(),140,y,puestos?600:478,jugador.equals(u.activo()));
            if(!puestos)ui.boton("Eliminar","eliminarUsuario:"+jugador.id(),636,y,124,false);
            y+=61;
        }
        ui.boton("Crear usuario","usuarioNuevo",782,188,345,true);
        navegacionUsuarios(u,782,252);
    }

    private void confirmarEliminacion(UsuariosPuntajes u) {
        ui.cabecera("Eliminar usuario",u.usuarioAEliminar().nombre());
        ui.panel(240,182,800,344,PaletaMorada.PANEL);
        ui.texto("¿Eliminar a "+u.usuarioAEliminar().nombre()+"?",285,256,25,ui.oro,true);
        ui.parrafo("El usuario dejará de aparecer en el juego. Sus partidas y puntajes quedarán en una carpeta de respaldo.",285,313,710,19,ui.papel);
        ui.boton("Cancelar","volver",285,428,280,true);
        ui.boton("Eliminar usuario","confirmarEliminarUsuario",711,428,280,false);
    }

    private void navegacionUsuarios(UsuariosPuntajes u,int x,int y) {
        if(u.hayAnterior())ui.boton("Usuarios anteriores","usuariosAnterior",x,y,280,false);
        if(u.haySiguiente())ui.boton("Más usuarios","usuariosSiguiente",x,y+50,280,false);
    }

    private void puntajes(UsuariosPuntajes u) {
        ui.cabecera("Puntajes",u.consultado().nombre());
        ui.panel(30,140,320,518,PaletaMorada.PANEL);
        ui.texto("USUARIOS",54,173,18,ui.oro,true);
        int y=190;
        for(var perfil:u.paginaUsuarios()) {
            ui.boton(perfil.nombre(),"usuario:"+perfil.id(),49,y,280,perfil.equals(u.consultado()));
            y+=55;
        }
        navegacionUsuarios(u,49,490);
        ui.panel(376,140,875,518,PaletaMorada.PANEL);
        var resumen=u.resumen();
        if(resumen==null)return;
        ui.texto(resumen.total()+" / 800",405,202,38,ui.oro,true);
        ui.texto(resumen.jugadas()+" de 8 misiones jugadas",665,196,19,ui.papel,false);
        ui.texto("MISIÓN",401,247,16,ui.oro,true);
        ui.texto("MEJOR",755,247,16,ui.oro,true);
        ui.texto("ÚLTIMO",852,247,16,ui.oro,true);
        ui.texto("RÉCORD EN",954,247,16,ui.oro,true);
        for(int n=1;n<=8;n++) {
            int base=265+(n-1)*46;
            ui.rect(395,base,838,42,n%2==0?PaletaMorada.PANEL:PaletaMorada.FONDO);
            var marca=resumen.misiones().get(n-1);
            ui.texto(n+" · "+MapaControles.nombreMision(n),405,base+27,16,ui.papel,true);
            ui.texto(nota(marca.mejor()),755,base+27,19,ui.oro,true);
            ui.texto(nota(marca.ultimo()),852,base+27,16,ui.papel,false);
            String contexto=marca.mejor()==null?"—":contexto(marca.mejor());
            ui.texto(contexto,954,base+27,14,ui.tenue,false);
            if(u.progresoConsultado().ganada(n))ui.texto("GANADA",1152,base+27,13,TemaPixel.CIAN,true);
            else if(!u.progresoConsultado().disponible(n))ui.texto("BLOQUEADA",1140,base+27,12,ui.tenue,false);
        }
    }

    private static String nota(Fila fila){return fila==null?"—":fila.puntuable()?Integer.toString(fila.puntaje()):"Crítico";}

    private static String contexto(Fila fila){
        String dificultad=switch(fila.dificultad()){
            case "FACIL" -> "Fácil";
            case "NORMAL" -> "Normal";
            case "DIFICIL" -> "Difícil";
            case "SOLO" -> "Individual";
            case "DUO" -> "Dúo";
            default -> fila.dificultad();
        };
        String modo=fila.modalidad();
        if(fila.mision()==6&&!modo.isBlank())return modo;
        if(modo.isBlank())return dificultad.isBlank()?"—":dificultad;
        return dificultad.isBlank()?modo:modo+" · "+dificultad;
    }

    private void cierre(UsuariosPuntajes u) {
        var resultado=u.cierre();
        var equipo=resultado.equipo();
        var marcas=resultado.marcas();
        var marca=marcas.isEmpty()?null:marcas.get(u.marcaElegida());
        Fila fila=marca!=null&&marca.fila()!=null?marca.fila():equipo;
        String detalle=(resultado.prueba()?"Final de prueba · ":"")+contexto(fila);
        ui.cabecera("Misión "+fila.mision()+" · "+MapaControles.nombreMision(fila.mision()),detalle);
        if(u.paginaDetalle()>=0) { decisiones(u,fila);return; }
        ui.panel(30,139,544,451,PaletaMorada.PANEL);
        ui.panel(599,139,651,451,PaletaMorada.PANEL);
        String estado=marca!=null&&marca.fila()==null?(equipo.victoria()?"JEFE · DERROTA":"JEFE · VICTORIA"):
            !fila.puntuable()?"FALLO CRÍTICO":fila.victoria()?"VICTORIA":fila.mision()==7&&fila.datos().stream().anyMatch(d->d.endsWith("Empate"))?"EMPATE":"DERROTA";
        ui.texto(estado,55,192,29,fila.victoria()?TemaPixel.CIAN:ui.oro,true);
        ui.texto(fila.puntaje()+" / 100",55,263,52,ui.oro,true);
        if(marca!=null&&marca.fila()==null) {
            ui.texto(marca.perfil().nombre()+" · Jefe",58,290,17,ui.tenue,false);
            ui.texto("Puntaje del equipo de héroes",58,315,15,ui.tenue,false);
        }
        else if(marca!=null)ui.texto(marca.perfil().nombre(),58,290,19,ui.papel,true);
        ui.parrafo(resultado.motivo(),58,marca!=null&&marca.fila()==null?350:329,479,18,ui.papel);
        if(marca!=null&&marca.acumulado()>=0) {
            ui.texto("Récord personal  "+(fila.puntuable()||marca.anterior()>=0?marca.mejor()+" / 100":"—"),58,410,19,ui.papel,true);
            ui.texto("Acumulado  "+marca.acumulado()+" / 800",58,446,24,ui.oro,true);
            if(fila.puntuable()&&(marca.anterior()<0||marca.mejora()>0))ui.texto("NUEVO RÉCORD  +"+marca.mejora(),58,480,16,TemaPixel.CIAN,true);
        }
        if(marcas.size()>1) {
            String siguiente=marcas.get((u.marcaElegida()+1)%marcas.size()).perfil().nombre();
            ui.boton("Ver puntaje de "+siguiente,"resultadoJugador",55,517,486,false);
        }
        ui.texto("DESGLOSE DEL INTENTO",625,182,20,ui.oro,true);
        int y=213;
        for(var componente:fila.desglose()){
            ui.texto(componente.etiqueta(),627,y,17,ui.papel,false);
            ui.texto(puntos.format(componente.puntos())+" / "+componente.maximo(),1105,y,17,ui.oro,true);
            ui.rect(627,y+10,574,5,PaletaMorada.VIOLETA);
            int largo=(int)(574*Math.clamp(componente.puntos()/componente.maximo(),0,1));
            ui.rect(627,y+10,largo,5,TemaPixel.CIAN);
            y+=46;
        }
        y+=8;
        for(String dato:fila.datos())y=ui.parrafo(dato,627,y,576,15,ui.papel);
        if(!fila.decisiones().isEmpty())ui.boton("Ver decisiones","resultadoDetalles",884,534,315,false);
        if(u.registroPendiente())ui.boton("Reintentar guardado","resultadoGuardar",884,610,315,false);
        boolean siguiente=equipo.victoria()&&equipo.mision()<8&&!resultado.prueba()&&ui.juego.progreso.disponible(equipo.mision()+1);
        ui.boton("Volver a misiones","resultadoSalir",30,610,288,!siguiente);
        if(siguiente)ui.boton("Siguiente misión","resultadoSiguienteMision",334,610,280,true);
        ui.boton("Reintentar","resultadoReintentar",siguiente?630:334,610,235,false);
    }

    private void decisiones(UsuariosPuntajes u,Fila fila) {
        ui.panel(30,139,1220,452,PaletaMorada.PANEL);
        ui.texto("Decisiones registradas",55,184,27,ui.oro,true);
        List<String> decisiones=fila.decisiones();
        int inicio=u.paginaDetalle()*DECISIONES_POR_PAGINA;
        for(int i=inicio;i<Math.min(decisiones.size(),inicio+DECISIONES_POR_PAGINA);i++) {
            String decision=decisiones.get(i);
            if(fila.mision()==7)decision=PuntajesMision.mostrarDecisionCarrera(decision);
            ui.parrafo(decision,58,232+(i-inicio)*64,1157,17,ui.papel);
        }
        ui.boton("Volver al resumen","resultadoResumen",30,610,310,true);
        if(inicio>0)ui.boton("Anterior","resultadoAnterior",610,610,225,false);
        if(inicio+DECISIONES_POR_PAGINA<decisiones.size())ui.boton("Siguiente","resultadoSiguiente",852,610,245,false);
    }
}
