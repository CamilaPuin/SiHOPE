package edu.uptc.swii.sihope.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthenticatedUser(
        Integer id,
        @JsonProperty("correo") String email,
        @JsonProperty("rol") String role,
        @JsonProperty("nombre") String name,
        @JsonProperty("iniciales") String initials) {

    public static final String ATTRIBUTE = "usuarioAutenticado";

    public String correo() {
        return email;
    }

    public String rol() {
        return role;
    }

    public String nombre() {
        return name;
    }

    public String iniciales() {
        return initials;
    }
}
