package edu.uptc.swii.sihope.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.dto.UsuarioAutenticado;
import edu.uptc.swii.sihope.dto.request.RecoverPasswordRequest;
import edu.uptc.swii.sihope.dto.request.ResetPasswordRequest;
import edu.uptc.swii.sihope.dto.request.UpdatePasswordRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.service.UserService;
import edu.uptc.swii.sihope.service.UserService.ResultadoCambio;
import edu.uptc.swii.sihope.service.UserService.ResultadoReset;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/credenciales")
@Tag(name = "Credenciales", description = "Cambio, recuperación y restablecimiento de contraseña.")
public class CredencialesController {

    private final UserService userService;

    public CredencialesController(UserService userService) {
        this.userService = userService;
    }

    /** Cambio de contraseña de la cuenta autenticada (identificada por el token JWT). */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(@RequestBody UpdatePasswordRequest request,
                                                             UsuarioAutenticado autenticado) {

        if (autenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Debes iniciar sesión para cambiar la contraseña."));
        }

        if (request.getNueva() == null || !request.getNueva().equals(request.getNueva2())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Las contraseñas nuevas no coinciden."));
        }

        ResultadoCambio resultado = userService.cambiarPassword(
                autenticado.correo(), request.getActual(), request.getNueva());

        return switch (resultado) {
            case OK -> ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada correctamente."));
            case ACTUAL_INCORRECTA -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("La contraseña actual es incorrecta."));
            case NUEVA_INVALIDA -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("La nueva contraseña no cumple los requisitos de seguridad."));
        };
    }

    /** Solicita un correo de recuperación. Respuesta genérica para no revelar si el correo existe. */
    @PostMapping("/recuperar")
    public ResponseEntity<ApiResponse<Void>> recuperar(@RequestBody RecoverPasswordRequest request) {
        userService.solicitarRecuperacion(request.getCorreo());
        return ResponseEntity.ok(ApiResponse.ok(
                "Si el correo está registrado, recibirás instrucciones para restablecer la contraseña."));
    }

    /** Restablece la contraseña usando el token recibido por correo. */
    @PostMapping("/restablecer")
    public ResponseEntity<ApiResponse<Void>> restablecer(@RequestBody ResetPasswordRequest request) {

        if (request.getNueva() == null || !request.getNueva().equals(request.getNueva2())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Las contraseñas no coinciden."));
        }

        ResultadoReset resultado = userService.restablecerPassword(request.getToken(), request.getNueva());

        return switch (resultado) {
            case OK -> ResponseEntity.ok(ApiResponse.ok("Contraseña restablecida correctamente."));
            case TOKEN_INVALIDO -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El enlace de recuperación no es válido."));
            case EXPIRADO -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El enlace expiró. Solicita uno nuevo."));
            case NUEVA_INVALIDA -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("La contraseña no cumple los requisitos de seguridad."));
        };
    }
}
