package edu.uptc.swii.sihope.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uptc.swii.sihope.domain.Asignatura;
import edu.uptc.swii.sihope.domain.Vacancy;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.request.CreateVacancyRequest;
import edu.uptc.swii.sihope.dto.response.VacancyResponse;
import edu.uptc.swii.sihope.repository.AsignaturaRepository;
import edu.uptc.swii.sihope.repository.VacancyRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final AsignaturaRepository asignaturaRepository;

    public VacancyService(VacancyRepository vacancyRepository,
                          UserRepository userRepository,
                          AsignaturaRepository asignaturaRepository) {
        this.vacancyRepository = vacancyRepository;
        this.userRepository = userRepository;
        this.asignaturaRepository = asignaturaRepository;
    }

    @Transactional
    public Map<String, String> create(Integer coordinatorId, CreateVacancyRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(req.getTitle()))     errors.put("titulo", "El título es obligatorio.");
        if (isBlank(req.getRequirements())) errors.put("requisitos", "Los requisitos son obligatorios.");

        Set<Asignatura> subjects = new LinkedHashSet<>();
        if (req.getSubjectIds() == null || req.getSubjectIds().isEmpty()) {
            errors.put("materiaIds", "Selecciona al menos una materia del catálogo.");
        } else {
            for (Integer id : req.getSubjectIds()) {
                Asignatura asignatura = (id == null) ? null
                        : asignaturaRepository.findById(id).orElse(null);
                if (asignatura == null) {
                    errors.put("materiaIds", "Alguna de las materias seleccionadas no existe en el catálogo.");
                    break;
                }
                subjects.add(asignatura);
            }
        }

        if (req.getSlots() == null || req.getSlots() < 1) {
            errors.put("plazas", "Las plazas deben ser un número mayor o igual a 1.");
        }

        LocalDate deadline = null;
        if (isBlank(req.getDeadline())) {
            errors.put("fechaLimite", "La fecha límite es obligatoria.");
        } else {
            try {
                deadline = LocalDate.parse(req.getDeadline().trim());
                if (deadline.isBefore(LocalDate.now())) {
                    errors.put("fechaLimite", "La fecha límite no puede estar en el pasado.");
                }
            } catch (DateTimeParseException e) {
                errors.put("fechaLimite", "La fecha límite debe tener el formato yyyy-MM-dd.");
            }
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        User coordinator = userRepository.findById(coordinatorId).orElse(null);

        Vacancy vacancy = new Vacancy();
        vacancy.setTitle(req.getTitle().trim());
        vacancy.setDescription(req.getDescription());
        vacancy.setRequirements(req.getRequirements().trim());
        vacancy.setSubjects(subjects);
        vacancy.setSubject(subjects.stream().map(Asignatura::getName)
                .collect(Collectors.joining(", ")));
        vacancy.setSlots(req.getSlots());
        vacancy.setDeadline(deadline);
        vacancy.setStatus(Vacancy.ABIERTA);
        vacancy.setCreatedAt(LocalDateTime.now());
        vacancy.setCoordinator(coordinator);
        vacancyRepository.save(vacancy);

        return errors;
    }

    @Transactional(readOnly = true)
    public List<VacancyResponse> listAll() {
        return vacancyRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(VacancyResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<VacancyResponse> listOpen() {
        return vacancyRepository
                .findByStatusAndDeadlineGreaterThanEqualOrderByDeadlineAsc(
                        Vacancy.ABIERTA, LocalDate.now())
                .stream().map(VacancyResponse::from).toList();
    }

    public boolean close(Integer id) {
        Optional<Vacancy> opt = vacancyRepository.findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        Vacancy vacancy = opt.get();
        vacancy.setStatus(Vacancy.CERRADA);
        vacancyRepository.save(vacancy);
        return true;
    }

    public Optional<Vacancy> findById(Integer id) {
        return vacancyRepository.findById(id);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
