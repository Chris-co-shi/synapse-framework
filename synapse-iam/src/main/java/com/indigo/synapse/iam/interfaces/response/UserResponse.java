package com.indigo.synapse.iam.interfaces.response;

import com.indigo.synapse.iam.application.result.UserResult;

import java.util.List;

public record UserResponse(
        String userId,
        String username,
        String displayName,
        List<String> roleCodes
) {

    public static UserResponse from(UserResult result) {
        return new UserResponse(result.userId(), result.username(), result.displayName(), result.roleCodes());
    }
}
