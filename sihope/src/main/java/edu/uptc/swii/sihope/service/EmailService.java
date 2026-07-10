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

/**
 * Envío de correos transaccionales de SiHope (verificación de cuenta, credenciales
 * de acceso y recuperación de contraseña) a través de la API de Gmail.
 *
 * <p>Si el cliente {@link Gmail} no está configurado (falta el refresh token) o el
 * envío falla, el servicio cae a un modo <b>simulado</b> que registra el correo en
 * el log, de modo que el registro/recuperación nunca se rompen por un fallo de correo.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /** Cliente Gmail (puede ser null si no hay credenciales: ver GmailConfig). */
    private final ObjectProvider<Gmail> gmailProvider;
    private final String baseUrl;
    private final String remitente;

    public EmailService(ObjectProvider<Gmail> gmailProvider,
                        @Value("${app.frontend.base-url}") String baseUrl,
                        @Value("${app.mail.from:}") String remitente) {
        this.gmailProvider = gmailProvider;
        this.baseUrl = baseUrl;
        this.remitente = remitente;
    }

    public void enviarVerificacion(String correo, String token) {
        String enlace = baseUrl + "/verificar?token=" + token;
        String cuerpo = """
                <p>Bienvenido a <strong>SiHope</strong>.</p>
                <p>Activa tu cuenta ingresando al siguiente enlace:</p>
                <p><a href="%s">Activar mi cuenta</a></p>
                <p>Si no creaste esta cuenta, ignora este correo.</p>
                """.formatted(enlace);
        enviar(correo, "Activa tu cuenta de SiHope", cuerpo,
                "Verificación de cuenta", "Activa tu cuenta ingresando a:\n  " + enlace);
    }

    public void enviarCredenciales(String correo, String passwordTemporal) {
        String cuerpo = """
                <p>Se creó tu cuenta en <strong>SiHope</strong>.</p>
                <ul>
                  <li><strong>Usuario:</strong> %s</li>
                  <li><strong>Contraseña temporal:</strong> %s</li>
                </ul>
                <p>Cámbiala tras tu primer inicio de sesión.</p>
                """.formatted(correo, passwordTemporal);
        enviar(correo, "Tu cuenta de SiHope está lista", cuerpo,
                "Credenciales de acceso",
                "Usuario: " + correo + "\n  Contraseña temporal: " + passwordTemporal
                        + "\n  Cámbiala tras tu primer inicio de sesión.");
    }

    public void enviarRecuperacion(String correo, String token) {
        String enlace = baseUrl + "/restablecer?token=" + token;
        String cuerpo = """
                <p>Solicitaste restablecer tu contraseña de <strong>SiHope</strong>.</p>
                <p>El siguiente enlace vence en 30 minutos:</p>
                <p><a href="%s">Restablecer mi contraseña</a></p>
                <p>Si no lo solicitaste, ignora este correo.</p>
                """.formatted(enlace);
        enviar(correo, "Restablece tu contraseña de SiHope", cuerpo,
                "Recuperación de contraseña",
                "Solicitaste restablecer tu contraseña. El enlace vence en 30 minutos:\n  " + enlace);
    }

    /**
     * Intenta enviar el correo por la API de Gmail. Si el cliente no está disponible
     * o el envío falla, registra el correo en el log (modo simulado) para no bloquear
     * el flujo de negocio.
     */
    private void enviar(String destino, String asunto, String cuerpoHtml,
                        String tipoSimulado, String cuerpoSimulado) {
        Gmail gmail = gmailProvider.getIfAvailable();
        if (gmail == null) {
            simular(tipoSimulado, destino, asunto, cuerpoSimulado);
            return;
        }
        try {
            MimeMessage mime = construirMime(destino, asunto, cuerpoHtml);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mime.writeTo(buffer);
            String raw = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());

            Message mensaje = new Message().setRaw(raw);
            gmail.users().messages().send("me", mensaje).execute();
            log.info("Correo enviado a {} vía API de Gmail (asunto: {})", destino, asunto);
        } catch (Exception e) {
            log.error("Fallo al enviar correo a {} vía API de Gmail; se registra en modo simulado. Causa: {}",
                    destino, e.getMessage(), e);
            simular(tipoSimulado, destino, asunto, cuerpoSimulado);
        }
    }

    private MimeMessage construirMime(String destino, String asunto, String cuerpoHtml) throws Exception {
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        if (remitente != null && !remitente.isBlank()) {
            mime.setFrom(new InternetAddress(remitente));
        }
        mime.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(destino));
        mime.setSubject(asunto, StandardCharsets.UTF_8.name());
        mime.setText(cuerpoHtml, StandardCharsets.UTF_8.name(), "html");
        return mime;
    }

    private void simular(String tipo, String correo, String asunto, String cuerpo) {
        log.info("\n===== [CORREO SIMULADO] {} =====\n" +
                "Para: {}\n" +
                "Asunto: {}\n" +
                "Cuerpo: {}\n" +
                "====================================================",
                tipo, correo, asunto, cuerpo);
    }
}
