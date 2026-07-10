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

import edu.uptc.swii.sihope.domain.Convocatoria;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.request.CrearConvocatoriaRequest;
import edu.uptc.swii.sihope.repository.ConvocatoriaRepository;
import edu.uptc.swii.sihope.repository.UserRepository;

/**
 * Pruebas unitarias de {@link ConvocatoriaService} (HU_008): validaciones de
 * creación y control del estado.
 */
class ConvocatoriaServiceTest {

    @Mock
    private ConvocatoriaRepository convocatoriaRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ConvocatoriaService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(new User()));
    }

    private CrearConvocatoriaRequest requestValido() {
        CrearConvocatoriaRequest r = new CrearConvocatoriaRequest();
        r.setTitulo("Monitor de Cálculo");
        r.setMateria("Cálculo Diferencial");
        r.setRequisitos("Promedio >= 4.0");
        r.setPlazas(2);
        r.setFechaLimite(LocalDate.now().plusDays(10).toString());
        return r;
    }

    @Test
    void creaConvocatoriaValidaEnEstadoAbierta() {
        Map<String, String> errores = service.crear(1, requestValido());
        assertTrue(errores.isEmpty());
        verify(convocatoriaRepository).save(any(Convocatoria.class));
    }

    @Test
    void rechazaCamposObligatoriosVacios() {
        Map<String, String> errores = service.crear(1, new CrearConvocatoriaRequest());
        assertFalse(errores.isEmpty());
        assertTrue(errores.containsKey("titulo"));
        assertTrue(errores.containsKey("materia"));
        assertTrue(errores.containsKey("requisitos"));
        assertTrue(errores.containsKey("plazas"));
        assertTrue(errores.containsKey("fechaLimite"));
        verify(convocatoriaRepository, never()).save(any());
    }

    @Test
    void rechazaFechaLimiteEnElPasado() {
        CrearConvocatoriaRequest r = requestValido();
        r.setFechaLimite(LocalDate.now().minusDays(1).toString());
        Map<String, String> errores = service.crear(1, r);
        assertTrue(errores.containsKey("fechaLimite"));
        verify(convocatoriaRepository, never()).save(any());
    }

    @Test
    void rechazaPlazasNoPositivas() {
        CrearConvocatoriaRequest r = requestValido();
        r.setPlazas(0);
        Map<String, String> errores = service.crear(1, r);
        assertTrue(errores.containsKey("plazas"));
    }
}
