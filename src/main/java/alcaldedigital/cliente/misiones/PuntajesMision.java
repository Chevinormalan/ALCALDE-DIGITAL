package alcaldedigital.cliente.misiones;

import alcaldedigital.cliente.mision2.MisionDosSketch;
import alcaldedigital.cliente.mision3.MisionTresSketch;
import alcaldedigital.cliente.mision4.MisionCuatroSketch;
import alcaldedigital.cliente.mision6.MisionSeisMotor;
import alcaldedigital.compartido.batalla.EstadoBatalla;
import alcaldedigital.compartido.carrera.EstadoCarrera;
import alcaldedigital.compartido.guardado.TablaPuntajes.Fila;
import alcaldedigital.compartido.guardado.TablaPuntajes.Componente;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.compartido.modelo.ProgresoCampana;
import alcaldedigital.servidor.sesion.SesionMisionUnoLocal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Traduce resultados reales a la misma escala visual sin cambiar las reglas de victoria. */
public final class PuntajesMision {
    private PuntajesMision() { }
    private static int redondear(double n) { return (int)Math.round(n); }
    private static List<Integer> cambio(EstadoCiudad antes,EstadoCiudad despues){
        int[] a={antes.informacionVerificada(),antes.confianza(),antes.convivencia(),antes.bienestar(),antes.participacion(),antes.desinformacion(),antes.conflictos()};
        int[] b={despues.informacionVerificada(),despues.confianza(),despues.convivencia(),despues.bienestar(),despues.participacion(),despues.desinformacion(),despues.conflictos()};
        List<Integer> d=new ArrayList<>();for(int i=0;i<7;i++)d.add(b[i]-a[i]);return List.copyOf(d);
    }
    private static List<Integer> cambio(int[] antes,int[] despues){
        List<Integer> d=new ArrayList<>();for(int i=0;i<7;i++)d.add(despues[i]-antes[i]);return List.copyOf(d);
    }
    private static final List<Integer> CERO=List.of(0,0,0,0,0,0,0);

