package edu.uptc.swii.sihope.dto.request;

/**
 * Cuerpo para aprobar o rechazar una postulación (HU_009). {@code estado} debe ser
 * "APROBADA" o "RECHAZADA".
 */
public class EstadoPostulacionRequest {

    private String estado;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
