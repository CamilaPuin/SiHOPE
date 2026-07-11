package edu.uptc.swii.sihope.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<Gmail> gmailProvider;
    private final String baseUrl;
    private final String sender;

    public EmailService(ObjectProvider<Gmail> gmailProvider,
                        @Value("${app.frontend.base-url}") String baseUrl,
                        @Value("${app.mail.from:}") String sender) {
        this.gmailProvider = gmailProvider;
        this.baseUrl = baseUrl;
        this.sender = sender;
    }

    public void sendVerification(String correo, String token) {
        String link = baseUrl + "/verificar?token=" + token;
        String body = """
                <p>Bienvenido a <strong>SiHope</strong>.</p>
                <p>Activa tu cuenta ingresando al siguiente enlace:</p>
                <p><a href="%s">Activar mi cuenta</a></p>
                <p>Si no creaste esta cuenta, ignora este correo.</p>
                """.formatted(link);
        send(correo, "Activa tu cuenta de SiHope", body,
                "Verificación de cuenta", "Activa tu cuenta ingresando a:\n  " + link);
    }

    public void sendCredentials(String correo, String temporaryPassword) {
        String body = """
                <p>Se creó tu cuenta en <strong>SiHope</strong>.</p>
                <ul>
                  <li><strong>Usuario:</strong> %s</li>
                  <li><strong>Contraseña temporal:</strong> %s</li>
                </ul>
                <p>Cámbiala tras tu primer inicio de sesión.</p>
                """.formatted(correo, temporaryPassword);
        send(correo, "Tu cuenta de SiHope está lista", body,
                "Credenciales de acceso",
                "Usuario: " + correo + "\n  Contraseña temporal: " + temporaryPassword
                        + "\n  Cámbiala tras tu primer inicio de sesión.");
    }

    public void sendPasswordReset(String correo, String token) {
        String link = baseUrl + "/restablecer?token=" + token;
        String body = """
                <p>Solicitaste restablecer tu contraseña de <strong>SiHope</strong>.</p>
                <p>El siguiente enlace vence en 30 minutos:</p>
                <p><a href="%s">Restablecer mi contraseña</a></p>
                <p>Si no lo solicitaste, ignora este correo.</p>
                """.formatted(link);
        send(correo, "Restablece tu contraseña de SiHope", body,
                "Recuperación de contraseña",
                "Solicitaste restablecer tu contraseña. El enlace vence en 30 minutos:\n  " + link);
    }

    private void send(String recipient, String subject, String htmlBody,
                      String simulatedType, String simulatedBody) {
        Gmail gmail = gmailProvider.getIfAvailable();
        if (gmail == null) {
            simulate(simulatedType, recipient, subject, simulatedBody);
            return;
        }
        try {
            MimeMessage mime = buildMime(recipient, subject, htmlBody);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mime.writeTo(buffer);
            String raw = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());

            Message message = new Message().setRaw(raw);
            gmail.users().messages().send("me", message).execute();
            log.info("Correo enviado a {} vía API de Gmail (asunto: {})", recipient, subject);
        } catch (Exception e) {
            log.error("Fallo al enviar correo a {} vía API de Gmail; se registra en modo simulado. Causa: {}",
                    recipient, e.getMessage(), e);
            simulate(simulatedType, recipient, subject, simulatedBody);
        }
    }

    private MimeMessage buildMime(String recipient, String subject, String htmlBody) throws Exception {
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        if (sender != null && !sender.isBlank()) {
            mime.setFrom(new InternetAddress(sender));
        }
        mime.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(recipient));
        mime.setSubject(subject, StandardCharsets.UTF_8.name());
        mime.setText(htmlBody, StandardCharsets.UTF_8.name(), "html");
        return mime;
    }

    private void simulate(String type, String correo, String subject, String body) {
        log.info("\nCORREO SIMULADO] {} \n" +
                "Para: {}\n" +
                "Asunto: {}\n" +
                "Cuerpo: {}\n" +
                "-------------------------------------------------",
                type, correo, subject, body);
    }
}
