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

@Service
public class JwtService {

    public static final String CLAIM_ID = "id";
    public static final String CLAIM_ROLE = "rol";
    public static final String CLAIM_NAME = "nombre";
    public static final String CLAIM_INITIALS = "iniciales";
    public static final String CLAIM_TOKEN_VERSION = "tv";

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generate(User u) {
        String name = (nullToEmpty(u.getFirstName()) + " " + nullToEmpty(u.getLastName())).trim();
        String initials = (initial(u.getFirstName()) + initial(u.getLastName())).toUpperCase();
        String role = u.getRole() != null ? u.getRole().getName() : "";
        Date now = new Date();

        return Jwts.builder()
                .subject(u.getEmail())
                .claim(CLAIM_ID, u.getId())
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_NAME, name)
                .claim(CLAIM_INITIALS, initials)
                .claim(CLAIM_TOKEN_VERSION, u.getTokenVersion())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Claims parseAndValidate(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String initial(String s) {
        return (s == null || s.isBlank()) ? "" : s.substring(0, 1);
    }
}
