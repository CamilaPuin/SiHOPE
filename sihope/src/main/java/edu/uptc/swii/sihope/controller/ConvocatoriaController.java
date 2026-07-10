package edu.uptc.swii.sihope.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.domain.Convocatoria;
import edu.uptc.swii.sihope.dto.UsuarioAutenticado;
import edu.uptc.swii.sihope.dto.request.PostulacionRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.dto.response.ConvocatoriaResponse;
import edu.uptc.swii.sihope.service.ConvocatoriaService;
import edu.uptc.swii.sihope.service.PostulacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Convocatorias abiertas y postulación de aspirantes (HU_005).
 * Bajo {@code /api/convocatorias/**} el interceptor exige sesión; la postulación
 * se restringe además al rol ESTUDIANTE.
 */
@RestController
@RequestMapping("/api/convocatorias")
@Tag(name = "Convocatorias (aspirante)",
        description = "Listado de convocatorias abiertas y postulación de estudiantes (HU_005).")
public class ConvocatoriaController {

    private final ConvocatoriaService convocatoriaService;
    private final PostulacionService postulacionService;

    public ConvocatoriaController(ConvocatoriaService convocatoriaService,
                                  PostulacionService postulacionService) {
        this.convocatoriaService = convocatoriaService;
        this.postulacionService = postulacionService;
    }

    @GetMapping
    @Operation(summary = "Listar convocatorias abiertas",
            description = "Convocatorias en estado ABIERTA y no vencidas, visibles para los aspirantes.")
    public ResponseEntity<ApiResponse<List<ConvocatoriaResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Convocatorias obtenidas.", convocatoriaService.listarAbiertas()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una convocatoria")
    public ResponseEntity<ApiResponse<ConvocatoriaResponse>> detalle(@PathVariable Integer id) {
        return convocatoriaService.obtener(id)
                .map(c -> ResponseEntity.ok(ApiResponse.ok("Convocatoria obtenida.", ConvocatoriaResponse.desde(c))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("La convocatoria no existe.")));
    }

    @PostMapping("/{id}/postulaciones")
    @Operation(summary = "Postularse a una convocatoria",
            description = "Registra la postulación del estudiante autenticado con campos parametrizables.")
    public ResponseEntity<ApiResponse<Map<String, String>>> postular(@PathVariable Integer id,
                                                                      @RequestBody PostulacionRequest request,
                                                                      UsuarioAutenticado autenticado) {
        if (!"ESTUDIANTE".equals(autenticado.rol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Solo los estudiantes aspirantes pueden postularse."));
        }

        Map<String, String> errores = postulacionService.postular(id, autenticado.id(), request.getDatos());
        if (!errores.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo registrar la postulación.", errores));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("¡Postulación registrada correctamente!"));
    }
}
