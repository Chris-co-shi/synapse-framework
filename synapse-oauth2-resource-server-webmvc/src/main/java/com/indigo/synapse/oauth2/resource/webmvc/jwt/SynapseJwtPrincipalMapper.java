package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 将 JWT claims 映射为 Synapse 已认证主体。
 */
public final class SynapseJwtPrincipalMapper {

    public AuthenticatedPrincipal map(Jwt jwt) {
        String principalType = required(jwt, SynapseJwtClaimNames.PRINCIPAL_TYPE);
        String tenantId = jwt.getClaimAsString(SynapseJwtClaimNames.TENANT_ID);
        Set<String> roles = strings(jwt, SynapseJwtClaimNames.ROLES);
        Set<String> permissions = strings(jwt, SynapseJwtClaimNames.PERMISSIONS);
        if ("CLIENT".equals(principalType)) {
            String clientId = required(jwt, SynapseJwtClaimNames.CLIENT_ID);
            return new AuthenticatedClient(clientId, clientId, tenantId, roles, permissions);
        }
        if ("USER".equals(principalType)) {
            String userId = required(jwt, SynapseJwtClaimNames.SUBJECT);
            String username = jwt.getClaimAsString(SynapseJwtClaimNames.PREFERRED_USERNAME);
            return new AuthenticatedUser(
                    userId,
                    username == null || username.isBlank() ? userId : username,
                    tenantId,
                    roles,
                    permissions
            );
        }
        throw new IllegalArgumentException("unsupported principal_type: " + principalType);
    }

    private static String required(Jwt jwt, String claim) {
        String value = jwt.getClaimAsString(claim);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(claim + " must not be blank");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static Set<String> strings(Jwt jwt, String claim) {
        Object value = jwt.getClaims().get(claim);
        if (value instanceof String string) {
            Set<String> values = new LinkedHashSet<>();
            for (String part : string.split(" ")) {
                if (!part.isBlank()) {
                    values.add(part.trim());
                }
            }
            return values;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(part -> !part.isBlank())
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }
        return Set.of();
    }
}
