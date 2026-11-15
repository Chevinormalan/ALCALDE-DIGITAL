package alcaldedigital.compartido.guardado;
import java.lang.reflect.*;import java.util.*;
/** Codec de escalares de clases conocidas del juego; nunca instancia clases indicadas por el archivo. */
public final class CamposEstado {
 private CamposEstado(){}
 public static void escribir(Object objeto,Properties p,String prefijo){
  for(Class<?> c=objeto.getClass();c!=Object.class;c=c.getSuperclass())for(Field f:c.getDeclaredFields()){
   if(Modifier.isStatic(f.getModifiers())||Modifier.isFinal(f.getModifiers())||!escalar(f.getType()))continue;
   try{f.setAccessible(true);p.setProperty(prefijo+f.getName(),String.valueOf(f.get(objeto)));}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
  }
 }
 private static boolean escalar(Class<?> c){return c==int.class||c==long.class||c==float.class||c==double.class||c==boolean.class||c==String.class||c.isEnum();}
 @SuppressWarnings({"rawtypes","unchecked"})
 public static void leer(Object objeto,Properties p,String prefijo){
  for(Class<?> c=objeto.getClass();c!=Object.class;c=c.getSuperclass())for(Field f:c.getDeclaredFields()){
   if(Modifier.isStatic(f.getModifiers())||Modifier.isFinal(f.getModifiers())||!escalar(f.getType()))continue;
   String v=p.getProperty(prefijo+f.getName());if(v==null)continue;Class<?> t=f.getType();Object n;
   if(t==int.class)n=Integer.valueOf(v);else if(t==long.class)n=Long.valueOf(v);
   else if(t==float.class){float a=Float.parseFloat(v);if(!Float.isFinite(a))throw new IllegalArgumentException(f.getName());n=a;}
   else if(t==double.class){double a=Double.parseDouble(v);if(!Double.isFinite(a))throw new IllegalArgumentException(f.getName());n=a;}
   else if(t==boolean.class){if(!v.equals("true")&&!v.equals("false"))throw new IllegalArgumentException(f.getName());n=Boolean.valueOf(v);}
   else if(t.isEnum())n=Enum.valueOf((Class)t,v);else n=v;
   try{f.setAccessible(true);f.set(objeto,n);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
  }
 }
}
