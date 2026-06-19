package com.indigo.synapse.oauth2.resource.core;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapseTokenType;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * MVC 与 WebFlux 共享的 Resource Server 验证策略。
 *
 * <p>该值对象只描述协议语义，不包含密钥位置、Servlet 路径或 Reactor 配置。适配器应在创建
 * decoder 前调用 {@link #validate()}，确保两种技术栈以相同规则处理 issuer、audience、
 * required claims、token type 与 denylist。</p>
 *
 * @param issuerValidationEnabled 是否校验 issuer
 * @param issuerUri 期望的 issuer；启用 issuer 校验时不能为空
 * @param audienceValidationEnabled 是否校验 audience
 * @param audiences 当前服务接受的 audience
 * @param acceptedTokenTypes 当前服务接受的 token 类型
 * @param requiredClaims 必须存在的 claim 名称
 * @param clockSkew 时间校验允许的偏移
 * @param denylistEnabled 是否启用 denylist
 */
public record ResourceServerValidationPolicy(
        boolean issuerValidationEnabled,
        String issuerUri,
        boolean audienceValidationEnabled,
        List<String> audiences,
        List<SynapseTokenType> acceptedTokenTypes,
        List<String> requiredClaims,
        Duration clockSkew,
        boolean denylistEnabled) {

    /** 默认必填 claim。 */
    public static final List<String> DEFAULT_REQUIRED_CLAIMS = List.of(
            SynapseJwtClaimNames.SUBJECT,
            SynapseJwtClaimNames.EXPIRES_AT,
            SynapseJwtClaimNames.ISSUED_AT,
            SynapseJwtClaimNames.TOKEN_TYPE,
            SynapseJwtClaimNames.PRINCIPAL_TYPE
    );

    public ResourceServerValidationPolicy {
        audiences = audiences == null ? List.of() : List.copyOf(audiences);
        acceptedTokenTypes = acceptedTokenTypes == null ? List.of() : List.copyOf(acceptedTokenTypes);
        requiredClaims = requiredClaims == null ? List.of() : List.copyOf(requiredClaims);
        clockSkew = clockSkew == null ? Duration.ZERO : clockSkew;
    }

    /**
     * 校验共享策略。
     *
     * @throws IllegalStateException 配置无法形成确定验证语义时
     */
    public void validate() {
        if (issuerValidationEnabled && (issuerUri == null || issuerUri.isBlank())) {
            throw new IllegalStateException("issuer-uri must be configured when issuer validation is enabled");
        }
        if (audienceValidationEnabled && audiences.isEmpty()) {
            throw new IllegalStateException("audiences must not be empty when audience validation is enabled");
        }
        if (acceptedTokenTypes.isEmpty() || acceptedTokenTypes.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("accepted-token-types must not be empty");
        }
        if (requiredClaims.stream().anyMatch(claim -> claim == null || claim.isBlank())) {
            throw new IllegalStateException("required-claims must not contain blank values");
        }
        if (clockSkew.isNegative()) {
            throw new IllegalStateException("clock-skew must not be negative");
        }
    }
}
