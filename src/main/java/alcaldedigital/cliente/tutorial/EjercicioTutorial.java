package alcaldedigital.cliente.tutorial;

import alcaldedigital.cliente.entrada.EstadoMando.Boton;
import alcaldedigital.cliente.entrada.MapaControles;

import java.util.List;
import java.util.Set;

import static java.awt.event.KeyEvent.*;

/** Consignas y entradas de los ejercicios, en el orden de la campaña. */
public enum EjercicioTutorial {
    INICIO(1, "Empieza la práctica", "Bienvenida, Sofi. Te enseñaré a pensar por tu cuenta. Intenta no acostumbrarte.",
        new Control("Enter", List.of("A"), Set.of(VK_ENTER), Set.of(Boton.A))),
    ACERCARSE(1, "Camina hasta la marca", "Ven. Si te pierdes en una sala vacía, Civitas será toda una aventura.",
        movimiento("WASD o flechas", "Stick izquierdo o cruceta")),
    HABLAR(1, "Habla con Palomo", "Acércate, tengo algo importantísimo que decir. Eso suele funcionar.", interactuar()),
    PAPEL(1, "Recoge el periódico", "Ese papel lo trajo el viento. A veces hasta el clima coopera conmigo.", interactuar()),
    LUPA(1, "Activa la lupa", "Para leer la letra pequeña, usa la lupa. Qué afición tan inconveniente.",
        control("Q", VK_Q, Boton.LB)),
    CONTRASTAR(1, "Localiza una pista para contrastar", "Dicen que la fuente aprendió a hablar. Antes de compartirlo, mira qué datos aparecen en la publicación.",
        seleccion(false)),
    LIBRETA(1, "Abre la libreta", "Ahí quedan las pistas. No me hagas repetirlo, que luego me citan.", control("Tab", VK_TAB, Boton.X)),
    CORRER(2, "Corre por la redacción", "La red se cae. Camina si quieres ver cómo se apagan las pantallas una por una.",
        new Control("D + Mayús", List.of("RB"), Set.of(VK_SHIFT), Set.of(Boton.RB))),
    SALTO_TERMINAL(2, "Salta la barrera", "Un cable en medio del paso. Qué exquisita sorpresa. Salta mientras avanzas.", saltar(Boton.X)),
    REPARAR(2, "Repara la terminal", "Repara ese equipo. Difundir un rumor con la red apagada da muchísimo trabajo.", interactuar()),
    PING(3, "Lanza PING a distancia", "Al troll le encanta la atención. Envíale esta y mira cuánto le dura el entusiasmo.",
        control("F", VK_F, Boton.X, Boton.RT)),
    DASH(3, "Usa DASH para cruzar la señal", "La señal va a cortarte el paso. Deslízate por ella antes de que te alcance.",
        control("Espacio", VK_SPACE, Boton.RB)),
    FLASH(3, "Usa FLASH de cerca", "Ya lo tienes delante. Dale un flash. Las fotos poco favorecedoras son mi especialidad.",
        control("Q", VK_Q, Boton.LT)),
    LINTERNA(4, "Enciende la linterna de Civita", "En el laberinto todo parece un secreto. Alumbra el pasillo y verás qué queda de la intriga.",
        control("F", VK_F, Boton.LB)),
    ATAQUE(4, "Ataca al señuelo con la espada", "En campaña todos dicen que van al ataque. Tú puedes demostrarlo.",
        control("J", VK_J, Boton.X, Boton.RT)),
    ESCUDO(4, "Bloquea el ataque con el escudo", "Te van a responder. Esa es la parte que nunca incluyen en los discursos.",
        control("Q", VK_Q, Boton.Y, Boton.LT)),
    ESQUIVA_PLAZA(4, "Esquiva la zona marcada", "¿Ves la franja? Quédate ahí si quieres salir en el próximo comunicado.",
        control("Espacio", VK_SPACE, Boton.RB)),
    SALTO_PULSO(5, "Salta sobre el pulso", "Dentro del servidor hasta el suelo está en tu contra. Salta mientras avanzas.", saltar(Boton.X)),
    SERVIDOR(5, "Elige cómo responder a la alerta", "El ataque dio una tregua. Aprovecha y verifica la señal antes de tocar nada.", seleccion(true)),
    PILOTAR(6, "Lleva el helicóptero hasta el aro", "Bien. Ahora arriba. A la ciudad le encanta mirar los problemas desde lejos.",
        movimiento("WASD o flechas", "Stick izquierdo o cruceta")),
    IMPULSO(6, "Usa el impulso del helicóptero", "Cruza la interferencia con un impulso. No seas tímida, que en el aire se nota.",
        control("Espacio", VK_SPACE, Boton.RB)),
    DISPARO(6, "Dispara contra el dron", "El dron lleva un rato siguiéndote. Salúdalo con un disparo.",
        control("F", VK_F, Boton.X, Boton.RT)),
    MISIL(6, "Lanza un misil", "Activé el misil de práctica. Eso pondrá punto final a su cobertura aérea.",
        control("Q", VK_Q, Boton.Y, Boton.LT)),
    ACELERAR(7, "Gana velocidad en la carrera", "¿Ya te aprendiste ese botón? Qué pena. Aquí sirve para correr.",
        control("Mantén o pulsa Espacio", VK_SPACE, Boton.RB, Boton.X)),
    CARRIL(7, "Cambia de carril para esquivar la valla", "El carril de abajo tiene una valla. Si la saludas, perderás la primicia.",
        movimiento("W / S o flechas verticales", "Stick izquierdo: arriba / abajo")),
    NOTICIA(7, "Responde a la noticia mientras corres", "Un atajo de origen anónimo. La clase de oportunidad que compartes antes de comprobarla, ¿no?", seleccion(true)),
    LATERAL(8, "Muévete por la arena", "Ahora miras la arena de lado. Camina hasta la marca, que lo demás ya llegará solo.",
        movimiento("A / D", "Stick izquierdo: izquierda / derecha")),
    DOBLE_SALTO(8, "Salta otra vez en el aire", "Mira hacia arriba. Un salto no basta para llegar a todas partes. Afortunadamente.", saltar(Boton.A)),
    GOLPE_FINAL(8, "Prueba el ataque principal", "El último ejercicio. Confío en que no usarás nada de esto para interrumpirme luego.",
        control("F", VK_F, Boton.X)),
    ALCANCE(8, "Usa la segunda habilidad", "El otro blanco está más lejos. Prueba la acción que tiene más alcance.",
        control("Q", VK_Q, Boton.Y)),
    PROTECCION(8, "Activa la protección de Sofi", "Protege a tu equipo. Los aliados distraídos son mi público favorito.",
        control("C", VK_C, Boton.LB)),
    ESQUIVA_FINAL(8, "Esquiva la última señal", "Aparta a Sofi de la franja. Quiero tener público cuando empiece lo interesante.",
        control("R", VK_R, Boton.RB)),
    CIERRE(8, "Ve al selector de misiones", "Ya sabes moverte, preguntar y esquivar. Procura olvidar lo segundo cuando te llegue mi primer mensaje.",
        new Control("Enter", List.of("A"), Set.of(VK_ENTER), Set.of(Boton.A)));

