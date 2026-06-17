package com.indigo.synapse.oauth2.core.validation;

import java.util.List;

/**
 * JWT 校验器组合工厂。
 */
public final class SynapseJwtValidatorFactory {

    private SynapseJwtValidatorFactory() {
    }

    public static SynapseJwtValidator composite(List<SynapseJwtValidator> validators) {
        List<SynapseJwtValidator> safeValidators = validators == null ? List.of() : List.copyOf(validators);
        return claims -> {
            for (SynapseJwtValidator validator : safeValidators) {
                JwtValidationResult result = validator.validate(claims);
                if (!result.success()) {
                    return result;
                }
            }
            return JwtValidationResult.ok();
        };
    }
}
