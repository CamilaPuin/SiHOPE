package edu.uptc.swii.sihope.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uptc.swii.sihope.domain.Convocatoria;
import edu.uptc.swii.sihope.domain.Postulacion;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.response.PostulacionResponse;
import edu.uptc.swii.sihope.repository.ConvocatoriaRepository;
import edu.uptc.swii.sihope.repository.PostulacionRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

/**
 * Lógica de negocio de las postulaciones a convocatorias (HU_005 / HU_009).
 * Los campos del formulario se guardan como JSON ({@code datos_json}) para que el
 * formulario sea parametrizable sin cambios de esquema.
 */
@Service
public class PostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final ConvocatoriaRepository convocatoriaRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public PostulacionService(PostulacionRepository postulacionRepository,
                              ConvocatoriaRepository convocatoriaRepository,
                              UserRepository userRepository,
                              ObjectMapper objectMapper) {
        this.postulacionRepository = postulacionRepository;
        this.convocatoriaRepository = convocatoriaRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Registra la postulación de un aspirante a una convocatoria.
     *
     * @return mapa de errores; vacío si la postulación se registró correctamente.
     */
    public Map<String, String> postular(Integer convocatoriaId, Integer aspiranteId, Map<String, String> datos) {
        Map<String, String> errores = new LinkedHashMap<>();

        Optional<Convocatoria> opt = convocatoriaRepository.findById(convocatoriaId);
        if (opt.isEmpty()) {
            errores.put("convocatoria", "La convocatoria no existe.");
            return errores;
        }
        Convocatoria convocatoria = opt.get();

        if (!Convocatoria.ABIERTA.equals(convocatoria.getEstado())
                || convocatoria.getFechaLimite().isBefore(LocalDate.now())) {
            errores.put("convocatoria", "La convocatoria no está disponible para postulaciones.");
            return errores;
        }

        if (postulacionRepository.existsByConvocatoriaIdAndAspiranteId(convocatoriaId, aspiranteId)) {
            errores.put("convocatoria", "Ya te postulaste a esta convocatoria.");
            return errores;
        }

        User aspirante = userRepository.findById(aspiranteId).orElse(null);
        if (aspirante == null) {
            errores.put("aspirante", "El aspirante no existe.");
            return errores;
        }

        Postulacion p = new Postulacion();
        p.setConvocatoria(convocatoria);
        p.setAspirante(aspirante);
        p.setEstado(Postulacion.PENDIENTE);
        p.setDatosJson(serializar(datos));
        p.setFechaPostulacion(LocalDateTime.now());
        postulacionRepository.save(p);

        return errores;
    }

    /** Postulaciones de una convocatoria para el panel del coordinador. */
    public List<PostulacionResponse> listarPorConvocatoria(Integer convocatoriaId) {
        return postulacionRepository.findByConvocatoriaIdOrderByFechaPostulacionAsc(convocatoriaId)
                .stream().map(this::aResponse).toList();
    }

    /**
     * Aprueba o rechaza una postulación.
     *
     * @return true si la postulación existe y el estado es válido (APROBADA/RECHAZADA).
     */
    public boolean cambiarEstado(Integer postulacionId, String estado) {
        if (!Postulacion.APROBADA.equals(estado) && !Postulacion.RECHAZADA.equals(estado)) {
            return false;
        }
        Optional<Postulacion> opt = postulacionRepository.findById(postulacionId);
        if (opt.isEmpty()) {
            return false;
        }
        Postulacion p = opt.get();
        p.setEstado(estado);
        postulacionRepository.save(p);
        return true;
    }

    public Optional<Postulacion> obtener(Integer postulacionId) {
        return postulacionRepository.findById(postulacionId);
    }

    private PostulacionResponse aResponse(Postulacion p) {
        User a = p.getAspirante();
        return new PostulacionResponse(
                p.getId(),
                p.getConvocatoria() != null ? p.getConvocatoria().getId() : null,
                p.getConvocatoria() != null ? p.getConvocatoria().getTitulo() : null,
                a != null ? a.getId() : null,
                a != null ? (a.getNombres() + " " + a.getApellidos()).trim() : null,
                a != null ? a.getCorreo() : null,
                p.getEstado(),
                deserializar(p.getDatosJson()),
                p.getFechaPostulacion() != null ? p.getFechaPostulacion().toString() : null);
    }

    private String serializar(Map<String, String> datos) {
        try {
            return objectMapper.writeValueAsString(datos == null ? Map.of() : datos);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, String> deserializar(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            return Map.of();
        }
    }
}
