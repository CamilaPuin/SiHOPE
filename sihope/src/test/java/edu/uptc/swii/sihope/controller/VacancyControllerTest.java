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

import edu.uptc.swii.sihope.config.UserArgumentResolver;
import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.dto.response.VacancyResponse;
import edu.uptc.swii.sihope.service.VacancyService;
import edu.uptc.swii.sihope.service.ApplicationService;

class VacancyControllerTest {

    private VacancyService vacancyService;
    private ApplicationService applicationService;
    private MockMvc mvc;

    private static final AuthenticatedUser ESTUDIANTE =
            new AuthenticatedUser(10, "est@uptc.edu.co", "ESTUDIANTE", "Est Udiante", "EU");
    private static final AuthenticatedUser COORDINADOR =
            new AuthenticatedUser(2, "coord@uptc.edu.co", "COORDINADOR", "Coord", "C");

    @BeforeEach
    void setUp() {
        vacancyService = Mockito.mock(VacancyService.class);
        applicationService = Mockito.mock(ApplicationService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new VacancyController(vacancyService, applicationService))
                .setCustomArgumentResolvers(new UserArgumentResolver())
                .build();
    }

    @Test
    void listsOpenVacancies() throws Exception {
        when(vacancyService.listOpen()).thenReturn(List.of(
                new VacancyResponse(1, "Monitor de Cálculo", null, "req", "Cálculo",
                        2, "2026-12-31", "ABIERTA", "2026-07-01T10:00", "Coord")));

        mvc.perform(get("/api/convocatorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].titulo").value("Monitor de Cálculo"));
    }

    @Test
    void studentAppliesSuccessfully() throws Exception {
        when(applicationService.apply(anyInt(), anyInt(), any())).thenReturn(Map.of());

        mvc.perform(post("/api/convocatorias/5/postulaciones")
                        .requestAttr(AuthenticatedUser.ATTRIBUTE, ESTUDIANTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"datos\":{\"promedio\":\"4.5\"}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void rejectsApplicationFromNonStudent() throws Exception {
        mvc.perform(post("/api/convocatorias/5/postulaciones")
                        .requestAttr(AuthenticatedUser.ATTRIBUTE, COORDINADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"datos\":{}}"))
                .andExpect(status().isForbidden());
    }
}
