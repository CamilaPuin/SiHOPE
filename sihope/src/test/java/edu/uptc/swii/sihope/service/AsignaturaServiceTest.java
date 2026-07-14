package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import edu.uptc.swii.sihope.domain.Asignatura;
import edu.uptc.swii.sihope.repository.AsignaturaRepository;
import edu.uptc.swii.sihope.repository.CitaRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

class AsignaturaServiceTest {

    @Mock
    private AsignaturaRepository asignaturaRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CitaRepository citaRepository;
    @InjectMocks
    private AsignaturaService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createsSubjectWhenNameIsNewAndNotBlank() {
        when(asignaturaRepository.existsByNameIgnoreCase("Cálculo Diferencial")).thenReturn(false);

        List<String> errors = service.createSubject("  Cálculo Diferencial  ");

        assertTrue(errors.isEmpty());
        verify(asignaturaRepository).save(any(Asignatura.class));
    }

    @Test
    void rejectsBlankSubjectName() {
        List<String> errors = service.createSubject("   ");

        assertFalse(errors.isEmpty());
        verify(asignaturaRepository, never()).save(any(Asignatura.class));
    }

    @Test
    void rejectsDuplicateSubjectName() {
        when(asignaturaRepository.existsByNameIgnoreCase("Cálculo")).thenReturn(true);

        List<String> errors = service.createSubject("Cálculo");

        assertFalse(errors.isEmpty());
        assertEquals("Ya existe una asignatura con ese nombre.", errors.get(0));
        verify(asignaturaRepository, never()).save(any(Asignatura.class));
    }

    @Test
    void deletesSubjectWhenNotInUse() {
        when(asignaturaRepository.findById(7)).thenReturn(Optional.of(new Asignatura("Física")));
        when(userRepository.countBySubjects_Id(7)).thenReturn(0L);
        when(citaRepository.countBySubject_Id(7)).thenReturn(0L);

        List<String> errors = service.deleteSubject(7);

        assertTrue(errors.isEmpty());
        verify(asignaturaRepository).delete(any(Asignatura.class));
    }

    @Test
    void blocksDeletionWhenAssignedToAMonitor() {
        when(asignaturaRepository.findById(7)).thenReturn(Optional.of(new Asignatura("Física")));
        when(userRepository.countBySubjects_Id(7)).thenReturn(2L);

        List<String> errors = service.deleteSubject(7);

        assertFalse(errors.isEmpty());
        verify(asignaturaRepository, never()).delete(any(Asignatura.class));
    }

    @Test
    void blocksDeletionWhenCitasExist() {
        when(asignaturaRepository.findById(7)).thenReturn(Optional.of(new Asignatura("Física")));
        when(userRepository.countBySubjects_Id(7)).thenReturn(0L);
        when(citaRepository.countBySubject_Id(7)).thenReturn(5L);

        List<String> errors = service.deleteSubject(7);

        assertFalse(errors.isEmpty());
        verify(asignaturaRepository, never()).delete(any(Asignatura.class));
    }

    @Test
    void reportsMissingSubjectOnDelete() {
        when(asignaturaRepository.findById(99)).thenReturn(Optional.empty());

        List<String> errors = service.deleteSubject(99);

        assertFalse(errors.isEmpty());
        assertEquals("La asignatura no existe.", errors.get(0));
        verify(asignaturaRepository, never()).delete(any(Asignatura.class));
    }
}
