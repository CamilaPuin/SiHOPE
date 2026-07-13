package edu.uptc.swii.sihope.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByStudentCode(String studentCode);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByResetToken(String resetToken);

    List<User> findAllByOrderByIdAsc();

    List<User> findByRole_NameOrderByFirstNameAscLastNameAsc(String roleName);

    List<User> findDistinctByRole_NameAndSubjects_IdOrderByFirstNameAscLastNameAsc(
            String roleName, Integer subjectId);

    default List<User> findByRole_NombreOrderByNombresAscApellidosAsc(String nombreRol) {
        return findByRole_NameOrderByFirstNameAscLastNameAsc(nombreRol);
    }
}
