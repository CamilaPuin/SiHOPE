package edu.uptc.swii.sihope.dto.response;

import edu.uptc.swii.sihope.domain.Convocatoria;

/**
 * Vista de una convocatoria para el frontend (listados de coordinador y aspirante).
 */
public record ConvocatoriaResponse(
        Integer id,
        String titulo,
        String descripcion,
        String requisitos,
        String materia,
        int plazas,
        String fechaLimite,
        String estado,
        String fechaCreacion,
        String coordinador) {

    public static ConvocatoriaResponse desde(Convocatoria c) {
        String coord = c.getCoordinador() != null
                ? (c.getCoordinador().getNombres() + " " + c.getCoordinador().getApellidos()).trim()
                : "";
        return new ConvocatoriaResponse(
                c.getId(),
                c.getTitulo(),
                c.getDescripcion(),
                c.getRequisitos(),
                c.getMateria(),
                c.getPlazas(),
                c.getFechaLimite() != null ? c.getFechaLimite().toString() : null,
                c.getEstado(),
                c.getFechaCreacion() != null ? c.getFechaCreacion().toString() : null,
                coord);
    }
}
