package alcaldedigital.app;
import alcaldedigital.cliente.*;
import alcaldedigital.cliente.interfaz.*;
import alcaldedigital.compartido.modelo.*;
import alcaldedigital.servidor.sesion.SesionLocal;
import javax.imageio.ImageIO;
import java.nio.file.*;

/** Capturas deterministas desde el mismo render y controlador del juego. */
public final class EvidenciasApp {
    public static void main(String[] args) throws Exception {
        Path destino=Path.of(args.length>0?args[0]:"entregas/entrega1/evidencias");Files.createDirectories(destino);
        var juego=new JuegoCliente(SesionLocal::new);var render=new RenderJuego();
        captura(destino,"01-menu",juego,render);juego.accion("jugar");captura(destino,"02-roles",juego,render);
        juego.iniciar(Rol.CIUDADANO);captura(destino,"03-parque",juego,render);
        juego.accion("civitas");captura(destino,"04-civitas",juego,render);
        juego.accion("arboles");captura(destino,"12-radar-inicial",juego,render);
        juego.accion("camino");captura(destino,"13-camino-inicial",juego,render);
        juego.accion("laboratorio");juego.accion("radar");captura(destino,"05-avl-rotacion",juego,render);
        juego.accion("verificar");mover(juego,192,174);juego.accion("interactuar");
        captura(destino,"03b-parque-investigacion",juego,render);
        mover(juego,230,208);juego.accion("interactuar");juego.accion("evidencias");captura(destino,"06-evidencias",juego,render);
        juego.accion("volver");captura(destino,"14-aviso-fuentes",juego,render);
        juego.accion("arboles");juego.accion("jugador");captura(destino,"15-radar-verificado",juego,render);
        juego.accion("camino");captura(destino,"16-camino-desbloqueado",juego,render);
        juego.accion("laboratorio");captura(destino,"07-insercion-opciones",juego,render);
        juego.accion("contexto");
        juego.segundosAviso=0;captura(destino,"08-resultado",juego,render);
        juego.accion("arboles");captura(destino,"09-eliminacion-poda",juego,render);
        juego.accion("radar");captura(destino,"17-avl-correccion",juego,render);
        juego.pasoAVL=0;juego.accion("campoBusqueda");juego.tecla(49,'1');juego.accion("buscar");
        captura(destino,"18-busqueda-historica",juego,render);
        juego.accion("jugador");captura(destino,"19-radar-cierre",juego,render);
        juego.accion("ayuda");captura(destino,"10-ayuda",juego,render);
        juego.contraste=true;juego.accion("resultado");captura(destino,"11-alto-contraste",juego,render);
        juego.accion("arboles");juego.accion("camino");captura(destino,"20-camino-contraste",juego,render);
        transicion(destino);
        System.out.println("21 capturas de pantallas y 5 cuadros de transición reproducibles en "+destino);
    }
    static void transicion(Path destino) throws Exception {
        var j=new JuegoCliente(SesionLocal::new);j.iniciar(Rol.CIUDADANO);
        var r=new RenderJuego();var t=new TransicionPixel();
        t.presentar(r.dibujar(j),"parque",0,false,false);j.accion("ayuda");
        for(int i=0;i<5;i++) ImageIO.write(t.presentar(r.dibujar(j),"ayuda",i==0?0:.061,false,true),
            "png",destino.resolve("transicion-"+i+".png").toFile());
    }
    static void captura(Path p,String nombre,JuegoCliente j,RenderJuego r) throws Exception {ImageIO.write(r.dibujar(j),"png",p.resolve(nombre+".png").toFile());}
    static void mover(JuegoCliente j,int x,int y) {
        for(int i=0;i<1000&&Math.abs(j.sesion.vista().x()-x)>.5;i++) j.sesion.mover(Math.signum(x-j.sesion.vista().x())*2,0);
        for(int i=0;i<1000&&Math.abs(j.sesion.vista().y()-y)>.5;i++) j.sesion.mover(0,Math.signum(y-j.sesion.vista().y())*2);
        if(Math.abs(j.sesion.vista().x()-x)>2||Math.abs(j.sesion.vista().y()-y)>2) throw new IllegalStateException("Ruta bloqueada");
    }
}
