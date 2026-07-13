package edu.uptc.swii.sihope.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uptc.swii.sihope.domain.Asignatura;
import edu.uptc.swii.sihope.domain.Availability;
import edu.uptc.swii.sihope.domain.Cita;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.TimeBlock;
import edu.uptc.swii.sihope.dto.response.CitaResponse;
import edu.uptc.swii.sihope.repository.AsignaturaRepository;
import edu.uptc.swii.sihope.repository.AvailabilityRepository;
import edu.uptc.swii.sihope.repository.CitaRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

@Service
public class CitaService {

    private static final String MONITOR_ROLE = "MONITOR";
    private static final int SLOT_MINUTES = 60;
    private static final long MIN_CANCEL_HOURS = 2;
    private static final List<String> ACTIVE = List.of(Cita.RESERVADA, Cita.CONFIRMADA);

    private final CitaRepository citaRepository;
    private final UserRepository userRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final AvailabilityRepository availabilityRepository;
    private final EmailService emailService;

    public CitaService(CitaRepository citaRepository, UserRepository userRepository,
                       AsignaturaRepository asignaturaRepository,
                       AvailabilityRepository availabilityRepository, EmailService emailService) {
        this.citaRepository = citaRepository;
        this.userRepository = userRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.availabilityRepository = availabilityRepository;
        this.emailService = emailService;
    }

    public record Result(CitaResponse cita, String error, List<TimeBlock> available, boolean conflict) {
        static Result ok(CitaResponse c) {
            return new Result(c, null, null, false);
        }

        static Result error(String message) {
            return new Result(null, message, null, false);
        }

        static Result conflict(String message, List<TimeBlock> available) {
            return new Result(null, message, available, true);
        }
    }

   
    @Transactional(readOnly = true)
    public List<TimeBlock> freeSlots(Integer monitorId, LocalDate date) {
        List<TimeBlock> free = new ArrayList<>();
        if (date == null || date.isBefore(LocalDate.now())) {
            return free;
        }
        int weekday = date.getDayOfWeek().getValue(); 
        List<Availability> blocks = availabilityRepository
                .findByMonitorIdOrderByDayOfWeekAscStartTimeAsc(monitorId).stream()
                .filter(a -> a.getDayOfWeek() == weekday)
                .toList();

        Set<LocalTime> booked = citaRepository
                .findByMonitorIdAndDateAndStatusIn(monitorId, date, ACTIVE).stream()
                .map(Cita::getStartTime)
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        for (Availability b : blocks) {
            LocalTime start = b.getStartTime();
            while (!start.plusMinutes(SLOT_MINUTES).isAfter(b.getEndTime())) {
                LocalTime end = start.plusMinutes(SLOT_MINUTES);
                boolean past = LocalDateTime.of(date, start).isBefore(now);
                if (!booked.contains(start) && !past) {
                    free.add(new TimeBlock(weekday, start.toString(), end.toString()));
                }
                start = end;
            }
        }
        return free;
    }

