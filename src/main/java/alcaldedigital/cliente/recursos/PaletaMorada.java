package alcaldedigital.cliente.recursos;

import java.awt.Color;
import java.awt.image.BufferedImage;

/** Tabla de color de la interfaz y recolor determinista del arte pixel. */
public final class PaletaMorada {
    public static final Color FONDO = new Color(0x21152F);
    public static final Color PANEL = new Color(0x38264B);
    public static final Color VIOLETA = new Color(0x604077);
    public static final Color LILA = new Color(0xBD91D5);
    public static final Color PAPEL = new Color(0xF5E8FB);
    public static final Color ACENTO = new Color(0xF4CB6A);
    public static final Color SOMBRA = new Color(0x140C1D);
    public static final Color TEXTO_SUAVE = new Color(0xD6BFE2);
    public static final Color SUPERFICIE_CLARA = new Color(0xE8D1F2);
    public static final Color EXITO = new Color(0x7751A5);
    public static final Color PELIGRO = new Color(0xA8467B);
    public static final Color CESPED = new Color(0x9279AA);
    public static final Color AGUA = new Color(0x7779BF);
    public static final Color LINEA = new Color(0xA488B5);

    private PaletaMorada() { }

    /** Conserva siluetas y alfa; los tonos de piel de personajes siguen cálidos. */
    public static BufferedImage recolorear(String nombre, BufferedImage entrada) {
        BufferedImage salida = new BufferedImage(entrada.getWidth(), entrada.getHeight(), BufferedImage.TYPE_INT_ARGB);
        boolean personaje = nombre.startsWith("actores/") || nombre.startsWith("acciones/");
        boolean detallesDorados = nombre.startsWith("objetos/") || nombre.startsWith("iconos/")
            || nombre.startsWith("actores/noti_") || nombre.startsWith("efectos/");
        float[] hsv = new float[3];
        for (int y = 0; y < entrada.getHeight(); y++) for (int x = 0; x < entrada.getWidth(); x++) {
            int argb = entrada.getRGB(x, y);
            int alfa = argb >>> 24;
            if (alfa == 0) continue;
            Color.RGBtoHSB((argb >>> 16) & 255, (argb >>> 8) & 255, argb & 255, hsv);
            float tono = hsv[0], saturacion = hsv[1], brillo = hsv[2];
            boolean dorado = detallesDorados && tono >= .10f && tono < .18f
                && saturacion > .35f && brillo > .55f;
            boolean piel = personaje && tono >= .015f && tono < .13f
                && saturacion > .18f && brillo > .42f;
            float nuevoTono;
            float nuevaSaturacion;
            if (dorado) {
                nuevoTono = .12f;
                nuevaSaturacion = .68f;
            } else if (piel) {
                nuevoTono = .065f;
                nuevaSaturacion = Math.min(.50f, saturacion * .76f);
            } else {
                nuevoTono = tono < .11f || tono >= .92f ? .88f
                    : tono < .20f ? .81f : tono < .49f ? .76f
                    : tono < .71f ? .69f : .80f;
                nuevaSaturacion = saturacion < .12f
                    ? (brillo < .30f ? .45f : .17f)
                    : Math.min(.64f, Math.max(.28f, saturacion * .78f + .13f));
            }
            float nuevoBrillo = Math.min(1f, (dorado ? .17f : .04f) + brillo * (dorado ? .83f : .95f));
            int rgb = Color.HSBtoRGB(nuevoTono, nuevaSaturacion, nuevoBrillo);
            salida.setRGB(x, y, (alfa << 24) | (rgb & 0xFFFFFF));
        }
        return salida;
    }
}
