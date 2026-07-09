package edu.uptc.swii.sihope.config;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import edu.uptc.swii.sihope.dto.UsuarioSesion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Guarda de autenticación por sesión para la API REST.
 * En lugar de redirigir (incompatible con una API JSON), responde con
 * 401/403 y un cuerpo {@code ApiResponse} en JSON. Se mantiene HttpSession
 * temporalmente hasta la migración a JWT.
 */
public class SesionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Dejar pasar el preflight CORS (el navegador no envía cookies en OPTIONS).
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        UsuarioSesion usuario = (session != null)
                ? (UsuarioSesion) session.getAttribute("usuarioSesion")
                : null;

        if (usuario == null) {
            escribirError(response, HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para acceder a este recurso.");
            return false;
        }

        if (request.getRequestURI().contains("/admin/") && !"ADMINISTRADOR".equals(usuario.getRol())) {
            escribirError(response, HttpStatus.FORBIDDEN, "No tienes permisos para acceder a este recurso.");
            return false;
        }

        return true;
    }

    private void escribirError(HttpServletResponse response, HttpStatus status, String mensaje) throws Exception {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"success\":false,\"message\":\"" + mensaje + "\",\"data\":null}";
        response.getWriter().write(json);
    }
}
