package edu.uptc.swii.sihope.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import edu.uptc.swii.sihope.domain.Role;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.repository.RoleRepository;
import edu.uptc.swii.sihope.repository.UserRepository;
import edu.uptc.swii.sihope.service.UserService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           UserService userService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("Admin", "SiHope", "ADM-0001", "admin@uptc.edu.co", "Admin2026*", "ADMINISTRADOR");
        crearSiNoExiste("Coordinador", "SiHope", "COORD-0001", "coordinador@uptc.edu.co", "Coord2026*", "COORDINADOR");
        crearSiNoExiste("Monitor", "SiHope", "MON-0001", "monitor@uptc.edu.co", "Monitor2026*", "MONITOR");
        crearSiNoExiste("Estudiante", "SiHope", "202312345", "estudiante@uptc.edu.co", "Estudiante2026*", "ESTUDIANTE");
    }

    private void crearSiNoExiste(String nombres, String apellidos, String codigo,
                                 String correo, String rawPassword, String nombreRol) {
        if (userRepository.existsByCorreo(correo)) {
            return;
        }
        Role rol = roleRepository.findByNombre(nombreRol);
        if (rol == null) {
            rol = roleRepository.save(new Role(nombreRol));
        }
        User u = new User();
        u.setNombres(nombres);
        u.setApellidos(apellidos);
        u.setCodigo(codigo);
        u.setCorreo(correo);
        u.setPassword(userService.codificar(rawPassword));
        u.setActivo(true);
        u.setVerificado(true);
        u.setRole(rol);
        userRepository.save(u);
    }
}
