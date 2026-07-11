package edu.uptc.swii.sihope.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateVacancyRequest {

    @JsonProperty("titulo")
    private String title;

    @JsonProperty("descripcion")
    private String description;

    @JsonProperty("requisitos")
    private String requirements;

    @JsonProperty("materia")
    private String subject;

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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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
