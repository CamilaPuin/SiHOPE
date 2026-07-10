package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.uptc.swii.sihope.domain.Role;
import edu.uptc.swii.sihope.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

/**
 * Pruebas unitarias del {@link JwtService}: emisión, validación, expiración y
 * detección de firmas inválidas.
 */
class JwtServiceTest {

    private static final String SECRETO = "clave-de-prueba-sihope-min-32-bytes-1234567890";

    private User usuarioDemo() {
        User u = new User();
        u.setId(42);
        u.setNombres("Ana");
        u.setApellidos("Gómez");
        u.setCorreo("ana@uptc.edu.co");
        u.setTokenVersion(3);
        u.setRole(new Role("MONITOR"));
        return u;
    }

    @Test
    void generaYValidaTokenConLosClaimsEsperados() {
        JwtService jwt = new JwtService(SECRETO, 60_000);
        String token = jwt.generar(usuarioDemo());

        Claims claims = jwt.validarYExtraer(token);

        assertEquals("ana@uptc.edu.co", claims.getSubject());
        assertEquals(42, claims.get(JwtService.CLAIM_ID, Integer.class));
        assertEquals("MONITOR", claims.get(JwtService.CLAIM_ROL, String.class));
        assertEquals(3, claims.get(JwtService.CLAIM_TOKEN_VERSION, Integer.class));
    }

    @Test
    void rechazaTokenExpirado() {
        JwtService jwt = new JwtService(SECRETO, -1_000); // ya expirado al emitir
        String token = jwt.generar(usuarioDemo());
        assertThrows(JwtException.class, () -> jwt.validarYExtraer(token));
    }

    @Test
    void rechazaTokenConFirmaInvalida() {
        JwtService emisor = new JwtService(SECRETO, 60_000);
        JwtService otro = new JwtService("otra-clave-distinta-de-min-32-bytes-abcdef", 60_000);
        String token = emisor.generar(usuarioDemo());
        assertThrows(JwtException.class, () -> otro.validarYExtraer(token));
    }
}
