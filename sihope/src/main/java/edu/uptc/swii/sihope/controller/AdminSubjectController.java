package edu.uptc.swii.sihope.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.domain.Asignatura;
import edu.uptc.swii.sihope.dto.request.CreateSubjectRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.service.AsignaturaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/asignaturas")
@Tag(name = "Administración de asignaturas",
        description = "Gestión del catálogo de asignaturas por parte del administrador. "
                + "El catálogo ya no trae materias por defecto: se registran aquí.")
public class AdminSubjectController {

    private final AsignaturaService asignaturaService;

    public AdminSubjectController(AsignaturaService asignaturaService) {
        this.asignaturaService = asignaturaService;
    }

    @GetMapping
    @Operation(summary = "Listar el catálogo de asignaturas",
            description = "Devuelve todas las asignaturas registradas, ordenadas por nombre.")
    public ResponseEntity<ApiResponse<List<Asignatura>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Asignaturas obtenidas.", asignaturaService.listCatalog()));
    }

    @PostMapping
    @Operation(summary = "Registrar una asignatura",
            description = "Crea una asignatura en el catálogo. Falla si el nombre está vacío o ya existe.")
    public ResponseEntity<ApiResponse<List<String>>> create(@RequestBody CreateSubjectRequest request) {
        List<String> errors = asignaturaService.createSubject(request.getName());
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo registrar la asignatura.", errors));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Asignatura registrada correctamente."));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una asignatura",
            description = "Elimina una asignatura del catálogo. Se bloquea si algún monitor la atiende, "
                    + "si existen citas asociadas o si pertenece a una convocatoria.")
    public ResponseEntity<ApiResponse<List<String>>> delete(@PathVariable Integer id) {
        List<String> errors = asignaturaService.deleteSubject(id);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo eliminar la asignatura.", errors));
        }
        return ResponseEntity.ok(ApiResponse.ok("Asignatura eliminada correctamente."));
    }
}
