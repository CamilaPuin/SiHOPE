package edu.uptc.swii.sihope.dto.request;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplicationRequest {

    @JsonProperty("datos")
    private Map<String, String> data;

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

}
