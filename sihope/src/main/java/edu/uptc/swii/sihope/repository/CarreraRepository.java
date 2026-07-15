package edu.uptc.swii.sihope.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.Carrera;

public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

    List<Carrera> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
