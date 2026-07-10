package edu.uptc.swii.sihope.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.Disponibilidad;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Integer> {

    List<Disponibilidad> findByMonitorIdOrderByDiaSemanaAscHoraInicioAsc(Integer monitorId);

    void deleteByMonitorId(Integer monitorId);
}
