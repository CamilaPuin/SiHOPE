package edu.uptc.swii.sihope.dto.request;

/**
 * Datos para crear/publicar una convocatoria (HU_008). {@code fechaLimite} viaja
 * como texto "yyyy-MM-dd" para validarla explícitamente en el servicio.
 */
public class CrearConvocatoriaRequest {

    private String titulo;
    private String descripcion;
    private String requisitos;
    private String materia;
    private Integer plazas;
    private String fechaLimite;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public Integer getPlazas() {
        return plazas;
    }

    public void setPlazas(Integer plazas) {
        this.plazas = plazas;
    }

    public String getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(String fechaLimite) {
        this.fechaLimite = fechaLimite;
    }
}
