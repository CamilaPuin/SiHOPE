package edu.uptc.swii.sihope.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Expone un {@link ObjectMapper} como bean.
 *
 * <p>Con los starters modulares de Spring Boot 4 ({@code spring-boot-starter-webmvc}),
 * la autoconfiguración de Jackson no siempre publica un {@code ObjectMapper} inyectable.
 * {@link PostulacionService} lo necesita para serializar los campos parametrizables del
 * formulario de postulación ({@code datos_json}), por lo que lo definimos explícitamente.
 * {@code findAndAddModules()} registra los módulos disponibles en el classpath (p. ej.
 * JSR-310 para fechas) para que la (de)serialización funcione en cualquier entorno.</p>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }
}
