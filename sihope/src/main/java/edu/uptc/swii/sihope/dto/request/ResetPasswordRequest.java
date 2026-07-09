package edu.uptc.swii.sihope.dto.request;

/** Restablecimiento de contraseña mediante token de recuperación (@RequestBody). */
public class ResetPasswordRequest {

    private String token;
    private String nueva;
    private String nueva2;

    public ResetPasswordRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNueva() {
        return nueva;
    }

    public void setNueva(String nueva) {
        this.nueva = nueva;
    }

    public String getNueva2() {
        return nueva2;
    }

    public void setNueva2(String nueva2) {
        this.nueva2 = nueva2;
    }
}
