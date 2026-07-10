package edu.uptc.swii.sihope.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import edu.uptc.swii.sihope.config.UsuarioArgumentResolver;
import edu.uptc.swii.sihope.dto.UsuarioAutenticado;
import edu.uptc.swii.sihope.dto.response.ConvocatoriaResponse;
import edu.uptc.swii.sihope.service.ConvocatoriaService;
import edu.uptc.swii.sihope.service.PostulacionService;

/**
 * Prueba de integración de HU_005: listado de convocatorias abiertas y
 * postulación (con control de rol ESTUDIANTE).
 */
class ConvocatoriaControllerTest {

    private ConvocatoriaService convocatoriaService;
    private PostulacionService postulacionService;
    private MockMvc mvc;

    private static final UsuarioAutenticado ESTUDIANTE =
            new UsuarioAutenticado(10, "est@uptc.edu.co", "ESTUDIANTE", "Est Udiante", "EU");
    private static final UsuarioAutenticado COORDINADOR =
            new UsuarioAutenticado(2, "coord@uptc.edu.co", "COORDINADOR", "Coord", "C");

    @BeforeEach
    void setUp() {
        convocatoriaService = Mockito.mock(ConvocatoriaService.class);
        postulacionService = Mockito.mock(PostulacionService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new ConvocatoriaController(convocatoriaService, postulacionService))
                .setCustomArgumentResolvers(new UsuarioArgumentResolver())
                .build();
    }

    @Test
    void listaConvocatoriasAbiertas() throws Exception {
        when(convocatoriaService.listarAbiertas()).thenReturn(List.of(
                new ConvocatoriaResponse(1, "Monitor de Cálculo", null, "req", "Cálculo",
                        2, "2026-12-31", "ABIERTA", "2026-07-01T10:00", "Coord")));

        mvc.perform(get("/api/convocatorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].titulo").value("Monitor de Cálculo"));
    }

    @Test
    void estudianteSePostulaCorrectamente() throws Exception {
        when(postulacionService.postular(anyInt(), anyInt(), any())).thenReturn(Map.of());

        mvc.perform(post("/api/convocatorias/5/postulaciones")
                        .requestAttr(UsuarioAutenticado.ATRIBUTO, ESTUDIANTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"datos\":{\"promedio\":\"4.5\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void rechazaPostulacionDeNoEstudiante() throws Exception {
        mvc.perform(post("/api/convocatorias/5/postulaciones")
                        .requestAttr(UsuarioAutenticado.ATRIBUTO, COORDINADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"datos\":{}}"))
                .andExpect(status().isForbidden());
    }
}
