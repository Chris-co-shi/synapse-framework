package com.indigo.synapse.oauth2.core.validation;

import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 必要 claim 校验器。
 */
public final class RequiredClaimsValidator implements SynapseJwtValidator {

    private final Set<String> requiredClaims;

    public RequiredClaimsValidator(Collection<String> requiredClaims) {
        this.requiredClaims = requiredClaims == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(requiredClaims));
    }

    @Override
    public JwtValidationResult validate(JwtClaimAccessor claims) {
        if (claims == null) {
            return JwtValidationResult.failure(OAuth2ErrorCode.OAUTH2_INVALID_TOKEN, "claims must not be null");
        }
        for (String requiredClaim : requiredClaims) {
            if (claims.string(requiredClaim).isEmpty() && claims.strings(requiredClaim).isEmpty()) {
                return JwtValidationResult.failure(
                        OAuth2ErrorCode.OAUTH2_MISSING_REQUIRED_CLAIM,
                        requiredClaim + " is required"
                );
            }
        }
        return JwtValidationResult.ok();
    }
}
