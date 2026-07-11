package edu.uptc.swii.sihope.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombres")
    @JsonProperty("nombres")
    private String firstName;

    @Column(name = "apellidos")
    @JsonProperty("apellidos")
    private String lastName;

    @Column(name = "codigo")
    @JsonProperty("codigo")
    private String studentCode;

    @Column(name = "correo")
    @JsonProperty("correo")
    private String email;

    private String password;

    @Column(name = "activo")
    @JsonProperty("activo")
    private boolean active;

    @Column(name = "verificado")
    @JsonProperty("verificado")
    private boolean verified;

    @Column(name = "token_verificacion")
    @JsonProperty("tokenVerificacion")
    private String verificationToken;

    @Column(name = "token_reset")
    @JsonProperty("tokenReset")
    private String resetToken;

    @Column(name = "token_reset_expira")
    @JsonProperty("tokenResetExpira")
    private LocalDateTime resetTokenExpiresAt;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion = 0;

    @ManyToOne
    @JoinColumn(name = "rol_id")
    private Role role;

    public User() {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiresAt() {
        return resetTokenExpiresAt;
    }

    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) {
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(int tokenVersion) {
        this.tokenVersion = tokenVersion;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Spanish aliases for backward compatibility
    public String getNombres() {
        return getFirstName();
    }

    public void setNombres(String nombres) {
        setFirstName(nombres);
    }

    public String getApellidos() {
        return getLastName();
    }

    public void setApellidos(String apellidos) {
        setLastName(apellidos);
    }

    public String getCodigo() {
        return getStudentCode();
    }

    public void setCodigo(String codigo) {
        setStudentCode(codigo);
    }

    public String getCorreo() {
        return getEmail();
    }

    public void setCorreo(String correo) {
        setEmail(correo);
    }

    public boolean isActivo() {
        return isActive();
    }

    public void setActivo(boolean activo) {
        setActive(activo);
    }

    public boolean isVerificado() {
        return isVerified();
    }

    public void setVerificado(boolean verificado) {
        setVerified(verificado);
    }

    public String getTokenVerificacion() {
        return getVerificationToken();
    }

    public void setTokenVerificacion(String tokenVerificacion) {
        setVerificationToken(tokenVerificacion);
    }

    public String getTokenReset() {
        return getResetToken();
    }

    public void setTokenReset(String tokenReset) {
        setResetToken(tokenReset);
    }

    public LocalDateTime getTokenResetExpira() {
        return getResetTokenExpiresAt();
    }

    public void setTokenResetExpira(LocalDateTime tokenResetExpira) {
        setResetTokenExpiresAt(tokenResetExpira);
    }
}

