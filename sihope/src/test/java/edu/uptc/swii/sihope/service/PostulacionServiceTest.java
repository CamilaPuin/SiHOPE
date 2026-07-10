package edu.uptc.swii.sihope.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uptc.swii.sihope.domain.Convocatoria;
import edu.uptc.swii.sihope.domain.Postulacion;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.repository.ConvocatoriaRepository;
import edu.uptc.swii.sihope.repository.PostulacionRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

/**
 * Pruebas unitarias de {@link PostulacionService} (HU_005): reglas de postulación.
 */
class PostulacionServiceTest {

    @Mock
    private PostulacionRepository postulacionRepository;
    @Mock
    private ConvocatoriaRepository convocatoriaRepository;
    @Mock
    private UserRepository userRepository;
    private PostulacionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PostulacionService(postulacionRepository, convocatoriaRepository,
                userRepository, new ObjectMapper());
    }

    private Convocatoria convocatoriaAbierta() {
        Convocatoria c = new Convocatoria();
        c.setId(5);
        c.setEstado(Convocatoria.ABIERTA);
        c.setFechaLimite(LocalDate.now().plusDays(5));
        return c;
    }

    @Test
    void registraPostulacionValida() {
        when(convocatoriaRepository.findById(5)).thenReturn(Optional.of(convocatoriaAbierta()));
        when(postulacionRepository.existsByConvocatoriaIdAndAspiranteId(5, 10)).thenReturn(false);
        when(userRepository.findById(10)).thenReturn(Optional.of(new User()));

        Map<String, String> errores = service.postular(5, 10, Map.of("promedio", "4.5"));

        assertTrue(errores.isEmpty());
        verify(postulacionRepository).save(any(Postulacion.class));
    }

    @Test
    void rechazaSiLaConvocatoriaNoExiste() {
        when(convocatoriaRepository.findById(anyInt())).thenReturn(Optional.empty());
        Map<String, String> errores = service.postular(99, 10, Map.of());
        assertFalse(errores.isEmpty());
        verify(postulacionRepository, never()).save(any());
    }

    @Test
    void rechazaSiLaConvocatoriaEstaCerrada() {
        Convocatoria cerrada = convocatoriaAbierta();
        cerrada.setEstado(Convocatoria.CERRADA);
        when(convocatoriaRepository.findById(5)).thenReturn(Optional.of(cerrada));

        Map<String, String> errores = service.postular(5, 10, Map.of());
        assertFalse(errores.isEmpty());
        verify(postulacionRepository, never()).save(any());
    }

    @Test
    void rechazaPostulacionDuplicada() {
        when(convocatoriaRepository.findById(5)).thenReturn(Optional.of(convocatoriaAbierta()));
        when(postulacionRepository.existsByConvocatoriaIdAndAspiranteId(5, 10)).thenReturn(true);

        Map<String, String> errores = service.postular(5, 10, Map.of());
        assertFalse(errores.isEmpty());
        verify(postulacionRepository, never()).save(any());
    }
}
