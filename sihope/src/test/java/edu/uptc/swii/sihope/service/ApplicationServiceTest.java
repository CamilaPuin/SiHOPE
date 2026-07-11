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

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uptc.swii.sihope.domain.Vacancy;
import edu.uptc.swii.sihope.domain.Application;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.repository.VacancyRepository;
import edu.uptc.swii.sihope.repository.ApplicationRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private VacancyRepository vacancyRepository;
    @Mock
    private UserRepository userRepository;
    private ApplicationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ApplicationService(applicationRepository, vacancyRepository,
                userRepository, new ObjectMapper());
    }

    private Vacancy openVacancy() {
        Vacancy c = new Vacancy();
        c.setId(5);
        c.setStatus(Vacancy.ABIERTA);
        c.setDeadline(LocalDate.now().plusDays(5));
        return c;
    }

    @Test
    void registersValidApplication() {
        when(vacancyRepository.findById(5)).thenReturn(Optional.of(openVacancy()));
        when(applicationRepository.existsByVacancyIdAndApplicantId(5, 10)).thenReturn(false);
        when(userRepository.findById(10)).thenReturn(Optional.of(new User()));

        Map<String, String> errors = service.apply(5, 10, Map.of("promedio", "4.5"));

        assertTrue(errors.isEmpty());
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void rejectsIfVacancyDoesNotExist() {
        when(vacancyRepository.findById(anyInt())).thenReturn(Optional.empty());
        Map<String, String> errors = service.apply(99, 10, Map.of());
        assertFalse(errors.isEmpty());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void rejectsIfVacancyIsClosed() {
        Vacancy closed = openVacancy();
        closed.setStatus(Vacancy.CERRADA);
        when(vacancyRepository.findById(5)).thenReturn(Optional.of(closed));

        Map<String, String> errors = service.apply(5, 10, Map.of());
        assertFalse(errors.isEmpty());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateApplication() {
        when(vacancyRepository.findById(5)).thenReturn(Optional.of(openVacancy()));
        when(applicationRepository.existsByVacancyIdAndApplicantId(5, 10)).thenReturn(true);

        Map<String, String> errors = service.apply(5, 10, Map.of());
        assertFalse(errors.isEmpty());
        verify(applicationRepository, never()).save(any());
    }
}
