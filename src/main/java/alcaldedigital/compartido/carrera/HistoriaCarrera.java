package alcaldedigital.compartido.carrera;

import java.util.*;
import alcaldedigital.compartido.carrera.EstadoCarrera.*;

public final class HistoriaCarrera {
    private HistoriaCarrera(){}
    public record Linea(String quien,String texto){}
    public static List<Linea> dialogo(Fase f,int ganador){return switch(f){
        case ANUNCIO->List.of(new Linea("VOZ MISTERIOSA","Noticias de última hora. Por razones que nadie comprende… digo, completamente oficiales, adelantamos el anuncio electoral."),
            new Linea("VOZ MISTERIOSA","Esperamos sus votos ahora. Al ganador se le corona… digo, se le posesiona hoy mismo."),
            new Linea("CIVITAS","Apoyos provisionales. Compartir una publicación no equivale a votar. El recuento definitivo sigue pendiente."));
        case RIVALIDAD->List.of(new Linea("AMBOS","Esta noticia tiene que ser mía."),new Linea("PERIODISTA","Adelantar unas elecciones no es cualquier cosa. Necesito saber quién autorizó esto."),new Linea("INFLUENCER","Si llego primero, todo Ciudad Nova va a estar viendo mi transmisión."));
        case SALIDA->List.of(new Linea("CIUDADANO","¿Ahora qué pasó?"),new Linea("PERIODISTA","Si te lo explico, alguien va a publicarlo antes que yo."),new Linea("INFLUENCER","Batería cargada, cámara lista… criterio periodístico, luego vemos."));
        case ENCUENTRO->List.of(new Linea("PERIODISTA","¡Mira por dónde vas!"),new Linea("INFLUENCER","Estoy mirando dónde está la noticia."),new Linea("PERIODISTA","Pues acabas de atropellar a la competencia."),
            new Linea("MR. PALOMO","Uy, qué interesante. Dos personas informando y ninguna escuchando."),new Linea("INFLUENCER","¿Sabes dónde será el anuncio?"),new Linea("MR. PALOMO","En el parque. Pero sigan discutiendo; seguro la noticia los espera."),new Linea("PERIODISTA","Yo llego primero."),new Linea("MR. PALOMO","Eso quiero verlo."));
        case LLEGADA->List.of(new Linea(ganador==0?"PERIODISTA":ganador==1?"INFLUENCER":"AMBOS",ganador==2?"Llegamos juntos. La noticia es…":"Primero. La noticia es…"),new Linea("AMBOS","Pero… ¿qué es esto?"),new Linea("PANTALLA","COMPARTE PARA CONFIRMAR AL PRÓXIMO ALCALDE."),new Linea("CIUDADANO","Si apareció en Civitas… ¿ya está elegido?"));
        case EPILOGO->List.of(new Linea("PERIODISTA","Están confundiendo compartir una publicación con votar."),new Linea("INFLUENCER","Y nosotros veníamos compitiendo por transmitirla primero."),new Linea("PERIODISTA","Conservemos las fuentes y los mensajes. Hay que averiguar quién organizó esta transmisión."),new Linea("CIVITAS","La elección válida sigue pendiente. Los apoyos y antecedentes de la campaña se conservan para la misión 8."),new Linea("INFLUENCER","Entonces esta vez investigamos antes de pulsar publicar."));
        default->List.of();};}
    public static String titulo(Noticia t){return switch(t){case ATAJO->"¿Un atajo al parque?";case RUMOR->"Una acusación contra tu rival";case PACTO->"Un pacto oculto con la campaña de Juan";case RESULTADO->"¿Compartir confirma al alcalde?";};}
    public static String cuerpo(Evento e){return switch(e.tipo()){
        case ATAJO->"Una cuenta de "+EstadoElectoral.NOMBRES[e.candidato()]+" anuncia un atajo al parque. La ruta todavía no está contrastada.";
        case RUMOR->"Una cuenta afín a "+EstadoElectoral.NOMBRES[e.candidato()]+" acusa a tu rival de manipular la noticia. No aporta pruebas.";
        case PACTO->e.advertencia()?"Confirmar ejecuta un pago oculto y un montaje. Juan queda corrupto: NO podrá ganar. Rechaza para cancelar.":"Ofrecen un pago oculto a favor de Juan. Aceptarlo exige confirmación y lo excluye de la elección.";
        case RESULTADO->"Una cuenta que dice apoyar a "+EstadoElectoral.NOMBRES[e.candidato()]+" pide compartir para confirmarlo alcalde. Falta cerrar el recuento.";};}
    public static String[] opciones(Evento e){return switch(e.tipo()){
        case ATAJO->new String[]{"Contrastar la ruta","Seguir el mensaje","Continuar por la avenida"};
        case RUMOR->new String[]{"Investigar y aclarar","Difundir para distraer","Ignorar la acusación"};
        case PACTO->new String[]{"Rechazar el pacto",e.advertencia()?"Confirmar pacto corrupto":"Leer condiciones del pacto","Continuar sin aceptar"};
        case RESULTADO->new String[]{"Exigir fuente y aclarar","Difundir como definitivo","Guardar para investigar"};};}
}
