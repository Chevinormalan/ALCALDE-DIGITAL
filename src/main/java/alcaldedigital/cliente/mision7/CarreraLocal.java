package alcaldedigital.cliente.mision7;
import alcaldedigital.compartido.carrera.EstadoCarrera.*;
import alcaldedigital.servidor.carrera.*;

public final class CarreraLocal implements CanalCarrera {
    private final MotorCarrera motor;private final GuardadoCarrera guardado;private final RepositorioCarrera repositorio;
    private float resto;private int pasos;private String error="";private boolean corrupcionGuardada;
    public CarreraLocal(MotorCarrera m,GuardadoCarrera g,RepositorioCarrera r){motor=m;guardado=g;repositorio=r;for(int i=0;i<2;i++)m.conectar(i,true);}
    public Vista vista(){return motor.vista();}
    public void movimiento(int r,float y,boolean correr){motor.orden(r,y,correr,"",0);}
    public void accion(int r,String accion,int valor){if(accion.equals("GUARDAR"))guardar();else{var antes=motor.vista();motor.orden(r,0,false,accion,valor);var despues=motor.vista();if(antes.fase()!=despues.fase()||antes.historial().size()!=despues.historial().size()||antes.pausa()!=despues.pausa())guardar();}persistirCorrupcion();}
    public void actualizar(float dt){if(motor.vista().pausa()){resto=0;return;}resto+=Math.max(0,Math.min(.1f,dt));while(resto>=1f/60){resto-=1f/60;Vista antes=motor.vista();motor.paso();Vista despues=motor.vista();
        if(++pasos%600==0||antes.fase()!=despues.fase()||antes.historial().size()!=despues.historial().size())guardar();persistirCorrupcion();}}
    private void persistirCorrupcion(){if(!corrupcionGuardada&&repositorio!=null&&motor.vista().electoral().juanCorrupto())try{repositorio.registrarCorrupcion(motor.vista().electoral());corrupcionGuardada=true;}catch(java.io.IOException e){error=e.getMessage();}}
    private void guardar(){if(guardado!=null)try{guardado.guardar(motor);}catch(java.io.IOException e){error=e.getMessage();}}
    public String error(){return error;}public void close(){guardar();persistirCorrupcion();}
}
