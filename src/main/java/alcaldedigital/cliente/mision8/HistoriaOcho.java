package alcaldedigital.cliente.mision8;

import java.util.List;
import alcaldedigital.compartido.carrera.EstadoElectoral;

/** Guion secuencial. Cada intervención tiene un interlocutor y una acción escénica. */
public final class HistoriaOcho {
 private HistoriaOcho(){}
 public enum Accion { HABLAR, CONSTRUIR, APARECER, DESAPARECER, VUELO, INYECTAR, RETIRARSE, MUTAR, ACERCARSE, RECUPERAR, ESCAPAR, DISPARAR }
 public record Linea(String quien,String texto,Accion accion,int duracion,int expresion){
  public Linea(String quien,String texto){this(quien,texto,Accion.HABLAR,0,0);}
 }
 private static Linea l(String q,String t,Accion a,int d){return new Linea(q,t,a,d,0);}
 public static List<Linea> lineas(MotorOcho m){
  String candidato=EstadoElectoral.NOMBRES[Math.max(0,m.ganador)];
  return switch(m.escena){
   case OBRA->List.of(new Linea("PERIODISTA","Llegamos al parque. ¿Por qué están preparando una tarima?"),new Linea("INFLUENCER","¡Una nave! Voy a grabar lo que está pasando."),l("CIVITAS","Los constructores llegan entre martillos y humo. Preparan una tarima para las cuatro candidaturas.",Accion.CONSTRUIR,240),new Linea("SOFI","¡Esperen! También tengo algo que decir sobre estas elecciones."));
   case REVELACION->List.of(l("CIVITAS","¡POF! La plaza se llena de personas pendientes de sus celulares.",Accion.APARECER,90),new Linea("SOFI","Palomo, tú eres el culpable de todo lo que está ocurriendo."),new Linea("PERIODISTA","¿Tú organizaste los rumores y el anuncio adelantado?"),new Linea("INFLUENCER","¡Entonces nos hiciste correr detrás de una noticia manipulada!"),new Linea("MR. PALOMO","¿No era obvio? Pero eso ahora no importa. ¡Que comiencen las elecciones!"),new Linea("PERIODISTA","Compartir una publicación no es votar. Antes de elegir, revisemos las pruebas."));
   case ESCRUTINIO->List.of(new Linea("MR. PALOMO","¡El anuncio de Civitas da como ganador a "+EstadoElectoral.NOMBRES[Math.max(0,m.publicado)]+"!"),new Linea("PERIODISTA",m.publicado==m.ganador?"El recuento coincide con el anuncio. Conservaremos las evidencias de la elección.":"¡Ese anuncio no coincide con el recuento! Las pruebas revelan una proclamación falsa."),new Linea("SOFI","El escrutinio reconoce a "+candidato+". La elección depende del respaldo ciudadano y de los votos válidos."),new Linea(candidato,"Gracias a quienes confiaron en mí. Ahora debemos responderle a la ciudad."));
   case MUTACION->List.of(new Linea(candidato,m.ganador==0?"Haremos realidad nuestras promesas para esta ciudad.":"Pues bien, ya gané. Ahora comienza mi gobierno."),new Linea("MR. PALOMO","Sus deseos son órdenes… Aunque yo tengo mis propios planes."),l("MR. PALOMO","Dejemos a los votantes fuera de esto…",Accion.DESAPARECER,90),l("CIVITAS","Palomo levanta el vuelo y se acerca al candidato elegido con un inyector viral.",Accion.VUELO,120),l(candidato,"¿Qué llevas ahí? ¡Aléjate!",Accion.INYECTAR,105),l("MR. PALOMO","Este virus convertirá tu victoria en mi control sobre Civitas.",Accion.RETIRARSE,90),l(candidato,"No… puedo… controlarlo. ¡Mejor destruyámoslo todo!",Accion.MUTAR,150));
   case EPILOGO->List.of(l("SOFI","La gente está a salvo. ¡Acérquense, todavía podemos ayudarlo!",Accion.ACERCARSE,100),new Linea("PERIODISTA","Palomo intervino al candidato. Lo tenemos registrado y conservaremos las pruebas."),new Linea("INFLUENCER",m.ganador==0?"No dejes que el virus decida por ti. Recuerda lo que prometiste a esta ciudad.":"Estas elecciones estuvieron como que ratitas… Pero todavía podemos arreglar esto."),l(candidato,"Puedo escucharlos… El virus está perdiendo fuerza. Haré lo mejor que pueda por esta ciudad.",Accion.RECUPERAR,120),l("MR. PALOMO","Bueno, a la próxima me apodero de la ciudad. ¡Hasta nunca!",Accion.ESCAPAR,120),l("JUAN","Para ti no habrá próxima vez, Palomo. ¡Se acabó tu intervención!",Accion.DISPARAR,105),new Linea(candidato,"La ciudad eligió a "+candidato+". Vamos a responder con hechos, no con rumores."),new Linea("JUAN",m.antecedentes.electoral().juanCorrupto()?"Ayudar hoy no borra el pacto que confirmé. Me corresponde responder por él.":m.ganador==0?"Gobernaré escuchando, verificando y cumpliendo mis promesas.":"Respetaré el resultado y ayudaré a que esta ciudad salga adelante."),new Linea("SOFI",switch(m.antecedentes.estadoCiudad()){case "CONFIANZA CÍVICA"->"Conservamos la confianza y protegimos una elección legítima. Ahora nos toca cuidar lo que construimos.";case "CIUDAD EN CRISIS"->"La amenaza terminó, pero los rumores y los conflictos siguen aquí. Tenemos mucho que reparar.";default->"Podemos reconstruir nuestras redes y nuestra confianza. Vamos a hacerlo juntos.";}));
   default->List.of();
  };
 }
 public static Linea actual(MotorOcho m){var lineas=lineas(m);return lineas.isEmpty()?null:lineas.get(Math.min(m.lineaDialogo,lineas.size()-1));}
}
