package com.indigo.synapse.oauth2.resource.core;

import com.indigo.synapse.oauth2.core.token.NoopTokenDenylistPort;
import com.indigo.synapse.oauth2.core.token.TokenDenylistPort;
import com.indigo.synapse.oauth2.core.validation.AudienceValidator;
import com.indigo.synapse.oauth2.core.validation.DenylistedTokenValidator;
import com.indigo.synapse.oauth2.core.validation.PrincipalClaimsValidator;
import com.indigo.synapse.oauth2.core.validation.PrincipalTypeClaimValidator;
import com.indigo.synapse.oauth2.core.validation.RequiredClaimsValidator;
import com.indigo.synapse.oauth2.core.validation.SynapseJwtValidator;
import com.indigo.synapse.oauth2.core.validation.SynapseJwtValidatorFactory;
import com.indigo.synapse.oauth2.core.validation.TokenTypeValidator;

import java.util.ArrayList;
import java.util.List;

/** 创建 MVC 与 WebFlux 共用的协议级 JWT validator。 */
public final class ResourceServerValidatorFactory {

    private ResourceServerValidatorFactory() {
    }

    /**
     * 按固定顺序创建 validator，首个失败结果会终止后续验证。
     *
     * @param policy 验证策略
     * @param denylistPort denylist 端口；启用 denylist 时必须是真实实现
     * @return 组合后的 validator
     * @throws IllegalStateException 策略无效或启用 denylist 但未提供真实端口
     */
    public static SynapseJwtValidator create(
            ResourceServerValidationPolicy policy,
            TokenDenylistPort denylistPort) {
        policy.validate();
        List<SynapseJwtValidator> validators = new ArrayList<>();
        validators.add(new RequiredClaimsValidator(policy.requiredClaims()));
        if (policy.audienceValidationEnabled()) {
            validators.add(new AudienceValidator(policy.audiences()));
        }
        validators.add(new TokenTypeValidator(policy.acceptedTokenTypes()));
        validators.add(new PrincipalTypeClaimValidator());
        validators.add(new PrincipalClaimsValidator());
        if (policy.denylistEnabled()) {
            if (denylistPort == null || denylistPort instanceof NoopTokenDenylistPort) {
                throw new IllegalStateException("real TokenDenylistPort is required when denylist is enabled");
            }
            validators.add(new DenylistedTokenValidator(denylistPort));
        }
        return SynapseJwtValidatorFactory.composite(validators);
    }
}
