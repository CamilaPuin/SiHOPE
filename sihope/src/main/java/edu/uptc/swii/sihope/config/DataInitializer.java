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
        createIfMissing("Admin", "SiHope", "ADM-0001", "admin@uptc.edu.co", "Admin2026*", "ADMINISTRADOR");
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