    public static Fila uno(SesionMisionUnoLocal sesion) {
        var v=sesion.vista();
        int responsables=0;
        for(String d:sesion.decisionesRegistradas())if(d.contains("contrastada; compartir con contexto")||
            d.startsWith("Publicación 1: contrastada; reportar con contexto"))responsables++;
        var investigacion=sesion.investigacion();
        if(investigacion!=null) {
            responsables=(int)sesion.decisionesRegistradas().stream().filter(d->
                d.contains("contrastada; compartir con contexto")||d.contains("contrastada; reportar con contexto")).count();
            int vecinos=investigacion.calmaClasica()?v.calmados()*6:investigacion.ciudadanos()*12;
            String conteo=investigacion.calmaClasica()?"Vecinos calmados: "+v.calmados()+"/8":
                "Vecinos calmados: "+investigacion.ciudadanos()+"/4";
            int score=redondear(investigacion.clasificados()*10+vecinos+
                12.0*investigacion.evidencias()/alcaldedigital.compartido.modelo.InvestigacionCiudadana.totalEvidencias());
            return new Fila(1,sesion.partida(),"BUENO".equals(v.desenlace()),score,v.dificultad().name(),
                List.of("Casos clasificados: "+investigacion.clasificados()+"/4",
                    "Evidencias: "+investigacion.evidencias(),conteo),
                sesion.decisionesRegistradas(),cambio(EstadoCiudad.inicial(),v.ciudad()),
                Map.of("vecinos_calmados",v.calmados(),"publicaciones_responsables",responsables,"burlas",sesion.burlas(),
                    "casos_clasificados",investigacion.clasificados(),"evidencias",investigacion.evidencias()))
                .conDesglose(new Componente("Casos clasificados",investigacion.clasificados()*10,40),
                    new Componente("Vecinos calmados",vecinos,48),new Componente("Evidencias",score-investigacion.clasificados()*10-vecinos,12));
        }
        int score=redondear(v.calmados()*5+responsables*40.0/3+
            Math.max(0,Math.min(20,v.puntos()/4.0)));
        return new Fila(1,sesion.partida(),"BUENO".equals(v.desenlace()),score,v.dificultad().name(),
            List.of("Vecinos calmados: "+v.calmados()+"/8","Puntos: "+v.puntos(),"Desenlace: "+v.desenlace()),
            sesion.decisionesRegistradas(),cambio(EstadoCiudad.inicial(),v.ciudad()),
            Map.of("vecinos_calmados",v.calmados(),"publicaciones_responsables",responsables,"burlas",sesion.burlas()))
            .conDesglose(new Componente("Vecinos calmados",v.calmados()*5,40),new Componente("Publicaciones responsables",responsables*40.0/3,40),
                new Componente("Puntos de la misión",Math.clamp(v.puntos()/4.0,0,20),20));
    }
    public static Fila dos(MisionDosSketch.Resultado r,String partida,String dificultad) {
        boolean gano=r.terminada()&&r.exito()&&!r.falloCritico();
        int score=r.falloCritico()?0:redondear(75.0*r.protegidos()/6+25*Math.max(0,Math.min(1,r.coberturaMedia())));
        return new Fila(2,partida,gano,score,dificultad,
            List.of("Equipos seguros: "+r.protegidos()+"/6","Cobertura media: "+redondear(r.coberturaMedia()*100)+"%",
                r.falloCritico()?"Fallo crítico inevitable: no penaliza campaña":"Defensa terminada: "+r.terminada()),List.of(),
            gano?cambio(EstadoCiudad.inicial(),ProgresoCampana.aplicarMisionDos(EstadoCiudad.inicial(),r.protegidos())):CERO,
            Map.of("equipos_seguros",r.protegidos(),"cobertura_media",redondear(r.coberturaMedia()*100),
                "fallo_critico",r.falloCritico()?1:0))
            .conDesglose(new Componente("Equipos seguros",r.falloCritico()?0:75.0*r.protegidos()/6,75),
                new Componente("Cobertura media",r.falloCritico()?0:25*Math.clamp(r.coberturaMedia(),0,1),25));
    }
    public static Fila tres(MisionTresSketch.Resultado r,String partida) {
        int score=(r.completada()?60:0)+redondear(20*r.confianza()/100.0)+
            redondear(20*(100-r.desinformacion())/100.0);
        return new Fila(3,partida,r.completada(),score,"",
            List.of("Confianza: "+r.confianza()+"%","Desinformación: "+r.desinformacion()+"%","Amenazas vencidas: "+r.amenazas()),List.of(),
            List.of(0,r.confianza()-35,0,0,0,r.desinformacion()-8,0),
            Map.of("confianza_final",r.confianza(),"desinformacion_final",r.desinformacion(),"amenazas_vencidas",r.amenazas()))
            .conDesglose(new Componente("Finalización",r.completada()?60:0,60),new Componente("Confianza",redondear(20*r.confianza()/100.0),20),
                new Componente("Desinformación baja",redondear(20*(100-r.desinformacion())/100.0),20));
    }
    public static Fila cuatro(MisionCuatroSketch.Resultado r) {
        int favorables=0,desfavorables=0,neutrales=0;
        List<String> respuestas=new ArrayList<>();
        for(int i=0;i<r.respuestas().size();i++){
            int opcion=r.respuestas().get(i);if(opcion<0)continue;
            String valoracion=i==2?"neutral":opcion==0?"favorable":"desfavorable";
            if(i==2)neutrales++;else if(opcion==0)favorables++;else desfavorables++;
            respuestas.add("Pregunta "+(i+1)+": "+(opcion==0?"No":"Sí")+" · "+valoracion);
        }
        int score=(r.completada()?40:0)+redondear(r.publicacionesValidadas()*30.0/r.totalPublicaciones())+
            redondear(favorables*20.0/3)+redondear(r.enemigos()*10.0/3);
        return new Fila(4,r.partida(),r.completada(),score,"",
            List.of("Fuentes investigadas: "+r.publicacionesValidadas()+"/"+r.totalPublicaciones(),
                "Respuestas: "+favorables+" favorables, "+desfavorables+" "+(desfavorables==1?"desfavorable":"desfavorables")+
                    ", "+neutrales+" "+(neutrales==1?"neutral":"neutrales"),
                "Amenazas detenidas: "+r.enemigos()+"/3"),respuestas,
            cambio(EstadoCiudad.inicial(),r.respuestas().size()!=4||r.respuestas().stream().anyMatch(a->a<0)?ProgresoCampana.aplicarMisionCuatro(EstadoCiudad.inicial(),r.publicacionesValidadas(),r.respuestasResponsables()):
                ProgresoCampana.aplicarMisionCuatro(EstadoCiudad.inicial(),r.publicacionesValidadas(),favorables,desfavorables)),
            Map.of("fuentes_validadas",r.publicacionesValidadas(),"respuestas_favorables",favorables,
                "respuestas_desfavorables",desfavorables,"respuestas_neutrales",neutrales))
            .conDesglose(new Componente("Finalización",r.completada()?40:0,40),
                new Componente("Fuentes investigadas",redondear(r.publicacionesValidadas()*30.0/r.totalPublicaciones()),30),
                new Componente("Respuestas favorables",redondear(favorables*20.0/3),20),
                new Componente("Amenazas detenidas",redondear(r.enemigos()*10.0/3),10));
    }
    public static Fila cinco(EstadoBatalla.Vista v) {
        boolean gano=v.fase()==EstadoBatalla.Fase.FINAL&&v.gano();
        int jugadores=v.solo()?1:2,vidas=0,puntos=0;
        List<String> datos=new ArrayList<>();
        for(int i=0;i<jugadores;i++){var j=v.jugadores()[i];vidas+=j.vidas();puntos+=j.puntos();
            datos.add(j.nombre()+": "+j.puntos()+" puntos, "+j.vidas()+" vidas");}
        datos.add("Publicaciones verificadas: "+v.verificados());
        int score=(gano?50:0)+Math.min(25,v.verificados()*5)+redondear(25.0*vidas/(5*jugadores));
        List<String> historial=List.of(v.historial());
        if(historial.size()>128)historial=historial.subList(historial.size()-128,historial.size());
        return new Fila(5,v.id(),gano,score,v.dificultad().name(),datos,historial,cambio(v.base(),v.ciudad()),
            Map.of("publicaciones_verificadas",v.verificados(),"rumores",v.rumores(),"vidas_restantes",vidas))
            .conDesglose(new Componente("Victoria",gano?50:0,50),new Componente("Verificaciones",Math.min(25,v.verificados()*5),25),
                new Componente("Vidas restantes",redondear(25.0*vidas/(5*jugadores)),25));
    }
    public static Fila seis(MisionSeisMotor.Resultado r) {
        List<String> respuestas=new ArrayList<>();int escucha=0,conviccion=0;
        for(int i=0;i<r.respuestas().size();i++){
            int opcion=r.respuestas().get(i);if(opcion<0)continue;
            if(opcion==0)escucha++;else conviccion++;
            respuestas.add("Pregunta "+(i+1)+": "+(opcion==0?"escucha":"convicción y diálogo"));
        }
        int n=r.modo()==MisionSeisMotor.Modo.DUO?2:1;
        int vidas=Math.max(0,r.integridad1())+(n==2?Math.max(0,r.integridad2()):0);
        int score=(r.completada()?70:0)+redondear(30.0*vidas/(5*n));
        return new Fila(6,r.partida(),r.completada(),score,r.modo().name(),
            List.of("Integridad de naves: "+vidas+"/"+(5*n),
                "Entrevista: "+escucha+" escucha, "+conviccion+" convicción",
                "Ruta: "+r.ruta()),respuestas,CERO,
            Map.of("respuestas_escucha",escucha,"respuestas_conviccion",conviccion,"integridad_restante",vidas))
            .conDesglose(new Componente("Finalización",r.completada()?70:0,70),
                new Componente("Integridad de las naves",redondear(30.0*vidas/(5*n)),30));
    }
    public static Fila siete(EstadoCarrera.Vista v) {
        boolean gano=v.fase()==EstadoCarrera.Fase.RESULTADO&&v.ganador()<2&&v.humano(v.ganador());
        int total=0,reputacion=0,verificadas=0,humanos=0;
        List<String> datos=new ArrayList<>(),decisiones=new ArrayList<>();
        for(int i=0;i<2;i++)if(v.humano(i)){var c=v.corredores()[i];humanos++;total+=c.puntos();reputacion+=c.reputacion();
            datos.add((i==0?"Periodista":"Influencer")+": "+c.puntos()+" puntos, reputación "+c.reputacion());}
        for(String h:v.historial())if(h.matches("r[01]e[0-3]a[0-3].*")){decisiones.add(h);if(h.matches("r[01]e[0-3]a0.*"))verificadas++;}
        datos.add("Noticias contrastadas: "+verificadas);
        int score=(gano?35:0)+redondear(25.0*reputacion/(100*Math.max(1,humanos)))+
            Math.min(20,verificadas*5)+Math.min(20,Math.max(0,total)/20);
        return new Fila(7,v.id(),gano,score,v.dificultad().name(),datos,decisiones,cambio(v.base(),v.ciudad()),
            Map.of("noticias_contrastadas",verificadas,"juan_corrupto",v.electoral().juanCorrupto()?1:0,
                "reputacion_media",redondear((double)reputacion/Math.max(1,humanos))))
            .conDesglose(new Componente("Victoria",gano?35:0,35),
                new Componente("Reputación",redondear(25.0*reputacion/(100*Math.max(1,humanos))),25),
                new Componente("Noticias contrastadas",Math.min(20,verificadas*5),20),
                new Componente("Puntos de carrera",Math.min(20,Math.max(0,total)/20),20));
    }

