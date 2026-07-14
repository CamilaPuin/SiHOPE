package edu.uptc.swii.sihope.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uptc.swii.sihope.domain.Asignatura;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.repository.AsignaturaRepository;
import edu.uptc.swii.sihope.repository.CitaRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

@Service
public class AsignaturaService {

    private static final int MAX_SUBJECTS_PER_MONITOR = 15;

    private final AsignaturaRepository asignaturaRepository;
    private final UserRepository userRepository;
    private final CitaRepository citaRepository;

    public AsignaturaService(AsignaturaRepository asignaturaRepository,
                             UserRepository userRepository,
                             CitaRepository citaRepository) {
        this.asignaturaRepository = asignaturaRepository;
        this.userRepository = userRepository;
        this.citaRepository = citaRepository;
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
     * Elimina una asignatura del catálogo. Se bloquea si algún monitor la atiende
     * o si hay citas asociadas, para no romper la integridad referencial.
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
        asignaturaRepository.delete(asignatura);
        return errors;
    }

    @Transactional(readOnly = true)
    public List<String> subjectsOf(Integer monitorId) {
        return userRepository.findById(monitorId)
                .map(u -> u.getSubjects().stream().map(Asignatura::getName).sorted().toList())
                .orElseGet(List::of);
    }

    @Transactional
    public List<String> replaceSubjects(Integer monitorId, List<String> names) {
        List<String> errors = new ArrayList<>();

        User monitor = userRepository.findById(monitorId).orElse(null);
        if (monitor == null) {
            errors.add("El monitor no existe.");
            return errors;
        }

        List<String> cleaned = normalize(names);
        if (cleaned.size() > MAX_SUBJECTS_PER_MONITOR) {
            errors.add("Puedes registrar como máximo " + MAX_SUBJECTS_PER_MONITOR + " asignaturas.");
            return errors;
        }

        Set<Asignatura> resolved = new LinkedHashSet<>();
        for (String name : cleaned) {
            resolved.add(findOrCreate(name));
        }

        monitor.setSubjects(resolved);
        userRepository.save(monitor);
        return errors;
    }

    @Transactional
    public void assignByName(Integer monitorId, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        User monitor = userRepository.findById(monitorId).orElse(null);
        if (monitor == null) {
            return;
        }
        monitor.getSubjects().add(findOrCreate(name));
        userRepository.save(monitor);
    }

    @Transactional
    public Asignatura findOrCreate(String name) {
        String clean = name.trim();
        Optional<Asignatura> existing = asignaturaRepository.findByNameIgnoreCase(clean);
        return existing.orElseGet(() -> asignaturaRepository.save(new Asignatura(clean)));
    }

    private List<String> normalize(List<String> names) {
        List<String> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        if (names == null) {
            return out;
        }
        for (String raw : names) {
            if (raw == null) {
                continue;
            }
            String clean = raw.trim();
            if (clean.isEmpty()) {
                continue;
            }
            String key = clean.toLowerCase();
            if (seen.add(key)) {
                out.add(clean);
            }
        }
        return out;
    }
}
