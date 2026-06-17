package com.indigo.synapse.oauth2.core.validation;

import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * audience 校验器。
 */
public final class AudienceValidator implements SynapseJwtValidator {

    private final Set<String> acceptedAudiences;

    public AudienceValidator(Collection<String> acceptedAudiences) {
        this.acceptedAudiences = acceptedAudiences == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(acceptedAudiences));
    }

    @Override
    public JwtValidationResult validate(JwtClaimAccessor claims) {
        Set<String> tokenAudiences = new LinkedHashSet<>(claims.strings(SynapseJwtClaimNames.AUDIENCE));
        boolean matched = tokenAudiences.stream().anyMatch(acceptedAudiences::contains);
        return matched
                ? JwtValidationResult.ok()
                : JwtValidationResult.failure(OAuth2ErrorCode.OAUTH2_INVALID_AUDIENCE, "audience is not accepted");
    }
}
