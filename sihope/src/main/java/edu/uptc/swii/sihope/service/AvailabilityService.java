package edu.uptc.swii.sihope.service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.uptc.swii.sihope.domain.Availability;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.TimeBlock;
import edu.uptc.swii.sihope.dto.response.MonitorDirectoryResponse;
import edu.uptc.swii.sihope.repository.AvailabilityRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public AvailabilityService(AvailabilityRepository availabilityRepository,
                               UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    private static final String MONITOR_ROLE = "MONITOR";

    public List<MonitorDirectoryResponse> listMonitors() {
        return userRepository.findByRole_NameOrderByFirstNameAscLastNameAsc(MONITOR_ROLE)
                .stream()
                .map(m -> new MonitorDirectoryResponse(
                        m.getId(),
                        fullName(m),
                        initials(m),
                        m.getEmail(),
                        getBlocks(m.getId())))
                .toList();
    }

    private static String fullName(User u) {
        return ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                + (u.getLastName() == null ? "" : u.getLastName())).trim();
    }

    private static String initials(User u) {
        char firstNameInitial = firstLetter(u.getFirstName());
        char lastNameInitial = firstLetter(u.getLastName());
        String initials = ("" + firstNameInitial + lastNameInitial).trim();
        return initials.isBlank() ? "?" : initials.toUpperCase();
    }

    private static char firstLetter(String text) {
        return (text == null || text.isBlank()) ? ' ' : text.trim().charAt(0);
    }

    public List<TimeBlock> getBlocks(Integer monitorId) {
        return availabilityRepository
                .findByMonitorIdOrderByDayOfWeekAscStartTimeAsc(monitorId)
                .stream()
                .map(d -> new TimeBlock(d.getDayOfWeek(),
                        d.getStartTime().toString(), d.getEndTime().toString()))
                .toList();
    }

    @Transactional
    public List<String> replace(Integer monitorId, List<TimeBlock> blocks) {
        List<String> errors = new ArrayList<>();

        User monitor = userRepository.findById(monitorId).orElse(null);
        if (monitor == null) {
            errors.add("El monitor no existe.");
            return errors;
        }

        List<TimeBlock> list = (blocks == null) ? List.of() : blocks;
        List<Availability> toSave = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            TimeBlock b = list.get(i);
            String label = "Bloque " + (i + 1) + ": ";

            if (b.diaSemana() < 1 || b.diaSemana() > 6) {
                errors.add(label + "el día de la semana debe estar entre 1 (Lunes) y 6 (Sabado).");
                continue;
            }
            LocalTime start = parseTime(b.horaInicio());
            LocalTime end = parseTime(b.horaFin());
            if (start == null || end == null) {
                errors.add(label + "las horas deben tener el formato HH:mm.");
                continue;
            }
            if (!start.isBefore(end)) {
                errors.add(label + "la hora de inicio debe ser anterior a la de fin.");
                continue;
            }
            toSave.add(new Availability(monitor, b.diaSemana(), start, end));
        }

        errors.addAll(detectOverlaps(toSave));

        if (!errors.isEmpty()) {
            return errors;
        }

        availabilityRepository.deleteByMonitorId(monitorId);
        availabilityRepository.saveAll(toSave);
        return errors;
    }

    private List<String> detectOverlaps(List<Availability> blocks) {
        List<String> errors = new ArrayList<>();
        for (int day = 1; day <= 7; day++) {
            final int d = day;
            List<Availability> ofDay = blocks.stream()
                    .filter(b -> b.getDayOfWeek() == d)
                    .sorted(Comparator.comparing(Availability::getStartTime))
                    .toList();
            for (int i = 1; i < ofDay.size(); i++) {
                if (ofDay.get(i).getStartTime().isBefore(ofDay.get(i - 1).getEndTime())) {
                    errors.add("Hay franjas que se solapan en el mismo día. Revisa los horarios.");
                    break;
                }
            }
        }
        return errors;
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(time.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
