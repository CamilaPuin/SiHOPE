package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.uptc.swii.sihope.domain.Carrera;
import edu.uptc.swii.sihope.repository.CarreraRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

class CarreraServiceTest {

    @Mock
    private CarreraRepository carreraRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private CarreraService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void rejectsBlankName() {
        List<String> errors = service.createCareer("   ");
        assertFalse(errors.isEmpty());
        verify(carreraRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateName() {
        when(carreraRepository.existsByNameIgnoreCase("Ingeniería Civil")).thenReturn(true);
        List<String> errors = service.createCareer("Ingeniería Civil");
        assertFalse(errors.isEmpty());
        verify(carreraRepository, never()).save(any());
    }

    @Test
    void createsValidCareer() {
        when(carreraRepository.existsByNameIgnoreCase("Ingeniería Civil")).thenReturn(false);
        List<String> errors = service.createCareer("Ingeniería Civil");
        assertTrue(errors.isEmpty());
        verify(carreraRepository).save(any(Carrera.class));
    }

    @Test
    void deleteBlockedWhenUsersAssociated() {
        Carrera carrera = new Carrera("Ingeniería Civil");
        carrera.setId(1);
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(userRepository.countByCareer_Id(1)).thenReturn(2L);

        List<String> errors = service.deleteCareer(1);

        assertFalse(errors.isEmpty());
        verify(carreraRepository, never()).delete(any());
    }

    @Test
    void deletesUnusedCareer() {
        Carrera carrera = new Carrera("Ingeniería Civil");
        carrera.setId(1);
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(userRepository.countByCareer_Id(1)).thenReturn(0L);

        List<String> errors = service.deleteCareer(1);

        assertTrue(errors.isEmpty());
        verify(carreraRepository).delete(carrera);
    }

    @Test
    void deleteReportsMissingCareer() {
        when(carreraRepository.findById(99)).thenReturn(Optional.empty());
        assertFalse(service.deleteCareer(99).isEmpty());
    }
}
