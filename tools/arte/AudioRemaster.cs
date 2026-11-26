using System;
using System.IO;

public static class AudioRemaster {
    const int Tasa = 22050;
    const double Tau = Math.PI * 2;

    public static void Producir(string destino) {
        Directory.CreateDirectory(destino);
        Papel(destino,"papel",2790,71);
        Papel(destino,"pagina",1610,295);
        Barrido(destino,"red-alarma",.34,714,423,.22,2895,.024,31);
        Barrido(destino,"red-estacion",.18,690,1380,.23,3250,.001,78);
        Golpe(destino,"red-fallo",.30,198,53,.27,.18,38);

        Barrido(destino,"nova-ping",.125,1030,392,.18,870,.082,44);
        Barrido(destino,"nova-flash",.34,490,1210,.21,2210,.06,11);
        Barrido(destino,"nova-deslizar",.205,340,102,.095,2200,.27,145);
        Barrido(destino,"nova-privado",.23,780,450,.16,1560,.035,91);
        Barrido(destino,"nova-live",.44,570,1350,.19,2510,.062,415);
        Golpe(destino,"nova-impacto",.18,147,53,.27,.19,121);

        Barrido(destino,"civita-portal",.52,173,710,.20,1820,.055,72);
        Barrido(destino,"civita-espada",.165,420,122,.08,2910,.23,229);
        Barrido(destino,"civita-escudo",.20,820,690,.28,1430,.052,81);
        Golpe(destino,"civita-impacto",.16,225,74,.26,.23,245);

        Barrido(destino,"servidor-alarma",.38,291,412,.23,875,.034,105);
        Barrido(destino,"servidor-turno",.23,630,945,.21,1610,.016,461);
        Golpe(destino,"servidor-impacto",.21,125,48,.25,.23,111);
        Barrido(destino,"servidor-confirmar",.15,770,950,.18,1680,.012,128);

        Golpe(destino,"vuelo-canon-a",.075,238,85,.21,.21,236);
        Golpe(destino,"vuelo-canon-b",.086,268,75,.24,.19,172);
        Golpe(destino,"vuelo-colision",.19,125,47,.25,.27,893);
        Barrido(destino,"vuelo-dron",.195,660,187,.23,1580,.083,126);
        Golpe(destino,"vuelo-misil",.35,118,41,.31,.29,761);

        Golpe(destino,"carrera-paso-a",.095,197,74,.16,.23,994);
        Golpe(destino,"carrera-paso-b",.094,213,85,.14,.20,885);
        Barrido(destino,"carrera-prensa",.195,760,1140,.18,2370,.058,617);
        Barrido(destino,"carrera-alerta",.24,770,641,.17,1396,.021,597);

        Barrido(destino,"final-carga",.30,410,690,.15,1325,.07,556);
        Barrido(destino,"final-orbe",.22,660,280,.23,1560,.068,331);
        Barrido(destino,"final-descarga",.24,270,810,.18,2850,.15,767);
        Golpe(destino,"final-impacto",.18,185,57,.26,.21,719);
    }

    static void Barrido(string ruta,string nombre,double dur,double ini,double fin,double tono,
                        double brillo,double textura,int semilla) {
        var ruido=new Random(semilla);
        double fase=0, pasa=0;
        Escribir(ruta,nombre,dur,t=>{
            double u=t/dur;
            double hz=ini+(fin-ini)*u*u;
            fase+=Tau*hz/Tasa;
            double bruto=ruido.NextDouble()*2-1;
            pasa+=.18*(bruto-pasa);
            double sobre=Sobre(u,.075,3.9);
            return sobre*(tono*(Math.Sin(fase)+.22*Math.Sin(fase*2.004))
                + textura*(bruto-pasa)*Math.Cos(Tau*brillo*t)*Math.Exp(-u*2));
        });
    }

    static void Golpe(string ruta,string nombre,double dur,double ini,double fin,double cuerpo,double grano,int semilla) {
        var ruido=new Random(semilla);
        double fase=0, lento=0, medio=0;
        Escribir(ruta,nombre,dur,t=>{
            double u=t/dur;
            fase+=Tau*(fin+(ini-fin)*Math.Exp(-6*u))/Tasa;
            double src=ruido.NextDouble()*2-1;
            lento+=.13*(src-lento);
            medio+=.48*(src-medio);
            return Sobre(u,.034,5.7)*(cuerpo*Math.Sin(fase)+grano*(medio-lento)*Math.Exp(-2.2*u));
        });
    }

    static void Papel(string ruta,string nombre,double frecuencia,int semilla) {
        var ruido=new Random(semilla);
        double lento=0, rapido=0;
        Escribir(ruta,nombre,.265,t=>{
            double src=ruido.NextDouble()*2-1;
            lento+=.16*(src-lento); rapido+=.67*(src-rapido);
            double detalle=(rapido-lento)*(.6+.4*Math.Sin(Tau*frecuencia*t));
            double hojas=.66*Sobre(t/.265,.25,1.45)
                +.35*Sobre((t-.107)/.158,.17,4.5);
            return detalle*hojas*.40;
        });
    }

    static double Sobre(double u,double ataque,double cola) {
        if(u<=0||u>=1)return 0;
        return Math.Min(1,u/ataque)*Math.Exp(-cola*u)*Math.Min(1,(1-u)*9);
    }

    static void Escribir(string ruta,string nombre,double segundos,Func<double,double> producir) {
        int frames=(int)Math.Round(segundos*Tasa);
        using(var f=new BinaryWriter(File.Create(Path.Combine(ruta,nombre+".wav")))){
            f.Write(new char[]{'R','I','F','F'}); f.Write(36+frames*2);
            f.Write(new char[]{'W','A','V','E','f','m','t',' '}); f.Write(16);
            f.Write((short)1);f.Write((short)1);f.Write(Tasa);f.Write(Tasa*2);f.Write((short)2);f.Write((short)16);
            f.Write(new char[]{'d','a','t','a'});f.Write(frames*2);
            for(int i=0;i<frames;i++){
                double valor=producir(i/(double)Tasa);
                f.Write((short)(32767*Math.Max(-.96,Math.Min(.96,valor))));
            }
        }
    }
}
