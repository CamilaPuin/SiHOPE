package edu.uptc.swii.sihope.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uptc.swii.sihope.domain.User;

public class UserSession {

    @JsonProperty("nombre")
    private final String name;

    @JsonProperty("iniciales")
    private final String initials;

    @JsonProperty("correo")
    private final String email;

    @JsonProperty("rol")
    private final String role;

    public UserSession(String name, String initials, String email, String role) {
        this.name = name;
        this.initials = initials;
        this.email = email;
        this.role = role;
    }

    public static UserSession from(User u) {
        String name = (u.getNombres() + " " + u.getApellidos()).trim();
        String initials = initial(u.getNombres()) + initial(u.getApellidos());
        String role = u.getRole() != null ? u.getRole().getNombre() : "";
        return new UserSession(name, initials.toUpperCase(), u.getCorreo(), role);
    }

    private static String initial(String s) {
        return (s == null || s.isBlank()) ? "" : s.substring(0, 1);
    }

    public String getName() {
        return name;
    }

    public String getInitials() {
        return initials;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
