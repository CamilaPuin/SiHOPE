package edu.uptc.swii.sihope.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final UserArgumentResolver userArgumentResolver;

    public WebConfig(JwtAuthInterceptor jwtAuthInterceptor,
            UserArgumentResolver userArgumentResolver) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.userArgumentResolver = userArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns(
                        "/api/auth/me",
                        "/api/admin/**",
                        "/api/monitor/**",
                        "/api/monitores",
                        "/api/monitores/**",
                        "/api/asignaturas",
                        "/api/asignaturas/**",
                        "/api/carreras",
                        "/api/carreras/**",
                        "/api/citas",
                        "/api/citas/**",
                        "/api/coordinador/**",
                        "/api/convocatorias/**",
                        "/api/credenciales/password");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type") 
                .allowCredentials(true)
                .maxAge(3600);
    }
}
