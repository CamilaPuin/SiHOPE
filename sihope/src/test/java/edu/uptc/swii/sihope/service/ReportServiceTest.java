package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.uptc.swii.sihope.domain.Asignatura;
import edu.uptc.swii.sihope.domain.Cita;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.response.CitasReportResponse;
import edu.uptc.swii.sihope.repository.CitaRepository;

class ReportServiceTest {

    private CitaRepository citaRepository;
    private ReportService reportService;

    private static final LocalDate FROM = LocalDate.of(2026, 3, 1);
    private static final LocalDate TO = LocalDate.of(2026, 3, 31);

    @BeforeEach
    void setUp() {
        citaRepository = Mockito.mock(CitaRepository.class);
        reportService = new ReportService(citaRepository);
    }

    private User user(int id, String first, String last) {
        User u = new User();
        u.setId(id);
        u.setFirstName(first);
        u.setLastName(last);
        return u;
    }

    private Asignatura subject(String name) {
        Asignatura a = new Asignatura();
        a.setName(name);
        return a;
    }

    private Cita cita(User monitor, User student, Asignatura subject, LocalDate date) {
        Cita c = new Cita();
        c.setStatus(Cita.ATENDIDA);
        c.setMonitor(monitor);
        c.setStudent(student);
        c.setSubject(subject);
        c.setDate(date);
        c.setStartTime(LocalTime.of(8, 0));
        c.setEndTime(LocalTime.of(9, 0));
        return c;
    }

    @Test
    void queriesRepositoryWithTheGivenPeriod() {
        when(citaRepository.findByStatusAndDateBetweenOrderByDateAscStartTimeAsc(
                eq(Cita.ATENDIDA), eq(FROM), eq(TO)))
                .thenReturn(List.of());

        CitasReportResponse report = reportService.citasAtendidas(FROM, TO);

        // El periodo solicitado se traslada tal cual al repositorio y a la respuesta.
        verify(citaRepository).findByStatusAndDateBetweenOrderByDateAscStartTimeAsc(
                Cita.ATENDIDA, FROM, TO);
        assertEquals("2026-03-01", report.from());
        assertEquals("2026-03-31", report.to());
    }

    @Test
    void aggregatesByMonitorAndSubjectWithoutMonitorFilter() {
        User monitorA = user(1, "Ana", "Pérez");
        User monitorB = user(2, "Luis", "Gómez");
        User student = user(9, "Est", "Udiante");
        Asignatura calculo = subject("Cálculo");
        Asignatura fisica = subject("Física");

        when(citaRepository.findByStatusAndDateBetweenOrderByDateAscStartTimeAsc(
                eq(Cita.ATENDIDA), eq(FROM), eq(TO)))
                .thenReturn(List.of(
                        cita(monitorA, student, calculo, LocalDate.of(2026, 3, 5)),
                        cita(monitorA, student, fisica, LocalDate.of(2026, 3, 6)),
                        cita(monitorB, student, calculo, LocalDate.of(2026, 3, 7))));

        CitasReportResponse report = reportService.citasAtendidas(FROM, TO);

        assertEquals(3, report.total());
        assertEquals(2, report.byMonitor().size());
        assertEquals(2, report.bySubject().size());
        // Sin filtro por monitor no se incluye el detalle de citas.
        assertNull(report.details());
        assertNull(report.message());
    }

    @Test
    void includesDetailRowsWhenFilteredByMonitor() {
        User monitor = user(1, "Ana", "Pérez");
        User student = user(9, "Est", "Udiante");
        Asignatura calculo = subject("Cálculo");

        when(citaRepository.findByStatusAndMonitorIdAndDateBetweenOrderByDateAscStartTimeAsc(
                eq(Cita.ATENDIDA), eq(1), eq(FROM), eq(TO)))
                .thenReturn(List.of(
                        cita(monitor, student, calculo, LocalDate.of(2026, 3, 5))));

        CitasReportResponse report = reportService.citasAtendidas(FROM, TO, 1);

        // Con filtro por monitor se usa la consulta específica y se incluye el detalle.
        verify(citaRepository).findByStatusAndMonitorIdAndDateBetweenOrderByDateAscStartTimeAsc(
                Cita.ATENDIDA, 1, FROM, TO);
        assertEquals(1, report.total());
        assertNotNull(report.details());
        assertEquals(1, report.details().size());
        assertEquals("Est Udiante", report.details().get(0).student());
        assertEquals("Cálculo", report.details().get(0).subject());
    }

    @Test
    void returnsMessageWhenNoAppointmentsInPeriod() {
        when(citaRepository.findByStatusAndDateBetweenOrderByDateAscStartTimeAsc(
                eq(Cita.ATENDIDA), eq(FROM), eq(TO)))
                .thenReturn(List.of());

        CitasReportResponse report = reportService.citasAtendidas(FROM, TO);

        assertEquals(0, report.total());
        assertNotNull(report.message());
        assertTrue(report.message().toLowerCase().contains("no hay"));
    }
}
