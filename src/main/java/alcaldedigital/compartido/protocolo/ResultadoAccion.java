package alcaldedigital.compartido.protocolo;

/** Aceptada significa ejecutada; no implica que la decisión sea beneficiosa. */
public record ResultadoAccion(boolean aceptada, String mensaje, int version) { }
