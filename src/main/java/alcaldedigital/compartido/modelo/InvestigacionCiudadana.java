package alcaldedigital.compartido.modelo;

import java.util.List;

/** Datos del periódico, materiales de contraste y vista de la libreta de Sofi. */
public final class InvestigacionCiudadana {
    private InvestigacionCiudadana() { }

    public enum Veredicto {
        VERDADERA("Verdadera"), FALSA("Falsa"), ENGANOSA("Fuera de contexto"), NO_VERIFICADA("No verificada");
        public final String etiqueta;
        Veredicto(String etiqueta) {this.etiqueta=etiqueta;}
    }
    public enum Zona {
        TITULAR("Titular"), IMAGEN("Imagen"), EDICION("Edición"), FUENTES("Fuentes");
        public final String etiqueta;
        Zona(String etiqueta) {this.etiqueta=etiqueta;}
    }
    public enum Escena {
        NINGUNA(0), RUMORES(4.6), VECINOS(3.8), CIERRE(4.2);
        public final double segundos;
        Escena(double segundos) {this.segundos=segundos;}
    }
    public record Hallazgo(Zona zona,String detalle,String explicacion) { }
    public record Documento(String nombre,String detalle) { }
    public record Caso(int id,int x,int y,String lugar,String nombre,String titular,String edicion,
                       String pieFoto,String credito,String nota,List<Hallazgo> hallazgos,
                       List<Documento> documentos,Veredicto veredicto,List<String> razones,
                       int razonCorrecta,String conclusion) {
        public Hallazgo hallazgo(Zona zona) {
            return hallazgos.stream().filter(h->h.zona()==zona).findFirst().orElse(null);
        }
        public int mascaraHallazgos() {
            return hallazgos.stream().mapToInt(h->1<<h.zona().ordinal()).reduce(0,(a,b)->a|b);
        }
    }
    public record Vecino(int id,int caso,String nombre,String pregunta,String prueba,String reaccion) { }
    public record Ficha(int hallazgos,int documentos,boolean clasificada) {
        public boolean encontro(Zona zona) {return (hallazgos&(1<<zona.ordinal()))!=0;}
        public boolean leyo(int indice) {return (documentos&(1<<indice))!=0;}
        public int encontrados() {return Integer.bitCount(hallazgos);}
    }
    public record Vista(List<Ficha> fichas,int abierto,int vecino,boolean lupa,boolean contraste,
                        Escena escena,double segundosEscena,int orientados,String respuesta,int ultimoHallazgo,
                        boolean calmaClasica,boolean premium,boolean publicacion) {
        public Ficha ficha(int id) {return fichas.get(id-1);}
        public int clasificados() {return (int)fichas.stream().filter(Ficha::clasificada).count();}
        public int evidencias() {return fichas.stream().mapToInt(Ficha::encontrados).sum();}
        public boolean orientado(int id) {return (orientados&(1<<id))!=0;}
        public int ciudadanos() {return Integer.bitCount(orientados);}
    }

