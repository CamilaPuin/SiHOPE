package edu.uptc.swii.sihope.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uptc.swii.sihope.dto.TimeBlock;

public record MonitorDirectoryResponse(
        Integer id,
        @JsonProperty("nombre") String name,
        @JsonProperty("iniciales") String initials,
        @JsonProperty("correo") String email,
        @JsonProperty("disponibilidad") List<TimeBlock> availability) {

    public String nombre() {
        return name;
    }

    public String iniciales() {
        return initials;
    }

    public String correo() {
        return email;
    }

    public List<TimeBlock> disponibilidad() {
        return availability;
    }
}
