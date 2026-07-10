package edu.uptc.swii.sihope.dto;

/**
 * Datos del usuario autenticado extraídos del JWT tras validarlo. El
 * {@code JwtAuthInterceptor} lo deja como atributo del request y el
 * {@code UsuarioArgumentResolver} lo inyecta en los métodos de los controllers
 * que declaren un parámetro de este tipo.
 */
public record UsuarioAutenticado(Integer id, String correo, String rol,
                                 String nombre, String iniciales) {

    /** Nombre del atributo con el que viaja en el request. */
    public static final String ATRIBUTO = "usuarioAutenticado";
}
