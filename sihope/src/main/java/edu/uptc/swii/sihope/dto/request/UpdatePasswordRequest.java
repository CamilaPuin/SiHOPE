package edu.uptc.swii.sihope.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdatePasswordRequest {

    @JsonProperty("actual")
    private String currentPassword;

    @JsonProperty("nueva")
    private String newPassword;

    @JsonProperty("nueva2")
    private String newPassword2;

    public UpdatePasswordRequest() {
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword2() {
        return newPassword2;
    }

    public void setNewPassword2(String newPassword2) {
        this.newPassword2 = newPassword2;
    }

}
