package edu.uptc.swii.sihope.dto.request;

/** Datos de inicio de sesión enviados desde React (@RequestBody). */
public class LoginRequest {

    private String correo;
    private String password;

    public LoginRequest() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
