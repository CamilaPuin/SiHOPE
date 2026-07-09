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
    public ResponseEntity<ApiResponse<List<UserResponse>>> listar() {
        List<UserResponse> usuarios = userService.listarUsuarios().stream()
                .map(UserResponse::desde)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Usuarios obtenidos.", usuarios));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> crear(@RequestBody CreateUserRequest request) {
        Map<String, String> errores = userService.crearUsuario(
                request.getNombre(), request.getCorreo(), request.getDocumento(), request.getRol());

        if (!errores.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo crear el usuario.", errores));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario creado correctamente. Se enviaron las credenciales por correo."));
    }

    @PutMapping("/{id}/rol")
    public ResponseEntity<ApiResponse<Void>> cambiarRol(@PathVariable Integer id,
                                                        @RequestBody ChangeRoleRequest request) {
        boolean ok = userService.cambiarRol(id, request.getRol());
        if (ok) {
            return ResponseEntity.ok(ApiResponse.ok("Rol actualizado correctamente."));
        }

        boolean existe = userService.listarUsuarios().stream()
                .anyMatch(u -> id.equals(u.getId()));
        if (!existe) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("El usuario no existe."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("El rol indicado no es válido."));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<Boolean>> cambiarEstado(@PathVariable Integer id) {
        Boolean nuevoEstado = userService.cambiarEstado(id);
        if (nuevoEstado == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("El usuario no existe."));
        }
        String mensaje = nuevoEstado ? "Cuenta activada." : "Cuenta desactivada.";
        return ResponseEntity.ok(ApiResponse.ok(mensaje, nuevoEstado));
    }
}
