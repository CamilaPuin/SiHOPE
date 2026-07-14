package edu.uptc.swii.sihope.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.domain.Carrera;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.service.CarreraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/carreras")
@Tag(name = "Carreras",
        description = "Catálogo de carreras de la institución. "
                + "Se usa para asociar la carrera al crear usuarios con rol ESTUDIANTE.")
public class CarreraController {

    private final CarreraService carreraService;

    public CarreraController(CarreraService carreraService) {
        this.carreraService = carreraService;
    }

    @GetMapping
    @Operation(summary = "Listar el catálogo de carreras",
            description = "Devuelve todas las carreras registradas, ordenadas por nombre. "
                    + "Visible para cualquier usuario autenticado.")
    public ResponseEntity<ApiResponse<List<Carrera>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Carreras obtenidas.", carreraService.listCatalog()));
    }
}
