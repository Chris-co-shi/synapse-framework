package com.indigo.synapse.oauth2.resource.webflux.jwt;

import com.indigo.synapse.oauth2.core.validation.JwtValidationResult;
import com.indigo.synapse.oauth2.core.validation.SynapseJwtValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;

/** 将共享 Synapse JWT validator 适配为 Spring Security Reactive decoder 使用的 validator。 */
public final class SynapseReactiveJwtValidatorAdapter implements OAuth2TokenValidator<Jwt> {

    private final SynapseJwtValidator validator;

    public SynapseReactiveJwtValidatorAdapter(SynapseJwtValidator validator) {
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        JwtValidationResult result = validator.validate(new SpringJwtClaimAccessor(token));
        if (result.success()) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                result.errorCode().code(), result.description(), null));
    }
}
