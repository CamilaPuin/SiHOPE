package edu.uptc.swii.sihope.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import edu.uptc.swii.sihope.domain.Role;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.repository.RoleRepository;
import edu.uptc.swii.sihope.repository.UserRepository;
import edu.uptc.swii.sihope.repository.VacancyRepository;
import edu.uptc.swii.sihope.service.AsignaturaService;
import edu.uptc.swii.sihope.service.UserService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final VacancyRepository vacancyRepository;
    private final AsignaturaService asignaturaService;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           UserService userService, VacancyRepository vacancyRepository,
                           AsignaturaService asignaturaService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.vacancyRepository = vacancyRepository;
        this.asignaturaService = asignaturaService;
    }

    @Override
    public void run(String... args) {
        createIfMissing("Admin", "SiHope", "ADM-0001", "admin@uptc.edu.co", "Admin2026*", "ADMINISTRADOR");
        createIfMissing("Coordinador", "SiHope", "COORD-0001", "coordinador@uptc.edu.co", "Coord2026*", "COORDINADOR");
        createIfMissing("Monitor", "SiHope", "MON-0001", "monitor@uptc.edu.co", "Monitor2026*", "MONITOR");
        createIfMissing("Estudiante", "SiHope", "202312345", "estudiante@uptc.edu.co", "Estudiante2026*", "ESTUDIANTE");
        ingestVacancySubjects();
    }

    private void ingestVacancySubjects() {
        vacancyRepository.findAll().forEach(v -> {
            if (v.getSubject() != null && !v.getSubject().isBlank()) {
                asignaturaService.findOrCreate(v.getSubject());
            }
        });
    }

    private void createIfMissing(String firstName, String lastName, String studentCode,
                                 String email, String rawPassword, String roleName) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            role = roleRepository.save(new Role(roleName));
        }
        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setStudentCode(studentCode);
        u.setEmail(email);
        u.setPassword(userService.encode(rawPassword));
        u.setActive(true);
        u.setVerified(true);
        u.setRole(role);
        userRepository.save(u);
    }
}
