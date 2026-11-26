package alcaldedigital.cliente.mision4;

import java.util.ArrayList;
import java.util.List;

/** Footprint collisions, independently of a sprite's visible hair or equipment. */
final class CollisionWorld {
    record Box(float x,float y,float w,float h) {
        boolean overlaps(float a,float b,float aw,float ah) {
            return a<x+w && a+aw>x && b<y+h && b+ah>y;
        }
    }
    private final List<Box> solids=new ArrayList<>();
    private final float width,height;
    CollisionWorld(float width,float height) {this.width=width;this.height=height;}
    void add(float x,float y,float w,float h) {solids.add(new Box(x,y,w,h));}
    boolean blocked(float x,float y,float radius) {
        if(x-radius<12||x+radius>width-12||y-radius<24||y+radius>height-16)return true;
        for(Box solid:solids)if(solid.overlaps(x-radius,y-radius,radius*2,radius*2))return true;
        return false;
    }
    boolean blockedRect(float x,float y,float halfWidth,float halfHeight) {
        if(x-halfWidth<12||x+halfWidth>width-12||y-halfHeight<24||y+halfHeight>height-16)return true;
        for(Box solid:solids)if(solid.overlaps(x-halfWidth,y-halfHeight,halfWidth*2,halfHeight*2))return true;
        return false;
    }
    void moveVehicle(Actor actor,float dx,float dy) {
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(dx),Math.abs(dy))/4));
        for(int i=0;i<steps;i++){
            if(!blockedRect(actor.x+dx/steps,actor.y,52,20))actor.x+=dx/steps;
            if(!blockedRect(actor.x,actor.y+dy/steps,52,20))actor.y+=dy/steps;
        }
    }
    void move(Actor actor,float dx,float dy) {
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(dx),Math.abs(dy))/4));
        for(int i=0;i<steps;i++) {
            if(!blocked(actor.x+dx/steps,actor.y,actor.radius))actor.x+=dx/steps;
            if(!blocked(actor.x,actor.y+dy/steps,actor.radius))actor.y+=dy/steps;
        }
    }
    static CollisionWorld plaza() {
        CollisionWorld c=new CollisionWorld(1024,576);
        c.add(74,22,37,392); c.add(0,0,142,260); // wall and foliage
        c.add(142,55,343,240); // stage, approached via its stairs
        c.add(130,310,46,30); c.add(251,309,51,31); // staircase rails
        c.add(0,452,351,65); c.add(902,0,122,291); // fences and buildings
        c.add(8,67,82,82);c.add(795,64,82,70);c.add(123,5,105,60);
        c.add(433,0,97,68);c.add(880,196,25,39); // trees and furniture
        c.add(855,405,116,38); // mayor's car
        return c;
    }
    static CollisionWorld camino() {
        CollisionWorld c=new CollisionWorld(2180,576);
        c.add(0,0,2180,150);c.add(0,510,2180,66);
        c.add(680,157,64,80);c.add(755,454,74,56);
        c.add(1160,159,70,82);c.add(1250,450,66,56);
        c.add(1815,155,88,75);c.add(1870,458,69,50);
        for(int i=0;i<8;i++){
            int x=250+i*260;
            c.add(x+18,492,i%2==0?13:33,15);
        }
        c.add(1988,210,25,25);
        return c;
    }
}

class Actor {
    float x,y,radius=10, invulnerable, dodge, knockX,knockY,walk;
    int direction=0; // front, back, left, right
    Actor(float x,float y){this.x=x;this.y=y;}
    void move(float dx,float dy,CollisionWorld world,float dt) {
        if(dx==0&&dy==0)return;
        float n=(float)Math.hypot(dx,dy);dx/=n;dy/=n;
        direction=Math.abs(dx)>Math.abs(dy)?(dx<0?2:3):(dy<0?1:0);
        float ox=x,oy=y;world.move(this,dx*dt*speed(),dy*dt*speed());if(x!=ox||y!=oy)walk+=dt*10;
    }
    void move(float dx,float dy,CollisionWorld world,float dt,float multiplier) {
        move(dx,dy,world,dt*multiplier);
    }
    float speed(){return dodge>0?280:155;}
}
