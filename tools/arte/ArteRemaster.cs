using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;

public static class ArteRemaster {
    public static void Sofi(string origen, string destino) {
        using (var fuente = new Bitmap(origen))
        using (var atlas = new Bitmap(384,60)) {
            if (fuente.Width != 1536 || fuente.Height != 1024) throw new ArgumentException("Atlas de Sofi incompatible");
            // Los pies marcan el anclaje, incluso en la pose donde la mano toca el suelo.
            int[] pies = {262, 746, 1236, 256, 758, 1266};
            for (int pose = 0; pose < pies.Length; pose++) {
                for (int y = 0; y < 60; y++) for (int x = 0; x < 64; x++) {
                    int sx = pies[pose] + (x - 27) * 9;
                    int sy = 453 + pose / 3 * 512 + (y - 53) * 9;
                    if (sx < pose % 3 * 512 || sx >= (pose % 3 + 1) * 512 ||
                        sy < pose / 3 * 512 || sy >= (pose / 3 + 1) * 512) continue;
                    var pixel = fuente.GetPixel(sx, sy);
                    if (pixel.A >= 132) atlas.SetPixel(pose * 64 + x, y, Color.FromArgb(255,pixel));
                }
            }
            atlas.Save(destino, ImageFormat.Png);
        }
    }

