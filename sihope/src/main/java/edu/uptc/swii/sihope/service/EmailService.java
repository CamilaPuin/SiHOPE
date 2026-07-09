package edu.uptc.swii.sihope.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BASE_URL = "http://localhost:8080";

    public void enviarVerificacion(String correo, String token) {
        String enlace = BASE_URL + "/verificar?token=" + token;
        log.info("\n===== [CORREO SIMULADO] Verificación de cuenta =====\n" +
                "Para: {}\n" +
                "Asunto: Activa tu cuenta de SiHope\n" +
                "Cuerpo: Bienvenido a SiHope. Activa tu cuenta ingresando a:\n  {}\n" +
                "====================================================", correo, enlace);
    }

    public void enviarCredenciales(String correo, String passwordTemporal) {
        log.info("\n===== [CORREO SIMULADO] Credenciales de acceso =====\n" +
                "Para: {}\n" +
                "Asunto: Tu cuenta de SiHope está lista\n" +
                "Cuerpo: Se creó tu cuenta en SiHope.\n" +
                "  Usuario: {}\n" +
                "  Contraseña temporal: {}\n" +
                "  Cámbiala tras tu primer inicio de sesión.\n" +
                "====================================================", correo, correo, passwordTemporal);
    }

    public void enviarRecuperacion(String correo, String token) {
        String enlace = BASE_URL + "/restablecer?token=" + token;
        log.info("\n===== [CORREO SIMULADO] Recuperación de contraseña =====\n" +
                "Para: {}\n" +
                "Asunto: Restablece tu contraseña de SiHope\n" +
                "Cuerpo: Solicitaste restablecer tu contraseña. El enlace vence en 30 minutos:\n  {}\n" +
                "========================================================", correo, enlace);
    }
}
