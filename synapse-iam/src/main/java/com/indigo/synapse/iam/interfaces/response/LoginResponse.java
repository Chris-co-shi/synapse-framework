package com.indigo.synapse.iam.interfaces.response;

import com.indigo.synapse.iam.application.result.LoginResult;

import java.time.Instant;
import java.util.Set;

public record LoginResponse(
        String accessToken,
        Instant expiresAt,
        String userId,
        String username,
        String displayName,
        Set<String> roles,
        Set<String> permissions
) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.expiresAt(),
                result.userId(),
                result.username(),
                result.displayName(),
                result.permissionSummary().roles(),
                result.permissionSummary().permissions()
        );
    }
}