    public static void Poses(string origen, string destino, int pivoteX, int linea1, int linea2, int altoModelo, int alfa) {
        using (var fuente = new Bitmap(origen))
        using (var atlas = new Bitmap(128 * 8,128)) {
            for (int i = 0; i < 8; i++) {
                int col = i % 4, fila = i / 4;
                int izq = col * fuente.Width / 4, der = (col + 1) * fuente.Width / 4;
                int arriba = fila * fuente.Height / 2, abajo = (fila + 1) * fuente.Height / 2;
                int pie = fila == 0 ? linea1 : linea2;
                double escala = altoModelo / 90.0;
                for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
                    int sx = izq + pivoteX + (int)Math.Round((x - 64) * escala);
                    int sy = arriba + pie + (int)Math.Round((y - 115) * escala);
                    if (sx < izq || sx >= der || sy < arriba || sy >= abajo) continue;
                    var pixel = fuente.GetPixel(sx,sy);
                    if (pixel.A >= alfa) atlas.SetPixel(i * 128 + x,y,Color.FromArgb(255,pixel));
                }
            }
            atlas.Save(destino,ImageFormat.Png);
        }
    }

    public static void Rivales(string origen, string destino) {
        string[] nombres = {"troll","spambot","deepfake"};
        int[] pivote = {180,190,173}, apoyo = {321,305,329}, altura = {291,284,304};
        using (var fuente = new Bitmap(origen)) {
            for (int f = 0; f < 3; f++) using (var atlas = new Bitmap(512,128)) {
                int arriba = f * fuente.Height / 3, abajo = (f + 1) * fuente.Height / 3;
                double escala = altura[f] / 90.0;
                for (int c = 0; c < 4; c++) {
                    int izq = c * fuente.Width / 4, der = (c+1) * fuente.Width / 4;
                    for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
                        int sx = izq + pivote[f] + (int)Math.Round((x-64)*escala);
                        int sy = arriba + apoyo[f] + (int)Math.Round((y-115)*escala);
                        if (sx < izq || sx >= der || sy < arriba || sy >= abajo) continue;
                        var pixel = fuente.GetPixel(sx,sy);
                        if (pixel.A >= 150) atlas.SetPixel(c*128+x,y,Color.FromArgb(255,pixel));
                    }
                }
                atlas.Save(Path.Combine(destino,nombres[f]+".png"),ImageFormat.Png);
            }
        }
    }

    public static void Helicoptero(string origen, string cuerpo, string rotor) {
        using (var nave = new Bitmap(origen)) {
            // Retira las aspas impresas. Conserva el buje, el fuselaje y la cola.
            for (int y = (int)(nave.Height * .101); y < nave.Height * .337; y++) {
                float vy = y * 360f / nave.Height;
                for (int x = (int)(nave.Width * .159); x < nave.Width * .943; x++) {
                    float vx = x * 720f / nave.Width;
                    bool izquierda = vx >= 114 && vx < 366 && (vy < 76 || vy > 85 && vy < 117 - (vx-165)*.12f);
                    bool derecha = vx > 430 && vy < 124;
                    if (izquierda || derecha) nave.SetPixel(x,y,Color.Transparent);
                }
            }
            nave.Save(cuerpo, ImageFormat.Png);
        }
        using (var imagen = new Bitmap(176 * 6, 40 * 2))
        using (var g = Graphics.FromImage(imagen)) {
            g.SmoothingMode = SmoothingMode.None;
            for (int i = 0; i < 12; i++) {
                var anterior = g.Save();
                g.TranslateTransform(i % 6 * 176 + 88, i / 6 * 40 + 20);
                // Proyecta cuatro aspas con perspectiva en cada fotograma.
                for (int pala = 0; pala < 4; pala++) {
                    double angulo = (i / 12.0 + pala) * Math.PI / 2 + .21;
                    Aspa(g,(float)Math.Cos(angulo),(float)Math.Sin(angulo),i > 0);
                }
                g.Restore(anterior);
            }
            imagen.Save(rotor, ImageFormat.Png);
        }
    }

    static void Aspa(Graphics g, float dx, float dy, bool activa) {
        var forma = new PointF[]{ new PointF(-dy*1.3f,dx*.9f),new PointF(dx*71-dy*4,dy*15+dx*2.2f),
            new PointF(dx*78-dy*2,dy*16+dx*.9f),new PointF(dx*78+dy*3,dy*16-dx*1.15f),
            new PointF(dx*64+dy*4,dy*14-dx*2.2f),new PointF(dy,-dx*.55f) };
        using (var tinta = new SolidBrush(Color.FromArgb(255,25,20,39)))
        using (var metal = new SolidBrush(Color.FromArgb(255,111+(int)((dy+1)*23),106,159)))
        using (var oro = new Pen(Color.FromArgb(255,255,201,102),2))
        using (var reflejo = new Pen(Color.FromArgb(activa?120:190,222,209,247),1)) {
            g.FillPolygon(tinta,forma);
            g.FillPolygon(metal,new PointF[]{forma[0],new PointF(dx*67-dy*2,dy*13+dx),forma[3],forma[5]});
            g.DrawLine(reflejo,dx*13,dy*2,dx*62,dy*12);
            g.DrawLine(oro,dx*68,dy*14,dx*76,dy*15);
        }
    }

    public static void Explosion(string destino) {
        const int tam = 88;
        using (var atlas = new Bitmap(tam * 5,tam * 2))
        using (var g = Graphics.FromImage(atlas)) {
            g.SmoothingMode = SmoothingMode.None;
            for (int i = 0; i < 10; i++) {
                var anterior = g.Save();
                g.TranslateTransform(i % 5 * tam + tam / 2, i / 5 * tam + tam / 2);
                double t = i / 9.0;
                for (int j = 0; j < 12; j++) {
                    double a = j * Math.PI / 6 + j % 3 * .14;
                    float x = (float)(Math.Cos(a) * (6+i*3.6) * (.72+j%4*.1));
                    float y = (float)(Math.Sin(a) * (6+i*3.4) * (.82+j%3*.12));
                    int radio = Math.Max(2,(int)((1-t)*(6+j%3)));
                    int alfa = (int)(240 * (1-.83*t));
                    using (var borde = new SolidBrush(Color.FromArgb(alfa,117,53,135)))
                    using (var medio = new SolidBrush(Color.FromArgb(alfa,247,121+j%3*25,92)))
                    using (var claro = new SolidBrush(Color.FromArgb(alfa,255,228,153))) {
                        g.FillEllipse(borde,x-radio-1,y-radio-1,radio*2+3,radio*2+3);
                        g.FillEllipse(medio,x-radio+1,y-radio,radio*2-1,radio*2);
                        g.FillRectangle(claro,x-radio/2,y-radio/2,Math.Max(1,radio/2),Math.Max(1,radio/2));
                    }
                }
                if (i < 6) {
                    int r = 4 + (i < 3 ? i*4 : (5-i)*4);
                    using (var b = new SolidBrush(Color.FromArgb(248-i*9,255,240,188))) {
                        g.FillPolygon(b,new Point[]{new Point(0,-r-7),new Point(r/2,-r/2),new Point(r+8,0),
                            new Point(r/2,r/2),new Point(0,r+7),new Point(-r/2,r/2),new Point(-r-8,0),new Point(-r/2,-r/2)});
                    }
                }
                g.Restore(anterior);
            }
            atlas.Save(destino,ImageFormat.Png);
        }
    }

    public static void Hallazgo(string destino) {
        const int tasa = 22050;
        int muestra = (int)(tasa * .31);
        using (var f = new BinaryWriter(File.Create(destino))) {
            f.Write(new char[]{'R','I','F','F'}); f.Write(36 + muestra*2);
            f.Write(new char[]{'W','A','V','E','f','m','t',' '}); f.Write(16); f.Write((short)1);
            f.Write((short)1); f.Write(tasa); f.Write(tasa*2); f.Write((short)2); f.Write((short)16);
            f.Write(new char[]{'d','a','t','a'}); f.Write(muestra*2);
            for (int i = 0; i < muestra; i++) {
                double t = i/(double)tasa;
                double valor = Nota(t,698.46,.25) + Nota(t-.058,1046.5,.22);
                f.Write((short)(Math.Max(-.95,Math.Min(.95,valor))*32767));
            }
        }
    }

    static double Nota(double t,double hz,double volumen) {
        if (t < 0 || t > .242) return 0;
        double envolvente = Math.Min(1,t/.006)*Math.Exp(-19*t)*Math.Min(1,(.242-t)/.032);
        double frecuencia = t * hz * 2 * Math.PI;
        return volumen*envolvente*(Math.Sin(frecuencia)+.27*Math.Sin(frecuencia*2.005)+.10*Math.Sin(frecuencia*3.98));
    }
}
