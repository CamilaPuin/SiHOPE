package edu.uptc.swii.sihope.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubjectsRequest {

    @JsonProperty("asignaturas")
    private List<String> subjects;

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }
}
