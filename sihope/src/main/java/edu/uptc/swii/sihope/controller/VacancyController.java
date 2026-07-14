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

import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.dto.request.ApplicationRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.dto.response.ApplicationResponse;
import edu.uptc.swii.sihope.dto.response.VacancyResponse;
import edu.uptc.swii.sihope.service.VacancyService;
import edu.uptc.swii.sihope.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/convocatorias")
@Tag(name = "Convocatorias (aspirante)",
        description = "Listado de convocatorias abiertas y postulación de estudiantes (HU_005).")
public class VacancyController {

    private final VacancyService vacancyService;
    private final ApplicationService applicationService;

    public VacancyController(VacancyService vacancyService,
                             ApplicationService applicationService) {
        this.vacancyService = vacancyService;
        this.applicationService = applicationService;
    }

    @GetMapping
    @Operation(summary = "Listar convocatorias abiertas",
            description = "Convocatorias en estado ABIERTA y no vencidas, visibles para los aspirantes.")
    public ResponseEntity<ApiResponse<List<VacancyResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Convocatorias obtenidas.", vacancyService.listOpen()));
    }

    @GetMapping("/mis-postulaciones")
    @Operation(summary = "Mis postulaciones",
            description = "Postulaciones del usuario autenticado, con su estado actual "
                    + "(PENDIENTE, APROBADA, RECHAZADA o MONITOR_ASIGNADO).")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> myApplications(
            AuthenticatedUser authenticated) {
        return ResponseEntity.ok(ApiResponse.ok("Postulaciones obtenidas.",
                applicationService.listByApplicant(authenticated.id())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una convocatoria")
    public ResponseEntity<ApiResponse<VacancyResponse>> detail(@PathVariable Integer id) {
        return vacancyService.findById(id)
                .map(c -> ResponseEntity.ok(ApiResponse.ok("Convocatoria obtenida.", VacancyResponse.from(c))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("La convocatoria no existe.")));
    }

    @PostMapping("/{id}/postulaciones")
    @Operation(summary = "Postularse a una convocatoria",
            description = "Registra la postulación del estudiante autenticado con campos parametrizables.")
    public ResponseEntity<ApiResponse<Map<String, String>>> apply(@PathVariable Integer id,
                                                                  @RequestBody ApplicationRequest request,
                                                                  AuthenticatedUser authenticated) {
        if (!"ESTUDIANTE".equals(authenticated.role())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Solo los estudiantes aspirantes pueden postularse."));
        }

        Map<String, String> errors = applicationService.apply(id, authenticated.id(), request.getData());
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo registrar la postulación.", errors));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("¡Postulación registrada correctamente!"));
    }
}
