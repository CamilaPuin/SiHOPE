package edu.uptc.swii.sihope.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AssignSubjectsRequest {

    @JsonProperty("asignaturaIds")
    private List<Integer> subjectIds;

    public List<Integer> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(List<Integer> subjectIds) {
        this.subjectIds = subjectIds;
    }
}
