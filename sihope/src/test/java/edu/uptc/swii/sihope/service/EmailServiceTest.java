package edu.uptc.swii.sihope.service;

import com.google.api.services.gmail.Gmail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class EmailServiceTest {

    @Test
    void doesNotThrowWhenNoSmtpConfigurationIsPresent() {
        ObjectProvider<Gmail> gmailProvider = mock(ObjectProvider.class);
        EmailService service = new EmailService(gmailProvider, "http://localhost:5173", "sihope@uptc.edu.co",
                "", "", "", 587, "true", "true", "true", "");

        assertDoesNotThrow(() -> service.sendVerification("destino@dominio.com", "token-123"));
    }
}
