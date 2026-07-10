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

import edu.uptc.swii.sihope.domain.Postulacion;
import edu.uptc.swii.sihope.domain.Role;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.repository.HistorialRepository;
import edu.uptc.swii.sihope.repository.PostulacionRepository;
import edu.uptc.swii.sihope.repository.RoleRepository;
import edu.uptc.swii.sihope.repository.UserRepository;
import edu.uptc.swii.sihope.service.UserService.ResultadoPromocion;

/**
 * Pruebas unitarias de la promoción a monitor (HU_009): solo aspirantes aprobados
 * y con invalidación del token (incremento de tokenVersion).
 */
class UserServicePromocionTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private HistorialRepository historialRepository;
    @Mock
    private PostulacionRepository postulacionRepository;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private UserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private User aspirante() {
        User u = new User();
        u.setId(10);
        u.setTokenVersion(0);
        u.setRole(new Role("ESTUDIANTE"));
        return u;
    }

    @Test
    void promueveAspiranteAprobadoEInvalidaSuToken() {
        User u = aspirante();
        when(userRepository.findById(10)).thenReturn(Optional.of(u));
        when(postulacionRepository.existsByAspiranteIdAndEstado(10, Postulacion.APROBADA)).thenReturn(true);
        when(roleRepository.findByNombre("MONITOR")).thenReturn(new Role("MONITOR"));

        ResultadoPromocion resultado = service.promoverAMonitor(10);

        assertEquals(ResultadoPromocion.OK, resultado);
        assertEquals("MONITOR", u.getRole().getNombre());
        assertEquals(1, u.getTokenVersion()); // token anterior invalidado
        verify(userRepository).save(u);
        verify(historialRepository).save(any());
    }

    @Test
    void noPromueveSiNoHayPostulacionAprobada() {
        when(userRepository.findById(10)).thenReturn(Optional.of(aspirante()));
        when(postulacionRepository.existsByAspiranteIdAndEstado(10, Postulacion.APROBADA)).thenReturn(false);

        ResultadoPromocion resultado = service.promoverAMonitor(10);

        assertEquals(ResultadoPromocion.NO_APROBADO, resultado);
        verify(userRepository, never()).save(any());
    }

    @Test
    void reportaSiElAspiranteNoExiste() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertEquals(ResultadoPromocion.NO_EXISTE, service.promoverAMonitor(77));
    }

    @Test
    void noPromueveSiYaEsMonitor() {
        User u = aspirante();
        u.setRole(new Role("MONITOR"));
        when(userRepository.findById(10)).thenReturn(Optional.of(u));

        assertEquals(ResultadoPromocion.YA_ES_MONITOR, service.promoverAMonitor(10));
        verify(postulacionRepository, never()).existsByAspiranteIdAndEstado(anyInt(), eq(Postulacion.APROBADA));
    }
}
