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

import edu.uptc.swii.sihope.config.UsuarioArgumentResolver;
import edu.uptc.swii.sihope.domain.Postulacion;
import edu.uptc.swii.sihope.domain.User;
import edu.uptc.swii.sihope.dto.UsuarioAutenticado;
import edu.uptc.swii.sihope.service.ConvocatoriaService;
import edu.uptc.swii.sihope.service.PostulacionService;
import edu.uptc.swii.sihope.service.UserService;
import edu.uptc.swii.sihope.service.UserService.ResultadoPromocion;

/**
 * Pruebas de integración de HU_008 (crear convocatoria) y HU_009 (promover a
 * monitor) sobre el CoordinadorController.
 */
class CoordinadorControllerTest {

    private ConvocatoriaService convocatoriaService;
    private PostulacionService postulacionService;
    private UserService userService;
    private MockMvc mvc;

    private static final UsuarioAutenticado COORDINADOR =
            new UsuarioAutenticado(2, "coord@uptc.edu.co", "COORDINADOR", "Coord", "C");

    @BeforeEach
    void setUp() {
        convocatoriaService = Mockito.mock(ConvocatoriaService.class);
        postulacionService = Mockito.mock(PostulacionService.class);
        userService = Mockito.mock(UserService.class);
        mvc = MockMvcBuilders
                .standaloneSetup(new CoordinadorController(convocatoriaService, postulacionService, userService))
                .setCustomArgumentResolvers(new UsuarioArgumentResolver())
                .build();
    }

    @Test
    void creaConvocatoria() throws Exception {
        when(convocatoriaService.crear(anyInt(), any())).thenReturn(Map.of());

        mvc.perform(post("/api/coordinador/convocatorias")
                        .requestAttr(UsuarioAutenticado.ATRIBUTO, COORDINADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Monitor de Cálculo\",\"materia\":\"Cálculo\","
                                + "\"requisitos\":\"Promedio 4.0\",\"plazas\":2,\"fechaLimite\":\"2026-12-31\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void promueveAspiranteAprobado() throws Exception {
        User aspirante = new User();
        aspirante.setId(10);
        Postulacion postulacion = new Postulacion();
        postulacion.setId(1);
        postulacion.setAspirante(aspirante);

        when(postulacionService.obtener(1)).thenReturn(Optional.of(postulacion));
        when(userService.promoverAMonitor(10)).thenReturn(ResultadoPromocion.OK);

        mvc.perform(post("/api/coordinador/postulaciones/1/promover")
                        .requestAttr(UsuarioAutenticado.ATRIBUTO, COORDINADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void rechazaPromocionSiNoHayAprobacion() throws Exception {
        User aspirante = new User();
        aspirante.setId(10);
        Postulacion postulacion = new Postulacion();
        postulacion.setId(1);
        postulacion.setAspirante(aspirante);

        when(postulacionService.obtener(1)).thenReturn(Optional.of(postulacion));
        when(userService.promoverAMonitor(10)).thenReturn(ResultadoPromocion.NO_APROBADO);

        mvc.perform(post("/api/coordinador/postulaciones/1/promover")
                        .requestAttr(UsuarioAutenticado.ATRIBUTO, COORDINADOR))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
