package alcaldedigital.servidor.sesion;

import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.compartido.estructuras.grafo.GrafoSocialMisionUno;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.compartido.modelo.Publicacion;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import java.util.*;

/** Autoridad local de la misión de Sofi. Prepara el mismo contrato para un servidor remoto. */
public final class SesionMisionUnoLocal implements SesionMisionUno {
    private static final String[] NOMBRES={"Álex","Mara","Don Elías","Nico","Lina","Sam","Eva","Doña Luz"};
    private static final String[] INTRO={
        "Sofi descansa en la banca. La gente pasea alrededor de la fuente.",
        "Mr. Palomo: ¿Noticias, opiniones y rumores? Civitas tiene de todo, querida.",
        "Mr. Palomo: Incluso puedo manipu... digo, mostrarte lo que comenta la ciudad.",
        "Sofi: Las palomas están rarísimas. Voy a mirar antes de creerles."
    };
    private static final String[] FUENTES={
        "El aviso del parque no informa de contaminación. La publicación carece de fuente.",
        "La agenda municipal confirma una jornada de limpieza esta tarde.",
        "Es una broma sobre las palomas; no describe un hecho verificable."
    };
    private static final float[][] POSICIONES={{276,137},{360,145},{440,206},{450,285},
        {365,366},{278,370},{191,287},{183,205}};
    private static final float[][] SALIDAS={{320,-30},{670,240},{320,510},{-30,240}};
    // Borde exterior del estanque en las coordenadas lógicas del fondo 640 × 480.
    private static final int[][] ESTANQUE={{49,283},{64,278},{80,279},{97,288},{117,296},
        {129,310},{135,335},{145,348},{164,350},{184,349},{204,357},{220,369},
        {229,384},{229,400},{220,414},{53,414},{42,403},{38,388},{39,368},
        {43,349},{37,334},{36,312},{41,294}};
    private ArbolAVLPublicaciones avl=new ArbolAVLPublicaciones();
    private ArbolDecisiones decisiones=new ArbolDecisiones("mision-1","Sofi y el parque");
    private final Map<String,String> solicitudes=new HashMap<>();
    private final Publicacion[] publicaciones={
        new Publicacion(1,65,"Cuenta Anónima","¡La fuente está contaminada! Hay que cerrarla ahora.","Sin comprobar"),
        new Publicacion(2,35,"Agenda municipal","Hoy habrá una jornada de limpieza en el parque.","Sin comprobar"),
        new Publicacion(3,20,"Chiste del barrio","Hay más palomas que votantes en Ciudad Nova.","Sin comprobar")
    };
    private final boolean[] consultadas=new boolean[3], resueltas=new boolean[3];
    private final String[] accionesPublicaciones={"","",""};
    private final PeatonMutable[] peatones=new PeatonMutable[8];
    private final GrafoSocialMisionUno grafoSocial=new GrafoSocialMisionUno();
    private final int[] olaSocial=grafoSocial.distanciasDesde(0,1);
    private final Dificultad dificultad;
    private final double plazo;
    private Fase fase=Fase.INTRO;
    private final int ladoPalomo;
    private EstadoPalomo estadoPalomo=EstadoPalomo.LLEGANDO;
    private double progresoPalomo;
    private int dialogo, puntos, calmados, parejasFuera, rumorCompartido;
    private int burlas;
    private String partida=UUID.randomUUID().toString();
    private float x=156,y=110;
    private int direccion;
    private boolean caminando;
    private double transcurrido, animacion;
    private int deterioros;
    private EstadoCiudad ciudad=EstadoCiudad.inicial();
    private String mensaje=INTRO[0], desenlace="";

