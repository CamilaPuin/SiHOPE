package edu.uptc.swii.sihope.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String JWT_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI sihopeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SiHope API")
                        .version("v2 (Sprint 2)")
                        .description("API del programa de monitorías académicas de la UPTC. "
                                + "Autenticación por JWT: usa POST /api/auth/login y envía el token como "
                                + "'Authorization: Bearer <token>'."))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME))
                .components(new Components().addSecuritySchemes(JWT_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
