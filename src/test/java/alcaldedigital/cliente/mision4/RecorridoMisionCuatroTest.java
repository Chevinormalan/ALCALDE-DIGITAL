package alcaldedigital.cliente.mision4;

import java.util.ArrayDeque;
import org.junit.jupiter.api.Test;
import static alcaldedigital.cliente.mision4.Civita.*;
import static java.awt.event.KeyEvent.*;
import static org.junit.jupiter.api.Assertions.*;

class RecorridoMisionCuatroTest {
    private static void paso(MisionCuatroGame g,int ticks){for(int i=0;i<ticks;i++)g.update(1f/60);}
    private static void tecla(MisionCuatroGame g,int k){g.press(k,(char)k);g.release(k);}
    private static void dialogo(MisionCuatroGame g){
        var fase=g.scenes.current;
        for(int i=0;i<24&&g.scenes.current==fase&&g.dialogue.active();i++)tecla(g,VK_ENTER);
    }

    @Test void recorridoCompletoConTarimaExpedientesCombatesYRegreso() {
        var g=new MisionCuatroGame();
        g.press(VK_D,'d');paso(g,600);g.release(VK_D);
        assertTrue(g.vehicle.x>1970);tecla(g,VK_ENTER);assertEquals(MisionCuatroGame.Phase.PLAZA,g.scenes.current);
        g.press(VK_A,'a');for(int i=0;i<400&&g.hero.x>214;i++)paso(g,1);g.release(VK_A);
        g.press(VK_W,'w');for(int i=0;i<100&&g.hero.y>370;i++)paso(g,1);g.release(VK_W);
        tecla(g,VK_ENTER);assertEquals(MisionCuatroGame.Phase.STAGE,g.scenes.current);paso(g,90);tecla(g,VK_ENTER);
        for(int i=0;i<4;i++){
            assertEquals(MisionCuatroGame.Phase.QUESTION,g.scenes.current);dialogo(g);
            assertEquals(MisionCuatroGame.Phase.ANSWER,g.scenes.current);tecla(g,VK_N);dialogo(g);
        }
        assertEquals(MisionCuatroGame.Phase.CONFUSED,g.scenes.current);dialogo(g);
        assertEquals(MisionCuatroGame.Phase.PALOMO,g.scenes.current);dialogo(g);paso(g,350);
        assertEquals(MisionCuatroGame.Phase.DOWNLOAD,g.scenes.current);tecla(g,VK_ENTER);
        assertEquals(MisionCuatroGame.Phase.CIVITA_ENTRADA,g.scenes.current);paso(g,295);
        assertEquals(MisionCuatroGame.Phase.CIVITA,g.scenes.current);
        for(var pista:FICHAS)assertNotNull(vecinoDeRuta(g.civita,INICIO,pista));
        ir(g,ARCHIVOS[0].papel());tecla(g,VK_E);assertFalse(g.civita.visto(0));
        tecla(g,VK_F);assertTrue(g.civita.linterna);
        ir(g,FICHAS[0]);tecla(g,VK_E);
        assertEquals(1,g.civita.fichas);assertFalse(g.civita.rastro(g.hero).isEmpty());
        var espera=MisionCuatroGame.restore(g.checkpoint());assertNotNull(espera);assertTrue(espera.civita.linterna);
        for(int i=0;i<ARCHIVOS.length;i++){
            var arc=ARCHIVOS[i];
            ir(g,arc.papel());tecla(g,VK_E);assertEquals(Hoja.EXPEDIENTE,g.civita.hoja);
            paso(g,20);tecla(g,VK_E);
            if(arc.guardian()>=0&&!g.civita.vencido(arc.guardian()))ir(g,GUARDIANES[arc.guardian()]);
            ir(g,arc.terminal());tecla(g,VK_E);assertEquals(Hoja.CONTRASTE,g.civita.hoja);
            assertEquals(i,g.civita.total());paso(g,50);tecla(g,VK_E);
            assertEquals(i+1,g.civita.total());
        }
        var copia=MisionCuatroGame.restore(g.checkpoint());
        assertNotNull(copia);assertEquals(4,copia.civita.total());assertEquals(2,copia.combat.kills);
        ir(g,NUCLEO);g.hero.direction=1;paso(g,25);tecla(g,VK_E);
        assertNotNull(g.combat.enemy);combate(g);
        tecla(g,VK_E);if(g.scenes.current==MisionCuatroGame.Phase.CIVITA){ir(g,NUCLEO);tecla(g,VK_E);}
        assertEquals(MisionCuatroGame.Phase.CIVITA_SALIDA,g.scenes.current);paso(g,195);
        assertEquals(MisionCuatroGame.Phase.EPILOGUE,g.scenes.current);assertTrue(g.audience.stream().allMatch(c->c.exited));
        dialogo(g);assertTrue(g.ending());assertEquals(3,g.combat.kills);assertEquals(4,g.approval);assertEquals(4,g.answered);
        assertArrayEquals(new int[]{0,0,0,0},g.respuestas);
        assertArrayEquals(g.respuestas,MisionCuatroGame.restore(g.checkpoint()).respuestas);
    }

