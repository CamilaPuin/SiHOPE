package edu.uptc.swii.sihope.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.uptc.swii.sihope.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Generación y validación de JSON Web Tokens (JWT) con firma HMAC-SHA256.
 *
 * <p>El token es la única fuente de autenticación de la API (stateless): sustituye
 * a la HttpSession del Sprint 1. Cada token transporta los datos de la sesión como
 * <em>claims</em> ({@code correo}, {@code rol}, {@code nombre}, {@code iniciales}) y
 * una versión ({@code tv}) que permite invalidarlo de inmediato al cambiar el rol,
 * el estado o la contraseña del usuario (ver {@code token_version} en {@link User}).
 */
@Service
public class JwtService {

    public static final String CLAIM_ID = "id";
    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_NOMBRE = "nombre";
    public static final String CLAIM_INICIALES = "iniciales";
    public static final String CLAIM_TOKEN_VERSION = "tv";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    /** Emite un token firmado para el usuario indicado. */
    public String generar(User u) {
        String nombre = (nvl(u.getNombres()) + " " + nvl(u.getApellidos())).trim();
        String iniciales = (inicial(u.getNombres()) + inicial(u.getApellidos())).toUpperCase();
        String rol = u.getRole() != null ? u.getRole().getNombre() : "";
        Date ahora = new Date();

        return Jwts.builder()
                .subject(u.getCorreo())
                .claim(CLAIM_ID, u.getId())
                .claim(CLAIM_ROL, rol)
                .claim(CLAIM_NOMBRE, nombre)
                .claim(CLAIM_INICIALES, iniciales)
                .claim(CLAIM_TOKEN_VERSION, u.getTokenVersion())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /**
     * Valida la firma y la expiración del token y devuelve sus claims.
     *
     * @throws JwtException si el token es inválido, está manipulado o expiró.
     */
    public Claims validarYExtraer(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String inicial(String s) {
        return (s == null || s.isBlank()) ? "" : s.substring(0, 1);
    }
}
