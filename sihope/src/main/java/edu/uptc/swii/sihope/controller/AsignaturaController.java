package edu.uptc.swii.sihope.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.domain.Asignatura;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.service.AsignaturaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/asignaturas")
@Tag(name = "Asignaturas",
        description = "Catálogo de asignaturas del programa (Paso 0, sustenta HU_004 y HU_002).")
public class AsignaturaController {

    private final AsignaturaService asignaturaService;

    public AsignaturaController(AsignaturaService asignaturaService) {
        this.asignaturaService = asignaturaService;
    }

    @GetMapping
    @Operation(summary = "Listar el catálogo de asignaturas",
            description = "Devuelve todas las asignaturas registradas, ordenadas por nombre. "
                    + "Visible para cualquier usuario autenticado.")
    public ResponseEntity<ApiResponse<List<Asignatura>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Asignaturas obtenidas.", asignaturaService.listCatalog()));
    }
}
