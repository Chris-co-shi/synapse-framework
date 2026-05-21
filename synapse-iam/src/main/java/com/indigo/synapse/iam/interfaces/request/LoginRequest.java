package com.indigo.synapse.iam.interfaces.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank
    private String clientId;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
