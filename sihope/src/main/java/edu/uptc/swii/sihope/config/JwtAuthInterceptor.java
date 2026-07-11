package edu.uptc.swii.sihope.config;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.repository.UserRepository;
import edu.uptc.swii.sihope.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER)) {
            writeError(response, HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para acceder a este recurso.");
            return false;
        }

        Claims claims;
        try {
            claims = jwtService.parseAndValidate(header.substring(BEARER.length()).trim());
        } catch (JwtException | IllegalArgumentException e) {
            writeError(response, HttpStatus.UNAUTHORIZED,
                    "Tu sesión expiró o no es válida. Inicia sesión de nuevo.");
            return false;
        }

        Integer id = claims.get(JwtService.CLAIM_ID, Integer.class);
        Integer tv = claims.get(JwtService.CLAIM_TOKEN_VERSION, Integer.class);
        Optional<User> opt = (id != null) ? userRepository.findById(id) : Optional.empty();

        if (opt.isEmpty() || !opt.get().isActivo()
                || tv == null || tv != opt.get().getTokenVersion()) {
            writeError(response, HttpStatus.UNAUTHORIZED,
                    "Tu sesión ya no es válida. Vuelve a iniciar sesión.");
            return false;
        }

        User user = opt.get();
        String role = user.getRole() != null ? user.getRole().getName() : "";

        String requiredRole = requiredRoleFor(request.getRequestURI());
        if (requiredRole != null && !requiredRole.equals(role)) {
            writeError(response, HttpStatus.FORBIDDEN,
                    "No tienes permisos para acceder a este recurso.");
            return false;
        }

        request.setAttribute(AuthenticatedUser.ATTRIBUTE, new AuthenticatedUser(
                user.getId(), user.getEmail(), role,
                claims.get(JwtService.CLAIM_NAME, String.class),
                claims.get(JwtService.CLAIM_INITIALS, String.class)));
        return true;
    }

    private String requiredRoleFor(String uri) {
        if (uri.contains("/api/admin/"))       return "ADMINISTRADOR";
        if (uri.contains("/api/coordinador/"))  return "COORDINADOR";
        if (uri.contains("/api/monitor/"))      return "MONITOR";
        return null;
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws Exception {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"success\":false,\"message\":\"" + message + "\",\"data\":null}";
        response.getWriter().write(json);
    }
}
