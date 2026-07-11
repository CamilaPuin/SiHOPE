package edu.uptc.swii.sihope.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import edu.uptc.swii.sihope.config.UserArgumentResolver;
import edu.uptc.swii.sihope.domain.Application;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.service.VacancyService;
import edu.uptc.swii.sihope.service.ApplicationService;
import edu.uptc.swii.sihope.service.UserService;
import edu.uptc.swii.sihope.service.UserService.PromotionResult;

class CoordinatorControllerTest {

    private VacancyService vacancyService;
    private ApplicationService applicationService;
    private UserService userService;
    private MockMvc mvc;

    private static final AuthenticatedUser COORDINADOR =
            new AuthenticatedUser(2, "coord@uptc.edu.co", "COORDINADOR", "Coord", "C");

    @BeforeEach
    void setUp() {
        vacancyService = Mockito.mock(VacancyService.class);
        applicationService = Mockito.mock(ApplicationService.class);
        userService = Mockito.mock(UserService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new CoordinatorController(vacancyService, applicationService, userService))
                .setCustomArgumentResolvers(new UserArgumentResolver())
                .build();
    }

    @Test
    void createsVacancy() throws Exception {
        when(vacancyService.create(anyInt(), any())).thenReturn(Map.of());

        mvc.perform(post("/api/coordinador/convocatorias")
                        .requestAttr(AuthenticatedUser.ATTRIBUTE, COORDINADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Monitor de Cálculo\",\"materia\":\"Cálculo\","
                                + "\"requisitos\":\"Promedio 4.0\",\"plazas\":2,\"fechaLimite\":\"2026-12-31\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void promotesApprovedApplicant() throws Exception {
        User applicant = new User();
        applicant.setId(10);
        Application application = new Application();
        application.setId(1);
        application.setAspirante(applicant);

        when(applicationService.findById(1)).thenReturn(Optional.of(application));
        when(userService.promoteToMonitor(10)).thenReturn(PromotionResult.OK);

        mvc.perform(post("/api/coordinador/postulaciones/1/promover")
                        .requestAttr(AuthenticatedUser.ATTRIBUTE, COORDINADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void rejectsPromotionWithoutApproval() throws Exception {
        User applicant = new User();
        applicant.setId(10);
        Application application = new Application();
        application.setId(1);
        application.setAspirante(applicant);

        when(applicationService.findById(1)).thenReturn(Optional.of(application));
        when(userService.promoteToMonitor(10)).thenReturn(PromotionResult.NOT_APPROVED);

        mvc.perform(post("/api/coordinador/postulaciones/1/promover")
                        .requestAttr(AuthenticatedUser.ATTRIBUTE, COORDINADOR))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
