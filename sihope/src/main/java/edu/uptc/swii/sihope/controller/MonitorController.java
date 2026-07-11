package edu.uptc.swii.sihope.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.dto.TimeBlock;
import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.dto.request.AvailabilityRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.dto.response.MonitorDirectoryResponse;
import edu.uptc.swii.sihope.service.AvailabilityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Disponibilidad del monitor",
        description = "Gestión de la matriz horaria semanal del monitor (HU_006).")
public class MonitorController {

    private final AvailabilityService availabilityService;

    public MonitorController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/api/monitor/disponibilidad")
    @Operation(summary = "Consultar mi disponibilidad",
            description = "Devuelve las franjas horarias semanales del monitor autenticado.")
    public ResponseEntity<ApiResponse<List<TimeBlock>>> myAvailability(AuthenticatedUser authenticated) {
        List<TimeBlock> blocks = availabilityService.getBlocks(authenticated.id());
        return ResponseEntity.ok(ApiResponse.ok("Disponibilidad obtenida.", blocks));
    }

    @PutMapping("/api/monitor/disponibilidad")
    @Operation(summary = "Actualizar mi disponibilidad",
            description = "Reemplaza por completo la matriz horaria del monitor. Valida días (1-7), "
                    + "formato de horas, inicio<fin y ausencia de solapes.")
    public ResponseEntity<ApiResponse<List<String>>> update(@RequestBody AvailabilityRequest request,
                                                             AuthenticatedUser authenticated) {
        List<String> errors = availabilityService.replace(authenticated.id(), request.getBlocks());
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo guardar la disponibilidad.", errors));
        }
        return ResponseEntity.ok(ApiResponse.ok("Disponibilidad actualizada correctamente."));
    }

    @GetMapping("/api/monitores")
    @Operation(summary = "Directorio de monitores",
            description = "Lista los monitores con las franjas horarias que han marcado como "
                    + "disponibles; visible para todos los roles autenticados.")
    public ResponseEntity<ApiResponse<List<MonitorDirectoryResponse>>> listMonitors() {
        return ResponseEntity.ok(
                ApiResponse.ok("Monitores obtenidos.", availabilityService.listMonitors()));
    }

    @GetMapping("/api/monitores/{id}/disponibilidad")
    @Operation(summary = "Ver la disponibilidad de un monitor",
            description = "Devuelve solo las franjas marcadas por el monitor indicado (lectura para estudiantes).")
    public ResponseEntity<ApiResponse<List<TimeBlock>>> availabilityOf(@PathVariable Integer id) {
        List<TimeBlock> blocks = availabilityService.getBlocks(id);
        return ResponseEntity.ok(ApiResponse.ok("Disponibilidad obtenida.", blocks));
    }
}