    final int mision;
    final String objetivo, frase;
    final Control control;

    EjercicioTutorial(int mision, String objetivo, String frase, Control control) {
        this.mision = mision;
        this.objetivo = objetivo;
        this.frase = frase;
        this.control = control;
    }

    String indicacion(String mando) {
        if (mando.isBlank()) return control.teclado();
        var contexto = new MapaControles.Contexto(mision, false, null, null, mando);
        if (this == CONTRASTAR || this == SERVIDOR || this == NOTICIA)
            return "Cruceta + " + MapaControles.etiquetaBoton(contexto, "A");
        String botones = control.mando().stream()
            .map(b -> MapaControles.etiquetaBoton(contexto, b)).collect(java.util.stream.Collectors.joining(" / "));
        return switch (this) {
            case CORRER -> "Stick a la derecha + " + botones;
            case SALTO_TERMINAL, SALTO_PULSO -> "Stick a la derecha + " + botones;
            case DOBLE_SALTO -> "Pulsa " + botones + " dos veces";
            case ACELERAR -> "Mantén o pulsa " + botones;
            default -> botones;
        };
    }

    String[] opciones() {
        return switch (this) {
            case CONTRASTAR -> new String[]{"Revisar las fuentes", "Repetir el titular", "Ignorar el documento"};
            case SERVIDOR -> new String[]{"Verificar la señal", "Propagar el aviso", "Esperar sin revisar"};
            case NOTICIA -> new String[]{"Contrastar la ruta", "Seguir el mensaje", "Ignorar el aviso"};
            default -> throw new IllegalStateException("El ejercicio no tiene opciones");
        };
    }

    private static Control interactuar() { return control("E", VK_E, Boton.A); }

    private static Control saltar(Boton boton) {
        return boton == Boton.A ? control("Pulsa Espacio dos veces", VK_SPACE, boton)
            : control("D + Espacio", VK_SPACE, boton);
    }

    private static Control control(String etiqueta, int tecla, Boton... botones) {
        return new Control(etiqueta, java.util.Arrays.stream(botones).map(Boton::name).toList(), Set.of(tecla), Set.of(botones));
    }

    private static Control movimiento(String teclado, String mando) {
        return new Control(teclado, List.of(mando), Set.of(), Set.of());
    }

    private static Control seleccion(boolean numeros) {
        return new Control(numeros ? "1 / 2 / 3 o clic" : "Flechas y Enter, o clic", List.of(), Set.of(VK_ENTER, VK_E), Set.of(Boton.A));
    }

    record Control(String teclado, List<String> mando, Set<Integer> teclas, Set<Boton> botones) {}
}
