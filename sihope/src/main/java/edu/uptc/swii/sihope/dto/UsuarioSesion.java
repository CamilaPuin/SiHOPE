package edu.uptc.swii.sihope.dto;

import edu.uptc.swii.sihope.domain.User;

public class UsuarioSesion {

    private final String nombre;
    private final String iniciales;
    private final String correo;
    private final String rol;

    public UsuarioSesion(String nombre, String iniciales, String correo, String rol) {
        this.nombre = nombre;
        this.iniciales = iniciales;
        this.correo = correo;
        this.rol = rol;
    }

    public static UsuarioSesion desde(User u) {
        String nombre = (u.getNombres() + " " + u.getApellidos()).trim();
        String iniciales = inicial(u.getNombres()) + inicial(u.getApellidos());
        String rol = u.getRole() != null ? u.getRole().getNombre() : "";
        return new UsuarioSesion(nombre, iniciales.toUpperCase(), u.getCorreo(), rol);
    }

    private static String inicial(String s) {
        return (s == null || s.isBlank()) ? "" : s.substring(0, 1);
    }

    public String getNombre() {
        return nombre;
    }

    public String getIniciales() {
        return iniciales;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() {
        return rol;
    }
}
