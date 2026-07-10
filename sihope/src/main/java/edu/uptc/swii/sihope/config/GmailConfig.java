package edu.uptc.swii.sihope.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del cliente de la API de Gmail.
 *
 * <p>Publica un bean {@link Gmail} construido con credenciales OAuth2 de usuario
 * (client id/secret + refresh token). La librería de Google renueva el access
 * token automáticamente a partir del refresh token en cada envío.
 *
 * <p>El bean es <b>condicional</b> a que exista {@code app.mail.gmail.refresh-token}:
 * si no está configurado (p. ej. en desarrollo o CI), el bean no se crea y
 * {@link edu.uptc.swii.sihope.service.EmailService} cae al modo simulado (log).
 */
@Configuration
public class GmailConfig {

    @Bean
    @ConditionalOnProperty(name = "app.mail.gmail.refresh-token")
    public Gmail gmailClient(
            @Value("${app.mail.gmail.client-id}") String clientId,
            @Value("${app.mail.gmail.client-secret}") String clientSecret,
            @Value("${app.mail.gmail.refresh-token}") String refreshToken) throws Exception {

        // Los scopes concedidos (gmail.send) los determina el consentimiento original
        // asociado al refresh token; no hace falta re-declararlos aquí.
        UserCredentials credenciales = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credenciales))
                .setApplicationName("SiHope")
                .build();
    }
}
