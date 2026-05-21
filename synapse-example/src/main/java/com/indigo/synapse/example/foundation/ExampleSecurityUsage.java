package com.indigo.synapse.example.foundation;

public record ExampleSecurityUsage(
        String subject,
        String tokenType,
        String keyId,
        boolean tokenExpired,
        boolean oauth2EndpointPublic,
        boolean permissionMatched
) {
}
