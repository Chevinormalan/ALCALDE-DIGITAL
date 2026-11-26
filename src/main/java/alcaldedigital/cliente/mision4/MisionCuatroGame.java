package alcaldedigital.cliente.mision4;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PFont;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Properties;

/** Narrative progression, live world, render order and action input for Juan's chapter. */
final class MisionCuatroGame {
    static final int W=1024,H=576;
    enum Phase { CAR, PLAZA, STAGE, QUESTION, ANSWER, REACTION, CONFUSED, PALOMO, DOWNLOAD,
        RUMOR, ROAD, BATTLE, VICTORY, RETURN, EPILOGUE, COMPLETE, CIVITA_ENTRADA, CIVITA, CIVITA_SALIDA }
    static final String[] QUESTIONS={
        "¿Es verdad que su vicepresidente odia a los niños?",
        "¿Es verdad que usted come bebés?",
        "¿Abrirá un centro comercial exclusivo para palomas?",
        "¿Le entregará su puesto a una paloma cuando gane?"};
    static final String[] RUMORS={
        "Juan va a cobrar peaje por mirar las palomas.",
        "El candidato instalará robots que aplauden cada discurso.",
        "Un doble de Juan vendió la plaza a los bots."};
    static final String[] CLUES={
        "No hay enlace al supuesto decreto ni firma oficial.",
        "La imagen circula sin fecha y la cuenta repite el mensaje.",
        "El video no muestra su origen y presenta cortes extraños."};
    static final String[] FOES={"TROLL","SPAMBOT","DEEPFAKE"};
    static final String[] CITIZEN_LINES={
        "Vine a escuchar propuestas, pero en Civitas dicen cosas muy extrañas.",
        "En mi familia compartieron esa historia sin saber de dónde salió.",
        "Ojalá alguien explique qué es verdad antes de que empiece el discurso."};
    final SceneManager scenes=new SceneManager();
    final DialogueManager dialogue=new DialogueManager();
    final CombatSystem combat=new CombatSystem();
    final Civita civita=new Civita();
    final SpriteBank sprites=new SpriteBank();
    PImage roadBackground;
    final CollisionWorld plaza=CollisionWorld.plaza(), salidaPlaza=CollisionWorld.plaza(), road=CollisionWorld.camino();
    final Actor hero=new Actor(825,440);
    final Actor vehicle=new Actor(80,330);
    final boolean[] citizensHelped=new boolean[2];
    final boolean[] keys=new boolean[512];
    final List<Citizen> audience=new ArrayList<>();
    final Random random=new Random(21);
    final PFont pixelFont=loadFont();
    private PFont loadFont(){
        try(var in=getClass().getResourceAsStream("/cliente/fuentes/PixelifySans.ttf")){
            if(in!=null)return new PFont(java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,in).deriveFont(18f),true);
        }catch(Exception ignored){}
        return new PFont(new java.awt.Font("Monospaced",java.awt.Font.PLAIN,18),true);
    }
    String partida=java.util.UUID.randomUUID().toString();
    String nombreMando="";
    float elapsed,sceneTime,camera,walkTime,transition,carX=855,carSpeed,carVelocityY,palomoX=-80,palomoY=90;
    float giroCarro,rotacionRuedas;
    boolean carroIzquierda;
    int question,encounter,selected,approval,reputation,answered;
    final int[] respuestas={-1,-1,-1,-1};
    boolean previousGuard,completed;
    String notice="",lastDecision="";
    MisionCuatroGame(){
        vehicle.radius=26;
        for(int i=0;i<19;i++){
            float x=470+(i%6)*51+(i/6%2)*13, y=210+(i/6)*64;
            // The first two rows must begin beside the stage, not inside its footprint.
            if(y<300)x=Math.max(505,x);
            audience.add(new Citizen(x,y,i%8));
            plaza.add(x-9,y-11,18,14);
        }
    }
    static final class SceneManager {
        Phase current=Phase.CAR;
        void set(Phase next){current=next;}
    }
    static final class Citizen {
        float x,y,walkDistance;
        final int sprite;
        boolean exited;
        int direction;
        Citizen(float x,float y,int sprite){this.x=x;this.y=y;this.sprite=sprite;}
        int frame(){return (int)(walkDistance/18)%4;}
    }
    void set(Phase phase){scenes.set(phase);sceneTime=0;selected=0;}
    boolean ending(){return scenes.current==Phase.COMPLETE;}
    Properties checkpoint(){
        Properties p=new Properties();p.setProperty("version","1");p.setProperty("partida",partida);
        Phase saved=scenes.current;int savedQuestion=question;
        if(saved==Phase.BATTLE&&combat.enemy==null)saved=Phase.VICTORY;
        if(saved==Phase.REACTION){savedQuestion=question+1;saved=savedQuestion<4?Phase.QUESTION:Phase.CONFUSED;}
        if(saved==Phase.PALOMO)saved=Phase.CONFUSED;
        if(saved==Phase.ANSWER)saved=Phase.QUESTION;
        p.setProperty("phase",saved.name());p.setProperty("question",Integer.toString(Math.min(3,savedQuestion)));
        p.setProperty("encounter",Integer.toString(encounter));p.setProperty("approval",Integer.toString(approval));
        p.setProperty("reputation",Integer.toString(reputation));p.setProperty("answered",Integer.toString(answered));
        for(int i=0;i<respuestas.length;i++)p.setProperty("respuesta."+i,Integer.toString(respuestas[i]));
        p.setProperty("kills",Integer.toString(combat.kills));p.setProperty("health",Integer.toString(Math.max(1,combat.health)));
        p.setProperty("heroX",Float.toString(hero.x));p.setProperty("heroY",Float.toString(hero.y));
        p.setProperty("carX",Float.toString(vehicle.x));p.setProperty("carY",Float.toString(vehicle.y));
        p.setProperty("citizen0",Boolean.toString(citizensHelped[0]));p.setProperty("citizen1",Boolean.toString(citizensHelped[1]));
        civita.guardar(p);
        if(combat.enemy!=null){p.setProperty("enemyX",Float.toString(combat.enemy.x));p.setProperty("enemyY",Float.toString(combat.enemy.y));p.setProperty("enemyHp",Integer.toString(combat.enemy.hp));}
        return p;
    }
    static MisionCuatroGame restore(Properties p){
        try{
            MisionCuatroGame g=new MisionCuatroGame();Phase phase=Phase.valueOf(p.getProperty("phase"));g.partida=p.getProperty("partida",java.util.UUID.nameUUIDFromBytes(new java.util.TreeMap<>(p).toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString());
            g.question=number(p,"question",0,3);g.encounter=number(p,"encounter",0,2);
            g.approval=number(p,"approval",0,4);g.reputation=number(p,"reputation",-4,4);
            g.answered=number(p,"answered",0,4);g.combat.kills=number(p,"kills",0,3);
            for(int i=0;i<g.respuestas.length;i++){
                g.respuestas[i]=Integer.parseInt(p.getProperty("respuesta."+i,"-1"));
                if(g.respuestas[i]<-1||g.respuestas[i]>1)return null;
            }
            boolean digital=phase==Phase.CIVITA||phase==Phase.CIVITA_SALIDA;
            g.combat.health=number(p,"health",1,5);g.hero.x=coordinate(p,"heroX",12,digital?Civita.ANCHO-12:2168);
            g.hero.y=coordinate(p,"heroY",24,digital?Civita.ALTO-16:560);
            g.vehicle.x=coordinate(p,"carX",64,2116);g.vehicle.y=coordinate(p,"carY",170,490);
            g.citizensHelped[0]=Boolean.parseBoolean(p.getProperty("citizen0"));g.citizensHelped[1]=Boolean.parseBoolean(p.getProperty("citizen1"));
            g.set(phase);g.camera=PApplet.constrain(g.hero.x-W*.43f,0,2180-W);
            g.completed=phase==Phase.COMPLETE;
            g.civita.restaurar(g,p);
            if(digital&&!g.civita.activa)return null;
            if(phase==Phase.CIVITA&&g.combat.enemy!=null&&p.containsKey("enemyHp")&&"2".equals(p.getProperty("civita.mapa"))){
                g.combat.enemy.x=coordinate(p,"enemyX",12,Civita.ANCHO-12);
                g.combat.enemy.y=coordinate(p,"enemyY",24,Civita.ALTO-16);
                g.combat.enemy.hp=number(p,"enemyHp",1,g.combat.enemy.maxHp);
            }
            if(phase==Phase.DOWNLOAD||phase==Phase.CIVITA_ENTRADA)for(Citizen c:g.audience)c.exited=true;
            if(phase==Phase.EPILOGUE&&g.civita.activa)g.volverDeCivita();
            if(phase==Phase.QUESTION)g.ask();
            if(phase==Phase.CONFUSED)g.dialogue.start(new DialogueManager.Line("Candidato Juan","¿Qué le pasa a la gente hoy? Nadie me ha preguntado por el presupuesto.","confused"));
            if(phase==Phase.STAGE)g.sceneTime=1.3f;
            if(phase==Phase.BATTLE){
                float ex=coordinate(p,"enemyX",27,2153),ey=coordinate(p,"enemyY",40,545);
                g.combat.spawn(g.encounter,ex,ey);g.combat.health=number(p,"health",1,5);
                g.combat.enemy.hp=number(p,"enemyHp",1,g.combat.enemy.maxHp);g.hero.invulnerable=1;
            }
            if(phase==Phase.EPILOGUE&&!g.civita.activa){g.dialogue.start(new DialogueManager.Line("Candidato Juan","Ya no queda nadie escuchando...", "confused"),new DialogueManager.Line("Candidato Juan","Mañana regresaré para escuchar y reconstruir la confianza.","speech"));}
            if(phase==Phase.COMPLETE)g.completed=true;
            return g;
        }catch(RuntimeException ex){System.err.println("Autoguardado de misión 4 inválido: "+ex.getMessage());return null;}
    }
    private static int number(Properties p,String name,int min,int max){int n=Integer.parseInt(p.getProperty(name));if(n<min||n>max)throw new IllegalArgumentException(name);return n;}
    private static float coordinate(Properties p,String name,float min,float max){float n=Float.parseFloat(p.getProperty(name));if(!Float.isFinite(n)||n<min||n>max)throw new IllegalArgumentException(name);return n;}
    void update(float dt){
        dt=Math.min(.05f,Math.max(0,dt));elapsed+=dt;sceneTime+=dt;dialogue.update(dt);
        hero.invulnerable=Math.max(0,hero.invulnerable-dt);
        if(scenes.current==Phase.CIVITA_ENTRADA){if(sceneTime>=4.65f)civita.entrar(this);return;}
        if(scenes.current==Phase.CIVITA){civita.actualizar(this,dt);return;}
        if(scenes.current==Phase.CIVITA_SALIDA){if(sceneTime>=3.1f)volverDeCivita();return;}
        if(scenes.current==Phase.CAR){
            float dx=axis(KeyEvent.VK_D,KeyEvent.VK_RIGHT)-axis(KeyEvent.VK_A,KeyEvent.VK_LEFT);
            float dy=axis(KeyEvent.VK_S,KeyEvent.VK_DOWN)-axis(KeyEvent.VK_W,KeyEvent.VK_UP);
            float length=(float)Math.hypot(dx,dy);if(length>1){dx/=length;dy/=length;}
            carSpeed+=(dx*205-carSpeed)*Math.min(1,dt*3.5f);
            carVelocityY+=(dy*150-carVelocityY)*Math.min(1,dt*3.5f);
            float oldX=vehicle.x,oldY=vehicle.y;
            road.moveVehicle(vehicle,carSpeed*dt,carVelocityY*dt);
            rotacionRuedas=(rotacionRuedas+(float)Math.hypot(vehicle.x-oldX,vehicle.y-oldY)/8)%PApplet.TWO_PI;
            if(Math.abs(carSpeed)>17)carroIzquierda=carSpeed<0;
            giroCarro+=(PApplet.constrain(carVelocityY/150f,-1,1)*.085f-giroCarro)*Math.min(1,dt*7);
            if(Math.abs(vehicle.x-oldX)<.01f)carSpeed=0;
            if(Math.abs(vehicle.y-oldY)<.01f)carVelocityY=0;
            camera+=(PApplet.constrain(vehicle.x-W*.35f,0,2180-W)-camera)*Math.min(1,dt*5);
            notice=vehicle.x>1970?"ENTER · llegar a la plaza y estacionar": "WASD / flechas · conduce hasta la plaza";
        }
        if(scenes.current==Phase.PLAZA){
            if(!dialogue.active())move(dt,plaza);
            if(Math.hypot(hero.x-212,hero.y-351)<42)notice="ENTER · subir a la tarima";
            else if(nearestPlazaCitizen()>=0)notice="E · escuchar a un vecino";
            else notice="WASD · camina hacia las escaleras";
        }
        if(scenes.current==Phase.STAGE){
            hero.x+=(342-hero.x)*Math.min(1,dt*4);hero.y+=(219-hero.y)*Math.min(1,dt*4);
            if(sceneTime>1.25f){notice="ENTER · comenzar discurso";}
        }
        if(scenes.current==Phase.QUESTION||scenes.current==Phase.ANSWER||scenes.current==Phase.REACTION||scenes.current==Phase.CONFUSED){
            if(scenes.current==Phase.ANSWER && !dialogue.active())notice="Responde desde el podio · SÍ / NO";
        }
        if(scenes.current==Phase.PALOMO){
            if(sceneTime<1.4f){palomoX=Math.min(380,palomoX+dt*420);palomoY=160+(float)Math.sin(elapsed*8)*10;}
            else if(!dialogue.active() && sceneTime>2.5f){
                evacuarVecinos(dt);
                palomoX+=dt*440;palomoY-=dt*95;
                if(palomoX>1100&&audience.stream().allMatch(c->c.exited))set(Phase.DOWNLOAD);
            }
        }
        if(scenes.current==Phase.ROAD||scenes.current==Phase.BATTLE){
            if(hero.knockX!=0||hero.knockY!=0){road.move(hero,hero.knockX,hero.knockY);hero.knockX=hero.knockY=0;}
            if(scenes.current==Phase.ROAD){
                if(!dialogue.active())move(dt,road);
                float trigger=encounter==1?890:encounter==2?1500:0;
                if(trigger>0&&hero.x>trigger){set(Phase.RUMOR);notice="Nueva publicación en Civitas";}
                else if(!dialogue.active()){
                    int nearby=nearCitizen();
                    notice=nearby>=0?"E · conversar con el vecino":"Sigue el camino · busca la siguiente señal de Civitas";
                }
            }else{
                if(combat.health<=0){combat.health=5;combat.spawn(encounter,hero.x+125,hero.y-25);hero.invulnerable=1.5f;notice="Juan se recupera. Inténtalo de nuevo.";}
                combat.defend(keys[KeyEvent.VK_Q]||keys[KeyEvent.VK_K]);
                float mx=axis(KeyEvent.VK_D,KeyEvent.VK_RIGHT)-axis(KeyEvent.VK_A,KeyEvent.VK_LEFT);
                float my=axis(KeyEvent.VK_S,KeyEvent.VK_DOWN)-axis(KeyEvent.VK_W,KeyEvent.VK_UP);
                if(hero.dodge>0 && mx==0 && my==0){
                    mx=hero.direction==2?-1:hero.direction==3?1:0;
                    my=hero.direction==1?-1:hero.direction==0?1:0;
                }
                hero.move(mx,my,road,dt,combat.guarding?.38f:1);
                combat.update(dt,hero,road);
                if(combat.enemy==null){set(Phase.VICTORY);notice="Amenaza detenida";}
            }
            float target=PApplet.constrain(hero.x-W*.43f,0,2180-W);
            camera+=(target-camera)*Math.min(1,dt*6);
        }
        if(scenes.current==Phase.VICTORY && sceneTime>1.65f){
            if(encounter==2){set(Phase.RETURN);transition=0;}
            else{encounter++;set(Phase.ROAD);hero.x=Math.max(hero.x,encounter==1?680:1290);}
        }
        if(scenes.current==Phase.RETURN){transition=Math.min(1,transition+dt*.65f);
            if(sceneTime>2.1f){hero.x=350;hero.y=218;set(Phase.EPILOGUE);
                dialogue.start(new DialogueManager.Line("Candidato Juan","Ya no queda nadie escuchando... Llegué tarde a una conversación que ya estaba en marcha.","confused"),
                    new DialogueManager.Line("Candidato Juan",approval>=2?"Revisar las fuentes me ayudó a detener los rumores, pero eso no devuelve la confianza de inmediato.":"Respondí demasiado deprisa. Debí buscar las fuentes antes de enfrentar cada rumor.","confused"),
                    new DialogueManager.Line("Candidato Juan","Mañana regresaré para escuchar, explicar mis decisiones y reconstruir la confianza paso a paso.","speech"));}
        }
    }
    float axis(int a,int b){return keys[a]||keys[b]?1:0;}
    /** The opening between the two lower fence sections is x=605..756. */
    void evacuarVecinos(float dt){
        for(int i=0;i<audience.size();i++){
            Citizen c=audience.get(i);
            if(c.exited)continue;
            float exitX=625+(i%5)*22;
            float targetX=exitX,targetY=Math.abs(c.x-exitX)>3?c.y:548;
            float dx=targetX-c.x,dy=targetY-c.y;
            float distance=(float)Math.hypot(dx,dy);
            if(distance>0){
                Actor walker=new Actor(c.x,c.y);walker.radius=8;
                float step=Math.min(distance,235*dt);
                salidaPlaza.move(walker,dx/distance*step,dy/distance*step);
                c.walkDistance+=(float)Math.hypot(walker.x-c.x,walker.y-c.y);
                c.direction=Math.abs(dx)>Math.abs(dy)?(dx<0?2:3):0;
                c.x=walker.x;c.y=walker.y;
            }
            if(Math.abs(c.x-exitX)<=3&&c.y>=547)c.exited=true;
        }
    }
    void move(float dt,CollisionWorld world){move(dt,world,axis(KeyEvent.VK_D,KeyEvent.VK_RIGHT)-axis(KeyEvent.VK_A,KeyEvent.VK_LEFT),axis(KeyEvent.VK_S,KeyEvent.VK_DOWN)-axis(KeyEvent.VK_W,KeyEvent.VK_UP));}
    void move(float dt,CollisionWorld world,float dx,float dy){hero.move(dx,dy,world,dt);}
    void press(int code,char ch){
        if(code>=0&&code<keys.length)keys[code]=true;
        Phase phase=scenes.current;
        if(phase==Phase.CIVITA_ENTRADA||phase==Phase.CIVITA_SALIDA) {
            if(sceneTime>1.15f&&(code==KeyEvent.VK_E||code==KeyEvent.VK_ENTER)){
                if(phase==Phase.CIVITA_ENTRADA)civita.entrar(this);else volverDeCivita();
            }
            return;
        }
        if(phase==Phase.CIVITA){civita.tecla(this,code);return;}
        if(phase==Phase.CAR && (code==KeyEvent.VK_ENTER||code==KeyEvent.VK_E) && vehicle.x>1970){
            carX=855;camera=0;hero.x=825;hero.y=440;set(Phase.PLAZA);notice="Llega al pie de la tarima";return;
        }
        if(phase==Phase.ROAD && (code==KeyEvent.VK_ENTER||code==KeyEvent.VK_E)){
            if(dialogue.active()){dialogue.advance();return;}
            int index=nearCitizen();
            if(index>=0){
                boolean first=!citizensHelped[index];citizensHelped[index]=true;
                if(first)combat.health=Math.min(combat.maxHealth,combat.health+1);
                dialogue.start(new DialogueManager.Line(index==0?"Lina, vecina":"Tomás, vecino",
                    index==0?"Vi la publicación original: no cita ninguna fuente. Comprueba antes de responder.":"Los mensajes repetidos no son pruebas. Busca quién los publicó primero.","vecino_"+(index+3)),
                    new DialogueManager.Line("Candidato Juan",first?"Gracias por ayudarme a escuchar. Recupero una unidad de salud.":"Gracias. Seguiré buscando el origen de la historia.","speech"));
                return;
            }
        }
        if(phase==Phase.PLAZA && dialogue.active()){
            if(code==KeyEvent.VK_ENTER||code==KeyEvent.VK_E||code==KeyEvent.VK_SPACE)dialogue.advance();
            return;
        }
        if(phase==Phase.PLAZA && (code==KeyEvent.VK_ENTER||code==KeyEvent.VK_E) && Math.hypot(hero.x-212,hero.y-351)<56){
            hero.x=211;hero.y=335;set(Phase.STAGE);notice="";return;
        }
        if(phase==Phase.PLAZA && (code==KeyEvent.VK_ENTER||code==KeyEvent.VK_E)){
            int index=nearestPlazaCitizen();if(index>=0){Citizen c=audience.get(index);
                dialogue.start(new DialogueManager.Line("Vecino",CITIZEN_LINES[index%3],"vecino_"+c.sprite));return;}
        }
        if(phase==Phase.STAGE && sceneTime>1.25f && (code==KeyEvent.VK_ENTER||code==KeyEvent.VK_E)){
            set(Phase.QUESTION);ask();return;
        }
        if(phase==Phase.QUESTION || phase==Phase.REACTION || phase==Phase.CONFUSED || phase==Phase.PALOMO || phase==Phase.EPILOGUE){
            if(code==KeyEvent.VK_ENTER||code==KeyEvent.VK_E||code==KeyEvent.VK_SPACE){
                if(!dialogue.advance())return;
                if(phase==Phase.QUESTION){set(Phase.ANSWER);return;}
                if(phase==Phase.REACTION){question++;if(question<QUESTIONS.length){set(Phase.QUESTION);ask();}
                    else {set(Phase.CONFUSED);dialogue.start(new DialogueManager.Line("Candidato Juan","¿Qué le pasa a la gente hoy? Nadie me ha preguntado por el presupuesto.","confused"));}return;}
                if(phase==Phase.CONFUSED){set(Phase.PALOMO);dialogue.start(new DialogueManager.Line("Mr. Palomo","¡Descarga Civitas, hombre! Ahí es donde nacen estas cadenas.","palomo"),
                    new DialogueManager.Line("Candidato Juan","¿La paloma acaba de recomendarme una red social?","confused"));return;}
                if(phase==Phase.EPILOGUE){set(Phase.COMPLETE);completed=true;return;}
            }
            return;
        }
        if(phase==Phase.ANSWER){
            if(code==KeyEvent.VK_LEFT||code==KeyEvent.VK_A)selected=0;
            if(code==KeyEvent.VK_RIGHT||code==KeyEvent.VK_D)selected=1;
            if(code==KeyEvent.VK_S||code==KeyEvent.VK_Y)answer(true);
            if(code==KeyEvent.VK_N)answer(false);
            if(code==KeyEvent.VK_ENTER)answer(selected==0);
            return;
        }
        if(phase==Phase.DOWNLOAD && (code==KeyEvent.VK_ENTER||code==KeyEvent.VK_E)){
            set(Phase.CIVITA_ENTRADA);notice="El teléfono de Juan vibra";return;
        }
        if(phase==Phase.RUMOR){
            if(code==KeyEvent.VK_LEFT||code==KeyEvent.VK_A)selected=0;
            if(code==KeyEvent.VK_RIGHT||code==KeyEvent.VK_D)selected=1;
            if(code==KeyEvent.VK_V)respond(true);
            if(code==KeyEvent.VK_N)respond(false);
            if(code==KeyEvent.VK_ENTER)respond(selected==0);
            return;
        }
        if(phase==Phase.BATTLE){
            if(code==KeyEvent.VK_J||code==KeyEvent.VK_F)combat.attack(hero);
            if(code==KeyEvent.VK_SHIFT||code==KeyEvent.VK_SPACE)combat.dodge(hero);
        }
    }
    void release(int code){if(code>=0&&code<keys.length)keys[code]=false;if(code==KeyEvent.VK_K||code==KeyEvent.VK_Q)combat.defend(keys[KeyEvent.VK_K]||keys[KeyEvent.VK_Q]);}
    int nearCitizen(){
        for(int i=0;i<2;i++)if(Math.hypot(hero.x-(i==0?780:1370),hero.y-350)<60)return i;
        return -1;
    }
    int nearestPlazaCitizen(){
        int nearest=-1;double distance=57;
        for(int i=0;i<audience.size();i++){Citizen c=audience.get(i);double d=Math.hypot(hero.x-c.x,hero.y-c.y);if(d<distance){distance=d;nearest=i;}}
        return nearest;
    }
    void loseFocus(){java.util.Arrays.fill(keys,false);combat.defend(false);}
    void ask(){
        dialogue.start(new DialogueManager.Line("Ciudadano "+(question+1),QUESTIONS[question],"vecino_"+((question+2)%8)));
        notice="¿? · El público espera una respuesta";
    }
    void answer(boolean yes){
        answered++;respuestas[question]=yes?1:0;
        if(yes && question!=2)reputation--; else if(!yes && question!=2)reputation++;
        set(Phase.REACTION);
        String[] yesAnswers={"Sí... Un momento: acepté una acusación sin pruebas. Respondí demasiado rápido.","¿Bebés? No... Un momento, respondí demasiado rápido.","Una plaza para palomas podría ser divertida, pero no es un proyecto aprobado.","Sí... ¿Qué acabo de prometer? Los cargos se deciden mediante elecciones."};
        String[] noAnswers={"No. Mi equipo trabaja por los niños; podemos revisar sus propuestas.","No. Una afirmación así necesita pruebas antes de difundirse.","No hay ningún centro comercial de palomas en nuestro plan.","No. Los cargos públicos se deciden mediante elecciones, no publicaciones."};
        String response=yes?yesAnswers[question]:noAnswers[question];
        String crowd=yes?"Varios vecinos sacan sus teléfonos: la respuesta ya está circulando.":"Algunos vecinos guardan sus teléfonos y escuchan la explicación.";
        dialogue.start(new DialogueManager.Line("Candidato Juan",response,"speech"),
            new DialogueManager.Line("Público",crowd,"vecino_"+((question+2)%8)));
    }
    void respond(boolean validate){
        approval+=validate?1:0;
        lastDecision=validate?"INVESTIGAR: compruebas la fuente antes de responder.":"RESPONDER: actúas sin comprobar la fuente.";
        float bx=encounter==0?480:encounter==1?1050:1680;
        hero.x=bx-155;hero.y=330;camera=PApplet.constrain(hero.x-W*.43f,0,2180-W);
        combat.spawn(encounter,bx,315);set(Phase.BATTLE);notice=lastDecision;
        if(!validate){combat.health=Math.max(3,combat.health-1);combat.enemy.cooldown=.4f;}
    }
    private void volverDeCivita() {
        camera=0;hero.x=350;hero.y=218;hero.direction=0;loseFocus();
        for(Citizen c:audience)c.exited=true;
        set(Phase.EPILOGUE);
        dialogue.start(new DialogueManager.Line("Candidato Juan","La plaza está vacía. La gente escuchó los rumores antes de escucharme a mí.","confused"),
            new DialogueManager.Line("Candidato Juan","Una imagen recortada y varias copias parecían pruebas. Los expedientes cambiaron la historia.","speech"),
            new DialogueManager.Line("Mr. Palomo","¡Cucurrucucú! Mañana la noticia volará igual. ¿Quién la revisará primero?","palomo"));
    }
    void draw(processing.core.PGraphics a){
        a.textFont(pixelFont);
        if(scenes.current==Phase.CIVITA){civita.arte.laberinto(a,this,civita,true);return;}
        if(scenes.current==Phase.CIVITA_SALIDA){
            civita.arte.laberinto(a,this,civita,false);civita.arte.telefono(a,this,sceneTime,true);return;
        }
        a.background(33,21,47);
        a.pushMatrix();
        if(scenes.current==Phase.CAR||scenes.current==Phase.ROAD||scenes.current==Phase.BATTLE||scenes.current==Phase.RUMOR||scenes.current==Phase.VICTORY||scenes.current==Phase.RETURN){
            a.translate(-camera,0);drawRoad(a);
            if(scenes.current==Phase.CAR){drawCar(a,vehicle.x-58,vehicle.y-34);drawCarForeground(a);}
            else if(scenes.current!=Phase.RETURN){drawRoadCitizens(a);if(combat.enemy!=null && combat.enemy.y<hero.y){drawEnemy(a);drawHero(a,true);}else{drawHero(a,true);drawEnemy(a);}if(scenes.current==Phase.BATTLE)combat.drawEffects(a,hero);}
        }else{drawPlaza(a);}
        a.popMatrix();
        drawHud(a);
        if(dialogue.active())drawDialogue(a);
        if(scenes.current==Phase.ANSWER)drawChoice(a,"RESPONDE ANTE EL PÚBLICO","SÍ","NO","S / N · flechas y ENTER");
        if(scenes.current==Phase.RUMOR)drawRumor(a);
        if(scenes.current==Phase.DOWNLOAD)drawDownload(a);
        if(scenes.current==Phase.CIVITA_ENTRADA)civita.arte.telefono(a,this,sceneTime,false);
        if(scenes.current==Phase.RETURN){a.fill(22,13,34,(int)(transition*255));a.rect(0,0,W,H);a.fill(245,232,251);a.textAlign(PApplet.CENTER);a.textSize(24);a.text("Las publicaciones siguen circulando...",W/2,280);a.textAlign(PApplet.LEFT);}
        if(scenes.current==Phase.COMPLETE)drawComplete(a);
        drawSceneTransition(a);
    }
    void drawSceneTransition(processing.core.PGraphics a){
        // Velo breve al llegar a escenarios nuevos: la simulación conserva su tiempo original.
        if(sceneTime<.30f&&(scenes.current==Phase.CAR||scenes.current==Phase.PLAZA||
                scenes.current==Phase.ROAD||scenes.current==Phase.EPILOGUE)){
            int alpha=(int)(170*(1-sceneTime/.30f));
            a.noStroke();a.fill(20,12,33,alpha);a.rect(0,56,W,H-56);
        }
    }
    /** Escenario y actores se ordenan por la posición de sus pies. */
    void drawPlaza(processing.core.PGraphics a){
        a.image(sprites.image("plaza_presentacion"),0,0,1024,576);
        // Mobiliario reutilizado del parque; únicamente decoración en zonas ya bloqueadas.
        a.image(sprites.shared("arte/objetos/flores"),22,349,40,40);
        a.image(sprites.shared("arte/objetos/rosa"),864,84,33,33);
        a.image(sprites.shared("arte/objetos/lampara"),881,139,45,65);
        record Capa(float pies,Runnable pintar){}
        List<Capa> capas=new ArrayList<>();
        if(scenes.current!=Phase.EPILOGUE && scenes.current!=Phase.COMPLETE){
            for(int i=0;i<audience.size();i++){
                final int index=i;Citizen c=audience.get(i);
                if(c.exited)continue;
                float bob=(float)Math.sin(elapsed*2+i*1.9)*1.1f;
                float reaction=scenes.current==Phase.REACTION?(float)Math.sin(elapsed*12+i)*3:0;
                boolean walking=c.walkDistance>0;
                capas.add(new Capa(c.y,()->{
                    a.noStroke();a.fill(23,14,35,95);a.ellipse(c.x,c.y-1,24,8);
                    if(walking)sprites.drawCitizenWalking(a,c.sprite,c.x,c.y,35,47,c.frame(),c.direction);
                    else sprites.draw(a,"vecino_"+c.sprite,c.x,c.y+bob+reaction,35,47);
                    if((scenes.current==Phase.QUESTION||scenes.current==Phase.ANSWER)&&index==question*4+2){
                        a.fill(244,203,106);a.stroke(33,21,47);a.strokeWeight(3);a.ellipse(c.x,c.y-58,25,25);
                        a.fill(33,21,47);a.textSize(20);a.textAlign(PApplet.CENTER);a.text("?",c.x,c.y-51);a.textAlign(PApplet.LEFT);a.noStroke();
                    }
                }));
            }
        }
        if(scenes.current==Phase.PLAZA)capas.add(new Capa(443,()->drawCar(a,carX,405)));
        if(scenes.current!=Phase.CAR&&(scenes.current!=Phase.CIVITA_ENTRADA||sceneTime<2.05f))
            capas.add(new Capa(hero.y,()->drawHero(a,false)));
        capas.sort(java.util.Comparator.comparingDouble(Capa::pies));
        for(Capa capa:capas)capa.pintar().run();
        if(scenes.current==Phase.PALOMO){PImage p=sprites.palomoVuelo((int)(elapsed*7)%4);a.image(p,palomoX-28,palomoY-26,56,56);}
        if(scenes.current==Phase.STAGE)drawPrompt(a,351,132,"ENTER · hablar");
        if(scenes.current==Phase.PLAZA&&Math.hypot(hero.x-212,hero.y-351)<56)drawPrompt(a,211,296,"ENTER · subir");
    }
    void drawCar(processing.core.PGraphics a,float x,float y){
        boolean marcha=scenes.current==Phase.CAR;
        a.noStroke();a.fill(20,22,39,115);a.ellipse(x+60,y+34,107,13);
        if(marcha&&Math.hypot(carSpeed,carVelocityY)>34){
            float dust=(float)Math.sin(elapsed*12)*2;
            float cola=carroIzquierda?x+129:x-9;
            a.fill(217,196,166,55);a.ellipse(cola,y+30+dust,15,7);a.ellipse(cola+(carroIzquierda?14:-14),y+32-dust,9,5);
        }
        a.pushMatrix();
        if(marcha){
            a.translate(x+60,y+33);a.rotate(giroCarro);if(carroIzquierda)a.scale(-1,1);
            a.translate(-x-60,-y-33);
        }
        a.image(sprites.image("vehiculo"),x,y-20,120,56);
        a.noStroke();a.fill(255,219,132,120+(int)(30*Math.sin(elapsed*3)));a.rect(x+112,y+7,5,4);
        a.fill(225,89,112,180);a.rect(x+3,y+8,4,4);
        a.stroke(61,70,91);a.strokeWeight(2);float spin=marcha?rotacionRuedas:0;
        for(int i=0;i<2;i++){float cx=x+30+i*63;a.line(cx-(float)Math.cos(spin)*4,y+27-(float)Math.sin(spin)*4,cx+(float)Math.cos(spin)*4,y+27+(float)Math.sin(spin)*4);}a.noStroke();
        a.popMatrix();
    }
    void drawCarForeground(processing.core.PGraphics a){
        for(var p:new int[][]{{680,157,64,80},{755,454,74,56},{1160,159,70,82},{1250,450,66,56},{1815,155,88,75},{1870,458,69,50}})
            if(p[1]+p[3]>vehicle.y&&Math.abs(p[0]+p[2]/2f-vehicle.x)<142)drawProp(a,p[0],p[1],p[2],p[3]);
    }
    void drawRoadCitizens(processing.core.PGraphics a){
        for(int i=0;i<2;i++){
            float x=i==0?780:1370;
            a.fill(20,12,29,110);a.ellipse(x,351,30,10);
            sprites.draw(a,"vecino_"+(i+3),x,350+(float)Math.sin(elapsed*2+i)*1.5f,37,52);
            if(scenes.current==Phase.ROAD && nearCitizen()==i && !dialogue.active())drawPrompt(a,x,270,"E · hablar");
        }
    }
    void drawHero(processing.core.PGraphics a,boolean fighting){
        String name=heroVisualPose(fighting);
        boolean walking=keys[KeyEvent.VK_A]||keys[KeyEvent.VK_D]||keys[KeyEvent.VK_W]||keys[KeyEvent.VK_S]||keys[KeyEvent.VK_UP]||keys[KeyEvent.VK_DOWN]||keys[KeyEvent.VK_LEFT]||keys[KeyEvent.VK_RIGHT];
        float bob=walking?(float)Math.sin(hero.walk)*1.2f:0;
        a.noStroke();a.fill(20,12,29,110);a.ellipse(hero.x,hero.y-2,30,10);
        if(scenes.current==Phase.CIVITA&&combat.golpeCivitaActivo()){
            float progreso=combat.progresoGolpe(), impulso=(float)Math.sin(progreso*Math.PI);
            int dir=combat.direccionCivita();
            float dx=dir==2?-1:dir==3?1:0,dy=dir==1?-1:dir==0?1:0;
            a.pushMatrix();a.translate(hero.x+dx*7*impulso,hero.y+dy*6*impulso);
            a.rotate(dx*(progreso<.24f?-.12f:.095f*impulso));
            sprites.juan.dibujar(a,progreso<.23f?"idle":progreso<.79f?"attack":"idle",
                dir,hero.walk,elapsed,0,0,60,73);
            a.popMatrix();
        }else if(name.equals("attack")||hero.invulnerable<=0||(int)(elapsed*12)%2==0){
            String pose=name.startsWith("walk_")?"walk":name.startsWith("combat_")?"idle":name;
            sprites.juan.dibujar(a,pose,hero.direction,hero.walk,elapsed,hero.x,hero.y+bob,fighting?60:50,fighting?73:66);
        }
    }
    String heroVisualPose(boolean fighting){
        String[] dirs={"front","back","left","right"};
        String name=dirs[hero.direction];
        boolean combate=scenes.current==Phase.BATTLE||scenes.current==Phase.CIVITA;
        if(scenes.current==Phase.EPILOGUE)name="confused";
        else if(scenes.current==Phase.REACTION||scenes.current==Phase.QUESTION||scenes.current==Phase.ANSWER)name="speech";
        else if(fighting&&combate){name=combat.swordTime>0?"attack":combat.guarding?"defend":hero.invulnerable>0?"hurt":"combat_"+name;}
        boolean walking=keys[KeyEvent.VK_A]||keys[KeyEvent.VK_D]||keys[KeyEvent.VK_W]||keys[KeyEvent.VK_S]||
            keys[KeyEvent.VK_UP]||keys[KeyEvent.VK_DOWN]||keys[KeyEvent.VK_LEFT]||keys[KeyEvent.VK_RIGHT];
        if(walking&&(!fighting||!combate||(!combat.guarding&&combat.swordTime<=0&&hero.invulnerable<=0))&&
            (scenes.current==Phase.PLAZA||scenes.current==Phase.ROAD||combate))
            name="walk_"+dirs[hero.direction]+"_"+((int)hero.walk%4);
        return name;
    }
    void drawRoad(processing.core.PGraphics a){
        if(roadBackground==null){
            var layer=new processing.awt.PGraphicsJava2D();layer.setParent(new PApplet());layer.setPrimary(false);layer.setSize(2180,576);
            layer.noSmooth();layer.beginDraw();layer.textFont(pixelFont);drawRoadBackground(layer);layer.endDraw();
            roadBackground=layer.get();layer.dispose();
        }
        a.image(roadBackground,0,0);
        if(combat.enemy!=null&&scenes.current==Phase.BATTLE){
            CombatSystem.Enemy e=combat.enemy;
            if(e.windup>0){a.noFill();a.stroke(226,91,132,180);a.strokeWeight(3);a.ellipse(e.targetX,e.targetY,84,84);a.line(e.x,e.y,e.targetX,e.targetY);a.noStroke();}
        }
    }
    void drawRoadBackground(processing.core.PGraphics a){
        a.noStroke();
        PImage camino=sprites.image("camino_presentacion");
        int corte1=(int)(camino.height*.23f),corte2=(int)(camino.height*.87f);
        drawRoadStrip(a,camino,0,corte1,0,150);drawRoadStrip(a,camino,corte1,corte2-corte1,150,360);drawRoadStrip(a,camino,corte2,camino.height-corte2,510,66);
        // El parque continúa fuera del camino: reutiliza los mismos árboles,
        // flores y mobiliario de las misiones anteriores sin alterar las colisiones.
        a.noStroke();a.fill(44,32,58,100);a.rect(0,145,2180,12);a.rect(0,507,2180,10);
        // Franjas de adoquines cálidos dan profundidad a la avenida.
        a.fill(66,43,68,55);a.rect(0,151,2180,5);a.rect(0,503,2180,5);
        drawProp(a,680,157,64,80);drawProp(a,755,454,74,56);
        drawProp(a,1160,159,70,82);drawProp(a,1250,450,66,56);
        drawProp(a,1815,155,88,75);drawProp(a,1870,458,69,50);
        for(int i=0;i<8;i++){
            float x=160+i*260;
            a.image(sprites.shared("arte/objetos/flores"),x,112+(i%2)*6,42,42);
            a.image(sprites.shared(i%2==0?"arte/objetos/lampara":"arte/objetos/banco"),x+90,456,55,55);
        }
        a.image(sprites.shared("arte/objetos/aviso"),1960,155,95,95);
        a.fill(35,24,53,220);a.rect(1980,225,155,37,4);a.fill(244,203,106);a.textSize(14);a.text("PLAZA  →",2000,249);
        for(int i=0;i<3;i++){
            int x=i==0?325:i==1?885:1495;
            a.fill(46,26,67);a.rect(x,181,132,48,5);a.fill(244,203,106);a.textSize(14);
            a.text("CIVITAS / "+(i+1),x+10,202);a.fill(232,214,239);a.text("RUMOR 0"+(i+1),x+10,221);
        }
    }
    void drawRoadStrip(processing.core.PGraphics a,PImage imagen,int inicio,int alto,float y,float destinoAlto){
        PImage banda=imagen.get(0,inicio,imagen.width,alto);float ancho=banda.width*destinoAlto/banda.height;
        for(float x=0;x<2180;x+=ancho)a.image(banda,x,y,ancho,destinoAlto);
    }
    void drawProp(processing.core.PGraphics a,int x,int y,int w,int h){
        a.fill(24,18,44,90);a.ellipse(x+w/2,y+h,w+15,16);
        PImage arbol=sprites.recortada("arbol_violeta");float escala=Math.min((w+20f)/arbol.width,(h+35f)/arbol.height);
        a.image(arbol,x+w/2f-arbol.width*escala/2,y+h-arbol.height*escala,arbol.width*escala,arbol.height*escala);
    }
    void drawEnemy(processing.core.PGraphics a){
        CombatSystem.Enemy e=combat.enemy;if(e==null)return;
        a.noStroke();a.fill(30,14,42,130);a.ellipse(e.x,e.y,52,13);
        String asset=switch(e.pattern){case TROLL->"enemigo_troll";case SPAMBOT->"enemigo_spambot";case DEEPFAKE->"enemigo_deepfake";};
        float bob=(float)Math.sin(e.age*6)*2;
                if(e.flash<=0||(int)(elapsed*16)%2==0){
            float dx=hero.x-e.x,dy=hero.y-e.y;
            String dir=Math.abs(dx)>Math.abs(dy)?(dx<0?"left":"right"):(dy<0?"back":"front");
            String variant=e.pattern.name().toLowerCase(java.util.Locale.ROOT);
            String frame="mision3/redisenio/enemy_"+variant+"_"+dir+"_"+(1+(int)(e.age*7)%4);
            a.image(sprites.shared(frame),e.x-33,e.y-78+bob,66,88);
        }
        a.fill(32,21,47);a.rect(e.x-30,e.y-82,60,7);a.fill(244,203,106);a.rect(e.x-30,e.y-82,60f*e.hp/e.maxHp,7);
        if(e.windup>0){a.fill(239,91,134);a.textSize(20);a.text("!",e.x-2,e.y-85);}
    }
    void drawHud(processing.core.PGraphics a){
        a.noStroke();a.fill(33,21,47,230);a.rect(0,0,W,56);
        a.stroke(189,145,213);a.strokeWeight(2);a.line(0,56,W,56);a.noStroke();a.fill(244,203,106);a.rect(10,6,5,5);a.rect(W-15,6,5,5);a.fill(189,145,213);a.rect(10,48,5,5);a.rect(W-15,48,5,5);
        a.fill(244,203,106);a.textSize(13);a.text("CIUDAD NOVA  /  MISIÓN 4",20,21);
        a.fill(245,232,251);a.textSize(18);a.text("La plaza que dejó de escuchar",20,45);
        if(scenes.current==Phase.BATTLE||scenes.current==Phase.ROAD||scenes.current==Phase.VICTORY){
            for(int i=0;i<5;i++){a.fill(i<combat.health?0xffed739c:0xff69445e);a.ellipse(751+i*23,23,15,15);}
            a.fill(214,191,226);a.textSize(11);a.text("ESCUDO",745,47);
            a.fill(82,55,98);a.rect(803,37,112,10,3);a.fill(244,203,106);a.rect(803,37,112*combat.shieldLeft/5,10,3);
            a.fill(245,232,251);a.textSize(12);a.text((encounter+1)+" / 3",940,34);a.fill(214,191,226);a.textSize(10);a.text("VIDA",704,27);
        }else if(scenes.current==Phase.CAR){
            a.fill(214,191,226);a.textSize(11);a.text("RUTA A LA PLAZA",754,20);
            a.fill(82,55,98);a.rect(754,29,205,9,4);a.fill(244,203,106);a.rect(754,29,205*PApplet.constrain((vehicle.x-80)/1890,0,1),9,4);
        }else {a.fill(214,191,226);a.textSize(13);a.text("JUAN · CANDIDATO",810,34);}
        if(!dialogue.active()&&scenes.current!=Phase.COMPLETE&&scenes.current!=Phase.RETURN){
            a.fill(33,21,47,215);a.rect(0,538,W,38);a.fill(245,232,251);a.textSize(14);
            a.text(notice,20,563);
            if(scenes.current==Phase.BATTLE)a.text("WASD mover · J espada · K escudo (5 s) · SHIFT esquivar",530,563);
        }
    }
    void drawDialogue(processing.core.PGraphics a){
        DialogueManager.Line line=dialogue.line();if(line==null)return;
        pixelPanel(a,42,406,940,146,0xf621152f);
        a.noStroke();a.fill(56,38,75);a.rect(59,421,92,98,4);
        if(line.portrait().equals("palomo")){a.image(sprites.palomo(),65,420,80,95);}
        else sprites.draw(a,line.portrait(),105,521,70,93);
        a.fill(244,203,106);a.textSize(18);a.text(line.speaker(),170,443);
        a.fill(245,232,251);a.textSize(17);
        String visible=line.text().substring(0,dialogue.visible());
        wrapText(a,visible,170,476,775,23);
        a.fill(214,191,226);a.textSize(11);a.text("ENTER · continuar",813,534);
    }
    void wrapText(processing.core.PGraphics a,String text,float x,float y,float max,float line){
        StringBuilder current=new StringBuilder();for(String word:text.split(" ")){
            String next=current.isEmpty()?word:current+" "+word;
            if(a.textWidth(next)>max && !current.isEmpty()){a.text(current.toString(),x,y);y+=line;current=new StringBuilder(word);}
            else current=new StringBuilder(next);
        }a.text(current.toString(),x,y);
    }
    /** Marco compartido de Ciudad Nova: sombra, borde violeta y esquinas doradas. */
    void pixelPanel(processing.core.PGraphics a,float x,float y,float w,float h,int color){
        alcaldedigital.cliente.interfaz.MarcosProcessing.marco(a,x,y,w,h,color,0xffbd91d5);
    }
    void pixelButton(processing.core.PGraphics a,float x,float y,float w,float h,String label,boolean active){
        alcaldedigital.cliente.interfaz.MarcosProcessing.marco(a,x,y,w,h,active?0xfff4cb6a:0xff604077,0xffbd91d5);
        a.fill(active?0xff21152f:0xfff5e8fb);a.textSize(17);a.textAlign(PApplet.CENTER,PApplet.CENTER);
        a.text(label,x+w/2,y+h/2-1);a.textAlign(PApplet.LEFT,PApplet.BASELINE);
    }
    void drawChoice(processing.core.PGraphics a,String title,String first,String second,String hint){
        pixelPanel(a,643,214,345,166,0xee21152f);
        a.fill(244,203,106);a.textSize(15);a.text(title,661,242);
        pixelButton(a,661,261,145,58,first,selected==0);
        pixelButton(a,817,261,145,58,second,selected==1);
        a.fill(214,191,226);a.textSize(12);a.text(hint,663,352);
    }
    void drawRumor(processing.core.PGraphics a){
        a.noStroke();a.fill(16,11,30,175);a.rect(0,56,W,H-56);
        pixelPanel(a,197,100,630,390,0xff2d1f40);
        a.fill(244,203,106);a.textSize(18);a.text("CIVITAS  ·  TENDENCIA #"+(encounter+1),226,133);
        a.image(sprites.shared("arte/iconos/16_desinformacion"),227,144,35,35);
        a.fill(230,201,243);a.textSize(14);a.text("@CadenasSinFuente · hace un momento",271,166);
        a.fill(33,21,47);a.rect(221,187,582,96,3);a.fill(245,232,251);a.textSize(19);
        wrapText(a,RUMORS[encounter],240,221,542,26);
        a.fill(60,43,75);a.rect(221,296,582,54,3);
        a.image(sprites.shared("arte/iconos/08_verificar"),231,307,28,28);
        a.fill(244,203,106);a.textSize(12);a.text("PISTA",271,313);
        a.fill(232,214,239);a.textSize(13);wrapText(a,CLUES[encounter],271,334,512,16);
        pixelButton(a,228,365,278,48,"INVESTIGAR [V]",selected==0);
        pixelButton(a,518,365,278,48,"RESPONDER [N]",selected==1);
        a.fill(214,191,226);a.textSize(12);
        a.text("Investigar preserva salud y da más tiempo antes del combate.",228,454);
    }
    void drawDownload(processing.core.PGraphics a){
        a.noStroke();a.fill(22,13,35,180);a.rect(0,55,W,H-55);
        pixelPanel(a,266,132,492,310,0xff38264b);
        a.image(sprites.shared("arte/iconos/01_celular"),296,166,55,55);
        a.fill(244,203,106);a.textSize(32);a.text("CIVITA",365,203);
        a.fill(245,232,251);a.textSize(18);a.text("La ciudad está hablando de ti.",309,255);
        a.fill(213,191,226);a.textSize(15);a.text("Descarga la red y sigue el origen de los rumores.",309,286);
        pixelButton(a,322,329,380,55,"ABRIR CIVITA  [ENTER]",true);
    }
    void drawComplete(processing.core.PGraphics a){
        a.noStroke();a.fill(20,13,30,225);a.rect(0,0,W,H);
        pixelPanel(a,161,122,702,332,0xff38264b);
        a.fill(244,203,106);a.textAlign(PApplet.CENTER);a.textSize(42);a.text("MISIÓN COMPLETADA",512,233);
        a.fill(245,232,251);a.textSize(20);a.text("La plaza quedó vacía, pero Juan escuchó la señal.",512,290);
        a.fill(213,191,226);a.textSize(17);a.text("Fuentes revisadas: "+approval+(civita.activa?" / 4":" / 3")+"    ·    Amenazas detenidas: "+combat.kills+" / 3",512,332);
        a.text(civita.activa?"Fichas de red: "+Integer.bitCount(civita.fichas)+" / 4":
            "Vecinos escuchados en el camino: "+((citizensHelped[0]?1:0)+(citizensHelped[1]?1:0))+" / 2",512,365);
        a.textSize(15);a.text("ENTER · volver al selector · ESC/P: pausa",512,410);a.textAlign(PApplet.LEFT);
    }
    void drawPrompt(processing.core.PGraphics a,float x,float y,String text){
        a.noStroke();a.fill(13,8,24,150);a.rect(x-67,y-25,140,31);a.fill(48,30,65,240);a.stroke(189,145,213);a.strokeWeight(2);a.rect(x-70,y-28,140,31,3);a.noStroke();a.fill(244,203,106);a.rect(x-68,y-26,5,5);a.textSize(13);a.text(text,x-60,y-8);
    }
}
