package alcaldedigital.cliente.mision4;

import processing.core.PApplet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Three authored enemy patterns with telegraphs, recovery, shield, knockback and hit feedback. */
final class CombatSystem {
    enum Pattern { TROLL, SPAMBOT, DEEPFAKE }
    record Shot(float x,float y,float vx,float vy,float age) {}
    static final class Enemy extends Actor {
        final Pattern pattern;
        final int maxHp;
        int hp;
        float windup, recovery, cooldown, flash, age, targetX,targetY,attackTimer,dashTime,dashX,dashY;
        boolean firing;
        Enemy(Pattern pattern,float x,float y,int hp){super(x,y);this.pattern=pattern;this.hp=this.maxHp=hp;radius=15;}
    }
    final List<Shot> shots=new ArrayList<>();
    final List<Particle> particles=new ArrayList<>();
    final Random random=new Random(44);
    Enemy enemy;
    float swordTime, swordCooldown, shieldLeft=5, shieldRecover, screenShake, dodgeCooldown;
    private static final float[] DURACION_GOLPES={.32f,.34f,.46f}, PREPARACION={.075f,.09f,.12f};
    private float tiempoGolpe, golpeEnCola, ventanaCombo;
    private int combo,direccionGolpe;
    private boolean impacto;
    boolean guarding;
    int health=5, maxHealth=5, kills;
    record Particle(float x,float y,float vx,float vy,float time,int color) {}
    void spawn(int index,float x,float y){
        shots.clear();particles.clear();shieldLeft=5;shieldRecover=0;guarding=false;dodgeCooldown=0;
        tiempoGolpe=golpeEnCola=ventanaCombo=0;combo=0;
        enemy=new Enemy(Pattern.values()[index],x,y,3+index*2);
        enemy.cooldown=1;health=Math.max(health,3);
    }
    void attack(Actor hero) {
        if(enemy==null||swordCooldown>0||guarding)return;
        swordCooldown=.34f;swordTime=.16f;
        float dx=enemy.x-hero.x,dy=enemy.y-hero.y;
        float dist=(float)Math.hypot(dx,dy);
        float facing=switch(hero.direction){case 0->dy;case 1->-dy;case 2->-dx;default->dx;};
        if(dist<62 && facing>dist*.3f && enemy.flash<=0){
            impacto(dx,dy,dist,1);
        }
    }
    boolean golpeCivitaActivo(){return tiempoGolpe>0;}
    int direccionCivita(){return direccionGolpe;}
    float progresoGolpe(){return golpeCivitaActivo()?1-tiempoGolpe/DURACION_GOLPES[combo]:0;}
    float faseArco(){
        return Math.clamp((DURACION_GOLPES[combo]-tiempoGolpe-PREPARACION[combo])/.15f,0,1);
    }
    void ataqueCivita(Actor hero){
        if(enemy==null||guarding||hero.dodge>0)return;
        if(golpeCivitaActivo()){
            if(tiempoGolpe<=.16f&&combo<2)golpeEnCola=.19f;
            return;
        }
        iniciarGolpe(hero,ventanaCombo>0&&combo<2?combo+1:0);
    }
    private void iniciarGolpe(Actor hero,int siguiente){
        combo=siguiente;direccionGolpe=hero.direction;
        tiempoGolpe=DURACION_GOLPES[combo];golpeEnCola=0;ventanaCombo=0;impacto=false;
        swordTime=.16f;
    }
    private void avanzarGolpe(float dt,Actor hero,CollisionWorld world){
        golpeEnCola=Math.max(0,golpeEnCola-dt);ventanaCombo=Math.max(0,ventanaCombo-dt);
        if(!golpeCivitaActivo())return;
        tiempoGolpe=Math.max(0,tiempoGolpe-dt);
        if(!impacto&&DURACION_GOLPES[combo]-tiempoGolpe>=PREPARACION[combo]){
            impacto=true;
            if(enemy!=null){
                float dx=enemy.x-hero.x,dy=enemy.y-hero.y,dist=(float)Math.hypot(dx,dy);
                float frente=switch(direccionGolpe){case 0->dy;case 1->-dy;case 2->-dx;default->dx;};
                float radio=combo==2?75:66;
                if(dist<=radio&&(dist<9||frente>=dist*.30f)&&enemy.flash<=0&&
                    !world.blocked(hero.x+dx*.5f,hero.y+dy*.5f,3))impacto(dx,dy,dist,combo==2?2:1);
            }
        }
        if(tiempoGolpe==0){
            if(golpeEnCola>0&&combo<2&&enemy!=null&&!guarding)iniciarGolpe(hero,combo+1);
            else ventanaCombo=.20f;
        }
    }
    private void impacto(float dx,float dy,float dist,int fuerza){
        enemy.hp-=fuerza;enemy.flash=.23f;enemy.recovery=.48f;enemy.windup=0;enemy.dashTime=0;
        float n=Math.max(1,dist);enemy.knockX=dx/n*(fuerza==2?19:13);enemy.knockY=dy/n*(fuerza==2?19:13);
        burst(enemy.x,enemy.y,fuerza==2?0xffffe5a4:0xfff4cb6a,fuerza==2?22:13);screenShake=fuerza==2?8:5;
        if(enemy.hp<=0){kills++;burst(enemy.x,enemy.y,0xffd998ef,28);enemy=null;shots.clear();}
    }
    void dodge(Actor hero) {if(hero.dodge>0||hero.invulnerable>0||dodgeCooldown>0)return;hero.dodge=.22f;hero.invulnerable=.30f;dodgeCooldown=.8f;}
    void defend(boolean value){guarding=value && shieldLeft>0 && shieldRecover<=0;}
    void update(float dt,Actor hero,CollisionWorld world){
        swordTime=Math.max(0,swordTime-dt);swordCooldown=Math.max(0,swordCooldown-dt);
        dodgeCooldown=Math.max(0,dodgeCooldown-dt);
        shieldRecover=Math.max(0,shieldRecover-dt);screenShake=Math.max(0,screenShake-dt*25);
        hero.dodge=Math.max(0,hero.dodge-dt);
        avanzarGolpe(dt,hero,world);
        if(guarding){shieldLeft=Math.max(0,shieldLeft-dt);if(shieldLeft==0){guarding=false;shieldRecover=1.2f;}}
        else if(shieldRecover<=0)shieldLeft=Math.min(5,shieldLeft+dt*.3f);
        for(int i=particles.size()-1;i>=0;i--){Particle p=particles.get(i);if(p.time<=dt)particles.remove(i);else particles.set(i,new Particle(p.x+p.vx*dt,p.y+p.vy*dt,p.vx,p.vy,p.time-dt,p.color));}
        if(enemy==null)return;
        Enemy e=enemy;e.age+=dt;e.flash=Math.max(0,e.flash-dt);
        if(e.knockX!=0||e.knockY!=0){world.move(e,e.knockX,e.knockY);e.knockX=e.knockY=0;}
        float dx=hero.x-e.x,dy=hero.y-e.y,dist=(float)Math.hypot(dx,dy),n=Math.max(1,dist);
        if(e.dashTime>0){
            e.dashTime=Math.max(0,e.dashTime-dt);
            world.move(e,e.dashX*dt,e.dashY*dt);
            if(Math.hypot(hero.x-e.x,hero.y-e.y)<43)damage(hero,e.x,e.y);
        }
        else if(e.recovery>0)e.recovery-=dt;
        else if(e.windup>0){
            e.windup-=dt;
            if(e.windup<=0){
                dx=e.targetX-e.x;dy=e.targetY-e.y;n=Math.max(1,(float)Math.hypot(dx,dy));
                if(e.pattern==Pattern.TROLL){
                    e.dashX=dx/n*280;e.dashY=dy/n*280;e.dashTime=.32f;
                }else if(e.pattern==Pattern.SPAMBOT){
                    shoot(e.x,e.y,dx/n,dy/n,175);
                    shoot(e.x,e.y,(dx/n*.87f-dy/n*.5f),(dy/n*.87f+dx/n*.5f),158);
                    shoot(e.x,e.y,(dx/n*.87f+dy/n*.5f),(dy/n*.87f-dx/n*.5f),158);
                }else {
                    for(int j=-1;j<=1;j++)shoot(e.x,e.y,dx/n*(210-j*12)-dy/n*j*50,dy/n*(210-j*12)+dx/n*j*50,1);
                    if(dist<62)damage(hero,e.x,e.y);
                }
                e.recovery=.55f;e.cooldown=e.pattern==Pattern.DEEPFAKE?.65f:1.05f;
                burst(e.x,e.y,0xffd588e2,9);
            }
        }else if(e.recovery<=0){
            e.cooldown-=dt;
            if(e.cooldown<=0 && dist<245){e.windup=e.pattern==Pattern.DEEPFAKE?.95f:.75f;e.targetX=hero.x;e.targetY=hero.y;e.firing=true;}
            else if(dist>52 && dist<430){float speed=e.pattern==Pattern.TROLL?72:e.pattern==Pattern.SPAMBOT?47:62;world.move(e,dx/n*speed*dt,dy/n*speed*dt);}
            else if(dist<42 && e.pattern!=Pattern.TROLL)world.move(e,-dx/n*48*dt,-dy/n*48*dt);
        }
        for(int i=shots.size()-1;i>=0;i--){Shot s=shots.get(i);float xx=s.x+s.vx*dt,yy=s.y+s.vy*dt;
            if(s.age>2.3||world.blocked(xx,yy,5)){shots.remove(i);continue;}
            if(Math.hypot(xx-hero.x,yy-hero.y)<hero.radius+10){damage(hero,xx,yy);shots.remove(i);continue;}
            shots.set(i,new Shot(xx,yy,s.vx,s.vy,s.age+dt));
        }
    }
    private void shoot(float x,float y,float vx,float vy,float speed){shots.add(new Shot(x,y,vx*speed,vy*speed,0));}
    private void damage(Actor hero,float sourceX,float sourceY){
        float dx=hero.x-sourceX,dy=hero.y-sourceY,n=Math.max(1,(float)Math.hypot(dx,dy));
        float facing=switch(hero.direction){case 0->-dy;case 1->dy;case 2->dx;default->-dx;};
        if(guarding && facing>=0){shieldLeft=Math.max(0,shieldLeft-.35f);burst(hero.x,hero.y,0xfff4cb6a,10);screenShake=2;return;}
        if(hero.invulnerable>0)return;
        health--;hero.invulnerable=.85f;worldKnock(hero,dx/n*24,dy/n*24);screenShake=9;burst(hero.x,hero.y,0xffe56892,13);
    }
    private void worldKnock(Actor hero,float dx,float dy){hero.knockX=dx;hero.knockY=dy;}
    private void burst(float x,float y,int color,int count){
        for(int i=0;i<count;i++){double a=random.nextDouble()*Math.PI*2;float speed=25+random.nextFloat()*110;
            particles.add(new Particle(x,y,(float)Math.cos(a)*speed,(float)Math.sin(a)*speed,.25f+random.nextFloat()*.4f,color));}
    }
    void drawEffects(processing.core.PGraphics a,Actor hero){
        a.noStroke();
        for(Shot s:shots){a.fill(42,23,59,130);a.rect(s.x-8,s.y-7,25,17,3);a.fill(236,144,200);a.textSize(16);a.text("!",s.x+1,s.y+6);}
        for(Particle p:particles){a.fill(p.color);a.rect(p.x,p.y,4,4);}
        if(golpeCivitaActivo()&&faseArco()>0&&faseArco()<1){
            float ang=switch(direccionGolpe){case 0->PApplet.HALF_PI;case 1->-PApplet.HALF_PI;case 2->PApplet.PI;default->0;};
            float fase=combo==1?1-faseArco():faseArco(),barrido=ang-1.27f+fase*2.54f;
            float ancho=combo==2?142:124;
            a.noFill();a.stroke(181,100,228,165);a.strokeWeight(10);
            a.arc(hero.x,hero.y-8,ancho,ancho,barrido-.36f,barrido+.36f);
            a.stroke(255,224,153,227);a.strokeWeight(4);
            a.arc(hero.x,hero.y-8,ancho,ancho,barrido-.29f,barrido+.29f);a.noStroke();
        }else if(swordTime>0&&!golpeCivitaActivo()){a.noFill();a.stroke(245,223,142,230);a.strokeWeight(6);
            float ang=switch(hero.direction){case 0->PApplet.HALF_PI;case 1->-PApplet.HALF_PI;case 2->PApplet.PI;default->0;};
            a.arc(hero.x,hero.y-12,100,100,ang-.75f,ang+.75f);a.noStroke();}
        if(guarding){a.noFill();a.stroke(240,202,104,210);a.strokeWeight(4);a.arc(hero.x,hero.y-18,80,80,0,PApplet.TWO_PI);a.noStroke();}
    }
}
