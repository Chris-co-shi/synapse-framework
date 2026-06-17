package com.indigo.synapse.oauth2.core.validation;

import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;

/**
 * USER / CLIENT 主体 claim 组合校验器。
 */
public final class PrincipalClaimsValidator implements SynapseJwtValidator {

    @Override
    public JwtValidationResult validate(JwtClaimAccessor claims) {
        String principalType = claims.string(SynapseJwtClaimNames.PRINCIPAL_TYPE).orElse(null);
        String subject = claims.string(SynapseJwtClaimNames.SUBJECT).orElse(null);
        if (subject == null || subject.isBlank()) {
            return JwtValidationResult.failure(OAuth2ErrorCode.OAUTH2_MISSING_REQUIRED_CLAIM, "sub is required");
        }
        if (PrincipalTypeClaimValidator.CLIENT.equals(principalType)
                && claims.string(SynapseJwtClaimNames.CLIENT_ID).isEmpty()) {
            return JwtValidationResult.failure(OAuth2ErrorCode.OAUTH2_MISSING_REQUIRED_CLAIM, "client_id is required");
        }
        return JwtValidationResult.ok();
    }
}
