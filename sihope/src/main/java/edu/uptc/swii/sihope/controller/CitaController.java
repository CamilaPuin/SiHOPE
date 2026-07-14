package edu.uptc.swii.sihope.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.dto.TimeBlock;
import edu.uptc.swii.sihope.dto.request.CancelCitaRequest;
import edu.uptc.swii.sihope.dto.request.CreateCitaRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.dto.response.CitaResponse;
import edu.uptc.swii.sihope.service.CitaService;
import edu.uptc.swii.sihope.service.CitaService.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Citas de monitoría",
        description = "Agendamiento, confirmación y cancelación de citas entre estudiantes y monitores (HU_002).")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping("/api/monitores/{monitorId}/horarios-disponibles")
    @Operation(summary = "Horarios libres de un monitor para una fecha",
            description = "Devuelve las franjas de 1 hora disponibles (dentro de la disponibilidad del "
                    + "monitor, sin las ya reservadas ni las pasadas) para la fecha indicada.")
    public ResponseEntity<ApiResponse<List<TimeBlock>>> freeSlots(
            @PathVariable Integer monitorId,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String fecha) {
        LocalDate date;
        try {
            date = LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("La fecha debe tener el formato yyyy-MM-dd."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Horarios disponibles obtenidos.",
                citaService.freeSlots(monitorId, date)));
    }

    @GetMapping("/api/citas")
    @Operation(summary = "Mis citas",
            description = "Lista las citas del usuario autenticado, ya sea como estudiante o como monitor.")
    public ResponseEntity<ApiResponse<List<CitaResponse>>> myCitas(AuthenticatedUser authenticated) {
        return ResponseEntity.ok(ApiResponse.ok("Citas obtenidas.",
                citaService.listForUser(authenticated.id())));
    }

    @PostMapping("/api/citas")
    @Operation(summary = "Agendar una cita",
            description = "Reserva una cita con un monitor sobre una asignatura que él atiende, en un "
                    + "horario disponible. Si el horario ya está ocupado devuelve 409 con los horarios libres.")
    public ResponseEntity<ApiResponse<CitaResponse>> create(@RequestBody CreateCitaRequest request,
                                                            AuthenticatedUser authenticated) {
        Result result = citaService.create(authenticated.id(), request.getMonitorId(),
                request.getAsignaturaId(), request.getDate(), request.getStartTime(), request.getTopic());
        return toResponse(result, HttpStatus.CREATED, "Cita reservada. Se notificó al monitor.");
    }

    @PatchMapping("/api/citas/{id}/confirmar")
    @Operation(summary = "Confirmar una cita (monitor)")
    public ResponseEntity<ApiResponse<CitaResponse>> confirm(@PathVariable Integer id,
                                                            AuthenticatedUser authenticated) {
        return toResponse(citaService.confirm(id, authenticated.id()),
                HttpStatus.OK, "Cita confirmada. Se notificó al estudiante.");
    }

    @PatchMapping("/api/citas/{id}/cancelar")
    @Operation(summary = "Cancelar una cita",
            description = "Estudiante o monitor pueden cancelar hasta 2 horas antes del inicio. "
                    + "Libera el cupo y notifica a ambas partes.")
    public ResponseEntity<ApiResponse<CitaResponse>> cancel(@PathVariable Integer id,
                                                           @RequestBody(required = false) CancelCitaRequest request,
                                                           AuthenticatedUser authenticated) {
        String reason = request != null ? request.getReason() : null;
        return toResponse(citaService.cancel(id, authenticated.id(), reason),
                HttpStatus.OK, "Cita cancelada. Se notificó a ambas partes.");
    }

    @PatchMapping("/api/citas/{id}/atender")
    @Operation(summary = "Marcar una cita como atendida (monitor)")
    public ResponseEntity<ApiResponse<CitaResponse>> attend(@PathVariable Integer id,
                                                           AuthenticatedUser authenticated) {
        return toResponse(citaService.markAttended(id, authenticated.id()),
                HttpStatus.OK, "Cita marcada como atendida.");
    }

    private ResponseEntity<ApiResponse<CitaResponse>> toResponse(Result result, HttpStatus okStatus,
                                                                 String okMessage) {
        if (result.error() != null) {
            HttpStatus status = result.conflict() ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
            ApiResponse<CitaResponse> body = new ApiResponse<>(false, result.error(), null);
            return ResponseEntity.status(status).body(body);
        }
        return ResponseEntity.status(okStatus).body(ApiResponse.ok(okMessage, result.cita()));
    }
}
