package alcaldedigital.compartido.protocolo;

import java.util.Map;

/** Contrato lógico; la codificación JSON se implementará en el módulo de red. */
public record Solicitud(
        int versionProtocolo,
        TipoMensaje tipo,
        String requestId,
        Map<String, Object> datos) {
}

