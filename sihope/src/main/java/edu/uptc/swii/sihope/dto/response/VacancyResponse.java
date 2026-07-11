package edu.uptc.swii.sihope.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uptc.swii.sihope.domain.Vacancy;

public record VacancyResponse(
        Integer id,
        @JsonProperty("titulo") String title,
        @JsonProperty("descripcion") String description,
        @JsonProperty("requisitos") String requirements,
        @JsonProperty("materia") String subject,
        @JsonProperty("plazas") int slots,
        @JsonProperty("fechaLimite") String deadline,
        @JsonProperty("estado") String status,
        @JsonProperty("fechaCreacion") String creationDate,
        @JsonProperty("coordinador") String coordinator) {

    public static VacancyResponse from(Vacancy c) {
        String coord = c.getCoordinador() != null
                ? (c.getCoordinador().getNombres() + " " + c.getCoordinador().getApellidos()).trim()
                : "";
        return new VacancyResponse(
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

    public String getTitulo() {
        return title;
    }

    public String getDescripcion() {
        return description;
    }

    public String getRequisitos() {
        return requirements;
    }

    public String getMateria() {
        return subject;
    }

    public int getPlazas() {
        return slots;
    }

    public String getFechaLimite() {
        return deadline;
    }

    public String getEstado() {
        return status;
    }

    public String getFechaCreacion() {
        return creationDate;
    }

    public String getCoordinador() {
        return coordinator;
    }
}
