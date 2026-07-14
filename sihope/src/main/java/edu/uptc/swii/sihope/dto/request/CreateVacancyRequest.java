package edu.uptc.swii.sihope.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateVacancyRequest {

    @JsonProperty("titulo")
    private String title;

    @JsonProperty("descripcion")
    private String description;

    @JsonProperty("requisitos")
    private String requirements;

    /** IDs de asignaturas del catálogo que orientará el monitor ganador. */
    @JsonProperty("materiaIds")
    private List<Integer> subjectIds;

    @JsonProperty("plazas")
    private Integer slots;

    @JsonProperty("fechaLimite")
    private String deadline;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public List<Integer> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(List<Integer> subjectIds) {
        this.subjectIds = subjectIds;
    }

    public Integer getSlots() {
        return slots;
    }

    public void setSlots(Integer slots) {
        this.slots = slots;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

}
