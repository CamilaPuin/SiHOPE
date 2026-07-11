package edu.uptc.swii.sihope.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.Availability;

public interface AvailabilityRepository extends JpaRepository<Availability, Integer> {

    List<Availability> findByMonitorIdOrderByDayOfWeekAscStartTimeAsc(Integer monitorId);

    void deleteByMonitorId(Integer monitorId);

    default List<Availability> findByMonitorIdOrderByDiaSemanaAscHoraInicioAsc(Integer monitorId) {
        return findByMonitorIdOrderByDayOfWeekAscStartTimeAsc(monitorId);
    }
}
