package com.indigo.synapse.oauth2.core.validation;

import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapseTokenType;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * token_type claim 校验器。
 */
public final class TokenTypeValidator implements SynapseJwtValidator {

    private final Set<String> acceptedTypes;

    public TokenTypeValidator(Collection<SynapseTokenType> acceptedTypes) {
        this.acceptedTypes = acceptedTypes == null ? Set.of() : acceptedTypes.stream()
                .map(Enum::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public JwtValidationResult validate(JwtClaimAccessor claims) {
        String tokenType = claims.string(SynapseJwtClaimNames.TOKEN_TYPE).orElse(null);
        return tokenType != null && acceptedTypes.contains(tokenType)
                ? JwtValidationResult.ok()
                : JwtValidationResult.failure(OAuth2ErrorCode.OAUTH2_INVALID_TOKEN_TYPE, "token_type is not accepted");
    }
}
