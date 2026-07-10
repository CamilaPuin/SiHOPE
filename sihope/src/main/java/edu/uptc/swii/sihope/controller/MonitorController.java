package edu.uptc.swii.sihope.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.dto.BloqueHorario;
import edu.uptc.swii.sihope.dto.UsuarioAutenticado;
import edu.uptc.swii.sihope.dto.request.DisponibilidadRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.dto.response.MonitorDirectorioResponse;
import edu.uptc.swii.sihope.service.DisponibilidadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoints de disponibilidad horaria del monitor (HU_006).
 * Las rutas {@code /api/monitor/**} exigen el rol MONITOR (ver JwtAuthInterceptor);
 * la lectura por id vive bajo {@code /api/monitores/**} (solo requiere sesión).
 */
@RestController
@Tag(name = "Disponibilidad del monitor",
        description = "Gestión de la matriz horaria semanal del monitor (HU_006).")
public class MonitorController {

    private final DisponibilidadService disponibilidadService;

    public MonitorController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @GetMapping("/api/monitor/disponibilidad")
    @Operation(summary = "Consultar mi disponibilidad",
            description = "Devuelve las franjas horarias semanales del monitor autenticado.")
    public ResponseEntity<ApiResponse<List<BloqueHorario>>> miDisponibilidad(UsuarioAutenticado autenticado) {
        List<BloqueHorario> bloques = disponibilidadService.consultar(autenticado.id());
        return ResponseEntity.ok(ApiResponse.ok("Disponibilidad obtenida.", bloques));
    }

    @PutMapping("/api/monitor/disponibilidad")
    @Operation(summary = "Actualizar mi disponibilidad",
            description = "Reemplaza por completo la matriz horaria del monitor. Valida días (1-7), "
                    + "formato de horas, inicio<fin y ausencia de solapes.")
    public ResponseEntity<ApiResponse<List<String>>> actualizar(@RequestBody DisponibilidadRequest request,
                                                                 UsuarioAutenticado autenticado) {
        List<String> errores = disponibilidadService.reemplazar(autenticado.id(), request.getBloques());
        if (!errores.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo guardar la disponibilidad.", errores));
        }
        return ResponseEntity.ok(ApiResponse.ok("Disponibilidad actualizada correctamente."));
    }

    @GetMapping("/api/monitores")
    @Operation(summary = "Directorio de monitores",
            description = "Lista los monitores con las franjas horarias que han marcado como "
                    + "disponibles; visible para todos los roles autenticados.")
    public ResponseEntity<ApiResponse<List<MonitorDirectorioResponse>>> listarMonitores() {
        return ResponseEntity.ok(
                ApiResponse.ok("Monitores obtenidos.", disponibilidadService.listarMonitores()));
    }

    @GetMapping("/api/monitores/{id}/disponibilidad")
    @Operation(summary = "Ver la disponibilidad de un monitor",
            description = "Devuelve solo las franjas marcadas por el monitor indicado (lectura para estudiantes).")
    public ResponseEntity<ApiResponse<List<BloqueHorario>>> disponibilidadDe(@PathVariable Integer id) {
        List<BloqueHorario> bloques = disponibilidadService.consultar(id);
        return ResponseEntity.ok(ApiResponse.ok("Disponibilidad obtenida.", bloques));
    }
}