    private static final class PeatonMutable {
        final int id, salida, necesarias;
        final float inicioX,inicioY;
        float x,y;
        int direccion, conversaciones;
        Animo animo=Animo.NORMAL;
        PeatonMutable(int id) {
            this.id=id; salida=id/2; necesarias=id%2==0?2:3;
            inicioX=POSICIONES[id][0];inicioY=POSICIONES[id][1];x=inicioX;y=inicioY;
        }
        Peaton vista() {return new Peaton(id,x,y,direccion,animo,conversaciones,necesarias,salida);}
    }
    public SesionMisionUnoLocal(long semilla,Dificultad dificultad) {
        this.dificultad=Objects.requireNonNull(dificultad);
        ladoPalomo=Math.floorMod(semilla,4);
        plazo=dificultad.segundos();
        for(Publicacion p:publicaciones) {
            avl.insertar(p);
            decisiones.insertar("mision-1","post-"+p.id(),"Publicación #"+p.id());
            for(String accion:List.of("compartir","verificar","reportar","ignorar"))
                decisiones.insertar("post-"+p.id(),accion+"-"+p.id(),accion);
        }
        for(int i=0;i<peatones.length;i++) peatones[i]=new PeatonMutable(i);
        // La semilla determina el borde de llegada y regreso del palomo.
    }
    public Properties capturar(){
        Properties p=new Properties();p.setProperty("version","1");p.setProperty("dificultad",dificultad.name());p.setProperty("lado",Integer.toString(ladoPalomo));
        alcaldedigital.compartido.guardado.CamposEstado.escribir(this,p,"sesion.");
        var campos=new String[]{"informacionVerificada","confianza","convivencia","bienestar","participacion","desinformacion","conflictos"};
        int[] valores={ciudad.informacionVerificada(),ciudad.confianza(),ciudad.convivencia(),ciudad.bienestar(),ciudad.participacion(),ciudad.desinformacion(),ciudad.conflictos()};
        for(int i=0;i<7;i++)p.setProperty("ciudad."+campos[i],Integer.toString(valores[i]));
        for(int i=0;i<8;i++)alcaldedigital.compartido.guardado.CamposEstado.escribir(peatones[i],p,"peaton."+i+".");
        for(int i=0;i<3;i++){Publicacion v=publicaciones[i];p.setProperty("post."+i+".riesgo",Integer.toString(v.riesgo()));p.setProperty("post."+i+".estado",v.estado());p.setProperty("consultada."+i,Boolean.toString(consultadas[i]));p.setProperty("resuelta."+i,Boolean.toString(resueltas[i]));p.setProperty("accion."+i,accionesPublicaciones[i]);}
        for(String id:decisiones.recorrer("preorden")){var v=decisiones.buscar(id);p.setProperty("arbol."+id+".texto",v.texto());p.setProperty("arbol."+id+".ejecutado",Boolean.toString(v.ejecutado()));if(!id.equals("mision-1")){var ruta=decisiones.ruta(id);p.setProperty("arbol."+id+".padre",ruta.get(ruta.size()-2));}}
        p.setProperty("nodos",String.join(",",decisiones.recorrer("preorden")));
        p.setProperty("avl.ids",avl.recorrer("inorden").stream().map(v->Long.toString(v.id())).collect(java.util.stream.Collectors.joining(",")));
        solicitudes.forEach((k,v)->p.setProperty("solicitud."+k,v));return p;
    }
    public static SesionMisionUnoLocal restaurar(Properties p){
        if(!"1".equals(p.getProperty("version")))throw new IllegalArgumentException("Versión de misión 1 inválida");
        var s=new SesionMisionUnoLocal(Long.parseLong(p.getProperty("lado")),Dificultad.valueOf(p.getProperty("dificultad")));
        alcaldedigital.compartido.guardado.CamposEstado.leer(s,p,"sesion.");
        String[] campos={"informacionVerificada","confianza","convivencia","bienestar","participacion","desinformacion","conflictos"};int[] n=new int[7];for(int i=0;i<7;i++)n[i]=Integer.parseInt(p.getProperty("ciudad."+campos[i]));s.ciudad=new EstadoCiudad(n[0],n[1],n[2],n[3],n[4],n[5],n[6]);
        for(int i=0;i<8;i++)alcaldedigital.compartido.guardado.CamposEstado.leer(s.peatones[i],p,"peaton."+i+".");
        s.avl=new ArbolAVLPublicaciones();for(int i=0;i<3;i++){var v=s.publicaciones[i];s.publicaciones[i]=new Publicacion(v.id(),Integer.parseInt(p.getProperty("post."+i+".riesgo")),v.autor(),v.texto(),p.getProperty("post."+i+".estado"));if(Arrays.asList(p.getProperty("avl.ids", "1,2,3").split(",")).contains(Long.toString(v.id())))s.avl.insertar(s.publicaciones[i]);s.consultadas[i]=Boolean.parseBoolean(p.getProperty("consultada."+i));s.resueltas[i]=Boolean.parseBoolean(p.getProperty("resuelta."+i));s.accionesPublicaciones[i]=p.getProperty("accion."+i,"");}
        String[] ids=p.getProperty("nodos").split(",");s.decisiones=new ArbolDecisiones("mision-1",p.getProperty("arbol.mision-1.texto"));for(String id:ids)if(!id.equals("mision-1"))s.decisiones.insertar(p.getProperty("arbol."+id+".padre"),id,p.getProperty("arbol."+id+".texto"));for(String id:ids)if(Boolean.parseBoolean(p.getProperty("arbol."+id+".ejecutado")))s.decisiones.ejecutar(id);
        for(String k:p.stringPropertyNames())if(k.startsWith("solicitud."))s.solicitudes.put(k.substring(10),p.getProperty(k));
        s.caminando=false;return s;
    }
    public String partida(){return partida;}
    public int burlas(){return burlas;}
    public List<String> decisionesRegistradas(){
        List<String> salida=new ArrayList<>();
        for(int i=0;i<3;i++)salida.add("Publicación "+(i+1)+": "+(consultadas[i]?"contrastada; ":"sin contrastar; ")+
            (accionesPublicaciones[i].isBlank()?"sin decisión":accionesPublicaciones[i]));
        salida.add("Burlas a vecinos: "+burlas);
        return List.copyOf(salida);
    }
    @Override public Vista vista() {
        return new Vista(fase,dificultad,dialogo,new Palomo(ladoPalomo,progresoPalomo,estadoPalomo),
            x,y,direccion,caminando,Math.max(0,plazo-transcurrido),puntos,calmados,
            parejasFuera,mensaje,Arrays.stream(peatones).map(PeatonMutable::vista).toList(),
            List.of(publicaciones.clone()),java.util.stream.IntStream.range(0,3)
                .mapToObj(i->consultadas[i]?FUENTES[i]:"").toList(),
            java.util.stream.IntStream.range(0,3).mapToObj(i->resueltas[i]).toList(),ciudad,desenlace,
            avl.vista(),decisiones.vista(),grafoSocial.aristas());
    }
    @Override public String solicitar(String id,String accion) {
        if(id==null||id.isBlank()||accion==null) throw new IllegalArgumentException("Solicitud inválida");
        if(solicitudes.containsKey(id)) return solicitudes.get(id);
        String respuesta=resolver(accion);
        solicitudes.put(id,respuesta);mensaje=respuesta;
        return respuesta;
    }
    private String resolver(String accion) {
        if(fase==Fase.RESULTADO) return "La misión terminó.";
        if(fase==Fase.CRISIS&&accion.startsWith("verificar:")) {
            int indice;
            try {indice=Integer.parseInt(accion.substring(10))-1;} catch(NumberFormatException e) {return "Publicación desconocida.";}
            if(indice<0||indice>=3) return "Publicación desconocida.";
            if(consultadas[indice]) return FUENTES[indice];
            consultadas[indice]=true;puntos+=3;ciudad=cambiar(3,1,0,0,0,-2,0);
            return FUENTES[indice];
        }
        if(fase==Fase.INTRO) {
            if(!accion.equals("continuar")) return "Escucha a Mr. Palomo antes de continuar.";
            if(estadoPalomo==EstadoPalomo.LLEGANDO) return "Mr. Palomo todavía se acerca volando.";
            if(estadoPalomo==EstadoPalomo.SALIENDO) return "Mr. Palomo regresa por donde llegó.";
            if(++dialogo<INTRO.length) return INTRO[dialogo];
            estadoPalomo=EstadoPalomo.SALIENDO;progresoPalomo=0;
            return "Mr. Palomo se despide y vuela de regreso. Sofi abre Civitas.";
        }
        if(fase==Fase.CIVITAS) {
            String[] partes=accion.split(":");
            if(partes.length!=2) return "Elige una acción para una publicación.";
            int indice;
            try {indice=Integer.parseInt(partes[1])-1;} catch(NumberFormatException e) {return "Publicación desconocida.";}
            if(indice<0||indice>=3) return "Publicación desconocida.";
            if(resueltas[indice]) return "Ya decidiste qué hacer con esa publicación.";
            String tipo=partes[0];
            if(tipo.equals("verificar")) {
                if(consultadas[indice]) return FUENTES[indice];
                consultadas[indice]=true;
                decisiones.ejecutar("verificar-"+(indice+1));
                reindexar(indice,Math.max(5,publicaciones[indice].riesgo()-25),"Contrastada");
                ciudad=cambiar(5,2,0,0,2,-4,-1);puntos+=5;
                return FUENTES[indice]+" Ahora decide qué hacer con la publicación.";
            }
            if(!Set.of("compartir","reportar","ignorar").contains(tipo)) return "Acción desconocida.";
            resueltas[indice]=true;
            accionesPublicaciones[indice]=tipo+(consultadas[indice]?" con contexto":" sin contexto");
            decisiones.ejecutar(tipo+"-"+(indice+1));
            String respuesta;
            if(tipo.equals("compartir")) {
                if(!consultadas[indice]) {rumorCompartido++;reindexar(indice,Math.min(100,publicaciones[indice].riesgo()+25),"Compartida sin verificar");
                    ciudad=cambiar(-3,-4,-4,-3,0,8,6);puntos-=5;
                    respuesta="Compartiste sin contexto. La tensión en el parque aumenta.";}
                else if(indice==0) {avl.eliminar(publicaciones[indice].id());
                    ciudad=cambiar(7,5,4,3,3,-8,-4);puntos+=10;
                    respuesta="Publicaste la falta de pruebas y frenaste el rumor de la fuente.";}
                else {ciudad=cambiar(4,3,2,2,3,-2,-1);puntos+=6;
                    respuesta="Compartiste con contexto: "+FUENTES[indice];}
            } else if(tipo.equals("reportar")) {
                if(consultadas[indice]&&indice==0) {avl.eliminar(publicaciones[indice].id());
                    ciudad=cambiar(5,3,2,2,0,-6,-2);puntos+=6;
                    respuesta="Reporte sustentado: la publicación sobre la fuente sale del índice activo.";}
                else {ciudad=cambiar(0,-2,0,0,0,0,1);puntos-=2;
                    respuesta="El reporte no demuestra que la publicación sea falsa.";}
            } else {respuesta="No amplificaste esta publicación.";puntos+=1;}
            if(resueltas[0]&&resueltas[1]&&resueltas[2]) iniciarCrisis();
            return respuesta+(fase==Fase.CRISIS?" Ocho personas se alteran: tienes "+(int)plazo+" segundos para calmarlas.":"");
        }
        if(accion.equals("interactuar")||accion.equals("burlarse")) {
            PeatonMutable cercano=null;float distancia=Float.MAX_VALUE;
            for(PeatonMutable p:peatones) if(p.animo==Animo.ALTERADO) {
                float d=(float)Math.hypot(x-p.x,y-p.y);
                if(d<distancia) {distancia=d;cercano=p;}
            }
            if(cercano==null||distancia>38) return "Acércate a una persona alterada y pulsa E.";
            if(accion.equals("burlarse")) {
                burlas++;puntos-=3;ciudad=cambiar(0,-2,-3,-2,0,0,4);
                cercano.conversaciones=0;
                return "La burla empeoró el diálogo con "+NOMBRES[cercano.id]+". Escuchar puede reparar el daño.";
            }
            cercano.conversaciones++;
            if(cercano.conversaciones<cercano.necesarias)
                return NOMBRES[cercano.id]+": Escucho, pero sigo preocupado. Háblame otra vez.";
            cercano.animo=Animo.CALMADO;cercano.direccion=0;calmados++;puntos+=8;
            ciudad=cambiar(2,2,4,3,1,-2,-4);
            int pareja=cercano.salida;
            if(peatones[pareja*2].animo==Animo.CALMADO&&peatones[pareja*2+1].animo==Animo.CALMADO) {
                peatones[pareja*2].animo=Animo.SALIENDO;
                peatones[pareja*2+1].animo=Animo.SALIENDO;
                return NOMBRES[cercano.id]+" se calmó. La pareja sale por el camino "+new String[]{"norte","este","sur","oeste"}[pareja]+".";
            }
            return NOMBRES[cercano.id]+" respiró y dejó el teléfono. "+calmados+" de 8 personas calmadas.";
        }
        return "Acción no disponible ahora.";
    }
    private void iniciarCrisis() {
        fase=Fase.CRISIS;transcurrido=0;
        for(PeatonMutable p:peatones) {
            p.x=p.inicioX;p.y=p.inicioY;p.animo=olaSocial[p.id]==0?Animo.ALTERADO:Animo.NORMAL;
        }
        ciudad=cambiar(0,-4,-5,-4,0,4,5);
    }
    private void reindexar(int i,int riesgo,String estado) {
        publicaciones[i]=publicaciones[i].conRiesgo(riesgo,estado);
        avl.actualizar(publicaciones[i]);
    }
    private EstadoCiudad cambiar(int informacion,int confianza,int convivencia,int bienestar,
                                 int participacion,int desinformacion,int conflictos) {
        return new EstadoCiudad(ciudad.informacionVerificada()+informacion,ciudad.confianza()+confianza,
            ciudad.convivencia()+convivencia,ciudad.bienestar()+bienestar,ciudad.participacion()+participacion,
            ciudad.desinformacion()+desinformacion,ciudad.conflictos()+conflictos);
    }
    @Override public void mover(float dx,float dy) {
        if(fase!=Fase.CRISIS||!Float.isFinite(dx)||!Float.isFinite(dy)) return;
        float largo=(float)Math.hypot(dx,dy);
        if(largo>7) {dx*=7/largo;dy*=7/largo;}
        float nx=Math.max(22,Math.min(618,x+dx)),ny=Math.max(22,Math.min(458,y+dy));
        if(Math.hypot(nx-320,ny-240)>45&&!enEstanque(nx,ny)) {x=nx;y=ny;}
        if(dx!=0||dy!=0) {direccion=Math.abs(dx)>Math.abs(dy)?(dx>0?2:1):(dy>0?0:3);caminando=true;}
        else caminando=false;
    }
    private static boolean enEstanque(float px,float py) {
        boolean dentro=false;
        for(int i=0,j=ESTANQUE.length-1;i<ESTANQUE.length;j=i++) {
            int[] a=ESTANQUE[i],b=ESTANQUE[j];
            if((a[1]>py)!=(b[1]>py)&&px<(b[0]-a[0])*(py-a[1])/(b[1]-a[1])+a[0])
                dentro=!dentro;
        }
        return dentro;
    }
    @Override public void actualizar(double segundos) {
        if(!Double.isFinite(segundos)||segundos<0) throw new IllegalArgumentException("Tiempo inválido");
        segundos=Math.min(.1,segundos);animacion+=segundos;
        if(fase==Fase.INTRO) {
            if(estadoPalomo==EstadoPalomo.LLEGANDO) {
                progresoPalomo=Math.min(1,progresoPalomo+segundos/1.5);
                if(progresoPalomo>=1-1e-8) {progresoPalomo=1;estadoPalomo=EstadoPalomo.POSADO;}
            } else if(estadoPalomo==EstadoPalomo.SALIENDO) {
                progresoPalomo=Math.min(1,progresoPalomo+segundos/1.35);
                if(progresoPalomo>=1-1e-8) {
                    progresoPalomo=1;
                    estadoPalomo=EstadoPalomo.FUERA;fase=Fase.CIVITAS;
                    mensaje="Civitas se abre. Examina las publicaciones antes de compartirlas.";
                }
            }
            for(PeatonMutable p:peatones) {
                float nx=p.inicioX+(float)Math.sin(animacion*.65+p.id)*10;
                p.direccion=nx>=p.x?2:1;p.x=nx;
            }
            return;
        }
        if(fase!=Fase.CRISIS) return;
        transcurrido+=segundos;
        for(PeatonMutable p:peatones) if(p.animo==Animo.NORMAL&&transcurrido>=olaSocial[p.id]*2) {
            p.animo=Animo.ALTERADO;
            mensaje="El rumor llegó a "+NOMBRES[p.id]+" por la red social. La tensión se propaga.";
        }
        int pulso=(int)(transcurrido/5);
        while(deterioros<pulso) {
            deterioros++;
            long alterados=Arrays.stream(peatones).filter(p->p.animo==Animo.ALTERADO).count();
            if(alterados>0) ciudad=cambiar(0,-1,-2,-2,0,1,2);
        }
        for(PeatonMutable p:peatones) {
            if(p.animo==Animo.ALTERADO) {
                float nuevoX=p.inicioX+(float)Math.sin(animacion*1.2+p.id)*8;
                float nuevoY=p.inicioY+(float)Math.cos(animacion*.9+p.id)*6;
                p.direccion=nuevoX>=p.x?2:1;p.x=nuevoX;p.y=nuevoY;
            } else if(p.animo==Animo.SALIENDO) {
                float tx=SALIDAS[p.salida][0],ty=SALIDAS[p.salida][1];
                float dx=tx-p.x,dy=ty-p.y,len=(float)Math.hypot(dx,dy);
                float avance=(float)(segundos*115);
                if(len<=avance) {p.x=tx;p.y=ty;p.animo=Animo.FUERA;}
                else {p.x+=dx/len*avance;p.y+=dy/len*avance;p.direccion=Math.abs(dx)>Math.abs(dy)?(dx>0?2:1):(dy>0?0:3);}
            }
        }
        int fuera=0;for(int i=0;i<4;i++) if(peatones[i*2].animo==Animo.FUERA&&peatones[i*2+1].animo==Animo.FUERA) fuera++;
        parejasFuera=fuera;
        if(calmados==8&&parejasFuera==4) terminar();
        else if(transcurrido>=plazo&&calmados<8) terminar();
    }
    private void terminar() {
        fase=Fase.RESULTADO;
        int balance=calmados*8+(ciudad.informacionVerificada()-50)+
            (ciudad.convivencia()-50)-(ciudad.desinformacion()-20)-rumorCompartido*8;
        desenlace=calmados<=1?"MALO":balance>=75&&calmados>=6?"BUENO":balance>=25?"NEUTRAL":"MALO";
        mensaje=switch(desenlace) {
            case "BUENO" -> "Sofi: La gente se calmó. Verificar antes de compartir protegió el parque.";
            case "NEUTRAL" -> "Sofi: Algunos escucharon, pero el parque aún necesita cuidado y contexto.";
            default -> "Sofi: ¿Qué pasó aquí? Un rumor también deja huellas fuera de la pantalla.";
        };
    }
}
