package edu.uptc.swii.sihope.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uptc.swii.sihope.domain.Asignatura;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.domain.Vacancy;
import edu.uptc.swii.sihope.repository.AsignaturaRepository;
import edu.uptc.swii.sihope.repository.CitaRepository;
import edu.uptc.swii.sihope.repository.UserRepository;
import edu.uptc.swii.sihope.repository.VacancyRepository;

@Service
public class AsignaturaService {

    private static final int MAX_SUBJECTS_PER_MONITOR = 15;
    private static final String MONITOR_ROLE = "MONITOR";

    private final AsignaturaRepository asignaturaRepository;
    private final UserRepository userRepository;
    private final CitaRepository citaRepository;
    private final VacancyRepository vacancyRepository;

    public AsignaturaService(AsignaturaRepository asignaturaRepository,
                             UserRepository userRepository,
                             CitaRepository citaRepository,
                             VacancyRepository vacancyRepository) {
        this.asignaturaRepository = asignaturaRepository;
        this.userRepository = userRepository;
        this.citaRepository = citaRepository;
        this.vacancyRepository = vacancyRepository;
    }

    public List<Asignatura> listCatalog() {
        return asignaturaRepository.findAllByOrderByNameAsc();
    }

    /** Alta manual de una asignatura del catálogo (panel del administrador). */
    @Transactional
    public List<String> createSubject(String name) {
        List<String> errors = new ArrayList<>();
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty()) {
            errors.add("El nombre de la asignatura es obligatorio.");
            return errors;
        }
        if (asignaturaRepository.existsByNameIgnoreCase(clean)) {
            errors.add("Ya existe una asignatura con ese nombre.");
            return errors;
        }
        asignaturaRepository.save(new Asignatura(clean));
        return errors;
    }

    /**
     * Elimina una asignatura del catálogo. Se bloquea si algún monitor la atiende,
     * si hay citas asociadas o si pertenece a una convocatoria, para no romper la
     * integridad referencial.
     */
    @Transactional
    public List<String> deleteSubject(Integer id) {
        List<String> errors = new ArrayList<>();
        Asignatura asignatura = asignaturaRepository.findById(id).orElse(null);
        if (asignatura == null) {
            errors.add("La asignatura no existe.");
            return errors;
        }
        if (userRepository.countBySubjects_Id(id) > 0) {
            errors.add("No se puede eliminar: hay monitores que atienden esta asignatura.");
            return errors;
        }
        if (citaRepository.countBySubject_Id(id) > 0) {
            errors.add("No se puede eliminar: hay citas asociadas a esta asignatura.");
            return errors;
        }
        if (vacancyRepository.countBySubjects_Id(id) > 0) {
            errors.add("No se puede eliminar: hay convocatorias asociadas a esta asignatura.");
            return errors;
        }
        asignaturaRepository.delete(asignatura);
        return errors;
    }

    @Transactional(readOnly = true)
    public List<String> subjectsOf(Integer monitorId) {
        return userRepository.findById(monitorId)
                .map(u -> u.getSubjects().stream().map(Asignatura::getName).sorted().toList())
                .orElseGet(List::of);
    }

    /**
     * Asignación de asignaturas a un monitor por parte del coordinador. Reemplaza
     * por completo las asignaturas del monitor; todas deben existir en el catálogo
     * (registradas previamente por el administrador).
     */
    @Transactional
    public List<String> assignSubjects(Integer monitorId, List<Integer> subjectIds) {
        List<String> errors = new ArrayList<>();

        User monitor = userRepository.findById(monitorId).orElse(null);
        if (monitor == null || monitor.getRole() == null
                || !MONITOR_ROLE.equals(monitor.getRole().getName())) {
            errors.add("El monitor no existe.");
            return errors;
        }

        List<Integer> ids = subjectIds == null ? List.of() : subjectIds;
        if (ids.size() > MAX_SUBJECTS_PER_MONITOR) {
            errors.add("Un monitor puede tener como máximo " + MAX_SUBJECTS_PER_MONITOR + " asignaturas.");
            return errors;
        }

        Set<Asignatura> resolved = new LinkedHashSet<>();
        for (Integer id : ids) {
            Asignatura asignatura = (id == null) ? null
                    : asignaturaRepository.findById(id).orElse(null);
            if (asignatura == null) {
                errors.add("Alguna de las asignaturas seleccionadas no existe en el catálogo.");
                return errors;
            }
            resolved.add(asignatura);
        }

        monitor.setSubjects(resolved);
        userRepository.save(monitor);
        return errors;
    }

    /**
     * Al promover al ganador de una convocatoria, se le asignan las materias que
     * el coordinador registró en esa convocatoria.
     */
    @Transactional
    public void assignVacancySubjects(Integer monitorId, Vacancy vacancy) {
        if (vacancy == null || vacancy.getId() == null) {
            return;
        }
        Vacancy managed = vacancyRepository.findById(vacancy.getId()).orElse(null);
        if (managed == null || managed.getSubjects().isEmpty()) {
            return;
        }
        User monitor = userRepository.findById(monitorId).orElse(null);
        if (monitor == null) {
            return;
        }
        monitor.getSubjects().addAll(managed.getSubjects());
        userRepository.save(monitor);
    }
}
