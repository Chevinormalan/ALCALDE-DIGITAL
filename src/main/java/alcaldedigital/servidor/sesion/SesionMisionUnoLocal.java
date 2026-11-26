package alcaldedigital.servidor.sesion;

import alcaldedigital.compartido.estructuras.avl.ArbolAVLPublicaciones;
import alcaldedigital.compartido.estructuras.decisiones.ArbolDecisiones;
import alcaldedigital.compartido.estructuras.grafo.GrafoSocialMisionUno;
import alcaldedigital.compartido.modelo.EstadoCiudad;
import alcaldedigital.compartido.modelo.Publicacion;
import alcaldedigital.compartido.protocolo.SesionMisionUno;
import alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;
import static alcaldedigital.compartido.modelo.InvestigacionCiudadana.*;
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
    private static final String[] INTRO_INVESTIGACION={
        "Sofi descansa en la banca. Civitas parece tranquilo por un momento.",
        "Mr. Palomo: ¡Cucurrucucú! Dicen que van a cerrar la fuente. ¿Quieres ser la primera en compartirlo?",
        "Sofi: ¿Y quién lo dijo?  Mr. Palomo: Una cuenta anónima. Tiene foto de perfil, si eso ayuda.",
        "Sofi: La foto no es una prueba. Voy a ver qué dicen los papeles del parque."
    };
    private static final String[] FUENTES={
        "El aviso del parque no informa de contaminación. La publicación carece de fuente.",
        "La agenda municipal confirma una jornada de limpieza esta tarde.",
        "Es una broma sobre las palomas; no describe un hecho verificable.",
        "La agenda de hoy sí incluye la limpieza. El aviso del parque confirma la misma jornada."
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
        new Publicacion(3,20,"Chiste del barrio","Hay más palomas que votantes en Ciudad Nova.","Sin comprobar"),
        new Publicacion(4,48,"Boletín reenviado",caso(4).titular(),"Sin comprobar")
    };
    private final boolean[] consultadas=new boolean[4], resueltas=new boolean[4];
    private final String[] accionesPublicaciones={"","","",""};
    private ExpedientesSofi expedientes;
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
    private static final int INICIO_X=156, INICIO_Y=110;
    private float x=INICIO_X,y=INICIO_Y;
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
        int direccion, conversaciones, tramo;
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
        for(int i=0;i<3;i++) insertarPublicacion(publicaciones[i]);
        for(int i=0;i<peatones.length;i++) peatones[i]=new PeatonMutable(i);
        // La semilla determina el borde de llegada y regreso del palomo.
    }
    private void insertarPublicacion(Publicacion p) {
        avl.insertar(p);
        decisiones.insertar("mision-1","post-"+p.id(),"Publicación #"+p.id());
        for(String accion:List.of("compartir","verificar","reportar","ignorar"))
            decisiones.insertar("post-"+p.id(),accion+"-"+p.id(),accion);
    }
    @Override public void activarInvestigacion() {
        if(fase!=Fase.INTRO||expedientes!=null) return;
        expedientes=new ExpedientesSofi();expedientes.premium=true;
        insertarPublicacion(publicaciones[3]);
        mensaje=INTRO_INVESTIGACION[0];
    }
    @Override public alcaldedigital.compartido.modelo.InvestigacionCiudadana.Vista investigacion() {
        return expedientes==null?null:expedientes.vista();
    }
    private int cantidadPublicaciones() {return expedientes==null?3:publicaciones.length;}
    public Properties capturar(){
        Properties p=new Properties();p.setProperty("version","1");p.setProperty("dificultad",dificultad.name());p.setProperty("lado",Integer.toString(ladoPalomo));
        alcaldedigital.compartido.guardado.CamposEstado.escribir(this,p,"sesion.");
        var campos=new String[]{"informacionVerificada","confianza","convivencia","bienestar","participacion","desinformacion","conflictos"};
        int[] valores={ciudad.informacionVerificada(),ciudad.confianza(),ciudad.convivencia(),ciudad.bienestar(),ciudad.participacion(),ciudad.desinformacion(),ciudad.conflictos()};
        for(int i=0;i<7;i++)p.setProperty("ciudad."+campos[i],Integer.toString(valores[i]));
        for(int i=0;i<8;i++)alcaldedigital.compartido.guardado.CamposEstado.escribir(peatones[i],p,"peaton."+i+".");
        if(expedientes!=null) {p.setProperty("investigacion.activa","true");expedientes.guardar(p);}
        for(int i=0;i<cantidadPublicaciones();i++){Publicacion v=publicaciones[i];p.setProperty("post."+i+".riesgo",Integer.toString(v.riesgo()));p.setProperty("post."+i+".estado",v.estado());p.setProperty("consultada."+i,Boolean.toString(consultadas[i]));p.setProperty("resuelta."+i,Boolean.toString(resueltas[i]));p.setProperty("accion."+i,accionesPublicaciones[i]);}
        for(String id:decisiones.recorrer("preorden")){var v=decisiones.buscar(id);p.setProperty("arbol."+id+".texto",v.texto());p.setProperty("arbol."+id+".ejecutado",Boolean.toString(v.ejecutado()));if(!id.equals("mision-1")){var ruta=decisiones.ruta(id);p.setProperty("arbol."+id+".padre",ruta.get(ruta.size()-2));}}
        p.setProperty("nodos",String.join(",",decisiones.recorrer("preorden")));
        p.setProperty("avl.ids",avl.recorrer("inorden").stream().map(v->Long.toString(v.id())).collect(java.util.stream.Collectors.joining(",")));
        solicitudes.forEach((k,v)->p.setProperty("solicitud."+k,v));return p;
    }
    public static SesionMisionUnoLocal restaurar(Properties p){
        if(!"1".equals(p.getProperty("version")))throw new IllegalArgumentException("Versión de misión 1 inválida");
        var s=new SesionMisionUnoLocal(Long.parseLong(p.getProperty("lado")),Dificultad.valueOf(p.getProperty("dificultad")));
        if(Boolean.parseBoolean(p.getProperty("investigacion.activa"))) {
            s.activarInvestigacion();s.expedientes.premium=false;s.expedientes.cargar(p);
        }
        alcaldedigital.compartido.guardado.CamposEstado.leer(s,p,"sesion.");
        String[] campos={"informacionVerificada","confianza","convivencia","bienestar","participacion","desinformacion","conflictos"};int[] n=new int[7];for(int i=0;i<7;i++)n[i]=Integer.parseInt(p.getProperty("ciudad."+campos[i]));s.ciudad=new EstadoCiudad(n[0],n[1],n[2],n[3],n[4],n[5],n[6]);
        for(int i=0;i<8;i++)alcaldedigital.compartido.guardado.CamposEstado.leer(s.peatones[i],p,"peaton."+i+".");
        s.avl=new ArbolAVLPublicaciones();for(int i=0;i<s.cantidadPublicaciones();i++){var v=s.publicaciones[i];s.publicaciones[i]=new Publicacion(v.id(),Integer.parseInt(p.getProperty("post."+i+".riesgo")),v.autor(),v.texto(),p.getProperty("post."+i+".estado"));if(Arrays.asList(p.getProperty("avl.ids", "1,2,3").split(",")).contains(Long.toString(v.id())))s.avl.insertar(s.publicaciones[i]);s.consultadas[i]=Boolean.parseBoolean(p.getProperty("consultada."+i));s.resueltas[i]=Boolean.parseBoolean(p.getProperty("resuelta."+i));s.accionesPublicaciones[i]=p.getProperty("accion."+i,"");}
        String[] ids=p.getProperty("nodos").split(",");s.decisiones=new ArbolDecisiones("mision-1",p.getProperty("arbol.mision-1.texto"));for(String id:ids)if(!id.equals("mision-1"))s.decisiones.insertar(p.getProperty("arbol."+id+".padre"),id,p.getProperty("arbol."+id+".texto"));for(String id:ids)if(Boolean.parseBoolean(p.getProperty("arbol."+id+".ejecutado")))s.decisiones.ejecutar(id);
        for(String k:p.stringPropertyNames())if(k.startsWith("solicitud."))s.solicitudes.put(k.substring(10),p.getProperty(k));
        s.caminando=false;return s;
    }
    public String partida(){return partida;}
    public int burlas(){return burlas;}
    public List<String> decisionesRegistradas(){
        List<String> salida=new ArrayList<>();
        for(int i=0;i<cantidadPublicaciones();i++)salida.add("Publicación "+(i+1)+": "+(consultadas[i]?"contrastada; ":"sin contrastar; ")+
            (accionesPublicaciones[i].isBlank()?"sin decisión":accionesPublicaciones[i]));
        salida.add("Burlas a vecinos: "+burlas);
        return List.copyOf(salida);
    }
    @Override public Vista vista() {
        return new Vista(fase,dificultad,dialogo,new Palomo(ladoPalomo,progresoPalomo,estadoPalomo),
            x,y,direccion,caminando,Math.max(0,plazo-transcurrido),puntos,calmados,
            parejasFuera,mensaje,Arrays.stream(peatones).map(PeatonMutable::vista).toList(),
            List.of(Arrays.copyOf(publicaciones,cantidadPublicaciones())),java.util.stream.IntStream.range(0,cantidadPublicaciones())
                .mapToObj(i->consultadas[i]?FUENTES[i]:"").toList(),
            java.util.stream.IntStream.range(0,cantidadPublicaciones()).mapToObj(i->resueltas[i]).toList(),ciudad,desenlace,
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
        if(expedientes!=null&&fase!=Fase.INTRO&&!minijuegoCalmaActivo()) return resolverInvestigacion(accion);
        if(minijuegoCalmaActivo()&&accion.startsWith("verificar:")) return "Las publicaciones ya fueron comprobadas.";
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
            String[] guion=expedientes==null?INTRO:INTRO_INVESTIGACION;
            if(++dialogo<guion.length) return guion[dialogo];
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
        x=INICIO_X;y=INICIO_Y;direccion=0;caminando=false;
        for(PeatonMutable p:peatones) {
            p.x=p.inicioX;p.y=p.inicioY;p.animo=olaSocial[p.id]==0?Animo.ALTERADO:Animo.NORMAL;
        }
        ciudad=cambiar(0,-4,-5,-4,0,4,5);
    }
    private String resolverInvestigacion(String accion) {
        if(expedientes.escena!=Escena.NINGUNA) {
            if(accion.equals("saltarEscena")&&(!expedientes.premium||expedientes.escena==Escena.CIERRE)) {avanzarEscena();return mensaje;}
            return "La historia continúa en el parque.";
        }
        String[] partes=accion.split(":");
        try {
            return switch(partes[0]) {
                case "papel" -> abrirPapel(Integer.parseInt(partes[1]));
                case "cerrarExpediente" -> cerrarExpediente();
                case "lupa" -> cambiarLupa();
                case "hallazgo" -> registrarHallazgo(Zona.valueOf(partes[1]));
                case "publicacion" -> abrirPublicacion();
                case "comunicar" -> prepararAviso();
                case "presentar" -> presentarAviso();
                case "contraste" -> expedientes.premium?"La lupa reúne las pruebas del expediente.":abrirContraste();
                case "comprobar" -> expedientes.premium?"Prepara el aviso a partir de tus hallazgos.":comprobar(Integer.parseInt(partes[1]));
                case "inspeccion" -> volverAlPeriodico();
                case "compartir","reportar","ignorar" -> expedientes.premium?"Utiliza la página de publicación.":publicarDecision(partes[0],Integer.parseInt(partes[1]));
                case "irVecinos" -> convocarVecinos();
                case "vecino" -> abordarVecino(Integer.parseInt(partes[1]));
                case "calmar" -> calmarVecino(Integer.parseInt(partes[1]));
                case "mostrar" -> expedientes.premium?"Presenta el aviso preparado.":orientarVecino(Integer.parseInt(partes[1]));
                default -> "Acércate a un papel o a un vecino para interactuar.";
            };
        } catch(IllegalArgumentException|IndexOutOfBoundsException e) {
            return "Esta opción no pertenece al expediente.";
        }
    }
    private String abrirPapel(int id) {
        var caso=caso(id);
        if(fase!=Fase.CIVITAS||caso==null||Math.hypot(x-caso.x(),y-caso.y())>29)
            return "Acércate al periódico para investigarlo.";
        if(id!=1&&!(expedientes.premium?resueltas[0]:expedientes.tutorial())) return "Empieza por el rumor del agua, junto a la banca.";
        expedientes.abrir(id);caminando=false;
        return "Periódico abierto. Activa la lupa para revisar los detalles.";
    }
    private String cerrarExpediente() {
        expedientes.cerrar();
        if(fase==Fase.CRISIS&&calmados==VECINOS.size()&&!expedientes.premium) {
            for(PeatonMutable p:peatones) if(!expedientes.orientado(p.id)) p.animo=Animo.NORMAL;
            expedientes.escena(Escena.CIERRE);
        }
        return fase==Fase.CRISIS?"Habla con los vecinos que tienen dudas.":"Sigue buscando pistas en el parque.";
    }
    private String cambiarLupa() {
        if(expedientes.abierto==0||expedientes.contraste||expedientes.publicacion) return "Abre el periódico para usar la lupa.";
        expedientes.lupa=!expedientes.lupa;
        return expedientes.lupa?"Lupa activa. Examina los detalles del periódico.":"Lupa guardada.";
    }
    private String comentario(String texto) {
        expedientes.respuesta=texto;
        return texto;
    }
    private String registrarHallazgo(Zona zona) {
        Caso caso=caso(expedientes.abierto);
        if(caso==null||!expedientes.lupa||expedientes.contraste||expedientes.publicacion) return "Activa la lupa para registrar un hallazgo.";
        var hallazgo=caso.hallazgo(zona);
        if(hallazgo==null) return comentario("Ese detalle no ayuda a resolver este caso. Explora otra parte del periódico.");
        if(expedientes.marcar(caso,zona)) {
            puntos+=2;
            if(expedientes.premium&&expedientes.evidenciaCompleta(caso)) concluir(caso);
        }
        return comentario(hallazgo.explicacion());
    }
    private String abrirContraste() {
        Caso caso=caso(expedientes.abierto);
        if(caso==null||!expedientes.evidenciaCompleta(caso)) return "Registra todas las pistas útiles antes de contrastar.";
        expedientes.lupa=false;expedientes.contraste=true;
        expedientes.mostrarFuentes(caso);
        return comentario("");
    }
    private String volverAlPeriodico() {
        if(expedientes.abierto==0) return "Primero abre un periódico.";
        expedientes.contraste=false;
        expedientes.publicacion=false;
        return comentario("");
    }
    private String abrirPublicacion() {
        Caso caso=caso(expedientes.abierto);
        if(!expedientes.premium||caso==null||!expedientes.completo(caso.id()))
            return "Termina de revisar los detalles con la lupa.";
        expedientes.lupa=false;expedientes.publicacion=true;
        return comentario("");
    }
    private String prepararAviso() {
        Caso caso=caso(expedientes.abierto);
        if(!expedientes.premium||!expedientes.publicacion||caso==null)
            return "Abre la página de publicación.";
        String accion=switch(caso.veredicto()) {
            case VERDADERA,ENGANOSA -> "compartir";
            case FALSA -> "reportar";
            case NO_VERIFICADA -> "ignorar";
        };
        if(resueltas[caso.id()-1]) return "El aviso ya está preparado.";
        publicarDecision(accion,caso.id());
        if(caso.veredicto()==Veredicto.NO_VERIFICADA) {
            puntos+=3;
            ciudad=cambiar(2,1,0,0,0,-2,0);
        }
        return comentario(aviso(caso));
    }
    private String presentarAviso() {
        var vecino=vecino(expedientes.vecino);
        if(!expedientes.premium||vecino==null||!resueltas[vecino.caso()-1])
            return "Prepara el aviso para este vecino.";
        if(!vecinoAlAlcance(vecino.id())) return "Vuelve a acercarte a "+vecino.nombre()+".";
        String respuesta=orientarVecino(vecino.caso());
        if(calmados==VECINOS.size()) expedientes.escena(Escena.CIERRE);
        return respuesta;
    }
    private String calmarVecino(int id) {
        if(!expedientes.premium||!vecinoAlAlcance(id)) return "Acércate a un vecino preocupado.";
        PeatonMutable p=peatones[id];
        boolean escuchado=p.conversaciones>0||expedientes.vecino==id;
        String pregunta=abordarVecino(id);
        p.conversaciones=1;
        return escuchado?presentarAviso():pregunta;
    }
    private String comprobar(int eleccion) {
        Caso caso=caso(expedientes.abierto);
        if(caso==null||!expedientes.contraste||!expedientes.evidenciaCompleta(caso))
            return comentario("Abre el contraste para comparar el titular con la fuente.");
        if(expedientes.completo(caso.id())) return caso.conclusion();
        expedientes.mostrarFuentes(caso);
        if(eleccion!=caso.razonCorrecta()) {
            return comentario(switch(caso.id()) {
                case 1 -> "El aviso explica la limpieza, pero no mide el agua. ¿Puedes probar o refutar la contaminación?";
                case 2 -> "Compara el lugar y la fecha con el anuncio de la agenda municipal.";
                case 3 -> "La frase es la misma. Revisa qué cambió al quitar el encabezado del original.";
                default -> "La afirmación se refiere a la agenda. Busca si la entrada existe en la edición correcta.";
            });
        }
        concluir(caso);
        return comentario("Clasificación sustentada. "+caso.conclusion());
    }
    private void concluir(Caso caso) {
        expedientes.mostrarFuentes(caso);
        expedientes.clasificar(caso);
        int i=caso.id()-1;
        consultadas[i]=true;decisiones.ejecutar("verificar-"+caso.id());
        reindexar(i,Math.max(5,publicaciones[i].riesgo()-25),caso.veredicto().etiqueta);
        ciudad=cambiar(5,2,0,0,2,-4,-1);puntos+=10;
    }
    private String publicarDecision(String accion,int id) {
        Caso caso=caso(id);
        if(fase!=Fase.CIVITAS||caso==null||!expedientes.completo(id)) return "Primero sustenta la clasificación.";
        int i=id-1;
        if(resueltas[i]) return "La decisión de difusión ya está registrada.";
        resueltas[i]=true;accionesPublicaciones[i]=accion+" con contexto";
        decisiones.ejecutar(accion+"-"+id);
        if(accion.equals("compartir")) {
            ciudad=cambiar(3,2,1,0,2,-3,0);puntos+=3;
            return comentario("Compartiste tu explicación con la fuente y el contexto.");
        }
        if(accion.equals("reportar")&&caso.veredicto()!=Veredicto.VERDADERA) {
            avl.eliminar(id);ciudad=cambiar(2,1,0,0,0,-2,0);puntos+=3;
            return comentario("Registraste un reporte sustentado con lo que demuestra la fuente.");
        }
        return comentario("La publicación no se difundió desde tu cuenta.");
    }
    private String convocarVecinos() {
        if(fase!=Fase.CIVITAS||expedientes.clasificados()!=CASOS.size())
            return "Clasifica los cuatro periódicos antes de orientar a los vecinos.";
        if(expedientes.premium) {
            for(boolean preparada:resueltas) if(!preparada) return "Prepara los cuatro avisos antes de convocar.";
            if(Math.hypot(x-TABLON_X,y-TABLON_Y)>ALCANCE_TABLON) return "Acércate al tablón junto a la fuente.";
            for(var vecino:VECINOS) peatones[vecino.id()].tramo=0;
        }
        fase=Fase.CRISIS;caminando=false;expedientes.calmaClasica=!expedientes.premium;expedientes.escena(Escena.VECINOS);
        return "Los titulares ya recorren el parque.";
    }
    private String abordarVecino(int id) {
        var vecino=vecino(id);
        if(!vecinoAlAlcance(id)) return "Acércate a un vecino que tenga una duda.";
        expedientes.cerrar();expedientes.vecino=id;caminando=false;
        return vecino.pregunta();
    }
    private boolean vecinoAlAlcance(int id) {
        return fase==Fase.CRISIS&&vecino(id)!=null&&!expedientes.orientado(id)&&
            Math.hypot(x-peatones[id].x,y-peatones[id].y)<=38;
    }
    private String orientarVecino(int prueba) {
        var vecino=vecino(expedientes.vecino);
        if(fase!=Fase.CRISIS||vecino==null||expedientes.orientado(vecino.id())) return "Abre una conversación para mostrar una evidencia.";
        if(prueba!=vecino.caso()||!expedientes.completo(prueba))
            return comentario(prueba==0?"Repetir el titular no resuelve mi duda. ¿Me muestras una fuente?":
                "Ese documento trata de otra pregunta. ¿Puedes mostrarme la prueba que aclara mi duda?");
        expedientes.orientar(vecino.id());peatones[vecino.id()].animo=Animo.CALMADO;
        calmados++;puntos+=14;ciudad=cambiar(2,3,4,2,2,-3,-3);
        return comentario(vecino.reaccion());
    }
    private void avanzarEscena() {
        Escena anterior=expedientes.escena;
        expedientes.escena(Escena.NINGUNA);
        if(anterior==Escena.VECINOS) {
            if(expedientes.calmaClasica) {
                iniciarCrisis();
                mensaje="Empieza el minijuego. Acércate a los vecinos alterados y escúchalos con E.";
            } else {
                for(var vecino:VECINOS) peatones[vecino.id()].animo=Animo.ALTERADO;
                if(expedientes.premium) transcurrido=0;
                mensaje=expedientes.premium?"Escucha a cada vecino y vuelve a interactuar para calmarlo con su aviso.":
                    "Acércate a los cuatro vecinos y muéstrales pruebas de tu libreta.";
            }
        } else if(anterior==Escena.CIERRE) {
            fase=Fase.RESULTADO;
            if(!expedientes.calmaClasica) {
                boolean completa=!expedientes.premium||calmados==VECINOS.size();
                desenlace=completa?"BUENO":"NEUTRAL";
                mensaje=completa?"Sofi: Hoy verificamos juntos. Antes de compartir, podemos preguntar, escuchar y buscar la fuente.":
                    "Se acabó el tiempo. Sofi calmó a "+calmados+" de "+VECINOS.size()+" vecinos.";
            }
        }
    }
    private boolean minijuegoCalmaActivo() {
        return expedientes!=null&&expedientes.calmaClasica&&fase==Fase.CRISIS&&expedientes.escena==Escena.NINGUNA;
    }
    private void actualizarInvestigacion(double segundos) {
        if(expedientes.escena!=Escena.NINGUNA) {
            expedientes.segundosEscena+=segundos;
            if(expedientes.premium&&expedientes.escena==Escena.VECINOS) {
                if(caminarAlTablon(segundos)&&expedientes.segundosEscena>=DURACION_CONVOCATORIA) avanzarEscena();
                return;
            }
            if(expedientes.escena==Escena.VECINOS) for(PeatonMutable p:peatones)
                if(expedientes.segundosEscena>=.35+olaSocial[p.id]*.6) p.animo=Animo.ALTERADO;
            if(expedientes.segundosEscena>=expedientes.escena.segundos) avanzarEscena();
        } else if(expedientes.premium&&fase==Fase.CRISIS) {
            actualizarCalma(segundos);
        }
        if(fase==Fase.RESULTADO||expedientes.abierto!=0||!expedientes.premium&&expedientes.vecino!=-1) return;
        for(PeatonMutable p:peatones) {
            if(p.animo==Animo.CALMADO||expedientes.premium&&fase==Fase.CRISIS&&vecino(p.id)!=null) continue;
            float nuevoX=p.inicioX+(float)Math.sin(animacion*.85+p.id)*8;
            p.direccion=nuevoX>=p.x?2:1;p.x=nuevoX;
        }
    }
    private void actualizarCalma(double segundos) {
        // También cierra con victoria si se reanuda un guardado con el último aviso ya presentado.
        if(calmados==VECINOS.size()) {
            expedientes.escena(Escena.CIERRE);
            return;
        }
        transcurrido=Math.min(plazo,transcurrido+segundos);
        if(plazo-transcurrido<=1e-8) {
            transcurrido=plazo;
            expedientes.escena(Escena.CIERRE);
            mensaje="Se acabó el tiempo.";
            ciudad=cambiar(0,-2,-2,-1,0,2,2);
        }
    }
    // Los vecinos cruzan por los caminos al norte y al este de la fuente.
    private static final int[][][] CAMINOS_TABLON={
        {{349,150},{389,167},{392,195}},
        {{425,269},{415,234}},
        {{419,355},{421,314},{425,274}},
        {{203,163},{349,154},{379,170},{373,254}}
    };
    private boolean caminarAlTablon(double segundos) {
        boolean reunidos=true;
        for(int i=0;i<VECINOS.size();i++) {
            PeatonMutable p=peatones[VECINOS.get(i).id()];
            int[][] camino=CAMINOS_TABLON[i];
            if(p.tramo>=camino.length) continue;
            reunidos=false;
            if(expedientes.segundosEscena<.55+i*.19) continue;
            int[] destino=camino[p.tramo];
            double dx=destino[0]-p.x,dy=destino[1]-p.y,distancia=Math.hypot(dx,dy);
            double paso=Math.min(1,58*segundos/Math.max(.001,distancia));
            float nx=(float)(p.x+dx*paso),ny=(float)(p.y+dy*paso);
            if(transitable(nx,ny)) {
                p.direccion=Math.abs(dx)>Math.abs(dy)?(dx>0?2:1):(dy>0?0:3);
                p.animo=Animo.SALIENDO;p.x=nx;p.y=ny;
                if(paso>=1&&++p.tramo==camino.length) p.animo=Animo.ALTERADO;
            }
        }
        return reunidos;
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
        if((fase!=Fase.CRISIS&&fase!=Fase.CIVITAS)||!Float.isFinite(dx)||!Float.isFinite(dy)) return;
        if(expedientes!=null&&(expedientes.escena!=Escena.NINGUNA||expedientes.abierto!=0||!expedientes.premium&&expedientes.vecino!=-1)) return;
        float largo=(float)Math.hypot(dx,dy);
        if(largo>7) {dx*=7/largo;dy*=7/largo;}
        float nx=Math.max(22,Math.min(618,x+dx)),ny=Math.max(22,Math.min(458,y+dy));
        if(transitable(nx,ny)) {x=nx;y=ny;}
        else if(expedientes!=null&&!minijuegoCalmaActivo()) {
            if(transitable(nx,y)) x=nx;
            if(transitable(x,ny)) y=ny;
        }
        if(dx!=0||dy!=0) {direccion=Math.abs(dx)>Math.abs(dy)?(dx>0?2:1):(dy>0?0:3);caminando=true;}
        else caminando=false;
    }
    private boolean transitable(float px,float py) {
        boolean mapaClasico=expedientes==null||minijuegoCalmaActivo();
        if(Math.hypot(px-320,py-(mapaClasico?240:229))<=(mapaClasico?45:47)||enEstanque(px,py)) return false;
        return mapaClasico||!(dentro(px,py,124,86,172,108)||dentro(px,py,238,286,278,309)||
            dentro(px,py,360,286,401,309)||dentro(px,py,64,121,80,141)||
            dentro(px,py,563,169,584,188)||dentro(px,py,480,328,500,350));
    }
    private static boolean dentro(float px,float py,int izquierda,int arriba,int derecha,int abajo) {
        return px>=izquierda&&px<=derecha&&py>=arriba&&py<=abajo;
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
                    mensaje="Sofi se levanta. Busca en el parque las pistas de lo que circula en Civitas.";
                    if(expedientes!=null) expedientes.escena(Escena.RUMORES);
                }
            }
            for(PeatonMutable p:peatones) {
                float nx=p.inicioX+(float)Math.sin(animacion*.65+p.id)*10;
                p.direccion=nx>=p.x?2:1;p.x=nx;
            }
            return;
        }
        if(expedientes!=null&&!minijuegoCalmaActivo()) {actualizarInvestigacion(segundos);return;}
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
        if(expedientes!=null&&expedientes.calmaClasica&&"BUENO".equals(desenlace)) {
            fase=Fase.CRISIS;
            expedientes.escena(Escena.CIERRE);
        }
    }
}
