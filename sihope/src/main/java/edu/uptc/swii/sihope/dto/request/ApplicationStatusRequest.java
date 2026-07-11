package edu.uptc.swii.sihope.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplicationStatusRequest {

    @JsonProperty("estado")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