    public static final List<Caso> CASOS=List.of(
        new Caso(1,229,123,"Junto a la banca noroeste","El rumor del agua",
            "¡La fuente está contaminada! Hay que cerrarla ahora.","Civitas · Cuenta Anónima · Hoy",
            "La fuente de Ciudad Nova.","Origen del dato: sin enlace ni informe.",
            "La publicación pide cerrar la fuente basándose en una acusación sin atribución.",
            List.of(new Hallazgo(Zona.TITULAR,"«Está contaminada»: afirmación categórica.",
                    "Un titular urgente puede llamar la atención. La urgencia no prueba lo que afirma."),
                new Hallazgo(Zona.FUENTES,"No se adjunta un informe ni una persona responsable.",
                    "Sin un origen comprobable, falta evidencia para afirmar algo sobre el agua.")),
            List.of(new Documento("Aviso de la administración del parque",
                "Aviso de hoy: jornada de limpieza del parque. El aviso no informa de contaminación ni aporta un análisis del agua.")),
            Veredicto.NO_VERIFICADA,
            List.of("El aviso de limpieza demuestra que el agua es segura.","Falta un informe sobre el agua; el aviso habla de limpieza.","La urgencia del titular confirma el peligro."),
            1,"El aviso explica la actividad del parque. La afirmación sobre el agua queda sin verificar."),
        new Caso(2,472,219,"En el camino este","La jornada de limpieza",
            "Hoy habrá una jornada de limpieza en el parque.","Civitas · Agenda municipal · Edición de hoy",
            "El parque, lugar de la jornada.","Agenda municipal de Ciudad Nova · Actividades de hoy.",
            "La nota informa de una actividad anunciada para esta tarde.",
            List.of(new Hallazgo(Zona.EDICION,"Edición de hoy, no un aviso de otra fecha.",
                    "Para confirmar un evento importa que la fecha del documento coincida con la del anuncio."),
                new Hallazgo(Zona.FUENTES,"La publicación identifica la agenda municipal.",
                    "Una fuente identificada se puede consultar. Todavía hay que comprobar qué dice.")),
            List.of(new Documento("Agenda municipal · Actividades de hoy",
                "Ciudad Nova. Hoy por la tarde: jornada de limpieza en el parque. El lugar y la fecha coinciden con lo publicado.")),
            Veredicto.VERDADERA,List.of("La agenda de hoy confirma el evento y el lugar.","Todo anuncio municipal es verdadero sin revisarlo.","La foto del parque demuestra la fecha."),
            0,"La fuente oficial y vigente respalda que la jornada está anunciada para hoy."),
        new Caso(3,186,226,"En el camino oeste","La frase de las palomas",
            "Hay más palomas que votantes en Ciudad Nova.","Reenvío vecinal · Publicado hoy",
            "Captura recortada: se ve la frase, pero no la cabecera.","Reenvío de una publicación del barrio.",
            "El reenvío presenta como dato una frase cuyo encabezado quedó fuera de la captura.",
            List.of(new Hallazgo(Zona.IMAGEN,"El recorte empieza debajo del encabezado.",
                "Un recorte puede conservar las palabras y eliminar la información que explica cómo leerlas."),
                new Hallazgo(Zona.EDICION,"La copia dice «Reenvío vecinal» y omite la sección original.",
                "Ver una copia no permite distinguir una broma de un dato hasta consultar su contexto.")),
            List.of(new Documento("Publicación completa · Chiste del barrio",
                "Encabezado original: CHISTE DEL BARRIO · Humor. Debajo aparece la misma frase sobre las palomas, sin censo ni dato electoral.")),
            Veredicto.ENGANOSA,List.of("La foto permite contar todos los votantes.","La frase cambió de significado al perder la etiqueta de humor.","La cantidad es cierta porque se repite mucho."),
            1,"El reenvío usa una frase real, pero le quita la sección de humor y la presenta como estadística."),
        new Caso(4,320,361,"En el sendero sur","El boletín de la agenda",
            "La agenda municipal de hoy no incluye ninguna limpieza.","Boletín reenviado · Ciudad Nova · Hoy",
            "Imagen de la fuente, sin mostrar la agenda.","La publicación asegura haber revisado la edición de hoy.",
            "El boletín usa la limpieza del parque para afirmar que no hay ningún anuncio oficial.",
            List.of(new Hallazgo(Zona.TITULAR,"Afirma que el evento no figura en la agenda, no que ya ocurrió.",
                    "Antes de buscar pruebas, identifica qué afirmación exacta vas a comprobar."),
                new Hallazgo(Zona.EDICION,"Dice «hoy» y se refiere a Ciudad Nova.",
                    "Comparar con otra ciudad o fecha no resolvería este caso."),
                new Hallazgo(Zona.FUENTES,"Menciona la agenda, pero no reproduce la entrada.",
                    "Una referencia puede comprobarse consultando el documento citado y otra fuente pertinente.")),
            List.of(new Documento("Agenda municipal · Edición de hoy","Ciudad Nova. Entrada vigente: jornada de limpieza del parque, esta tarde."),
                new Documento("Aviso del parque · Convocatoria","La administración del parque publica hoy el aviso de la misma jornada de limpieza. Fecha y lugar coinciden con la agenda.")),
            Veredicto.FALSA,List.of("La imagen demuestra que la jornada ya ocurrió.","El boletín no se puede refutar sin ver quién lo escribió.","La agenda de hoy sí incluye la limpieza y el aviso coincide."),
            2,"La entrada oficial contradice directamente la afirmación. El aviso confirma que es la jornada del mismo parque y la misma fecha.")
    );
    public static final List<Vecino> VECINOS=List.of(
        new Vecino(0,1,"Álex","¿Tengo que decirle a mi familia que el agua está contaminada?",
            "Aviso de limpieza y falta de un informe del agua","Entiendo. El aviso habla de limpieza. Voy a esperar pruebas sobre el agua."),
        new Vecino(3,3,"Nico","Vi dos versiones de la frase de las palomas. ¿Puedes mostrarme la publicación completa?",
            "Publicación completa con su sección de humor","¡Ahora veo el encabezado! Compartí un chiste como si fuera una cifra."),
        new Vecino(4,4,"Lina","El boletín dice que no hay limpieza en la agenda. ¿Qué dicen los documentos de hoy?",
            "Agenda vigente y aviso del mismo parque","Las dos fuentes coinciden. Voy a corregir lo que dije sobre la agenda."),
        new Vecino(7,2,"Doña Luz","Quiero ir a la jornada. ¿Dónde puedo confirmar que es esta tarde?",
            "Agenda municipal de hoy con fecha y lugar","¡Gracias! Ahora sé dónde consultar la hora y el lugar.")
    );

