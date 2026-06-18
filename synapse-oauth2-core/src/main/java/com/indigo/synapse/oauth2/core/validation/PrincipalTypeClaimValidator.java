package com.indigo.synapse.oauth2.core.validation;

import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapsePrincipalType;
import java.util.Set;

/**
 * principal_type claim 校验器。
 */
public final class PrincipalTypeClaimValidator implements SynapseJwtValidator {

    private static final Set<String> ACCEPTED_TYPES = Set.of(
            SynapsePrincipalType.USER.name(),
            SynapsePrincipalType.CLIENT.name()
    );

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
