package edu.uptc.swii.sihope.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateSubjectRequest {

    @JsonProperty("nombre")
    private String name;

    public CreateSubjectRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
