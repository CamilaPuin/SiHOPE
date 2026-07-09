package edu.uptc.swii.sihope.dto.request;

/** Cambio de contraseña de la cuenta autenticada (@RequestBody). */
public class UpdatePasswordRequest {

    private String actual;
    private String nueva;
    private String nueva2;

    public UpdatePasswordRequest() {
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
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
