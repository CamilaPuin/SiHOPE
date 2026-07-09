package edu.uptc.swii.sihope.dto.request;

/** Cambio de rol de un usuario (@RequestBody). */
public class ChangeRoleRequest {

    private String rol;

    public ChangeRoleRequest() {
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
