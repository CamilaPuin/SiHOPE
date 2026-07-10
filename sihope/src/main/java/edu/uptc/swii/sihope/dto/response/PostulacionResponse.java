package edu.uptc.swii.sihope.dto.response;

import java.util.Map;

/**
 * Vista de una postulación para el panel del coordinador (HU_005 / HU_009).
 * {@code datos} son los campos parametrizables ya deserializados desde JSON.
 */
public record PostulacionResponse(
        Integer id,
        Integer convocatoriaId,
        String convocatoriaTitulo,
        Integer aspiranteId,
        String aspiranteNombre,
        String aspiranteCorreo,
        String estado,
        Map<String, String> datos,
        String fechaPostulacion) {
}
