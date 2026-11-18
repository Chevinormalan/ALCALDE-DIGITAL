package alcaldedigital.cliente.mision5;

import alcaldedigital.servidor.batalla.*;
import alcaldedigital.compartido.batalla.EstadoBatalla.Vista;

public final class CanalLocal implements CanalBatalla {
    private final MotorBatalla motor;private final GuardadoBatalla guardado;private float resto;private int pasos;private String error="";
    public CanalLocal(MotorBatalla m,GuardadoBatalla g){motor=m;guardado=g;motor.conectar(0,"Ciudadano",true);motor.conectar(1,"Periodista",true);}
    public MotorBatalla motor(){return motor;}
    public Vista vista(){return motor.vista();}
    public void movimiento(int rol,float x,float y,boolean salto){motor.orden(rol,x,y,salto,"",0);}
    public void accion(int rol,String a,int v){if(a.equals("GUARDAR"))guardar();else motor.orden(rol,0,0,false,a,v);}
    public void actualizar(float dt){if(motor.vista().pausa()){resto=0;return;}resto+=Math.min(.1f,Math.max(0,dt));while(resto>=1f/60){resto-=1f/60;var fase=motor.vista().fase();motor.paso();if(++pasos%600==0||fase!=motor.vista().fase())guardar();}}
    private void guardar(){if(guardado!=null)try{guardado.guardar(motor);error="";}catch(Exception e){error="No se pudo guardar: "+e.getMessage();}}
    public String error(){return error;}
    public void close(){guardar();}
}
