package edu.uptc.swii.sihope.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.dto.request.RecoverPasswordRequest;
import edu.uptc.swii.sihope.dto.request.ResetPasswordRequest;
import edu.uptc.swii.sihope.dto.request.UpdatePasswordRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.service.UserService;
import edu.uptc.swii.sihope.service.UserService.PasswordChangeResult;
import edu.uptc.swii.sihope.service.UserService.PasswordResetResult;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/credenciales")
@Tag(name = "Credenciales", description = "Cambio, recuperación y restablecimiento de contraseña.")
public class CredentialsController {

    private final UserService userService;

    public CredentialsController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody UpdatePasswordRequest request,
                                                            AuthenticatedUser authenticated) {

        if (authenticated == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Debes iniciar sesión para cambiar la contraseña."));
        }

        if (request.getNewPassword() == null || !request.getNewPassword().equals(request.getNewPassword2())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Las contraseñas nuevas no coinciden."));
        }

        PasswordChangeResult result = userService.changePassword(
                authenticated.email(), request.getCurrentPassword(), request.getNewPassword());

        return switch (result) {
            case OK -> ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada correctamente."));
            case WRONG_CURRENT -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("La contraseña actual es incorrecta."));
            case INVALID_NEW -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("La nueva contraseña no cumple los requisitos de seguridad."));
        };
    }

    @PostMapping("/recuperar")
    public ResponseEntity<ApiResponse<Void>> recover(@RequestBody RecoverPasswordRequest request) {
        userService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(
                "Si el correo está registrado, recibirás instrucciones para restablecer la contraseña."));
    }

    @PostMapping("/restablecer")
    public ResponseEntity<ApiResponse<Void>> reset(@RequestBody ResetPasswordRequest request) {

        if (request.getNewPassword() == null || !request.getNewPassword().equals(request.getNewPassword2())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Las contraseñas no coinciden."));
        }

        PasswordResetResult result = userService.resetPassword(request.getToken(), request.getNewPassword());

        return switch (result) {
            case OK -> ResponseEntity.ok(ApiResponse.ok("Contraseña restablecida correctamente."));
            case INVALID_TOKEN -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El enlace de recuperación no es válido."));
            case EXPIRED -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El enlace expiró. Solicita uno nuevo."));
            case INVALID_NEW -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("La contraseña no cumple los requisitos de seguridad."));
        };
    }
}
