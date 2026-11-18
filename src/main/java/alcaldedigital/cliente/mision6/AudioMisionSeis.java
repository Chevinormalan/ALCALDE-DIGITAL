package alcaldedigital.cliente.mision6;
import alcaldedigital.cliente.presentacion.*;
import java.util.*;
import static alcaldedigital.cliente.mision6.MisionSeisMotor.*;
/** Observador de presentación: nunca modifica la simulación. */
public final class AudioMisionSeis implements AutoCloseable {
 private final AudioEscenaSalida salida;private boolean listo;private Fase fase;private float tiempo,ataque,impacto;
 private final float[] armas=new float[2],misiles=new float[2],x=new float[2],y=new float[2],impulsos=new float[2];private final int[] vidas=new int[2];
 private float global,pasos;private final Set<Proyectil> especiales=Collections.newSetFromMap(new IdentityHashMap<>());
 public AudioMisionSeis(){this(new AudioEscena("M6"));}public AudioMisionSeis(AudioEscenaSalida s){salida=s;}
 private void fx(String id,float vol,double intervalo){salida.efecto(id,"presentacion/audio/"+id,vol,intervalo);}
 public void observar(MisionSeisMotor m){
  boolean vuelo=switch(m.fase){case DESPEGUE,AVISO,ENTRENAMIENTO,COMBATE,RETIRADA,LLEGADA->true;default->false;};
  float aterrizaje=m.fase==Fase.LLEGADA?Math.min(1,m.tiempo/5.2f):0;
  float movimiento=m.fase==Fase.DESPEGUE?.8f:.15f,dt=m.global-global;
  if(listo&&dt>0&&(m.fase==Fase.COMBATE||m.fase==Fase.ENTRENAMIENTO)){movimiento=0;for(int i=0;i<(m.modo==Modo.SOLO?1:2);i++)movimiento=Math.max(movimiento,Math.min(1,(float)Math.hypot(m.naves[i].x-x[i],m.naves[i].y-y[i])/dt/350));}
  salida.bucle("hover","presentacion/audio/helicoptero_hover",vuelo?.17f*(1-movimiento)*(1-aterrizaje):0);
  salida.bucle("rotor","presentacion/audio/helicoptero_vuelo",vuelo?.19f*movimiento*(1-aterrizaje):0);
  salida.bucle("descenso","presentacion/audio/helicoptero_descenso",m.fase==Fase.LLEGADA?.15f*(float)Math.sin(aterrizaje*Math.PI):0);
  boolean plaza=m.fase==Fase.DISCURSO||m.fase==Fase.BUSCAR||m.fase==Fase.ENCUENTRO||m.fase==Fase.CAMINAR;
  salida.bucle("ambiente","presentacion/audio/ambiente",plaza?.055f:0);
  if(listo){for(int i=0;i<(m.modo==Modo.SOLO?1:2);i++){
    if(m.naves[i].arma>armas[i]+.08f)fx("disparo_jugador",.42f,.065);
    if(m.naves[i].misil>misiles[i]+1)fx("cohete",.43f,.12);
    if(m.naves[i].recargaImpulso>impulsos[i]+1)salida.efecto("impulso","mision3/audio/dash",.24f,.12);
    if(m.naves[i].vida<vidas[i])fx("dano",.24f,.16);
    if(m.naves[i].vida>vidas[i]&&m.fase==Fase.COMBATE)salida.efecto("curacion","mision3/audio/ui",.18f,.15);
   }
   if(m.fase==Fase.COMBATE&&fase==Fase.COMBATE&&m.ataqueJefe>ataque+.3f)fx("disparo_dron",.34f,.12);
   boolean coheteImpacto=false;for(Proyectil p:especiales)if(!m.proyectiles.contains(p)&&p.edad<8&&Math.hypot(p.x-m.jefeX,p.y-m.jefeY)<80&&(m.fase==Fase.COMBATE&&fase==Fase.COMBATE||m.fase==Fase.RETIRADA&&p.x==m.impactoX&&p.y==m.impactoY))coheteImpacto=true;
   if(coheteImpacto||m.impacto>impacto+.025f)fx(coheteImpacto||m.impacto>.2f?"explosion_cohete":"impacto",coheteImpacto||m.impacto>.2f?.43f:.24f,.085);
   if(m.fase==Fase.RETIRADA&&fase!=Fase.RETIRADA)fx("radio",.18f,.5);
   if(m.fase==Fase.DESPEGUE&&fase!=Fase.DESPEGUE)fx("encendido_helicoptero",.20f,.5);
   if(m.fase==Fase.RETIRADA&&m.tiempo>=.5f&&(fase!=Fase.RETIRADA||tiempo<.5f))fx("destruccion_dron",.51f,1);
   if((plaza||m.fase==Fase.EXPLORAR_SALA)&&(int)(m.pasos/2.6f)>(int)(pasos/2.6f))fx(m.fase==Fase.EXPLORAR_SALA?"paso_madera":"paso_adoquin",.17f,.16);
  }
  especiales.clear();for(Proyectil p:m.proyectiles)if(p.especial&&!p.enemigo)especiales.add(p);
  for(int i=0;i<2;i++){armas[i]=m.naves[i].arma;misiles[i]=m.naves[i].misil;vidas[i]=m.naves[i].vida;x[i]=m.naves[i].x;y[i]=m.naves[i].y;impulsos[i]=m.naves[i].recargaImpulso;}
  global=m.global;pasos=m.pasos;
  fase=m.fase;tiempo=m.tiempo;ataque=m.ataqueJefe;impacto=m.impacto;listo=true;
 }
 public void confirmar(){salida.efecto("confirmar","mision3/audio/ui",.11f,.12);}
 public void pausar(){salida.pausar();}public void restablecer(){listo=false;especiales.clear();}
 @Override public void close(){salida.close();}
}
