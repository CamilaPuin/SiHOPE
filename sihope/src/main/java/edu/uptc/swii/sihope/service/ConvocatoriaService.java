package edu.uptc.swii.sihope.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import edu.uptc.swii.sihope.domain.Convocatoria;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.request.CrearConvocatoriaRequest;
import edu.uptc.swii.sihope.dto.response.ConvocatoriaResponse;
import edu.uptc.swii.sihope.repository.ConvocatoriaRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

/**
 * Lógica de negocio de las convocatorias (HU_008 / HU_005).
 *
 * <p>Aplica las validaciones obligatorias de la mitigación de HU_008 (campos
 * requeridos, plazas positivas y fecha límite futura) antes de publicar, y
 * controla el estado ABIERTA/CERRADA. Una convocatoria abierta pero vencida deja
 * de listarse como visible para los aspirantes.
 */
@Service
public class ConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final UserRepository userRepository;

    public ConvocatoriaService(ConvocatoriaRepository convocatoriaRepository,
                               UserRepository userRepository) {
        this.convocatoriaRepository = convocatoriaRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crea y publica una convocatoria en estado ABIERTA.
     *
     * @return mapa de errores por campo; vacío si se creó correctamente.
     */
    public Map<String, String> crear(Integer coordinadorId, CrearConvocatoriaRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();

        if (esVacio(req.getTitulo()))     errores.put("titulo", "El título es obligatorio.");
        if (esVacio(req.getMateria()))    errores.put("materia", "La materia es obligatoria.");
        if (esVacio(req.getRequisitos())) errores.put("requisitos", "Los requisitos son obligatorios.");

        if (req.getPlazas() == null || req.getPlazas() < 1) {
            errores.put("plazas", "Las plazas deben ser un número mayor o igual a 1.");
        }

        LocalDate fechaLimite = null;
        if (esVacio(req.getFechaLimite())) {
            errores.put("fechaLimite", "La fecha límite es obligatoria.");
        } else {
            try {
                fechaLimite = LocalDate.parse(req.getFechaLimite().trim());
                if (fechaLimite.isBefore(LocalDate.now())) {
                    errores.put("fechaLimite", "La fecha límite no puede estar en el pasado.");
                }
            } catch (DateTimeParseException e) {
                errores.put("fechaLimite", "La fecha límite debe tener el formato yyyy-MM-dd.");
            }
        }

        if (!errores.isEmpty()) {
            return errores;
        }

        User coordinador = userRepository.findById(coordinadorId).orElse(null);

        Convocatoria c = new Convocatoria();
        c.setTitulo(req.getTitulo().trim());
        c.setDescripcion(req.getDescripcion());
        c.setRequisitos(req.getRequisitos().trim());
        c.setMateria(req.getMateria().trim());
        c.setPlazas(req.getPlazas());
        c.setFechaLimite(fechaLimite);
        c.setEstado(Convocatoria.ABIERTA);
        c.setFechaCreacion(LocalDateTime.now());
        c.setCoordinador(coordinador);
        convocatoriaRepository.save(c);

        return errores;
    }

    /** Todas las convocatorias (panel del coordinador), más recientes primero. */
    public List<ConvocatoriaResponse> listarTodas() {
        return convocatoriaRepository.findAllByOrderByFechaCreacionDesc()
                .stream().map(ConvocatoriaResponse::desde).toList();
    }

    /** Convocatorias visibles para aspirantes: abiertas y no vencidas. */
    public List<ConvocatoriaResponse> listarAbiertas() {
        return convocatoriaRepository
                .findByEstadoAndFechaLimiteGreaterThanEqualOrderByFechaLimiteAsc(
                        Convocatoria.ABIERTA, LocalDate.now())
                .stream().map(ConvocatoriaResponse::desde).toList();
    }

    /** Cierra una convocatoria. @return true si existía. */
    public boolean cerrar(Integer id) {
        Optional<Convocatoria> opt = convocatoriaRepository.findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        Convocatoria c = opt.get();
        c.setEstado(Convocatoria.CERRADA);
        convocatoriaRepository.save(c);
        return true;
    }

    public Optional<Convocatoria> obtener(Integer id) {
        return convocatoriaRepository.findById(id);
    }

    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}
