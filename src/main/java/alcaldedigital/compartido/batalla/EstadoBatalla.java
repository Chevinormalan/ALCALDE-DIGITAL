package alcaldedigital.compartido.batalla;

import java.io.*;
import java.util.*;

/** Datos de red de misión 5. Ninguna clase gráfica atraviesa el protocolo. */
public final class EstadoBatalla {
    private EstadoBatalla(){}
    public static final int VERSION=2, MISION=5, PUERTO=48505;
    public enum Dificultad {
        FACIL(.75f,1.25f,1.35f,1.3f), NORMAL(1,1,1,1), DIFICIL(1.25f,.85f,.8f,.8f);
        public final float velocidad,cadencia,aviso,apertura;
        Dificultad(float v,float c,float a,float h){velocidad=v;cadencia=c;aviso=a;apertura=h;}
        public String nombre(){return this==FACIL?"Fácil":this==DIFICIL?"Difícil":"Normal";}
    }
    public enum Fase { ESPERA, INTRO, OFICINA, ABSORCION, AVISO, ESQUIVA, TURNO, DECISION, TRAICION, EPILOGO, FINAL }
    public record Jugador(String nombre,float x,float y,float vx,float vy,int vidas,int puntos,int invulnerable,int recuperaciones,int orientacion,boolean movido,boolean listo,boolean conectado,boolean accionLista){}
    public record Obstaculo(float x,float y,float w,float h,float vx,float vy,int tipo,int edad,int aviso,int duracion,int objetivo){}
    public record Vista(String id,long semilla,long tick,Fase fase,int ronda,int patron,int reloj,int jefe,int linea,int[] golpes,int[] elecciones,Jugador[] jugadores,List<Obstaculo> obstaculos,boolean pausa,boolean gravedad,int gravedadDir,boolean solo,boolean gano,int verificados,int rumores,int[] ciudad,int[] base,String mensaje,String[] historial,String[] publicaciones,Dificultad dificultad){
        public Vista(String id,long semilla,long tick,Fase fase,int ronda,int patron,int reloj,int jefe,int linea,int[] golpes,int[] elecciones,Jugador[] jugadores,List<Obstaculo> obstaculos,boolean pausa,boolean gravedad,int gravedadDir,boolean solo,boolean gano,int verificados,int rumores,int[] ciudad,int[] base,String mensaje,String[] historial,String[] publicaciones){this(id,semilla,tick,fase,ronda,patron,reloj,jefe,linea,golpes,elecciones,jugadores,obstaculos,pausa,gravedad,gravedadDir,solo,gano,verificados,rumores,ciudad,base,mensaje,historial,publicaciones,Dificultad.NORMAL);}
    }
    public record Orden(long secuencia,int rol,float dx,float dy,boolean salto,String accion,int valor){}
    public static void escribirOrden(DataOutput o,Orden a)throws IOException{o.writeLong(a.secuencia());o.writeInt(a.rol());o.writeFloat(a.dx());o.writeFloat(a.dy());o.writeBoolean(a.salto());o.writeUTF(a.accion());o.writeInt(a.valor());}
    public static Orden leerOrden(DataInput in)throws IOException{return new Orden(in.readLong(),in.readInt(),in.readFloat(),in.readFloat(),in.readBoolean(),in.readUTF(),in.readInt());}
    public static void escribir(DataOutput o,Vista v)throws IOException{
        o.writeUTF(v.id());o.writeLong(v.semilla());o.writeLong(v.tick());o.writeInt(v.fase().ordinal());o.writeInt(v.ronda());o.writeInt(v.patron());o.writeInt(v.reloj());o.writeInt(v.jefe());o.writeInt(v.linea());
        for(int n:v.golpes())o.writeInt(n);for(int n:v.elecciones())o.writeInt(n);
        for(Jugador j:v.jugadores()){o.writeUTF(j.nombre());o.writeFloat(j.x());o.writeFloat(j.y());o.writeFloat(j.vx());o.writeFloat(j.vy());o.writeInt(j.vidas());o.writeInt(j.puntos());o.writeInt(j.invulnerable());o.writeInt(j.recuperaciones());o.writeInt(j.orientacion());o.writeBoolean(j.movido());o.writeBoolean(j.listo());o.writeBoolean(j.conectado());o.writeBoolean(j.accionLista());}
        o.writeInt(v.obstaculos().size());for(var a:v.obstaculos()){o.writeFloat(a.x());o.writeFloat(a.y());o.writeFloat(a.w());o.writeFloat(a.h());o.writeFloat(a.vx());o.writeFloat(a.vy());o.writeInt(a.tipo());o.writeInt(a.edad());o.writeInt(a.aviso());o.writeInt(a.duracion());o.writeInt(a.objetivo());}
        o.writeBoolean(v.pausa());o.writeBoolean(v.gravedad());o.writeInt(v.gravedadDir());o.writeBoolean(v.solo());o.writeBoolean(v.gano());o.writeInt(v.verificados());o.writeInt(v.rumores());for(int n:v.ciudad())o.writeInt(n);for(int n:v.base())o.writeInt(n);o.writeUTF(v.mensaje());o.writeInt(v.historial().length);for(String s:v.historial())o.writeUTF(s);o.writeInt(v.publicaciones().length);for(String s:v.publicaciones())o.writeUTF(s);o.writeInt(v.dificultad().ordinal());
    }
    public static Vista leer(DataInput in)throws IOException{return leer(in,VERSION);}
    public static Vista leer(DataInput in,int version)throws IOException{
        String id=in.readUTF();long semilla=in.readLong(),tick=in.readLong();int f=in.readInt();if(f<0||f>=Fase.values().length)throw new IOException("Fase inválida");
        int ronda=in.readInt(),patron=in.readInt(),reloj=in.readInt(),jefe=in.readInt(),linea=in.readInt();int[] golpes={in.readInt(),in.readInt()},elecciones={in.readInt(),in.readInt()};Jugador[] js=new Jugador[2];
        for(int i=0;i<2;i++)js[i]=new Jugador(in.readUTF(),in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readBoolean(),in.readBoolean(),in.readBoolean(),in.readBoolean());
        int n=in.readInt();if(n<0||n>1200)throw new IOException("Exceso de obstáculos");List<Obstaculo> obs=new ArrayList<>();for(int i=0;i<n;i++)obs.add(new Obstaculo(in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readFloat(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt()));
        boolean pausa=in.readBoolean(),gravedad=in.readBoolean();int gd=in.readInt();boolean solo=in.readBoolean(),gano=in.readBoolean();int ver=in.readInt(),rum=in.readInt();int[] ciudad=new int[7];for(int i=0;i<7;i++)ciudad[i]=in.readInt();int[] base=new int[7];for(int i=0;i<7;i++)base[i]=in.readInt();String mensaje=in.readUTF();int h=in.readInt();if(h<0||h>512)throw new IOException("Historial inválido");String[] hist=new String[h];for(int i=0;i<h;i++)hist[i]=in.readUTF();
        int np=in.readInt();if(np<0||np>100)throw new IOException("Índice de publicaciones inválido");String[] pubs=new String[np];for(int i=0;i<np;i++)pubs[i]=in.readUTF();
        int nivel=version>=2?in.readInt():Dificultad.NORMAL.ordinal();if(nivel<0||nivel>=Dificultad.values().length)throw new IOException("Dificultad inválida");
        return new Vista(id,semilla,tick,Fase.values()[f],ronda,patron,reloj,jefe,linea,golpes,elecciones,js,List.copyOf(obs),pausa,gravedad,gd,solo,gano,ver,rum,ciudad,base,mensaje,hist,pubs,Dificultad.values()[nivel]);
    }
}
