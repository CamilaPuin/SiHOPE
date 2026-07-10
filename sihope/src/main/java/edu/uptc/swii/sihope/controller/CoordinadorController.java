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

import edu.uptc.swii.sihope.domain.Postulacion;
import edu.uptc.swii.sihope.dto.UsuarioAutenticado;
import edu.uptc.swii.sihope.dto.request.CrearConvocatoriaRequest;
import edu.uptc.swii.sihope.dto.request.EstadoPostulacionRequest;
import edu.uptc.swii.sihope.dto.response.ApiResponse;
import edu.uptc.swii.sihope.dto.response.ConvocatoriaResponse;
import edu.uptc.swii.sihope.dto.response.PostulacionResponse;
import edu.uptc.swii.sihope.service.ConvocatoriaService;
import edu.uptc.swii.sihope.service.PostulacionService;
import edu.uptc.swii.sihope.service.UserService;
import edu.uptc.swii.sihope.service.UserService.ResultadoPromocion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Gestión de convocatorias, postulaciones y promoción a monitor por el coordinador
 * (HU_008 / HU_009). Todo {@code /api/coordinador/**} exige el rol COORDINADOR.
 */
@RestController
@RequestMapping("/api/coordinador")
@Tag(name = "Coordinador",
        description = "Creación de convocatorias, revisión de postulaciones y promoción a monitor (HU_008/HU_009).")
public class CoordinadorController {

    private final ConvocatoriaService convocatoriaService;
    private final PostulacionService postulacionService;
    private final UserService userService;

    public CoordinadorController(ConvocatoriaService convocatoriaService,
                                 PostulacionService postulacionService,
                                 UserService userService) {
        this.convocatoriaService = convocatoriaService;
        this.postulacionService = postulacionService;
        this.userService = userService;
    }

    @PostMapping("/convocatorias")
    @Operation(summary = "Crear y publicar una convocatoria",
            description = "Valida campos obligatorios y fecha límite futura; se publica en estado ABIERTA.")
    public ResponseEntity<ApiResponse<Map<String, String>>> crear(@RequestBody CrearConvocatoriaRequest request,
                                                                  UsuarioAutenticado autenticado) {
        Map<String, String> errores = convocatoriaService.crear(autenticado.id(), request);
        if (!errores.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No se pudo crear la convocatoria.", errores));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Convocatoria publicada correctamente."));
    }

    @GetMapping("/convocatorias")
    @Operation(summary = "Listar todas las convocatorias")
    public ResponseEntity<ApiResponse<List<ConvocatoriaResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Convocatorias obtenidas.", convocatoriaService.listarTodas()));
    }

    @PatchMapping("/convocatorias/{id}/cerrar")
    @Operation(summary = "Cerrar una convocatoria")
    public ResponseEntity<ApiResponse<Void>> cerrar(@PathVariable Integer id) {
        if (!convocatoriaService.cerrar(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("La convocatoria no existe."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Convocatoria cerrada."));
    }

    @GetMapping("/convocatorias/{id}/postulaciones")
    @Operation(summary = "Listar postulaciones de una convocatoria")
    public ResponseEntity<ApiResponse<List<PostulacionResponse>>> postulaciones(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Postulaciones obtenidas.", postulacionService.listarPorConvocatoria(id)));
    }

    @PatchMapping("/postulaciones/{id}/estado")
    @Operation(summary = "Aprobar o rechazar una postulación",
            description = "El estado debe ser APROBADA o RECHAZADA.")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(@PathVariable Integer id,
                                                           @RequestBody EstadoPostulacionRequest request) {
        if (!postulacionService.cambiarEstado(id, request.getEstado())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Postulación no encontrada o estado inválido (usa APROBADA/RECHAZADA)."));
        }
        return ResponseEntity.ok(ApiResponse.ok("Estado de la postulación actualizado."));
    }

    @PostMapping("/postulaciones/{id}/promover")
    @Operation(summary = "Promover al aspirante a monitor",
            description = "Solo aspirantes con postulación APROBADA. Invalida el token del usuario "
                    + "(fuerza re-login) y le otorga el rol MONITOR.")
    public ResponseEntity<ApiResponse<Void>> promover(@PathVariable Integer id) {
        Postulacion postulacion = postulacionService.obtener(id).orElse(null);
        if (postulacion == null || postulacion.getAspirante() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("La postulación no existe."));
        }

        ResultadoPromocion resultado = userService.promoverAMonitor(postulacion.getAspirante().getId());
        return switch (resultado) {
            case OK -> ResponseEntity.ok(ApiResponse.ok(
                    "Aspirante promovido a monitor. Deberá iniciar sesión de nuevo para aplicar los permisos."));
            case NO_EXISTE -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("El aspirante no existe."));
            case NO_APROBADO -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Solo se puede promover a aspirantes con una postulación aprobada."));
            case YA_ES_MONITOR -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("El aspirante ya tiene el rol de monitor."));
        };
    }
}