    @Transactional
    public Result create(Integer studentId, Integer monitorId, Integer asignaturaId,
                         String dateRaw, String startRaw) {
        LocalDate date = parseDate(dateRaw);
        LocalTime start = parseTime(startRaw);
        if (date == null || start == null) {
            return Result.error("Fecha u hora inválidas.");
        }
        LocalTime end = start.plusMinutes(SLOT_MINUTES);

        User student = userRepository.findById(studentId).orElse(null);
        if (student == null) {
            return Result.error("El estudiante no existe.");
        }
        if (monitorId == null || monitorId.equals(studentId)) {
            return Result.error("Selecciona un monitor válido (distinto de ti).");
        }
        User monitor = userRepository.findById(monitorId).orElse(null);
        if (monitor == null || monitor.getRole() == null
                || !MONITOR_ROLE.equals(monitor.getRole().getName())) {
            return Result.error("El monitor seleccionado no existe.");
        }

        Asignatura subject = (asignaturaId == null)
                ? null : asignaturaRepository.findById(asignaturaId).orElse(null);
        if (subject == null) {
            return Result.error("La asignatura seleccionada no existe.");
        }
        boolean monitorTeaches = monitor.getSubjects().stream()
                .anyMatch(a -> a.getId().equals(subject.getId()));
        if (!monitorTeaches) {
            return Result.error("El monitor no atiende esa asignatura.");
        }

        if (date.isBefore(LocalDate.now())
                || LocalDateTime.of(date, start).isBefore(LocalDateTime.now())) {
            return Result.error("No puedes agendar en una fecha u hora pasada.");
        }

        if (!fitsAvailability(monitorId, date.getDayOfWeek().getValue(), start, end)) {
            return Result.conflict("El horario seleccionado no está dentro de la disponibilidad del monitor.",
                    freeSlots(monitorId, date));
        }

        String slotKey = Cita.buildSlotKey(monitorId, date, start);
        if (citaRepository.existsBySlotKey(slotKey)) {
            return Result.conflict("Ese horario ya está reservado. Elige otro de los disponibles.",
                    freeSlots(monitorId, date));
        }

        Cita cita = new Cita();
        cita.setStudent(student);
        cita.setMonitor(monitor);
        cita.setSubject(subject);
        cita.setDate(date);
        cita.setStartTime(start);
        cita.setEndTime(end);
        cita.setStatus(Cita.RESERVADA);
        cita.setReminderSent(false);
        cita.setCreatedAt(LocalDateTime.now());
        cita.setSlotKey(slotKey);

        try {
            citaRepository.saveAndFlush(cita);
        } catch (DataIntegrityViolationException e) {
            return Result.conflict("Ese horario acaba de ser reservado. Elige otro de los disponibles.",
                    freeSlots(monitorId, date));
        }

        emailService.sendCitaReservada(monitor.getEmail(), fullName(student),
                subject.getName(), date.toString(), hhmm(start));
        return Result.ok(CitaResponse.from(cita, studentId));
    }

    @Transactional
    public Result confirm(Integer citaId, Integer monitorId) {
        Cita cita = citaRepository.findById(citaId).orElse(null);
        if (cita == null) {
            return Result.error("La cita no existe.");
        }
        if (cita.getMonitor() == null || !cita.getMonitor().getId().equals(monitorId)) {
            return Result.error("No puedes confirmar una cita que no es tuya.");
        }
        if (!Cita.RESERVADA.equals(cita.getStatus())) {
            return Result.error("Solo puedes confirmar citas en estado reservado.");
        }
        cita.setStatus(Cita.CONFIRMADA);
        citaRepository.save(cita);

        emailService.sendCitaConfirmada(cita.getStudent().getEmail(), fullName(cita.getMonitor()),
                cita.getSubject().getName(), cita.getDate().toString(), hhmm(cita.getStartTime()));
        return Result.ok(CitaResponse.from(cita, monitorId));
    }

    @Transactional
    public Result cancel(Integer citaId, Integer userId, String reason) {
        Cita cita = citaRepository.findById(citaId).orElse(null);
        if (cita == null) {
            return Result.error("La cita no existe.");
        }
        boolean isParticipant = (cita.getStudent() != null && cita.getStudent().getId().equals(userId))
                || (cita.getMonitor() != null && cita.getMonitor().getId().equals(userId));
        if (!isParticipant) {
            return Result.error("No puedes cancelar una cita en la que no participas.");
        }
        if (!ACTIVE.contains(cita.getStatus())) {
            return Result.error("La cita ya no se puede cancelar (estado: " + cita.getStatus() + ").");
        }
        if (cita.startsAt().minusHours(MIN_CANCEL_HOURS).isBefore(LocalDateTime.now())) {
            return Result.error("Solo puedes cancelar con al menos " + MIN_CANCEL_HOURS
                    + " horas de anticipación.");
        }

        cita.setStatus(Cita.CANCELADA);
        cita.setCancellationReason(reason);
        cita.setSlotKey(null); 
        citaRepository.save(cita);

        String date = cita.getDate().toString();
        String time = hhmm(cita.getStartTime());
        String subjectName = cita.getSubject().getName();
        emailService.sendCitaCancelada(cita.getStudent().getEmail(), subjectName, date, time, reason);
        emailService.sendCitaCancelada(cita.getMonitor().getEmail(), subjectName, date, time, reason);
        return Result.ok(CitaResponse.from(cita, userId));
    }

