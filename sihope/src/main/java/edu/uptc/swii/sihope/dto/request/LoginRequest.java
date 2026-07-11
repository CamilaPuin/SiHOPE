package edu.uptc.swii.sihope.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {

    @JsonProperty("email")
    @JsonAlias({"correo", "email"})
    private String email;

    @JsonProperty("password")
    @JsonAlias({"password", "contrasena"})
    private String password;

    public LoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
