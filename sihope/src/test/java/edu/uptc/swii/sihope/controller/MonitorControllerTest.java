package edu.uptc.swii.sihope.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import edu.uptc.swii.sihope.config.UsuarioArgumentResolver;
import edu.uptc.swii.sihope.dto.UsuarioAutenticado;
import edu.uptc.swii.sihope.service.DisponibilidadService;

/**
 * Prueba de integración del endpoint principal de HU_006
 * (PUT /api/monitor/disponibilidad) con MockMvc standalone.
 */
class MonitorControllerTest {

    private DisponibilidadService service;
    private MockMvc mvc;

    private static final UsuarioAutenticado MONITOR =
            new UsuarioAutenticado(1, "monitor@uptc.edu.co", "MONITOR", "Mon Itor", "MI");

    @BeforeEach
    void setUp() {
        service = Mockito.mock(DisponibilidadService.class);
        mvc = MockMvcBuilders.standaloneSetup(new MonitorController(service))
                .setCustomArgumentResolvers(new UsuarioArgumentResolver())
                .build();
    }

    @Test
    void guardaDisponibilidadValida() throws Exception {
        when(service.reemplazar(anyInt(), any())).thenReturn(List.of());

        mvc.perform(put("/api/monitor/disponibilidad")
                        .requestAttr(UsuarioAutenticado.ATRIBUTO, MONITOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bloques\":[{\"diaSemana\":1,\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void devuelve400CuandoHayErroresDeValidacion() throws Exception {
        when(service.reemplazar(anyInt(), any())).thenReturn(List.of("Hay franjas que se solapan."));

        mvc.perform(put("/api/monitor/disponibilidad")
                        .requestAttr(UsuarioAutenticado.ATRIBUTO, MONITOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bloques\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
