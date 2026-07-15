package edu.uptc.swii.sihope.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.uptc.swii.sihope.domain.History;
import edu.uptc.swii.sihope.domain.Application;
import edu.uptc.swii.sihope.domain.Carrera;
import edu.uptc.swii.sihope.domain.Role;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.UserDTO;
import edu.uptc.swii.sihope.repository.HistoryRepository;
import edu.uptc.swii.sihope.repository.ApplicationRepository;
import edu.uptc.swii.sihope.repository.CarreraRepository;
import edu.uptc.swii.sihope.repository.RoleRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

@Service
public class UserService {

    public static final String UPTC_DOMAIN = "@uptc.edu.co";
    private static final List<String> VALID_ROLES =
            List.of("ADMINISTRADOR", "COORDINADOR", "MONITOR", "ESTUDIANTE");
    private static final int RESET_VALIDITY_MIN = 30;
    private static final int NAME_MAX = 50;
    // Solo letras (incluye tildes/ñ/ü) separadas por espacios simples
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\p{L}+(?:\\s\\p{L}+)*$");
    // Alfanumérico sin espacios ni caracteres especiales, máx. 15
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9]{1,15}$");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HistoryRepository historyRepository;
    private final ApplicationRepository applicationRepository;
    private final CarreraRepository carreraRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       HistoryRepository historyRepository,
                       ApplicationRepository applicationRepository,
                       CarreraRepository carreraRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.historyRepository = historyRepository;
        this.applicationRepository = applicationRepository;
        this.carreraRepository = carreraRepository;
        this.emailService = emailService;
    }

    public Optional<User> authenticate(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim())
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()));
    }

    public String routeByRole(String role) {
        if (role == null) {
            return "/login";
        }
        return switch (role) {
            case "ADMINISTRADOR" -> "/admin/usuarios";
            case "COORDINADOR"   -> "/coordinador";
            case "MONITOR"       -> "/monitor";
            case "ESTUDIANTE"    -> "/home";
            default              -> "/login";
        };
    }

    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public List<String> missingRequirements(String pwd) {
        List<String> missing = new ArrayList<>();
        if (pwd == null) pwd = "";
        if (pwd.length() < 8)             missing.add("Mínimo 8 caracteres");
        if (!pwd.matches(".*[A-Z].*"))    missing.add("Una letra mayúscula");
        if (!pwd.matches(".*[a-z].*"))    missing.add("Una letra minúscula");
        if (!pwd.matches(".*\\d.*"))      missing.add("Un número");
        if (!pwd.matches(".*[^A-Za-z0-9].*")) missing.add("Un carácter especial");
        return missing;
    }

    private boolean isPasswordValid(String pwd) {
        return missingRequirements(pwd).isEmpty();
    }

    private boolean isInstitutionalEmail(String correo) {
        return correo != null && correo.trim().toLowerCase().endsWith(UPTC_DOMAIN)
                && correo.trim().length() > UPTC_DOMAIN.length();
    }

    private boolean isValidName(String s) {
        String t = s == null ? "" : s.trim();
        return !t.isEmpty() && t.length() <= NAME_MAX && NAME_PATTERN.matcher(t).matches();
    }

    private boolean isValidCode(String s) {
        return s != null && CODE_PATTERN.matcher(s.trim()).matches();
    }

    // Normaliza un nombre completo para comparar: sin tildes/diacríticos, minúsculas,
    // espacios colapsados. Así "Juan Manuel Ojeda Sanchez" y "Juán  Manuel Ojeda Sánchez"
    // se consideran el mismo nombre, sin importar cómo se dividan en nombres/apellidos.
    private String normalizeName(String s) {
        if (s == null) {
            return "";
        }
        String noAccents = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noAccents.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private boolean fullNameExists(String firstName, String lastName) {
        String target = normalizeName((firstName == null ? "" : firstName) + " "
                + (lastName == null ? "" : lastName));
        if (target.isBlank()) {
            return false;
        }
        return userRepository.findAll().stream()
                .anyMatch(u -> normalizeName(
                        (u.getFirstName() == null ? "" : u.getFirstName()) + " "
                                + (u.getLastName() == null ? "" : u.getLastName()))
                        .equals(target));
    }

    public Map<String, String> registerStudent(UserDTO dto, Integer careerId) {
        Map<String, String> errors = new LinkedHashMap<>();

        boolean firstNameOk = false;
        boolean lastNameOk = false;
        if (isBlank(dto.getFirstName())) {
            errors.put("nombres", "Ingresa tus nombres.");
        } else if (!isValidName(dto.getFirstName())) {
            errors.put("nombres", "Los nombres solo pueden contener letras y espacios (máx. 50 caracteres).");
        } else {
            firstNameOk = true;
        }
        if (isBlank(dto.getLastName())) {
            errors.put("apellidos", "Ingresa tus apellidos.");
        } else if (!isValidName(dto.getLastName())) {
            errors.put("apellidos", "Los apellidos solo pueden contener letras y espacios (máx. 50 caracteres).");
        } else {
            lastNameOk = true;
        }
        if (firstNameOk && lastNameOk
                && fullNameExists(dto.getFirstName(), dto.getLastName())) {
            errors.put("apellidos", "Ya existe un usuario con ese nombre y apellidos.");
        }

        if (isBlank(dto.getStudentCode())) {
            errors.put("codigo", "El código estudiantil es obligatorio.");
        } else if (!isValidCode(dto.getStudentCode())) {
            errors.put("codigo", "El código debe ser alfanumérico, sin espacios ni caracteres especiales (máx. 15 caracteres).");
        } else if (userRepository.existsByStudentCode(dto.getStudentCode().trim())) {
            errors.put("codigo", "El código estudiantil ya está registrado.");
        }

        if (!isInstitutionalEmail(dto.getEmail())) {
            errors.put("correo", "Debes usar un correo institucional de la UPTC (" + UPTC_DOMAIN + ").");
        } else if (userRepository.existsByEmail(dto.getEmail().trim())) {
            errors.put("correo", "Este correo ya tiene una cuenta registrada.");
        }

        Carrera career = null;
        if (careerId == null) {
            errors.put("carrera", "Selecciona tu carrera.");
        } else {
            career = carreraRepository.findById(careerId).orElse(null);
            if (career == null) {
                errors.put("carrera", "La carrera seleccionada no existe.");
            }
        }

        if (!isPasswordValid(dto.getPassword())) {
            errors.put("password", "La contraseña no cumple los requisitos de seguridad.");
        }
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getPassword2())) {
            errors.put("password2", "Las contraseñas no coinciden.");
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        Role studentRole = getOrCreateRole("ESTUDIANTE");
        String token = UUID.randomUUID().toString();

        User u = new User();
        u.setFirstName(dto.getFirstName().trim());
        u.setLastName(dto.getLastName().trim());
        u.setStudentCode(dto.getStudentCode().trim());
        u.setEmail(dto.getEmail().trim());
        u.setPassword(encode(dto.getPassword()));
        u.setActive(true);
        u.setVerified(false);
        u.setVerificationToken(token);
        u.setRole(studentRole);
        u.setCareer(career);
        userRepository.save(u);

        emailService.sendVerification(u.getEmail(), token);
        return errors;
    }

    public boolean verifyAccount(String token) {
        Optional<User> opt = userRepository.findByVerificationToken(token);
        if (opt.isEmpty()) {
            return false;
        }
        User u = opt.get();
        u.setVerified(true);
        u.setVerificationToken(null);
        userRepository.save(u);
        return true;
    }

    public List<User> listUsers() {
        return userRepository.findAllByOrderByIdAsc();
    }

    public Map<String, String> createUser(String fullName, String email,
                                           String studentCode, String roleName, Integer careerId) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (isBlank(fullName)) {
            errors.put("nombre", "El nombre es obligatorio.");
        } else if (!isValidName(fullName)) {
            errors.put("nombre", "El nombre solo puede contener letras y espacios (máx. 50 caracteres).");
        } else if (fullNameExists(fullName, "")) {
            errors.put("nombre", "Ya existe un usuario con ese nombre y apellidos.");
        }
        if (isBlank(studentCode)) {
            errors.put("documento", "El documento/código es obligatorio.");
        } else if (!isValidCode(studentCode)) {
            errors.put("documento", "El documento/código debe ser alfanumérico, sin espacios ni caracteres especiales (máx. 15 caracteres).");
        } else if (userRepository.existsByStudentCode(studentCode.trim())) {
            errors.put("documento", "Ya existe un usuario con ese documento/código.");
        }

        if (!isInstitutionalEmail(email)) {
            errors.put("correo", "Ingresa un correo institucional válido (" + UPTC_DOMAIN + ").");
        } else if (userRepository.existsByEmail(email.trim())) {
            errors.put("correo", "Ya existe un usuario con ese correo.");
        }

        if (isBlank(roleName) || !VALID_ROLES.contains(roleName)) {
            errors.put("rol", "Selecciona un rol válido.");
        }

        Carrera career = null;
        if ("ESTUDIANTE".equals(roleName)) {
            if (careerId == null) {
                errors.put("carrera", "Selecciona la carrera del estudiante.");
            } else {
                career = carreraRepository.findById(careerId).orElse(null);
                if (career == null) {
                    errors.put("carrera", "La carrera seleccionada no existe.");
                }
            }
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        String[] parts = fullName.trim().split("\\s+", 2);
        String temporaryPassword = generateTemporaryPassword();

        User u = new User();
        u.setFirstName(parts[0]);
        u.setLastName(parts.length > 1 ? parts[1] : "");
        u.setStudentCode(studentCode.trim());
        u.setEmail(email.trim());
        u.setPassword(encode(temporaryPassword));
        u.setActive(true);
        u.setVerified(true);
        u.setRole(getOrCreateRole(roleName));
        u.setCareer(career);
        userRepository.save(u);

        emailService.sendCredentials(u.getEmail(), temporaryPassword);
        return errors;
    }

    public enum RoleChangeResult { OK, NOT_FOUND, INVALID_ROLE, LAST_ADMIN }

    public RoleChangeResult changeRole(Integer userId, String roleName, Integer actingUserId) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            return RoleChangeResult.NOT_FOUND;
        }
        if (isBlank(roleName) || !VALID_ROLES.contains(roleName)) {
            return RoleChangeResult.INVALID_ROLE;
        }
        User u = opt.get();
        boolean targetIsAdmin = u.getRole() != null && "ADMINISTRADOR".equals(u.getRole().getName());
        if (targetIsAdmin && !"ADMINISTRADOR".equals(roleName)
                && userId.equals(actingUserId)
                && userRepository.countByRole_NameAndActiveTrueAndIdNot("ADMINISTRADOR", userId) == 0) {
            return RoleChangeResult.LAST_ADMIN;
        }
        String previousRole = u.getRole() != null ? u.getRole().getName() : "—";
        u.setRole(getOrCreateRole(roleName));
        u.setTokenVersion(u.getTokenVersion() + 1);
        userRepository.save(u);
        logHistory(u, History.CAMBIO_ROL, "Rol cambiado de " + previousRole + " a " + roleName);
        return RoleChangeResult.OK;
    }

    public enum StatusChangeResult { ACTIVATED, DEACTIVATED, NOT_FOUND, LAST_ADMIN }

    public StatusChangeResult changeStatus(Integer userId) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            return StatusChangeResult.NOT_FOUND;
        }
        User u = opt.get();
        boolean isActiveAdmin = u.isActive() && u.getRole() != null
                && "ADMINISTRADOR".equals(u.getRole().getName());
        if (isActiveAdmin
                && userRepository.countByRole_NameAndActiveTrueAndIdNot("ADMINISTRADOR", userId) == 0) {
            return StatusChangeResult.LAST_ADMIN;
        }
        u.setActive(!u.isActive());
        u.setTokenVersion(u.getTokenVersion() + 1);
        userRepository.save(u);
        logHistory(u, History.ESTADO_CUENTA, u.isActive() ? "Cuenta activada" : "Cuenta desactivada");
        return u.isActive() ? StatusChangeResult.ACTIVATED : StatusChangeResult.DEACTIVATED;
    }

    public enum PromotionResult { OK, NOT_FOUND, NOT_APPROVED, ALREADY_MONITOR }

    public PromotionResult promoteToMonitor(Integer userId) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            return PromotionResult.NOT_FOUND;
        }
        User u = opt.get();
        if (u.getRole() != null && "MONITOR".equals(u.getRole().getName())) {
            return PromotionResult.ALREADY_MONITOR;
        }
        if (!applicationRepository.existsByApplicantIdAndState(userId, Application.APROBADA)) {
            return PromotionResult.NOT_APPROVED;
        }
        String previousRole = u.getRole() != null ? u.getRole().getName() : "—";
        u.setRole(getOrCreateRole("MONITOR"));
        u.setTokenVersion(u.getTokenVersion() + 1);
        userRepository.save(u);
        logHistory(u, History.CAMBIO_ROL,
                "Promovido de " + previousRole + " a MONITOR tras aprobación de postulación");
        return PromotionResult.OK;
    }

    public enum PasswordChangeResult { OK, WRONG_CURRENT, INVALID_NEW }

    public PasswordChangeResult changePassword(String email, String currentPassword, String newPassword) {
        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isEmpty()) {
            return PasswordChangeResult.WRONG_CURRENT;
        }
        User u = opt.get();

        if (!passwordEncoder.matches(currentPassword, u.getPassword())) {
            logHistory(u, History.SEGURIDAD, "Intento fallido de cambio de contraseña (contraseña actual incorrecta)");
            return PasswordChangeResult.WRONG_CURRENT;
        }
        if (!isPasswordValid(newPassword)) {
            return PasswordChangeResult.INVALID_NEW;
        }
        u.setPassword(encode(newPassword));
        userRepository.save(u);
        logHistory(u, History.SEGURIDAD, "Contraseña actualizada correctamente");
        return PasswordChangeResult.OK;
    }

    public void requestPasswordReset(String email) {
        if (!isInstitutionalEmail(email)) {
            return;
        }
        userRepository.findByEmail(email.trim()).ifPresent(u -> {
            String token = UUID.randomUUID().toString();
            u.setResetToken(token);
            u.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(RESET_VALIDITY_MIN));
            userRepository.save(u);
            emailService.sendPasswordReset(u.getEmail(), token);
        });
    }

    public enum PasswordResetResult { OK, INVALID_TOKEN, EXPIRED, INVALID_NEW }

    public PasswordResetResult resetPassword(String token, String newPassword) {
        Optional<User> opt = userRepository.findByResetToken(token);
        if (opt.isEmpty()) {
            return PasswordResetResult.INVALID_TOKEN;
        }
        User u = opt.get();
        if (u.getResetTokenExpiresAt() == null || u.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return PasswordResetResult.EXPIRED;
        }
        if (!isPasswordValid(newPassword)) {
            return PasswordResetResult.INVALID_NEW;
        }
        u.setPassword(encode(newPassword));
        u.setResetToken(null);
        u.setResetTokenExpiresAt(null);
        userRepository.save(u);
        logHistory(u, History.SEGURIDAD, "Contraseña restablecida por recuperación");
        return PasswordResetResult.OK;
    }

    private void logHistory(User u, String tipo, String descripcion) {
        historyRepository.save(new History(u, tipo, descripcion, LocalDateTime.now()));
    }

    private Role getOrCreateRole(String name) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = roleRepository.save(new Role(name));
        }
        return role;
    }

    private String generateTemporaryPassword() {
        return "Sh1#" + UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
