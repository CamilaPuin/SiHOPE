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

import edu.uptc.swii.sihope.domain.Vacancy;
import edu.uptc.swii.sihope.domain.Application;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.response.ApplicationResponse;
import edu.uptc.swii.sihope.repository.VacancyRepository;
import edu.uptc.swii.sihope.repository.ApplicationRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ApplicationService(ApplicationRepository applicationRepository,
                              VacancyRepository vacancyRepository,
                              UserRepository userRepository,
                              ObjectMapper objectMapper) {
        this.applicationRepository = applicationRepository;
        this.vacancyRepository = vacancyRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, String> apply(Integer vacancyId, Integer applicantId, Map<String, String> datos) {
        Map<String, String> errors = new LinkedHashMap<>();

        Optional<Vacancy> opt = vacancyRepository.findById(vacancyId);
        if (opt.isEmpty()) {
            errors.put("convocatoria", "La convocatoria no existe.");
            return errors;
        }
        Vacancy vacancy = opt.get();

        if (!Vacancy.ABIERTA.equals(vacancy.getStatus())
                || vacancy.getDeadline().isBefore(LocalDate.now())) {
            errors.put("convocatoria", "La convocatoria no está disponible para postulaciones.");
            return errors;
        }

        if (applicationRepository.existsByVacancyIdAndApplicantId(vacancyId, applicantId)) {
            errors.put("convocatoria", "Ya te postulaste a esta convocatoria.");
            return errors;
        }

        User applicant = userRepository.findById(applicantId).orElse(null);
        if (applicant == null) {
            errors.put("aspirante", "El aspirante no existe.");
            return errors;
        }

        Application application = new Application();
        application.setVacancy(vacancy);
        application.setApplicant(applicant);
        application.setState(Application.PENDIENTE);
        application.setDataJson(serialize(datos));
        application.setAppliedAt(LocalDateTime.now());
        applicationRepository.save(application);

        return errors;
    }

    public List<ApplicationResponse> listByVacancy(Integer vacancyId) {
        return applicationRepository.findByVacancyIdOrderByAppliedAtAsc(vacancyId)
                .stream().map(this::toResponse).toList();
    }

    public List<ApplicationResponse> listByApplicant(Integer applicantId) {
        return applicationRepository.findByApplicantId(applicantId)
                .stream().map(this::toResponse).toList();
    }

    public boolean changeStatus(Integer applicationId, String status) {
        if (!Application.APROBADA.equals(status) && !Application.RECHAZADA.equals(status)) {
            return false;
        }
        Optional<Application> opt = applicationRepository.findById(applicationId);
        if (opt.isEmpty()) {
            return false;
        }
        Application application = opt.get();
        if (Application.MONITOR_ASIGNADO.equals(application.getState())) {
            return false;
        }
        application.setState(status);
        applicationRepository.save(application);
        return true;
    }

    public long countAssignedMonitors(Integer vacancyId) {
        return applicationRepository.countByVacancyIdAndState(vacancyId, Application.MONITOR_ASIGNADO);
    }

    public void markMonitorAssigned(Application application) {
        application.setState(Application.MONITOR_ASIGNADO);
        applicationRepository.save(application);
    }

    public Optional<Application> findById(Integer applicationId) {
        return applicationRepository.findById(applicationId);
    }

    private ApplicationResponse toResponse(Application application) {
        User a = application.getApplicant();
        return new ApplicationResponse(
                application.getId(),
                application.getVacancy() != null ? application.getVacancy().getId() : null,
                application.getVacancy() != null ? application.getVacancy().getTitle() : null,
                a != null ? a.getId() : null,
                a != null ? (a.getFirstName() + " " + a.getLastName()).trim() : null,
                a != null ? a.getEmail() : null,
                application.getState(),
                deserialize(application.getDataJson()),
                application.getAppliedAt() != null ? application.getAppliedAt().toString() : null);
    }

    private String serialize(Map<String, String> datos) {
        try {
            return objectMapper.writeValueAsString(datos == null ? Map.of() : datos);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, String> deserialize(String json) {
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
