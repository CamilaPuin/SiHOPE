package edu.uptc.swii.sihope.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.Postulacion;

public interface PostulacionRepository extends JpaRepository<Postulacion, Integer> {

    boolean existsByConvocatoriaIdAndAspiranteId(Integer convocatoriaId, Integer aspiranteId);

    List<Postulacion> findByConvocatoriaIdOrderByFechaPostulacionAsc(Integer convocatoriaId);

    List<Postulacion> findByAspiranteId(Integer aspiranteId);

    /** ¿El aspirante tiene alguna postulación en el estado dado? (usado en la promoción a monitor). */
    boolean existsByAspiranteIdAndEstado(Integer aspiranteId, String estado);
}
