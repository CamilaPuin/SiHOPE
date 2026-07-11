package edu.uptc.swii.sihope.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.History;

public interface HistoryRepository extends JpaRepository<History, Integer> {
}
