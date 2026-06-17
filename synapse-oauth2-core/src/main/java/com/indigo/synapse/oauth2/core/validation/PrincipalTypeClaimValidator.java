package com.indigo.synapse.oauth2.core.validation;

import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;

import java.util.Set;

/**
 * principal_type claim 校验器。
 */
public final class PrincipalTypeClaimValidator implements SynapseJwtValidator {

    public static final String USER = "USER";
    public static final String CLIENT = "CLIENT";
    private static final Set<String> ACCEPTED_TYPES = Set.of(USER, CLIENT);

    @Override
    public JwtValidationResult validate(JwtClaimAccessor claims) {
        String principalType = claims.string(SynapseJwtClaimNames.PRINCIPAL_TYPE).orElse(null);
        return ACCEPTED_TYPES.contains(principalType)
                ? JwtValidationResult.ok()
                : JwtValidationResult.failure(
                        OAuth2ErrorCode.OAUTH2_INVALID_PRINCIPAL_TYPE,
                        "principal_type must be USER or CLIENT"
                );
    }
}
