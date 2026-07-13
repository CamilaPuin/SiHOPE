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
import edu.uptc.swii.sihope.repository.UserRepository;

@Service
public class AsignaturaService {

    private static final int MAX_SUBJECTS_PER_MONITOR = 15;

    private final AsignaturaRepository asignaturaRepository;
    private final UserRepository userRepository;

    public AsignaturaService(AsignaturaRepository asignaturaRepository,
                             UserRepository userRepository) {
        this.asignaturaRepository = asignaturaRepository;
        this.userRepository = userRepository;
    }

    public List<Asignatura> listCatalog() {
        return asignaturaRepository.findAllByOrderByNameAsc();
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
