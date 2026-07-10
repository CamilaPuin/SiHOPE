package edu.uptc.swii.sihope.config;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.UsuarioAutenticado;
import edu.uptc.swii.sihope.repository.UserRepository;
import edu.uptc.swii.sihope.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Guarda de autenticación por JWT para la API REST (reemplaza al
 * {@code SesionInterceptor} basado en HttpSession del Sprint 1).
 *
 * <p>Valida el {@code Authorization: Bearer <token>}, verifica la firma y la
 * expiración, y confirma contra la base de datos que el usuario siga activo y que
 * la versión del token ({@code tv}) coincida con la actual. Este último control es
 * la clave de la mitigación de HU_009: al promover a un aspirante (o cambiar su
 * rol/estado) se incrementa {@code token_version}, dejando obsoleto cualquier token
 * anterior y forzando un nuevo inicio de sesión.
 *
 * <p>Además aplica autorización por prefijo de ruta:
 * {@code /api/admin/**}→ADMINISTRADOR, {@code /api/monitor/**}→MONITOR,
 * {@code /api/coordinador/**}→COORDINADOR. El resto de rutas protegidas solo
 * requieren estar autenticado.
 */
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

        // Dejar pasar el preflight CORS (el navegador no envía cabeceras en OPTIONS).
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String cabecera = request.getHeader("Authorization");
        if (cabecera == null || !cabecera.startsWith(BEARER)) {
            escribirError(response, HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para acceder a este recurso.");
            return false;
        }

        Claims claims;
        try {
            claims = jwtService.validarYExtraer(cabecera.substring(BEARER.length()).trim());
        } catch (JwtException | IllegalArgumentException e) {
            escribirError(response, HttpStatus.UNAUTHORIZED,
                    "Tu sesión expiró o no es válida. Inicia sesión de nuevo.");
            return false;
        }

        Integer id = claims.get(JwtService.CLAIM_ID, Integer.class);
        Integer tv = claims.get(JwtService.CLAIM_TOKEN_VERSION, Integer.class);
        Optional<User> opt = (id != null) ? userRepository.findById(id) : Optional.empty();

        if (opt.isEmpty() || !opt.get().isActivo()
                || tv == null || tv != opt.get().getTokenVersion()) {
            escribirError(response, HttpStatus.UNAUTHORIZED,
                    "Tu sesión ya no es válida. Vuelve a iniciar sesión.");
            return false;
        }

        User usuario = opt.get();
        String rol = usuario.getRole() != null ? usuario.getRole().getNombre() : "";

        String rolRequerido = rolRequeridoPara(request.getRequestURI());
        if (rolRequerido != null && !rolRequerido.equals(rol)) {
            escribirError(response, HttpStatus.FORBIDDEN,
                    "No tienes permisos para acceder a este recurso.");
            return false;
        }

        request.setAttribute(UsuarioAutenticado.ATRIBUTO, new UsuarioAutenticado(
                usuario.getId(), usuario.getCorreo(), rol,
                claims.get(JwtService.CLAIM_NOMBRE, String.class),
                claims.get(JwtService.CLAIM_INICIALES, String.class)));
        return true;
    }

    /** Rol exigido según el prefijo de la ruta, o {@code null} si basta con estar autenticado. */
    private String rolRequeridoPara(String uri) {
        if (uri.contains("/api/admin/"))       return "ADMINISTRADOR";
        if (uri.contains("/api/coordinador/"))  return "COORDINADOR";
        if (uri.contains("/api/monitor/"))      return "MONITOR";
        return null;
    }

    private void escribirError(HttpServletResponse response, HttpStatus status, String mensaje) throws Exception {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"success\":false,\"message\":\"" + mensaje + "\",\"data\":null}";
        response.getWriter().write(json);
    }
}
