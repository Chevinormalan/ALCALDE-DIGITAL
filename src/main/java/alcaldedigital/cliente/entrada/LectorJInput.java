package alcaldedigital.cliente.entrada;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Locale;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import static alcaldedigital.cliente.entrada.EstadoMando.Boton;

/** Lectura física; la lógica del juego solo recibe EstadoMando. */
public final class LectorJInput {
    private Controller mando;
    private boolean disponible=true;
    private boolean nativosPreparados;
    private long siguienteBusqueda;

    public EstadoMando leer() {
        if(!disponible) return EstadoMando.desconectado();
        try {
            if(!nativosPreparados) {
                prepararNativos();
                nativosPreparados=true;
            }
            if(mando==null) {
                if(System.nanoTime()<siguienteBusqueda) return EstadoMando.desconectado();
                siguienteBusqueda=System.nanoTime()+2_000_000_000L;
                for(Controller candidato:ControllerEnvironment.getDefaultEnvironment().getControllers()) {
                    if(candidato.getType()==Controller.Type.GAMEPAD||candidato.getType()==Controller.Type.STICK) {
                        mando=candidato;break;
                    }
                }
                if(mando==null) return EstadoMando.desconectado();
            }
            if(!mando.poll()) {mando=null;return EstadoMando.desconectado();}
            return convertir(mando);
        } catch(IOException|LinkageError|RuntimeException error) {
            disponible=false;
            System.err.println("Mando no disponible: "+error.getMessage());
            return EstadoMando.desconectado();
        }
    }

    /** Al ejecutar desde Maven/IDE, los nativos vienen dentro del JAR clasificado. */
    private static void prepararNativos() throws IOException {
        String configurada=System.getProperty("net.java.games.input.librarypath");
        if(configurada!=null&&!configurada.isBlank()) return;
        String[] archivos={"libjinput-linux64.so","jinput-raw_64.dll","jinput-dx8_64.dll",
            "jinput-wintab.dll","libjinput-osx.jnilib"};
        Path carpeta=null;
        for(String archivo:archivos) {
            try(var origen=LectorJInput.class.getResourceAsStream("/"+archivo)) {
                if(origen==null) continue;
                if(carpeta==null) {
                    carpeta=Files.createTempDirectory("alcalde-mando-");
                    carpeta.toFile().deleteOnExit();
                }
                Path destino=carpeta.resolve(archivo);
                Files.copy(origen,destino);
                destino.toFile().deleteOnExit();
            }
        }
        if(carpeta!=null) System.setProperty("net.java.games.input.librarypath",carpeta.toString());
    }

    static EstadoMando convertir(Controller mando) {
        String nombre=mando.getName();
        boolean sony=nombre.toLowerCase(Locale.ROOT)
            .matches(".*(dualshock|dualsense|wireless controller|sony|playstation).*");
        EnumSet<Boton> botones=EnumSet.noneOf(Boton.class);
        float lx=0,ly=0,rx=0,ry=0,z=0,rz=0;
        for(Component componente:mando.getComponents()) {
            var id=componente.getIdentifier();float valor=componente.getPollData();
            if(id==Component.Identifier.Axis.X) lx=valor;
            else if(id==Component.Identifier.Axis.Y) ly=valor;
            else if(id==Component.Identifier.Axis.RX) rx=valor;
            else if(id==Component.Identifier.Axis.RY) ry=valor;
            else if(id==Component.Identifier.Axis.Z) z=valor;
            else if(id==Component.Identifier.Axis.RZ) rz=valor;
            else if(id==Component.Identifier.Axis.POV) {
                if(valor==Component.POV.UP||valor==Component.POV.UP_LEFT||valor==Component.POV.UP_RIGHT) botones.add(Boton.ARRIBA);
                if(valor==Component.POV.DOWN||valor==Component.POV.DOWN_LEFT||valor==Component.POV.DOWN_RIGHT) botones.add(Boton.ABAJO);
                if(valor==Component.POV.LEFT||valor==Component.POV.UP_LEFT||valor==Component.POV.DOWN_LEFT) botones.add(Boton.IZQUIERDA);
                if(valor==Component.POV.RIGHT||valor==Component.POV.UP_RIGHT||valor==Component.POV.DOWN_RIGHT) botones.add(Boton.DERECHA);
            } else if(id instanceof Component.Identifier.Button boton&&valor>.5f) {
                Boton accion=traducirBoton(boton,sony);
                if(accion!=null) botones.add(accion);
            }
        }
        // Los disparadores pueden aparecer como botones o ejes independientes.
        if(rz>.55f) botones.add(Boton.RT);
        if(z>.55f) botones.add(Boton.LT);
        return new EstadoMando(nombre,zonaMuerta(lx),zonaMuerta(ly),zonaMuerta(rx),zonaMuerta(ry),botones);
    }

    private static float zonaMuerta(float valor) {
        float modulo=Math.abs(valor);
        return modulo<.20f?0:Math.copySign(Math.min(1,(modulo-.20f)/.80f),valor);
    }

    private static Boton traducirBoton(Component.Identifier.Button id,boolean sony) {
        if(id==Component.Identifier.Button.A) return Boton.A;
        if(id==Component.Identifier.Button.B) return Boton.B;
        if(id==Component.Identifier.Button.X) return Boton.X;
        if(id==Component.Identifier.Button.Y) return Boton.Y;
        if(id==Component.Identifier.Button.START) return Boton.MENU;
        if(id==Component.Identifier.Button.SELECT||id==Component.Identifier.Button.BACK) return Boton.VISTA;
        String nombre=id.getName();
        int indice;
        try {indice=Integer.parseInt(nombre);} catch(NumberFormatException error) {return null;}
        return switch(indice) {
            case 0 -> sony?Boton.X:Boton.A;
            case 1 -> sony?Boton.A:Boton.B;
            case 2 -> sony?Boton.B:Boton.X;
            case 3 -> Boton.Y;
            case 4 -> Boton.LB;
            case 5 -> Boton.RB;
            case 6 -> sony?Boton.LT:Boton.VISTA;
            case 7 -> sony?Boton.RT:Boton.MENU;
            case 8 -> sony?Boton.VISTA:null;
            case 9 -> sony?Boton.MENU:null;
            default -> null;
        };
    }
}
