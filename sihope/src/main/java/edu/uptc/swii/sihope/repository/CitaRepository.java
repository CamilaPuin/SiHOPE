package edu.uptc.swii.sihope.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.Cita;

public interface CitaRepository extends JpaRepository<Cita, Integer> {

    boolean existsBySlotKey(String slotKey);

    List<Cita> findByStudentIdOrderByDateDescStartTimeDesc(Integer studentId);

    List<Cita> findByMonitorIdOrderByDateDescStartTimeDesc(Integer monitorId);

    List<Cita> findByMonitorIdAndDateAndStatusIn(Integer monitorId, LocalDate date, List<String> statuses);

    List<Cita> findByStatusInAndReminderSentFalse(List<String> statuses);

    List<Cita> findByStatus(String status);

    List<Cita> findByStatusAndDateBetweenOrderByDateAscStartTimeAsc(
            String status, LocalDate from, LocalDate to);
}
