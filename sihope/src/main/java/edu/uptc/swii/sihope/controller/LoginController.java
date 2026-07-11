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
import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.dto.UserSession;
import edu.uptc.swii.sihope.dto.request.LoginRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.dto.response.LoginResponse;
import edu.uptc.swii.sihope.service.JwtService;
import edu.uptc.swii.sihope.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login por JWT, sesión actual y cierre de sesión.")
public class LoginController {

    private final UserService userService;
    private final JwtService jwtService;

    public LoginController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión",
            description = "Valida las credenciales y devuelve un token JWT junto con los datos del usuario.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {

        Optional<User> result = userService.authenticate(request.getEmail(), request.getPassword());

        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Correo o contraseña incorrectos."));
        }

        User user = result.get();

        if (!user.isVerificado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("La cuenta aún no ha sido verificada."));
        }

        if (!user.isActivo()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("La cuenta está inactiva. Contacta al administrador."));
        }

        String token = jwtService.generate(user);
        LoginResponse body = new LoginResponse(token, UserSession.from(user));

        return ResponseEntity.ok(ApiResponse.ok("Inicio de sesión exitoso.", body));
    }

    @GetMapping("/me")
    @Operation(summary = "Sesión actual",
            description = "Devuelve el usuario asociado al token JWT enviado en el header Authorization.")
    public ResponseEntity<ApiResponse<UserSession>> me(AuthenticatedUser authenticated) {
        if (authenticated == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No hay una sesión activa."));
        }
        UserSession session = new UserSession(
                authenticated.name(), authenticated.initials(), authenticated.email(), authenticated.role());
        return ResponseEntity.ok(ApiResponse.ok("Sesión activa.", session));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión",
            description = "La autenticación es stateless: el cliente descarta el token. Se responde OK por conveniencia.")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada correctamente."));
    }
}
