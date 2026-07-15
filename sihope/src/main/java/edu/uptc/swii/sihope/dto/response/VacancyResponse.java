package edu.uptc.swii.sihope.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uptc.swii.sihope.domain.Asignatura;
import edu.uptc.swii.sihope.domain.Vacancy;

public record VacancyResponse(
        Integer id,
        @JsonProperty("titulo") String title,
        @JsonProperty("descripcion") String description,
        @JsonProperty("requisitos") String requirements,
        @JsonProperty("materia") String subject,
        @JsonProperty("materias") List<String> subjects,
        @JsonProperty("plazas") int slots,
        @JsonProperty("fechaLimite") String deadline,
        @JsonProperty("estado") String status,
        @JsonProperty("fechaCreacion") String creationDate,
        @JsonProperty("coordinador") String coordinator) {

    public static VacancyResponse from(Vacancy c) {
        String coord = c.getCoordinador() != null
                ? (c.getCoordinador().getNombres() + " " + c.getCoordinador().getApellidos()).trim()
                : "";
        List<String> subjectNames = c.getSubjects().stream()
                .map(Asignatura::getName).sorted().toList();
        String subjectSummary = !subjectNames.isEmpty()
                ? String.join(", ", subjectNames)
                : c.getMateria();
        return new VacancyResponse(
                c.getId(),
                c.getTitulo(),
                c.getDescripcion(),
                c.getRequisitos(),
                subjectSummary,
                subjectNames,
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

    public List<String> getMaterias() {
        return subjects;
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
