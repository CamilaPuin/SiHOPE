package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.BloqueHorario;
import edu.uptc.swii.sihope.repository.DisponibilidadRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

/**
 * Pruebas unitarias de {@link DisponibilidadService} (HU_006): validación de
 * franjas y reemplazo transaccional.
 */
class DisponibilidadServiceTest {

    @Mock
    private DisponibilidadRepository disponibilidadRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private DisponibilidadService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        User monitor = new User();
        monitor.setId(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(monitor));
    }

    @Test
    void guardaBloquesValidos() {
        List<BloqueHorario> bloques = List.of(
                new BloqueHorario(1, "10:00", "12:00"),
                new BloqueHorario(3, "14:00", "16:00"));

        List<String> errores = service.reemplazar(1, bloques);

        assertTrue(errores.isEmpty());
        verify(disponibilidadRepository).deleteByMonitorId(1);
        verify(disponibilidadRepository).saveAll(any());
    }

    @Test
    void rechazaFranjasSolapadasEnElMismoDia() {
        List<BloqueHorario> bloques = List.of(
                new BloqueHorario(1, "10:00", "12:00"),
                new BloqueHorario(1, "11:00", "13:00"));

        List<String> errores = service.reemplazar(1, bloques);

        assertFalse(errores.isEmpty());
        verify(disponibilidadRepository, never()).saveAll(any());
    }

    @Test
    void rechazaInicioPosteriorAlFin() {
        List<String> errores = service.reemplazar(1, List.of(new BloqueHorario(2, "16:00", "14:00")));
        assertFalse(errores.isEmpty());
        verify(disponibilidadRepository, never()).saveAll(any());
    }

    @Test
    void rechazaDiaFueraDeRango() {
        List<String> errores = service.reemplazar(1, List.of(new BloqueHorario(9, "10:00", "12:00")));
        assertFalse(errores.isEmpty());
    }

    @Test
    void reportaErrorSiElMonitorNoExiste() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        List<String> errores = service.reemplazar(99, List.of(new BloqueHorario(1, "10:00", "12:00")));
        assertFalse(errores.isEmpty());
    }
}
