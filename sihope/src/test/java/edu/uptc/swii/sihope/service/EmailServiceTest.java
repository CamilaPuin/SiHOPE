package edu.uptc.swii.sihope.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EmailServiceTest {

    @Test
    void doesNotThrowWhenNoSmtpConfigurationIsPresent() {
        EmailService service = new EmailService("http://localhost:5173", "sihope@uptc.edu.co",
                "", "", "", 587, "true", "true", "true", "");

        assertDoesNotThrow(() -> service.sendVerification("destino@dominio.com", "token-123"));
    }
}
