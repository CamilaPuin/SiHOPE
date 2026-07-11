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

import edu.uptc.swii.sihope.config.UserArgumentResolver;
import edu.uptc.swii.sihope.dto.AuthenticatedUser;
import edu.uptc.swii.sihope.service.AvailabilityService;

class MonitorControllerTest {

    private AvailabilityService service;
    private MockMvc mvc;

    private static final AuthenticatedUser MONITOR =
            new AuthenticatedUser(1, "monitor@uptc.edu.co", "MONITOR", "Mon Itor", "MI");

    @BeforeEach
    void setUp() {
        service = Mockito.mock(AvailabilityService.class);
        mvc = MockMvcBuilders.standaloneSetup(new MonitorController(service))
                .setCustomArgumentResolvers(new UserArgumentResolver())
                .build();
    }

    @Test
    void savesValidAvailability() throws Exception {
        when(service.replace(anyInt(), any())).thenReturn(List.of());

        mvc.perform(put("/api/monitor/disponibilidad")
                        .requestAttr(AuthenticatedUser.ATTRIBUTE, MONITOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bloques\":[{\"diaSemana\":1,\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void returns400WhenThereAreValidationErrors() throws Exception {
        when(service.replace(anyInt(), any())).thenReturn(List.of("Hay franjas que se solapan."));

        mvc.perform(put("/api/monitor/disponibilidad")
                        .requestAttr(AuthenticatedUser.ATTRIBUTE, MONITOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bloques\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
