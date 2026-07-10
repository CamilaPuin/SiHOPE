package edu.uptc.swii.sihope.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.Convocatoria;

public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Integer> {

    List<Convocatoria> findAllByOrderByFechaCreacionDesc();

    /** Convocatorias visibles para aspirantes: abiertas y no vencidas. */
    List<Convocatoria> findByEstadoAndFechaLimiteGreaterThanEqualOrderByFechaLimiteAsc(
            String estado, LocalDate fecha);
}
