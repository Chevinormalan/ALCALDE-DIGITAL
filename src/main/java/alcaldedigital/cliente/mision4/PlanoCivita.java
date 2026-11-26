package alcaldedigital.cliente.mision4;

/** Huellas de tránsito trazadas sobre el mapa del PDF, en sus coordenadas de 1024 × 765. */
final class PlanoCivita {
    static final int ANCHO=1024, ALTO=765;
    private PlanoCivita(){}

    static boolean transitable(float x,float y) {
        if(x<20||x>1004||y<20||y>745)return false;
        if(dis(x,y,511,384)<70||dis(x,y,141,124)<27||dis(x,y,413,59)<19||
            dis(x,y,708,673)<21||dis(x,y,920,673)<21||dis(x,y,920,503)<20) return false;
        if(caja(x,y,491,18,535,747)||caja(x,y,19,363,1005,395)||
            dis(x,y,511,384)<137)return true;
        if(caja(x,y,62,65,216,177)||caja(x,y,59,255,214,306)||
            camino(x,y,22,139,165,139,370)||camino(x,y,17,197,114,511,114))return true;
        if(camino(x,y,22,284,379,284,282,324,252,355,222,380,197,512,197)||
            camino(x,y,21,512,197,646,197,677,224,710,250,744,285,744,379)||
            camino(x,y,22,260,381,282,493,324,534,370,562,512,562,651,562,699,535,730,491,745,384))return true;
        if(camino(x,y,25,873,382,880,321,845,277,813,275,814,209,844,200,904,202,923,154,902,101)||
            camino(x,y,23,844,274,917,280,951,318,951,381))return true;
        if(camino(x,y,25,512,675,690,675,919,675,919,502,777,502,742,474,743,386)||
            camino(x,y,25,918,503,880,458,863,412,863,385)||
            dis(x,y,708,673)<45||dis(x,y,920,673)<48||dis(x,y,920,503)<43)return true;
        return camino(x,y,27,511,728,442,728,437,648,388,616,318,617,270,582,205,585,166,566,104,572,88,548)||
            camino(x,y,23,204,584,260,643,274,716,330,732,437,728)||
            camino(x,y,22,318,617,317,556,324,534);
    }
    private static boolean caja(float x,float y,int a,int b,int c,int d){return x>=a&&x<=c&&y>=b&&y<=d;}
    private static double dis(float x,float y,float a,float b){return Math.hypot(x-a,y-b);}
    private static boolean camino(float x,float y,int ancho,int... puntos) {
        for(int i=2;i<puntos.length;i+=2) {
            float ax=puntos[i-2],ay=puntos[i-1],dx=puntos[i]-ax,dy=puntos[i+1]-ay;
            float t=Math.clamp(((x-ax)*dx+(y-ay)*dy)/(dx*dx+dy*dy),0,1);
            if(dis(x,y,ax+dx*t,ay+dy*t)<=ancho)return true;
        }
        return false;
    }
}