    private static void ir(MisionCuatroGame g,Punto destino) {
        for(int intento=0;intento<180;intento++){
            if(g.combat.enemy!=null)combate(g);
            if(Math.hypot(g.hero.x-destino.x(),g.hero.y-destino.y())<10)return;
            Punto actual=new Punto((int)(g.hero.x/BALDOSA),(int)(g.hero.y/BALDOSA));
            Punto siguiente=vecinoDeRuta(g.civita,actual,destino);
            if(siguiente.equals(actual)){eje(g,destino.x(),true);eje(g,destino.y(),false);}
            else{eje(g,actual.x(),true);eje(g,actual.y(),false);eje(g,siguiente.x(),true);eje(g,siguiente.y(),false);}
        }
        fail("No se pudo recorrer Civita hasta "+destino);
    }
    private static void eje(MisionCuatroGame g,float destino,boolean horizontal) {
        float antes=horizontal?g.hero.x:g.hero.y;
        if(Math.abs(destino-antes)<3||g.combat.enemy!=null)return;
        int k=horizontal?(destino>antes?VK_D:VK_A):(destino>antes?VK_S:VK_W);
        g.press(k,(char)k);
        for(int i=0;i<40&&Math.abs((horizontal?g.hero.x:g.hero.y)-destino)>3&&g.combat.enemy==null;i++)paso(g,1);
        g.release(k);
    }
    private static Punto vecinoDeRuta(Civita mapa,Punto origen,Punto destino){
        if(origen.equals(destino))return origen;
        Punto[][] anterior=new Punto[FILAS][COLUMNAS];
        var pendientes=new ArrayDeque<Punto>();pendientes.add(origen);anterior[origen.fila()][origen.columna()]=origen;
        while(!pendientes.isEmpty()){
            Punto p=pendientes.removeFirst();
            for(int[] delta:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){
                int x=p.columna()+delta[0],y=p.fila()+delta[1];
                if(!mapa.suelo(x,y)||anterior[y][x]!=null)continue;
                anterior[y][x]=p;pendientes.add(new Punto(x,y));
            }
        }
        assertNotNull(anterior[destino.fila()][destino.columna()]);
        Punto paso=destino;
        while(!anterior[paso.fila()][paso.columna()].equals(origen))paso=anterior[paso.fila()][paso.columna()];
        return paso;
    }
    private static void combate(MisionCuatroGame g){
        for(int i=0;i<4800&&g.combat.enemy!=null;i++){
            var e=g.combat.enemy;float dx=e.x-g.hero.x,dy=e.y-g.hero.y;
            int direccion=Math.abs(dx)>Math.abs(dy)?(dx<0?2:3):(dy<0?1:0);
            boolean horizontal=direccion>=2;
            if(Math.hypot(dx,dy)>47){
                g.loseFocus();int k=horizontal?(dx<0?VK_A:VK_D):(dy<0?VK_W:VK_S);
                float antes=horizontal?g.hero.x:g.hero.y;g.press(k,(char)k);paso(g,1);g.release(k);
                if(antes==(horizontal?g.hero.x:g.hero.y)&&g.combat.enemy!=null){
                    int alterno=horizontal?(dy<0?VK_W:VK_S):(dx<0?VK_A:VK_D);
                    g.press(alterno,(char)alterno);paso(g,1);g.release(alterno);
                }
            }else {g.hero.direction=direccion;tecla(g,VK_J);paso(g,1);}
        }
        assertNull(g.combat.enemy,"El combate debe poder terminar");
    }

    @Test void restaurarConservaUnaPartidaAnteriorConRumores(){
        var g=new MisionCuatroGame();g.set(MisionCuatroGame.Phase.RUMOR);g.press(VK_V,'v');paso(g,10);
        var copia=MisionCuatroGame.restore(g.checkpoint());
        assertNotNull(copia);assertEquals(g.partida,copia.partida);assertEquals(1,copia.approval);
        assertEquals(g.combat.enemy.hp,copia.combat.enemy.hp);
        for(boolean key:copia.keys)assertFalse(key);
    }

    @Test void espadaAplicaElGolpeDuranteLaAnimacionYLaLuzRespetaElBorde(){
        var combate=new CombatSystem();var hero=new Actor(200,180);hero.direction=3;
        var piso=new CollisionWorld(600,600);combate.spawn(0,254,180);
        combate.ataqueCivita(hero);
        assertEquals(3,combate.enemy.hp);
        combate.update(.03f,hero,piso);assertEquals(3,combate.enemy.hp);
        combate.update(.05f,hero,piso);assertEquals(2,combate.enemy.hp);
        var mundo=new Civita();
        assertTrue(mundo.alcance(INICIO.x(),INICIO.y(),(float)Math.PI,RADIO_LUZ)<RADIO_LUZ);
    }
}
