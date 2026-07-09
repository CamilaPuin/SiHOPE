package edu.uptc.swii.sihope.dto.request;

/** Solicitud de recuperación de contraseña por correo (@RequestBody). */
public class RecoverPasswordRequest {

    private String correo;

    public RecoverPasswordRequest() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
