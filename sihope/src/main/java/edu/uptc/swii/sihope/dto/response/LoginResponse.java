package edu.uptc.swii.sihope.dto.response;

import edu.uptc.swii.sihope.dto.UsuarioSesion;

/**
 * Respuesta del login: el token JWT que el frontend guarda y adjunta como
 * {@code Authorization: Bearer}, junto con los datos del usuario para hidratar
 * la sesión sin una llamada adicional.
 */
public record LoginResponse(String token, UsuarioSesion usuario) {
}
