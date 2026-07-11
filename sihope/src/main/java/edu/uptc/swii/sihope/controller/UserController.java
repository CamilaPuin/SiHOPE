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
                request.getName(), request.getEmail(), request.getDocument(), request.getRole());

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo crear el usuario.", errors));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario creado correctamente. Se enviaron las credenciales por correo."));
    }

    @PutMapping("/{id}/rol")
    public ResponseEntity<ApiResponse<Void>> changeRole(@PathVariable Integer id,
                                                        @RequestBody ChangeRoleRequest request) {
        boolean ok = userService.changeRole(id, request.getRole());
        if (ok) {
            return ResponseEntity.ok(ApiResponse.ok("Rol actualizado correctamente."));
        }

        boolean exists = userService.listUsers().stream()
                .anyMatch(u -> id.equals(u.getId()));
        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("El usuario no existe."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("El rol indicado no es válido."));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Boolean>> changeStatus(@PathVariable Integer id) {
        Boolean newStatus = userService.changeStatus(id);
        if (newStatus == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("El usuario no existe."));
        }
        String message = newStatus ? "Cuenta activada." : "Cuenta desactivada.";
        return ResponseEntity.ok(ApiResponse.ok(message, newStatus));
    }
}
