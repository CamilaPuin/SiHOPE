package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.uptc.swii.sihope.domain.Availability;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.TimeBlock;
import edu.uptc.swii.sihope.dto.response.MonitorDirectoryResponse;
import edu.uptc.swii.sihope.repository.AvailabilityRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AvailabilityService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        User monitor = new User();
        monitor.setId(1);
        when(userRepository.findById(1)).thenReturn(Optional.of(monitor));
    }

    @Test
    void savesValidBlocks() {
        List<TimeBlock> blocks = List.of(
                new TimeBlock(1, "10:00", "12:00"),
                new TimeBlock(3, "14:00", "16:00"));

        List<String> errors = service.replace(1, blocks);

        assertTrue(errors.isEmpty());
        verify(availabilityRepository).deleteByMonitorId(1);
        verify(availabilityRepository).saveAll(any());
    }

    @Test
    void rejectsOverlappingBlocksOnTheSameDay() {
        List<TimeBlock> blocks = List.of(
                new TimeBlock(1, "10:00", "12:00"),
                new TimeBlock(1, "11:00", "13:00"));

        List<String> errors = service.replace(1, blocks);

        assertFalse(errors.isEmpty());
        verify(availabilityRepository, never()).saveAll(any());
    }

    @Test
    void rejectsStartAfterEnd() {
        List<String> errors = service.replace(1, List.of(new TimeBlock(2, "16:00", "14:00")));
        assertFalse(errors.isEmpty());
        verify(availabilityRepository, never()).saveAll(any());
    }

    @Test
    void rejectsDayOutOfRange() {
        List<String> errors = service.replace(1, List.of(new TimeBlock(9, "10:00", "12:00")));
        assertFalse(errors.isEmpty());
    }

    @Test
    void rejectsMoreThanEightTotalHours() {
        // 5 h (Lunes) + 4 h (Martes) = 9 h en total, supera el tope de 8 h.
        List<TimeBlock> blocks = List.of(
                new TimeBlock(1, "08:00", "13:00"),
                new TimeBlock(2, "08:00", "12:00"));

        List<String> errors = service.replace(1, blocks);

        assertFalse(errors.isEmpty());
        verify(availabilityRepository, never()).saveAll(any());
    }

    @Test
    void acceptsExactlyEightTotalHours() {
        List<TimeBlock> blocks = List.of(
                new TimeBlock(1, "08:00", "12:00"),
                new TimeBlock(2, "08:00", "12:00"));

        List<String> errors = service.replace(1, blocks);

        assertTrue(errors.isEmpty());
        verify(availabilityRepository).saveAll(any());
    }

    @Test
    void reportsErrorIfMonitorDoesNotExist() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        List<String> errors = service.replace(99, List.of(new TimeBlock(1, "10:00", "12:00")));
        assertFalse(errors.isEmpty());
    }

    @Test
    void listsMonitorsWithTheirAvailabilityAndInitials() {
        User monitor = new User();
        monitor.setId(1);
        monitor.setFirstName("Ana");
        monitor.setLastName("Gómez");
        monitor.setEmail("ana@uptc.edu.co");
        when(userRepository.findByRole_NameOrderByFirstNameAscLastNameAsc("MONITOR"))
                .thenReturn(List.of(monitor));
        when(availabilityRepository.findByMonitorIdOrderByDayOfWeekAscStartTimeAsc(1))
                .thenReturn(List.of(new Availability(monitor, 1, LocalTime.of(10, 0), LocalTime.of(12, 0))));

        List<MonitorDirectoryResponse> monitors = service.listMonitors();

        assertEquals(1, monitors.size());
        MonitorDirectoryResponse dto = monitors.get(0);
        assertEquals("Ana Gómez", dto.nombre());
        assertEquals("AG", dto.iniciales());
        assertEquals(1, dto.disponibilidad().size());
        assertEquals("10:00", dto.disponibilidad().get(0).horaInicio());
    }
}
