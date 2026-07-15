package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.uptc.swii.sihope.domain.Role;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.repository.HistoryRepository;
import edu.uptc.swii.sihope.repository.ApplicationRepository;
import edu.uptc.swii.sihope.repository.CarreraRepository;
import edu.uptc.swii.sihope.repository.RoleRepository;
import edu.uptc.swii.sihope.repository.UserRepository;
import edu.uptc.swii.sihope.service.UserService.RoleChangeResult;
import edu.uptc.swii.sihope.service.UserService.StatusChangeResult;

class UserServiceLastAdminTest {

    private static final String ADMIN = "ADMINISTRADOR";

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

    private User admin(int id) {
        User u = new User();
        u.setId(id);
        u.setActive(true);
        u.setTokenVersion(0);
        u.setRole(new Role(ADMIN));
        return u;
    }

    // ---------- changeRole ----------

    @Test
    void soleAdminCannotChangeOwnRole() {
        when(userRepository.findById(1)).thenReturn(Optional.of(admin(1)));
        when(userRepository.countByRole_NameAndActiveTrueAndIdNot(ADMIN, 1)).thenReturn(0L);

        RoleChangeResult result = service.changeRole(1, "ESTUDIANTE", 1);

        assertEquals(RoleChangeResult.LAST_ADMIN, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void adminCanChangeOwnRoleWhenAnotherAdminExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(admin(1)));
        when(userRepository.countByRole_NameAndActiveTrueAndIdNot(ADMIN, 1)).thenReturn(1L);
        when(roleRepository.findByName("ESTUDIANTE")).thenReturn(new Role("ESTUDIANTE"));

        assertEquals(RoleChangeResult.OK, service.changeRole(1, "ESTUDIANTE", 1));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void adminCanChangeAnotherAdminsRole() {
        // El actor (id 2) cambia el rol del admin id 1: la regla solo aplica al propio rol.
        when(userRepository.findById(1)).thenReturn(Optional.of(admin(1)));
        when(roleRepository.findByName("COORDINADOR")).thenReturn(new Role("COORDINADOR"));

        assertEquals(RoleChangeResult.OK, service.changeRole(1, "COORDINADOR", 2));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void nonAdminRoleChangeIsUnaffected() {
        User student = admin(5);
        student.setRole(new Role("ESTUDIANTE"));
        when(userRepository.findById(5)).thenReturn(Optional.of(student));
        when(roleRepository.findByName("MONITOR")).thenReturn(new Role("MONITOR"));

        assertEquals(RoleChangeResult.OK, service.changeRole(5, "MONITOR", 5));
    }

    @Test
    void reportsInvalidRole() {
        when(userRepository.findById(1)).thenReturn(Optional.of(admin(1)));
        assertEquals(RoleChangeResult.INVALID_ROLE, service.changeRole(1, "SUPREMO", 1));
    }

    @Test
    void reportsMissingUser() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        assertEquals(RoleChangeResult.NOT_FOUND, service.changeRole(99, "MONITOR", 1));
    }

    // ---------- changeStatus ----------

    @Test
    void cannotDeactivateSoleActiveAdmin() {
        when(userRepository.findById(1)).thenReturn(Optional.of(admin(1)));
        when(userRepository.countByRole_NameAndActiveTrueAndIdNot(ADMIN, 1)).thenReturn(0L);

        assertEquals(StatusChangeResult.LAST_ADMIN, service.changeStatus(1));
        verify(userRepository, never()).save(any());
    }

    @Test
    void canDeactivateAdminWhenAnotherActiveAdminExists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(admin(1)));
        when(userRepository.countByRole_NameAndActiveTrueAndIdNot(ADMIN, 1)).thenReturn(1L);

        assertEquals(StatusChangeResult.DEACTIVATED, service.changeStatus(1));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void canReactivateInactiveUser() {
        User inactive = admin(1);
        inactive.setActive(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(inactive));

        assertEquals(StatusChangeResult.ACTIVATED, service.changeStatus(1));
    }
}
