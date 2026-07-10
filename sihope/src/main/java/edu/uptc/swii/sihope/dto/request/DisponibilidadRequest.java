package edu.uptc.swii.sihope.dto.request;

import java.util.List;

import edu.uptc.swii.sihope.dto.BloqueHorario;

/**
 * Cuerpo del PUT de disponibilidad: la matriz semanal completa del monitor.
 * Reemplaza por entero la disponibilidad anterior (los bloques no enviados se
 * eliminan), de modo que solo quedan visibles las franjas marcadas.
 */
public class DisponibilidadRequest {

    private List<BloqueHorario> bloques;

    public List<BloqueHorario> getBloques() {
        return bloques;
    }

    public void setBloques(List<BloqueHorario> bloques) {
        this.bloques = bloques;
    }
}
