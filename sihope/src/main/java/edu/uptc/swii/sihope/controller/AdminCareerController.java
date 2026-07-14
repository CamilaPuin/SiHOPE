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

import edu.uptc.swii.sihope.domain.Carrera;
import edu.uptc.swii.sihope.dto.request.CreateCareerRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.service.CarreraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/carreras")
@Tag(name = "Administración de carreras",
        description = "Gestión del catálogo de carreras por parte del administrador.")
public class AdminCareerController {

    private final CarreraService carreraService;

    public AdminCareerController(CarreraService carreraService) {
        this.carreraService = carreraService;
    }

    @GetMapping
    @Operation(summary = "Listar el catálogo de carreras",
            description = "Devuelve todas las carreras registradas, ordenadas por nombre.")
    public ResponseEntity<ApiResponse<List<Carrera>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Carreras obtenidas.", carreraService.listCatalog()));
    }

    @PostMapping
    @Operation(summary = "Registrar una carrera",
            description = "Crea una carrera en el catálogo. Falla si el nombre está vacío o ya existe.")
    public ResponseEntity<ApiResponse<List<String>>> create(@RequestBody CreateCareerRequest request) {
        List<String> errors = carreraService.createCareer(request.getName());
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo registrar la carrera.", errors));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Carrera registrada correctamente."));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una carrera",
            description = "Elimina una carrera del catálogo. Se bloquea si hay usuarios asociados a ella.")
    public ResponseEntity<ApiResponse<List<String>>> delete(@PathVariable Integer id) {
        List<String> errors = carreraService.deleteCareer(id);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo eliminar la carrera.", errors));
        }
        return ResponseEntity.ok(ApiResponse.ok("Carrera eliminada correctamente."));
    }
}
