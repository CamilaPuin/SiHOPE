package edu.uptc.swii.sihope.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByCodigo(String codigo);

    Optional<User> findByTokenVerificacion(String tokenVerificacion);

    Optional<User> findByTokenReset(String tokenReset);

    List<User> findAllByOrderByIdAsc();

    /** Monitores del directorio (usuarios con rol MONITOR), ordenados por nombre. */
    List<User> findByRole_NombreOrderByNombresAscApellidosAsc(String nombreRol);
}
