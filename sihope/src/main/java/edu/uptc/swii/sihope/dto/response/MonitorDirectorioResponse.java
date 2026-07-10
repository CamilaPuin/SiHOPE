package edu.uptc.swii.sihope.dto.response;

import java.util.List;

import edu.uptc.swii.sihope.dto.BloqueHorario;

/**
 * Vista de un monitor para el directorio (lectura de HU_006): sus datos básicos y
 * las franjas horarias que ha marcado como disponibles, visibles para el resto de
 * roles (estudiantes y coordinadores).
 */
public record MonitorDirectorioResponse(
        Integer id,
        String nombre,
        String iniciales,
        String correo,
        List<BloqueHorario> disponibilidad) {
}
