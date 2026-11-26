package alcaldedigital.cliente.presentacion;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.HashSet;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecursosRemasterTest {
    @Test void atlasContienenPosesConAlphaYCuadrosDistintos() throws Exception {
        atlas("arte/mision1/sofi-recogida",6,1);
        atlas("mision6/helicoptero-rotor",6,2);
        atlas("mision6/explosion-remaster",5,2);
        for(String nombre:new String[]{"sofi","periodista","influencer","juan","palomo"})
            atlas("presentacion/remaster/"+nombre,8,1);
        for(String nombre:new String[]{"troll","spambot","deepfake"})
            atlas("presentacion/remaster/"+nombre,4,1);
    }

    @Test void losSonidosDeEscenaSePuedenMezclarEnEstereo() throws Exception {
        String[] efectos={
            "papel","pagina","red-alarma","red-estacion","red-fallo",
            "nova-ping","nova-flash","nova-deslizar","nova-privado","nova-live","nova-impacto",
            "civita-portal","civita-espada","civita-escudo","civita-impacto",
            "servidor-alarma","servidor-turno","servidor-impacto","servidor-confirmar",
            "vuelo-canon-a","vuelo-canon-b","vuelo-colision","vuelo-dron","vuelo-misil",
            "carrera-paso-a","carrera-paso-b","carrera-prensa","carrera-alerta",
            "final-carga","final-orbe","final-descarga","final-impacto"
        };
        var formato=new javax.sound.sampled.AudioFormat(22050,16,2,true,false);
        for(String id:efectos) {
            try(var recurso=getClass().getResourceAsStream("/cliente/audio/remaster/"+id+".wav")) {
                assertNotNull(recurso,id);
                try(var original=AudioSystem.getAudioInputStream(new BufferedInputStream(recurso));
                    var mezcla=AudioSystem.getAudioInputStream(formato,original)) {
                    byte[] datos=mezcla.readAllBytes();
                    assertTrue(datos.length>3500&&datos.length<55000,id);
                    int pico=0;
                    for(int i=0;i<datos.length;i+=2)
                        pico=Math.max(pico,Math.abs((short)((datos[i]&255)|(datos[i+1]<<8))));
                    assertTrue(pico>1400&&pico<26000,id+": "+pico);
                }
            }
        }
    }

    private void atlas(String nombre, int columnas, int filas) throws Exception {
        try (var in = getClass().getResourceAsStream("/cliente/"+nombre+".png")) {
            assertNotNull(in,nombre);
            BufferedImage im = ImageIO.read(in);
            assertEquals(0,im.getWidth()%columnas);
            assertEquals(0,im.getHeight()%filas);
            int w=im.getWidth()/columnas,h=im.getHeight()/filas;
            var vistos = new HashSet<Integer>();
            for(int f=0;f<filas;f++) for(int c=0;c<columnas;c++) {
                int[] px=im.getRGB(c*w,f*h,w,h,null,0,w);
                assertTrue(Arrays.stream(px).anyMatch(p->p>>>24==0));
                assertTrue(Arrays.stream(px).anyMatch(p->p>>>24>16));
                assertTrue(vistos.add(Arrays.hashCode(px)),nombre+" contiene un cuadro duplicado");
            }
        }
    }

    @Test void sonidoDeHallazgoDecodificaComoPcmYEsBreve() throws Exception {
        try (var recurso = getClass().getResourceAsStream("/cliente/audio/fx/lupa-hallazgo.wav")) {
            assertNotNull(recurso);
            try (var audio = AudioSystem.getAudioInputStream(new BufferedInputStream(recurso))) {
                assertEquals(16,audio.getFormat().getSampleSizeInBits());
                double duracion=audio.getFrameLength()/audio.getFormat().getFrameRate();
                assertTrue(duracion>.2 && duracion<.4);
                byte[] bytes=audio.readAllBytes();
                int pico=0;
                for(int i=0;i<bytes.length;i+=2)
                    pico=Math.max(pico,Math.abs((short)((bytes[i]&255)|(bytes[i+1]<<8))));
                assertTrue(pico>2000 && pico<22000);
            }
        }
    }
}