    public static String mostrarDecisionCarrera(String decision){
        if(decision.matches("r[01]e2a[01] · .*")){
            String corredor=decision.charAt(1)=='0'?"Periodista":"Influencer";
            return corredor+(decision.charAt(5)=='1'?": aceptó el pacto.":": rechazó el pacto.");
        }
        return decision.replaceFirst("^r[01]e[0-3]a[0-3] · ","");
    }

    public static Fila corredor(EstadoCarrera.Vista vista,int rol) {
        if(!vista.humano(rol))throw new IllegalArgumentException("El corredor debe ser humano");
        var corredor=vista.corredores()[rol];
        boolean gano=vista.ganador()==rol;
        String nombre=rol==0?"Periodista":"Influencer";
        String prefijo="r"+rol+"e";
        var decisiones=vista.historial().stream().filter(s->s.matches(prefijo+"[0-3]a[0-3].*")).toList();
        int verificadas=(int)decisiones.stream().filter(s->s.matches(prefijo+"[0-3]a0.*")).count();
        int victoria=gano?35:0,reputacion=redondear(25.0*corredor.reputacion()/100),
            noticias=Math.min(20,verificadas*5),puntos=Math.min(20,Math.max(0,corredor.puntos())/20);
        return new Fila(7,vista.id(),gano,victoria+reputacion+noticias+puntos,vista.dificultad().name(),
            List.of(nombre+" · "+(gano?"Llegó primero":vista.ganador()==2?"Empate":"Terminó detrás"),
                "Puntos de carrera: "+corredor.puntos(),"Reputación: "+corredor.reputacion(),"Noticias contrastadas: "+verificadas),
            decisiones,CERO,Map.of())
            .conDesglose(new Componente("Victoria",victoria,35),new Componente("Reputación",reputacion,25),
                new Componente("Noticias contrastadas",noticias,20),new Componente("Puntos de carrera",puntos,20));
    }
}
