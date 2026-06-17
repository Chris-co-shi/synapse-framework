package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.validation.JwtValidationResult;
import com.indigo.synapse.oauth2.core.validation.SynapseJwtValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * 将 Synapse 协议无关 validator 适配为 Spring OAuth2 validator。
 */
public final class SynapseSpringJwtValidatorAdapter implements OAuth2TokenValidator<Jwt> {

    private final SynapseJwtValidator validator;

    public SynapseSpringJwtValidatorAdapter(SynapseJwtValidator validator) {
        this.validator = validator;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        JwtValidationResult result = validator.validate(new SpringJwtClaimAccessor(token));
        if (result.success()) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                result.errorCode().code(),
                result.description(),
                null
        ));
    }
}
