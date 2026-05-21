package com.indigo.synapse.iam.interfaces.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class CreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String displayName;

    @NotBlank
    private String password;

    private List<String> roleCodes;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}
