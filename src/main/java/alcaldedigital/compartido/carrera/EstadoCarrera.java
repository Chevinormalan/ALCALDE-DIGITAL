package alcaldedigital.compartido.carrera;

import java.io.*;
import java.util.*;
import alcaldedigital.compartido.modelo.Publicacion;

/** Protocolo propio de misión 7, sin deserialización de objetos Java. */
public final class EstadoCarrera {
    private EstadoCarrera(){}
    public static final int VERSION=1,PUERTO=48507,MAGIA=0x43495637;
    public enum Fase { ESPERA,ANUNCIO,RIVALIDAD,SALIDA,ENCUENTRO,CUENTA,CARRERA,LLEGADA,EPILOGO,RESULTADO }
    public enum Dificultad { FACIL("Fácil",20,.25f),NORMAL("Normal",15,.13f),DIFICIL("Difícil",12,.04f);
        public final String nombre;public final int ritmoCPU;public final float errorCPU;
        Dificultad(String nombre,int ritmo,float error){this.nombre=nombre;ritmoCPU=ritmo;errorCPU=error;}}
    public enum Noticia { ATAJO,RUMOR,PACTO,RESULTADO }
    public record Evento(int indice,Noticia tipo,int restante,boolean advertencia,int candidato,boolean verdadera){}
    public record Corredor(float distancia,float y,float impulso,float velocidad,int puntos,int reputacion,
        int escudo,int frenado,int turbo,int habilidad,int avisoAtaque,float objetivoAtaque,long ultimoPulso,
        int pulsos,int siguienteEvento,boolean conectado,boolean listo,boolean confirma,boolean asistido,Evento evento){}
    public record Objeto(int id,float x,float y,int tipo,int consumido,int grupo){}
    public record Onda(int nodo,int estado,int demora){}
    public record Vista(String id,long semilla,long tick,Fase fase,int reloj,int linea,Dificultad dificultad,
        int humanos,boolean pausa,int ganador,Corredor[] corredores,List<Objeto> objetos,int[] grupos,
        EstadoElectoral electoral,int[] ciudad,int[] base,List<String> historial,List<Publicacion> publicaciones,List<Onda> ondas,String mensaje){
        public boolean humano(int rol){return rol>=0&&rol<2&&(humanos&(1<<rol))!=0;}
    }
    public record Orden(long secuencia,int rol,float vertical,boolean correr,String accion,int valor){}
    public static void orden(DataOutput o,Orden a)throws IOException{o.writeLong(a.secuencia);o.writeInt(a.rol);o.writeFloat(a.vertical);o.writeBoolean(a.correr);o.writeUTF(a.accion);o.writeInt(a.valor);}
    public static Orden orden(DataInput i)throws IOException{return new Orden(i.readLong(),i.readInt(),i.readFloat(),i.readBoolean(),i.readUTF(),i.readInt());}
    public static void escribir(DataOutput o,Vista v)throws IOException{
        o.writeUTF(v.id);o.writeLong(v.semilla);o.writeLong(v.tick);o.writeInt(v.fase.ordinal());o.writeInt(v.reloj);o.writeInt(v.linea);o.writeInt(v.dificultad.ordinal());
        o.writeInt(v.humanos);o.writeBoolean(v.pausa);o.writeInt(v.ganador);
        for(var c:v.corredores){o.writeFloat(c.distancia);o.writeFloat(c.y);o.writeFloat(c.impulso);o.writeFloat(c.velocidad);o.writeInt(c.puntos);o.writeInt(c.reputacion);o.writeInt(c.escudo);o.writeInt(c.frenado);o.writeInt(c.turbo);o.writeInt(c.habilidad);o.writeInt(c.avisoAtaque);o.writeFloat(c.objetivoAtaque);o.writeLong(c.ultimoPulso);o.writeInt(c.pulsos);o.writeInt(c.siguienteEvento);o.writeBoolean(c.conectado);o.writeBoolean(c.listo);o.writeBoolean(c.confirma);o.writeBoolean(c.asistido);o.writeBoolean(c.evento!=null);
            if(c.evento!=null){var e=c.evento;o.writeInt(e.indice);o.writeInt(e.tipo.ordinal());o.writeInt(e.restante);o.writeBoolean(e.advertencia);o.writeInt(e.candidato);o.writeBoolean(e.verdadera);}}
        o.writeInt(v.objetos.size());for(var a:v.objetos){o.writeInt(a.id);o.writeFloat(a.x);o.writeFloat(a.y);o.writeInt(a.tipo);o.writeInt(a.consumido);o.writeInt(a.grupo);}
        enteros(o,v.grupos);electoral(o,v.electoral);enteros(o,v.ciudad);enteros(o,v.base);textos(o,v.historial);
        o.writeInt(v.publicaciones.size());for(var a:v.publicaciones){o.writeLong(a.id());o.writeInt(a.riesgo());o.writeUTF(a.autor());o.writeUTF(a.texto());o.writeUTF(a.estado());}
        o.writeInt(v.ondas.size());for(var a:v.ondas){o.writeInt(a.nodo);o.writeInt(a.estado);o.writeInt(a.demora);}o.writeUTF(v.mensaje);
    }
    public static Vista leer(DataInput i)throws IOException{
        String id=i.readUTF();long semilla=i.readLong(),tick=i.readLong();Fase fase=valor(Fase.values(),i.readInt());int reloj=i.readInt(),linea=i.readInt();Dificultad d=valor(Dificultad.values(),i.readInt());
        int humanos=i.readInt();if(humanos<1||humanos>3)throw new IOException("Roles inválidos");boolean pausa=i.readBoolean();int ganador=i.readInt();Corredor[] c=new Corredor[2];
        for(int r=0;r<2;r++){float x=i.readFloat(),y=i.readFloat(),imp=i.readFloat(),vel=i.readFloat();int p=i.readInt(),rep=i.readInt(),esc=i.readInt(),f=i.readInt(),tur=i.readInt(),hab=i.readInt(),av=i.readInt();float obj=i.readFloat();long pul=i.readLong();int ps=i.readInt(),sig=i.readInt();boolean con=i.readBoolean(),li=i.readBoolean(),cf=i.readBoolean(),as=i.readBoolean();Evento e=i.readBoolean()?new Evento(i.readInt(),valor(Noticia.values(),i.readInt()),i.readInt(),i.readBoolean(),i.readInt(),i.readBoolean()):null;
            if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(imp)||!Float.isFinite(vel))throw new IOException("Posición inválida");
            c[r]=new Corredor(x,y,imp,vel,p,rep,esc,f,tur,hab,av,obj,pul,ps,sig,con,li,cf,as,e);}
        int n=tamano(i,200);List<Objeto> os=new ArrayList<>();for(int k=0;k<n;k++)os.add(new Objeto(i.readInt(),i.readFloat(),i.readFloat(),i.readInt(),i.readInt(),i.readInt()));
        int[] grupos=enteros(i,7);EstadoElectoral e=electoral(i);int[] ciudad=enteros(i,7),base=enteros(i,7);List<String> hist=textos(i,300);List<Publicacion> pub=new ArrayList<>();
        n=tamano(i,16);for(int k=0;k<n;k++)pub.add(new Publicacion(i.readLong(),i.readInt(),i.readUTF(),i.readUTF(),i.readUTF()));
        List<Onda> ondas=new ArrayList<>();n=tamano(i,120);for(int k=0;k<n;k++)ondas.add(new Onda(i.readInt(),i.readInt(),i.readInt()));String msg=i.readUTF();
        return new Vista(id,semilla,tick,fase,reloj,linea,d,humanos,pausa,ganador,c,List.copyOf(os),grupos,e,ciudad,base,hist,List.copyOf(pub),List.copyOf(ondas),msg);
    }
    private static <T>T valor(T[] a,int n)throws IOException{if(n<0||n>=a.length)throw new IOException("Enumeración inválida");return a[n];}
    public static void electoral(DataOutput o,EstadoElectoral e)throws IOException{o.writeBoolean(e.juanCorrupto());enteros(o,e.apoyo());enteros(o,e.confianza());enteros(o,e.verificadas());enteros(o,e.conflictos());enteros(o,e.participacion());textos(o,e.historial());}
    public static EstadoElectoral electoral(DataInput i)throws IOException{return new EstadoElectoral(i.readBoolean(),enteros(i,4),enteros(i,4),enteros(i,4),enteros(i,4),enteros(i,4),textos(i,120));}
    private static void enteros(DataOutput o,int[] a)throws IOException{for(int v:a)o.writeInt(v);}
    private static int[] enteros(DataInput i,int n)throws IOException{int[] a=new int[n];for(int k=0;k<n;k++)a[k]=i.readInt();return a;}
    private static void textos(DataOutput o,List<String> a)throws IOException{o.writeInt(a.size());for(String s:a)o.writeUTF(s);}
    private static List<String> textos(DataInput i,int max)throws IOException{int n=tamano(i,max);List<String> a=new ArrayList<>();for(int k=0;k<n;k++)a.add(i.readUTF());return List.copyOf(a);}
    private static int tamano(DataInput i,int max)throws IOException{int n=i.readInt();if(n<0||n>max)throw new IOException("Lista fuera de límite");return n;}
}
