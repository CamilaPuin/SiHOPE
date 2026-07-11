package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.uptc.swii.sihope.domain.Vacancy;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.request.CreateVacancyRequest;
import edu.uptc.swii.sihope.repository.VacancyRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

class VacancyServiceTest {

    @Mock
    private VacancyRepository vacancyRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private VacancyService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(new User()));
    }

    private CreateVacancyRequest validRequest() {
        CreateVacancyRequest r = new CreateVacancyRequest();
        r.setTitle("Monitor de Cálculo");
        r.setSubject("Cálculo Diferencial");
        r.setRequirements("Promedio >= 4.0");
        r.setSlots(2);
        r.setDeadline(LocalDate.now().plusDays(10).toString());
        return r;
    }

    @Test
    void createsValidVacancyInOpenState() {
        Map<String, String> errors = service.create(1, validRequest());
        assertTrue(errors.isEmpty());
        verify(vacancyRepository).save(any(Vacancy.class));
    }

    @Test
    void rejectsEmptyRequiredFields() {
        Map<String, String> errors = service.create(1, new CreateVacancyRequest());
        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey("titulo"));
        assertTrue(errors.containsKey("materia"));
        assertTrue(errors.containsKey("requisitos"));
        assertTrue(errors.containsKey("plazas"));
        assertTrue(errors.containsKey("fechaLimite"));
        verify(vacancyRepository, never()).save(any());
    }

    @Test
    void rejectsDeadlineInThePast() {
        CreateVacancyRequest r = validRequest();
        r.setDeadline(LocalDate.now().minusDays(1).toString());
        Map<String, String> errors = service.create(1, r);
        assertTrue(errors.containsKey("fechaLimite"));
        verify(vacancyRepository, never()).save(any());
    }

    @Test
    void rejectsNonPositivePlazas() {
        CreateVacancyRequest r = validRequest();
        r.setSlots(0);
        Map<String, String> errors = service.create(1, r);
        assertTrue(errors.containsKey("plazas"));
    }
}