    @Transactional
    public Result markAttended(Integer citaId, Integer monitorId) {
        Cita cita = citaRepository.findById(citaId).orElse(null);
        if (cita == null) {
            return Result.error("La cita no existe.");
        }
        if (cita.getMonitor() == null || !cita.getMonitor().getId().equals(monitorId)) {
            return Result.error("No puedes marcar una cita que no es tuya.");
        }
        if (!Cita.CONFIRMADA.equals(cita.getStatus())) {
            return Result.error("Solo puedes marcar como atendida una cita confirmada.");
        }
        cita.setStatus(Cita.ATENDIDA);
        cita.setSlotKey(null);
        citaRepository.save(cita);
        return Result.ok(CitaResponse.from(cita, monitorId));
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> listForUser(Integer userId) {
        List<Cita> citas = new ArrayList<>();
        citas.addAll(citaRepository.findByStudentIdOrderByDateDescStartTimeDesc(userId));
        citas.addAll(citaRepository.findByMonitorIdOrderByDateDescStartTimeDesc(userId));
        return citas.stream()
                .sorted(Comparator.comparing(Cita::getDate).reversed()
                        .thenComparing(Comparator.comparing(Cita::getStartTime).reversed()))
                .map(c -> CitaResponse.from(c, userId))
                .toList();
    }

    @Transactional
    public int sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(24);
        int count = 0;
        for (Cita cita : citaRepository.findByStatusInAndReminderSentFalse(ACTIVE)) {
            LocalDateTime startsAt = cita.startsAt();
            if (startsAt.isAfter(now) && !startsAt.isAfter(threshold)) {
                String date = cita.getDate().toString();
                String time = hhmm(cita.getStartTime());
                String subjectName = cita.getSubject().getName();
                emailService.sendCitaRecordatorio(cita.getStudent().getEmail(), subjectName, date, time,
                        fullName(cita.getMonitor()));
                emailService.sendCitaRecordatorio(cita.getMonitor().getEmail(), subjectName, date, time,
                        fullName(cita.getStudent()));
                cita.setReminderSent(true);
                citaRepository.save(cita);
                count++;
            }
        }
        return count;
    }

    @Transactional
    public int autoMarkAttended() {
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (Cita cita : citaRepository.findByStatus(Cita.CONFIRMADA)) {
            if (cita.endsAt().isBefore(now)) {
                cita.setStatus(Cita.ATENDIDA);
                cita.setSlotKey(null);
                citaRepository.save(cita);
                count++;
            }
        }
        return count;
    }

    private boolean fitsAvailability(Integer monitorId, int weekday, LocalTime start, LocalTime end) {
        return availabilityRepository.findByMonitorIdOrderByDayOfWeekAscStartTimeAsc(monitorId).stream()
                .filter(a -> a.getDayOfWeek() == weekday)
                .anyMatch(a -> !start.isBefore(a.getStartTime()) && !end.isAfter(a.getEndTime()));
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(raw.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String fullName(User u) {
        if (u == null) {
            return "";
        }
        return ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                + (u.getLastName() == null ? "" : u.getLastName())).trim();
    }

    private static String hhmm(LocalTime t) {
        return t == null ? "" : t.toString().length() >= 5 ? t.toString().substring(0, 5) : t.toString();
    }
}
