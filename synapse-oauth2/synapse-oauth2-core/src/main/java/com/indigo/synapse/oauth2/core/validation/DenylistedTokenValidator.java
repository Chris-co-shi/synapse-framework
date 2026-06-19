package com.indigo.synapse.oauth2.core.validation;

import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.token.TokenDenylistPort;

/**
 * token denylist 校验器。
 */
public final class DenylistedTokenValidator implements SynapseJwtValidator {

    private final TokenDenylistPort denylistPort;

    public DenylistedTokenValidator(TokenDenylistPort denylistPort) {
        if (denylistPort == null) {
            throw new IllegalArgumentException("denylistPort must not be null");
        }
        this.denylistPort = denylistPort;
    }

    @Override
    public JwtValidationResult validate(JwtClaimAccessor claims) {
        String tokenId = claims.string(SynapseJwtClaimNames.TOKEN_ID).orElse(null);
        if (tokenId == null || tokenId.isBlank()) {
            return JwtValidationResult.ok();
        }
        return denylistPort.isDenied(tokenId)
                ? JwtValidationResult.failure(OAuth2ErrorCode.OAUTH2_DENYLISTED_TOKEN, "token is denylisted")
                : JwtValidationResult.ok();
    }
}
