package edu.uptc.swii.sihope.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uptc.swii.sihope.domain.User;

public class UserResponse {

    private Integer id;

    @JsonProperty("nombres")
    private String firstName;

    @JsonProperty("apellidos")
    private String lastName;

    @JsonProperty("codigo")
    private String studentCode;

    @JsonProperty("correo")
    private String email;

    @JsonProperty("activo")
    private boolean active;

    @JsonProperty("verificado")
    private boolean verified;

    @JsonProperty("rol")
    private String role;

    public UserResponse() {
    }

    public UserResponse(Integer id, String firstName, String lastName, String studentCode,
                        String email, boolean active, boolean verified, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentCode = studentCode;
        this.email = email;
        this.active = active;
        this.verified = verified;
        this.role = role;
    }

    public static UserResponse from(User u) {
        return new UserResponse(
                u.getId(),
                u.getNombres(),
                u.getApellidos(),
                u.getCodigo(),
                u.getCorreo(),
                u.isActivo(),
                u.isVerificado(),
                u.getRole() != null ? u.getRole().getNombre() : null
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
