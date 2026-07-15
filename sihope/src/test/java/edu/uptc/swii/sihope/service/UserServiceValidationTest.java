package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.uptc.swii.sihope.domain.Carrera;
import edu.uptc.swii.sihope.domain.Role;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.UserDTO;
import edu.uptc.swii.sihope.repository.HistoryRepository;
import edu.uptc.swii.sihope.repository.ApplicationRepository;
import edu.uptc.swii.sihope.repository.CarreraRepository;
import edu.uptc.swii.sihope.repository.RoleRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

class UserServiceValidationTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private CarreraRepository carreraRepository;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private UserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void rejectsNameWithDigits() {
        Map<String, String> errors = service.createUser(
                "Juan P3rez", "juan@uptc.edu.co", "202312345", "MONITOR", null);
        assertTrue(errors.containsKey("nombre"));
    }

    @Test
    void rejectsNameWithSpecialCharacters() {
        Map<String, String> errors = service.createUser(
                "Jose-Luis Diaz", "jose@uptc.edu.co", "202312345", "MONITOR", null);
        assertTrue(errors.containsKey("nombre"));
    }

    @Test
    void rejectsNameLongerThanFiftyCharacters() {
        String longName = "A".repeat(51);
        Map<String, String> errors = service.createUser(
                longName, "largo@uptc.edu.co", "202312345", "MONITOR", null);
        assertTrue(errors.containsKey("nombre"));
    }

    @Test
    void acceptsNameWithAccentsAndEnie() {
        when(roleRepository.findByName("MONITOR")).thenReturn(new Role("MONITOR"));
        Map<String, String> errors = service.createUser(
                "María Ñuñez Güell", "maria@uptc.edu.co", "202312345", "MONITOR", null);
        assertFalse(errors.containsKey("nombre"));
        assertTrue(errors.isEmpty());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void rejectsCodeWithHyphen() {
        Map<String, String> errors = service.createUser(
                "Juan Perez", "juan@uptc.edu.co", "ADM-0001", "MONITOR", null);
        assertTrue(errors.containsKey("documento"));
    }

    @Test
    void rejectsCodeLongerThanFifteenCharacters() {
        Map<String, String> errors = service.createUser(
                "Juan Perez", "juan@uptc.edu.co", "1234567890123456", "MONITOR", null);
        assertTrue(errors.containsKey("documento"));
    }

    @Test
    void rejectsCodeWithInnerSpaces() {
        Map<String, String> errors = service.createUser(
                "Juan Perez", "juan@uptc.edu.co", "2023 12345", "MONITOR", null);
        assertTrue(errors.containsKey("documento"));
    }

    @Test
    void acceptsAlphanumericCode() {
        when(roleRepository.findByName("MONITOR")).thenReturn(new Role("MONITOR"));
        Map<String, String> errors = service.createUser(
                "Juan Perez", "juan@uptc.edu.co", "202312345", "MONITOR", null);
        assertFalse(errors.containsKey("documento"));
        assertTrue(errors.isEmpty());
    }

    @Test
    void requiresCareerForStudent() {
        Map<String, String> errors = service.createUser(
                "Ana Gomez", "ana@uptc.edu.co", "202312345", "ESTUDIANTE", null);
        assertTrue(errors.containsKey("carrera"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsNonexistentCareerForStudent() {
        when(carreraRepository.findById(99)).thenReturn(Optional.empty());
        Map<String, String> errors = service.createUser(
                "Ana Gomez", "ana@uptc.edu.co", "202312345", "ESTUDIANTE", 99);
        assertTrue(errors.containsKey("carrera"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignsCareerToStudent() {
        Carrera carrera = new Carrera("Ingeniería de Sistemas y Computación");
        carrera.setId(1);
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(roleRepository.findByName("ESTUDIANTE")).thenReturn(new Role("ESTUDIANTE"));

        Map<String, String> errors = service.createUser(
                "Ana Gomez", "ana@uptc.edu.co", "202312345", "ESTUDIANTE", 1);

        assertTrue(errors.isEmpty());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(carrera, captor.getValue().getCareer());
    }

    @Test
    void doesNotRequireCareerForNonStudentRoles() {
        when(roleRepository.findByName("MONITOR")).thenReturn(new Role("MONITOR"));
        Map<String, String> errors = service.createUser(
                "Ana Gomez", "ana@uptc.edu.co", "202312345", "MONITOR", null);
        assertFalse(errors.containsKey("carrera"));
        assertTrue(errors.isEmpty());
    }

    private UserDTO validDto() {
        UserDTO dto = new UserDTO();
        dto.setFirstName("Ana");
        dto.setLastName("Gomez");
        dto.setStudentCode("202312345");
        dto.setEmail("ana@uptc.edu.co");
        dto.setPassword("Segura123*");
        dto.setPassword2("Segura123*");
        return dto;
    }

    @Test
    void registerRejectsInvalidNames() {
        UserDTO dto = validDto();
        dto.setFirstName("An4");
        dto.setLastName("G0mez!");
        Map<String, String> errors = service.registerStudent(dto, 1);
        assertTrue(errors.containsKey("nombres"));
        assertTrue(errors.containsKey("apellidos"));
    }

    @Test
    void registerRejectsInvalidCode() {
        UserDTO dto = validDto();
        dto.setStudentCode("2023-12345");
        Map<String, String> errors = service.registerStudent(dto, 1);
        assertTrue(errors.containsKey("codigo"));
    }

    @Test
    void registerRequiresCareer() {
        Map<String, String> errors = service.registerStudent(validDto(), null);
        assertTrue(errors.containsKey("carrera"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRejectsDuplicateFullName() {
        User existing = new User();
        existing.setFirstName("Ana");
        existing.setLastName("Gómez");
        when(userRepository.findAll()).thenReturn(java.util.List.of(existing));
        Carrera carrera = new Carrera("Ingeniería de Sistemas y Computación");
        carrera.setId(1);
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));

        Map<String, String> errors = service.registerStudent(validDto(), 1);

        assertTrue(errors.containsKey("apellidos"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerAcceptsValidData() {
        Carrera carrera = new Carrera("Ingeniería de Sistemas y Computación");
        carrera.setId(1);
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(userRepository.existsByStudentCode(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ESTUDIANTE")).thenReturn(new Role("ESTUDIANTE"));

        Map<String, String> errors = service.registerStudent(validDto(), 1);

        assertTrue(errors.isEmpty());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(carrera, captor.getValue().getCareer());
    }
}
