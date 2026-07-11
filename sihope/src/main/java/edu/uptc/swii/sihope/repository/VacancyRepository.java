package edu.uptc.swii.sihope.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.Vacancy;

public interface VacancyRepository extends JpaRepository<Vacancy, Integer> {

    List<Vacancy> findAllByOrderByCreatedAtDesc();

    List<Vacancy> findByStatusAndDeadlineGreaterThanEqualOrderByDeadlineAsc(
            String status, LocalDate deadline);
}
