package edu.uptc.swii.sihope.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uptc.swii.sihope.dto.UserSession;

public record LoginResponse(String token,
                            @JsonProperty("usuario") UserSession user) {

    public UserSession usuario() {
        return user;
    }
}
