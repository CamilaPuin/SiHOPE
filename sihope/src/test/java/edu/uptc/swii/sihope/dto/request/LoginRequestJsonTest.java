package edu.uptc.swii.sihope.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uptc.swii.sihope.dto.UserSession;

class LoginRequestJsonTest {

    @Test
    void deserializesEmailAndPasswordFromJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        LoginRequest request = mapper.readValue(
                "{\"correo\":\"admin@uptc.edu.co\",\"password\":\"Admin2026*\"}",
                LoginRequest.class);

        assertEquals("admin@uptc.edu.co", request.getEmail());
        assertEquals("Admin2026*", request.getPassword());
    }

    @Test
    void serializesUserSessionWithoutAmbiguousPropertyNames() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UserSession session = new UserSession("Ana Torres", "AT", "ana@uptc.edu.co", "ESTUDIANTE");

        String json = mapper.writeValueAsString(session);

        assertTrue(json.contains("\"nombre\":\"Ana Torres\""));
        assertTrue(json.contains("\"rol\":\"ESTUDIANTE\""));
    }
}
