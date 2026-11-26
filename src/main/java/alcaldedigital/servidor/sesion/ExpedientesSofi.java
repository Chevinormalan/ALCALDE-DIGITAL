package alcaldedigital.servidor.sesion;

import alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;
import alcaldedigital.compartido.guardado.CamposEstado;
import java.util.ArrayList;
import java.util.Properties;
import static alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;

/** Estado local de los casos. Las búsquedas, lecturas y conclusiones sobreviven al guardado. */
final class ExpedientesSofi {
    private final int[] hallazgos=new int[CASOS.size()], documentos=new int[CASOS.size()];
    private final boolean[] clasificados=new boolean[CASOS.size()];
    int abierto, vecino=-1, orientados, ultimoHallazgo=-1;
    boolean lupa, contraste, calmaClasica, premium, publicacion;
    String respuesta="";
    Escena escena=Escena.NINGUNA;
    double segundosEscena;

    Vista vista() {
        var fichas=new ArrayList<Ficha>(CASOS.size());
        for(int i=0;i<CASOS.size();i++) fichas.add(new Ficha(hallazgos[i],documentos[i],clasificados[i]));
        return new Vista(java.util.List.copyOf(fichas),abierto,vecino,lupa,contraste,escena,segundosEscena,
            orientados,respuesta,ultimoHallazgo,calmaClasica,premium,publicacion);
    }
    void abrir(int id) {
        abierto=id;lupa=false;contraste=!premium&&clasificados[id-1];publicacion=premium&&clasificados[id-1];ultimoHallazgo=-1;respuesta="";
    }
    void cerrar() {
        abierto=0;vecino=-1;lupa=false;contraste=false;publicacion=false;ultimoHallazgo=-1;respuesta="";
    }
    boolean completo(int id) {return clasificados[id-1];}
    boolean tutorial() {return clasificados[0];}
    int clasificados() {int total=0;for(boolean valor:clasificados) if(valor) total++;return total;}
    boolean evidenciaCompleta(Caso caso) {return hallazgos[caso.id()-1]==caso.mascaraHallazgos();}
    boolean marcar(Caso caso,Zona zona) {
        int i=caso.id()-1,bit=1<<zona.ordinal();
        if((hallazgos[i]&bit)!=0) return false;
        hallazgos[i]|=bit;ultimoHallazgo=zona.ordinal();
        return true;
    }
    void mostrarFuentes(Caso caso) {documentos[caso.id()-1]=(1<<caso.documentos().size())-1;}
    void clasificar(Caso caso) {clasificados[caso.id()-1]=true;}
    boolean orientado(int id) {return (orientados&(1<<id))!=0;}
    void orientar(int id) {orientados|=1<<id;}
    void escena(Escena siguiente) {
        cerrar();escena=siguiente;segundosEscena=0;
    }
    void guardar(Properties p) {
        CamposEstado.escribir(this,p,"investigacion.");
        for(int i=0;i<CASOS.size();i++) {
            p.setProperty("investigacion.hallazgos."+i,Integer.toString(hallazgos[i]));
            p.setProperty("investigacion.documentos."+i,Integer.toString(documentos[i]));
            p.setProperty("investigacion.clasificado."+i,Boolean.toString(clasificados[i]));
        }
    }
    void cargar(Properties p) {
        CamposEstado.leer(this,p,"investigacion.");
        for(int i=0;i<CASOS.size();i++) {
            hallazgos[i]=Integer.parseInt(p.getProperty("investigacion.hallazgos."+i,"0"));
            documentos[i]=Integer.parseInt(p.getProperty("investigacion.documentos."+i,"0"));
            clasificados[i]=Boolean.parseBoolean(p.getProperty("investigacion.clasificado."+i,"false"));
        }
    }
}
