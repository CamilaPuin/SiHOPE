package edu.uptc.swii.sihope.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.dto.request.ChangeRoleRequest;
import edu.uptc.swii.sihope.dto.request.CreateUserRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.dto.response.UserResponse;
import edu.uptc.swii.sihope.service.UserService;

@RestController
@RequestMapping("/api/admin/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> list() {
        List<UserResponse> users = userService.listUsers().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Usuarios obtenidos.", users));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> create(@RequestBody CreateUserRequest request) {
        Map<String, String> errors = userService.createUser(
                request.getName(), request.getEmail(), request.getDocument(),
                request.getRole(), request.getCareerId());

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo crear el usuario.", errors));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario creado correctamente. Se enviaron las credenciales por correo."));
    }

    @PutMapping("/{id}/rol")
    public ResponseEntity<ApiResponse<Void>> changeRole(@PathVariable Integer id,
                                                        @RequestBody ChangeRoleRequest request,
                                                        AuthenticatedUser auth) {
        return switch (userService.changeRole(id, request.getRole(), auth.id())) {
            case OK -> ResponseEntity.ok(ApiResponse.ok("Rol actualizado correctamente."));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("El usuario no existe."));
            case INVALID_ROLE -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El rol indicado no es válido."));
            case LAST_ADMIN -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(
                            "Debe existir al menos un administrador activo. Asigna otro administrador antes de cambiar tu rol."));
        };
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Boolean>> changeStatus(@PathVariable Integer id) {
        return switch (userService.changeStatus(id)) {
            case ACTIVATED -> ResponseEntity.ok(ApiResponse.ok("Cuenta activada.", true));
            case DEACTIVATED -> ResponseEntity.ok(ApiResponse.ok("Cuenta desactivada.", false));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("El usuario no existe."));
            case LAST_ADMIN -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("No puedes desactivar la única cuenta de administrador activa."));
        };
    }
}
