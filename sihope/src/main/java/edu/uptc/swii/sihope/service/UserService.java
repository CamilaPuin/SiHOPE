package edu.uptc.swii.sihope.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import edu.uptc.swii.sihope.domain.Historial;
import edu.uptc.swii.sihope.domain.Postulacion;
import edu.uptc.swii.sihope.domain.Role;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.UserDTO;
import edu.uptc.swii.sihope.repository.HistorialRepository;
import edu.uptc.swii.sihope.repository.PostulacionRepository;
import edu.uptc.swii.sihope.repository.RoleRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

@Service
public class UserService {

    public static final String DOMINIO_UPTC = "@uptc.edu.co";
    private static final List<String> ROLES_VALIDOS =
            List.of("ADMINISTRADOR", "COORDINADOR", "MONITOR", "ESTUDIANTE");
    private static final int VIGENCIA_RESET_MIN = 30;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HistorialRepository historialRepository;
    private final PostulacionRepository postulacionRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       HistorialRepository historialRepository,
                       PostulacionRepository postulacionRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.historialRepository = historialRepository;
        this.postulacionRepository = postulacionRepository;
        this.emailService = emailService;
    }

    public Optional<User> autenticar(String correo, String rawPassword) {
        if (correo == null || rawPassword == null) {
            return Optional.empty();
        }
        return userRepository.findByCorreo(correo.trim())
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()));
    }

    public String rutaPorRol(String rol) {
        if (rol == null) {
            return "/login";
        }
        return switch (rol) {
            case "ADMINISTRADOR" -> "/admin/usuarios";
            case "COORDINADOR"   -> "/coordinador";
            case "MONITOR"       -> "/monitor";
            case "ESTUDIANTE"    -> "/home";
            default              -> "/login";
        };
    }

    public String codificar(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public List<String> requisitosFaltantes(String pwd) {
        List<String> faltan = new ArrayList<>();
        if (pwd == null) pwd = "";
        if (pwd.length() < 8)             faltan.add("Mínimo 8 caracteres");
        if (!pwd.matches(".*[A-Z].*"))    faltan.add("Una letra mayúscula");
        if (!pwd.matches(".*[a-z].*"))    faltan.add("Una letra minúscula");
        if (!pwd.matches(".*\\d.*"))      faltan.add("Un número");
        if (!pwd.matches(".*[^A-Za-z0-9].*")) faltan.add("Un carácter especial");
        return faltan;
    }

    private boolean contrasenaValida(String pwd) {
        return requisitosFaltantes(pwd).isEmpty();
    }

    private boolean correoInstitucional(String correo) {
        return correo != null && correo.trim().toLowerCase().endsWith(DOMINIO_UPTC)
                && correo.trim().length() > DOMINIO_UPTC.length();
    }

    public Map<String, String> registrarEstudiante(UserDTO dto) {
        Map<String, String> errores = new LinkedHashMap<>();

        if (esVacio(dto.getNombres()))   errores.put("nombres", "Ingresa tus nombres.");
        if (esVacio(dto.getApellidos())) errores.put("apellidos", "Ingresa tus apellidos.");

        if (esVacio(dto.getCodigo())) {
            errores.put("codigo", "El código estudiantil es obligatorio.");
        } else if (userRepository.existsByCodigo(dto.getCodigo().trim())) {
            errores.put("codigo", "El código estudiantil ya está registrado.");
        }

        if (!correoInstitucional(dto.getCorreo())) {
            errores.put("correo", "Debes usar un correo institucional de la UPTC (" + DOMINIO_UPTC + ").");
        } else if (userRepository.existsByCorreo(dto.getCorreo().trim())) {
            errores.put("correo", "Este correo ya tiene una cuenta registrada.");
        }

        if (!contrasenaValida(dto.getPassword())) {
            errores.put("password", "La contraseña no cumple los requisitos de seguridad.");
        }
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getPassword2())) {
            errores.put("password2", "Las contraseñas no coinciden.");
        }

        if (!errores.isEmpty()) {
            return errores;
        }

        Role rolEstudiante = obtenerRol("ESTUDIANTE");
        String token = UUID.randomUUID().toString();

        User u = new User();
        u.setNombres(dto.getNombres().trim());
        u.setApellidos(dto.getApellidos().trim());
        u.setCodigo(dto.getCodigo().trim());
        u.setCorreo(dto.getCorreo().trim());
        u.setPassword(codificar(dto.getPassword()));
        u.setActivo(true);
        u.setVerificado(false);
        u.setTokenVerificacion(token);
        u.setRole(rolEstudiante);
        userRepository.save(u);

        emailService.enviarVerificacion(u.getCorreo(), token);
        return errores;
    }

    public boolean verificarCuenta(String token) {
        Optional<User> opt = userRepository.findByTokenVerificacion(token);
        if (opt.isEmpty()) {
            return false;
        }
        User u = opt.get();
        u.setVerificado(true);
        u.setTokenVerificacion(null);
        userRepository.save(u);
        return true;
    }

    public List<User> listarUsuarios() {
        return userRepository.findAllByOrderByIdAsc();
    }

    public Map<String, String> crearUsuario(String nombreCompleto, String correo,
                                             String codigo, String rolNombre) {
        Map<String, String> errores = new LinkedHashMap<>();

        if (esVacio(nombreCompleto)) errores.put("nombre", "El nombre es obligatorio.");
        if (esVacio(codigo))         errores.put("documento", "El documento/código es obligatorio.");

        if (!correoInstitucional(correo)) {
            errores.put("correo", "Ingresa un correo institucional válido (" + DOMINIO_UPTC + ").");
        } else if (userRepository.existsByCorreo(correo.trim())) {
            errores.put("correo", "Ya existe un usuario con ese correo.");
        }

        if (esVacio(rolNombre) || !ROLES_VALIDOS.contains(rolNombre)) {
            errores.put("rol", "Selecciona un rol válido.");
        }
        if (!esVacio(codigo) && userRepository.existsByCodigo(codigo.trim())) {
            errores.put("documento", "Ya existe un usuario con ese documento/código.");
        }

        if (!errores.isEmpty()) {
            return errores;
        }

        String[] partes = nombreCompleto.trim().split("\\s+", 2);
        String passwordTemporal = generarPasswordTemporal();

        User u = new User();
        u.setNombres(partes[0]);
        u.setApellidos(partes.length > 1 ? partes[1] : "");
        u.setCodigo(codigo.trim());
        u.setCorreo(correo.trim());
        u.setPassword(codificar(passwordTemporal));
        u.setActivo(true);
        u.setVerificado(true);
        u.setRole(obtenerRol(rolNombre));
        userRepository.save(u);

        emailService.enviarCredenciales(u.getCorreo(), passwordTemporal);
        return errores;
    }

    public boolean cambiarRol(Integer usuarioId, String rolNombre) {
        Optional<User> opt = userRepository.findById(usuarioId);
        if (opt.isEmpty() || esVacio(rolNombre) || !ROLES_VALIDOS.contains(rolNombre)) {
            return false;
        }
        User u = opt.get();
        String rolAnterior = u.getRole() != null ? u.getRole().getNombre() : "—";
        u.setRole(obtenerRol(rolNombre));
        u.setTokenVersion(u.getTokenVersion() + 1); // invalida los JWT vigentes tras el cambio de rol
        userRepository.save(u);
        registrar(u, Historial.CAMBIO_ROL, "Rol cambiado de " + rolAnterior + " a " + rolNombre);
        return true;
    }

    public Boolean cambiarEstado(Integer usuarioId) {
        Optional<User> opt = userRepository.findById(usuarioId);
        if (opt.isEmpty()) {
            return null;
        }
        User u = opt.get();
        u.setActivo(!u.isActivo());
        u.setTokenVersion(u.getTokenVersion() + 1); // invalida los JWT vigentes tras el cambio de estado
        userRepository.save(u);
        registrar(u, Historial.ESTADO_CUENTA, u.isActivo() ? "Cuenta activada" : "Cuenta desactivada");
        return u.isActivo();
    }

    /** Resultado de la promoción de un aspirante a monitor (HU_009). */
    public enum ResultadoPromocion { OK, NO_EXISTE, NO_APROBADO, YA_ES_MONITOR }

    /**
     * Promueve a MONITOR a un aspirante aprobado (HU_009).
     *
     * <p>Solo procede si el usuario tiene al menos una postulación en estado
     * APROBADA (mitigación 4.2). Al cambiar el rol incrementa {@code tokenVersion},
     * invalidando de inmediato cualquier JWT vigente del usuario para forzar un
     * nuevo inicio de sesión con los permisos actualizados (mitigación de mayor riesgo).
     */
    public ResultadoPromocion promoverAMonitor(Integer usuarioId) {
        Optional<User> opt = userRepository.findById(usuarioId);
        if (opt.isEmpty()) {
            return ResultadoPromocion.NO_EXISTE;
        }
        User u = opt.get();
        if (u.getRole() != null && "MONITOR".equals(u.getRole().getNombre())) {
            return ResultadoPromocion.YA_ES_MONITOR;
        }
        if (!postulacionRepository.existsByAspiranteIdAndEstado(usuarioId, Postulacion.APROBADA)) {
            return ResultadoPromocion.NO_APROBADO;
        }
        String rolAnterior = u.getRole() != null ? u.getRole().getNombre() : "—";
        u.setRole(obtenerRol("MONITOR"));
        u.setTokenVersion(u.getTokenVersion() + 1);
        userRepository.save(u);
        registrar(u, Historial.CAMBIO_ROL,
                "Promovido de " + rolAnterior + " a MONITOR tras aprobación de postulación");
        return ResultadoPromocion.OK;
    }

    public enum ResultadoCambio { OK, ACTUAL_INCORRECTA, NUEVA_INVALIDA }

    public ResultadoCambio cambiarPassword(String correo, String actual, String nueva) {
        Optional<User> opt = userRepository.findByCorreo(correo);
        if (opt.isEmpty()) {
            return ResultadoCambio.ACTUAL_INCORRECTA;
        }
        User u = opt.get();

        if (!passwordEncoder.matches(actual, u.getPassword())) {
            registrar(u, Historial.SEGURIDAD, "Intento fallido de cambio de contraseña (contraseña actual incorrecta)");
            return ResultadoCambio.ACTUAL_INCORRECTA;
        }
        if (!contrasenaValida(nueva)) {
            return ResultadoCambio.NUEVA_INVALIDA;
        }
        u.setPassword(codificar(nueva));
        userRepository.save(u);
        registrar(u, Historial.SEGURIDAD, "Contraseña actualizada correctamente");
        return ResultadoCambio.OK;
    }

    public void solicitarRecuperacion(String correo) {
        if (!correoInstitucional(correo)) {
            return;
        }
        userRepository.findByCorreo(correo.trim()).ifPresent(u -> {
            String token = UUID.randomUUID().toString();
            u.setTokenReset(token);
            u.setTokenResetExpira(LocalDateTime.now().plusMinutes(VIGENCIA_RESET_MIN));
            userRepository.save(u);
            emailService.enviarRecuperacion(u.getCorreo(), token);
        });
    }

    public enum ResultadoReset { OK, TOKEN_INVALIDO, EXPIRADO, NUEVA_INVALIDA }

    public ResultadoReset restablecerPassword(String token, String nueva) {
        Optional<User> opt = userRepository.findByTokenReset(token);
        if (opt.isEmpty()) {
            return ResultadoReset.TOKEN_INVALIDO;
        }
        User u = opt.get();
        if (u.getTokenResetExpira() == null || u.getTokenResetExpira().isBefore(LocalDateTime.now())) {
            return ResultadoReset.EXPIRADO;
        }
        if (!contrasenaValida(nueva)) {
            return ResultadoReset.NUEVA_INVALIDA;
        }
        u.setPassword(codificar(nueva));
        u.setTokenReset(null);
        u.setTokenResetExpira(null);
        userRepository.save(u);
        registrar(u, Historial.SEGURIDAD, "Contraseña restablecida por recuperación");
        return ResultadoReset.OK;
    }

    private void registrar(User u, String tipo, String descripcion) {
        historialRepository.save(new Historial(u, tipo, descripcion, LocalDateTime.now()));
    }

    private Role obtenerRol(String nombre) {
        Role rol = roleRepository.findByNombre(nombre);
        if (rol == null) {
            rol = roleRepository.save(new Role(nombre));
        }
        return rol;
    }

    private String generarPasswordTemporal() {
        return "Sh1#" + UUID.randomUUID().toString().substring(0, 8);
    }

    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}
