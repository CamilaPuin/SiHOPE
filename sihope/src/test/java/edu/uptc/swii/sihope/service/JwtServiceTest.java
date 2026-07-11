package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.uptc.swii.sihope.domain.Role;
import edu.uptc.swii.sihope.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "clave-de-prueba-sihope-min-32-bytes-1234567890";

    private User demoUser() {
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
    void generatesAndValidatesTokenWithExpectedClaims() {
        JwtService jwt = new JwtService(SECRET, 60_000);
        String token = jwt.generate(demoUser());

        Claims claims = jwt.parseAndValidate(token);

        assertEquals("ana@uptc.edu.co", claims.getSubject());
        assertEquals(42, claims.get(JwtService.CLAIM_ID, Integer.class));
        assertEquals("MONITOR", claims.get(JwtService.CLAIM_ROLE, String.class));
        assertEquals(3, claims.get(JwtService.CLAIM_TOKEN_VERSION, Integer.class));
    }

    @Test
    void rejectsExpiredToken() {
        JwtService jwt = new JwtService(SECRET, -1_000);
        String token = jwt.generate(demoUser());
        assertThrows(JwtException.class, () -> jwt.parseAndValidate(token));
    }

    @Test
    void rejectsTokenWithInvalidSignature() {
        JwtService issuer = new JwtService(SECRET, 60_000);
        JwtService other = new JwtService("otra-clave-distinta-de-min-32-bytes-abcdef", 60_000);
        String token = issuer.generate(demoUser());
        assertThrows(JwtException.class, () -> other.parseAndValidate(token));
    }
}
