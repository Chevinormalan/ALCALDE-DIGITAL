package alcaldedigital.cliente.audio;
import java.util.Set;

/** Política narrativa pura. La fase autoritativa determina la música, nunca al revés. */
public final class Partitura {
 private Partitura(){}
 public record Contexto(int mision,String fase,float avance,int etapa,boolean dialogo,boolean victoria,String ciudad){}
 public record Cue(String pista,float intensidad,boolean dialogo,String ambiente){}
 private static Cue c(String id,float i,boolean d,String amb){return new Cue(id,Math.clamp(i,0,1),d,amb);}
 public static Cue elegir(Contexto x){String f=x.fase;float i=.25f+Math.clamp(x.avance,0,1)*.75f;
  return switch(x.mision){
   case 1->switch(f){case "CRISIS"->c("M102",i,false,"parque");case "CIVITAS"->c("G02",.2f,true,"parque");case "RESULTADO"->c(x.victoria?"G03":"G03-crisis",x.victoria?.45f:.1f,false,"parque");default->c("M101",.2f,x.dialogo,"parque");};
   case 2->switch(f){case "DEFENSA","FALLO_CRITICO"->c("M202",f.equals("FALLO_CRITICO")?1:i,false,"oficina");case "ALARMA"->c("M202",.35f,true,"oficina");case "RESULTADO"->c(x.victoria?"G03":"G03-crisis",x.victoria?.4f:.1f,false,"oficina");default->c("M201",.2f,true,"oficina");};
   case 3->c(f.equals("boss")?"M303":f.equals("combat")?"M302":f.equals("ending")?"G03":"M301",f.equals("boss")?.4f+Math.max(0,x.etapa-1)*.3f:.6f,x.dialogo,"red");
   case 4->Set.of("BATTLE").contains(f)?c("M402",.4f+Math.max(0,x.etapa)*.27f,false,"plaza"):Set.of("CAR","ROAD","PLAZA").contains(f)?c("M401",.35f,false,"plaza"):c("G02",.18f,true,"plaza");
   case 5->switch(f){case "AVISO","ESQUIVA","TURNO","TRAICION"->c("M502",f.equals("TRAICION")?1:f.equals("TURNO")?.18f:.32f+Math.max(0,x.etapa-1)*.17f,false,"red");case "DECISION"->c("G02",.15f,true,"red");case "EPILOGO","FINAL"->c(x.victoria?"G03":"G03-crisis",x.victoria?.45f:.1f,true,"red");default->c("M501",.2f,true,"oficina");};
   case 6->switch(f){case "COMBATE"->c("M602",.35f+Math.max(0,x.etapa-1)*.3f,false,"");case "DESPEGUE","AVISO","ENTRENAMIENTO","RETIRADA","LLEGADA"->c("M601",f.equals("ENTRENAMIENTO")?.3f:.5f,x.dialogo,"");case "DERROTA"->c("G03-crisis",.1f,false,"");case "ENTREVISTA","RESPUESTA","SALA","FINAL","EXPLORAR_SALA"->c("G02",.15f,true,"oficina");default->c("M401",.2f,true,"plaza");};
   case 7->switch(f){case "CUENTA","CARRERA"->c("M702",i,false,"viento");case "RESULTADO","LLEGADA","EPILOGO"->c("M701",.25f,true,"plaza");default->c("M701",.2f,true,"plaza");};
   case 8->switch(f){case "COMBATE"->c("M802",.3f+Math.max(0,x.etapa-1)*.175f,false,"");case "MUTACION"->c("M802",.2f,true,"");case "FINAL"->c(x.ciudad.equals("CONFIANZA CÍVICA")?"G03":x.ciudad.equals("CIUDAD EN CRISIS")?"G03-crisis":"G03-reconstruccion",x.ciudad.equals("CONFIANZA CÍVICA")?.65f:x.ciudad.equals("CIUDAD EN CRISIS")?.08f:.35f,true,"plaza");case "EPILOGO"->c("G02",.2f,true,"plaza");case "DERROTA"->c("G03-crisis",.1f,false,"");default->c("M801",.25f,x.dialogo,"plaza");};
   default->c("G01",.3f,false,"");
  };
 }
}
