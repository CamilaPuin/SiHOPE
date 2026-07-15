package edu.uptc.swii.sihope.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.Asignatura;

public interface AsignaturaRepository extends JpaRepository<Asignatura, Integer> {

    List<Asignatura> findAllByOrderByNameAsc();

    Optional<Asignatura> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
