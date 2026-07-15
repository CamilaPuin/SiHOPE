package edu.uptc.swii.sihope.service;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final String baseUrl;
    private final String sender;
    private final String mailHost;
    private final String mailUsername;
    private final String mailPassword;
    private final int mailPort;
    private final String smtpAuth;
    private final String starttlsEnabled;
    private final String starttlsRequired;
    private final String smtpSslTrust;

    public EmailService(@Value("${app.frontend.base-url}") String baseUrl,
                        @Value("${app.mail.from:}") String sender,
                        @Value("${spring.mail.host:}") String mailHost,
                        @Value("${spring.mail.username:}") String mailUsername,
                        @Value("${spring.mail.password:}") String mailPassword,
                        @Value("${spring.mail.port:587}") int mailPort,
                        @Value("${spring.mail.properties.mail.smtp.auth:true}") String smtpAuth,
                        @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") String starttlsEnabled,
                        @Value("${spring.mail.properties.mail.smtp.starttls.required:true}") String starttlsRequired,
                        @Value("${spring.mail.properties.mail.smtp.ssl.trust:}") String smtpSslTrust) {
        this.baseUrl = baseUrl;
        this.sender = sender;
        this.mailHost = mailHost;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
        this.mailPort = mailPort;
        this.smtpAuth = smtpAuth;
        this.starttlsEnabled = starttlsEnabled;
        this.starttlsRequired = starttlsRequired;
        this.smtpSslTrust = smtpSslTrust;
    }

    public void sendVerification(String correo, String token) {
        String link = baseUrl + "/verificar?token=" + token;
        String body = """
                <p>Bienvenido a <strong>SiHope</strong>.</p>
                <p>Activa tu cuenta ingresando al siguiente enlace:</p>
                <p><a href="%s">Activar mi cuenta</a></p>
                <p>Si no creaste esta cuenta, ignora este correo.</p>
                """.formatted(link);
        send(correo, "Activa tu cuenta de SiHope", body);
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
        send(correo, "Tu cuenta de SiHope está lista", body);
    }

    public void sendPasswordReset(String correo, String token) {
        String link = baseUrl + "/restablecer?token=" + token;
        String body = """
                <p>Solicitaste restablecer tu contraseña de <strong>SiHope</strong>.</p>
                <p>El siguiente enlace vence en 30 minutos:</p>
                <p><a href="%s">Restablecer mi contraseña</a></p>
                <p>Si no lo solicitaste, ignora este correo.</p>
                """.formatted(link);
        send(correo, "Restablece tu contraseña de SiHope", body);
    }

    public void sendCitaReservada(String monitorEmail, String studentName, String subjectName,
                                  String date, String time) {
        String body = """
                <p><strong>%s</strong> reservó una monitoría contigo.</p>
                <ul>
                  <li><strong>Asignatura:</strong> %s</li>
                  <li><strong>Fecha:</strong> %s</li>
                  <li><strong>Hora:</strong> %s</li>
                </ul>
                <p>Ingresa a SiHope para confirmarla o revisarla.</p>
                """.formatted(studentName, subjectName, date, time);
        send(monitorEmail, "Nueva cita de monitoría por confirmar", body);
    }

    public void sendCitaConfirmada(String studentEmail, String monitorName, String subjectName,
                                   String date, String time) {
        String body = """
                <p>Tu monitoría fue <strong>confirmada</strong> por %s.</p>
                <ul>
                  <li><strong>Asignatura:</strong> %s</li>
                  <li><strong>Fecha:</strong> %s</li>
                  <li><strong>Hora:</strong> %s</li>
                </ul>
                """.formatted(monitorName, subjectName, date, time);
        send(studentEmail, "Tu cita de monitoría fue confirmada", body);
    }

    public void sendCitaCancelada(String recipientEmail, String subjectName, String date, String time,
                                  String reason) {
        String extra = (reason == null || reason.isBlank()) ? "" : "<p><strong>Motivo:</strong> " + reason + "</p>";
        String body = """
                <p>La monitoría de <strong>%s</strong> del %s a las %s fue <strong>cancelada</strong>.</p>
                %s
                <p>El horario queda liberado nuevamente.</p>
                """.formatted(subjectName, date, time, extra);
        send(recipientEmail, "Una cita de monitoría fue cancelada", body);
    }

    public void sendCitaRecordatorio(String recipientEmail, String subjectName, String date, String time,
                                     String counterpart) {
        String body = """
                <p>Te recordamos tu monitoría próxima:</p>
                <ul>
                  <li><strong>Asignatura:</strong> %s</li>
                  <li><strong>Con:</strong> %s</li>
                  <li><strong>Fecha:</strong> %s</li>
                  <li><strong>Hora:</strong> %s</li>
                </ul>
                """.formatted(subjectName, counterpart, date, time);
        send(recipientEmail, "Recordatorio: tienes una monitoría mañana", body);
    }

    private void send(String recipient, String subject, String htmlBody) {
        if (!smtpConfigured()) {
            log.warn("SMTP no configurado (spring.mail.host/username/password vacíos); no se envió el correo a {} (asunto: {})",
                    recipient, subject);
            return;
        }
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", mailHost);
            props.put("mail.smtp.port", String.valueOf(mailPort));
            props.put("mail.smtp.auth", smtpAuth);
            props.put("mail.smtp.starttls.enable", starttlsEnabled);
            props.put("mail.smtp.starttls.required", starttlsRequired);
            if (!smtpSslTrust.isBlank()) {
                props.put("mail.smtp.ssl.trust", smtpSslTrust);
                props.put("mail.smtp.ssl.checkserveridentity", "false");
            }

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailUsername, mailPassword);
                }
            });

            MimeMessage mime = buildMime(session, recipient, subject, htmlBody);
            Transport.send(mime);
            log.info("Correo enviado a {} vía SMTP (asunto: {})", recipient, subject);
        } catch (Exception e) {
            log.error("Fallo al enviar correo a {} vía SMTP. Causa: {}", recipient, e.getMessage(), e);
        }
    }

    private boolean smtpConfigured() {
        return !mailHost.isBlank() && !mailUsername.isBlank() && !mailPassword.isBlank();
    }

    private MimeMessage buildMime(Session session, String recipient, String subject, String htmlBody) throws Exception {
        MimeMessage mime = new MimeMessage(session);
        if (sender != null && !sender.isBlank()) {
            mime.setFrom(new InternetAddress(sender, "SiHope", StandardCharsets.UTF_8.name()));
        }
        mime.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(recipient));
        mime.setSubject(subject, StandardCharsets.UTF_8.name());
        mime.setText(htmlBody, StandardCharsets.UTF_8.name(), "html");
        return mime;
    }
}
