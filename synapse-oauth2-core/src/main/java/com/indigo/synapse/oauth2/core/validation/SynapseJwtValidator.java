package com.indigo.synapse.oauth2.core.validation;

/**
 * 协议无关 JWT claim 校验器。
 */
@FunctionalInterface
public interface SynapseJwtValidator {

    JwtValidationResult validate(JwtClaimAccessor claims);
}
