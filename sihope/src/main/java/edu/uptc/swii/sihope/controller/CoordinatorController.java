package edu.uptc.swii.sihope.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uptc.swii.sihope.domain.Application;
import edu.uptc.swii.sihope.domain.Vacancy;
import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.dto.request.AssignSubjectsRequest;
import edu.uptc.swii.sihope.dto.request.CreateVacancyRequest;
import edu.uptc.swii.sihope.dto.request.ApplicationStatusRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.dto.response.CitasReportResponse;
import edu.uptc.swii.sihope.dto.response.VacancyResponse;
import edu.uptc.swii.sihope.dto.response.ApplicationResponse;
import edu.uptc.swii.sihope.service.AsignaturaService;
import edu.uptc.swii.sihope.service.ReportService;
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
    private final AsignaturaService asignaturaService;
    private final ReportService reportService;

    public CoordinatorController(VacancyService vacancyService,
                                 ApplicationService applicationService,
                                 UserService userService,
                                 AsignaturaService asignaturaService,
                                 ReportService reportService) {
        this.vacancyService = vacancyService;
        this.applicationService = applicationService;
        this.userService = userService;
        this.asignaturaService = asignaturaService;
        this.reportService = reportService;
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
            description = "Solo aspirantes con postulación APROBADA y mientras queden plazas libres "
                    + "en la convocatoria. Invalida el token del usuario (fuerza re-login), le otorga "
                    + "el rol MONITOR y le asigna las materias registradas en la convocatoria.")
    public ResponseEntity<ApiResponse<Void>> promote(@PathVariable Integer id) {
        Application application = applicationService.findById(id).orElse(null);
        if (application == null || application.getAspirante() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("La postulación no existe."));
        }

        if (Application.MONITOR_ASIGNADO.equals(application.getState())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Este aspirante ya ocupa una plaza de esta convocatoria."));
        }
        if (!Application.APROBADA.equals(application.getState())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Solo se puede promover a aspirantes con una postulación aprobada."));
        }

        Vacancy vacancy = application.getVacancy();
        if (vacancy != null) {
            long assigned = applicationService.countAssignedMonitors(vacancy.getId());
            if (assigned >= vacancy.getSlots()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("La convocatoria ya tiene sus " + vacancy.getSlots()
                                + " plaza(s) ocupada(s). No es posible asignar más monitores."));
            }
        }

        PromotionResult result = userService.promoteToMonitor(application.getAspirante().getId());
        if (result == PromotionResult.OK) {
            applicationService.markMonitorAssigned(application);
            asignaturaService.assignVacancySubjects(application.getAspirante().getId(),
                    vacancy);
            if (vacancy != null
                    && applicationService.countAssignedMonitors(vacancy.getId()) >= vacancy.getSlots()) {
                vacancyService.close(vacancy.getId());
            }
        }
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

    @GetMapping("/monitores/{id}/asignaturas")
    @Operation(summary = "Consultar las asignaturas de un monitor",
            description = "Devuelve las asignaturas que orienta el monitor indicado.")
    public ResponseEntity<ApiResponse<List<String>>> monitorSubjects(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Asignaturas obtenidas.", asignaturaService.subjectsOf(id)));
    }

    @PutMapping("/monitores/{id}/asignaturas")
    @Operation(summary = "Asignar asignaturas a un monitor",
            description = "Reemplaza por completo las asignaturas que orienta el monitor. Todas deben "
                    + "existir en el catálogo registrado por el administrador.")
    public ResponseEntity<ApiResponse<List<String>>> assignSubjects(@PathVariable Integer id,
                                                                    @RequestBody AssignSubjectsRequest request) {
        List<String> errors = asignaturaService.assignSubjects(id, request.getSubjectIds());
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudieron asignar las asignaturas.", errors));
        }
        return ResponseEntity.ok(ApiResponse.ok("Asignaturas asignadas correctamente."));
    }

    @GetMapping("/reportes/citas")
    @Operation(summary = "Reporte de citas atendidas por periodo (HU_007)",
            description = "Devuelve totales de citas atendidas por monitor y por tema entre dos fechas. "
                    + "Si el periodo no tiene datos, incluye un mensaje indicándolo.")
    public ResponseEntity<ApiResponse<CitasReportResponse>> citasReport(
            @RequestParam("desde") String desde,
            @RequestParam("hasta") String hasta,
            @RequestParam(value = "monitorId", required = false) Integer monitorId) {
        LocalDate[] range = parseRange(desde, hasta);
        if (range == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Las fechas 'desde' y 'hasta' deben tener el formato yyyy-MM-dd "
                            + "y 'desde' no puede ser posterior a 'hasta'."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Reporte generado.",
                reportService.citasAtendidas(range[0], range[1], monitorId)));
    }

    @GetMapping("/reportes/citas/export")
    @Operation(summary = "Exportar el reporte de citas atendidas (HU_007)",
            description = "Descarga el reporte en formato 'pdf' o 'excel'.")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam("desde") String desde,
            @RequestParam("hasta") String hasta,
            @RequestParam(value = "formato", defaultValue = "pdf") String formato,
            @RequestParam(value = "monitorId", required = false) Integer monitorId) {
        LocalDate[] range = parseRange(desde, hasta);
        if (range == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        boolean excel = "excel".equalsIgnoreCase(formato) || "xlsx".equalsIgnoreCase(formato);
        byte[] content = excel
                ? reportService.exportExcel(range[0], range[1], monitorId)
                : reportService.exportPdf(range[0], range[1], monitorId);
        String filename = "reporte-citas-" + desde + "-a-" + hasta + (excel ? ".xlsx" : ".pdf");
        MediaType type = excel
                ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.APPLICATION_PDF;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(type)
                .body(content);
    }

    private LocalDate[] parseRange(String desde, String hasta) {
        try {
            LocalDate from = LocalDate.parse(desde);
            LocalDate to = LocalDate.parse(hasta);
            if (from.isAfter(to)) {
                return null;
            }
            return new LocalDate[]{from, to};
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
