package edu.uptc.swii.sihope.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.dto.UserDTO;
import edu.uptc.swii.sihope.dto.request.RegisterRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.service.UserService;

@RestController
@RequestMapping("/api/registro")
public class RegistroController {

    private final UserService userService;

    public RegistroController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> registrar(@RequestBody RegisterRequest request) {
        UserDTO form = new UserDTO();
        form.setNombres(request.getNombres());
        form.setApellidos(request.getApellidos());
        form.setCodigo(request.getCodigo());
        form.setCorreo(request.getCorreo());
        form.setPassword(request.getPassword());
        form.setPassword2(request.getPassword2());

        Map<String, String> errores = userService.registrarEstudiante(form);

        if (!errores.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo completar el registro.", errores));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registro exitoso. Revisa tu correo para verificar la cuenta."));
    }

    @GetMapping("/verificar")
    public ResponseEntity<ApiResponse<Void>> verificar(@RequestParam("token") String token) {
        boolean ok = userService.verificarCuenta(token);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El enlace de verificación no es válido o ya fue utilizado."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Cuenta verificada correctamente. Ya puedes iniciar sesión."));
    }
}
