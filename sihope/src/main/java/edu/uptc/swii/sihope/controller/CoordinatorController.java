package edu.uptc.swii.sihope.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.domain.Application;
import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.dto.request.CreateVacancyRequest;
import edu.uptc.swii.sihope.dto.request.ApplicationStatusRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.dto.response.VacancyResponse;
import edu.uptc.swii.sihope.dto.response.ApplicationResponse;
import edu.uptc.swii.sihope.service.VacancyService;
import edu.uptc.swii.sihope.service.ApplicationService;
import edu.uptc.swii.sihope.service.UserService;
import edu.uptc.swii.sihope.service.UserService.PromotionResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/coordinador")
@Tag(name = "Coordinador",
        description = "Creación de convocatorias, revisión de postulaciones y promoción a monitor (HU_008/HU_009).")
public class CoordinatorController {

    private final VacancyService vacancyService;
    private final ApplicationService applicationService;
    private final UserService userService;

    public CoordinatorController(VacancyService vacancyService,
                                 ApplicationService applicationService,
                                 UserService userService) {
        this.vacancyService = vacancyService;
        this.applicationService = applicationService;
        this.userService = userService;
    }

    @PostMapping("/convocatorias")
    @Operation(summary = "Crear y publicar una convocatoria",
            description = "Valida campos obligatorios y fecha límite futura; se publica en estado ABIERTA.")
    public ResponseEntity<ApiResponse<Map<String, String>>> create(@RequestBody CreateVacancyRequest request,
                                                                    AuthenticatedUser authenticated) {
        Map<String, String> errors = vacancyService.create(authenticated.id(), request);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo crear la convocatoria.", errors));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Convocatoria publicada correctamente."));
    }

    @GetMapping("/convocatorias")
    @Operation(summary = "Listar todas las convocatorias")
    public ResponseEntity<ApiResponse<List<VacancyResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Convocatorias obtenidas.", vacancyService.listAll()));
    }

    @PatchMapping("/convocatorias/{id}/cerrar")
    @Operation(summary = "Cerrar una convocatoria")
    public ResponseEntity<ApiResponse<Void>> close(@PathVariable Integer id) {
        if (!vacancyService.close(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("La convocatoria no existe."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Convocatoria cerrada."));
    }

    @GetMapping("/convocatorias/{id}/postulaciones")
    @Operation(summary = "Listar postulaciones de una convocatoria")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> applications(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Postulaciones obtenidas.", applicationService.listByVacancy(id)));
    }

    @PatchMapping("/postulaciones/{id}/estado")
    @Operation(summary = "Aprobar o rechazar una postulación",
            description = "El estado debe ser APROBADA o RECHAZADA.")
    public ResponseEntity<ApiResponse<Void>> changeStatus(@PathVariable Integer id,
                                                          @RequestBody ApplicationStatusRequest request) {
        if (!applicationService.changeStatus(id, request.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Postulación no encontrada o estado inválido (usa APROBADA/RECHAZADA)."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Estado de la postulación actualizado."));
    }

    @PostMapping("/postulaciones/{id}/promover")
    @Operation(summary = "Promover al aspirante a monitor",
            description = "Solo aspirantes con postulación APROBADA. Invalida el token del usuario "
                    + "(fuerza re-login) y le otorga el rol MONITOR.")
    public ResponseEntity<ApiResponse<Void>> promote(@PathVariable Integer id) {
        Application application = applicationService.findById(id).orElse(null);
        if (application == null || application.getAspirante() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("La postulación no existe."));
        }

        PromotionResult result = userService.promoteToMonitor(application.getAspirante().getId());
        return switch (result) {
            case OK -> ResponseEntity.ok(ApiResponse.ok(
                    "Aspirante promovido a monitor. Deberá iniciar sesión de nuevo para aplicar los permisos."));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("El aspirante no existe."));
            case NOT_APPROVED -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Solo se puede promover a aspirantes con una postulación aprobada."));
            case ALREADY_MONITOR -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El aspirante ya tiene el rol de monitor."));
        };
    }
}
