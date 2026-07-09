package edu.uptc.swii.sihope.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uptc.swii.sihope.domain.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByNombre(String nombre);
}
