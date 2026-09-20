package alcaldedigital.estructuras;
import alcaldedigital.compartido.estructuras.avl.*;
import alcaldedigital.compartido.modelo.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ArbolAVLTest {
    @Test void borrarHojaUnHijoRaizYUltimoNodo() {
        var a=new ArbolAVLPublicaciones();a.insertar(p(2,20));a.insertar(p(1,10));a.insertar(p(3,30));a.insertar(p(4,40));
        assertTrue(a.eliminar(1));validar(a.vista(),null,null);
        assertTrue(a.eliminar(3));validar(a.vista(),null,null);
        assertTrue(a.eliminar(a.vista().valor().id()));validar(a.vista(),null,null);
        assertTrue(a.eliminar(a.vista().valor().id()));assertNull(a.vista());assertEquals(0,a.cantidad());
    }
    Publicacion p(long id,int riesgo) {return new Publicacion(id,riesgo,"autor"+id,"texto"+id,"sin comprobar");}
    @Test void cuatroRotaciones() {
        for(int[] sec:new int[][]{{30,20,10},{10,20,30},{30,10,20},{10,30,20}}) {
            var a=new ArbolAVLPublicaciones();for(int i=0;i<3;i++) a.insertar(p(i+1,sec[i]));
            assertEquals(20,a.vista().valor().riesgo());validar(a.vista(),null,null);
            assertTrue(a.pasos().get(2).operacion().contains("rotación"));
        }
    }
    @Test void eliminacionConSucesorConservaEntidad() {
        var a=new ArbolAVLPublicaciones();for(int n:new int[]{50,30,70,60,80}) a.insertar(p(n,n));
        assertTrue(a.eliminar(50));assertNull(a.buscar(50));
        assertEquals("texto60",a.buscar(60).texto());assertEquals(4,a.cantidad());validar(a.vista(),null,null);
    }
    @Test void riesgoEmpatadoReindexacionYDuplicados() {
        var a=new ArbolAVLPublicaciones();a.insertar(p(1,20));a.insertar(p(2,20));
        assertThrows(IllegalArgumentException.class,()->a.insertar(p(1,99)));
        a.actualizar(p(1,90));assertEquals(List.of(2L,1L),a.recorrer("inorden").stream().map(Publicacion::id).toList());
        assertFalse(a.eliminar(999));assertEquals(2,a.cantidad());
    }
    @Test void recorridosYVacio() {
        var a=new ArbolAVLPublicaciones();assertTrue(a.recorrer("inorden").isEmpty());assertEquals(0,a.altura());
        for(int n:new int[]{20,10,30}) a.insertar(p(n,n));
        assertEquals(List.of(20L,10L,30L),a.recorrer("preorden").stream().map(Publicacion::id).toList());
        assertEquals(List.of(10L,30L,20L),a.recorrer("postorden").stream().map(Publicacion::id).toList());
        assertEquals(List.of(30L,20L,10L),a.recorrer("inverso").stream().map(Publicacion::id).toList());
    }
    @Test void secuenciaAleatoriaContraOraculo() {
        Random r=new Random(20260917);var a=new ArbolAVLPublicaciones();Map<Long,Publicacion> esperado=new HashMap<>();
        for(int i=0;i<2500;i++) {
            long id=r.nextInt(150)+1;
            if(r.nextBoolean()) {
                Publicacion p=p(id,r.nextInt(101));
                if(esperado.containsKey(id)) a.actualizar(p);else a.insertar(p);esperado.put(id,p);
            } else {assertEquals(esperado.remove(id)!=null,a.eliminar(id));}
            assertEquals(esperado.size(),a.cantidad());
            assertEquals(esperado.values().stream().sorted(Comparator.comparing(Publicacion::clave)).toList(),a.recorrer("inorden"));
            validar(a.vista(),null,null);
        }
    }
    int validar(ArbolAVLPublicaciones.Vista n,ClavePublicacion min,ClavePublicacion max) {
        if(n==null) return 0;
        if(min!=null) assertTrue(n.valor().clave().compareTo(min)>0);
        if(max!=null) assertTrue(n.valor().clave().compareTo(max)<0);
        int l=validar(n.izquierdo(),min,n.valor().clave()),r=validar(n.derecho(),n.valor().clave(),max);
        assertEquals(r-l,n.fe());assertTrue(Math.abs(n.fe())<=1);assertEquals(1+Math.max(l,r),n.altura());return n.altura();
    }
}
