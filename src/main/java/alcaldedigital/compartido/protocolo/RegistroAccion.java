package alcaldedigital.compartido.protocolo;

import alcaldedigital.compartido.modelo.EstadoCiudad;

/** Auditoría local inmutable. No contiene variante secreta ni reemplaza el historial narrativo. */
public record RegistroAccion(String requestId, String accion, ResultadoAccion resultado,
                            int versionAntes, int puntosAntes, int puntosDespues,
                            EstadoCiudad ciudadAntes, EstadoCiudad ciudadDespues) { }
