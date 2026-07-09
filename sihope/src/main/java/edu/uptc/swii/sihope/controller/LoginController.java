package edu.uptc.swii.sihope.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.UsuarioSesion;
import edu.uptc.swii.sihope.dto.request.LoginRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.service.UserService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UsuarioSesion>> login(@RequestBody LoginRequest request,
                                                            HttpSession session) {

        Optional<User> resultado = userService.autenticar(request.getCorreo(), request.getPassword());

        if (resultado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Correo o contraseña incorrectos."));
        }

        User usuario = resultado.get();

        if (!usuario.isVerificado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("La cuenta aún no ha sido verificada."));
        }

        if (!usuario.isActivo()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("La cuenta está inactiva. Contacta al administrador."));
        }

        UsuarioSesion sesion = UsuarioSesion.desde(usuario);
        session.setAttribute("usuarioSesion", sesion);

        return ResponseEntity.ok(ApiResponse.ok("Inicio de sesión exitoso.", sesion));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioSesion>> me(HttpSession session) {
        UsuarioSesion sesion = (UsuarioSesion) session.getAttribute("usuarioSesion");
        if (sesion == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No hay una sesión activa."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Sesión activa.", sesion));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada correctamente."));
    }
}
