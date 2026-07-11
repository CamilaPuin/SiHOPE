package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.uptc.swii.sihope.domain.Application;
import edu.uptc.swii.sihope.domain.Role;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.repository.HistoryRepository;
import edu.uptc.swii.sihope.repository.ApplicationRepository;
import edu.uptc.swii.sihope.repository.RoleRepository;
import edu.uptc.swii.sihope.repository.UserRepository;
import edu.uptc.swii.sihope.service.UserService.PromotionResult;

class UserServicePromotionTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private HistoryRepository historyRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private UserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User applicant() {
        User u = new User();
        u.setId(10);
        u.setTokenVersion(0);
        u.setRole(new Role("ESTUDIANTE"));
        return u;
    }

    @Test
    void promotesApprovedApplicantAndInvalidatesToken() {
        User u = applicant();
        when(userRepository.findById(10)).thenReturn(Optional.of(u));
        when(applicationRepository.existsByApplicantIdAndState(10, Application.APROBADA)).thenReturn(true);
        when(roleRepository.findByName("MONITOR")).thenReturn(new Role("MONITOR"));

        PromotionResult result = service.promoteToMonitor(10);

        assertEquals(PromotionResult.OK, result);
        assertEquals("MONITOR", u.getRole().getNombre());
        assertEquals(1, u.getTokenVersion());
        verify(userRepository).save(u);
        verify(historyRepository).save(any());
    }

    @Test
    void doesNotPromoteWithoutApprovedApplication() {
        when(userRepository.findById(10)).thenReturn(Optional.of(applicant()));
        when(applicationRepository.existsByApplicantIdAndState(10, Application.APROBADA)).thenReturn(false);

        PromotionResult result = service.promoteToMonitor(10);

        assertEquals(PromotionResult.NOT_APPROVED, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void reportsIfApplicantDoesNotExist() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertEquals(PromotionResult.NOT_FOUND, service.promoteToMonitor(77));
    }

    @Test
    void doesNotPromoteIfAlreadyMonitor() {
        User u = applicant();
        u.setRole(new Role("MONITOR"));
        when(userRepository.findById(10)).thenReturn(Optional.of(u));

        assertEquals(PromotionResult.ALREADY_MONITOR, service.promoteToMonitor(10));
        verify(applicationRepository, never()).existsByApplicantIdAndState(anyInt(), eq(Application.APROBADA));
    }
}