    public static final double DURACION_CONVOCATORIA=6.65;
    public static final int TABLON_X=392, TABLON_Y=247, ALCANCE_TABLON=29;

    public static String accionAviso(Caso caso) {
        return switch(caso.veredicto()) {
            case VERDADERA -> "Publicar información verificada";
            case FALSA -> "Publicar el desmentido";
            case ENGANOSA -> "Publicar la aclaración";
            case NO_VERIFICADA -> "Guardar aviso y esperar pruebas";
        };
    }
    public static String selloAviso(Caso caso) {
        return switch(caso.veredicto()) {
            case VERDADERA -> "INFORMACIÓN VERIFICADA";
            case FALSA -> "RUMOR DESMENTIDO";
            case ENGANOSA -> "CONTEXTO RESTAURADO";
            case NO_VERIFICADA -> "DIFUSIÓN EN ESPERA";
        };
    }
    public static String aviso(Caso caso) {
        return switch(caso.id()) {
            case 1 -> "El aviso de la fuente anuncia una limpieza. Todavía falta un análisis del agua. Esperemos ese informe antes de compartir la acusación.";
            case 2 -> "La agenda municipal de Ciudad Nova anuncia una jornada de limpieza esta tarde en el parque.";
            case 3 -> "La frase sobre las palomas era un chiste del barrio. El reenvío cortó el encabezado de humor. La publicación original nunca presentó una cifra electoral.";
            case 4 -> "El boletín decía que la agenda no anunciaba una limpieza. La entrada de hoy y el aviso del parque confirman que la jornada será esta tarde.";
            default -> throw new IllegalArgumentException("Expediente desconocido");
        };
    }
    public static Caso caso(int id) {return id>=1&&id<=CASOS.size()?CASOS.get(id-1):null;}
    public static Vecino vecino(int id) {return VECINOS.stream().filter(v->v.id()==id).findFirst().orElse(null);}
    public static int totalEvidencias() {return CASOS.stream().mapToInt(c->c.hallazgos().size()).sum();}
}
