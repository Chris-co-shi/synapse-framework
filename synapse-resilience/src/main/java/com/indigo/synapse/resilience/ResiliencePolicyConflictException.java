package com.indigo.synapse.resilience;

/**
 * 同一稳定策略名被重复绑定到不同配置时抛出。
 *
 * <p>Resilience4j 实例需要按名称保留熔断和隔离状态，因此不能静默接受同名不同配置。</p>
 */
public final class ResiliencePolicyConflictException extends IllegalStateException {

    public ResiliencePolicyConflictException(String policyName) {
        super("resilience policy configuration changed for existing name: " + policyName);
    }
}
