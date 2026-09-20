package alcaldedigital.servidor.eventos;
import processing.data.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Datos privados; no se envían íntegros a la vista del jugador. */
public record EventoParque(String titulo, String rumor, List<Variante> variantes) {
    public record Variante(String id, String aviso, String testimonio, String conclusion) { }
    public static EventoParque cargar() {
        try (InputStream in = EventoParque.class.getResourceAsStream("/servidor/eventos/dia_01_parque.json")) {
            if (in == null) throw new IllegalStateException("Falta evento");
            JSONObject json = JSONObject.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            JSONArray array = json.getJSONArray("variantes");
            List<Variante> variantes = new ArrayList<>(); Set<String> ids = new HashSet<>();
            for (int i=0;i<array.size();i++) {
                JSONObject v=array.getJSONObject(i);
                if (!ids.add(v.getString("id"))) throw new IllegalStateException("Variante duplicada");
                variantes.add(new Variante(v.getString("id"),v.getString("aviso"),v.getString("testimonio"),v.getString("conclusion")));
            }
            if (variantes.size()!=4) throw new IllegalStateException("Se requieren cuatro variantes");
            return new EventoParque(json.getString("titulo"),json.getString("publicacion"),List.copyOf(variantes));
        } catch(IOException e) { throw new UncheckedIOException(e); }
    }
}
