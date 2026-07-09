package edu.uptc.swii.sihope.dto.response;

import edu.uptc.swii.sihope.domain.User;

/**
 * Vista de salida de un usuario para la API. Evita serializar la entidad JPA
 * {@link User} directamente (no expone el hash BCrypt ni la relación perezosa Role).
 * La entidad permanece intacta (regla 13).
 */
public class UserResponse {

    private Integer id;
    private String nombres;
    private String apellidos;
    private String codigo;
    private String correo;
    private boolean activo;
    private boolean verificado;
    private String rol;

    public UserResponse() {
    }

    public UserResponse(Integer id, String nombres, String apellidos, String codigo,
                        String correo, boolean activo, boolean verificado, String rol) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.codigo = codigo;
        this.correo = correo;
        this.activo = activo;
        this.verificado = verificado;
        this.rol = rol;
    }

    public static UserResponse desde(User u) {
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

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
