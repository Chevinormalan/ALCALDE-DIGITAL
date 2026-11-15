package alcaldedigital.cliente.mision4;

import java.util.ArrayDeque;
import java.util.Queue;

/** Progressive speech; Enter reveals a line before it can advance. */
final class DialogueManager {
    record Line(String speaker,String text,String portrait) {}
    private final Queue<Line> pending=new ArrayDeque<>();
    private Line current;
    private float characters;
    void start(Line... lines) {pending.clear();current=null;characters=0;for(Line l:lines)pending.add(l);next();}
    void next() {current=pending.poll();characters=0;}
    boolean active(){return current!=null;}
    Line line(){return current;}
    int visible(){return current==null?0:Math.min(current.text().length(),(int)characters);}
    void update(float dt){if(current!=null)characters=Math.min(current.text().length(),characters+dt*42);}
    boolean advance(){
        if(current==null)return true;
        if(visible()<current.text().length()){characters=current.text().length();return false;}
        next();return current==null;
    }
}
