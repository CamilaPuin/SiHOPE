package edu.uptc.swii.sihope.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.Application;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    boolean existsByVacancyIdAndApplicantId(Integer vacancyId, Integer applicantId);

    List<Application> findByVacancyIdOrderByAppliedAtAsc(Integer vacancyId);

    List<Application> findByApplicantId(Integer applicantId);

    boolean existsByApplicantIdAndState(Integer applicantId, String state);

    default boolean existsByConvocatoriaIdAndAspiranteId(Integer convocatoriaId, Integer aspiranteId) {
        return existsByVacancyIdAndApplicantId(convocatoriaId, aspiranteId);
    }

    default boolean existsByAspiranteIdAndEstado(Integer aspiranteId, String estado) {
        return existsByApplicantIdAndState(aspiranteId, estado);
    }
}
